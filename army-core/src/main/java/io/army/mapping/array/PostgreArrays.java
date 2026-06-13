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


    /// @see BoxArrayDeserializerHolder#BOX_DESERIALIZER
    private static final ItemsDeserializer DEFAULT_DESERIALIZER = ItemsDeserializer.builder()
            .delim(_Constant.COMMA)
            .dimensionFunc(PostgreArrays::parseArrayDimension)
            .lengthFunc(PostgreArrays::parseArrayLength)
            .backSlashEscapeOn(true)
            .quoteEscapeOn(false)
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
                                         final DataType dataType, final MappingType type) {
        try {
            final Class<?> javaType = type.javaType();
            if (javaType != Object.class && !ClassUtils.isAssignableFrom(javaType, source.getClass())) {
                throw UserMappingType.paramError(type, dataType, source, null);
            }
            final ArraySerializer parser;
            if (dataType == PgType.BOX_ARRAY) {
                parser = ArraySerializer.builder()
                        .delimChar(';')
                        .build();
            } else {
                parser = ArraySerializer.defaultParser();
            }

            return parser.parse(((MappingType.SqlArray) type).underlyingJavaType(), source, consumer);
        } catch (Exception e) {
            throw UserMappingType.paramError(type, dataType, source, e);
        }
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
        char ch;
        boolean leftBrace = true, inBrace = false, inQuote = false, arrayEnd = false;

        int length = 0;
        for (int i = offset, itemCount = 0; i < endIndex; i++) {
            ch = text.charAt(i);
            if (leftBrace) {
                if (ch == _Constant.LEFT_BRACE) {
                    leftBrace = false;
                } else if (!Character.isWhitespace(ch)) {
                    throw isNotWhitespaceError(i);
                }
            } else if (inQuote) {
                if (ch == _Constant.BACK_SLASH) {
                    i++;
                } else if (ch == _Constant.DOUBLE_QUOTE) {
                    inQuote = false;
                }
            } else if (inBrace) {
                if (ch == _Constant.RIGHT_BRACE) {
                    inBrace = false;
                }
            } else if (ch == _Constant.LEFT_BRACE) {
                itemCount++;
                inBrace = true;
            } else if (ch == _Constant.DOUBLE_QUOTE) {
                itemCount++;
                inQuote = true;
            } else if (ch == _Constant.COMMA) {
                length++;
            } else if (ch == _Constant.RIGHT_BRACE) {
                if (itemCount > 0) {
                    length++;
                }
                arrayEnd = true;
                break;
            } else if (itemCount == 0 && !Character.isWhitespace(ch)) {
                itemCount++;
            }

        }
        if (leftBrace) {
            throw noLeftBrace(offset);
        } else if (!arrayEnd) {
            throw noRightBrace(endIndex);
        }
        return length;
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


    /// @see #arrayAfterGet(MappingType, DataType, Object, TextFunction)
    private static boolean isInstanceOfType(final MappingType type, final Object source) {
        final Class<?> javaType = type.javaType();
        final boolean match;
        if (javaType == Object.class) {
            match = source.getClass().isArray();
        } else if (javaType.isArray()) {
            match = javaType.isInstance(source);
        } else if (!List.class.isAssignableFrom(javaType)) {
            match = false;
        } else if (source instanceof List<?> list) {
            final Class<?> elementType = ((UnaryGenericsMapping) type).genericsType();
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


    private static IllegalArgumentException noRightBrace(int end) {
        return new IllegalArgumentException(String.format("postgre array no right brace before offset[%s] nearby", end));
    }

    private static IllegalArgumentException noLeftBrace(int offset) {
        return new IllegalArgumentException(String.format("postgre array no left brace at offset[%s] nearby", offset));
    }

    private static IllegalArgumentException isNotWhitespaceError(int offset) {
        return new IllegalArgumentException(String.format("postgre array error at offset[%s]", offset));
    }


    private static final class BoxArrayDeserializerHolder {

        /// @see #DEFAULT_DESERIALIZER
        private static final ItemsDeserializer BOX_DESERIALIZER = ItemsDeserializer.builder()
                .delim(_Constant.SEMICOLON)
                .dimensionFunc(PostgreArrays::parseArrayDimension)
                .lengthFunc(PostgreArrays::parseArrayLength)
                .backSlashEscapeOn(true)
                .quoteEscapeOn(false)
                .build();
    }


}
