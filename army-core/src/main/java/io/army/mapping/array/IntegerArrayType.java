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
import io.army.mapping.IntegerType;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.sqltype.SQLType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;
import io.army.util._Assert;

import java.util.Objects;

public class IntegerArrayType extends _ArmyBuildInArrayType {


    public static IntegerArrayType from(final Class<?> javaType) {
        final IntegerArrayType instance;
        final Class<?> componentType;
        if (javaType == Integer[].class) {
            instance = LINEAR;
        } else if (javaType == int[].class) {
            instance = PRIMITIVE_LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(IntegerArrayType.class, javaType);
        } else if ((componentType = ArrayUtils.underlyingComponent(javaType)) == int.class
                || componentType == Integer.class) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(IntegerArrayType.class, javaType);
        }
        return instance;
    }

    public static final IntegerArrayType UNLIMITED = new IntegerArrayType(Object.class, Integer.class);

    public static final IntegerArrayType LINEAR = new IntegerArrayType(Integer[].class);

    public static final IntegerArrayType PRIMITIVE_LINEAR = new IntegerArrayType(int[].class);

    private final Class<?> javaType;

    private final Class<?> underlyingJavaType;


    /// private constructor
    private IntegerArrayType(final Class<?> javaType) {
        this.javaType = javaType;
        this.underlyingJavaType = ArrayUtils.underlyingComponent(javaType);
    }

    /// private constructor
    private IntegerArrayType(Class<Object> javaType, Class<?> underlyingJavaType) {
        _Assert.isTrue(underlyingJavaType == Integer.class, "");
        this.javaType = javaType;
        this.underlyingJavaType = underlyingJavaType;
    }


    @Override
    public Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        return mapToDataType(this, meta);
    }


    @Override
    public Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, IntegerArrayType::appendToText, dataType, this);
    }

    @Override
    public Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, IntegerArrayType::parseText, null);
    }

    @Override
    public Class<?> underlyingJavaType() {
        return this.underlyingJavaType;
    }


    @Override
    public MappingType elementType() {
        final Class<?> javaType = this.javaType;
        final MappingType instance;
        if (javaType == Object.class) {
            instance = this;
        } else if (javaType == Integer[].class || javaType == int[].class) {
            instance = IntegerType.INSTANCE;
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
    public MappingType underlyingType() {
        return IntegerType.INSTANCE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.javaType, this.underlyingJavaType);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof IntegerArrayType o) {
            match = o.javaType == this.javaType
                    && o.underlyingJavaType == this.underlyingJavaType;
        } else {
            match = false;
        }
        return match;
    }


    /*-------------------below static methods -------------------*/

    static DataType mapToDataType(final MappingType type, final ServerMeta meta) {
        final SQLType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = PgType.INTEGER_ARRAY;
                break;
            case Oracle:
            case H2:
            case MySQL:
            default:
                throw mapError(type, meta);
        }
        return dataType;
    }


    private static int parseText(final String text, final int offset, final int end) {
        return Integer.parseInt(text.substring(offset, end));
    }

    private static void appendToText(final Object element, final StringBuilder appender) {
        if (!(element instanceof Integer)) {
            // no bug,never here
            throw new IllegalArgumentException();
        }
        appender.append(element);
    }

    private static final class ClassValueHolder {

        private static final ClassValue<IntegerArrayType> CLASS_VALUE = FuncClassValue.create(IntegerArrayType::new);

    }


}
