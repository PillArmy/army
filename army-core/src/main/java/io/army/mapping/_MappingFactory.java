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

import io.army.annotation.Mapping;
import io.army.lang.Nullable;
import io.army.mapping.array.*;
import io.army.meta.MetaException;
import io.army.struct.CodeEnum;
import io.army.util.ArrayUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.time.*;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public abstract class _MappingFactory {

    private static final Map<Class<?>, MappingType> DEFAULT_TYPE_MAP;

    private static final Map<Class<?>, Function<Class<?>, MappingType>> DEFAULT_ARRAY_FUNC_MAP;

    static {
        DEFAULT_TYPE_MAP = Map.copyOf(createDefaultMappingMap());
        DEFAULT_ARRAY_FUNC_MAP = Map.copyOf(createDefaultArrayFuncMap());
    }

    private _MappingFactory() {
        throw new UnsupportedOperationException();
    }


    public static MappingType getDefault(Class<?> javaType) throws MetaException {
        final MappingType type;
        type = getDefaultIfMatch(javaType);
        if (type == null) {
            String m = String.format("Not found default mapping for %s .", javaType.getName());
            throw new MetaException(m);
        }
        return type;
    }

    @Nullable
    public static MappingType getDefaultIfMatch(final Class<?> javaType) {
        final MappingType type;

        final Class<?> componentClass;

        if (javaType == byte[].class) {
            type = VarBinaryType.INSTANCE;
        } else if (Enum.class.isAssignableFrom(javaType)) {
            if (CodeEnum.class.isAssignableFrom(javaType)) {
                type = CodeEnumType.from(javaType);
            } else if (TextEnumType.class.isAssignableFrom(javaType)) {
                type = TextEnumType.from(javaType);
            } else if (Month.class.isAssignableFrom(javaType)) {
                type = MonthType.DEFAULT;
            } else if (DayOfWeek.class.isAssignableFrom(javaType)) {
                type = DayOfWeekType.DEFAULT;
            } else {
                type = NameEnumType.from(javaType);
            }
        } else if (!javaType.isArray()) {
            type = DEFAULT_TYPE_MAP.get(javaType);
        } else if (!Enum.class.isAssignableFrom(componentClass = ArrayUtils.underlyingComponent(javaType))) {
            final Function<Class<?>, MappingType> func;
            func = DEFAULT_ARRAY_FUNC_MAP.get(componentClass);
            if (func == null) {
                type = null;
            } else {
                type = func.apply(javaType);
            }
        } else if (CodeEnum.class.isAssignableFrom(componentClass)) {
            type = CodeEnumArrayType.from(javaType);
        } else if (TextEnumType.class.isAssignableFrom(componentClass)) {
            type = TextEnumArrayType.from(javaType);
        } else if (Month.class.isAssignableFrom(componentClass)) {
            type = MonthArrayType.from(javaType);
        } else if (DayOfWeek.class.isAssignableFrom(componentClass)) {
            type = DayOfWeekArrayType.from(javaType);
        } else {
            type = NameEnumArrayType.from(javaType);
        }
        return type;
    }

    public static MappingType map(final Field field) {
        final Mapping mapping = field.getAnnotation(Mapping.class);
        final MappingType type;
        if (mapping == null) {
            type = _MappingFactory.getDefault(field.getType());
        } else {
            type = _MappingFactory.map(mapping, field);
        }
        return type;
    }

    private static MappingType map(final Mapping mapping, final Field field) {
        final Class<?> mappingClass;
        mappingClass = getMappingClass(mapping, field);

        final boolean textMapping, elementMapping;
        textMapping = TextMappingType.class.isAssignableFrom(mappingClass);
        elementMapping = MultiGenericsMappingType.class.isAssignableFrom(mappingClass);

        try {
            final Method method;
            final Object mappingType;
            if (textMapping && elementMapping) {
                method = mappingClass.getDeclaredMethod("forMixture", Class.class, Class[].class, Charset.class);
                assertFactoryMethod(method);
                final Charset charset = Charset.forName(mapping.charset());
                mappingType = method.invoke(null, field.getType(), mapping.elements(), charset);
            } else if (textMapping) {
                method = mappingClass.getDeclaredMethod("forText", Class.class, Charset.class);
                assertFactoryMethod(method);
                mappingType = method.invoke(null, field.getType(), Charset.forName(mapping.charset()));
            } else if (elementMapping) {
                method = mappingClass.getDeclaredMethod("forElements", Class.class, Class[].class);
                assertFactoryMethod(method);
                mappingType = method.invoke(null, field.getType(), mapping.elements());
            } else {
                method = mappingClass.getDeclaredMethod("from", Class.class);
                assertFactoryMethod(method);
                mappingType = method.invoke(null, field.getType());
            }
            if (mappingType == null) {
                String m = String.format("%s %s factory method return null.", mappingClass.getName(), method.getName());
                throw new MetaException(m);
            }
            return (MappingType) mappingType;
        } catch (NoSuchMethodException | IllegalAccessException e) {
            String m = String.format("%s factory method definition error for %s.%s"
                    , mappingClass.getName(), field.getDeclaringClass().getName(), field.getName());
            throw new MetaException(m, e);
        } catch (InvocationTargetException e) {
            String m = String.format("Factory method of %s invocation occur error for %s.%s"
                    , mappingClass.getName(), field.getDeclaringClass().getName(), field.getName());
            throw new MetaException(m, e);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            String m = String.format("%s.%s %s.charset() error."
                    , field.getDeclaringClass().getName(), field.getName(), Mapping.class.getName());
            throw new MetaException(m, e);
        }

    }


    private static Class<?> getMappingClass(final Mapping mapping, final Field field) {
        Class<?> mappingClass;
        mappingClass = mapping.type();
        if (mappingClass == void.class) {
            try {
                mappingClass = Class.forName(mapping.value());
            } catch (ClassNotFoundException e) {
                String m = String.format("Not found %s.%s mapping type %s .",
                        field.getDeclaringClass().getName(), field.getName(), mapping.value());
                throw new MetaException(m);
            }
        }

        if (!MappingType.class.isAssignableFrom(mappingClass)) {
            String m = String.format("Mapping type[%s] of %s.%s isn't sub class of %s .", mappingClass.getName(),
                    field.getDeclaringClass().getName(), field.getName(), MappingType.class.getName());
            throw new MetaException(m);
        }
        return mappingClass;
    }


    private static void assertFactoryMethod(final Method method) {
        final int modifiers;
        modifiers = method.getModifiers();
        if (!(Modifier.isPublic(modifiers)
                && Modifier.isStatic(modifiers)
                && method.getDeclaringClass().isAssignableFrom(method.getReturnType()))) {
            String m = String.format("Not found %s method (static factory method) in %s ."
                    , method.getName(), method.getDeclaringClass().getName());
            throw new MetaException(m);
        }

    }

    private static Map<Class<?>, MappingType> createDefaultMappingMap() {
        final Map<Class<?>, MappingType> map = new HashMap<>();

        // map.put( byte[].class,VarBinaryType.INSTANCE);
        map.put(String.class, StringType.INSTANCE);

        map.put(boolean.class, BooleanType.INSTANCE);
        map.put(Boolean.class, BooleanType.INSTANCE);
        map.put(int.class, IntegerType.INSTANCE);
        map.put(Integer.class, IntegerType.INSTANCE);

        map.put(long.class, LongType.INSTANCE);
        map.put(Long.class, LongType.INSTANCE);
        map.put(float.class, FloatType.INSTANCE);
        map.put(Float.class, FloatType.INSTANCE);

        map.put(double.class, DoubleType.INSTANCE);
        map.put(Double.class, DoubleType.INSTANCE);
        map.put(short.class, ShortType.INSTANCE);
        map.put(Short.class, ShortType.INSTANCE);

        map.put(byte.class, ByteType.INSTANCE);
        map.put(Byte.class, ByteType.INSTANCE);
        map.put(char.class, SqlCharType.INSTANCE);
        map.put(Character.class, SqlCharType.INSTANCE);

        map.put(BigInteger.class, BigIntegerType.INSTANCE);
        map.put(BigDecimal.class, BigDecimalType.INSTANCE);
        map.put(LocalDateTime.class, LocalDateTimeType.INSTANCE);
        map.put(OffsetDateTime.class, OffsetDateTimeType.INSTANCE);

        map.put(ZonedDateTime.class, ZonedDateTimeType.INSTANCE);
        map.put(LocalDate.class, LocalDateType.INSTANCE);
        map.put(LocalTime.class, LocalTimeType.INSTANCE);
        map.put(OffsetTime.class, OffsetTimeType.INSTANCE);

        map.put(Instant.class, InstantType.INSTANCE);
        map.put(Year.class, YearType.INSTANCE);
        map.put(YearMonth.class, YearMonthType.INSTANCE);
        map.put(MonthDay.class, MonthDayType.INSTANCE);

        map.put(ZoneId.class, ZoneIdType.INSTANCE);
        map.put(BitSet.class, BitSetType.INSTANCE);
        map.put(UUID.class, UUIDType.INSTANCE);

        return map;
    }

    private static Map<Class<?>, Function<Class<?>, MappingType>> createDefaultArrayFuncMap() {
        final Map<Class<?>, Function<Class<?>, MappingType>> map = new HashMap<>();

        map.put(String.class, StringArrayType::from);

        map.put(boolean.class, BooleanArrayType::from);
        map.put(Boolean.class, BooleanArrayType::from);
        map.put(int.class, IntegerArrayType::from);
        map.put(Integer.class, IntegerArrayType::from);

        map.put(long.class, LongArrayType::from);
        map.put(Long.class, LongArrayType::from);
        map.put(float.class, FloatArrayType::from);
        map.put(Float.class, FloatArrayType::from);

        map.put(double.class, DoubleArrayType::from);
        map.put(Double.class, DoubleArrayType::from);
        map.put(short.class, ShortArrayType::from);
        map.put(Short.class, ShortArrayType::from);

        map.put(byte.class, ByteArrayType::from);
        map.put(Byte.class, ByteArrayType::from);
        map.put(char.class, SqlCharArrayType::from);
        map.put(Character.class, SqlCharArrayType::from);

        map.put(BigInteger.class, BigIntegerArrayType::from);
        map.put(BigDecimal.class, BigDecimalArrayType::from);
        map.put(LocalDateTime.class, LocalDateTimeArrayType::from);
        map.put(OffsetDateTime.class, OffsetDateTimeArrayType::from);

        map.put(ZonedDateTime.class, ZonedDateTimeArrayType::from);
        map.put(LocalDate.class, LocalDateArrayType::from);
        map.put(LocalTime.class, LocalTimeArrayType::from);
        map.put(OffsetTime.class, OffsetTimeArrayType::from);

        map.put(Instant.class, InstantArrayType::from);
        map.put(Year.class, YearArrayType::from);
        map.put(YearMonth.class, YearMonthArrayType::from);
        map.put(MonthDay.class, MonthDayArrayType::from);

        map.put(ZoneId.class, ZoneIdArrayType::from);
        map.put(BitSet.class, BitSetArrayType::from);
        map.put(UUID.class, UUIDArrayType::from);

        return map;
    }


}
