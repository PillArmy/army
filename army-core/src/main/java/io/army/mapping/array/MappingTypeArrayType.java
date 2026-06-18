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
import io.army.dialect.MappingHandler;
import io.army.dialect.TypeMappingBundle;
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
public class MappingTypeArrayType extends _ArmyBuildInArrayType {

    public static MappingTypeArrayType from(Class<?> javaType) {
        if (!javaType.isArray()) {
            throw errorJavaType(MappingTypeArrayType.class, javaType);
        }
        if (ArrayUtils.underlyingComponent(javaType) != MappingType.class) {
            throw errorJavaType(MappingTypeArrayType.class, javaType);
        }
        return new MappingTypeArrayType(javaType);
    }


    private final Class<?> javaType;

    private final Class<?> underlyingType;

    /// private constructor
    private MappingTypeArrayType(Class<?> javaType) {
        this.javaType = javaType;
        this.underlyingType = ArrayUtils.underlyingComponent(javaType);
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        return StringArrayType.mapToSqlType(this, meta);
    }

    @Override
    public final String beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        if (!this.javaType.isInstance(source)) {
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
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        if (this.javaType.isInstance(source)) {
            return source;
        }
        final MappingHandler handler = env.mappingHandler();
        final TextFunction<MappingType> function;
        function = (text, offset, end) -> {
            final TypeMappingBundle bundle;
            try {
                bundle = handler.handleType(text.substring(offset, end));
            } catch (Exception e) {
                throw dataAccessError(this, dataType, source, e);
            }
            return bundle.mappingType;
        };
        return PostgreArrays.arrayAfterGet(this, dataType, source, function, null);
    }


    @Override
    public final Class<?> underlyingJavaType() {
        return this.underlyingType;
    }

    @Override
    public final MappingType underlyingType() {
        return MappingTypeType.INSTANCE;
    }

    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        return from(ArrayUtils.arrayClassOf(this.javaType));
    }


    @Override
    public final MappingType elementType() {
        final Class<?> component = this.javaType.componentType();
        final MappingType type;
        if (component.isArray()) {
            type = from(component);
        } else {
            type = MappingTypeType.INSTANCE;
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
        } else if (obj instanceof MappingTypeArrayType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }


}
