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
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping.UUIDType;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;
import io.army.util.ClassUtils;
import io.army.util.FuncClassValue;

import java.util.UUID;

public class UUIDArrayType extends _ArmyBuildInArrayType {


    public static UUIDArrayType from(final Class<?> javaType) {
        final Class<?> clazz;
        final UUIDArrayType instance;
        if (javaType == UUID[].class) {
            instance = LINEAR;
        } else if (javaType.isArray()
                && ((clazz = ArrayUtils.underlyingComponent(javaType)) == UUID.class || clazz == String.class)) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(UUIDArrayType.class, javaType);
        }
        return instance;
    }


    public static final UUIDArrayType UNLIMITED = new UUIDArrayType(Object.class);

    public static final UUIDArrayType LINEAR = new UUIDArrayType(UUID[].class);


    private final Class<?> javaType;

    private final Class<?> underlyingJavaType;

    private UUIDArrayType(Class<?> javaType) {
        this.javaType = javaType;
        if (javaType == Object.class) {
            this.underlyingJavaType = UUID.class;
        } else {
            this.underlyingJavaType = ArrayUtils.underlyingComponent(javaType);
        }

    }


    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        return switch (meta.serverDatabase()) {
            case PostgreSQL -> PgType.UUID_ARRAY;
            default -> throw MAP_ERROR_HANDLER.apply(this, meta);
        };
    }


    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, this::appendToText, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, this::parseText, null);
    }

    @Override
    public final Class<?> underlyingJavaType() {
        return this.underlyingJavaType;
    }

    @Override
    public final MappingType underlyingType() {
        final MappingType instance;
        if (this.underlyingJavaType == String.class) {
            instance = UUIDType.TEXT;
        } else {
            instance = UUIDType.INSTANCE;
        }
        return instance;
    }

    @Override
    public final MappingType elementType() {
        final Class<?> javaType = this.javaType;
        final MappingType instance;
        if (javaType == Object.class) {
            instance = this;
        } else if (javaType == UUID[].class) {
            instance = UUIDType.INSTANCE;
        } else if (javaType == String[].class) {
            instance = UUIDType.TEXT;
        } else {
            instance = from(javaType.getComponentType());
        }
        return instance;
    }

    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        final Class<?> javaType = this.javaType;
        if (javaType == Object.class) {
            return this;
        }
        return from(ArrayUtils.arrayClassOf(javaType));
    }

    @Override
    public final int hashCode() {
        return this.javaType.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof UUIDArrayType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }


    private Object parseText(final String text, final int offset, final int end) {
        final Object element;
        if (this.underlyingJavaType == String.class) {
            element = text.substring(offset, end);
        } else {
            element = UUID.fromString(text.substring(offset, end));
        }
        return element;

    }

    private void appendToText(final Object element, final StringBuilder appender) {
        final String text;
        if (element instanceof UUID) {
            text = element.toString();
        } else if (element instanceof String) {
            text = (String) element;
        } else {
            throw new IllegalArgumentException(String.format("%s unsupported", ClassUtils.safeClassName(element)));
        }
        PostgreArrays.encodeElement(text, appender);
    }

    private static final class ClassValueHolder {

        private static final ClassValue<UUIDArrayType> CLASS_VALUE = FuncClassValue.create(UUIDArrayType::new);

    }


}