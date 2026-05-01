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
import io.army.criteria.standard.SQLFunction;
import io.army.mapping.*;
import io.army.mapping.optional.IntervalType;
import io.army.meta.TypeMeta;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

/// 
/// This class is util class used to create standard sql element :
/// 
/// - statement parameter
/// - sql literal
/// - standard sql function
/// 
/// @see SQLs
@SuppressWarnings("unused")
abstract class Functions {


    /// package constructor,forbid application developer directly extend this util class.
    Functions() {
        throw new UnsupportedOperationException();
    }

    public interface _WithOrdinalityClause {

        _TabularFunction withOrdinality();

        _TabularFunction ifWithOrdinality(BooleanSupplier predicate);

    }


    /// @see <a href="https://www.postgresql.org/docs/current/queries-table-expressions.html#QUERIES-TABLEFUNCTIONS"> Table Functions
    /// </a>
    public interface _TabularFunction extends DerivedTable, SQLFunction {

    }

    /// @see <a href="https://www.postgresql.org/docs/current/queries-table-expressions.html#QUERIES-TABLEFUNCTIONS"> Table Functions
    /// </a>
    public interface _ColumnFunction extends _TabularFunction, SelectionSpec {

    }

    public interface _TabularWithOrdinalityFunction extends _TabularFunction, _WithOrdinalityClause {

    }


    public interface _ColumnWithOrdinalityFunction extends _ColumnFunction, _TabularWithOrdinalityFunction {

    }

    interface _NullTreatmentClause<NR> {

        NR respectNulls();

        NR ignoreNulls();

        NR ifRespectNulls(BooleanSupplier predicate);

        NR ifIgnoreNulls(BooleanSupplier predicate);

    }

    public interface _FromFirstLastClause<FR> {
        FR fromFirst();

        FR fromLast();

        FR ifFromFirst(BooleanSupplier predicate);

        FR ifFromLast(BooleanSupplier predicate);

    }

    /// package interface,this interface only is implemented by class or enum,couldn't is extended by interface.
    interface ArmyKeyWord extends SQLToken {

    }


    /// Create searched case function.
    public static SQLFunction._CaseFuncWhenClause cases() {
        return LiteralFunctions.caseFunc(null);
    }

    /// Create simple case function.
    /// @param expression {@link Expression} instance or literal
    public static SQLFunction._CaseFuncWhenClause cases(Expression expression) {
        ContextStack.assertNonNull(expression);
        return LiteralFunctions.caseFunc(expression);
    }


    /// The {@link MappingType} of function return type: {@link  DoubleType}
    /// @param expr non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_acos">ACOS(X)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-TRIG-TABLE">acos ( double precision ) → double precision</a>
    public static SimpleExpression acos(final Object expr) {
        return LiteralFunctions.oneArgFunc("ACOS", expr);
    }

    /// The {@link MappingType} of function return type: {@link  DoubleType}
    /// @param expr non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_asin">ASIN(X)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-TRIG-TABLE">asin ( double precision ) → double precision</a>
    public static SimpleExpression asin(final Object expr) {
        return LiteralFunctions.oneArgFunc("ASIN", expr);
    }

    /// The {@link MappingType} of function return type: {@link  DoubleType}
    /// @param expr non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_atan">ATAN(X)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-TRIG-TABLE">atan ( double precision ) → double precision</a>
    public static SimpleExpression atan(final Object expr) {
        return LiteralFunctions.oneArgFunc("ATAN", expr);
    }

    /// The {@link MappingType} of function return type: {@link  DoubleType}
    /// @param x non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @param y non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_atan2">ATAN(X,y)</a>
    public static SimpleExpression atan(final Object x, final Object y) {
        return LiteralFunctions.twoArgFunc("ATAN", x, y);
    }


    /// The {@link MappingType} of function return type:
    /// 
    /// - if {@link MappingType} of exp is number type,then {@link MappingType} of exp
    /// - else {@link BigDecimalType}
    /// 
    /// @param exp non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_ceil">CEIL(X)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">ceil ( numeric ) → numeric,ceil ( double precision ) → double precision</a>
    public static SimpleExpression ceil(final Object exp) {
        final Expression expression;
        expression = SQLs._nonNullLiteral(exp);
        return LiteralFunctions.oneArgFunc("CEIL", expression);
    }

    /// 
    /// The {@link MappingType} of function return type: the {@link  MappingType} or expr
    /// @param expr     non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @param fromBase non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @param toBase   non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_conv">CONV(X)</a>
    public static SimpleExpression conv(final Object expr, final Object fromBase, final Object toBase) {
        final Expression expression;
        expression = SQLs._nonNullLiteral(expr);
        return LiteralFunctions.threeArgFunc("CONV", expression, fromBase, toBase);
    }

    /// The {@link MappingType} of function return type: {@link  DoubleType}
    /// @param expr non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_cos">COS(X)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-TRIG-TABLE">cos ( double precision ) → double precision</a>
    public static SimpleExpression cos(final Object expr) {
        return LiteralFunctions.oneArgFunc("COS", expr);
    }

    /// The {@link MappingType} of function return type: {@link  DoubleType}
    /// @param expr non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_cot">COT(X)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-TRIG-TABLE">cot ( double precision ) → double precision</a>
    public static SimpleExpression cot(final Object expr) {
        return LiteralFunctions.oneArgFunc("COT", expr);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link  IntegerType}
    /// @param expr non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_crc32">CRC32(expr)</a>
    public static SimpleExpression crc32(final Object expr) {
        return LiteralFunctions.oneArgFunc("CRC32", expr);
    }

    /// The {@link MappingType} of function return type: {@link  DoubleType}
    /// @param expr non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_degrees">DEGREES(x)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-OP-TABLE">degrees ( double precision )</a>
    public static SimpleExpression degrees(final Object expr) {
        return LiteralFunctions.oneArgFunc("DEGREES", expr);
    }

    /// The {@link MappingType} of function return type:
    /// 
    /// - If the {@link MappingType} of exp is float number type,then {@link DoubleType}
    /// - Else {@link BigDecimalType}
    /// 
    /// @param expr non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_exp">EXP(x)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">exp ( numeric )</a>
    public static SimpleExpression exp(final Object expr) {
        final Expression expression;
        expression = SQLs._nonNullLiteral(expr);
        return LiteralFunctions.oneArgFunc("EXP", expression);
    }

    /// The {@link MappingType} of function return type: {@link  LongType}
    /// @param expr non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_floor">FLOOR(x)</a>
    public static SimpleExpression floor(final Object expr) {
        return LiteralFunctions.oneArgFunc("FLOOR", expr);
    }

    /// The {@link MappingType} of function return type: {@link  StringType}
    /// @param x non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @param d non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_format">FORMAT(x,d)</a>
    public static SimpleExpression format(final Object x, final Object d) {
        return LiteralFunctions.twoArgFunc("FORMAT", x, d);
    }


    /// The {@link MappingType} of function return type:
    /// 
    /// - If the {@link MappingType} of exp is float number type,then {@link DoubleType}
    /// - Else {@link BigDecimalType}
    /// 
    /// @param x non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_ln">LN(x)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">ln ( numeric ) → numeric,ln ( double precision ) → double precision</a>
    public static SimpleExpression ln(final Object x) {
        final Expression expression;
        expression = SQLs._nonNullLiteral(x);
        return LiteralFunctions.oneArgFunc("LN", expression);
    }

    /// The {@link MappingType} of function return type:
    /// 
    /// - If the {@link MappingType} of exp is float number type,then {@link DoubleType}
    /// - Else {@link BigDecimalType}
    /// 
    /// @param x non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_log">LOG(x)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">log ( numeric ) → numeric,log ( double precision ) → double precision</a>
    public static SimpleExpression log(final Object x) {
        final Expression expression;
        expression = SQLs._nonNullLiteral(x);
        return LiteralFunctions.oneArgFunc("LOG", expression);
    }

    /// The {@link MappingType} of function return type: {@link  BigDecimalType}
    /// @param b non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @param x non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_log">LOG(B,X)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">log ( b numeric, x numeric ) → numeric</a>
    public static SimpleExpression log(final Object b, final Object x) {
        return LiteralFunctions.twoArgFunc("LOG", b, x);
    }

    /// The {@link MappingType} of function return type:
    /// 
    /// - If the {@link MappingType} of exp is float number type,then {@link DoubleType}
    /// - Else {@link BigDecimalType}
    /// 
    /// @param x non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_log10">LOG10(x)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">log10 ( numeric ) → numeric,log10 ( double precision ) → double precision</a>
    public static SimpleExpression log10(final Object x) {
        final Expression expression;
        expression = SQLs._nonNullLiteral(x);
        return LiteralFunctions.oneArgFunc("LOG10", expression);
    }


    /// 
    /// The {@link MappingType} of function return type: {@link DoubleType} .
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_pi">PI()</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">pi ( ) → double precision</a>
    public static SimpleExpression pi() {
        return LiteralFunctions.zeroArgFunc("PI");
    }

    /// The {@link MappingType} of function return type: {@link MappingType} of x
    /// @param x non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @param y non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_pow">POW(x,y)</a>
    public static SimpleExpression pow(final Object x, final Object y) {
        final Expression expression;
        expression = SQLs._nonNullLiteral(x);
        return LiteralFunctions.twoArgFunc("POW", expression, y);
    }

    /// The {@link MappingType} of function return type: {@link DoubleType} .
    /// @param x non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_radians">RADIANS(x)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">radians ( double precision ) → double precision</a>
    public static SimpleExpression radians(final Object x) {
        return LiteralFunctions.oneArgFunc("RADIANS", x);
    }

    /// 
    /// The {@link MappingType} of function return type: {@link DoubleType} .
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_rand">RAND([N])</a>
    public static SimpleExpression rand() {
        return LiteralFunctions.zeroArgFunc("RAND");
    }

    /// The {@link MappingType} of function return type: {@link DoubleType} .
    /// @param n non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_rand">RAND([N])</a>
    public static SimpleExpression rand(final Object n) {
        return LiteralFunctions.oneArgFunc("RAND", n);
    }

    /// The {@link MappingType} of function return type: {@link BigDecimalType} .
    /// @param x non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_round">ROUND(x)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">round ( numeric ) → numeric</a>
    public static SimpleExpression round(final Object x) {
        return LiteralFunctions.oneArgFunc("ROUND", x);
    }

    /// The {@link MappingType} of function return type: {@link BigDecimalType} .
    /// @param x non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @param d non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_round">ROUND(x,d)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">round ( v numeric, s integer ) → numeric</a>
    public static SimpleExpression round(final Object x, final Object d) {
        return LiteralFunctions.twoArgFunc("ROUND", x, d);
    }

    /// The {@link MappingType} of function return type: {@link IntegerType} .
    /// @param x non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_sign">SIGN(x)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">sign ( numeric ) → numeric</a>
    public static SimpleExpression sign(final Object x) {
        return LiteralFunctions.oneArgFunc("SIGN", x);
    }

    /// The {@link MappingType} of function return type: {@link DoubleType} .
    /// @param x non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_sin">SIN(x)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">sin ( numeric ) → numeric</a>
    public static SimpleExpression sin(final Object x) {
        return LiteralFunctions.oneArgFunc("SIN", x);
    }


    /// The {@link MappingType} of function return type: {@link DoubleType} .
    /// @param x non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_tan">TAN(x)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">tan ( numeric ) → numeric</a>
    public static SimpleExpression tan(final Object x) {
        return LiteralFunctions.oneArgFunc("TAN", x);
    }

    /// The {@link MappingType} of function return type: {@link DoubleType} .
    /// @param x non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @param d non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_truncate">TRUNCATE(x,d)</a>
    public static SimpleExpression truncate(final Object x, final Object d) {
        return LiteralFunctions.twoArgFunc("TRUNCATE", x, d);
    }

    /*-------------------below standard sql92 functions-------------------*/

    /// standard sql92 functions
    /// The {@link MappingType} of function return type: the {@link MappingType} of n.
    /// @param n non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @param m non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_mod">MOD(N,M), N % M, N MOD M</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">mod ( y numeric_type, x numeric_type ) → numeric_type</a>
    public static SimpleExpression mod(final Object n, final Object m) {
        final Expression expression;
        expression = SQLs._nonNullLiteral(n);
        return LiteralFunctions.twoArgFunc("MOD", expression, m);
    }

    /// standard sql92 functions
    /// The {@link MappingType} of function return type: {@link  MappingType} of expr
    /// @param expr non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_abs">ABS(X)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">ABS(numeric_type)</a>
    public static SimpleExpression abs(final Object expr) {
        final Expression expression;
        expression = SQLs._nonNullLiteral(expr);
        return LiteralFunctions.oneArgFunc("ABS", expression);
    }

    /// standard sql92 functions
    /// The {@link MappingType} of function return type:
    /// 
    /// - If the {@link MappingType} of exp is float number type,then {@link DoubleType}
    /// - Else {@link BigDecimalType}
    /// 
    /// @param x non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/mathematical-functions.html#function_sqrt">SQRT(x)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-FUNC-TABLE">sqrt ( numeric ) → numeric,sqrt ( double precision ) → double precision</a>
    public static SimpleExpression sqrt(final Object x) {
        final Expression expression;
        expression = SQLs._nonNullLiteral(x);
        return LiteralFunctions.oneArgFunc("SQRT", expression);
    }

    /// standard sql92 functions
    /// The {@link MappingType} of function return type:the {@link  MappingType} of expr1
    /// @param expr1 non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @param expr2 non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @throws CriteriaException throw when argument error
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/flow-control-functions.html#function_nullif">NULLIF(expr1,expr2)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-conditional.html#FUNCTIONS-NULLIF">NULLIF(expr1,expr2)</a>
    public static SimpleExpression nullIf(final Object expr1, final Object expr2) {
        FuncExpUtils.assertLiteralExp(expr1);
        FuncExpUtils.assertLiteralExp(expr2);

        final Expression expression1;
        expression1 = SQLs._nonNullLiteral(expr1);

        return LiteralFunctions.twoArgFunc("NULLIF", expression1, expr2);
    }

    /// standard sql92 functions
    /// The {@link MappingType} of function return type: {@link IntegerType} .
    /// @param exp non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_length">LENGTH(str)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-string.html#FUNCTIONS-STRING-OTHER">length ( text ) → integer</a>
    public static SimpleExpression length(Expression exp) {
        return LiteralFunctions.oneArgFunc("LENGTH", exp);
    }

    /// standard sql92 functions
    /// The {@link MappingType} of function return type: {@link MappingType} of str.
    /// @param str non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link String} literal
    /// 
    /// @param pos non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link Integer} literal
    /// 
    /// @param len non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link Integer} literal
    /// 
    /// @throws CriteriaException throw when argument error
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_substring">MySQL SUBSTRING(str,pos,len)</a>
    /// @see <a href="https://www.h2database.com/html/functions.html#substring">H2 SUBSTRING(str,pos,len)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-string.html#FUNCTIONS-STRING-SQL">H2 SUBSTRING(str,pos,len)</a>
    public static SimpleExpression substring(final Object str, Object pos, Object len) {
        final Expression strExp;
        if (str instanceof Expression) {
            strExp = (Expression) str;
        } else if (str instanceof String) {
            strExp = SQLs.literal(StringType.INSTANCE, str);
        } else {
            throw ContextStack.clearStackAndCriteriaError("str must be Expression or String");
        }
        FuncExpUtils.assertIntExp(pos);
        FuncExpUtils.assertIntExp(len);
        return LiteralFunctions.threeArgFunc("SUBSTRING", strExp, pos, len);
    }


    /// standard sql92 functions
    /// The {@link MappingType} of function return type:{@link StringType}
    /// @param str non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link String} literal
    /// 
    /// @throws CriteriaException throw when argument error
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_trim">TRIM(str)</a>
    public static SimpleExpression trim(final Object str) {
        FuncExpUtils.assertTextExp(str);
        return LiteralFunctions.oneArgFunc("TRIM", str);
    }

    /// The {@link MappingType} of function return type:{@link StringType}
    /// @param remstr non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link String} literal
    /// 
    /// @param from   see {@link SQLs#FROM}
    /// @param str    non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link String} literal
    /// 
    /// @throws CriteriaException throw when argument error
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_trim">TRIM(remstr FROM str)</a>
    public static SimpleExpression trim(Object remstr, SQLs.WordFrom from, Object str) {
        FuncExpUtils.assertTextExp(remstr);
        FuncExpUtils.assertTextExp(str);
        FuncExpUtils.assertWord(from, SQLs.FROM);
        return LiteralFunctions.compositeFunc("TRIM", Arrays.asList(remstr, from, str));
    }

    /// standard sql92 functions
    /// The {@link MappingType} of function return type:{@link StringType}
    /// @param position non-null,should be below:
    /// 
    /// - {@link SQLs#BOTH}
    /// - {@link SQLs#LEADING}
    /// - {@link SQLs#TRAILING}
    /// 
    /// @param from     see {@link SQLs#FROM}
    /// @param str      non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link String} literal
    /// 
    /// @throws CriteriaException throw when argument error
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_trim">TRIM([BOTH | LEADING | TRAILING] remstr FROM str), TRIM([remstr FROM] str),TRIM(remstr FROM str)</a>
    public static SimpleExpression trim(SQLs.TrimSpec position, SQLs.WordFrom from, Object str) {
        FuncExpUtils.assertTrimSpec(position);
        FuncExpUtils.assertWord(from, SQLs.FROM);
        FuncExpUtils.assertTextExp(str);

        return LiteralFunctions.compositeFunc("TRIM", Arrays.asList(position, from, str));
    }

    /// standard sql92 functions
    /// The {@link MappingType} of function return type:{@link StringType}
    /// @param position non-null,should be below:
    /// 
    /// - {@link SQLs#BOTH}
    /// - {@link SQLs#LEADING}
    /// - {@link SQLs#TRAILING}
    /// 
    /// @param remstr   non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link String} literal
    /// 
    /// @param from     see {@link SQLs#FROM}
    /// @param str      non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link String} literal
    /// 
    /// @throws CriteriaException throw when argument error
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_trim">TRIM([BOTH | LEADING | TRAILING] remstr FROM str), TRIM([remstr FROM] str),TRIM(remstr FROM str)</a>
    public static SimpleExpression trim(SQLs.TrimSpec position, Object remstr, SQLs.WordFrom from, Object str) {
        FuncExpUtils.assertTrimSpec(position);
        FuncExpUtils.assertTextExp(remstr);
        FuncExpUtils.assertWord(from, SQLs.FROM);
        FuncExpUtils.assertTextExp(str);

        return LiteralFunctions.compositeFunc("TRIM", Arrays.asList(position, remstr, from, str));
    }


    /// standard sql92 functions
    /// The {@link MappingType} of function return type:{@link IntegerType}
    /// @param substr non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link String} literal
    /// 
    /// @param str    non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link String} literal
    /// 
    /// @return int {@link Expression} ,based one .
    /// @throws CriteriaException throw when argument error
    /// @see #locate(Object, Object, Object)
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_locate">LOCATE(substr,str)</a>
    public static SimpleExpression locate(final Object substr, final Object str) {
        FuncExpUtils.assertTextExp(substr);
        FuncExpUtils.assertTextExp(str);
        return LiteralFunctions.twoArgFunc("LOCATE", substr, str);
    }

    /// standard sql92 functions
    /// The {@link MappingType} of function return type:{@link IntegerType}
    /// @param substr non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link String} literal
    /// 
    /// @param str    non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link String} literal
    /// 
    /// @param pos    non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link Integer} literal
    /// 
    /// @throws CriteriaException throw when argument error
    /// @see #locate(Object, Object, Object)
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_locate">LOCATE(substr,str,pos)</a>
    public static SimpleExpression locate(Object substr, Object str, Object pos) {
        FuncExpUtils.assertTextExp(substr);
        FuncExpUtils.assertTextExp(str);
        FuncExpUtils.assertIntExp(pos);
        return LiteralFunctions.threeArgFunc("LOCATE", substr, str, pos);
    }

    /// standard sql92 functions
    /// The {@link MappingType} of function return type:{@link IntegerType}
    /// @param str non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - literal
    /// 
    /// @throws CriteriaException throw when argument
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_bit-length">BIT_LENGTH(str)</a>
    public static SimpleExpression binLength(final Object str) {
        FuncExpUtils.assertLiteralExp(str);
        return LiteralFunctions.oneArgFunc("BIT_LENGTH", str);
    }

    /// standard sql92 functions
    /// The {@link MappingType} of function return type:{@link StringType}
    /// @param str non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link String} literal
    /// 
    /// @throws CriteriaException throw when argument error
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_lower">LOWER(str)</a>
    public static SimpleExpression lower(Object str) {
        FuncExpUtils.assertTextExp(str);
        return LiteralFunctions.oneArgFunc("LOWER", str);
    }

    /// standard sql92 functions
    /// The {@link MappingType} of function return type:{@link StringType}
    /// @param str non-null, one of following :
    /// 
    /// - {@link Expression} instance
    /// - {@link String} literal
    /// 
    /// @throws CriteriaException throw when argument error
    /// @see #lower(Object)
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/string-functions.html#function_upper">UPPER(str)</a>
    public static SimpleExpression upper(Object str) {
        FuncExpUtils.assertTextExp(str);
        return LiteralFunctions.oneArgFunc("UPPER", str);
    }

    /*-------------------below Aggregate Function-------------------*/

    /// The {@link MappingType} of function return type: {@link  LongType}
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_count">COUNT(expr)</a>
    public static SimpleExpression countAsterisk() {
        return Functions.count(SQLs.ASTERISK);
    }

    /// The {@link MappingType} of function return type: {@link  LongType}
    /// @param expr non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_count">COUNT(expr)</a>
    public static SimpleExpression count(Expression expr) {
        return LiteralFunctions.oneArgFunc("COUNT", expr);
    }

    /// The {@link MappingType} of function return type: {@link  LongType}
    /// @param distinct see {@link SQLs#DISTINCT}
    /// @param expr     non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_count">COUNT(expr) [over_clause]</a>
    public static SimpleExpression count(SQLs.ArgDistinct distinct, Expression expr) {
        FuncExpUtils.assertDistinct(distinct, SQLs.DISTINCT);
        return LiteralFunctions.compositeFunc("COUNT", Arrays.asList(distinct, expr));
    }

    /// The {@link MappingType} of function return type: {@link  MappingType} of exp
    /// @param exp non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_min">MIN(expr)</a>
    public static SimpleExpression min(Expression exp) {
        return LiteralFunctions.oneArgFunc("MIN", exp);
    }

    /// The {@link MappingType} of function return type: {@link  MappingType} of exp
    /// @param exp non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_max">MAX(expr)</a>
    public static SimpleExpression max(Expression exp) {
        return LiteralFunctions.oneArgFunc("MAX", exp);
    }

    /// The {@link MappingType} of function return type:
    /// 
    /// - If exp is following types :
    /// 
    /// - tiny int
    /// - small int
    /// - medium int
    /// 
    /// ,then {@link IntegerType}
    /// 
    /// - Else if exp is int,then {@link LongType}
    /// - Else if exp is bigint,then {@link BigIntegerType}
    /// - Else if exp is decimal,then {@link BigDecimalType}
    /// - Else if exp is float type ,then {@link DoubleType}
    /// - Else he {@link MappingType} of exp
    /// 
    /// @param exp non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_sum">MySQL SUM([DISTINCT] expr)</a>
    /// @see <a href="https://www.h2database.com/html/functions-aggregate.html#sum">H2 SUM([DISTINCT] expr)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-aggregate.html#FUNCTIONS-AGGREGATE-TABLE">Postgre SUM([DISTINCT] expr)</a>
    public static SimpleExpression sum(Expression exp) {
        return LiteralFunctions.oneArgFunc("SUM", exp);
    }

    /// The {@link MappingType} of function return type:
    /// 
    /// - If exp is following types :
    /// 
    /// - tiny int
    /// - small int
    /// - medium int
    /// 
    /// ,then {@link IntegerType}
    /// 
    /// - Else if exp is int,then {@link LongType}
    /// - Else if exp is bigint,then {@link BigIntegerType}
    /// - Else if exp is decimal,then {@link BigDecimalType}
    /// - Else if exp is float type ,then {@link DoubleType}
    /// - Else he {@link MappingType} of exp
    /// 
    /// @param distinct see {@link SQLs#DISTINCT}
    /// @param exp      non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_sum">MySQL SUM([DISTINCT] expr)</a>
    /// @see <a href="https://www.h2database.com/html/functions-aggregate.html#sum">H2 SUM([DISTINCT] expr)</a>
    public static SimpleExpression sum(SQLs.ArgDistinct distinct, Expression exp) {
        FuncExpUtils.assertDistinct(distinct, SQLs.DISTINCT);
        return LiteralFunctions.compositeFunc("SUM", Arrays.asList(distinct, exp));
    }

    /// The {@link MappingType} of function return type:
    /// 
    /// - sql float type : {@link DoubleType}
    /// - sql integer/decimal type : {@link BigDecimalType}
    /// - sql interval : {@link IntervalType}
    /// - else : {@link TextType}
    /// 
    /// @param expr non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_avg">AVG([DISTINCT] expr)</a>
    public static SimpleExpression avg(Expression expr) {
        return LiteralFunctions.oneArgFunc("AVG", expr);
    }


    /// The {@link MappingType} of function return type:
    /// 
    /// - sql float type : {@link DoubleType}
    /// - sql integer/decimal type : {@link BigDecimalType}
    /// - sql interval : {@link IntervalType}
    /// - else : {@link TextType}
    /// 
    /// @param distinct see {@link SQLs#DISTINCT}
    /// @param expr     non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_avg">AVG([DISTINCT] expr)</a>
    public static SimpleExpression avg(SQLs.ArgDistinct distinct, Expression expr) {
        FuncExpUtils.assertDistinct(distinct, SQLs.DISTINCT);
        return LiteralFunctions.compositeFunc("AVG", Arrays.asList(distinct, expr));
    }

    /// The {@link MappingType} of function return type: {@link JsonType#TEXT}
    /// @param exp non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_json-arrayagg">MySQL JSON_ARRAYAGG(col_or_expr)</a>
    /// @see <a href="https://www.h2database.com/html/functions-aggregate.html#json_arrayagg">H2 JSON_ARRAYAGG(col_or_expr)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-aggregate.html#FUNCTIONS-AGGREGATE-TABLE">Postgre JSON_ARRAYAGG(col_or_expr)</a>
    public static SimpleExpression jsonArrayAgg(Expression exp) {
        return LiteralFunctions.oneArgFunc("JSON_ARRAYAGG", exp);
    }

    /// The {@link MappingType} of function return type: {@link JsonType#TEXT}
    /// @param key   non-null
    /// @param value non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_json-objectagg">MySQL JSON_OBJECTAGG(col_or_expr)</a>
    /// @see <a href="https://www.h2database.com/html/functions-aggregate.html#json_objectagg">H2 JSON_OBJECTAGG(col_or_expr)</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-aggregate.html#FUNCTIONS-AGGREGATE-TABLE">Postgre JSON_OBJECTAGG(col_or_expr)</a>
    public static SimpleExpression jsonObjectAgg(Expression key, Expression value) {
        return LiteralFunctions.twoArgFunc("JSON_OBJECTAGG", key, value);
    }

    /// The {@link MappingType} of function return type: {@link DoubleType}
    /// @param exp non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_std">STD(expr)</a>
    public static SimpleExpression std(Expression exp) {
        return LiteralFunctions.oneArgFunc("STD", exp);
    }

    /// The {@link MappingType} of function return type: {@link DoubleType}
    /// @param exp non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_stddev">STDDEV(expr)</a>
    public static SimpleExpression stdDev(Expression exp) {
        return LiteralFunctions.oneArgFunc("STDDEV", exp);
    }

    /// The {@link MappingType} of function return type: {@link DoubleType}
    /// @param exp non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_stddev-pop">STDDEV_POP(expr)</a>
    public static SimpleExpression stdDevPop(Expression exp) {
        return LiteralFunctions.oneArgFunc("STDDEV_POP", exp);
    }

    /// The {@link MappingType} of function return type: {@link DoubleType}
    /// @param exp non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_stddev-samp">STDDEV_SAMP(expr)</a>
    public static SimpleExpression stdDevSamp(Expression exp) {
        return LiteralFunctions.oneArgFunc("STDDEV_SAMP", exp);
    }

    /// The {@link MappingType} of function return type: {@link DoubleType}
    /// @param exp non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_var-pop">VAR_POP(expr)</a>
    public static SimpleExpression varPop(Expression exp) {
        return LiteralFunctions.oneArgFunc("VAR_POP", exp);
    }


    /// The {@link MappingType} of function return type: {@link DoubleType}
    /// @param exp non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_var-samp">VAR_SAMP(expr)</a>
    public static SimpleExpression varSamp(Expression exp) {
        return LiteralFunctions.oneArgFunc("VAR_SAMP", exp);
    }

    /// The {@link MappingType} of function return type: {@link DoubleType}
    /// @param exp non-null
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_variance">VARIANCE(expr)</a>
    public static SimpleExpression variance(Expression exp) {
        return LiteralFunctions.oneArgFunc("VARIANCE", exp);
    }


    /*-------------------below custom function -------------------*/

    /// User defined no argument function
    /// The {@link MappingType} of function return type is returnType
    /// @param name       function name
    public static SimpleExpression myFunc(String name, TypeMeta returnType) {
        return LiteralFunctions.myZeroArgFunc(name);
    }

    /// User defined no argument boolean function
    /// The {@link MappingType} of function return type: {@link  BooleanType}
    /// @param name function name
    public static SimplePredicate myFunc(String name) {
        return LiteralFunctions.myZeroArgPredicate(name);
    }

    /// User defined one argument function
    /// The {@link MappingType} of function return type is returnType
    /// @param name       function name
    /// @param expr       argument
    /// @param returnType function return type.
    public static SimpleExpression myFunc(String name, Expression expr, TypeMeta returnType) {
        return LiteralFunctions.myOneArgFunc(name, expr);
    }

    /// User defined one argument boolean function
    /// The {@link MappingType} of function return type: {@link  BooleanType}
    /// @param name function name
    /// @param expr argument
    public static SimplePredicate myFunc(String name, Expression expr) {
        return LiteralFunctions.myOneArgPredicate(name, expr);
    }

    /// User defined two argument function
    /// The {@link MappingType} of function return type is returnType
    /// @param name       function name
    /// @param expr1      argument
    /// @param expr2      argument
    /// @param returnType function return type.
    public static SimpleExpression myFunc(String name, Expression expr1, Expression expr2, TypeMeta returnType) {
        return LiteralFunctions.myTwoArgFunc(name, expr1, expr2);
    }

    /// User defined two argument boolean function
    /// The {@link MappingType} of function return type: {@link  BooleanType}
    /// @param name  function name
    /// @param expr1 argument
    /// @param expr2 argument
    public static SimplePredicate myFunc(String name, Expression expr1, Expression expr2) {
        return LiteralFunctions.myTwoArgPredicate(name, expr1, expr2);
    }

    /// User defined three argument function
    /// The {@link MappingType} of function return type is returnType
    /// @param name       function name
    /// @param expr1      argument
    /// @param expr2      argument
    /// @param expr3      argument
    /// @param returnType function return type.
    public static SimpleExpression myFunc(String name, Expression expr1, Expression expr2, Expression expr3, TypeMeta returnType) {
        return LiteralFunctions.myThreeArgFunc(name, expr1, expr2, expr3);
    }

    /// User defined three argument boolean function
    /// The {@link MappingType} of function return type: {@link  BooleanType}
    /// @param name  function name
    /// @param expr1 argument
    /// @param expr2 argument
    /// @param expr3 argument
    public static SimplePredicate myFunc(String name, Expression expr1, Expression expr2, Expression expr3) {
        return LiteralFunctions.myThreeArgPredicate(name, expr1, expr2, expr3);
    }

    /// User defined multi-argument function
    /// The {@link MappingType} of function return type is returnType
    /// @param name       function name
    /// @param expList    argument
    /// @param returnType function return type.
    public static SimpleExpression myFunc(String name, List<Expression> expList, TypeMeta returnType) {
        return LiteralFunctions.myMultiArgFunc(name, expList);
    }

    /// User defined multi-argument boolean function
    /// The {@link MappingType} of function return type: {@link  BooleanType}
    /// @param name function name
    public static SimplePredicate myFunc(String name, List<Expression> expList) {
        return LiteralFunctions.myMultiArgPredicate(name, expList);
    }



    /*################################## blow static inner class  ##################################*/


    /*-------------------below package method -------------------*/

    static CriteriaException _customFuncNameError(String name) {
        String m = String.format("custom function name[%s] error.", name);
        return ContextStack.criteriaError(ContextStack.peek(), m);
    }






    /*-------------------below private method-------------------*/




}
