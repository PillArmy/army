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

import io.army.mapping.BooleanType;
import io.army.mapping.MappingType;
import io.army.sqltype.DataType;
import io.army.sqltype.SQLiteType;

import java.util.Locale;

final class SQLiteTypeMappingHandler extends TypeMappingHandlerSupport {

    SQLiteTypeMappingHandler(DialectEnv env) {
        super(env);
    }


    /// @see <a href="https://sqlite.org/datatype3.html">Datatypes In SQLite</a>
    /// @see <a href="https://sqlite.org/datatypes.html">Datatypes In SQLite Version 2</a>
    @Override
    public DataType apply(String typeName, MappingType[] typeArray, int index) {

        final SQLiteType dataType;
        switch (typeName.toUpperCase(Locale.ROOT)) {
            case "BOOLEAN":
                dataType = SQLiteType.BOOLEAN;
                typeArray[index] = BooleanType.INSTANCE;
                break;
            case "TINYINT":  //TODO fix this method for SQLite
                dataType = SQLiteType.TINYINT;
                break;
            case "SMALLINT":
            case "INT2":
                dataType = SQLiteType.SMALLINT;
                break;
            case "MEDIUMINT":
                dataType = SQLiteType.MEDIUMINT;
                break;
            case "INTEGER":
            case "INT":
            case "INT4":
                dataType = SQLiteType.INTEGER;
                break;
            case "BIGINT":
            case "INT8":
                dataType = SQLiteType.BIGINT;
                break;
            case "UNSIGNED BIG INT":
                dataType = SQLiteType.UNSIGNED_BIG_INT;
                break;
            case "DECIMAL":
            case "NUMERIC":
                dataType = SQLiteType.DECIMAL;
                break;
            case "FLOAT":
            case "FLOAT4":
                dataType = SQLiteType.FLOAT;
                break;
            case "DOUBLE":
            case "REAL":
            case "FLOAT8":
            case "DOUBLE PRECISION":
                dataType = SQLiteType.DOUBLE;
                break;
            case "CHAR":
            case "CHARACTER":
            case "NCHAR":
            case "NATIVE CHARACTER":
                dataType = SQLiteType.CHAR;
                break;
            case "VARCHAR":
            case "VARYING CHARACTER":
                dataType = SQLiteType.VARCHAR;
                break;
            case "TEXT":
            case "CLOB":
                dataType = SQLiteType.TEXT;
                break;
            case "VARBINARY":
                dataType = SQLiteType.VARBINARY;
                break;
            case "BLOB":
                dataType = SQLiteType.BLOB;
                break;
            case "TIME":
                dataType = SQLiteType.TIME;
                break;
            case "TIME WITH TIMEZONE":
            case "TIMETZ":
                dataType = SQLiteType.TIME_WITH_TIMEZONE;
                break;
            case "TIMESTAMP":
            case "DATETIME":
                dataType = SQLiteType.TIMESTAMP;
                break;
            case "TIMESTAMP WITH TIMEZONE":
            case "TIMESTAMPTZ":
                dataType = SQLiteType.TIMESTAMP_WITH_TIMEZONE;
                break;
            case "DATE":
                dataType = SQLiteType.DATE;
                break;
            case "YEAR":
                dataType = SQLiteType.YEAR;
                break;
            case "YEAR MONTH":
                dataType = SQLiteType.YEAR_MONTH;
                break;
            case "MONTH DAY":
                dataType = SQLiteType.MONTH_DAY;
                break;
            case "DURATION":
                dataType = SQLiteType.DURATION;
                break;
            case "PERIOD":
                dataType = SQLiteType.PERIOD;
                break;
            case "BIT":
                dataType = SQLiteType.BIT;
                break;
            case "JSON":
                dataType = SQLiteType.JSON;
                break;
            case "":
                dataType = SQLiteType.DYNAMIC;
                break;
            case "UNKNOWN":
            default:
                dataType = SQLiteType.UNKNOWN;
        }
        return dataType;
    }

}
