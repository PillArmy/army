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

package io.army.criteria;

import io.army.criteria.impl.SQLs;
import io.army.function.OptionalClauseOperator;
import io.army.function.TeFunction;
import io.army.lang.Nullable;
import io.army.mapping.IntegerType;
import io.army.mapping.MappingType;
import io.army.mapping.StringType;
import io.army.meta.FieldMeta;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static io.army.dialect.Database.*;

/**
 * Interface representing the sql expression, eg: column,function.
 * <p> This interface is the base interface of below"
 * <ul>
 *     <li>{@link SimpleExpression}</li>
 *     <li>{@link CompoundExpression}</li>
 *     <li>{@link IPredicate}</li>
 * </ul>
 *
 * @see FieldMeta
 * @since 0.6.0
 */
@SuppressWarnings("unused")
public interface Expression extends SQLExpression, SortItem,
        GroupByItem.ExpressionItem, RightOperand, AssignmentItem, SelectionSpec {


    /**
     * <p>
     * <strong>=</strong> operator
     *
     * @param operand non-null
     * @throws CriteriaException throw when Operand isn't operable {@link Expression},for example {@link SQLs#DEFAULT},
     *                           {@link SQLs#rowParam(TypeInfer, Collection)}
     * @see TypedExpression#equal(BiFunction, Object)
     * @see TypedField#spaceEqual(BiFunction)
     */
    CompoundPredicate equal(Expression operand);

    CompoundPredicate notEqual(Expression operand);

    @Support({MySQL, PostgreSQL, H2})
    CompoundPredicate nullSafeEqual(Expression operand);

    /**
     * <p>
     * <strong>&lt;</strong> operator
     *
     * @param operand non-null
     * @throws CriteriaException throw when Operand isn't operable {@link Expression},for example {@link SQLs#DEFAULT},
     *                           {@link SQLs#rowParam(TypeInfer, Collection)}
     */
    CompoundPredicate less(Expression operand);


    CompoundPredicate lessEqual(Expression operand);


    CompoundPredicate greater(Expression operand);

    CompoundPredicate greaterEqual(Expression operand);


    /**
     * @param and {@link SQLs#AND}
     */
    CompoundPredicate between(Expression first, SQLs.WordAnd and, Expression second);


    /**
     * @param and {@link SQLs#AND}
     */
    CompoundPredicate notBetween(Expression first, SQLs.WordAnd and, Expression second);

    /**
     * @param and {@link SQLs#AND}
     */
    @Support({PostgreSQL, H2})
    CompoundPredicate between(@Nullable SQLs.BetweenModifier modifier, Expression first, SQLs.WordAnd and, Expression second);

    /**
     * @param and {@link SQLs#AND}
     */
    @Support({PostgreSQL, H2})
    CompoundPredicate notBetween(@Nullable SQLs.BetweenModifier modifier, Expression first, SQLs.WordAnd and, Expression second);

    /**
     * @param operand <ul>
     *                <li>{@link SQLs#TRUE}</li>
     *                <li>{@link SQLs#FALSE}</li>
     *                <li>{@link SQLs#UNKNOWN}</li>
     *                <li>{@link SQLs#NULL}</li>
     *                <li>other</li>
     *                </ul>
     */
    CompoundPredicate is(SQLs.BooleanTestWord operand);

    /**
     * @param operand <ul>
     *                <li>{@link SQLs#TRUE}</li>
     *                <li>{@link SQLs#FALSE}</li>
     *                <li>{@link SQLs#UNKNOWN}</li>
     *                <li>{@link SQLs#NULL}</li>
     *                <li>other</li>
     *                </ul>
     */
    CompoundPredicate isNot(SQLs.BooleanTestWord operand);

    CompoundPredicate isNull();

    CompoundPredicate isNotNull();

    /**
     * @param operator see <ul>
     *                 <li>{@link SQLs#DISTINCT_FROM}</li>
     *                 </ul>
     */
    @Support({PostgreSQL, H2})
    CompoundPredicate is(SQLs.IsComparisonWord operator, Expression operand);

    /**
     * @param operator see <ul>
     *                 <li>{@link SQLs#DISTINCT_FROM}</li>
     *                 </ul>
     */
    @Support({PostgreSQL, H2})
    CompoundPredicate isNot(SQLs.IsComparisonWord operator, Expression operand);

    CompoundPredicate like(Expression pattern);

    CompoundPredicate like(Expression pattern, SQLs.WordEscape escape, char escapeChar);

    CompoundPredicate like(Expression pattern, SQLs.WordEscape escape, Expression escapeChar);

    CompoundPredicate notLike(Expression pattern);

    CompoundPredicate notLike(Expression pattern, SQLs.WordEscape escape, char escapeChar);

    CompoundPredicate notLike(Expression pattern, SQLs.WordEscape escape, Expression escapeChar);

    CompoundExpression mod(Expression operand);

    CompoundExpression times(Expression operand);

    CompoundExpression plus(Expression operand);

    CompoundExpression minus(Expression minuend);

    CompoundExpression divide(Expression divisor);


    /**
     * Bitwise AND
     *
     * @return {@link BigInteger} expression
     */
    CompoundExpression bitwiseAnd(Expression operand);

    /**
     * Bitwise OR
     *
     * @return {@link BigInteger} expression
     * @see #bitwiseAnd(Expression)
     * @see SQLs#bitwiseNot(Expression)
     */
    CompoundExpression bitwiseOr(Expression operand);

    /**
     * Bitwise XOR
     *
     * @return {@link BigInteger} expression
     */
    CompoundExpression bitwiseXor(Expression operand);


    /**
     * Shifts a  number to the right.
     *
     * @return {@link BigInteger} expression
     */
    CompoundExpression rightShift(Expression bitNumber);


    /**
     * Shifts a  number to the left.
     *
     * @return {@link BigInteger} expression
     */
    CompoundExpression leftShift(Expression bitNumber);

    ///
    ///  Access the slice of 1D array
    ///
    /// @param lower lower is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link SQLs#ABSENT} : placeholder,output nothing
    /// 3. literal
    /// @param colon see {@link SQLs#COLON}
    /// @param upper upper is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link SQLs#ABSENT} : placeholder,output nothing
    /// 3. literal
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-ACCESSING">ARRAYS-ACCESSING</a>
    @Support({H2, PostgreSQL})
    Expression slice(Object lower, SQLs.SymbolColon colon, Object upper);

    ///
    ///  Access the slice of 1D array
    ///
    /// @param func  a method reference of one of below:
    /// 1. {@link SQLs#param(TypeInfer, Object)} : parameter function
    /// 2. {@link SQLs#literal(TypeInfer, Object)}  : literal function
    /// 3. {@link SQLs#constant(TypeInfer, Object)}  : constant function
    /// 4. your custom function
    /// @param colon see {@link SQLs#COLON}
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-ACCESSING">ARRAYS-ACCESSING</a>
    @Support({H2, PostgreSQL})
    Expression slice(BiFunction<IntegerType, Integer, Expression> func, int lower, SQLs.SymbolColon colon, int upper);

    ///
    ///  Access the slice of 1D array without upper
    ///
    /// @param lower lower is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link SQLs#ABSENT} : placeholder,output nothing
    /// 3. literal
    /// @param colon see {@link SQLs#COLON}
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-ACCESSING">ARRAYS-ACCESSING</a>
    @Support({H2, PostgreSQL})
    Expression slice(Object lower, SQLs.SymbolColon colon);

    ///
    ///  Access the slice of 1D array without lower
    ///
    /// @param upper upper is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link SQLs#ABSENT} : placeholder,output nothing
    /// 3. literal
    /// @param colon see {@link SQLs#COLON}
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-ACCESSING">ARRAYS-ACCESSING</a>
    @Support({H2, PostgreSQL})
    Expression slice(SQLs.SymbolColon colon, Object upper);

    ///
    ///  Access the slice of 1D array without lower and upper
    ///
    /// @param colon see {@link SQLs#COLON}
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-ACCESSING">ARRAYS-ACCESSING</a>
    @Support({H2, PostgreSQL})
    Expression slice(SQLs.SymbolColon colon);

    ///
    ///  Access the slice of 2D array
    ///
    /// @param lower  lower is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link SQLs#ABSENT} : placeholder,output nothing
    /// 3. literal
    /// @param colon  see {@link SQLs#COLON}
    /// @param upper  upper is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link SQLs#ABSENT} : placeholder,output nothing
    /// 3. literal
    /// @param lower1 lower1 is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link SQLs#ABSENT} : placeholder,output nothing
    /// 3. literal
    /// @param colon1 see {@link SQLs#COLON}
    /// @param upper1 upper1 is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link SQLs#ABSENT} : placeholder,output nothing
    /// 3. literal
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-ACCESSING">ARRAYS-ACCESSING</a>
    @Support({H2, PostgreSQL})
    Expression slice(Object lower, SQLs.SymbolColon colon, Object upper, Object lower1, SQLs.SymbolColon colon1, Object upper1);

    ///
    ///  Access the slice of 2D array
    ///
    /// @param func   a method reference of one of below:
    /// 1. {@link SQLs#param(TypeInfer, Object)} : parameter function
    /// 2. {@link SQLs#literal(TypeInfer, Object)}  : literal function
    /// 3. {@link SQLs#constant(TypeInfer, Object)}  : constant function
    /// 4. your custom function
    /// @param colon  see {@link SQLs#COLON}
    /// @param colon1 see {@link SQLs#COLON}
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-ACCESSING">ARRAYS-ACCESSING</a>
    @Support({H2, PostgreSQL})
    Expression slice(BiFunction<IntegerType, Integer, Expression> func, int lower, SQLs.SymbolColon colon, int upper, int lower1, SQLs.SymbolColon colon1, int upper1);

    ///
    ///  Access the slice of 2D array without the lower of 1D
    ///
    /// @param colon  see {@link SQLs#COLON}
    /// @param upper  upper is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link SQLs#ABSENT} : placeholder,output nothing
    /// 3. literal
    /// @param lower1 lower1 is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link SQLs#ABSENT} : placeholder,output nothing
    /// 3. literal
    /// @param colon1 see {@link SQLs#COLON}
    /// @param upper1 upper1 is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link SQLs#ABSENT} : placeholder,output nothing
    /// 3. literal
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-ACCESSING">ARRAYS-ACCESSING</a>
    @Support({H2, PostgreSQL})
    Expression slice(SQLs.SymbolColon colon, Object upper, Object lower1, SQLs.SymbolColon colon1, Object upper1);

    ///
    ///  Access the slice of 2D array without the upper of 2D
    ///
    /// @param lower  lower is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link SQLs#ABSENT} : placeholder,output nothing
    /// 3. literal
    /// @param colon  see {@link SQLs#COLON}
    /// @param upper  upper is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link SQLs#ABSENT} : placeholder,output nothing
    /// 3. literal
    /// @param lower1 lower1 is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link SQLs#ABSENT} : placeholder,output nothing
    /// 3. literal
    /// @param colon1 see {@link SQLs#COLON}
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-ACCESSING">ARRAYS-ACCESSING</a>
    @Support({H2, PostgreSQL})
    Expression slice(Object lower, SQLs.SymbolColon colon, Object upper, Object lower1, SQLs.SymbolColon colon1);

    ///
    /// Access the slice of 2D array without all lower and upper
    ///
    /// @param colon  see {@link SQLs#COLON}
    /// @param colon1 see {@link SQLs#COLON}
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-ACCESSING">ARRAYS-ACCESSING</a>
    @Support({H2, PostgreSQL})
    Expression slice(SQLs.SymbolColon colon, SQLs.SymbolColon colon1);

    ///
    /// Access the slice of nD array
    ///
    /// @param indexList a List with an even size,the element is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link SQLs#ABSENT} : placeholder,output nothing
    /// 3. literal
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-ACCESSING">ARRAYS-ACCESSING</a>
    @Support({H2, PostgreSQL})
    Expression sliceAtSubs(List<?> indexList);

    ///
    /// dot operator.
    ///
    /// @param func      a method reference of one of below:
    /// 1. {@link SQLs#param(TypeInfer, Object)} : parameter function
    /// 2. {@link SQLs#literal(TypeInfer, Object)}  : literal function
    /// 3. {@link SQLs#constant(TypeInfer, Object)}  : constant function
    /// 4. your custom function
    /// @param indexList a List with an even size
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-ACCESSING">ARRAYS-ACCESSING</a>
    @Support({H2, PostgreSQL})
    Expression sliceAtSubs(BiFunction<MappingType, Integer, Expression> func, List<Integer> indexList);


    ///
    /// dot operator.
    ///
    /// @param key key is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link String} literal
    /// @see <a href="https://www.postgresql.org/docs/current/datatype-json.html#JSONB-SUBSCRIPTING">JSONB-SUBSCRIPTING</a>
    Expression dot(Object key);

    Expression dot(Object key, Object key1);

    Expression dot(Object key, Object key1, Object key2);

    Expression dot(Object key, Object key1, Object key2, Object key3);

    Expression dot(Object key, Object key1, Object key2, Object key3, Object key4);

    Expression dots(List<?> subscriptList);

    ///
    ///  bracket operator.
    ///
    /// @param key key is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link String} literal
    /// 3. {@link Integer} literal
    /// @see <a href="https://www.postgresql.org/docs/current/datatype-json.html#JSONB-SUBSCRIPTING">JSONB-SUBSCRIPTING</a>
    Expression bracket(Object key);

    Expression bracket(Object key, Object key1);

    Expression bracket(Object key, Object key1, Object key2);

    Expression bracket(Object key, Object key1, Object key2, Object key3);

    Expression bracket(Object key, Object key1, Object key2, Object key3, Object key4);

    ///
    ///  bracket operator.
    ///
    /// @param key key is one of below:
    /// 1. {@link Expression} : subscript expression
    /// 2. {@link String} literal
    /// 3. {@link Integer} literal
    /// @see <a href="https://www.postgresql.org/docs/current/datatype-json.html#JSONB-SUBSCRIPTING">JSONB-SUBSCRIPTING</a>
    Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key);

    Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1);

    Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1, Object key2);

    Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1, Object key2, Object key3);

    Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1, Object key2, Object key3, Object key4);

    Expression brackets(List<?> subscriptList);

    Expression brackets(BiFunction<MappingType, Object, Expression> func, List<?> subscriptList);

    /**
     * <p>Map the expression to a known type expression,but don't support codec.
     */
    TypedExpression mapTo(MappingType typeMeta);


    @Support({PostgreSQL})
    TypedExpression castTo(MappingType type);

    /**
     * @return always this
     */
    @Override
    SortItem asSortItem();

    SortItem asc();

    SortItem desc();

    SortItem ascSpace(@Nullable SQLs.NullsFirstLast firstLast);


    SortItem descSpace(@Nullable SQLs.NullsFirstLast firstLast);

    /*-------------------below dialect operator method -------------------*/

    /**
     * <p>
     * This method is designed for dialect key word syntax. For example : postgre using key word
     *
     * <p>
     * <strong>Note</strong>: The first argument of funcRef always is <strong>this</strong>.
     *
     * @param funcRef the reference of the method of dialect operator,<strong>NOTE</strong>: not lambda.
     *                The first argument of funcRef always is <strong>this</strong>.
     *                For example: {@code Postgres.using(Expression)}
     */
    <R extends UnaryResult> R space(Function<Expression, R> funcRef);


    /**
     * <p>This method is designed for dialect operator.
     *
     * <p><strong>Note</strong>: The first argument of funcRef always is <strong>this</strong>.
     *
     * @param funcRef the reference of the method of dialect operator,<strong>NOTE</strong>: not lambda.
     *                The first argument of funcRef always is <strong>this</strong>.
     *                For example: {@code Postgres.pound(Expression,Expression)}
     * @param right   the right operand of dialect operator.  It will be passed to funcRef as the second argument of funcRef
     */
    <T, R extends ResultExpression> R space(BiFunction<Expression, T, R> funcRef, T right);


    /**
     * <p>
     * This method is designed for dialect operator.
     *
     * <p>
     * <strong>Note</strong>: The first argument of funcRef always is <strong>this</strong>.
     *
     * @param funcRef the reference of the method of dialect operator,<strong>NOTE</strong>: not lambda.
     *                The first argument of funcRef always is <strong>this</strong>.
     *                For example: {@code Postgres.pound(Expression,Expression)}
     * @param right   the right operand of dialect operator.  It will be passed to funcRef as the second argument of funcRef
     */

    <M extends SQLToken, R extends ResultExpression> R space(OptionalClauseOperator<M, Expression, R> funcRef, Expression right, M modifier, Expression optionalExp);

    /**
     * <p>
     * This method is designed for dialect operator.
     *
     * <p>
     * <strong>Note</strong>: The first argument of funcRef always is <strong>this</strong>.
     *
     * @param funcRef the reference of the method of dialect operator,<strong>NOTE</strong>: not lambda.
     *                The first argument of funcRef always is <strong>this</strong>.
     *                For example: {@code Postgres.pound(Expression,Expression)}
     * @param right   the right operand of dialect operator.  It will be passed to funcRef as the second argument of funcRef
     */
    <M extends SQLToken, R extends ResultExpression> R space(OptionalClauseOperator<M, Expression, R> funcRef, Expression right, M modifier, char escapeChar);


    /**
     * <p>
     * This method is designed for dialect operator that produce boolean type expression.
     * This method name is 'whiteSpace' not 'space' ,because of {@link Statement._WhereAndClause#and(UnaryOperator, io.army.criteria.impl.SQLs.SymbolSpace, IPredicate)} type infer.
     *
     * <p>
     * <strong>Note</strong>: The first argument of funcRef always is <strong>this</strong>.
     *
     * <p>
     *
     * @param funcRef the reference of the method of dialect operator,<strong>NOTE</strong>: not lambda.
     *                The first argument of funcRef always is <strong>this</strong>.
     *                For example: {@code Postgres.pound(Expression,Expression)}
     * @param right   the right operand of dialect operator.  It will be passed to funcRef as the second argument of funcRef
     */
    <T> CompoundPredicate whiteSpace(BiFunction<Expression, T, CompoundPredicate> funcRef, T right);


    /**
     * <p>
     * This method is designed for dialect operator that produce boolean type expression.
     * This method name is 'whiteSpace' not 'space' ,because of {@link Statement._WhereAndClause#and(UnaryOperator, io.army.criteria.impl.SQLs.SymbolSpace, IPredicate)} type infer.
     *
     * <p>
     * <strong>Note</strong>: The first argument of funcRef always is <strong>this</strong>.
     *
     * <p>
     *
     * @param funcRef the reference of the method of dialect operator,<strong>NOTE</strong>: not lambda.
     *                The first argument of funcRef always is <strong>this</strong>.
     *                For example: {@code Postgres.pound(Expression,Expression)}
     * @param right   the right operand of dialect operator.  It will be passed to funcRef as the second argument of funcRef
     */
    <M extends SQLToken, T extends RightOperand> CompoundPredicate whiteSpace(TeFunction<Expression, M, T, CompoundPredicate> funcRef, final M modifier, T right);

    /**
     * <p>
     * This method is designed for dialect operator that produce boolean type expression.
     * This method name is 'whiteSpace' not 'space' ,because of {@link Statement._WhereAndClause#and(UnaryOperator, io.army.criteria.impl.SQLs.SymbolSpace, IPredicate)} type infer.
     *
     * <p>
     * <strong>Note</strong>: The first argument of funcRef always is <strong>this</strong>.
     *
     * <p>
     *
     * @param funcRef the reference of the method of dialect operator,<strong>NOTE</strong>: not lambda.
     *                The first argument of funcRef always is <strong>this</strong>.
     *                For example: {@code Postgres.pound(Expression,Expression)}
     * @param right   the right operand of dialect operator.  It will be passed to funcRef as the second argument of funcRef
     */

    <M extends SQLToken> CompoundPredicate whiteSpace(OptionalClauseOperator<M, Expression, CompoundPredicate> funcRef, Expression right, M modifier, Expression optionalExp);

    /**
     * <p>
     * This method is designed for dialect operator that produce boolean type expression.
     * This method name is 'whiteSpace' not 'space' ,because of {@link Statement._WhereAndClause#and(UnaryOperator, io.army.criteria.impl.SQLs.SymbolSpace, IPredicate)} type infer.
     *
     * <p>
     * <strong>Note</strong>: The first argument of funcRef always is <strong>this</strong>.
     *
     * <p>
     *
     * @param funcRef the reference of the method of dialect operator,<strong>NOTE</strong>: not lambda.
     *                The first argument of funcRef always is <strong>this</strong>.
     *                For example: {@code Postgres.pound(Expression,Expression)}
     * @param right   the right operand of dialect operator.  It will be passed to funcRef as the second argument of funcRef
     */

    <M extends SQLToken> CompoundPredicate whiteSpace(OptionalClauseOperator<M, Expression, CompoundPredicate> funcRef, Expression right, M modifier, char escapeChar);


}
