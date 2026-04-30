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
import io.army.meta.TableMeta;

import java.util.Collection;
import java.util.List;

public interface SchemaResult {

    @Nullable
    String catalog();

    @Nullable
    String schema();

    List<TableMeta<?>> dropTableList();

    List<TableMeta<?>> newTableList();

    List<TableResult> changeTableList();

    List<MappingType> dropTypeList();

    List<MappingType> newTypeList();

    List<TypeResult> modifyTypeList();

    boolean hasChanges();

    static SchemaResult dropCreate(@Nullable String catalog, @Nullable String schema, Collection<TableMeta<?>> tables,
                                   Collection<MappingType> types) {
        return DropCreateSchemaResult.create(catalog, schema, tables, types);
    }


}
