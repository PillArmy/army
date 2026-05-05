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
import io.army.dialect.TypeMappingHandler;
import io.army.dialect.UnsupportedDialectException;
import io.army.executor.DataAccessException;
import io.army.function.TextFunction;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping.MappingTypeType;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.util.ArrayUtils;

import java.util.Objects;
import java.util.function.BiConsumer;

/// The array type of {@link io.army.mapping.MappingTypeType}
public final class MappingTypeArrayType extends _ArmyBuildInArrayType {

    public static MappingTypeArrayType from(Class<?> javaType) {
        if (!javaType.isArray()) {
            throw errorJavaType(MappingTypeArrayType.class, javaType);
        }
        final Class<?> underlyingType = ArrayUtils.underlyingComponent(javaType);
        if (!MappingType.class.isAssignableFrom(underlyingType)) {
            throw errorJavaType(MappingTypeArrayType.class, javaType);
        }
        return new MappingTypeArrayType(javaType, underlyingType);
    }


    private final Class<?> arrayClass;

    private final Class<?> underlyingType;

    /// private constructor
    private MappingTypeArrayType(Class<?> arrayClass, Class<?> underlyingType) {
        this.arrayClass = arrayClass;
        this.underlyingType = underlyingType;
    }

    @Override
    public Class<?> javaType() {
        return this.arrayClass;
    }

    @Override
    public DataType map(ServerMeta meta) throws UnsupportedDialectException {
        return StringArrayType.mapToSqlType(this, meta);
    }

    @Override
    public String beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        if (!this.arrayClass.isInstance(source)) {
            throw paramError(this, dataType, source, null);
        }
        final ServerMeta serverMeta = env.serverMeta();
        final BiConsumer<Object, StringBuilder> consumer;
        consumer = (element, builder) -> {
            if (!(element instanceof MappingType)) {
                // no bug,never here
                throw new IllegalArgumentException();
            }
            final String typeName;
            typeName = ((MappingType) element).map(serverMeta).typeName();
            PostgreArrays.encodeElement(typeName, builder);
        };

        return PostgreArrays.arrayBeforeBind(source, consumer, dataType, this);
    }


    @Override
    public Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        if (this.arrayClass.isInstance(source)) {
            return source;
        }
        final TypeMappingHandler func = env.typeMapFunc();
        final MappingType[] holder = new MappingType[1];
        final TextFunction<MappingType> function;
        function = (text, offset, end) -> {
            holder[0] = null;
            try {
                func.apply(text.substring(offset, end), holder, 0);
            } catch (IllegalArgumentException e) {
                throw dataAccessError(this, dataType, source, e);
            }
            return Objects.requireNonNull(holder[0]);
        };
        return PostgreArrays.arrayAfterGet(this, dataType, source, false, function);
    }


    @Override
    public Class<?> underlyingJavaType() {
        return this.underlyingType;
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return from(ArrayUtils.arrayClassOf(this.arrayClass));
    }


    @Override
    public MappingType elementType() {
        final Class<?> component = this.arrayClass.componentType();
        final MappingType type;
        if (component.isArray()) {
            type = from(component);
        } else {
            type = MappingTypeType.INSTANCE;
        }
        return type;
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.arrayClass);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof MappingTypeArrayType o) {
            match = o.arrayClass == this.arrayClass;
        } else {
            match = false;
        }
        return match;
    }


}
