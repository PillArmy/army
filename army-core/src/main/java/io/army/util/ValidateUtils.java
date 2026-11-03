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

import io.army.meta.FieldMeta;
import io.army.meta.IndexMeta;
import io.army.meta.TableMeta;

import java.util.List;

public abstract class ValidateUtils {

    private ValidateUtils() {
    }

    public static void assertExistsIndex(TableMeta<?> tableMeta, final boolean unique, List<String> fieldNameList) {
        if (notExistsIndex(tableMeta, unique, fieldNameList)) {
            throw notExistsIndexError(tableMeta, unique, fieldNameList);
        }
    }

    /// @return true if index ***not*** exists
    public static <T> boolean notExistsIndex(TableMeta<T> tableMeta, final boolean unique, List<String> fieldNameList) {

        final int expectedMatchCount = fieldNameList.size();
        if (expectedMatchCount == 0) {
            throw new IllegalArgumentException("Field name list cannot be empty");
        }

        List<FieldMeta<T>> fieldList;
        int matchCount = 0;
        for (IndexMeta<T> index : tableMeta.indexList()) {
            if (index.isUnique() != unique) {
                continue;
            }
            fieldList = index.fieldList();
            if (fieldList.size() != expectedMatchCount) {
                continue;
            }

            for (FieldMeta<T> field : fieldList) {
                if (fieldNameList.contains(field.fieldName())) {
                    matchCount++;
                }
            } // inner loop

            if (matchCount == expectedMatchCount) {
                break;
            }

            matchCount = 0;

        } // top loop

        return matchCount != expectedMatchCount;
    }

    public static IllegalArgumentException notExistsIndexError(TableMeta<?> tableMeta, boolean unique, List<String> fieldNameList) {
        String m = String.format("%s not exists index[unique=%b] thant have %d fields: %s",
                tableMeta, unique, fieldNameList.size(), fieldNameList);
        return new IllegalArgumentException(m);
    }


}
