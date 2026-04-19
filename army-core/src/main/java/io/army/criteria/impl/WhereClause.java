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
import io.army.criteria.impl.inner._Statement;
import io.army.function.BetweenDualOperator;
import io.army.function.BetweenValueOperator;
import io.army.function.ExpressionOperator;
import io.army.function.TeFunction;
import io.army.lang.Nullable;
import io.army.util._Collections;
import io.army.util._Exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>
 * package class
 *
 * @since 0.6.0
 */
@SuppressWarnings("unchecked")
abstract class WhereClause<WR, WA, OR, OD, LR, LO, LF> extends LimitRowOrderByClause<OR, OD, LR, LO, LF>
        implements Statement._WhereClause<WR, WA>
        , Statement._WhereAndClause<WA>
        , UpdateStatement._UpdateWhereAndClause<WA>
        , _Statement._WherePredicateListSpec {

    final CriteriaContext context;


    private List<_Predicate> predicateList;

    WhereClause(CriteriaContext context) {
        super(context);
        this.context = context;
    }


    @Override
    public final WR where(Consumer<Consumer<IPredicate>> consumer) {
        ClauseUtils.invokeConsumer(this::and, consumer);
        if (this.predicateList == null) {
            throw ContextStack.clearStackAnd(_Exceptions::predicateListIsEmpty);
        }
        this.endWhereClauseIfNeed();
        return (WR) this;
    }


    @Override
    public final WA where(IPredicate predicate) {
        if (this.predicateList != null) {
            throw duplicationWhere();
        }
        return this.and(predicate);
    }

    @Override
    public final <T> WA where(Function<T, IPredicate> expOperator, T operand) {
        if (this.predicateList != null) {
            throw duplicationWhere();
        }
        return this.and(expOperator.apply(operand));
    }

    @Override
    public final WA whereIf(Supplier<IPredicate> supplier) {
        if (this.predicateList != null) {
            throw duplicationWhere();
        }
        return this.ifAnd(supplier);
    }

    @Override
    public final <T> WA whereIf(Function<T, IPredicate> expOperator, @Nullable T value) {
        if (this.predicateList != null) {
            throw duplicationWhere();
        }
        return this.ifAnd(expOperator, value);
    }

    @Override
    public final <T> WA whereIf(ExpressionOperator<TypedExpression, T, IPredicate> expOperator, BiFunction<TypedExpression, T, Expression> operator, @Nullable T value) {
        if (this.predicateList != null) {
            throw duplicationWhere();
        }
        return this.ifAnd(expOperator, operator, value);
    }


    @Override
    public final <T> WA whereIf(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator, SQLs.BiOperator operator, @Nullable T value) {
        if (this.predicateList != null) {
            throw duplicationWhere();
        }
        return this.ifAnd(expOperator, operator, value);
    }


    @Override
    public final <T> WA whereIf(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator, SQLs.BiOperator operator, BiFunction<TypedExpression, T, Expression> func, @Nullable T value) {
        if (this.predicateList != null) {
            throw duplicationWhere();
        }
        return this.ifAnd(expOperator, operator, func, value);
    }

    @Override
    public final <T> WA whereIf(BetweenValueOperator<T> expOperator, BiFunction<TypedExpression, T, Expression> operator, @Nullable T value1, SQLs.WordAnd and, @Nullable T value2) {
        if (this.predicateList != null) {
            throw duplicationWhere();
        }
        return this.ifAnd(expOperator, operator, value1, and, value2);
    }

    @Override
    public final <T, U> WA whereIf(BetweenDualOperator<T, U> expOperator, BiFunction<TypedExpression, T, Expression> firstFuncRef, @Nullable T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, @Nullable U second) {
        if (this.predicateList != null) {
            throw duplicationWhere();
        }
        return this.ifAnd(expOperator, firstFuncRef, first, and, secondFuncRef, second);
    }

    @Override
    public final WA and(@Nullable IPredicate predicate) {
        if (predicate == null) {
            throw ContextStack.clearStackAndNullPointer();
        }
        List<_Predicate> predicateList = this.predicateList;
        if (predicateList == null) {
            predicateList = _Collections.arrayList();
            this.predicateList = predicateList;
        } else if (!(predicateList instanceof ArrayList)) {
            throw ContextStack.clearStackAndCastCriteriaApi();
        }
        predicateList.add((OperationPredicate) predicate);
        return (WA) this;
    }

    @Override
    public final <T> WA and(Function<T, IPredicate> expOperator, T operand) {
        return this.and(expOperator.apply(operand));
    }

    @Override
    public final WA ifAnd(Supplier<IPredicate> supplier) {
        final IPredicate predicate;
        predicate = supplier.get();
        if (predicate != null) {
            this.and(predicate);
        }
        return (WA) this;
    }

    @Override
    public final <T> WA ifAnd(Function<T, IPredicate> expOperator, @Nullable T value) {
        if (value != null) {
            this.and(expOperator.apply(value));
        }
        return (WA) this;
    }

    @Override
    public final <T> WA ifAnd(ExpressionOperator<TypedExpression, T, IPredicate> expOperator, BiFunction<TypedExpression, T, Expression> operator, @Nullable T value) {
        if (value != null) {
            this.and(expOperator.apply(operator, value));
        }
        return (WA) this;
    }


    @Override
    public final <T> WA ifAnd(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator, SQLs.BiOperator operator, @Nullable T value) {
        if (value != null) {
            this.and(expOperator.apply(operator, value));
        }
        return (WA) this;
    }


    @Override
    public final <T> WA ifAnd(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator, SQLs.BiOperator operator, BiFunction<TypedExpression, T, Expression> func, @Nullable T value) {
        if (value != null) {
            this.and(expOperator.apply(operator, func, value));
        }
        return (WA) this;
    }

    @Override
    public final <T> WA ifAnd(BetweenValueOperator<T> expOperator, BiFunction<TypedExpression, T, Expression> operator, @Nullable T value1, SQLs.WordAnd and, @Nullable T value2) {
        if (value1 != null && value2 != null) {
            this.and(expOperator.apply(operator, value1, and, value2));
        }
        return (WA) this;
    }

    @Override
    public final <T, U> WA ifAnd(BetweenDualOperator<T, U> expOperator, BiFunction<TypedExpression, T, Expression> firstFuncRef, @Nullable T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, @Nullable U second) {
        if (first != null && second != null) {
            this.and(expOperator.apply(firstFuncRef, first, and, secondFuncRef, second));
        }
        return (WA) this;
    }

    @Override
    public final <T> WA ifAnd(Function<T, Expression> expOperator1, @Nullable T operand1, BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand) {
        if (operand1 != null) {
            this.and(expOperator2.apply(expOperator1.apply(operand1), numberOperand));
        }
        return (WA) this;
    }

    @Override
    public final <T> WA ifAnd(ExpressionOperator<TypedExpression, T, Expression> expOperator1, BiFunction<TypedExpression, T, Expression> operator, @Nullable T operand1, BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand) {
        if (operand1 != null) {
            this.and(expOperator2.apply(expOperator1.apply(operator, operand1), numberOperand));
        }
        return (WA) this;
    }

    @Override
    public final List<_Predicate> wherePredicateList() {
        final List<_Predicate> list = this.predicateList;
        if (list == null || list instanceof ArrayList) {
            throw ContextStack.clearStackAndCastCriteriaApi();
        }
        return list;
    }


    final void clearWhereClause() {
        this.predicateList = null;
    }

    final List<_Predicate> endWhereClauseIfNeed() {
        List<_Predicate> list = this.predicateList;
        if (list instanceof ArrayList) {
            list = _Collections.unmodifiableList(list);
            this.predicateList = list;
        } else if (list == null) {
            if (this instanceof Statement.DmlStatementSpec) {
                //dml statement must have where clause
                throw ContextStack.criteriaError(this.context, _Exceptions::dmlNoWhereClause);
            }
            list = _Collections.emptyList();
            this.predicateList = list;
        }
        return list;
    }

    private CriteriaException duplicationWhere() {
        return ContextStack.clearStackAndCriteriaError("duplication where clause");
    }


    static abstract class WhereClauseClause<WR, WA> extends WhereClause<WR, WA, Object, Object, Object, Object, Object> {

        WhereClauseClause(CriteriaContext context) {
            super(context);
        }


    }//WhereClauseClause


    static final class SimpleWhereClause extends WhereClauseClause<Item, Statement._SimpleWhereAndClause>
            implements Statement._SimpleWhereAndClause,
            Statement._SimpleWhereClause {

        SimpleWhereClause(CriteriaContext context) {
            super(context);
        }

    }//SimpleWhereClause


}
