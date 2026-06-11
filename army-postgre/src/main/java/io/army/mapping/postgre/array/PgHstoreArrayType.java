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

package io.army.mapping.postgre.array;

import io.army.criteria.CriteriaException;
import io.army.dialect.UnsupportedDialectException;
import io.army.executor.DataAccessException;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.util.FuncClassValue;

import java.util.Map;
import java.util.Objects;

/// Mapping type for PostgreSQL HSTORE key/value array type.
///
/// @see <a href="https://www.postgresql.org/docs/current/hstore.html">hstore key/value datatype</a>
public class PgHstoreArrayType extends _ArmyBuildInArrayType {


    private final Class<?> javaType;

    private PgHstoreArrayType(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public DataType map(ServerMeta meta) throws UnsupportedDialectException {
        //TODO
        throw new UnsupportedOperationException();
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
    public Class<?> underlyingJavaType() {
        return Map.class;
    }

    @Override
    public MappingType elementType() {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public MappingType underlyingType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.javaType);
    }

    @Override
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof PgHstoreArrayType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }


    private static final class ClassValueHolder {

        private static final ClassValue<PgHstoreArrayType> CLASS_VALUE = FuncClassValue.create(PgHstoreArrayType::new);

    }
}
