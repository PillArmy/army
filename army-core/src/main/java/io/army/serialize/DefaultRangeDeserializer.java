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

import io.army.function.TextFunction;
import io.army.lang.Nullable;
import io.army.util._Exceptions;

import java.util.Arrays;
import java.util.Objects;

final class DefaultRangeDeserializer extends ArmyDeserializer.MultiBoundaryDeserializer<RangeDeserializer.Builder>
        implements RangeDeserializer {

    static Builder newBuilder() {
        return new DefaultBuilder();
    }


    private DefaultRangeDeserializer(DefaultBuilder builder) {
        super(builder);
    }


    @Override
    public void deserialize(final String text, final int offset, final int endIndex, TextFunction<?> func,
                            @Nullable StringBuilder builder) {

        Objects.requireNonNull(func);

        final char firstChar = text.charAt(offset);
        if (isBoundaries(this.leftBoundaries, firstChar)) {
            parseRange(text, offset, endIndex, func, builder);
        } else if (text.regionMatches(true, offset, "empty", 0, 5)) {
            func.apply("", 0, 0);
        } else {
            throw syntaxError(getSyntaxType(), text, offset);
        }

    }


    @Override
    public int skipRange(final String text, final int offset, final int endIndex) {
        if (!isBoundaries(this.leftBoundaries, text.charAt(offset))) {
            throw syntaxError(getSyntaxType(), text, offset);
        }
        return parseDirectNestedElement(text, offset, endIndex, false, null, null, null);
    }

    @Override
    public char[] copyLeftBoundaries() {
        return Arrays.copyOf(this.leftBoundaries, this.leftBoundaries.length);
    }

    private void parseRange(final String text, final int offset, final int endIndex, @Nullable TextFunction<?> func,
                            @Nullable StringBuilder builder) {

        final char firstChar = text.charAt(offset);

        final char[] leftBoundaries = this.leftBoundaries, rightBoundaries = this.rightBoundaries;
        final char quoteChar, itemDelim;

        final boolean allowNothing = this.allowNothing, allowWhitespace = this.allowWhitespace;

        quoteChar = this.quoteChar;
        itemDelim = this.itemDelim;


        final StringBuilder[] holder = new StringBuilder[1];
        holder[0] = builder;

        char ch;

        int rightIndex = -1;

        if (func != null) {
            func.apply(String.valueOf(firstChar), 0, 1);
        }

        for (int i = offset + 1, delimFlat = 0, delimCount = 0, itemCount = 0; i < endIndex; i++) {
            ch = text.charAt(i);

            if (isBoundaries(rightBoundaries, ch)) {

                if (itemCount <= delimCount) {
                    if (!allowNothing) {
                        throw syntaxError(getSyntaxType(), text, i);
                    } else if (func != null) {
                        func.apply(text, 0, 0); // representing nothing
                    }
                    itemCount++;
                }
                if (itemCount != 2) {
                    throw syntaxError(getSyntaxType(), text, i);
                } else if (func != null) {
                    func.apply(String.valueOf(ch), 0, 1);
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

            if (ch == quoteChar) {
                if (func == null) {
                    i = skipQuoteElement(text, i, endIndex);
                } else {
                    i = parseQuoteElement(text, i + 1, endIndex, quoteChar, holder, null, func);
                }
                delimFlat = 1;
                itemCount++;
            } else if (ch == itemDelim) {
                if (allowNothing) {
                    if (func != null) {
                        func.apply(text, 0, 0);  // representing nothing
                    }
                    itemCount++;
                    delimCount++;
                    continue;
                }
                throw _Exceptions.redundantDelimError(text, i, itemDelim);
            } else if (isBoundaries(leftBoundaries, ch)) {
                throw syntaxError(getSyntaxType(), text, i);
            } else {
                if (!allowWhitespace && Character.isWhitespace(ch)) {
                    continue;
                }
                if (func == null) {
                    i = skipUnquotedElement(text, i, endIndex, rightBoundaries, null, null);
                } else {
                    i = parseUnQuoteElement(text, i, endIndex, null, func, null, null);
                }
                delimFlat = 1;
                itemCount++;
            }


        } // loop


        if (rightIndex < 0) {
            throw _Exceptions.missingEndingError(text, endIndex, rightBoundaries);
        }

    }

    @Override
    String getSyntaxType() {
        return "range";
    }

    private static final class DefaultBuilder extends ArmyMultiBoundaryBuilder<Builder>
            implements Builder {


        private DefaultBuilder() {
        }


        @Override
        public RangeDeserializer build() {
            return new DefaultRangeDeserializer(this);
        }


    }


}
