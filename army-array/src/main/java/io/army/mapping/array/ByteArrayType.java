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
import io.army.mapping.ByteType;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;
import io.army.util._Assert;

import java.util.Objects;

public class ByteArrayType extends _ArmyCoreArrayType {


    public static ByteArrayType from(final Class<?> javaType) {
        final ByteArrayType instance;
        final Class<?> componentType;
        if (javaType == Byte[].class) {
            instance = LINEAR;
        } else if (javaType == byte[].class) {
            instance = PRIMITIVE_LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(ByteArrayType.class, javaType);
        } else if ((componentType = ArrayUtils.underlyingComponent(javaType)) == byte.class
                || componentType == Byte.class) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(ByteArrayType.class, javaType);
        }
        return instance;
    }

    public static final ByteArrayType UNLIMITED = new ByteArrayType(Object.class, Byte.class);

    public static final ByteArrayType LINEAR = new ByteArrayType(Byte[].class);

    public static final ByteArrayType PRIMITIVE_LINEAR = new ByteArrayType(byte[].class);

    static {
        addArrayFromFunc(ByteArrayType.class, ByteArrayType::from);
    }


    private final Class<?> javaType;

    private final Class<?> underlyingJavaType;

    /// private constructor
    private ByteArrayType(final Class<?> javaType) {
        this.javaType = javaType;
        this.underlyingJavaType = ArrayUtils.underlyingComponent(javaType);
    }

    /// private constructor
    private ByteArrayType(final Class<Object> javaType, Class<?> underlyingJavaType) {
        _Assert.isTrue(underlyingJavaType == Byte.class, "");
        this.javaType = javaType;
        this.underlyingJavaType = underlyingJavaType;
    }


    @Override
    public Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public DataType map(ServerMeta meta) throws UnsupportedDialectException {
        // currently,same
        return ShortArrayType.mapToDataType(this, meta);
    }

    @Override
    public Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, ByteArrayType::appendToText, dataType, this);
    }

    @Override
    public Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, ByteArrayType::parseText, null);
    }


    @Override
    public Class<?> underlyingJavaType() {
        return this.underlyingJavaType;
    }


    @Override
    public MappingType underlyingType() {
        return ByteType.INSTANCE;
    }

    @Override
    public MappingType elementType() {
        final Class<?> javaType = this.javaType;
        final MappingType instance;
        if (javaType == Object.class) {
            instance = this;
        } else if (javaType == Byte[].class || javaType == byte[].class) {
            instance = ByteType.INSTANCE;
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
        } else if (obj instanceof ByteArrayType o) {
            match = o.javaType == this.javaType
                    && o.underlyingJavaType == this.underlyingJavaType
            ;
        } else {
            match = false;
        }
        return match;
    }


    private static byte parseText(final String text, final int offset, final int end) {
        return Byte.parseByte(text.substring(offset, end));
    }

    private static void appendToText(final Object element, final StringBuilder appender) {
        if (!(element instanceof Byte)) {
            // no bug,never here
            throw new IllegalArgumentException();
        }
        appender.append(element);
    }

    private static final class ClassValueHolder {

        private static final ClassValue<ByteArrayType> CLASS_VALUE = FuncClassValue.create(ByteArrayType::new);

    }


}
