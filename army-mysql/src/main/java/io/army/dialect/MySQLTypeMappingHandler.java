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
import io.army.sqltype.DataType;
import io.army.sqltype.MySQLType;

import java.util.Locale;

final class MySQLTypeMappingHandler extends TypeMappingHandlerSupport {


    MySQLTypeMappingHandler(DialectEnv env) {
        super(env);
    }

    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/data-types.html">MySQL Data Types</a>
    @Override
    public DataType apply(String typeName, MappingType[] typeArray, int index) {
        final MySQLType type;
        switch (typeName.toUpperCase(Locale.ROOT)) {
            case "BOOL":
            case "BOOLEAN":
                type = MySQLType.BOOLEAN;
                typeArray[index] = BooleanType.INSTANCE;
                break;
            case "TINYINT":
                type = MySQLType.TINYINT;
                typeArray[index] = ByteType.INSTANCE;
                break;
            case "TINYINT UNSIGNED":
                type = MySQLType.TINYINT_UNSIGNED;
                typeArray[index] = UnsignedTinyIntType.INSTANCE;
                break;
            case "SMALLINT":
                type = MySQLType.SMALLINT;
                typeArray[index] = ShortType.INSTANCE;
                break;
            case "SMALLINT UNSIGNED":
                type = MySQLType.SMALLINT_UNSIGNED;
                typeArray[index] = UnsignedSmallIntType.INSTANCE;
                break;
            case "MEDIUMINT":
                type = MySQLType.MEDIUMINT;
                typeArray[index] = MediumIntType.INSTANCE;
                break;
            case "MEDIUMINT UNSIGNED":
                type = MySQLType.MEDIUMINT_UNSIGNED;
                typeArray[index] = UnsignedMediumIntType.INSTANCE;
                break;
            case "INT":
            case "INTEGER":
                type = MySQLType.INT;
                typeArray[index] = IntegerType.INSTANCE;
                break;
            case "INT UNSIGNED":
            case "INTEGER UNSIGNED":
                type = MySQLType.INT_UNSIGNED;
                typeArray[index] = UnsignedSqlIntType.INSTANCE;
                break;
            case "BIGINT":
                type = MySQLType.BIGINT;
                typeArray[index] = LongType.INSTANCE;
                break;
            case "BIGINT UNSIGNED":
                type = MySQLType.BIGINT_UNSIGNED;
                typeArray[index] = UnsignedBigintType.INSTANCE;
                break;
            case "DECIMAL":
            case "DEC":
            case "NUMERIC":
                type = MySQLType.DECIMAL;
                typeArray[index] = BigDecimalType.INSTANCE;
                break;
            case "DECIMAL UNSIGNED":
            case "DEC UNSIGNED":
            case "NUMERIC UNSIGNED":
                type = MySQLType.DECIMAL_UNSIGNED;
                typeArray[index] = BigDecimalType.INSTANCE;
                break;
            case "FLOAT":
            case "FLOAT UNSIGNED":
                type = MySQLType.FLOAT;
                typeArray[index] = FloatType.INSTANCE;
                break;
            case "DOUBLE":
            case "DOUBLE UNSIGNED":
                type = MySQLType.DOUBLE;
                typeArray[index] = DoubleType.INSTANCE;
                break;
            case "TIME":
                type = MySQLType.TIME;
                typeArray[index] = LocalTimeType.INSTANCE;
                break;
            case "DATE":
                type = MySQLType.DATE;
                typeArray[index] = LocalDateType.INSTANCE;
                break;
            case "YEAR":
                type = MySQLType.YEAR;
                typeArray[index] = YearType.INSTANCE;
                break;
            case "TIMESTAMP":
            case "DATETIME":
                type = MySQLType.DATETIME;
                typeArray[index] = LocalDateTimeType.INSTANCE;
                break;
            case "CHAR":
                type = MySQLType.CHAR;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "VARCHAR":
                type = MySQLType.VARCHAR;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "BIT":
                type = MySQLType.BIT;
                typeArray[index] = MySqlBitType.INSTANCE;
                break;
            case "ENUM":
                type = MySQLType.ENUM;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "SET":
                type = MySQLType.SET;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "JSON":
                type = MySQLType.JSON;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "TINYTEXT":
                type = MySQLType.TINYTEXT;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "MEDIUMTEXT":
                type = MySQLType.MEDIUMTEXT;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "TEXT":
                type = MySQLType.TEXT;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "LONGTEXT":
                type = MySQLType.LONGTEXT;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "BINARY":
                type = MySQLType.BINARY;
                typeArray[index] = BinaryType.INSTANCE;
                break;
            case "VARBINARY":
                type = MySQLType.VARBINARY;
                typeArray[index] = BinaryType.INSTANCE;
                break;
            case "TINYBLOB":
                type = MySQLType.TINYBLOB;
                typeArray[index] = BinaryType.INSTANCE;
                break;
            case "MEDIUMBLOB":
                type = MySQLType.MEDIUMBLOB;
                typeArray[index] = BinaryType.INSTANCE;
                break;
            case "BLOB":
                type = MySQLType.BLOB;
                typeArray[index] = BinaryType.INSTANCE;
                break;
            case "LONGBLOB":
                type = MySQLType.LONGBLOB;
                typeArray[index] = BinaryType.INSTANCE;
                break;
            case "GEOMETRY":
                type = MySQLType.GEOMETRY;
                typeArray[index] = BinaryType.INSTANCE;
                break;
            case "NULL":
                type = MySQLType.NULL;
                typeArray[index] = StringType.INSTANCE;
                break;
            case "UNKNOWN":
            default:
                type = MySQLType.UNKNOWN;
                typeArray[index] = StringType.INSTANCE;
        }

        return type;
    }

}
