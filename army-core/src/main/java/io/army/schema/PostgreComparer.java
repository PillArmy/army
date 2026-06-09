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
import io.army.dialect._DialectUtils;
import io.army.meta.*;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ReflectionUtils;
import io.army.util._StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

final class PostgreComparer extends ArmySchemaComparer {

    static PostgreComparer create(ServerMeta serverMeta) {
        return new PostgreComparer(serverMeta);
    }

    private final Pattern checkPattern = Pattern.compile("(?i)(?:CONSTRAINT\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s+)?\\s*CHECK\\s*\\(((?:[^()]|\\([^()]*\\))*)\\)(?:\\s*;)?");

    private final Map<String, PgType> aliasToTypeMap;

    private final Function<String, List<String>> decodeIdentifierFunc;
    ;

    @SuppressWarnings("unchecked")
    private PostgreComparer(ServerMeta serverMeta) {
        super(serverMeta);
        final String className = "io.army.dialect._PostgreDialectUtils";
        final Class<?>[] paramTypeArray = new Class<?>[0];

        this.aliasToTypeMap = (Map<String, PgType>) ReflectionUtils.invokeStaticFactoryMethod(
                className, Map.class, "getAliasToTypeMap", paramTypeArray);
        this.decodeIdentifierFunc = (Function<String, List<String>>) ReflectionUtils.invokeStaticFactoryMethod(
                className, Function.class, "decodeIdentifierFunc", paramTypeArray);
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
        typeName = identifierOf(dataType.typeName());
        if (!(dataType instanceof PgType)) {
            final boolean notMatch;
            if (dataType.isArray()) {
                notMatch = !typeName.equals(dataType.typeName().toUpperCase(Locale.ROOT))
                        && typeName.equals('_' + _DialectUtils.obtainElementType(dataType).typeName().toUpperCase(Locale.ROOT))
                ;
            } else {
                notMatch = !typeName.equals(dataType.typeName().toUpperCase(Locale.ROOT));
            }
            return notMatch;
        }

        final PgType type;
        type = this.aliasToTypeMap.get(typeName);

        final int precision, scale;
        precision = field.precision();
        scale = field.scale();

        final boolean notMatch;
        if (type == null) {
            notMatch = true;
        } else switch (type) {
            case CHAR:
            case CHAR_ARRAY:
            case VARCHAR:
            case VARCHAR_ARRAY:
            case BYTEA:
            case BYTEA_ARRAY:
            case BIT:
            case BIT_ARRAY:
            case VARBIT:
            case VARBIT_ARRAY:
                notMatch = precision > -1 && precision != columnInfo.precision();
                break;
            case TIME:
            case TIME_ARRAY:
            case TIMETZ:
            case TIMETZ_ARRAY:
            case TIMESTAMP:
            case TIMESTAMP_ARRAY:
            case TIMESTAMPTZ:
            case TIMESTAMPTZ_ARRAY:
                notMatch = scale > -1 && scale != columnInfo.scale();
                break;
            case VECTOR:  // Current JDBC does not return the real length of vector.
            case VECTOR_ARRAY:
            default:
                notMatch = (precision > -1 && precision != columnInfo.precision())
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
            list = this.decodeIdentifierFunc.apply(typeName);
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
