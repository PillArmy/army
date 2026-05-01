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
import io.army.mapping.array.ShortArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.MySQLType;
import io.army.sqltype.PgType;
import io.army.sqltype.SQLiteType;
import io.army.util.ClassUtils;

/// 
/// This class is mapping class of {@link Short}.
/// This mapping type can convert below java type:
/// 
/// - {@link Byte}
/// - {@link Short}
/// - {@link Integer}
/// - {@link Long}
/// - {@link java.math.BigInteger}
/// - {@link java.math.BigDecimal},it has a zero fractional part
/// - {@link Boolean} true : 1 , false: 0
/// - {@link String} 
/// 
/// to {@link Short},if overflow,throw {@link io.army.ArmyException}
/// @since 0.6.0
public final class ShortType extends _NumericType._IntegerType {

    public static ShortType from(final Class<?> fieldType) {
        if (!ClassUtils.isAssignableFrom(Short.class, fieldType)) {
            throw errorJavaType(ShortType.class, fieldType);
        }
        return INSTANCE;
    }

    public static final ShortType INSTANCE = new ShortType();

    /// private constructor
    private ShortType() {
    }

    @Override
    public Class<?> javaType() {
        return Short.class;
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return ShortArrayType.LINEAR;
    }

    @Override
    public DataType map(final ServerMeta meta) {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case MySQL:
                dataType = MySQLType.SMALLINT;
                break;
            case PostgreSQL:
                dataType = PgType.SMALLINT;
                break;
            case SQLite:
                dataType = SQLiteType.SMALLINT;
                break;
            case Oracle:
            case H2:
            default:
                throw MAP_ERROR_HANDLER.apply(this, meta);
        }
        return dataType;
    }


    @Override
    public Short beforeBind(DataType dataType, MappingEnv env, final Object source) {
        return (short) IntegerType.toInt(this, dataType, source, Short.MIN_VALUE, Short.MAX_VALUE, PARAM_ERROR_HANDLER);
    }

    @Override
    public Short afterGet(DataType dataType, MappingEnv env, Object source) {
        return (short) IntegerType.toInt(this, dataType, source, Short.MIN_VALUE, Short.MAX_VALUE, ACCESS_ERROR_HANDLER);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ShortType;
    }


}
