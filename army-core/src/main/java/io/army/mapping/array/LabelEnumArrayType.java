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
import io.army.mapping.LabelEnumType;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.struct.DefinedType;
import io.army.struct.LabelEnum;
import io.army.util.*;

import java.util.Map;
import java.util.Objects;

/// @see LabelEnumType
public class LabelEnumArrayType extends _ArmyBuildInArrayType {

    public static LabelEnumArrayType from(final Class<?> javaType) {
        checkJavaType(javaType);

        return CLASS_VALUE.get(javaType);
    }

    public static LabelEnumArrayType fromParam(final Class<?> javaType, String enumTypeName) {
        final Class<?> enumClass;
        enumClass = checkJavaType(javaType);
        if (enumClass.getAnnotation(DefinedType.class) != null) {
            throw errorJavaType(LabelEnumArrayType.class, javaType);
        }
        _Assert.assertTypeName(enumTypeName);
        return new LabelEnumArrayType(javaType, enumTypeName);
    }

    private static final ClassValue<LabelEnumArrayType> CLASS_VALUE = FuncClassValue.create(LabelEnumArrayType::new);

    private final Class<?> javaType;

    private final Class<?> enumClass;

    private final Map<String, LabelEnum> textMap;

    private final DataType dataType;

    /// private constructor
    private LabelEnumArrayType(final Class<?> javaType) {
        this.javaType = javaType;
        this.enumClass = ClassUtils.enumClass(ArrayUtils.underlyingComponent(javaType));
        this.textMap = Map.copyOf(LabelEnumType.createTextMap(this.enumClass));

        final String typeName;
        typeName = AnnotationUtils.definedTypeNameOf(javaType);
        if (typeName == null) {
            this.dataType = null;
        } else {
            this.dataType = NameEnumArrayType.createDataType(typeName, javaType);
        }
    }

    /// private constructor
    private LabelEnumArrayType(final Class<?> javaType, String typeName) {
        this.javaType = javaType;
        this.enumClass = ClassUtils.enumClass(ArrayUtils.underlyingComponent(javaType));
        this.textMap = Map.copyOf(LabelEnumType.createTextMap(this.enumClass));
        this.dataType = NameEnumArrayType.createDataType(typeName, javaType);
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        DataType dataType = this.dataType;
        if (dataType == null) {
            dataType = PgType.VARCHAR_ARRAY;
        }
        return dataType;
    }

    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, this::appendToText, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, this::parseText, null, null, null);
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
            instance = LabelEnumType.fromParam(this.enumClass, dataType.componentTypeName());
        } else {
            instance = LabelEnumType.from(this.enumClass);
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
            instance = fromParam(componentType, dataType.componentTypeName());
        } else {
            instance = from(componentType);
        }
        return instance;
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        final DataType dataType = this.dataType;
        final MappingType instance;
        if (dataType != null && this.enumClass.getAnnotation(DefinedType.class) == null) {
            instance = fromParam(ArrayUtils.arrayClassOf(this.javaType), dataType.componentTypeName());
        } else {
            instance = from(ArrayUtils.arrayClassOf(this.javaType));
        }
        return instance;
    }


    @SuppressWarnings("unused")
    public Map<String, LabelEnum> getTextMap() {
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
        } else if (obj instanceof LabelEnumArrayType o) {
            match = o.javaType == this.javaType
                    && o.enumClass == this.enumClass
                    && Objects.equals(o.dataType, this.dataType)
            ;
        } else {
            match = false;
        }
        return match;
    }


    private LabelEnum parseText(final String text, final int offset, final int end) {
        final String textValue = text.substring(offset, end);
        final LabelEnum value;
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
        final String textValue = ((LabelEnum) element).label();
        PostgreArrays.encodeElement(textValue, appender);
    }


    /// @see #from(Class)
    /// @see #fromParam(Class, String)
    private static Class<?> checkJavaType(final Class<?> javaType) {
        if (!javaType.isArray()) {
            throw errorJavaType(LabelEnumArrayType.class, javaType);
        }
        final Class<?> enumClass;
        enumClass = ArrayUtils.underlyingComponent(javaType);

        if (!Enum.class.isAssignableFrom(enumClass)
                || !LabelEnum.class.isAssignableFrom(enumClass)) {
            throw errorJavaType(LabelEnumArrayType.class, javaType);
        }
        return ClassUtils.enumClass(enumClass);
    }


}
