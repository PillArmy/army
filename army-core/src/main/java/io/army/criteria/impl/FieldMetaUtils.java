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
import io.army.generator.FieldGenerator;
import io.army.lang.Nullable;
import io.army.mapping.MappingType;
import io.army.mapping._MappingFactory;
import io.army.meta.*;
import io.army.modelgen._MetaBridge;
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

/**
 * @see FieldMeta
 * @see TableFieldMeta
 */
abstract class FieldMetaUtils extends TableMetaUtils {

    private FieldMetaUtils() {

    }


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

    static Column columnMeta(final Class<?> domainClass, final Field field) throws MetaException {
        final Column column = field.getAnnotation(Column.class);
        if (column == null) {
            String m = String.format("Field[%s.%s] isn't annotated by %s."
                    , domainClass.getName(), field.getName(), Column.class.getName());
            throw new MetaException(m);
        }
        return column;
    }

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
                && javaType != BigInteger.class) {
            throw _Exceptions.autoIdErrorJavaType((PrimaryFieldMeta<?>) field);
        }
    }

    static GeneratorMeta columnGeneratorMeta(Generator generator, Field field, FieldMeta<?> fieldMeta, boolean isDiscriminator) {
        final String fieldName = fieldMeta.fieldName();
        if (isDiscriminator || (!_MetaBridge.ID.equals(fieldName) && _MetaBridge.RESERVED_FIELDS.contains(fieldName))) {
            String m = String.format("%s is managed by army ,so must no %s", fieldMeta, Generator.class.getName());
            throw new MetaException(m);
        }
        final Class<?> generatorClass;
        generatorClass = loadPreGeneratorClass(fieldMeta, generator.value());

        final Map<String, String> paramMap, finalMap;
        paramMap = getOrcreateFieldParmMap(generator.params(), fieldMeta, field);
        finalMap = overrideParamMapIfNeed(generatorClass, fieldMeta, paramMap);
        return new PreGeneratorMetaImpl(fieldMeta, generatorClass, finalMap);
    }


    static MappingType fieldMappingType(final Field field, final boolean isDiscriminator) {

        final Class<?> fieldJavaType = field.getType();

        if (isDiscriminator && !Enum.class.isAssignableFrom(fieldJavaType)) {
            String m = String.format("%s.%s is discriminator,but isn't enum.", field.getDeclaringClass().getName(), field.getName());
            throw new MetaException(m);
        }
        return _MappingFactory.map(field);

    }


    static boolean isDiscriminator(final Class<?> tableJavaType, final String fieldName) {
        final Inheritance inheritance = tableJavaType.getAnnotation(Inheritance.class);
        return inheritance != null && fieldName.equals(inheritance.value());
    }

    static boolean columnInsertable(FieldMeta<?> field, @Nullable Generator generator,
                                    final Column column, final boolean isDiscriminator) {
        final boolean insertable;
        if (generator == null) {
            insertable = isDiscriminator
                    || _MetaBridge.RESERVED_FIELDS.contains(field.fieldName())
                    || column.insertable();
        } else {
            switch (generator.type()) {
                case PRECEDE:
                    insertable = true;
                    break;
                case POST:
                    // child insertable
                    insertable = field.tableMeta() instanceof ChildTableMeta;
                    break;
                default:
                    throw _Exceptions.unexpectedEnum(generator.type());
            }

        }
        return insertable;
    }

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


    static String columnComment(final Column column, FieldMeta<?> fieldMeta, final boolean isDiscriminator) {
        String comment = column.comment();
        if (_MetaBridge.RESERVED_FIELDS.contains(fieldMeta.fieldName()) || isDiscriminator) {
            if (!_StringUtils.hasText(comment)) {
                comment = commentManagedByArmy(fieldMeta);
            }
        } else if (!_StringUtils.hasText(comment)) {
            String m = String.format("Domain[%s] column[%s] isn't reserved properties or discriminator, so must have common"
                    , fieldMeta.tableMeta().javaType().getName()
                    , fieldMeta.columnName());
            throw new MetaException(m);
        }
        return comment;
    }



    /*################################## blow private method ##################################*/


    /// @return an unmodified map
    @SuppressWarnings("unchecked")
    private static Map<String, String> getOrcreateFieldParmMap(final Param[] params, final FieldMeta<?> fieldMeta, final Field field) {
        if (params.length == 0) {
            return Map.of();
        }

        synchronized (DefaultTableMeta.LOCK) {

            final String key = field.getDeclaringClass().getName() + '.' + field.getName() + '.' + "generator.paramMap";
            final Object value = TableMetaUtils.getCache(key);
            Map<String, String> paramMap;
            if (value instanceof Map) {
                paramMap = (Map<String, String>) value;
            } else {
                paramMap = createFieldParmMap(params, fieldMeta);
                TableMetaUtils.putCache(key, paramMap);
            }
            return paramMap;
        } // synchronized
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


    /**
     * @see #columnGeneratorMeta(Generator, FieldMeta, boolean)
     */
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
