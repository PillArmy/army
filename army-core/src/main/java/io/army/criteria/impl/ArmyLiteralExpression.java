/*
 * Copyright 2023-2043 the original author or authors.
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
import io.army.mapping.*;
import io.army.meta.FieldMeta;
import io.army.meta.TypeMeta;
import io.army.util._StringUtils;

import java.util.Objects;


/**
 * <p>
 * This class representing single-literal expression.
 * <p>
 * Below is chines signature:<br/>
 * 当你在阅读这段代码时,我才真正在写这段代码,你阅读到哪里,我便写到哪里.
 *
 * @see ArmyParamExpression
 * @see ArmyRowLiteralExpression
 * @see ArmyRowParamExpression
 * @since 0.6.0
 */
abstract class ArmyLiteralExpression extends OperationExpression.OperationTypedExpression
        implements _LiteralExpression, ArmySimpleExpression {

    /**
     * @see SQLs#literalValue(Object)
     */
    static LiteralExpression from(final @Nullable Object value, final boolean typeName) {
        if (value == null) {
            throw ContextStack.clearStackAndNullPointer();
        }
        final MappingType type;
        type = _MappingFactory.getDefaultIfMatch(value.getClass());
        if (type == null) {
            throw CriteriaUtils.clearStackAndNonDefaultType(value);
        }
        return forMappingType(type, value, typeName);
    }


    /**
     * @throws CriteriaException throw when infer return codec {@link TableField}.
     * @see SQLs#literal(TypeInfer, Object)
     */
    static LiteralExpression single(final @Nullable TypeInfer infer, final @Nullable Object value, final boolean typeName) {
        final TypeMeta type;
        if (infer == null) {
            throw ContextStack.clearStackAndNullPointer();
        } else if ((type = infer.typeMeta()) instanceof TableField && ((TableField) type).codec()) {
            throw ArmyParamExpression.typeInferReturnCodecField("encodingLiteral");
        }

        if (infer instanceof MappingType) {
            return forMappingType((MappingType) type, value, typeName);
        }
        return new AnonymousLiteral(type, value, typeName);
    }

    /**
     * @throws CriteriaException throw when <ul>
     *                           <li>infer return codec {@link TableField}.</li>
     *                           <li>name have no text</li>
     *                           </ul>
     * @see SQLs#namedLiteral(TypeInfer, String)
     */
    static ArmyLiteralExpression named(final @Nullable TypeInfer infer, final @Nullable String name, final boolean typeName) {
        final TypeMeta type;
        if (infer == null) {
            throw ContextStack.clearStackAndNullPointer();
        } else if (!_StringUtils.hasText(name)) {
            throw nameHaveNoText();
        } else if ((type = infer.typeMeta()) instanceof TableField && ((TableField) type).codec()) {
            throw ArmyParamExpression.typeInferReturnCodecField("encodingNamedLiteral");
        }
        return new ArmyNamedLiteral(name, type, typeName);
    }


    static CriteriaException nameHaveNoText() {
        return ContextStack.clearStackAndCriteriaError("name must have text for single-literal.");
    }


    private static LiteralExpression forMappingType(final MappingType type, final @Nullable Object value, final boolean typeName) {
        final LiteralExpression literal;
        if (type == BooleanType.INSTANCE) {
            if (!(value instanceof Boolean)) {
                literal = new AnonymousLiteral(type, value, typeName);
            } else if (Boolean.TRUE.equals(value)) {
                literal = SQLs.TRUE;
            } else {
                literal = SQLs.FALSE;
            }
        } else if (type == IntegerType.INSTANCE) {
            if (value instanceof Integer) {
                literal = integerLiteral((Integer) value, typeName);
            } else {
                literal = new AnonymousLiteral(type, value, typeName);
            }
        } else if (type == StringType.INSTANCE) {
            if (!(value instanceof String)) {
                literal = new AnonymousLiteral(type, value, typeName);
            } else if ("".equals(value)) {
                literal = typeName ? SQLs.LITERAL_EMPTY_STRING : SQLs.CONST_EMPTY_STRING;
            } else if (" ".equals(value)) {
                literal = typeName ? SQLs.LITERAL_SPACE : SQLs.CONST_SPACE;
            } else {
                literal = new AnonymousLiteral(type, value, typeName);
            }
        } else if (type == BigDecimalType.INSTANCE) {
            literal = new AnonymousLiteral(type, value, typeName);
        } else if (type == DoubleType.INSTANCE) {
            literal = new AnonymousLiteral(type, value, typeName);
        } else {
            literal = new AnonymousLiteral(type, value, typeName);
        }
        return literal;
    }


    private static LiteralExpression integerLiteral(final Integer value, final boolean typeName) {
        return switch (value) {
            case -1 -> typeName ? SQLs.LITERAL_MINUS_1 : SQLs.CONST_MINUS_1;
            case 0 -> typeName ? SQLs.LITERAL_0 : SQLs.CONST_0;
            case 1 -> typeName ? SQLs.LITERAL_1 : SQLs.CONST_1;
            case 2 -> typeName ? SQLs.LITERAL_2 : SQLs.CONST_2;
            case 3 -> typeName ? SQLs.LITERAL_3 : SQLs.CONST_3;
            case 4 -> typeName ? SQLs.LITERAL_4 : SQLs.CONST_4;
            case 5 -> typeName ? SQLs.LITERAL_5 : SQLs.CONST_5;
            case 6 -> typeName ? SQLs.LITERAL_6 : SQLs.CONST_6;
            case 7 -> typeName ? SQLs.LITERAL_7 : SQLs.CONST_7;
            case 8 -> typeName ? SQLs.LITERAL_8 : SQLs.CONST_8;
            case 9 -> typeName ? SQLs.LITERAL_9 : SQLs.CONST_9;
            case 10 -> typeName ? SQLs.LITERAL_10 : SQLs.CONST_10;
            case 100 -> typeName ? SQLs.LITERAL_100 : SQLs.CONST_100;
            case 1000 -> typeName ? SQLs.LITERAL_1000 : SQLs.CONST_1000;
            default -> new AnonymousLiteral(IntegerType.INSTANCE, value, typeName);
        };
    }

    final TypeMeta type;

    final boolean typeName;

    /**
     * private constructor
     */
    private ArmyLiteralExpression(TypeMeta type, boolean typeName) {
        if (type instanceof QualifiedField) {
            this.type = ((QualifiedField<?>) type).fieldMeta();
        } else {
            assert type instanceof FieldMeta || type instanceof MappingType;
            this.type = type;
        }
        this.typeName = typeName;

    }

    @Override
    public final TypeMeta typeMeta() {
        return this.type;
    }


    private static class AnonymousLiteral extends ArmyLiteralExpression
            implements SingleAnonymousValue {

        private final Object value;


        /**
         * @see #single(TypeInfer, Object, boolean)
         * @see #encodingSingle(TypeInfer, Object, boolean)
         */
        private AnonymousLiteral(TypeMeta type, @Nullable Object value, boolean typeName) {
            super(type, typeName);
            this.value = value;
        }

        @Override
        public final Object value() {
            return this.value;
        }

        @Override
        public void appendSqlWithoutType(StringBuilder sqlBuilder, _SqlContext context) {
            context.appendLiteral(this.type, this.value, false);
        }

        @Override
        public final void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            context.appendLiteral(this.type, this.value, this.typeName);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(this.type, this.value);
        }

        @Override
        public final boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof AnonymousLiteral o) {
                match = o.type.equals(this.type)
                        && Objects.equals(o.value, this.value);
            } else {
                match = false;
            }
            return match;
        }

        @Override
        public final String toString() {
            final String s;
            if (this.value == null) {
                s = _Constant.SPACE_NULL;
            } else if (this.type instanceof TableField && ((TableField) this.type).codec()) {
                s = " {LITERAL}";
            } else {
                s = " " + this.value;
            }
            return s;
        }


    }//AnonymousLiteral


    private static class ArmyNamedLiteral extends ArmyLiteralExpression implements NamedLiteral {

        private final String name;


        private ArmyNamedLiteral(String name, TypeMeta type, boolean typeName) {
            super(type, typeName);
            this.name = name;
        }


        @Override
        public final String name() {
            return this.name;
        }


        @Override
        public void appendSqlWithoutType(StringBuilder sqlBuilder, _SqlContext context) {
            context.appendLiteral(this, false);
        }

        @Override
        public final void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            context.appendLiteral(this, this.typeName);
        }


        @Override
        public final String toString() {
            return _StringUtils.builder()
                    .append('{')
                    .append("LITERAL")
                    .append(':')
                    .append(this.name)
                    .append('}')
                    .toString();
        }

        @Override
        public final int hashCode() {
            return Objects.hash(this.type, this.name);
        }

        @Override
        public final boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof ArmyNamedLiteral o) {
                match = o.name.equals(this.name)
                        && o.type.equals(this.type);
            } else {
                match = false;
            }
            return match;
        }


    }//NamedLiteral


}
