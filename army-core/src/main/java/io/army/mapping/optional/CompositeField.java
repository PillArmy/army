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

package io.army.mapping.optional;

import io.army.lang.Nullable;
import io.army.meta.FieldMeta;
import io.army.meta.TypeMeta;
import io.army.util._Assert;
import io.army.util._StringUtils;

import java.util.Objects;

/// @see io.army.mapping.CompositeType
public final class CompositeField {

    @Deprecated
    public static CompositeField from(String fieldName, String columnName, TypeMeta typeMeta) {
        return from(fieldName, columnName, typeMeta, null);
    }

    public static CompositeField from(String fieldName, String columnName, TypeMeta typeMeta, @Nullable String collation) {
        _Assert.hasText(fieldName, "");
        _Assert.hasText(columnName, "");
        Objects.requireNonNull(typeMeta);
        _Assert.isFalse(typeMeta instanceof FieldMeta, "");
        return new CompositeField(fieldName, columnName, typeMeta);
    }

    public final String fieldName;

    public final String columnName;

    public final TypeMeta typeMeta;

    private CompositeField(String fieldName, String columnName, TypeMeta typeMeta) {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.typeMeta = typeMeta;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.fieldName, this.columnName, this.typeMeta);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof CompositeField o) {
            match = o.fieldName.equals(this.fieldName)
                    && o.columnName.equals(this.columnName)
                    && o.typeMeta.equals(this.typeMeta);
        } else {
            match = false;
        }
        return match;
    }

    @Override
    public String toString() {
        return _StringUtils.builder()
                .append(this.getClass().getSimpleName())
                .append('[')
                .append("fieldName")
                .append(':')
                .append(this.fieldName)
                .append(',')
                .append("columnName")
                .append(':')
                .append(this.columnName)
                .append(',')
                .append("type")
                .append(':')
                .append(this.typeMeta)
                .append(']')
                .toString();
    }


}
