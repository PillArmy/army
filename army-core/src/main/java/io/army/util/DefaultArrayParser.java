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

package io.army.util;


import java.lang.reflect.Array;
import java.util.function.BiConsumer;

final class DefaultArrayParser implements ArrayParser {

    static Builder newBuilder() {
        return new DefaultBuilder();
    }

    static final ArrayParser DEFAULT = newBuilder().build();


    private final char leftBoundary;

    private final char rightBoundary;

    private final char delimChar;


    private DefaultArrayParser(DefaultBuilder builder) {
        this.leftBoundary = builder.leftBoundary;
        this.rightBoundary = builder.rightBoundary;
        this.delimChar = builder.delimChar;
    }


    @Override
    public String parse(final Class<?> underlyingJavaType, final Object array, final BiConsumer<Object, StringBuilder> consumer)
            throws IllegalArgumentException {
        final Class<?> arrayClass = array.getClass();
        if (!ArrayUtils.underlyingComponentMatch(underlyingJavaType, arrayClass)) {
            throw new IllegalArgumentException("array type mismatch");
        }

        final int length = Array.getLength(array);
        final StringBuilder builder = new StringBuilder(2 + length * 8);
        arrayToString(underlyingJavaType, array, consumer, builder);
        return builder.toString();
    }


    private void arrayToString(final Class<?> underlyingType, final Object array, final BiConsumer<Object, StringBuilder> consumer, final StringBuilder builder) {
        final int arrayLength;
        arrayLength = Array.getLength(array);
        final boolean multiDimension;
        multiDimension = array.getClass().getComponentType() != underlyingType;

        Object element;
        builder.append(this.leftBoundary);
        for (int i = 0; i < arrayLength; i++) {
            if (i > 0) {
                builder.append(this.delimChar);
            }
            element = Array.get(array, i);
            if (multiDimension) {
                if (element == null) {
                    throw new IllegalArgumentException("element array couldn't be null.");
                }
                arrayToString(underlyingType, element, consumer, builder);
            } else if (element == null) {
                builder.append("null");
            } else {
                consumer.accept(element, builder);
            }

        }
        builder.append(this.rightBoundary);
    }


    private static final class DefaultBuilder implements Builder {

        private char leftBoundary = '{';

        private char rightBoundary = '}';

        private char delimChar = ',';


        @Override
        public Builder leftBoundary(char boundary) {
            this.leftBoundary = boundary;
            return this;
        }

        @Override
        public Builder rightBoundary(char boundary) {
            this.rightBoundary = boundary;
            return this;
        }

        @Override
        public Builder delimChar(char delim) {
            this.delimChar = delim;
            return this;
        }


        @Override
        public ArrayParser build() {
            return new DefaultArrayParser(this);
        }

    } // DefaultBuilder


}
