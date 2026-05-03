package io.army.util;

import io.army.criteria.Selection;
import io.army.executor.ExecutorSupport;
import io.army.lang.Nullable;
import io.army.mapping.MappingType;
import io.army.mapping.optional.CompositeField;
import io.army.mapping.optional.CompositeFieldFactory;
import io.army.meta.MetaException;
import io.army.pojo.ObjectAccessor;
import io.army.pojo.ObjectAccessorFactory;
import io.army.result.CurrentRecord;
import io.army.result.DataRecord;
import io.army.result.RecordMeta;
import io.army.schema.TypeInfo;
import io.army.stmt.SingleSqlStmt;
import io.army.stmt.TwoStmtQueryStmt;
import io.army.struct.TypeCategory;
import io.army.type.ImmutableSpec;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public abstract class RowFunctions {

    private RowFunctions() {
    }


    public static Function<CurrentRecord, Map<String, Object>> hashMapRowFunc(boolean immutableMap) {
        final IntFunction<Map<String, Object>> constructor;
        constructor = _Collections::hashMap;

        final MapRowReader<Map<String, Object>> reader;
        reader = new MapRowReader<>(constructor, immutableMap);
        return reader::readRow;
    }


    public static Function<CurrentRecord, Map<String, Object>> treeMapRowFunc(boolean immutableMap) {
        final Supplier<Map<String, Object>> constructor;
        constructor = RowMaps::treeMap;

        final MapRowReader<Map<String, Object>> reader;
        reader = new MapRowReader<>(constructor, immutableMap);
        return reader::readRow;
    }

    public static Function<CurrentRecord, Map<String, Object>> linkedHashMapRowFunc(boolean immutableMap) {
        final Supplier<Map<String, Object>> constructor;
        constructor = RowMaps::linkedHashMap;

        final MapRowReader<Map<String, Object>> reader;
        reader = new MapRowReader<>(constructor, immutableMap);
        return reader::readRow;
    }


    public static <R> Function<CurrentRecord, R> objectRowFunc(final Supplier<R> constructor, boolean immutableMap) {
        Objects.requireNonNull(constructor);
        final ObjectRowReader<R> reader;
        reader = new ObjectRowReader<>(constructor, null, immutableMap);
        return reader::readRow;
    }


    public static <R> Function<CurrentRecord, R> mapRowFunc(final IntFunction<R> constructor, boolean immutableMap) {
        Objects.requireNonNull(constructor);
        final MapRowReader<R> reader;
        reader = new MapRowReader<>(constructor, immutableMap);
        return reader::readRow;
    }

    public static <R> Function<CurrentRecord, R> classRowFunc(final Class<R> resultClass, final SingleSqlStmt stmt) {
        final Function<CurrentRecord, R> rowFunc;
        final List<? extends Selection> selectionList = stmt.selectionList();
        if ((stmt instanceof TwoStmtQueryStmt && ((TwoStmtQueryStmt) stmt).maxColumnSize() == 1)
                || selectionList.size() == 1) {
            rowFunc = record -> record.get(0, resultClass);
        } else {
            rowFunc = RowFunctions.beanRowFunc(resultClass);
        }
        return rowFunc;
    }


    public static <R> Function<CurrentRecord, R> beanRowFunc(final Class<R> resultClass) {

        final ObjectAccessor accessor;
        accessor = ObjectAccessorFactory.forPojo(resultClass);
        final ObjectRowReader<R> reader;
        reader = new ObjectRowReader<>(ObjectAccessorFactory.beanConstructor(resultClass), accessor, true);
        return reader::readRow;
    }

    public static Function<CurrentRecord, Boolean> typeInfoFunc(final Map<String, TypeInfo> typeInfoMap) {
        final TypeInfoReader reader = new TypeInfoReader(typeInfoMap);
        return reader::readRow;
    }


    public static void readRowColumns(final Class<?>[] columnClassArray, final @Nullable String[] columnLabelArray,
                                      final @Nullable int[] propIndexArray, final DataRecord record,
                                      final Object row, final ObjectAccessor accessor) {

        if ((columnLabelArray == null) == (propIndexArray == null)) {
            throw new IllegalArgumentException();
        }

        final RecordMeta meta = record.getRecordMeta();
        final int columnCount = record.getColumnCount();

        if (columnCount != columnClassArray.length) {
            throw _Exceptions.columnCountAndSelectionCountNotMatch(columnCount, columnClassArray.length);
        } else if (propIndexArray != null && propIndexArray.length != columnCount) {
            throw new IllegalArgumentException();
        } else if (columnLabelArray != null && columnLabelArray.length != columnCount) {
            throw new IllegalArgumentException();
        }

        final boolean pseudoAccessor = accessor == ExecutorSupport.RECORD_PSEUDO_ACCESSOR;

        String propertyName;
        Class<?> columnClass;
        Object value;
        for (int i = 0, propIndex; i < columnCount; i++) {

            columnClass = columnClassArray[i];

            if (columnClass == null) {
                if (propIndexArray == null) {  // Map instance
                    columnClass = meta.getMappingType(i).javaType();
                    columnLabelArray[i] = meta.getColumnLabel(i);
                } else {
                    propertyName = meta.getColumnLabel(i);
                    propIndexArray[i] = propIndex = accessor.getIndex(propertyName);
                    if (propIndex < 0) {
                        throw _Exceptions.notFoundBeanProp(propertyName, row.getClass());
                    }
                    columnClass = accessor.getJavaType(propIndex);
                }
                columnClassArray[i] = columnClass;
            }

            value = record.get(i, columnClass);

            if (value == null && columnClass.isPrimitive()) {
                throw _Exceptions.primitiveNullColumn(meta.getColumnLabel(i), row.getClass());
            }

            if (pseudoAccessor) {
                continue;
            }

            if (propIndexArray == null) {
                accessor.set(row, columnLabelArray[i], value);
            } else {
                accessor.set(row, propIndexArray[i], value);
            }

        } // for loop
    }

    private static final class ObjectRowReader<R> {

        private final Supplier<R> constructor;

        private final boolean immutableMap;

        private Class<?>[] columnClassArray;

        private String[] columnLabelArray;

        private int[] propIndexArray;

        private ObjectAccessor accessor;

        private ObjectRowReader(Supplier<R> constructor, @Nullable ObjectAccessor accessor, boolean immutableMap) {
            this.constructor = constructor;
            this.accessor = accessor;
            this.immutableMap = immutableMap;
        }


        @SuppressWarnings("unchecked")
        private R readRow(final CurrentRecord record) {
            final RecordMeta meta = record.getRecordMeta();
            final int columnCount = meta.getColumnCount();

            final R row;
            row = this.constructor.get();
            if (row == null) {
                throw _Exceptions.objectConstructorError();
            }
            ObjectAccessor accessor = this.accessor;
            if (accessor == null) {
                this.accessor = accessor = ObjectAccessorFactory.fromInstance(row);
            }

            String[] columnLabelArray = this.columnLabelArray;
            Class<?>[] columnClassArray = this.columnClassArray;
            int[] propIndexArray = this.propIndexArray;
            if (columnClassArray == null) {
                this.columnClassArray = columnClassArray = new Class<?>[columnCount];

                if (row instanceof Map<?, ?>) {
                    this.columnLabelArray = columnLabelArray = new String[columnCount];
                    this.propIndexArray = null;
                } else {
                    this.propIndexArray = propIndexArray = new int[columnCount];
                    this.columnLabelArray = null;
                }

            }

            RowFunctions.readRowColumns(columnClassArray, columnLabelArray, propIndexArray, record, row, accessor);

            final R finalRow;
            if (this.immutableMap && row instanceof Map && row instanceof ImmutableSpec) {
                finalRow = (R) _Collections.unmodifiableMap((Map<String, Object>) row);
            } else {
                finalRow = row;
            }
            return finalRow;
        }


    } // SelectionRowReader

    private static final class MapRowReader<R> {

        private final Object constructor;

        private final boolean immutableMap;

        private final ObjectAccessor accessor;

        private String[] columnLabelArray;


        private MapRowReader(Object constructor, boolean immutableMap) {
            this.constructor = constructor;
            this.immutableMap = immutableMap;
            this.accessor = ObjectAccessorFactory.MAP_ACCESSOR;
        }


        @SuppressWarnings("unchecked")
        private R readRow(final DataRecord record) {
            final int columnCount = record.getColumnCount();

            String[] columnLabelArray = this.columnLabelArray;
            if (columnLabelArray == null) {
                this.columnLabelArray = columnLabelArray = new String[columnCount];
            }


            final Object constructor = this.constructor;
            final R row;
            if (constructor instanceof Supplier<?>) {
                row = ((Supplier<R>) constructor).get();
            } else {
                row = ((IntFunction<R>) constructor).apply((int) (columnCount / 0.75f));
            }

            if (row == null) {
                throw _Exceptions.objectConstructorError();
            }

            final ObjectAccessor accessor = this.accessor;

            String propertyName;
            for (int i = 0; i < columnCount; i++) {
                propertyName = columnLabelArray[i];
                accessor.set(row, propertyName, record.get(i));
            }

            final R finalRow;
            if (this.immutableMap && row instanceof Map) {
                finalRow = (R) _Collections.unmodifiableMap((Map<String, Object>) row);
            } else {
                finalRow = row;
            }
            return finalRow;
        }


    } // MapRowReader


    private static final class TypeInfoReader {

        private final Map<String, TypeInfo> typeInfoMap;

        private String typeName;

        private TypeCategory category;

        private List<String> enumLabelList;

        private List<CompositeField> fieldList;

        private int order = -1;

        private TypeInfo.Builder typeInfoBuilder;

        private TypeInfoReader(Map<String, TypeInfo> typeInfoMap) {
            this.typeInfoMap = typeInfoMap;
        }

        Boolean readRow(final CurrentRecord record) {
            final String lastTypeName = this.typeName, typeName;
            typeName = record.getNonNull("typeName", String.class).toUpperCase(Locale.ROOT);

            if (lastTypeName == null) {
                receiveNewType(typeName, record);
            } else if (typeName.equals(lastTypeName)) {
                receiveCurrentType(record);
            } else {
                putLastType(lastTypeName);
                receiveNewType(typeName, record);
            }
            return Boolean.TRUE;
        }


        private void receiveCurrentType(final CurrentRecord record) {
            final TypeCategory typeCategory = record.getNonNull("category", TypeCategory.class);
            if (typeCategory != this.category) {
                throw stmtError();
            }
            final int lastOrder = this.order, currentOrder;
            switch (typeCategory) {
                case ENUM: {
                    final List<String> enumLabelList = Objects.requireNonNull(this.enumLabelList);
                    enumLabelList.add(record.getNonNull("enumLabel", String.class));
                    currentOrder = record.getNonNull("enumOrder", Integer.class);
                }
                break;
                case COMPOSITE: {
                    final List<CompositeField> fieldList = Objects.requireNonNull(this.fieldList);
                    final String labelName, collation;

                    labelName = record.getNonNull("comFieldName", String.class).toLowerCase(Locale.ROOT);
                    collation = record.getOrDefault("comFieldCollation", String.class, "").toLowerCase(Locale.ROOT);
                    final MappingType type = record.getNonNull("comFieldType", MappingType.class);

                    fieldList.add(CompositeFieldFactory.from( labelName, type, collation));
                    currentOrder = record.getNonNull("comFieldOrder", Integer.class);
                }
                break;
                default:
                    throw _Exceptions.unexpectedEnum(typeCategory);
            }
            if (currentOrder != lastOrder + 1) {
                throw stmtError();  // no bug,never here
            }

            this.order = currentOrder; // update
        }


        private void receiveNewType(final String typeName, final CurrentRecord record) {
            this.typeName = typeName;

            final TypeCategory typeCategory = record.getNonNull("category", TypeCategory.class);
            this.category = typeCategory;

            switch (typeCategory) {
                case ENUM: {
                    final List<String> enumLabelList;
                    this.enumLabelList = enumLabelList = new ArrayList<>();
                    enumLabelList.add(record.getNonNull("enumLabel", String.class));
                    this.order = record.getNonNull("enumOrder", Integer.class);
                }
                break;
                case COMPOSITE: {
                    final List<CompositeField> fieldList;
                    this.fieldList = fieldList = new ArrayList<>();
                    final String labelName, collation;

                    labelName = record.getNonNull("comFieldName", String.class);
                    final MappingType type = record.getNonNull("comFieldType", MappingType.class);
                    collation = record.getOrDefault("comFieldCollation", String.class, "");

                    fieldList.add(CompositeFieldFactory.from( labelName, type, collation));
                    this.order = record.getNonNull("comFieldOrder", Integer.class);
                }
                break;
                case RANGE: {
                    final TypeInfo typeInfo;
                    typeInfo = getTypeInfoBuilder()
                            .name(typeName)
                            .category(typeCategory)

                            .rangeSubType(record.get("rangeSubType", String.class))
                            .rangeCollation(record.get("rangeCollation", String.class))
                            .rangeMulti(record.get("rangeMulti", String.class))
                            .rangeSubOpc(record.get("rangeSubOpc", String.class))
                            .rangeCanonical(record.get("rangeCanonical", String.class))
                            .rangeSubDiff(record.get("rangeSubDiff", String.class))
                            .build();

                    putTypeInfo(typeInfo);
                    this.typeName = null;
                    this.category = null;
                    this.order = -1;
                }
                break;
                case DOMAIN: {
                    final TypeInfo typeInfo;
                    typeInfo = getTypeInfoBuilder()
                            .name(typeName)
                            .category(typeCategory)

                            .baseType(record.getNonNull("baseType", String.class))
                            .collation(record.get("collation", String.class))
                            .notNull(record.getNonNull("notNull", Boolean.class))
                            .defaultValue(record.get("default", String.class))
                            .constraintName(record.get("constraint", String.class))

                            .build();

                    putTypeInfo(typeInfo);
                    this.typeName = null;
                    this.category = null;
                    this.order = -1;
                }
                break;
                default:
                    throw _Exceptions.unexpectedEnum(typeCategory);
            }

        }


        private void putTypeInfo(final TypeInfo typeInfo) {
            if (this.typeInfoMap.putIfAbsent(typeInfo.name(), typeInfo) != null) {
                throw stmtError();  // no bug,never here
            }
        }


        private void putLastType(final String lastTypeName) {
            _Assert.isTrue(lastTypeName.equals(this.typeName), "");
            final TypeCategory typeCategory = Objects.requireNonNull(this.category);
            switch (typeCategory) {
                case ENUM: {
                    final List<String> enumLabelList = Objects.requireNonNull(this.enumLabelList);
                    final TypeInfo typeInfo;
                    typeInfo = getTypeInfoBuilder()
                            .name(lastTypeName)
                            .category(typeCategory)
                            .enumLabelList(enumLabelList)
                            .build();

                    putTypeInfo(typeInfo);

                    this.typeName = null;
                    this.category = null;
                    this.order = -1;
                    this.enumLabelList = null;
                }
                break;
                case COMPOSITE: {
                    final List<CompositeField> fieldList = Objects.requireNonNull(this.fieldList);

                    final TypeInfo typeInfo;
                    typeInfo = getTypeInfoBuilder()
                            .name(lastTypeName)
                            .category(typeCategory)
                            .compositeFieldList(fieldList)
                            .build();

                    putTypeInfo(typeInfo);

                    this.typeName = null;
                    this.category = null;
                    this.order = -1;
                    this.fieldList = null;
                }
                break;
                default:
                    throw _Exceptions.unexpectedEnum(typeCategory);
            }

        }

        private TypeInfo.Builder getTypeInfoBuilder() {
            TypeInfo.Builder builder = this.typeInfoBuilder;
            if (builder == null) {
                this.typeInfoBuilder = builder = TypeInfo.builder();
            }
            return builder;
        }


        private static MetaException stmtError() {
            return new MetaException("stmt error");
        }

    } // TypeInfoReader


}
