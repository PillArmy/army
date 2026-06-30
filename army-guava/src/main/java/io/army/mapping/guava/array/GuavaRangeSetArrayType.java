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

import com.google.common.collect.RangeSet;
import io.army.criteria.CriteriaException;
import io.army.dialect.UnsupportedDialectException;
import io.army.executor.DataAccessException;
import io.army.function.TextFunction;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping.UnaryGenericsMapping;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.mapping.array.PostgreArrays;
import io.army.mapping.guava.GuavaRangeSetType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;
import io.army.util._Assert;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.BiConsumer;

public abstract class GuavaRangeSetArrayType extends _ArmyBuildInArrayType implements UnaryGenericsMapping {

    public static GuavaRangeSetArrayType fromTypeArg(final Class<?> javaType, final Class<?> typeArg) {
        if (!javaType.isArray()) {
            throw errorJavaType(GuavaRangeSetArrayType.class, javaType);
        } else if (ArrayUtils.underlyingComponent(javaType) != RangeSet.class) {
            throw errorJavaType(GuavaRangeSetArrayType.class, javaType);
        }

        final GuavaRangeSetArrayType instance;
        if (typeArg == Integer.class) {
            instance = BuildInIntegerRangeSetArrayType.CLASS_VALUE.get(javaType);
        } else if (typeArg == Long.class) {
            instance = BuildInLongRangeSetArrayType.CLASS_VALUE.get(javaType);
        } else if (typeArg == BigDecimal.class) {
            instance = BuildInBigDecimalRangeSetArrayType.CLASS_VALUE.get(javaType);
        } else if (typeArg == java.time.LocalDateTime.class) {
            instance = BuildInLocalDateTimeRangeSetArrayType.CLASS_VALUE.get(javaType);
        } else if (typeArg == java.time.LocalDate.class) {
            instance = BuildInLocalDateRangeSetArrayType.CLASS_VALUE.get(javaType);
        } else if (typeArg == java.time.OffsetDateTime.class) {
            instance = BuildInOffsetDateTimeRangeSetArrayType.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(GuavaRangeSetArrayType.class, javaType);
        }
        return instance;
    }

    final Class<?> javaType;

    private final GuavaRangeSetType underlyingType;

    /// private constructor
    private GuavaRangeSetArrayType(Class<?> javaType, GuavaRangeSetType underlyingType) {
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
                    case INT4MULTIRANGE:
                        dataType = PgType.INT4MULTIRANGE_ARRAY;
                        break;
                    case INT8MULTIRANGE:
                        dataType = PgType.INT8MULTIRANGE_ARRAY;
                        break;
                    case NUMMULTIRANGE:
                        dataType = PgType.NUMMULTIRANGE_ARRAY;
                        break;
                    case DATEMULTIRANGE:
                        dataType = PgType.DATEMULTIRANGE_ARRAY;
                        break;
                    case TSMULTIRANGE:
                        dataType = PgType.TSMULTIRANGE_ARRAY;
                        break;
                    case TSTZMULTIRANGE:
                        dataType = PgType.TSTZMULTIRANGE_ARRAY;
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

            GuavaRangeSetType.serialize(this.underlyingType, env, element, tempBuilder);
            PostgreArrays.encodeElement(tempBuilder, sqlBuilder);
        };
        return PostgreArrays.arrayBeforeBind(source, consumer, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        final StringBuilder tempBuilder = new StringBuilder(30);

        final TextFunction<?> func;
        func = (text, offset, end) -> GuavaRangeSetType.deserialize(this.underlyingType, env, text, offset, end, tempBuilder);
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


    private static final class BuildInIntegerRangeSetArrayType extends GuavaRangeSetArrayType {

        private static BuildInIntegerRangeSetArrayType fromInt(final Class<?> javaType) {
            final GuavaRangeSetType underlyingType;
            underlyingType = GuavaRangeSetType.fromTypeArg(RangeSet.class, Integer.class);
            return new BuildInIntegerRangeSetArrayType(javaType, underlyingType);
        }


        private static final ClassValue<BuildInIntegerRangeSetArrayType> CLASS_VALUE = FuncClassValue.create(BuildInIntegerRangeSetArrayType::fromInt);


        private BuildInIntegerRangeSetArrayType(Class<?> javaType, GuavaRangeSetType underlyingType) {
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
            } else if (obj instanceof BuildInIntegerRangeSetArrayType o) {
                match = o.javaType == this.javaType;
            } else {
                match = false;
            }
            return match;
        }


    } // BuildInIntegerRangeArrayType


    private static final class BuildInLongRangeSetArrayType extends GuavaRangeSetArrayType {

        private static BuildInLongRangeSetArrayType fromLong(final Class<?> javaType) {
            final GuavaRangeSetType underlyingType;
            underlyingType = GuavaRangeSetType.fromTypeArg(RangeSet.class, Long.class);
            return new BuildInLongRangeSetArrayType(javaType, underlyingType);
        }


        private static final ClassValue<BuildInLongRangeSetArrayType> CLASS_VALUE = FuncClassValue.create(BuildInLongRangeSetArrayType::fromLong);


        private BuildInLongRangeSetArrayType(Class<?> javaType, GuavaRangeSetType underlyingType) {
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
            } else if (obj instanceof BuildInLongRangeSetArrayType o) {
                match = o.javaType == this.javaType;
            } else {
                match = false;
            }
            return match;
        }


    } // BuildInLongRangeArrayType


    private static final class BuildInBigDecimalRangeSetArrayType extends GuavaRangeSetArrayType {

        private static BuildInBigDecimalRangeSetArrayType fromBigDecimal(final Class<?> javaType) {
            final GuavaRangeSetType underlyingType;
            underlyingType = GuavaRangeSetType.fromTypeArg(RangeSet.class, BigDecimal.class);
            return new BuildInBigDecimalRangeSetArrayType(javaType, underlyingType);
        }


        private static final ClassValue<BuildInBigDecimalRangeSetArrayType> CLASS_VALUE = FuncClassValue.create(BuildInBigDecimalRangeSetArrayType::fromBigDecimal);


        private BuildInBigDecimalRangeSetArrayType(Class<?> javaType, GuavaRangeSetType underlyingType) {
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
            } else if (obj instanceof BuildInBigDecimalRangeSetArrayType o) {
                match = o.javaType == this.javaType;
            } else {
                match = false;
            }
            return match;
        }


    } // BuildInBigDecimalRangeArrayType

    private static final class BuildInLocalDateTimeRangeSetArrayType extends GuavaRangeSetArrayType {

        private static BuildInLocalDateTimeRangeSetArrayType fromLocalDateTime(final Class<?> javaType) {
            final GuavaRangeSetType underlyingType;
            underlyingType = GuavaRangeSetType.fromTypeArg(RangeSet.class, java.time.LocalDateTime.class);
            return new BuildInLocalDateTimeRangeSetArrayType(javaType, underlyingType);
        }


        private static final ClassValue<BuildInLocalDateTimeRangeSetArrayType> CLASS_VALUE = FuncClassValue.create(BuildInLocalDateTimeRangeSetArrayType::fromLocalDateTime);


        private BuildInLocalDateTimeRangeSetArrayType(Class<?> javaType, GuavaRangeSetType underlyingType) {
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
            } else if (obj instanceof BuildInLocalDateTimeRangeSetArrayType o) {
                match = o.javaType == this.javaType;
            } else {
                match = false;
            }
            return match;
        }


    } // BuildInLocalDateTimeRangeArrayType


    private static final class BuildInLocalDateRangeSetArrayType extends GuavaRangeSetArrayType {

        private static BuildInLocalDateRangeSetArrayType fromLocalDate(final Class<?> javaType) {
            final GuavaRangeSetType underlyingType;
            underlyingType = GuavaRangeSetType.fromTypeArg(RangeSet.class, java.time.LocalDate.class);
            return new BuildInLocalDateRangeSetArrayType(javaType, underlyingType);
        }


        private static final ClassValue<BuildInLocalDateRangeSetArrayType> CLASS_VALUE = FuncClassValue.create(BuildInLocalDateRangeSetArrayType::fromLocalDate);


        private BuildInLocalDateRangeSetArrayType(Class<?> javaType, GuavaRangeSetType underlyingType) {
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
            } else if (obj instanceof BuildInLocalDateRangeSetArrayType o) {
                match = o.javaType == this.javaType;
            } else {
                match = false;
            }
            return match;
        }


    } // BuildInLocalDateRangeArrayType

    private static final class BuildInOffsetDateTimeRangeSetArrayType extends GuavaRangeSetArrayType {

        private static BuildInOffsetDateTimeRangeSetArrayType fromOffsetDateTime(final Class<?> javaType) {
            final GuavaRangeSetType underlyingType;
            underlyingType = GuavaRangeSetType.fromTypeArg(RangeSet.class, java.time.OffsetDateTime.class);
            return new BuildInOffsetDateTimeRangeSetArrayType(javaType, underlyingType);
        }


        private static final ClassValue<BuildInOffsetDateTimeRangeSetArrayType> CLASS_VALUE = FuncClassValue.create(BuildInOffsetDateTimeRangeSetArrayType::fromOffsetDateTime);


        private BuildInOffsetDateTimeRangeSetArrayType(Class<?> javaType, GuavaRangeSetType underlyingType) {
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
            } else if (obj instanceof BuildInOffsetDateTimeRangeSetArrayType o) {
                match = o.javaType == this.javaType;
            } else {
                match = false;
            }
            return match;
        }


    } // BuildInOffsetDateTimeRangeArrayType


}
