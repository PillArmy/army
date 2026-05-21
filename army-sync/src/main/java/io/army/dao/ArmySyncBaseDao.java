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

package io.army.dao;

import io.army.criteria.Select;
import io.army.criteria.Update;
import io.army.lang.Nullable;
import io.army.meta.FieldMeta;
import io.army.meta.PrimaryFieldMeta;
import io.army.meta.TableMeta;
import io.army.modelgen._MetaBridge;
import io.army.session.SyncSession;
import io.army.session.SyncSessionContext;
import io.army.util.SQLStmts;
import io.army.util.SyncCriteria;

import java.util.List;

/// This class is an abstract implementation of {@link SyncBaseDao}
///
/// @since 0.6.0
public abstract class ArmySyncBaseDao implements SyncBaseDao {

    protected final SyncSessionContext sessionContext;

    protected ArmySyncBaseDao(SyncSessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }

    @Override
    public <T> void save(T domain) {
        this.sessionContext.currentSession().save(domain);
    }

    @Override
    public <T> void batchSave(List<T> domainList) {
        this.sessionContext.currentSession().batchSave(domainList);
    }

    @Nullable
    @Override
    public <T> T get(Class<T> domainClass, Object id) {
        return findByUnique(domainClass, _MetaBridge.ID, id);
    }

    @Nullable
    @Override
    public <T> T getByUnique(Class<T> domainClass, String fieldName, Object fieldValue) {
        return findByUnique(domainClass, fieldName, fieldValue);
    }

    @Nullable
    @Override
    public <T> T findById(Class<T> domainClass, Object id) {
        return findByUnique(domainClass, _MetaBridge.ID, id);
    }

    @Nullable
    @Override
    public <T> T findByUnique(Class<T> domainClass, String fieldName, Object fieldValue) {
        final SyncSession session;
        session = this.sessionContext.currentSession();
        return findByUniqueFor(session.tableMeta(domainClass), domainClass, session, fieldName, fieldValue);
    }

    @Override
    public <T> boolean existsById(Class<T> domainClass, Object id) {
        final SyncSession session = this.sessionContext.currentSession();
        final TableMeta<T> tableMeta = session.tableMeta(domainClass);
        final PrimaryFieldMeta<T> idField = tableMeta.id();

        final Select stmt;
        stmt = SQLStmts.existsByFieldStmt(idField, id);
        return session.queryOne(stmt, idField.javaType()) != null;
    }

    @Override
    public <T> boolean existsByByField(Class<T> domainClass, String fieldName, Object fieldValue) {
        final SyncSession session = this.sessionContext.currentSession();
        final TableMeta<T> tableMeta = session.tableMeta(domainClass);
        final FieldMeta<T> field = tableMeta.field(fieldName);

        final Select stmt;
        stmt = SQLStmts.existsByFieldStmt(field, fieldValue);
        return session.queryOne(stmt, field.javaType()) != null;
    }

    @Override
    public <T> long rowCount(final Class<T> domainClass) {
        return SyncCriteria.rowCount(domainClass, this.sessionContext.currentSession());
    }


    @Override
    public <T> long updateField(Class<T> domainClass, Object id, String fieldName, Object fieldValue) {
        final SyncSession session = this.sessionContext.currentSession();
        final Update stmt;
        stmt = SQLStmts.updateFieldStmt(session.tableMeta(domainClass), id, fieldName, fieldValue);
        return session.update(stmt);
    }

    @Override
    public <T, F> long updateFieldWhenMatch(Class<T> domainClass, Object id, String fieldName, F fieldValue, @Nullable F defaultValue) {
        final SyncSession session = this.sessionContext.currentSession();
        final Update stmt;
        stmt = SQLStmts.updateFieldWhenMatchStmt(session.tableMeta(domainClass), id, fieldName, fieldValue, defaultValue);
        return session.update(stmt);
    }

    @Nullable
    protected static <T, R> R findByUniqueFor(final TableMeta<T> domainTable, final Class<R> returnClass,
                                              final SyncSession session, final String fieldName,
                                              final Object fieldValue) {
        final Select stmt;
        stmt = SQLStmts.queryDomainByUniqueStmtFor(domainTable, fieldName, fieldValue);
        return session.queryOne(stmt, returnClass);
    }

}
