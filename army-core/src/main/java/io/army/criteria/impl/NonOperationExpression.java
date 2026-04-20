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

import io.army.annotation.SortOrder;
import io.army.criteria.*;
import io.army.dialect._Constant;
import io.army.dialect._SqlContext;
import io.army.lang.Nullable;
import io.army.mapping.IntegerType;
import io.army.mapping.MappingType;
import io.army.mapping.NullType;
import io.army.meta.TypeMeta;
import io.army.util._StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * <p>
 * This class representing non-operation expression.
 * This class is base class of following : <ul>
 * <li>{@link SQLs#DEFAULT}</li>
 * <li>{@link SQLs#ASTERISK}</li>
 * </ul>
 */
abstract class NonOperationExpression implements ArmyExpression {


    NonOperationExpression() {
    }


    @Override
    public final boolean isNullValue() {
        final boolean nullable;
        if (this instanceof UpdateTimePlaceHolderExpression) {
            nullable = false;
        } else if (this instanceof SqlValueParam.SingleAnonymousValue) {
            nullable = ((SqlValueParam.SingleAnonymousValue) this).value() == null;
        } else {
            nullable = false;
        }
        return nullable;
    }

    @Override
    public final IPredicate equal(Object operand) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate notEqual(Object operand) {
        throw unsupportedOperation(this);
    }


    @Override
    public final IPredicate nullSafeEqual(Object operand) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate equalAny(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate equalSome(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate equalAll(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate less(Object operand) {
        throw unsupportedOperation(this);
    }


    @Override
    public final IPredicate lessAny(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate lessSome(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate lessAll(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate lessEqual(Object operand) {
        throw unsupportedOperation(this);
    }


    @Override
    public final IPredicate lessEqualAny(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate lessEqualSome(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate lessEqualAll(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate greater(Object operand) {
        throw unsupportedOperation(this);
    }


    @Override
    public final IPredicate greaterAny(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate greaterSome(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate greaterAll(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate greaterEqual(Object operand) {
        throw unsupportedOperation(this);
    }


    @Override
    public final IPredicate greaterEqualAny(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate greaterEqualSome(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate greaterEqualAll(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate notEqualAny(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate notEqualSome(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate notEqualAll(SubQuery subQuery) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate between(Object first, SQLs.WordAnd and, Object second) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate notBetween(Object first, SQLs.WordAnd and, Object second) {
        throw unsupportedOperation(this);
    }


    @Override
    public final IPredicate between(@Nullable SQLs.BetweenModifier modifier, Object first, SQLs.WordAnd and, Object second) {
        throw unsupportedOperation(this);
    }


    @Override
    public final IPredicate notBetween(@Nullable SQLs.BetweenModifier modifier, Object first, SQLs.WordAnd and, Object second) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate is(SQLs.BoolTestWord operand) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate isNot(SQLs.BoolTestWord operand) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate is(SQLs.IsComparisonWord operator, Object operand) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate isNot(SQLs.IsComparisonWord operator, Object operand) {
        throw unsupportedOperation(this);
    }


    @Override
    public final IPredicate isNull() {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate isNotNull() {
        throw unsupportedOperation(this);
    }


    @Override
    public final IPredicate in(SQLColumnList row) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate notIn(SQLColumnList row) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate like(Object pattern) {
        throw unsupportedOperation(this);
    }


    @Override
    public final IPredicate like(Object pattern, SQLs.WordEscape escape, Object escapeChar) {
        throw unsupportedOperation(this);
    }


    @Override
    public final IPredicate notLike(Object pattern) {
        throw unsupportedOperation(this);
    }


    @Override
    public final IPredicate notLike(Object pattern, SQLs.WordEscape escape, Object escapeChar) {
        throw unsupportedOperation(this);
    }


    @Override
    public final IPredicate similarTo(Object pattern) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate similarTo(Object pattern, SQLs.WordEscape escape, Object escapeChar) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate notSimilarTo(Object pattern) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate notSimilarTo(Object pattern, SQLs.WordEscape escape, Object escapeChar) {
        throw unsupportedOperation(this);
    }


    @Override
    public final Expression mod(Object operand) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression times(Object operand) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression plus(Object operand) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression minus(Object minuend) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression divide(Object divisor) {
        throw unsupportedOperation(this);
    }


    @Override
    public final Expression bitwiseAnd(Object operand) {
        throw unsupportedOperation(this);
    }


    @Override
    public final Expression bitwiseOr(Object operand) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression bitwiseXor(Object operand) {
        throw unsupportedOperation(this);
    }


    @Override
    public final Expression rightShift(Object bitNumber) {
        throw unsupportedOperation(this);
    }


    @Override
    public final Expression leftShift(Object bitNumber) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression slice(Object lower, SQLs.SymbolColon colon, Object upper) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression slice(BiFunction<IntegerType, Integer, Expression> func, int lower, SQLs.SymbolColon colon, int upper) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression slice(Object lower, SQLs.SymbolColon colon) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression slice(SQLs.SymbolColon colon, Object upper) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression slice(SQLs.SymbolColon colon) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression slice(Object lower, SQLs.SymbolColon colon, Object upper, Object lower1, SQLs.SymbolColon colon1, Object upper1) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression slice(BiFunction<IntegerType, Integer, Expression> func, int lower, SQLs.SymbolColon colon, int upper, int lower1, SQLs.SymbolColon colon1, int upper1) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression slice(SQLs.SymbolColon colon, Object upper, Object lower1, SQLs.SymbolColon colon1, Object upper1) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression slice(Object lower, SQLs.SymbolColon colon, Object upper, Object lower1, SQLs.SymbolColon colon1) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression slice(SQLs.SymbolColon colon, SQLs.SymbolColon colon1) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression sliceAtSubs(List<?> indexList) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression sliceAtSubs(BiFunction<MappingType, Integer, Expression> func, List<Integer> indexList) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression dot(Object key) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression dot(Object key, Object key1) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression dot(Object key, Object key1, Object key2) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression dot(Object key, Object key1, Object key2, Object key3) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression dot(Object key, Object key1, Object key2, Object key3, Object key4) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression dots(List<?> subscriptList) {
        throw unsupportedOperation(this);
    }


    @Override
    public final Expression bracket(Object key) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression bracket(Object key, Object key1) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression bracket(Object key, Object key1, Object key2) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression bracket(Object key, Object key1, Object key2, Object key3) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression bracket(Object key, Object key1, Object key2, Object key3, Object key4) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1, Object key2) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1, Object key2, Object key3) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1, Object key2, Object key3, Object key4) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression brackets(List<?> subscriptList) {
        throw unsupportedOperation(this);
    }

    @Override
    public final Expression brackets(BiFunction<MappingType, Object, Expression> func, List<?> subscriptList) {
        throw unsupportedOperation(this);
    }


    @Override
    public final Expression space(SQLs.DualOperator operator, Object right) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate space(SQLs.BiOperator operator, Object right) {
        throw unsupportedOperation(this);
    }

    @Override
    public final IPredicate space(SQLs.BiOperator operator, Object right, SQLToken modifier, Object optionalExp) {
        throw unsupportedOperation(this);
    }


    @Override
    public final TypedExpression mapTo(MappingType typeMeta) {
        throw unsupportedOperation(this);
    }


    @Override
    public final TypedExpression castTo(MappingType type) {
        throw unsupportedOperation(this);
    }



    @Override
    public final Selection as(String selectionLabel) {
        if (this instanceof NullWord) {
            return ArmySelections.forExp(this, selectionLabel);
        }
        throw unsupportedOperation(this);
    }

    @Override
    public final SortItem asSortItem() {
        if (!(this instanceof CriteriaContexts.SelectionReference)) {
            throw unsupportedOperation(this);
        }
        return this;
    }

    @Override
    public final SortItem asc() {
        if (!(this instanceof CriteriaContexts.SelectionReference)) {
            throw unsupportedOperation(this);
        }
        return ArmySortItems.create(this, SortOrder.ASC, null);
    }

    @Override
    public final SortItem desc() {
        if (!(this instanceof CriteriaContexts.SelectionReference)) {
            throw unsupportedOperation(this);
        }
        return ArmySortItems.create(this, SortOrder.DESC, null);
    }

    @Override
    public final SortItem ascSpace(@Nullable SQLs.NullsFirstLast firstLast) {
        if (!(this instanceof CriteriaContexts.SelectionReference)) {
            throw unsupportedOperation(this);
        }
        return ArmySortItems.create(this, SortOrder.ASC, firstLast);
    }

    @Override
    public final SortItem descSpace(@Nullable SQLs.NullsFirstLast firstLast) {
        if (!(this instanceof CriteriaContexts.SelectionReference)) {
            throw unsupportedOperation(this);
        }
        return ArmySortItems.create(this, SortOrder.DESC, firstLast);
    }

    String operationErrorMessage() {
        return String.format("%s don't support any operation.", this.getClass().getName());
    }

    /**
     * @see SQLs#NULL
     */
    static SQLs.WordNull nullWord() {
        return NullWord.INSTANCE;
    }

    static Expression updateTimePlaceHolder() {
        return UpdateTimePlaceHolderExpression.PLACEHOLDER;
    }

    static Expression identifier(String identity) {
        if (!_StringUtils.hasText(identity)) {
            throw ContextStack.clearStackAndCriteriaError("identifier must have text");
        }
        return new IdentifierExpression(identity);
    }


    static CriteriaException unsupportedOperation(NonOperationExpression expression) {
        return ContextStack.clearStackAndCriteriaError(expression.operationErrorMessage());
    }

    static RuntimeException nonOperationExpression(final @Nullable Expression expression) {
        final RuntimeException e;
        if (expression == null) {
            e = ContextStack.clearStackAndNullPointer();
        } else if (expression instanceof NonOperationExpression) {
            e = unsupportedOperation((NonOperationExpression) expression);
        } else {
            String m = String.format("%s isn't army expression", expression.getClass().getName());
            e = ContextStack.clearStackAndCriteriaError(m);
        }
        return e;
    }


    static final class IdentifierExpression extends NonOperationExpression implements ArmySimpleExpression {

        private final String identifier;

        private IdentifierExpression(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public void appendSql(StringBuilder sqlBuilder, _SqlContext context) {
            sqlBuilder.append(_Constant.SPACE);
            context.identifier(this.identifier, sqlBuilder);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.identifier);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof IdentifierExpression) {
                match = ((IdentifierExpression) obj).identifier.equals(this.identifier);
            } else {
                match = false;
            }
            return match;
        }

        @Override
        public String toString() {
            return _Constant.SPACE + this.identifier;
        }


    } // SQLIdentifierImpl


    /**
     * <p>
     * This class representing sql {@code NULL} key word.
     *
     * @see SQLs#NULL
     */
    static final class NullWord extends NonOperationExpression
            implements SqlValueParam.SingleAnonymousValue,
            ArmySimpleExpression,
            SQLs.WordNull,
            SQLs.ArmyKeyWord {

        private static final NullWord INSTANCE = new NullWord();


        private NullWord() {
        }

        @Override
        public String spaceRender() {
            return _Constant.SPACE_NULL;
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, _SqlContext context) {
            sqlBuilder.append(_Constant.SPACE_NULL);
        }

        @Override
        public TypeMeta typeMeta() {
            return NullType.INSTANCE;
        }

        @Nullable
        @Override
        public Object value() {
            //always null
            return null;
        }

        @Override
        public String toString() {
            return _Constant.SPACE_NULL;
        }

        @Override
        String operationErrorMessage() {
            return "SQL key word NULL don't support operator";
        }


    } // NullWord


    private static final class UpdateTimePlaceHolderExpression extends NonOperationExpression
            implements ArmySimpleExpression {

        private static final UpdateTimePlaceHolderExpression PLACEHOLDER = new UpdateTimePlaceHolderExpression();

        private UpdateTimePlaceHolderExpression() {
        }


        @Override
        public void appendSql(StringBuilder sqlBuilder, _SqlContext context) {
            throw new CriteriaException("SQLs.UPDATE_TIME_PLACEHOLDER present in error context");
        }


        @Override
        public String toString() {
            if (this != SQLs.UPDATE_TIME_PLACEHOLDER) {
                // no bug,never here
                throw new IllegalStateException();
            }
            return "SQLs.UPDATE_TIME_PLACEHOLDER";
        }


    } // UpdateTimePlaceHolderExpression


}
