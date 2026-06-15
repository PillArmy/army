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


package io.army.serialize;

import io.army.function.TextFunction;
import io.army.function.TextToIntFunc;
import io.army.lang.Nullable;
import io.army.mapping.MappingType;

/// @see ArraySerializer
public interface ArrayDeserializer {


    Object deserialize(String text, int offset, int endIndex, MappingType type, TextFunction<?> func,
                       @Nullable char[] boundaries, @Nullable TextToIntFunc skipFunc, @Nullable StringBuilder builder);


    static Builder builder() {
        return DefaultArrayDeserializer.newBuilder();
    }


    interface Builder {

        Builder leftBoundary(char ch);

        Builder delim(char ch);

        Builder rightBoundary(char ch);

        Builder skipPrefixFunc(TextToIntFunc func);

        Builder backSlashEscapeOn(boolean yes);

        Builder quoteEscapeOn(boolean yes);

        Builder quoteChar(char ch);

        ArrayDeserializer build();

    }

}
