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
import io.army.criteria.impl.inner._Predicate;
import io.army.dialect._Constant;
import io.army.dialect._SqlContext;
import io.army.function.BetweenDualOperator;
import io.army.function.BetweenValueOperator;
import io.army.function.ExpressionOperator;
import io.army.function.TeFunction;
import io.army.lang.Nullable;
import io.army.meta.ChildTableMeta;
import io.army.meta.FieldMeta;
import io.army.meta.PrimaryFieldMeta;
import io.army.meta.TableMeta;
import io.army.modelgen._MetaBridge;
import io.army.util._Collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/// This class is base class of all {@link IPredicate} implementation .
abstract class OperationPredicate extends OperationExpression.PredicateExpression {

    /// 
/// Private constructor .
/// @see OperationSimplePredicate#OperationSimplePredicate()
/// @see OperationCompoundPredicate#OperationPredicate()
    private OperationPredicate() {

    }


    @Override
    public final IPredicate or(final @Nullable IPredicate predicate) {
        if (predicate == null) {
            throw ContextStack.clearStackAndNullPointer();
        }
        return orPredicate(this, predicate);
    }

    @Override
    public final <T> IPredicate or(Function<T, IPredicate> expOperator, T operand) {
        return this.or(expOperator.apply(operand));
    }


    @Override
    public final IPredicate orGroup(Consumer<Consumer<IPredicate>> consumer) {
        final List<IPredicate> list = _Collections.arrayList();
        ClauseUtils.invokeConsumer(list::add, consumer);
        if (list.isEmpty()) {
            throw CriteriaUtils.dontAddAnyItem();
        }
        return orGroupPredicate(this, list);
    }

    @Override
    public final IPredicate orGroup(IPredicate t, IPredicate u) {
        return orGroupPredicate(this, List.of(t, u));
    }

    @Override
    public final IPredicate orGroup(IPredicate t, IPredicate u, IPredicate v) {
        return orGroupPredicate(this, List.of(t, u, v));
    }

    @Override
    public final IPredicate ifOrGroup(Consumer<Consumer<IPredicate>> consumer) {
        final List<IPredicate> list = _Collections.arrayList();
        ClauseUtils.invokeConsumer(list::add, consumer);
        if (list.isEmpty()) {
            return this;
        }
        return orGroupPredicate(this, list);
    }

    @Override
    public final IPredicate ifOr(Supplier<IPredicate> supplier) {
        IPredicate predicate;
        if ((predicate = supplier.get()) == null) {
            predicate = this;
        } else {
            predicate = this.or(predicate);
        }
        return predicate;
    }

    @Override
    public final <T> IPredicate ifOr(Function<T, IPredicate> expOperator, @Nullable T value) {
        final IPredicate predicate;
        if (value == null) {
            predicate = this;
        } else {
            predicate = this.or(expOperator.apply(value));
        }
        return predicate;
    }

    @Override
    public final <T> IPredicate ifOr(ExpressionOperator<TypedExpression, T, IPredicate> expOperator, BiFunction<TypedExpression, T, Expression> operator, @Nullable T value) {
        final IPredicate predicate;
        if (value == null) {
            predicate = this;
        } else {
            predicate = this.or(expOperator.apply(operator, value));
        }
        return predicate;
    }


    @Override
    public final <T> IPredicate ifOr(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator, SQLs.BiOperator operator, @Nullable T value) {
        final IPredicate predicate;
        if (value == null) {
            predicate = this;
        } else {
            predicate = this.or(expOperator.apply(operator, value));
        }
        return predicate;
    }


    @Override
    public final <T> IPredicate ifOr(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator, SQLs.BiOperator operator, BiFunction<TypedExpression, T, Expression> func, @Nullable T value) {
        final IPredicate predicate;
        if (value == null) {
            predicate = this;
        } else {
            predicate = this.or(expOperator.apply(operator, func, value));
        }
        return predicate;
    }

    @Override
    public final <T> IPredicate ifOr(BetweenValueOperator<T> expOperator, BiFunction<TypedExpression, T, Expression> operator, @Nullable T value1, SQLs.WordAnd and, @Nullable T value2) {
        final IPredicate predicate;
        if (value1 == null || value2 == null) {
            predicate = this;
        } else {
            predicate = this.or(expOperator.apply(operator, value1, and, value2));
        }
        return predicate;
    }

    @Override
    public final <T, U> IPredicate ifOr(BetweenDualOperator<T, U> expOperator, BiFunction<TypedExpression, T, Expression> firstFuncRef, @Nullable T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, @Nullable U second) {
        final IPredicate predicate;
        if (first == null || second == null) {
            predicate = this;
        } else {
            predicate = this.or(expOperator.apply(firstFuncRef, first, and, secondFuncRef, second));
        }
        return predicate;
    }

    @Override
    public final IPredicate and(final @Nullable IPredicate predicate) {
        if (predicate == null) {
            throw ContextStack.clearStackAndNullPointer();
        }
        return andPredicate(this, predicate);
    }


    @Override
    public final IPredicate ifAnd(Supplier<IPredicate> supplier) {
        final IPredicate predicate;
        final IPredicate operand;
        if ((operand = supplier.get()) == null) {
            predicate = this;
        } else {
            predicate = andPredicate(this, operand);
        }
        return predicate;
    }

    @Override
    public final <T> IPredicate and(Function<T, IPredicate> expOperator, T operand) {
        return this.and(expOperator.apply(operand));
    }


    @Override
    public final <T> IPredicate ifAnd(Function<T, IPredicate> expOperator, @Nullable T value) {
        final IPredicate predicate;
        if (value == null) {
            predicate = this;
        } else {
            predicate = this.and(expOperator.apply(value));
        }
        return predicate;
    }

    @Override
    public final <T> IPredicate ifAnd(ExpressionOperator<TypedExpression, T, IPredicate> expOperator, BiFunction<TypedExpression, T, Expression> operator, @Nullable T value) {
        final IPredicate predicate;
        if (value == null) {
            predicate = this;
        } else {
            predicate = this.and(expOperator.apply(operator, value));
        }
        return predicate;
    }


    @Override
    public final <T> IPredicate ifAnd(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator, SQLs.BiOperator operator, @Nullable T value) {
        final IPredicate predicate;
        if (value == null) {
            predicate = this;
        } else {
            predicate = this.and(expOperator.apply(operator, value));
        }
        return predicate;
    }


    @Override
    public final <T> IPredicate ifAnd(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator, SQLs.BiOperator operator, BiFunction<TypedExpression, T, Expression> func, @Nullable T value) {
        final IPredicate predicate;
        if (value == null) {
            predicate = this;
        } else {
            predicate = this.and(expOperator.apply(operator, func, value));
        }
        return predicate;
    }

    @Override
    public final <T> IPredicate ifAnd(BetweenValueOperator<T> expOperator, BiFunction<TypedExpression, T, Expression> operator, @Nullable T value1, SQLs.WordAnd and, @Nullable T value2) {
        final IPredicate predicate;
        if (value1 == null || value2 == null) {
            predicate = this;
        } else {
            predicate = this.and(expOperator.apply(operator, value1, and, value2));
        }
        return predicate;
    }

    @Override
    public final <T, U> IPredicate ifAnd(BetweenDualOperator<T, U> expOperator, BiFunction<TypedExpression, T, Expression> firstFuncRef, @Nullable T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, @Nullable U second) {
        final IPredicate predicate;
        if (first == null || second == null) {
            predicate = this;
        } else {
            predicate = this.and(expOperator.apply(firstFuncRef, first, and, secondFuncRef, second));
        }
        return predicate;
    }

    @Override
    public final boolean isOptimistic() {
        final boolean match;
        final Expressions.DualPredicate predicate;
        if (!(this instanceof Expressions.DualPredicate)
                || (predicate = (Expressions.DualPredicate) this).operator != DualBooleanOperator.EQUAL) {
            match = false;
        } else if (predicate.left instanceof TableField
                && _MetaBridge.VERSION.equals(((TableField) predicate.left).fieldName())) {
            match = predicate.right instanceof SqlValueParam.SingleValue
                    || predicate.right instanceof NamedParam;
        } else if (predicate.right instanceof TableField
                && _MetaBridge.VERSION.equals(((TableField) predicate.right).fieldName())) {
            match = predicate.left instanceof SqlValueParam.SingleValue
                    || predicate.left instanceof NamedParam;

        } else {
            match = false;
        }
        return match;
    }

    @Override
    public final _Predicate getIdPredicate() {
        final _Predicate predicate;
        if (this instanceof Expressions.DualPredicate) {
            predicate = getIdPredicateFromDual((Expressions.DualPredicate) this);
        } else if (this instanceof Expressions.InOperationPredicate) {
            predicate = getIdPredicateFromInPredicate((Expressions.InOperationPredicate) this);
        } else if (this instanceof AndPredicate) {
            predicate = getIdPredicateFromAndPredicate((AndPredicate) this);
        } else {
            predicate = null;
        }
        return predicate;
    }


    @Override
    public final TableField findParentId(final ChildTableMeta<?> child, final String alias) {
        final TableField parentId;
        final Expressions.DualPredicate predicate;
        final TableMeta<?> leftTable, rightTable;
        final TableField leftField, rightField;


        if (!(this instanceof Expressions.DualPredicate) || (predicate = (Expressions.DualPredicate) this).operator != DualBooleanOperator.EQUAL) {
            parentId = null;
        } else if (!(predicate.left instanceof TableField && predicate.right instanceof TableField)) {
            parentId = null;
        } else if (!((leftField = (TableField) predicate.left).fieldName().equals(_MetaBridge.ID)
                && (rightField = (TableField) predicate.right).fieldName().equals(_MetaBridge.ID))) {
            parentId = null;
        } else if ((leftTable = leftField.tableMeta()) != child & (rightTable = rightField.tableMeta()) != child) {
            parentId = null;
        } else if ((leftTable == child && rightTable == child.parentMeta())) {
            if (leftField instanceof FieldMeta || ((QualifiedField<?>) leftField).tableAlias().equals(alias)) {
                parentId = rightField;
            } else {
                parentId = null;
            }
        } else if (rightTable == child && leftTable == child.parentMeta()) {
            if (rightField instanceof FieldMeta || ((QualifiedField<?>) rightField).tableAlias().equals(alias)) {
                parentId = leftField;
            } else {
                parentId = null;
            }
        } else {
            parentId = null;
        }
        return parentId;
    }

    static OperationSimplePredicate bracketPredicate(final IPredicate predicate) {
        final OperationSimplePredicate result;
        if (predicate instanceof BracketPredicate) {
            result = (BracketPredicate) predicate;
        } else {
            result = new BracketPredicate((OperationPredicate) predicate);
        }
        return result;
    }

    static OperationSimplePredicate orPredicate(OperationPredicate left, IPredicate right) {
        return new OrPredicate(left, Collections.singletonList((OperationPredicate) right));
    }

    static OperationSimplePredicate orGroupPredicate(OperationPredicate left, List<IPredicate> rightList) {
        final int size = rightList.size();
        assert size > 0;
        final List<OperationPredicate> list = new ArrayList<>(size);
        for (IPredicate right : rightList) {
            list.add((OperationPredicate) right);
        }
        return new OrPredicate(left, Collections.unmodifiableList(list));
    }


    static AndPredicate andPredicate(OperationPredicate left, @Nullable IPredicate right) {
        assert right != null;
        return new AndPredicate(left, (OperationPredicate) right);
    }

    static OperationPredicate notPredicate(final IPredicate predicate) {
        return new NotPredicate((OperationPredicate) predicate);
    }


    /// @see SQLs#TRUE
/// @see SQLs#FALSE
    static SQLs.WordBoolean booleanWord(final boolean value) {
        return value ? BooleanWord.TRUE : BooleanWord.FALSE;
    }


    @Nullable
    private static _Predicate getIdPredicateFromAndPredicate(final AndPredicate andPredicate) {
        _Predicate predicate = null;
        if (andPredicate.left instanceof Expressions.DualPredicate) {
            predicate = getIdPredicateFromDual((Expressions.DualPredicate) andPredicate.left);
        } else if (andPredicate.left instanceof Expressions.InOperationPredicate) {
            predicate = getIdPredicateFromInPredicate((Expressions.InOperationPredicate) andPredicate.left);
        } else if (andPredicate.left instanceof AndPredicate) {
            predicate = getIdPredicateFromAndPredicate((AndPredicate) andPredicate.left);
        }
        if (predicate != null) {
            return predicate;
        }

        if (andPredicate.right instanceof Expressions.DualPredicate) {
            predicate = getIdPredicateFromDual((Expressions.DualPredicate) andPredicate.right);
        } else if (andPredicate.right instanceof Expressions.InOperationPredicate) {
            predicate = getIdPredicateFromInPredicate((Expressions.InOperationPredicate) andPredicate.right);
        } else if (andPredicate.right instanceof AndPredicate) {
            predicate = getIdPredicateFromAndPredicate((AndPredicate) andPredicate.right);
        }
        return predicate;
    }

    @Nullable
    private static _Predicate getIdPredicateFromInPredicate(final Expressions.InOperationPredicate predicate) {
        final _Predicate result;
        if (!(predicate.left instanceof PrimaryFieldMeta)) {
            result = null;
        } else if (predicate.not) {
            result = null;
        } else if (predicate.right instanceof RowValueExpression) {
            result = predicate;
        } else {
            result = null;
        }
        return result;
    }

    @Nullable
    private static _Predicate getIdPredicateFromDual(final Expressions.DualPredicate predicate) {
        final _Predicate result;
        if (!(predicate.left instanceof PrimaryFieldMeta)) {
            result = null;
        } else if (predicate.operator == DualBooleanOperator.EQUAL && predicate.right instanceof ValueExpression) {
            result = predicate;
        } else {
            result = null;
        }
        return result;
    }

/// 
/// Private class.This class is base class of below:
/// - {@link BooleanWord}
/// - {@link BracketPredicate}
/// - {@link SqlFunctionPredicate}
/// - {@link OrPredicate},because OR/XOR operator always have outer parentheses。
    static abstract class OperationSimplePredicate extends OperationPredicate
            implements SimplePredicate, ArmySimpleExpression {

/// 
/// **Private constructor**
        private OperationSimplePredicate() {
        }


    }//OperationSimplePredicate


    static abstract class SqlFunctionPredicate extends OperationSimplePredicate implements ArmySQLFunction {

        final String name;

        private final boolean buildIn;

        SqlFunctionPredicate(String name, boolean buildIn) {
            this.name = name;
            this.buildIn = buildIn;
        }

        @Override
        public final String name() {
            return this.name;
        }


        @Override
        public final void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {

            context.appendFuncName(this.buildIn, this.name);

            if (this instanceof FunctionUtils.NoParensFunction) {
                return;
            }

            if (this instanceof FunctionUtils.NoArgFunction) {
                sqlBuilder.append(_Constant.PARENS);
            } else {
                sqlBuilder.append(_Constant.LEFT_PAREN);
                this.appendArg(sqlBuilder, context);
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }

            if (this instanceof FunctionUtils.FunctionOuterClause) {
                ((FunctionUtils.FunctionOuterClause) this).appendFuncRest(sqlBuilder, context);
            }

        }


        @Override
        public final String toString() {
            final StringBuilder builder = new StringBuilder();

            builder.append(_Constant.SPACE)
                    .append(this.name); // function name
            if (!(this instanceof FunctionUtils.NoParensFunction)) {
                if (this instanceof FunctionUtils.NoArgFunction) {
                    builder.append(_Constant.RIGHT_PAREN);
                } else {
                    builder.append(_Constant.LEFT_PAREN);
                    argToString(builder);
                    builder.append(_Constant.SPACE_RIGHT_PAREN);
                }
            }
            if (this instanceof FunctionUtils.FunctionOuterClause) {
                ((FunctionUtils.FunctionOuterClause) this).funcRestToString(builder);
            }
            return builder.toString();
        }

        abstract void appendArg(StringBuilder sqlBuilder, _SqlContext context);


        abstract void argToString(StringBuilder builder);


    }//SqlFunctionPredicate


    static abstract class OperationCompoundPredicate extends OperationPredicate implements CompoundPredicate {

        OperationCompoundPredicate() {
        }


    }// CompoundPredicate


    private static final class BracketPredicate extends OperationSimplePredicate {

        private final OperationPredicate predicate;

        private BracketPredicate(OperationPredicate predicate) {
            this.predicate = predicate;
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);

            this.predicate.appendSql(sqlBuilder, context);

            sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);

        }


    } //BracketPredicate

    private static final class OrPredicate extends OperationSimplePredicate {

        private final OperationPredicate left;

        private final List<OperationPredicate> rightList;

        private OrPredicate(OperationPredicate left, List<OperationPredicate> rightList) {
            this.left = left;
            this.rightList = rightList;
        }


        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            this.appendOrPredicate(sqlBuilder, context);
        }


        @Override
        public String toString() {
            final StringBuilder builder;
            builder = new StringBuilder();
            this.appendOrPredicate(builder, null);
            return builder.toString();
        }

        private void appendOrPredicate(final StringBuilder sqlBuilder, final @Nullable _SqlContext context) {
            sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);// outer left paren

            final OperationPredicate left = this.left;

            if (context == null) {
                sqlBuilder.append(left);
            } else {
                left.appendSql(sqlBuilder, context);
            }

            boolean rightInnerParen;
            for (OperationPredicate right : this.rightList) {

                sqlBuilder.append(_Constant.SPACE_OR);
                rightInnerParen = right instanceof AndPredicate;
                if (rightInnerParen) {
                    sqlBuilder.append(_Constant.SPACE_LEFT_PAREN); // inner left bracket
                }

                if (context == null) {
                    sqlBuilder.append(right);
                } else {
                    right.appendSql(sqlBuilder, context);
                }

                if (rightInnerParen) {
                    sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);// inner right bracket
                }
            }

            sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN); // outer right paren
        }


    }//OrPredicate

    private static final class AndPredicate extends OperationCompoundPredicate {

        final OperationPredicate left;

        final OperationPredicate right;

        private AndPredicate(OperationPredicate left, OperationPredicate right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            // 1. append left operand
            this.left.appendSql(sqlBuilder, context);

            // 2. append AND operator
            sqlBuilder.append(_Constant.SPACE_AND);

            final OperationPredicate right = this.right;
            final boolean rightOuterParens = right instanceof AndPredicate;

            if (rightOuterParens) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }
            // 3. append right operand
            right.appendSql(sqlBuilder, context);
            if (rightOuterParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }

        }


        @Override
        public String toString() {
            final StringBuilder sqlBuilder = new StringBuilder();

            // 1. append left operand
            sqlBuilder.append(this.left);
            // 2. append AND operator
            sqlBuilder.append(_Constant.SPACE_AND);

            final OperationPredicate right = this.right;
            final boolean rightOuterParens = right instanceof AndPredicate;

            if (rightOuterParens) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }
            // 3. append right operand
            sqlBuilder.append(right);
            if (rightOuterParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }
            return sqlBuilder.toString();
        }


    }//AndPredicate

    private static final class NotPredicate extends OperationCompoundPredicate {

        private final OperationPredicate predicate;

        /// @see #notPredicate(IPredicate)
        private NotPredicate(OperationPredicate predicate) {
            this.predicate = predicate;
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            sqlBuilder.append(_Constant.SPACE_NOT);

            final OperationPredicate predicate = this.predicate;
            final boolean operandOuterParens = predicate instanceof AndPredicate;

            if (operandOuterParens) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }
            predicate.appendSql(sqlBuilder, context);

            if (operandOuterParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }

        }


        @Override
        public String toString() {
            final StringBuilder builder;
            builder = new StringBuilder()
                    .append(" NOT");

            final OperationPredicate predicate = this.predicate;
            final boolean operandOuterParens = predicate instanceof AndPredicate;

            if (operandOuterParens) {
                builder.append(_Constant.SPACE_LEFT_PAREN);
            }
            builder.append(this.predicate);

            if (operandOuterParens) {
                builder.append(_Constant.SPACE_RIGHT_PAREN);
            }
            return builder.toString();
        }


    }//NotPredicate


    /// @see SQLs#TRUE
/// @see SQLs#FALSE
    static final class BooleanWord extends OperationSimplePredicate
            implements SQLs.WordBoolean, SQLs.ArmyKeyWord, _LiteralExpression, SqlValueParam.SingleAnonymousValue {

        private static final BooleanWord TRUE = new BooleanWord(true);

        private static final BooleanWord FALSE = new BooleanWord(false);

        private final boolean value;

        private final String spaceWord;

        private BooleanWord(boolean value) {
            this.value = value;
            this.spaceWord = value ? " TRUE" : " FALSE";
        }

        @Override
        public String spaceRender() {
            return this.spaceWord;
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            sqlBuilder.append(this.spaceWord);
        }


        @Override
        public void appendSqlWithoutType(StringBuilder sqlBuilder, _SqlContext context) {
            sqlBuilder.append(this.spaceWord);
        }

        @Override
        public Object value() {
            return this.value;
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof BooleanWord o) {
                match = o.value == this.value;
            } else {
                match = false;
            }
            return match;
        }

        @Override
        public int hashCode() {
            return Boolean.hashCode(this.value);
        }

        @Override
        public String toString() {
            return this.spaceWord;
        }


    }//BooleanWord


}
