/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.army.agentscope.state;

import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.state.ListHashUtil;
import io.agentscope.core.state.State;
import io.agentscope.core.state.VersionedState;
import io.agentscope.core.util.JsonCodec;
import io.agentscope.core.util.JsonUtils;
import io.army.criteria.*;
import io.army.criteria.impl.MySQLs;
import io.army.criteria.impl.Postgres;
import io.army.criteria.impl.SQLs;
import io.army.executor.DriverException;
import io.army.mapping.MappingType;
import io.army.meta.FieldMeta;
import io.army.meta.PrimaryFieldMeta;
import io.army.meta.SimpleTableMeta;
import io.army.meta.TableMeta;
import io.army.pojo.ObjectAccessorFactory;
import io.army.result.CurrentRecord;
import io.army.result.OptimisticLockException;
import io.army.session.SyncLocalSession;
import io.army.session.SyncSession;
import io.army.session.SyncSessionFactory;
import io.army.transaction.TransactionOption;
import io.army.util.ValidateUtils;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.army.criteria.impl.SQLs.AS;
import static io.army.criteria.impl.SQLs.CONST_1;

/// Army Criteria API implementation of {@link io.agentscope.core.state.AgentStateStore}.
///
/// Subclass {@link AgentScopeSessions} with @Table and a unique index over
/// (userId, sessionId, stateKey, itemIndex), then pass the generated SimpleTableMeta to
/// {@link #builder(SimpleTableMeta)}.
///
/// supportsVersioning() returns true. save is a dialect upsert (PostgreSQL ON CONFLICT DO UPDATE,
/// MySQL ON DUPLICATE KEY UPDATE). saveIfVersion is a CAS write on the Army reserved version
/// field: expectedVersion 0 means create-if-absent, UNVERSIONED means unconditional overwrite,
/// any other value requires an exact version match.
///
/// Only PostgreSQL and MySQL are supported.
public final class ArmyAgentStateStore<D extends AgentScopeSessions> implements AgentStateStore {

    /// Suffix for hash storage keys.
    private static final String HASH_KEY_SUFFIX = ":_hash";

    /// item_index value for single state values.
    private static final int SINGLE_STATE_INDEX = 0;


    private final SyncSessionFactory sessionFactory;

    private final SimpleTableMeta<D> tableMeta;

    private final PrimaryFieldMeta<D> id;

    private final FieldMeta<D> version;

    private final FieldMeta<D> userId;

    private final FieldMeta<D> sessionId;

    private final FieldMeta<D> stateKey;

    private final FieldMeta<D> itemIndex;

    private final FieldMeta<D> stateValue;

    private final Supplier<String> anonymousUserIdSupplier;

    /// The null handling mode.
    private final NullMode nullMode;

    /// The literal handling mode.
    private final LiteralMode literalMode;


    private final Supplier<D> constructor;


    private ArmyAgentStateStore(Builder<D> builder) {
        this.sessionFactory = builder.sessionFactory;
        this.tableMeta = builder.tableMeta;

        this.id = this.tableMeta.id();
        this.version = this.tableMeta.version();
        this.userId = this.tableMeta.field("userId");
        this.sessionId = this.tableMeta.field("sessionId");

        this.itemIndex = this.tableMeta.field("itemIndex");
        this.stateKey = this.tableMeta.field("stateKey");
        this.stateValue = this.tableMeta.field("stateValue");

        this.anonymousUserIdSupplier = builder.anonymousUserIdSupplier;
        this.nullMode = builder.nullMode;
        this.literalMode = builder.literalMode;

        this.constructor = ObjectAccessorFactory.pojoConstructor(this.tableMeta.javaType());

    }

    @Override
    public boolean supportsVersioning() {
        return true;
    }


    @Override
    public void save(String userId, String sessionId, String key, State value) {
        saveSingleItem(userId, sessionId, key, value, false);
    }


    @Override
    public long saveIfVersion(String userId, String sessionId, String key, State value, final long expectedVersion) {
        if (expectedVersion == UNVERSIONED) {
            return saveSingleItem(userId, sessionId, key, value, true);
        }

        final String finalUserId;
        finalUserId = validateIdStrings(userId, sessionId, key, true);

        final String sessionName = getClass().getName() + '.' + "saveIfVersion";
        try (SyncLocalSession session = this.sessionFactory.localSession(sessionName, false)) {

            final boolean needTransaction = expectedVersion == 0L;

            if (needTransaction) {
                session.startTransaction(TransactionOption.readCommitted());
            }

            try {
                final long newVersion;
                if (expectedVersion != 0L) {
                    newVersion = updateItem(session, finalUserId, sessionId, key, value, expectedVersion);
                } else if (insertIfAbsent(session, finalUserId, sessionId, key, value) == UNVERSIONED) {
                    newVersion = updateItem(session, finalUserId, sessionId, key, value, 0L);
                } else {
                    newVersion = queryVersion(session, finalUserId, sessionId, key);
                }

                if (needTransaction) {
                    session.commit();
                }
                return newVersion;
            } catch (Throwable e) {
                if (needTransaction) {
                    session.rollback();
                }
                throw e;
            }
        }

    }

    @Override
    public void save(String userId, String sessionId, String key, List<? extends State> values) {
        final String finalUserId;
        finalUserId = validateIdStrings(userId, sessionId, key, true);

        if (values.isEmpty()) {
            return;
        }

        final String sessionName = getClass().getName() + '.' + "saveList";
        try (SyncLocalSession session = this.sessionFactory.localSession(sessionName, false)) {

            session.startTransaction(TransactionOption.readCommitted());

            try {

                final String hashKey, currentHash, storedHash;
                hashKey = key + HASH_KEY_SUFFIX;
                currentHash = ListHashUtil.computeHash(values);
                storedHash = queryStateValue(session, finalUserId, sessionId, hashKey, SINGLE_STATE_INDEX);

                final int existingCount;
                existingCount = queryListCount(session, finalUserId, sessionId, key);

                final boolean needsFullRewrite;
                needsFullRewrite = ListHashUtil.needsFullRewrite(values, storedHash, existingCount);

                final D domainForHash;
                domainForHash = createStateDomain(finalUserId, sessionId, key, currentHash, SINGLE_STATE_INDEX);

                final List<D> domainList;
                if (needsFullRewrite) {
                    deleteListItems(session, finalUserId, sessionId, key);

                    domainList = createStateDomainList(finalUserId, sessionId, key, values, 0);
                    saveStateList(session, domainList);

                    saveStateList(session, List.of(domainForHash));
                } else if (values.size() > existingCount) {
                    final List<? extends State> newItems = values.subList(existingCount, values.size());

                    domainList = createStateDomainList(finalUserId, sessionId, key, newItems, existingCount);
                    saveStateList(session, domainList);

                    saveStateList(session, List.of(domainForHash));
                }

                session.commit();

            } catch (Throwable e) {
                session.rollback();
                throw e;
            }

        }


    }

    @Override
    public <T extends State> Optional<T> get(String userId, String sessionId, String key, Class<T> type) {
        final String finalUserId;
        finalUserId = validateIdStrings(userId, sessionId, key, true);

        final String sessionName = getClass().getName() + '.' + "get";

        try (SyncSession session = this.sessionFactory.localSession(sessionName, true)) {
            final String stateValue;
            stateValue = queryStateValue(session, finalUserId, sessionId, key, SINGLE_STATE_INDEX);

            final Optional<T> optional;
            if (stateValue == null) {
                optional = Optional.empty();
            } else {
                optional = Optional.of(JsonUtils.getJsonCodec().fromJson(stateValue, type));
            }
            return optional;
        }


    }

    @Override
    public <T extends State> VersionedState<T> getVersioned(String userId, String sessionId, String key, Class<T> type) {
        final String finalUserId;
        finalUserId = validateIdStrings(userId, sessionId, key, true);

        final String sessionName = getClass().getName() + '.' + "getVersioned";

        try (SyncSession session = this.sessionFactory.localSession(sessionName, true)) {
            final Select stmt;
            stmt = queryStateValueStmt(finalUserId, sessionId, key, SINGLE_STATE_INDEX, true);

            final Function<CurrentRecord, VersionedState<T>> mapFunc;
            mapFunc = record -> {
                final T value;
                value = JsonUtils.getJsonCodec().fromJson(record.getNonNull(0, String.class), type);
                return new VersionedState<>(value, record.getNonNull(1, Long.class));
            };

            VersionedState<T> versionedState;
            versionedState = session.queryOneRecord(stmt, mapFunc);
            if (versionedState == null) {
                versionedState = new VersionedState<>(null, 0L);
            }
            return versionedState;
        }


    }


    @Override
    public <T extends State> List<T> getList(String userId, String sessionId, String key, Class<T> itemType) {
        final String finalUserId;
        finalUserId = validateIdStrings(userId, sessionId, key, true);

        final String sessionName = getClass().getName() + '.' + "getList";
        try (SyncSession session = this.sessionFactory.localSession(sessionName, true)) {
            final Select stmt;
            stmt = queryStateValueStmt(finalUserId, sessionId, key, null, false);

            final Function<CurrentRecord, T> mapFunc;
            mapFunc = record -> JsonUtils.getJsonCodec().fromJson(record.getNonNull(0, String.class), itemType);

            return session.queryRecordList(stmt, mapFunc);
        }
    }


    @Override
    public boolean exists(String userId, String sessionId) {
        userId = validateIdStrings(userId, sessionId, null, false);

        final String sessionName = getClass().getName() + '.' + "exists";
        try (SyncSession session = this.sessionFactory.localSession(sessionName, true)) {
            final Select stmt;
            stmt = SQLs.query()
                    .select(this.id)
                    .from(this.tableMeta, AS, "t")
                    .where(this.userId.equal(userId))
                    .and(this.sessionId.equal(sessionId))
                    .limit(CONST_1)
                    .asQuery();

            return session.queryOne(stmt, Object.class) != null;
        }
    }

    @Override
    public void delete(String userId, String sessionId) {
        userId = validateIdStrings(userId, sessionId, null, false);

        doDelete("delete", userId, sessionId, null);
    }

    @Override
    public void delete(String userId, String sessionId, String key) {
        userId = validateIdStrings(userId, sessionId, key, true);

        doDelete("deleteWithKey", userId, sessionId, key);
    }


    @Override
    public Set<String> listSessionIds(@Nullable String userId) {
        if (userId == null) {
            userId = this.anonymousUserIdSupplier.get();
        }
        validateIdString(this.userId, userId);

        final String sessionName = getClass().getName() + '.' + "listSessionIds";
        try (SyncSession session = this.sessionFactory.localSession(sessionName, true)) {
            final Select stmt;
            stmt = SQLs.query()
                    .select(this.sessionId)
                    .from(this.tableMeta, AS, "t")
                    .where(this.userId.equal(userId))
                    .groupBy(this.sessionId)
                    .asQuery();

            final List<String> sessionList;
            sessionList = session.queryList(stmt, String.class);

            return new HashSet<>(sessionList);
        }
    }


    private long saveSingleItem(String userId, String sessionId, String key, State value, final boolean needVersion) {
        final String finalUserId;
        finalUserId = validateIdStrings(userId, sessionId, key, true);

        final D o = createStateDomain(finalUserId, sessionId, key,
                JsonUtils.getJsonCodec().toJson(value), SINGLE_STATE_INDEX);

        String sessionName = getClass().getName() + '.';
        if (needVersion) {
            sessionName += "saveIfVersion";
        } else {
            sessionName += "save";
        }

        try (SyncLocalSession session = this.sessionFactory.localSession(sessionName, false)) {

            if (needVersion) {
                session.startTransaction(TransactionOption.readCommitted());
            }

            try {
                saveStateList(session, List.of(o));
                final long version;
                if (needVersion) {
                    version = queryVersion(session, finalUserId, sessionId, key);
                } else {
                    version = 0L;
                }

                if (needVersion) {
                    session.commit();
                }
                return version;
            } catch (Throwable e) {
                if (needVersion) {
                    session.rollback();
                }
                throw e;
            }
        }

    }


    private long queryVersion(SyncSession session, String userId, String sessionId, String key) {
        final Select stmt;
        stmt = SQLs.query()
                .select(this.version)
                .from(this.tableMeta, AS, "t")
                .where(this.userId.equal(userId))
                .and(this.sessionId.equal(sessionId))
                .and(this.stateKey.equal(key))
                .and(this.itemIndex.equal(SINGLE_STATE_INDEX))
                .asQuery();

        final Long version;
        version = session.queryOne(stmt, Long.class);
        return version == null ? 0L : version;
    }

    private void saveStateList(SyncSession session, List<D> list) {
        switch (session.dialectDatabase()) {
            case PostgreSQL:
                session.update(postgreInsertOrUpdateStmt(list));
                break;
            case MySQL:
                session.update(mysqlInsertOrUpdateStmt(list));
                break;
            default:
                throw new IllegalStateException(String.format("%s is supported", session.dialectDatabase()));
        }
    }


    private long insertIfAbsent(SyncSession session, String userId, String sessionId, String key, State value) {
        final D o = createStateDomain(userId, sessionId, key,
                JsonUtils.getJsonCodec().toJson(value), SINGLE_STATE_INDEX);

        final List<D> list = List.of(o);
        final Insert stmt;
        switch (session.dialectDatabase()) {
            case PostgreSQL:
                stmt = postgreInsertOrDoNothing(list);
                break;
            case MySQL:
                stmt = SQLs.singleInsert()
                        .nullMode(this.nullMode)
                        .literalMode(this.literalMode)
                        .insertInto(this.tableMeta)
                        .values(list)
                        .asInsert();
                break;
            default:
                throw new IllegalStateException(String.format("%s is supported", session.dialectDatabase()));
        }

        long currentVersion;
        try {
            final long rowAffected;
            rowAffected = session.update(stmt);
            if (rowAffected == 0L) {
                currentVersion = UNVERSIONED;
            } else {
                currentVersion = 0L;
            }
        } catch (DriverException e) {
            if (Objects.equals(e.getSqlState(), "23000")) {
                currentVersion = UNVERSIONED;
            } else {
                throw e;
            }
        }

        return currentVersion;
    }

    /// @return the new version after update.
    private long updateItem(SyncSession session, String userId, String sessionId, String key, State value, long expectedVersion) {
        final Update stmt;
        stmt = SQLs.singleUpdate()
                .update(this.tableMeta, AS, "t")
                .set(this.stateValue, JsonUtils.getJsonCodec().toJson(value))
                .where(this.userId.equal(userId))
                .and(this.sessionId.equal(sessionId))
                .and(this.stateKey.equal(key))
                .and(this.itemIndex.equal(SINGLE_STATE_INDEX))
                .and(this.version.equal(expectedVersion))
                .asUpdate();

        try {
            final long rowAffected;
            rowAffected = session.update(stmt);
            if (rowAffected < 1L) {
                expectedVersion = UNVERSIONED;
            } else {
                expectedVersion++;
            }
        } catch (OptimisticLockException e) {
            expectedVersion = UNVERSIONED;
        }
        return expectedVersion;
    }


    private Insert postgreInsertOrDoNothing(final List<D> list) {
        return Postgres.singleInsert()
                .nullMode(this.nullMode)
                .literalMode(this.literalMode)
                .insertInto(this.tableMeta).as("t")
                .values(list)
                .onConflict().parens(s -> s.space(this.userId)
                        .comma(this.sessionId)
                        .comma(this.stateKey)
                        .comma(this.itemIndex)
                )
                .doNothing()
                .asInsert();
    }


    private Insert postgreInsertOrUpdateStmt(final List<D> list) {
        return Postgres.singleInsert()
                .nullMode(this.nullMode)
                .literalMode(this.literalMode)
                .insertInto(this.tableMeta).as("t")
                .values(list)
                .onConflict().parens(s -> s.space(this.userId)
                        .comma(this.sessionId)
                        .comma(this.stateKey)
                        .comma(this.itemIndex)
                )
                .doUpdate()
                .set(this.stateValue, Postgres.excluded(this.stateValue))
                .asInsert();
    }

    private Insert mysqlInsertOrUpdateStmt(final List<D> list) {
        final String rowAlias = "r";
        return MySQLs.singleInsert()
                .nullMode(this.nullMode)
                .literalMode(this.literalMode)
                .insertInto(this.tableMeta)
                .values(list)
                .as(rowAlias)
                .onDuplicateKey()
                .update(this.stateValue, SQLs.field(rowAlias, this.stateValue))
                .asInsert();
    }


    @Nullable
    private String queryStateValue(SyncSession session, String userId, String sessionId, String stateKey, int itemIndex) {
        final Select stmt;
        stmt = queryStateValueStmt(userId, sessionId, stateKey, itemIndex, false);
        return session.queryOne(stmt, String.class);
    }


    private Select queryStateValueStmt(String userId, String sessionId, String stateKey, @Nullable Integer itemIndex, boolean needVersion) {
        return SQLs.query()
                .selects(s -> {
                    s.selection(this.stateValue);
                    if (needVersion) {
                        s.selection(this.version);
                    }
                })
                .from(this.tableMeta, AS, "t")
                .where(this.userId.equal(userId))
                .and(this.sessionId.equal(sessionId))
                .and(this.stateKey.equal(stateKey))
                .ifAnd(this.itemIndex::equal, itemIndex)
                .ifOrderBy(oc -> {
                    if (itemIndex == null) {
                        oc.accept(this.itemIndex);
                    }
                })
                .asQuery();
    }

    private int queryListCount(SyncSession session, String userId, String sessionId, String stateKey) {
        final Select stmt;
        stmt = SQLs.query()
                .select(SQLs.max(this.itemIndex).as("maxIndex"))
                .from(this.tableMeta, AS, "t")
                .where(this.userId.equal(userId))
                .and(this.sessionId.equal(sessionId))
                .and(this.stateKey.equal(stateKey))
                .asQuery();

        final Integer maxIndex;
        maxIndex = session.queryOne(stmt, Integer.class);
        return maxIndex == null ? 0 : maxIndex + 1;
    }


    private void doDelete(String methodName, String userId, String sessionId, @Nullable String stateKey) {
        final String sessionName = getClass().getName() + '.' + methodName;

        try (SyncSession session = this.sessionFactory.localSession(sessionName, false)) {
            final Delete stmt;
            stmt = SQLs.singleDelete()
                    .deleteFrom(this.tableMeta, AS, "t")
                    .where(this.userId.equal(userId))
                    .and(this.sessionId.equal(sessionId))
                    .ifAnd(this.stateKey::equal, stateKey)
                    .asDelete();

            session.update(stmt);
        }

    }

    private void deleteListItems(SyncSession session, String userId, String sessionId, String stateKey) {
        final Delete stmt;
        stmt = SQLs.singleDelete()
                .deleteFrom(this.tableMeta, AS, "t")
                .where(this.userId.equal(userId))
                .and(this.sessionId.equal(sessionId))
                .and(this.stateKey.equal(stateKey))
                .asDelete();

        session.update(stmt);
    }


    private List<D> createStateDomainList(String userId, String sessionId, String key, List<? extends State> values,
                                          int startItemIndex) {
        int itemIndex = startItemIndex;
        final List<D> list = new ArrayList<>(values.size());
        final JsonCodec jsonCodec = JsonUtils.getJsonCodec();
        for (State value : values) {
            list.add(createStateDomain(userId, sessionId, key, jsonCodec.toJson(value), itemIndex++));
        }
        return list;
    }

    private D createStateDomain(String userId, String sessionId, String key, String value, int itemIndex) {
        final D o = this.constructor.get();

        o.setUserId(userId)
                .setSessionId(sessionId)
                .setStateKey(key)
                .setItemIndex(itemIndex)
                .setStateValue(value)
        ;
        return o;
    }


    private String validateIdStrings(@Nullable String userId, String sessionId, @Nullable String stateKey,
                                     boolean validateState) {
        if (userId == null) {
            userId = this.anonymousUserIdSupplier.get();
        }

        validateIdString(this.userId, userId);

        validateIdString(this.sessionId, sessionId);

        if (validateState) {
            validateIdString(this.stateKey, stateKey);
        }

        return userId;

    }

    private static void validateIdString(FieldMeta<?> field, String idStr) {
        final int precision;
        if (idStr == null || idStr.trim().isEmpty()) {
            String m = String.format("AgentStateStore %s cannot be null or empty", field.fieldName());
            throw new IllegalArgumentException(m);
        } else if (field.mappingType() instanceof MappingType.SqlString
                && (precision = field.precision()) > 0
                && idStr.length() > precision) {
            String m = String.format("AgentStateStore %s cannot exceed %s characters",
                    field.fieldName(), precision);
            throw new IllegalArgumentException(m);
        }

    }


    public static <T extends AgentScopeSessions> Builder<T> builder(SimpleTableMeta<T> tableMeta) {
        return new Builder<>(tableMeta);
    }


    public static final class Builder<T extends AgentScopeSessions> {

        private final SimpleTableMeta<T> tableMeta;

        private SyncSessionFactory sessionFactory;


        private Supplier<String> anonymousUserIdSupplier;

        /// The null handling mode.
        private NullMode nullMode = NullMode.DEFAULT;

        /// The literal handling mode.
        private LiteralMode literalMode = LiteralMode.DEFAULT;

        public Builder(SimpleTableMeta<T> tableMeta) {
            this.tableMeta = tableMeta;
        }

        public Builder<T> sessionFactory(SyncSessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
            return this;
        }


        public Builder<T> anonymousUserIdSupplier(Supplier<String> anonymousUserIdSupplier) {
            this.anonymousUserIdSupplier = anonymousUserIdSupplier;
            return this;
        }

        public Builder<T> nullMode(NullMode nullMode) {
            this.nullMode = nullMode;
            return this;
        }

        public Builder<T> literalMode(LiteralMode literalMode) {
            this.literalMode = literalMode;
            return this;
        }

        public ArmyAgentStateStore<T> build() {
            final TableMeta<T> tableMeta = this.tableMeta;

            if (this.sessionFactory == null) {
                throw missingConfig("sessionFactory");
            } else if (tableMeta == null) {
                throw missingConfig("tableMeta");
            } else if (this.anonymousUserIdSupplier == null) {
                throw missingConfig("anonymousUserIdSupplier");
            }

            ValidateUtils.assertExistsIndex(this.tableMeta, true, List.of("userId", "sessionId", "stateKey", "itemIndex"));


            if (this.nullMode == null) {
                this.nullMode = NullMode.DEFAULT;
            }

            if (this.literalMode == null) {
                this.literalMode = LiteralMode.DEFAULT;
            }

            return new ArmyAgentStateStore<>(this);
        }

        private static IllegalStateException missingConfig(String configName) {
            return new IllegalStateException(String.format("%s is required", configName));
        }


    } // Builder


}
