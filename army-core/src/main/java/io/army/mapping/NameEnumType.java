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
import io.army.util.*;

import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/// Mapping type for Java {@code enum} constants that use {@link Enum#name()} as database value.
///
/// @see Enum
/// @see TextEnumType
public final class NameEnumType extends _ArmyNoInjectionType implements MappingType.SqlEnum {

    public static NameEnumType from(final Class<?> javaType) {
        return CLASS_VALUE.get(checkEnumClass(javaType));
    }

    /// @param javaType The enum that is not annotated with {@link DefinedType}.
    public static NameEnumType fromParam(final Class<?> javaType, final String enumName) {
        final Class<?> enumClass;
        enumClass = checkEnumClass(javaType);

        if (enumClass.getAnnotation(DefinedType.class) != null) {
            throw errorJavaType(NameEnumType.class, enumClass);
        }
        _Assert.assertTypeName(enumName);
        return new NameEnumType(enumClass, DataType.from(enumName));
    }


    private static final ClassValue<NameEnumType> CLASS_VALUE = FuncClassValue.create(NameEnumType::new);

    private final Class<?> enumClass;

    private final DataType dataType;

    /// private constructor
    private NameEnumType(Class<?> javaType) {
        this.enumClass = ClassUtils.enumClass(javaType);
        this.dataType = AnnotationUtils.dataTypeOf(javaType, false);
    }

    /// private constructor
    private NameEnumType(Class<?> javaType, DataType dataType) {
        this.enumClass = ClassUtils.enumClass(javaType);
        this.dataType = dataType;
    }


    @Override
    public Class<?> javaType() {
        return this.enumClass;
    }

    @Override
    public DataType map(final ServerMeta meta) {
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
    public MappingType arrayTypeOfThis() throws CriteriaException {
        final DataType dataType = this.dataType;
        final MappingType instance;
        if (dataType != null && this.enumClass.getAnnotation(DefinedType.class) == null) {
            instance = NameEnumArrayType.fromParam(ArrayUtils.arrayClassOf(this.enumClass), dataType.typeName());
        } else {
            instance = NameEnumArrayType.from(ArrayUtils.arrayClassOf(this.enumClass));
        }
        return instance;
    }

    @Override
    public DataType dataType() {
        return this.dataType;
    }

    @Override
    public List<String> enumLabelList() {
        return ClassUtils.enumNameListOf(this.enumClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.enumClass, this.dataType);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof NameEnumType o) {
            match = o.enumClass == this.enumClass
                    && Objects.equals(o.dataType, this.dataType);
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
                final DataType temp;
                if (type instanceof SqlEnum o && (temp = o.dataType()) != null) {
                    dataType = temp;
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
                throw mapError(type, meta);

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


}
