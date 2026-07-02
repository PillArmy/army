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
import io.army.lang.Nullable;
import io.army.meta.ServerMeta;
import io.army.sqltype.*;
import io.army.struct.CodeEnum;
import io.army.struct.DefinedType;
import io.army.struct.LabelEnum;
import io.army.util.*;

import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/// Mapping type for Java {@code enum} constants that use {@link Enum#name()} as database value.
///
/// @see Enum
/// @see LabelEnumType
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

        return new NameEnumType(enumClass, enumName);
    }


    private static final ClassValue<NameEnumType> CLASS_VALUE = FuncClassValue.create(NameEnumType::new);

    private final Class<?> enumClass;

    private final String typeName;

    private DataType dataType;

    /// private constructor
    private NameEnumType(Class<?> javaType) {
        this.enumClass = ClassUtils.enumClass(javaType);
        this.typeName = AnnotationUtils.definedTypeNameOf(this.enumClass);
    }

    /// private constructor
    private NameEnumType(Class<?> javaType, String typeName) {
        this.enumClass = ClassUtils.enumClass(javaType);
        this.typeName = typeName;
    }


    @Override
    public Class<?> javaType() {
        return this.enumClass;
    }

    @Override
    public DataType map(final ServerMeta meta) {
        return mapToDataType(this, meta, this::tryDataType);
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
        final String typeName = this.typeName;
        final MappingType instance;
        if (typeName != null && this.enumClass.getAnnotation(DefinedType.class) == null) {
            instance = ArrayFactoryFuncHolder.PARAM_FUNC.apply(ArrayUtils.arrayClassOf(this.enumClass), typeName);
        } else {
            instance = ArrayFactoryFuncHolder.FUNCTION.apply(ArrayUtils.arrayClassOf(this.enumClass));
        }
        return instance;
    }


    @Override
    public List<String> enumLabelList() {
        return ClassUtils.enumNameListOf(this.enumClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.enumClass, this.typeName);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof NameEnumType o) {
            match = o.enumClass == this.enumClass
                    && Objects.equals(o.typeName, this.typeName);
        } else {
            match = false;
        }
        return match;
    }

    @Nullable
    private DataType tryDataType(ServerMeta meta) {
        DataType dataType = this.dataType;
        if (dataType == null) {
            this.dataType = dataType = tryCreateDataType(this.typeName, this.enumClass, meta);
        }
        return dataType;
    }


    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T valueOf(Class<?> javaType, final String name)
            throws IllegalArgumentException {
        return Enum.valueOf((Class<T>) ClassUtils.enumClass(javaType), name);
    }


    /// for {@link LabelEnumType}
    @Nullable
    static DataType tryCreateDataType(@Nullable String typeName, Class<?> enumClass, ServerMeta meta) {
        if (typeName == null) {
            return null;
        }
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = CustomType.builder()
                        .typeName(typeName)
                        .javaType(enumClass)
                        .componentType(ArmyType.ENUM)
                        .componentCreateDdl(true)
                        .build();
                break;
            case MySQL:
            case SQLite:
            default:
                dataType = null;
        }
        return dataType;
    }


    static DataType mapToDataType(final MappingType type, final ServerMeta meta, Function<ServerMeta, DataType> func) {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case MySQL:
                dataType = MySQLType.ENUM;
                break;
            case PostgreSQL: {
                final DataType temp;
                if ((temp = func.apply(meta)) == null) {
                    dataType = PgType.VARCHAR;
                } else {
                    dataType = temp;
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
        if (LabelEnum.class.isAssignableFrom(javaType)) {
            String m = String.format("enum %s implements %s,please use %s.", javaType.getName(),
                    LabelEnum.class.getName(), LabelEnumType.class.getName());
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

    private static class ArrayFactoryFuncHolder {

        private static final Function<Class<?>, MappingType> FUNCTION;

        private static final BiFunction<Class<?>, String, MappingType> PARAM_FUNC;

        static {
            FUNCTION = removeArrayFromFunc(NameEnumType.class);
            PARAM_FUNC = removeArrayFromParamFunc(NameEnumType.class);
        }

    } // ArrayFactoryFuncHolder


}
