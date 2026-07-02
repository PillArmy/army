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
package io.army.mapping.array;

import io.army.mapping.ArrayMappingType;
import io.army.mapping.MappingType;
import io.army.mapping._ArmyBuildInCoreType;
import io.army.util._Collections;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.BitSet;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public abstract class _ArmyCoreArrayType extends _ArmyBuildInCoreType implements ArrayMappingType {

    /// package private constructor
    _ArmyCoreArrayType() {
    }


    /// reflection invoking
    @SuppressWarnings("unused")
    public static Function<Class<?>, MappingType> compositeArrayTypeFunc() {
        return CompositeArrayType::from;
    }

    /// reflection invoking
    @SuppressWarnings("unused")
    public static Function<Class<?>, MappingType> codeEnumArrayTypeFunc() {
        return CodeEnumArrayType::from;
    }

    /// reflection invoking
    @SuppressWarnings("unused")
    public static Function<Class<?>, MappingType> labelEnumArrayTypeFunc() {
        return LabelEnumArrayType::from;
    }

    /// reflection invoking
    @SuppressWarnings("unused")
    public static Function<Class<?>, MappingType> nameEnumArrayTypeFunc() {
        return NameEnumArrayType::from;
    }


    /// reflection invoking
    @SuppressWarnings("unused")
    public static Map<Class<?>, Function<Class<?>, MappingType>> createCoreArrayFuncMap() {
        final Map<Class<?>, Function<Class<?>, MappingType>> map = _Collections.hashMapForSize(9 * 4);

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
