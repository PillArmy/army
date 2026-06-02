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

import io.army.annotation.*;
import io.army.dialect._Constant;
import io.army.generator.FieldGenerator;
import io.army.generator.GeneratorStrategy;
import io.army.generator.PostGeneratorStrategy;
import io.army.lang.Nullable;
import io.army.meta.*;
import io.army.modelgen._MetaBridge;
import io.army.util.ReflectionUtils;
import io.army.util._Assert;
import io.army.util._Exceptions;
import io.army.util._StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/// Utility class for resolving **field-level metadata** such as generators, column attributes,
/// insertability, updatability, and comments.
///
/// <p>Extends `TableMetaUtils` to leverage shared placeholder resolution logic.
/// This class focuses on field-specific concerns including:</p>
/// - **Generator strategy** resolution (`@Generator` annotation + `TableMeta.properties` overrides)
/// - **Column insertability/updatability** rules based on generator type and reserved fields
/// - **Column comment** resolution with support for enum types and managed fields
/// - **Discriminator field** detection for table inheritance hierarchies
///
/// ### Example: Generator resolution via properties
/// ```properties
/// # TableMeta.properties
/// com.example.Stock.id.GeneratorStrategy=io.army.generator.Snowflake8GeneratorStrategy:{"startTime":1779012232202}
/// com.example.Stock.id.Mapping.value=io.army.mapping.SqlBigIntType
/// ```
///
/// @see TableMetaUtils
/// @see io.army.meta.FieldMeta
/// @see io.army.annotation.Generator
abstract class FieldMetaUtils extends TableMetaUtils {

    private FieldMetaUtils() {

    }


    /// Implementation of `GeneratorMeta` for **PRECEDE-type** (pre-insert) field generators.
    ///
    /// <p>Holds the generator Java class, target field metadata, and an immutable parameter map.
    /// Created during field metadata resolution when a `@Generator` annotation specifies
    /// a generator class or when a `GeneratorStrategy` resolves to `GeneratorType.PRECEDE`.</p>
    static final class PreGeneratorMetaImpl implements GeneratorMeta {

        private final FieldMeta<?> fieldMeta;

        private final Class<?> javaType;

        private final Map<String, String> params;


        private PreGeneratorMetaImpl(FieldMeta<?> fieldMeta, Class<?> javaType, Map<String, String> params) {
            this.javaType = javaType;
            this.fieldMeta = fieldMeta;
            this.params = Map.copyOf(params);
        }

        @Override
        public FieldMeta<?> field() {
            return fieldMeta;
        }

        @Override
        public Class<?> javaType() {
            return this.javaType;
        }

        @Override
        public Map<String, String> params() {
            return params;
        }
    }


    /// Validates that a `@Generator` annotation configured for `GeneratorType.POST` mode
    /// satisfies all constraints for post-generation (database auto-increment) fields.
    ///
    /// <p>Validation rules:</p>
    /// - No generator class or params should be specified (POST uses database auto-id)
    /// - Discriminator fields cannot use POST generation
    /// - Only the `id` field supports POST generation
    /// - The field must **not** be insertable
    /// - Java type must be `int`, `long`, `Integer`, `Long`, `String`, or `BigInteger`
    ///
    /// @param field           the field metadata to validate
    /// @param generator       the `@Generator` annotation instance
    /// @param isDiscriminator whether the field is a discriminator column
    /// @throws MetaException if any validation rule is violated
    static void validatePostGenerator(FieldMeta<?> field, Generator generator, boolean isDiscriminator) {
        if (!generator.value().isEmpty() || generator.params().length != 0) {
            String m = String.format("%s config error on %s.", Generator.class.getName(), field);
            throw new MetaException(m);
        }
        if (isDiscriminator) {
            String m = String.format("%s is discriminator,so don't support %s.", field, Generator.class.getName());
            throw new MetaException(m);
        }
        if (!_MetaBridge.ID.equals(field.fieldName())) {
            String m = String.format("%s %s type support only %s field."
                    , Generator.class.getName(), GeneratorType.POST, _MetaBridge.ID);
            throw new MetaException(m);
        }
        if (field.insertable()) {
            String m = String.format("%s insertable error.", field);
            throw new MetaException(m);
        }
        final Class<?> javaType = field.javaType();
        if (javaType != Integer.class
                && javaType != int.class
                && javaType != Long.class
                && javaType != long.class
                && javaType != String.class
                && javaType != BigInteger.class) {
            throw _Exceptions.autoIdErrorJavaType((PrimaryFieldMeta<?>) field);
        }
    }

    /// Resolves the **generator metadata** for a column-level `@Generator` annotation.
    ///
    /// <p>This method loads the generator class, builds the parameter map from `@Param` annotations,
    /// and applies any `@OverrideParams` declared at the table level.</p>
    ///
    /// <p>Reserved Army-managed fields (e.g., `createTime`, `updateTime`) and discriminator
    /// fields are not allowed to have generators.</p>
    ///
    /// @param generator       the `@Generator` annotation on the field
    /// @param field           the Java reflection `Field` object
    /// @param fieldMeta       the resolved field metadata
    /// @param isDiscriminator whether the field is a discriminator column
    /// @param context         the metadata resolution context
    /// @return a `GeneratorMeta` instance for PRECEDE-type generators
    /// @throws MetaException if the field is reserved or the generator class cannot be loaded
    static GeneratorMeta columnGeneratorMeta(Generator generator, Field field, FieldMeta<?> fieldMeta,
                                             boolean isDiscriminator, MetaContext context) {
        final String fieldName = fieldMeta.fieldName();
        if (isDiscriminator || (!_MetaBridge.ID.equals(fieldName) && _MetaBridge.RESERVED_FIELDS.contains(fieldName))) {
            String m = String.format("%s is managed by army ,so must no %s", fieldMeta, Generator.class.getName());
            throw new MetaException(m);
        }

        final Class<?> generatorClass;
        generatorClass = loadPreGeneratorClass(fieldMeta, generator.value());

        final Map<String, String> paramMap, finalMap;
        paramMap = getOrcreateFieldParmMap(generator.params(), fieldMeta, field, context);
        finalMap = overrideParamMapIfNeed(generatorClass, fieldMeta, paramMap);
        return new PreGeneratorMetaImpl(fieldMeta, generatorClass, finalMap);
    }

    /// Loads a `GeneratorStrategy` instance from `TableMeta.properties` for a given field.
    ///
    /// <p>The property key follows the pattern:
    /// `{className}.{fieldName}.GeneratorStrategy`.</p>
    ///
    /// <p>The property value format is: `fully.qualified.StrategyClass[:jsonParams]`.</p>
    ///
    /// ### Example
    /// ```properties
    /// io.army.example.stock.domain.Stock.id.GeneratorStrategy=\
    ///   io.army.generator.Snowflake8GeneratorStrategy:{"startTime":1779012232202}
    /// ```
    ///
    /// @param fieldMeta   the resolved field metadata
    /// @param generatorType the expected generator type (`PRECEDE`, `POST`, or `RUNTIME`)
    /// @param context     the metadata resolution context
    /// @return the loaded `GeneratorStrategy` instance
    /// @throws MetaException if the property is missing for `RUNTIME` type or loading fails
    static GeneratorStrategy loadGeneratorStrategy(final FieldMeta<?> fieldMeta, final GeneratorType generatorType,
                                                   final MetaContext context) {
        final String key, fieldName;
        fieldName = fieldMeta.fieldName();
        key = context.tempBuilderAndClear()
                .append(fieldMeta.tableMeta().javaType().getName())
                .append('.')
                .append(fieldMeta.fieldName())
                .append('.')
                .append(GeneratorStrategy.class.getSimpleName())
                .toString();

        String value;
        value = context.tableMetaProperties().getProperty(key);
        if (!_StringUtils.hasText(value)) {
            if (generatorType == GeneratorType.RUNTIME || !fieldName.equals(_MetaBridge.ID)) {
                String m = String.format("%s no %s", fieldMeta, GeneratorStrategy.class.getName());
                throw new MetaException(m);
            }
            return PostGeneratorStrategy.create();
        }
        value = value.trim();
        final int colonIndex = value.indexOf(':');
        final String className, paramStr;
        if (colonIndex < 0) {
            className = value;
            paramStr = null;
        } else {
            className = value.substring(0, colonIndex);
            paramStr = value.substring(colonIndex + 1).trim();
        }


        final GeneratorStrategy strategy;
        try {
            final Method method;
            if (paramStr == null) {
                method = ReflectionUtils.getStaticFactoryMethod(className, GeneratorStrategy.class, "create");
            } else {
                method = ReflectionUtils.getStaticFactoryMethod(className, GeneratorStrategy.class, "create", String.class);
            }
            if (paramStr == null) {
                strategy = (GeneratorStrategy) ReflectionUtils.invokeStaticFactoryMethod(method);
            } else {
                strategy = (GeneratorStrategy) ReflectionUtils.invokeStaticFactoryMethod(method, paramStr);
            }
        } catch (Exception e) {
            throw new MetaException(e.getMessage(), e);
        }
        return strategy;
    }

    /// Creates a `GeneratorMeta` from a resolved `GeneratorStrategy`.
    ///
    /// <p>Returns `null` for `POST`-type generators (database auto-increment),
    /// since they require no application-side generator configuration.
    /// For `PRECEDE`-type generators, returns a `PreGeneratorMetaImpl`.</p>
    ///
    /// @param strategy       the resolved generator strategy
    /// @param fieldMeta      the field metadata
    /// @param isDiscriminator whether the field is a discriminator column
    /// @param context        the metadata resolution context
    /// @return a `GeneratorMeta` for PRECEDE generators, or `null` for POST generators
    /// @throws MetaException if the field is reserved or managed by Army
    @Nullable
    static GeneratorMeta createGeneratorMeta(GeneratorStrategy strategy, FieldMeta<?> fieldMeta,
                                             boolean isDiscriminator, MetaContext context) {
        final String fieldName = fieldMeta.fieldName();
        if (isDiscriminator || (!_MetaBridge.ID.equals(fieldName) && _MetaBridge.RESERVED_FIELDS.contains(fieldName))) {
            String m = String.format("%s is managed by army ,so must no %s", fieldMeta, Generator.class.getName());
            throw new MetaException(m);
        }

        final GeneratorType type = strategy.type();
        if (type == GeneratorType.POST) {
            return null;
        }
        _Assert.isTrue(type == GeneratorType.PRECEDE, "");
        return new PreGeneratorMetaImpl(fieldMeta, strategy.generatorClass(), strategy.paramMap());
    }


    /// Determines whether a given field is a **discriminator column** for table inheritance.
    ///
    /// <p>A field is a discriminator if the table class has an `@Inheritance` annotation
    /// whose value matches the given field name.</p>
    ///
    /// @param tableJavaType the domain class to check for `@Inheritance`
    /// @param fieldName     the field name to test
    /// @return `true` if the field is the discriminator column
    static boolean isDiscriminator(final Class<?> tableJavaType, final String fieldName) {
        final Inheritance inheritance = tableJavaType.getAnnotation(Inheritance.class);
        return inheritance != null && fieldName.equals(inheritance.value());
    }

    /// Determines whether a column is **insertable** based on its generator type and role.
    ///
    /// <p>Insertability rules:</p>
    /// - **No generator**: insertable if it's a discriminator, reserved field, or `@Column(insertable=true)`
    /// - **PRECEDE generator**: always insertable (application generates the value)
    /// - **POST generator**: insertable only for child tables (inheritance)
    ///
    /// @param field         the field metadata
    /// @param generatorType the generator type, or `null` if no generator
    /// @param column        the `@Column` annotation instance
    /// @param isDiscriminator whether the field is a discriminator column
    /// @return `true` if the column should be included in INSERT statements
    static boolean columnInsertable(FieldMeta<?> field, @Nullable GeneratorType generatorType,
                                    final Column column, final boolean isDiscriminator) {
        final boolean insertable;
        if (generatorType == null) {
            insertable = isDiscriminator
                    || _MetaBridge.RESERVED_FIELDS.contains(field.fieldName())
                    || column.insertable();
        } else switch (generatorType) {
            case PRECEDE:
                insertable = true;
                break;
            case POST:
                // child insertable
                insertable = field.tableMeta() instanceof ChildTableMeta;
                break;
            default:
                throw _Exceptions.unexpectedEnum(generatorType);
        }
        return insertable;
    }

    /// Determines whether a column is **updatable** based on its role and table immutability.
    ///
    /// <p>Update rules:</p>
    /// - **Never updatable**: discriminator, `id`, `createTime`, or fields on immutable tables
    /// - **Always updatable**: `updateTime`, `version`, `visible`
    /// - **Otherwise**: follows `@Column(updatable=...)` annotation value
    ///
    /// @param field         the field metadata
    /// @param column        the `@Column` annotation instance
    /// @param isDiscriminator whether the field is a discriminator column
    /// @return `true` if the column should be included in UPDATE statements
    static boolean columnUpdatable(FieldMeta<?> field, final Column column, boolean isDiscriminator) {
        final String fieldName = field.fieldName();
        final boolean able;
        if (isDiscriminator
                || field.tableMeta().immutable()
                || _MetaBridge.ID.equals(fieldName)
                || _MetaBridge.CREATE_TIME.equals(fieldName)) {
            able = false;
        } else if (_MetaBridge.UPDATE_TIME.equals(fieldName)
                || _MetaBridge.VERSION.equals(fieldName)
                || _MetaBridge.VISIBLE.equals(fieldName)) {
            able = true;
        } else {
            able = column.updatable();
        }
        return able;
    }


    /// Resolves the **column comment** from the `@Column.comment()` attribute and
    /// optional `TableMeta.properties` overrides.
    ///
    /// <p>Comment resolution supports:</p>
    /// - `${DEFAULT}`: auto-generate from enum type or camelCase-to-spaced conversion
    /// - `${RUNTIME}`: must be provided in properties (key: `{className}.{fieldName}.Column.comment`)
    /// - Reserved Army fields: automatic comments like `"primary key"`, `"create time"`, etc.
    /// - Discriminator fields: auto-generated `@see` comment pointing to the enum class
    ///
    /// ### Example
    /// ```java
    /// @Column(comment = "${DEFAULT}")
    /// public StockStatus status;
    /// // Resolved to: "enum, @see io.army.example.stock.domain.StockStatus"
    /// ```
    ///
    /// @param column        the `@Column` annotation instance
    /// @param fieldMeta     the resolved field metadata
    /// @param isDiscriminator whether the field is a discriminator column
    /// @param context       the metadata resolution context
    /// @return the resolved comment string
    /// @throws MetaException if the comment is required but cannot be resolved
    static String columnComment(final Column column, FieldMeta<?> fieldMeta, final boolean isDiscriminator,
                                MetaContext context) {
        final String value = column.comment(), finalValue;
        switch (value) {
            case DEFAULT_EXP:
            case RUNTIME_EXP: {
                final String key, configValue;
                key = context.tempBuilderAndClear()
                        .append(fieldMeta.tableMeta().javaType().getName())
                        .append('.')
                        .append(fieldMeta.fieldName())
                        .append('.')
                        .append("Column")
                        .append('.')
                        .append("comment")
                        .toString();

                configValue = context.tableMetaProperties().getProperty(key);
                if (_StringUtils.hasText(configValue)) {
                    finalValue = configValue.trim();
                } else switch (value) {
                    case DEFAULT_EXP: {
                        final Class<?> javaType = fieldMeta.javaType();
                        if (Enum.class.isAssignableFrom(javaType)) {
                            finalValue = context.tempBuilderAndClear()
                                    .append("enum")
                                    .append(_Constant.COMMA)
                                    .append("@see")
                                    .append(_Constant.SPACE)
                                    .append(javaType.getName())
                                    .toString();
                        } else {
                            finalValue = _MetaBridge.camelToComment(fieldMeta.fieldName(), context.tempBuilderAndClear());
                        }
                    }
                    break;
                    case RUNTIME_EXP:
                        throw new MetaException(String.format("%s no config", key));
                    default:
                        throw new IllegalStateException("bug");
                }
            }
            break;
            case OPTIONAL_EXP: {
                String m = String.format("%s in %s.%s %s.%s is unsupported", value, fieldMeta.tableMeta().javaType().getName(),
                        fieldMeta.fieldName(), "Column", "comment");
                throw new MetaException(m);
            }
            default: {
                if (_StringUtils.hasText(value)) {
                    finalValue = value;
                } else if (_MetaBridge.RESERVED_FIELDS.contains(fieldMeta.fieldName()) || isDiscriminator) {
                    finalValue = commentManagedByArmy(fieldMeta);
                } else {
                    String m = String.format("Domain[%s] column[%s] isn't reserved properties or discriminator, so must have common"
                            , fieldMeta.tableMeta().javaType().getName()
                            , fieldMeta.columnName());
                    throw new MetaException(m);
                }
            } // default
        }
        return finalValue;
    }



    /*################################## blow private method ##################################*/


    /// @return an unmodified map
    private static Map<String, String> getOrcreateFieldParmMap(final Param[] params, final FieldMeta<?> fieldMeta,
                                                               final Field field, final MetaContext context) {
        if (params.length == 0) {
            return Map.of();
        }

        final String key;
        key = context.tempBuilderAndClear()
                .append(field.getDeclaringClass().getName())
                .append('.')
                .append(field.getName())
                .append('.')
                .append("generator")
                .append('.')
                .append("paramMap")
                .toString();

        Map<String, String> paramMap;
        paramMap = context.getGeneratorParamMap(key);
        if (paramMap == null) {
            paramMap = createFieldParmMap(params, fieldMeta);
            context.putGeneratorParamMap(key, paramMap);
        }
        return paramMap;
    }

    /// @return an unmodified map
    private static Map<String, String> createFieldParmMap(final Param[] params, final FieldMeta<?> fieldMeta) {

        final Map<String, String> paramMap = new HashMap<>((int) (params.length / 0.75f));
        for (Param param : params) {
            if (paramMap.putIfAbsent(param.name(), param.value()) != null) {
                String m = String.format("%s %s[%s] duplication", fieldMeta, Param.class.getName(), param.name());
                throw new MetaException(m);
            }
        }
        return Map.copyOf(paramMap);
    }

    /// @param paramMap an unmodified map
    /// @return an unmodified map
    private static Map<String, String> overrideParamMapIfNeed(final Class<?> generatorClass, final FieldMeta<?> fieldMeta, final Map<String, String> paramMap) {
        final OverrideParams overrideParams;
        overrideParams = fieldMeta.tableMeta().javaType().getAnnotation(OverrideParams.class);
        if (overrideParams == null) {
            return paramMap;
        }
        final Set<String> set = obtainParamNameSet(generatorClass);
        final Map<String, String> newMap = new HashMap<>(paramMap);
        for (FieldParam fieldParam : overrideParams.fields()) {
            if (!fieldParam.name().equals(fieldMeta.fieldName())) {
                continue;
            }
            for (Param param : fieldParam.params()) {
                if (set.contains(param.name())) {
                    newMap.put(param.name(), param.value());
                }
            }
        }

        return Map.copyOf(newMap);
    }


    @SuppressWarnings("unchecked")
    private static Set<String> obtainParamNameSet(Class<?> generatorClass) {
        try {
            final Method method;
            method = generatorClass.getMethod("paramNameSet");
            final int modifiers = method.getModifiers();
            if (!Modifier.isPublic(modifiers)
                    || !Modifier.isStatic(modifiers)
                    || method.getReturnType() != Set.class) {
                String m = String.format("Not found paramNameSet() method in %s", generatorClass.getName());
                throw new RuntimeException(m);
            }
            final Object value;
            value = method.invoke(null);
            if (value == null || ((Set<?>) value).isEmpty()) {
                throw new RuntimeException("set is null/empty");
            }
            return (Set<String>) value;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    private static String commentManagedByArmy(FieldMeta<?> fieldMeta) {
        String comment = "";
        switch (fieldMeta.fieldName()) {
            case _MetaBridge.ID:
                comment = "primary key";
                break;
            case _MetaBridge.CREATE_TIME:
                comment = "create time";
                break;
            case _MetaBridge.UPDATE_TIME:
                comment = "update time";
                break;
            case _MetaBridge.VERSION:
                comment = "version that's update counter of row";
                break;
            case _MetaBridge.VISIBLE:
                comment = "visible for soft delete";
                break;
            default:
                if (fieldMeta.javaType().isEnum()) {
                    comment = "@see " + fieldMeta.javaType().getName();
                }
        }
        return comment;
    }


    private static Class<?> loadPreGeneratorClass(FieldMeta<?> fieldMeta, final String className) {
        if (!_StringUtils.hasText(className)) {
            String m = String.format("%s generator no class name", fieldMeta);
            throw new MetaException(m);
        }
        try {
            final Class<?> clazz;
            clazz = Class.forName(className);
            if (!FieldGenerator.class.isAssignableFrom(clazz)) {
                String m = String.format("%s generator[%s] isn't %s type."
                        , fieldMeta, className, FieldGenerator.class.getName());
                throw new MetaException(m);
            }
            return clazz;
        } catch (ClassNotFoundException e) {
            String m = String.format("%s generator[%s] not found.", fieldMeta, className);
            throw new MetaException(m, e);
        }

    }


    /*################################## blow static inner class ##################################*/


}
