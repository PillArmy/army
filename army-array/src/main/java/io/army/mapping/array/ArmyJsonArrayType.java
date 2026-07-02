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


import io.army.codec.JsonCodec;
import io.army.criteria.CriteriaException;
import io.army.executor.DataAccessException;
import io.army.function.TextFunction;
import io.army.mapping.DualGenericsMapping;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping.UnaryGenericsMapping;
import io.army.sqltype.DataType;
import io.army.util.ArrayUtils;

import java.util.Objects;
import java.util.function.BiConsumer;


/// Package class
/// This class is base class of following :
///
/// - {@link JsonArrayType}
/// - {@link JsonbArrayType}
/// - {@link PreferredJsonbArrayType}
///
/// @since 0.6.0
abstract class ArmyJsonArrayType extends _ArmyCoreArrayType {


    final Class<?> javaType;


    /// package constructor
    ArmyJsonArrayType(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }


    @Override
    public final String beforeBind(DataType dataType, MappingEnv env, final Object source) throws CriteriaException {
        final JsonCodec codec;
        codec = env.jsonCodec();

        final BiConsumer<Object, StringBuilder> consumer;
        consumer = (element, appender) -> PostgreArrays.encodeElement(codec.encode(element), appender);

        return PostgreArrays.arrayBeforeBind(source, consumer, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {

        final MappingType underlyingType;
        underlyingType = underlyingType();

        final DataType underlyingDataType;
        underlyingDataType = underlyingType.map(env.serverMeta());

        final TextFunction<?> function;
        function = (text, offset, end) -> underlyingType.afterGet(underlyingDataType, env, text.substring(offset, end));
        return PostgreArrays.arrayAfterGet(this, dataType, source, function, null);
    }


    @Override
    public final MappingType elementType() {
        final MappingType instance;
        final Class<?> javaType = this.javaType, componentType;

        if (javaType == Object.class) {
            instance = this;
        } else if (!(componentType = ArrayUtils.underlyingComponent(javaType)).isArray()) {
            instance = underlyingType();
        } else if (this instanceof UnaryGenericsMapping ug) {
            instance = doFromTypeArg(componentType, ug.genericsType());
        } else if (this instanceof DualGenericsMapping dg) {
            instance = doFromTypeArgs(componentType, dg.firstGenericsType(), dg.secondGenericsType());
        } else {
            instance = doFrom(componentType);
        }
        return instance;
    }

    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        final MappingType instance;
        final Class<?> javaType = this.javaType;
        if (javaType == Object.class) { // unlimited dimension array
            instance = this;
        } else if (this instanceof UnaryGenericsMapping ug) {
            instance = doFromTypeArg(ArrayUtils.arrayClassOf(javaType), ug.genericsType());
        } else if (this instanceof DualGenericsMapping dg) {
            instance = doFromTypeArgs(ArrayUtils.arrayClassOf(javaType), dg.firstGenericsType(), dg.secondGenericsType());
        } else {
            instance = doFrom(ArrayUtils.arrayClassOf(javaType));
        }
        return instance;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.javaType, underlyingType());
    }

    @Override
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (!(obj instanceof ArmyJsonArrayType o)) {
            match = false;
        } else if (obj.getClass() != this.getClass()) {
            match = false;
        } else {
            match = o.javaType == this.javaType
                    && Objects.equals(o.underlyingType(), this.underlyingType());
        }
        return match;
    }

    abstract ArmyJsonArrayType doFromTypeArg(Class<?> javaType, Class<?> typeArg);

    abstract ArmyJsonArrayType doFromTypeArgs(Class<?> javaType, Class<?> keyClass, Class<?> valueClass);

    abstract ArmyJsonArrayType doFrom(Class<?> javaType);


}
