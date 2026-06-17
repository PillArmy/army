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

public interface Deserializer {


    interface DeserializerBuilder<B extends DeserializerBuilder<B>> {

        B dataTypeLabel(String name);

        B delim(char ch);

        B backSlashEscapeOn(boolean yes);

        B quoteEscapeOn(boolean yes);

        B quoteChar(char ch);

        /// eg : PostgreSQL composite nothing representing null
        B allowNothing(boolean yes);

        /// eg:  PostgreSQL composite whitespace is part of field value
        B allowWhitespace(boolean yes);

        B allowQuote(boolean yes);

        /// eg : PostgreSQL array null is {@code null}.  PostgreSQL composite nothing is {@code null}, null is field value in composite.
        B nullAsNull(boolean yes);

        Deserializer build();

    }


    interface SingleBoundaryBuilder<B extends SingleBoundaryBuilder<B>> extends DeserializerBuilder<B> {

        B leftBoundary(char ch);

        B rightBoundary(char ch);

    }

    interface MultiBoundaryBuilder<B extends MultiBoundaryBuilder<B>> extends DeserializerBuilder<B> {

        B leftBoundaries(char[] array);

        B rightBoundaries(char[] array);

    }


}
