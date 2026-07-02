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
import io.army.lang.Nullable;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.SQLType;
import io.army.struct.CodeEnum;
import io.army.struct.DefinedType;
import io.army.struct.LabelEnum;
import io.army.util.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

///
/// This class representing the mapping from {@link LabelEnum} to {@link SQLType}.
///
/// @see LabelEnum
/// @see NameEnumType
/// @see CodeEnumType
public final class LabelEnumType extends _ArmyBuildInCoreType implements MappingType.SqlEnum {

    public static LabelEnumType from(final Class<?> javaType) {
        return CLASS_VALUE.get(checkEnumClass(LabelEnumType.class, javaType));
    }

    public static LabelEnumType fromParam(final Class<?> javaType, final String enumName) {
        final Class<?> enumClass;
        enumClass = checkEnumClass(LabelEnumType.class, javaType);

        if (enumClass.getAnnotation(DefinedType.class) != null) {
            throw errorJavaType(NameEnumType.class, enumClass);
        }
        _Assert.assertTypeName(enumName);
        return new LabelEnumType(enumClass, enumName);
    }


    private static final ClassValue<LabelEnumType> CLASS_VALUE = FuncClassValue.create(LabelEnumType::new);


    private final Class<?> enumClass;

    private final String typeName;

    private final Map<String, LabelEnum> textEnumMap;

    private DataType dataType;

    /// private constructor
    private LabelEnumType(final Class<?> javaType) {
        this.enumClass = ClassUtils.enumClass(javaType);
        this.typeName = AnnotationUtils.definedTypeNameOf(this.enumClass);
        this.textEnumMap = Map.copyOf(createTextMap(this.enumClass));
    }

    /// private constructor
    private LabelEnumType(final Class<?> javaType, String typeName) {
        this.enumClass = ClassUtils.enumClass(javaType);
        this.typeName = typeName;
        this.textEnumMap = Map.copyOf(createTextMap(this.enumClass));
    }


    @Override
    public Class<?> javaType() {
        return this.enumClass;
    }

    @Override
    public DataType map(final ServerMeta meta) {
        return NameEnumType.mapToDataType(this, meta, this::tryDataType);
    }

    @Override
    public String beforeBind(DataType dataType, MappingEnv env, final Object source) {
        if (!this.enumClass.isInstance(source)) {
            throw paramError(this, dataType, source, null);
        }
        return ((LabelEnum) source).label();
    }

    @Override
    public LabelEnum afterGet(DataType dataType, MappingEnv env, Object source) {
        if (this.enumClass.isInstance(source)) {
            return ((LabelEnum) source);
        }
        final LabelEnum value;
        if (!(source instanceof String) || (value = this.textEnumMap.get(source)) == null) {
            throw dataAccessError(this, dataType, source, null);
        }
        return value;
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        final String typeName = this.typeName;
        final MappingType instance;
        if (typeName != null && this.enumClass.getAnnotation(DefinedType.class) == null) {
            instance = ArrayFactoryFuncHolder.PARAM_FUNC.apply(ArrayUtils.arrayClassOf(this.enumClass), typeName);
        } else {
            instance = ArrayFactoryFuncHolder.FUNCTION.apply(ArrayUtils.arrayClassOf(this.enumClass));
        }
        return instance;
    }


    @Override
    public List<String> enumLabelList() {
        return ClassUtils.textEnumLabelList(this.enumClass);
    }

    @SuppressWarnings("unused")
    public Map<String, LabelEnum> getTextEnumMap() {
        return this.textEnumMap;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.enumClass, this.typeName);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof LabelEnumType o) {
            match = o.enumClass == this.enumClass
                    && Objects.equals(o.typeName, this.typeName);
        } else {
            match = false;
        }
        return match;
    }

    @Nullable
    private DataType tryDataType(ServerMeta meta) {
        DataType dataType = this.dataType;
        if (dataType == null) {
            this.dataType = dataType = NameEnumType.tryCreateDataType(this.typeName, this.enumClass, meta);
        }
        return dataType;
    }

    /// @return an unmodifiable map
    public static Map<String, LabelEnum> createTextMap(final Class<?> javaType)
            throws IllegalArgumentException {
        if (!javaType.isEnum() || !LabelEnum.class.isAssignableFrom(javaType)) {
            String m = String.format("%s isn't %s enum.", javaType.getName(), LabelEnum.class.getName());
            throw new IllegalArgumentException(m);
        }

        final Enum<?>[] values = ClassUtils.enumConstantsOf(javaType);
        final Map<String, LabelEnum> map = _Collections.hashMapForSize(values.length);
        LabelEnum labelEnum;
        for (Enum<?> value : values) {
            labelEnum = (LabelEnum) value;
            if (map.putIfAbsent(labelEnum.label(), labelEnum) != null) {
                String m;
                m = String.format("%s.%s text[%s] duplicate", javaType.getName(), labelEnum.name(), labelEnum.label());
                throw new IllegalArgumentException(m);
            }
        }
        return Map.copyOf(map);
    }


    public static Class<?> checkEnumClass(Class<? extends MappingType> typeClass, final Class<?> javaType) {
        if (!Enum.class.isAssignableFrom(javaType)
                || !LabelEnum.class.isAssignableFrom(javaType)
                || CodeEnum.class.isAssignableFrom(javaType)) {
            throw errorJavaType(typeClass, javaType);
        }
        return ClassUtils.enumClass(javaType);
    }

    private static class ArrayFactoryFuncHolder {

        private static final Function<Class<?>, MappingType> FUNCTION;

        private static final BiFunction<Class<?>, String, MappingType> PARAM_FUNC;

        static {
            FUNCTION = _ArmyBuildInCoreType.removeArrayFromFunc(LabelEnumType.class);
            PARAM_FUNC = _ArmyBuildInCoreType.removeArrayFromParamFunc(LabelEnumType.class);
        }

    } // ArrayFactoryFuncHolder


}
