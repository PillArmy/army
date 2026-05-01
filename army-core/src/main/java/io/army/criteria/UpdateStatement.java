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

package io.army.criteria;


import io.army.function.ExpressionOperator;

import io.army.lang.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/// 
/// This interface representing primary update statement.This interface is base interface of below:
/// 
/// - {@link Update}
/// - {@link BatchUpdate}
/// - {@link io.army.criteria.dialect.ReturningUpdate}
/// - {@link io.army.criteria.dialect.BatchReturningUpdate}
/// 
/// @since 0.6.0
public interface UpdateStatement extends NarrowDmlStatement {

    @Deprecated
    interface _UpdateSpec extends DmlStatement._DmlUpdateSpec<UpdateStatement> {

    }


    interface _ItemPairBuilder {

    }

    interface _DynamicSetClause<B extends _ItemPairBuilder, SR> {
        SR sets(Consumer<B> consumer);

    }

    /// @param <SR> java type of next clause.
    interface _StaticSetClause<F extends SqlField, SR> extends Item {

        SR set(F field, @Nullable Object value);

        <E> SR set(F field, BiFunction<F, E, AssignmentItem> valueOperator, @Nullable E value);

        <E> SR set(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator, BiFunction<F, E, Expression> valueOperator, @Nullable E value);

        SR ifSet(F field, @Nullable Object value);

        <E> SR ifSet(F field, BiFunction<F, E, AssignmentItem> valueOperator, @Nullable E value);

        <E> SR ifSet(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator, BiFunction<F, E, Expression> valueOperator, @Nullable E value);

    }



    /// @param <SR> java type of next clause.
    interface _StaticBatchSetClause<F extends SqlField, SR> extends _StaticSetClause<F, SR> {


        SR setSpace(F field, BiFunction<F, String, Expression> valueOperator);

        SR setSpace(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator, BiFunction<F, String, Expression> valueOperator);

    }


    interface _StaticRowSetClause<F extends SqlField, SR> extends _StaticSetClause<F, SR> {

        SR setRow(F field1, F field2, SubQuery subQuery);

        SR setRow(F field1, F field2, F field3, SubQuery subQuery);

        SR setRow(F field1, F field2, F field3, F field4, SubQuery subQuery);

        SR setRow(List<F> fieldList, SubQuery subQuery);

        SR setRow(Consumer<Consumer<F>> consumer, SubQuery subQuery);

        SR ifSetRow(F field1, F field2, Supplier<SubQuery> supplier);

        SR ifSetRow(F field1, F field2, F field3, Supplier<SubQuery> supplier);

        SR ifSetRow(F field1, F field2, F field3, F field4, Supplier<SubQuery> supplier);

        SR ifSetRow(List<F> fieldList, Supplier<SubQuery> supplier);

        SR ifSetRow(Consumer<Consumer<F>> consumer, Supplier<SubQuery> supplier);

    }


    interface _StaticBatchRowSetClause<F extends SqlField, SR> extends _StaticRowSetClause<F, SR>,
            _StaticBatchSetClause<F, SR> {

    }


    interface _ItemPairs<F extends SqlField> extends _ItemPairBuilder,
            _StaticSetClause<F, _ItemPairs<F>> {


    }

    interface _BatchItemPairs<F extends SqlField> extends _ItemPairBuilder,
            _StaticBatchSetClause<F, _BatchItemPairs<F>> {


    }

    interface _RowPairs<F extends SqlField> extends _ItemPairBuilder,
            _StaticRowSetClause<F, _RowPairs<F>> {


    }

    interface _BatchRowPairs<F extends SqlField> extends _ItemPairBuilder,
            _StaticRowSetClause<F, _BatchRowPairs<F>>,
            _StaticBatchSetClause<F, _BatchRowPairs<F>> {


    }

    interface _UpdateWhereAndClause<WA> extends Statement._WhereAndClause<WA> {

/// @param numberOperand see 
/// - {@link io.army.criteria.impl.SQLs#LITERAL_0}
/// - {@link io.army.criteria.impl.SQLs#LITERAL_DECIMAL_0}
/// - {@link io.army.criteria.impl.SQLs#PARAM_0}
/// - {@link io.army.criteria.impl.SQLs#PARAM_DECIMAL_0}
/// 
        <T> WA ifAnd(Function<T, Expression> expOperator1, @Nullable T operand1,
                     BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand);

/// @param numberOperand see 
/// - {@link io.army.criteria.impl.SQLs#LITERAL_0}
/// - {@link io.army.criteria.impl.SQLs#LITERAL_DECIMAL_0}
/// - {@link io.army.criteria.impl.SQLs#PARAM_0}
/// - {@link io.army.criteria.impl.SQLs#PARAM_DECIMAL_0}
/// 
        <T> WA ifAnd(ExpressionOperator<TypedExpression, T, Expression> expOperator1,
                     BiFunction<TypedExpression, T, Expression> operator, @Nullable T operand1,
                     BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand);


    } // _UpdateWhereAndClause


}
