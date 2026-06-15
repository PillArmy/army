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
import io.army.mapping.MappingEnv;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.sqltype.DataType;

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
abstract class ArmyJsonArrayType extends _ArmyBuildInArrayType {


    final Class<?> javaType;

    final Class<?> underlyingJavaType;

    /// package constructor
    ArmyJsonArrayType(Class<?> javaType, Class<?> underlyingJavaType) {
        this.javaType = javaType;
        this.underlyingJavaType = underlyingJavaType;
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final Class<?> underlyingJavaType() {
        return this.underlyingJavaType;
    }


    @Override
    public final String beforeBind(DataType dataType, MappingEnv env, final Object source) throws CriteriaException {
        final JsonCodec codec;
        codec = env.jsonCodec();

        final BiConsumer<Object, StringBuilder> consumer;
        consumer = (element, appender) -> {
            PostgreArrays.encodeElement(codec.encode(element), appender);
        };

        return PostgreArrays.arrayBeforeBind(source, consumer, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        final JsonCodec codec;
        codec = env.jsonCodec();

        final TextFunction<?> function;
        function = (text, offset, end) -> codec.decode(text.substring(offset, end), this.underlyingJavaType);

        return PostgreArrays.arrayAfterGet(this, dataType, source, function, null);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.javaType, this.underlyingJavaType);
    }


}
