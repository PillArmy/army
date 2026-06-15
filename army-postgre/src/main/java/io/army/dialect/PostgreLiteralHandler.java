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
import io.army.mapping.array.PostgreArrays;
import io.army.meta.TypeMeta;
import io.army.sqltype.*;
import io.army.util.HexUtils;
import io.army.util._Exceptions;
import io.army.util._StringUtils;
import io.army.util._TimeUtils;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;

final class PostgreLiteralHandler extends ArmyLiteralHandler<PostgreParser> {


    PostgreLiteralHandler(PostgreParser parser) {
        super(parser);
    }


    @Override
    public void bindLiteral(TypeMeta typeMeta, DataType dataType, @Nullable Object value, boolean typeName,
                            StringBuilder sqlBuilder, @Nullable DataType container) {

        final boolean pgDataType = dataType instanceof PgType;
        if (!pgDataType && dataType instanceof SQLType) {
            throw _Exceptions.mapMethodError(typeMeta.mappingType(), PgType.class);
        }

        final char currentQuote;
        if (container == null) {
            currentQuote = _Constant.QUOTE;
        } else {
            currentQuote = _Constant.DOUBLE_QUOTE;
        }

        if (value == null) {

            if (container == null) {
                sqlBuilder.append(_Constant.NULL);
            } else if (container instanceof CustomType ct && ct.armyType() == ArmyType.COMPOSITE) {
                // output nothing, see https://www.postgresql.org/docs/current/rowtypes.html
            } else {
                sqlBuilder.append("null");
            }

            if (typeName) {
                sqlBuilder.append("::");
                if (pgDataType) {
                    sqlBuilder.append(dataType.componentTypeName());
                } else {
                    this.parser.safeObjectName(dataType, sqlBuilder);
                }
                if (dataType.isArray()) {
                    arrayDimensionSuffix(typeMeta, sqlBuilder);
                }
            }
        } else if (!pgDataType) {
            if (!(value instanceof String)) {
                throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
            }

            stringEscape((String) value, sqlBuilder, container);

            if (typeName) {
                sqlBuilder.append("::");
                this.parser.safeObjectName(dataType, sqlBuilder);
                if (dataType.isArray()) {
                    arrayDimensionSuffix(typeMeta, sqlBuilder);
                }
            }

        } else if (dataType.isArray()) {
            if (!(value instanceof String) || dataType == PgType.RECORD_ARRAY) {
                throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
            }

            stringEscape((String) value, sqlBuilder, container);

            if (typeName) {
                sqlBuilder.append("::")
                        .append(dataType.componentTypeName());
                arrayDimensionSuffix(typeMeta, sqlBuilder);
            }
        } else switch ((PgType) dataType) {
            case BOOLEAN:
                _Literals.bindBoolean(typeMeta, dataType, value, sqlBuilder);
                break;
            case SMALLINT: {
                if (!(value instanceof Short)) {
                    throw _Exceptions.beforeBindMethod(dataType, typeMeta.mappingType(), value);
                }
                sqlBuilder.append(value);
                if (typeName) {
                    sqlBuilder.append("::")
                            .append(dataType.typeName());
                }
            }
            break;
            case INTEGER: {
                if (!(value instanceof Integer)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(value);
                if (typeName) {
                    sqlBuilder.append("::")
                            .append(dataType.typeName());
                }
            }
            break;
            case BIGINT: {
                if (!(value instanceof Long)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(value);
                if (typeName) {
                    sqlBuilder.append("::")
                            .append(dataType.typeName());
                }
            }
            break;
            case DECIMAL: {
                if (!(value instanceof BigDecimal)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(((BigDecimal) value).toPlainString());
                if (typeName) {
                    sqlBuilder.append("::")
                            .append(dataType.typeName());
                }
            }
            break;
            case DOUBLE: {
                if (!(value instanceof Double)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(value);
                if (typeName) {
                    sqlBuilder.append("::")
                            .append(dataType.typeName());
                }
            }
            break;
            case REAL: {
                if (!(value instanceof Float)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                sqlBuilder.append(value);
                if (typeName) {
                    sqlBuilder.append("::")
                            .append(dataType.typeName());
                }
            }
            break;
            case TIME: {
                if (!(value instanceof LocalTime)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                if (typeName) {
                    sqlBuilder.append(dataType.typeName())
                            .append(_Constant.SPACE);
                }

                sqlBuilder.append(currentQuote)
                        .append(_TimeUtils.TIME_FORMATTER_6.format((LocalTime) value))
                        .append(currentQuote);
            }
            break;
            case DATE: {
                if (!(value instanceof LocalDate)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                if (typeName) {
                    sqlBuilder.append(dataType.typeName())
                            .append(_Constant.SPACE);
                }
                sqlBuilder.append(currentQuote)
                        .append(DateTimeFormatter.ISO_LOCAL_DATE.format((LocalDate) value))
                        .append(currentQuote);
            }
            break;
            case TIMETZ: {
                if (!(value instanceof OffsetTime)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                if (typeName) {
                    sqlBuilder.append(dataType.typeName())
                            .append(_Constant.SPACE);
                }
                sqlBuilder.append(currentQuote)
                        .append(_TimeUtils.OFFSET_TIME_FORMATTER_6.format((OffsetTime) value))
                        .append(currentQuote);
            }
            break;
            case TIMESTAMP: {
                if (!(value instanceof LocalDateTime)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                if (typeName) {
                    sqlBuilder.append(dataType.typeName())
                            .append(_Constant.SPACE);
                }
                sqlBuilder.append(currentQuote)
                        .append(_TimeUtils.DATETIME_FORMATTER_6.format((LocalDateTime) value))
                        .append(currentQuote);
            }
            break;
            case TIMESTAMPTZ: {
                if (!(value instanceof OffsetDateTime)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                }
                if (typeName) {
                    sqlBuilder.append(dataType.typeName())
                            .append(_Constant.SPACE);
                }
                sqlBuilder.append(currentQuote)
                        .append(_TimeUtils.OFFSET_DATETIME_FORMATTER_6.format((OffsetDateTime) value))
                        .append(currentQuote);
            }
            break;
            case CHAR:
            case BPCHAR:
            case VARCHAR:
            case TEXT:
            case JSON:
            case JSONB:
            case JSONPATH:
            case XML:
                // Geometric Types
            case POINT:
            case LINE:
            case LSEG:
            case BOX:
            case PATH:
            case POLYGON:
            case CIRCLE:
                // Network Address Types
            case CIDR:
            case INET:
            case MACADDR:
            case MACADDR8:
                //
            case UUID:
            case MONEY:
            case TSQUERY:
            case TSVECTOR:
                // Range Types
            case INT4RANGE:
            case INT8RANGE:
            case NUMRANGE:
            case TSRANGE:
            case TSTZRANGE:
            case DATERANGE:
                // multi range Types
            case INT4MULTIRANGE:
            case INT8MULTIRANGE:
            case NUMMULTIRANGE:
            case DATEMULTIRANGE:
            case TSMULTIRANGE:
            case TSTZMULTIRANGE:

            case INTERVAL:

            case ACLITEM:
            case PG_LSN:
            case PG_SNAPSHOT: {
                if (!(value instanceof String)) {
                    throw _Exceptions.beforeBindMethod(dataType, typeMeta.mappingType(), value);
                }
                if (typeName) {
                    sqlBuilder.append(dataType.typeName())
                            .append(_Constant.SPACE); //use dataType 'string' syntax not 'string'::dataType syntax,because XMLEXISTS function not work, see PostgreSQL 15.1 on x86_64-apple-darwin20.6.0, compiled by Apple clang version 12.0.0 (clang-1200.0.32.29), 64-bit
                }
                stringEscape((String) value, sqlBuilder, container);
            }
            break;
            case BYTEA: {
                if (!(value instanceof byte[])) {
                    throw _Exceptions.beforeBindMethod(dataType, typeMeta.mappingType(), value);
                }

                if (typeName) {
                    sqlBuilder.append(dataType.typeName())
                            .append(_Constant.SPACE); //use dataType 'string' syntax not 'string'::dataType syntax,because XMLEXISTS function not work, see PostgreSQL 15.1 on x86_64-apple-darwin20.6.0, compiled by Apple clang version 12.0.0 (clang-1200.0.32.29), 64-bit
                }

                sqlBuilder.append(currentQuote)
                        .append(_Constant.BACK_SLASH)
                        .append('x')
                        .append(HexUtils.hexEscapesText(true, (byte[]) value))
                        .append(currentQuote);
            }
            break;
            case VARBIT:
            case BIT: {

                if (!(value instanceof String)) {
                    throw ExecutorSupport.beforeBindMethodError(typeMeta.mappingType(), dataType, value);
                } else if (!_StringUtils.isBinary((String) value)) {
                    throw _Exceptions.valueOutRange(dataType, value);
                }
                if (typeName) {
                    sqlBuilder.append(dataType.typeName())
                            .append(_Constant.SPACE); //use dataType 'string' syntax not 'string'::dataType syntax,because XMLEXISTS function not work, see PostgreSQL 15.1 on x86_64-apple-darwin20.6.0, compiled by Apple clang version 12.0.0 (clang-1200.0.32.29), 64-bit
                }
                sqlBuilder.append('B')
                        .append(currentQuote)
                        .append(value)
                        .append(currentQuote);
            }
            break;
            case UNKNOWN:
            case REF_CURSOR:
            case RECORD:
                throw ExecutorSupport.mapMethodError(typeMeta.mappingType(), dataType);
            default:
                throw _Exceptions.unexpectedEnum((PgType) dataType);


        } // switch
    }


    /// @see #bindLiteral(TypeMeta, DataType, Object, boolean, StringBuilder, DataType)
    private void stringEscape(final CharSequence value, final StringBuilder builder, @Nullable DataType container) {
        if (container != null) {
            if (container.armyType() == ArmyType.COMPOSITE) {
                _PostgreLiterals.doubleQuoteBackSlashEscape(value, builder);
            } else {
                PostgreArrays.encodeElement(value, builder);
            }
        } else switch (this.literalEscapeMode) {
            case DEFAULT:
            case BACK_SLASH:
                _PostgreLiterals.backslashEscape(value, 0, value.length(), builder);
                break;
            case UNICODE:
                _PostgreLiterals.unicodeEscape(value, 0, value.length(), _Constant.BACK_SLASH, builder);
                break;
            default:
                throw _Exceptions.dontSupportEscapeMode(this.literalEscapeMode, this.serverMeta.usedDialect());
        }

    }


}
