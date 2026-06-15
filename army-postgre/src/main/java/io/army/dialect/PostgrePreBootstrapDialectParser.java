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

package io.army.dialect;

import io.army.meta.ServerMeta;

import java.util.*;

final class PostgrePreBootstrapDialectParser implements PreBootstrapParser {

    private final ServerMeta serverMeta;

    private final IdentifierHandler identifierHandler;

    private final Set<String> keywordsSet;

    PostgrePreBootstrapDialectParser(ServerMeta serverMeta) {
        this.serverMeta = serverMeta;
        this.identifierHandler = _PostgreDialectUtils::handleIdentifier;
        this.keywordsSet = _PostgreDialectUtils.createKeywordsSet();
    }

    ///
    /// @see <a href="https://www.postgresql.org/docs/current/sql-createextension.html">CREATE EXTENSION</a>
    @Override
    public List<String> extensionStmts(boolean currentSchema, Map<String, String> extensionSchemaMap, Set<String> extensionSet) {
        final int size = extensionSet.size();

        if (size == 0) {
            return List.of();
        }

        final boolean extensionMapIsEmpty;
        extensionMapIsEmpty = extensionSchemaMap.isEmpty();

        final List<String> extensionList = new ArrayList<>(size);
        final StringBuilder builder = new StringBuilder(50);
        final String schema = this.serverMeta.schema();

        String targetSchema;
        for (String name : extensionSet) {

            builder.setLength(0); // clear

            builder.append("CREATE")
                    .append(_Constant.SPACE)
                    .append("EXTENSION")
                    .append(_Constant.SPACE)
                    .append("IF")
                    .append(_Constant.SPACE)
                    .append("NOT")
                    .append(_Constant.SPACE)
                    .append("EXISTS")
                    .append(_Constant.SPACE);

            safeIdentifier(name, builder);

            targetSchema = null;
            if (extensionMapIsEmpty) {
                targetSchema = extensionSchemaMap.get(name.toUpperCase(Locale.ROOT));
            }
            if (targetSchema == null) {
                if (currentSchema) {
                    targetSchema = schema;
                } else {
                    targetSchema = "public";
                }
            }

            if (targetSchema != null) {
                builder.append(_Constant.SPACE)
                        .append("WITH")
                        .append(_Constant.SPACE)
                        .append("SCHEMA")
                        .append(_Constant.SPACE);

                safeIdentifier(targetSchema, builder);
            }


            extensionList.add(builder.toString());
        }
        return List.copyOf(extensionList);
    }

    ///
    /// 1. [pg_type](https://www.postgresql.org/docs/current/catalog-pg-type.html)
    /// 2. [pg_class.relkind](https://www.postgresql.org/docs/current/catalog-pg-class.html)
    /// 3. [pg_extension](https://www.postgresql.org/docs/current/catalog-pg-extension.html)
    /// 4. [pg_depend](https://www.postgresql.org/docs/current/catalog-pg-depend.html)
    @Override
    public String queryDefinedTypeSchema() {
        return """
                SELECT t.typname AS "typeName",
                       n.nspname AS "schemaName",
                       e.extname AS "extensionName"
                FROM pg_namespace AS n
                         JOIN pg_type AS t ON t.typnamespace = n.oid
                         LEFT JOIN pg_class AS c ON c.oid = t.typrelid
                         LEFT JOIN pg_depend AS d ON d.objid = t.oid AND d.deptype = 'e'
                         LEFT JOIN pg_extension as e ON e.oid = d.refobjid
                WHERE n.nspname NOT IN ('pg_catalog', 'information_schema')
                  AND (t.typcategory IN ('E', 'R', 'U') OR t.typbasetype > 0 OR (t.typcategory = 'C' AND c.relkind = 'c'))
                """;
    }


    private void safeIdentifier(String effectiveName, StringBuilder sqlBuilder) {
        if (this.keywordsSet.contains(effectiveName.toUpperCase(Locale.ROOT))) {
            sqlBuilder.append(_Constant.DOUBLE_QUOTE)
                    .append(effectiveName)
                    .append(_Constant.DOUBLE_QUOTE);
        } else {
            this.identifierHandler.safeIdentifier(null, effectiveName, sqlBuilder);
        }
    }


}
