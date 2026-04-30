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
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 * This class representing the mapping from {@link TextEnum} to {@link SQLType}.
 *
 * @see TextEnum
 * @see NameEnumType
 * @see CodeEnumType
 */
public class TextEnumType extends _ArmyBuildInType {

    public static TextEnumType from(final Class<?> enumType) {
        final Class<?> actualEnumType;
        actualEnumType = checkEnumClass(enumType);

        final TextEnumType instance;
        if (actualEnumType.getAnnotation(DefinedType.class) == null) {
            instance = INSTANCE_MAP.computeIfAbsent(actualEnumType, TextEnumType::new);
        } else {
            instance = INSTANCE_MAP.computeIfAbsent(actualEnumType, TextEnumType::createDefinedType);
        }
        return instance;
    }

    public static TextEnumType fromParam(final Class<?> enumType, final String enumName) {
        _Assert.hasText(enumName, "no text");

        final Class<?> actualEnumType;
        actualEnumType = checkEnumClass(enumType);
        if (actualEnumType.getAnnotation(DefinedType.class) != null) {
            throw new IllegalArgumentException("error enum");
        }

        TextEnumType instance;
        instance = INSTANCE_MAP.computeIfAbsent(actualEnumType, type -> new TextEnumNamedType(type, enumName));
        if (!(instance instanceof TextEnumNamedType o) || !enumName.equals(o.enumName)) {
            instance = new TextEnumNamedType(actualEnumType, enumName);
        }
        return instance;
    }

    /// @param enumType The enum that is annotated with {@link DefinedType}.
    private static TextEnumNamedType createDefinedType(final Class<?> enumType) {
        final String typeName;
        typeName = AnnotationUtils.getDefinedTypeName(enumType);
        if (typeName == null) {
            // no bug,never here
            throw new IllegalArgumentException();
        }
        return new TextEnumNamedType(enumType, typeName);
    }

    private static Class<?> checkEnumClass(final Class<?> javaType) {
        if (!Enum.class.isAssignableFrom(javaType)
                || !TextEnum.class.isAssignableFrom(javaType)
                || CodeEnum.class.isAssignableFrom(javaType)) {
            throw errorJavaType(TextEnumType.class, javaType);
        }
        return ClassUtils.enumClass(javaType);
    }

    private static final ConcurrentMap<Class<?>, TextEnumType> INSTANCE_MAP = _Collections.concurrentHashMap();


    private final Class<?> enumClass;

    private final Map<String, ? extends TextEnum> textMap;

    /**
     * private constructor
     */
    private TextEnumType(final Class<?> enumClass) {
        this.enumClass = enumClass;
        this.textMap = TextEnum.getTextToEnumMap(enumClass);
    }

    @Override
    public final Class<?> javaType() {
        return this.enumClass;
    }

    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        final MappingType arrayType;
        if (this.enumClass.getAnnotation(DefinedType.class) == null && this instanceof TextEnumNamedType o) {
            arrayType = TextEnumArrayType.fromParam(ArrayUtils.arrayClassOf(this.enumClass), o.enumName);
        } else {
            arrayType = TextEnumArrayType.from(ArrayUtils.arrayClassOf(this.enumClass));
        }
        return arrayType;
    }

    @Override
    public final DataType map(final ServerMeta meta) {
        return NameEnumType.mapToDataType(this, meta);
    }

    @Override
    public final String beforeBind(DataType dataType, MappingEnv env, final Object source) {
        if (!this.enumClass.isInstance(source)) {
            throw paramError(this, dataType, source, null);
        }
        return ((TextEnum) source).text();
    }

    @Override
    public final TextEnum afterGet(DataType dataType, MappingEnv env, Object source) {
        if (this.enumClass.isInstance(source)) {
            return ((TextEnum) source);
        }
        final TextEnum value;
        if (!(source instanceof String) || (value = this.textMap.get(source)) == null) {
            throw dataAccessError(this, dataType, source, null);
        }
        return value;
    }


    @Override
    public final int hashCode() {
        final int hash;
        if (this instanceof TextEnumNamedType o) {
            hash = Objects.hash(this.enumClass, o.enumName);
        } else {
            hash = Objects.hash(this.enumClass);
        }
        return hash;
    }

    @Override
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof TextEnumNamedType o) {
            if (this instanceof TextEnumNamedType t) {
                match = ((TextEnumType) o).enumClass == this.enumClass
                        && o.enumName.equals(t.enumName);
            } else {
                match = false;
            }
        } else if (obj instanceof TextEnumType o) {
            match = !(this instanceof TextEnumNamedType) && o.enumClass == this.enumClass;
        } else {
            match = false;
        }
        return match;
    }


    private static final class TextEnumNamedType extends TextEnumType implements MappingType.SqlEnum {

        private final String enumName;

        private TextEnumNamedType(Class<?> enumClass, String enumName) {
            super(enumClass);
            this.enumName = enumName;
        }

        @Override
        public String objectName() {
            return this.enumName;
        }

        @Override
        public List<String> enumLabelList() {
            @SuppressWarnings("unchecked")
            Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) ((TextEnumType) this).enumClass;
            final Enum<?>[] enumArray = enumClass.getEnumConstants();
            final List<String> enumLabelList = new ArrayList<>(enumArray.length);
            for (Enum<?> enumConstant : enumArray) {
                enumLabelList.add(((TextEnum) enumConstant).text());
            }
            return List.copyOf(enumLabelList);
        }

        @Override
        public String comment() {
            return "";
        }
    } // TextEnumNamedType

}
