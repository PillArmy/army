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
import io.army.dialect._Constant;
import io.army.executor.DataAccessException;
import io.army.mapping.LocalTimeType;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping._ArmyBuildInArrayType; 
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;
import io.army.util._TimeUtils;

import java.time.LocalTime;
import java.util.Objects;

public class LocalTimeArrayType extends _ArmyBuildInArrayType {


    public static LocalTimeArrayType from(final Class<?> javaType) {
        final LocalTimeArrayType instance;
        if (javaType == LocalTime[].class) {
            instance = LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(LocalTimeArrayType.class, javaType);
        } else if (ArrayUtils.underlyingComponent(javaType) == LocalTime.class) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(LocalTimeArrayType.class, javaType);
        }
        return instance;
    }


    public static final LocalTimeArrayType LINEAR = new LocalTimeArrayType(LocalTime[].class);

    public static final LocalTimeArrayType UNLIMITED = new LocalTimeArrayType(Object.class);

    private final Class<?> javaType;

    /// private constructor
    private LocalTimeArrayType(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = PgType.TIME_ARRAY;
                break;
            case MySQL:
            case SQLite:
            case H2:
            case Oracle:
            default:
                throw MAP_ERROR_HANDLER.apply(this, meta);
        }
        return dataType;
    }

    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, LocalTimeArrayType::appendToText, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, LocalTimeArrayType::parseText, null, null, null);
    }


    @Override
    public final Class<?> underlyingJavaType() {
        return LocalTime.class;
    }

    @Override
    public final MappingType underlyingType() {
        return LocalTimeType.INSTANCE;
    }

    @Override
    public final MappingType elementType() {
        final MappingType instance;
        final Class<?> javaType = this.javaType;
        if (javaType == Object.class) {
            instance = this;
        } else if (javaType == LocalTime[].class) {
            instance = LocalTimeType.INSTANCE;
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
        } else if (obj instanceof LocalTimeArrayType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }

    /*-------------------below static methods -------------------*/

    private static LocalTime parseText(final String text, final int offset, final int end) {
        return LocalTime.parse(text.substring(offset, end), _TimeUtils.TIME_FORMATTER_6);
    }

    private static void appendToText(final Object element, final StringBuilder appender) {
        if (!(element instanceof LocalTime)) {
            // no bug,never here
            throw new IllegalArgumentException();
        }

        appender.append(_Constant.DOUBLE_QUOTE);
        appender.append(((LocalTime) element).format(_TimeUtils.TIME_FORMATTER_6));
        appender.append(_Constant.DOUBLE_QUOTE);

    }

    private static final class ClassValueHolder {

        private static final ClassValue<LocalTimeArrayType> CLASS_VALUE = FuncClassValue.create(LocalTimeArrayType::new);

    }


}
