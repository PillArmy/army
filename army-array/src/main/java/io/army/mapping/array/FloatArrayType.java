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
import io.army.mapping.FloatType;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;
import io.army.util._Assert;

import java.util.Objects;

public class FloatArrayType extends _ArmyCoreArrayType {


    public static FloatArrayType from(final Class<?> javaType) {
        final FloatArrayType instance;
        final Class<?> componentType;
        if (javaType == Float[].class) {
            instance = LINEAR;
        } else if (javaType == float[].class) {
            instance = PRIMITIVE_LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(FloatArrayType.class, javaType);
        } else if ((componentType = ArrayUtils.underlyingComponent(javaType)) == float.class
                || componentType == Float.class) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(FloatArrayType.class, javaType);
        }
        return instance;
    }


    public static final FloatArrayType UNLIMITED = new FloatArrayType(Object.class, Float.class);

    public static final FloatArrayType LINEAR = new FloatArrayType(Float[].class);

    public static final FloatArrayType PRIMITIVE_LINEAR = new FloatArrayType(float[].class);

    static {
        addArrayFromFunc(FloatArrayType.class, FloatArrayType::from);
    }


    private final Class<?> javaType;

    private final Class<?> underlyingJavaType;

    /// private constructor
    private FloatArrayType(Class<?> javaType) {
        this.javaType = javaType;
        this.underlyingJavaType = ArrayUtils.underlyingComponent(javaType);
    }

    /// private constructor
    private FloatArrayType(Class<Object> javaType, Class<?> underlyingJavaType) {
        _Assert.isTrue(underlyingJavaType == Float.class, "");
        this.javaType = javaType;
        this.underlyingJavaType = underlyingJavaType;
    }

    @Override
    public Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public DataType map(ServerMeta meta) throws UnsupportedDialectException {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = PgType.REAL_ARRAY;
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
    public Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, FloatArrayType::appendToText, dataType, this);
    }

    @Override
    public Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, FloatArrayType::parseText, null);
    }

    @Override
    public Class<?> underlyingJavaType() {
        return this.underlyingJavaType;
    }


    @Override
    public MappingType underlyingType() {
        return FloatType.INSTANCE;
    }

    @Override
    public MappingType elementType() {
        final MappingType instance;
        final Class<?> javaType = this.javaType;
        if (javaType == float[].class || javaType == Float[].class) {
            instance = FloatType.INSTANCE;
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
        return Objects.hash(this.javaType, this.underlyingJavaType);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof FloatArrayType o) {
            match = o.javaType == this.javaType
                    && o.underlyingJavaType == this.underlyingJavaType
            ;
        } else {
            match = false;
        }
        return match;
    }


    /*-------------------below static methods -------------------*/

    private static float parseText(final CharSequence text, final int offset, final int end) {
        return Float.parseFloat(text.subSequence(offset, end).toString());
    }

    private static void appendToText(final Object element, final StringBuilder appender) {
        if (!(element instanceof Float)) {
            // no bug,never here
            throw new IllegalArgumentException();
        }
        appender.append(element);
    }

    private static final class ClassValueHolder {

        private static final ClassValue<FloatArrayType> CLASS_VALUE = FuncClassValue.create(FloatArrayType::new);

    }


}
