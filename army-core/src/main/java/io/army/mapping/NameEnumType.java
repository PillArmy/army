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

package io.army.mapping;

import io.army.criteria.CriteriaException;
import io.army.mapping.array.NameEnumArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.*;
import io.army.struct.CodeEnum;
import io.army.struct.DefinedType;
import io.army.struct.TextEnum;
import io.army.util.AnnotationUtils;
import io.army.util.ArrayUtils;
import io.army.util.ClassUtils;
import io.army.util._StringUtils;

import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @see Enum
 * @see TextEnumType
 */
public class NameEnumType extends _ArmyNoInjectionType {

    public static NameEnumType from(final Class<?> enumType) {
        final Class<?> actualEnumType;
        actualEnumType = checkEnumClass(enumType);

        final NameEnumType instance;
        if (actualEnumType.getAnnotation(DefinedType.class) == null) {
            instance = INSTANCE_MAP.computeIfAbsent(actualEnumType, NameEnumType::new);
        } else {
            instance = INSTANCE_MAP.computeIfAbsent(actualEnumType, NameEnumType::createDefinedType);
        }
        return instance;
    }

    /// @param enumType The enum that is not annotated with {@link DefinedType}.
    public static NameEnumType fromParam(final Class<?> enumType, final String enumName) {
        if (!_StringUtils.hasText(enumName)) {
            throw new IllegalArgumentException("no text");
        }
        final Class<?> actualEnumType;
        actualEnumType = checkEnumClass(enumType);
        if (actualEnumType.getAnnotation(DefinedType.class) != null) {
            throw new IllegalArgumentException("error enum");
        }

        NameEnumType instance;
        instance = INSTANCE_MAP.computeIfAbsent(actualEnumType, type -> new NameEnumNamedType(type, enumName));
        if (!(instance instanceof NameEnumNamedType o) || !enumName.equals(o.enumName)) {
            // same class but different enum name
            instance = new NameEnumNamedType(actualEnumType, enumName);
        }
        return instance;
    }

    /// @param enumType The enum that is annotated with {@link DefinedType}.
    private static NameEnumNamedType createDefinedType(final Class<?> enumType) {
        final String typeName;
        typeName = AnnotationUtils.getDefinedTypeName(enumType);
        if (typeName == null) {
            // no bug,never here
            throw new IllegalArgumentException();
        }
        return new NameEnumNamedType(enumType, typeName);
    }


    private static final ConcurrentMap<Class<?>, NameEnumType> INSTANCE_MAP = new ConcurrentHashMap<>();

    private final Class<?> enumClass;

    /**
     * private constructor
     */
    private NameEnumType(Class<?> enumClass) {
        this.enumClass = enumClass;
    }


    @Override
    public final Class<?> javaType() {
        return this.enumClass;
    }

    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        final MappingType arrayType;
        if (this.enumClass.getAnnotation(DefinedType.class) == null && this instanceof NameEnumNamedType o) {
            arrayType = NameEnumArrayType.fromParam(ArrayUtils.arrayClassOf(this.enumClass), o.enumName);
        } else {
            arrayType = NameEnumArrayType.from(ArrayUtils.arrayClassOf(this.enumClass));
        }
        return arrayType;
    }


    @Override
    public final DataType map(final ServerMeta meta) {
        return mapToDataType(this, meta);
    }


    @Override
    public String beforeBind(DataType dataType, MappingEnv env, final Object source) {
        if (!this.enumClass.isInstance(source)) {
            throw paramError(this, dataType, source, null);
        }
        return ((Enum<?>) source).name();
    }

    @Override
    public Enum<?> afterGet(DataType dataType, MappingEnv env, final Object source) {
        if (this.enumClass.isInstance(source)) {
            return (Enum<?>) source;
        }

        final Enum<?> value;
        if (this.enumClass == Month.class) {
            value = toMonth(this, dataType, source);
        } else if (this.enumClass == DayOfWeek.class) {
            value = toDayOfWeek(this, dataType, source);
        } else if (source instanceof String) {
            try {
                value = valueOf(this.enumClass, (String) source);
            } catch (IllegalArgumentException e) {
                throw dataAccessError(this, dataType, source, e);
            }
        } else {
            throw dataAccessError(this, dataType, source, null);
        }
        return value;
    }

    @Override
    public final int hashCode() {
        final int hash;
        if (this instanceof NameEnumNamedType o) {
            hash = Objects.hash(this.enumClass, o.enumName);
        } else {
            hash = Objects.hash(this.enumClass);
        }
        return hash;
    }

    @Override
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof NameEnumNamedType o) {
            if (this instanceof NameEnumNamedType t) {
                match = ((NameEnumType) o).enumClass == this.enumClass
                        && o.enumName.equals(t.enumName);
            } else {
                match = false;
            }
        } else if (obj instanceof NameEnumType o) {
            match = o.enumClass == this.enumClass;
        } else {
            match = false;
        }
        return match;
    }


    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T valueOf(Class<?> javaType, final String name)
            throws IllegalArgumentException {
        return Enum.valueOf((Class<T>) ClassUtils.enumClass(javaType), name);
    }


    static DataType mapToDataType(final MappingType type, final ServerMeta meta) {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case MySQL:
                dataType = MySQLType.ENUM;
                break;
            case PostgreSQL: {
                if (type instanceof SqlUserDefined o) {
                    dataType = DataType.from(o.typeName());
                } else {
                    dataType = PgType.VARCHAR;
                }
            }
            break;
            case SQLite:
                dataType = SQLiteType.VARCHAR;
                break;
            case H2:
                dataType = H2Type.ENUM;
                break;
            default:
                throw MAP_ERROR_HANDLER.apply(type, meta);

        }
        return dataType;
    }


    private static Class<?> checkEnumClass(final Class<?> javaType) {
        if (!Enum.class.isAssignableFrom(javaType)) {
            throw errorJavaType(NameEnumType.class, javaType);
        }
        if (CodeEnum.class.isAssignableFrom(javaType)) {
            String m = String.format("enum %s implements %s,please use %s.", javaType.getName(),
                    CodeEnum.class.getName(), CodeEnumType.class.getName());
            throw new IllegalArgumentException(m);
        }
        if (TextEnum.class.isAssignableFrom(javaType)) {
            String m = String.format("enum %s implements %s,please use %s.", javaType.getName(),
                    TextEnum.class.getName(), TextEnumType.class.getName());
            throw new IllegalArgumentException(m);
        }
        return ClassUtils.enumClass(javaType);
    }


    private static Month toMonth(final MappingType type, final DataType dataType, final Object source) {
        final Month value;

        final String sourceStr;
        final int length;

        if (source instanceof Month) {
            value = (Month) source;
        } else if (source instanceof LocalDate
                || source instanceof YearMonth
                || source instanceof MonthDay
                || source instanceof LocalDateTime
                || source instanceof OffsetDateTime
                || source instanceof ZonedDateTime) {
            value = Month.from((TemporalAccessor) source);
        } else if (source instanceof Integer) { // https://dev.mysql.com/doc/refman/8.0/en/date-and-time-functions.html#function_month
            final int v = (Integer) source;
            if (v < 1 || v > 12) {
                throw dataAccessError(type, dataType, source, null);
            }
            value = Month.of(v);
        } else if (source instanceof Long) {  // https://dev.mysql.com/doc/refman/8.0/en/date-and-time-functions.html#function_month
            final long v = (Long) source;
            if (v < 1 || v > 12) {
                throw dataAccessError(type, dataType, source, null);
            }
            value = Month.of((int) v);
        } else if (!(source instanceof String) || (length = (sourceStr = (String) source).length()) == 0) {
            throw dataAccessError(type, dataType, source, null);
        } else if (length < 10) {
            try {
                // https://dev.mysql.com/doc/refman/8.0/en/date-and-time-functions.html#function_monthname
                value = Month.valueOf(sourceStr.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw dataAccessError(type, dataType, source, e);
            }
        } else {
            throw dataAccessError(type, dataType, source, null);
        }
        return value;
    }


    private static DayOfWeek toDayOfWeek(final MappingType type, final DataType dataType, final Object source) {
        final DayOfWeek value;
        final String sourceStr;
        final int length;

        if (source instanceof DayOfWeek) {
            value = (DayOfWeek) source;
        } else if (source instanceof LocalDate
                || source instanceof LocalDateTime
                || source instanceof OffsetDateTime
                || source instanceof ZonedDateTime) {
            value = DayOfWeek.from((TemporalAccessor) source);
        } else if (source instanceof Integer) {
            value = weekFromInt(type, dataType, (Integer) source);
        } else if (source instanceof Long) {
            final long v = (Long) source;
            if (v < 1 || v > 7) {
                throw dataAccessError(type, dataType, source, null);
            }
            value = weekFromInt(type, dataType, (int) v);
        } else if (!(source instanceof String) || (length = (sourceStr = (String) source).length()) == 0) {
            throw dataAccessError(type, dataType, source, null);
        } else if (length < 10) {
            try {
                // https://dev.mysql.com/doc/refman/8.0/en/date-and-time-functions.html#function_dayname
                value = DayOfWeek.valueOf(sourceStr.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw dataAccessError(type, dataType, source, e);
            }
        } else {
            throw dataAccessError(type, dataType, source, null);
        }
        return value;
    }


    private static DayOfWeek weekFromInt(final MappingType type, final DataType dataType, final int source) {
        final DayOfWeek value;
        if (dataType == MySQLType.INT || dataType == MySQLType.BIGINT) {
            // https://dev.mysql.com/doc/refman/8.0/en/date-and-time-functions.html#function_dayofweek
            if (source == 1) {
                value = DayOfWeek.SUNDAY;
            } else {
                value = DayOfWeek.of(source - 1);
            }
        } else {
            throw dataAccessError(type, dataType, source, null);
        }
        return value;
    }


    private static final class NameEnumNamedType extends NameEnumType implements SqlUserDefined {

        private final String enumName;

        private NameEnumNamedType(Class<?> enumClass, String enumName) {
            super(enumClass);
            this.enumName = enumName;
        }

        @Override
        public String typeName() {
            return this.enumName;
        }

    } // NameEnumNamedType


}
