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

import io.army.annotation.Column;
import io.army.annotation.MappedSuperclass;
import io.army.mapping.CompositeMappingType;
import io.army.mapping.MappingType;
import io.army.meta.CompositeField;
import io.army.meta.MetaException;
import io.army.struct.DefinedType;
import io.army.util._Assert;
import io.army.util._Collections;
import io.army.util._StringUtils;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.lang.reflect.Field;
import java.util.*;

/// Factory for creating composite field metadata from a {@link io.army.mapping.CompositeType}.
///
/// @see io.army.mapping.CompositeType
public abstract class CompositeFieldFactory {


    private CompositeFieldFactory() {
    }


    public static CompositeField from(String columnName, MappingType mappingType, String collation) {
        _Assert.hasText(columnName, "");
        Objects.requireNonNull(mappingType);
        Objects.requireNonNull(collation);
        return new CompositeFieldInfo(columnName, mappingType, collation);
    }


    /// @return an unmodifiable list
    public static List<CompositeField> forType(final CompositeMappingType compositeType) {
        if (!(compositeType instanceof MappingType)) {
            throw new IllegalArgumentException();
        } else if (compositeType.javaType().getEnclosingClass() != null) {
            throw new IllegalArgumentException();
        }

        return createFieldList(compositeType);
    }


    private static List<CompositeField> createFieldList(final CompositeMappingType compositeType) {

        final MetaContext context = _TableMetaFactory.getContext();
        final Class<?> javaType = compositeType.javaType();
        final String[] fieldOrder = javaType.getAnnotation(DefinedType.class).fieldOrder();

        final List<CompositeField> fieldList = new ArrayList<>(fieldOrder.length);
        Column column;
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
                fieldList.add(new DefaultCompositeField(compositeType, field, column, context));

            } // field loop

        } // class loop

        final int columnCount = fieldList.size();
        if (columnCount == 0) {
            throw definedTypeError(javaType, "no field");
        } else if (columnCount != fieldOrder.length) {
            throw definedTypeError(javaType, "fieldOrder length error");
        }

        final Map<String, Integer> fieldToOrder = _StringUtils.createOrderMap(fieldOrder);
        fieldList.sort(Comparator.comparingInt(field -> {
            final Integer order;
            order = fieldToOrder.get(field.fieldName());
            if (order == null) {
                String m = String.format("%s not in fieldOrder", field.fieldName());
                throw definedTypeError(javaType, m);
            }
            return order;
        }));
        return List.copyOf(fieldList);
    }


    public static Map<String, CompositeField> createFieldMap(Class<?> javaType, final List<CompositeField> fieldList) {
        final Map<String, CompositeField> fieldMap = _Collections.hashMapForSize(fieldList.size());
        for (CompositeField field : fieldList) {
            fieldMap.put(field.fieldName(), field);
        }
        if (fieldMap.size() != fieldList.size()) {
            // no bug ,never here
            String m = String.format("%s filed name duplicate", javaType.getName());
            throw new MetaException(m);
        }
        return Map.copyOf(fieldMap);
    }


    private static MetaException definedTypeError(Class<?> javaType, String suffixMsg) {
        String m = String.format("%s[%s] %s", DefinedType.class.getSimpleName(), javaType.getName(), suffixMsg);
        return new MetaException(m);
    }



    private static final class DefaultCompositeField implements CompositeField {

        private final CompositeMappingType compositeType;

        private final String fieldName;

        private final Class<?> javaType;

        private final String columnName;

        private final MappingType mappingType;

        private final int precision;

        private final int scale;

        private final String collation;


        private DefaultCompositeField(final CompositeMappingType compositeType, Field field, Column column, MetaContext context) {
            final Class<?> typeClass = compositeType.javaType();

            this.compositeType = compositeType;
            this.fieldName = field.getName();
            this.javaType = field.getType();
            this.mappingType = _MappingFactory.map(typeClass, field, context);

            this.columnName = TableMetaUtils.columnName(typeClass, column, field, context);
            this.precision = TableMetaUtils.columnPrecision(column, this, context);
            this.scale = TableMetaUtils.columnScale(column, this, context);
            this.collation = TableMetaUtils.columnCollation(column, this, context);
        }

        @Override
        public CompositeMappingType compositeType() {
            return this.compositeType;
        }

        @Override
        public String fieldName() {
            return this.fieldName;
        }

        @Override
        public String columnName() {
            return this.columnName;
        }

        @Override
        public Class<?> javaType() {
            return this.javaType;
        }

        @Override
        public MappingType mappingType() {
            return this.mappingType;
        }

        @Override
        public String collation() {
            return this.collation;
        }

        @Override
        public int precision() {
            return this.precision;
        }

        @Override
        public int scale() {
            return this.scale;
        }

        @Override
        public String objectName() {
            return this.columnName;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.compositeType.javaType(), this.fieldName);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof DefaultCompositeField o) {
                match = o.compositeType.javaType() == this.compositeType.javaType()
                        && o.fieldName.equals(this.fieldName);
            } else {
                match = false;
            }
            return match;
        }

        @Override
        public String toString() {
            return _StringUtils.builder()
                    .append(CompositeField.class.getSimpleName())
                    .append('[')
                    .append(this.compositeType.javaType().getName())
                    .append('.')
                    .append(this.fieldName)
                    .append(']')
                    .toString();
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


    } // DefaultCompositeField


    private static final class CompositeFieldInfo implements CompositeField {


        private final String columnName;

        private final MappingType mappingType;

        private final String collation;

        private CompositeFieldInfo(String columnName, MappingType mappingType, String collation) {
            this.columnName = columnName;
            this.mappingType = mappingType;
            this.collation = collation;
        }

        @Override
        public CompositeMappingType compositeType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Class<?> javaType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String fieldName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String columnName() {
            return this.columnName;
        }

        @Override
        public MappingType mappingType() {
            return this.mappingType;
        }

        @Override
        public String collation() {
            return this.collation;
        }

        @Override
        public int precision() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int scale() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String objectName() {
            return this.columnName;
        }


    } // CompositeFieldInfo


}
