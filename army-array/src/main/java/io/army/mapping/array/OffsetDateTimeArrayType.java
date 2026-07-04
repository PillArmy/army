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
import io.army.mapping.OffsetDateTimeType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;
import io.army.util._TimeUtils;

import java.time.OffsetDateTime;
import java.util.Objects;

public class OffsetDateTimeArrayType extends _ArmyCoreArrayType {


    public static OffsetDateTimeArrayType from(final Class<?> javaType) {
        final OffsetDateTimeArrayType instance;
        if (javaType == OffsetDateTime[].class) {
            instance = LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(OffsetDateTimeArrayType.class, javaType);
        } else if (ArrayUtils.underlyingComponent(javaType) == OffsetDateTime.class) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(OffsetDateTimeArrayType.class, javaType);
        }
        return instance;
    }

    public static final OffsetDateTimeArrayType LINEAR = new OffsetDateTimeArrayType(OffsetDateTime[].class);

    public static final OffsetDateTimeArrayType UNLIMITED = new OffsetDateTimeArrayType(Object.class);

    static {
        addArrayFromFunc(OffsetDateTimeArrayType.class, OffsetDateTimeArrayType::from);
    }

    private final Class<?> javaType;

    /// private constructor
    private OffsetDateTimeArrayType(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        return mapToDataType(this, meta);
    }

    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, OffsetDateTimeArrayType::appendToText, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, OffsetDateTimeArrayType::parseText, null);
    }

    @Override
    public final Class<?> underlyingJavaType() {
        return OffsetDateTime.class;
    }

    @Override
    public final MappingType underlyingType() {
        return OffsetDateTimeType.INSTANCE;
    }

    @Override
    public final MappingType elementType() {
        final MappingType instance;
        final Class<?> javaType = this.javaType;
        if (javaType == Object.class) {
            instance = this;
        } else if (javaType == OffsetDateTime[].class) {
            instance = OffsetDateTimeType.INSTANCE;
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
    public int hashCode() {
        return Objects.hash(this.javaType);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof OffsetDateTimeArrayType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }


    /*-------------------below static methods -------------------*/

    static DataType mapToDataType(final MappingType type, final ServerMeta meta) {
        if (meta.serverDatabase() != Database.PostgreSQL) {
            throw mapError(type, meta);
        }
        return PgType.TIMESTAMPTZ_ARRAY;
    }

    private static OffsetDateTime parseText(final CharSequence text, final int offset, final int end) {
        return OffsetDateTime.parse(text.subSequence(offset, end), _TimeUtils.OFFSET_DATETIME_FORMATTER_6);
    }

    private static void appendToText(final Object element, final StringBuilder appender) {
        if (!(element instanceof OffsetDateTime)) {
            // no bug,never here
            throw new IllegalArgumentException();
        }

        appender.append(_Constant.DOUBLE_QUOTE);
        appender.append(((OffsetDateTime) element).format(_TimeUtils.OFFSET_DATETIME_FORMATTER_6));
        appender.append(_Constant.DOUBLE_QUOTE);

    }


    private static final class ClassValueHolder {

        private static final ClassValue<OffsetDateTimeArrayType> CLASS_VALUE = FuncClassValue.create(OffsetDateTimeArrayType::new);

    }


}
