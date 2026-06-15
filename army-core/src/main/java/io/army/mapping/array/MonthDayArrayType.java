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
import io.army.mapping.MonthDayType;
import io.army.mapping._ArmyBuildInArrayType; 
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Objects;

public class MonthDayArrayType extends _ArmyBuildInArrayType {


    public static MonthDayArrayType from(final Class<?> javaType) {
        final MonthDayArrayType instance;
        if (javaType == MonthDay[].class) {
            instance = LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(MonthDayArrayType.class, javaType);
        } else if (ArrayUtils.underlyingComponent(javaType) == MonthDay.class) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(MonthDayArrayType.class, javaType);
        }
        return instance;
    }


    public static final MonthDayArrayType LINEAR = new MonthDayArrayType(MonthDay[].class);

    private final Class<?> javaType;

    /// private constructor
    private MonthDayArrayType(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        return LocalDateArrayType.mapToSqlType(this, meta);
    }

    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, MonthDayArrayType::appendToText, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, MonthDayArrayType::parseText, null);
    }

    @Override
    public final Class<?> underlyingJavaType() {
        return MonthDay.class;
    }

    @Override
    public final MappingType underlyingType() {
        return MonthDayType.INSTANCE;
    }

    @Override
    public final MappingType elementType() {
        final MappingType instance;
        final Class<?> javaType = this.javaType;
        if (javaType == MonthDay[].class) {
            instance = MonthDayType.INSTANCE;
        } else {
            instance = from(javaType.getComponentType());
        }
        return instance;
    }

    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        return from(ArrayUtils.arrayClassOf(this.javaType));
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
        } else if (obj instanceof MonthDayArrayType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }


    /*-------------------below static methods -------------------*/


    private static MonthDay parseText(final String text, final int offset, final int end) {
        final String timeStr;
        timeStr = text.substring(offset, end);

        final MonthDay value;
        if (timeStr.length() == 5) {
            value = MonthDay.parse(timeStr);
        } else {
            value = MonthDay.from(LocalDate.parse(timeStr));
        }
        return value;
    }

    private static void appendToText(final Object element, final StringBuilder appender) {
        if (!(element instanceof MonthDay)) {
            // no bug,never here
            throw new IllegalArgumentException();
        }

        appender.append(_Constant.DOUBLE_QUOTE);
        appender.append("1970-");
        appender.append(element);
        appender.append(_Constant.DOUBLE_QUOTE);

    }

    private static final class ClassValueHolder {

        private static final ClassValue<MonthDayArrayType> CLASS_VALUE = FuncClassValue.create(MonthDayArrayType::new);

    }


}
