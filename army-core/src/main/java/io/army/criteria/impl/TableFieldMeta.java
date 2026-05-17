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
import io.army.annotation.*;
import io.army.criteria.Expression;
import io.army.criteria.TableField;
import io.army.criteria.Visible;
import io.army.criteria.impl.inner._Selection;
import io.army.dialect._Constant;
import io.army.dialect._SqlContext;
import io.army.generator.FieldGenerator;
import io.army.generator.GeneratorStrategy;
import io.army.generator.snowflake.Snowflake8Generator;
import io.army.lang.Nullable;
import io.army.mapping.ArrayMappingType;
import io.army.mapping.MappingType;
import io.army.mapping.MultiGenericsMappingType;
import io.army.mapping._MappingFactory;
import io.army.meta.*;
import io.army.modelgen._MetaBridge;
import io.army.util._Assert;
import io.army.util._Exceptions;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/// @since 0.6.0
abstract class TableFieldMeta<T> extends OperationTypedField implements FieldMeta<T>, _Selection {


    private static final ConcurrentMap<TableFieldMeta<?>, TableFieldMeta<?>> INSTANCE_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentMap<FieldMeta<?>, Boolean> CODEC_MAP = new ConcurrentHashMap<>();

    /// @see DefaultTableMeta#getTableMeta(Class, MetaContext)
    @SuppressWarnings("unchecked")
    static <T> FieldMeta<T> createFieldMeta(final TableMeta<T> table, final Field field, final MetaContext context) {
        final String fieldName = field.getName();

        final MappingType type;
        type = _MappingFactory.map(table.javaType(), field, context);

        final boolean idField = fieldName.equals(_MetaBridge.ID);

        final boolean arrayType;
        arrayType = !idField && type instanceof ArrayMappingType;

        final DefaultSimpleFieldMeta<T> fieldMeta;
        if (idField) {
            fieldMeta = new DefaultPrimaryFieldMeta<>(table, field, type, context);
        } else if (arrayType) {
            fieldMeta = new DefaultArrayFieldMeta<>(table, field, type, context);
        } else {
            fieldMeta = new DefaultSimpleFieldMeta<>(table, field, type, context);
        }
        final TableFieldMeta<?> cache;
        cache = INSTANCE_MAP.putIfAbsent(fieldMeta, fieldMeta);

        final DefaultSimpleFieldMeta<T> simple;
        if (cache == null) {
            simple = fieldMeta;
        } else if (!(cache instanceof DefaultSimpleFieldMeta)) {
            String m = String.format("%s.%s can't mapping to simple %s.", table.javaType().getName()
                    , field.getName(), FieldMeta.class.getName());
            throw new IllegalArgumentException(m);
        } else if (arrayType) {
            simple = (DefaultArrayFieldMeta<T>) cache;
        } else {
            // drop fieldMeta ,return cache.
            simple = (DefaultSimpleFieldMeta<T>) cache;
        }
        return simple;

    }


    static Set<FieldMeta<?>> codecFieldMetaSet() {
        return CODEC_MAP.keySet();
    }


    final DefaultTableMeta<T> table;

    final String fieldName;

    final Class<?> javaType;

    final String columnName;

    private final String comment;

    private final String defaultValue;

    final MappingType mappingType;

    final boolean notNull;

    final boolean insertable;

    final boolean updatable;

    private final int precision;

    private final int scale;

    private final String collation;

    private final GeneratorMeta generatorMeta;

    final GeneratorType generatorType;

    private final List<Class<?>> elementTypeList;

    private final boolean codec;

    private TableFieldMeta(final TableMeta<T> table, final Field field, MappingType type, final MetaContext context)
            throws MetaException {

        this.table = (DefaultTableMeta<T>) table;
        final Class<?> domainClass = this.table.javaType;
        this.fieldName = field.getName();
        this.javaType = field.getType();
        this.mappingType = type;

        try {
            final Column column;
            column = field.getAnnotation(Column.class);
            final boolean isDiscriminator;
            isDiscriminator = FieldMetaUtils.isDiscriminator(domainClass, this.fieldName);

            if (isDiscriminator && !Enum.class.isAssignableFrom(this.javaType)) {
                String m = String.format("%s.%s is discriminator,but isn't enum.", domainClass.getName(), field.getName());
                throw new MetaException(m);
            }

            if (this.mappingType instanceof MultiGenericsMappingType) {
                this.elementTypeList = List.of(field.getAnnotation(Mapping.class).elements());
            } else {
                this.elementTypeList = List.of();
            }

            this.precision = TableMetaUtils.columnPrecision(column, this, context);
            this.scale = TableMetaUtils.columnScale(column, this, context);
            this.collation = TableMetaUtils.columnCollation(column, this, context);
            this.columnName = TableMetaUtils.columnName(domainClass, column, field, context);
            this.defaultValue = TableMetaUtils.columnDefault(column, this, context);


            final Generator generator;
            if (table instanceof ChildTableMeta && _MetaBridge.ID.equals(this.fieldName)) {
                generator = null;
            } else {
                generator = field.getAnnotation(Generator.class);
            }
            this.updatable = FieldMetaUtils.columnUpdatable(this, column, isDiscriminator);
            this.comment = FieldMetaUtils.columnComment(column, this, isDiscriminator, context);
            this.notNull = table.allColumnNotNull()
                    || _MetaBridge.RESERVED_FIELDS.contains(this.fieldName)
                    || isDiscriminator
                    || field.getType().isPrimitive()
                    || column.notNull();


            this.codec = field.getAnnotation(Codec.class) != null;

            final GeneratorType generatorType;
            if (generator == null) {
                this.generatorType = null;
                this.generatorMeta = null;
            } else if ((generatorType = generator.type()) == GeneratorType.PRECEDE) {
                this.generatorType = generatorType;
                this.generatorMeta = FieldMetaUtils.columnGeneratorMeta(generator, field, this, isDiscriminator, context);
            } else if (generatorType == GeneratorType.POST) {
                this.generatorType = generatorType;
                this.generatorMeta = null;
            } else if (generatorType == GeneratorType.RUNTIME || generatorType == GeneratorType.DEFAULT) {
                final GeneratorStrategy strategy;
                strategy = FieldMetaUtils.loadGeneratorStrategy(this, context);
                this.generatorType = strategy.type();
                this.generatorMeta = FieldMetaUtils.createGeneratorMeta(strategy, this, isDiscriminator, context);
            } else {
                throw _Exceptions.unexpectedEnum(generatorType);
            }

            if (this.generatorType == GeneratorType.POST) {
                FieldMetaUtils.validatePostGenerator(this, generator, isDiscriminator);
            }
            this.insertable = FieldMetaUtils.columnInsertable(this, this.generatorType, column, isDiscriminator);
        } catch (ArmyException e) {
            throw e;
        } catch (RuntimeException e) {
            String m = String.format("Domain class[%s] mapping field[%s] meta error."
                    , table.javaType().getName(), field.getName());
            throw new MetaException(m, e);
        }

    }


    @Override
    public final String label() {
        return this.fieldName;
    }

    @Override
    public final FieldMeta<?> fieldMeta() {
        return this;
    }

    @Override
    public final TypeMeta typeMeta() {
        return this;
    }

    @Override
    public final boolean primary() {
        return _MetaBridge.ID.equals(this.fieldName);
    }

    @Override
    public final boolean unique() {
        return this instanceof UniqueFieldMeta;
    }

    @Override
    public final boolean index() {
        return this instanceof IndexFieldMeta;
    }

    @Override
    public final boolean notNull() {
        return this.notNull;
    }


    @Override
    public final TableMeta<T> tableMeta() {
        return this.table;
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final MappingType mappingType() {
        return this.mappingType;
    }


    @Override
    public final boolean insertable() {
        return this.insertable;
    }

    @Override
    public final boolean updatable() {
        return this.updatable;
    }

    @Override
    public final String comment() {
        return this.comment;
    }

    @Override
    public final String defaultValue() {
        return this.defaultValue;
    }

    @Override
    public final GeneratorType generatorType() {
        return this.generatorType;
    }

    @Override
    public final boolean codec() {
        return this.codec;
    }

    @Override
    public final int precision() {
        return this.precision;
    }

    @Override
    public final int scale() {
        return this.scale;
    }

    @Override
    public final String collation() {
        return this.collation;
    }

    @Override
    public final String objectName() {
        return this.columnName;
    }

    @Override
    public final String columnName() {
        return this.columnName;
    }

    @Override
    public final TableField tableField() {
        //return this
        return this;
    }

    @Override
    public final String fieldName() {
        return this.fieldName;
    }

    @Nullable
    @Override
    public final GeneratorMeta generator() {
        return this.generatorMeta;
    }

    @Nullable
    @Override
    public final FieldMeta<?> dependField() {
        final GeneratorMeta meta;
        meta = this.generatorMeta;
        final String fieldName;
        if (meta == null || (fieldName = meta.params().get(FieldGenerator.DEPEND_FIELD_NAME)) == null) {
            return null;
        }
        FieldMeta<?> field;
        field = this.table.fieldNameToFields.get(fieldName);
        if (field == null) {
            if (this.table instanceof ChildTableMeta) {
                final DefaultTableMeta<?> parent;
                parent = (DefaultTableMeta<?>) ((ChildTableMeta<?>) this.table).parentMeta();
                field = parent.fieldNameToFields.get(fieldName);
            }
        }
        if (field == null) {
            String m = String.format("%s %s meta error.", this, GeneratorMeta.class.getName());
            throw new MetaException(m);
        }
        return field;
    }

    @Override
    public final List<Class<?>> elementTypes() {
        return this.elementTypeList;
    }

    @Override
    public final boolean isSerial() {
        final GeneratorMeta meta = this.generatorMeta;
        return this.generatorType == GeneratorType.POST
                || (meta != null && meta.javaType() == Snowflake8Generator.class);
    }

    @Override
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof TableFieldMeta<?> o) {
            match = this.table.javaType == o.table.javaType
                    && this.fieldName.equals(o.fieldName);
        } else {
            match = false;
        }
        return match;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.table.javaType, this.fieldName);
    }

    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder();
        if (this instanceof PrimaryFieldMeta) {
            builder.append(PrimaryFieldMeta.class.getSimpleName());
        } else if (this instanceof IndexFieldMeta) {
            builder.append(IndexFieldMeta.class.getSimpleName());
        } else {
            builder.append(FieldMeta.class.getSimpleName());
        }
        return builder.append('[')
                .append(this.table.javaType.getName())
                .append('.')
                .append(this.fieldName)
                .append(']')
                .toString();
    }

    @Override
    public final void appendSelectItem(final StringBuilder sqlBuilder, final _SqlContext context) {
        context.appendField(this);
        sqlBuilder.append(_Constant.SPACE_AS_SPACE);

        context.identifier(this.fieldName, sqlBuilder);
    }

    @Override
    public final Expression underlyingExp() {
        return this;
    }

    @Override
    public final void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
        if (_MetaBridge.VISIBLE.equals(this.fieldName) && context.visible() != Visible.BOTH) {
            throw _Exceptions.visibleField(context.visible(), this);
        }
        context.appendField(this);
    }


    /**
     * prevent default deserialization
     */
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        throw new InvalidObjectException("can't deserialize enum");
    }


    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("can't deserialize enum");
    }


    /*################################## blow private method ##################################*/

    private static class DefaultSimpleFieldMeta<T> extends TableFieldMeta<T> {

        private DefaultSimpleFieldMeta(TableMeta<T> table, Field field, MappingType type, MetaContext context) throws MetaException {
            super(table, field, type, context);
        }

    } // DefaultSimpleFieldMeta

    private static final class DefaultArrayFieldMeta<T> extends DefaultSimpleFieldMeta<T> implements ArrayFieldMeta<T> {

        private DefaultArrayFieldMeta(TableMeta<T> table, Field field, MappingType type, MetaContext context) throws MetaException {
            super(table, field, type, context);
            _Assert.isTrue(this.mappingType instanceof ArrayMappingType, "");
        }


    } // DefaultArrayFieldMeta


    private static final class DefaultPrimaryFieldMeta<T> extends DefaultSimpleFieldMeta<T>
            implements PrimaryFieldMeta<T> {

        private DefaultPrimaryFieldMeta(TableMeta<T> table, Field field, MappingType type, MetaContext context)
                throws MetaException {
            super(table, field, type, context);
            if (!_MetaBridge.ID.equals(field.getName())) {
                String m = String.format("[%s] not primary.", field.getName());
                throw new MetaException(m);
            }
        }


    }//DefaultPrimaryFieldMeta


}
