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
import io.army.executor.DataAccessException;
import io.army.mapping.array.CodeEnumArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.struct.CodeEnum;
import io.army.struct.TextEnum;
import io.army.util.ArrayUtils;
import io.army.util.ClassUtils;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * This class is mapping of enum that implements {@link CodeEnum}.
 * * @see Enum
 *
 * @see io.army.struct.CodeEnum
 * @see TextEnumType
 * @see NameEnumType
 * @since 0.6.0
 */
public final class CodeEnumType extends _ArmyNoInjectionType {


    public static CodeEnumType from(final Class<?> enumClass) {
        if (!(Enum.class.isAssignableFrom(enumClass) && CodeEnum.class.isAssignableFrom(enumClass))) {
            throw errorJavaType(CodeEnumType.class, enumClass);
        } else if (TextEnum.class.isAssignableFrom(enumClass)) {
            throw errorJavaType(CodeEnumType.class, enumClass);
        }
        return INSTANCE_CLASS_VALUE.get(ClassUtils.enumClass(enumClass));
    }

    private static final ClassValue<CodeEnumType> INSTANCE_CLASS_VALUE = new ClassValue<>() {
        @Override
        protected CodeEnumType computeValue(Class<?> type) {
            return new CodeEnumType(type);
        }
    };


    private final Class<?> enumClass;

    private final Map<Integer, ? extends CodeEnum> codeMap;

    /**
     * private constructor
     */
    private CodeEnumType(Class<?> enumClass) {
        this.enumClass = enumClass;
        this.codeMap = CodeEnum.getCodeToEnumMap(enumClass);
    }

    @Override
    public Class<?> javaType() {
        return this.enumClass;
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return CodeEnumArrayType.from(ArrayUtils.arrayClassOf(this.enumClass));
    }

    @Override
    public DataType map(final ServerMeta meta) {
        return IntegerType.mapToDataType(this, meta);
    }

    @Override
    public Integer beforeBind(DataType dataType, MappingEnv env, final Object source) {
        if (!this.enumClass.isInstance(source)) {
            throw paramError(this, dataType, source, null);
        }
        return ((CodeEnum) source).code();
    }

    @Override
    public CodeEnum afterGet(DataType dataType, MappingEnv env, final Object source) {
        if (this.enumClass.isInstance(source)) {
            return (CodeEnum) source;
        }

        final int code;
        if (source instanceof Integer) {
            code = (Integer) source;
        } else if (source instanceof Long) {
            final long v = (Long) source;
            if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) {
                throw dataAccessError(this, dataType, source, null);
            }
            code = (int) v;
        } else if (source instanceof Short || source instanceof Byte) {
            code = ((Number) source).intValue();
        } else if (source instanceof BigInteger) {
            try {
                code = ((BigInteger) source).intValueExact();
            } catch (ArithmeticException e) {
                throw dataAccessError(this, dataType, source, e);
            }
        } else if (source instanceof String) {
            try {
                code = Integer.parseInt((String) source);
            } catch (NumberFormatException e) {
                throw dataAccessError(this, dataType, source, e);
            }
        } else {
            throw dataAccessError(this, dataType, source, null);
        }
        final CodeEnum codeEnum;
        codeEnum = this.codeMap.get(code);
        if (codeEnum == null) {
            String m = String.format("Not found enum instance for code[%s] in enum[%s].",
                    source, this.enumClass.getName());
            throw new DataAccessException(m);
        }
        return codeEnum;
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.enumClass);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof CodeEnumType o) {
            match = o.enumClass == this.enumClass;
        } else {
            match = false;
        }
        return match;
    }




    /*################################## blow private method ##################################*/


}
