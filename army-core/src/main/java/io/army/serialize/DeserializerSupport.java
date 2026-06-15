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

import java.util.function.Consumer;

abstract class DeserializerSupport {


    final char itemDelim;


    final char quoteChar;

    final boolean backSlashEscape;

    final boolean quoteEscape;

    DeserializerSupport(BuilderSupport builder) {
        this.itemDelim = builder.itemDelim;

        this.quoteChar = builder.quoteChar;
        this.backSlashEscape = builder.backSlashEscape;
        this.quoteEscape = builder.quoteEscape;
    }


    /// @param offset the next index of left quote
    /// @return the index of right quote
    final int parseQuoteElement(final String text, final int offset, final int endIndex, final char quote,
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

                if (i > lastWritten) {
                    builder.append(text, lastWritten, i);
                }

                i++; // skip escape(current char),
                lastWritten = i;

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
            throw _Exceptions.missingClosingError(text, endIndex, quote);
        }
        return rightIndex;
    }


    /// @param offset the index of left quote
    final int skipQuoteElement(final String text, final int offset, final int endIndex) {
//         private method trust upper
//        if (text.charAt(offset) != this.quoteChar) {
//            throw new IllegalArgumentException();
//        }

        final boolean backSlashEscape, quoteEscape;
        backSlashEscape = this.backSlashEscape;
        quoteEscape = this.quoteEscape;

        final char quoteChar = this.quoteChar;

        int rightIndex = -1;
        char ch;
        for (int i = offset + 1, nextIndex; i < endIndex; i++) {
            ch = text.charAt(i);

            if (ch == _Constant.BACK_SLASH && backSlashEscape) {
                i++; // skip current char
                continue;
            }
            if (ch != quoteChar) {
                continue;
            }

            if (quoteEscape
                    && (nextIndex = i + 1) < endIndex
                    && text.charAt(nextIndex) == quoteChar) {
                i++; // skip current char
                continue;
            }

            rightIndex = i;
            break;

        } // loop

        if (rightIndex < 0) {
            throw _Exceptions.missingClosingError(text, endIndex, quoteChar);
        }
        return rightIndex;
    }


    /// @param offset the index of first not whitespace char
    final int skipUnquotedElement(final String text, final int offset, final int endIndex, final char rightBoundary,
                                  final @Nullable char[] boundaries, @Nullable TextToIntFunc subFunc) {
//        private method trust upper
//        final char firstChar;
//        firstChar = text.charAt(offset);
//
//        if (firstChar == arrayDelim || firstChar == rightBoundary) {
//            throw new IllegalArgumentException();
//        } else if (Character.isWhitespace(firstChar)) {
//            throw new IllegalArgumentException();
//        }


        final char quoteChar = this.quoteChar, itemDelim = this.itemDelim;

        int rightIndex = -1;
        char ch;
        for (int i = offset + 1, oldIndex; i < endIndex; i++) {
            ch = text.charAt(i);

            if (boundaries != null && isBoundaries(boundaries, ch)) {
                assert subFunc != null;
                oldIndex = i;
                i = subFunc.apply(text, i, endIndex);
                if (i <= oldIndex) {
                    throw subFuncBug(subFunc);
                }
                continue;
            }

            if (ch == itemDelim || ch == rightBoundary) {
                rightIndex = i - 1;
                break;
            }

            if (ch == quoteChar) {
                throw _Exceptions.unexpectedQuoteError(text, i, ch);
            }

        } // loop

        if (rightIndex < 0) {
            throw unquotedElementNotEnd(text, endIndex);
        }
        return rightIndex;
    }


    static boolean isBoundaries(final char[] boundaries, final char ch) {
        boolean match = false;
        for (char boundary : boundaries) {
            if (boundary == ch) {
                match = true;
                break;
            }
        }
        return match;
    }

    static IllegalArgumentException subFuncBug(@Nullable TextToIntFunc subFunc) {
        return new IllegalArgumentException(String.format("subFunc[%s] bug", subFunc));
    }

    static IllegalArgumentException unquotedElementNotEnd(String text, int offset) {
        String m = String.format("unquoted element not end at nearby offset[%s] -> %s",
                offset, _StringUtils.surroundingText(text, offset, 4));
        return new IllegalArgumentException(m);
    }

    static IllegalArgumentException syntaxError(String target, String text, int offset) {
        String m = String.format("syntax[%s] error at nearby offset[%s] -> %s",
                target, offset, _StringUtils.surroundingText(text, offset, 4));
        return new IllegalArgumentException(m);
    }

    static abstract class BuilderSupport {

        char itemDelim = _Constant.COMMA;

        char quoteChar = _Constant.DOUBLE_QUOTE;

        boolean backSlashEscape;

        boolean quoteEscape;


    } // BuilderSupport


}
