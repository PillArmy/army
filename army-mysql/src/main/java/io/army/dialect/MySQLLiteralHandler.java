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

import io.army.criteria.CriteriaException;
import io.army.executor.ExecutorSupport;
import io.army.lang.Nullable;
import io.army.meta.TypeMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.MySQLType;
import io.army.util.HexUtils;
import io.army.util._Exceptions;
import io.army.util._TimeUtils;

import java.math.BigInteger;
import java.time.*;

final class MySQLLiteralHandler extends ArmyLiteralHandler<MySQLParser> {

    MySQLLiteralHandler(MySQLParser parser) {
        super(parser);
    }

    @Override
    void bindLiteral(TypeMeta typeMeta, DataType dataType, @Nullable Object value, boolean typeName,
                     StringBuilder sqlBuilder, @Nullable DataType container) {
        if (!(dataType instanceof MySQLType)) {
            throw _Exceptions.unrecognizedTypeLiteral(this.serverMeta.usedDialect().database(), dataType);
        }

        if (value == null) {
            sqlBuilder.append(_Constant.NULL);
        } else switch ((MySQLType) dataType) {
            case BOOLEAN:
                _Literals.bindBoolean(typeMeta, dataType, value, sqlBuilder);
                break;
            case SMALLINT_UNSIGNED:
            case INT:
            case MEDIUMINT:
            case MEDIUMINT_UNSIGNED: {
                if (!(value instanceof Integer)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(value);
            }
            break;
            case INT_UNSIGNED:
            case BIGINT: {
                if (!(value instanceof Long)) {
                    throw _Exceptions.beforeBindMethod(dataType, typeMeta.mappingType(), value);
                }
                sqlBuilder.append(value);
            }
            break;
            case DECIMAL:
            case DECIMAL_UNSIGNED:
                _Literals.bindBigDecimal(typeMeta, dataType, value, sqlBuilder);
                break;
            case DATETIME: {
                final String timeText;
                if (value instanceof LocalDateTime) {
                    timeText = _TimeUtils.format((LocalDateTime) value, typeMeta);
                } else if (!this.parser.asOf80) {
                    throw new CriteriaException("prior to MySQL 8.x,don't support time zone offset to DATETIME type.");
                } else if (value instanceof OffsetDateTime) {
                    timeText = _TimeUtils.format((OffsetDateTime) value, typeMeta);
                } else if (value instanceof ZonedDateTime) {
                    timeText = _TimeUtils.format(((ZonedDateTime) value).toOffsetDateTime(), typeMeta);
                } else {
                    throw _Exceptions.outRangeOfSqlType(MySQLType.DATETIME, value);
                }

                if (typeName) {
                    sqlBuilder.append("TIMESTAMP ");
                }
                sqlBuilder.append(_Constant.QUOTE)
                        .append(timeText)
                        .append(_Constant.QUOTE);
            }
            break;
            case DATE: {
                if (typeName) {
                    sqlBuilder.append("DATE ");
                }
                _Literals.bindLocalDate(typeMeta, dataType, value, sqlBuilder);
            }
            break;
            case TIME: {
                if (typeName) {
                    sqlBuilder.append("TIME ");
                }
                sqlBuilder.append(_Constant.QUOTE);
                if (value instanceof LocalTime) {
                    sqlBuilder.append(_TimeUtils.format((LocalTime) value, typeMeta));
                } else if (value instanceof Duration) {
                    sqlBuilder.append(_TimeUtils.durationToTimeText((Duration) value));
                } else {
                    throw _Exceptions.beforeBindMethod(dataType, typeMeta.mappingType(), value);
                }
                sqlBuilder.append(_Constant.QUOTE);
            }
            break;
            case CHAR:
            case VARCHAR:
            case TINYTEXT:
            case TEXT:
            case MEDIUMTEXT:
            case LONGTEXT:
            case JSON:
            case ENUM:
            case SET: {
                if (!(value instanceof String)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                MySQLLiterals.mysqlEscapes(this.literalEscapeMode, (String) value, sqlBuilder);
            }
            break;
            case BINARY:
            case VARBINARY:
            case TINYBLOB:
            case BLOB:
            case MEDIUMBLOB:
            case LONGBLOB: {
                if (!(value instanceof byte[])) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append("0x")
                        .append(_Literals.hexEscapes((byte[]) value));
            }
            break;
            case BIT: {
                if (!(value instanceof Long)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                // https://dev.mysql.com/doc/refman/8.0/en/bit-value-literals.html

                sqlBuilder.append("0b")
                        .append(Long.toBinaryString((Long) value));
            }
            break;
            case DOUBLE: {
                if (!(value instanceof Double || value instanceof Float)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(value);
            }
            break;
            case FLOAT: {
                if (!(value instanceof Float)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(value);
            }
            break;
            case TINYINT: {
                if (!(value instanceof Byte)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(value);
            }
            break;
            case BIGINT_UNSIGNED: {
                if (!(value instanceof BigInteger)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(value);
            }
            break;
            case TINYINT_UNSIGNED:
            case SMALLINT: {
                if (!(value instanceof Short)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(value);
            }
            break;
            case YEAR: {
                if (value instanceof Short) {
                    sqlBuilder.append(value);
                } else if (value instanceof Year) {
                    sqlBuilder.append(((Year) value).getValue());
                } else {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
            }
            break;
            case POINT:
            case LINESTRING:
            case POLYGON:
            case MULTIPOINT:
            case MULTIPOLYGON:
            case MULTILINESTRING:
            case GEOMETRYCOLLECTION:
            case GEOMETRY: {
                if (value instanceof byte[]) {
                    sqlBuilder.append("0x")
                            .append(HexUtils.hexEscapesText(true, (byte[]) value));
                } else if (value instanceof String) {
                    MySQLLiterals.mysqlEscapes(this.literalEscapeMode, (String) value, sqlBuilder);
                } else {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
            }
            break;
            case VECTOR: {
                if (!(value instanceof byte[])) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append("0x")
                        .append(HexUtils.hexEscapesText(true, (byte[]) value));
            }
            break;
            case NULL:
            case UNKNOWN:
                throw ExecutorSupport.mapMethodError(typeMeta.mappingType(), dataType);
            default:
                throw _Exceptions.unexpectedEnum((MySQLType) dataType);
        }
    }


}
