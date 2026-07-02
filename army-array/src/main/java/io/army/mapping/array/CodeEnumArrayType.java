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
import io.army.mapping.CodeEnumType;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.struct.CodeEnum;
import io.army.struct.LabelEnum;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.util.Map;
import java.util.Objects;

/// This class is mapping class of {@link CodeEnum}.
///
/// @see CodeEnumType
public class CodeEnumArrayType extends _ArmyCoreArrayType {


    public static CodeEnumArrayType from(final Class<?> javaType) {
        if (!javaType.isArray()) {
            throw errorJavaType(CodeEnumArrayType.class, javaType);
        }
        final Class<?> enumClass;
        enumClass = ArrayUtils.underlyingComponent(javaType);

        if (!(Enum.class.isAssignableFrom(enumClass) && CodeEnum.class.isAssignableFrom(enumClass))) {
            throw errorJavaType(CodeEnumArrayType.class, javaType);
        } else if (LabelEnum.class.isAssignableFrom(enumClass)) {
            throw errorJavaType(CodeEnumArrayType.class, enumClass);
        }
        return CLASS_VALUE.get(javaType);
    }


    private static final ClassValue<CodeEnumArrayType> CLASS_VALUE = FuncClassValue.create(CodeEnumArrayType::new);

    static {
        addArrayFromFunc(CodeEnumArrayType.class, CodeEnumArrayType::from);
    }

    private final Class<?> javaType;

    private final Class<?> enumClass;

    private final Map<Integer, CodeEnum> codeMap;

    /// private constructor
    private CodeEnumArrayType(Class<?> javaType) {
        this.javaType = javaType;
        this.enumClass = ArrayUtils.underlyingComponent(javaType);
        this.codeMap = Map.copyOf(CodeEnumType.createCodeMap(this.enumClass));
    }

    @Override
    public Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public DataType map(ServerMeta meta) throws UnsupportedDialectException {
        return IntegerArrayType.mapToDataType(this, meta);
    }

    @Override
    public Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, this::appendToText, dataType, this);
    }

    @Override
    public Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, this::parseText, null);
    }


    @Override
    public Class<?> underlyingJavaType() {
        return this.enumClass;
    }


    @Override
    public MappingType underlyingType() {
        return CodeEnumType.from(this.enumClass);
    }

    @Override
    public MappingType elementType() {
        final Class<?> javaType = this.javaType, componentType;
        final MappingType instance;

        if (javaType == Object.class) {
            instance = this;
        } else if ((componentType = javaType.getComponentType()).isArray()) {
            instance = from(componentType);
        } else {
            instance = CodeEnumType.from(componentType);
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

    /// @return an unmodifiable map
    @SuppressWarnings("unused")
    public final Map<Integer, CodeEnum> getCodeMap() {
        return this.codeMap;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.javaType, this.enumClass);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof CodeEnumArrayType o) {
            match = o.javaType == this.javaType
                    && o.enumClass == this.enumClass;
        } else {
            match = false;
        }
        return match;
    }


    private CodeEnum parseText(final String text, final int offset, final int end) {
        final int code;
        code = Integer.parseInt(text.substring(offset, end));
        final CodeEnum value;
        value = this.codeMap.get(code);
        if (value == null) {
            String m = String.format("code[%s] no appropriate instance of %s", code, this.enumClass.getName());
            throw new IllegalArgumentException(m);
        }
        return value;
    }

    private void appendToText(final Object element, final StringBuilder appender) {
        if (!this.enumClass.isInstance(element)) {
            // no bug,never here
            throw new IllegalArgumentException();
        }
        appender.append(((CodeEnum) element).code());
    }


}
