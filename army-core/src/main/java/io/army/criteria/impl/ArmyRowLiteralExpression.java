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

import io.army.criteria.*;
import io.army.dialect._Constant;
import io.army.dialect._SqlContext;
import io.army.lang.Nullable;
import io.army.mapping.MappingType;
import io.army.meta.TypeMeta;
import io.army.util._StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * This class representing multi-value literal expression.
 * <p>
 * Below is chines signature:<br/>
 * 当你在阅读这段代码时,我才真正在写这段代码,你阅读到哪里,我便写到哪里.
 *
 * @see ArmyParamExpression
 * @see ArmyLiteralExpression
 * @see ArmyRowParamExpression
 * @since 0.6.0
 */

abstract class ArmyRowLiteralExpression extends OperationRowExpression implements
        RowLiteralExpression, ArmySimpleSQLExpression {

    /**
     * @throws CriteriaException throw when <ul>
     *                           <li>values is empty</li>
     *                           <li>infer return codec {@link TableField}</li>
     *                           </ul>
     * @see SQLs#rowLiteral(TypeInfer, Collection)
     */
    static ArmyRowLiteralExpression multi(final @Nullable TypeInfer infer, final @Nullable Collection<?> values,
                                          final boolean typeName) {
        final TypeMeta type;
        if (infer == null) {
            throw ContextStack.clearStackAndNullPointer();
        } else if (values == null) {
            throw ContextStack.clearStackAndNullPointer();
        } else if (values.isEmpty()) {
            throw valuesIsEmpty();
        } else if ((type = infer.typeMeta()) instanceof TableField && ((TableField) type).codec()) {
            throw ArmyParamExpression.typeInferReturnCodecField("encodingMultiLiteral");
        }
        return new AnonymousRowLiteral(type, values, typeName);
    }


    /**
     * @throws CriteriaException throw when <ul>
     *                           <li>name have no text</li>
     *                           <li>size less than 1</li>
     *                           <li>infer return codec {@link TableField}</li>
     *                           </ul>
     * @see SQLs#namedRowLiteral(TypeInfer, String)
     */

    static ArmyRowLiteralExpression named(final @Nullable TypeInfer infer, final @Nullable String name,
                                          final boolean typeName) {
        final TypeMeta type;
        if (infer == null) {
            throw ContextStack.clearStackAndNullPointer();
        } else if (!_StringUtils.hasText(name)) {
            throw nameHaveNoText();
        } else if ((type = infer.typeMeta()) instanceof TableField && ((TableField) type).codec()) {
            throw ArmyParamExpression.typeInferReturnCodecField("encodingNamedMultiLiteral");
        }
        return new ArmyNamedRowLiteral(name, type, typeName);
    }



    private static CriteriaException valuesIsEmpty() {
        return ContextStack.clearStackAndCriteriaError("values must non-empty for multi-value literal.");
    }

    private static CriteriaException nameHaveNoText() {
        return ContextStack.clearStackAndCriteriaError("name must have text for multi-value named literal.");
    }

    private static CriteriaException sizeLessThanOne(final int size) {
        final String m = String.format("size[%s] must greater than 0 for multi-value named literal.", size);
        return ContextStack.clearStackAndCriteriaError(m);
    }


    /**
     * private constructor
     */
    private ArmyRowLiteralExpression() {
    }


    static final class AnonymousRowLiteral extends ArmyRowLiteralExpression implements LengthTypedRowExpression {

        private final TypeMeta type;

        private final List<?> valueList;

        private final boolean typeName;

        /**
         * @see #multi(TypeInfer, Collection, boolean)
         */
        private AnonymousRowLiteral(TypeMeta type, Collection<?> values, final boolean typeName) {
            assert !values.isEmpty();
            if (type instanceof QualifiedField) {
                this.type = ((QualifiedField<?>) type).fieldMeta();
            } else {
                assert type instanceof FieldMeta || type instanceof MappingType;
                this.type = type;
            }
            this.valueList = List.copyOf(values);
            this.typeName = typeName;
        }


        @Override
        public int columnSize() {
            return this.valueList.size();
        }

        @Override
        public TypeMeta typeMeta() {
            return this.type;
        }


        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            final List<?> valueList = this.valueList;
            final int valueSize = valueList.size();
            assert valueSize > 0;

            sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            final TypeMeta type = this.type;
            for (int i = 0; i < valueSize; i++) {
                if (i > 0) {
                    sqlBuilder.append(_Constant.SPACE_COMMA);
                }
                context.appendLiteral(type, valueList.get(i), this.typeName);
            }

            sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
        }


        @Override
        public String toString() {
            final boolean encoding;
            final TypeMeta type = this.type;
            encoding = type instanceof TableField && ((TableField) type).codec();

            final List<?> valueList = this.valueList;
            final int valueSize = valueList.size();
            assert valueSize > 0;

            final StringBuilder builder;
            builder = new StringBuilder()
                    .append(_Constant.SPACE_LEFT_PAREN);

            for (int i = 0; i < valueSize; i++) {
                if (i > 0) {
                    builder.append(_Constant.SPACE_COMMA_SPACE);
                } else {
                    builder.append(_Constant.SPACE);
                }
                if (encoding) {
                    builder.append("{LITERAL}");
                } else {
                    builder.append(valueList.get(i));
                }
            }

            return builder.append(_Constant.SPACE_RIGHT_PAREN)
                    .toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.type, this.valueList);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof AnonymousRowLiteral o) {
                match = o.type.equals(this.type)
                        && o.valueList.equals(this.valueList);
            } else {
                match = false;
            }
            return match;
        }


    }//AnonymousMultiLiteral


    private static final class ArmyNamedRowLiteral extends ArmyRowLiteralExpression implements NamedLiteral,
            NamedMultiLiteral {

        private final String name;

        private final TypeMeta type;


        private final boolean typeName;

        /**
         * @see #named(TypeInfer, String, boolean)
         */
        private ArmyNamedRowLiteral(String name, TypeMeta type, boolean typeName) {
            this.name = name;
            if (type instanceof QualifiedField) {
                this.type = ((QualifiedField<?>) type).fieldMeta();
            } else {
                assert type instanceof FieldMeta || type instanceof MappingType;
                this.type = type;
            }
            this.typeName = typeName;
        }

        @Override
        public TypeMeta typeMeta() {
            return this.type;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            context.appendLiteral(this, this.typeName);
        }


        @Override
        public String toString() {
            return _StringUtils.builder()
                    .append(_Constant.SPACE_LEFT_PAREN)
                    .append('{')
                    .append("ROW_LITERAL")
                    .append(':')
                    .append(this.name)
                    .append('}')
                    .append(_Constant.SPACE_RIGHT_PAREN)
                    .toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.type, this.name);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof ArmyNamedRowLiteral o) {
                match = o.type.equals(this.type)
                        && o.name.equals(this.name);
            } else {
                match = false;
            }
            return match;
        }


    }//NamedMultiLiteral


}
