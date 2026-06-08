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

import io.army.lang.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class DefaultSchemaMeta implements SchemaMeta {

    private static final ConcurrentMap<String, SchemaMeta> SCHEMA_META_HOLDER = new ConcurrentHashMap<>();


    static SchemaMeta getSchema(@Nullable String catalog, @Nullable String schema) {
        final String finalCatalog, finalSchema;
        if (catalog == null) {
            finalCatalog = "";
        } else {
            finalCatalog = catalog.toLowerCase(Locale.ROOT);
        }

        if (schema == null) {
            finalSchema = "";
        } else {
            finalSchema = schema.toLowerCase(Locale.ROOT);
        }
        final String key = catalog + '.' + schema;
        return SCHEMA_META_HOLDER.computeIfAbsent(key, _ -> new DefaultSchemaMeta(finalCatalog, finalSchema));
    }


    private final String catalog;

    private final String schema;

    private DefaultSchemaMeta(final String catalog, final String schema) {
        this.catalog = catalog;
        this.schema = schema;
    }


    @Override
    public String catalog() {
        return this.catalog;
    }

    @Override
    public boolean defaultSchema() {
        return this.catalog.isEmpty() && this.schema.isEmpty();
    }

    @Override
    public String schema() {
        return this.schema;
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof DefaultSchemaMeta o) {
            match = this.catalog.equals(o.catalog)
                    && this.schema.equals(o.schema);
        } else {
            match = false;
        }
        return match;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.catalog, this.schema);
    }

    @Override
    public String toString() {
        return this.catalog + '.' + this.schema;
    }

}
