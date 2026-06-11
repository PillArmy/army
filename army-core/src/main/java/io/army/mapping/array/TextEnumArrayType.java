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
import io.army.dialect._DialectUtils;
import io.army.executor.DataAccessException;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping.TextEnumType;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.struct.DefinedType;
import io.army.struct.TextEnum;
import io.army.util.*;

import java.util.Map;
import java.util.Objects;

public class TextEnumArrayType extends _ArmyBuildInArrayType {

    public static TextEnumArrayType from(final Class<?> javaType) {
        checkJavaType(javaType);

        return CLASS_VALUE.get(javaType);
    }

    public static TextEnumArrayType fromParam(final Class<?> javaType, String enumTypeName) {
        final Class<?> enumClass;
        enumClass = checkJavaType(javaType);
        if (enumClass.getAnnotation(DefinedType.class) != null) {
            throw errorJavaType(TextEnumArrayType.class, javaType);
        }
        _Assert.assertTypeName(enumTypeName);
        return new TextEnumArrayType(javaType, DataType.from(enumTypeName + "[]"));
    }

    private static final ClassValue<TextEnumArrayType> CLASS_VALUE = FuncClassValue.create(TextEnumArrayType::new);

    private final Class<?> javaType;

    private final Class<?> enumClass;

    private final Map<String, TextEnum> textMap;

    private final DataType dataType;

    /// private constructor
    private TextEnumArrayType(final Class<?> javaType) {
        this.javaType = javaType;
        this.enumClass = ClassUtils.enumClass(ArrayUtils.underlyingComponent(javaType));
        this.textMap = Map.copyOf(TextEnumType.createTextMap(this.enumClass));
        this.dataType = AnnotationUtils.dataTypeOf(this.enumClass, true);
    }

    /// private constructor
    private TextEnumArrayType(final Class<?> javaType, DataType dataType) {
        this.javaType = javaType;
        this.enumClass = ClassUtils.enumClass(ArrayUtils.underlyingComponent(javaType));
        this.textMap = Map.copyOf(TextEnumType.createTextMap(this.enumClass));
        this.dataType = dataType;
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        DataType dataType = this.dataType;
        if (dataType == null) {
            dataType = StringArrayType.mapToSqlType(this, meta);
        }
        return dataType;
    }

    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, this::appendToText, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, false, this::parseText);
    }

    @Override
    public final Class<?> underlyingJavaType() {
        return this.enumClass;
    }

    @Override
    public MappingType underlyingType() {
        final MappingType instance;
        final DataType dataType = this.dataType;
        if (dataType != null && this.enumClass.getAnnotation(DefinedType.class) == null) {
            instance = TextEnumType.fromParam(this.enumClass, _DialectUtils.obtainElementType(dataType).typeName());
        } else {
            instance = TextEnumType.from(this.enumClass);
        }
        return instance;
    }

    @Override
    public final MappingType elementType() {
        final Class<?> componentType;
        final DataType dataType = this.dataType;

        final MappingType instance;
        if (!(componentType = this.javaType.getComponentType()).isArray()) {
            instance = underlyingType();
        } else if (dataType != null && this.enumClass.getAnnotation(DefinedType.class) == null) {
            instance = fromParam(componentType, _DialectUtils.obtainElementType(dataType).typeName());
        } else {
            instance = from(componentType);
        }
        return instance;
    }

    @SuppressWarnings("unused")
    public Map<String, TextEnum> getTextMap() {
        return this.textMap;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.javaType, this.enumClass, this.dataType);
    }

    @Override
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof TextEnumArrayType o) {
            match = o.javaType == this.javaType
                    && o.enumClass == this.enumClass
                    && Objects.equals(o.dataType, this.dataType)
            ;
        } else {
            match = false;
        }
        return match;
    }


    private TextEnum parseText(final String text, final int offset, final int end) {
        final String textValue = text.substring(offset, end);
        final TextEnum value;
        value = this.textMap.get(textValue);
        if (value == null) {
            String m = String.format("%s isn't element of %s", textValue, this.enumClass.getName());
            throw new IllegalArgumentException(m);
        }
        return value;
    }

    private void appendToText(final Object element, final StringBuilder appender) {
        if (!this.enumClass.isInstance(element)) {
            // no bug,never here
            throw new IllegalArgumentException();
        }
        final String textValue = ((TextEnum) element).text();
        PostgreArrays.encodeElement(textValue, appender);
    }


    /// @see #from(Class)
    /// @see #fromParam(Class, String)
    private static Class<?> checkJavaType(final Class<?> javaType) {
        if (!javaType.isArray()) {
            throw errorJavaType(TextEnumArrayType.class, javaType);
        }
        final Class<?> enumClass;
        enumClass = ArrayUtils.underlyingComponent(javaType);

        if (!Enum.class.isAssignableFrom(enumClass)
                || !TextEnum.class.isAssignableFrom(enumClass)) {
            throw errorJavaType(TextEnumArrayType.class, javaType);
        }
        return ClassUtils.enumClass(enumClass);
    }


}
