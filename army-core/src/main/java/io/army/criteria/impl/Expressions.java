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
import io.army.criteria.impl.inner._DerivedTable;
import io.army.dialect.Database;
import io.army.dialect._Constant;
import io.army.dialect._SqlContext;
import io.army.lang.Nullable;
import io.army.mapping.*;
import io.army.meta.TypeMeta;
import io.army.util.ArrayUtils;
import io.army.util._Collections;
import io.army.util._Exceptions;
import io.army.util._StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * <p>
 * This class hold the methods that create {@link Expression} and {@link IPredicate}.
 * <p>
 * Below is chines signature:<br/>
 * 当你在阅读这段代码时,我才真正在写这段代码,你阅读到哪里,我便写到哪里.
 *
 * @since 0.6.0
 */
abstract class Expressions {

    private static final Logger LOG = LoggerFactory.getLogger(Expressions.class);

    private static final LiteralMode LITERAL_MODE;

    static {
        try {
            final String location;
            location = String.format("META-INF/army/%s.properties", Expressions.class.getSimpleName());
            final Enumeration<URL> enumeration;
            enumeration = Thread.currentThread().getContextClassLoader().getResources(location);
            URL url = null;
            final Properties properties = new Properties();
            while (enumeration.hasMoreElements()) {
                url = enumeration.nextElement();
                properties.load(new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)));
            } // while loop

            final String key = "expression.literal.mode";
            final String modeValue;
            modeValue = properties.getProperty(key, LiteralMode.DEFAULT.name());
            if (url != null) {
                LOG.debug("Army {} is {} , effective url {}", key, modeValue, url);
            }
            LITERAL_MODE = LiteralMode.valueOf(modeValue);
        } catch (Exception e) {
            throw new RuntimeException("expression config load failure", e);
        }
    }


    private Expressions() {
    }

    static Expression dualExp(final Expression left, final SQLs.DualOperator operator, final Object right) {
        final Expression result;
        if (operator == SQLs.COLLATE) {
            result = collateExp(left, right);
        } else {
            result = new DualExpression(left, operator, wrapRight(left, left));
        }
        return result;
    }

    static Expression wrapRight(final Expression left, final @Nullable Object right) {
        final Expression rightExp;
        if (right == null) {
            throw ContextStack.clearStackAndNullPointer("right operand must non-null");
        } else if (right == SQLs.ABSENT) {
            throw ContextStack.clearStackAndNullPointer("right operand couldn't be absent.");
        } else if (right instanceof Expression) {
            rightExp = (Expression) right;
        } else if (left instanceof TypedExpression) {
            rightExp = wrapValue((TypedExpression) left, right);
        } else {
            rightExp = wrapValue(_MappingFactory.getDefault(right.getClass()), right);
        }
        return rightExp;
    }

    static Expression wrapEscape(final @Nullable Object right) {
        final Expression rightExp;
        if (right == null) {
            throw ContextStack.clearStackAndNullPointer("right operand must non-null");
        } else if (right == SQLs.ABSENT) {
            throw ContextStack.clearStackAndNullPointer("right operand couldn't be absent.");
        } else if (right instanceof Expression) {
            rightExp = (Expression) right;
        } else if (right instanceof String || right instanceof Character) {
            rightExp = wrapValue(StringType.INSTANCE, right);
        } else {
            throw ContextStack.clearStackAndCriteriaError("escape of LIKE/SIMILAR TO operator right operand must be Expression or String.");
        }
        return rightExp;
    }

    /// @see #wrapRight(Expression, Object)
    /// @see #wrapEscape(Object)
    static Expression wrapValue(final TypeInfer infer, final @Nullable Object value) {
        final Expression valueExp;
        switch (LITERAL_MODE) {
            case DEFAULT:
                valueExp = SQLs.param(infer, value);
                break;
            case LITERAL:
                valueExp = SQLs.literal(infer, value);
                break;
            case CONST:
                valueExp = SQLs.constant(infer, value);
                break;
            case PREFERENCE: {
                final MappingType type;
                if (infer instanceof MappingType) {
                    type = (MappingType) infer;
                } else if (infer instanceof TypeMeta) {
                    type = ((TypeMeta) infer).mappingType();
                } else {
                    type = infer.typeMeta().mappingType();
                }

                if (type instanceof _ArmyNoInjectionType) {
                    valueExp = SQLs.literal(infer, value);
                } else {
                    valueExp = SQLs.param(infer, value);
                }
            }
            break;
            default:
                throw _Exceptions.unexpectedEnum(LITERAL_MODE);
        }
        return valueExp;
    }


    /**
     * @see #dualExp(Expression, SQLs.DualOperator, Object)
     */
    static SimpleResultExpression collateExp(final Expression exp, final Object collation) {
        if (!(collation instanceof String)) {
            throw ContextStack.clearStackAndCriteriaError("collation should be String");
        } else if (_StringUtils.hasText((String) collation)) {
            throw ContextStack.clearStackAndCriteriaError("collation should have text");
        }
        return new CollateExpression(exp, (String) collation);
    }


    static SimpleExpression unaryExp(final UnaryExpOperator operator, final Expression operand) {
        if (!(operand instanceof OperationExpression)) {
            throw NonOperationExpression.nonOperationExpression(operand);
        }
        return new StandardUnaryExpression(operator, operand);
    }


    static OperationExpression.OperationTypedExpression mapExpType(OperationExpression expression, @Nullable MappingType type) {
        if (type == null) {
            throw ContextStack.clearStackAndNullPointer();
        }
        return new MappingExpression(expression, type);
    }

    static TypedExpression castExpToType(OperationExpression expression, TypeMeta typeMeta) {
        return new CastTypeExpression(expression, typeMeta);
    }


    static SimpleExpression scalarExpression(final SubQuery subQuery) {
        validateScalarSubQuery(subQuery);
        return new ScalarExpression(subQuery);
    }

    static OperationPredicate existsPredicate(final boolean not, final @Nullable SubQuery operand) {
        if (operand == null) {
            throw ContextStack.clearStackAndNullPointer();
        }
        validateSubQueryContext(operand);
        return new ExistsPredicate(not, operand);
    }

    static CompoundPredicate inPredicate(final SQLExpression left, final boolean not,
                                         final @Nullable SQLColumnSet right) {
        if (right == null) {
            throw ContextStack.clearStackAndNullPointer();
        } else if (left instanceof LengthTypedRowExpression) {
            RowExpressions.validateColumnSize((RowExpression) left, right);
        } else if (right instanceof SubQuery) {
            validateSubQueryContext((SubQuery) right);
            final int selectionCount;
            if ((selectionCount = ((ArmySubQuery) right).refAllSelection().size()) != 1) {
                String m;
                m = String.format("left operand is expression ,but right operand subquery selection count is %s",
                        selectionCount);
                throw ContextStack.clearStackAndCriteriaError(m);
            }
        } else if (!(right instanceof LengthTypedRowExpression)) {
            String m = String.format("don't right operand %s", right);
            throw ContextStack.clearStackAndCriteriaError(m);
        }
        return new InOperationPredicate(left, not, right);
    }


    static CompoundPredicate booleanTestPredicate(final OperationExpression expression,
                                                  boolean not, SQLs.BoolTestWord operand) {
        return new BooleanTestPredicate(expression, not, operand);
    }

    static CompoundPredicate isComparisonPredicate(final OperationExpression left, boolean not,
                                                   SQLs.IsComparisonWord operator, Object right) {
        return new IsComparisonPredicate(left, not, operator, (ArmyExpression) wrapRight(left, right));
    }

    static IPredicate biPredicate(OperationExpression left, final SQLs.BiOperator operator, final Object right) {
        return new DualPredicate(left, operator, wrapRight(left, right));
    }

    static CompoundPredicate dualPredicate(final OperationSQLExpression left, final SQLs.BiOperator operator,
                                           final @Nullable RightOperand right) {
        if (right == null) {
            throw ContextStack.clearStackAndNullPointer();
        } else if (!(right instanceof ArmyRightOperand)) {
            String m = String.format("don't support right operand %s", right);
            throw ContextStack.clearStackAndCriteriaError(m);
        } else if (left instanceof SQLColumnSet && right instanceof SQLColumnSet) {
            RowExpressions.validateColumnSize((SQLColumnSet) left, (SQLColumnSet) right);
        }
        return new DualPredicate(left, operator, right);
    }

    static CompoundPredicate likePredicate(final Expression left, final DualBooleanOperator operator,
                                           final Object right, SQLToken escape,
                                           final @Nullable Object escapeChar) {

        switch (operator) {
            case LIKE:
            case NOT_LIKE:
            case SIMILAR_TO: // currently,postgre only
            case NOT_SIMILAR_TO: // currently,postgre only
                break;
            default:
                // no bug,never here
                throw _Exceptions.unexpectedEnum(operator);
        }
        if (!(left instanceof OperationExpression)) {
            throw NonOperationExpression.nonOperationExpression(left);
        } else if (escape != SQLs.ESCAPE) {
            throw CriteriaUtils.errorModifier(escape);
        }
        final Expression escapeExp;
        if (escapeChar == null) {
            escapeExp = null;
        } else {
            escapeExp = wrapEscape(escapeChar);
        }
        return new LikePredicate((OperationExpression) left, operator, wrapRight(left, right), escapeExp);
    }


    static CompoundPredicate betweenPredicate(OperationExpression left, boolean not,
                                              @Nullable SQLs.BetweenModifier modifier,
                                              Object center, Object right) {
        return new BetweenPredicate(left, not, modifier, wrapRight(left, center), wrapRight(left, right));
    }

    static CompoundPredicate compareQueryPredicate(final SQLExpression left, final DualBooleanOperator operator,
                                                   final SQLs.QuantifiedWord queryOperator, final RightOperand right) {
        if (!(left instanceof OperationSQLExpression)) {
            throw ContextStack.clearStackAndNonArmyItem(left);
        }
        switch (operator) {
            case LESS:
            case LESS_EQUAL:
            case EQUAL:
            case NOT_EQUAL:
            case GREATER:
            case GREATER_EQUAL:
                break;
            default:
                // no bug,never here
                throw _Exceptions.unexpectedEnum(operator);
        }
        if (queryOperator != SQLs.ALL && queryOperator != SQLs.SOME && queryOperator != SQLs.ANY) {
            throw CriteriaUtils.notArmyOperator(queryOperator);
        }
        if (right instanceof SubQuery) {
            assertColumnSubQuery(operator, queryOperator, (SubQuery) right);
        }
        //else if (!(right instanceof ArrayExpression)) {  // TODO 为什么 有这行
//            // no bug,never here
//            throw new IllegalArgumentException();
//        }
        else if (!(right instanceof OperationExpression)) {
            throw ContextStack.clearStackAndNonArmyItem(right);
        }
        return new SubQueryPredicate(left, operator, queryOperator, right);
    }


    static Expression arraySliceExp(OperationExpression array, List<?> subscriptList) {
        return new ArraySliceExpression(array, subscriptList);
    }

    static Expression objectDotExp(OperationExpression object, List<?> subscriptList) {
        return new DotOperatorExpression(object, subscriptList);
    }

    static Expression objectBracketExp(OperationExpression object, List<?> subscriptList) {
        return new BracketOperatorExpression(object, subscriptList);
    }


    static <T> List<Expression> createSubscriptExpList(BiFunction<MappingType, T, Expression> func, List<T> subscriptList) {
        final List<Expression> list = _Collections.arrayList(subscriptList.size());
        for (T subscript : subscriptList) {
            list.add(createSubscriptExp(func, subscript));
        } // loop
        return list;
    }


    static <T> Expression createSubscriptExp(BiFunction<MappingType, T, Expression> func, @Nullable T subscript) {
        final Expression exp;
        exp = switch (subscript) {
            case null -> throw ContextStack.clearStackAndNullPointer();
            case Integer _ -> func.apply(IntegerType.INSTANCE, subscript);
            case String _ -> func.apply(StringType.INSTANCE, subscript);
            default -> throw ContextStack.clearStackAndCriteriaError("subscript literal must be Integer or String");
        };
        if (exp == null) {
            throw ContextStack.clearStackAndNullPointer();
        }

        return exp;
    }


    @Deprecated
    static TypeMeta nonNullFirstArrayType(final List<Object> elementList) {
        throw new UnsupportedOperationException();
    }


    static Expression array(@Nullable List<?> elementList) {
        if (elementList == null) {
            throw ContextStack.clearStackAndNullPointer();
        }
        return new SimpleArrayExpression(_Collections.copyList(elementList));
    }

    static Expression array(@Nullable SubQuery subQuery) {
        if (subQuery == null) {
            throw ContextStack.clearStackAndNullPointer();
        }
        return new SubQueryArrayExpression(subQuery);
    }


    static GroupByItem.ExpressionGroup emptyParens() {
        return EmptyParensGroupByItem.INSTANCE;
    }


    static GroupByItem parens(final @Nullable GroupingModifier modifier, final @Nullable GroupByItem exp) {
        if (!(exp instanceof ArmyGroupByItem)) {
            throw ContextStack.clearStackAndNonArmyItem(exp);
        }
        final List<ArmyGroupByItem> list;
        list = _Collections.singletonList((ArmyGroupByItem) exp);
        final GroupByItem item;
        if (modifier == null) {
            item = new ParensGroupByItemGroup(null, list);
        } else {
            item = new ParensGroupByItem(modifier, list);
        }
        return item;
    }

    static GroupByItem parens(final @Nullable GroupingModifier modifier, final GroupByItem exp1,
                              final GroupByItem exp2) {
        if (!(exp1 instanceof ArmyGroupByItem)) {
            throw ContextStack.clearStackAndNonArmyItem(exp1);
        } else if (!(exp2 instanceof ArmyGroupByItem)) {
            throw ContextStack.clearStackAndNonArmyItem(exp2);
        }

        final List<ArmyGroupByItem> list;
        list = ArrayUtils.of((ArmyGroupByItem) exp1, (ArmyGroupByItem) exp2);
        final GroupByItem item;
        if (modifier == null) {
            item = new ParensGroupByItemGroup(null, list);
        } else {
            item = new ParensGroupByItem(modifier, list);
        }
        return item;
    }

    static GroupByItem parens(final @Nullable GroupingModifier modifier, final GroupByItem exp1,
                              final GroupByItem exp2, final GroupByItem exp3, GroupByItem... rest) {
        if (!(exp1 instanceof ArmyGroupByItem)) {
            throw ContextStack.clearStackAndNonArmyItem(exp1);
        } else if (!(exp2 instanceof ArmyGroupByItem)) {
            throw ContextStack.clearStackAndNonArmyItem(exp2);
        } else if (!(exp3 instanceof ArmyGroupByItem)) {
            throw ContextStack.clearStackAndNonArmyItem(exp3);
        }
        final List<ArmyGroupByItem> list = _Collections.arrayList(3 + rest.length);

        list.add((ArmyGroupByItem) exp1);
        list.add((ArmyGroupByItem) exp2);
        list.add((ArmyGroupByItem) exp3);

        for (GroupByItem e : rest) {
            if (!(e instanceof ArmyGroupByItem)) {
                throw ContextStack.clearStackAndNonArmyItem(e);
            }
            list.add((ArmyGroupByItem) e);
        }
        final GroupByItem item;
        if (modifier == null) {
            item = new ParensGroupByItemGroup(null, list);
        } else {
            item = new ParensGroupByItem(modifier, list);
        }
        return item;
    }

    static <T extends GroupByItem> GroupByItem parens(final @Nullable GroupingModifier modifier,
                                                      final Consumer<Consumer<T>> consumer) {
        final List<ArmyGroupByItem> list = _Collections.arrayList();
        consumer.accept(e -> {
            if (!(e instanceof ArmyGroupByItem)) {
                throw ContextStack.clearStackAndNonArmyItem(e);
            }
            list.add((ArmyGroupByItem) e);
        });
        final GroupByItem item;
        if (list.isEmpty()) {
            if (modifier != null) {
                throw CriteriaUtils.dontAddAnyItem();
            }
            item = EmptyParensGroupByItem.INSTANCE;
        } else if (modifier == null) {
            item = new ParensGroupByItemGroup(null, list);
        } else {
            item = new ParensGroupByItem(modifier, list);
        }
        return item;
    }


    static void validateSubQueryContext(final @Nullable SubQuery subQuery) {
        if (subQuery == null) {
            throw ContextStack.clearStackAndNullPointer();
        }
        final CriteriaContext context, currentContext;
        context = ((CriteriaContextSpec) subQuery).getContext();
        currentContext = ContextStack.peek();
        if (context.getOuterContext() != currentContext) {
            String m = String.format("outer context of %s don't match.", subQuery);
            throw ContextStack.clearStackAndCriteriaError(m);
        }
        currentContext.validateDialect(context);

    }



    /*-------------------below private method-------------------*/

    /**
     * @see CastTypeExpression
     */
    private static void appendTypeCastExp(ArmySQLExpression expression, StringBuilder sqlBuilder, _SqlContext context, MappingType type) {
        final boolean outerParen = !(expression instanceof ArmySimpleSQLExpression);
        if (outerParen) {
            sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
        }
        expression.appendSql(sqlBuilder, context);
        if (outerParen) {
            sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
        }
        appendTypeCastSuffix(sqlBuilder, context, type);
    }

    /**
     * @see #appendTypeCastExp(ArmySQLExpression, StringBuilder, _SqlContext, MappingType)
     * @see SimpleArrayExpression
     */
    private static void appendTypeCastSuffix(StringBuilder sqlBuilder, _SqlContext context, MappingType type) {
        switch (context.dialectDatabase()) {
            case PostgreSQL: {
                sqlBuilder.append(_Constant.DOUBLE_COLON);
                context.parser().typeName(type, sqlBuilder);
            }
            break;
            case MySQL:
            case H2:
            default: // TODO add other database
                throw new CriteriaException(String.format("not support %s", context.dialectDatabase()));
        }
    }

    /**
     * @see #scalarExpression(SubQuery)
     * @see #array(SubQuery)
     */
    private static void validateScalarSubQuery(final SubQuery subQuery) {
        validateSubQueryContext(subQuery);
        final List<? extends Selection> selectionList;
        selectionList = ((_DerivedTable) subQuery).refAllSelection();
        if (selectionList.size() != 1) {
            throw ContextStack.clearStackAnd(_Exceptions::nonScalarSubQuery, subQuery);
        }
    }


    /**
     * @see #compareQueryPredicate(SQLExpression, DualBooleanOperator, SQLs.QuantifiedWord, RightOperand)
     */
    private static void assertColumnSubQuery(final DualBooleanOperator operator
            , final SQLs.QuantifiedWord queryOperator, final SubQuery subQuery) {
        validateSubQueryContext(subQuery);
        if (((_DerivedTable) subQuery).refAllSelection().size() != 1) {
            StringBuilder builder = _StringUtils.builder();
            builder.append("Operator ")
                    .append(operator.name())
                    .append(queryOperator)
                    .append(operator)
                    .append(" only support column sub query.");
            throw ContextStack.clearStackAndCriteriaError(builder.toString());
        }

    }


    ///
    ///
    /// @param nullAction 1. {@link Boolean#TRUE} : output the null as parameter or literal
    /// 2. {@link Boolean#FALSE} : no action
    /// 3. {@link String} : the error message when expression is null
    ///
    ///
    static void appendRightExpression(Object expression, StringBuilder sqlBuilder, _SqlContext context, Object nullAction) {
        if (expression instanceof SQLExpression) {
            appendConcatenationExpression((SQLExpression) expression, sqlBuilder, context);
        } else if (expression != SQLs.ABSENT) {
            context.appendParam(SQLs.parameter(expression));
        } else if (nullAction == Boolean.TRUE) {
            context.appendParam(SQLs.param(StringType.INSTANCE, null));
        } else if (nullAction == Boolean.FALSE) {
            // no-op
        } else {
            throw new CriteriaException(nullAction.toString());
        }
    }

    static void appendConcatenationExpression(SQLExpression expression, StringBuilder sqlBuilder, _SqlContext context) {
        final boolean outerParen = !(expression instanceof ArmySimpleSQLExpression);
        if (outerParen) {
            sqlBuilder.append(_Constant.SPACE)
                    .append(_Constant.LEFT_PAREN);
        }
        ((ArmySQLExpression) expression).appendSql(sqlBuilder, context);
        if (outerParen) {
            sqlBuilder.append(_Constant.RIGHT_PAREN);
        }
    }

    ///
    ///
    /// @param nullAction 1. {@link Boolean#TRUE} : output the null as parameter or literal
    /// 2. {@link Boolean#FALSE} : no action
    /// 3. {@link String} : the error message when expression is null
    ///
    ///
    static void toStringRightExpression(@Nullable Object expression, StringBuilder builder, Object nullAction) {
        switch (expression) {
            case null:
                if (nullAction == Boolean.TRUE) {
                    builder.append(" ?");
                } else if (nullAction == Boolean.FALSE) {
                    // no-op
                } else {
                    throw new CriteriaException(nullAction.toString());
                }
                break;
            case SQLExpression _:
                toStringConcatenationExpression((SQLExpression) expression, builder);
                break;
            default:
                builder.append(" ?");

        }

    }

    static void toStringConcatenationExpression(SQLExpression expression, StringBuilder builder) {
        final boolean outerParen = !(expression instanceof ArmySimpleSQLExpression);
        if (outerParen) {
            builder.append(_Constant.SPACE)
                    .append(_Constant.LEFT_PAREN);
        }
        builder.append(expression);
        if (outerParen) {
            builder.append(_Constant.RIGHT_PAREN);
        }
    }


    static void assertSliceSubscriptListSize(List<?> subscriptList) {
        final int size = subscriptList.size();
        if ((size & 1) != 0) {
            String s = String.format("array slice subscript list size[%s] is odd", size);
            throw ContextStack.clearStackAndCriteriaError(s);
        }
    }


    static void assertSubscriptListNotEmpty(List<?> subscriptList, String type) {
        if (subscriptList.isEmpty()) {
            throw ContextStack.clearStackAndCriteriaError(String.format("%s subscript list must non-empty.", type));
        }
    }


    private static CriteriaException subscriptError(@Nullable Object subscript, String type) {
        return new CriteriaException(String.format("subscript[%s] and bracket of %s not match", subscript, type));
    }


    /**
     * @see #inPredicate(SQLExpression, boolean, SQLColumnSet)
     */
    private static CriteriaException inOpeRowAndSubQueryNotMatch(int leftSize, int rightSize) {
        String m = String.format("Left operand of IN  is row expression and column size is %s, but subquery selection count is %s",
                leftSize, rightSize);
        throw ContextStack.clearStackAndCriteriaError(m);
    }


    private static CriteriaException unsupportedOperator(Operator operator, Database database) {
        String m = String.format("%s isn't supported by %s", operator, database);
        return new CriteriaException(m);
    }


    /**
     * This class is a implementation of {@link Expression}.
     * The expression consist of a left {@link Expression} ,a {@link DualBooleanOperator} and right {@link Expression}.
     *
     * @since 0.6.0
     */
    private static class DualExpression extends OperationExpression.OperationCompoundExpression {

        final ArmyExpression left;

        final SQLs.DualOperator operator;

        final ArmyExpression right;

        /**
         * @see #dualExp(Expression, SQLs.DualOperator, Object)
         */
        private DualExpression(final Expression left, final SQLs.DualOperator operator, final Expression right) {
            this.left = (ArmyExpression) left;
            this.operator = operator;
            this.right = (ArmyExpression) right;
        }

        @Override
        public final void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {

            final SQLs.DualOperator operator = this.operator;

            final ArmyExpression left = this.left;

            final boolean leftOuterParens;
            leftOuterParens = left instanceof OperationPredicate.OperationCompoundPredicate; // if CompoundPredicate must append outer parens

            //1. append left expression
            if (leftOuterParens) {   // TODO 正确吗?
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }

            left.appendSql(sqlBuilder, context);

            if (leftOuterParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }

            //2. append operator
            sqlBuilder.append(operator.spaceRender(context.dialectDatabase()));

            appendRightExpression(this.right, sqlBuilder, context, "right operand couldn't be absent.");

        }

        @Override
        public final String toString() {
            final StringBuilder sqlBuilder = new StringBuilder();
            final ArmyExpression left = this.left, right = this.right;
            final boolean leftOuterParens, rightOuterParens;
            leftOuterParens = left instanceof OperationPredicate.OperationCompoundPredicate; // if CompoundPredicate must append outer parens

            //1. append left expression
            if (leftOuterParens) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }

            sqlBuilder.append(left);

            if (leftOuterParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }

            //2. append operator
            sqlBuilder.append(this.operator.spaceRender(Database.PostgreSQL));

            rightOuterParens = !(right instanceof ArmySimpleExpression);
            if (rightOuterParens) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }
            //3. append right expression
            sqlBuilder.append(right);

            if (rightOuterParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }

            return sqlBuilder.toString();
        }


    }//DualExpression


    /**
     * <p>
     * This class representing unary expression,unary expression always out outer bracket.
     * <p>
     * This class is a implementation of {@link Expression}.
     * The expression consist of a  {@link Expression} and a {@link UnaryExpOperator}.
     */
    static class UnaryExpression extends OperationExpression.OperationSimpleExpression {

        private final Operator.SqlUnaryExpOperator operator;

        final ArmyExpression operand;

        /**
         * @see #unaryExp(UnaryExpOperator, Expression)
         */
        UnaryExpression(Operator.SqlUnaryExpOperator operator, Expression operand) {
            this.operator = operator;
            this.operand = (ArmyExpression) operand;
        }


        @Override
        public final void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {

            final Operator.SqlUnaryExpOperator operator = this.operator;
            final boolean outerParens;
            outerParens = operator != UnaryExpOperator.NEGATE;


            if (outerParens) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }
            sqlBuilder.append(operator.spaceRender(context.dialectDatabase()));

            final ArmyExpression operand = this.operand;
            final boolean operandOuterParens = !(operand instanceof ArmySimpleExpression);

            if (operandOuterParens) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }
            // append expression
            operand.appendSql(sqlBuilder, context);

            if (operandOuterParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }

            if (outerParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }

        }


        @Override
        public final String toString() {
            final Operator.SqlUnaryExpOperator operator = this.operator;
            final boolean outerParens;
            outerParens = operator != UnaryExpOperator.NEGATE;

            final StringBuilder builder = new StringBuilder();

            if (outerParens) {
                builder.append(_Constant.SPACE_LEFT_PAREN);
            }
            builder.append(operator.spaceRender());

            final ArmyExpression operand = this.operand;
            final boolean operandOuterParens = !(operand instanceof ArmySimpleExpression);

            if (operandOuterParens) {
                builder.append(_Constant.SPACE_LEFT_PAREN);
            }
            // append expression
            builder.append(operand);

            if (operandOuterParens) {
                builder.append(_Constant.SPACE_RIGHT_PAREN);
            }

            if (outerParens) {
                builder.append(_Constant.SPACE_RIGHT_PAREN);
            }
            return builder.toString();
        }


    }//UnaryExpression


    private static final class StandardUnaryExpression extends UnaryExpression {

        /**
         * @see #unaryExp(UnaryExpOperator, Expression)
         */
        private StandardUnaryExpression(UnaryExpOperator operator, Expression operand) {
            super(operator, operand);
        }

    }//StandardUnaryExpression


    static final class ScalarExpression extends OperationExpression.OperationSimpleExpression {

        private final SubQuery subQuery;

        /**
         * @see #scalarExpression(SubQuery)
         */
        private ScalarExpression(SubQuery subQuery) {
            this.subQuery = subQuery;
        }


        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            context.appendSubQuery(this.subQuery);
        }


        @Override
        public String toString() {
            return _StringUtils.builder()
                    .append(_Constant.SPACE_LEFT_PAREN)
                    .append(" scalar sub query: ")
                    .append(this.subQuery.getClass().getName())
                    .append(_Constant.SPACE_RIGHT_PAREN)
                    .toString();
        }

        /**
         * @return see {@link JoinableClause.SimpleQuery#validateIdDefaultExpression()}
         */
        List<String> validateIdDefaultExpression() {
            final SubQuery subQuery = this.subQuery;
            if (!(subQuery instanceof SimpleQueries)) {
                String m = "scalar sub expression must be simple sub query in child id default expression.";
                throw ContextStack.clearStackAnd(IllegalOneStmtModeException::new, m);
            }
            return ((JoinableClause.SimpleQuery) subQuery).validateIdDefaultExpression();
        }


    }//ScalarExpression

    /*-------------------below predicate class-------------------*/

    private static final class ExistsPredicate extends OperationPredicate.OperationCompoundPredicate {

        private final boolean not;

        private final ArmySubQuery subQuery;

        /**
         * @see #existsPredicate(boolean, SubQuery)
         */
        private ExistsPredicate(boolean not, SubQuery subQuery) {
            this.not = not;
            this.subQuery = (ArmySubQuery) subQuery;
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            if (this.not) {
                sqlBuilder.append(_Constant.SPACE_NOT);
            }
            sqlBuilder.append(_Constant.SPACE_EXISTS);
            context.appendSubQuery(this.subQuery);
        }


        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            if (this.not) {
                builder.append(_Constant.SPACE_NOT);
            }
            builder.append(_Constant.SPACE_EXISTS)
                    .append(this.subQuery);
            return builder.toString();
        }


    }//UnaryPredicate


    static final class DualPredicate extends OperationPredicate.OperationCompoundPredicate {


        final ArmySQLExpression left;

        final SQLs.BiOperator operator;

        final ArmyRightOperand right;


        DualPredicate(OperationSQLExpression left, SQLs.BiOperator operator, RightOperand right) {
            this.left = left;
            this.operator = operator;
            this.right = (ArmyRightOperand) right;
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {

            final ArmySQLExpression left = this.left;
            final boolean leftOuterParens;
            leftOuterParens = left instanceof OperationCompoundPredicate;

            if (leftOuterParens) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }
            left.appendSql(sqlBuilder, context);
            if (leftOuterParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }

            sqlBuilder.append(this.operator.spaceRender(context.dialectDatabase()));

            final ArmyRightOperand right = this.right;
            if (right instanceof SubQuery) {
                context.appendSubQuery((SubQuery) right);
            } else {
                final boolean rightOuterParens = !(right instanceof ArmySimpleSQLExpression);
                if (rightOuterParens) {
                    sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
                }
                ((ArmySQLExpression) right).appendSql(sqlBuilder, context);
                if (rightOuterParens) {
                    sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
                }
            }

        }

        @Override
        public String toString() {

            final StringBuilder sqlBuilder;
            sqlBuilder = new StringBuilder();

            final ArmySQLExpression left = this.left;

            final boolean leftOuterParens;
            leftOuterParens = left instanceof OperationCompoundPredicate;

            if (leftOuterParens) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }
            sqlBuilder.append(left);
            if (leftOuterParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }

            final ArmyRightOperand right = this.right;
            if (right instanceof SubQuery) {
                sqlBuilder.append(right);
            } else {
                final boolean rightOuterParens = !(right instanceof ArmySimpleExpression);

                if (rightOuterParens) {
                    sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
                }
                sqlBuilder.append(right);

                if (rightOuterParens) {
                    sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
                }
            }
            return sqlBuilder.toString();
        }


    }//DualPredicate


    private static final class LikePredicate extends OperationPredicate.OperationCompoundPredicate {

        private final ArmyExpression left;

        private final DualBooleanOperator operator;

        private final ArmyExpression right;

        private final ArmyExpression escapeChar;

        /**
         * @see #likePredicate(Expression, DualBooleanOperator, Object, SQLToken, Object)
         */
        private LikePredicate(OperationExpression left, DualBooleanOperator operator, Expression right,
                              @Nullable Expression escapeChar) {
            this.left = left;
            this.operator = operator;
            this.right = (ArmyExpression) right;
            this.escapeChar = (ArmyExpression) escapeChar;
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {

            final ArmyExpression left = this.left, right = this.right, escapeChar = this.escapeChar;
            final boolean leftOuterParens, rightOuterParens, escapeCharOuterParens;
            leftOuterParens = left instanceof IPredicate;// TODO 正确吗? if predicate must append outer parens
            // 1. append left
            if (leftOuterParens) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }
            left.appendSql(sqlBuilder, context);
            if (leftOuterParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }

            // 2. append operator
            switch (this.operator) {
                case LIKE:
                case NOT_LIKE:
                    break;
                case SIMILAR_TO:
                case NOT_SIMILAR_TO: {
                    if (context.dialectDatabase() != Database.PostgreSQL) {
                        throw unsupportedOperator(this.operator, context.dialectDatabase());
                    }
                }
                break;
                default:
                    //no bug,never here
                    throw _Exceptions.unexpectedEnum(this.operator);
            }

            sqlBuilder.append(this.operator.spaceOperator);

            rightOuterParens = !(right instanceof ArmySimpleExpression);
            // 3. append right
            if (rightOuterParens) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }
            right.appendSql(sqlBuilder, context);
            if (rightOuterParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }
            // 4. append ESCAPES clause
            if (escapeChar != null) {
                sqlBuilder.append(SQLs.ESCAPE.spaceRender());
                escapeCharOuterParens = !(escapeChar instanceof ArmySimpleExpression);
                if (escapeCharOuterParens) {
                    sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
                }
                escapeChar.appendSql(sqlBuilder, context);
                if (escapeCharOuterParens) {
                    sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
                }
            }


        }


        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append(this.left)
                    .append(this.operator.spaceOperator)
                    .append(this.right);

            if (this.escapeChar != null) {
                builder.append(SQLs.ESCAPE.spaceRender())
                        .append(this.escapeChar);
            }
            return builder.toString();
        }


    }//LikePredicate


    private static class BetweenPredicate extends OperationPredicate.OperationCompoundPredicate {

        private final boolean not;

        private final SQLs.BetweenModifier modifier;

        private final ArmyExpression left;

        private final ArmyExpression center;

        private final ArmyExpression right;

        /**
         * @see #betweenPredicate(OperationExpression, boolean, SQLs.BetweenModifier, Expression, Expression)
         */
        private BetweenPredicate(Expression left, boolean not, @Nullable SQLs.BetweenModifier modifier,
                                 Expression center, Expression right) {
            this.not = not;
            this.modifier = modifier;
            this.left = (ArmyExpression) left;
            this.center = (ArmyExpression) center;
            this.right = (ArmyExpression) right;
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {

            final ArmyExpression left = this.left, center = this.center, right = this.right;
            final boolean leftOuterParens, centerOuterParens, rightOuterParens;
            leftOuterParens = left instanceof OperationCompoundPredicate; // if predicate must append outer parens
            // 1. append left
            if (leftOuterParens) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }
            left.appendSql(sqlBuilder, context);
            if (leftOuterParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }

            // 2. append NOT operator(or not)
            if (this.not) {
                sqlBuilder.append(" NOT");
            }
            // 3. append BETWEEN operator
            sqlBuilder.append(" BETWEEN");

            // 4. append modifier (or not)
            if (this.modifier != null) {
                //TODO validate database support
                sqlBuilder.append(this.modifier.spaceRender());
            }

            centerOuterParens = !(center instanceof ArmySimpleExpression);
            rightOuterParens = !(right instanceof ArmySimpleExpression);

            // 5. append center operand
            if (centerOuterParens) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }
            center.appendSql(sqlBuilder, context);
            if (centerOuterParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }
            // 6. append AND operator
            sqlBuilder.append(_Constant.SPACE_AND);

            // 7. append right operand
            if (rightOuterParens) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }
            right.appendSql(sqlBuilder, context);
            if (rightOuterParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }

        }


        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            // 1. append left operand
            builder.append(this.left);
            // 2. append NOT operator(or not)
            if (this.not) {
                builder.append(" NOT");
            }
            // 3. append BETWEEN operator
            builder.append(" BETWEEN");

            // 4. append modifier (or not)
            if (this.modifier != null) {
                //TODO validate database support
                builder.append(this.modifier.spaceRender());
            }
            final ArmyExpression center = this.center, right = this.right;

            final boolean centerOuterParens, rightOuterParens;
            centerOuterParens = !(center instanceof ArmySimpleExpression);
            rightOuterParens = !(right instanceof ArmySimpleExpression);

            // 5. append center operand
            if (centerOuterParens) {
                builder.append(_Constant.SPACE_LEFT_PAREN);
            }
            builder.append(center);
            if (centerOuterParens) {
                builder.append(_Constant.SPACE_RIGHT_PAREN);
            }
            // 6. append AND operator
            builder.append(_Constant.SPACE_AND);

            // 7. append right operand
            if (rightOuterParens) {
                builder.append(_Constant.SPACE_LEFT_PAREN);
            }
            builder.append(right);
            if (rightOuterParens) {
                builder.append(_Constant.SPACE_RIGHT_PAREN);
            }
            return builder.toString();
        }


    }//BetweenPredicate


    private static class MappingExpression extends OperationExpression.OperationTypedExpression {

        private final ArmyExpression expression;

        private final TypeMeta type;


        private MappingExpression(OperationExpression expression, MappingType type) {
            this.expression = expression;
            this.type = type;
        }

        @Override
        public MappingType typeMeta() {
            return this.type.mappingType();
        }

        @Override
        public final void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            this.expression.appendSql(sqlBuilder, context);
        }


        @Override
        public final String toString() {
            return this.expression.toString();
        }


    } // MappingExpression


    private static class CastTypeExpression extends OperationExpression.OperationTypedExpression
            implements ArmySimpleExpression {

        private final ArmySQLExpression expression;

        private final MappingType type;


        private CastTypeExpression(ArmySQLExpression expression, TypeMeta type) {
            this.expression = expression;
            if (type instanceof MappingType) {
                this.type = (MappingType) type;
            } else {
                this.type = type.mappingType();
            }
        }


        @Override
        public TypeMeta typeMeta() {
            return this.type;
        }

        @Override
        public final void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            appendTypeCastExp(this.expression, sqlBuilder, context, this.type);
        }


        @Override
        public final String toString() {
            return this.expression.toString();
        }


    } // CastTypeExpression


    private static final class SubQueryPredicate extends OperationPredicate.OperationCompoundPredicate {
        private final ArmySQLExpression left;

        private final DualBooleanOperator operator;

        private final SQLs.QuantifiedWord queryOperator;

        private final RightOperand right;

        /**
         * @see #compareQueryPredicate(SQLExpression, DualBooleanOperator, SQLs.QuantifiedWord, RightOperand)
         */
        private SubQueryPredicate(SQLExpression left, DualBooleanOperator operator,
                                  SQLs.QuantifiedWord queryOperator, RightOperand right) {
            this.left = (ArmySQLExpression) left;
            this.operator = operator;
            this.queryOperator = queryOperator;
            this.right = right;
        }


        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {

            this.left.appendSql(sqlBuilder, context);
            sqlBuilder.append(this.operator.spaceOperator)
                    .append(this.queryOperator.spaceRender());

            final RightOperand right = this.right;

            if (right instanceof SubQuery) {
                context.appendSubQuery((SubQuery) right);
            } else {
                ((ArmySQLExpression) right).appendSql(sqlBuilder, context);
            }

        }


        @Override
        public String toString() {
            return _StringUtils.builder()
                    .append(this.left)
                    .append(this.operator.spaceOperator)
                    .append(this.queryOperator.spaceRender())
                    .append(this.right)
                    .toString();
        }


    } // SubQueryPredicate


    private static final class BooleanTestPredicate extends OperationPredicate.OperationCompoundPredicate {

        private final OperationExpression expression;

        private final boolean not;


        private final SQLs.BoolTestWord operand;

        /**
         * @see #booleanTestPredicate(OperationExpression, boolean, SQLs.BoolTestWord)
         */
        private BooleanTestPredicate(OperationExpression expression, boolean not, SQLs.BoolTestWord operand) {
            this.expression = expression;
            this.not = not;
            this.operand = operand;
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            this.expression.appendSql(sqlBuilder, context);
            if (this.not) {
                sqlBuilder.append(" IS NOT");
            } else {
                sqlBuilder.append(" IS");
            }
            sqlBuilder.append(this.operand.spaceRender());
        }


        @Override
        public String toString() {
            final StringBuilder sqlBuilder;
            sqlBuilder = new StringBuilder()
                    .append(this.expression);
            if (this.not) {
                sqlBuilder.append(" IS NOT");
            } else {
                sqlBuilder.append(" IS");
            }

            return sqlBuilder.append(this.operand.spaceRender())
                    .toString();
        }


    }//BooleanTestPredicate

    private static final class IsComparisonPredicate extends OperationPredicate.OperationCompoundPredicate {

        private final ArmyExpression left;

        private final boolean not;

        private final SQLs.IsComparisonWord operator;

        private final ArmyExpression right;


        /**
         * @see #isComparisonPredicate(OperationExpression, boolean, SQLs.IsComparisonWord, Object)
         */
        private IsComparisonPredicate(OperationExpression left, boolean not, SQLs.IsComparisonWord operator,
                                      ArmyExpression right) {
            this.left = left;
            this.not = not;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            //TODO validate database
            this.left.appendSql(sqlBuilder, context);

            sqlBuilder.append(" IS");

            if (this.not) {
                sqlBuilder.append(" NOT");
            }
            sqlBuilder.append(this.operator.spaceRender());

            final ArmyExpression right = this.right;
            final boolean rightOuterParens = !(right instanceof ArmySimpleExpression);
            if (rightOuterParens) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            }
            right.appendSql(sqlBuilder, context);

            if (rightOuterParens) {
                sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
            }
        }


        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append(this.left)
                    .append(" IS");
            if (this.not) {
                builder.append(" NOT");
            }
            builder.append(this.operator.spaceRender());

            final ArmyExpression right = this.right;
            final boolean rightOuterParens = !(right instanceof ArmySimpleExpression);
            if (rightOuterParens) {
                builder.append(_Constant.SPACE_LEFT_PAREN);
            }
            builder.append(right);

            if (rightOuterParens) {
                builder.append(_Constant.SPACE_RIGHT_PAREN);
            }
            return builder.toString();
        }


    }//IsComparisonPredicates


    static final class InOperationPredicate extends OperationPredicate.OperationCompoundPredicate {

        final ArmySQLExpression left;

        final boolean not;

        final SQLColumnSet right;


        /**
         * @see #inPredicate(SQLExpression, boolean, SQLColumnSet)
         */
        private InOperationPredicate(SQLExpression left, boolean not, SQLColumnSet right) {
            this.left = (ArmySQLExpression) left;
            this.not = not;
            this.right = right;
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {

            this.left.appendSql(sqlBuilder, context);

            if (this.not) {
                sqlBuilder.append(_Constant.SPACE_NOT);
            }
            sqlBuilder.append(" IN");

            final SQLColumnSet right = this.right;
            if (right instanceof SubQuery) {
                context.appendSubQuery((SubQuery) right);
            } else {
                ((LengthTypedRowExpression) right).appendSql(sqlBuilder, context);
            }
        }


        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append(this.left);

            if (this.not) {
                builder.append(" NOT");
            }
            builder.append(" IN");
            return builder.append(this.right)
                    .toString();
        }


    }//InOperationPredicate


    ///
    /// @see <a href="https://www.postgresql.org/docs/current/arrays.html#ARRAYS-ACCESSING">ARRAYS-ACCESSING</a>
    ///
    private static final class ArraySliceExpression extends OperationExpression.OperationSimpleExpression {

        private final ArmyExpression expression;

        private final List<?> subscriptList;

        /**
         * @see #arraySliceExp(OperationExpression, List)
         */
        private ArraySliceExpression(ArmyExpression expression, List<?> subscriptList) {
            assertSliceSubscriptListSize(subscriptList);
            this.expression = expression;
            this.subscriptList = subscriptList;

        }


        @Override
        public void appendSql(StringBuilder sqlBuilder, _SqlContext context) {
            appendConcatenationExpression(this.expression, sqlBuilder, context);
            final List<?> list = this.subscriptList;
            final int size = list.size();
            for (int i = 0; i < size; i++) {
                sqlBuilder.append('[');
                appendRightExpression(list.get(i++), sqlBuilder, context, Boolean.FALSE);
                sqlBuilder.append(':');
                appendRightExpression(list.get(i), sqlBuilder, context, Boolean.FALSE);
                sqlBuilder.append(']');
            }

        }

        @Override
        public String toString() {
            final StringBuilder builder = _StringUtils.builder();
            toStringConcatenationExpression(this.expression, builder);
            final List<?> list = this.subscriptList;
            final int size = list.size();
            for (int i = 0; i < size; i++) {
                builder.append('[');
                toStringRightExpression(list.get(i++), builder, Boolean.FALSE);
                builder.append(':');
                toStringRightExpression(list.get(i), builder, Boolean.FALSE);
                builder.append(']');
            }
            return builder.toString();
        }

    } // ArraySliceExpression


    private static final class DotOperatorExpression extends OperationExpression.OperationSimpleExpression {

        private final ArmyExpression expression;

        private final List<?> subscriptList;

        /// @see #objectDotExp(OperationExpression, List)
        /// @see <a href="https://www.postgresql.org/docs/current/datatype-json.html#JSONB-SUBSCRIPTING">JSONB-SUBSCRIPTING</a>
        private DotOperatorExpression(ArmyExpression expression, List<?> subscriptList) {
            assertSubscriptListNotEmpty(subscriptList, "OBJECT");
            this.expression = expression;
            this.subscriptList = subscriptList;
        }

        @Override
        public void appendSql(StringBuilder sqlBuilder, _SqlContext context) {
            sqlBuilder.append(_Constant.SPACE);

            final int subscriptCount = this.subscriptList.size();
            for (int i = 0; i < subscriptCount; i++) {
                sqlBuilder.append(_Constant.LEFT_PAREN);  // https://www.postgresql.org/docs/current/rowtypes.html#ROWTYPES-ACCESSING
            }

            this.expression.appendSql(sqlBuilder, context);


            String sub;
            for (Object subscript : this.subscriptList) {

                sqlBuilder.append(_Constant.RIGHT_PAREN);

                sqlBuilder.append('.');

                if (subscript == null || subscript == SQLs.ABSENT) {
                    throw subscriptError(subscript, "OBJECT");
                }

                switch (subscript) {
                    case Expression _:
                        ((ArmyExpression) subscript).appendSql(sqlBuilder, context);
                        break;
                    case String _: {
                        sub = (String) subscript;
                        if (sub.equals("*")) {
                            sqlBuilder.append(sub);
                        } else {
                            context.identifier(sub, sqlBuilder);
                        }
                    }
                    break;
                    default:
                        throw subscriptError(subscript, "OBJECT");
                } // switch

            } // for loop

        }

        @Override
        public String toString() {
            final StringBuilder builder = _StringUtils.builder();
            builder.append(_Constant.SPACE);
            final int subscriptCount = this.subscriptList.size();
            for (int i = 0; i < subscriptCount; i++) {
                builder.append(_Constant.LEFT_PAREN);  // https://www.postgresql.org/docs/current/rowtypes.html#ROWTYPES-ACCESSING
            }


            for (Object subscript : this.subscriptList) {
                builder.append(_Constant.RIGHT_PAREN);

                builder.append('.');

                if (subscript == null || subscript == SQLs.ABSENT) {
                    throw subscriptError(null, "OBJECT");
                } else switch (subscript) {
                    case Expression _:
                        builder.append(subscript);
                        break;
                    case String _:
                        builder.append((String) subscript);
                        break;
                    default:
                        throw subscriptError(subscript, "OBJECT");
                } // switch

            } // for loop
            return builder.toString();
        }


    } // DotOperatorExpression


    private static final class BracketOperatorExpression extends OperationExpression.OperationSimpleExpression {

        private final ArmyExpression expression;

        private final List<?> subscriptList;

        /// @see #objectBracketExp(OperationExpression, List)
        /// @see <a href="https://www.postgresql.org/docs/current/datatype-json.html#JSONB-SUBSCRIPTING">JSONB-SUBSCRIPTING</a>
        private BracketOperatorExpression(ArmyExpression expression, List<?> subscriptList) {
            assertSubscriptListNotEmpty(subscriptList, "OBJECT");
            this.expression = expression;
            this.subscriptList = subscriptList;
        }

        @Override
        public void appendSql(StringBuilder sqlBuilder, _SqlContext context) {
            // NOTE ,here don't output Leading space,because here output bracket of object
            appendConcatenationExpression(this.expression, sqlBuilder, context);
            String sub;
            for (Object subscript : this.subscriptList) {

                sqlBuilder.append('[');

                if (subscript == null || subscript == SQLs.ABSENT) {
                    throw subscriptError(null, "OBJECT");
                } else switch (subscript) {
                    case Expression _:
                        ((ArmyExpression) subscript).appendSql(sqlBuilder, context);
                        break;
                    case String _: {
                        sub = (String) subscript;
                        if (sub.equals("*")) {
                            sqlBuilder.append(sub);
                        } else {
                            context.appendLiteral(StringType.INSTANCE, subscript, false);
                        }
                    }
                    break;
                    case Integer _:
                        sqlBuilder.append(subscript);
                        break;
                    default:
                        throw subscriptError(subscript, "OBJECT");
                } // switch

                sqlBuilder.append(']');

            } // for loop

        }

        @Override
        public String toString() {
            // NOTE ,here don't output Leading space,because here output bracket of object
            final StringBuilder builder = _StringUtils.builder();
            toStringConcatenationExpression(this.expression, builder);
            for (Object subscript : this.subscriptList) {

                builder.append('[');

                if (subscript == null || subscript == SQLs.ABSENT) {
                    throw subscriptError(null, "OBJECT");
                } else switch (subscript) {
                    case Expression _:
                        builder.append(subscript);
                        break;
                    case String _:
                    case Integer _:
                        builder.append(subscript);
                        break;
                    default:
                        throw subscriptError(subscript, "OBJECT");
                } // switch

                builder.append(']');

            } // for loop
            return builder.toString();
        }


    } // BracketOperatorExpression


    private static final class SimpleArrayExpression extends OperationExpression.OperationSimpleExpression {

        private final List<?> elementList;


        /**
         * @see #array(List)
         */
        private SimpleArrayExpression(List<?> elementList) {
            this.elementList = elementList;
        }

        /**
         * @see <a href="https://www.postgresql.org/docs/current/sql-expressions.html#SQL-SYNTAX-ARRAY-CONSTRUCTORS">Array Constructors</a>
         */
        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            final List<?> elementList = this.elementList;
            final int elementSize = elementList.size();

            sqlBuilder.append(_Constant.SPACE)
                    .append("ARRAY")
                    .append('[');

            Object element;
            MappingType elementType;
            for (int i = 0; i < elementSize; i++) {
                if (i > 0) {
                    sqlBuilder.append(_Constant.COMMA);
                }
                element = elementList.get(i);
                if (element == null) {
                    sqlBuilder.append(_Constant.NULL);
                } else if (element instanceof SQLExpression) {
                    ((ArmySQLExpression) element).appendSql(sqlBuilder, context);
                } else if ((elementType = _MappingFactory.getDefaultIfMatch(element.getClass())) == null) {
                    String m = String.format("Not found %s for %s", MappingType.class.getName(),
                            element.getClass().getName());
                    throw new CriteriaException(m);
                } else {
                    context.appendLiteral(elementType, element, true);
                }
            }

            sqlBuilder.append(']');

        }


        @Override
        public String toString() {
            final StringBuilder sqlBuilder;
            sqlBuilder = new StringBuilder()
                    .append(_Constant.SPACE)
                    .append("ARRAY")
                    .append('[');

            final List<?> elementList = this.elementList;
            final int elementSize = elementList.size();
            Object element;
            for (int i = 0; i < elementSize; i++) {
                if (i > 0) {
                    sqlBuilder.append(_Constant.SPACE_COMMA);
                }
                element = elementList.get(i);
                if (!(element instanceof SQLExpression)) {
                    sqlBuilder.append(_Constant.SPACE);
                }
                sqlBuilder.append(element);
            }
            return sqlBuilder.append(']')
                    .toString();
        }


    } // SimpleArrayExpression

    private static final class SubQueryArrayExpression extends OperationExpression.OperationSimpleExpression {

        private final SubQuery subQuery;

        /// @see #array(SubQuery)
        private SubQueryArrayExpression(SubQuery subQuery) {
            this.subQuery = subQuery;
        }

        @Override
        public void appendSql(StringBuilder sqlBuilder, _SqlContext context) {
            sqlBuilder.append(_Constant.SPACE)
                    .append("ARRAY")
                    .append('[');
            context.appendSubQuery(this.subQuery);
            sqlBuilder.append(']');
        }

        @Override
        public String toString() {
            return _StringUtils.builder()
                    .append(_Constant.SPACE)
                    .append("ARRAY")
                    .append('[')
                    .append(this.subQuery)
                    .append(']')
                    .toString();
        }

    } // SubQueryArrayExpression


    private static final class CollateExpression extends OperationExpression.OperationSimpleExpression
            implements SimpleResultExpression {

        private final ArmyExpression exp;

        private final String collation;

        private CollateExpression(Expression exp, String collation) {
            this.exp = (ArmyExpression) exp;
            this.collation = collation;
        }


        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            this.exp.appendSql(sqlBuilder, context);

            sqlBuilder.append(" COLLATE ");
            context.identifier(this.collation, sqlBuilder);
        }


        @Override
        public String toString() {
            return _StringUtils.builder()
                    .append(this.exp)
                    .append(" COLLATE ")
                    .append(this.collation)
                    .toString();

        }


    }//CollateExpression


    private static final class EmptyParensGroupByItem implements ArmyGroupByItem, GroupByItem.ExpressionGroup {

        private static final EmptyParensGroupByItem INSTANCE = new EmptyParensGroupByItem();

        private EmptyParensGroupByItem() {
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {
            sqlBuilder.append(" ()");
        }

        @Override
        public String toString() {
            return " ()";
        }


    }//EmptyParensGroupByItem

    enum GroupingModifier {
        ROLLUP(" ROLLUP"),
        CUBE(" CUBE"),
        GROUPING_SETS(" GROUPING SETS");

        private final String spaceWords;

        GroupingModifier(String spaceWords) {
            this.spaceWords = spaceWords;
        }


        @Override
        public final String toString() {
            return CriteriaUtils.enumToString(this);
        }

    }//GroupingModifier

    private static class ParensGroupByItem implements ArmyGroupByItem {

        private final GroupingModifier modifier;

        private final List<? extends ArmyGroupByItem> itemList;

        private ParensGroupByItem(@Nullable GroupingModifier modifier, List<? extends ArmyGroupByItem> itemList) {
            assert modifier == null || itemList.size() > 0;
            this.modifier = modifier;
            this.itemList = itemList;
        }

        @Override
        public final void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {

            if (this.modifier == null) {
                sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);
            } else {
                sqlBuilder.append(this.modifier.spaceWords)
                        .append(_Constant.LEFT_PAREN);
            }

            CriteriaUtils.appendSelfDescribedList(this.itemList, sqlBuilder, context);

            sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);
        }

        @Override
        public final String toString() {
            final StringBuilder sqlBuilder;
            sqlBuilder = new StringBuilder()
                    .append(_Constant.SPACE_LEFT_PAREN);

            CriteriaUtils.selfDescribedListToString(this.itemList, sqlBuilder);
            return sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN)
                    .toString();
        }


    }//ParensGroupByItem

    private static final class ParensGroupByItemGroup extends ParensGroupByItem implements GroupByItem.ExpressionGroup {

        private ParensGroupByItemGroup(@Nullable GroupingModifier modifier, List<? extends ArmyGroupByItem> itemList) {
            super(modifier, itemList);
        }


    }//ParensGroupByItemGroup


}
