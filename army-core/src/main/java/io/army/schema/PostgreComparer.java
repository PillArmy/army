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

import io.army.meta.*;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util._Exceptions;
import io.army.util._StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

final class PostgreComparer extends ArmySchemaComparer {

    static PostgreComparer create(ServerMeta serverMeta) {
        return new PostgreComparer(serverMeta);
    }

    private final Pattern checkPattern = Pattern.compile("(?i)(?:CONSTRAINT\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s+)?\\s*CHECK\\s*\\(((?:[^()]|\\([^()]*\\))*)\\)(?:\\s*;)?");

    private PostgreComparer(ServerMeta serverMeta) {
        super(serverMeta);
    }


    @Override
    boolean compareSchema(SchemaInfo schemaInfo, SchemaMeta schemaMeta) {
        final String serverDatabase, serverSchema, catalog, schema;
        serverDatabase = schemaInfo.catalog();
        serverSchema = schemaInfo.schema();
        catalog = schemaMeta.catalog();
        schema = schemaMeta.schema();
        return !((catalog.isEmpty() || catalog.equals(serverDatabase)) && (schema.isEmpty() || schema.equals(serverSchema)));
    }

    @Override
    boolean compareSqlType(final ColumnInfo columnInfo, final FieldMeta<?> field, final DataType dataType) {
        final String typeName;
        typeName = columnInfo.typeName().toUpperCase(Locale.ROOT);
        if (!(dataType instanceof PgType)) {
            return !typeName.equals(dataType.typeName().toUpperCase(Locale.ROOT));
        }
        final int precision, scale;

        final boolean notMatch;
        switch ((PgType) dataType) {
            case BOOLEAN:
                switch (typeName) {
                    case "BOOLEAN":
                    case "BOOL":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case SMALLINT:
                switch (typeName) {
                    case "INT2":
                    case "SMALLINT":
                    case "SMALLSERIAL":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case INTEGER:
                switch (typeName) {
                    case "INT":
                    case "INT4":
                    case "SERIAL":
                    case "INTEGER":
                    case "XID":  // https://www.postgresql.org/docs/current/datatype-oid.html
                    case "CID":  // https://www.postgresql.org/docs/current/datatype-oid.html
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case BIGINT:
                switch (typeName) {
                    case "INT8":
                    case "BIGINT":
                    case "BIGSERIAL":
                    case "SERIAL8":
                    case "XID8":  // https://www.postgresql.org/docs/current/datatype-oid.html  TODO what's tid ?
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case DECIMAL:
                switch (typeName) {
                    case "NUMERIC":
                    case "DECIMAL": {
                        precision = field.precision();
                        scale = field.scale();
                        notMatch = (precision > -1 && precision != columnInfo.precision())
                                || (scale > -1 && scale != columnInfo.scale());
                    }
                    break;
                    default:
                        notMatch = true;
                }
                break;
            case FLOAT8:
                switch (typeName) {
                    case "FLOAT8":
                    case "DOUBLE PRECISION":
                    case "FLOAT":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case REAL:
                switch (typeName) {
                    case "FLOAT4":
                    case "REAL":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case TIME:
                switch (typeName) {
                    case "TIME":
                    case "TIME WITHOUT TIME ZONE": {
                        scale = field.scale();
                        notMatch = scale > -1 && scale != columnInfo.scale();
                    }
                    break;
                    default:
                        notMatch = true;
                }
                break;
            case TIMETZ:
                switch (typeName) {
                    case "TIMETZ":
                    case "TIME WITH TIME ZONE": {
                        scale = field.scale();
                        notMatch = scale > -1 && scale != columnInfo.scale();
                    }
                    break;
                    default:
                        notMatch = true;
                }
                break;
            case TIMESTAMP:
                switch (typeName) {
                    case "TIMESTAMP":
                    case "TIMESTAMP WITHOUT TIME ZONE": {
                        scale = field.scale();
                        notMatch = scale > -1 && scale != columnInfo.scale();
                    }
                    break;
                    default:
                        notMatch = true;
                }
                break;
            case TIMESTAMPTZ:
                switch (typeName) {
                    case "TIMESTAMPTZ":
                    case "TIMESTAMP WITH TIME ZONE": {
                        scale = field.scale();
                        notMatch = scale > -1 && scale != columnInfo.scale();
                    }
                    break;
                    default:
                        notMatch = true;
                }
                break;
            case CHAR:
                switch (typeName) {
                    case "CHAR":
                    case "CHARACTER": {
                        precision = field.precision();
                        notMatch = precision > -1 && precision != columnInfo.precision();
                    }
                    break;
                    default:
                        notMatch = true;
                }
                break;
            case VARCHAR:
                switch (typeName) {
                    case "VARCHAR":
                    case "CHARACTER VARYING": {
                        precision = field.precision();
                        notMatch = precision > -1 && precision != columnInfo.precision();
                    }
                    break;
                    default:
                        notMatch = true;
                }
                break;
            case TEXT:
                switch (typeName) {
                    case "TEXT":
                    case "TXID_SNAPSHOT":  // TODO txid_snapshot is text?
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case VARBIT:
                switch (typeName) {
                    case "BIT VARYING":
                    case "VARBIT": {
                        precision = field.precision();
                        notMatch = precision > -1 && precision != columnInfo.precision();
                    }
                    break;
                    default:
                        notMatch = true;
                }
                break;
            case BOOLEAN_ARRAY:
                switch (typeName) {
                    case "BOOLEAN[]":
                    case "BOOL[]":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case SMALLINT_ARRAY:
                switch (typeName) {
                    case "INT2[]":
                    case "SMALLINT[]":
                    case "SMALLSERIAL[]":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case INTEGER_ARRAY:
                switch (typeName) {
                    case "INT[]":
                    case "INT4[]":
                    case "INTEGER[]":
                    case "SERIAL[]":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case BIGINT_ARRAY:
                switch (typeName) {
                    case "INT8[]":
                    case "BIGINT[]":
                    case "SERIAL8[]":
                    case "BIGSERIAL[]":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case DECIMAL_ARRAY:
                switch (typeName) {
                    case "NUMERIC[]":
                    case "DECIMAL[]":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case FLOAT8_ARRAY:
                switch (typeName) {
                    case "FLOAT8[]":
                    case "FLOAT[]":
                    case "DOUBLE PRECISION[]":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case REAL_ARRAY:
                switch (typeName) {
                    case "FLOAT4[]":
                    case "REAL[]":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case CHAR_ARRAY:
                switch (typeName) {
                    case "CHAR[]":
                    case "CHARACTER[]":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case VARCHAR_ARRAY:
                switch (typeName) {
                    case "VARCHAR[]":
                    case "CHARACTER VARYING[]":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case TEXT_ARRAY:
                switch (typeName) {
                    case "TEXT[]":
                    case "TXID_SNAPSHOT[]":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case TIME_ARRAY:
                switch (typeName) {
                    case "TIME[]":
                    case "TIME WITHOUT TIME ZONE[]":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case TIMETZ_ARRAY:
                switch (typeName) {
                    case "TIMETZ[]":
                    case "TIME WITH TIME ZONE[]":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case TIMESTAMP_ARRAY:
                switch (typeName) {
                    case "TIMESTAMP[]":
                    case "TIMESTAMP WITHOUT TIME ZONE[]":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case TIMESTAMPTZ_ARRAY:
                switch (typeName) {
                    case "TIMESTAMPTZ[]":
                    case "TIMESTAMP WITH TIME ZONE[]":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case VARBIT_ARRAY:
                switch (typeName) {
                    case "VARBIT[]":
                    case "BIT VARYING[]":
                        notMatch = false;
                        break;
                    default:
                        notMatch = true;
                }
                break;
            case REF_CURSOR:
            case UNKNOWN:
                throw _Exceptions.unexpectedEnum((Enum<?>) dataType);
            default:
                notMatch = !typeName.equalsIgnoreCase(dataType.typeName());
        }
        return notMatch;
    }


    @Override
    boolean compareDefault(ColumnInfo columnInfo, FieldMeta<?> field, DataType sqlType) {
        //TODO  complete me
        return !(field instanceof IndexFieldMeta)  // 如果是索引,其默认值可能是自增的. 如 primary key
                && (_StringUtils.hasText(field.defaultValue()) ^ _StringUtils.hasText(columnInfo.defaultExp()));
    }

    @Override
    boolean isSameType(String typeName, String typeName1) {
        return getFanonicalName(typeName).equals(getFanonicalName(typeName1));
    }

    @Override
    Pattern getCheckPattern() {
        return this.checkPattern;
    }

    @Override
    boolean supportColumnComment() {
        return true;
    }

    @Override
    boolean supportTableComment() {
        return true;
    }

    @Override
    String primaryKeyName(TableMeta<?> table) {
        //eg: china_region_pkey
        return table.tableName() + "_pkey";
    }


    /// Get the canonical (standard) name for the given PostgreSQL type name.
    /// This follows the alias resolution logic in `compareSqlType` method,
    /// mapping type aliases to their canonical PostgreSQL type names.
    ///
    /// **Examples:**
    /// - `"int"` → `"INTEGER"`
    /// - `"bool"` → `"BOOLEAN"`
    /// - `"varchar"` → `"VARCHAR"`
    /// - `"time without time zone"` → `"TIME"`
    ///
    /// @param typeName the type name to get canonical name for
    /// @return the canonical type name, or the original name if no mapping exists
    private String getFanonicalName(String typeName) {
        typeName = typeName.toUpperCase(Locale.ROOT);
        final String result;
        switch (typeName) {
            // BOOLEAN aliases
            case "BOOLEAN":
            case "BOOL":
                result = "BOOLEAN";
                break;
            // SMALLINT aliases
            case "INT2":
            case "SMALLINT":
            case "SMALLSERIAL":
                result = "SMALLINT";
                break;
            // INTEGER aliases
            case "INT":
            case "INT4":
            case "SERIAL":
            case "INTEGER":
                result = "INTEGER";
                break;
            // BIGINT aliases
            case "INT8":
            case "BIGINT":
            case "BIGSERIAL":
            case "SERIAL8":
                result = "BIGINT";
                break;
            // DECIMAL aliases
            case "NUMERIC":
            case "DECIMAL":
                result = "DECIMAL";
                break;
            // FLOAT8 aliases
            case "FLOAT8":
            case "DOUBLE PRECISION":
            case "FLOAT":
                result = "FLOAT8";
                break;
            // REAL aliases
            case "FLOAT4":
            case "REAL":
                result = "REAL";
                break;
            // TIME aliases
            case "TIME":
            case "TIME WITHOUT TIME ZONE":
                result = "TIME";
                break;
            // TIMETZ aliases
            case "TIMETZ":
            case "TIME WITH TIME ZONE":
                result = "TIMETZ";
                break;
            // TIMESTAMP aliases
            case "TIMESTAMP":
            case "TIMESTAMP WITHOUT TIME ZONE":
                result = "TIMESTAMP";
                break;
            // TIMESTAMPTZ aliases
            case "TIMESTAMPTZ":
            case "TIMESTAMP WITH TIME ZONE":
                result = "TIMESTAMPTZ";
                break;
            // CHAR aliases
            case "CHAR":
            case "CHARACTER":
                result = "CHAR";
                break;
            // VARCHAR aliases
            case "VARCHAR":
            case "CHARACTER VARYING":
                result = "VARCHAR";
                break;
            // TEXT aliases
            case "TEXT":
                result = "TEXT";
                break;
            // VARBIT aliases
            case "BIT VARYING":
            case "VARBIT":
                result = "VARBIT";
                break;
            // ARRAY types - remove [] suffix and process base type
            case "BOOLEAN[]":
            case "BOOL[]":
                result = "BOOLEAN[]";
                break;
            case "INT2[]":
            case "SMALLINT[]":
            case "SMALLSERIAL[]":
                result = "SMALLINT[]";
                break;
            case "INT[]":
            case "INT4[]":
            case "SERIAL[]":
            case "INTEGER[]":
                result = "INTEGER[]";
                break;
            case "INT8[]":
            case "BIGINT[]":
            case "BIGSERIAL[]":
            case "SERIAL8[]":
                result = "BIGINT[]";
                break;
            case "NUMERIC[]":
            case "DECIMAL[]":
                result = "DECIMAL[]";
                break;
            case "FLOAT8[]":
            case "FLOAT[]":
            case "DOUBLE PRECISION[]":
                result = "FLOAT8[]";
                break;
            case "FLOAT4[]":
            case "REAL[]":
                result = "REAL[]";
                break;
            case "TIME[]":
            case "TIME WITHOUT TIME ZONE[]":
                result = "TIME[]";
                break;
            case "TIMETZ[]":
            case "TIME WITH TIME ZONE[]":
                result = "TIMETZ[]";
                break;
            case "TIMESTAMP[]":
            case "TIMESTAMP WITHOUT TIME ZONE[]":
                result = "TIMESTAMP[]";
                break;
            case "TIMESTAMPTZ[]":
            case "TIMESTAMP WITH TIME ZONE[]":
                result = "TIMESTAMPTZ[]";
                break;
            case "CHAR[]":
            case "CHARACTER[]":
                result = "CHAR[]";
                break;
            case "VARCHAR[]":
            case "CHARACTER VARYING[]":
                result = "VARCHAR[]";
                break;
            case "TEXT[]":
            case "TXID_SNAPSHOT[]":
                result = "TEXT[]";
                break;
            case "BIT VARYING[]":
            case "VARBIT[]":
                result = "VARBIT[]";
                break;
            default:
                result = typeName;
                break;
        }
        return result;
    }


}
