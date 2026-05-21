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
import io.army.mapping.NameEnumType;
import io.army.mapping.UUIDType;
import io.army.mapping.optional.CompositeField;
import io.army.meta.*;
import io.army.modelgen._MetaBridge;
import io.army.util.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;


public abstract class TableMetaUtils {

    TableMetaUtils() {
        throw new UnsupportedOperationException();
    }


    public static final String DEFAULT_EXP = "${DEFAULT}";

    public static final String RUNTIME_EXP = "${RUNTIME}";

    public static final String OPTIONAL_EXP = "${OPTIONAL}";

    public static final String DEFAULT_NAME_EXP = "${DEFAULT_NAME}";


    public static String columnName(Class<?> domainClass, Column column, Field field, MetaContext context) {
        final String value = column.name(), fieldName = field.getName();
        final String finalValue;
        switch (value) {
            case DEFAULT_EXP:
            case RUNTIME_EXP: {
                final String key, configValue;
                key = context.tempBuilderAndClear().append(domainClass.getName())
                        .append('.')
                        .append(fieldName)
                        .append('.')
                        .append("Column")
                        .append('.')
                        .append("name")
                        .toString();

                configValue = context.tableMetaProperties().getProperty(key);
                if (_StringUtils.hasText(configValue)) {
                    finalValue = configValue.trim();
                } else switch (value) {
                    case DEFAULT_EXP: {
                        finalValue = _MetaBridge.camelToLowerCase(fieldName, context.tempBuilderAndClear());
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
                String m = String.format("%s in %s.%s %s.%s is unsupported", value, domainClass.getName(), field.getName(),
                        "Column", "name");
                throw new MetaException(m);
            }
            default: {
                if (_StringUtils.hasText(value)) {
                    finalValue = value;
                } else {
                    finalValue = _MetaBridge.camelToLowerCase(fieldName, context.tempBuilderAndClear());
                }
            } // default

        } // switch
        context.validateColumnName(domainClass, finalValue);
        return finalValue;
    }


    public static int columnPrecision(Column column, DatabaseObject.FieldObject field, MetaContext context) {
        return columnIntValue(column.precision(), "precision", field, context);
    }

    public static int columnScale(Column column, DatabaseObject.FieldObject field, MetaContext context) {
        return columnIntValue(column.scale(), "scale", field, context);
    }

    public static String columnCollation(Column column, DatabaseObject.FieldObject field, MetaContext context) {
        return columnStringValue(column.collation(), "collation", field, context);
    }


    static String columnDefault(Column column, DatabaseObject.FieldObject field, MetaContext context) {
        return columnStringValue(column.defaultValue(), "defaultValue", field, context);
    }


    static SchemaMeta schemaMeta(Table table, Class<?> domainClass, MetaContext context) {
        final String[] methods = {"catalog", "schema"};
        final String[] values = {table.catalog(), table.schema()};

        String key, value, configValue;
        for (int i = 0; i < methods.length; i++) {
            value = values[i];
            switch (value) {
                case DEFAULT_EXP:
                case RUNTIME_EXP: {
                    key = context.tempBuilderAndClear()
                            .append(domainClass.getName())
                            .append('.')
                            .append("Table")
                            .append('.')
                            .append(methods[i])
                            .toString();
                    configValue = context.tableMetaProperties().getProperty(key);
                    if (_StringUtils.hasText(configValue)) {
                        values[i] = configValue.trim();
                    } else switch (value) {
                        case DEFAULT_EXP:
                            values[i] = "";
                            break;
                        case RUNTIME_EXP:
                            throw new MetaException(String.format("%s no config", key));
                        default:
                            throw new IllegalStateException("bug");
                    }
                }
                break;
                case OPTIONAL_EXP: {
                    String m = String.format("%s in %s %s.%s is unsupported", value, domainClass.getName(),
                            "Table", methods[i]);
                    throw new MetaException(m);
                }
                default:
                    // no-op

            } // switch

        } // loop
        return _SchemaMetaFactory.getSchema(values[0], values[1]);
    }

    static String tableName(Table table, Class<?> domainClass, MetaContext context) {
        final String tableName = table.name();
        if (!_StringUtils.hasText(tableName)) {
            String m = String.format("Domain[%s] table name required", domainClass.getName());
            throw new MetaException(m);
        }
        final String finalName;
        switch (tableName) {
            case DEFAULT_EXP:
            case RUNTIME_EXP: {
                final String key, configValue;
                key = context.tempBuilderAndClear()
                        .append(domainClass.getName())
                        .append('.')
                        .append("Table")
                        .append('.')
                        .append("name")
                        .toString();

                configValue = context.tableMetaProperties().getProperty(key);
                if (_StringUtils.hasText(configValue)) {
                    if (_StringUtils.isCamelCase(configValue)) {
                        throw tableNameIsCamel(domainClass);
                    }
                    finalName = configValue.trim();
                } else finalName = switch (tableName) {
                    case DEFAULT_EXP ->
                            _MetaBridge.camelToLowerCase(domainClass.getSimpleName(), context.tempBuilderAndClear());
                    case RUNTIME_EXP -> throw new MetaException(String.format("%s no config", key));
                    default -> throw new IllegalStateException("bug");
                };
            }
            break;
            case OPTIONAL_EXP: {
                String m = String.format("%s in %s %s.%s is unsupported", tableName, domainClass.getName(),
                        "Table", "name");
                throw new MetaException(m);
            }
            default: {
                finalName = tableName;
            }
        }
        if (_StringUtils.isCamelCase(finalName)) {
            throw tableNameIsCamel(domainClass);
        }
        return finalName;
    }


    static String tableComment(Table table, Class<?> domainClass, MetaContext context) {
        final String comment = table.comment();

        final String finalComment;
        switch (comment) {
            case DEFAULT_EXP:
            case RUNTIME_EXP: {
                final String key, configValue;
                key = context.tempBuilderAndClear()
                        .append(domainClass.getName())
                        .append('.')
                        .append("Table")
                        .append('.')
                        .append("comment")
                        .toString();

                configValue = context.tableMetaProperties().getProperty(key);
                if (_StringUtils.hasText(configValue)) {
                    finalComment = configValue.trim();
                } else finalComment = switch (comment) {
                    case DEFAULT_EXP ->
                            _MetaBridge.camelToComment(domainClass.getSimpleName(), context.tempBuilderAndClear());
                    case RUNTIME_EXP -> throw new MetaException(String.format("%s no config", key));
                    default -> throw new IllegalStateException("bug");
                };
            }
            break;
            case OPTIONAL_EXP: {
                String m = String.format("%s in %s %s.%s is unsupported", comment, domainClass.getName(),
                        "Table", "comment");
                throw new MetaException(m);
            }
            default: {
                if (!_StringUtils.hasText(comment)) {
                    String m = String.format("%s %s.%s no text", domainClass.getName(), "Table", "comment");
                    throw new MetaException(m);
                }
                finalComment = comment;
            }
        }
        return finalComment;
    }

    static boolean immutable(Table table, Class<?> domainClass) {
        final boolean immutable = table.immutable();
        if (immutable && domainClass.getAnnotation(Inheritance.class) != null) {
            String m = String.format("Parent Domain[%s] couldn't be immutable.", domainClass.getName());
            throw new MetaException(m);
        }
        return immutable;
    }

    static boolean tableCreateDdl(Table table, Class<?> domainClass, MetaContext context) {
        final DdlMode mode = table.ddlMode();
        boolean createDdl;
        switch (mode) {
            case CREATE:
                createDdl = true;
                break;
            case NONE:
                createDdl = false;
                break;
            case DEFAULT: {
                final String key, configValue;
                key = context.tempBuilderAndClear()
                        .append(domainClass.getName())
                        .append('.')
                        .append("Table")
                        .append('.')
                        .append("ddlMode")
                        .toString();

                configValue = context.tableMetaProperties().getProperty(key);

                if (_StringUtils.hasText(configValue)) {
                    createDdl = Boolean.parseBoolean(configValue);
                } else {
                    createDdl = true;
                }
            }
            break;
            default:
                throw _Exceptions.unexpectedEnum(mode);
        }
        return createDdl;
    }

    static <T> void assertParentTableMeta(ParentTableMeta<? super T> parentTableMeta
            , Class<T> domainClass) {
        if (!(parentTableMeta instanceof DefaultTableMeta)) {
            String m = String.format("%s isn't instance of %s", TableMeta.class.getName()
                    , DefaultTableMeta.class.getName());
            throw new MetaException(m);
        }
        if (!parentTableMeta.javaType().isAssignableFrom(domainClass)) {
            String m = String.format("%s java type[%s] isn't isAssignable from of %s", TableMeta.class.getName()
                    , parentTableMeta.javaType().getName()
                    , domainClass.getName());
            throw new MetaException(m);
        }
    }

    static Enum<?> discriminatorValue(final Class<?> fieldJavaClass, final Class<?> domainClass) {

        final DiscriminatorValue discriminatorValue;
        discriminatorValue = domainClass.getAnnotation(DiscriminatorValue.class);
        if (discriminatorValue == null) {
            String m = String.format("Domain[%s] isn't annotated by %s."
                    , domainClass.getName(), DiscriminatorValue.class.getName());
            throw new MetaException(m);
        }

        try {
            return NameEnumType.valueOf(fieldJavaClass, discriminatorValue.value());
        } catch (IllegalArgumentException e) {
            String m = String.format("Domain %s DiscriminatorValue[%s] error",
                    domainClass.getName(), discriminatorValue.value());
            throw new MetaException(m, e);
        }
    }

    static <T> FieldMeta<T> discriminator(final Map<String, FieldMeta<T>> fieldMetaMap,
                                          final Class<T> domainClass) {
        final Inheritance inheritance = domainClass.getAnnotation(Inheritance.class);
        Objects.requireNonNull(inheritance);
        final String fieldName = inheritance.value();
        final FieldMeta<T> discriminator = fieldMetaMap.get(fieldName);
        if (discriminator == null) {
            throw notFoundDiscriminator(fieldName, domainClass);
        }
        final Class<?> fieldJavaType = discriminator.javaType();
        if (!fieldJavaType.isEnum()) {
            String m = String.format("Discriminator[%s] in domain[%s] isn't enum type.", fieldName, domainClass.getName());
            throw new MetaException(m);
        }
        return discriminator;
    }


    static DomainPair mappedClassPair(final Class<?> domainClass) throws MetaException {
        List<Class<?>> list = new ArrayList<>(6);
        // add entity class firstly
        list.add(domainClass);
        Class<?> parentDomainClass = null;

        final boolean inheritance = domainClass.getAnnotation(Inheritance.class) != null;
        for (Class<?> superClass = domainClass.getSuperclass(); superClass != null;
             superClass = superClass.getSuperclass()) {
            if (superClass.getAnnotation(Inheritance.class) != null) {
                if (inheritance) {
                    throw inheritanceDuplication(domainClass);
                }
                parentDomainClass = superClass;
                break;
            }
            if (superClass.getAnnotation(MappedSuperclass.class) != null
                    || superClass.getAnnotation(Table.class) != null) {
                list.add(superClass);
            } else {
                break;
            }
        }

        if (list.size() == 1) {
            list = Collections.singletonList(list.getFirst());
        } else {
            // reverse class list
            Collections.reverse(list);
            list = Collections.unmodifiableList(list);
        }
        return new DomainPair(list, parentDomainClass);
    }

    static <T> List<FieldMeta<T>> createFieldMetaList(final TableMeta<T> tableMeta, final MetaContext context) {
        final Class<?> domainClass = tableMeta.javaType();

        final List<FieldMeta<T>> list = new ArrayList<>(20);
        final boolean inheritance = domainClass.getAnnotation(Inheritance.class) != null;
        Field[] fieldArray;
        Field field;
        Column column;
        String columnName, key, configValue;
        for (Class<?> clazz = domainClass; clazz != null; clazz = clazz.getSuperclass()) {

            if (clazz != domainClass && clazz.getAnnotation(Inheritance.class) != null) {
                if (inheritance) {
                    throw inheritanceDuplication(domainClass);
                }
                break;
            }

            if (clazz != domainClass
                    && clazz.getAnnotation(MappedSuperclass.class) == null
                    && clazz.getAnnotation(Table.class) == null) {
                break;
            }

            fieldArray = clazz.getDeclaredFields();
            for (int i = fieldArray.length - 1; i > -1; i--) {
                field = fieldArray[i];
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                column = field.getAnnotation(Column.class);
                if (column == null) {
                    continue;
                }
                list.add(TableFieldMeta.createFieldMeta(tableMeta, field, context));
            } // field loop

        } // class loop

        Collections.reverse(list);
        return List.copyOf(list);
    }


    static <T> Map<String, FieldMeta<T>> createFieldMap(final List<FieldMeta<T>> list) {
        final Map<String, FieldMeta<T>> map = _Collections.hashMapForSize(list.size());
        for (FieldMeta<T> field : list) {
            if (map.putIfAbsent(field.fieldName(), field) != null) {
                throw fieldMetaDuplication(field);
            }
        }
        return Map.copyOf(map);
    }


    static <T> List<IndexMeta<T>> createIndexList(TableMeta<T> tableMeta, MetaContext context,
                                                  Map<String, FieldMeta<T>> fieldMetaMap) {
        final Class<?> domainClass = tableMeta.javaType();
        final Table table = domainClass.getAnnotation(Table.class);
        final Index[] indexArray = table.indexes();
        final List<IndexMeta<T>> indexList = new ArrayList<>(indexArray.length);

        IndexField[] fieldArray;
        List<IndexColumnMeta> metaList;
        List<FieldMeta<T>> fieldList;
        String indexName, indexType;
        String[] fieldNameArray;
        Index index;
        boolean containIdIndex = false;
        for (int i = 0; i < indexArray.length; i++) {
            index = indexArray[i];
            indexType = parseIndexType(domainClass, index.type(), i, context);
            if (indexType == null) {
                continue;
            }
            indexName = parseIndexName(tableMeta, domainClass, index, i, context);
            if (indexName == null) {
                continue;
            }

            fieldArray = index.fields();
            if (fieldArray.length == 0) {
                fieldNameArray = index.fieldList();
                fieldList = new ArrayList<>(fieldNameArray.length);
                metaList = createIndexColumnList(index.fieldList(), tableMeta, indexName, fieldList, context, fieldMetaMap);
            } else {
                fieldList = new ArrayList<>(fieldArray.length);
                metaList = createIndexColumnMetaList(i, fieldArray, tableMeta, fieldList, indexName, context, fieldMetaMap);
            }

            _Assert.isTrue(metaList.size() == fieldList.size(), "");

            if (fieldList.size() == 1 && fieldList.getFirst() instanceof PrimaryFieldMeta<T>) {
                if (!index.unique()) {
                    String m = String.format("%s %s[%s].%s error", domainClass.getName(), "Index", i, "unique");
                    throw new MetaException(m);
                }
                containIdIndex = true;
            }

            indexList.add(new DefaultIndexMeta<>(tableMeta, indexName, index.unique(), fieldList, metaList, indexType));
        }

        if (!containIdIndex) {
            indexName = context.tempBuilderAndClear()
                    .append("uni")
                    .append('_')
                    .append(tableMeta.tableName().toLowerCase(Locale.ROOT))
                    .append('_')
                    .append(tableMeta.id().columnName().toLowerCase(Locale.ROOT))
                    .toString();

            metaList = context.minIndexColumnMetaList(List.of(MinIndexColumnMeta.INSTANCE));
            fieldList = List.of(tableMeta.id());
            indexList.add(new DefaultIndexMeta<>(tableMeta, indexName, true, fieldList, metaList, ""));
        }
        return List.copyOf(indexList);
    }


    @Nullable
    private static String parseIndexType(Class<?> domainClass, final String indexType, int indexOfIndex, MetaContext context) {
        if (indexType.isEmpty()) {
            return "";
        }
        final String finalIndexType;
        switch (indexType) {
            case DEFAULT_EXP:
            case RUNTIME_EXP:
            case OPTIONAL_EXP: {
                final String key, configIndexType;
                key = context.tempBuilderAndClear()
                        .append(domainClass.getName())
                        .append('.')
                        .append("IndexMeta")
                        .append('[')
                        .append(indexOfIndex)
                        .append(']')
                        .append('.')
                        .append("type")
                        .toString();

                configIndexType = context.tableMetaProperties().getProperty(key);

                if (_StringUtils.hasText(configIndexType)) {
                    finalIndexType = configIndexType.trim();
                } else finalIndexType = switch (indexType) {
                    case DEFAULT_EXP -> "";
                    case RUNTIME_EXP -> throw new MetaException(String.format("%s no config", key));
                    case OPTIONAL_EXP -> null;
                    default -> throw new IllegalStateException("bug");
                };
            }
            break;
            default:
                finalIndexType = indexType;
        }
        return finalIndexType;
    }

    @Nullable
    private static String parseIndexName(TableMeta<?> tableMeta, Class<?> domainClass, final Index index,
                                         int indexOfIndex, MetaContext context) {
        final String indexName = index.name();
        final String finalIndexName;
        switch (indexName) {
            case DEFAULT_NAME_EXP:
                finalIndexName = defaultIndexName(tableMeta, index, context);
                break;
            case DEFAULT_EXP:
            case RUNTIME_EXP:
            case OPTIONAL_EXP: {
                final String key, configIndexName;
                key = context.tempBuilderAndClear()
                        .append(domainClass.getName())
                        .append('.')
                        .append("IndexMeta")
                        .append('[')
                        .append(indexOfIndex)
                        .append(']')
                        .append('.')
                        .append("name")
                        .toString();

                configIndexName = context.tableMetaProperties().getProperty(key);

                if (_StringUtils.hasText(configIndexName)) {
                    finalIndexName = configIndexName.trim();
                } else finalIndexName = switch (indexName) {
                    case DEFAULT_EXP -> defaultIndexName(tableMeta, index, context);
                    case RUNTIME_EXP -> throw new MetaException(String.format("%s no config", key));
                    case OPTIONAL_EXP -> null;
                    default -> throw new IllegalStateException("bug");
                };
            }
            break;
            default: {
                if (!_StringUtils.hasText(indexName)) {
                    String m = String.format("%s %s[%s].%s error", domainClass.getName(), "Index", indexOfIndex, "name");
                    throw new MetaException(m);
                }
                finalIndexName = indexName;
            } // default
        } // switch
        return finalIndexName;
    }


    /// @param propName collation or opclass
    private static String parseIndexColumnProperty(Class<?> domainClass, final String propValue, int indexOfIndex,
                                                   String fieldName, MetaContext context, final String propName) {
        if (propValue.isEmpty()) {
            return "";
        }
        final String finalOpclass;
        switch (propValue) {
            case DEFAULT_EXP:
            case RUNTIME_EXP: {
                final String key, configOpclass;
                key = context.tempBuilderAndClear()
                        .append(domainClass.getName())
                        .append('.')
                        .append("IndexMeta")
                        .append('[')
                        .append(indexOfIndex)
                        .append(']')
                        .append('.')
                        .append("field")
                        .append('.')
                        .append(fieldName)
                        .append('.')
                        .append(propName)
                        .toString();

                configOpclass = context.tableMetaProperties().getProperty(key);

                if (_StringUtils.hasText(configOpclass)) {
                    finalOpclass = configOpclass.trim();
                } else finalOpclass = switch (propValue) {
                    case DEFAULT_EXP -> "";
                    case RUNTIME_EXP -> throw new MetaException(String.format("%s no config", key));
                    default -> throw new IllegalStateException("bug");
                };
            }
            break;
            case OPTIONAL_EXP:
                throw new MetaException(String.format("%s in %s index[%s] field[%s] property[%s] is unsupported", propValue,
                        domainClass.getName(), indexOfIndex, fieldName, propName));
            default:
                finalOpclass = propValue;
        }
        return finalOpclass;
    }


    private static String defaultIndexName(final TableMeta<?> tableMeta, final Index index, final MetaContext context) {
        final StringBuilder builder = context.tempBuilderAndClear();
        if (index.unique()) {
            builder.append("uni");
        } else {
            builder.append("idx");
        }
        builder.append('_')
                .append(tableMeta.tableName().toLowerCase(Locale.ROOT));

        FieldMeta<?> fieldMeta;
        final IndexField[] fieldArray = index.fields();
        if (fieldArray.length > 0) {
            for (int i = 0; i < fieldArray.length; i++) {
                fieldMeta = tableMeta.field(fieldArray[i].name()); // FieldMeta is created before IndexMeta
                if (i > 0) {
                    builder.append('_');
                }
                builder.append(fieldMeta.columnName().toLowerCase(Locale.ROOT));
            }
        } else {
            final String[] fieldNameArray = index.fieldList();
            for (int i = 0; i < fieldNameArray.length; i++) {
                fieldMeta = tableMeta.field(fieldNameArray[i]); // FieldMeta is created before IndexMeta
                if (i > 0) {
                    builder.append('_');
                }
                builder.append(fieldMeta.columnName().toLowerCase(Locale.ROOT));
            }
        }

        return builder.toString();
    }


    /// @see #createIndexList(TableMeta, MetaContext, Map)
    private static <T> List<IndexColumnMeta> createIndexColumnMetaList(final int indexOfIndex, IndexField[] fieldArray,
                                                                       TableMeta<T> tableMeta, List<FieldMeta<T>> fieldList,
                                                                       String indexName, MetaContext context,
                                                                       Map<String, FieldMeta<T>> fieldMetaMap) {
        final Class<?> domainClass = tableMeta.javaType();
        final List<IndexColumnMeta> list = new ArrayList<>(fieldArray.length);
        String fieldName, collation, opclass;
        FieldMeta<T> fieldMeta;
        for (IndexField field : fieldArray) {
            fieldName = field.name();

            fieldMeta = fieldMetaMap.get(fieldName);
            if (fieldMeta == null) {
                throw notFoundIndexField(tableMeta, indexName, fieldName);
            }
            collation = parseIndexColumnProperty(domainClass, field.collation(), indexOfIndex, fieldName, context, "collation");

            opclass = parseIndexColumnProperty(domainClass, field.opclass(), indexOfIndex, fieldName, context, "opclass");

            list.add(new DefaultIndexColumnMeta(collation, opclass, field.order(), field.nulls()));
            fieldList.add(fieldMeta);
        }
        return context.indexColumnMetaList(list);
    }

    /// @see #createIndexList(TableMeta, MetaContext, Map)
    private static <T> List<IndexColumnMeta> createIndexColumnList(String[] fieldNameArray, TableMeta<T> tableMeta,
                                                                   String indexName, List<FieldMeta<T>> fieldList,
                                                                   MetaContext context,
                                                                   Map<String, FieldMeta<T>> fieldMetaMap) {
        if (fieldNameArray.length == 0) {
            throw noIndexField(tableMeta, indexName);
        }
        final List<IndexColumnMeta> list = new ArrayList<>(fieldNameArray.length);

        FieldMeta<T> fieldMeta;
        for (String fieldName : fieldNameArray) {
            fieldMeta = fieldMetaMap.get(fieldName);
            if (fieldMeta == null) {
                throw notFoundIndexField(tableMeta, indexName, fieldName);
            }
            list.add(MinIndexColumnMeta.INSTANCE);
            fieldList.add(fieldMeta);
        }
        return context.minIndexColumnMetaList(list);
    }


    static <T> List<FieldMeta<?>> createGeneratorChain(final Map<String, FieldMeta<T>> nameToField)
            throws MetaException {

        final List<Pair<FieldMeta<T>, Integer>> levelList = new ArrayList<>(4);
        GeneratorMeta generatorMeta;
        String dependName;
        for (FieldMeta<T> field : nameToField.values()) {

            generatorMeta = field.generator();
            if (generatorMeta == null) {
                continue;
            }
            dependName = generatorMeta.params().get(FieldGenerator.DEPEND_FIELD_NAME);
            int level = 0;
            for (FieldMeta<?> dependField; dependName != null; ) {
                dependField = nameToField.get(dependName);
                if (dependField == null) {
                    String m = String.format("%s depend %s,but not found dependent field[%s] in %s"
                            , field, dependName, dependName, field.tableMeta());
                    throw new MetaException(m);
                }
                level++;
                generatorMeta = dependField.generator();
                if (generatorMeta == null) {
                    break;
                }
                dependName = generatorMeta.params().get(FieldGenerator.DEPEND_FIELD_NAME);
            }
            levelList.add(Pair.create(field, level));
        }

        final List<FieldMeta<?>> generatorChain;
        switch (levelList.size()) {
            case 0:
                generatorChain = List.of();
                break;
            case 1:
                generatorChain = List.of(levelList.getFirst().getFirst());
                break;
            default: {
                levelList.sort(Comparator.comparingInt(Pair::getSecond));
                final List<FieldMeta<T>> list = new ArrayList<>(levelList.size());
                for (Pair<FieldMeta<T>, Integer> f : levelList) {
                    list.add(f.getFirst());
                }
                generatorChain = List.copyOf(list);
            }
        }
        return generatorChain;
    }


    private static Class<?> getTypeObjetClass(DatabaseObject.FieldObject field) {
        final Class<?> clazz;
        if (field instanceof FieldMeta<?>) {
            clazz = ((FieldMeta<?>) field).tableMeta().javaType();
        } else if (field instanceof CompositeField) {
            clazz = ((CompositeField) field).compositeType().javaType();
        } else {
            throw new IllegalArgumentException("bug");
        }
        return clazz;

    }

    /// @see #columnCollation(Column, DatabaseObject.FieldObject, MetaContext)
    private static String columnStringValue(final String value, String method, DatabaseObject.FieldObject field,
                                            MetaContext context) {
        final String finalValue;
        switch (value) {
            case DEFAULT_EXP:
            case RUNTIME_EXP: {
                final String key, configValue;
                key = context.tempBuilderAndClear()
                        .append(getTypeObjetClass(field).getName())
                        .append('.')
                        .append(field.fieldName())
                        .append('.')
                        .append("Column")
                        .append('.')
                        .append(method)
                        .toString();

                configValue = context.tableMetaProperties().getProperty(key);
                if (_StringUtils.hasText(configValue)) {
                    finalValue = configValue.trim();
                } else switch (value) {
                    case DEFAULT_EXP: {
                        if (method.equals("defaultValue")) {
                            finalValue = zeroValue(field);
                        } else {
                            finalValue = "";
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
            default:
                finalValue = value;

        } // switch
        return finalValue;
    }


    /// @see #columnStringValue(String, String, DatabaseObject.FieldObject, MetaContext)
    private static String zeroValue(DatabaseObject.FieldObject field) {
        final MappingType type;
        final String value;
        if (field instanceof PrimaryFieldMeta<?>
                || (type = field.mappingType()) instanceof MappingType.SqlEnum
                || Enum.class.isAssignableFrom(field.javaType())) {
            value = "";
        } else if (type instanceof MappingType.SqlString) {
            value = "''";
        } else if (type instanceof MappingType.SqlNumber) {
            value = "0";
        } else if (type instanceof MappingType.SqlBoolean) {
            value = "false";
        } else if (type instanceof MappingType.SqlLocalTime) {
            value = "'00:00:00'";
        } else if (type instanceof MappingType.SqlLocalDate) {
            value = "'1970-01-01'";
        } else if (type instanceof MappingType.SqlLocalDateTime) {
            value = "'1970-01-01 00:00:00'";
        } else if (type instanceof MappingType.SqlOffsetDateTime) {
            value = "'1970-01-01 00:00:00+00:00'";
        } else if (type instanceof MappingType.SqlOffsetTime) {
            value = "'00:00:00+00:00'";
        } else {
            value = "";
        }
        return value;
    }

    /// @see #columnIntValue(int, String, DatabaseObject.FieldObject, MetaContext)
    private static int precisionDefault(DatabaseObject.FieldObject field) {
        final MappingType type;
        final Class<?> javaType;
        final int value;
        if (field instanceof PrimaryFieldMeta<?>) {
            value = -1;
        } else if ((type = field.mappingType()) instanceof MappingType.SqlDecimal) {
            value = 16;
        } else if (type instanceof MappingType.SqlEnum t) {
            int maxLength = -1, length;
            for (String label : t.enumLabelList()) {
                length = label.length();
                if (length > maxLength) {
                    maxLength = length;
                }
            }
            value = maxLength + 5;
        } else if (type instanceof MappingType.SqlString) {
            value = 255;
        } else if (Enum.class.isAssignableFrom(javaType = field.javaType())) {
            int maxLength = -1, length;
            for (Enum<?> e : ClassUtils.getEnumConstants(javaType)) {
                length = e.name().length();
                if (length > maxLength) {
                    maxLength = length;
                }
            }
            value = maxLength + 5;
        } else if (type instanceof UUIDType || javaType == UUID.class) {
            value = 36;
        } else {
            value = -1;
        }
        return value;
    }

    /// @see #columnIntValue(int, String, DatabaseObject.FieldObject, MetaContext)
    private static int scaleDefault(DatabaseObject.FieldObject field) {
        final MappingType type;
        final int value;
        if (field instanceof PrimaryFieldMeta<?>) {
            value = -1;
        } else if ((type = field.mappingType()) instanceof MappingType.SqlDecimal) {
            value = 3;
        } else if (type instanceof MappingType.SqlLocalDateTime
                || type instanceof MappingType.SqlOffsetDateTime
                || type instanceof MappingType.SqlLocalTime
                || type instanceof MappingType.SqlOffsetTime) {
            value = 6;
        } else {
            value = -1;
        }
        return value;
    }


    /// @see #columnPrecision(Column, DatabaseObject.FieldObject, MetaContext)
    /// @see #columnScale(Column, DatabaseObject.FieldObject, MetaContext)
    private static int columnIntValue(final int value, String method, DatabaseObject.FieldObject field, MetaContext context) {
        final int finalValue;
        switch (value) {
            case Column.DEFAULT_EXP:
            case Column.RUNTIME_EXP: {
                final String key, configValue;
                key = context.tempBuilderAndClear()
                        .append(getTypeObjetClass(field).getName())
                        .append('.')
                        .append(field.fieldName())
                        .append('.')
                        .append("Column")
                        .append('.')
                        .append(method)
                        .toString();

                configValue = context.tableMetaProperties().getProperty(key);
                if (_StringUtils.hasText(configValue)) {
                    try {
                        finalValue = Integer.parseInt(configValue.trim());
                    } catch (NumberFormatException e) {
                        throw new MetaException(String.format("%s config error", key), e);
                    }
                } else switch (value) {
                    case Column.DEFAULT_EXP: {
                        if (method.equals("precision")) {
                            finalValue = precisionDefault(field);
                        } else if (method.equals("scale")) {
                            finalValue = scaleDefault(field);
                        } else {
                            finalValue = -1;
                        }
                    }
                    break;
                    case Column.RUNTIME_EXP:
                        throw new MetaException(String.format("%s no config", key));
                    default:
                        throw new IllegalStateException("bug");
                }
            }
            break;
            default: {
                finalValue = Math.max(value, -1);
            } // default
        } // switch
        return finalValue;
    }


    static MetaException notFoundDiscriminator(String fieldName, Class<?> domainClass) {
        String m = String.format("Not found discriminator[%s] in domain[%s].", fieldName, domainClass.getName());
        return new MetaException(m);
    }


    /// @see #mappedClassPair(Class)
    private static MetaException inheritanceDuplication(Class<?> domainClass) {
        String m = String.format("Domain[%s] extends link %s count great than 1 in link of extends",
                domainClass.getName(),
                Inheritance.class.getName());
        return new MetaException(m);
    }


    private static MetaException notFoundIndexField(TableMeta<?> tableMeta, String indexName, String fieldName) {
        String m = String.format("Not found index field[%s] for domain[%s] index[%s]",
                fieldName, tableMeta.javaType().getName(), indexName);
        return new MetaException(m);
    }

    private static MetaException noIndexField(TableMeta<?> tableMeta, String indexName) {
        String m = String.format("Not found index field for domain[%s] index[%s]",
                tableMeta.javaType().getName(), indexName);
        return new MetaException(m);
    }

    /// @see #createFieldMap(List)
    private static MetaException fieldMetaDuplication(FieldMeta<?> fieldMeta) {
        String m = String.format("%s.%s duplication.",
                fieldMeta.tableMeta().javaType().getName(), fieldMeta.fieldName());
        return new MetaException(m);
    }

    private static MetaException tableNameIsCamel(Class<?> domainClass) {
        String m = String.format("%s %s.%s is came", domainClass.getName(), "Table", "name");
        return new MetaException(m);
    }


    private static final class DefaultIndexMeta<T> implements IndexMeta<T> {

        private final TableMeta<T> table;

        private final String name;

        private final boolean unique;

        private final List<FieldMeta<T>> fieldList;

        private final List<IndexColumnMeta> columnList;

        private final String indexType;

        private DefaultIndexMeta(TableMeta<T> table, String name, boolean unique, List<FieldMeta<T>> fieldList,
                                 List<IndexColumnMeta> columnList, String indexType) {
            if (fieldList.size() != columnList.size()) {
                throw new IllegalArgumentException();
            }
            this.table = table;
            this.name = name;
            this.unique = unique;
            this.fieldList = List.copyOf(fieldList);
            this.columnList = List.copyOf(columnList);
            this.indexType = indexType;
        }

        @Override
        public TableMeta<T> tableMeta() {
            return this.table;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public List<FieldMeta<T>> fieldList() {
            return this.fieldList;
        }

        @Override
        public List<IndexColumnMeta> columnList() {
            return this.columnList;
        }

        @Override
        public boolean isPrimaryKey() {
            return this.unique
                    && this.fieldList.size() == 1
                    && this.fieldList.getFirst() instanceof PrimaryFieldMeta<T>;
        }

        @Override
        public boolean isUnique() {
            return this.unique;
        }

        @Override
        public String type() {
            return this.indexType;
        }


    } // DefaultIndexMeta


    static final class DomainPair {

        final List<Class<?>> mappedList;

        final Class<?> parent;

        private DomainPair(List<Class<?>> mappedList, @Nullable Class<?> parent) {
            this.mappedList = mappedList;
            this.parent = parent;
        }


    }


    private static final class MinIndexColumnMeta implements IndexColumnMeta {

        private static final MinIndexColumnMeta INSTANCE = new MinIndexColumnMeta();

        private MinIndexColumnMeta() {
        }

        @Override
        public String collation() {
            return "";
        }

        @Override
        public String opclass() {
            return "";
        }

        @Override
        public SortOrder order() {
            return SortOrder.DEFAULT;
        }

        @Override
        public NullsOrder nulls() {
            return NullsOrder.DEFAULT;
        }


    } // MinIndexColumnMeta

    private static final class DefaultIndexColumnMeta implements IndexColumnMeta {

        private final String collation;

        private final String opclass;

        private final SortOrder order;

        private final NullsOrder nulls;

        private DefaultIndexColumnMeta(String collation, String opclass, SortOrder order, NullsOrder nulls) {
            this.collation = collation;
            this.opclass = opclass;
            this.order = order;
            this.nulls = nulls;
        }

        @Override
        public String collation() {
            return this.collation;
        }

        @Override
        public String opclass() {
            return this.opclass;
        }

        @Override
        public SortOrder order() {
            return this.order;
        }

        @Override
        public NullsOrder nulls() {
            return this.nulls;
        }


        @Override
        public int hashCode() {
            return Objects.hash(this.collation, this.opclass, this.order, this.nulls);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof DefaultIndexColumnMeta o) {
                match = Objects.equals(this.collation, o.collation)
                        && Objects.equals(this.opclass, o.opclass)
                        && this.order == o.order
                        && this.nulls == o.nulls;
            } else {
                match = false;
            }
            return match;
        }

    } // DefaultIndexColumnMeta


}
