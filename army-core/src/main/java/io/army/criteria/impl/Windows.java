package io.army.criteria.impl;

import io.army.criteria.Expression;
import io.army.criteria.SimpleExpression;
import io.army.criteria.Support;
import io.army.criteria.dialect.Window;
import io.army.criteria.standard.SQLFunction;
import io.army.mapping.*;
import io.army.mapping.optional.IntervalType;

import java.util.Arrays;
import java.util.List;

import static io.army.dialect.Database.H2;
import static io.army.dialect.Database.MySQL;


/// This class provide standard window function.
/// **NOTE**: You shouldn't static import these window function method , because you shouldn't avoid the conflict with dialect window function methods.
/// @since 0.6.4
@SuppressWarnings("unused")
public abstract class Windows {

    private Windows() {
        throw new UnsupportedOperationException();
    }

    public interface _OverSpec extends Window._OverWindowClause<Window._StandardPartitionBySpec> {


    }

    public interface _WindowAggSpec extends _OverSpec, SQLFunction.AggregateFunction, SimpleExpression {

    }

/// The {@link MappingType} of function return type:  {@link  DoubleType}.
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_cume-dist">CUME_DIST() over_clause</a>
/// @see <a href="https://www.postgresql.org/docs/current/functions-window.html#FUNCTIONS-WINDOW-TABLE">cume_dist () → double precision
/// Returns the cumulative distribution, that is (number of partition rows preceding or peers with current row) / (total partition rows). The value thus ranges from 1/N to 1.
/// </a>
    public static _OverSpec cumeDist() {
        return WindowFunctions.zeroArgWindowFunc("CUME_DIST");
    }

/// The {@link MappingType} of function return type:  {@link  LongType}.
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_row-number">ROW_NUMBER() over_clause</a>
/// @see <a href="https://www.postgresql.org/docs/current/functions-window.html#FUNCTIONS-WINDOW-TABLE">row_number () → bigint
/// Returns the number of the current row within its partition, counting from 1.
/// </a>
    public static _OverSpec rowNumber() {
        return WindowFunctions.zeroArgWindowFunc("ROW_NUMBER");
    }

/// The {@link MappingType} of function return type:  {@link  LongType}.
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_dense-rank">DENSE_RANK() over_clause</a>
/// @see <a href="https://www.postgresql.org/docs/current/functions-window.html#FUNCTIONS-WINDOW-TABLE">dense_rank () → bigint
/// Returns the rank of the current row, without gaps; this function effectively counts peer groups.
/// </a>
    public static _OverSpec denseRank() {
        return WindowFunctions.zeroArgWindowFunc("DENSE_RANK");
    }

/// The {@link MappingType} of function return type:  {@link  MappingType} or exp.
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_first-value">FIRST_VALUE() over_clause</a>
/// @see <a href="https://www.postgresql.org/docs/current/functions-window.html#FUNCTIONS-WINDOW-TABLE">first_value () → anyelement</a>
    public static _OverSpec firstValue(Expression exp) {
        return WindowFunctions.oneArgWindowFunc("FIRST_VALUE", exp);
    }


/// 
/// The {@link MappingType} of function return type: the {@link MappingType} of expr.
/// @param expr non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_last-value">LAST_VALUE(expr) [null_treatment] over_clause</a>
    public static _OverSpec lastValue(Expression expr) {
        return WindowFunctions.zeroArgWindowFunc("LAST_VALUE");
    }

/// 
/// The {@link MappingType} of function return type: the {@link MappingType} of expr.
/// @param expr non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_lag">LAG(expr [, N[, default]]) [null_treatment] over_clause</a>
    public static _OverSpec lag(Expression expr) {
        return WindowFunctions.oneArgWindowFunc("LAG", expr);
    }

/// 
/// The {@link MappingType} of function return type: the {@link MappingType} of expr.
/// @param expr non-null parameter or {@link  Expression}
/// @param n    nullable,probably is below:
/// 
/// - null
/// - {@link Long} type
/// - {@link Integer} type
/// - {@link SQLs#parameter(Object)},argument type is {@link Long} or {@link Integer}
/// - {@link SQLs#literalValue(Object) },argument type is {@link Long} or {@link Integer}
/// 
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_lag">LAG(expr [, N[, default]]) [null_treatment] over_clause</a>
    public static _OverSpec lag(Expression expr, Expression n) {
        return WindowFunctions.twoArgWindowFunc("LAG", expr, n);
    }


/// 
/// The {@link MappingType} of function return type: the {@link MappingType} of expr.
/// @param expr         non-null parameter or {@link  Expression},but couldn't be {@link SQLs#NULL}
/// @param n            nullable,probably is below:
/// 
/// - null
/// - {@link Long} type
/// - {@link Integer} type
/// - {@link SQLs#parameter(Object)},argument type is {@link Long} or {@link Integer}
/// - {@link SQLs#literalValue(Object) },argument type is {@link Long} or {@link Integer}
/// 
/// @param defaultValue non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_lag">LAG(expr [, N[, default]]) [null_treatment] over_clause</a>
    public static _OverSpec lag(Expression expr, Expression n, Expression defaultValue) {
        return WindowFunctions.compositeWindowFunc("LAG", Arrays.asList(expr, SQLs.COMMA, n, SQLs.COMMA, defaultValue));
    }

/// 
/// The {@link MappingType} of function return type: the {@link MappingType} of expr.
/// @param expr non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_lead">LEAD(expr [, N[, default]]) [null_treatment] over_clause</a>
    public static _OverSpec lead(Expression expr) {
        return WindowFunctions.oneArgWindowFunc("LEAD", expr);
    }


/// The {@link MappingType} of function return type: the {@link MappingType} of expr.
/// @param expr non-null parameter or {@link  Expression},but couldn't be {@link  SQLs#NULL}
/// @param n    nullable,probably is below:
/// 
/// - null
/// - {@link Long} type
/// - {@link Integer} type
/// - {@link SQLs#parameter(Object)},argument type is {@link Long} or {@link Integer}
/// - {@link SQLs#literalValue(Object) },argument type is {@link Long} or {@link Integer}
/// 
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_lead">LEAD(expr [, N[, default]]) [null_treatment] over_clause</a>
    public static _OverSpec lead(Expression expr, Expression n) {
        return WindowFunctions.twoArgWindowFunc("LEAD", expr, n);
    }

/// The {@link MappingType} of function return type: the {@link MappingType} of expr.
/// @param expr         non-null parameter or {@link  Expression},but couldn't be {@link  SQLs#NULL}
/// @param n            nullable,probably is below:
/// 
/// - null
/// - {@link Long} type
/// - {@link Integer} type
/// - {@link SQLs#parameter(Object)},argument type is {@link Long} or {@link Integer}
/// - {@link SQLs#literalValue(Object) },argument type is {@link Long} or {@link Integer}
/// 
/// @param defaultValue non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_lead">LEAD(expr [, N[, default]]) [null_treatment] over_clause</a>
    public static _OverSpec lead(Expression expr, Expression n, Expression defaultValue) {
        return WindowFunctions.compositeWindowFunc("LEAD", Arrays.asList(expr, SQLs.COMMA, n, SQLs.COMMA, defaultValue));
    }

/// 
/// The {@link MappingType} of function return type: the {@link MappingType} of expr.
/// @param expr non-null {@link  Expression}
/// @param n    positive.output literal.
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_nth-value">NTH_VALUE(expr, N) [from_first_last] [null_treatment] over_clause</a>
    public static _OverSpec nthValue(Expression expr, Expression n) {
        return WindowFunctions.twoArgWindowFunc("NTH_VALUE", expr, n);
    }

/// The {@link MappingType} of function return type: {@link LongType}
/// @param n positive number or {@link  Expression}.in any of the following forms:
/// 
/// - positive number:
/// 
/// - {@link  Long}
/// - {@link  Integer}
/// - {@link  Short}
/// - {@link  Byte}
/// 
/// 
/// - positive number parameter {@link  Expression},eg:{@link SQLs#parameter(Object)}
/// - positive number literal {@link  Expression},eg:{@link SQLs#literalValue(Object)}
/// - variable {@link  Expression}
/// 
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_ntile">NTILE(N) over_clause</a>
    public static _OverSpec ntile(Expression n) {
        return WindowFunctions.oneArgWindowFunc("NTILE", n);
    }

/// The {@link MappingType} of function return type: {@link DoubleType}.
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_percent-rank">PERCENT_RANK() over_clause</a>
    public static _OverSpec percentRank() {
        return WindowFunctions.zeroArgWindowFunc("PERCENT_RANK");
    }

/// The {@link MappingType} of function return type: {@link LongType}.
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/window-function-descriptions.html#function_percent-rank">RANK() over_clause</a>
    public static _OverSpec rank() {
        return WindowFunctions.zeroArgWindowFunc("RANK");
    }



    /*-------------------below Aggregate Function-------------------*/

/// The {@link MappingType} of function return type: {@link  LongType}
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_count">COUNT(expr) [over_clause]</a>
    public static _WindowAggSpec countAsterisk() {
        return count(SQLs.ASTERISK);
    }

/// The {@link MappingType} of function return type: {@link  LongType}
/// @param expr non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_count">COUNT(expr) [over_clause]</a>
    public static _WindowAggSpec count(Expression expr) {
        return WindowFunctions.oneArgWindowAggFunc("COUNT", expr);
    }

/// The {@link MappingType} of function return type: {@link  LongType}
/// @param expr non-null
/// @throws io.army.criteria.CriteriaException throw when
/// 
/// - distinct isn't {@link SQLs#DISTINCT}
/// - not in statement context
/// 
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_count">COUNT(expr) [over_clause]</a>
    public static _WindowAggSpec count(SQLs.ArgDistinct distinct, Expression expr) {
        FuncExpUtils.assertDistinct(distinct, SQLs.DISTINCT);
        return WindowFunctions.compositeWindowAggFunc("COUNT", Arrays.asList(distinct, expr));
    }


/// The {@link MappingType} of function return type: {@link  MappingType} of exp
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_min">MIN(expr) [over_clause]</a>
    public static _WindowAggSpec min(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("MIN", exp);
    }

/// The {@link MappingType} of function return type: {@link  MappingType} of exp
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_max">MAX(expr) [over_clause]</a>
    public static _WindowAggSpec max(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("MAX", exp);
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
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_sum">MySQL SUM([DISTINCT] expr)</a>
/// @see <a href="https://www.h2database.com/html/functions-aggregate.html#sum">H2 SUM([DISTINCT] expr)</a>
/// @see <a href="https://www.postgresql.org/docs/current/functions-aggregate.html#FUNCTIONS-AGGREGATE-TABLE">Postgre SUM([DISTINCT] expr) [over_clause]</a>
    public static _WindowAggSpec sum(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("SUM", exp);
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
/// @throws io.army.criteria.CriteriaException throw when
/// 
/// - distinct isn't {@link SQLs#DISTINCT}
/// - not in statement context
/// 
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_sum">MySQL SUM([DISTINCT] expr) [over_clause]</a>
/// @see <a href="https://www.h2database.com/html/functions-aggregate.html#sum">H2 SUM([DISTINCT] expr)</a>
    @Support({MySQL, H2})
    public static _WindowAggSpec sum(SQLs.ArgDistinct distinct, Expression exp) {
        FuncExpUtils.assertDistinct(distinct, SQLs.DISTINCT);
        return WindowFunctions.compositeWindowAggFunc("SUM", List.of(distinct, exp));
    }

/// The {@link MappingType} of function return type:
/// 
/// - sql float type : {@link DoubleType}
/// - sql integer/decimal type : {@link BigDecimalType}
/// - sql interval : {@link IntervalType}
/// - else : {@link TextType}
/// 
/// @param expr non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_avg">AVG([DISTINCT] expr) [over_clause]</a>
    public static _WindowAggSpec avg(Expression expr) {
        return WindowFunctions.oneArgWindowAggFunc("AVG", expr);
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
/// @throws io.army.criteria.CriteriaException throw when
/// 
/// - distinct isn't {@link SQLs#DISTINCT}
/// - not in statement context
/// 
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_avg">AVG([DISTINCT] expr) [over_clause]</a>
    public static _WindowAggSpec avg(SQLs.ArgDistinct distinct, Expression expr) {
        FuncExpUtils.assertDistinct(distinct, SQLs.DISTINCT);
        return WindowFunctions.compositeWindowAggFunc("AVG", Arrays.asList(distinct, expr));
    }

/// The {@link MappingType} of function return type: {@link JsonType#TEXT}
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_json-arrayagg">MySQL JSON_ARRAYAGG(col_or_expr) [over_clause]</a>
/// @see <a href="https://www.h2database.com/html/functions-aggregate.html#json_arrayagg">H2 JSON_ARRAYAGG(col_or_expr) [over_clause]</a>
/// @see <a href="https://www.postgresql.org/docs/current/functions-aggregate.html#FUNCTIONS-AGGREGATE-TABLE">Postgre JSON_ARRAYAGG(col_or_expr) [over_clause]</a>
    public static _WindowAggSpec jsonArrayAgg(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("JSON_ARRAYAGG", exp);
    }

/// The {@link MappingType} of function return type: {@link JsonType#TEXT}
/// @param key   non-null
/// @param value non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_json-objectagg">MySQL JSON_OBJECTAGG(col_or_expr) [over_clause]</a>
/// @see <a href="https://www.h2database.com/html/functions-aggregate.html#json_objectagg">H2 JSON_OBJECTAGG(col_or_expr) [over_clause]</a>
/// @see <a href="https://www.postgresql.org/docs/current/functions-aggregate.html#FUNCTIONS-AGGREGATE-TABLE">Postgre JSON_OBJECTAGG(col_or_expr) [over_clause]</a>
    public static _WindowAggSpec jsonObjectAgg(Expression key, Expression value) {
        return WindowFunctions.twoArgAggWindow("JSON_OBJECTAGG", key, value);
    }

/// The {@link MappingType} of function return type: {@link DoubleType}
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_std">STD(expr) [over_clause]</a>
    public static _WindowAggSpec std(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("STD", exp);
    }

/// The {@link MappingType} of function return type: {@link DoubleType}
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_stddev">STDDEV(expr) [over_clause]</a>
    public static _WindowAggSpec stdDev(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("STDDEV", exp);
    }

/// The {@link MappingType} of function return type: {@link DoubleType}
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_stddev-pop">STDDEV_POP(expr) [over_clause]</a>
    public static _WindowAggSpec stdDevPop(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("STDDEV_POP", exp);
    }

/// The {@link MappingType} of function return type: {@link DoubleType}
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_stddev-samp">STDDEV_SAMP(expr) [over_clause]</a>
    public static _WindowAggSpec stdDevSamp(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("STDDEV_SAMP", exp);
    }

/// The {@link MappingType} of function return type: {@link DoubleType}
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_var-pop">VAR_POP(expr) [over_clause]</a>
    public static _WindowAggSpec varPop(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("VAR_POP", exp);
    }


/// The {@link MappingType} of function return type: {@link DoubleType}
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_var-samp">VAR_SAMP(expr) [over_clause]</a>
    public static _WindowAggSpec varSamp(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("VAR_SAMP", exp);
    }

/// The {@link MappingType} of function return type: {@link DoubleType}
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_variance">VARIANCE(expr) [over_clause]</a>
    public static _WindowAggSpec variance(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("VARIANCE", exp);
    }

/// The {@link MappingType} of function return type:
/// 
/// - If expr is integer number type ,then {@link UnsignedLongType}
/// - else {@link VarBinaryType}
/// 
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_bit-and">BIT_AND(expr) [over_clause]</a>
    public static _WindowAggSpec bitAnd(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("BIT_AND", exp);
    }

/// The {@link MappingType} of function return type:
/// 
/// - If expr is integer number type ,then {@link UnsignedLongType}
/// - else {@link VarBinaryType}
/// 
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_bit-or">BIT_OR(expr) [over_clause]</a>
    public static _WindowAggSpec bitOr(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("BIT_OR", exp);
    }

/// The {@link MappingType} of function return type:
/// 
/// - If expr is integer number type ,then {@link UnsignedLongType}
/// - else {@link VarBinaryType}
/// 
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html#function_bit-xor">BIT_XOR(expr) [over_clause]</a>
    public static _WindowAggSpec bitXor(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("BIT_XOR", exp);
    }


/// The {@link MappingType} of function return type:
/// 
/// - If expr is integer number type ,then {@link UnsignedLongType}
/// - else {@link VarBinaryType}
/// 
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://www.h2database.com/html/functions-aggregate.html#bit_and_agg">BIT_AND_AGG(expr) [over_clause]</a>
    @Support({H2})
    public static _WindowAggSpec bitAndAgg(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("BIT_AND_AGG", exp);
    }

/// The {@link MappingType} of function return type:
/// 
/// - If expr is integer number type ,then {@link UnsignedLongType}
/// - else {@link VarBinaryType}
/// 
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://www.h2database.com/html/functions-aggregate.html#bit_and_agg">BIT_OR_AGG(expr) [over_clause]</a>
    @Support({H2})
    public static _WindowAggSpec bitOrAgg(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("BIT_OR_AGG", exp);
    }

/// The {@link MappingType} of function return type:
/// 
/// - If expr is integer number type ,then {@link UnsignedLongType}
/// - else {@link VarBinaryType}
/// 
/// @param exp non-null
/// @throws io.army.criteria.CriteriaException throw when not in statement context
/// @see <a href="https://www.h2database.com/html/functions-aggregate.html#bit_and_agg">BIT_XOR_AGG(expr) [over_clause]</a>
    @Support({H2})
    public static _WindowAggSpec bitXorAgg(Expression exp) {
        return WindowFunctions.oneArgWindowAggFunc("BIT_XOR_AGG", exp);
    }


}
