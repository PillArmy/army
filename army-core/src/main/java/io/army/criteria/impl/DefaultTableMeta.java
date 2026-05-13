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

import io.army.ArmyException;
import io.army.annotation.DiscriminatorValue;
import io.army.annotation.Inheritance;
import io.army.annotation.Table;
import io.army.lang.NonNull;
import io.army.lang.Nullable;
import io.army.meta.*;
import io.army.modelgen._MetaBridge;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/// @since 0.6.0
abstract class DefaultTableMeta<T> implements TableMeta<T> {

    static final Class<?> LOCK = DefaultTableMeta.class;


    private static final ConcurrentMap<Class<?>, DefaultTableMeta<?>> INSTANCE_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    static <T> TableMeta<T> getTableMeta(final Class<T> domainClass, @Nullable MetaContext context) {
        final TableMeta<T> cache = (TableMeta<T>) INSTANCE_MAP.get(domainClass);
        if (cache != null) {
            if (cache.javaType() != domainClass) {
                throw instanceMapError();
            }
            return cache;
        }
        if (domainClass.getAnnotation(Table.class) == null) {
            throw mappingError(TableMeta.class, domainClass);
        }
        final boolean newContext = context == null;
        if (newContext) {
            context = new DefaultMetaContext();
        }
        synchronized (DefaultTableMeta.LOCK) {
            final TableMeta<T> tableMeta;
            if (domainClass.getAnnotation(Inheritance.class) != null) {
                tableMeta = createParentTableMeta(domainClass, context);
            } else if (domainClass.getAnnotation(DiscriminatorValue.class) != null) {
                tableMeta = createChildTableMeta(domainClass, context);
            } else {
                tableMeta = createSimpleTableMeta(domainClass, context);
            }

            if (newContext) {
                context.clear();
            }
            return tableMeta;
        } // synchronized

    }


    private static <T> ParentTableMeta<T> getParentTableMeta(final Class<T> domainClass, final MetaContext context) {
        ParentTableMeta<T> parent;
        parent = getParentFromCache(domainClass);
        if (parent == null) {
            parent = createParentTableMeta(domainClass, context);
        }
        return parent;
    }


    @SuppressWarnings("unchecked")
    @Nullable
    private static <T> ParentTableMeta<T> getParentFromCache(final Class<T> domainClass) {
        final TableMeta<?> tableMeta = INSTANCE_MAP.get(domainClass);
        final ParentTableMeta<T> parent;
        if (tableMeta == null) {
            parent = null;
        } else if (tableMeta.javaType() != domainClass) {
            throw instanceMapError();
        } else if (tableMeta instanceof ParentTableMeta) {
            parent = (ParentTableMeta<T>) tableMeta;
        } else {
            throw mappingError(ParentTableMeta.class, domainClass);
        }
        return parent;
    }


    @SuppressWarnings("unchecked")
    private static <T> ParentTableMeta<T> createParentTableMeta(final Class<T> domainClass, final MetaContext context) {
        DefaultParentTable<T> parentTable;
        parentTable = new DefaultParentTable<>(domainClass, context);

        final DefaultTableMeta<?> cache;
        cache = INSTANCE_MAP.putIfAbsent(domainClass, parentTable);
        if (cache != null) {
            if (cache.javaType != domainClass) {
                throw instanceMapError();
            }
            parentTable = (DefaultParentTable<T>) cache;
        }
        return parentTable;
    }


    @SuppressWarnings("unchecked")
    private static <P, T extends P> ChildTableMeta<T> createChildTableMeta(final Class<T> domainClass, final MetaContext context) {
        final TableMetaUtils.DomainPair pair;
        pair = TableMetaUtils.mappedClassPair(domainClass);
        final Class<?> parentClass = pair.parent;
        if (parentClass == null) {
            String m = String.format("Not found parent domain for domain[%s].", domainClass.getName());
            throw new IllegalArgumentException(m);
        }
        DefaultChildTable<P, T> childTable;
        childTable = new DefaultChildTable<>(getParentTableMeta((Class<P>) parentClass, context), domainClass, context);


        final DefaultTableMeta<?> cache;
        cache = INSTANCE_MAP.putIfAbsent(domainClass, childTable);
        if (cache != null) {
            if (cache.javaType != domainClass) {
                throw instanceMapError();
            }
            childTable = (DefaultChildTable<P, T>) cache;
        }
        return childTable;
    }

    @SuppressWarnings("unchecked")
    private static <T> SimpleTableMeta<T> createSimpleTableMeta(final Class<T> domainClass, final MetaContext context) {
        DefaultSimpleTable<T> simpleTable;
        simpleTable = new DefaultSimpleTable<>(domainClass, context);

        final DefaultTableMeta<?> cache;
        cache = INSTANCE_MAP.putIfAbsent(domainClass, simpleTable);
        if (cache != null) {
            if (cache.javaType != domainClass) {
                throw instanceMapError();
            }
            simpleTable = (DefaultSimpleTable<T>) cache;
        }
        return simpleTable;

    }


    private static IllegalStateException instanceMapError() {
        return new IllegalStateException("INSTANCE_MAP state error.");
    }

    private static IllegalArgumentException mappingError(Class<?> tableMetaClass, Class<?> domainClass) {
        String m = String.format("Domain class %s couldn't mapping to %s.", domainClass.getName()
                , tableMetaClass.getName());
        return new IllegalArgumentException(m);
    }


    final Class<T> javaType;

    private final String tableName;

    private final boolean immutable;

    private final boolean allColumnNotNull;

    private final String comment;

    private final String tableOption;

    private final String partitionOption;

    private final SchemaMeta schemaMeta;

    final Map<String, FieldMeta<T>> fieldNameToFields;

    private final List<FieldMeta<T>> fieldList;

    private final List<IndexMeta<T>> indexMetaList;

    final PrimaryFieldMeta<T> primaryField;


    private final List<FieldMeta<?>> generatorChain;

    private DefaultTableMeta(final Class<T> domainClass, final MetaContext context) {
        Objects.requireNonNull(domainClass, "javaType required");
        this.javaType = domainClass;
        try {

            final Table table = domainClass.getAnnotation(Table.class);

            this.schemaMeta = _SchemaMetaFactory.getSchema(table.catalog(), table.schema());

            this.tableName = TableMetaUtils.tableName(table, this.schemaMeta, domainClass, context);
            this.comment = TableMetaUtils.tableComment(table, domainClass, context);
            this.immutable = TableMetaUtils.immutable(table, domainClass);
            this.allColumnNotNull = table.allColumnNotNull();

            this.tableOption = table.tableOptions();
            this.partitionOption = table.partitionOptions();

            this.fieldList = List.copyOf(TableMetaUtils.createFieldMetaList(this, context));
            this.fieldNameToFields = Map.copyOf(TableMetaUtils.createFieldMap(this.fieldList));
            this.indexMetaList = List.copyOf(TableMetaUtils.createIndexList(this, context, this.fieldNameToFields));
            this.generatorChain = List.copyOf(TableMetaUtils.createGeneratorChain(this.fieldNameToFields));

            this.primaryField = (PrimaryFieldMeta<T>) this.fieldNameToFields.get(_MetaBridge.ID);
            if (this.primaryField == null) {
                String m = String.format("Not found primary field meta in domain[%s]", domainClass.getName());
                throw new NullPointerException(m);
            }
        } catch (ArmyException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new MetaException(e.getMessage(), e);
        }

    }

    @Override
    public final Class<T> javaType() {
        return this.javaType;
    }

    @Override
    public final String objectName() {
        return this.tableName;
    }

    @Override
    public final String tableName() {
        return this.tableName;
    }

    @Override
    public final boolean immutable() {
        return this.immutable;
    }

    @Override
    public final boolean allColumnNotNull() {
        return this.allColumnNotNull;
    }

    @Override
    public final String comment() {
        return this.comment;
    }

    @Override
    public final PrimaryFieldMeta<T> id() {
        return this.primaryField;
    }


    @Override
    public final List<IndexMeta<T>> indexList() {
        return this.indexMetaList;
    }

    @Override
    public final List<FieldMeta<T>> fieldList() {
        return this.fieldList;
    }


    @Override
    public final String tableOptions() {
        return this.tableOption;
    }

    @Override
    public final String partitionOptions() {
        return this.partitionOption;
    }

    @Override
    public final SchemaMeta schema() {
        return this.schemaMeta;
    }

    @Override
    public final boolean containField(final String fieldName) {
        return this.fieldNameToFields.containsKey(fieldName);
    }

    @Override
    public final FieldMeta<T> field(final String fieldName) throws IllegalArgumentException {
        final FieldMeta<T> fieldMeta;
        fieldMeta = this.fieldNameToFields.get(fieldName);
        if (fieldMeta == null) {
            throw notFoundField(fieldName);
        }
        return fieldMeta;
    }


    @Override
    public final FieldMeta<T> tryField(String fieldName) {
        return this.fieldNameToFields.get(fieldName);
    }

    @Override
    public final boolean isThisField(final FieldMeta<?> field) {
        return field.tableMeta() == this;
    }

    @Override
    public final IndexFieldMeta<T> indexField(final String fieldName) {
        final FieldMeta<T> fieldMeta;
        fieldMeta = field(fieldName);
        if (!(fieldMeta instanceof IndexFieldMeta)) {
            String m = String.format("%s's %s[%s] java type not match", this
                    , IndexFieldMeta.class.getName(), fieldName);
            throw new IllegalArgumentException(m);
        }
        return (IndexFieldMeta<T>) fieldMeta;
    }

    @Override
    public final UniqueFieldMeta<T> uniqueField(final String fieldName) {
        final IndexFieldMeta<T> fieldMeta;
        fieldMeta = indexField(fieldName);
        if (!(fieldMeta instanceof UniqueFieldMeta)) {
            String m = String.format("%s's %s[%s] java type not match", this
                    , UniqueFieldMeta.class.getName(), fieldName);
            throw new IllegalArgumentException(m);
        }
        return (UniqueFieldMeta<T>) fieldMeta;
    }

    @Override
    public final ArrayFieldMeta<T> arrayField(String fieldName) {
        final FieldMeta<T> fieldMeta;
        fieldMeta = this.fieldNameToFields.get(fieldName);
        if (fieldMeta == null) {
            throw notFoundField(fieldName);
        }

        if (!(fieldMeta instanceof ArrayFieldMeta)) {
            String m = String.format("%s's %s[%s] java type not match", this
                    , ArrayFieldMeta.class.getName(), fieldName);
            throw new IllegalArgumentException(m);
        }
        return (ArrayFieldMeta<T>) fieldMeta;
    }

    @Override
    public final List<FieldMeta<?>> fieldChain() {
        return this.generatorChain;
    }


    @Override
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof DefaultTableMeta) {
            match = this.javaType == ((DefaultTableMeta<?>) obj).javaType;
        } else {
            match = false;
        }
        return match;
    }

    @Override
    public final int hashCode() {
        return this.javaType.hashCode();
    }

    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder();
        if (this instanceof ChildTableMeta) {
            builder.append(ChildTableMeta.class.getSimpleName());
        } else if (this instanceof ParentTableMeta) {
            builder.append(ParentTableMeta.class.getSimpleName());
        } else {
            builder.append(SimpleTableMeta.class.getSimpleName());
        }
        return builder.append('[')
                .append(this.javaType.getName())
                .append(']').toString();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    FieldMeta<? super T> tryGetReservedField(String fieldName) {
        final FieldMeta<? super T> field;
        if (this instanceof ChildTableMeta) {
            final DefaultTableMeta<? super T> parent;
            parent = (DefaultTableMeta<? super T>) ((ChildTableMeta<T>) this).parentMeta();
            field = parent.fieldNameToFields.get(fieldName);
        } else {
            field = this.fieldNameToFields.get(fieldName);
        }
        return field;
    }

    IllegalArgumentException notFoundField(String fieldName) {
        String m = String.format("%s's %s[%s] not found", this, FieldMeta.class.getName(), fieldName);
        return new IllegalArgumentException(m);
    }


    /*################################## blow static class ##################################*/

    private static abstract class DefaultSingleTableMeta<T> extends DefaultTableMeta<T>
            implements SingleTableMeta<T> {

        private DefaultSingleTableMeta(Class<T> domainClass, final MetaContext context) {
            super(domainClass, context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public final FieldMeta<T> createTime() {
            final FieldMeta<?> field;
            field = tryGetReservedField(_MetaBridge.CREATE_TIME);
            assert field != null;
            return (FieldMeta<T>) field;
        }

        @SuppressWarnings("unchecked")
        @Override
        public final FieldMeta<T> updateTime() {
            final FieldMeta<?> field;
            field = tryGetReservedField(_MetaBridge.UPDATE_TIME);
            if (field == null) {
                throw notFoundField(_MetaBridge.UPDATE_TIME);
            }
            return (FieldMeta<T>) field;
        }

        @SuppressWarnings("unchecked")
        @Override
        public final FieldMeta<T> version() {
            final FieldMeta<?> field;
            field = tryGetReservedField(_MetaBridge.VERSION);
            if (field == null) {
                throw notFoundField(_MetaBridge.VERSION);
            }
            return (FieldMeta<T>) field;
        }

        @SuppressWarnings("unchecked")
        @Override
        public final FieldMeta<T> visible() {
            final FieldMeta<?> field;
            field = tryGetReservedField(_MetaBridge.VISIBLE);
            if (field == null) {
                throw notFoundField(_MetaBridge.VISIBLE);
            }
            return (FieldMeta<T>) field;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public final FieldMeta<T> tryUpdateTime() {
            return (FieldMeta<T>) tryGetReservedField(_MetaBridge.UPDATE_TIME);
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public final FieldMeta<T> tryVersion() {
            return (FieldMeta<T>) tryGetReservedField(_MetaBridge.VERSION);
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public final FieldMeta<T> tryVisible() {
            return (FieldMeta<T>) tryGetReservedField(_MetaBridge.VISIBLE);
        }

    } // DefaultNonChildTableMeta

    private static final class DefaultSimpleTable<T> extends DefaultSingleTableMeta<T>
            implements SimpleTableMeta<T> {

        private DefaultSimpleTable(final Class<T> domainClass, final MetaContext context) {
            super(domainClass, context);
        }


        @Override
        public boolean containComplexField(final String fieldName) {
            return this.fieldNameToFields.containsKey(fieldName);
        }

        @Override
        public boolean isComplexField(final FieldMeta<?> field) {
            return field.tableMeta() == this;
        }

        @Override
        public FieldMeta<? super T> complexFiled(final String filedName) {
            final FieldMeta<? super T> field;
            field = this.fieldNameToFields.get(filedName);
            if (field == null) {
                throw notFoundComplexField(this, filedName);
            }
            return field;
        }

        @Override
        public FieldMeta<? super T> tryComplexFiled(String filedName) {
            return this.fieldNameToFields.get(filedName);
        }


        @Override
        public PrimaryFieldMeta<? super T> nonChildId() {
            return this.primaryField;
        }

        @Nullable
        @Override
        public FieldMeta<? super T> discriminator() {
            // always null
            return null;
        }

        @Nullable
        @Override
        public Enum<?> discriminatorValue() {
            // always null
            return null;
        }


    }

    private static final class DefaultParentTable<T> extends DefaultSingleTableMeta<T>
            implements ParentTableMeta<T> {

        private final TableFieldMeta<T> discriminator;

        private final Enum<?> discriminatorEnum;

        private DefaultParentTable(final Class<T> domainClass, final MetaContext context) {
            super(domainClass, context);
            this.discriminator = (TableFieldMeta<T>) TableMetaUtils.discriminator(this.fieldNameToFields, domainClass);
            this.discriminatorEnum = TableMetaUtils.discriminatorValue(this.discriminator.javaType, domainClass);
        }

        @Override
        public boolean containComplexField(final String fieldName) {
            return this.fieldNameToFields.containsKey(fieldName);
        }

        @Override
        public boolean isComplexField(final FieldMeta<?> field) {
            return field.tableMeta() == this;
        }

        @Override
        public FieldMeta<? super T> complexFiled(final String filedName) {
            final FieldMeta<? super T> field;
            field = this.fieldNameToFields.get(filedName);
            if (field == null) {
                throw notFoundComplexField(this, filedName);
            }
            return field;
        }

        @Override
        public FieldMeta<? super T> tryComplexFiled(String filedName) {
            return this.fieldNameToFields.get(filedName);
        }

        @Override
        public PrimaryFieldMeta<? super T> nonChildId() {
            return this.primaryField;
        }

        @Override
        public FieldMeta<T> discriminator() {
            return this.discriminator;
        }

        @Override
        public Enum<?> discriminatorValue() {
            return this.discriminatorEnum;
        }


    }

    private static final class DefaultChildTable<P, T extends P> extends DefaultTableMeta<T>
            implements ComplexTableMeta<P, T> {

        private final DefaultParentTable<P> parent;

        private final Enum<?> discriminatorEnum;

        private DefaultChildTable(final ParentTableMeta<P> parent, final Class<T> domainClass, final MetaContext context) {
            super(domainClass, context);
            TableMetaUtils.assertParentTableMeta(parent, domainClass);
            this.parent = (DefaultParentTable<P>) parent;
            this.discriminatorEnum = TableMetaUtils.discriminatorValue(this.parent.discriminator.javaType, domainClass);
        }


        @Override
        public FieldMeta<? super T> createTime() {
            final FieldMeta<? super T> field;
            field = tryGetReservedField(_MetaBridge.CREATE_TIME);
            assert field != null;
            return field;
        }

        @Override
        public FieldMeta<? super T> updateTime() {
            final FieldMeta<? super T> field;
            field = tryGetReservedField(_MetaBridge.UPDATE_TIME);
            if (field == null) {
                throw notFoundField(_MetaBridge.UPDATE_TIME);
            }
            return field;
        }

        @Override
        public FieldMeta<? super T> version() {
            final FieldMeta<? super T> field;
            field = tryGetReservedField(_MetaBridge.VERSION);
            if (field == null) {
                throw notFoundField(_MetaBridge.VERSION);
            }
            return field;
        }

        @Override
        public FieldMeta<? super T> visible() {
            final FieldMeta<? super T> field;
            field = tryGetReservedField(_MetaBridge.VISIBLE);
            if (field == null) {
                throw notFoundField(_MetaBridge.VISIBLE);
            }
            return field;
        }

        @Nullable
        @Override
        public FieldMeta<? super T> tryUpdateTime() {
            return tryGetReservedField(_MetaBridge.UPDATE_TIME);
        }

        @Nullable
        @Override
        public FieldMeta<? super T> tryVersion() {
            return tryGetReservedField(_MetaBridge.VERSION);
        }

        @Nullable
        @Override
        public FieldMeta<? super T> tryVisible() {
            return tryGetReservedField(_MetaBridge.VISIBLE);
        }

        @Override
        public boolean containComplexField(final String fieldName) {
            return this.fieldNameToFields.containsKey(fieldName) || this.parent.containField(fieldName);
        }

        @Override
        public boolean isComplexField(final FieldMeta<?> field) {
            final TableMeta<?> fieldTable;
            fieldTable = field.tableMeta();
            return fieldTable == this || fieldTable == this.parent;
        }

        @Override
        public FieldMeta<? super T> complexFiled(final String filedName) {
            FieldMeta<? super T> field;
            field = this.fieldNameToFields.get(filedName);
            if (field == null) {
                field = this.parent.fieldNameToFields.get(filedName);
                if (field == null) {
                    throw notFoundComplexField(this, filedName);
                }
            }
            return field;
        }

        @Override
        public FieldMeta<? super T> tryComplexFiled(String filedName) {
            FieldMeta<? super T> field;
            field = this.fieldNameToFields.get(filedName);
            if (field == null) {
                field = this.parent.fieldNameToFields.get(filedName);
            }
            return field;
        }

        @Override
        public PrimaryFieldMeta<? super T> nonChildId() {
            return this.parent.primaryField;
        }

        @NonNull
        @Override
        public FieldMeta<? super T> discriminator() {
            return this.parent.discriminator;
        }

        @Override
        public ParentTableMeta<P> parentMeta() {
            return this.parent;
        }

        @Override
        public Enum<?> discriminatorValue() {
            return this.discriminatorEnum;
        }


    }//DefaultChildTable


    private static IllegalArgumentException notFoundComplexField(TableMeta<?> table, String fieldName) {
        String m = String.format("Not found complex field[%s] in %s .", fieldName, table);
        return new IllegalArgumentException(m);
    }


}
