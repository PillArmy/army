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

package io.army.mapping.array;

import io.army.criteria.CriteriaException;
import io.army.dialect.UnsupportedDialectException;
import io.army.executor.DataAccessException;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping.SqlCharType;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.sqltype.SQLType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.util.Objects;

public class SqlCharArrayType extends _ArmyBuildInArrayType {

    public static SqlCharArrayType from(final Class<?> javaType) {
        final SqlCharArrayType instance;
        if (javaType == String[].class) {
            instance = LINEAR;
        } else if (javaType.isArray() && ArrayUtils.underlyingComponent(javaType) == String.class) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(SqlCharArrayType.class, javaType);
        }
        return instance;
    }


    public static final SqlCharArrayType UNLIMITED = new SqlCharArrayType(Object.class);

    public static final SqlCharArrayType LINEAR = new SqlCharArrayType(String[].class);

    private final Class<?> javaType;

    /// private constructor
    private SqlCharArrayType(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        final SQLType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = PgType.CHAR_ARRAY;
                break;
            case Oracle:
            case H2:
            case MySQL:
            default:
                throw MAP_ERROR_HANDLER.apply(this, meta);
        }
        return dataType;
    }


    @Override
    public final String beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, TextArrayType::appendToText, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, String::substring, null, null, null);
    }

    @Override
    public final Class<?> underlyingJavaType() {
        return String.class;
    }

    @Override
    public final MappingType underlyingType() {
        return SqlCharType.INSTANCE;
    }

    @Override
    public final MappingType elementType() {
        final Class<?> javaType = this.javaType;
        final MappingType instance;
        if (javaType == Object.class) {
            instance = this;
        } else if (javaType == String[].class) {
            instance = SqlCharType.INSTANCE;
        } else {
            instance = from(javaType.getComponentType());
        }
        return instance;
    }

    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        final Class<?> javaType = this.javaType;
        if (javaType == Object.class) { // unlimited dimension array
            return this;
        }
        return from(ArrayUtils.arrayClassOf(javaType));
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
        } else if (obj instanceof SqlCharArrayType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }

    private static final class ClassValueHolder {

        private static final ClassValue<SqlCharArrayType> CLASS_VALUE = FuncClassValue.create(SqlCharArrayType::new);

    }


}
