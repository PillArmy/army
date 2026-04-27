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
import io.army.mapping.optional.CompositeField;
import io.army.util._Assert;
import io.army.util._Exceptions;

import java.util.List;

final class DefaultTypeInfo implements TypeInfo {

    static Builder newBuilder() {
        return new DefaultBuilder();
    }

    private final String typeName;

    private final TypeCategory typeCategory;

    private final List<String> enumLabelList;

    private final List<CompositeField> compositeFieldList;

    private final String baseTypeName;

    private final String collation;

    private final boolean notNull;

    private final String defaultValue;

    private final String constraint;

    private final String rangeSubType;

    private final String rangeCollation;

    private final String rangeMulti;

    private final String rangeSubOpc;

    private final String rangeCanonical;

    private final String rangeSubDiff;


    private DefaultTypeInfo(final DefaultBuilder builder) {
        this.typeName = builder.typeName;
        this.typeCategory = builder.typeCategory;

        switch (this.typeCategory) {
            case ENUM:
                _Assert.notEmpty(builder.enumLabelList, "empty");
                this.enumLabelList = List.copyOf(builder.enumLabelList);

                this.compositeFieldList = List.of();

                this.baseTypeName = null;
                this.collation = null;
                this.notNull = false;
                this.defaultValue = null;
                this.constraint = null;

                this.rangeSubType = null;
                this.rangeCollation = null;
                this.rangeMulti = null;
                this.rangeSubOpc = null;
                this.rangeCanonical = null;
                this.rangeSubDiff = null;
                break;
            case COMPOSITE:
                _Assert.notEmpty(builder.compositeFieldList, "empty");
                this.compositeFieldList = List.copyOf(builder.compositeFieldList);

                this.enumLabelList = List.of();

                this.baseTypeName = null;
                this.collation = null;
                this.notNull = false;
                this.defaultValue = null;
                this.constraint = null;

                this.rangeSubType = null;
                this.rangeCollation = null;
                this.rangeMulti = null;
                this.rangeSubOpc = null;
                this.rangeCanonical = null;
                this.rangeSubDiff = null;
                break;
            case RANGE:
                this.rangeSubType = builder.rangeSubType;
                this.rangeCollation = builder.rangeCollation;
                this.rangeMulti = builder.rangeMulti;
                this.rangeSubOpc = builder.rangeSubOpc;
                this.rangeCanonical = builder.rangeCanonical;
                this.rangeSubDiff = builder.rangeSubDiff;

                this.enumLabelList = List.of();
                this.compositeFieldList = List.of();

                this.baseTypeName = null;
                this.collation = null;
                this.notNull = false;
                this.defaultValue = null;
                this.constraint = null;
                break;
            case DOMAIN:
                _Assert.hasText(builder.baseTypeName, "no text");
                this.baseTypeName = builder.baseTypeName;
                this.collation = builder.collation;
                this.notNull = builder.notNull;
                this.defaultValue = builder.defaultValue;
                this.constraint = builder.constraint;

                this.enumLabelList = List.of();
                this.compositeFieldList = List.of();

                this.rangeSubType = null;
                this.rangeCollation = null;
                this.rangeMulti = null;
                this.rangeSubOpc = null;
                this.rangeCanonical = null;
                this.rangeSubDiff = null;
                break;
            default:
                throw _Exceptions.unexpectedEnum(this.typeCategory);
        }

    }

    @Override
    public String name() {
        return this.typeName;
    }

    @Override
    public TypeCategory category() {
        return this.typeCategory;
    }

    @Override
    public List<String> enumLabelList() {
        return this.enumLabelList;
    }

    @Override
    public List<CompositeField> compositeFieldList() {
        return this.compositeFieldList;
    }

    @Override
    public boolean isNotNull() {
        return this.notNull;
    }

    @Override
    public String defaultValue() {
        return this.defaultValue;
    }

    @Override
    public String constraint() {
        return this.constraint;
    }

    @Override
    public String collation() {
        return this.collation;
    }

    @Override
    public String rangeSubType() {
        return this.rangeSubType;
    }

    @Override
    public String rangeCollation() {
        return this.rangeCollation;
    }

    @Override
    public String rangeMulti() {
        return this.rangeMulti;
    }

    @Override
    public String rangeSubOpc() {
        return this.rangeSubOpc;
    }

    @Override
    public String rangeCanonical() {
        return this.rangeCanonical;
    }

    @Override
    public String rangeSubDiff() {
        return this.rangeSubDiff;
    }

    private static final class DefaultBuilder implements Builder {

        private String typeName;

        private TypeCategory typeCategory;

        private List<String> enumLabelList;

        private List<CompositeField> compositeFieldList;

        private String baseTypeName;

        private String collation;

        private boolean notNull;

        private String defaultValue;

        private String constraint;

        private String rangeSubType;

        private String rangeCollation;

        private String rangeMulti;

        private String rangeSubOpc;

        private String rangeCanonical;

        private String rangeSubDiff;

        @Override
        public Builder name(String typeName) {
            this.typeName = typeName;
            return this;
        }

        @Override
        public Builder category(TypeCategory typeCategory) {
            this.typeCategory = typeCategory;
            return this;
        }

        @Override
        public Builder enumLabelList(List<String> enumLabelList) {
            this.enumLabelList = enumLabelList;
            return this;
        }

        @Override
        public Builder compositeFieldList(List<CompositeField> compositeFieldList) {
            this.compositeFieldList = compositeFieldList;
            return this;
        }

        @Override
        public Builder baseType(String typeName) {
            this.baseTypeName = typeName;
            return this;
        }

        @Override
        public Builder notNull(boolean yes) {
            this.notNull = yes;
            return this;
        }

        @Override
        public Builder defaultValue(@Nullable String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @Override
        public Builder constraint(@Nullable String constraint) {
            this.constraint = constraint;
            return this;
        }

        @Override
        public Builder collation(@Nullable String collation) {
            this.collation = collation;
            return this;
        }

        @Override
        public Builder rangeSubType(@Nullable String rangeSubType) {
            this.rangeSubType = rangeSubType;
            return this;
        }

        @Override
        public Builder rangeCollation(@Nullable String rangeCollation) {
            this.rangeCollation = rangeCollation;
            return this;
        }

        @Override
        public Builder rangeMulti(@Nullable String rangeMulti) {
            this.rangeMulti = rangeMulti;
            return this;
        }

        @Override
        public Builder rangeSubOpc(@Nullable String rangeSubOpc) {
            this.rangeSubOpc = rangeSubOpc;
            return this;
        }

        @Override
        public Builder rangeCanonical(@Nullable String rangeCanonical) {
            this.rangeCanonical = rangeCanonical;
            return this;
        }

        @Override
        public Builder rangeSubDiff(@Nullable String rangeSubDiff) {
            this.rangeSubDiff = rangeSubDiff;
            return this;
        }

        @Override
        public TypeInfo build() {
            _Assert.hasText(this.typeName, "no text");
            _Assert.notNull(this.typeCategory, "not null");

            final TypeInfo typeInfo;
            typeInfo = new DefaultTypeInfo(this);

            clear();
            return typeInfo;
        }

        private void clear() {
            this.typeName = null;
            this.typeCategory = null;

            this.enumLabelList = null;

            this.compositeFieldList = null;

            this.baseTypeName = null;
            this.collation = null;
            this.notNull = false;
            this.defaultValue = null;
            this.constraint = null;

            this.rangeSubType = null;
            this.rangeCollation = null;
            this.rangeMulti = null;
            this.rangeSubOpc = null;
            this.rangeCanonical = null;
            this.rangeSubDiff = null;
        }

    } // DefaultBuilder


}
