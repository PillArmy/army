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
import io.army.dialect.Database;
import io.army.dialect.UnsupportedDialectException;
import io.army.dialect._Constant;
import io.army.executor.DataAccessException;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping.YearMonthType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public class YearMonthArrayType extends _ArmyCoreArrayType {


    public static YearMonthArrayType from(Class<?> javaType) {
        final YearMonthArrayType instance;
        if (javaType == YearMonth[].class) {
            instance = LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(YearMonthArrayType.class, javaType);
        } else if (ArrayUtils.underlyingComponent(javaType) == YearMonth.class) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(YearMonthArrayType.class, javaType);
        }
        return instance;
    }

    public static final YearMonthArrayType LINEAR = new YearMonthArrayType(YearMonth[].class);

    static {
        addArrayFromFunc(YearMonthArrayType.class, YearMonthArrayType::from);
    }

    private final Class<?> javaType;

    /// private constructor
    private YearMonthArrayType(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        if (meta.serverDatabase() != Database.PostgreSQL) {
            throw mapError(this, meta);
        }
        return PgType.DATE_ARRAY;
    }

    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, YearMonthArrayType::appendToText, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, YearMonthArrayType::parseText, null);
    }

    @Override
    public final Class<?> underlyingJavaType() {
        return YearMonth.class;
    }

    @Override
    public final MappingType underlyingType() {
        return YearMonthType.INSTANCE;
    }

    @Override
    public final MappingType elementType() {
        final MappingType instance;
        if (this.javaType == YearMonth[].class) {
            instance = YearMonthType.INSTANCE;
        } else {
            instance = from(this.javaType.getComponentType());
        }
        return instance;
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return from(ArrayUtils.arrayClassOf(this.javaType));
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
        } else if (obj instanceof YearMonthArrayType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }

    private static void appendToText(Object value, StringBuilder builder) {
        if (!(value instanceof YearMonth y)) {
            throw new IllegalArgumentException();
        }
        builder.append(_Constant.DOUBLE_QUOTE)
                .append(y.getYear())
                .append('-')
                .append(y.getMonthValue())
                .append('-')
                .append("01")
                .append(_Constant.DOUBLE_QUOTE);

    }

    private static YearMonth parseText(final String text, final int offset, final int end) {
        return YearMonth.from(LocalDate.parse(text.substring(offset, end)));
    }


    private static final class ClassValueHolder {

        private static final ClassValue<YearMonthArrayType> CLASS_VALUE = FuncClassValue.create(YearMonthArrayType::new);

    }


}
