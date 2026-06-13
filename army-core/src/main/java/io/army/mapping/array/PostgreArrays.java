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
import io.army.dialect._Constant;
import io.army.function.TextFunction;
import io.army.lang.Nullable;
import io.army.mapping.MappingType;
import io.army.mapping.UnaryGenericsMapping;
import io.army.mapping.UserMappingType;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.*;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class PostgreArrays extends ArrayMappings {

    private PostgreArrays() {
    }

    private static final ArraySerializer DEFAULT_SERIALIZER = createDefaultSerializerBuilder()
            .delimChar(_Constant.COMMA)
            .build();


    /// @see BoxArrayDeserializerHolder#BOX_DESERIALIZER
    private static final ItemsDeserializer DEFAULT_DESERIALIZER = createDefaultDeserializerBuilder()
            .delim(_Constant.COMMA)
            .build();


    public static byte[] parseBytea(final String text, int offset, final int end) {
        if (!text.startsWith("0x", offset)) {
            throw new IllegalArgumentException("not start with 0x");
        }
        offset += 2;

        final byte[] bytea;
        bytea = text.substring(offset, end).getBytes(StandardCharsets.UTF_8);
        return HexUtils.decodeHex(bytea, 0, bytea.length);
    }


    /// decode array element
    ///
    /// @see #encodeElement(String, StringBuilder)
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-IO">Array Input and Output Syntax</a>
    public static String decodeElement(final String text, int offset, int end) {
        final boolean enclose;
        if (text.charAt(offset) == _Constant.DOUBLE_QUOTE) {
            if (text.charAt(end - 1) != _Constant.DOUBLE_QUOTE) {
                throw new IllegalArgumentException("postgre array format error");
            }
            offset++;
            end--;
            enclose = true;
        } else {
            enclose = false;
        }

        char ch;
        StringBuilder builder = null;
        int lastWritten = offset;
        for (int i = offset; i < end; i++) {
            ch = text.charAt(i);

            if (ch != _Constant.BACK_SLASH) {
                continue;
            }

            if (builder == null) {
                builder = new StringBuilder((end - offset) + 10);
            }

            if (i > lastWritten) {
                builder.append(text, lastWritten, i);
            }

            i++;  // skip current char
            lastWritten = i;

        }

        if (builder != null && lastWritten < end) {
            builder.append(text, lastWritten, end);
        }

        final String elementText;
        if (builder != null) {
            elementText = builder.toString();
        } else if (enclose) {
            elementText = text.substring(offset, end);
        } else {
            elementText = text;
        }
        return elementText;
    }

    /// escape array element
    ///
    /// @see #decodeElement(String, int, int)
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-IO">Array Input and Output Syntax</a>
    public static void encodeElement(final String element, final StringBuilder builder) {

        builder.append(_Constant.DOUBLE_QUOTE); // left doubleQuote

        final int length = element.length();
        int lastWritten = 0;
        char ch;
        for (int i = 0; i < length; i++) {
            ch = element.charAt(i);
            if (ch == _Constant.BACK_SLASH || ch == _Constant.DOUBLE_QUOTE) {
                if (i > lastWritten) {
                    builder.append(element, lastWritten, i);
                }
                builder.append(_Constant.BACK_SLASH);
                lastWritten = i; // not i + 1 as current char wasn't written
            }
        }

        if (lastWritten < length) {
            builder.append(element, lastWritten, length);
        }

        builder.append(_Constant.DOUBLE_QUOTE);// right doubleQuote
    }


    public static String arrayBeforeBind(final Object source, final BiConsumer<Object, StringBuilder> consumer,
                                         final DataType dataType, final MappingType type,
                                         final ErrorHandler handler) {

        throw new UnsupportedOperationException();
    }

    public static Object arrayAfterGet(MappingType type, DataType dataType, final Object source,
                                       final boolean nonNull, final TextFunction<?> elementFunc) {
        throw new UnsupportedOperationException();
    }


    /// @return int[2] , int[0] is dimension, int[1] is the index of left boundary.
    /// @see ItemsDeserializer.Builder#dimensionFunc(TextFunction)
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-IO">Array Input and Output Syntax</a>
    public static int[] parseArrayDimension(final String text, int offset, final int endIndex) {

        char ch;

        if (offset == 0) {
            for (; offset < endIndex; offset++) {
                ch = text.charAt(offset);
                if (Character.isWhitespace(ch)) {
                    continue;
                }
                if (ch != _Constant.LEFT_SQUARE_BRACKET) {
                    break;
                }
                offset = text.indexOf('=');
                if (offset < 0) {
                    throw new IllegalArgumentException("postgre array meta error");
                }

                offset++;

                break;
            } // loop
            if (offset == endIndex) {
                throw new IllegalArgumentException("no text");
            }

        } //   if(offset == 0){


        int dimension = 0, leftIndex = -1;
        for (; offset < endIndex; offset++) {
            ch = text.charAt(offset);
            if (ch == _Constant.LEFT_BRACE) {
                dimension++;
                if (leftIndex < 0) {
                    leftIndex = offset;
                }
            } else if (!Character.isWhitespace(ch)) {
                break;
            }
        } // loop

        if (dimension == 0) {
            throw new IllegalArgumentException("postgre array dimension is zero");
        }
        return new int[]{dimension, leftIndex};
    }


    /// @return the length of array
    /// @see ItemsDeserializer.Builder#lengthFunc(TextFunction)
    public static int parseArrayLength(final String text, final int offset, final int endIndex)
            throws IllegalArgumentException {

        if (text.charAt(offset) != _Constant.LEFT_BRACE) {
            throw new IllegalArgumentException();
        }

        int rightIndex = -1;
        char ch;

        int length = 0;
        for (int i = offset + 1, commaFlag = 0; i < endIndex; i++) {

            ch = text.charAt(i);

            if (ch == _Constant.RIGHT_BRACE) {
                rightIndex = i;
                break;
            }

            if (commaFlag > 0) {
                if (ch == _Constant.COMMA) {
                    commaFlag = 0;
                    continue;
                } else if (Character.isWhitespace(ch)) {
                    continue;
                }
                throw _Exceptions.missDelimError(text, offset, _Constant.COMMA);
            }


            if (ch == _Constant.LEFT_BRACE) {
                i = skipArrayElement(text, i, endIndex);
                commaFlag = 1;
            } else if (ch == _Constant.DOUBLE_QUOTE) {
                i = skipQuoteElement(text, i, endIndex);
                commaFlag = 1;
            } else if (ch == _Constant.COMMA) {
                throw _Exceptions.redundantDelimError(text, i, _Constant.COMMA);
            } else if (!Character.isWhitespace(ch)) {
                i = skipUnquotedElement(text, i, endIndex);
                commaFlag = 1;
            }

            if (commaFlag > 0) {
                length++;
            }


        } // loop

        if (rightIndex < 0) {
            throw _Exceptions.missingClosingError(text, endIndex, _Constant.RIGHT_BRACE);
        }
        return length;
    }

    public static String arrayBeforeBind(final Object source, final BiConsumer<Object, StringBuilder> consumer,
                                         final DataType dataType, final MappingType type) {
        try {
            if (!(type instanceof MappingType.SqlArray)) {
                String m = String.format("%s not %s", type.getClass().getName(), MappingType.SqlArray.class.getName());
                throw new IllegalArgumentException(m);
            }
            final Class<?> javaType = type.javaType(), underlyingJavaType, sourceClass;
            underlyingJavaType = ((MappingType.SqlArray) type).underlyingJavaType();
            sourceClass = source.getClass();

            if (javaType == Object.class) {
                if (!sourceClass.isArray()) {
                    throw new IllegalArgumentException("source not array");
                } else if (!ArrayUtils.underlyingComponentMatch(underlyingJavaType, sourceClass)) {
                    throw _Exceptions.arrayUnderlyingComponentMatch(underlyingJavaType, sourceClass);
                }
            } else if (!List.class.isAssignableFrom(javaType) && !javaType.isInstance(source)) {
                throw UserMappingType.paramError(type, dataType, source, null);
            }

            final ArraySerializer parser;
            if (dataType == PgType.BOX_ARRAY) {
                parser = BoxArrayDeserializerHolder.BOX_SERIALIZER;
            } else {
                parser = DEFAULT_SERIALIZER;
            }

            return parser.parse(((MappingType.SqlArray) type).underlyingJavaType(), source, consumer);
        } catch (CriteriaException e) {
            throw e;
        } catch (Exception e) {
            throw UserMappingType.paramError(type, dataType, source, e);
        }
    }

    public static Object arrayAfterGet(MappingType type, DataType dataType, final Object source,
                                       final TextFunction<?> elementFunc, @Nullable StringBuilder builder) {

        if (!(type instanceof MappingType.SqlArray)) {
            throw new IllegalArgumentException("not array type");
        }
        if (isInstanceOfType(type, source)) {
            return source;
        }
        if (!(source instanceof String sourceText)) {
            throw UserMappingType.dataAccessError(type, dataType, source, null);
        }

        final ItemsDeserializer deserializer;
        if (dataType == PgType.BOX_ARRAY) {
            deserializer = BoxArrayDeserializerHolder.BOX_DESERIALIZER;
        } else {
            deserializer = DEFAULT_DESERIALIZER;
        }

        try {
            return deserializer.deserialize(sourceText, 0, sourceText.length(), type, elementFunc, builder);
        } catch (Exception e) {
            throw UserMappingType.dataAccessError(type, dataType, source, e);
        }
    }


    @Deprecated
    public static Object arrayAfterGet(MappingType type, DataType dataType, final Object source,
                                       final TextFunction<?> elementFunc) {

        return arrayAfterGet(type, dataType, source, true, elementFunc, null);
    }


    @Deprecated
    public static Object arrayAfterGet(MappingType type, DataType dataType, final Object source,
                                       final boolean nonNull, final TextFunction<?> elementFunc, ErrorHandler errorHandler) {
        throw new UnsupportedOperationException();
    }


    public static StringBuilder byteaArrayToText(final MappingType type, final DataType dataType, final Object source,
                                                 final StringBuilder builder, final ErrorHandler errorHandler) {
        final Class<?> sourceClass = source.getClass();
        final int dimension;
        if (!sourceClass.isArray()
                || ArrayUtils.underlyingComponent(sourceClass) != byte.class
                || (dimension = ArrayUtils.dimensionOf(sourceClass) - 1) < 1) {
            throw errorHandler.apply(type, dataType, source, null);
        }

        _byteaArrayToText(type, dataType, source, dimension, builder, errorHandler);
        return builder;
    }


    /// @see #byteaArrayToText(MappingType, DataType, Object, StringBuilder, ErrorHandler)
    private static void _byteaArrayToText(final MappingType type, final DataType dataType, final Object source,
                                          final int dimension, final StringBuilder builder,
                                          final ErrorHandler errorHandler) {


        final int length;
        length = Array.getLength(source);

        builder.append(_Constant.LEFT_BRACE);

        Object element;
        for (int i = 0; i < length; i++) {
            element = Array.get(source, i);

            if (i > 0) {
                builder.append(_Constant.COMMA);
            }

            if (element == null) {
                if (dimension > 1) {
                    final IllegalArgumentException e;
                    e = new IllegalArgumentException("multi-dimension must not null");
                    throw errorHandler.apply(type, dataType, source, e);
                }
                builder.append("null");
            } else if (dimension > 1) {
                _byteaArrayToText(type, dataType, element, dimension - 1, builder, errorHandler);
            } else {
                builder.append("0x")
                        .append(HexUtils.hexEscapesText(false, (byte[]) element));
            }

        } // for loop

        builder.append(_Constant.RIGHT_BRACE);

    }


    /// @see #arrayAfterGet(MappingType, DataType, Object, TextFunction, StringBuilder)
    private static boolean isInstanceOfType(final MappingType type, final Object source) {
        final Class<?> javaType = type.javaType(), underlyingType;
        underlyingType = ((MappingType.SqlArray) type).underlyingJavaType();

        final boolean match;
        if (javaType == Object.class) {
            final Class<?> sourceClass = source.getClass();
            match = sourceClass.isArray() && ArrayUtils.underlyingComponentMatch(underlyingType, sourceClass);
        } else if (javaType.isArray()) {
            match = javaType.isInstance(source);
        } else if (!List.class.isAssignableFrom(javaType)) {
            match = false;
        } else if (source instanceof List<?> list) {
            final Class<?> elementType = ((UnaryGenericsMapping) type).genericsType();
            if (elementType != underlyingType) {
                throw UserMappingType.arrayUnderlyingTypeElementTypeNotMatch(type);
            }
            boolean elementMatch = true;
            for (Object o : list) {
                if (o == null) {
                    continue;
                }
                if (!elementType.isInstance(o)) {
                    elementMatch = false;
                    break;
                }
            } // loop
            match = elementMatch;
        } else {
            match = false;
        }
        return match;
    }


    /// @param offset the index of left {@link _Constant#DOUBLE_QUOTE}
    /// @see #skipArrayElement(String, int, int)
    /// @see #parseArrayLength(String, int, int)
    private static int skipQuoteElement(final String text, final int offset, final int endIndex) {
//         private method trust upper
//        if (text.charAt(offset) != _Constant.DOUBLE_QUOTE) {
//            throw new IllegalArgumentException();
//        }
        int rightIndex = -1;
        char ch;
        for (int i = offset + 1; i < endIndex; i++) {
            ch = text.charAt(i);

            if (ch == _Constant.BACK_SLASH) {
                i++; // skip current char
                continue;
            }
            if (ch == _Constant.DOUBLE_QUOTE) {
                rightIndex = i;
                break;
            }

        } // loop

        if (rightIndex < 0) {
            throw _Exceptions.missingClosingError(text, endIndex, _Constant.DOUBLE_QUOTE);
        }
        return rightIndex;
    }

    /// @param offset the index of first not whitespace char
    /// @see #skipArrayElement(String, int, int)
    /// @see #parseArrayLength(String, int, int)
    private static int skipUnquotedElement(final String text, final int offset, final int endIndex) {
//        private method trust upper
//        final char firstChar;
//        firstChar = text.charAt(offset);
//
//        if (firstChar == _Constant.COMMA || firstChar == _Constant.RIGHT_BRACE) {
//            throw new IllegalArgumentException();
//        } else if (Character.isWhitespace(firstChar)) {
//            throw new IllegalArgumentException();
//        }

        int rightIndex = -1;
        char ch;
        for (int i = offset + 1; i < endIndex; i++) {
            ch = text.charAt(i);

            if (ch == _Constant.COMMA || ch == _Constant.RIGHT_BRACE) {
                rightIndex = i - 1;
                break;
            }

            if (ch == _Constant.DOUBLE_QUOTE) {
                throw _Exceptions.unexpectedQuoteError(text, i, ch);
            }

        } // loop

        if (rightIndex < 0) {
            throw _Exceptions.missingEndingError(text, endIndex, new char[]{_Constant.COMMA, _Constant.RIGHT_BRACE});
        }
        return rightIndex;
    }

    /// @param offset the index of left {@link _Constant#LEFT_BRACE}
    /// @see #skipQuoteElement(String, int, int)
    /// @see #skipUnquotedElement(String, int, int)
    private static int skipArrayElement(final String text, final int offset, final int endIndex) {
//        private method trust upper
//        if (text.charAt(offset) != _Constant.LEFT_BRACE) {
//            throw new IllegalArgumentException();
//        }
        int rightIndex = -1;
        char ch;
        for (int i = offset + 1, commaFlag = -1; i < endIndex; i++) {
            ch = text.charAt(i);
            if (ch == _Constant.RIGHT_BRACE) {
                rightIndex = i;
                break;
            }

            if (commaFlag > -1) {
                if (ch == _Constant.COMMA) {
                    commaFlag = -1;
                    continue;
                } else if (Character.isWhitespace(ch)) {
                    continue;
                }
                throw _Exceptions.missDelimError(text, offset, _Constant.COMMA);
            }

            if (ch == _Constant.LEFT_BRACE) {
                i = skipArrayElement(text, i, endIndex);
                commaFlag = i;
            } else if (ch == _Constant.DOUBLE_QUOTE) {
                i = skipQuoteElement(text, i, endIndex);
                commaFlag = i;
            } else if (ch == _Constant.COMMA) {
                throw _Exceptions.redundantDelimError(text, i, _Constant.COMMA);
            } else if (!Character.isWhitespace(ch)) {
                i = skipUnquotedElement(text, i, endIndex);
                commaFlag = i;
            }

        } // loop

        if (rightIndex < 0) {
            throw _Exceptions.missingClosingError(text, endIndex, _Constant.RIGHT_BRACE);
        }
        return rightIndex;
    }

    /// @see #DEFAULT_DESERIALIZER
    /// @see BoxArrayDeserializerHolder#BOX_DESERIALIZER
    private static ItemsDeserializer.Builder createDefaultDeserializerBuilder() {
        return ItemsDeserializer.builder()
                .dimensionFunc(PostgreArrays::parseArrayDimension)
                .lengthFunc(PostgreArrays::parseArrayLength)
                .backSlashEscapeOn(true)
                .quoteEscapeOn(false)
                .quoteChar(_Constant.DOUBLE_QUOTE);
    }


    /// @see #DEFAULT_SERIALIZER
    /// @see BoxArrayDeserializerHolder#BOX_SERIALIZER
    private static ArraySerializer.Builder createDefaultSerializerBuilder() {
        return ArraySerializer.builder()
                .leftBoundary(_Constant.LEFT_BRACE)
                .rightBoundary(_Constant.RIGHT_BRACE)
                ;
    }


    private static final class BoxArrayDeserializerHolder {

        /// @see #DEFAULT_DESERIALIZER
        private static final ItemsDeserializer BOX_DESERIALIZER = createDefaultDeserializerBuilder()
                .delim(_Constant.SEMICOLON)
                .build();

        /// @see #DEFAULT_SERIALIZER
        private static final ArraySerializer BOX_SERIALIZER = createDefaultSerializerBuilder()
                .delimChar(_Constant.SEMICOLON)
                .build();
    }


}
