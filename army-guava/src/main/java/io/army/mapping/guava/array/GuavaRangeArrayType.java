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
package io.army.mapping.guava.array;

import com.google.common.collect.Range;
import io.army.criteria.CriteriaException;
import io.army.dialect.UnsupportedDialectException;
import io.army.executor.DataAccessException;
import io.army.function.TextFunction;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping.UnaryGenericsMapping;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.mapping.array.PostgreArrays;
import io.army.mapping.guava.GuavaRangeType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;
import io.army.util._Assert;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.BiConsumer;

/// @see GuavaRangeType
public abstract class GuavaRangeArrayType extends _ArmyBuildInArrayType implements UnaryGenericsMapping {

    public static GuavaRangeArrayType fromTypeArg(final Class<?> javaType, final Class<?> typeArg) {
        if (!javaType.isArray()) {
            throw errorJavaType(GuavaRangeArrayType.class, javaType);
        } else if (ArrayUtils.underlyingComponent(javaType) != Range.class) {
            throw errorJavaType(GuavaRangeArrayType.class, javaType);
        }

        final GuavaRangeArrayType instance;
        if (typeArg == Integer.class) {
            instance = BuildInIntegerRangeArrayType.CLASS_VALUE.get(javaType);
        } else if (typeArg == Long.class) {
            instance = BuildInLongRangeArrayType.CLASS_VALUE.get(javaType);
        } else if (typeArg == BigDecimal.class) {
            instance = BuildInBigDecimalRangeArrayType.CLASS_VALUE.get(javaType);
        } else if (typeArg == java.time.LocalDateTime.class) {
            instance = BuildInLocalDateTimeRangeArrayType.CLASS_VALUE.get(javaType);
        } else if (typeArg == java.time.LocalDate.class) {
            instance = BuildInLocalDateRangeArrayType.CLASS_VALUE.get(javaType);
        } else if (typeArg == java.time.OffsetDateTime.class) {
            instance = BuildInOffsetDateTimeRangeArrayType.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(GuavaRangeArrayType.class, javaType);
        }
        return instance;
    }


    final Class<?> javaType;

    private final GuavaRangeType underlyingType;

    /// private constructor
    public GuavaRangeArrayType(Class<?> javaType, GuavaRangeType underlyingType) {
        this.javaType = javaType;
        this.underlyingType = underlyingType;
    }


    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL: {
                final DataType subDataType;
                subDataType = this.underlyingType.map(meta);
                if (!(subDataType instanceof PgType)) {
                    throw mapError(this, meta);
                } else switch ((PgType) subDataType) {
                    case INT4RANGE:
                        dataType = PgType.INT4RANGE_ARRAY;
                        break;
                    case INT8RANGE:
                        dataType = PgType.INT8RANGE_ARRAY;
                        break;
                    case NUMRANGE:
                        dataType = PgType.NUMRANGE_ARRAY;
                        break;
                    case DATERANGE:
                        dataType = PgType.DATERANGE_ARRAY;
                        break;
                    case TSRANGE:
                        dataType = PgType.TSRANGE_ARRAY;
                        break;
                    case TSTZRANGE:
                        dataType = PgType.TSTZRANGE_ARRAY;
                        break;
                    default:
                        throw mapError(this, meta);
                } // inner switch

            }
            break;
            case SQLite:
            case MySQL:
            default:
                throw mapError(this, meta);
        }
        return dataType;
    }

    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {

        final StringBuilder tempBuilder = new StringBuilder(30);

        final BiConsumer<Object, StringBuilder> consumer;
        consumer = (element, sqlBuilder) -> {
            tempBuilder.setLength(0); // firstly clear

            GuavaRangeType.serialize(this.underlyingType, env, element, tempBuilder);
            PostgreArrays.encodeElement(tempBuilder, sqlBuilder);
        };
        return PostgreArrays.arrayBeforeBind(source, consumer, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        final StringBuilder tempBuilder = new StringBuilder(30);

        final TextFunction<?> func;
        func = (text, offset, end) -> GuavaRangeType.deserialize(this.underlyingType, env, text, offset, end, tempBuilder);
        return PostgreArrays.arrayAfterGet(this, dataType, source, func, tempBuilder);
    }

    @Override
    public final Class<?> underlyingJavaType() {
        return this.underlyingType.javaType();
    }

    @Override
    public final Class<?> genericsType() {
        return this.underlyingType.genericsType();
    }

    @Override
    public final MappingType underlyingType() {
        return this.underlyingType;
    }

    @Override
    public final MappingType elementType() {
        final Class<?> componentType;
        final MappingType instance;
        if ((componentType = this.javaType.getComponentType()).isArray()) {
            instance = fromTypeArg(componentType, genericsType());
        } else {
            instance = this.underlyingType;
        }
        return instance;
    }


    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        return fromTypeArg(ArrayUtils.arrayClassOf(this.javaType), genericsType());
    }


    private static final class BuildInIntegerRangeArrayType extends GuavaRangeArrayType {

        private static BuildInIntegerRangeArrayType fromInt(final Class<?> javaType) {
            final GuavaRangeType underlyingType;
            underlyingType = GuavaRangeType.fromTypeArg(Range.class, Integer.class);
            return new BuildInIntegerRangeArrayType(javaType, underlyingType);
        }


        private static final ClassValue<BuildInIntegerRangeArrayType> CLASS_VALUE = FuncClassValue.create(BuildInIntegerRangeArrayType::fromInt);


        private BuildInIntegerRangeArrayType(Class<?> javaType, GuavaRangeType underlyingType) {
            super(javaType, underlyingType);
            _Assert.isTrue(underlyingType.genericsType() == Integer.class, "bug");
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.javaType);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof BuildInIntegerRangeArrayType o) {
                match = o.javaType == this.javaType;
            } else {
                match = false;
            }
            return match;
        }


    } // BuildInIntegerRangeArrayType


    private static final class BuildInLongRangeArrayType extends GuavaRangeArrayType {

        private static BuildInLongRangeArrayType fromLong(final Class<?> javaType) {
            final GuavaRangeType underlyingType;
            underlyingType = GuavaRangeType.fromTypeArg(Range.class, Long.class);
            return new BuildInLongRangeArrayType(javaType, underlyingType);
        }


        private static final ClassValue<BuildInLongRangeArrayType> CLASS_VALUE = FuncClassValue.create(BuildInLongRangeArrayType::fromLong);


        private BuildInLongRangeArrayType(Class<?> javaType, GuavaRangeType underlyingType) {
            super(javaType, underlyingType);
            _Assert.isTrue(underlyingType.genericsType() == Long.class, "bug");
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.javaType);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof BuildInLongRangeArrayType o) {
                match = o.javaType == this.javaType;
            } else {
                match = false;
            }
            return match;
        }


    } // BuildInLongRangeArrayType


    private static final class BuildInBigDecimalRangeArrayType extends GuavaRangeArrayType {

        private static BuildInBigDecimalRangeArrayType fromBigDecimal(final Class<?> javaType) {
            final GuavaRangeType underlyingType;
            underlyingType = GuavaRangeType.fromTypeArg(Range.class, BigDecimal.class);
            return new BuildInBigDecimalRangeArrayType(javaType, underlyingType);
        }


        private static final ClassValue<BuildInBigDecimalRangeArrayType> CLASS_VALUE = FuncClassValue.create(BuildInBigDecimalRangeArrayType::fromBigDecimal);


        private BuildInBigDecimalRangeArrayType(Class<?> javaType, GuavaRangeType underlyingType) {
            super(javaType, underlyingType);
            _Assert.isTrue(underlyingType.genericsType() == BigDecimal.class, "bug");
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.javaType);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof BuildInBigDecimalRangeArrayType o) {
                match = o.javaType == this.javaType;
            } else {
                match = false;
            }
            return match;
        }


    } // BuildInBigDecimalRangeArrayType

    private static final class BuildInLocalDateTimeRangeArrayType extends GuavaRangeArrayType {

        private static BuildInLocalDateTimeRangeArrayType fromLocalDateTime(final Class<?> javaType) {
            final GuavaRangeType underlyingType;
            underlyingType = GuavaRangeType.fromTypeArg(Range.class, java.time.LocalDateTime.class);
            return new BuildInLocalDateTimeRangeArrayType(javaType, underlyingType);
        }


        private static final ClassValue<BuildInLocalDateTimeRangeArrayType> CLASS_VALUE = FuncClassValue.create(BuildInLocalDateTimeRangeArrayType::fromLocalDateTime);


        private BuildInLocalDateTimeRangeArrayType(Class<?> javaType, GuavaRangeType underlyingType) {
            super(javaType, underlyingType);
            _Assert.isTrue(underlyingType.genericsType() == java.time.LocalDateTime.class, "bug");
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.javaType);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof BuildInLocalDateTimeRangeArrayType o) {
                match = o.javaType == this.javaType;
            } else {
                match = false;
            }
            return match;
        }


    } // BuildInLocalDateTimeRangeArrayType


    private static final class BuildInLocalDateRangeArrayType extends GuavaRangeArrayType {

        private static BuildInLocalDateRangeArrayType fromLocalDate(final Class<?> javaType) {
            final GuavaRangeType underlyingType;
            underlyingType = GuavaRangeType.fromTypeArg(Range.class, java.time.LocalDate.class);
            return new BuildInLocalDateRangeArrayType(javaType, underlyingType);
        }


        private static final ClassValue<BuildInLocalDateRangeArrayType> CLASS_VALUE = FuncClassValue.create(BuildInLocalDateRangeArrayType::fromLocalDate);


        private BuildInLocalDateRangeArrayType(Class<?> javaType, GuavaRangeType underlyingType) {
            super(javaType, underlyingType);
            _Assert.isTrue(underlyingType.genericsType() == java.time.LocalDate.class, "bug");
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.javaType);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof BuildInLocalDateRangeArrayType o) {
                match = o.javaType == this.javaType;
            } else {
                match = false;
            }
            return match;
        }


    } // BuildInLocalDateRangeArrayType

    private static final class BuildInOffsetDateTimeRangeArrayType extends GuavaRangeArrayType {

        private static BuildInOffsetDateTimeRangeArrayType fromOffsetDateTime(final Class<?> javaType) {
            final GuavaRangeType underlyingType;
            underlyingType = GuavaRangeType.fromTypeArg(Range.class, java.time.OffsetDateTime.class);
            return new BuildInOffsetDateTimeRangeArrayType(javaType, underlyingType);
        }


        private static final ClassValue<BuildInOffsetDateTimeRangeArrayType> CLASS_VALUE = FuncClassValue.create(BuildInOffsetDateTimeRangeArrayType::fromOffsetDateTime);


        private BuildInOffsetDateTimeRangeArrayType(Class<?> javaType, GuavaRangeType underlyingType) {
            super(javaType, underlyingType);
            _Assert.isTrue(underlyingType.genericsType() == java.time.OffsetDateTime.class, "bug");
        }


        @Override
        public int hashCode() {
            return Objects.hash(this.javaType);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof BuildInOffsetDateTimeRangeArrayType o) {
                match = o.javaType == this.javaType;
            } else {
                match = false;
            }
            return match;
        }


    } // BuildInOffsetDateTimeRangeArrayType


}
