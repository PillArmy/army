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
import io.army.criteria.impl.inner._Selection;
import io.army.function.TeNamedParamsFunc;
import io.army.mapping.StringType;

import java.util.function.BiFunction;

/// 
/// This class is a implementation of {@link SqlField},and This class is base class of below:
/// 
/// - {@link TableFieldMeta}
/// - {@link QualifiedFieldImpl}
/// - {@link  CriteriaContexts.ImmutableDerivedField}
/// - {@code   CriteriaContexts.MutableDerivedField}
/// 
abstract class OperationTypedField extends OperationExpression.OperationTypedExpression implements TypedField,
        _Selection, ArmySimpleExpression {

    @Override
    public final IPredicate spaceEqual(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.biPredicate(this, DualBooleanOperator.EQUAL, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final IPredicate spaceNotEqual(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.biPredicate(this, DualBooleanOperator.NOT_EQUAL, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final IPredicate spaceNullSafeEqual(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.biPredicate(this, DualBooleanOperator.NULL_SAFE_EQUAL, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final IPredicate spaceLess(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.biPredicate(this, DualBooleanOperator.LESS, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final IPredicate spaceLessEqual(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.biPredicate(this, DualBooleanOperator.LESS_EQUAL, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final IPredicate spaceGreater(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.biPredicate(this, DualBooleanOperator.GREATER, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final IPredicate spaceGreaterEqual(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.biPredicate(this, DualBooleanOperator.GREATER_EQUAL, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final IPredicate spaceLike(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.likePredicate(this, DualBooleanOperator.LIKE, namedOperator.apply(this, this.fieldName()), SQLs.ESCAPE, null);
    }

    @Override
    public final IPredicate spaceLike(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar) {
        final Expression operand;
        operand = namedOperator.apply(this, this.fieldName());
        return Expressions.likePredicate(this, DualBooleanOperator.LIKE, operand, escape, wrapEscapeIfNeed(operand, escapeChar));
    }


    @Override
    public final IPredicate spaceNotLike(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.likePredicate(this, DualBooleanOperator.NOT_LIKE, namedOperator.apply(this, this.fieldName()), SQLs.ESCAPE, null);
    }

    @Override
    public final IPredicate spaceNotLike(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar) {
        final Expression operand;
        operand = namedOperator.apply(this, this.fieldName());
        return Expressions.likePredicate(this, DualBooleanOperator.NOT_LIKE, operand, escape, wrapEscapeIfNeed(operand, escapeChar));
    }

    @Override
    public final  IPredicate spaceSimilarTo(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.likePredicate(this, DualBooleanOperator.SIMILAR_TO, namedOperator.apply(this, this.fieldName()), SQLs.ESCAPE, null);
    }

    @Override
    public final  IPredicate spaceSimilarTo(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar) {
        final Expression operand;
        operand = namedOperator.apply(this, this.fieldName());
        return Expressions.likePredicate(this, DualBooleanOperator.SIMILAR_TO, operand, escape, wrapEscapeIfNeed(operand, escapeChar));
    }

    @Override
    public final  IPredicate spaceNotSimilarTo(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.likePredicate(this, DualBooleanOperator.NOT_SIMILAR_TO, namedOperator.apply(this, this.fieldName()), SQLs.ESCAPE, null);
    }

    @Override
    public final  IPredicate spaceNotSimilarTo(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar) {
        final Expression operand;
        operand = namedOperator.apply(this, this.fieldName());
        return Expressions.likePredicate(this, DualBooleanOperator.NOT_SIMILAR_TO, operand, escape, wrapEscapeIfNeed(operand, escapeChar));
    }

    @Override
    public final IPredicate spaceIn(BiFunction<TypedField, String, RowExpression> namedOperator) {
        return Expressions.inPredicate(this, false, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final IPredicate spaceNotIn(BiFunction<TypedField, String, RowExpression> namedOperator) {
        return Expressions.inPredicate(this, true, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final IPredicate spaceIn(TeNamedParamsFunc<TypedField> namedOperator, int size) {
        return Expressions.inPredicate(this, false, namedOperator.apply(this, this.fieldName(), size));
    }

    @Override
    public final IPredicate spaceNotIn(TeNamedParamsFunc<TypedField> namedOperator, int size) {
        return Expressions.inPredicate(this, true, namedOperator.apply(this, this.fieldName(), size));
    }

    @Override
    public final Expression spaceMod(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.dualExp(this, DualExpOperator.MOD, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final Expression spacePlus(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.dualExp(this, DualExpOperator.PLUS, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final Expression spaceMinus(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.dualExp(this, DualExpOperator.MINUS, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final Expression spaceTimes(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.dualExp(this, DualExpOperator.TIMES, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final Expression spaceDivide(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.dualExp(this, DualExpOperator.DIVIDE, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final Expression spaceBitwiseAnd(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.dualExp(this, DualExpOperator.BITWISE_AND, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final Expression spaceBitwiseOr(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.dualExp(this, DualExpOperator.BITWISE_OR, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final Expression spaceXor(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.dualExp(this, DualExpOperator.BITWISE_XOR, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final Expression spaceRightShift(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.dualExp(this, DualExpOperator.RIGHT_SHIFT, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final Expression spaceLeftShift(BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.dualExp(this, DualExpOperator.LEFT_SHIFT, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final Expression spaceSpace(SQLs.DualOperator operator, BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.dualExp(this, operator, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final IPredicate spaceSpace(SQLs.BiOperator operator, BiFunction<TypedField, String, Expression> namedOperator) {
        return Expressions.biPredicate(this, operator, namedOperator.apply(this, this.fieldName()));
    }

    @Override
    public final IPredicate spaceSpace(SQLs.BiOperator operator, BiFunction<TypedField, String, Expression> namedOperator, SQLToken modifier, Object optionalExp) {
        final Expression operand;
        operand = namedOperator.apply(this, this.fieldName());
        return similarToPredicate(operator, operand, modifier, wrapEscapeIfNeed(operand, optionalExp));
    }


}
