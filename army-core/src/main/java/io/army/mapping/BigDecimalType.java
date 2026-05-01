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
import io.army.mapping.array.BigDecimalArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/// 
/// This class is mapping class of {@link BigDecimal}.
/// This mapping type can convert below java type:
/// 
/// - {@link Byte}
/// - {@link Short}
/// - {@link Integer}
/// - {@link Long}
/// - {@link java.math.BigInteger}
/// - {@link Double}
/// - {@link Float}
/// - {@link Boolean} true : {@link BigDecimal#ONE} , false: {@link BigDecimal#ZERO}
/// - {@link String}
/// 
/// to {@link BigDecimal},if overflow,throw {@link io.army.ArmyException}
/// @since 0.6.0
public final class BigDecimalType extends _NumericType implements MappingType.SqlDecimal {


    public static BigDecimalType from(Class<?> javaType) {
        if (javaType != BigDecimal.class) {
            throw errorJavaType(BigDecimalType.class, javaType);
        }
        return INSTANCE;
    }


    public static final BigDecimalType INSTANCE = new BigDecimalType();

    /// private constructor
    private BigDecimalType() {
    }


    @Override
    public Class<?> javaType() {
        return BigDecimal.class;
    }


    @Override
    public DataType map(final ServerMeta meta) {
        return mapToDataType(this, meta);
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return BigDecimalArrayType.LINEAR;
    }

    @Override
    public BigDecimal beforeBind(DataType dataType, MappingEnv env, final Object source) {
        return toBigDecimal(this, dataType, source, PARAM_ERROR_HANDLER);
    }

    @Override
    public BigDecimal afterGet(DataType dataType, MappingEnv env, final Object source) {
        return toBigDecimal(this, dataType, source, ACCESS_ERROR_HANDLER);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof BigDecimalType;
    }


    /*-------------------below static methods -------------------*/

    public static BigDecimal toBigDecimal(final MappingType type, final DataType dataType, final Object nonNull,
                                          final ErrorHandler errorHandler) {
        final BigDecimal value;
        if (nonNull instanceof BigDecimal) {
            value = (BigDecimal) nonNull;
        } else if (nonNull instanceof Integer
                || nonNull instanceof Long
                || nonNull instanceof Short
                || nonNull instanceof Byte) {
            value = BigDecimal.valueOf(((Number) nonNull).longValue());
        } else if (nonNull instanceof BigInteger) {
            value = new BigDecimal((BigInteger) nonNull);
        } else if (nonNull instanceof Boolean) {
            value = (Boolean) nonNull ? BigDecimal.ONE : BigDecimal.ZERO;
        } else if (nonNull instanceof Double || nonNull instanceof Float) {
            value = new BigDecimal(nonNull.toString()); // must use double string.
        } else if (nonNull instanceof String) {
            // TODO handle postgre money
            try {
                value = new BigDecimal((String) nonNull);
            } catch (NumberFormatException e) {
                throw errorHandler.apply(type, dataType, nonNull, e);
            }
        } else {
            throw errorHandler.apply(type, dataType, nonNull, null);
        }
        return value;
    }

    public static DataType mapToDataType(final MappingType type, final ServerMeta meta) {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case MySQL:
                dataType = MySQLType.DECIMAL;
                break;
            case PostgreSQL:
                dataType = PgType.DECIMAL;
                break;
            case SQLite:
                dataType = SQLiteType.DECIMAL;
                break;
            case H2:
                dataType = H2Type.DECIMAL;
                break;
            case Oracle:
                dataType = OracleDataType.NUMBER;
                break;
            default:
                throw MAP_ERROR_HANDLER.apply(type, meta);

        }
        return dataType;
    }


}
