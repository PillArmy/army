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

import io.army.function.TeNamedParamsFunc;

import java.util.function.BiFunction;


/**
 * <p>
 * This interface is base interface of below:
 * <ul>
 *     <li>{@link TypedDerivedField}</li>
 * </ul>
 *
 * @since 0.6.0
 */
public interface TypedField extends SqlField, TypedExpression, SimpleExpression {


    CompoundPredicate spaceEqual(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundPredicate spaceLess(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundPredicate spaceLessEqual(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundPredicate spaceGreater(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundPredicate spaceGreaterEqual(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundPredicate spaceNotEqual(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundPredicate spaceLike(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundPredicate spaceNotLike(BiFunction<TypedField, String, Expression> namedOperator);


    CompoundPredicate spaceIn(TeNamedParamsFunc<TypedField> namedOperator, int size);

    CompoundPredicate spaceNotIn(TeNamedParamsFunc<TypedField> namedOperator, int size);

    CompoundExpression spaceMod(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundExpression spacePlus(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundExpression spaceMinus(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundExpression spaceTimes(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundExpression spaceDivide(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundExpression spaceBitwiseAnd(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundExpression spaceBitwiseOr(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundExpression spaceXor(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundExpression spaceRightShift(BiFunction<TypedField, String, Expression> namedOperator);

    CompoundExpression spaceLeftShift(BiFunction<TypedField, String, Expression> namedOperator);
}
