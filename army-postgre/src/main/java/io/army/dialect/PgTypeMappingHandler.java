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
import io.army.mapping.postgre.PgMultiRangeType;
import io.army.mapping.postgre.PgSingleRangeType;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;

import java.util.Locale;

final class PgTypeMappingHandler extends TypeMappingHandlerSupport {


    PgTypeMappingHandler(DialectEnv env) {
        super(env);
    }

    @Override
    public DataType apply(final String typeName, final MappingType[] typeArray, final int index) {
        final DataType type;
        switch (typeName.toUpperCase(Locale.ROOT)) {
            case "BOOLEAN":
            case "BOOL":
                type = PgType.BOOLEAN;
                typeArray[index] = BooleanType.INSTANCE;
                break;
            case "INT2":
            case "SMALLINT":
            case "SMALLSERIAL":
                type = PgType.SMALLINT;
                typeArray[index] = ShortType.INSTANCE;
                break;
            case "INT":
            case "INT4":
            case "SERIAL":
            case "INTEGER":
            case "XID":  // https://www.postgresql.org/docs/current/datatype-oid.html
            case "CID":  // https://www.postgresql.org/docs/current/datatype-oid.html
                type = PgType.INTEGER;
                typeArray[index] = IntegerType.INSTANCE;
                break;
            case "INT8":
            case "BIGINT":
            case "BIGSERIAL":
            case "SERIAL8":
            case "XID8":  // https://www.postgresql.org/docs/current/datatype-oid.html  TODO what's tid ?
                type = PgType.BIGINT;
                typeArray[index] = LongType.INSTANCE;
                break;
            case "NUMERIC":
            case "DECIMAL":
                type = PgType.DECIMAL;
                typeArray[index] = BigDecimalType.INSTANCE;
                break;
            case "FLOAT8":
            case "DOUBLE PRECISION":
            case "FLOAT":
                type = PgType.FLOAT8;
                typeArray[index] = DoubleType.INSTANCE;
                break;
            case "FLOAT4":
            case "REAL":
                type = PgType.REAL;
                typeArray[index] = FloatType.INSTANCE;
                break;
            case "CHAR":
            case "CHARACTER":
                type = PgType.CHAR;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "VARCHAR":
            case "CHARACTER VARYING":
                type = PgType.VARCHAR;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "BPCHAR":
                type = PgType.BPCHAR;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "TEXT":
            case "TXID_SNAPSHOT":  // TODO txid_snapshot is text?
                type = PgType.TEXT;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "BYTEA":
                type = PgType.BYTEA;
                typeArray[index] = BinaryType.INSTANCE;
                break;
            case "DATE":
                type = PgType.DATE;
                typeArray[index] = LocalDateType.INSTANCE;
                break;
            case "TIME":
            case "TIME WITHOUT TIME ZONE":
                type = PgType.TIME;
                typeArray[index] = LocalTimeType.INSTANCE;
                break;
            case "TIMETZ":
            case "TIME WITH TIME ZONE":
                type = PgType.TIMETZ;
                typeArray[index] = OffsetTimeType.INSTANCE;
                break;
            case "TIMESTAMP":
            case "TIMESTAMP WITHOUT TIME ZONE":
                type = PgType.TIMESTAMP;
                typeArray[index] = LocalDateTimeType.INSTANCE;
                break;
            case "TIMESTAMPTZ":
            case "TIMESTAMP WITH TIME ZONE":
                type = PgType.TIMESTAMPTZ;
                typeArray[index] = OffsetDateTimeType.INSTANCE;
                break;
            case "INTERVAL":
                type = PgType.INTERVAL;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "JSON":
                type = PgType.JSON;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "JSONB":
                type = PgType.JSONB;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "JSONPATH":
                type = PgType.JSONPATH;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "XML":
                type = PgType.XML;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "BIT":
                type = PgType.BIT;
                typeArray[index] = BitSetType.INSTANCE;
                break;
            case "BIT VARYING":
            case "VARBIT":
                type = PgType.VARBIT;
                typeArray[index] = BitSetType.INSTANCE;
                break;

            case "CIDR":
                type = PgType.CIDR;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "INET":
                type = PgType.INET; // TODO add INET type ?
                typeArray[index] = StringType.INSTANCE;
                break;
            case "MACADDR8":
                type = PgType.MACADDR8;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "MACADDR":
                type = PgType.MACADDR;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "BOX":
                type = PgType.BOX;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "LSEG":
                type = PgType.LSEG;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "LINE":
                type = PgType.LINE;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "PATH":
                type = PgType.PATH;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "POINT":
                type = PgType.POINT;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "CIRCLE":
                type = PgType.CIRCLE;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "POLYGON":
                type = PgType.POLYGON;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "TSVECTOR":
                type = PgType.TSVECTOR;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "TSQUERY":
                type = PgType.TSQUERY;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "INT4RANGE":
                type = PgType.INT4RANGE;
                typeArray[index] = PgSingleRangeType.INT4_RANGE_TEXT;
                break;
            case "INT8RANGE":
                type = PgType.INT8RANGE;
                typeArray[index] = PgSingleRangeType.INT8_RANGE_TEXT;
                break;
            case "NUMRANGE":
                type = PgType.NUMRANGE;
                typeArray[index] = PgSingleRangeType.NUM_RANGE_TEXT;
                break;
            case "TSRANGE":
                type = PgType.TSRANGE;
                typeArray[index] = PgSingleRangeType.TS_RANGE_TEXT;
                break;
            case "DATERANGE":
                type = PgType.DATERANGE;
                typeArray[index] = PgSingleRangeType.DATE_RANGE_TEXT;
                break;
            case "TSTZRANGE":
                type = PgType.TSTZRANGE;
                typeArray[index] = PgSingleRangeType.TS_TZ_RANGE_TEXT;
                break;

            case "INT4MULTIRANGE":
                type = PgType.INT4MULTIRANGE;
                typeArray[index] = PgMultiRangeType.INT4_MULTI_RANGE_TEXT;
                break;
            case "INT8MULTIRANGE":
                type = PgType.INT8MULTIRANGE;
                typeArray[index] = PgMultiRangeType.INT8_MULTI_RANGE_TEXT;
                break;
            case "NUMMULTIRANGE":
                type = PgType.NUMMULTIRANGE;
                typeArray[index] = PgMultiRangeType.NUM_MULTI_RANGE_TEXT;
                break;
            case "DATEMULTIRANGE":
                type = PgType.DATEMULTIRANGE;
                typeArray[index] = PgMultiRangeType.DATE_MULTI_RANGE_TEXT;
                break;
            case "TSMULTIRANGE":
                type = PgType.TSMULTIRANGE;
                typeArray[index] = PgMultiRangeType.TS_MULTI_RANGE_TEXT;
                break;
            case "TSTZMULTIRANGE":
                type = PgType.TSTZMULTIRANGE;
                typeArray[index] = PgMultiRangeType.TS_TZ_MULTI_RANGE_TEXT;
                break;

            case "UUID":
                type = PgType.UUID;
                typeArray[index] = UUIDType.INSTANCE;
                break;
            case "MONEY":
                type = PgType.MONEY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "RECORD":
                type = PgType.RECORD;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "ACLITEM":
                type = PgType.ACLITEM;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "PG_LSN":
                type = PgType.PG_LSN;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "PG_SNAPSHOT":
                type = PgType.PG_SNAPSHOT;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "BOOLEAN[]":
            case "BOOL[]":
                type = PgType.BOOLEAN_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "INT2[]":
            case "SMALLINT[]":
            case "SMALLSERIAL[]":
                type = PgType.SMALLINT_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "INT[]":
            case "INT4[]":
            case "INTEGER[]":
            case "SERIAL[]":
                type = PgType.INTEGER_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "INT8[]":
            case "BIGINT[]":
            case "SERIAL8[]":
            case "BIGSERIAL[]":
                type = PgType.BIGINT_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "NUMERIC[]":
            case "DECIMAL[]":
                type = PgType.DECIMAL_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "FLOAT8[]":
            case "FLOAT[]":
            case "DOUBLE PRECISION[]":
                type = PgType.FLOAT8_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "FLOAT4[]":
            case "REAL[]":
                type = PgType.REAL_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "CHAR[]":
            case "CHARACTER[]":
                type = PgType.CHAR_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "VARCHAR[]":
            case "CHARACTER VARYING[]":
                type = PgType.VARCHAR_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "TEXT[]":
            case "TXID_SNAPSHOT[]":
                type = PgType.TEXT_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "BYTEA[]":
                type = PgType.BYTEA_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "DATE[]":
                type = PgType.DATE_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "TIME[]":
            case "TIME WITHOUT TIME ZONE[]":
                type = PgType.TIME_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "TIMETZ[]":
            case "TIME WITH TIME ZONE[]":
                type = PgType.TIMETZ_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "TIMESTAMP[]":
            case "TIMESTAMP WITHOUT TIME ZONE[]":
                type = PgType.TIMESTAMP_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "TIMESTAMPTZ[]":
            case "TIMESTAMP WITH TIME ZONE[]":
                type = PgType.TIMESTAMPTZ_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "INTERVAL[]":
                type = PgType.INTERVAL_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "JSON[]":
                type = PgType.JSON_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "JSONB[]":
                type = PgType.JSONB_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "JSONPATH[]":
                type = PgType.JSONPATH_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "XML[]":
                type = PgType.XML_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "VARBIT[]":
            case "BIT VARYING[]":
                type = PgType.VARBIT_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "BIT[]":
                type = PgType.BIT_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "UUID[]":
                type = PgType.UUID_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "CIDR[]":
                type = PgType.CIDR_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "INET[]":
                type = PgType.INET_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "MACADDR[]":
                type = PgType.MACADDR_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "MACADDR8[]":
                type = PgType.MACADDR8_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "BOX[]":
                type = PgType.BOX_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "LSEG[]":
                type = PgType.LSEG_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "LINE[]":
                type = PgType.LINE_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "PATH[]":
                type = PgType.PATH_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "POINT[]":
                type = PgType.POINT_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "CIRCLE[]":
                type = PgType.CIRCLE_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "POLYGON[]":
                type = PgType.POLYGON_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "TSQUERY[]":
                type = PgType.TSQUERY_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "TSVECTOR[]":
                type = PgType.TSVECTOR_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "INT4RANGE[]":
                type = PgType.INT4RANGE_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "INT8RANGE[]":
                type = PgType.INT8RANGE_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "NUMRANGE[]":
                type = PgType.NUMRANGE_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "DATERANGE[]":
                type = PgType.DATERANGE_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "TSRANGE[]":
                type = PgType.TSRANGE_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "TSTZRANGE[]":
                type = PgType.TSTZRANGE_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "INT4MULTIRANGE[]":
                type = PgType.INT4MULTIRANGE_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "INT8MULTIRANGE[]":
                type = PgType.INT8MULTIRANGE_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "NUMMULTIRANGE[]":
                type = PgType.NUMMULTIRANGE_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "DATEMULTIRANGE[]":
                type = PgType.DATEMULTIRANGE_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "TSMULTIRANGE[]":
                type = PgType.TSMULTIRANGE_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "TSTZMULTIRANGE[]":
                type = PgType.TSTZMULTIRANGE_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;

            case "MONEY[]":
                type = PgType.MONEY_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "RECORD[]":
            case "_RECORD":
                type = PgType.RECORD_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "PG_LSN[]":
                type = PgType.PG_LSN_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "PG_SNAPSHOT[]":
                type = PgType.PG_SNAPSHOT_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "ACLITEM[]":
                type = PgType.ACLITEM_ARRAY;
                typeArray[index] = StringType.INSTANCE;
                break;
            default:
                type = DataType.from(typeName);
                typeArray[index] = handleDefinedType(typeName, StringType.INSTANCE);
        }
        return type;
    }


}
