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

import io.army.dialect._Constant;
import io.army.dialect._PostgreDialectUtils;
import io.army.meta.*;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util._StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/// Reflection call
///
/// @see ArmySchemaComparer#createComparer(ServerMeta)
@SuppressWarnings("unused")
public final class PostgreComparer extends ArmySchemaComparer {

    public static PostgreComparer create(ServerMeta serverMeta) {
        return new PostgreComparer(serverMeta);
    }

    private final Pattern checkPattern = Pattern.compile("(?i)(?:CONSTRAINT\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s+)?\\s*CHECK\\s*\\(((?:[^()]|\\([^()]*\\))*)\\)(?:\\s*;)?");

    private final Map<String, PgType> aliasToTypeMap;


    private PostgreComparer(ServerMeta serverMeta) {
        super(serverMeta);
        this.aliasToTypeMap = _PostgreDialectUtils.getAliasToTypeMap();
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
        final int precision, scale;
        precision = field.precision();
        scale = field.scale();

        final PgType type;

        final boolean notMatch;
        if (!(dataType instanceof PgType)) {
            notMatch = compareUserDefinedSqlType(columnInfo, field, dataType);
        } else if ((type = this.aliasToTypeMap.get(columnInfo.typeName().toUpperCase(Locale.ROOT))) == null) {
            notMatch = true;
        } else switch (type) {
            case CHAR:
            case BPCHAR:
                notMatch = precision > -1 && precision != columnInfo.precision();
                break;
            case CHAR_ARRAY:
            case VARCHAR:
            case VARCHAR_ARRAY:
            case BYTEA:
            case BYTEA_ARRAY:
            case BIT:
            case BIT_ARRAY:
            case VARBIT:
            case VARBIT_ARRAY:
                notMatch = type != dataType
                        || precision > -1 && precision != columnInfo.precision();
                break;
            case TIME:
            case TIME_ARRAY:
            case TIMETZ:
            case TIMETZ_ARRAY:
            case TIMESTAMP:
            case TIMESTAMP_ARRAY:
            case TIMESTAMPTZ:
            case TIMESTAMPTZ_ARRAY:
                notMatch = type != dataType
                        || scale > -1 && scale != columnInfo.scale();
                break;
            default:
                notMatch = type != dataType
                        || (precision > -1 && precision != columnInfo.precision())
                        || (scale > -1 && scale != columnInfo.scale());
        } // switch

        return notMatch;
    }


    @Override
    boolean compareDefault(ColumnInfo columnInfo, FieldMeta<?> field, DataType sqlType) {
        //TODO  complete me
        return !(field instanceof PrimaryFieldMeta<?>)  // 如果是索引,其默认值可能是自增的. 如 primary key
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

    private boolean compareUserDefinedSqlType(final ColumnInfo columnInfo, final FieldMeta<?> field,
                                              final DataType dataType) {

        final String typeNameFromDb, typeNameFromJava, aliasFromJava;
        typeNameFromJava = dataType.typeName().toUpperCase(Locale.ROOT);
        aliasFromJava = dataType.safeTypeAlias().toUpperCase(Locale.ROOT);

        typeNameFromDb = identifierOf(columnInfo.typeName());

        return !typeNameFromDb.equals(typeNameFromJava)
                && !typeNameFromDb.equals(aliasFromJava);
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
        typeName = identifierOf(typeName);
        final PgType dataType;
        dataType = this.aliasToTypeMap.get(typeName);
        if (dataType != null) {
            typeName = dataType.typeName();
        }
        return typeName;
    }

    /// uppercase and remove double quotes and schema name if any
    private String identifierOf(String typeName) {
        if (typeName.indexOf(_Constant.DOUBLE_QUOTE) > -1) {
            final List<String> list;
            list = _PostgreDialectUtils.decodeIdentifier(typeName);
            typeName = list.getLast();
        } else {
            final int index = typeName.indexOf('.');
            if (index > -1) {
                typeName = typeName.substring(index + 1);
            }
        }
        return typeName.toUpperCase(Locale.ROOT);
    }


}
