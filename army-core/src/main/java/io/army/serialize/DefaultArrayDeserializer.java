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

import io.army.dialect._Constant;
import io.army.function.TextFunction;
import io.army.function.TextToIntFunc;
import io.army.lang.Nullable;
import io.army.mapping.MappingType;
import io.army.mapping.UnaryGenericsMapping;
import io.army.mapping.UserMappingType;
import io.army.util.ArrayUtils;
import io.army.util._Exceptions;
import io.army.util._StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

final class DefaultArrayDeserializer extends ArmyDeserializer.SingleBoundaryDeserializer<ArrayDeserializer.Builder>
        implements ArrayDeserializer {

    static Builder newBuilder() {
        return new DefaultBuilder();
    }


    private final TextToIntFunc skipPrefixFunc;

    private DefaultArrayDeserializer(DefaultBuilder builder) {
        super(builder);
        this.skipPrefixFunc = Objects.requireNonNull(builder.skipPrefixFunc);
    }


    @Override
    public Object deserialize(CharSequence text, int offset, int endIndex, MappingType type, TextFunction<?> func,
                              @Nullable char[] boundaries, @Nullable TextToIntFunc subFunc, @Nullable StringBuilder builder) {

        if ((boundaries == null) != (subFunc == null)) {
            throw new IllegalArgumentException();
        } else if (boundaries != null && isBoundaries(boundaries, this.leftBoundary)) {
            throw new IllegalArgumentException();
        }

        if (!(type instanceof MappingType.SqlArray sa)) {
            throw new IllegalArgumentException();
        }
        final Class<?> javaType, underlyingJavaType, arrayJavaType;
        javaType = type.javaType();
        underlyingJavaType = sa.underlyingJavaType();

        final int offsetIndex, dimension;
        offsetIndex = this.skipPrefixFunc.apply(text, offset, endIndex);

        if (offsetIndex < offset
                || text.charAt(offsetIndex) != this.leftBoundary) {
            throw dimensionFuncBug(this.skipPrefixFunc);
        }

        dimension = parseArrayDimension(text, offsetIndex, endIndex);

        // determine array java type
        if (javaType == Object.class) {
            arrayJavaType = ArrayUtils.arrayClassOf(underlyingJavaType, dimension);
        } else if (List.class.isAssignableFrom(javaType)) {
            if (((UnaryGenericsMapping) type).genericsType() != underlyingJavaType) {
                throw UserMappingType.arrayUnderlyingTypeElementTypeNotMatch(type);
            }
            arrayJavaType = javaType;
        } else if (!ArrayUtils.underlyingComponentMatch(underlyingJavaType, javaType)) {
            throw _Exceptions.arrayUnderlyingComponentMatch(underlyingJavaType, javaType);
        } else if (ArrayUtils.dimensionOfType(type) == dimension) {
            arrayJavaType = javaType;
        } else {
            throw new IllegalArgumentException("array dimension not match");
        }


        final StringBuilder[] holder = new StringBuilder[1];
        holder[0] = builder;

        final Object[] arrayHolder;
        arrayHolder = new Object[1];
        final Consumer<Object> consumer;
        consumer = value -> arrayHolder[0] = value;

        int rightIndex;
        rightIndex = parseArray(text, offsetIndex, endIndex, holder, arrayJavaType, underlyingJavaType, consumer, func, boundaries, subFunc);

        for (rightIndex++; rightIndex < endIndex; rightIndex++) {
            if (Character.isWhitespace(text.charAt(rightIndex))) {
                continue;
            }
            String m = String.format("array tail has text, format error at nearby offset[%s] -> %s",
                    offset, _StringUtils.surroundingText(text, rightIndex, 4));
            throw new IllegalArgumentException(m);
        }
        return Objects.requireNonNull(arrayHolder[0]);
    }


    /// @param offset the index of left boundary
    /// @return the index of right boundary
    @SuppressWarnings("unchecked")
    private int parseArray(final CharSequence text, final int offset, final int endIndex, final StringBuilder[] holder,
                           final Class<?> javaType, final Class<?> underlyingJavaType,
                           final Consumer<Object> outerConsumer, TextFunction<?> func,
                           final @Nullable char[] boundaries, final @Nullable TextToIntFunc subFunc) {


        final int offsetIndex;
        offsetIndex = this.skipPrefixFunc.apply(text, offset, endIndex);

        if (offsetIndex < offset || (offsetIndex > offset && text.charAt(offsetIndex) != this.leftBoundary)) {
            throw dimensionFuncBug(this.skipPrefixFunc);
        }

        final int arrayLength;
        arrayLength = parseArrayLength(text, offset, endIndex, boundaries, subFunc);


        if (arrayLength < 0) {
            throw lengthFuncBug();
        }

        final Class<?> componentType;
        final boolean oneDimension;
        final Object array;

        if (!List.class.isAssignableFrom(javaType)) {
            componentType = javaType.getComponentType();
            oneDimension = componentType == underlyingJavaType;
            array = Array.newInstance(componentType, arrayLength);
        } else if (arrayLength > 0) {
            array = new ArrayList<>(arrayLength);
            componentType = underlyingJavaType;
            oneDimension = true;
        } else {
            array = List.of();
            componentType = underlyingJavaType;
            oneDimension = true;
        }

        final int dimension;
        dimension = parseArrayDimension(text, offset, endIndex);
        if (dimension > 1 && oneDimension) {
            throw dimensionFuncBug(this.skipPrefixFunc);
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

        final char leftBoundary = this.leftBoundary, rightBoundary = this.rightBoundary;

        final char itemDelim, quoteChar;
        itemDelim = this.itemDelim;
        quoteChar = this.quoteChar;

        char ch;

        int rightIndex = -1;

        for (int i = offsetIndex + 1, delimFlat = -1; i < endIndex; i++) {
            ch = text.charAt(i);

            if (ch == rightBoundary) {
                rightIndex = i;
                break;
            }

            if (elementConsumer == null) {   // here array length is 0
                if (Character.isWhitespace(ch)) {
                    continue;
                }
                throw lengthFuncBug();
            }

            if (delimFlat > -1) {
                if (ch == itemDelim) {
                    delimFlat = -1;
                    continue;
                } else if (Character.isWhitespace(ch)) {
                    continue;
                }
                throw _Exceptions.missDelimError(text, i, itemDelim);
            }

            if (ch == leftBoundary) {
                if (oneDimension) {
                    String m = String.format("expected one dimension array but multi-dimension array at nearby offset[%s] -> %s",
                            i, _StringUtils.surroundingText(text, i, 4));
                    throw new IllegalArgumentException(m);
                }

                i = parseArray(text, i, endIndex, holder, componentType, underlyingJavaType, elementConsumer, func, boundaries, subFunc);
                delimFlat = i;
            } else if (ch == quoteChar) {
                if (!oneDimension) {
                    throw arrayFormatError(text, i);
                }
                i = parseQuoteElement(text, i + 1, endIndex, ch, holder, elementConsumer, func);
                delimFlat = i;
            } else if (ch == itemDelim) {
                throw _Exceptions.redundantDelimError(text, i, itemDelim);
            } else if (Character.isWhitespace(ch)) {
                continue;
            } else if (oneDimension) {
                i = parseUnQuoteElement(text, i, endIndex, elementConsumer, func, boundaries, subFunc);
                delimFlat = i;
            } else {
                throw arrayFormatError(text, i);
            }


        } // top loop

        if (rightIndex < 0) {
            throw _Exceptions.missingEndingError(text, endIndex, new char[]{rightBoundary});
        }

        outerConsumer.accept(array);
        return rightIndex;
    }


    private static IllegalArgumentException arrayFormatError(CharSequence text, int offset) {
        String m = String.format("array format error at nearby offset[%s] -> %s",
                offset, _StringUtils.surroundingText(text, offset, 4));
        return new IllegalArgumentException(m);
    }


    private int parseArrayDimension(final CharSequence text, final int offset, final int endIndex) {

        final char leftBoundary = this.leftBoundary;

        int dimension = 0;
        char ch;
        for (int i = offset; i < endIndex; i++) {
            ch = text.charAt(i);
            if (ch == leftBoundary) {
                dimension++;
            } else if (!Character.isWhitespace(ch)) {
                break;
            }
        } // loop

        if (dimension == 0) {
            throw new IllegalArgumentException("array dimension is zero");
        }
        return dimension;
    }

    private int parseArrayLength(final CharSequence text, final int offset, final int endIndex,
                                 final @Nullable char[] boundaries, @Nullable TextToIntFunc subFunc) {

        final char leftBoundary = this.leftBoundary, arrayDelim = this.itemDelim, rightBoundary = this.rightBoundary;
        final char quoteChar = this.quoteChar;

        if (text.charAt(offset) != leftBoundary) {
            throw new IllegalArgumentException();
        }


        int rightIndex = -1;
        char ch;

        int length = 0;
        for (int i = offset + 1, commaFlag = 0; i < endIndex; i++) {

            ch = text.charAt(i);

            if (ch == rightBoundary) {
                rightIndex = i;
                break;
            }

            if (commaFlag > 0) {
                if (ch == arrayDelim) {
                    commaFlag = 0;
                    continue;
                } else if (Character.isWhitespace(ch)) {
                    continue;
                }
                throw _Exceptions.missDelimError(text, i, arrayDelim);
            }


            if (ch == leftBoundary) {
                i = skipArrayElement(text, i, endIndex, boundaries, subFunc);
                commaFlag = 1;
            } else if (ch == quoteChar) {
                i = skipQuoteElement(text, i, endIndex);
                commaFlag = 1;
            } else if (ch == arrayDelim) {
                throw _Exceptions.redundantDelimError(text, i, arrayDelim);
            } else if (!Character.isWhitespace(ch)) {
                i = skipUnquotedElement(text, i, endIndex, rightBoundary, boundaries, subFunc);
                commaFlag = 1;
            }

            if (commaFlag > 0) {
                length++;
            }


        } // loop

        if (rightIndex < 0) {
            throw _Exceptions.missingClosingError(text, endIndex, rightBoundary);
        }
        return length;
    }


    /// @param offset the index of left boundary
    /// @see #skipQuoteElement(CharSequence, int, int)
    /// @see #skipUnquotedElement(String, int, int, char, char[], TextToIntFunc)
    private int skipArrayElement(final CharSequence text, final int offset, final int endIndex,
                                 final @Nullable char[] boundaries, @Nullable TextToIntFunc subFunc) {
//        private method trust upper
//        if (text.charAt(offset) != leftBoundary) {
//            throw new IllegalArgumentException();
//        }

        final char leftBoundary = this.leftBoundary, arrayDelim = this.itemDelim, rightBoundary = this.rightBoundary;
        final char quoteChar = this.quoteChar;

        int rightIndex = -1;
        char ch;
        for (int i = offset + 1, commaFlag = -1; i < endIndex; i++) {
            ch = text.charAt(i);
            if (ch == this.rightBoundary) {
                rightIndex = i;
                break;
            }

            if (commaFlag > -1) {
                if (ch == arrayDelim) {
                    commaFlag = -1;
                    continue;
                } else if (Character.isWhitespace(ch)) {
                    continue;
                }
                throw _Exceptions.missDelimError(text, i, arrayDelim);
            }

            if (ch == leftBoundary) {
                i = skipArrayElement(text, i, endIndex, boundaries, subFunc);
                commaFlag = i;
            } else if (ch == quoteChar) {
                i = skipQuoteElement(text, i, endIndex);
                commaFlag = i;
            } else if (ch == arrayDelim) {
                throw _Exceptions.redundantDelimError(text, i, ch);
            } else if (!Character.isWhitespace(ch)) {
                i = skipUnquotedElement(text, i, endIndex, rightBoundary, boundaries, subFunc);
                commaFlag = i;
            }

        } // loop

        if (rightIndex < 0) {
            throw _Exceptions.missingClosingError(text, endIndex, rightBoundary);
        }
        return rightIndex;
    }


    private IllegalArgumentException dimensionFuncBug(Object func) {
        return new IllegalArgumentException(String.format("func bug,func : %s", func));
    }

    private IllegalArgumentException lengthFuncBug() {
        return new IllegalArgumentException("array length func bug");
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


    private static final class DefaultBuilder extends ArmySingleBoundaryBuilder<Builder>
            implements Builder {

        private TextToIntFunc skipPrefixFunc;

        @Override
        public Builder skipPrefixFunc(TextToIntFunc func) {
            this.skipPrefixFunc = func;
            return this;
        }

        @Override
        public ArrayDeserializer build() {
            if (this.quoteChar != _Constant.DOUBLE_QUOTE && this.quoteChar != _Constant.QUOTE) {
                throw new IllegalArgumentException("quoteChar must be \" or '");
            }
            return new DefaultArrayDeserializer(this);
        }


    } // DefaultBuilder
}




