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
import io.army.mapping.BigDecimalType;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.sqltype.SQLType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.math.BigDecimal;
import java.util.Objects;

/// This class representing the mapping that map the array of {@link BigDecimal} to database decimal array,for example {@link PgType#DECIMAL_ARRAY}.
///
/// @see io.army.mapping.BigDecimalType
/// @since 0.6.0
public class BigDecimalArrayType extends _ArmyCoreArrayType {

    public static BigDecimalArrayType from(final Class<?> javaType) {
        final BigDecimalArrayType instance;

        if (javaType == BigDecimal[].class) {
            instance = LINEAR;
        } else if (javaType == Object.class) {
            instance = UNLIMITED;
        } else if (!javaType.isArray()) {
            throw errorJavaType(BigDecimalArrayType.class, javaType);
        } else if (ArrayUtils.underlyingComponent(javaType) == BigDecimal.class) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(BigDecimalArrayType.class, javaType);
        }
        return instance;
    }

    public static final BigDecimalArrayType UNLIMITED = new BigDecimalArrayType(Object.class);

    public static final BigDecimalArrayType LINEAR = new BigDecimalArrayType(BigDecimal[].class);

    static {
        addArrayFromFunc(BigDecimalArrayType.class, BigDecimalArrayType::from);
    }


    private final Class<?> javaType;

    /// private constructor
    private BigDecimalArrayType(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        return mapToSqlType(this, meta);
    }

    @Override
    public final String beforeBind(DataType dataType, MappingEnv env, final Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, BigDecimalArrayType::appendToText, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, false, BigDecimalArrayType::parseBigDecimal, null);
    }

    @Override
    public final Class<?> underlyingJavaType() {
        return BigDecimal.class;
    }

    @Override
    public MappingType underlyingType() {
        return BigDecimalType.INSTANCE;
    }

    @Override
    public final MappingType elementType() {
        final MappingType instance;
        final Class<?> javaType = this.javaType;
        if (javaType == Object.class) { // unlimited dimension array
            instance = this;
        } else if (javaType == BigDecimal[].class) {
            instance = BigDecimalType.INSTANCE;
        } else {
            instance = from(javaType.getComponentType());
        }
        return instance;
    }


    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        final Class<?> javaType = this.javaType;
        if (javaType == Object.class) { // unlimited dimension array
            return this;
        }
        return from(ArrayUtils.arrayClassOf(javaType));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.javaType);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof BigDecimalArrayType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }




    /*-------------------below static methods -------------------*/

    public static SQLType mapToSqlType(final MappingType type, final ServerMeta meta) {
        final SQLType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = PgType.DECIMAL_ARRAY;
                break;
            case MySQL:
            case SQLite:
            case H2:
            case Oracle:
            default:
                throw MAP_ERROR_HANDLER.apply(type, meta);
        }
        return dataType;
    }

    public static BigDecimal parseBigDecimal(String text, int offset, int end) {
        return new BigDecimal(text.substring(offset, end));
    }

    public static void appendToText(final Object element, final StringBuilder appender) {
        if (!(element instanceof BigDecimal)) {
            // no bug,never here
            throw new IllegalArgumentException();
        }
        appender.append(((BigDecimal) element).toPlainString());
    }

    private static final class ClassValueHolder {

        private static final ClassValue<BigDecimalArrayType> CLASS_VALUE = FuncClassValue.create(BigDecimalArrayType::new);

    }


}
