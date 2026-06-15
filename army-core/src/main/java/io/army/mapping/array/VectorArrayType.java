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
import io.army.dialect.Database;
import io.army.dialect.UnsupportedDialectException;
import io.army.executor.DataAccessException;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping.VectorType;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.ArmyType;
import io.army.sqltype.CustomType;
import io.army.sqltype.DataType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.util.Objects;
import java.util.function.BiConsumer;


/// Vector array type
///
/// @see VectorType
public class VectorArrayType extends _ArmyBuildInArrayType {


    public static VectorArrayType from(final Class<?> javaClass) {
        final VectorArrayType instance;
        if (javaClass == float[].class) {
            instance = LINEAR;
        } else if (!javaClass.isArray()) {
            throw errorJavaType(VectorArrayType.class, javaClass);
        } else if (ArrayUtils.underlyingComponentMatch(float[].class, javaClass)) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaClass);
        } else {
            throw errorJavaType(VectorArrayType.class, javaClass);
        }
        return instance;
    }


    public static final VectorArrayType LINEAR = new VectorArrayType(float[][].class);

    public static final VectorArrayType UNLIMITED = new VectorArrayType(Object.class);

    private final Class<?> javaType;

    private final CustomType customType;

    /// private constructor
    private VectorArrayType(Class<?> javaType) {
        this.javaType = javaType;
        this.customType = CustomType.builder()
                .typeName("VECTOR[]")
                .javaType(javaType)
                .componentType(ArmyType.VECTOR)
                .componentCreateDdl(true)
                .build();
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        if (meta.serverDatabase() != Database.PostgreSQL) {
            throw MAP_ERROR_HANDLER.apply(this, meta);
        }
        return this.customType;
    }

    @Override
    public final String beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        final Class<?> sourceClass = source.getClass();
        if (!sourceClass.isArray()) {
            throw paramError(this, dataType, source, null);
        } else if (!ArrayUtils.underlyingComponentMatch(underlyingJavaType(), sourceClass)) {
            throw paramError(this, dataType, source, null);
        } else if (!this.javaType.isInstance(source)) {
            throw paramError(this, dataType, source, null);
        }
        final BiConsumer<Object, StringBuilder> consumer;
        consumer = (element, builder) -> {
            if (!(element instanceof float[] vector)) {
                throw paramError(this, dataType, source, null);
            }
            VectorType.vectorToString(vector, builder);
        };
        return PostgreArrays.arrayBeforeBind(source, consumer, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        final Class<?> sourceClass = source.getClass();
        if (sourceClass.isArray()
                && ArrayUtils.underlyingComponentMatch(underlyingJavaType(), sourceClass)) {
            return source;
        }
        if (!(source instanceof String)) {
            throw dataAccessError(this, dataType, source, null);
        }
        return PostgreArrays.arrayAfterGet(this, dataType, source, VectorType::stringToVector, null, null, null);
    }


    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        return from(ArrayUtils.arrayClassOf(this.javaType));
    }

    @Override
    public final Class<?> underlyingJavaType() {
        return float[].class;
    }

    @Override
    public final MappingType underlyingType() {
        return VectorType.INSTANCE;
    }

    @Override
    public final MappingType elementType() {
        final Class<?> componentType = this.javaType.getComponentType();
        final MappingType type;
        if (this.javaType == Object.class) {
            type = this;
        } else if (componentType == float[].class) {
            type = VectorType.INSTANCE;
        } else {
            type = from(componentType);
        }
        return type;
    }


    @Override
    public final int hashCode() {
        return Objects.hash(this.javaType);
    }

    @Override
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof VectorArrayType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }


    private static final class ClassValueHolder {

        private static final ClassValue<VectorArrayType> CLASS_VALUE = FuncClassValue.create(VectorArrayType::new);

    }


}
