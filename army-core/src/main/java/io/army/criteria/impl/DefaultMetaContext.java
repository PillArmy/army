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

package io.army.criteria.impl;

import io.army.lang.Nullable;
import io.army.meta.IndexColumnMeta;
import io.army.meta.MetaException;
import io.army.meta.SchemaMeta;
import io.army.meta.TableMeta;
import io.army.util._ResourceUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class DefaultMetaContext implements MetaContext {

    private Map<SchemaMeta, Map<String, Class<?>>> tableNameValidMap;

    private Map<Class<?>, Map<String, Boolean>> columnNameValidMap;

    private StringBuilder tempBuilder;

    private Map<String, Map<String, String>> generatorParamMap;

    private Properties tableMetaProperties;

    private Map<List<IndexColumnMeta>, List<IndexColumnMeta>> minColumnMetaMap;

    private Map<List<IndexColumnMeta>, List<IndexColumnMeta>> columnMetaMap;

    private Map<List<Class<?>>, List<Class<?>>> classListMap;

    @Override
    public void validateTableName(SchemaMeta meta, Class<?> domainClass, String tableName) {
        Map<SchemaMeta, Map<String, Class<?>>> map = this.tableNameValidMap;
        if (map == null) {
            this.tableNameValidMap = map = new HashMap<>();
        }
        final Class<?> oldValue;
        oldValue = map.computeIfAbsent(meta, _ -> new HashMap<>())
                .putIfAbsent(tableName, domainClass);

        if (oldValue != null && oldValue != domainClass) {
            throw new MetaException(String.format("%s %s.%s duplication", domainClass.getName(), "Table", "name"));
        }
    }

    @Override
    public void validateColumnName(Class<?> domainClass, String columnName) {
        Map<Class<?>, Map<String, Boolean>> map = this.columnNameValidMap;
        if (map == null) {
            this.columnNameValidMap = map = new HashMap<>();
        }

        final Boolean oldValue;
        oldValue = map.computeIfAbsent(domainClass, _ -> new HashMap<>())
                .putIfAbsent(columnName, Boolean.TRUE);
        if (oldValue != null) {
            String m = String.format("%s %s.%s duplication", domainClass.getName(), "Column", "name");
            throw new MetaException(m);
        }
    }

    @Override
    public List<IndexColumnMeta> minIndexColumnMetaList(final List<IndexColumnMeta> list) {
        Map<List<IndexColumnMeta>, List<IndexColumnMeta>> map = this.minColumnMetaMap;

        if (map == null) {
            this.minColumnMetaMap = map = new HashMap<>();
        }
        List<IndexColumnMeta> result;
        result = map.get(list);
        if (result == null) {
            result = List.copyOf(list);
            map.put(result, result);
        }
        return result;
    }

    @Override
    public List<IndexColumnMeta> indexColumnMetaList(List<IndexColumnMeta> list) {
        Map<List<IndexColumnMeta>, List<IndexColumnMeta>> map = this.columnMetaMap;
        if (map == null) {
            this.columnMetaMap = map = new HashMap<>();
        }
        List<IndexColumnMeta> result;
        result = map.get(list);
        if (result == null) {
            result = List.copyOf(list);
            map.put(result, result);
        }
        return result;
    }

    @Override
    public List<Class<?>> classList(List<Class<?>> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Map<List<Class<?>>, List<Class<?>>> map = this.classListMap;
        if (map == null) {
            this.classListMap = map = new HashMap<>();
        }
        list = List.copyOf(list);
        final List<Class<?>> oldValue;
        oldValue = map.putIfAbsent(list, list);
        if (oldValue != null && oldValue != list) {
            list = oldValue;
        }
        return list;
    }

    @Override
    public Properties tableMetaProperties() {
        Properties properties = this.tableMetaProperties;
        if (properties == null) {
            this.tableMetaProperties = properties = _ResourceUtils.loadArmyProperties(TableMeta.class.getSimpleName());
        }
        return properties;
    }


    @Override
    public StringBuilder tempBuilderAndClear() {
        StringBuilder builder = this.tempBuilder;
        if (builder == null) {
            this.tempBuilder = builder = new StringBuilder(40);
        } else {
            builder.setLength(0); // clear
        }
        return builder;
    }

    @Nullable
    @Override
    public Map<String, String> getGeneratorParamMap(String key) {
        Map<String, Map<String, String>> map = this.generatorParamMap;
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    @Override
    public void putGeneratorParamMap(String key, Map<String, String> value) {
        Map<String, Map<String, String>> map = this.generatorParamMap;
        if (map == null) {
            this.generatorParamMap = map = new HashMap<>();
        }
        map.put(key, Map.copyOf(value));
    }

    @Override
    public void clear() {
        this.tempBuilder = null;

        Map<?, ?> map;

        map = this.tableNameValidMap;
        this.tableNameValidMap = null;
        if (map != null) {
            map.clear();
        }

        map = this.columnNameValidMap;
        this.columnNameValidMap = null;
        if (map != null) {
            map.clear();
        }

        map = this.tableMetaProperties;
        this.tableMetaProperties = null;
        if (map != null) {
            map.clear();
        }

        map = this.minColumnMetaMap;
        this.minColumnMetaMap = null;
        if (map != null) {
            map.clear();
        }

        map = this.columnMetaMap;
        this.columnMetaMap = null;
        if (map != null) {
            map.clear();
        }

        map = this.generatorParamMap;
        this.generatorParamMap = null;
        if (map != null) {
            map.clear();
        }

    }


}
