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
import io.army.struct.TypeCategory;
import io.army.util._Assert;
import io.army.util._Collections;
import io.army.util._Exceptions;
import io.army.util._StringUtils;

import java.util.List;

final class DefaultTypeResult implements TypeResult {

    static Builder newBuilder() {
        return new DefaultBuilder();
    }

    private final MappingType type;

    private final TypeCategory typeCategory;

    private final List<String> enumNewLabelList;

    private final List<CompositeField> compositeDropFieldList;

    private final List<CompositeField> compositeNewFieldList;

    private final List<CompositeField> compositeModifyFieldList;

    private final boolean containNotNull;

    private final boolean containBaseType;

    private final boolean containDefault;

    private final String constraintName;

    private final boolean containCollation;

    private final boolean containRangeSubType;

    private final boolean containRangeCollation;

    private final boolean containRangeMulti;

    private final boolean containRangeSubOpc;

    private final boolean containRangeCanonical;

    private final boolean containRangeSubDiff;


    private DefaultTypeResult(final DefaultBuilder builder) {
        this.type = builder.type;
        this.typeCategory = builder.typeCategory;

        switch (this.typeCategory) {
            case ENUM:
                _Assert.notEmpty(builder.enumNewLabelList, "empty");
                this.enumNewLabelList = _Collections.safeCopyList(builder.enumNewLabelList);

                this.compositeDropFieldList = List.of();
                this.compositeNewFieldList = List.of();
                this.compositeModifyFieldList = List.of();

                this.containNotNull = false;
                this.containBaseType = false;
                this.containDefault = false;
                this.constraintName = null;
                this.containCollation = false;

                this.containRangeSubType = false;
                this.containRangeCollation = false;
                this.containRangeMulti = false;
                this.containRangeSubOpc = false;
                this.containRangeCanonical = false;
                this.containRangeSubDiff = false;
                break;
            case COMPOSITE:
                _Assert.notEmpty(builder.compositeNewFieldList, "empty");
                this.compositeNewFieldList = _Collections.safeCopyList(builder.compositeNewFieldList);
                this.compositeDropFieldList = _Collections.safeCopyList(builder.compositeDropFieldList);
                this.compositeModifyFieldList = _Collections.safeCopyList(builder.compositeModifyFieldList);

                this.enumNewLabelList = List.of();

                this.containNotNull = false;
                this.containBaseType = false;
                this.containDefault = false;
                this.constraintName = null;
                this.containCollation = false;

                this.containRangeSubType = false;
                this.containRangeCollation = false;
                this.containRangeMulti = false;
                this.containRangeSubOpc = false;
                this.containRangeCanonical = false;
                this.containRangeSubDiff = false;
                break;
            case RANGE:
                this.containRangeSubType = builder.containRangeSubType;
                this.containRangeCollation = builder.containRangeCollation;
                this.containRangeMulti = builder.containRangeMulti;
                this.containRangeSubOpc = builder.containRangeSubOpc;
                this.containRangeCanonical = builder.containRangeCanonical;
                this.containRangeSubDiff = builder.containRangeSubDiff;

                this.enumNewLabelList = List.of();

                this.compositeDropFieldList = List.of();
                this.compositeNewFieldList = List.of();
                this.compositeModifyFieldList = List.of();

                this.containNotNull = false;
                this.containBaseType = false;
                this.containDefault = false;
                this.constraintName = null;
                this.containCollation = false;
                break;
            case DOMAIN:
                this.containNotNull = builder.containNotNull;
                this.containBaseType = builder.containBaseType;
                this.containDefault = builder.containDefault;
                this.constraintName = builder.constraintName;
                this.containCollation = builder.containCollation;

                this.enumNewLabelList = List.of();

                this.compositeDropFieldList = List.of();
                this.compositeNewFieldList = List.of();
                this.compositeModifyFieldList = List.of();

                this.containRangeSubType = false;
                this.containRangeCollation = false;
                this.containRangeMulti = false;
                this.containRangeSubOpc = false;
                this.containRangeCanonical = false;
                this.containRangeSubDiff = false;
                break;
            default:
                throw _Exceptions.unexpectedEnum(this.typeCategory);
        }

    }

    @Override
    public MappingType type() {
        return this.type;
    }

    @Override
    public TypeCategory category() {
        return this.typeCategory;
    }

    @Override
    public List<String> enumNewLabelList() {
        return this.enumNewLabelList;
    }


    @Override
    public List<CompositeField> compositeNewFieldList() {
        return this.compositeNewFieldList;
    }

    @Override
    public List<CompositeField> compositeDropFieldList() {
        return this.compositeDropFieldList;
    }

    @Override
    public List<CompositeField> compositeModifyFieldList() {
        return this.compositeModifyFieldList;
    }

    @Override
    public boolean containNotNull() {
        return this.containNotNull;
    }

    @Override
    public boolean containBaseType() {
        return this.containBaseType;
    }

    @Override
    public boolean containDefault() {
        return this.containDefault;
    }

    @Override
    public String constraintName() {
        return this.constraintName;
    }

    @Override
    public boolean containCollation() {
        return this.containCollation;
    }

    @Override
    public boolean containRangeSubType() {
        return this.containRangeSubType;
    }

    @Override
    public boolean containRangeCollation() {
        return this.containRangeCollation;
    }

    @Override
    public boolean containRangeMulti() {
        return this.containRangeMulti;
    }

    @Override
    public boolean containRangeSubOpc() {
        return this.containRangeSubOpc;
    }

    @Override
    public boolean containRangeCanonical() {
        return this.containRangeCanonical;
    }

    @Override
    public boolean containRangeSubDiff() {
        return this.containRangeSubDiff;
    }

    private static final class DefaultBuilder implements Builder {

        private MappingType type;

        private TypeCategory typeCategory;

        private List<String> enumNewLabelList;

        private List<CompositeField> compositeDropFieldList;

        private List<CompositeField> compositeNewFieldList;

        private List<CompositeField> compositeModifyFieldList;

        private boolean containNotNull;

        private boolean containBaseType;

        private boolean containDefault;

        private String constraintName;

        private boolean containCollation;

        private boolean containRangeSubType;

        private boolean containRangeCollation;

        private boolean containRangeMulti;

        private boolean containRangeSubOpc;

        private boolean containRangeCanonical;

        private boolean containRangeSubDiff;

        @Override
        public Builder type(MappingType type) {
            this.type = type;
            return this;
        }

        @Override
        public Builder category(TypeCategory typeCategory) {
            this.typeCategory = typeCategory;
            return this;
        }

        @Override
        public Builder enumNewLabelList(@Nullable List<String> list) {
            this.enumNewLabelList = list;
            return this;
        }

        @Override
        public Builder compositeDropFieldList(@Nullable List<CompositeField> list) {
            this.compositeDropFieldList = list;
            return this;
        }

        @Override
        public Builder compositeNewFieldList(@Nullable List<CompositeField> list) {
            this.compositeNewFieldList = list;
            return this;
        }

        @Override
        public Builder compositeModifyFieldList(@Nullable List<CompositeField> list) {
            this.compositeModifyFieldList = list;
            return this;
        }

        @Override
        public Builder containNotNull(boolean yes) {
            this.containNotNull = yes;
            return this;
        }

        @Override
        public Builder containBaseType(boolean yes) {
            this.containBaseType = yes;
            return this;
        }

        @Override
        public Builder containDefault(boolean yes) {
            this.containDefault = yes;
            return this;
        }

        @Override
        public Builder constraintName(@Nullable String name) {
            this.constraintName = name;
            return this;
        }

        @Override
        public Builder containCollation(boolean yes) {
            this.containCollation = yes;
            return this;
        }

        @Override
        public Builder containRangeSubType(boolean yes) {
            this.containRangeSubType = yes;
            return this;
        }

        @Override
        public Builder containRangeCollation(boolean yes) {
            this.containRangeCollation = yes;
            return this;
        }

        @Override
        public Builder containRangeMulti(boolean yes) {
            this.containRangeMulti = yes;
            return this;
        }

        @Override
        public Builder containRangeSubOpc(boolean yes) {
            this.containRangeSubOpc = yes;
            return this;
        }

        @Override
        public Builder containRangeCanonical(boolean yes) {
            this.containRangeCanonical = yes;
            return this;
        }

        @Override
        public Builder containRangeSubDiff(boolean yes) {
            this.containRangeSubDiff = yes;
            return this;
        }

        @Override
        public boolean hasDifference() {
            final boolean hasDiff;
            switch (this.typeCategory) {
                case ENUM:
                    hasDiff = !_Collections.isEmpty(this.enumNewLabelList);
                    break;
                case COMPOSITE:
                    hasDiff = !_Collections.isEmpty(this.compositeNewFieldList)
                            || !_Collections.isEmpty(this.compositeDropFieldList)
                            || !_Collections.isEmpty(this.compositeModifyFieldList);
                    break;
                case RANGE:
                    hasDiff = this.containRangeSubType
                            || this.containRangeCollation
                            || this.containRangeMulti
                            || this.containRangeSubOpc
                            || this.containRangeCanonical
                            || this.containRangeSubDiff;
                    break;
                case DOMAIN:
                    hasDiff = this.containNotNull
                            || this.containBaseType
                            || this.containDefault
                            || _StringUtils.hasText(this.constraintName)
                            || this.containCollation;
                    break;
                default:
                    throw _Exceptions.unexpectedEnum(this.typeCategory);
            }
            return hasDiff;
        }

        @Override
        public void clear() {
            this.type = null;
            this.typeCategory = null;

            this.enumNewLabelList = null;

            this.compositeDropFieldList = null;
            this.compositeNewFieldList = null;
            this.compositeModifyFieldList = null;

            this.containNotNull = false;
            this.containBaseType = false;
            this.containDefault = false;
            this.constraintName = null;
            this.containCollation = false;

            this.containRangeSubType = false;
            this.containRangeCollation = false;
            this.containRangeMulti = false;
            this.containRangeSubOpc = false;
            this.containRangeCanonical = false;
            this.containRangeSubDiff = false;

        }

        @Override
        public TypeResult build() {
            _Assert.notNull(this.type, "type");
            _Assert.notNull(this.typeCategory, "not null");

            final TypeResult typeResult;
            typeResult = new DefaultTypeResult(this);

            clear();
            return typeResult;
        }


    } // DefaultBuilder


}
