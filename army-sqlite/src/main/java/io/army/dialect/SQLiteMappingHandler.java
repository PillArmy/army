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

import io.army.mapping.*;
import io.army.mapping.mysql.MySqlBitType;
import io.army.sqltype.SQLiteType;
import io.army.transaction.Isolation;
import io.army.util._Collections;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

final class SQLiteMappingHandler extends TypeMappingHandlerSupport {

    private static final Map<String, TypeMappingBundle> ALIAS_TO_BUNDLE_MAP = Map.copyOf(createTypeMappingBoundleMap());

    SQLiteMappingHandler(DialectEnv env) {
        super(env);
    }


    /// @see <a href="https://sqlite.org/datatype3.html">Datatypes In SQLite</a>
    /// @see <a href="https://sqlite.org/datatypes.html">Datatypes In SQLite Version 2</a>
    @Override
    public TypeMappingBundle apply(String typeName) {
        TypeMappingBundle bundle;
        bundle = ALIAS_TO_BUNDLE_MAP.get(typeName.toUpperCase(Locale.ROOT));
        if (bundle == null) {
            bundle = handleDefined(typeName);
        }
        return bundle;
    }

    @Override
    public Isolation nameToIsolation(String level) {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String isolationToName(Isolation isolation) {
        //TODO
        throw new UnsupportedOperationException();
    }

    /// @see <a href="https://sqlite.org/datatype3.html">Datatypes In SQLite</a>
    /// @see <a href="https://sqlite.org/datatypes.html">Datatypes In SQLite Version 2</a>
    private static Map<String, TypeMappingBundle> createTypeMappingBoundleMap() {
        final SQLiteType[] values = SQLiteType.values();
        final Map<SQLiteType, TypeMappingBundle> map = _Collections.hashMapForSize(values.length);

        MappingType mappingType;
        for (SQLiteType type : values) {
            switch (type) {
                case BOOLEAN:
                    mappingType = BooleanType.INSTANCE;
                    break;
                case TINYINT:
                    mappingType = ByteType.INSTANCE;
                    break;
                case SMALLINT:
                    mappingType = ShortType.INSTANCE;
                    break;
                case MEDIUMINT:
                    mappingType = MediumIntType.INSTANCE;
                    break;
                case INTEGER:
                    mappingType = IntegerType.INSTANCE;
                    break;
                case BIGINT:
                    mappingType = LongType.INSTANCE;
                    break;
                case DECIMAL:
                    mappingType = BigDecimalType.INSTANCE;
                    break;
                case FLOAT:
                    mappingType = FloatType.INSTANCE;
                    break;
                case DOUBLE:
                    mappingType = DoubleType.INSTANCE;
                    break;
                case BINARY:
                case VARBINARY:
                case BLOB:
                    mappingType = VarBinaryType.INSTANCE;
                    break;
                case DATE:
                    mappingType = LocalDateType.INSTANCE;
                    break;
                case TIME:
                    mappingType = LocalTimeType.INSTANCE;
                    break;
                case TIMESTAMP:
                    mappingType = LocalDateTimeType.INSTANCE;
                    break;
                case BIT:
                    mappingType = MySqlBitType.INSTANCE;
                    break;
                case YEAR:
                    mappingType = YearType.INSTANCE;
                    break;
                default:
                    mappingType = StringType.INSTANCE;
            } // switch

            map.put(type, TypeMappingBundle.of(type, mappingType));
        } // loop

        final Map<String, SQLiteType> aliasToTypeMap = _SQLiteDialectUtils.getAliasToTypeMap();

        TypeMappingBundle bundle;
        final Map<String, TypeMappingBundle> aliasToBoundleMap = _Collections.hashMapForSize(aliasToTypeMap.size());
        for (Map.Entry<String, SQLiteType> e : aliasToTypeMap.entrySet()) {

            bundle = map.get(e.getValue());
            Objects.requireNonNull(bundle);
            aliasToBoundleMap.put(e.getKey(), bundle);
        }

        return Map.copyOf(aliasToBoundleMap);
    }

}
