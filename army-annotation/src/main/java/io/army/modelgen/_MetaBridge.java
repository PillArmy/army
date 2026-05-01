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
        return camelToUnderline(camel, tempBuilder).toUpperCase(Locale.ROOT);
    }

    public static String camelToLowerCase(String camel, @Nullable StringBuilder tempBuilder) {
        return camelToUnderline(camel, tempBuilder).toLowerCase(Locale.ROOT);
    }

    private static String camelToUnderline(final String camel, @Nullable StringBuilder tempBuilder) {
        final int len = camel.length();
        if (tempBuilder == null) {
            tempBuilder = new StringBuilder(camel.length() + 5);
        } else {
            tempBuilder.setLength(0); // clear
        }
        char ch;
        int preIndex = 0;
        for (int i = 0; i < len; i++) {
            ch = camel.charAt(i);
            if (Character.isUpperCase(ch)) {
                tempBuilder.append(camel, preIndex, i);
                tempBuilder.append('_');
                preIndex = i;
            }
        }
        tempBuilder.append(camel, preIndex, len);
        return tempBuilder.toString();
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
        return text != null
                && !text.toLowerCase(Locale.ROOT).equals(text)
                && !text.toUpperCase(Locale.ROOT).equals(text);

    }


}
