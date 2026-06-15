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

import io.army.executor.ExecutorSupport;
import io.army.mapping.*;
import io.army.mapping.mysql.MySqlBitType;
import io.army.sqltype.MySQLType;
import io.army.transaction.Isolation;
import io.army.util._Collections;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

final class MySQLMappingHandler extends TypeMappingHandlerSupport {

    private static final Map<String, TypeMappingBundle> ALIAS_TO_BUNDLE_MAP = Map.copyOf(createTypeMappingBoundleMap());

    MySQLMappingHandler(DialectEnv env) {
        super(env);
    }

    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/data-types.html">MySQL Data Types</a>
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
    public Isolation nameToIsolation(final String level) {
        final Isolation isolation;
        switch (level.toUpperCase(Locale.ROOT)) {
            case "READ-COMMITTED":
                isolation = Isolation.READ_COMMITTED;
                break;
            case "REPEATABLE-READ":
                isolation = Isolation.REPEATABLE_READ;
                break;
            case "SERIALIZABLE":
                isolation = Isolation.SERIALIZABLE;
                break;
            case "READ-UNCOMMITTED":
                isolation = Isolation.READ_UNCOMMITTED;
                break;
            default:
                throw ExecutorSupport.unknownIsolation(level);
        }
        return isolation;
    }

    @Override
    public String isolationToName(Isolation isolation) {
        return standardIsolationToName(isolation);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/datatype.html">Data Types</a>
    private static Map<String, TypeMappingBundle> createTypeMappingBoundleMap() {
        final MySQLType[] values = MySQLType.values();
        final Map<MySQLType, TypeMappingBundle> map = _Collections.hashMapForSize(values.length);

        MappingType mappingType;
        for (MySQLType type : values) {
            switch (type) {
                case BOOLEAN:
                    mappingType = BooleanType.INSTANCE;
                    break;
                case TINYINT:
                    mappingType = ByteType.INSTANCE;
                    break;
                case TINYINT_UNSIGNED:
                    mappingType = TinyIntUnsignedType.INSTANCE;
                    break;
                case SMALLINT:
                    mappingType = ShortType.INSTANCE;
                    break;
                case SMALLINT_UNSIGNED:
                    mappingType = SmallIntUnsignedType.INSTANCE;
                    break;
                case MEDIUMINT:
                    mappingType = MediumIntType.INSTANCE;
                    break;
                case MEDIUMINT_UNSIGNED:
                    mappingType = MediumIntUnsignedType.INSTANCE;
                    break;
                case INT:
                    mappingType = IntegerType.INSTANCE;
                    break;
                case INT_UNSIGNED:
                    mappingType = SqlIntUnsignedType.INSTANCE;
                    break;
                case BIGINT:
                    mappingType = LongType.INSTANCE;
                    break;
                case BIGINT_UNSIGNED:
                    mappingType = BigintUnsignedType.INSTANCE;
                    break;
                case DECIMAL:
                    mappingType = BigDecimalType.INSTANCE;
                    break;
                case DECIMAL_UNSIGNED:
                    mappingType = BigDecimalUnsignedType.INSTANCE;
                    break;
                case FLOAT:
                    mappingType = FloatType.INSTANCE;
                    break;
                case DOUBLE:
                    mappingType = DoubleType.INSTANCE;
                    break;
                case BINARY:
                case VARBINARY:
                case TINYBLOB:
                case MEDIUMBLOB:
                case BLOB:
                case LONGBLOB:
                case GEOMETRY:  // In the MySQL client protocol, only the GEOMETRY type is available, with no other GEOMETRY subtypes.
                    // https://dev.mysql.com/doc/dev/mysql-server/latest/field__types_8h.html#a69e798807026a0f7e12b1d6c72374854
                    mappingType = VarBinaryType.INSTANCE;
                    break;
                case DATE:
                    mappingType = LocalDateType.INSTANCE;
                    break;
                case TIME:
                    mappingType = LocalTimeType.INSTANCE;
                    break;
                case DATETIME:
                    mappingType = LocalDateTimeType.INSTANCE;
                    break;
                case BIT:
                    mappingType = MySqlBitType.INSTANCE;
                    break;
                case YEAR:
                    mappingType = YearType.INSTANCE;
                    break;
                case VECTOR:
                    mappingType = VectorType.INSTANCE;
                    break;
                default:
                    mappingType = StringType.INSTANCE;
            } // switch

            map.put(type, TypeMappingBundle.of(type, mappingType));
        } // loop

        final Map<String, MySQLType> aliasToTypeMap = _MySQLDialectUtils.getAliasToTypeMap();

        TypeMappingBundle bundle;
        final Map<String, TypeMappingBundle> aliasToBoundleMap = _Collections.hashMapForSize(aliasToTypeMap.size());
        for (Map.Entry<String, MySQLType> e : aliasToTypeMap.entrySet()) {

            bundle = map.get(e.getValue());
            Objects.requireNonNull(bundle);
            aliasToBoundleMap.put(e.getKey(), bundle);
        }

        return Map.copyOf(aliasToBoundleMap);
    }


}
