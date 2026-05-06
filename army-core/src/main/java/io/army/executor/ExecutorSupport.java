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

package io.army.executor;

import io.army.ArmyException;
import io.army.criteria.CriteriaException;
import io.army.criteria.Selection;
import io.army.criteria.TypeInfer;
import io.army.env.ArmyKey;
import io.army.env.SqlLogMode;
import io.army.lang.Nullable;
import io.army.mapping.MappingType;
import io.army.mapping.NoMatchMappingException;
import io.army.mapping.NullType;
import io.army.meta.MetaException;
import io.army.meta.TypeMeta;
import io.army.option.Option;
import io.army.pojo.ObjectAccessException;
import io.army.pojo.ObjectAccessor;
import io.army.pojo.ObjectAccessorFactory;
import io.army.result.*;
import io.army.session.Session;
import io.army.session.StmtOption;
import io.army.sqltype.ArmyType;
import io.army.sqltype.DataType;
import io.army.sqltype.MySQLType;
import io.army.sqltype.SQLType;
import io.army.stmt.DeclareCursorStmt;
import io.army.transaction.Isolation;
import io.army.util.ClassUtils;
import io.army.util._Collections;
import io.army.util._Exceptions;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ExecutorSupport {


    public static final ObjectAccessor SINGLE_COLUMN_PSEUDO_ACCESSOR = new PseudoWriterAccessor();

    public static final ObjectAccessor RECORD_PSEUDO_ACCESSOR = new PseudoWriterAccessor();


    protected ExecutorSupport() {

    }


    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }


    protected final SqlLogMode readSqlLogMode(ExecutorFactorySupport factory) {
        final SqlLogMode mode;
        if (factory.sqlLogDynamic) {
            mode = factory.armyEnv.getOrDefault(ArmyKey.SQL_LOG_MODE);
        } else {
            mode = factory.sqlLogMode;
        }
        return mode;
    }


    protected final void printSqlIfNeed(final ExecutorFactorySupport factory, final String sessionName, final Logger log,
                                        final String sql) {
        final SqlLogMode mode;
        if (factory.sqlLogDynamic) {
            mode = factory.armyEnv.getOrDefault(ArmyKey.SQL_LOG_MODE);
        } else {
            mode = factory.sqlLogMode;
        }

        final String format = "session[name : {} , executorHash : {}]\n\n{}\n";
        switch (mode) {
            case OFF:
                break;
            case SIMPLE:
            case BEAUTIFY:
                log.info(format, sessionName, System.identityHashCode(this), sql);
                break;
            case DEBUG:
            case BEAUTIFY_DEBUG: {
                if (log.isDebugEnabled()) {
                    log.debug(format, sessionName, System.identityHashCode(this), sql);
                }
            }
            break;
            default:
                throw _Exceptions.unexpectedEnum(mode);
        }

    }

    protected final ArmyException unsupportedIsolation(Isolation isolation) {
        return new ArmyException(String.format("%s don't support %s", this, isolation));
    }


    public static MappingType compatibleTypeFrom(final TypeInfer infer, final DataType dataType,
                                                 final @Nullable Class<?> resultClass,
                                                 final ObjectAccessor accessor, final String fieldName)
            throws NoMatchMappingException {
        final MappingType type;
        if (infer instanceof MappingType) {
            type = (MappingType) infer;
        } else if (infer instanceof TypeMeta) {
            type = ((TypeMeta) infer).mappingType();
        } else {
            final TypeMeta meta = infer.typeMeta();
            if (meta instanceof MappingType) {
                type = (MappingType) meta;
            } else {
                type = meta.mappingType();
            }
        }

        final MappingType compatibleType;
        if (accessor == SINGLE_COLUMN_PSEUDO_ACCESSOR) {
            assert resultClass != null;
            if (resultClass.isAssignableFrom(type.javaType())) {
                compatibleType = type;
            } else {
                compatibleType = type.compatibleFor(dataType, resultClass);
            }
        } else if (accessor == RECORD_PSEUDO_ACCESSOR) {
            assert resultClass != null;
            compatibleType = type.compatibleFor(dataType, resultClass);
        } else if (accessor == ObjectAccessorFactory.MAP_ACCESSOR) {
            compatibleType = type;
        } else if (type == NullType.INSTANCE || accessor.isWritable(fieldName, type.javaType())) {
            compatibleType = type;
        } else {
            compatibleType = type.compatibleFor(dataType, accessor.getJavaType(fieldName));
        }
        return compatibleType;
    }

    @SuppressWarnings("unchecked")
    protected static <R> Class<R> rowResultClass(R row) {
        final Class<?> resultClass;
        if (row instanceof Map) {
            resultClass = Map.class;
        } else {
            resultClass = row.getClass();
        }
        return (Class<R>) resultClass;
    }


    /// @return a unmodified map
    protected static Map<String, Integer> createAliasToIndexMap(final List<? extends Selection> selectionList) {
        final int selectionSize = selectionList.size();
        Map<String, Integer> map = _Collections.hashMap((int) (selectionSize / 0.75f));
        for (int i = 0; i < selectionSize; i++) {
            map.put(selectionList.get(i).label(), i); // If alias duplication,then override.
        }
        return _Collections.unmodifiableMap(map);
    }

    protected static Map<String, Selection> createLabelToSelectionMap(final List<? extends Selection> selectionList) {
        final int selectionSize = selectionList.size();
        final Map<String, Selection> map = _Collections.hashMapForSize(selectionSize);
        Selection selection;
        for (int i = 0; i < selectionSize; i++) {
            selection = selectionList.get(i);
            map.put(selection.label(), selection); // If alias duplication,then override.
        }
        return _Collections.unmodifiableMap(map);
    }

    /// This method is designed for second query,so :
    /// 
    /// - resultList should be {@link java.util.ArrayList}
    /// - If accessor is {@link ExecutorSupport#SINGLE_COLUMN_PSEUDO_ACCESSOR} ,then resultList representing single column row
    /// 
    protected static <R> Map<Object, R> createIdToRowMap(final List<R> resultList, final String idFieldName,
                                                         final ObjectAccessor accessor) {
        final int rowSize = resultList.size();
        final Map<Object, R> map = _Collections.hashMap((int) (rowSize / 0.75f));
        final boolean singleColumnRow = accessor == SINGLE_COLUMN_PSEUDO_ACCESSOR;

        Object id;
        R row;
        for (int i = 0; i < rowSize; i++) {

            row = resultList.get(i);

            if (row == null) {
                // no bug,never here
                throw new NullPointerException(String.format("%s row is null", i + 1));
            }

            if (singleColumnRow) {
                id = row;
            } else {
                id = accessor.get(row, idFieldName);
            }

            if (id == null) {
                // no bug,never here
                throw new NullPointerException(String.format("%s row id is null", i + 1));
            }

            if (map.putIfAbsent(id, row) != null) {
                throw new CriteriaException(String.format("%s row id[%s] duplication", i + 1, id));
            }

        } // for loop


        return _Collections.unmodifiableMap(map);
    }

    protected static <T> T convertToTarget(final Object source, final Class<T> targetClass) {
        throw new UnsupportedOperationException();
    }


    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/data-types.html">MySQL Data Types</a>
    protected static MySQLType getMySqlType(final String typeName, final MappingType[] typeArray, final int index) {
        throw new UnsupportedOperationException();
    }


    protected static DataType getPostgreType(final String typeName, final MappingType[] typeArray, final int index) {
        throw new UnsupportedOperationException();
    }


    /// @see <a href="https://sqlite.org/datatype3.html">Datatypes In SQLite</a>
    /// @see <a href="https://sqlite.org/datatypes.html">Datatypes In SQLite Version 2</a>
    protected static DataType getSQLiteType(final String typeName, final MappingType[] typeArray, final int index) {
        throw new UnsupportedOperationException();
    }

    protected static Consumer<ResultStates> combineConsumer(final Consumer<ResultStates> consumer, final StmtOption stmtOption) {
        final Consumer<ResultStates> consumerOfOption, finalConsumer;
        consumerOfOption = stmtOption.stateConsumer();

        if (consumer == ResultStates.IGNORE_STATES) {
            finalConsumer = consumerOfOption;
        } else if (consumerOfOption == ResultStates.IGNORE_STATES) {
            finalConsumer = consumer;
        } else {
            finalConsumer = consumerOfOption.andThen(consumer);
        }
        return finalConsumer;
    }

    /*-------------------below Exception  -------------------*/

    protected static IllegalArgumentException notInTransactionAndChainConflict() {
        String m = String.format("session not in transaction block,don't support %s option", Option.CHAIN);
        return new IllegalArgumentException(m);
    }


    protected static NullPointerException currentRecordColumnIsNull(int indexBasedZero, String columnLabel) {
        String m = String.format("value is null of current record index[%s] column label[%s] ",
                indexBasedZero, columnLabel);
        return new NullPointerException(m);
    }

    protected static NullPointerException currentRecordDefaultValueNonNull() {
        return new NullPointerException("current record default must non-null");
    }

    protected static NullPointerException currentRecordSupplierReturnNull(Supplier<?> supplier) {
        String m = String.format("current record %s %s return null", Supplier.class.getName(), supplier);
        return new NullPointerException(m);
    }


    protected static DataAccessException secondQueryRowCountNotMatch(final int firstRowCount, final int secondRowCount) {
        String m = String.format("second query row count[%s] and first query row[%s] not match.",
                secondRowCount, firstRowCount);
        return new DataAccessException(m);
    }

    protected static DataAccessException transactionExistsRejectStart(String sessionName) {
        String m = String.format("Session[%s] in transaction ,reject start a new transaction before commit or rollback.", sessionName);
        return new DataAccessException(m);
    }

    protected static DataAccessException unknownIsolation(String isolation) {
        String m = String.format("unknown isolation %s", isolation);
        return new DataAccessException(m);
    }

    public static MetaException mapMethodError(MappingType type, DataType dataType) {
        String m = String.format("%s map(ServerMeta) method error,return %s ", type.getClass(), dataType);
        return new MetaException(m);
    }

    public static DataAccessException driverError() {
        // driver no bug,never here
        return new DataAccessException("driver error");
    }

    public static MetaException beforeBindMethodError(MappingType type, DataType dataType,
                                                      @Nullable Object returnValue) {
        String m = String.format("%s beforeBind() method return type %s and %s type not match.",
                type.getClass().getName(), ClassUtils.safeClassName(returnValue), dataType);
        return new MetaException(m);
    }

    public static MetaException afterGetMethodError(MappingType type, DataType dataType,
                                                    @Nullable Object returnValue) {
        String m = String.format("%s afterGet() method return type %s and %s type not match.",
                type.getClass().getName(), ClassUtils.safeClassName(returnValue), dataType);
        return new MetaException(m);
    }


    public static ArmyException executorFactoryClosed(ExecutorFactory factory) {
        String m = String.format("%s have closed.", factory);
        return new ArmyException(m);
    }


    protected static abstract class ArmyResultRecordMeta implements RecordMeta {

        private final int resultNo;

        protected final DataType[] dataTypeArray;

        protected final MappingType[] typeArray;

        final Function<Class<?>, Function<Object, ?>> converterFunc;

        protected ArmyResultRecordMeta(int resultNo, DataType[] dataTypeArray, MappingType[] typeArray, ExecutorEnv env) {
            assert resultNo > 0;
            this.resultNo = resultNo;
            this.dataTypeArray = dataTypeArray;
            this.typeArray = typeArray;
            this.converterFunc = env.converterFunc();
        }

        @Override
        public final int resultNo() {
            return this.resultNo;
        }

        @Override
        public final int getColumnCount() {
            return this.dataTypeArray.length;
        }

        @Override
        public final DataType getDataType(int indexBasedZero) throws DataAccessException {
            return this.dataTypeArray[checkIndex(indexBasedZero)];
        }

        @Override
        public final MappingType getMappingType(int indexBasedZero) {
            return this.typeArray[checkIndex(indexBasedZero)];
        }

        @Override
        public final <T> T getNonNullOf(int indexBasedZero, Option<T> option) throws DataAccessException {
            final T value;
            value = getOf(indexBasedZero, option);
            if (value == null) {
                throw new NullPointerException();
            }
            return value;
        }

        @Override
        public final ArmyType getArmyType(final int indexBasedZero) throws DataAccessException {
            final DataType dataType;
            dataType = this.dataTypeArray[checkIndex(indexBasedZero)];
            final ArmyType armyType;
            if (dataType instanceof SQLType) {
                armyType = ((SQLType) dataType).armyType();
            } else {
                armyType = ArmyType.UNKNOWN;
            }
            return armyType;
        }



        /*-------------------below label methods -------------------*/

        @Override
        public final MappingType getMappingType(String columnLabel) {
            return getMappingType(getColumnIndex(columnLabel));
        }

        @Override
        public final Selection getSelection(String columnLabel) throws DataAccessException {
            return getSelection(getColumnIndex(columnLabel));
        }

        @Override
        public final DataType getDataType(String columnLabel) throws DataAccessException {
            return getDataType(getColumnIndex(columnLabel));
        }

        @Override
        public final ArmyType getArmyType(String columnLabel) throws DataAccessException {
            return getArmyType(getColumnIndex(columnLabel));
        }

        @Nullable
        @Override
        public final <T> T getOf(String columnLabel, Option<T> option) throws DataAccessException {
            return getOf(getColumnIndex(columnLabel), option);
        }

        @Override
        public final <T> T getNonNullOf(String columnLabel, Option<T> option) throws DataAccessException {
            return getNonNullOf(getColumnIndex(columnLabel), option);
        }

        @Nullable
        @Override
        public final String getCatalogName(String columnLabel) throws DataAccessException {
            return getCatalogName(getColumnIndex(columnLabel));
        }

        @Nullable
        @Override
        public final String getSchemaName(String columnLabel) throws DataAccessException {
            return getSchemaName(getColumnIndex(columnLabel));
        }

        @Nullable
        @Override
        public final String getTableName(String columnLabel) throws DataAccessException {
            return getTableName(getColumnIndex(columnLabel));
        }

        @Nullable
        @Override
        public final String getColumnName(String columnLabel) throws DataAccessException {
            return getColumnName(getColumnIndex(columnLabel));
        }

        @Override
        public final int getPrecision(String columnLabel) throws DataAccessException {
            return getPrecision(getColumnIndex(columnLabel));
        }

        @Override
        public final int getScale(String columnLabel) throws DataAccessException {
            return getScale(getColumnIndex(columnLabel));
        }

        @Override
        public final FieldType getFieldType(String columnLabel) throws DataAccessException {
            return getFieldType(getColumnIndex(columnLabel));
        }

        @Nullable
        @Override
        public final Boolean getAutoIncrementMode(String columnLabel) throws DataAccessException {
            return getAutoIncrementMode(getColumnIndex(columnLabel));
        }

        @Override
        public final KeyType getKeyMode(String columnLabel) throws DataAccessException {
            return getKeyMode(getColumnIndex(columnLabel));
        }

        @Nullable
        @Override
        public final Boolean getNullableMode(String columnLabel) throws DataAccessException {
            return getNullableMode(getColumnIndex(columnLabel));
        }

        @Override
        public final Class<?> getFirstJavaType(String columnLabel) throws DataAccessException {
            return getFirstJavaType(getColumnIndex(columnLabel));
        }

        @Nullable
        @Override
        public final Class<?> getSecondJavaType(String columnLabel) throws DataAccessException {
            return getSecondJavaType(getColumnIndex(columnLabel));
        }

        public final int checkIndex(final int indexBasedZero) {
            if (indexBasedZero < 0 || indexBasedZero >= this.dataTypeArray.length) {
                String m = String.format("index not in [0,%s)", this.dataTypeArray.length);
                throw new DataAccessException(m);
            }
            return indexBasedZero;
        }

        public final int checkIndexAndToBasedOne(final int indexBasedZero) {
            if (indexBasedZero < 0 || indexBasedZero >= this.dataTypeArray.length) {
                String m = String.format("index not in [0,%s)", this.dataTypeArray.length);
                throw new DataAccessException(m);
            }
            return indexBasedZero + 1;
        }

    } // ArmyResultRecordMeta


    private static abstract class ArmyDataRecord implements DataRecord {

        @Override
        public abstract ArmyResultRecordMeta getRecordMeta();

        @Override
        public final int resultNo() {
            return getRecordMeta().resultNo();
        }


        @Override
        public final int getColumnCount() {
            return getRecordMeta().getColumnCount();
        }

        @Override
        public final String getColumnLabel(int indexBasedZero) throws IllegalArgumentException {
            return getRecordMeta().getColumnLabel(indexBasedZero);
        }

        @Override
        public final int getColumnIndex(String columnLabel) throws IllegalArgumentException {
            return getRecordMeta().getColumnIndex(columnLabel);
        }

        @Override
        public final Object getNonNull(final int indexBasedZero) {
            final Object value;
            value = get(indexBasedZero);
            if (value == null) {
                throw currentRecordColumnIsNull(indexBasedZero, getColumnLabel(indexBasedZero));
            }
            return value;
        }

        @Override
        public final Object getOrDefault(int indexBasedZero, @Nullable Object defaultValue) {
            if (defaultValue == null) {
                throw currentRecordDefaultValueNonNull();
            }
            Object value;
            value = get(indexBasedZero);
            if (value == null) {
                value = defaultValue;
            }
            return value;
        }

        @Override
        public final Object getOrSupplier(int indexBasedZero, Supplier<?> supplier) {
            Object value;
            value = get(indexBasedZero);
            if (value == null) {
                if ((value = supplier.get()) == null) {
                    throw currentRecordSupplierReturnNull(supplier);
                }
            }
            return value;
        }


        @Override
        public final <T> T getNonNull(int indexBasedZero, Class<T> columnClass) {
            final T value;
            value = get(indexBasedZero, columnClass);
            if (value == null) {
                throw currentRecordColumnIsNull(indexBasedZero, getColumnLabel(indexBasedZero));
            }
            return value;
        }

        @Override
        public final <T> T getOrDefault(int indexBasedZero, Class<T> columnClass, final @Nullable T defaultValue) {
            if (defaultValue == null) {
                throw currentRecordDefaultValueNonNull();
            }
            T value;
            value = get(indexBasedZero, columnClass);
            if (value == null) {
                value = defaultValue;
            }
            return value;
        }

        @Override
        public final <T> T getOrSupplier(int indexBasedZero, Class<T> columnClass, Supplier<T> supplier) {
            T value;
            value = get(indexBasedZero, columnClass);
            if (value == null) {
                if ((value = supplier.get()) == null) {
                    throw currentRecordSupplierReturnNull(supplier);
                }
            }
            return value;
        }

        /*-------------------below label methods -------------------*/

        @Override
        public final Object get(String columnLabel) {
            return get(getRecordMeta().getColumnIndex(columnLabel));
        }

        @Override
        public final Object getNonNull(String columnLabel) {
            return getNonNull(getRecordMeta().getColumnIndex(columnLabel));
        }

        @Override
        public final Object getOrDefault(String columnLabel, Object defaultValue) {
            return getOrDefault(getRecordMeta().getColumnIndex(columnLabel), defaultValue);
        }

        @Override
        public final Object getOrSupplier(String columnLabel, Supplier<?> supplier) {
            return getOrSupplier(getRecordMeta().getColumnIndex(columnLabel), supplier);
        }

        @Nullable
        @Override
        public final <T> T get(String columnLabel, Class<T> columnClass) {
            return get(getRecordMeta().getColumnIndex(columnLabel), columnClass);
        }

        @Override
        public final <T> T getNonNull(String columnLabel, Class<T> columnClass) {
            return getNonNull(getRecordMeta().getColumnIndex(columnLabel), columnClass);
        }

        @Override
        public final <T> T getOrDefault(String columnLabel, Class<T> columnClass, T defaultValue) {
            return getOrDefault(getRecordMeta().getColumnIndex(columnLabel), columnClass, defaultValue);
        }

        @Override
        public final <T> T getOrSupplier(String columnLabel, Class<T> columnClass, Supplier<T> supplier) {
            return getOrSupplier(getRecordMeta().getColumnIndex(columnLabel), columnClass, supplier);
        }


    } // ArmyDataRecord


    private static abstract class ArmyStmtDataRecord extends ArmyDataRecord {

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public final <T> T get(int indexBasedZero, Class<T> columnClass) {
            Object value;
            value = get(indexBasedZero);
            if (value == null || columnClass.isInstance(value)) {
                return (T) value;
            }
            final Function<Class<?>, Function<Object, ?>> converterFunc;
            converterFunc = this.getRecordMeta().converterFunc;
            final Function<Object, ?> convertor;
            if (converterFunc == null) {
                convertor = null;
            } else {
                convertor = converterFunc.apply(columnClass);
            }

            if (convertor == null) {
                value = convertToTarget(value, columnClass);
            } else {
                try {
                    value = convertor.apply(value);
                } catch (Exception e) {
                    throw new DataAccessException("user custom convertor occur error.", e);
                }
                if (value != null && !columnClass.isInstance(value)) {
                    String m = String.format("user custom convertor don't return %s type.", columnClass.getName());
                    throw new DataAccessException(m);
                }
            }
            return (T) value;
        }

    } // ArmyStmtDataRecord


    protected static abstract class ArmyDriverCurrentRecord extends ArmyDataRecord implements CurrentRecord {

        @Override
        public abstract ArmyResultRecordMeta getRecordMeta();

        @Override
        public final ResultRecord asResultRecord() {
            return new ArmyResultRecord(this);
        }

        protected abstract Object[] copyValueArray();


    } // ArmyDriverCurrentRecord


    protected static abstract class ArmyStmtCurrentRecord extends ArmyDataRecord implements CurrentRecord {


        @Override
        public abstract ArmyResultRecordMeta getRecordMeta();

        @Override
        public final ResultRecord asResultRecord() {
            return new ArmyResultRecord(this);
        }


        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public final <T> T get(int indexBasedZero, Class<T> columnClass, MappingType type) {
            final Object value;
            value = get(indexBasedZero, type);

            if (value != null && !columnClass.isInstance(value)) {
                String m = String.format("%s and %s not match", columnClass.getName(), type.getClass().getName());
                throw new IllegalArgumentException(m);
            }
            return (T) value;
        }

        @Override
        public final <T> T getNonNull(int indexBasedZero, Class<T> columnClass, MappingType type) {
            final T value;
            value = get(indexBasedZero, columnClass, type);
            if (value == null) {
                throw currentRecordColumnIsNull(indexBasedZero, getColumnLabel(indexBasedZero));
            }
            return value;
        }

        @Override
        public final <T> T getOrDefault(int indexBasedZero, Class<T> columnClass, MappingType type,
                                        final @Nullable T defaultValue) {
            if (defaultValue == null) {
                throw currentRecordDefaultValueNonNull();
            }
            T value;
            value = get(indexBasedZero, columnClass, type);
            if (value == null) {
                value = defaultValue;
            }
            return value;
        }

        @Override
        public final <T> T getOrSupplier(int indexBasedZero, Class<T> columnClass, MappingType type, Supplier<T> supplier) {
            T value;
            value = get(indexBasedZero, columnClass, type);
            if (value == null) {
                value = supplier.get();
                if (value == null) {
                    throw currentRecordSupplierReturnNull(supplier);
                }
            }
            return value;
        }

        protected abstract Object[] copyValueArray();


    }// ArmyStmtCurrentRecord


    private static final class ArmyResultRecord extends ArmyStmtDataRecord implements ResultRecord {


        private final ArmyResultRecordMeta meta;

        private final Object[] valueArray;

        private ArmyResultRecord(ArmyStmtCurrentRecord currentRecord) {
            this.meta = currentRecord.getRecordMeta();
            this.valueArray = currentRecord.copyValueArray();
            assert this.valueArray.length == this.meta.getColumnCount();
        }

        private ArmyResultRecord(ArmyDriverCurrentRecord currentRecord) {
            this.meta = currentRecord.getRecordMeta();
            this.valueArray = currentRecord.copyValueArray();
            assert this.valueArray.length == this.meta.getColumnCount();
        }

        @Override
        public ArmyResultRecordMeta getRecordMeta() {
            return this.meta;
        }

        @Override
        public Object get(int indexBasedZero) {
            return this.valueArray[this.meta.checkIndex(indexBasedZero)];
        }

    }// ArmyResultRecord


    private static final class PseudoWriterAccessor implements ObjectAccessor {

        @Override
        public boolean isWritable(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isWritable(int index, Class<?> valueType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Object target, int index, @Nullable Object value) throws ObjectAccessException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getIndex(String propertyName) {
            return -1;
        }

        @Override
        public boolean isReadable(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(Object target, int index) throws ObjectAccessException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Class<?> getJavaType(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isWritable(String propertyName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isWritable(String propertyName, Class<?> valueType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Class<?> getJavaType(String propertyName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Object target, String propertyName, @Nullable Object value) throws ObjectAccessException {
            throw new UnsupportedOperationException();
        }


        @Override
        public Set<String> writablePropertySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> readablePropertySet() {
            throw new UnsupportedOperationException();
        }


        @Override
        public boolean isReadable(String propertyName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(Object target, String propertyName) throws ObjectAccessException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Class<?> getAccessedType() {
            throw new UnsupportedOperationException();
        }

    }// PseudoWriterAccessor


    protected static abstract class ArmyStmtCursor implements StmtCursor {

        public final DeclareCursorStmt stmt;

        protected final Session session;

        public final Function<Option<?>, ?> sessionFunc;

        public final List<? extends Selection> selectionList;

        private Map<String, Selection> selectionMap;

        protected ArmyStmtCursor(DeclareCursorStmt stmt, Function<Option<?>, ?> sessionFunc) {
            this.stmt = stmt;
            this.sessionFunc = sessionFunc;
            this.session = (Session) sessionFunc.apply(Option.ARMY_SESSION);
            assert this.session != null;
            this.selectionList = stmt.selectionList();
        }


        @Override
        public final String name() {
            return this.stmt.cursorName();
        }

        @Override
        public final String safeName() {
            return this.stmt.safeCursorName();
        }

        @Override
        public final List<? extends Selection> selectionList() {
            return this.selectionList;
        }

        @Override
        public final Selection selection(final int indexBasedZero) {
            if (indexBasedZero < 0 || indexBasedZero >= this.selectionList.size()) {
                String m = String.format("index[%s] not in [0,%s)", indexBasedZero, this.selectionList.size());
                throw new IllegalArgumentException(m);
            }
            return this.selectionList.get(indexBasedZero);
        }

        @Override
        public final Selection selection(final String name) {
            Map<String, Selection> map = this.selectionMap;
            if (map == null) {
                this.selectionMap = map = createLabelToSelectionMap(this.selectionList);
            }
            return map.get(name);
        }


    } // ArmyStmtCursor


}
