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
import io.army.dialect._Constant;
import io.army.dialect._SqlContext;
import io.army.lang.Nullable;
import io.army.util._Exceptions;
import io.army.util._StringUtils;

import java.util.Objects;
import java.util.function.BiFunction;

abstract class PgExpressions {

    private PgExpressions() {
        throw new UnsupportedOperationException();
    }


    /// @see Postgres#period(Expression, Expression)
    static Postgres._PeriodOverlapsClause overlaps(final @Nullable Expression start,
                                                   final @Nullable Expression endOrLength) {
        if (start == null || endOrLength == null) {
            throw ContextStack.clearStackAndNullPointer();
        } else if (start instanceof SqlValueParam.MultiParamValue || endOrLength instanceof SqlValueParam.MultiParamValue) {
            throw overlapsDontSupportMultiValue();
        }
        return new PeriodOverlapsPredicate(start, endOrLength);
    }


    static SimpleExpression unaryExpression(final PostgreUnaryExpOperator operator, final Expression operand) {
        if (!(operand instanceof OperationExpression)) {
            throw NonOperationExpression.nonOperationExpression(operand);
        }
        return new PostgreUnaryExpression(operator, operand);
    }


    static OperationPredicate unaryPredicate(PostgreBooleanUnaryOperator operator, Expression operand) {
        if (!(operand instanceof OperationExpression)) {
            throw NonOperationExpression.nonOperationExpression(operand);
        }
        return new PostgreUnaryPredicate(operator, operand);
    }

    static <T extends Expression> IPredicate dualPredicate(final T left, final PgDualBoolOperator operator,
                                                                     final T right) {

        return Expressions.biPredicate((OperationExpression) left, operator, right);
    }




    /*-------------------below private -------------------*/

    private static CriteriaException overlapsDontSupportMultiValue() {
        String m = "Postgre OVERLAPS operator don't support multi-value parameter/literal";
        return ContextStack.clearStackAndCriteriaError(m);
    }


    private static final class PeriodOverlapsPredicate extends OperationPredicate.OperationCompoundPredicate
            implements Postgres._PeriodOverlapsClause {

        private final ArmyExpression start1;

        private final ArmyExpression endOrLength1;

        private ArmyExpression start2;

        private ArmyExpression endOrLength2;

        /// @see Postgres#period(Expression, Expression)
        private PeriodOverlapsPredicate(Expression start1, Expression endOrLength1) {
            this.start1 = (OperationExpression) start1;
            this.endOrLength1 = (OperationExpression) endOrLength1;
        }

        @Override
        public IPredicate overlaps(final @Nullable Expression start, final @Nullable Expression endOrLength) {
            if (start == null || endOrLength == null) {
                throw ContextStack.clearStackAndNullPointer();
            } else if (start instanceof SqlValueParam.MultiParamValue || endOrLength instanceof SqlValueParam.MultiParamValue) {
                throw overlapsDontSupportMultiValue();
            } else if (this.start2 != null || this.endOrLength2 != null) {
                throw ContextStack.clearStackAnd(_Exceptions::castCriteriaApi);
            }
            this.start2 = (OperationExpression) start;
            this.endOrLength2 = (OperationExpression) endOrLength;
            return this;
        }

        @Override
        public <T> IPredicate overlaps(Expression start, BiFunction<Expression, T, Expression> valueOperator, T value) {
            return this.overlaps(start, valueOperator.apply(start, value));
        }

        @Override
        public <T> IPredicate overlaps(BiFunction<Expression, T, Expression> valueOperator, T value, Expression endOrLength) {
            return this.overlaps(valueOperator.apply(endOrLength, value), endOrLength);
        }

        @Override
        public IPredicate overlaps(TypeInfer type, BiFunction<TypeInfer, Object, Expression> valueOperator, Object start, Object endOrLength) {
            return this.overlaps(valueOperator.apply(type, start), valueOperator.apply(type, endOrLength));
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            final ArmyExpression start2 = this.start2, endOrLength2 = this.endOrLength2;
            if (start2 == null || endOrLength2 == null) {
                throw _Exceptions.castCriteriaApi();
            }

            sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);

            this.start1.appendSql(sqlBuilder, context);
            sqlBuilder.append(_Constant.SPACE_COMMA);
            this.endOrLength1.appendSql(sqlBuilder, context);

            sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN)
                    .append(" OVERLAPS")
                    .append(_Constant.SPACE_LEFT_PAREN);

            start2.appendSql(sqlBuilder, context);
            sqlBuilder.append(_Constant.SPACE_COMMA);
            endOrLength2.appendSql(sqlBuilder, context);

            sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
        }


        @Override
        public int hashCode() {
            return Objects.hash(this.start1, this.endOrLength1, this.start2, this.endOrLength2);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof PeriodOverlapsPredicate) {
                final PeriodOverlapsPredicate o = (PeriodOverlapsPredicate) obj;
                match = o.start1.equals(this.start1)
                        && o.endOrLength1.equals(this.endOrLength1)
                        && Objects.equals(o.start2, this.start2)
                        && Objects.equals(o.endOrLength2, this.endOrLength2);
            } else {
                match = false;
            }
            return match;
        }

        @Override
        public String toString() {
            return _StringUtils.builder()
                    .append(_Constant.SPACE_LEFT_PAREN)
                    .append(this.start1)
                    .append(_Constant.SPACE_COMMA)
                    .append(this.endOrLength1)
                    .append(_Constant.SPACE_RIGHT_PAREN)

                    .append(" OVERLAPS")

                    .append(_Constant.SPACE_LEFT_PAREN)
                    .append(this.start2)
                    .append(_Constant.SPACE_COMMA)
                    .append(this.endOrLength2)
                    .append(_Constant.SPACE_RIGHT_PAREN)
                    .toString();
        }


    }//PeriodOverlapsPredicate




    private static final class PostgreUnaryExpression extends Expressions.UnaryExpression {

        /// @see #unaryExpression(PostgreUnaryExpOperator, Expression)
        private PostgreUnaryExpression(PostgreUnaryExpOperator operator, Expression operand) {
            super(operator, operand);
        }
    }//PostgreUnaryExpression


    private static final class PostgreUnaryPredicate extends OperationPredicate.OperationCompoundPredicate {

        private final PostgreBooleanUnaryOperator operator;

        private final ArmyExpression operand;

        /// @see #unaryPredicate(PostgreBooleanUnaryOperator, Expression)
        private PostgreUnaryPredicate(PostgreBooleanUnaryOperator operator, Expression operand) {
            this.operator = operator;
            this.operand = (ArmyExpression) operand;
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            sqlBuilder.append(this.operator.spaceOperator);
            this.operand.appendSql(sqlBuilder, context);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.operator, this.operand);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof PostgreUnaryPredicate) {
                final PostgreUnaryPredicate o = (PostgreUnaryPredicate) obj;
                match = o.operator == this.operator
                        && o.operand.equals(this.operand);
            } else {
                match = false;
            }
            return match;
        }

        @Override
        public String toString() {
            return _StringUtils.builder()
                    .append(this.operator)
                    .append(this.operand)
                    .toString();
        }


    }//PostgreUnaryPredicate


}
