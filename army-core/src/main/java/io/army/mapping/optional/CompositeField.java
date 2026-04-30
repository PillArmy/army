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

import io.army.meta.DatabaseObject;
import io.army.meta.FieldMeta;
import io.army.meta.TypeMeta;
import io.army.util._Assert;
import io.army.util._StringUtils;

import java.util.Objects;

/// @see io.army.mapping.CompositeType
public final class CompositeField implements DatabaseObject.FieldObject {


    public static CompositeField from(String fieldName, String columnName, TypeMeta typeMeta, String collation) {
        _Assert.hasText(fieldName, "");
        _Assert.hasText(columnName, "");
        Objects.requireNonNull(typeMeta);
        _Assert.isFalse(typeMeta instanceof FieldMeta, "");
        Objects.requireNonNull(collation);
        return new CompositeField(fieldName, columnName, typeMeta, collation);
    }

    public final String fieldName;

    public final String columnName;

    public final TypeMeta typeMeta;

    public final String collation;

    private CompositeField(String fieldName, String columnName, TypeMeta typeMeta, String collation) {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.typeMeta = typeMeta;
        this.collation = collation;
    }


    @Override
    public String objectName() {
        return this.columnName;
    }

    @Override
    public String comment() {
        return "";
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
                .append("collation")
                .append(':')
                .append(this.collation)
                .append(',')
                .append("type")
                .append(':')
                .append(this.typeMeta)
                .append(']')
                .toString();
    }


}
