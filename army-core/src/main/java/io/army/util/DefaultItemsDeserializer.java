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

import io.army.dialect._Constant;
import io.army.function.TextFunction;
import io.army.lang.Nullable;
import io.army.mapping.MappingType;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

final class DefaultItemsDeserializer implements ItemsDeserializer {

    static Builder newBuilder() {
        return new DefaultBuilder();
    }

    private final char[] leftBoundaries;

    private final char itemDelim;

    private final char[] rightBoundaries;

    private final TextFunction<int[]> dimensionFunc;

    private final TextFunction<Integer> lengthFunc;

    private final boolean alwaysParseLength;

    private final boolean backSlashEscape;

    private final boolean quoteEscape;


    private DefaultItemsDeserializer(DefaultBuilder builder) {

        this.leftBoundaries = builder.leftBoundaries;
        this.itemDelim = builder.itemDelim;
        this.rightBoundaries = builder.rightBoundaries;
        this.dimensionFunc = builder.dimensionFunc;

        this.lengthFunc = builder.lengthFunc;
        this.alwaysParseLength = builder.alwaysParseLength;
        this.quoteEscape = builder.quoteEscape;
        this.backSlashEscape = builder.backSlashEscape;
    }


    @Override
    public Object deserialize(final String text, final int offset, final int endIndex, final MappingType type,
                              final TextFunction<?> func, @Nullable StringBuilder builder) {

        final boolean arrayParser = type instanceof MappingType.SqlArray;

        final Class<?> javaType, underlyingJavaType, arrayJavaType;
        javaType = type.javaType();

        final int offsetIndex;
        if (arrayParser) {

            underlyingJavaType = ((MappingType.SqlArray) type).underlyingJavaType();

            final int[] dimensionAndIndex;
            dimensionAndIndex = obtainDimensionFunc().apply(text, offset, endIndex);
            final int dimension = dimensionAndIndex[0];
            offsetIndex = dimensionAndIndex[1];
            if (dimensionAndIndex.length != 2
                    || dimension < 1
                    || offsetIndex < offset
                    || !isBoundaries(this.leftBoundaries, text.charAt(offsetIndex))) {
                throw dimensionFuncBug();
            }

            // determine array java type
            if (javaType == Object.class) {
                arrayJavaType = ArrayUtils.arrayClassOf(underlyingJavaType, dimension);
            } else if (ArrayUtils.dimensionOf(javaType) == dimension) {
                arrayJavaType = javaType;
            } else {
                throw new IllegalArgumentException("array dimension not match");
            }
        } else {
            offsetIndex = offset;
            underlyingJavaType = javaType;
            arrayJavaType = javaType;
        }


        final StringBuilder[] holder = new StringBuilder[1];
        holder[0] = builder;

        final Object[] arrayHolder;
        final Consumer<Object> consumer;
        if (arrayParser) {
            arrayHolder = new Object[1];
            consumer = value -> arrayHolder[0] = value;
        } else {
            arrayHolder = null;
            consumer = null;
        }

        int rightIndex;
        rightIndex = parseBlockElement(text, offsetIndex, endIndex, holder, arrayJavaType, underlyingJavaType, consumer, func);

        for (rightIndex++; rightIndex < endIndex; rightIndex++) {
            if (Character.isWhitespace(text.charAt(rightIndex))) {
                continue;
            }
            String m = String.format("array tail has text, format error at nearby offset[%s] -> %s",
                    offset, _StringUtils.surroundingText(text, rightIndex, 4));
            throw new IllegalArgumentException(m);
        }

        final Object array;
        if (arrayHolder == null) {
            array = List.of();
        } else {
            array = Objects.requireNonNull(arrayHolder[0]);
        }
        return array;
    }


    /// @param offset the index of left boundary
    /// @return the index of right boundary
    private int parseBlockElement(final String text, final int offset, final int endIndex, final StringBuilder[] holder,
                                  final Class<?> javaType, final Class<?> underlyingJavaType,
                                  @Nullable Consumer<Object> outerConsumer, TextFunction<?> func) {

        final Consumer<Object> elementConsumer;
        final Class<?> componentType;
        final boolean oneDimension;
        final ArrayContext arrayContext;

        if (outerConsumer == null) {
            elementConsumer = null;
            oneDimension = true;
            componentType = underlyingJavaType;
            arrayContext = null;
        } else {
            arrayContext = createContext(text, offset, endIndex, javaType, underlyingJavaType);
            elementConsumer = arrayContext.consumer;
            oneDimension = arrayContext.oneDimension;
            componentType = arrayContext.componentType;
        }


        final char itemDelim;
        itemDelim = this.itemDelim;

        char ch;

        int rightIndex = -1;

        for (int i = offset + 1, delimFlat = -1; i < endIndex; i++) {
            ch = text.charAt(i);

            if (isBoundaries(this.rightBoundaries, ch)) {
                rightIndex = i;
                break;
            }

            if (arrayContext != null && elementConsumer == null) {   // here array length is 0
                if (Character.isWhitespace(ch)) {
                    continue;
                }
                throw lengthFuncBug();
            }

            if (delimFlat > 0) {
                if (ch == itemDelim) {
                    delimFlat = -1;
                    continue;
                } else if (Character.isWhitespace(ch)) {
                    continue;
                }
                String m = String.format("missing delim at nearby offset[%s] -> %s",
                        offset, _StringUtils.surroundingText(text, offset, 4));
                throw new IllegalArgumentException(m);
            }

            if (isBoundaries(this.leftBoundaries, ch)) {
                if (arrayContext != null && oneDimension) {
                    String m = String.format("expected one dimension array but multi-dimension array at nearby offset[%s] -> %s",
                            offset, _StringUtils.surroundingText(text, offset, 4));
                    throw new IllegalArgumentException(m);
                }

                i = parseBlockElement(text, i, endIndex, holder, componentType, underlyingJavaType, elementConsumer, func);
                delimFlat = i;
            } else if (ch == _Constant.QUOTE || ch == _Constant.DOUBLE_QUOTE) {
                i = parseQuoteElement(text, i + 1, endIndex, ch, holder, elementConsumer, func);
                delimFlat = i;
            } else if (ch == itemDelim) {
                String m = String.format("redundant array delim at nearby offset[%s] -> %s",
                        offset, _StringUtils.surroundingText(text, offset, 4));
                throw new IllegalArgumentException(m);
            } else if (!Character.isWhitespace(ch)) {
                if (arrayContext != null && !oneDimension) {
                    String m = String.format("array format error at nearby offset[%s] -> %s",
                            offset, _StringUtils.surroundingText(text, offset, 4));
                    throw new IllegalArgumentException(m);
                }
                i = parseUnQuoteElement(text, i, endIndex, elementConsumer, func);
                delimFlat = i;
            }


        } // top loop

        if (rightIndex < 0) {
            throw arrayNotEnclose(text, offset);
        }

        if (outerConsumer != null) {
            outerConsumer.accept(arrayContext.array);
        }

        return rightIndex;
    }


    /// @return the previous index of array delim or right boundary
    private int parseUnQuoteElement(final String text, final int offset, final int endIndex,
                                    final @Nullable Consumer<Object> consumer, TextFunction<?> func) {
        final char itemDelim, firstChar;
        itemDelim = this.itemDelim;
        firstChar = text.charAt(offset);

        final boolean firstIsN = firstChar == 'n' || firstChar == 'N';

        int rightIndex = -1;

        Object value;
        char ch;
        for (int i = offset + 1, elementEndInex = -1; i < endIndex; i++) {
            ch = text.charAt(i);

            if (ch == itemDelim || isBoundaries(this.rightBoundaries, ch)) {

                if (elementEndInex < 0) {
                    if (i - offset == 4
                            && firstIsN
                            && text.regionMatches(true, offset, _Constant.NULL, 0, 4)) {
                        value = null;
                    } else {
                        value = func.apply(text, offset, i);
                    }

                    if (consumer != null) {
                        consumer.accept(value);
                    }
                }

                rightIndex = i - 1;
                break;
            }

            if (!Character.isWhitespace(ch)) {
                continue;
            }

            if (elementEndInex < 0) {
                if (firstIsN
                        && i - offset == 4
                        && text.regionMatches(true, offset, _Constant.NULL, 0, 4)) {
                    value = null;
                } else {
                    value = func.apply(text, offset, i);
                }

                if (consumer != null) {
                    consumer.accept(value);
                }
                elementEndInex = i;
            } else {
                String m = String.format("unquoted element exists whitespace at nearby offset[%s] -> %s",
                        offset, _StringUtils.surroundingText(text, offset, 4));
                throw new IllegalArgumentException(m);
            }

        } // loop

        if (rightIndex < 0) {
            String m = String.format("unquoted element not end at nearby offset[%s] -> %s",
                    offset, _StringUtils.surroundingText(text, offset, 4));
            throw new IllegalArgumentException(m);
        }
        return rightIndex;
    }

    /// @param offset the next index of left quote
    /// @return the index of right quote
    private int parseQuoteElement(final String text, final int offset, final int endIndex, final char quote,
                                  final StringBuilder[] holder, @Nullable Consumer<Object> consumer, TextFunction<?> func) {

        StringBuilder builder = holder[0];
        if (builder != null) {
            builder.setLength(0); // clear
        }

        final boolean quoteEscapeOn = this.quoteEscape, backSlashEscapeOn = this.backSlashEscape;


        char ch;
        int rightIndex = -1;
        int lastWritten = offset;
        boolean backSlashEscape, quoteEscape;
        for (int i = offset, nextIndex, escapeCount = 0; i < endIndex; i++) {
            ch = text.charAt(i);

            backSlashEscape = backSlashEscapeOn && ch == _Constant.BACK_SLASH;
            if (quoteEscapeOn) {
                quoteEscape = (ch == quote && (nextIndex = i + 1) < endIndex && text.charAt(nextIndex) == quote);
            } else {
                quoteEscape = false;
            }

            if (backSlashEscape || quoteEscape) {
                escapeCount++;

                if (builder == null) {
                    holder[0] = builder = new StringBuilder((i - offset) << 1);
                }

                // skip escape(current char),
                if (i > lastWritten) {
                    builder.append(text, lastWritten, i);
                    i++; // skip escape(current char),
                    lastWritten = i;
                }

                continue;
            }

            if (ch != quote) {
                continue;
            }

            final Object value;
            if (escapeCount > 0) {
                builder.append(text, lastWritten, i);
                final String effectiveText;
                effectiveText = builder.toString();
                value = func.apply(effectiveText, 0, effectiveText.length());
            } else {
                value = func.apply(text, offset, i);
            }

            if (consumer != null) {
                consumer.accept(value);
            }

            rightIndex = i;

            break;

        } // top loop


        if (rightIndex < 0) {
            String m = String.format("quote element not end at nearby offset[%s] -> %s",
                    offset, _StringUtils.surroundingText(text, offset, 4));
            throw new IllegalArgumentException(m);
        }
        return rightIndex;
    }

    private TextFunction<int[]> obtainDimensionFunc() {
        return Objects.requireNonNull(this.dimensionFunc, "array parser dimensionFunc required");
    }

    @SuppressWarnings("unchecked")
    private ArrayContext createContext(final String text, final int offset, final int endIndex,
                                       final Class<?> javaType, final Class<?> underlyingJavaType) {

        final int[] dimensionAndIndex;
        dimensionAndIndex = obtainDimensionFunc().apply(text, offset, endIndex);

        final Class<?> componentType;
        final boolean oneDimension;
        final Object array;
        final int arrayLength;

        final TextFunction<Integer> lengthFunc = Objects.requireNonNull(this.lengthFunc, "array parser lengthFunc required");

        if (!List.class.isAssignableFrom(javaType)) {
            arrayLength = lengthFunc.apply(text, offset, endIndex);
            if (arrayLength < 0) {
                throw lengthFuncBug();
            }
            componentType = javaType.getComponentType();
            oneDimension = componentType == underlyingJavaType;
            array = Array.newInstance(componentType, arrayLength);
        } else if (!this.alwaysParseLength) {
            arrayLength = Integer.MAX_VALUE;
            array = new ArrayList<>();
            componentType = underlyingJavaType;
            oneDimension = true;
        } else if ((arrayLength = lengthFunc.apply(text, offset, endIndex)) < 0) {
            throw lengthFuncBug();
        } else if (arrayLength > 0) {
            array = new ArrayList<>(arrayLength);
            componentType = underlyingJavaType;
            oneDimension = true;
        } else {
            array = List.of();
            componentType = underlyingJavaType;
            oneDimension = true;
        }

        if (dimensionAndIndex[0] > 1 && oneDimension) {
            throw dimensionFuncBug();
        }

        final Consumer<Object> elementConsumer;
        if (arrayLength == 0) {
            elementConsumer = null;
        } else if (array instanceof List<?>) {
            elementConsumer = ((List<Object>) array)::add;
        } else if (oneDimension && !ArrayUtils.underlyingIsPrimitive(underlyingJavaType)) {
            elementConsumer = new OneDimensionConsumer((Object[]) array);
        } else {
            elementConsumer = new MultiDimensionConsumer(array, underlyingJavaType);
        }
        return new ArrayContext(oneDimension, elementConsumer, componentType, array);
    }


    private IllegalArgumentException dimensionFuncBug() {
        return new IllegalArgumentException(String.format("array dimension func bug,func : %s", this.dimensionFunc));
    }

    private IllegalArgumentException lengthFuncBug() {
        return new IllegalArgumentException(String.format("array length func bug,func : %s", this.lengthFunc));
    }

    private static boolean isBoundaries(final char[] boundaries, final char ch) {
        boolean match = false;
        for (char boundary : boundaries) {
            if (boundary == ch) {
                match = true;
                break;
            }
        }
        return match;
    }


    private static IllegalArgumentException arrayNotEnclose(String text, int offset) {
        String m = String.format("array not enclose at nearby offset[%s] -> %s", offset,
                _StringUtils.surroundingText(text, offset, 4));
        return new IllegalArgumentException(m);
    }


    private static final class MultiDimensionConsumer implements Consumer<Object> {

        private final Object array;

        private final boolean primitive;

        private int index = 0;

        private MultiDimensionConsumer(Object array, Class<?> underlyingJavaType) {
            this.array = array;
            this.primitive = underlyingJavaType.isPrimitive();
        }

        @Override
        public void accept(@Nullable Object o) {
            if (o == null && this.primitive) {
                throw new IllegalArgumentException("primitive type cannot be null");
            }
            try {
                Array.set(this.array, this.index++, o);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    } // MultiDimensionConsumer

    private static final class OneDimensionConsumer implements Consumer<Object> {


        private final Object[] array;

        private int index = 0;

        private OneDimensionConsumer(Object[] array) {
            this.array = array;
        }

        @Override
        public void accept(@Nullable Object o) {
            this.array[this.index++] = o;
        }


    } // OneDimensionConsumer

    private static final class ArrayContext {


        private final boolean oneDimension;

        private final Consumer<Object> consumer;

        private final Class<?> componentType;

        private final Object array;

        private ArrayContext(boolean oneDimension, @Nullable Consumer<Object> consumer, Class<?> componentType, Object array) {
            this.oneDimension = oneDimension;
            this.consumer = consumer;
            this.componentType = componentType;
            this.array = array;
        }

    } // ArrayContext


    private static final class DefaultBuilder implements Builder {

        private static final char[] DEFAULT_LEFT_BOUNDARIES = new char[]{'{'};
        private static final char[] DEFAULT_RIGHT_BOUNDARIES = new char[]{'}'};


        private char[] leftBoundaries = DEFAULT_LEFT_BOUNDARIES;

        private char itemDelim = _Constant.COMMA;

        private char[] rightBoundaries = DEFAULT_RIGHT_BOUNDARIES;

        private TextFunction<int[]> dimensionFunc;

        private TextFunction<Integer> lengthFunc;

        private boolean alwaysParseLength;

        private boolean backSlashEscape;

        private boolean quoteEscape;

        @Override
        public Builder leftBoundaries(char[] array) {
            this.leftBoundaries = array;
            return this;
        }

        @Override
        public Builder delim(char ch) {
            this.itemDelim = ch;
            return this;
        }

        @Override
        public Builder rightBoundaries(char[] array) {
            this.rightBoundaries = array;
            return this;
        }

        @Override
        public Builder dimensionFunc(TextFunction<int[]> func) {
            this.dimensionFunc = func;
            return this;
        }

        @Override
        public Builder lengthFunc(TextFunction<Integer> func) {
            this.lengthFunc = func;
            return this;
        }

        @Override
        public Builder alwaysParseLength(boolean yes) {
            this.alwaysParseLength = yes;
            return this;
        }


        @Override
        public Builder backSlashEscapeOn(boolean yes) {
            this.backSlashEscape = yes;
            return this;
        }

        @Override
        public Builder quoteEscapeOn(boolean yes) {
            this.quoteEscape = yes;
            return this;
        }

        @Override
        public ItemsDeserializer build() {
            if (this.leftBoundaries == null) {
                throw new NullPointerException("leftBoundaries");
            } else if (this.rightBoundaries == null) {
                throw new NullPointerException("rightBoundaries");
            }
            return new DefaultItemsDeserializer(this);
        }


    } // DefaultBuilder
}




