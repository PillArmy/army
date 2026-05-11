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
import io.army.meta.TableMeta;
import io.army.util._ResourceUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

final class DefaultMetaContext implements MetaContext {


    private StringBuilder tempBuilder;

    private Map<String, Map<String, String>> generatorParamMap;

    private Properties tableMetaProperties;

    private Map<List<IndexColumnMeta>, List<IndexColumnMeta>> minColumnMetaMap;

    private Map<List<IndexColumnMeta>, List<IndexColumnMeta>> columnMetaMap;

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
