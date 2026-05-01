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
import io.army.function.TeNamedParamsFunc;
import io.army.lang.Nullable;
import io.army.mapping.StringType;

import java.util.Collection;
import java.util.function.BiFunction;

import static io.army.dialect.Database.H2;
import static io.army.dialect.Database.PostgreSQL;


/// This interface is base interface of following :
/// 
/// - {@link TypedField}
/// - {@link ValueExpression}
/// - {@link IPredicate}
/// 
public interface TypedExpression extends Expression, TypeInfer {


    ///
    /// {@code =} operator
    ///
    /// @param funcRef right operand , one of below :
    /// 1. {@link SQLs#param(TypeInfer, Object)}
    /// 2. {@link SQLs#literal(TypeInfer, Object)}
    /// 3. {@link SQLs#constant(TypeInfer, Object)}
    /// 4. {@link SQLs#namedParam(TypeInfer, String)}
    /// 5. {@link SQLs#namedLiteral(TypeInfer, String)}
    /// 6. {@link SQLs#namedConst(TypeInfer, String)}
    /// 7. Your custom function
    /// @throws CriteriaException throw when Operand isn't operable {@link Expression},for example {@link SQLs#DEFAULT},
    ///                          {@link SQLs#rowParam(TypeInfer, Collection)}
    /// @see Expression#equal(Object)
    /// @see TypedField#spaceEqual(BiFunction)
    ///
    <T> IPredicate equal(BiFunction<TypedExpression, T, Expression> funcRef, T value);


    <T> IPredicate notEqual(BiFunction<TypedExpression, T, Expression> funcRef, T value);


    <T> IPredicate nullSafeEqual(BiFunction<TypedExpression, T, Expression> funcRef, @Nullable T value);


    <T> IPredicate less(BiFunction<TypedExpression, T, Expression> funcRef, T value);


    <T> IPredicate lessEqual(BiFunction<TypedExpression, T, Expression> funcRef, T value);


    <T> IPredicate greater(BiFunction<TypedExpression, T, Expression> funcRef, T value);


    <T> IPredicate greaterEqual(BiFunction<TypedExpression, T, Expression> funcRef, T value);

    /// @param and see {@link SQLs#AND}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-comparison.html#FUNCTIONS-COMPARISON-PRED-TABLE">Postgres BETWEEN</a>
    <T> IPredicate between(BiFunction<TypedExpression, T, Expression> funcRef, T first, SQLs.WordAnd and, T second);

    /// @param and      see {@link SQLs#AND}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-comparison.html#FUNCTIONS-COMPARISON-PRED-TABLE">Postgres BETWEEN</a>
    <T, U> IPredicate between(BiFunction<TypedExpression, T, Expression> firstFuncRef, T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second);

    /// @param and      see {@link SQLs#AND}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-comparison.html#FUNCTIONS-COMPARISON-PRED-TABLE">Postgres BETWEEN</a>
    <T> IPredicate notBetween(BiFunction<TypedExpression, T, Expression> funcRef, T first, SQLs.WordAnd and, T second);

    /// @param and      see {@link SQLs#AND}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-comparison.html#FUNCTIONS-COMPARISON-PRED-TABLE">Postgres BETWEEN</a>
    <T, U> IPredicate notBetween(BiFunction<TypedExpression, T, Expression> firstFuncRef, T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second);


    /// @param and      see {@link SQLs#AND}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-comparison.html#FUNCTIONS-COMPARISON-PRED-TABLE">Postgres BETWEEN</a>
    @Support({PostgreSQL, H2})
    <T> IPredicate between(@Nullable SQLs.BetweenModifier modifier, BiFunction<TypedExpression, T, Expression> funcRef, T first, SQLs.WordAnd and, T second);

    @Support({PostgreSQL, H2})
    <T, U> IPredicate between(@Nullable SQLs.BetweenModifier modifier, BiFunction<TypedExpression, T, Expression> firstFuncRef, T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second);


    @Support({PostgreSQL, H2})
    <T> IPredicate notBetween(@Nullable SQLs.BetweenModifier modifier, BiFunction<TypedExpression, T, Expression> funcRef, T first, SQLs.WordAnd and, T second);


    @Support({PostgreSQL, H2})
    <T, U> IPredicate notBetween(@Nullable SQLs.BetweenModifier modifier, BiFunction<TypedExpression, T, Expression> firstFuncRef, T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second);


    @Support({PostgreSQL, H2})
    <T> IPredicate is(SQLs.IsComparisonWord operator, BiFunction<TypedExpression, T, Expression> funcRef, @Nullable T value);


    @Support({PostgreSQL, H2})
    <T> IPredicate isNot(SQLs.IsComparisonWord operator, BiFunction<TypedExpression, T, Expression> funcRef, @Nullable T value);

    IPredicate in(BiFunction<TypedExpression, Collection<?>, RowExpression> funcRef, Collection<?> value);

    IPredicate notIn(BiFunction<TypedExpression, Collection<?>, RowExpression> funcRef, Collection<?> value);

    IPredicate in(TeNamedParamsFunc<TypedExpression> funcRef, String paramName, int size);

    IPredicate notIn(TeNamedParamsFunc<TypedExpression> funcRef, String paramName, int size);

    <T> Expression mod(BiFunction<TypedExpression, T, Expression> funcRef, T value);

    <T> Expression times(BiFunction<TypedExpression, T, Expression> funcRef, T value);

    <T> Expression plus(BiFunction<TypedExpression, T, Expression> funcRef, T value);

    <T> Expression minus(BiFunction<TypedExpression, T, Expression> funcRef, T value);

    <T> Expression divide(BiFunction<TypedExpression, T, Expression> funcRef, T value);

    <T> Expression bitwiseAnd(BiFunction<TypedExpression, T, Expression> funcRef, T value);

    <T> Expression bitwiseOr(BiFunction<TypedExpression, T, Expression> funcRef, T value);

    <T> Expression bitwiseXor(BiFunction<TypedExpression, T, Expression> funcRef, T value);

    <T> Expression rightShift(BiFunction<TypedExpression, T, Expression> funcRef, T value);

    <T> Expression leftShift(BiFunction<TypedExpression, T, Expression> funcRef, T value);

    <T> IPredicate like(BiFunction<TypedExpression, T, Expression> funcRef, T value);

    <T> IPredicate like(BiFunction<TypedExpression, T, Expression> funcRef, T value, SQLs.WordEscape escape, T escapeChar);

    <T> IPredicate notLike(BiFunction<TypedExpression, T, Expression> funcRef, T value);

    <T> IPredicate notLike(BiFunction<TypedExpression, T, Expression> funcRef, T value, SQLs.WordEscape escape, T escapeChar);

    @Support({PostgreSQL})
    <T> IPredicate similarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value);

    @Support({PostgreSQL})
    <T> IPredicate similarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value, SQLs.WordEscape escape, T escapeChar);

    @Support({PostgreSQL})
    <T> IPredicate notSimilarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value);

    @Support({PostgreSQL})
    <T> IPredicate notSimilarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value, SQLs.WordEscape escape, T escapeChar);


    /*-------------------below dialect operator method-------------------*/

    <T> Expression space(SQLs.DualOperator operator, BiFunction<TypedExpression, T, Expression> funcRef, T value);


    <T> IPredicate space(SQLs.BiOperator operator, BiFunction<TypedExpression, T, Expression> funcRef, T right);

    ///
    /// @see <a href="https://www.postgresql.org/docs/current/functions-matching.html#FUNCTIONS-SIMILARTO-REGEXP">SIMILAR TO Regular Expressions</a>
    <T> IPredicate space(SQLs.BiOperator operator, BiFunction<StringType, T, Expression> funcRef, T right, SQLToken modifier, T optionalExp);


}
