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
import io.army.lang.Nullable;
import io.army.mapping.IntegerType;
import io.army.mapping.MappingType;
import io.army.meta.FieldMeta;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import static io.army.dialect.Database.*;

/// Interface representing the sql expression, eg: column,function.
/// This interface is the base interface of below"
/// 
/// - {@link SimpleExpression}
/// - {@link CompoundExpression}
/// - {@link IPredicate}
/// 
/// @see FieldMeta
/// @since 0.6.0
@SuppressWarnings("unused")
public interface Expression extends SortItem, RowElement, RightOperand,
        GroupByItem.ExpressionItem, AssignmentItem, SQLElement, SelectionSpec {


    /// 
    /// {@code =} operator
    /// 
    /// @param operand right operand , one of below :
    /// 1. {@link Expression}
    /// 2. java literal,if this is {@link TypedExpression} ,then invoke {@link SQLs#param(TypeInfer, Object)} else invoke {@link SQLs#parameter(Object)}
    /// @throws CriteriaException throw when Operand isn't operable {@link Expression},for example {@link SQLs#DEFAULT},
    /// {@link SQLs#rowParam(TypeInfer, Collection)}
    /// @see TypedExpression#equal(BiFunction, Object)
    /// @see TypedField#spaceEqual(BiFunction)
    /// 
    IPredicate equal(Object operand);

    IPredicate notEqual(Object operand);

    @Support({MySQL, PostgreSQL, H2})
    IPredicate nullSafeEqual(Object operand);

    /// 
    /// **<** operator
    /// @param operand non-null
    /// @throws CriteriaException throw when Operand isn't operable {@link Expression},for example {@link SQLs#DEFAULT},
    /// {@link SQLs#rowParam(TypeInfer, Collection)}
    IPredicate less(Object operand);


    IPredicate lessEqual(Object operand);


    IPredicate greater(Object operand);

    IPredicate greaterEqual(Object operand);


    /// @param and see {@link SQLs#AND}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-comparison.html#FUNCTIONS-COMPARISON-PRED-TABLE">Postgres BETWEEN</a>
    IPredicate between(Object first, SQLs.WordAnd and, Object second);


    /// @param and      see {@link SQLs#AND}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-comparison.html#FUNCTIONS-COMPARISON-PRED-TABLE">Postgres BETWEEN</a>
    IPredicate notBetween(Object first, SQLs.WordAnd and, Object second);

    /// @param modifier see
    /// 1. {@link SQLs#SYMMETRIC} modifier
    /// 1. {@link SQLs#ASYMMETRIC} modifier
    /// @param and      see {@link SQLs#AND}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-comparison.html#FUNCTIONS-COMPARISON-PRED-TABLE">Postgres BETWEEN</a>
    @Support({PostgreSQL, H2})
    IPredicate between(@Nullable SQLs.BetweenModifier modifier, Object first, SQLs.WordAnd and, Object second);

    /// @param modifier see
    /// 1. {@link SQLs#SYMMETRIC} modifier
    /// 1. {@link SQLs#ASYMMETRIC} modifier
    /// @param and      see {@link SQLs#AND}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-comparison.html#FUNCTIONS-COMPARISON-PRED-TABLE">Postgres BETWEEN</a>
    @Support({PostgreSQL, H2})
    IPredicate notBetween(@Nullable SQLs.BetweenModifier modifier, Object first, SQLs.WordAnd and, Object second);

    /// @param operand
    /// - {@link SQLs#TRUE}
    /// - {@link SQLs#FALSE}
    /// - {@link SQLs#UNKNOWN}
    /// - {@link SQLs#NULL}
    /// - other
    /// 
    IPredicate is(SQLs.BoolTestWord operand);

    /// @param operand
    /// - {@link SQLs#TRUE}
    /// - {@link SQLs#FALSE}
    /// - {@link SQLs#UNKNOWN}
    /// - {@link SQLs#NULL}
    /// - other
    /// 
    IPredicate isNot(SQLs.BoolTestWord operand);

    IPredicate isNull();

    IPredicate isNotNull();

    /// @param operator see
    /// - {@link SQLs#DISTINCT_FROM}
    /// 
    @Support({PostgreSQL, H2})
    IPredicate is(SQLs.IsComparisonWord operator, Object operand);

    /// @param operator see
    /// - {@link SQLs#DISTINCT_FROM}
    /// 
    @Support({PostgreSQL, H2})
    IPredicate isNot(SQLs.IsComparisonWord operator, Object operand);

    IPredicate like(Object pattern);

    IPredicate like(Object pattern, SQLs.WordEscape escape, Object escapeChar);

    IPredicate notLike(Object pattern);

    IPredicate notLike(Object pattern, SQLs.WordEscape escape, Object escapeChar);

    @Support({PostgreSQL})
    IPredicate similarTo(Object pattern);

    @Support({PostgreSQL})
    IPredicate similarTo(Object pattern, SQLs.WordEscape escape, Object escapeChar);

    @Support({PostgreSQL})
    IPredicate notSimilarTo(Object pattern);

    @Support({PostgreSQL})
    IPredicate notSimilarTo(Object pattern, SQLs.WordEscape escape, Object escapeChar);


    /// 
    /// **= ANY** operator
    IPredicate equalAny(SubQuery subQuery);

    /// Operator {@code = SOME}
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/9.6/en/any-in-some-subqueries.html">Subqueries with ANY, IN, or SOME</a>
    IPredicate equalSome(SubQuery subQuery);

    IPredicate equalAll(SubQuery subQuery);

    IPredicate notEqualAny(SubQuery subQuery);

    IPredicate notEqualSome(SubQuery subQuery);

    IPredicate notEqualAll(SubQuery subQuery);

    IPredicate lessAny(SubQuery subQuery);

    IPredicate lessSome(SubQuery subQuery);

    IPredicate lessAll(SubQuery subQuery);

    IPredicate lessEqualAny(SubQuery subQuery);

    IPredicate lessEqualSome(SubQuery subQuery);

    IPredicate lessEqualAll(SubQuery subQuery);

    IPredicate greaterAny(SubQuery subQuery);

    IPredicate greaterSome(SubQuery subQuery);

    IPredicate greaterAll(SubQuery subQuery);

    IPredicate greaterEqualAny(SubQuery subQuery);

    IPredicate greaterEqualSome(SubQuery subQuery);

    IPredicate greaterEqualAll(SubQuery subQuery);


    IPredicate in(SQLColumnList row);

    IPredicate notIn(SQLColumnList row);

    Expression mod(Object operand);

    Expression times(Object operand);

    Expression plus(Object operand);

    Expression minus(Object minuend);

    Expression divide(Object divisor);


    /// Bitwise AND
    /// @return {@link BigInteger} expression
    Expression bitwiseAnd(Object operand);

    /// Bitwise OR
    /// @return {@link BigInteger} expression
    /// @see #bitwiseAnd(Object)
    /// @see SQLs#bitwiseNot(Expression)
    Expression bitwiseOr(Object operand);

    /// Bitwise XOR
    /// @return {@link BigInteger} expression
    Expression bitwiseXor(Object operand);


    /// Shifts a  number to the right.
    /// @return {@link BigInteger} expression
    Expression rightShift(Object bitNumber);


    /// Shifts a  number to the left.
    /// @return {@link BigInteger} expression
    Expression leftShift(Object bitNumber);

    /// 
    /// Access the slice of 1D array
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
    /// Access the slice of 1D array
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
    /// Access the slice of 1D array without upper
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
    /// Access the slice of 1D array without lower
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
    /// Access the slice of 1D array without lower and upper
    /// 
    /// @param colon see {@link SQLs#COLON}
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-ACCESSING">ARRAYS-ACCESSING</a>
    @Support({H2, PostgreSQL})
    Expression slice(SQLs.SymbolColon colon);

    /// 
    /// Access the slice of 2D array
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
    /// Access the slice of 2D array
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
    /// Access the slice of 2D array without the lower of 1D
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
    /// Access the slice of 2D array without the upper of 2D
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
    /// @see <a href="https://www.postgresql.org/docs/current/rowtypes.html#ROWTYPES-ACCESSING">Accessing Composite Types</a>
    Expression dot(Object key);

    Expression dot(Object key, Object key1);

    Expression dot(Object key, Object key1, Object key2);

    Expression dot(Object key, Object key1, Object key2, Object key3);

    Expression dot(Object key, Object key1, Object key2, Object key3, Object key4);

    Expression dots(List<?> subscriptList);

    /// 
    /// bracket operator.
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
    /// bracket operator.
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

    /// Map the expression to a known type expression,but don't support codec.
    TypedExpression mapTo(MappingType typeMeta);


    @Support({PostgreSQL})
    TypedExpression castTo(MappingType type);

    /// @return always this
    @Override
    SortItem asSortItem();

    SortItem asc();

    SortItem desc();

    SortItem ascSpace(@Nullable SQLs.NullsFirstLast firstLast);


    SortItem descSpace(@Nullable SQLs.NullsFirstLast firstLast);

    /*-------------------below dialect operator method -------------------*/

    /// 
    /// @param operator dialect operator
    /// @param right    {@link Expression} or java literal
    Expression space(SQLs.DualOperator operator, Object right);


    IPredicate space(SQLs.BiOperator operator, Object right);

    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-matching.html#FUNCTIONS-SIMILARTO-REGEXP">SIMILAR TO Regular Expressions</a>
    IPredicate space(SQLs.BiOperator operator, Object right, SQLToken modifier, Object optionalExp);


}
