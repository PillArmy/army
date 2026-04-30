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
import io.army.util._Collections;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

final class DropCreateSchemaResult implements SchemaResult {

    static SchemaResult create(@Nullable String catalog, @Nullable String schema, Collection<TableMeta<?>> tables,
                               Collection<MappingType> types) {
        final Map<MappingType, Integer> typeMap = _Collections.hashMapForSize(types.size());
        types.forEach(type -> typeMap.put(type, -1));

        final List<MappingType> dropTypeList;
        dropTypeList = ArmySchemaComparer.updateTypeOrder(typeMap, true);
        return new DropCreateSchemaResult(catalog, schema, tables, dropTypeList);
    }


    private final String catalog;

    private final String schema;

    private final List<TableMeta<?>> tableList;

    private final List<MappingType> dropTypeList;

    private DropCreateSchemaResult(@Nullable String catalog, @Nullable String schema, Collection<TableMeta<?>> tables,
                                   List<MappingType> dropTypeList) {
        this.catalog = catalog;
        this.schema = schema;
        this.tableList = List.copyOf(tables);
        this.dropTypeList = List.copyOf(dropTypeList);
    }

    @Override
    public String catalog() {
        return this.catalog;
    }

    @Override
    public String schema() {
        return this.schema;
    }

    @Override
    public List<TableMeta<?>> dropTableList() {
        return this.tableList;
    }

    @Override
    public List<TableMeta<?>> newTableList() {
        return this.tableList;
    }

    @Override
    public List<TableResult> changeTableList() {
        return Collections.emptyList();
    }

    @Override
    public List<MappingType> dropTypeList() {
        return this.dropTypeList;
    }

    @Override
    public List<MappingType> newTypeList() {
        return this.dropTypeList;
    }

    @Override
    public List<TypeResult> modifyTypeList() {
        return List.of();
    }

    @Override
    public boolean hasChanges() {
        return !this.tableList.isEmpty()
                || !this.dropTypeList.isEmpty()
                ;
    }


}
