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

package io.army.modelgen;


import io.army.lang.Nullable;

import java.util.List;
import java.util.Locale;

/// Meta Constant set
public abstract class _MetaBridge {

    private _MetaBridge() {
        throw new UnsupportedOperationException();
    }


    public static final String ID = "id";
    public static final String CREATE_TIME = "createTime";
    public static final String VISIBLE = "visible";
    public static final String UPDATE_TIME = "updateTime";

    public static final String VERSION = "version";

    public static final List<String> RESERVED_FIELDS = List.of(
            ID, CREATE_TIME, UPDATE_TIME, VERSION, VISIBLE
    );


    public static final String TABLE_META = "T";

    public static final String META_CLASS_NAME_SUFFIX = "_";


    public static String camelToUpperCase(String camel, @Nullable StringBuilder tempBuilder) {
        return splitCamel(camel, '_', tempBuilder).toUpperCase(Locale.ROOT);
    }

    public static String camelToLowerCase(String camel, @Nullable StringBuilder tempBuilder) {
        return splitCamel(camel, '_', tempBuilder).toLowerCase(Locale.ROOT);
    }

    public static String camelToComment(String camel, @Nullable StringBuilder tempBuilder) {
        return splitCamel(camel, ' ', tempBuilder);
    }

    public static boolean isReserved(final String fieldName) {
        final boolean match;
        switch (fieldName) {
            case _MetaBridge.ID:
            case _MetaBridge.CREATE_TIME:
            case _MetaBridge.UPDATE_TIME:
            case _MetaBridge.VERSION:
            case _MetaBridge.VISIBLE:
                match = true;
                break;
            default:
                match = false;
        }
        return match;

    }


    public static String createErrorMessage(final String title, final List<String> errorMsgList) {
        final StringBuilder builder = new StringBuilder(errorMsgList.size() * 20)
                .append(title);
        final int size = errorMsgList.size();
        for (int i = 0; i < size; i++) {
            builder.append('\n')
                    .append(i + 1)
                    .append(" : ")
                    .append(errorMsgList.get(i));
        }
        return builder.toString();
    }

    public static boolean isCamelCase(final @Nullable String text) {
        if (text == null) {
            return false;
        }
        final int len = text.length();

        char ch;
        boolean camel = false;
        for (int i = 0, firstCase = 0; i < len; i++) {
            ch = text.charAt(i);

            if (firstCase == 0) {
                if (Character.isUpperCase(ch)) {
                    firstCase = 1;
                } else if (Character.isLowerCase(ch)) {
                    firstCase = -1;
                }
                continue;
            }

            if (Character.isUpperCase(ch)) {
                if (firstCase == -1) {
                    camel = true;
                    break;
                }
            } else if (Character.isLowerCase(ch)) {
                if (firstCase == 1) {
                    camel = true;
                    break;
                }
            }


        } // loop

        return camel;
    }

    private static String splitCamel(final String camel, final char replacement, @Nullable StringBuilder tempBuilder) {
        final int len = camel.length();
        if (tempBuilder == null) {
            tempBuilder = new StringBuilder(len + 5);
        } else {
            tempBuilder.setLength(0); // clear
        }

        int lastWritten = 0;
        boolean preIsUpper = len > 0 && Character.isUpperCase(camel.charAt(0)), curIsUpper;
        for (int i = 1; i < len; i++) {

            curIsUpper = Character.isUpperCase(camel.charAt(i));

            if (curIsUpper && !preIsUpper && i > lastWritten) {
                tempBuilder.append(camel, lastWritten, i);
                tempBuilder.append(replacement);
                lastWritten = i;
            }

            preIsUpper = curIsUpper;

        } // loop

        if (lastWritten < len) {
            tempBuilder.append(camel, lastWritten, len);
        }
        return tempBuilder.toString();
    }


}
