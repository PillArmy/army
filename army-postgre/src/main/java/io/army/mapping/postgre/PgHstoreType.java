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

package io.army.mapping.postgre;

import io.army.criteria.CriteriaException;
import io.army.dialect.Database;
import io.army.dialect.UnsupportedDialectException;
import io.army.executor.DataAccessException;
import io.army.mapping.*;
import io.army.meta.ServerMeta;
import io.army.sqltype.ArmyType;
import io.army.sqltype.CustomType;
import io.army.sqltype.DataType;

import java.util.Map;

/// Mapping type for PostgreSQL HSTORE key/value datatype.
///
/// @see <a href="https://www.postgresql.org/docs/current/hstore.html">hstore key/value datatype</a>
public final class PgHstoreType extends _ArmyBuildInType implements MappingType.SqlUserDefined, DualGenericsMapping {


    public static final PgHstoreType INSTANCE = new PgHstoreType();


    private static final DataType DATA_TYPE = CustomType.builder()
            .typeName("HSTORE")
            .javaType(Map.class)
            .componentType(ArmyType.DIALECT_TYPE)
            .componentCreateDdl(false)
            .build();

    private PgHstoreType() {
    }

    @Override
    public Class<?> javaType() {
        return Map.class;
    }

    @Override
    public DataType map(ServerMeta meta) throws UnsupportedDialectException {
        if (meta.serverDatabase() != Database.PostgreSQL) {
            throw mapError(this, meta);
        }
        return DATA_TYPE;
    }

    @Override
    public Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return super.arrayTypeOfThis();
    }

    @Override
    public Class<?> firstGenericsType() {
        return String.class;
    }

    @Override
    public Class<?> secondGenericsType() {
        return String.class;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof IntegerType;
    }



}
