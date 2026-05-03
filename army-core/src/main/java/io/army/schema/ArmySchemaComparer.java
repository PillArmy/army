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

package io.army.schema;


import io.army.lang.Nullable;
import io.army.mapping.MappingType;
import io.army.mapping.optional.CompositeField;
import io.army.meta.*;
import io.army.sqltype.DataType;
import io.army.struct.TypeCategory;
import io.army.util._Collections;
import io.army.util._StringUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class ArmySchemaComparer implements SchemaComparer {

    final ServerMeta serverMeta;

    final boolean jdbc;

    private List<String> errorMsgList;

    ArmySchemaComparer(ServerMeta serverMeta) {
        this.serverMeta = serverMeta;
        this.jdbc = serverMeta.driverSpi().equals("java.sql");
    }

    @Override
    public final SchemaResult compare(SchemaInfo schemaInfo, SchemaMeta schemaMeta,
                                      Collection<TableMeta<?>> tableMetas,
                                      Map<String, MappingType> definedTypeMap) {
        if (compareSchema(schemaInfo, schemaMeta)) {
            String m = String.format("_SchemaInfo[%s,%s] and %s not match,serverMeta[%s].",
                    schemaInfo.catalog(), schemaInfo.schema(), schemaMeta, this.serverMeta);
            throw new IllegalArgumentException(m);
        }

        final Map<String, TableInfo> tableInfoMap = schemaInfo.tableMap();

        final TableResult.Builder builder = TableResult.builder();
        final List<TableMeta<?>> newTableList = _Collections.arrayList();
        final List<TableResult> tableResultList = _Collections.arrayList();
        final boolean supportTableComment = this.supportTableComment();

        final FieldResult.Builder fieldBuilder = FieldResult.builder();

        final Set<String> columnMetaSet;
        columnMetaSet = _Collections.hashSet();

        TableInfo tableInfo;
        for (TableMeta<?> table : tableMetas) {
            tableInfo = tableInfoMap.get(table.tableName());
            if (tableInfo == null) {
                newTableList.add(table);
                continue;
            }
            builder.table(table);
            if (supportTableComment) {
                builder.comment(!table.comment().equals(tableInfo.comment()));
            }

            compareColumns(tableInfo, table, builder, fieldBuilder);
            compareIndex(tableInfo, (TableMeta<?>) table, builder, columnMetaSet);
            tableResultList.add(builder.buildAndClear());
        }

        final Map<MappingType, Integer> typeMap = new HashMap<>();
        final List<TypeResult> typeResultList;
        typeResultList = compareTypes(definedTypeMap, schemaInfo.definedTypeMap(), typeMap);

        if (hasError()) {
            throw createSchemaCompareException();
        }

        final List<MappingType> newTypeList;
        if (typeMap.isEmpty()) {
            newTypeList = List.of();
        } else {
            newTypeList = updateTypeOrder(typeMap, false);
        }
        return new DefaultSchemaResult(schemaMeta.catalog(), schemaMeta.schema(), newTableList, tableResultList, typeResultList, newTypeList);
    }


    /// @return true : schema isn't match.
    abstract boolean compareSchema(SchemaInfo schemaInfo, SchemaMeta schemaMeta);

    /// @return true : sql type definition is different.
    abstract boolean compareSqlType(ColumnInfo columnInfo, FieldMeta<?> field, DataType dataType);

    /// @return true : default expression definition is different.
    abstract boolean compareDefault(ColumnInfo columnInfo, FieldMeta<?> field, DataType sqlType);

    /// @return true : support column comment.
    abstract boolean supportColumnComment();

    abstract boolean supportTableComment();

    abstract String primaryKeyName(TableMeta<?> table);


    boolean isSameType(String typeName, String typeName1) {
        throw new UnsupportedOperationException();
    }

    Pattern getCheckPattern() {
        throw new UnsupportedOperationException();
    }


    private void compareColumns(TableInfo tableInfo, TableMeta<?> table, TableResult.Builder tableBuilder,
                                FieldResult.Builder builder) {

        final Map<String, ColumnInfo> columnMap = tableInfo.columnMap();
        final ServerMeta serverMeta = this.serverMeta;
        final boolean supportColumnComment = this.supportColumnComment();

        ColumnInfo column;
        DataType dataType;
        Boolean notNull;
        for (FieldMeta<?> field : table.fieldList()) {
            column = columnMap.get(field.columnName());
            if (column == null) {
                tableBuilder.appendNewColumn(field);
                continue;
            }
            dataType = field.mappingType().map(serverMeta);
            builder.field(field)
                    .sqlType(compareSqlType(column, field, dataType))
                    .defaultExp(compareDefault(column, field, dataType));
            notNull = column.notNull();
            if (notNull != null) {
                builder.notNull(notNull != field.notNull());
            }
            if (supportColumnComment) {
                builder.comment(!field.comment().equals(column.comment()));
            }
            if (builder.hasDifference()) {
                tableBuilder.appendFieldResult(builder.build());
            }
            builder.clear();

        }// for


    }

    private <T> void compareIndex(final TableInfo tableInfo, final TableMeta<T> table,
                                  final TableResult.Builder tableBuilder, Set<String> columnMetaSet) {

        final Map<String, IndexInfo> indexMap = tableInfo.indexMap();
        IndexInfo indexInfo;
        String indexName;
        List<IndexFieldMeta<T>> indexFieldList;
        List<String> columnList;
        List<Boolean> ascList;
        for (IndexMeta<T> index : table.indexList()) {
            if (index.isPrimaryKey()) { // TODO add primary key code
                continue;
            }

            indexName = index.name();
            indexInfo = indexMap.get(indexName);
            if (indexInfo == null) {
                handleNotFoundIndex(index, indexMap, columnMetaSet, tableBuilder);
                continue;
            }
            if (indexInfo.unique() != index.isUnique()) {
                tableBuilder.appendChangeIndex(indexName);
                continue;
            }
            indexFieldList = index.fieldList();
            columnList = indexInfo.columnList();

            final int fieldSize = indexFieldList.size();
            if (columnList.size() != fieldSize) {
                tableBuilder.appendChangeIndex(indexName);
                continue;
            }
            ascList = indexInfo.ascList();
            for (int i = 0; i < fieldSize; i++) {
                IndexFieldMeta<T> field = indexFieldList.get(i);
                if (!columnList.contains(field.columnName())) {
                    tableBuilder.appendChangeIndex(indexName);
                    break;
                }
                Boolean fieldAsc = field.fieldAsc();
                Boolean columnAsc = ascList.get(i);
                if (fieldAsc == null || columnAsc == null) {
                    continue;
                }
                if (!fieldAsc.equals(columnAsc)) {
                    tableBuilder.appendChangeIndex(indexName);
                    break;
                }

            }//inner for

        }

    }

    private List<TypeResult> compareTypes(Map<String, MappingType> definedTypeMap, Map<String, TypeInfo> typeInfoMap,
                                          final Map<MappingType, Integer> typeMap) {
        String typeName;
        MappingType type;
        TypeInfo typeInfo;

        final List<TypeResult> typeResultList = _Collections.arrayList();
        final TypeResult.Builder typeBuilder = TypeResult.builder();

        TypeResult typeResult;
        for (Map.Entry<String, MappingType> e : definedTypeMap.entrySet()) {
            typeName = e.getKey();
            type = e.getValue();
            typeInfo = typeInfoMap.get(typeName);
            if (typeInfo == null) {
                typeMap.put(type, -1);
                continue;
            }

            if (type instanceof MappingType.SqlComposite) {
                typeResult = compareCompositeType((MappingType.SqlComposite) type, typeInfo, typeBuilder);
            } else if (type instanceof MappingType.SqlEnum) {
                typeResult = compareEnumType((MappingType.SqlEnum) type, typeInfo, typeBuilder);
            } else if (type instanceof MappingType.SqlRange) {
                typeResult = compareRangeType((MappingType.SqlRange) type, typeInfo, typeBuilder);
            } else if (type instanceof MappingType.SqlDomain) {
                typeResult = compareDomainType((MappingType.SqlDomain) type, typeInfo, typeBuilder);
            } else {
                typeResult = null;
            }

            if (typeResult != null) {
                typeResultList.add(typeResult);
            }

        } // type loop
        return typeResultList;
    }


    @Nullable
    private TypeResult compareCompositeType(final MappingType.SqlComposite compositeType, TypeInfo typeInfo,
                                            TypeResult.Builder builder) {
        if (typeInfo.category() != TypeCategory.COMPOSITE) {
            addTypeNotMatchError(compositeType, TypeCategory.COMPOSITE, typeInfo);
            return null;
        }

        final List<CompositeField> javaFieldList = compositeType.fieldList();
        final List<CompositeField> dbFieldList = typeInfo.compositeFieldList();

        final Map<String, CompositeField> javaFieldMap = compositeFieldListToMap(javaFieldList);

        final int javaFieldCount, dbFieldCount;
        javaFieldCount = javaFieldList.size();
        dbFieldCount = dbFieldList.size();

        List<CompositeField> dropFieldList = null, newFieldList = null, modifyFieldList = null;
        String columnName;
        boolean modify;
        CompositeField javaField, dbField;
        TypeMeta javaTypeMeta;

        topLoop:
        for (int javaIndex = 0, dbStart = 0; javaIndex < javaFieldCount; javaIndex++) {
            javaField = javaFieldList.get(javaIndex);
            columnName = javaField.columnName().toLowerCase(Locale.ROOT);

            for (int dbIndex = dbStart; dbIndex < dbFieldCount; dbIndex++) {
                dbField = dbFieldList.get(dbIndex);
                if (!columnName.equals(dbField.columnName())) {
                    if (javaFieldMap.containsKey(dbField.columnName())) {
                        String m = String.format("%s composite field order error, composite type don't support field order change,you can only add or drop field.",
                                compositeType.javaType().getName());
                        addErrorMessage(m);
                        break topLoop;
                    }
                    if (dropFieldList == null) {
                        dropFieldList = new ArrayList<>();
                    }
                    dropFieldList.add(dbField);
                    dbStart = dbIndex + 1;
                    continue;
                }

                javaTypeMeta = javaField.mappingType();
                if (!(javaTypeMeta instanceof MappingType)) {
                    javaTypeMeta = javaTypeMeta.mappingType();
                }

                modify = !javaTypeMeta.equals(dbField.mappingType())
                        && !((MappingType) javaTypeMeta).map(this.serverMeta).equals(((MappingType) dbField.mappingType()).map(this.serverMeta));

                if (!modify) {
                    modify = _StringUtils.hasText(javaField.collation())
                            && !javaField.collation().toLowerCase(Locale.ROOT).equals(dbField.collation());
                }

                if (modify) {
                    if (modifyFieldList == null) {
                        modifyFieldList = new ArrayList<>();
                    }
                    modifyFieldList.add(javaField);
                }

                dbStart = dbIndex + 1;
                continue topLoop;

            } // dbField loop


            if (newFieldList == null) {
                newFieldList = new ArrayList<>();
            }
            newFieldList.add(javaField); // not found, so new field

        } // top loop


        builder.type((MappingType) compositeType)
                .category(TypeCategory.COMPOSITE)
                .compositeDropFieldList(dropFieldList)
                .compositeNewFieldList(newFieldList)
                .compositeModifyFieldList(modifyFieldList);

        if (hasError() || !builder.hasDifference()) {
            builder.clear();
            return null;
        }

        return builder.build();
    }


    @Nullable
    private TypeResult compareEnumType(MappingType.SqlEnum enumType, TypeInfo typeInfo,
                                       TypeResult.Builder builder) {
        final Class<?> javaType = enumType.javaType();

        if (typeInfo.category() != TypeCategory.ENUM) {
            addTypeNotMatchError(enumType, TypeCategory.ENUM, typeInfo);
            return null;
        }

        if (!javaType.isEnum()) {
            String m = String.format("%s  type name[%s] should be enum ,but not "
                    , javaType.getName(), enumType.objectName());
            addErrorMessage(m);
            return null;
        }

        final Set<String> enumLabelSet = new HashSet<>(typeInfo.enumLabelList());

        List<String> newLabelList = null;
        for (String label : enumType.enumLabelList()) {
            if (enumLabelSet.contains(label)) {
                continue;
            }
            if (newLabelList == null) {
                newLabelList = new ArrayList<>();
            }
            newLabelList.add(label);
        }

        if (newLabelList != null) {
            return builder.type((MappingType) enumType)
                    .category(TypeCategory.ENUM)
                    .enumNewLabelList(newLabelList)
                    .build();
        }
        builder.clear();
        return null;
    }


    @Nullable
    private TypeResult compareRangeType(MappingType.SqlRange rangeType, TypeInfo typeInfo,
                                        TypeResult.Builder builder) {
        if (typeInfo.category() != TypeCategory.RANGE) {
            addTypeNotMatchError(rangeType, TypeCategory.RANGE, typeInfo);
            return null;
        }
        if (typeInfo.rangeMulti() == null) {
            // multi-range type, ignore
            return null;
        }

        builder.type((MappingType) rangeType)
                .category(TypeCategory.RANGE);

        final String typeName = typeInfo.rangeSubType();
        if (typeName == null) {
            String m = String.format("range type[%s] sub type is null", rangeType.objectName());
            addErrorMessage(m);
            return null;
        }
        builder.containRangeSubType(!isSameType(rangeType.rangeSubTypeName(), typeName));

        String value;

        value = rangeType.subTypeCollation().toLowerCase(Locale.ROOT);
        if (_StringUtils.hasText(value)) {
            builder.containRangeCollation(!value.equals(typeInfo.rangeCollation()));
        } else {
            builder.containRangeCollation(typeInfo.rangeCollation() != null);
        }

        value = rangeType.multiRangeTypeName().toUpperCase(Locale.ROOT);
        if (_StringUtils.hasText(value)) {
            builder.containRangeMulti(!value.equals(typeInfo.rangeMulti()));
        } else {
            builder.containRangeMulti(typeInfo.rangeMulti() != null);
        }

        value = rangeType.subtypeOperator().toLowerCase(Locale.ROOT);
        if (_StringUtils.hasText(value)) {
            builder.containRangeSubOpc(!value.equals(typeInfo.rangeSubOpc()));
        } else {
            builder.containRangeSubOpc(typeInfo.rangeSubOpc() != null);
        }

        value = rangeType.fanonicalFunc().toLowerCase(Locale.ROOT);
        if (_StringUtils.hasText(value)) {
            builder.containRangeCanonical(!value.equals(typeInfo.rangeCanonical()));
        } else {
            builder.containRangeCanonical(typeInfo.rangeCanonical() != null);
        }

        value = rangeType.subtypeDiffFunc().toLowerCase(Locale.ROOT);
        if (_StringUtils.hasText(value)) {
            builder.containRangeSubDiff(!value.equals(typeInfo.rangeSubDiff()));
        } else {
            builder.containRangeSubDiff(typeInfo.rangeSubDiff() != null);
        }

        if (builder.hasDifference()) {
            return builder.build();
        }
        builder.clear();
        return null;
    }


    @Nullable
    private TypeResult compareDomainType(MappingType.SqlDomain domainType, TypeInfo typeInfo,
                                         TypeResult.Builder builder) {
        if (typeInfo.category() != TypeCategory.DOMAIN) {
            addTypeNotMatchError(domainType, TypeCategory.DOMAIN, typeInfo);
            return null;
        }

        builder.type((MappingType) domainType)
                .category(TypeCategory.DOMAIN)
                .containNotNull(domainType.isNotNull() != typeInfo.isNotNull());

        final String typeName = typeInfo.baseTypeName();
        if (typeName == null) {
            String m = String.format("domain type[%s] base type is null", domainType.objectName());
            addErrorMessage(m);
            return null;
        }

        builder.containBaseType(!isSameType(domainType.baseTypeName(), typeName))
                .containDefault(_StringUtils.hasText(domainType.defaultValue()) ^ _StringUtils.hasText(typeInfo.defaultValue()));

        String value;
        value = domainType.collation().toLowerCase(Locale.ROOT);
        if (_StringUtils.hasText(value)) {
            builder.containCollation(!value.equals(typeInfo.collation()));
        } else {
            builder.containCollation(typeInfo.collation() != null);
        }

        value = domainType.constraint().toLowerCase(Locale.ROOT);

        final Matcher matcher = getCheckPattern().matcher(value);
        if (!matcher.find()) {
            String m = String.format("domain type[%s] constraint[%s] format error", domainType.objectName(), value);
            addErrorMessage(m);
            return null;
        }
        if (!matcher.group(1).equals(typeInfo.check())) {
            builder.constraintName(typeInfo.constraintName());
        }

        if (builder.hasDifference()) {
            return builder.build();
        }
        builder.clear();
        return null;
    }


    private static void handleNotFoundIndex(IndexMeta<?> indexMeta, Map<String, IndexInfo> indexMap,
                                            Set<String> columnMetaSet, TableResult.Builder tableBuilder) {

        if (!columnMetaSet.isEmpty()) {
            columnMetaSet.clear();
        }

        for (IndexFieldMeta<?> field : indexMeta.fieldList()) {
            columnMetaSet.add(field.columnName().toLowerCase(Locale.ROOT));
        }

        final int columnCount = columnMetaSet.size();
        List<String> columnNameList;
        IndexInfo actualIndex = null;
        for (IndexInfo info : indexMap.values()) {
            columnNameList = info.columnList();

            if (columnNameList.size() == columnCount
                    && columnMetaSet.containsAll(columnNameList)) {
                actualIndex = info;
                break;
            }
        }

        if (actualIndex == null) {
            tableBuilder.appendNewIndex(indexMeta.name());
        } else if (indexMeta.isUnique() != actualIndex.unique()) { // TODO check index type
            tableBuilder.appendDropIndex(actualIndex.indexName()); // drop old index(actualIndex)
            tableBuilder.appendNewIndex(indexMeta.name()); // create new index
        }

        columnMetaSet.clear();

    }


    private void addTypeNotMatchError(MappingType.SqlUserDefined type, TypeCategory category, TypeInfo typeInfo) {
        String m = String.format("%s category[%s] and  database type [%s] category[%s] not match"
                , type.javaType().getName(), category.name(), type.objectName(), typeInfo.category().name());
        addErrorMessage(m);
    }


    private void addErrorMessage(String msg) {
        List<String> errorMsgList = this.errorMsgList;
        if (errorMsgList == null) {
            this.errorMsgList = errorMsgList = new ArrayList<>();
        }
        errorMsgList.add(msg);
    }

    private boolean hasError() {
        return !_Collections.isEmpty(this.errorMsgList);
    }

    private MetaException createSchemaCompareException() {
        final List<String> errorMsgList = this.errorMsgList;
        final int size = errorMsgList.size();

        final StringBuilder msgBuilder = new StringBuilder(size * 30);
        msgBuilder.append("Schema compare error:");

        for (int i = 0; i < size; i++) {
            msgBuilder.append('\n')
                    .append(i + 1)
                    .append(':')
                    .append(errorMsgList.get(i));
        }

        return new MetaException(msgBuilder.toString());
    }


    static List<MappingType> updateTypeOrder(final Map<MappingType, Integer> typeMap, final boolean drop) {
        typeMap.replaceAll((t, _) -> definedTypeDependencyDepth(t, typeMap::containsKey));
        final List<MappingType> list = new ArrayList<>(typeMap.keySet());

        Comparator<MappingType> comparator = Comparator.comparingInt(typeMap::get);
        if (drop) {
            comparator = comparator.reversed();
        }

        list.sort(comparator);
        return List.copyOf(list);
    }


    private static int definedTypeDependencyDepth(final MappingType type, Predicate<MappingType> predicate) {
        if (type instanceof MappingType.SqlEnum) {
            return 0;
        }
        int deep = 0;
        if (type instanceof MappingType.SqlComposite) {
            deep = compositeDependencyDepth((MappingType.SqlComposite) type, predicate);
        } else if (type instanceof MappingType.SqlDomain) {
            deep = domainDependencyDepth((MappingType.SqlDomain) type, predicate);
        } else if (type instanceof MappingType.SqlRange) {
            deep = rangeDependencyDepth((MappingType.SqlRange) type, predicate);
        }
        return deep;
    }

    private static int compositeDependencyDepth(final MappingType.SqlComposite type, Predicate<MappingType> predicate) {
        final List<CompositeField> fieldList = type.fieldList();
        TypeMeta typeMeta;
        int deep = 0, tempDeep;
        for (CompositeField field : fieldList) {
            typeMeta = field.mappingType();
            if (!(typeMeta instanceof MappingType)) {
                typeMeta = typeMeta.mappingType();
            }
            if (typeMeta instanceof MappingType.SqlUserDefined && predicate.test((MappingType) typeMeta)) {
                tempDeep = 1 + definedTypeDependencyDepth((MappingType) typeMeta, predicate);
                deep = Math.max(deep, tempDeep);
            }
        }
        return deep;
    }

    private static int domainDependencyDepth(final MappingType.SqlDomain type, Predicate<MappingType> predicate) {
        final MappingType baseType = type.baseType();
        int deep = 0;
        if (baseType instanceof MappingType.SqlUserDefined && predicate.test(baseType)) {
            deep = 1 + definedTypeDependencyDepth(baseType, predicate);
        }
        return deep;
    }

    private static int rangeDependencyDepth(final MappingType.SqlRange type, Predicate<MappingType> predicate) {
        final MappingType subType = type.rangeSubType();
        int deep = 0;
        if (subType instanceof MappingType.SqlUserDefined && predicate.test(subType)) {
            deep = 1 + definedTypeDependencyDepth(subType, predicate);
        }
        return deep;
    }


    private static Map<String, CompositeField> compositeFieldListToMap(List<CompositeField> fieldList) {
        final int size = fieldList.size();
        final Map<String, CompositeField> fieldMap = _Collections.hashMapForSize(size);
        CompositeField field;
        for (int i = 0; i < size; i++) {
            field = fieldList.get(i);
            fieldMap.put(field.columnName().toLowerCase(Locale.ROOT), field);
        }
        return fieldMap;
    }


    private static final class DefaultSchemaResult implements SchemaResult {

        private final String catalog;

        private final String schema;

        private final List<TableMeta<?>> newTableList;

        private final List<TableResult> tableResultList;

        private final List<TypeResult> typeResultList;

        private final List<MappingType> newTypeList;

        private DefaultSchemaResult(@Nullable String catalog, @Nullable String schema, List<TableMeta<?>> newTableList,
                                    List<TableResult> tableResultList,
                                    List<TypeResult> typeResultList,
                                    List<MappingType> newTypeList) {
            this.catalog = catalog;
            this.schema = schema;
            this.newTableList = _Collections.unmodifiableList(newTableList);
            this.tableResultList = _Collections.unmodifiableList(tableResultList);
            this.typeResultList = List.copyOf(typeResultList);
            this.newTypeList = List.copyOf(newTypeList);
        }

        @Override
        public String catalog() {
            return this.catalog;
        }

        @Override
        public String schema() {
            return this.schema;
        }

        @Override
        public List<TableMeta<?>> dropTableList() {
            return Collections.emptyList();
        }

        @Override
        public List<TableMeta<?>> newTableList() {
            return this.newTableList;
        }

        @Override
        public List<TableResult> changeTableList() {
            return this.tableResultList;
        }

        @Override
        public List<MappingType> dropTypeList() {
            return List.of();
        }

        @Override
        public List<MappingType> newTypeList() {
            return this.newTypeList;
        }

        @Override
        public List<TypeResult> modifyTypeList() {
            return this.typeResultList;
        }

        @Override
        public boolean hasChanges() {
            return !this.newTableList.isEmpty()
                    || !this.tableResultList.isEmpty()
                    || !this.typeResultList.isEmpty()
                    || !this.newTypeList.isEmpty()
                    ;
        }


    } // DefaultSchemaResult


}
