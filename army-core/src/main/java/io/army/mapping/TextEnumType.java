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

package io.army.mapping;

import io.army.criteria.CriteriaException;
import io.army.mapping.array.TextEnumArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.SQLType;
import io.army.struct.CodeEnum;
import io.army.struct.DefinedType;
import io.army.struct.TextEnum;
import io.army.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

///
/// This class representing the mapping from {@link TextEnum} to {@link SQLType}.
///
/// @see TextEnum
/// @see NameEnumType
/// @see CodeEnumType
public final class TextEnumType extends _ArmyBuildInType implements MappingType.SqlEnum {

    public static TextEnumType from(final Class<?> javaType) {
        return CLASS_VALUE.get(checkEnumClass(TextEnumType.class, javaType));
    }

    public static TextEnumType fromParam(final Class<?> javaType, final String enumName) {
        final Class<?> enumClass;
        enumClass = checkEnumClass(TextEnumType.class, javaType);

        if (enumClass.getAnnotation(DefinedType.class) != null) {
            throw errorJavaType(NameEnumType.class, enumClass);
        }
        _Assert.assertTypeName(enumName);
        return new TextEnumType(enumClass, DataType.from(enumName));
    }


    private static final ClassValue<TextEnumType> CLASS_VALUE = FuncClassValue.create(TextEnumType::new);


    private final Class<?> enumClass;

    private final Map<String, TextEnum> textEnumMap;

    private final DataType dataType;

    /// private constructor
    private TextEnumType(final Class<?> javaType) {
        this.enumClass = ClassUtils.enumClass(javaType);
        this.textEnumMap = Map.copyOf(createTextMap(this.enumClass));
        this.dataType = AnnotationUtils.dataTypeOf(this.enumClass, false);
    }

    /// private constructor
    private TextEnumType(final Class<?> javaType, DataType dataType) {
        this.enumClass = ClassUtils.enumClass(javaType);
        this.textEnumMap = Map.copyOf(createTextMap(this.enumClass));
        this.dataType = dataType;
    }


    @Override
    public Class<?> javaType() {
        return this.enumClass;
    }

    @Override
    public DataType map(final ServerMeta meta) {
        return NameEnumType.mapToDataType(this, meta);
    }

    @Override
    public String beforeBind(DataType dataType, MappingEnv env, final Object source) {
        if (!this.enumClass.isInstance(source)) {
            throw paramError(this, dataType, source, null);
        }
        return ((TextEnum) source).text();
    }

    @Override
    public TextEnum afterGet(DataType dataType, MappingEnv env, Object source) {
        if (this.enumClass.isInstance(source)) {
            return ((TextEnum) source);
        }
        final TextEnum value;
        if (!(source instanceof String) || (value = this.textEnumMap.get(source)) == null) {
            throw dataAccessError(this, dataType, source, null);
        }
        return value;
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        final DataType dataType = this.dataType;
        final MappingType instance;
        if (dataType != null && this.enumClass.getAnnotation(DefinedType.class) == null) {
            instance = TextEnumArrayType.fromParam(ArrayUtils.arrayClassOf(this.enumClass), dataType.typeName());
        } else {
            instance = TextEnumArrayType.from(ArrayUtils.arrayClassOf(this.enumClass));
        }
        return instance;
    }

    @Override
    public DataType dataType() {
        return this.dataType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> enumLabelList() {
        Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) this.enumClass;
        final Enum<?>[] enumArray = enumClass.getEnumConstants();
        final List<String> enumLabelList = new ArrayList<>(enumArray.length);
        for (Enum<?> enumConstant : enumArray) {
            enumLabelList.add(((TextEnum) enumConstant).text());
        }
        return List.copyOf(enumLabelList);
    }

    @SuppressWarnings("unused")
    public Map<String, TextEnum> getTextEnumMap() {
        return this.textEnumMap;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.enumClass, this.dataType);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof TextEnumType o) {
            match = o.enumClass == this.enumClass
                    && Objects.equals(o.dataType, this.dataType);
        } else {
            match = false;
        }
        return match;
    }

    /// @return an unmodifiable map
    public static Map<String, TextEnum> createTextMap(final Class<?> javaType)
            throws IllegalArgumentException {
        if (!javaType.isEnum() || !TextEnum.class.isAssignableFrom(javaType)) {
            String m = String.format("%s isn't %s enum.", javaType.getName(), TextEnum.class.getName());
            throw new IllegalArgumentException(m);
        }

        final Enum<?>[] values = ClassUtils.enumConstantsOf(javaType);
        final Map<String, TextEnum> map = _Collections.hashMapForSize(values.length);
        TextEnum textEnum;
        for (Enum<?> value : values) {
            textEnum = (TextEnum) value;
            if (map.putIfAbsent(textEnum.text(), textEnum) != null) {
                String m;
                m = String.format("%s.%s text[%s] duplicate", javaType.getName(), textEnum.name(), textEnum.text());
                throw new IllegalArgumentException(m);
            }
        }
        return Map.copyOf(map);
    }


    public static Class<?> checkEnumClass(Class<? extends MappingType> typeClass, final Class<?> javaType) {
        if (!Enum.class.isAssignableFrom(javaType)
                || !TextEnum.class.isAssignableFrom(javaType)
                || CodeEnum.class.isAssignableFrom(javaType)) {
            throw errorJavaType(typeClass, javaType);
        }
        return ClassUtils.enumClass(javaType);
    }


}
