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

package io.army.schema;

import io.army.lang.Nullable;
import io.army.mapping.optional.CompositeField;
import io.army.struct.TypeCategory;

import java.util.List;

public interface TypeInfo {

    /// Upper case Type name
    String name();

    TypeCategory category();

    /// @return an unmodified list
    List<String> enumLabelList();

    /// {@link CompositeField#fieldName} field is meaningless here.
    /// The order of the list matches the order of the fields in the SQL composite type .
    ///
    /// Lower case {@link CompositeField#columnName} and {@link CompositeField#collation}
    ///
    /// @return an unmodified list
    List<CompositeField> compositeFieldList();

    boolean isNotNull();

    /// upper case type name
    @Nullable
    String baseTypeName();

    /// lower case
    @Nullable
    String defaultValue();

    /// lower case
    @Nullable
    String constraintName();

    /// lower case
    @Nullable
    String check();

    /// lower case
    @Nullable
    String collation();

    /// upper case type name
    @Nullable
    String rangeSubType();

    /// lower case
    @Nullable
    String rangeCollation();

    /// upper case type name
    @Nullable
    String rangeMulti();

    /// lower case
    @Nullable
    String rangeSubOpc();

    /// lower case
    @Nullable
    String rangeCanonical();

    /// lower case
    @Nullable
    String rangeSubDiff();

    static Builder builder() {
        return DefaultTypeInfo.newBuilder();
    }


    interface Builder {

        Builder name(String typeName);

        Builder category(TypeCategory typeCategory);

        /// Upper case enum labels
        Builder enumLabelList(List<String> enumLabelList);

        /// Lower case {@link CompositeField#columnName} and {@link CompositeField#collation}
        Builder compositeFieldList(List<CompositeField> compositeFieldList);

        Builder baseType(String typeName);

        Builder notNull(boolean yes);

        Builder defaultValue(@Nullable String defaultValue);

        Builder constraintName(@Nullable String constraint);

        Builder check(@Nullable String check);

        Builder collation(@Nullable String collation);

        Builder rangeSubType(@Nullable String rangeSubType);

        Builder rangeCollation(@Nullable String rangeCollation);

        Builder rangeMulti(@Nullable String rangeMulti);

        Builder rangeSubOpc(@Nullable String rangeSubOpc);

        Builder rangeCanonical(@Nullable String rangeCanonical);

        Builder rangeSubDiff(@Nullable String rangeSubDiff);

        /// create {@link TypeInfo} and clear
        TypeInfo build();
    }

}
