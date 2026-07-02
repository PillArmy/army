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
import io.army.mapping.array.*;
import io.army.sqltype.PgType;
import io.army.transaction.Isolation;
import io.army.util._Collections;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

final class PgMappingHandler extends TypeMappingHandlerSupport {


    PgMappingHandler(DialectEnv env) {
        super(env);
    }


    /// @see <a href="https://www.postgresql.org/docs/current/runtime-config-client.html#GUC-DEFAULT-TRANSACTION-ISOLATION">default_transaction_isolation</a>
    @Override
    public Isolation nameToIsolation(final String level) {
        final Isolation isolation;
        switch (level.toUpperCase(Locale.ROOT)) {
            case "READ COMMITTED":
                isolation = Isolation.READ_COMMITTED;
                break;
            case "REPEATABLE READ":
                isolation = Isolation.REPEATABLE_READ;
                break;
            case "SERIALIZABLE":
                isolation = Isolation.SERIALIZABLE;
                break;
            case "READ UNCOMMITTED":
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


    @Override
    MappingType obtainStringArrayType() {
        return StringArrayType.UNLIMITED;
    }

    /// @see <a href="https://www.postgresql.org/docs/current/datatype.html">Data Types</a>
    @Override
    Map<String, TypeMappingBundle> createBuildInTypeBundleMap(DialectEnv env) {
        EnumMap<PgType, MappingType> definedTypeMap;
        definedTypeMap = createSQLTypeToMappingTypeMap(PgType.class, env.serverMeta(), env.definedTypeSet());

        final PgType[] array = PgType.values();
        final Map<PgType, TypeMappingBundle> map = _Collections.hashMapForSize(array.length);

        MappingType mappingType;

        for (PgType type : array) {
            switch (type) {
                case BOOLEAN:
                    mappingType = BooleanType.INSTANCE;
                    break;
                case SMALLINT:
                    mappingType = ShortType.INSTANCE;
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
                case REAL:
                    mappingType = FloatType.INSTANCE;
                    break;
                case DOUBLE:
                    mappingType = DoubleType.INSTANCE;
                    break;
                case BYTEA:
                    mappingType = VarBinaryType.INSTANCE;
                    break;
                case DATE:
                    mappingType = LocalDateType.INSTANCE;
                    break;
                case TIME:
                    mappingType = LocalTimeType.INSTANCE;
                    break;
                case TIMETZ:
                    mappingType = OffsetTimeType.INSTANCE;
                    break;
                case TIMESTAMP:
                    mappingType = LocalDateTimeType.INSTANCE;
                    break;
                case TIMESTAMPTZ:
                    mappingType = OffsetDateTimeType.INSTANCE;
                    break;
                case UUID:
                    mappingType = UUIDType.INSTANCE;
                    break;
                case BIT:
                case VARBIT:
                    mappingType = BitSetType.INSTANCE;
                    break;
                // array
                case BOOLEAN_ARRAY:
                    mappingType = BooleanArrayType.UNLIMITED;
                    break;
                case SMALLINT_ARRAY:
                    mappingType = ShortArrayType.UNLIMITED;
                    break;
                case INTEGER_ARRAY:
                    mappingType = IntegerArrayType.UNLIMITED;
                    break;
                case BIGINT_ARRAY:
                    mappingType = LongArrayType.UNLIMITED;
                    break;
                case DECIMAL_ARRAY:
                    mappingType = BigDecimalArrayType.UNLIMITED;
                    break;
                case REAL_ARRAY:
                    mappingType = FloatArrayType.UNLIMITED;
                    break;
                case DOUBLE_ARRAY:
                    mappingType = DoubleArrayType.UNLIMITED;
                    break;
                case BYTEA_ARRAY:
                    mappingType = VarBinaryArrayType.UNLIMITED;
                    break;
                case DATE_ARRAY:
                    mappingType = LocalDateArrayType.UNLIMITED;
                    break;
                case TIME_ARRAY:
                    mappingType = LocalTimeArrayType.UNLIMITED;
                    break;
                case TIMETZ_ARRAY:
                    mappingType = OffsetTimeArrayType.UNLIMITED;
                    break;
                case TIMESTAMP_ARRAY:
                    mappingType = LocalDateTimeArrayType.UNLIMITED;
                    break;
                case TIMESTAMPTZ_ARRAY:
                    mappingType = OffsetDateTimeArrayType.UNLIMITED;
                    break;
                case UUID_ARRAY:
                    mappingType = UUIDArrayType.UNLIMITED;
                    break;
                default: {
                    mappingType = definedTypeMap.get(type);
                    if (mappingType != null) {
                        break;
                    } else if (type.isArray()) {
                        mappingType = StringArrayType.UNLIMITED;
                    } else {
                        mappingType = StringType.INSTANCE;
                    }
                } // default
            } // switch

            map.put(type, TypeMappingBundle.of(type, mappingType));
        } // loop

        final Map<String, PgType> aliasToTypeMap = _PostgreDialectUtils.getAliasToTypeMap();

        TypeMappingBundle bundle;
        final Map<String, TypeMappingBundle> aliasToBoundleMap = _Collections.hashMapForSize(aliasToTypeMap.size());
        for (Map.Entry<String, PgType> e : aliasToTypeMap.entrySet()) {

            bundle = map.get(e.getValue());
            Objects.requireNonNull(bundle);
            aliasToBoundleMap.put(e.getKey(), bundle);
        }

        return Map.copyOf(aliasToBoundleMap);
    }


}
