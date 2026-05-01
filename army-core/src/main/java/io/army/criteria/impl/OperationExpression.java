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


import io.army.annotation.SortOrder;
import io.army.criteria.*;
import io.army.criteria.impl.inner._Predicate;
import io.army.dialect._Constant;
import io.army.dialect._SqlContext;
import io.army.function.TeNamedParamsFunc;
import io.army.lang.Nullable;
import io.army.mapping.BooleanType;
import io.army.mapping.IntegerType;
import io.army.mapping.MappingType;
import io.army.mapping.StringType;
import io.army.util._StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

/// this class is base class of most implementation of {@link Expression}
/// @since 0.6.0
abstract class OperationExpression implements ArmyExpression {


    /// 
    /// Private constructor
    /// @see OperationSimpleExpression#OperationSimpleExpression()
    /// @see OperationCompoundExpression#OperationCompoundExpression()
    /// @see PredicateExpression#PredicateExpression()
    OperationExpression() {
    }


    @Override
    public final boolean isNullValue() {
        return this instanceof SqlValueParam.SingleAnonymousValue
                && ((SqlValueParam.SingleAnonymousValue) this).value() == null;
    }

    @Override
    public final IPredicate equal(Object operand) {
        return Expressions.biPredicate(this, DualBooleanOperator.EQUAL, operand);
    }

    @Override
    public final IPredicate notEqual(Object operand) {
        return Expressions.biPredicate(this, DualBooleanOperator.NOT_EQUAL, operand);
    }

    @Override
    public final IPredicate nullSafeEqual(Object operand) {
        return Expressions.biPredicate(this, DualBooleanOperator.NULL_SAFE_EQUAL, operand);
    }

    @Override
    public final IPredicate less(Object operand) {
        return Expressions.biPredicate(this, DualBooleanOperator.LESS, operand);
    }


    @Override
    public final IPredicate lessEqual(Object operand) {
        return Expressions.biPredicate(this, DualBooleanOperator.LESS_EQUAL, operand);
    }

    @Override
    public final IPredicate greater(Object operand) {
        return Expressions.biPredicate(this, DualBooleanOperator.GREATER, operand);
    }


    @Override
    public final IPredicate greaterEqual(Object operand) {
        return Expressions.biPredicate(this, DualBooleanOperator.GREATER_EQUAL, operand);
    }

    @Override
    public final IPredicate between(Object first, SQLs.WordAnd and, Object second) {
        return Expressions.betweenPredicate(this, false, null, first, second);
    }


    @Override
    public final IPredicate notBetween(Object first, SQLs.WordAnd and, Object second) {
        return Expressions.betweenPredicate(this, true, null, first, second);
    }

    @Override
    public final IPredicate between(@Nullable SQLs.BetweenModifier modifier, Object first, SQLs.WordAnd and, Object second) {
        return Expressions.betweenPredicate(this, false, modifier, first, second);
    }

    @Override
    public final IPredicate notBetween(@Nullable SQLs.BetweenModifier modifier, Object first, SQLs.WordAnd and, Object second) {
        return Expressions.betweenPredicate(this, true, modifier, first, second);
    }

    @Override
    public final IPredicate is(SQLs.BoolTestWord operand) {
        return Expressions.booleanTestPredicate(this, false, operand);
    }

    @Override
    public final IPredicate isNot(SQLs.BoolTestWord operand) {
        return Expressions.booleanTestPredicate(this, true, operand);
    }

    @Override
    public final IPredicate isNull() {
        return Expressions.booleanTestPredicate(this, false, SQLs.NULL);
    }

    @Override
    public final IPredicate isNotNull() {
        return Expressions.booleanTestPredicate(this, true, SQLs.NULL);
    }

    @Override
    public final IPredicate is(SQLs.IsComparisonWord operator, Object operand) {
        return Expressions.isComparisonPredicate(this, false, operator, operand);
    }

    @Override
    public final IPredicate isNot(SQLs.IsComparisonWord operator, Object operand) {
        return Expressions.isComparisonPredicate(this, true, operator, operand);
    }


    @Override
    public final IPredicate like(Object pattern) {
        return Expressions.likePredicate(this, DualBooleanOperator.LIKE, pattern, SQLs.ESCAPE, null);
    }


    @Override
    public final IPredicate like(Object pattern, SQLs.WordEscape escape, Object escapeChar) {
        return Expressions.likePredicate(this, DualBooleanOperator.LIKE, pattern, escape, escapeChar);
    }

    @Override
    public final IPredicate notLike(Object pattern) {
        return Expressions.likePredicate(this, DualBooleanOperator.NOT_LIKE, pattern, SQLs.ESCAPE, null);
    }

    @Override
    public final IPredicate notLike(Object pattern, SQLs.WordEscape escape, Object escapeChar) {
        return Expressions.likePredicate(this, DualBooleanOperator.NOT_LIKE, pattern, escape, escapeChar);
    }

    @Override
    public final IPredicate similarTo(Object pattern) {
        return Expressions.likePredicate(this, DualBooleanOperator.SIMILAR_TO, pattern,
                SQLs.ESCAPE, null);
    }

    @Override
    public final IPredicate similarTo(Object pattern, SQLs.WordEscape escape, Object escapeChar) {
        return Expressions.likePredicate(this, DualBooleanOperator.SIMILAR_TO, pattern,
                escape, escapeChar);
    }

    @Override
    public final IPredicate notSimilarTo(Object pattern) {
        return Expressions.likePredicate(this, DualBooleanOperator.NOT_SIMILAR_TO, pattern,
                SQLs.ESCAPE, null);
    }

    @Override
    public final IPredicate notSimilarTo(Object pattern, SQLs.WordEscape escape, Object escapeChar) {
        return Expressions.likePredicate(this, DualBooleanOperator.NOT_SIMILAR_TO, pattern,
                escape, escapeChar);
    }


    @Override
    public final IPredicate equalAny(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.EQUAL, SQLs.ANY, subQuery);
    }

    @Override
    public final IPredicate equalSome(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.EQUAL, SQLs.SOME, subQuery);
    }

    @Override
    public final IPredicate equalAll(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.EQUAL, SQLs.ALL, subQuery);
    }

    @Override
    public final IPredicate notEqualAny(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.NOT_EQUAL, SQLs.ANY, subQuery);
    }


    @Override
    public final IPredicate notEqualSome(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.NOT_EQUAL, SQLs.SOME, subQuery);
    }

    @Override
    public final IPredicate notEqualAll(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.NOT_EQUAL, SQLs.ALL, subQuery);
    }


    @Override
    public final IPredicate lessAny(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.LESS, SQLs.ANY, subQuery);
    }


    @Override
    public final IPredicate lessSome(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.LESS, SQLs.SOME, subQuery);
    }

    @Override
    public final IPredicate lessAll(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.LESS, SQLs.ALL, subQuery);
    }


    @Override
    public final IPredicate lessEqualAny(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.LESS_EQUAL, SQLs.ANY, subQuery);
    }


    @Override
    public final IPredicate lessEqualSome(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.LESS_EQUAL, SQLs.SOME, subQuery);
    }


    @Override
    public final IPredicate lessEqualAll(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.LESS_EQUAL, SQLs.ALL, subQuery);
    }

    @Override
    public final IPredicate greaterAny(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.GREATER, SQLs.ANY, subQuery);
    }


    @Override
    public final IPredicate greaterSome(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.GREATER, SQLs.SOME, subQuery);
    }


    @Override
    public final IPredicate greaterAll(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.GREATER, SQLs.ALL, subQuery);
    }

    @Override
    public final IPredicate greaterEqualAny(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.GREATER_EQUAL, SQLs.ANY, subQuery);
    }

    @Override
    public final IPredicate greaterEqualSome(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.GREATER_EQUAL, SQLs.SOME, subQuery);
    }


    @Override
    public final IPredicate greaterEqualAll(SubQuery subQuery) {
        return Expressions.compareQueryPredicate(this, DualBooleanOperator.GREATER_EQUAL, SQLs.ALL, subQuery);
    }


    @Override
    public final IPredicate in(SQLColumnList row) {
        return Expressions.inPredicate(this, false, row);
    }

    @Override
    public final IPredicate notIn(SQLColumnList row) {
        return Expressions.inPredicate(this, true, row);
    }


    @Override
    public final Expression mod(Object operand) {
        return Expressions.dualExp(this, DualExpOperator.MOD, operand);
    }


    @Override
    public final Expression times(Object operand) {
        return Expressions.dualExp(this, DualExpOperator.TIMES, operand);
    }


    @Override
    public final Expression plus(Object operand) {
        return Expressions.dualExp(this, DualExpOperator.PLUS, operand);
    }


    @Override
    public final Expression minus(Object operand) {
        return Expressions.dualExp(this, DualExpOperator.MINUS, operand);
    }


    @Override
    public final Expression divide(Object operand) {
        return Expressions.dualExp(this, DualExpOperator.DIVIDE, operand);
    }

    @Override
    public final Expression bitwiseAnd(Object operand) {
        return Expressions.dualExp(this, DualExpOperator.BITWISE_AND, operand);
    }


    @Override
    public final Expression bitwiseOr(Object operand) {
        return Expressions.dualExp(this, DualExpOperator.BITWISE_OR, operand);
    }

    @Override
    public final Expression bitwiseXor(Object operand) {
        return Expressions.dualExp(this, DualExpOperator.BITWISE_XOR, operand);
    }


    @Override
    public final Expression rightShift(Object operand) {
        return Expressions.dualExp(this, DualExpOperator.RIGHT_SHIFT, operand);
    }

    @Override
    public final Expression leftShift(Object operand) {
        return Expressions.dualExp(this, DualExpOperator.LEFT_SHIFT, operand);
    }


    @Override
    public final Expression slice(Object lower, SQLs.SymbolColon colon, Object upper) {
        return Expressions.arraySliceExp(this, List.of(lower, upper));
    }

    @Override
    public final Expression slice(BiFunction<IntegerType, Integer, Expression> func, int lower, SQLs.SymbolColon colon, int upper) {
        final List<?> list = List.of(func.apply(IntegerType.INSTANCE, lower),
                func.apply(IntegerType.INSTANCE, upper)
        );
        return Expressions.arraySliceExp(this, list);
    }

    @Override
    public final Expression slice(Object lower, SQLs.SymbolColon colon) {
        return Expressions.arraySliceExp(this, List.of(lower, SQLs.ABSENT));
    }

    @Override
    public final Expression slice(SQLs.SymbolColon colon, Object upper) {
        return Expressions.arraySliceExp(this, List.of(SQLs.ABSENT, upper));
    }

    @Override
    public final Expression slice(SQLs.SymbolColon colon) {
        return Expressions.arraySliceExp(this, List.of(SQLs.ABSENT, SQLs.ABSENT));
    }

    @Override
    public final Expression slice(Object lower, SQLs.SymbolColon colon, Object upper, Object lower1, SQLs.SymbolColon colon1, Object upper1) {
        return Expressions.arraySliceExp(this, List.of(lower, upper, lower1, upper1));
    }

    @Override
    public final Expression slice(BiFunction<IntegerType, Integer, Expression> func, int lower, SQLs.SymbolColon colon, int upper, int lower1, SQLs.SymbolColon colon1, int upper1) {
        final List<?> list = List.of(func.apply(IntegerType.INSTANCE, lower),
                func.apply(IntegerType.INSTANCE, upper),
                func.apply(IntegerType.INSTANCE, lower1),
                func.apply(IntegerType.INSTANCE, upper1)
        );
        return Expressions.arraySliceExp(this, list);
    }

    @Override
    public final Expression slice(SQLs.SymbolColon colon, Object upper, Object lower1, SQLs.SymbolColon colon1, Object upper1) {
        return Expressions.arraySliceExp(this, List.of(SQLs.ABSENT, upper, lower1, upper1));
    }

    @Override
    public final Expression slice(Object lower, SQLs.SymbolColon colon, Object upper, Object lower1, SQLs.SymbolColon colon1) {
        return Expressions.arraySliceExp(this, List.of(lower, upper, lower1, SQLs.ABSENT));
    }

    @Override
    public final Expression slice(SQLs.SymbolColon colon, SQLs.SymbolColon colon1) {
        return Expressions.arraySliceExp(this, List.of(SQLs.ABSENT, SQLs.ABSENT, SQLs.ABSENT, SQLs.ABSENT));
    }

    @Override
    public final Expression sliceAtSubs(List<?> indexList) {
        Expressions.assertSliceSubscriptListSize(indexList);
        return Expressions.arraySliceExp(this, List.copyOf(indexList));
    }

    @Override
    public final Expression sliceAtSubs(BiFunction<MappingType, Integer, Expression> func, List<Integer> indexList) {
        Expressions.assertSliceSubscriptListSize(indexList);
        return Expressions.arraySliceExp(this, Expressions.createSubscriptExpList(func, indexList));
    }

    @Override
    public final Expression dot(Object key) {
        return Expressions.objectDotExp(this, List.of(key));
    }

    @Override
    public final Expression dot(Object key, Object key1) {
        return Expressions.objectDotExp(this, List.of(key, key1));
    }

    @Override
    public final Expression dot(Object key, Object key1, Object key2) {
        return Expressions.objectDotExp(this, List.of(key, key1, key2));
    }

    @Override
    public final Expression dot(Object key, Object key1, Object key2, Object key3) {
        return Expressions.objectDotExp(this, List.of(key, key1, key2, key3));
    }

    @Override
    public final Expression dot(Object key, Object key1, Object key2, Object key3, Object key4) {
        return Expressions.objectDotExp(this, List.of(key, key1, key2, key3, key4));
    }

    @Override
    public final Expression dots(List<?> subscriptList) {
        return Expressions.objectDotExp(this, List.copyOf(subscriptList));
    }

    @Override
    public final Expression bracket(Object key) {
        return Expressions.objectBracketExp(this, List.of(key));
    }

    @Override
    public final Expression bracket(Object key, Object key1) {
        return Expressions.objectBracketExp(this, List.of(key, key1));
    }

    @Override
    public final Expression bracket(Object key, Object key1, Object key2) {
        return Expressions.objectBracketExp(this, List.of(key, key1, key2));
    }

    @Override
    public final Expression bracket(Object key, Object key1, Object key2, Object key3) {
        return Expressions.objectBracketExp(this, List.of(key, key1, key2, key3));
    }

    @Override
    public final Expression bracket(Object key, Object key1, Object key2, Object key3, Object key4) {
        return Expressions.objectBracketExp(this, List.of(key, key1, key2, key3, key4));
    }

    @Override
    public final Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key) {
        final List<Object> list = List.of(
                Expressions.createSubscriptExp(func, key)
        );
        return Expressions.objectBracketExp(this, list);
    }

    @Override
    public final Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1) {
        final List<Object> list = List.of(
                Expressions.createSubscriptExp(func, key),
                Expressions.createSubscriptExp(func, key1)
        );
        return Expressions.objectBracketExp(this, list);
    }

    @Override
    public final Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1, Object key2) {
        final List<Object> list = List.of(
                Expressions.createSubscriptExp(func, key),
                Expressions.createSubscriptExp(func, key1),
                Expressions.createSubscriptExp(func, key2)
        );
        return Expressions.objectBracketExp(this, list);
    }

    @Override
    public final Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1, Object key2, Object key3) {
        final List<Object> list = List.of(
                Expressions.createSubscriptExp(func, key),
                Expressions.createSubscriptExp(func, key1),
                Expressions.createSubscriptExp(func, key2),
                Expressions.createSubscriptExp(func, key3)
        );
        return Expressions.objectBracketExp(this, list);
    }

    @Override
    public final Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1, Object key2, Object key3, Object key4) {
        final List<Object> list = List.of(
                Expressions.createSubscriptExp(func, key),
                Expressions.createSubscriptExp(func, key1),
                Expressions.createSubscriptExp(func, key2),
                Expressions.createSubscriptExp(func, key3),
                Expressions.createSubscriptExp(func, key4)
        );
        return Expressions.objectBracketExp(this, list);
    }

    @Override
    public final Expression brackets(List<?> subscriptList) {
        return Expressions.objectBracketExp(this, List.copyOf(subscriptList));
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Expression brackets(BiFunction<MappingType, Object, Expression> func, List<?> subscriptList) {
        return Expressions.objectBracketExp(this, Expressions.createSubscriptExpList(func, (List<Object>) subscriptList));
    }


    @Override
    public final Expression space(SQLs.DualOperator operator, Object right) {
        return Expressions.dualExp(this, operator, right);
    }

    @Override
    public final IPredicate space(SQLs.BiOperator operator, Object right) {
        return Expressions.biPredicate(this, operator, right);
    }

    @Override
    public final IPredicate space(SQLs.BiOperator operator, Object right, SQLToken modifier, Object optionalExp) {
        return similarToPredicate(operator, right, modifier, optionalExp);
    }


    @Override
    public final TypedExpression mapTo(final MappingType typeMeta) {
        return Expressions.mapExpType(this, typeMeta);
    }

    @Override
    public final TypedExpression castTo(final @Nullable MappingType type) {
        if (type == null) {
            throw ContextStack.clearStackAndNullPointer();
        }
        return Expressions.castExpToType(this, type);
    }


    @Override
    public final Selection as(String selectionLabel) {
        if (this instanceof CriteriaContexts.SelectionReference) {
            String m = String.format("the reference of %s don't support as() method.", Selection.class.getName());
            throw ContextStack.clearStackAndCriteriaError(m);
        }
        return ArmySelections.forExp(this, selectionLabel);
    }


    @Override
    public final SortItem asSortItem() {
        //always return this
        return this;
    }

    @Override
    public final SortItem asc() {
        return ArmySortItems.create(this, SortOrder.ASC, null);
    }

    @Override
    public final SortItem desc() {
        return ArmySortItems.create(this, SortOrder.DESC, null);
    }

    @Override
    public final SortItem ascSpace(@Nullable SQLs.NullsFirstLast firstLast) {
        return ArmySortItems.create(this, SortOrder.ASC, firstLast);
    }

    @Override
    public final SortItem descSpace(@Nullable SQLs.NullsFirstLast firstLast) {
        return ArmySortItems.create(this, SortOrder.DESC, firstLast);
    }


    /// 
    /// @see OperationExpression#space(SQLs.BiOperator, Object, SQLToken, Object)
    /// @see OperationTypedExpression#space(SQLs.BiOperator, BiFunction, Object, SQLToken, Object)
    final IPredicate similarToPredicate(SQLs.BiOperator operator, Object right, SQLToken modifier, Object optionalExp) {
        if (operator != DualBooleanOperator.SIMILAR_TO && operator != DualBooleanOperator.NOT_SIMILAR_TO) {
            String m = String.format("operator[%s] is unknown", operator);
            throw ContextStack.clearStackAndCriteriaError(m); // TODO add new
        }
        return Expressions.likePredicate(this, (DualBooleanOperator) operator, right, modifier, optionalExp);
    }

    static OperationExpression bracketExp(final @Nullable Expression expression) {
        final OperationExpression bracket;
        if (!(expression instanceof OperationExpression)) {
            throw NonOperationExpression.nonOperationExpression(expression);
        } else if (expression instanceof OperationSimpleExpression) {
            bracket = (OperationSimpleExpression) expression;
        } else {
            bracket = new BracketsExpression((ArmyExpression) expression);
        }
        return bracket;
    }


    static abstract class OperationSimpleExpression extends OperationExpression
            implements SimpleExpression,
            ArmySimpleExpression {

        /// package constructor
        OperationSimpleExpression() {

        }


        /*-------------------below json operator method -------------------*/


    } // OperationSimpleExpression

    static abstract class OperationTypedExpression extends OperationExpression implements TypedExpression {


        OperationTypedExpression() {
        }


        @Override
        public final <T> IPredicate equal(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.biPredicate(this, DualBooleanOperator.EQUAL, funcRef.apply(this, value));
        }

        @Override
        public final <T> IPredicate nullSafeEqual(BiFunction<TypedExpression, T, Expression> funcRef, @Nullable T value) {
            return Expressions.biPredicate(this, DualBooleanOperator.NULL_SAFE_EQUAL, funcRef.apply(this, value));
        }

        @Override
        public final <T> IPredicate less(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.biPredicate(this, DualBooleanOperator.LESS, funcRef.apply(this, value));
        }

        @Override
        public final <T> IPredicate lessEqual(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.biPredicate(this, DualBooleanOperator.LESS_EQUAL, funcRef.apply(this, value));
        }

        @Override
        public final <T> IPredicate greater(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.biPredicate(this, DualBooleanOperator.GREATER, funcRef.apply(this, value));
        }

        @Override
        public final <T> IPredicate greaterEqual(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.biPredicate(this, DualBooleanOperator.GREATER_EQUAL, funcRef.apply(this, value));
        }

        @Override
        public final <T> IPredicate notEqual(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.biPredicate(this, DualBooleanOperator.NOT_EQUAL, funcRef.apply(this, value));
        }

        @Override
        public final <T> IPredicate between(BiFunction<TypedExpression, T, Expression> funcRef, T first,
                                            SQLs.WordAnd and, T second) {
            return Expressions.betweenPredicate(this, false, null, funcRef.apply(this, first),
                    funcRef.apply(this, second)
            );
        }

        @Override
        public final <T, U> IPredicate between(BiFunction<TypedExpression, T, Expression> firstFuncRef,
                                               T first, SQLs.WordAnd and,
                                               BiFunction<TypedExpression, U, Expression> secondFuncRef,
                                               U second) {
            return Expressions.betweenPredicate(this, false, null, firstFuncRef.apply(this, first),
                    secondFuncRef.apply(this, second)
            );
        }

        @Override
        public final <T> IPredicate notBetween(BiFunction<TypedExpression, T, Expression> funcRef, T first,
                                               SQLs.WordAnd and, T second) {
            return Expressions.betweenPredicate(this, true, null, funcRef.apply(this, first),
                    funcRef.apply(this, second)
            );
        }

        @Override
        public final <T, U> IPredicate notBetween(BiFunction<TypedExpression, T, Expression> firstFuncRef,
                                                  T first, SQLs.WordAnd and,
                                                  BiFunction<TypedExpression, U, Expression> secondFuncRef,
                                                  U second) {
            return Expressions.betweenPredicate(this, true, null, firstFuncRef.apply(this, first),
                    secondFuncRef.apply(this, second)
            );
        }

        @Override
        public final <T> IPredicate between(@Nullable SQLs.BetweenModifier modifier,
                                            BiFunction<TypedExpression, T, Expression> funcRef, T first,
                                            SQLs.WordAnd and, T second) {
            return Expressions.betweenPredicate(this, false, modifier, funcRef.apply(this, first),
                    funcRef.apply(this, second)
            );
        }

        @Override
        public final <T, U> IPredicate between(@Nullable SQLs.BetweenModifier modifier,
                                               BiFunction<TypedExpression, T, Expression> firstFuncRef,
                                               T first, SQLs.WordAnd and,
                                               BiFunction<TypedExpression, U, Expression> secondFuncRef,
                                               U second) {
            return Expressions.betweenPredicate(this, false, modifier, firstFuncRef.apply(this, first),
                    secondFuncRef.apply(this, second)
            );
        }

        @Override
        public final <T> IPredicate notBetween(@Nullable SQLs.BetweenModifier modifier,
                                               BiFunction<TypedExpression, T, Expression> funcRef, T first,
                                               SQLs.WordAnd and, T second) {
            return Expressions.betweenPredicate(this, true, modifier, funcRef.apply(this, first), funcRef.apply(this, second));
        }

        @Override
        public final <T, U> IPredicate notBetween(@Nullable SQLs.BetweenModifier modifier,
                                                  BiFunction<TypedExpression, T, Expression> firstFuncRef,
                                                  T first, SQLs.WordAnd and,
                                                  BiFunction<TypedExpression, U, Expression> secondFuncRef,
                                                  U second) {
            return Expressions.betweenPredicate(this, true, modifier, firstFuncRef.apply(this, first),
                    secondFuncRef.apply(this, second)
            );
        }


        @Override
        public final <T> IPredicate is(SQLs.IsComparisonWord operator,
                                       BiFunction<TypedExpression, T, Expression> funcRef, @Nullable T value) {
            return Expressions.isComparisonPredicate(this, false, operator, funcRef.apply(this, value));
        }

        @Override
        public final <T> IPredicate isNot(SQLs.IsComparisonWord operator,
                                          BiFunction<TypedExpression, T, Expression> funcRef, @Nullable T value) {
            return Expressions.isComparisonPredicate(this, true, operator, funcRef.apply(this, value));
        }

        @Override
        public final IPredicate in(BiFunction<TypedExpression, Collection<?>, RowExpression> funcRef,
                                   Collection<?> value) {
            return Expressions.inPredicate(this, false, funcRef.apply(this, value));
        }

        @Override
        public final IPredicate notIn(BiFunction<TypedExpression, Collection<?>, RowExpression> funcRef,
                                      Collection<?> value) {
            return Expressions.inPredicate(this, true, funcRef.apply(this, value));
        }

        @Override
        public final IPredicate in(TeNamedParamsFunc<TypedExpression> funcRef, String paramName, int size) {
            return Expressions.inPredicate(this, false, funcRef.apply(this, paramName, size));
        }

        @Override
        public final IPredicate notIn(TeNamedParamsFunc<TypedExpression> funcRef, String paramName, int size) {
            return Expressions.inPredicate(this, true, funcRef.apply(this, paramName, size));
        }


        @Override
        public final <T> Expression mod(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.dualExp(this, DualExpOperator.MOD, funcRef.apply(this, value));
        }

        @Override
        public final <T> Expression times(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.dualExp(this, DualExpOperator.TIMES, funcRef.apply(this, value));
        }

        @Override
        public final <T> Expression plus(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.dualExp(this, DualExpOperator.PLUS, funcRef.apply(this, value));
        }

        @Override
        public final <T> Expression minus(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.dualExp(this, DualExpOperator.MINUS, funcRef.apply(this, value));
        }

        @Override
        public final <T> Expression divide(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.dualExp(this, DualExpOperator.DIVIDE, funcRef.apply(this, value));
        }

        @Override
        public final <T> Expression bitwiseAnd(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.dualExp(this, DualExpOperator.BITWISE_AND, funcRef.apply(this, value));
        }

        @Override
        public final <T> Expression bitwiseOr(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.dualExp(this, DualExpOperator.BITWISE_OR, funcRef.apply(this, value));
        }

        @Override
        public final <T> Expression bitwiseXor(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.dualExp(this, DualExpOperator.BITWISE_XOR, funcRef.apply(this, value));
        }

        @Override
        public final <T> Expression rightShift(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.dualExp(this, DualExpOperator.RIGHT_SHIFT, funcRef.apply(this, value));
        }

        @Override
        public final <T> IPredicate like(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.likePredicate(this, DualBooleanOperator.LIKE, funcRef.apply(this, value),
                    SQLs.ESCAPE, null);
        }


        @Override
        public final <T> IPredicate like(BiFunction<TypedExpression, T, Expression> funcRef, T value,
                                         SQLs.WordEscape escape, T escapeChar) {
            final Expression operand;
            operand = funcRef.apply(this, value);
            return Expressions.likePredicate(this, DualBooleanOperator.LIKE, operand,
                    escape, wrapEscapeIfNeed(operand, escapeChar)
            );
        }


        @Override
        public final <T> IPredicate notLike(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.likePredicate(this, DualBooleanOperator.NOT_LIKE, funcRef.apply(this, value),
                    SQLs.ESCAPE, null);
        }

        @Override
        public final <T> IPredicate notLike(BiFunction<TypedExpression, T, Expression> funcRef, T value,
                                            SQLs.WordEscape escape, T escapeChar) {
            final Expression operand;
            operand = funcRef.apply(this, value);
            return Expressions.likePredicate(this, DualBooleanOperator.NOT_LIKE, operand,
                    escape, wrapEscapeIfNeed(operand, escapeChar)
            );
        }

        @Override
        public final <T> IPredicate similarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.likePredicate(this, DualBooleanOperator.SIMILAR_TO, funcRef.apply(this, value),
                    SQLs.ESCAPE, null);
        }

        @Override
        public final <T> IPredicate similarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value, SQLs.WordEscape escape, T escapeChar) {
            final Expression operand;
            operand = funcRef.apply(this, value);
            return Expressions.likePredicate(this, DualBooleanOperator.SIMILAR_TO, operand,
                    escape, wrapEscapeIfNeed(operand, escapeChar)
            );
        }

        @Override
        public final <T> IPredicate notSimilarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.likePredicate(this, DualBooleanOperator.NOT_SIMILAR_TO, funcRef.apply(this, value),
                    SQLs.ESCAPE, null);
        }

        @Override
        public final <T> IPredicate notSimilarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value, SQLs.WordEscape escape, T escapeChar) {
            final Expression operand;
            operand = funcRef.apply(this, value);
            return Expressions.likePredicate(this, DualBooleanOperator.NOT_SIMILAR_TO, operand,
                    escape, wrapEscapeIfNeed(operand, escapeChar)
            );
        }

        @Override
        public final <T> Expression leftShift(BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.dualExp(this, DualExpOperator.LEFT_SHIFT, funcRef.apply(this, value));
        }

        @Override
        public final <T> Expression space(SQLs.DualOperator operator, BiFunction<TypedExpression, T, Expression> funcRef, T value) {
            return Expressions.dualExp(this, operator, funcRef.apply(this, value));
        }

        @Override
        public final <T> IPredicate space(SQLs.BiOperator operator, BiFunction<TypedExpression, T, Expression> funcRef, T right) {
            return Expressions.biPredicate(this, operator, funcRef.apply(this, right));
        }

        @Override
        public final <T> IPredicate space(SQLs.BiOperator operator, BiFunction<StringType, T, Expression> funcRef, T right, SQLToken modifier, T optionalExp) {
            return similarToPredicate(operator, funcRef.apply(StringType.INSTANCE, right), modifier, funcRef.apply(StringType.INSTANCE, optionalExp));
        }

        static Object wrapEscapeIfNeed(Expression operand, Object escapeChar) {
            if (operand instanceof LiteralExpression && !(escapeChar instanceof Expression)) {
                escapeChar = SQLs.constant(StringType.INSTANCE, escapeChar);
            }
            return escapeChar;
        }


    } // OperationDefiniteExpression


    static abstract class SqlFunctionExpression extends OperationSimpleExpression
            implements ArmySQLFunction {

        final String name;

        private final boolean buildIn;

        /// package constructor
        SqlFunctionExpression(String name) {
            this.name = name;
            this.buildIn = true;
        }

        /// package constructor
        SqlFunctionExpression(String name, boolean buildIn) {
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


    }//FunctionExpression


    static abstract class OperationCompoundExpression extends OperationExpression {

        /// package constructor
        OperationCompoundExpression() {
        }


    }//CompoundExpression


    static abstract class PredicateExpression extends OperationTypedExpression implements _Predicate {

        /// package constructor
        PredicateExpression() {
        }


        @Override
        public final BooleanType typeMeta() {
            return BooleanType.INSTANCE;
        }


    }//PredicateExpression


    static final class BracketsExpression extends OperationExpression implements ArmySimpleExpression {

        private final ArmyExpression expression;

        /// 
        /// **Private constructor**
        /// @see #bracketExp(Expression)
        private BracketsExpression(ArmyExpression expression) {
            this.expression = expression;
        }


        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);

            this.expression.appendSql(sqlBuilder, context);

            sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
        }


        @Override
        public String toString() {
            return _StringUtils.builder()
                    .append(_Constant.SPACE_LEFT_PAREN)
                    .append(this.expression)
                    .append(_Constant.SPACE_RIGHT_PAREN)
                    .toString();
        }


    }//BracketsExpression


}
