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


final class DefaultRecordDeserializer extends DeserializerSupport implements RecordDeserializer {

    static RecordDeserializer.Builder newBuilder() {
        return new DefaultBuilder();
    }


    private final char leftBoundary;

    private final char rightBoundary;

    private final boolean allowNothing;

    private final boolean allowWhitespace;

    private DefaultRecordDeserializer(DefaultBuilder builder) {
        super(builder);
        this.leftBoundary = builder.leftBoundary;
        this.rightBoundary = builder.rightBoundary;
        this.allowNothing = builder.allowNothing;
        this.allowWhitespace = builder.allowWhitespace;
    }


    @Override
    public void deserialize(String text, int offset, int endIndex, TextFunction<?> func,
                            @Nullable char[] boundaries, @Nullable TextToIntFunc skipFunc, @Nullable StringBuilder builder) {

        if (text.charAt(offset) != this.leftBoundary) {
            throw new IllegalArgumentException();
        } else if ((boundaries == null) != (skipFunc == null)) {
            throw new IllegalArgumentException();
        } else if (offset >= endIndex) {
            throw new IllegalArgumentException();
        } else if (boundaries != null && isBoundaries(boundaries, this.leftBoundary)) {
            throw new IllegalArgumentException();
        }

        Objects.requireNonNull(func);

        final char leftBoundary = this.leftBoundary, itemDelim = this.itemDelim, rightBoundary = this.rightBoundary;
        final char quoteChar;
        quoteChar = this.quoteChar;

        final boolean allowNothing = this.allowNothing, allowWhitespace = this.allowWhitespace;

        final StringBuilder[] holder = new StringBuilder[1];
        holder[0] = builder;

        char ch;

        int rightIndex = -1;

        for (int i = offset + 1, delimFlat = 0, itemCount = 0, delimCount = 0; i < endIndex; i++) {
            ch = text.charAt(i);

            if (ch == rightBoundary) {
                if (itemCount <= delimCount) {
                    if (!allowNothing) {
                        throw new IllegalArgumentException(String.format("expected end at nearby offset[%s] -> %s",
                                i, _StringUtils.surroundingText(text, i, 4)));
                    }
                    func.apply("", 0, 0);
                }
                rightIndex = i;
                break;
            }

            if (delimFlat > 0) {
                if (ch == itemDelim) {
                    delimFlat = 0;
                    delimCount++;
                    continue;
                } else if (Character.isWhitespace(ch)) {
                    continue;
                }
                throw _Exceptions.missDelimError(text, i, itemDelim);
            }

            if (ch == leftBoundary) {
                i = parseSubRecord(text, i, endIndex, func, boundaries, skipFunc);
                delimFlat = 1;
                itemCount++;
            } else if (ch == quoteChar) {
                i = parseQuoteElement(text, i + 1, endIndex, ch, holder, null, func);
                delimFlat = 1;
                itemCount++;
            } else if (ch == itemDelim) {
                if (allowNothing) {
                    func.apply("", 0, 0);
                    itemCount++;
                    delimCount++;
                    continue;
                }
                throw _Exceptions.redundantDelimError(text, i, itemDelim);
            } else {
                if (!allowWhitespace && Character.isWhitespace(ch)) {
                    continue;
                }
                i = parseUnQuoteElement(text, i, endIndex, func, boundaries, skipFunc);
                delimFlat = 1;
                itemCount++;
            }


        } // loop


        if (rightIndex < 0) {
            throw _Exceptions.missingEndingError(text, endIndex, new char[]{rightBoundary});
        }

    }

    @Override
    public int skipRecord(String text, int offset, int endIndex) {
        return parseSubRecord(text, offset, endIndex, null, null, null);
    }

    @Override
    public int skipRecord(String text, int offset, int endIndex, @Nullable char[] boundaries, @Nullable TextToIntFunc subFunc) {
        return parseSubRecord(text, offset, endIndex, null, boundaries, subFunc);
    }

    private int parseSubRecord(final String text, final int offset, final int endIndex, @Nullable TextFunction<?> func,
                               final @Nullable char[] boundaries, final @Nullable TextToIntFunc subFunc) {

        final char leftBoundary = this.leftBoundary, itemDelim = this.itemDelim, rightBoundary = this.rightBoundary;
        final char quoteChar = this.quoteChar;

        if (text.charAt(offset) != leftBoundary) {
            throw new IllegalArgumentException();
        }

        int rightIndex = -1;
        char ch;

        for (int i = offset + 1, delimFlat = 0; i < endIndex; i++) {
            ch = text.charAt(i);

            if (ch == rightBoundary) {
                rightIndex = i;
                break;
            }

            if (delimFlat > 0) {
                if (ch == itemDelim) {
                    delimFlat = 0;
                    continue;
                } else if (Character.isWhitespace(ch)) {
                    continue;
                }
                throw _Exceptions.missDelimError(text, i, itemDelim);
            }

            if (ch == leftBoundary) {
                i = parseSubRecord(text, i, endIndex, null, boundaries, subFunc); // skip sub record , so func is null , TODO 实测,postgre 不允许 这样的子 record,是否去除
                delimFlat = 1;
            } else if (ch == quoteChar) {
                i = skipQuoteElement(text, i, endIndex);
                delimFlat = 1;
            } else if (ch == itemDelim) {
                if (this.allowNothing) {
                    continue;
                }
                throw _Exceptions.redundantDelimError(text, i, itemDelim);
            } else if (Character.isWhitespace(ch)) {
                continue;
            } else {
                i = skipUnquotedElement(text, i, endIndex, rightBoundary, boundaries, subFunc);
                delimFlat = 1;
            }


        } // loop

        if (rightIndex < 0) {
            throw _Exceptions.missingEndingError(text, endIndex, new char[]{rightBoundary});
        }

        if (func != null) {
            func.apply(text, offset, rightBoundary); // parse sub record
        }
        return rightIndex;
    }


    /// @return the previous index of array delim or right boundary
    private int parseUnQuoteElement(final String text, final int offset, final int endIndex, TextFunction<?> func,
                                    final @Nullable char[] boundaries, @Nullable TextToIntFunc subFunc) {

        final char itemDelim = this.itemDelim, quoteChar = this.quoteChar, rightBoundary = this.rightBoundary;

        final boolean allowWhitespace = this.allowWhitespace;

        int rightIndex = -1;

        char ch;
        for (int i = offset + 1, elementEndInex = -1, oldIndex; i < endIndex; i++) {
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

            if (ch == itemDelim || ch == rightBoundary) {

                if (elementEndInex < 0) {
                    func.apply(text, offset, i);
                }

                rightIndex = i - 1;
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

            if (elementEndInex < 0) {
                func.apply(text, offset, i);
                elementEndInex = i;
            } else {
                String m = String.format("unquoted element exists whitespace at nearby offset[%s] -> %s",
                        i, _StringUtils.surroundingText(text, i, 4));
                throw new IllegalArgumentException(m);
            }

        } // loop

        if (rightIndex < 0) {
            throw unquotedElementNotEnd(text, endIndex);
        }
        return rightIndex;
    }


    private static final class DefaultBuilder extends DeserializerSupport.BuilderSupport implements Builder {

        private char leftBoundary = _Constant.LEFT_BRACE;

        private char rightBoundary = _Constant.RIGHT_BRACE;

        private boolean allowNothing;

        private boolean allowWhitespace;

        private DefaultBuilder() {
        }

        @Override
        public Builder leftBoundary(char ch) {
            this.leftBoundary = ch;
            return this;
        }

        @Override
        public Builder delim(char ch) {
            this.itemDelim = ch;
            return this;
        }

        @Override
        public Builder rightBoundary(char ch) {
            this.rightBoundary = ch;
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
        public Builder quoteChar(char ch) {
            this.quoteChar = ch;
            return this;
        }

        @Override
        public Builder allowNothing(boolean yes) {
            this.allowNothing = yes;
            return this;
        }

        @Override
        public Builder allowWhitespace(boolean yes) {
            this.allowWhitespace = yes;
            return this;
        }

        @Override
        public RecordDeserializer build() {
            if (this.quoteChar != _Constant.DOUBLE_QUOTE && this.quoteChar != _Constant.QUOTE) {
                throw new IllegalArgumentException("quoteChar must be \" or '");
            }
            return new DefaultRecordDeserializer(this);
        }

    } // DefaultBuilder


}
