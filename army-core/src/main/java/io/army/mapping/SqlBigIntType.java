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

package io.army.mapping;

import io.army.criteria.CriteriaException;
import io.army.dialect.UnsupportedDialectException;
import io.army.executor.DataAccessException;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.util.FuncClassValue;

import java.math.BigInteger;
import java.util.Objects;

/// Map bigint to multiple Java types
public final class SqlBigIntType extends _ArmyNoInjectionType implements MappingType.SqlInteger {

    public static SqlBigIntType from(Class<?> javaType) {
        if (javaType == long.class) {
            javaType = Long.class;
        } else if (javaType != Long.class
                && javaType != String.class
                && javaType != BigInteger.class) {
            throw errorJavaType(SqlBigIntType.class, javaType);
        }
        return CLASS_VALUE.get(javaType);
    }

    private static final ClassValue<SqlBigIntType> CLASS_VALUE = FuncClassValue.create(SqlBigIntType::new);

    private final Class<?> javaType;

    private SqlBigIntType(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public DataType map(ServerMeta meta) throws UnsupportedDialectException {
        return LongType.mapToDataType(this, meta);
    }

    @Override
    public Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return LongType.toLong(this, dataType, source, Long.MIN_VALUE, Long.MAX_VALUE, PARAM_ERROR_HANDLER);
    }

    @Override
    public Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        final long v;
        v = LongType.toLong(this, dataType, source, Long.MIN_VALUE, Long.MAX_VALUE, ACCESS_ERROR_HANDLER);

        final Object value;
        if (this.javaType == String.class) {
            value = Long.toString(v);
        } else if (this.javaType == Long.class) {
            value = v;
        } else if (this.javaType == BigInteger.class) {
            value = BigInteger.valueOf(v);
        } else {
            throw dataAccessError(this, dataType, source, null);
        }
        return value;
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return super.arrayTypeOfThis();
    }


    @Override
    public int hashCode() {
        return Objects.hash(SqlBigIntType.class, this.javaType);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof SqlBigIntType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }


}
