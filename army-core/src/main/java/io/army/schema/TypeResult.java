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
import io.army.mapping.MappingType;
import io.army.mapping.optional.CompositeField;
import io.army.struct.TypeCategory;

import java.util.List;

public interface TypeResult {


    MappingType type();



    /// @return an unmodified list
    List<String> enumNewLabelList();

    List<CompositeField> compositeDropFieldList();

    /// {@link CompositeField#fieldName} field is meaningless here.
    /// The order of the list matches the order of the fields in the SQL composite type
    /// 
    /// @return an unmodified list
    List<CompositeField> compositeNewFieldList();

    List<CompositeField> compositeModifyFieldList();


    boolean containNotNull();

    boolean containBaseType();

    boolean containDefault();

    @Nullable
    String constraintName();


    boolean containCollation();


    boolean containRangeSubType();


    boolean containRangeCollation();


    boolean containRangeMulti();


    boolean containRangeSubOpc();


    boolean containRangeCanonical();


    boolean containRangeSubDiff();


    static Builder builder() {
        return DefaultTypeResult.newBuilder();
    }


    interface Builder {

        Builder type(MappingType type);

        Builder category(TypeCategory typeCategory);


        Builder enumNewLabelList(@Nullable List<String> list);

        Builder compositeDropFieldList(@Nullable List<CompositeField> list);

        Builder compositeNewFieldList(@Nullable List<CompositeField> list);

        Builder compositeModifyFieldList(@Nullable List<CompositeField> list);

        Builder containNotNull(boolean yes);

        Builder containBaseType(boolean yes);

        Builder containDefault(boolean yes);

        Builder constraintName(@Nullable String name);

        Builder containCollation(boolean yes);

        Builder containRangeSubType(boolean yes);

        Builder containRangeCollation(boolean yes);

        Builder containRangeMulti(boolean yes);

        Builder containRangeSubOpc(boolean yes);

        Builder containRangeCanonical(boolean yes);

        Builder containRangeSubDiff(boolean yes);

        boolean hasDifference();

        void clear();


        /// create {@link TypeResult} and {@link #clear()}
        TypeResult build();
    }


}
