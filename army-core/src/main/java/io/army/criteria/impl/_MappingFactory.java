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

package io.army.criteria.impl;

import io.army.annotation.Generator;
import io.army.annotation.GeneratorType;
import io.army.annotation.Mapping;
import io.army.generator.GeneratorStrategy;
import io.army.lang.Nullable;
import io.army.mapping.*;
import io.army.mapping.array.*;
import io.army.meta.MetaException;
import io.army.modelgen._MetaBridge;
import io.army.struct.CodeEnum;
import io.army.struct.DefinedType;
import io.army.util.ArrayUtils;
import io.army.util.ReflectionUtils;
import io.army.util._StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
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
            } else if (LabelEnumType.class.isAssignableFrom(javaType)) {
                type = LabelEnumType.from(javaType);
            } else {
                type = NameEnumType.from(javaType);
            }
        } else if (!javaType.isArray()) {
            final DefinedType definedType = javaType.getAnnotation(DefinedType.class);
            if (definedType == null) {
                type = DEFAULT_TYPE_MAP.get(javaType);
            } else switch (definedType.category()) {
                case COMPOSITE:
                    type = CompositeType.from(javaType);
                    break;
                case DOMAIN:
                case RANGE:
                default:
                    type = null;
            }

        } else if (!Enum.class.isAssignableFrom(componentClass = ArrayUtils.underlyingComponent(javaType))) {
            final Function<Class<?>, MappingType> func;
            final DefinedType definedType;
            func = DEFAULT_ARRAY_FUNC_MAP.get(componentClass);
            if (func != null) {
                type = func.apply(javaType);
            } else if ((definedType = componentClass.getAnnotation(DefinedType.class)) == null) {
                type = null;
            } else switch (definedType.category()) {
                case COMPOSITE:
                    type = CompositeArrayType.from(javaType);
                    break;
                case DOMAIN:
                case RANGE:
                default:
                    type = null;
            }
        } else if (CodeEnum.class.isAssignableFrom(componentClass)) {
            type = CodeEnumArrayType.from(javaType);
        } else if (LabelEnumType.class.isAssignableFrom(componentClass)) {
            type = LabelEnumArrayType.from(javaType);
        } else {
            type = NameEnumArrayType.from(javaType);
        }
        return type;
    }


    static MappingType map(Class<?> domainClass, Field field, MetaContext context) {
        final Mapping mapping = field.getAnnotation(Mapping.class);
        final Class<?> mappingClass;
        final MappingType type;
        if (mapping == null) {
            type = _MappingFactory.getDefault(field.getType());
        } else if ((mappingClass = mapping.type()) == void.class) {
            type = getMappingTypeFromValue(domainClass, field, mapping, context);
        } else if (MappingType.class.isAssignableFrom(mappingClass)) {
            String m = String.format("Mapping type[%s] of %s.%s isn't sub class of %s .", mappingClass.getName(),
                    domainClass.getName(), field.getName(), MappingType.class.getName());
            throw new MetaException(m);
        } else {
            type = mapType(domainClass, field, context, mappingClass, mapping);
        }
        return type;
    }


    private static MappingType mapType(final Class<?> domainClass, Field field, MetaContext context, final Class<?> mappingClass,
                                       Mapping mapping) {
        final List<Method> factoryMethodList;
        factoryMethodList = context.factoryMethodList(mappingClass);

        final Class<?> fieldJavaType = field.getType();

        final boolean haveConfig, haveTypeArg;
        haveConfig = mapping.elements().length > 0
                || mapping.params().length > 0
                || !mapping.func().isEmpty();

        haveTypeArg = !(field.getGenericType() instanceof Class<?>);


        MappingType type;
        String methodName;
        Class<?> typeClass;
        Class<?>[] typeArray;

        topLoop:
        for (Method factoryMethod : factoryMethodList) {
            methodName = factoryMethod.getName();

            switch (methodName) {
                case "from": {
                    if (haveConfig || haveTypeArg) {
                        continue;
                    }
                    type = oneClassFactoryMethod(domainClass, field, factoryMethod, fieldJavaType);
                }
                break topLoop;
                case "fromList": {
                    if (haveConfig || !haveTypeArg || fieldJavaType != List.class) {
                        continue;
                    }
                    typeClass = ReflectionUtils.tryOneTypeArgument(field);
                    if (typeClass == null) {
                        continue;
                    }
                    type = oneClassFactoryMethod(domainClass, field, factoryMethod, typeClass);
                }
                break topLoop;
                case "fromSet": {
                    if (haveConfig || !haveTypeArg || fieldJavaType != Set.class) {
                        continue;
                    }
                    typeClass = ReflectionUtils.tryOneTypeArgument(field);
                    if (typeClass == null) {
                        continue;
                    }
                    type = oneClassFactoryMethod(domainClass, field, factoryMethod, typeClass);
                }
                break topLoop;
                case "fromEnumSet": {
                    if (haveConfig || !haveTypeArg || fieldJavaType != EnumSet.class) {
                        continue;
                    }
                    typeClass = ReflectionUtils.tryOneTypeArgument(field);
                    if (typeClass == null) {
                        continue;
                    }
                    type = oneClassFactoryMethod(domainClass, field, factoryMethod, typeClass);
                }
                break topLoop;
                case "fromMap": {
                    if (haveConfig || !haveTypeArg || fieldJavaType != Map.class) {
                        continue;
                    }
                    typeArray = ReflectionUtils.tryTwoTypeArgument(field);
                    if (typeArray.length != 2) {
                        continue;
                    }
                    type = classListFactoryMethod(domainClass, field, factoryMethod, typeArray);
                }
                break topLoop;
                case "fromEnumMap": {
                    if (haveConfig || !haveTypeArg || fieldJavaType != EnumMap.class) {
                        continue;
                    }
                    typeArray = ReflectionUtils.tryTwoTypeArgument(field);
                    if (typeArray.length != 2) {
                        continue;
                    }
                    type = classListFactoryMethod(domainClass, field, factoryMethod, typeArray);
                }
                break topLoop;
                case "fromJavaField":
                    break;

            } // switch
        } // factoryMethod loop

        throw new UnsupportedOperationException();
    }


    private static MappingType oneClassFactoryMethod(final Class<?> domainClass, Field field, Method factoryMethod,
                                                     Class<?> javaType) {
        try {
            final MappingType type;
            type = (MappingType) factoryMethod.invoke(null, javaType);
            if (type == null) {
                throw factoryMethodReturnNull(factoryMethod);
            }
            return type;
        } catch (Exception e) {
            throw factoryInvokeError(domainClass, field, factoryMethod, e);
        }
    }

    private static MappingType classListFactoryMethod(final Class<?> domainClass, Field field, Method factoryMethod, Class<?>[] typeArray) {
        try {
            final Object[] valueArray = new Object[typeArray.length];
            System.arraycopy(typeArray, 0, valueArray, 0, valueArray.length);

            final MappingType type;
            type = (MappingType) factoryMethod.invoke(null, valueArray);
            if (type == null) {
                throw factoryMethodReturnNull(factoryMethod);
            }
            return type;
        } catch (Exception e) {
            throw factoryInvokeError(domainClass, field, factoryMethod, e);
        }
    }

    private static MappingType doMap(final Class<?> mappingClass, Field field, Mapping mapping) {
        throw new UnsupportedOperationException();
    }

    private static MetaException factoryInvokeError(final Class<?> domainClass, Field field, Method method, Throwable e) {
        String m = String.format("%s.%s\nfactory method[%s] invocation occur error",
                domainClass.getName(), field.getName(), method);
        return new MetaException(m, e);
    }

    private static MetaException factoryMethodReturnNull(Method method) {
        String m = String.format("factory method[%s] return null.", method);
        return new MetaException(m);
    }


    private static MappingType getMappingTypeFromValue(Class<?> domainClass, Field field, Mapping mapping, MetaContext context) {
        final String value = mapping.value();
        final String finalValue;
        switch (value) {
            case TableMetaUtils.DEFAULT_EXP:
            case TableMetaUtils.RUNTIME_EXP: {
                final String key, configValue;
                key = context.tempBuilderAndClear()
                        .append(domainClass.getName())
                        .append('.')
                        .append(field.getName())
                        .append('.')
                        .append("Mapping")
                        .append('.')
                        .append("value")
                        .toString();

                configValue = context.tableMetaProperties().getProperty(key);
                if (_StringUtils.hasText(configValue)) {
                    finalValue = configValue.trim();
                } else switch (value) {
                    case TableMetaUtils.DEFAULT_EXP:
                        finalValue = null;
                        break;
                    case TableMetaUtils.RUNTIME_EXP:
                        throw new MetaException(String.format("%s no config", key));
                    default:
                        throw new IllegalStateException("bug");
                }
            }
            break;
            case TableMetaUtils.OPTIONAL_EXP: {
                String m = String.format("%s in %s.%s %s.%s is unsupported", value, domainClass.getName(),
                        field.getName(), Mapping.class.getSimpleName(), "value");
                throw new MetaException(m);
            }
            default:
                finalValue = value;
        }

        if (finalValue == null) {
            final MappingType type;
            final Generator generator;
            if (_MetaBridge.ID.equals(field.getName())
                    && (generator = field.getAnnotation(Generator.class)) != null
                    && generator.type() == GeneratorType.DEFAULT
                    && generatorNoConfig(domainClass, field, context)) {
                type = SqlBigIntType.from(field.getType());
            } else {
                type = getDefaultIfMatch(field.getType());
            }
            if (type == null) {
                throw new MetaException(String.format("Not found default mapping type for %s.%s"
                        , domainClass.getName(), field.getName()));
            }
            return type;
        }

        final Class<?> mappingClass;
        try {
            mappingClass = Class.forName(finalValue);
        } catch (ClassNotFoundException e) {
            String m = String.format("Not found %s.%s mapping type %s .",
                    field.getDeclaringClass().getName(), field.getName(), mapping.value());
            throw new MetaException(m);
        }
        return doMap(mappingClass, field, mapping);
    }


    private static boolean generatorNoConfig(Class<?> domainClass, Field field, MetaContext context) {
        final String key, configValue;
        key = context.tempBuilderAndClear()
                .append(domainClass.getName())
                .append('.')
                .append(field.getName())
                .append('.')
                .append(GeneratorStrategy.class.getSimpleName())
                .toString();

        configValue = context.tableMetaProperties().getProperty(key);

        return !_StringUtils.hasText(configValue);
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
