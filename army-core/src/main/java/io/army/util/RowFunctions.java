package io.army.util;

import io.army.bean.ObjectAccessor;
import io.army.bean.ObjectAccessorFactory;
import io.army.criteria.Selection;
import io.army.result.DataRecord;
import io.army.result.ResultRecordMeta;
import io.army.stmt.SingleSqlStmt;
import io.army.stmt.TwoStmtQueryStmt;
import io.army.type.ImmutableSpec;

import io.army.lang.Nullable;

import java.util.*;
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
        accessor = ObjectAccessorFactory.forBean(resultClass);
        final ObjectRowReader<R> reader;
        reader = new ObjectRowReader<>(ObjectAccessorFactory.beanConstructor(resultClass), accessor, true);
        return reader::readRow;
    }


    private static final class ObjectRowReader<R> {

        private final Object constructor;

        private final boolean immutableMap;

        private Class<?>[] columnClassArray;

        private String[] columnLabelArray;

        private ObjectAccessor accessor;

        private ObjectRowReader(Object constructor, @Nullable ObjectAccessor accessor, boolean immutableMap) {
            this.constructor = constructor;
            this.accessor = accessor;
            this.immutableMap = immutableMap;
        }


        @SuppressWarnings("unchecked")
        private R readRow(final DataRecord record) {
            final int columnCount = record.getColumnCount();

            String[] columnLabelArray = this.columnLabelArray;
            Class<?>[] columnClassArray = this.columnClassArray;

            if (columnLabelArray == null) {
                this.columnLabelArray = columnLabelArray = new String[columnCount];
                this.columnClassArray = columnClassArray = new Class<?>[columnCount];
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
            ObjectAccessor accessor = this.accessor;
            if (accessor == null) {
                this.accessor = accessor = ObjectAccessorFactory.fromInstance(row);
            }

            final ResultRecordMeta meta = record.getRecordMeta();

            String propertyName;
            Class<?> clumnClass;
            Object value;
            for (int i = 0; i < columnCount; i++) {
                propertyName = columnLabelArray[i];
                if (propertyName == null) {
                    columnLabelArray[i] = propertyName = record.getColumnLabel(i);
                }
                clumnClass = columnClassArray[i];

                if (clumnClass == null) {
                    if (row instanceof Map) {
                        clumnClass = meta.getMappingType(i).javaType();
                    } else {
                        clumnClass = accessor.getJavaType(propertyName);
                    }
                    columnClassArray[i] = clumnClass;
                }

                value = record.get(i, clumnClass);

                if (value == null && clumnClass.isPrimitive()) {
                    throw _Exceptions.primitiveNullColumn(propertyName, clumnClass);
                }
                accessor.set(row, propertyName, value);
            }

            final R finalRow;
            if (row instanceof Map && (this.immutableMap || row instanceof ImmutableSpec)) {
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
