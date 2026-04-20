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

final class DefaultItemsParser implements ItemsParser {

    static Builder newBuilder() {
        return new DefaultBuilder();
    }

    static final ItemsParser DEFAULT = newBuilder().build();

    private final char parenDelim;

    private final char squareDelim;

    private final char curlyDelim;

    private final boolean backslashEscape;

    private DefaultItemsParser(DefaultBuilder builder) {
        this.parenDelim = builder.parenDelim;
        this.squareDelim = builder.squareDelim;
        this.curlyDelim = builder.curlyDelim;
        this.backslashEscape = builder.escape;
    }

    @Override
    public void parseItems(final String text, final int offset, final int endIndex, final TextFunction<?> func) {
        final char leftBoundary = text.charAt(offset), rightBoundary, delim;
        switch (leftBoundary) {
            case '(':
                rightBoundary = ')';
                delim = this.parenDelim;
                break;
            case '[':
                rightBoundary = ']';
                delim = this.squareDelim;
                break;
            case '{':
                rightBoundary = '}';
                delim = this.curlyDelim;
                break;
            default:
                String m = String.format("char['%s'] at offset[%s] is not boundary[ '(', '[', '{' ]", text.charAt(offset), offset);
                throw new IllegalArgumentException(m);
        }

        StringBuilder[] builderHolder = null;
        StringBuilder builder;
        String subText;
        char ch, boundary;
        boolean bockEnd = false;
        topLoop:
        for (int i = offset + 1, rightIndex, itemCount = 0, delimCount = 0, nextIndex; i < endIndex; i++) {
            ch = text.charAt(i);

            if (ch == rightBoundary) {
                bockEnd = true;
                break;
            }

            if (ch == _Constant.QUOTE || ch == _Constant.DOUBLE_QUOTE) {
                if (itemCount > 0 && itemCount != delimCount) {
                    throw blockAbsentDelim(i, delim);
                }
                if (builderHolder == null) {
                    builderHolder = new StringBuilder[1];
                }
                nextIndex = i + 1;
                rightIndex = rightIndexOfQuote(text, nextIndex, endIndex, ch, builderHolder);
                builder = builderHolder[0];

                if (builder != null && !builder.isEmpty()) {
                    subText = builder.toString();
                    func.apply(text, 0, subText.length());
                } else {
                    func.apply(text, nextIndex, rightIndex + 1);
                }
                i = rightIndex;
                itemCount++;
                continue;
            }

            switch (ch) {
                case '(':
                    boundary = ')';
                    break;
                case '[':
                    boundary = ']';
                    break;
                case '{':
                    boundary = '}';
                    break;
                default: {
                    if (ch == delim) {
                        delimCount++;
                        if (delimCount > itemCount) {
                            String m = String.format("delim[%s] duplicate at index[%s]", delim, i);
                            throw new IllegalArgumentException(m);
                        }
                        continue;
                    }
                    if (Character.isWhitespace(ch)) {
                        continue;
                    }
                    rightIndex = -1;
                    for (int index = i + 1; index < endIndex; index++) {
                        ch = text.charAt(index);
                        if (ch == delim) {
                            delimCount++;
                        } else if (ch == rightBoundary) {
                            bockEnd = true;
                        } else if (!Character.isWhitespace(ch)) {
                            continue;
                        }
                        rightIndex = index;
                        break;
                    } // for loop

                    if (rightIndex < 0) {
                        // error,item no end
                        break topLoop;
                    }
                    if (itemCount > 0 && itemCount != delimCount) {
                        throw blockAbsentDelim(i, delim);
                    }
                    func.apply(text, i, rightIndex); // here is rightIndex not rightIndex + 1
                    i = rightIndex;
                    itemCount++;
                    if (bockEnd) {
                        break topLoop;
                    }
                    continue;
                } // default

            } // switch

            if (itemCount > 0 && itemCount != delimCount) {
                throw blockAbsentDelim(i, delim);
            }
            rightIndex = rightIndexOfBlock(text, i + 1, endIndex, boundary);
            func.apply(text, i, rightIndex + 1);
            i = rightIndex;
            itemCount++;
        }

        if (!bockEnd) {
            throw blockNoEndError(offset, rightBoundary);
        }
    }


    private int rightIndexOfBlock(final String text, final int offset, final int endIndex, final char rightBoundary) {
        char ch, boundary;

        int rightIndex = -1;

        for (int i = offset; i < endIndex; i++) {
            ch = text.charAt(i);

            if (ch == rightBoundary) {
                rightIndex = i;
                break;
            }

            if (ch == _Constant.QUOTE || ch == _Constant.DOUBLE_QUOTE) {
                i = rightIndexOfQuote(text, i + 1, endIndex, ch, null);
                continue;
            }

            switch (ch) {
                case '(':
                    boundary = ')';
                    break;
                case '[':
                    boundary = ']';
                    break;
                case '{':
                    boundary = '}';
                    break;
                default:
                    continue;
            } // switch

            i = rightIndexOfBlock(text, i + 1, endIndex, boundary);


        } // top loop

        if (rightIndex < 0) {
            throw blockNoEndError(offset - 1, text.charAt(offset - 1));
        }

        return rightIndex;
    }

    private int rightIndexOfQuote(final String text, final int offset, final int endIndex, final char quote,
                                  final @Nullable StringBuilder[] holder) {
        char ch;
        int rightIndex = -1;
        StringBuilder builder;
        if (holder == null) {
            builder = null;
        } else if ((builder = holder[0]) != null) {
            builder.setLength(0); // clear
        }

        int lastWritten = offset, escapeCount = 0;
        boolean escape;
        for (int i = offset, nextIndex; i < endIndex; i++) {
            ch = text.charAt(i);

            escape = (ch == _Constant.BACK_SLASH && this.backslashEscape)
                    || (ch == quote && (nextIndex = i + 1) < endIndex && text.charAt(nextIndex) == quote);
            if (escape) {
                escapeCount++;
                if (holder != null && builder == null) {
                    holder[0] = builder = new StringBuilder();
                }

                if (builder == null) {
                    i++; // skip escape(current char),
                } else if (i > lastWritten) {
                    builder.append(text, lastWritten, i);
                    i++; // skip escape(current char),
                    lastWritten = i;
                } else {
                    i++; // skip escape(current char),
                    lastWritten = i;
                }
                continue;
            }

            if (ch == quote) {
                rightIndex = i;
                break;
            }

        } // top loop


        if (rightIndex < 0) {
            throw blockNoEndError(offset - 1, quote);
        }
        if (builder != null && escapeCount > 0 && lastWritten < rightIndex) {
            builder.append(text, lastWritten, rightIndex);
        }
        return rightIndex;
    }


    private static IllegalArgumentException blockNoEndError(final int index, final char boundary) {
        String m = String.format("text no end after index[%s] for char '%s'", boundary, index);
        return new IllegalArgumentException(m);
    }

    private static IllegalArgumentException blockAbsentDelim(int offset, char delim) {
        String m = String.format("text absent delim[%s] before index[%s]", delim, offset);
        return new IllegalArgumentException(m);
    }


    private final static class DefaultBuilder implements Builder {

        private char parenDelim = ',';

        private char squareDelim = ',';

        private char curlyDelim = ',';

        private boolean escape = true;

        @Override
        public Builder delimForParen(char delim) {
            this.parenDelim = delim;
            return this;
        }

        @Override
        public Builder delimForSquare(char delim) {
            this.squareDelim = delim;
            return this;
        }

        @Override
        public Builder delimForCurly(char delim) {
            this.curlyDelim = delim;
            return this;
        }

        @Override
        public Builder backslashEscape(boolean escape) {
            this.escape = escape;
            return this;
        }

        @Override
        public ItemsParser build() {
            return new DefaultItemsParser(this);
        }


    } // DefaultBuilder
}




