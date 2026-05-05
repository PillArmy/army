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
import io.army.mapping._ArmyNoInjectionType;
import io.army.mapping.optional.VectorType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;

import java.util.function.BiConsumer;


/// Vector array type
///
/// @see VectorType
public final class VectorArrayType extends _ArmyNoInjectionType implements MappingType.SqlArray {


    public static VectorArrayType from(final Class<?> javaClass) {
        if (!javaClass.isArray() || !javaClass.getComponentType().isArray()) {
            throw errorJavaType(VectorArrayType.class, javaClass);
        }
        final Class<?> componentType = ArrayUtils.underlyingComponent(javaClass);
        if (componentType != float.class) {
            throw errorJavaType(VectorArrayType.class, javaClass);
        }
        return new VectorArrayType(javaClass);
    }


    private final Class<?> javaClass;


    private VectorArrayType(Class<?> javaClass) {
        this.javaClass = javaClass;
    }

    @Override
    public Class<?> javaType() {
        return this.javaClass;
    }

    @Override
    public DataType map(ServerMeta meta) throws UnsupportedDialectException {
        if (meta.serverDatabase() != Database.PostgreSQL) {
            throw MAP_ERROR_HANDLER.apply(this, meta);
        }
        return PgType.VECTOR_ARRAY;
    }

    @Override
    public String beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        final Class<?> sourceClass = source.getClass();
        if (!sourceClass.isArray()) {
            throw paramError(this, dataType, source, null);
        } else if (!this.javaClass.isInstance(source)) {
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
    public Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        final Class<?> sourceClass = source.getClass();
        if (sourceClass.isArray()
                && ArrayUtils.underlyingComponentMatch(underlyingJavaType(), sourceClass)) {
            return source;
        }
        if (!(source instanceof String)) {
            throw dataAccessError(this, dataType, source, null);
        }
        try {
            return PostgreArrays.arrayAfterGet(this, dataType, source, true, VectorType::stringToVector);
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw dataAccessError(this, dataType, source, e);
        }
    }


    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return from(ArrayUtils.arrayClassOf(this.javaClass));
    }

    @Override
    public Class<?> underlyingJavaType() {
        return float[].class;
    }

    @Override
    public MappingType elementType() {
        if (this.javaClass == Object.class) {
            return this;
        }
        final Class<?> componentType = this.javaClass.getComponentType();
        final MappingType type;
        if (componentType == float[].class) {
            type = VectorType.INSTANCE;
        } else {
            type = from(componentType);
        }
        return type;
    }


    @Override
    public int hashCode() {
        return this.javaClass.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof VectorArrayType o) {
            match = o.javaClass == this.javaClass;
        } else {
            match = false;
        }
        return match;
    }


}
