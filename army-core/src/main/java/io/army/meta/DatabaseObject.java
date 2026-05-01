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

package io.army.meta;

import io.army.criteria.SQLElement;
import io.army.criteria.TableField;

/// 
/// This interface representing database object ,this interface is base interface of below:
/// 
/// - {@link  TableMeta}
/// - {@link  TableField}
/// - {@link  io.army.mapping.MappingType.SqlUserDefined}
/// - {@link io.army.mapping.optional.CompositeField}
/// 
/// @since 0.6.0
public interface DatabaseObject extends SQLElement {

    /// @see TableMeta#tableName()
    /// @see FieldMeta#columnName()
    String objectName();

    String comment();

    interface TypeObject extends DatabaseObject {

    }

    interface FieldObject extends DatabaseObject {

    }

}
