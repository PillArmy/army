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

package io.army.agentscope.store;

import com.fasterxml.jackson.core.type.TypeReference;
import io.agentscope.core.util.JsonUtils;
import io.agentscope.harness.agent.filesystem.remote.store.BaseStore;
import io.agentscope.harness.agent.filesystem.remote.store.StoreItem;
import io.army.criteria.*;
import io.army.criteria.impl.MySQLs;
import io.army.criteria.impl.Postgres;
import io.army.criteria.impl.SQLs;
import io.army.dialect.Database;
import io.army.executor.DriverException;
import io.army.meta.FieldMeta;
import io.army.meta.SimpleTableMeta;
import io.army.pojo.ObjectAccessorFactory;
import io.army.result.CurrentRecord;
import io.army.session.SyncLocalSession;
import io.army.session.SyncSession;
import io.army.session.SyncSessionFactory;
import io.army.util.ValidateUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.army.criteria.impl.Postgres.excluded;
import static io.army.criteria.impl.SQLs.AS;
import static io.army.criteria.impl.SQLs.field;

/// Army Criteria API implementation of {@link io.agentscope.harness.agent.filesystem.remote.store.BaseStore}.
///
/// Subclass {@link AgentScopeStore} with @Table and a unique index over (namespacePath, itemKey),
/// then pass the generated SimpleTableMeta to {@link #builder(SimpleTableMeta)}.
///
/// put is a dialect upsert (PostgreSQL ON CONFLICT DO UPDATE, MySQL ON DUPLICATE KEY UPDATE).
/// putIfVersion with expectedVersion 0 creates the row if absent; any positive value is a CAS
/// update with WHERE version = expectedVersion. The version exposed through StoreItem is the Army
/// reserved version field as-is.
///
/// Only PostgreSQL and MySQL are supported.
public final class ArmyBaseStore<D extends AgentScopeStore> implements BaseStore {

    private static final int MAX_NAMESPACE_PATH_LENGTH = 512;

    private static final int MAX_ITEM_KEY_LENGTH = 255;

    private static final char NAMESPACE_SEPARATOR = '\u001F';


    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };


    private final SyncSessionFactory sessionFactory;

    private final SimpleTableMeta<D> tableMeta;

    private final FieldMeta<D> namespacePath;

    private final FieldMeta<D> itemKey;

    private final FieldMeta<D> valueJson;

    private final FieldMeta<D> version;

    private final Supplier<D> constructor;


    /// The null handling mode.
    private final NullMode nullMode;

    /// The literal handling mode.
    private final LiteralMode literalMode;

    private ArmyBaseStore(final Builder<D> builder) {
        this.sessionFactory = builder.sessionFactory;
        this.tableMeta = builder.tableMeta;

        this.namespacePath = this.tableMeta.field("namespacePath");
        this.itemKey = this.tableMeta.field("itemKey");
        this.valueJson = this.tableMeta.field("valueJson");
        this.version = this.tableMeta.version();

        this.nullMode = builder.nullMode;
        this.literalMode = builder.literalMode;

        this.constructor = ObjectAccessorFactory.pojoConstructor(this.tableMeta.javaType());
    }

    @Override
    public StoreItem get(final List<String> namespace, final String key) {
        validateKey(key);

        final String namespacePath;
        namespacePath = encodeNamespace(namespace);

        final String sessionName = getClass().getName() + '.' + "get";
        try (SyncSession session = this.sessionFactory.localSession(sessionName, true)) {

            final Select stmt;
            stmt = SQLs.query()
                    .select(this.valueJson, this.version)
                    .from(this.tableMeta, AS, "t")
                    .where(this.namespacePath.equal(namespacePath))
                    .and(this.itemKey.equal(key))
                    .asQuery();

            final Function<CurrentRecord, StoreItem> mapFunc;
            mapFunc = r -> {
                final String valueJson;
                valueJson = r.getNonNull(0, String.class);
                final long version;
                version = r.getNonNull(1, Long.class);

                return new StoreItem(key, JsonUtils.getJsonCodec().fromJson(valueJson, MAP_TYPE), version);
            };
            return session.queryOneRecord(stmt, mapFunc);
        }


    }

    @Override
    public void put(final List<String> namespace, final String key, final Map<String, Object> value) {
        validateKey(key);
        Objects.requireNonNull(value);

        final String namespacePath;
        namespacePath = encodeNamespace(namespace);

        final String sessionName = getClass().getName() + '.' + "put";
        try (SyncSession session = this.sessionFactory.localSession(sessionName, false)) {

            final D domain;
            domain = createDomain(key, namespacePath, value);

            final Insert stmt;
            stmt = upsertStmt(this.sessionFactory.dialectDatabase(), domain);

            session.update(stmt);
        }

    }

    @Override
    public boolean putIfVersion(final List<String> namespace, final String key,
                                final Map<String, Object> value, final long expectedVersion) {

        if (expectedVersion < 0L) {
            throw new IllegalArgumentException("expectedVersion must be non-negative");
        }

        validateKey(key);
        final String namespacePath;
        namespacePath = encodeNamespace(namespace);

        final String sessionName = getClass().getName() + '.' + "putIfVersion";
        try (SyncLocalSession session = this.sessionFactory.localSession(sessionName, false)) {

            final boolean updated;
            if (expectedVersion > 0L) {
                updated = updateItem(session, namespacePath, key, value, expectedVersion) > 0L;
            } else {
                updated = insertIfAbsent(session, key, namespacePath, value) > 0L;
            }
            return updated;

        }


    }


    @Override
    public List<StoreItem> search(final List<String> namespace, final int limit, final int offset) {
        if (limit <= 0) {
            return List.of();
        }

        final String namespacePath;
        namespacePath = encodeNamespace(namespace);

        final String sessionName = getClass().getName() + '.' + "search";
        try (SyncSession session = this.sessionFactory.localSession(sessionName, true)) {

            final String pattern;
            pattern = likePrefixPattern(namespacePath);

            final int safeOffset = Math.max(offset, 0);

            final Select stmt;
            stmt = SQLs.query()
                    .select(this.itemKey, this.valueJson, this.version)
                    .from(this.tableMeta, AS, "t")
                    .where(this.namespacePath.like(pattern, SQLs.ESCAPE, '!'))
                    .orderBy(this.itemKey)
                    .limit(SQLs::constant, safeOffset, limit)
                    .asQuery();

            final Function<CurrentRecord, StoreItem> mapFunc;
            mapFunc = r -> {
                final String itemKey, valueJson;
                itemKey = r.getNonNull(0, String.class);
                valueJson = r.getNonNull(1, String.class);

                final long version;
                version = r.getNonNull(2, Long.class);
                return new StoreItem(itemKey, JsonUtils.getJsonCodec().fromJson(valueJson, MAP_TYPE), version);
            };
            return session.queryRecordList(stmt, mapFunc);
        }

    }

    @Override
    public void delete(final List<String> namespace, final String key) {
        validateKey(key);

        final String namespacePath;
        namespacePath = encodeNamespace(namespace);

        final String sessionName = getClass().getName() + '.' + "delete";
        try (SyncSession session = this.sessionFactory.localSession(sessionName, false)) {

            final Delete stmt;
            stmt = SQLs.singleDelete()
                    .deleteFrom(this.tableMeta, AS, "t")
                    .where(this.namespacePath.equal(namespacePath))
                    .and(this.itemKey.equal(key))
                    .asDelete();

            session.update(stmt);
        }

    }


    private String likePrefixPattern(final String namespacePath) {
        final char esc = '!';
        final StringBuilder sb = new StringBuilder();
        for (char ch : namespacePath.toCharArray()) {
            if (ch == esc || ch == '%' || ch == '_') {
                sb.append(esc);
            }
            sb.append(ch);
        }
        sb.append('%');
        return sb.toString();
    }

    private long updateItem(final SyncSession session, final String namespaceHash, String itemKey,
                            final Map<String, Object> value, final long expectedVersion) {
        final Update stmt;
        stmt = SQLs.singleUpdate()
                .update(this.tableMeta, AS, "t")
                .set(this.valueJson, JsonUtils.getJsonCodec().toJson(value))
                .where(this.namespacePath.equal(namespaceHash))
                .and(this.itemKey.equal(itemKey))
                .and(this.version.equal(expectedVersion))
                .asUpdate();

        return session.update(stmt);
    }

    private D createDomain(String key, String namespacePath, Map<String, Object> value) {
        final D domain;
        domain = this.constructor.get();
        domain.setNamespacePath(namespacePath)
                .setItemKey(key)
                .setValueJson(JsonUtils.getJsonCodec().toJson(value));
        return domain;
    }


    private long insertIfAbsent(SyncSession session, String key, String namespacePath, Map<String, Object> value) {
        final D domain;
        domain = createDomain(key, namespacePath, value);

        final Insert stmt;
        switch (session.dialectDatabase()) {
            case PostgreSQL:
                stmt = postgreInsertOrDoNothing(domain);
                break;
            case MySQL:
                stmt = SQLs.singleInsert()
                        .nullMode(this.nullMode)
                        .literalMode(this.literalMode)
                        .insertInto(this.tableMeta)
                        .values(List.of(domain))
                        .asInsert();
                break;
            default:
                throw new IllegalStateException(String.format("%s is supported", session.dialectDatabase()));
        }

        long rowAffected;
        try {
            rowAffected = session.update(stmt);
        } catch (DriverException e) {
            if (Objects.equals(e.getSqlState(), "23000")) {
                rowAffected = 0L;
            } else {
                throw e;
            }
        }
        return rowAffected;
    }

    private Insert postgreInsertOrDoNothing(final D domain) {
        return Postgres.singleInsert()
                .nullMode(this.nullMode)
                .literalMode(this.literalMode)
                .insertInto(this.tableMeta)
                .values(List.of(domain))
                .onConflict().parens(s -> s.space(this.namespacePath)
                        .comma(this.itemKey)
                )
                .doNothing()
                .asInsert();
    }

    private Insert upsertStmt(final Database database, final D domain) {
        return switch (database) {
            case PostgreSQL -> postgresUpsertStmt(domain);
            case MySQL -> mysqlUpsertStmt(domain);
            default -> throw new IllegalStateException(String.format("Unsupported database: %s", database));
        };
    }

    private Insert postgresUpsertStmt(D domain) {
        return Postgres.singleInsert()
                .nullMode(this.nullMode)
                .literalMode(this.literalMode)
                .insertInto(this.tableMeta)
                .values(List.of(domain))
                .onConflict()
                .parens(s -> s.space(this.namespacePath)
                        .comma(this.itemKey))
                .doUpdate()
                .set(this.valueJson, excluded(this.valueJson))
                .asInsert();
    }

    private Insert mysqlUpsertStmt(D domain) {
        return MySQLs.singleInsert()
                .nullMode(this.nullMode)
                .literalMode(this.literalMode)
                .insertInto(this.tableMeta)
                .values(List.of(domain))
                .as("r")
                .onDuplicateKey()
                .update(this.valueJson, field("r", this.valueJson))
                .asInsert();
    }

    private static String encodeNamespace(final List<String> namespace) {
        Objects.requireNonNull(namespace, "namespace");

        final StringBuilder builder;
        builder = new StringBuilder();
        for (String segment : namespace) {
            if (segment == null) {
                throw new IllegalArgumentException("namespace segment must not be null");
            }
            if (segment.indexOf(NAMESPACE_SEPARATOR) >= 0) {
                throw new IllegalArgumentException("namespace segment must not contain the unit separator (0x1F)");
            }
            builder.append(segment)
                    .append(NAMESPACE_SEPARATOR);
        } // loop

        final String path;
        path = builder.toString();
        if (path.length() > MAX_NAMESPACE_PATH_LENGTH) {
            throw new IllegalArgumentException(
                    "namespace path length must not exceed " + MAX_NAMESPACE_PATH_LENGTH);
        }
        return path;
    }

    private static void validateKey(final String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key must not be empty");
        }
        if (key.length() > MAX_ITEM_KEY_LENGTH) {
            throw new IllegalArgumentException(
                    "key length must not exceed " + MAX_ITEM_KEY_LENGTH);
        }
    }

    public static <D extends AgentScopeStore> Builder<D> builder(final SimpleTableMeta<D> tableMeta) {
        return new Builder<>(tableMeta);
    }


    public static final class Builder<D extends AgentScopeStore> {

        private final SimpleTableMeta<D> tableMeta;

        private SyncSessionFactory sessionFactory;

        /// The null handling mode.
        private NullMode nullMode = NullMode.DEFAULT;

        /// The literal handling mode.
        private LiteralMode literalMode = LiteralMode.DEFAULT;


        private Builder(final SimpleTableMeta<D> tableMeta) {
            this.tableMeta = tableMeta;
        }

        public Builder<D> sessionFactory(final SyncSessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
            return this;
        }

        public Builder<D> nullMode(final NullMode nullMode) {
            this.nullMode = nullMode;
            return this;
        }

        public Builder<D> literalMode(final LiteralMode literalMode) {
            this.literalMode = literalMode;
            return this;
        }

        public ArmyBaseStore<D> build() {
            if (this.tableMeta == null) {
                throw new IllegalStateException(String.format("%s is required", "tableMeta"));
            } else if (this.sessionFactory == null) {
                throw new IllegalStateException(String.format("%s is required", "sessionFactory"));
            }

            ValidateUtils.assertExistsIndex(this.tableMeta, true, List.of("namespacePath", "itemKey"));

            if (this.nullMode == null) {
                this.nullMode = NullMode.DEFAULT;
            }

            if (this.literalMode == null) {
                this.literalMode = LiteralMode.DEFAULT;
            }
            return new ArmyBaseStore<>(this);
        }



    }

}
