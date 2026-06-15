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
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping.ZonedDateTimeType;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;
import io.army.util._TimeUtils;

import java.time.ZonedDateTime;

public class ZonedDateTimeArrayType extends _ArmyBuildInArrayType {


    public static ZonedDateTimeArrayType from(final Class<?> javaType) {
        final ZonedDateTimeArrayType instance;
        if (javaType == ZonedDateTime[].class) {
            instance = LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(ZonedDateTimeArrayType.class, javaType);
        } else if (ArrayUtils.underlyingComponent(javaType) == ZonedDateTime.class) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(ZonedDateTimeArrayType.class, javaType);
        }
        return instance;
    }

    public static final ZonedDateTimeArrayType LINEAR = new ZonedDateTimeArrayType(ZonedDateTime[].class);


    private final Class<?> javaType;

    /// private constructor
    private ZonedDateTimeArrayType(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }


    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        return OffsetDateTimeArrayType.mapToDataType(this, meta);
    }

    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, ZonedDateTimeArrayType::appendToText, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, ZonedDateTimeArrayType::parseText, null);
    }


    @Override
    public final Class<?> underlyingJavaType() {
        return ZonedDateTime.class;
    }

    @Override
    public final MappingType underlyingType() {
        return ZonedDateTimeType.INSTANCE;
    }

    @Override
    public final MappingType elementType() {
        final MappingType instance;
        final Class<?> javaType = this.javaType;
        if (javaType == ZonedDateTime[].class) {
            instance = ZonedDateTimeType.INSTANCE;
        } else {
            instance = from(javaType.getComponentType());
        }
        return instance;
    }

    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        return from(ArrayUtils.arrayClassOf(this.javaType));
    }


    /*-------------------below static methods -------------------*/

    private static ZonedDateTime parseText(final String text, final int offset, final int end) {
        return ZonedDateTime.parse(text.substring(offset, end), _TimeUtils.OFFSET_DATETIME_FORMATTER_6);
    }

    private static void appendToText(final Object element, final StringBuilder appender) {
        if (!(element instanceof ZonedDateTime)) {
            // no bug,never here
            throw new IllegalArgumentException();
        }

        appender.append(_Constant.DOUBLE_QUOTE);
        appender.append(((ZonedDateTime) element).format(_TimeUtils.OFFSET_DATETIME_FORMATTER_6));
        appender.append(_Constant.DOUBLE_QUOTE);

    }

    private static final class ClassValueHolder {

        private static final ClassValue<ZonedDateTimeArrayType> CLASS_VALUE = FuncClassValue.create(ZonedDateTimeArrayType::new);

    }


}
