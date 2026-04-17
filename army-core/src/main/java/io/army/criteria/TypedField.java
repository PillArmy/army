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
import io.army.criteria.impl.TableField;
import io.army.function.TeNamedParamsFunc;

import java.util.function.BiFunction;


/// This interface is the base interface of below:
/// 1. {@link TableField} field of table
/// 2. {@link TypedDerivedField} derived field
///
public interface TypedField extends SqlField, TypedExpression, SimpleExpression {

    ///
    /// {@code =} operator for batch statement
    /// The implementation of this method is equivalent to following:
    ///
    /// ```java
    /// IPredicate spaceEqual(BiFunction<TypedField, String, Expression> namedOperator) {
    ///     return this.equal(namedOperator.apply(this, this.fieldName()))
    /// }
    /// ```
    ///
    /// @param namedOperator one of below:
    /// 1. {@link SQLs#namedParam(TypeInfer, String)} named parameter
    /// 2. {@link SQLs#namedLiteral(TypeInfer, String)} named literal
    /// 3. {@link SQLs#namedConst(TypeInfer, String)} named const
    /// 4. Your custom method
    IPredicate spaceEqual(BiFunction<TypedField, String, Expression> namedOperator);

    IPredicate spaceNotEqual(BiFunction<TypedField, String, Expression> namedOperator);

    IPredicate spaceNullSafeEqual(BiFunction<TypedField, String, Expression> namedOperator);

    ///
    /// {@code <} operator for batch statement
    /// The implementation of this method is equivalent to following:
    ///
    /// ```java
    /// IPredicate spaceLess(BiFunction<TypedField, String, Expression> namedOperator) {
    ///     return this.less(namedOperator.apply(this, this.fieldName()))
    /// }
    /// ```
    ///
    /// @param namedOperator one of below:
    /// 1. {@link SQLs#namedParam(TypeInfer, String)} named parameter
    /// 2. {@link SQLs#namedLiteral(TypeInfer, String)} named literal
    /// 3. {@link SQLs#namedConst(TypeInfer, String)} named const
    /// 4. Your custom method
    IPredicate spaceLess(BiFunction<TypedField, String, Expression> namedOperator);

    IPredicate spaceLessEqual(BiFunction<TypedField, String, Expression> namedOperator);

    IPredicate spaceGreater(BiFunction<TypedField, String, Expression> namedOperator);

    IPredicate spaceGreaterEqual(BiFunction<TypedField, String, Expression> namedOperator);


    IPredicate spaceLike(BiFunction<TypedField, String, Expression> namedOperator);

    ///
    /// LIKE operator for batch statement.
    ///
    /// The implementation of this method is equivalent to following:
    ///
    /// ```java
    /// IPredicate spaceLike(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar) {
    ///     final Expression operand; // the right operand of LIKE .
    ///     operand = namedOperator.apply(this, this.fieldName());
    ///     if (operand instanceof LiteralExpression && !(escapeChar instanceof Expression)) {
    ///             escapeChar = SQLs.constant(StringType.INSTANCE, escapeChar);
    ///     }
    ///     return this.like(operand,escape,escapeChar)
    /// }
    /// ```
    ///
    /// @param namedOperator one of below:
    /// 1. {@link SQLs#namedParam(TypeInfer, String)} named parameter
    /// 2. {@link SQLs#namedLiteral(TypeInfer, String)} named literal
    /// 3. {@link SQLs#namedConst(TypeInfer, String)} named const
    /// 4. Your custom method
    /// @param escape        {@link SQLs#ESCAPE}
    /// @param escapeChar    one of blow:
    /// 1. {@link Expression} expression
    /// 2. {@link String} literal
    /// 3. char literal
    IPredicate spaceLike(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar);

    IPredicate spaceNotLike(BiFunction<TypedField, String, Expression> namedOperator);

    ///
    /// NOT LIKE operator for batch statement.
    ///
    /// The implementation of this method is equivalent to following:
    ///
    /// ```java
    /// IPredicate spaceNotLike(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar) {
    ///     final Expression operand; // the right operand of LIKE .
    ///     operand = namedOperator.apply(this, this.fieldName());
    ///     if (operand instanceof LiteralExpression && !(escapeChar instanceof Expression)) {
    ///             escapeChar = SQLs.constant(StringType.INSTANCE, escapeChar);
    ///     }
    ///     return this.notLike(operand,escape,escapeChar)
    /// }
    /// ```
    ///
    /// @param namedOperator one of below:
    /// 1. {@link SQLs#namedParam(TypeInfer, String)} named parameter
    /// 2. {@link SQLs#namedLiteral(TypeInfer, String)} named literal
    /// 3. {@link SQLs#namedConst(TypeInfer, String)} named const
    /// @param escape        {@link SQLs#ESCAPE}
    /// @param escapeChar    one of blow:
    /// 1. {@link Expression} expression
    /// 2. {@link String} literal
    /// 3. char literal
    IPredicate spaceNotLike(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar);


    IPredicate spaceSimilarTo(BiFunction<TypedField, String, Expression> namedOperator);

    ///
    /// SIMILAR TO operator for batch statement.
    ///
    /// The implementation of this method is equivalent to following:
    ///
    /// ```java
    /// IPredicate spaceSimilarTo(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar) {
    ///     final Expression operand; // the right operand of SIMILAR TO .
    ///     operand = namedOperator.apply(this, this.fieldName());
    ///     if (operand instanceof LiteralExpression && !(escapeChar instanceof Expression)) {
    ///             escapeChar = SQLs.constant(StringType.INSTANCE, escapeChar);
    ///     }
    ///     return this.similarTo(operand,escape,escapeChar)
    /// }
    /// ```
    ///
    /// @param namedOperator one of below:
    /// 1. {@link SQLs#namedParam(TypeInfer, String)} named parameter
    /// 2. {@link SQLs#namedLiteral(TypeInfer, String)} named literal
    /// 3. {@link SQLs#namedConst(TypeInfer, String)} named const
    /// 4. Your custom method
    /// @param escape        {@link SQLs#ESCAPE}
    /// @param escapeChar    one of blow:
    /// 1. {@link Expression} expression
    /// 2. {@link String} literal
    /// 3. char literal
    IPredicate spaceSimilarTo(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar);

    IPredicate spaceNotSimilarTo(BiFunction<TypedField, String, Expression> namedOperator);

    ///
    /// NOT SIMILAR TO operator for batch statement.
    ///
    /// The implementation of this method is equivalent to following:
    ///
    /// ```java
    /// IPredicate spaceNotSimilarTo(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar) {
    ///     final Expression operand; // the right operand of NOT SIMILAR TO .
    ///     operand = namedOperator.apply(this, this.fieldName());
    ///     if (operand instanceof LiteralExpression && !(escapeChar instanceof Expression)) {
    ///             escapeChar = SQLs.constant(StringType.INSTANCE, escapeChar);
    ///     }
    ///     return this.notSimilarTo(operand,escape,escapeChar)
    /// }
    /// ```
    ///
    /// @param namedOperator one of below:
    /// 1. {@link SQLs#namedParam(TypeInfer, String)} named parameter
    /// 2. {@link SQLs#namedLiteral(TypeInfer, String)} named literal
    /// 3. {@link SQLs#namedConst(TypeInfer, String)} named const
    /// @param escape        {@link SQLs#ESCAPE}
    /// @param escapeChar    one of blow:
    /// 1. {@link Expression} expression
    /// 2. {@link String} literal
    /// 3. char literal
    IPredicate spaceNotSimilarTo(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar);



    /// IN operator  for batch statement
    ///
    /// @param namedOperator one of below:
    /// 1. {@link SQLs#namedRowLiteral(TypeInfer, String)} : row literal function
    /// 2. {@link SQLs#namedRowConst(TypeInfer, String)} : row const function
    /// 3. Your custom method
    IPredicate spaceIn(BiFunction<TypedField, String, RowExpression> namedOperator);

    /// IN operator  for batch statement
    ///
    /// @param namedOperator one of below:
    /// 1. {@link SQLs#namedRowLiteral(TypeInfer, String)} : row literal function
    /// 2. {@link SQLs#namedRowConst(TypeInfer, String)} : row const function
    /// 3. Your custom method
    IPredicate spaceNotIn(BiFunction<TypedField, String, RowExpression> namedOperator);

    /// IN operator  for batch statement
    ///
    /// @param namedOperator one of below:
    /// 1. {@link SQLs#namedRowParam(TypeInfer, String, int)} : row param function
    /// 2. Your custom method
    IPredicate spaceIn(TeNamedParamsFunc<TypedField> namedOperator, int size);

    IPredicate spaceNotIn(TeNamedParamsFunc<TypedField> namedOperator, int size);

    Expression spaceMod(BiFunction<TypedField, String, Expression> namedOperator);

    Expression spacePlus(BiFunction<TypedField, String, Expression> namedOperator);

    Expression spaceMinus(BiFunction<TypedField, String, Expression> namedOperator);

    Expression spaceTimes(BiFunction<TypedField, String, Expression> namedOperator);

    Expression spaceDivide(BiFunction<TypedField, String, Expression> namedOperator);

    Expression spaceBitwiseAnd(BiFunction<TypedField, String, Expression> namedOperator);

    Expression spaceBitwiseOr(BiFunction<TypedField, String, Expression> namedOperator);

    Expression spaceXor(BiFunction<TypedField, String, Expression> namedOperator);

    Expression spaceRightShift(BiFunction<TypedField, String, Expression> namedOperator);

    Expression spaceLeftShift(BiFunction<TypedField, String, Expression> namedOperator);

    ///
    /// @param operator dialect operator
    Expression spaceSpace(SQLs.DualOperator operator, BiFunction<TypedField, String, Expression> namedOperator);


    IPredicate spaceSpace(SQLs.BiOperator operator, BiFunction<TypedField, String, Expression> namedOperator);

    ///
    ///
    /// Dialect operator for batch statement.
    ///
    /// The implementation of this method is equivalent to following:
    ///
    /// ```java
    /// IPredicate spaceSpace(SQLs.BiOperator operator,BiFunction<TypedField, String, Expression> namedOperator, SQLToken modifier, Object optionalExp) {
    ///     final Expression operand; // the right operand of operator .
    ///     operand = namedOperator.apply(this, this.fieldName());
    ///     if (operand instanceof LiteralExpression && !(optionalExp instanceof Expression)) {
    ///             optionalExp = SQLs.constant(StringType.INSTANCE, optionalExp);
    ///     }
    ///     return this.space(operator,operand,modifier,optionalExp)
    /// }
    /// ```
    ///
    /// @param operator      one of below:
    /// 1. {@code  Postgres#SIMILAR_TO}  key word
    /// 2. {@code  Postgres#NOT_SIMILAR_TO} key word
    /// @param namedOperator one of below:
    /// 1. {@link SQLs#namedParam(TypeInfer, String)} named parameter
    /// 2. {@link SQLs#namedLiteral(TypeInfer, String)} named literal
    /// 3. {@link SQLs#namedConst(TypeInfer, String)} named const
    /// @param modifier      {@link SQLs#ESCAPE} key word
    /// @param optionalExp   one of blow:
    /// 1. {@link Expression} expression
    /// 2. {@link String} literal
    /// 3. char literal
    /// @see <a href="https://www.postgresql.org/docs/current/functions-matching.html#FUNCTIONS-SIMILARTO-REGEXP">SIMILAR TO Regular Expressions</a>
    IPredicate spaceSpace(SQLs.BiOperator operator, BiFunction<TypedField, String, Expression> namedOperator, SQLToken modifier, Object optionalExp);


}
