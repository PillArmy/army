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
import io.army.util._StringUtils;

import java.util.Objects;


final class DefaultRecordDeserializer extends ArmyDeserializer.SingleBoundaryDeserializer<RecordDeserializer.Builder>
        implements RecordDeserializer {

    static RecordDeserializer.Builder newBuilder() {
        return new DefaultBuilder();
    }


    private DefaultRecordDeserializer(DefaultBuilder builder) {
        super(builder);
    }


    @Override
    public void deserialize(String text, int offset, int endIndex, TextFunction<?> func, @Nullable StringBuilder builder) {

        if (text.charAt(offset) != this.leftBoundary) {
            throw new IllegalArgumentException();
        } else if (offset >= endIndex) {
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
                    func.apply(text, 0, 0);  // representing nothing
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
                // currently ,don't support nested record
//                i = parseDirectNestedElement(text, i, endIndex, func, boundaries, skipFunc);
//                delimFlat = 1;
//                itemCount++;
                throw syntaxError(getSyntaxType(), text, i);
            } else if (ch == quoteChar) {
                i = parseQuoteElement(text, i + 1, endIndex, ch, holder, null, func);
                delimFlat = 1;
                itemCount++;
            } else if (ch == itemDelim) {
                if (allowNothing) {
                    func.apply(text, 0, 0);  // representing nothing
                    itemCount++;
                    delimCount++;
                    continue;
                }
                throw _Exceptions.redundantDelimError(text, i, itemDelim);
            } else {
                if (!allowWhitespace && Character.isWhitespace(ch)) {
                    continue;
                }
                i = parseUnQuoteElement(text, i, endIndex, null, func, null, null);
                delimFlat = 1;
                itemCount++;
            }


        } // loop


        if (rightIndex < 0) {
            throw _Exceptions.missingEndingError(text, endIndex, new char[]{rightBoundary});
        }

    }


    @Override
    String getSyntaxType() {
        return "record";
    }

    private static final class DefaultBuilder extends ArmySingleBoundaryBuilder<Builder>
            implements Builder {

        private DefaultBuilder() {
        }


        @Override
        public RecordDeserializer build() {
            return new DefaultRecordDeserializer(this);
        }

    } // DefaultBuilder


}
