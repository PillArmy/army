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

import io.army.annotation.Column;
import io.army.annotation.MappedSuperclass;
import io.army.criteria.CriteriaException;
import io.army.criteria.impl.TableMetaUtils;
import io.army.dialect.UnsupportedDialectException;
import io.army.dialect._Constant;
import io.army.executor.DataAccessException;
import io.army.function.DecodeLiteralFunc;
import io.army.function.SafeLiteralFunc;
import io.army.mapping.optional.CompositeField;
import io.army.meta.MetaException;
import io.army.meta.ServerMeta;
import io.army.meta.TypeMeta;
import io.army.pojo.ObjectAccessor;
import io.army.pojo.ObjectAccessorFactory;
import io.army.sqltype.DataType;
import io.army.struct.DefinedType;
import io.army.util.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

/// Mapping the pojo annotated by {@link DefinedType} to sql composite type
///
/// @see <a href="https://www.postgresql.org/docs/current/rowtypes.html">Composite Types</a>
public final class CompositeType extends _ArmyBuildInType implements MappingType.SqlComposite {

    public static CompositeType from(final Class<?> javaType) {
        final DefinedType definedType = javaType.getAnnotation(DefinedType.class);
        if (definedType == null || definedType.fieldOrder().length == 0) {
            throw errorJavaType(CompositeType.class, javaType);
        }
        return CLASS_VALUE.get(javaType);
    }


    private static final ClassValue<CompositeType> CLASS_VALUE = new ClassValue<>() {
        @Override
        protected CompositeType computeValue(Class<?> type) {
            return createCompositeType(type);
        }
    };


    private final Class<?> javaType;

    private final String name;

    private final List<CompositeField> fieldList;


    private CompositeType(Class<?> javaType, String name, List<CompositeField> fieldList) {
        this.javaType = javaType;
        this.name = name;
        this.fieldList = List.copyOf(fieldList);
    }

    @Override
    public Class<?> javaType() {
        return this.javaType;
    }



    @Override
    public DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = DataType.from(this.name);
                break;
            case SQLite:
            case MySQL:
            default:
                throw mapError(this, meta);
        }
        return dataType;
    }


    @Override
    public String beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        final List<CompositeField> fieldList = this.fieldList;
        final int size = fieldList.size();
        final StringBuilder sqlBuilder = new StringBuilder(2 + size + (size * 10));
        bindToLiteral(this, dataType, env, source, sqlBuilder);
        return sqlBuilder.toString();
    }


    @Override
    public Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        if (this.javaType.isInstance(source)) {
            return source;
        }
        if (!(source instanceof String)) {
            throw dataAccessError(this, dataType, source, null);
        }
        final String text = ((String) source).trim();
        return parseToPojo(this, dataType, env, text, 0, text.length());
    }

    @Override
    public List<CompositeField> fieldList() {
        return this.fieldList;
    }

    @Override
    public String objectName() {
        return this.name;
    }

    @Override
    public String comment() {
        return "";
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
        } else if (obj instanceof CompositeType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }


    public static void bindToLiteral(final CompositeType instance, final DataType dataType, final MappingEnv env,
                                     final Object source, final StringBuilder sqlBuilder) {
        if (!instance.javaType.isInstance(source)) {
            String m = String.format("%s is instance of %s", ClassUtils.safeClassName(source), instance.javaType.getName());
            throw paramError(instance, dataType, source, new IllegalArgumentException(m));
        }

        final SafeLiteralFunc func = env.safeLiteralFunc();
        final ObjectAccessor accessor = ObjectAccessorFactory.forPojo(instance.javaType);
        final List<CompositeField> fieldList = instance.fieldList;

        final int size = fieldList.size();


        CompositeField field;
        Object value;
        sqlBuilder.append(_Constant.LEFT_PAREN);
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sqlBuilder.append(_Constant.COMMA);
            }
            field = fieldList.get(i);
            value = accessor.get(source, field.fieldName);
            if (value == null) {
                sqlBuilder.append(_Constant.NULL);
                continue;
            }

            func.safeLiteral(field.typeMeta, value, false, sqlBuilder);
        }
        sqlBuilder.append(_Constant.RIGHT_PAREN);

    }

    public static Object parseToPojo(final CompositeType instance, final DataType dataType, final MappingEnv env,
                                     final String source, final int offset, final int endIndex) {
        if (!_StringUtils.hasText(source)
                || source.charAt(offset) != _Constant.LEFT_PAREN
                || source.charAt(endIndex - 1) != _Constant.RIGHT_PAREN) {
            throw dataAccessError(instance, dataType, source, null);
        }

        final ObjectAccessor accessor = ObjectAccessorFactory.forPojo(instance.javaType);
        final Supplier<?> constructor = ObjectAccessorFactory.beanConstructor(instance.javaType);
        final FieldParser fieldParser = new FieldParser(accessor, constructor.get(), instance.fieldList, env);

        try {
            ItemsParser.defaultParser()
                    .parseItems(source, offset, endIndex, fieldParser::parseField);

            if (fieldParser.fieldIndex != fieldParser.fieldCount) {
                throw dataAccessError(instance, dataType, source, null);
            }
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw dataAccessError(instance, dataType, source, e);
        }

        return fieldParser.object;
    }

    private static CompositeType createCompositeType(final Class<?> javaType) {
        final String typeName = AnnotationUtils.getDefinedTypeName(javaType);
        if (typeName == null) {
            // no bug,never here
            throw new IllegalArgumentException();
        }
        final String[] fieldOrder = javaType.getAnnotation(DefinedType.class).fieldOrder();
        final Map<String, Integer> fieldToOrder = _StringUtils.createOrderMap(fieldOrder);

        Column column;

        final List<Pair<Field, Column>> columnList = new ArrayList<>();

        for (Class<?> clazz = javaType; clazz != Object.class; clazz = clazz.getSuperclass()) {
            if (clazz != javaType && clazz.getAnnotation(MappedSuperclass.class) == null) {
                break;
            }

            for (Field field : clazz.getDeclaredFields()) {
                column = field.getAnnotation(Column.class);
                if (column == null) {
                    continue;
                }
                // compile-time verification io.army.annotation.Column
                columnList.add(Pair.create(field, column));

            } // field loop

        } // class loop

        final int columnCount = columnList.size();
        if (columnCount == 0) {
            throw definedTypeError(javaType, "no field");
        } else if (columnCount != fieldOrder.length) {
            throw definedTypeError(javaType, "fieldOrder length error");
        }


        columnList.sort(Comparator.comparingInt(pair -> {
            final Integer order;
            order = fieldToOrder.get(pair.first.getName());
            if (order == null) {
                String m = String.format("%s not in fieldOrder", pair.first.getName());
                throw definedTypeError(javaType, m);
            }
            return order;
        }));


        Pair<Field, Column> pair;
        Field field;
        String columnName;


        final List<CompositeField> fieldList = new ArrayList<>(columnCount);
        final Map<String, Boolean> columnMap = new HashMap<>();
        for (int i = 0; i < columnCount; i++) {

            pair = columnList.get(i);
            field = pair.first;
            column = pair.second;

            columnName = TableMetaUtils.columnName(column, field);
            if (columnMap.putIfAbsent(columnName, Boolean.TRUE) != null) {
                String m = String.format("column[%s] duplicate", columnName);
                throw definedTypeError(javaType, m);
            }
            fieldList.add(CompositeField.from(field.getName(), columnName, _MappingFactory.map(field), column.collation()));

        } // column lop

        return new CompositeType(javaType, typeName, fieldList);
    }


    private static MetaException definedTypeError(Class<?> javaType, String suffixMsg) {
        String m = String.format("%s[%s] %s", DefinedType.class.getSimpleName(), javaType.getName(), suffixMsg);
        return new MetaException(m);
    }


    private static final class FieldParser {

        private final ObjectAccessor accessor;

        private final Object object;

        private final List<CompositeField> fieldList;

        private final MappingEnv env;

        private final DecodeLiteralFunc decodeFunc;

        private final ServerMeta serverMeta;

        private final int fieldCount;

        private int fieldIndex = 0;

        private FieldParser(ObjectAccessor accessor, Object object, List<CompositeField> fieldList, MappingEnv env) {
            this.accessor = accessor;
            this.object = object;
            this.fieldList = fieldList;
            this.env = env;
            this.decodeFunc = env.decodeLiteralFunc();
            this.serverMeta = env.serverMeta();
            this.fieldCount = fieldList.size();
        }

        Object parseField(final String text, final int offest, final int endIndex) {
            final int fieldIndex = this.fieldIndex;
            if (fieldIndex >= this.fieldCount) {
                throw new IllegalArgumentException("composite filed count error");
            }
            final CompositeField field = this.fieldList.get(fieldIndex);
            final TypeMeta typeMeta = field.typeMeta;

            final MappingType type;
            if (typeMeta instanceof MappingType) {
                type = (MappingType) typeMeta;
            } else {
                type = typeMeta.mappingType();
            }

            String literal;
            if (offest == 0 && endIndex == text.length()) {
                literal = text;
            } else {
                literal = text.substring(offest, endIndex);
            }

            if (type != typeMeta) {
                literal = this.decodeFunc.decodeLiteral(typeMeta, literal);
            }

            final Object value;
            value = type.afterGet(type.map(this.serverMeta), this.env, literal);
            this.accessor.set(this.object, field.fieldName, value);
            this.fieldIndex++;
            return Boolean.TRUE;
        }

    } // FieldParser


}
