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

import io.army.criteria.impl.SQLs;
import io.army.function.BetweenDualOperator;
import io.army.function.BetweenValueOperator;
import io.army.function.ExpressionOperator;
import io.army.function.TeFunction;
import io.army.lang.Nullable;
import io.army.mapping.BooleanType;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


/// 
/// This interface representing sql predicate in WHERE clause or HAVING clause.
/// This interface name is 'IPredicate' not 'Predicate', because of {@link java.util.function.Predicate}.
/// * @since 0.6.0
public interface IPredicate extends TypedExpression, Statement._WhereAndClause<IPredicate> {

    /// @return always return {@link BooleanType#INSTANCE}
    @Override
    BooleanType typeMeta();

    IPredicate or(IPredicate predicate);

    <T> IPredicate or(Function<T, IPredicate> expOperator, T operand);

    IPredicate orGroup(Consumer<Consumer<IPredicate>> consumer);

    IPredicate orGroup(IPredicate t, IPredicate u);

    IPredicate orGroup(IPredicate t, IPredicate u, IPredicate v);

    IPredicate ifOrGroup(Consumer<Consumer<IPredicate>> consumer);

    IPredicate ifOr(Supplier<IPredicate> supplier);

    <T> IPredicate ifOr(Function<T, IPredicate> expOperator, @Nullable T value);

    <T> IPredicate ifOr(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
                        BiFunction<TypedExpression, T, Expression> operator, @Nullable T value);

    /// @param expOperator see {@link TypedExpression#space(SQLs.BiOperator, Object)}
    <T> IPredicate ifOr(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator, SQLs.BiOperator operator, @Nullable T value);

    /// @param expOperator see {@link TypedExpression#space(SQLs.BiOperator, BiFunction, Object)}
    <T> IPredicate ifOr(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator, SQLs.BiOperator operator,
                        BiFunction<TypedExpression, T, Expression> func, @Nullable T value);


    <T> IPredicate ifOr(BetweenValueOperator<T> expOperator, BiFunction<TypedExpression, T, Expression> operator,
                        @Nullable T value1, SQLs.WordAnd and, @Nullable T value2);

    <T, U> IPredicate ifOr(BetweenDualOperator<T, U> expOperator, BiFunction<TypedExpression, T, Expression> firstFuncRef,
                           @Nullable T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef,
                           @Nullable U second);


}
