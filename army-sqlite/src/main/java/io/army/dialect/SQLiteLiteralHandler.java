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
import io.army.lang.Nullable;
import io.army.meta.TypeMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.SQLiteType;
import io.army.util.HexUtils;
import io.army.util._Exceptions;
import io.army.util._TimeUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

final class SQLiteLiteralHandler extends ArmyLiteralHandler<SQLiteParser> {

    SQLiteLiteralHandler(SQLiteParser parser) {
        super(parser);
    }

    /// @see <a href="https://sqlite.org/lang_expr.html">Literal Values (Constants)</a>
    @Override
    void bindLiteral(TypeMeta typeMeta, DataType dataType, @Nullable Object value, boolean typeName,
                     StringBuilder sqlBuilder, @Nullable DataType container) {
        if (!(dataType instanceof SQLiteType)) {
            throw _Exceptions.unrecognizedTypeLiteral(this.serverMeta.usedDialect().database(), dataType);
        }

        if (value == null) {
            sqlBuilder.append(_Constant.NULL);
        } else switch ((SQLiteType) dataType) {
            case BOOLEAN:
                _Literals.bindBoolean(typeMeta, dataType, value, sqlBuilder);
                break;
            case TINYINT: {
                if (!(value instanceof Byte)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(value);
            }
            break;
            case SMALLINT: {
                if (!(value instanceof Short)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(value);
            }
            break;
            case MEDIUMINT:
            case INTEGER: {
                if (!(value instanceof Integer)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(value);
            }
            break;
            case BIGINT: {
                if (!(value instanceof Long)) {
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
            case DOUBLE: {
                if (!(value instanceof Double || value instanceof Float)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(value);
            }
            break;
            case DECIMAL:
                _Literals.bindBigDecimal(typeMeta, dataType, value, sqlBuilder);
                break;
            case BIT: {
                if (!(value instanceof Long || value instanceof Integer)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(value);
            }
            break;
            case VARCHAR:
            case TEXT:
            case JSON: {
                if (!(value instanceof String)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                escapeText((String) value, 0, ((String) value).length(), sqlBuilder);
            }
            break;
            case VARBINARY:
            case BLOB: {
                if (!(value instanceof byte[])) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append('x')
                        .append(_Constant.QUOTE)
                        .append(HexUtils.hexEscapesText(true, (byte[]) value))
                        .append(_Constant.QUOTE);

            }
            break;
            case TIMESTAMP: {
                if (!(value instanceof LocalDateTime)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(_Constant.QUOTE)
                        .append(_TimeUtils.DATETIME_FORMATTER_6.format((LocalDateTime) value))
                        .append(_Constant.QUOTE);
            }
            break;
            case TIMESTAMP_WITH_TIMEZONE: {
                if (!(value instanceof OffsetDateTime || value instanceof ZonedDateTime)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(_Constant.QUOTE)
                        .append(_TimeUtils.OFFSET_DATETIME_FORMATTER_6.format((TemporalAccessor) value))
                        .append(_Constant.QUOTE);
            }
            break;
            case DATE: {
                if (!(value instanceof LocalDate)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(_Constant.QUOTE)
                        .append(DateTimeFormatter.ISO_LOCAL_DATE.format((TemporalAccessor) value))
                        .append(_Constant.QUOTE);
            }
            break;
            case TIME: {
                if (!(value instanceof LocalTime)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(_Constant.QUOTE)
                        .append(_TimeUtils.TIME_FORMATTER_6.format((TemporalAccessor) value))
                        .append(_Constant.QUOTE);
            }
            break;
            case TIME_WITH_TIMEZONE: {
                if (!(value instanceof OffsetTime)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(_Constant.QUOTE)
                        .append(_TimeUtils.OFFSET_TIME_FORMATTER_6.format((TemporalAccessor) value))
                        .append(_Constant.QUOTE);
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
            case MONTH_DAY: {
                if (!(value instanceof MonthDay)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(_Constant.QUOTE)
                        .append(value)
                        .append(_Constant.QUOTE);
            }
            break;
            case YEAR_MONTH: {
                if (!(value instanceof YearMonth)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(_Constant.QUOTE)
                        .append(value)
                        .append(_Constant.QUOTE);
            }
            break;
            case PERIOD: {
                if (!(value instanceof Period)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(_Constant.QUOTE)
                        .append(value)
                        .append(_Constant.QUOTE);
            }
            break;
            case DURATION: {
                if (!(value instanceof Duration)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(_Constant.QUOTE)
                        .append(value)
                        .append(_Constant.QUOTE);
            }
            break;
            case DYNAMIC: {
                if (value instanceof BigDecimal) {
                    _Literals.bindBigDecimal(typeMeta, dataType, value, sqlBuilder);
                } else if (value instanceof Number) {
                    if (value instanceof Integer
                            || value instanceof Long
                            || value instanceof Double
                            || value instanceof Float
                            || value instanceof Short
                            || value instanceof Byte
                            || value instanceof BigInteger) {
                        sqlBuilder.append(value);
                    } else {
                        final String v = value.toString();
                        escapeText(v, 0, v.length(), sqlBuilder);
                    }
                } else if (value instanceof String) {
                    escapeText((String) value, 0, ((String) value).length(), sqlBuilder);
                } else if (value instanceof byte[]) {
                    sqlBuilder.append('x')
                            .append(_Constant.QUOTE)
                            .append(HexUtils.hexEscapesText(true, (byte[]) value))
                            .append(_Constant.QUOTE);
                } else {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
            }
            break;
            case NULL:
            case UNKNOWN:
                throw ExecutorSupport.mapMethodError(typeMeta.mappingType(), dataType);
            default:
                throw _Exceptions.unexpectedEnum((SQLiteType) dataType);
        }

    }

    static void escapeText(final CharSequence value, final int offset, final int end, final StringBuilder builder) {


        builder.append(_Constant.QUOTE);

        int lastWritten = 0;
        for (int i = offset; i < end; i++) {
            if (value.charAt(i) == _Constant.QUOTE) {
                if (i > lastWritten) {
                    builder.append(value, lastWritten, i);
                }
                builder.append(_Constant.QUOTE);
                lastWritten = i; // not i + 1 as current char wasn't written
            }

        }

        if (lastWritten < end) {
            builder.append(value, lastWritten, end);
        }

        builder.append(_Constant.QUOTE);

    }


}
