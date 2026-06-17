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

package io.army.mapping.guava;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import io.army.criteria.CriteriaException;
import io.army.dialect.Database;
import io.army.dialect.LiteralHandler;
import io.army.dialect.UnsupportedDialectException;
import io.army.dialect._Constant;
import io.army.executor.DataAccessException;
import io.army.function.TextFunction;
import io.army.lang.Nullable;
import io.army.mapping.*;
import io.army.mapping.array.RangeArrayType;
import io.army.meta.ServerMeta;
import io.army.serialize.RangeDeserializer;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;
import io.army.util._Assert;
import io.army.util._Exceptions;

import java.math.BigDecimal;
import java.time.*;
import java.util.Objects;
import java.util.function.Supplier;

/// @see GuavaRangeSetType
/// @see Range
/// @see <a href="https://www.postgresql.org/docs/current/rangetypes.html#RANGETYPES-IO">Range Types</a>
public abstract class GuavaRangeType extends _ArmyBuildInType implements MappingType.SqlRange, UnaryGenericsMapping {

    public static GuavaRangeType fromTypeArg(final Class<?> javaType, final Class<?> typeArg) {
        if (javaType != Range.class) {
            throw errorJavaType(GuavaRangeType.class, javaType);
        }
        return BuildInRangeType.CLASS_VALUE.get(typeArg);
    }


    public static final RangeDeserializer PG_DESERIALIZER = RangeDeserializer.builder()
            .dataTypeLabel("PostgreSQL Range")
            .leftBoundaries(new char[]{'[', '('})
            .delim(_Constant.COMMA)
            .rightBoundaries(new char[]{']', ')'})
            .quoteChar(_Constant.DOUBLE_QUOTE)

            .backSlashEscapeOn(true)
            .quoteEscapeOn(true)

            .nullAsNull(false)

            .allowQuote(true)
            .allowNothing(true)
            .allowWhitespace(true)

            .build();


    final Class<?> subJavaType;

    private final MappingType subType;

    final DataType dataType;

    private final Supplier<? extends Comparable<?>> subTypeSupplier;

    /// private constructor
    private GuavaRangeType(Class<?> subJavaType, MappingType subType, DataType dataType,
                           Supplier<? extends Comparable<?>> subTypeSupplier) {
        this.subJavaType = subJavaType;
        this.subType = subType;
        this.dataType = dataType;
        this.subTypeSupplier = subTypeSupplier;
    }

    @Override
    public final Class<?> javaType() {
        return Range.class;
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        if (meta.serverDatabase() != Database.PostgreSQL) {
            throw mapError(this, meta);
        }
        return this.dataType;
    }

    /// @see <a href="https://www.postgresql.org/docs/current/rangetypes.html#RANGETYPES-IO">Range Input/Output</a>
    @Override
    public final String beforeBind(final DataType dataType, MappingEnv env, final Object source) throws CriteriaException {
        _Assert.isTrue(dataType == this.dataType, ""); // assert container match

        return serialize(this, env, source, new StringBuilder(3 + 10 * 2))
                .toString();
    }

    @Override
    public final Range<?> afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {

        final Range<?> value;
        if (source instanceof Range<?> range) {
            if (!isInstance(range)) {
                throw dataAccessError(this, this.dataType, source, null);
            }
            value = range;
        } else if (source instanceof String text) {
            text = text.trim();
            value = deserialize(this, env, text, 0, text.length(), null);
        } else {
            throw dataAccessError(this, this.dataType, source, null);
        }
        return value;
    }

    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        return RangeArrayType.from(ArrayUtils.arrayClassOf(this.subJavaType));
    }

    @Override
    public final Class<?> genericsType() {
        return this.subJavaType;
    }


    public final boolean isInstance(final Range<?> range) {
        final boolean match;
        if (range.hasUpperBound()) {
            match = this.subJavaType.isInstance(range.upperEndpoint());
        } else if (range.hasLowerBound()) {
            match = this.subJavaType.isInstance(range.lowerBoundType());
        } else {
            match = true;
        }
        return match;
    }

    public static StringBuilder serialize(final GuavaRangeType type, MappingEnv env, final Object source, final StringBuilder builder) {
        if (!(source instanceof Range<?> range)) {
            throw paramError(type, type.dataType, source, null);
        }

        if (range.isEmpty() && !range.hasLowerBound() && !range.hasUpperBound()) {
            builder.append("empty");   // Range no bug,never here
            return builder;
        }

        try {

            final LiteralHandler handler = env.literalHandler();

            Object endpoint;

            if (range.hasLowerBound()) {
                switch (range.lowerBoundType()) {
                    case OPEN:
                        builder.append('(');
                        break;
                    case CLOSED:
                        builder.append('[');
                        break;
                    default:
                        throw _Exceptions.unexpectedEnum(range.lowerBoundType());
                }

                endpoint = range.lowerEndpoint();

                if (!type.subJavaType.isInstance(endpoint)) {
                    throw paramError(type, type.dataType, range, null);
                }

                handler.safeLiteral(type.subType, endpoint, false, builder, Objects.requireNonNull(type.dataType));
            } else {
                builder.append('(');
            }

            builder.append(_Constant.COMMA);

            if (range.hasUpperBound()) {
                endpoint = range.upperEndpoint();

                if (!type.subJavaType.isInstance(endpoint)) {
                    throw paramError(type, type.dataType, range, null);
                }

                handler.safeLiteral(type.subType, endpoint, false, builder, Objects.requireNonNull(type.dataType));

                switch (range.upperBoundType()) {
                    case OPEN:
                        builder.append(')');
                        break;
                    case CLOSED:
                        builder.append(']');
                        break;
                    default:
                        throw _Exceptions.unexpectedEnum(range.upperBoundType());
                }
            } else {
                builder.append(')');
            }
            return builder;
        } catch (Exception e) {
            throw paramError(type, type.dataType, source, e);
        }
    }


    public static Range<?> deserialize(final GuavaRangeType type, MappingEnv env, final String source, final int offset,
                                       int endIndex, final @Nullable StringBuilder builder) {

        try {
            final RangeParserFunc func = new RangeParserFunc(type, env, source);
            PG_DESERIALIZER.deserialize(source, offset, endIndex, func, builder);

            final int count = func.consumeCount;
            final Comparable<?> lower = func.lower, upper = func.upper;

            final BoundType lowerBoundType, upperBoundType;
            lowerBoundType = func.lowerOpen ? BoundType.OPEN : BoundType.CLOSED;
            upperBoundType = func.upperOpen ? BoundType.OPEN : BoundType.CLOSED;


            final Range<?> range;
            if (count < 0) {   // server response empty
                final Comparable<?> endpoint = type.subTypeSupplier.get();
                range = Range.closedOpen(endpoint, endpoint);
            } else if (count != 4) {
                throw dataAccessError(type, type.dataType, source, new IllegalStateException("bug"));
            } else if (lower == null && upper == null) {
                if (lowerBoundType != BoundType.OPEN || upperBoundType != BoundType.OPEN) {
                    throw formatError(source);
                }
                range = Range.all();
            } else if (lower != null && upper != null) {
                if (!lower.equals(upper)) {
                    range = Range.range(lower, lowerBoundType, upper, upperBoundType);
                } else if (lowerBoundType == BoundType.OPEN && upperBoundType == BoundType.CLOSED) {
                    range = Range.openClosed(lower, upper);
                } else if (lowerBoundType == BoundType.CLOSED && upperBoundType == BoundType.OPEN) {
                    range = Range.closedOpen(lower, upper);
                } else if (lowerBoundType == BoundType.CLOSED) {
                    range = Range.closed(lower, upper);
                } else {
                    throw formatError(source);
                }
            } else if (lower != null) {
                if (upperBoundType != BoundType.OPEN) {
                    throw formatError(source);
                }
                range = Range.downTo(lower, lowerBoundType);
            } else if (lowerBoundType == BoundType.OPEN) {
                range = Range.upTo(upper, upperBoundType);
            } else {
                throw formatError(source);
            }
            return range;
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw dataAccessError(type, type.dataType, source, e);
        }
    }


    private static BuildInRangeType buildInOf(final Class<?> subJavaType) {
        final PgType dataType;
        final MappingType subType;

        final Supplier<? extends Comparable<?>> subTypeSupplier;

        if (subJavaType == Integer.class) {
            dataType = PgType.INT4RANGE;
            subType = IntegerType.INSTANCE;
            subTypeSupplier = () -> 0;
        } else if (subJavaType == Long.class) {
            dataType = PgType.INT8RANGE;
            subType = LongType.INSTANCE;
            subTypeSupplier = () -> 0L;
        } else if (subJavaType == BigDecimal.class) {
            dataType = PgType.NUMRANGE;
            subType = BigDecimalType.INSTANCE;
            subTypeSupplier = () -> BigDecimal.ZERO;
        } else if (subJavaType == LocalDateTime.class) {
            dataType = PgType.TSRANGE;
            subType = LocalDateTimeType.INSTANCE;
            subTypeSupplier = () -> LocalDateTime.of(LocalDate.EPOCH, LocalTime.MIDNIGHT);
        } else if (subJavaType == OffsetDateTime.class) {
            dataType = PgType.TSTZRANGE;
            subType = OffsetDateTimeType.INSTANCE;
            subTypeSupplier = () -> OffsetDateTime.of(LocalDateTime.of(LocalDate.EPOCH, LocalTime.MIDNIGHT), ZoneOffset.UTC);
        } else if (subJavaType == LocalDate.class) {
            dataType = PgType.DATERANGE;
            subType = LocalDateType.INSTANCE;
            subTypeSupplier = () -> LocalDate.EPOCH;
        } else {
            throw errorJavaType(GuavaRangeType.class, subJavaType);
        }
        return new BuildInRangeType(subJavaType, subType, dataType, subTypeSupplier);
    }


    private static IllegalStateException formatError(String source) {
        String m = String.format("%s unsupported by  %s", source, Range.class.getName());
        return new IllegalStateException(m);
    }


    private static final class RangeParserFunc implements TextFunction<Object> {

        private final GuavaRangeType type;

        private final MappingEnv env;

        private final Object source;

        private int consumeCount = 0;

        private boolean lowerOpen;

        private Comparable<?> lower;

        private boolean upperOpen;

        private Comparable<?> upper;


        private RangeParserFunc(GuavaRangeType type, MappingEnv env, Object source) {
            this.type = type;
            this.env = env;
            this.source = source;
        }

        @Override
        public Object apply(final String text, final int offset, final int end) {
            final int length = text.length();
            final int count = this.consumeCount++;

            final String textValue;
            if (length < 2) {
                textValue = null;
            } else if (offset == 0 && end == offset) {  // text not empty and offset == 0 and offset == end : representing nothing
                textValue = null;
            } else {
                textValue = text.substring(offset, end);
            }

            if (length == 0) {
                if (count != 0) {
                    throw dataAccessError(this.type, this.type.dataType, this.source, new IllegalStateException("bug"));
                }
                this.consumeCount = -8;   // server response empty
            } else if (length == 1) {
                final char ch = text.charAt(offset);
                if (count == 0) {
                    if (ch == '[') {
                        this.lowerOpen = false;
                    } else if (ch == '(') {
                        this.lowerOpen = true;
                    } else {
                        throw dataAccessError(this.type, this.type.dataType, this.source, new IllegalStateException("bug"));
                    }
                } else if (count != 3) {
                    throw dataAccessError(this.type, this.type.dataType, this.source, new IllegalStateException("bug"));
                } else if (ch == ']') {
                    this.upperOpen = false;
                } else if (ch == ')') {
                    this.upperOpen = true;
                } else {
                    throw dataAccessError(this.type, this.type.dataType, this.source, new IllegalStateException("bug"));
                }
            } else {
                if (count != 1 && count != 2) {
                    throw dataAccessError(this.type, this.type.dataType, this.source, new IllegalStateException("bug"));
                }
                final DataType subDataType;
                subDataType = this.type.subType.map(this.env.serverMeta());

                Object value;
                if (textValue == null) {
                    value = null;
                } else {
                    value = this.type.subType.afterGet(subDataType, this.env, textValue);

                    if (value == MappingType.DOCUMENT_NULL_VALUE) {
                        value = null;
                    }
                }


                if (count == 1) {
                    this.lower = (Comparable<?>) value;
                } else {
                    this.upper = (Comparable<?>) value;
                }

            }
            return Boolean.TRUE;
        }


    } // RangeParserFunc


    private static final class BuildInRangeType extends GuavaRangeType {

        private static final ClassValue<BuildInRangeType> CLASS_VALUE = FuncClassValue.create(GuavaRangeType::buildInOf);

        private BuildInRangeType(Class<?> subJavaType, MappingType subType, DataType dataType,
                                 Supplier<? extends Comparable<?>> subTypeSupplier) {
            super(subJavaType, subType, dataType, subTypeSupplier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.subJavaType);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof BuildInRangeType o) {
                match = o.subJavaType == this.subJavaType;
            } else {
                match = false;
            }
            return match;
        }


    } // BuildInRange


}
