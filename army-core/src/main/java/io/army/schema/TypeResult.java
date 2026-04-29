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


import io.army.mapping.optional.CompositeField;
import io.army.struct.TypeCategory;

import java.util.List;

public interface TypeResult {


    /// Type name
    String name();

    TypeCategory category();

    /// @return an unmodified list
    List<String> enumLabelList();

    /// {@link CompositeField#fieldName} field is meaningless here.
    /// The order of the list matches the order of the fields in the SQL composite type
    ///
    /// @return an unmodified list
    List<CompositeField> compositeFieldList();

    boolean containNotNull();

    boolean containDefault();

    boolean containConstraint();


    boolean containCollation();


    boolean containRangeSubType();


    boolean containRangeCollation();


    boolean containRangeMulti();


    boolean containRangeSubOpc();


    boolean containRangeCanonical();


    boolean containRangeSubDiff();


}
