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

package io.army.agentscope.snapshot;

import io.agentscope.harness.agent.sandbox.snapshot.RemoteSnapshotClient;
import io.army.criteria.Insert;
import io.army.criteria.LiteralMode;
import io.army.criteria.NullMode;
import io.army.criteria.Select;
import io.army.criteria.impl.MySQLs;
import io.army.criteria.impl.Postgres;
import io.army.criteria.impl.SQLs;
import io.army.dialect.Database;
import io.army.meta.FieldMeta;
import io.army.meta.PrimaryFieldMeta;
import io.army.meta.SimpleTableMeta;
import io.army.pojo.ObjectAccessorFactory;
import io.army.session.SyncSession;
import io.army.session.SyncSessionFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static io.army.criteria.impl.Postgres.excluded;
import static io.army.criteria.impl.SQLs.field;
import static io.army.criteria.impl.SQLs.AS;

/// Army-backed {@link RemoteSnapshotClient}.
///
/// <p>Subclass {@link AgentScopeSnapshot} with @Table, then pass its meta to
/// {@link #builder}.</p>
///
/// <p>Upload is an upsert — re-uploading the same snapshot replaces the archive atomically.
/// Only PostgreSQL and MySQL are supported.</p>
public final class ArmyRemoteSnapshotClient<D extends AgentScopeSnapshot> implements RemoteSnapshotClient {


    private final SyncSessionFactory sessionFactory;

    private final SimpleTableMeta<D> tableMeta;

    private final PrimaryFieldMeta<D> id;

    private final FieldMeta<D> data;

    /// The null handling mode.
    private final NullMode nullMode;

    /// The literal handling mode.
    private final LiteralMode literalMode;

    private final Supplier<D> constructor;



    private ArmyRemoteSnapshotClient(final Builder<D> builder) {
        this.sessionFactory = builder.sessionFactory;
        this.tableMeta = builder.tableMeta;

        this.id = this.tableMeta.id();
        this.data = this.tableMeta.field("data");

        this.nullMode = builder.nullMode;
        this.literalMode = builder.literalMode;

        this.constructor = ObjectAccessorFactory.pojoConstructor(this.tableMeta.javaType());
    }


    @Override
    public void upload(final String snapshotId, final InputStream data) throws Exception {
        Objects.requireNonNull(snapshotId, "snapshotId");
        Objects.requireNonNull(data, "data");

        final String sessionName = getClass().getName() + '.' + "upload";
        try (SyncSession session = this.sessionFactory.localSession(sessionName, false)) {

            final byte[] bytes;
            bytes = data.readAllBytes();

            final D domain;
            domain = this.constructor.get();
            domain.setId(snapshotId)
                    .setData(bytes);

            final Insert stmt;
            stmt = upsertStmt(this.sessionFactory.dialectDatabase(), domain);
            session.update(stmt);
        }
    }

    @Override
    public InputStream download(final String snapshotId) {
        Objects.requireNonNull(snapshotId, "snapshotId");

        final String sessionName = getClass().getName() + '.' + "download";
        try (SyncSession session = this.sessionFactory.localSession(sessionName, true)) {

            final Select stmt;
            stmt = SQLs.query()
                    .select(this.data)
                    .from(this.tableMeta, AS, "t")
                    .where(this.id.equal(snapshotId))
                    .asQuery();

            final byte[] bytes;
            bytes = session.queryOne(stmt, byte[].class);
            if (bytes == null) {
                String m = String.format("Snapshot %s not found", snapshotId);
                throw new RuntimeException(m);
            }
            return new ByteArrayInputStream(bytes);
        }

    }

    @Override
    public boolean exists(final String snapshotId)  {
        Objects.requireNonNull(snapshotId, "snapshotId");

        final String sessionName = getClass().getName() + '.' + "exists";
        try (SyncSession session = this.sessionFactory.localSession(sessionName, true)) {

            final Select stmt;
            stmt = SQLs.query()
                    .select(this.id)
                    .from(this.tableMeta, AS, "t")
                    .where(this.id.equal(snapshotId))
                    .asQuery();
            return session.queryOne(stmt, Object.class) != null;
        }
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
                .parens(s -> s.space(this.id))
                .doUpdate()
                .set(this.data, excluded(this.data))
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
                .update(this.data, field("r", this.data))
                .asInsert();
    }


    public static <D extends AgentScopeSnapshot> Builder<D> builder(SimpleTableMeta<D> tableMeta) {
        return new Builder<>(tableMeta);
    }


    public static final class Builder<D extends AgentScopeSnapshot> {

        private final SimpleTableMeta<D> tableMeta;

        private SyncSessionFactory sessionFactory;

        /// The null handling mode.
        private NullMode nullMode = NullMode.DEFAULT;

        /// The literal handling mode.
        private LiteralMode literalMode = LiteralMode.DEFAULT;

        private Builder(SimpleTableMeta<D> tableMeta) {
            this.tableMeta = tableMeta;
        }

        public Builder<D> sessionFactory(SyncSessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
            return this;
        }

        public ArmyRemoteSnapshotClient<D> build() {
            Objects.requireNonNull(this.tableMeta, "tableMeta");
            Objects.requireNonNull(this.sessionFactory, "sessionFactory");
            if (this.nullMode == null) {
                this.nullMode = NullMode.DEFAULT;
            }

            if (this.literalMode == null) {
                this.literalMode = LiteralMode.DEFAULT;
            }
            return new ArmyRemoteSnapshotClient<>(this);
        }

    }

}
