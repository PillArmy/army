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
import io.army.mapping.array.BigIntegerArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;

import java.math.BigDecimal;
import java.math.BigInteger;

/// 
/// This class is mapping class of {@link BigInteger}.
/// This mapping type can convert below java type:
/// 
/// - {@link Byte}
/// - {@link Short}
/// - {@link Integer}
/// - {@link Long}
/// - {@link java.math.BigInteger}
/// - {@link java.math.BigDecimal},it has a zero fractional part
/// - {@link Boolean} true : {@link BigInteger#ONE} , false: {@link BigInteger#ZERO}
/// - {@link String}
/// 
/// to {@link BigInteger},if overflow,throw {@link io.army.ArmyException}
/// @since 0.6.0
public final class BigIntegerType extends _NumericType._IntegerType {


    public static BigIntegerType from(final Class<?> javaType) {
        if (javaType != BigInteger.class) {
            throw errorJavaType(BigIntegerType.class, javaType);
        }
        return INSTANCE;
    }

    public static final BigIntegerType INSTANCE = new BigIntegerType();

    /// private constructor
    private BigIntegerType() {
    }


    @Override
    public Class<?> javaType() {
        return BigInteger.class;
    }

    @Override
    public DataType map(ServerMeta meta) {
        return BigDecimalType.mapToDataType(this, meta);
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return BigIntegerArrayType.LINEAR;
    }


    @Override
    public BigDecimal beforeBind(DataType dataType, MappingEnv env, final Object source) {
        final BigDecimal value;
        value = BigDecimalType.toBigDecimal(this, dataType, source, PARAM_ERROR_HANDLER)
                .stripTrailingZeros();
        if (value.scale() != 0) {
            throw PARAM_ERROR_HANDLER.apply(this, dataType, source, null);
        }
        return value;
    }

    @Override
    public BigInteger afterGet(final DataType dataType, MappingEnv env, final Object source) {
        return toBigInteger(this, dataType, source, ACCESS_ERROR_HANDLER);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof BigIntegerType;
    }



    public static BigInteger toBigInteger(final MappingType type, final DataType dataType, final Object nonNull,
                                          final ErrorHandler errorHandler) {
        final BigInteger value;
        if (nonNull instanceof BigInteger) {
            value = (BigInteger) nonNull;
        } else if (nonNull instanceof Integer
                || nonNull instanceof Long
                || nonNull instanceof Short
                || nonNull instanceof Byte) {
            value = BigInteger.valueOf(((Number) nonNull).longValue());
        } else if (nonNull instanceof Boolean) {
            value = (Boolean) nonNull ? BigInteger.ONE : BigInteger.ZERO;
        } else if (nonNull instanceof BigDecimal) {
            try {
                value = ((BigDecimal) nonNull).stripTrailingZeros().toBigIntegerExact();
            } catch (Exception e) {
                throw errorHandler.apply(type, dataType, nonNull, e);
            }
        } else if (nonNull instanceof String) {
            try {
                value = new BigInteger((String) nonNull);
            } catch (NumberFormatException e) {
                throw errorHandler.apply(type, dataType, nonNull, e);
            }
        } else {
            throw errorHandler.apply(type, dataType, nonNull, null);
        }
        return value;
    }


}
