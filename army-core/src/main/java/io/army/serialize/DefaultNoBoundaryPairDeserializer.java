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
import io.army.util._Exceptions;
import io.army.util._StringUtils;

import java.util.Objects;
import java.util.function.BiConsumer;

final class DefaultNoBoundaryPairDeserializer extends ArmyDeserializer implements NoBoundaryPairDeserializer {

    static Builder newBuilder() {
        return new DefaultBuilder();
    }

    private final String separator;

    private final char firstCharOfSeparator;

    private final int separatorLength;

    private final String nullRepresenting;

    private DefaultNoBoundaryPairDeserializer(DefaultBuilder builder) {
        super(builder);
        this.separator = Objects.requireNonNull(builder.separator);
        this.firstCharOfSeparator = this.separator.charAt(0);
        this.separatorLength = separator.length();
        this.nullRepresenting = String.valueOf(this.itemDelim);

    }


    @Override
    public void deserialize(final String text, final int offset, final int endIndex, BiConsumer<String, String> func,
                            @Nullable char[] boundaries, @Nullable TextToIntFunc skipFunc, @Nullable StringBuilder builder) {
        if (offset >= endIndex) {
            throw new IllegalArgumentException();
        } else if ((boundaries == null) != (skipFunc == null)) {
            throw new IllegalArgumentException();
        } else if (boundaries != null && isBoundaries(boundaries, this.itemDelim)) {
            throw new IllegalArgumentException();
        } else if (boundaries != null && isBoundaries(boundaries, this.firstCharOfSeparator)) {
            throw new IllegalArgumentException();
        } else if ((endIndex - offset) < 2 + this.separatorLength) {
            throw new IllegalArgumentException();
        }

        Objects.requireNonNull(func);

        final String separator = this.separator;

        final char itemDelim = this.itemDelim, quoteChar = this.quoteChar;
        final char firstCharOfSeparator = this.firstCharOfSeparator;

        final int separatorLength = this.separatorLength, lastIndex = endIndex - 1;

        final boolean allowNothing = this.allowNothing, allowWhitespace = this.allowWhitespace;


        final StringBuilder[] holder = new StringBuilder[1];
        holder[0] = builder;

        final String[] keyHolder = new String[2];
        final int[] itemFlagHolder = new int[1];
        final TextFunction<Void> function;
        function = (srcText, srcOffset, srcEnd) -> {
            final String value;
            if (srcOffset == 0 && srcEnd == srcOffset && this.nullRepresenting.equals(srcText)) {
                value = null;
            } else {
                value = srcText.substring(srcOffset, srcEnd);
            }
            if (keyHolder[0] == null) {
                if (value == null) {
                    throw keyIsNull();
                }
                keyHolder[0] = value;
            } else {
                func.accept(keyHolder[0], value);
                keyHolder[0] = null;
                itemFlagHolder[0] = 0;
            }
            return null;
        };


        char ch;
        for (int i = offset, delimFlag = 0, separatorFlag = 0, oldIndex; i < endIndex; i++) {
            ch = text.charAt(i);

            if (separatorFlag > 0) {
                if (ch != firstCharOfSeparator) {
                    if (Character.isWhitespace(ch)) {
                        continue;
                    }
                    throw _Exceptions.missSeparatorError(text, i, separator);
                } else if (text.regionMatches(false, i, separator, 0, separatorLength)) {
                    i += (separatorLength - 1);
                    separatorFlag = 0;
                    continue;
                }
                throw _Exceptions.missSeparatorError(text, i, separator);
            }

            if (delimFlag > 0) {
                if (ch == itemDelim) {
                    delimFlag = 0;
                    itemFlagHolder[0] = 1;
                    continue;
                } else if (Character.isWhitespace(ch)) {
                    continue;
                }
                throw _Exceptions.missDelimError(text, i, itemDelim);
            }

            if (ch == quoteChar) {
                i = parseQuoteElement(text, i + 1, endIndex, ch, holder, null, function);
                delimFlag = 1;
            } else if (boundaries != null && isBoundaries(boundaries, ch)) {
                oldIndex = i;
                i = skipFunc.apply(text, i, endIndex);
                if (i <= oldIndex || i > endIndex) {
                    throw subFuncBug(skipFunc);
                }
                function.apply(text, oldIndex, i + 1);
                delimFlag = 1;
            } else if (ch == itemDelim) {
                if (keyHolder[0] == null) {
                    throw _Exceptions.redundantDelimError(text, i, itemDelim);
                } else if (allowNothing) {
                    function.apply(text, 0, 0);  // representing nothing
                    continue;
                }
                throw _Exceptions.redundantDelimError(text, i, itemDelim);
            } else if (ch == firstCharOfSeparator && text.regionMatches(false, i, separator, 0, separatorLength)) {
                throw _Exceptions.redundantSeparatorError(text, i, separator);
            } else {
                if (!allowWhitespace && Character.isWhitespace(ch)) {
                    continue;
                }
                i = parseUnQuoteElement(text, i, endIndex, function, boundaries, skipFunc);

                delimFlag = 1;
            }

            if (keyHolder[0] != null) {
                separatorFlag = 1;
                delimFlag = 0;
            }


        } // loop

        if (keyHolder[0] != null) {
            throw _Exceptions.missingSeparatorError(text, endIndex, separator);
        } else if (itemFlagHolder[0] > 0) {
            String m = String.format("error tailer at nearby offset[%s] -> %s",
                    endIndex - 1, _StringUtils.surroundingText(text, endIndex, 4));
            throw new IllegalArgumentException(m);
        }

    } // deserialize

    /// @return the previous index of array delim or right boundary
    private int parseUnQuoteElement(final String text, final int offset, final int endIndex, TextFunction<?> func,
                                    final @Nullable char[] boundaries, @Nullable TextToIntFunc subFunc) {
        final char firstChar;
        firstChar = text.charAt(offset);

        final String separator = this.separator;
        final char firstCharOfSeparator = this.firstCharOfSeparator;
        final int separatorLength = this.separatorLength;

        final char itemDelim = this.itemDelim, quoteChar = this.quoteChar;

        final boolean allowWhitespace = this.allowWhitespace;
        final boolean nullAsNull = this.nullAsNull;

        final boolean firstIsN = nullAsNull && (firstChar == 'n' || firstChar == 'N');

        final int lastIndex = endIndex - 1;

        int rightIndex = -1;

        boolean nullValue, separatorBoundary;
        char ch;
        for (int i = offset + 1, elementEndInex = -1, oldIndex, eleEndIndex; i < endIndex; i++) {
            ch = text.charAt(i);

            if (boundaries != null && isBoundaries(boundaries, ch)) {
                oldIndex = i;
                assert subFunc != null;
                i = subFunc.apply(text, i, endIndex);
                if (i <= oldIndex) {
                    throw subFuncBug(subFunc);
                }
                continue;
            }

            separatorBoundary = ch == firstCharOfSeparator
                    && text.regionMatches(false, i, separator, 0, separatorLength);

            if (ch == itemDelim || separatorBoundary || i == lastIndex) {

                if (i == lastIndex && !separatorBoundary && ch != itemDelim) {
                    eleEndIndex = endIndex;
                } else {
                    eleEndIndex = i;
                }


                if (elementEndInex < 0) {
                    nullValue = firstIsN
                            && eleEndIndex - offset == 4
                            && text.regionMatches(true, offset, _Constant.NULL, 0, 4);
                    if (nullValue) {
                        func.apply(this.nullRepresenting, 0, 0);
                    } else {
                        func.apply(text, offset, eleEndIndex);
                    }

                }

                if (eleEndIndex == endIndex) {
                    rightIndex = i;
                } else {
                    rightIndex = i - 1;
                }

                break;
            }


            if (ch == quoteChar) {
                throw _Exceptions.unexpectedQuoteError(text, i, ch);
            }

            if (allowWhitespace) {
                continue;
            }

            if (!Character.isWhitespace(ch)) {
                continue;
            }

            if (elementEndInex >= 0) {
                String m = String.format("unquoted element exists whitespace at nearby offset[%s] -> %s",
                        i, _StringUtils.surroundingText(text, i, 4));
                throw new IllegalArgumentException(m);
            }

            nullValue = i - offset == 4
                    && firstIsN
                    && text.regionMatches(true, offset, _Constant.NULL, 0, 4);
            if (nullValue) {
                func.apply(this.nullRepresenting, 0, 0);
            } else {
                func.apply(text, offset, i);
            }

            elementEndInex = i;

        } // loop

        if (rightIndex < 0) {
            throw unquotedElementNotEnd(text, endIndex);
        }
        return rightIndex;
    }

    private IllegalArgumentException containSeparatorFirstCharError(String text, int offset) {
        String m = String.format("unquoted element contain separator first char[%s] at nearby offset[%s] -> %s",
                this.firstCharOfSeparator, offset, _StringUtils.surroundingText(text, offset, 4));
        return new IllegalArgumentException(m);
    }

    private static IllegalArgumentException keyIsNull() {
        return new IllegalArgumentException("exists key is null");
    }


    private static final class DefaultBuilder extends ArmyDeserializer.ArmyBuilder<NoBoundaryPairDeserializer.Builder>
            implements NoBoundaryPairDeserializer.Builder {

        private String separator;

        @Override
        public Builder pairSeparator(String separator) {
            this.separator = separator;
            return this;
        }

        @Override
        public NoBoundaryPairDeserializer build() {
            final String separator = this.separator;
            if (!_StringUtils.hasText(separator) || separator.indexOf(this.itemDelim) > -1) {
                throw new IllegalArgumentException();
            }
            final char first = separator.charAt(0);
            if (this.nullAsNull && (first == 'n' || first == 'N')) {
                throw new IllegalArgumentException();
            }
            return new DefaultNoBoundaryPairDeserializer(this);
        }

    } // DefaultBuilder


}
