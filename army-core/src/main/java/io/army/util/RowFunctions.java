package io.army.util;

import io.army.criteria.Selection;
import io.army.executor.ExecutorSupport;
import io.army.lang.Nullable;
import io.army.pojo.ObjectAccessor;
import io.army.pojo.ObjectAccessorFactory;
import io.army.result.DataRecord;
import io.army.result.RecordMeta;
import io.army.stmt.SingleSqlStmt;
import io.army.stmt.TwoStmtQueryStmt;
import io.army.type.ImmutableSpec;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public abstract class RowFunctions {

    private RowFunctions() {
    }


    public static Function<DataRecord, Map<String, Object>> hashMapRowFunc(boolean immutableMap) {
        final IntFunction<Map<String, Object>> constructor;
        constructor = _Collections::hashMap;

        final MapRowReader<Map<String, Object>> reader;
        reader = new MapRowReader<>(constructor, immutableMap);
        return reader::readRow;
    }


    public static Function<DataRecord, Map<String, Object>> treeMapRowFunc(boolean immutableMap) {
        final Supplier<Map<String, Object>> constructor;
        constructor = RowMaps::treeMap;

        final MapRowReader<Map<String, Object>> reader;
        reader = new MapRowReader<>(constructor, immutableMap);
        return reader::readRow;
    }

    public static Function<DataRecord, Map<String, Object>> linkedHashMapRowFunc(boolean immutableMap) {
        final Supplier<Map<String, Object>> constructor;
        constructor = RowMaps::linkedHashMap;

        final MapRowReader<Map<String, Object>> reader;
        reader = new MapRowReader<>(constructor, immutableMap);
        return reader::readRow;
    }


    public static <R> Function<DataRecord, R> objectRowFunc(final Supplier<R> constructor, boolean immutableMap) {
        Objects.requireNonNull(constructor);
        final ObjectRowReader<R> reader;
        reader = new ObjectRowReader<>(constructor, null, immutableMap);
        return reader::readRow;
    }


    public static <R> Function<DataRecord, R> mapRowFunc(final IntFunction<R> constructor, boolean immutableMap) {
        Objects.requireNonNull(constructor);
        final MapRowReader<R> reader;
        reader = new MapRowReader<>(constructor, immutableMap);
        return reader::readRow;
    }

    public static <R> Function<DataRecord, R> classRowFunc(final Class<R> resultClass, final SingleSqlStmt stmt) {
        final Function<DataRecord, R> rowFunc;
        final List<? extends Selection> selectionList = stmt.selectionList();
        if ((stmt instanceof TwoStmtQueryStmt && ((TwoStmtQueryStmt) stmt).maxColumnSize() == 1)
                || selectionList.size() == 1) {
            rowFunc = record -> record.get(0, resultClass);
        } else {
            rowFunc = RowFunctions.beanRowFunc(resultClass);
        }
        return rowFunc;
    }


    public static <R> Function<DataRecord, R> beanRowFunc(final Class<R> resultClass) {

        final ObjectAccessor accessor;
        accessor = ObjectAccessorFactory.forPojo(resultClass);
        final ObjectRowReader<R> reader;
        reader = new ObjectRowReader<>(ObjectAccessorFactory.beanConstructor(resultClass), accessor, true);
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
        private R readRow(final DataRecord record) {
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


}
