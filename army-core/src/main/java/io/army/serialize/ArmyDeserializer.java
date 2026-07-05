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

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

abstract class ArmyDeserializer implements Deserializer {

    final String name;

    final char itemDelim;

    final char quoteChar;

    final boolean backSlashEscape;

    final boolean quoteEscape;

    final boolean allowNothing;

    final boolean allowWhitespace;

    final boolean allowQuote;

    final boolean nullAsNull;

    ArmyDeserializer(ArmyBuilder<?> builder) {
        if (builder.quoteChar != _Constant.DOUBLE_QUOTE && builder.quoteChar != _Constant.QUOTE) {
            throw new IllegalArgumentException("quoteChar must be \" or '");
        } else if (builder.nullAsNull && builder.allowWhitespace) {
            throw new IllegalArgumentException("nullAsNull and allowWhitespace cannot be true at the same time");
        } else if (!_StringUtils.hasText(builder.name)) {
            throw new IllegalArgumentException("name must have text");
        }
        this.name = Objects.requireNonNull(builder.name);
        this.itemDelim = builder.itemDelim;
        this.quoteChar = builder.quoteChar;
        this.backSlashEscape = builder.backSlashEscape;
        this.quoteEscape = builder.quoteEscape;
        this.allowNothing = builder.allowNothing;
        this.allowQuote = builder.allowQuote;
        this.allowWhitespace = builder.allowWhitespace;
        this.nullAsNull = builder.nullAsNull;
    }


    /// @param offset the next index of left quote
    /// @return the index of right quote
    final int parseQuoteElement(final CharSequence text, final int offset, final int endIndex, final char quote,
                                final StringBuilder[] holder, @Nullable Consumer<Object> consumer, TextFunction<?> func) {

        if (!this.allowQuote) {
            throw currentDeserializerDontSupportQuote(text, offset);
        }
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
            if ((ch == quote && (nextIndex = i + 1) < endIndex && text.charAt(nextIndex) == quote)) {
                if (!quoteEscapeOn) {
                    throw dontSupportQuoteEscape(text, i);
                }
                quoteEscape = true;
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
                value = func.apply(builder, 0, builder.length());
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
    final int skipQuoteElement(final CharSequence text, final int offset, final int endIndex) {
        if (!this.allowQuote) {
            throw currentDeserializerDontSupportQuote(text, offset);
        }

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


    final String getSyntaxType() {
        return this.name;
    }


    private IllegalArgumentException dontSupportQuoteEscape(CharSequence text, int offset) {
        String m = String.format("current deserializer[%s] don't support quote escape,at nearby offset[%s] -> %s",
                getSyntaxType(), offset, _StringUtils.surroundingText(text, offset, 4));
        return new IllegalArgumentException(m);
    }

    private IllegalArgumentException currentDeserializerDontSupportQuote(CharSequence text, int offset) {
        String m = String.format("current deserializer[%s] don't support quote,at nearby offset[%s] -> %s",
                getSyntaxType(), offset, _StringUtils.surroundingText(text, offset, 4));
        return new IllegalArgumentException(m);
    }


    static boolean regionMatches(final CharSequence text, boolean ignoreCase, final int offset,
                                 final int endIndex, final String other) {
        final int otherLength = other.length();
        return (offset + otherLength) <= endIndex
                && _StringUtils.regionMatches(text, ignoreCase, offset, other, 0, otherLength);
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

    static boolean containsBoundaries(final char[] boundaries, final char[] other) {
        boolean match = false;

        topLoop:
        for (char boundary : boundaries) {
            for (char ch : other) {
                if (ch == boundary) {
                    match = true;
                    break topLoop;
                }
            } // inner loop
        } // top loop
        return match;
    }

    static IllegalArgumentException subFuncBug(@Nullable TextToIntFunc subFunc) {
        return new IllegalArgumentException(String.format("subFunc[%s] bug", subFunc));
    }

    static IllegalArgumentException unquotedElementNotEnd(CharSequence text, int offset) {
        String m = String.format("unquoted element not end at nearby offset[%s] -> %s",
                offset, _StringUtils.surroundingText(text, offset, 4));
        return new IllegalArgumentException(m);
    }

    static IllegalArgumentException syntaxError(String target, CharSequence text, int offset) {
        String m = String.format("syntax[%s] error at nearby offset[%s] -> %s",
                target, offset, _StringUtils.surroundingText(text, offset, 4));
        return new IllegalArgumentException(m);
    }


    static abstract class SingleBoundaryDeserializer<B extends SingleBoundaryBuilder<B>> extends ArmyDeserializer {

        final char leftBoundary;

        final char rightBoundary;

        SingleBoundaryDeserializer(ArmySingleBoundaryBuilder<B> builder) {
            super(builder);
            this.leftBoundary = builder.leftBoundary;
            this.rightBoundary = builder.rightBoundary;
        }

        /// @return the previous index of array delim or right boundary
        final int parseUnQuoteElement(final CharSequence text, final int offset, final int endIndex,
                                      final @Nullable Consumer<Object> consumer, TextFunction<?> func,
                                      final @Nullable char[] boundaries, @Nullable TextToIntFunc subFunc) {
            final char firstChar;
            firstChar = text.charAt(offset);

            final char itemDelim = this.itemDelim, quoteChar = this.quoteChar, rightBoundary = this.rightBoundary;

            final boolean allowWhitespace = this.allowWhitespace;
            final boolean nullAsNull = this.nullAsNull;

            final boolean firstIsN = nullAsNull && (firstChar == 'n' || firstChar == 'N');


            int rightIndex = -1;

            Object value;
            boolean nullValue;
            char ch;
            for (int i = offset, elementEndInex = -1, oldIndex; i < endIndex; i++) {
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
                        nullValue = firstIsN
                                && i - offset == 4
                                && regionMatches(text, true, offset, endIndex, "null");
                        if (nullValue) {
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
                        && regionMatches(text, true, offset, endIndex, "null");
                if (nullValue) {
                    value = null;
                } else {
                    value = func.apply(text, offset, i);
                }

                if (consumer != null) {
                    consumer.accept(value);
                }

                elementEndInex = i;

            } // loop

            if (rightIndex < 0) {
                throw unquotedElementNotEnd(text, endIndex);
            }
            return rightIndex;
        }


        final int skipUnquotedElement(final CharSequence text, final int offset, final int endIndex, final char rightBoundary,
                                      final @Nullable char[] boundaries, @Nullable TextToIntFunc subFunc) {

            final char quoteChar = this.quoteChar, itemDelim = this.itemDelim;

            int rightIndex = -1;
            char ch;
            for (int i = offset, oldIndex; i < endIndex; i++) {
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


        final int parseDirectNestedElement(final CharSequence text, final int offset, final int endIndex, @Nullable TextFunction<?> func,
                                           final @Nullable char[] boundaries, final @Nullable TextToIntFunc subFunc) {

            final char leftBoundary = this.leftBoundary, itemDelim = this.itemDelim, rightBoundary = this.rightBoundary;
            final char quoteChar = this.quoteChar;

            final boolean allowWhitespace = this.allowWhitespace, allowNothing = this.allowNothing;

            if (text.charAt(offset) != leftBoundary) {
                throw syntaxError(getSyntaxType(), text, offset);
            } else if (boundaries != null && isBoundaries(boundaries, leftBoundary)) {
                throw new IllegalArgumentException();
            } else if ((boundaries == null) != (subFunc == null)) {
                throw new IllegalArgumentException();
            }

            int rightIndex = -1;
            char ch;

            for (int i = offset + 1, delimFlat = 0, oldIndex; i < endIndex; i++) {
                ch = text.charAt(i);

                if (ch == rightBoundary) {
                    rightIndex = i;
                    break;
                }

                if (delimFlat > 0) {
                    if (ch == itemDelim) {
                        delimFlat = 0;
                        continue;
                    } else if (allowWhitespace && Character.isWhitespace(ch)) {
                        continue;
                    }
                    throw _Exceptions.missDelimError(text, i, itemDelim);
                }

                if (boundaries != null && isBoundaries(boundaries, ch)) {
                    oldIndex = i;
                    i = subFunc.apply(text, i, endIndex); // skip nested element
                    if (i <= oldIndex) {
                        throw subFuncBug(subFunc);
                    }
                    continue;
                }

                if (ch == leftBoundary) {
                    i = parseDirectNestedElement(text, i, endIndex, null, boundaries, subFunc); // here func is null , TODO 实测,postgre 不允许 这样的子 record,是否去除
                    delimFlat = 1;
                } else if (ch == quoteChar) {
                    i = skipQuoteElement(text, i, endIndex);
                    delimFlat = 1;
                } else if (ch == itemDelim) {
                    if (allowNothing) {
                        continue;
                    }
                    throw _Exceptions.redundantDelimError(text, i, itemDelim);
                } else {
                    if (!allowWhitespace && Character.isWhitespace(ch)) {
                        continue;
                    }
                    i = skipUnquotedElement(text, i, endIndex, rightBoundary, boundaries, subFunc);
                    delimFlat = 1;
                }


            } // loop

            if (rightIndex < 0) {
                throw _Exceptions.missingEndingError(text, endIndex, new char[]{rightBoundary});
            }

            if (func != null) {
                func.apply(text, offset, rightIndex); // parse sub record
            }
            return rightIndex;
        } // parseDirectNestedElement


    } // SingleBoundaryDeserializer


    static abstract class MultiBoundaryDeserializer<B extends MultiBoundaryBuilder<B>> extends ArmyDeserializer {

        final char[] leftBoundaries;

        final char[] rightBoundaries;

        MultiBoundaryDeserializer(ArmyMultiBoundaryBuilder<B> builder) {
            super(builder);
            this.leftBoundaries = Objects.requireNonNull(builder.leftBoundaries);
            this.rightBoundaries = Objects.requireNonNull(builder.rightBoundaries);
        }



        /// @return the previous index of array delim or right boundary
        final int parseUnQuoteElement(final CharSequence text, final int offset, final int endIndex,
                                      final @Nullable Consumer<Object> consumer, TextFunction<?> func,
                                      final @Nullable char[] boundaries, @Nullable TextToIntFunc subFunc) {
            final char firstChar;
            firstChar = text.charAt(offset);

            final char itemDelim = this.itemDelim, quoteChar = this.quoteChar;

            final char[] rightBoundaries = this.rightBoundaries;

            final boolean allowWhitespace = this.allowWhitespace, nullAsNull = this.nullAsNull;

            final boolean firstIsN = nullAsNull && (firstChar == 'n' || firstChar == 'N');


            int rightIndex = -1;

            Object value;
            boolean nullValue;
            char ch;
            for (int i = offset, elementEndInex = -1, oldIndex; i < endIndex; i++) {
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

                if (ch == itemDelim || isBoundaries(rightBoundaries, ch)) {

                    if (elementEndInex < 0) {
                        nullValue = firstIsN
                                && i - offset == 4
                                && regionMatches(text, true, offset, endIndex, "null");
                        if (nullValue) {
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
                        && regionMatches(text, true, offset, endIndex, "null");
                if (nullValue) {
                    value = null;
                } else {
                    value = func.apply(text, offset, i);
                }

                if (consumer != null) {
                    consumer.accept(value);
                }

                elementEndInex = i;

            } // loop

            if (rightIndex < 0) {
                throw unquotedElementNotEnd(text, endIndex);
            }
            return rightIndex;
        }

        final int skipUnquotedElement(final CharSequence text, final int offset, final int endIndex, final char[] rightBoundaries,
                                      final @Nullable char[] boundaries, @Nullable TextToIntFunc subFunc) {

            if ((boundaries == null) != (subFunc == null)) {
                throw new IllegalArgumentException();
            }

            final char quoteChar = this.quoteChar, itemDelim = this.itemDelim;

            int rightIndex = -1;
            char ch;
            for (int i = offset + 1, oldIndex; i < endIndex; i++) {
                ch = text.charAt(i);

                if (boundaries != null && isBoundaries(boundaries, ch)) {
                    oldIndex = i;
                    i = subFunc.apply(text, i, endIndex);
                    if (i <= oldIndex) {
                        throw subFuncBug(subFunc);
                    }
                    continue;
                }

                if (ch == itemDelim || isBoundaries(rightBoundaries, ch)) {
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
        } // skipUnquotedElement

        final int parseDirectNestedElement(final CharSequence text, final int offset, final int endIndex,
                                           final boolean allowNestedSelf, @Nullable TextFunction<?> func,
                                           final @Nullable char[] boundaries, final @Nullable TextToIntFunc subFunc) {

            final char[] leftBoundaries = this.leftBoundaries, rightBoundaries = this.rightBoundaries;
            final char quoteChar = this.quoteChar, itemDelim = this.itemDelim;

            final boolean allowWhitespace = this.allowWhitespace, allowNothing = this.allowNothing;

            if (!isBoundaries(leftBoundaries, text.charAt(offset))) {
                throw syntaxError(getSyntaxType(), text, offset);
            } else if (boundaries != null && containsBoundaries(boundaries, leftBoundaries)) {
                throw new IllegalArgumentException();
            } else if ((boundaries == null) != (subFunc == null)) {
                throw new IllegalArgumentException();
            }

            int rightIndex = -1;
            char ch;

            for (int i = offset + 1, delimFlat = 0, oldIndex; i < endIndex; i++) {
                ch = text.charAt(i);

                if (isBoundaries(rightBoundaries, ch)) {
                    rightIndex = i;
                    break;
                }

                if (delimFlat > 0) {
                    if (ch == itemDelim) {
                        delimFlat = 0;
                        continue;
                    } else if (allowWhitespace && Character.isWhitespace(ch)) {
                        continue;
                    }
                    throw _Exceptions.missDelimError(text, i, itemDelim);
                }

                if (boundaries != null && isBoundaries(boundaries, ch)) {
                    oldIndex = i;
                    i = subFunc.apply(text, i, endIndex); // skip nested element
                    if (i <= oldIndex) {
                        throw subFuncBug(subFunc);
                    }
                    continue;
                }

                if (isBoundaries(leftBoundaries, ch)) {
                    if (!allowNestedSelf) {
                        throw syntaxError(getSyntaxType(), text, i);
                    }
                    i = parseDirectNestedElement(text, i, endIndex, true, null, boundaries, subFunc); // here func is null ,
                    delimFlat = 1;
                } else if (ch == quoteChar) {
                    i = skipQuoteElement(text, i, endIndex);
                    delimFlat = 1;
                } else if (ch == itemDelim) {
                    if (allowNothing) {
                        continue;
                    }
                    throw _Exceptions.redundantDelimError(text, i, itemDelim);
                } else {
                    if (!allowWhitespace && Character.isWhitespace(ch)) {
                        continue;
                    }
                    i = skipUnquotedElement(text, i, endIndex, rightBoundaries, boundaries, subFunc);
                    delimFlat = 1;
                }


            } // loop

            if (rightIndex < 0) {
                throw _Exceptions.missingEndingError(text, endIndex, rightBoundaries);
            }

            if (func != null) {
                func.apply(text, offset, rightIndex); // parse nested element
            }
            return rightIndex;
        } // parseDirectNestedElement


    } // MultiBoundaryDeserializer

    @SuppressWarnings("unchecked")
    static abstract class ArmyBuilder<B extends DeserializerBuilder<B>> implements DeserializerBuilder<B> {

        String name = "";

        char itemDelim = _Constant.COMMA;

        char quoteChar = _Constant.DOUBLE_QUOTE;

        boolean backSlashEscape;

        boolean quoteEscape;

        boolean allowNothing;

        boolean allowWhitespace;

        boolean allowQuote = true;

        boolean nullAsNull;


        ArmyBuilder() {
        }

        @Override
        public final B dataTypeLabel(String name) {
            this.name = Objects.requireNonNull(name);
            return (B) this;
        }

        @Override
        public final B delim(char ch) {
            this.itemDelim = ch;
            return (B) this;
        }

        @Override
        public final B backSlashEscapeOn(boolean yes) {
            this.backSlashEscape = yes;
            return (B) this;
        }

        @Override
        public final B quoteEscapeOn(boolean yes) {
            this.quoteEscape = yes;
            return (B) this;
        }

        @Override
        public final B quoteChar(char ch) {
            this.quoteChar = ch;
            return (B) this;
        }

        @Override
        public final B allowNothing(boolean yes) {
            this.allowNothing = yes;
            return (B) this;
        }

        @Override
        public final B allowWhitespace(boolean yes) {
            this.allowWhitespace = yes;
            return (B) this;
        }

        @Override
        public final B allowQuote(boolean yes) {
            this.allowQuote = yes;
            return (B) this;
        }

        @Override
        public final B nullAsNull(boolean yes) {
            this.nullAsNull = yes;
            return (B) this;
        }


    } // ArmyBuilder


    @SuppressWarnings("unchecked")
    static abstract class ArmySingleBoundaryBuilder<B extends SingleBoundaryBuilder<B>>
            extends ArmyBuilder<B> implements SingleBoundaryBuilder<B> {

        char leftBoundary = '{';

        char rightBoundary = '}';


        @Override
        public final B leftBoundary(char ch) {
            this.leftBoundary = ch;
            return (B) this;
        }

        @Override
        public final B rightBoundary(char ch) {
            this.rightBoundary = ch;
            return (B) this;
        }


    } // ArmySingleBoundaryBuilder

    @SuppressWarnings("unchecked")
    static abstract class ArmyMultiBoundaryBuilder<B extends MultiBoundaryBuilder<B>>
            extends ArmyBuilder<B> implements MultiBoundaryBuilder<B> {

        char[] leftBoundaries;

        char[] rightBoundaries;

        @Override
        public final B leftBoundaries(char[] array) {
            this.leftBoundaries = Arrays.copyOf(array, array.length);
            return (B) this;
        }

        @Override
        public final B rightBoundaries(char[] array) {
            this.rightBoundaries = Arrays.copyOf(array, array.length);
            return (B) this;
        }


    } //ArmyMultiBoundaryBuilder


}
