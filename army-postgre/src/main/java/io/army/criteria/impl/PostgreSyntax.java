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
import io.army.dialect.PostgreDialect;
import io.army.dialect._Constant;
import io.army.mapping.*;
import io.army.mapping.optional.NoCastTextType;
import io.army.meta.FieldMeta;
import io.army.util._StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/// 
/// Package class
/// @since 0.6.0
@SuppressWarnings("unused")
abstract class PostgreSyntax extends PostgreWindowFunctions {

    /// Package constructor
    PostgreSyntax() {
    }


    /// Construct an empty array,and you have to invoke {@link Expression#castTo(MappingType)} method
    /// or database response error.
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/sql-expressions.html#SQL-SYNTAX-ARRAY-CONSTRUCTORS">Array Constructors</a>
    /// 
    public static Expression array() {
        return Expressions.array(List.of());
    }

    /// Static array constructor
    /// @param elementList {@link Expression} or literal list
    /// @see List#of(Object)
    /// @see <a href="https://www.postgresql.org/docs/current/sql-expressions.html#SQL-SYNTAX-ARRAY-CONSTRUCTORS">Array Constructors</a>
    public static Expression array(List<?> elementList) {
        return Expressions.array(elementList);
    }

    public static Expression array(SubQuery subQuery) {
        return Expressions.array(subQuery);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-insert.html">exclude of INSERT statement</a>
    public static Expression excluded(FieldMeta<?> field) {
        return ContextStack.peek().insertValueField(field, PostgreExcludedField::excludedField);
    }


    /// @param expression couldn't be multi-value parameter/literal, for example {@link SQLs#rowParam(TypeInfer, Collection)}
    /// @see <a href="https://www.postgresql.org/docs/15/sql-syntax-calling-funcs.html#SQL-SYNTAX-CALLING-FUNCS-POSITIONAL">Using Positional Notation</a>
    /// @see <a href="https://www.postgresql.org/docs/15/sql-syntax-calling-funcs.html#SQL-SYNTAX-CALLING-FUNCS-NAMED">Using Named Notation</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-datetime.html#FUNCTIONS-DATETIME-TABLE">make_interval ( [ years int [, months int [, weeks int [, days int [, hours int [, mins int [, secs double precision ]]]]]]] ) → interval</a>
    public static Expression namedNotation(String name, Expression expression) {
        return FunctionUtils.namedNotation(name, expression);
    }

    /// @param valueOperator couldn't return multi-value parameter/literal, for example {@link SQLs#rowParam(TypeInfer, Collection)}
    /// @see <a href="https://www.postgresql.org/docs/15/sql-syntax-calling-funcs.html#SQL-SYNTAX-CALLING-FUNCS-POSITIONAL">Using Positional Notation</a>
    /// @see <a href="https://www.postgresql.org/docs/15/sql-syntax-calling-funcs.html#SQL-SYNTAX-CALLING-FUNCS-NAMED">Using Named Notation</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-datetime.html#FUNCTIONS-DATETIME-TABLE">make_interval ( [ years int [, months int [, weeks int [, days int [, hours int [, mins int [, secs double precision ]]]]]]] ) → interval</a>
    public static <T> Expression namedNotation(String name, Function<T, Expression> valueOperator, T value) {
        return FunctionUtils.namedNotation(name, valueOperator.apply(value));
    }




    /*-------------------below operator method -------------------*/

    /// 
    /// The {@link MappingType} of function return type: the {@link  MappingType} of exp
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-OP-TABLE">Absolute value operator</a>
    public static Expression at(Expression operand) {
        return PgExpressions.unaryExpression(PostgreUnaryExpOperator.AT, operand);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">@-@ geometric_type → double precision
    /// Computes the total length. Available for lseg, path.
    /// </a>
    public static Expression atHyphenAt(Expression operand) {
        return PgExpressions.unaryExpression(PostgreUnaryExpOperator.AT_HYPHEN_AT, operand);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">@@ geometric_type → point
    /// Computes the center point. Available for box, lseg, polygon, circle.
    /// </a>
    public static Expression atAt(Expression operand) {
        return PgExpressions.unaryExpression(PostgreUnaryExpOperator.AT_AT, operand);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE"># geometric_type → integer
    /// Returns the number of points. Available for path, polygon.
    /// </a>
    public static Expression pound(Expression operand) {
        return PgExpressions.unaryExpression(PostgreUnaryExpOperator.POUND, operand);
    }

    /*-------------------below dual operator -------------------*/


    /// Create PostgreSQL-style typecast expression. Format : 'string'::type .
    /// This method is used for postgre dialect type, for example : text, regclass .
    /// **NOTE**: {@link LiteralExpression#typeMeta()} always is {@link NoCastTextType#INSTANCE}
    /// examples :
    /// <pre>
    /// Postgres.space("my_seq",Postgres.DOUBLE_COLON,"regclass")
    /// Postgres.space('my_seq',DOUBLE_COLON,"regclass")
    /// Postgres.space('QinArmy',DOUBLE_COLON,"text")
    /// </pre>
    /// @param literal     text literal
    /// @param doubleColon must be {@link Postgres#DOUBLE_COLON}
    /// @param typeName    not key word , a simple sql identifier.
    /// @return a {@link LiteralExpression} whose {@link LiteralExpression#typeMeta()} always is {@link NoCastTextType#INSTANCE}
    /// @throws CriteriaException throw when
    /// - literal error,here is delay , throw when parsing
    /// - typeName error,here is delay , throw when parsing
    /// - dialect isn't {@link PostgreDialect},here is delay , throw when parsing
    /// 
    public static LiteralExpression space(String literal, Postgres.DoubleColon doubleColon, String typeName) {
        if (doubleColon != Postgres.DOUBLE_COLON) {
            throw CriteriaUtils.errorSymbol(doubleColon);
        }
        return PostgreDoubleColonCastExpression.cast(literal, typeName);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">geometric_type # geometric_type → point
    /// Computes the point of intersection, or NULL if there is none. Available for lseg, line.
    /// </a>
    public static Expression pound(Expression left, Expression right) {
        return Expressions.dualExp(left, DualExpOperator.POUND, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">geometric_type <-> geometric_type → double precision
    /// Computes the distance between the objects. Available for all seven geometric types, for all combinations of point with another geometric type, and for these additional pairs of types: (box, lseg), (lseg, line), (polygon, circle) (and the commutator cases).
    /// </a>
    public static Expression ltHyphenGt(Expression left, Expression right) {
        return Expressions.dualExp(left, DualExpOperator.BI_ARROW, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-OP-TABLE">numeric_type + numeric_type → numeric_type</a>
    public static Expression plus(Expression left, Expression right) {
        return Expressions.dualExp(left, DualExpOperator.PLUS, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-OP-TABLE">numeric_type - numeric_type → numeric_type
    /// Subtraction</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-json.html#FUNCTIONS-JSONB-OP-TABLE">jsonb - text → jsonb
    /// jsonb - text[] → jsonb
    /// jsonb - integer → jsonb
    /// </a>
    public static Expression minus(Expression left, Expression right) {
        return Expressions.dualExp(left, DualExpOperator.MINUS, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-OP-TABLE">numeric_type * numeric_type → numeric_type
    /// Multiplication</a>
    public static Expression times(Expression left, Expression right) {
        return Expressions.dualExp(left, DualExpOperator.TIMES, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-OP-TABLE">numeric_type / numeric_type → numeric_type
    /// Division (for integral types, division truncates the result towards zero)</a>
    public static Expression divide(Expression left, Expression right) {
        return Expressions.dualExp(left, DualExpOperator.DIVIDE, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-OP-TABLE">numeric_type % numeric_type → numeric_type
    /// Modulo (remainder); available for smallint, integer, bigint, and numeric</a>
    public static Expression mode(Expression left, Expression right) {
        return Expressions.dualExp(left, DualExpOperator.MOD, right);
    }


    /// 
    /// The {@link MappingType} of function return type: follow <pre><code>
    /// private static MappingType caretResultType(final MappingType left, final MappingType right) {
    /// final MappingType returnType;
    /// if (left instanceof MappingType.IntegerOrDecimalType
    /// && right instanceof MappingType.IntegerOrDecimalType) {
    /// returnType = BigDecimalType.INSTANCE;
    /// } else {
    /// returnType = DoubleType.INSTANCE;
    /// }
    /// return returnType;
    /// }
    /// </code></pre>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-OP-TABLE">numeric ^ numeric → numeric
    /// double precision ^ double precision → double precision
    /// Exponentiation</a>
    public static Expression caret(final Expression left, final Expression right) {
        return Expressions.dualExp(left, DualExpOperator.CARET, right);
    }


    /// @see #doubleAmp(Expression, Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-OPERATORS-TABLE">tsquery && tsquery → tsquery
    /// ANDs two tsquerys together, producing a query that matches documents that match both input queries.
    /// </a>
    public static Expression ampAmp(Expression left, Expression right) {
        return Expressions.dualExp(left, DualExpOperator.AMP_AMP, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-json.html#FUNCTIONS-JSON-PROCESSING">json -> integer → json
    /// jsonb -> integer → jsonb
    /// Extracts n'th element of JSON array (array elements are indexed from zero, but negative integers count from the end).
    /// '[{"a":"foo"},{"b":"bar"},{"c":"baz"}]'::json -> 2 → {"c":"baz"}
    /// </a>
    public static Expression hyphenGt(Expression left, Expression right) {
        return Expressions.dualExp(left, DualExpOperator.ARROW, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-json.html#FUNCTIONS-JSON-PROCESSING">json ->> integer → text
    /// jsonb ->> integer → text
    /// Extracts n'th element of JSON array, as text.
    /// '[1,2,3]'::json ->> 2 → 3
    /// json ->> text → text
    /// jsonb ->> text → text
    /// </a>
    public static Expression hyphenGtGt(Expression left, Expression right) {
        return Expressions.dualExp(left, DualExpOperator.DARROW, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-json.html#FUNCTIONS-JSONB-OP-TABLE">jsonb #- text[] → jsonb
    /// Deletes the field or array element at the specified path, where path elements can be either field keys or array indexes.
    /// '["a", {"b":1}]'::jsonb #- '{1,b}' → ["a", {}]
    /// </a>
    public static Expression poundHyphen(Expression left, Expression right) {
        return Expressions.dualExp(left, DualExpOperator.POUND_HYPHEN, right);
    }


    /// @see <a href="https://www.postgresql.org/docs/current/functions-json.html#FUNCTIONS-JSON-PROCESSING">json #> text[] → json
    /// jsonb #> text[] → jsonb
    /// Extracts JSON sub-object at the specified path, where path elements can be either field keys or array indexes.
    /// '{"a": {"b": ["foo","bar"]}}'::json #> '{a,b,1}' → "bar"
    /// </a>
    public static Expression poundGt(Expression left, Expression right) {
        return Expressions.dualExp(left, DualExpOperator.POUND_GT, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-json.html#FUNCTIONS-JSON-PROCESSING">json #>> text[] → text
    /// jsonb #>> text[] → text
    /// Extracts JSON sub-object at the specified path as text.
    /// '{"a": {"b": ["foo","bar"]}}'::json #>> '{a,b,1}' → bar
    /// </a>
    public static Expression poundGtGt(Expression left, Expression right) {
        return Expressions.dualExp(left, DualExpOperator.POUND_GT_GT, right);
    }


    /// 
    /// The {@link MappingType} of operator return type: follow <pre><code>
    /// private static MappingType doubleVerticalType(final MappingType left, final MappingType right) {
    /// final MappingType returnType;
    /// if (left instanceof MappingType.SqlStringType || right instanceof MappingType.SqlStringType) {
    /// returnType = TextType.INSTANCE;
    /// } else if (left instanceof MappingType.SqlBinaryType || right instanceof MappingType.SqlBinaryType) {
    /// if (left instanceof MappingType.SqlBitType || right instanceof MappingType.SqlBitType) {
    /// throw CriteriaUtils.dualOperandError(DualOperator.DOUBLE_VERTICAL, left, right);
    /// }
    /// returnType = PrimitiveByteArrayType.INSTANCE;
    /// } else if (left instanceof MappingType.SqlBitType || right instanceof MappingType.SqlBitType) {
    /// returnType = BitSetType.INSTANCE;
    /// } else {
    /// throw CriteriaUtils.dualOperandError(DualOperator.DOUBLE_VERTICAL, left, right);
    /// }
    /// return returnType;
    /// }
    /// </code>
    /// </pre>
    /// @param left  not {@link SQLs#DEFAULT} etc.
    /// @param right not {@link SQLs#DEFAULT} etc.
    /// @see Expression#space(SQLs.DualOperator, Object)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-bitstring.html#FUNCTIONS-BIT-STRING-OP-TABLE">bit || bit → bit</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-string.html#FUNCTIONS-STRING-SQL">text || text → text
    /// text || anynonarray → text
    /// anynonarray || text → text
    /// </a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-binarystring.html#FUNCTIONS-BINARYSTRING-SQL">bytea || bytea → bytea</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-OPERATORS-TABLE">tsvector || tsvector → tsvector</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-OPERATORS-TABLE">tsquery || tsquery → tsquery</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-OPERATORS-TABLE">tsquery || tsquery → tsquery</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-json.html#FUNCTIONS-JSONB-OP-TABLE">jsonb || jsonb → jsonb</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html">anycompatiblearray || anycompatiblearray → anycompatiblearray</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html">anycompatible || anycompatiblearray → anycompatiblearray</a>
    private static Expression doubleVertical(final Expression left, final Expression right) {
        return Expressions.dualExp(left, DualExpOperator.CONCAT, right);
    }


    /// 
    /// The {@link MappingType} of function return type:{@link DoubleType}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-OP-TABLE">|/ double precision → double precision
    /// Square root
    /// </a>
    public static Expression verticalSlash(final Expression exp) {
        return PgExpressions.unaryExpression(PostgreUnaryExpOperator.VERTICAL_SLASH, exp);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-OPERATORS-TABLE">!! tsquery → tsquery
    /// Negates a tsquery, producing a query that matches documents that do not match the input query.
    /// </a>
    public static Expression doubleExclamation(final Expression exp) {
        return PgExpressions.unaryExpression(PostgreUnaryExpOperator.DOUBLE_EXCLAMATION, exp);
    }

    /// 
    /// The {@link MappingType} of function return type:{@link DoubleType}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-math.html#FUNCTIONS-MATH-OP-TABLE">||/ double precision → double precision
    /// Cube root
    /// </a>
    public static Expression doubleVerticalSlash(final Expression operand) {
        return PgExpressions.unaryExpression(PostgreUnaryExpOperator.DOUBLE_VERTICAL_SLASH, operand);
    }


    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">?- line → boolean
    /// ?- lseg → boolean
    /// Is line horizontal?
    /// </a>
    public static IPredicate questionHyphen(Expression operand) {
        return PgExpressions.unaryPredicate(PostgreBooleanUnaryOperator.QUESTION_HYPHEN, operand);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">?| line → boolean
    /// ?| lseg → boolean
    /// Is line vertical?
    /// </a>
    public static IPredicate questionVertical(Expression operand) {
        return PgExpressions.unaryPredicate(PostgreBooleanUnaryOperator.QUESTION_VERTICAL, operand);
    }


    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">geometric_type @> geometric_type → boolean
    /// Does first object contain second? Available for these pairs of types: (box, point), (box, box), (path, point), (polygon, point), (polygon, polygon), (circle, point), (circle, circle).
    /// </a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-OPERATORS-TABLE">tsquery @> tsquery → boolean
    /// </a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-json.html#FUNCTIONS-JSONB-OP-TABLE">jsonb @> jsonb → boolean
    /// </a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html">anyarray @> anyarray → boolean
    /// </a>
    private static IPredicate atGt(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.AT_GT, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-json.html#FUNCTIONS-JSONB-OP-TABLE">jsonb @? jsonpath → boolean
    /// Does JSON path return any item for the specified JSON value?
    /// '{"a":[1,2,3,4,5]}'::jsonb @? '$.a[*] ? (@ > 2)' → t
    /// </a>
    private static IPredicate atQuestion(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.AT_QUESTION, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">geometric_type <&#64; geometric_type → boolean
    /// Is first object contained in or on second? Available for these pairs of types: (point, box), (point, lseg), (point, line), (point, path), (point, polygon), (point, circle), (box, box), (lseg, box), (lseg, line), (polygon, polygon), (circle, circle).
    /// </a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-OPERATORS-TABLE">tsquery <&#64; tsquery → boolean
    /// </a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-json.html#FUNCTIONS-JSONB-OP-TABLE">jsonb <&#64; jsonb → boolean
    /// </a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html">anyarray <&#64; anyarray → boolean
    /// </a>
    private static IPredicate ltAt(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.LT_AT, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">geometric_type && geometric_type → boolean
    /// Do these objects overlap? (One point in common makes this true.) Available for box, polygon, circle.</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-net.html#CIDR-INET-OPERATORS-TABLE">inet && inet → boolean
    /// Does either subnet contain or equal the other?
    /// inet '192.168.1/24' && inet '192.168.1.80/28' → t
    /// inet '192.168.1/24' && inet '192.168.2.0/28' → f
    /// </a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-array.html">anyarray && anyarray → boolean
    /// </a>
    private static IPredicate doubleAmp(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.DOUBLE_AMP, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">geometric_type << geometric_type → boolean
    /// Is first object strictly left of second? Available for point, box, polygon, circle.
    /// inet << inet → boolean
    /// Is subnet strictly contained by subnet? This operator, and the next four, test for subnet inclusion. They consider only the network parts of the two
    /// addresses (ignoring any bits to the right of the netmasks) and determine whether one network is identical to or a subnet of the other.
    /// inet '192.168.1.5'  << inet '192.168.1/24' → t
    /// inet '192.168.0.5'  << inet '192.168.1/24' → f
    /// inet '192.168.1/24' << inet '192.168.1/24' → f
    /// </a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-net.html#CIDR-INET-OPERATORS-TABLE">inet << inet → boolean
    /// Is subnet strictly contained by subnet? This operator, and the next four, test for subnet inclusion. They consider only the network parts of the two
    /// addresses (ignoring any bits to the right of the netmasks) and determine whether one network is identical to or a subnet of the other.
    /// inet '192.168.1.5'  << inet '192.168.1/24' → t
    /// inet '192.168.0.5'  << inet '192.168.1/24' → f
    /// inet '192.168.1/24' << inet '192.168.1/24' → f
    /// </a>
    private static IPredicate ltLt(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.LT_LT, right);
    }


    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">geometric_type >> geometric_type → boolean
    /// Is first object strictly right of second? Available for point, box, polygon, circle.</a>
    private static IPredicate gtGt(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.GT_GT, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-net.html#CIDR-INET-OPERATORS-TABLE">inet <<= inet → boolean
    /// Is subnet contained by or equal to subnet?
    /// inet '192.168.1/24' <<= inet '192.168.1/24' → t
    /// </a>
    private static IPredicate ltLtEqual(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.LT_LT_EQUAL, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-net.html#CIDR-INET-OPERATORS-TABLE">inet >>= inet → boolean
    /// Does subnet contain or equal subnet?
    /// inet '192.168.1/24' >>= inet '192.168.1/24' → t
    /// </a>
    private static IPredicate gtGtEqual(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.GT_GT_EQUAL, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">geometric_type &< geometric_type → boolean
    /// Does first object not extend to the right of second? Available for box, polygon, circle.</a>
    private static IPredicate ampLt(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.AMP_LT, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">geometric_type &> geometric_type → boolean
    /// Does first object not extend to the left of second? Available for box, polygon, circle.</a>
    private static IPredicate ampGt(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.AMP_GT, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">geometric_type <<| geometric_type → boolean
    /// Is first object strictly below second? Available for point, box, polygon, circle.</a>
    private static IPredicate ltLtVertical(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.LT_LT_VERTICAL, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">geometric_type |>> geometric_type → boolean
    /// Is first object strictly above second? Available for point, box, polygon, circle.</a>
    private static IPredicate verticalGtGt(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.VERTICAL_GT_GT, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">geometric_type &<| geometric_type → boolean
    /// Does first object not extend above second? Available for box, polygon, circle.</a>
    private static IPredicate ampLtVertical(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.AMP_LT_VERTICAL, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">geometric_type |&> geometric_type → boolean
    /// Does first object not extend below second? Available for box, polygon, circle.</a>
    private static IPredicate verticalAmpGt(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.VERTICAL_AMP_GT, right);
    }


    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">box <^ box → boolean
    /// Is first object below second (allows edges to touch)?</a>
    private static IPredicate ltCaret(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.LT_CARET, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">box >^ box → boolean
    /// Is first object above second (allows edges to touch)?</a>
    private static IPredicate gtCaret(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.GT_CARET, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-json.html#FUNCTIONS-JSONB-OP-TABLE">jsonb ? text → boolean
    /// Does the text string exist as a top-level key or array element within the JSON value?
    /// '{"a":1, "b":2}'::jsonb ? 'b' → t
    /// '["a", "b", "c"]'::jsonb ? 'b' → t
    /// </a>
    private static IPredicate question(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.QUESTION, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">geometric_type ?# geometric_type → boolean
    /// Is first object above second (allows edges to touch)?</a>
    private static IPredicate questionPound(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.QUESTION_POUND, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-json.html#FUNCTIONS-JSONB-OP-TABLE">jsonb ?& text[] → boolean
    /// Do all of the strings in the text array exist as top-level keys or array elements?
    /// '["a", "b", "c"]'::jsonb ?& array['a', 'b'] → t
    /// </a>
    private static IPredicate questionAmp(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.QUESTION_AMP, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">point ?- point → boolean
    /// Are points horizontally aligned (that is, have same y coordinate)?</a>
    private static IPredicate questionHyphen(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.QUESTION_HYPHEN, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">point ?| point → boolean
    /// Are points vertically aligned (that is, have same x coordinate)?</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-json.html#FUNCTIONS-JSONB-OP-TABLE">jsonb ?| text[] → boolean
    /// Do any of the strings in the text array exist as top-level keys or array elements?
    /// '{"a":1, "b":2, "c":3}'::jsonb ?| array['b', 'd'] → t
    /// </a>
    private static IPredicate questionVertical(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.QUESTION_VERTICAL, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">line ?-| line → boolean
    /// lseg ?-| lseg → boolean</a>
    private static IPredicate questionHyphenVertical(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.QUESTION_HYPHEN_VERTICAL, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">line ?|| line → boolean
    /// lseg ?|| lseg → boolean</a>
    private static IPredicate questionVerticalVertical(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.QUESTION_VERTICAL_VERTICAL, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-geometry.html#FUNCTIONS-GEOMETRY-OP-TABLE">geometric_type ~= geometric_type → boolean
    /// Are these objects the same? Available for point, box, polygon, circle.
    /// </a>
    private static IPredicate tildeEqual(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.TILDE_EQUAL, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-OPERATORS-TABLE">tsvector @@ tsquery → boolean
    /// tsquery @@ tsvector → boolean
    /// text @@ tsquery → boolean
    /// </a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-json.html#FUNCTIONS-JSONB-OP-TABLE">jsonb @@ jsonpath → boolean
    /// tReturns the result of a JSON path predicate check for the specified JSON value. Only the first item of the result is taken into account. If the result is not Boolean, then NULL is returned.
    /// '{"a":[1,2,3,4,5]}'::jsonb @@ '$.a[*] > 2' → t
    /// </a>
    private static IPredicate doubleAt(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.AT_AT, right);
    }


    /// @see <a href="https://www.postgresql.org/docs/current/functions-textsearch.html#TEXTSEARCH-OPERATORS-TABLE">tsvector @@@ tsquery → boolean
    /// tsquery @@@ tsvector → boolean
    /// </a>
    private static IPredicate tripleAt(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.AT_AT_AT, right);
    }


    /// 
    /// The {@link MappingType} of operator return type: {@link  BooleanType} .
    /// @param left not {@link SQLs#DEFAULT} etc.
    /// @see Expression#space(SQLs.DualOperator, Object)
    /// @see Postgres#startsWith(Expression, Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-string.html#FUNCTIONS-STRING-OTHER">text ^@ text → boolean</a>
    private static IPredicate caretAt(Expression left, Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.CARET_AT, right);
    }


    /// 
    /// The {@link MappingType} of function return type:{@link BooleanType}
    /// @see Postgres#regexpLike(Expression, Expression)
    /// @see Postgres#regexpLike(Expression, Expression, Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-matching.html#FUNCTIONS-POSIX-TABLE">text ~ text → boolean
    /// String matches regular expression, case sensitively</a>
    private static IPredicate tilde(final Expression left, final Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.TILDE, right);
    }

    /// 
    /// The {@link MappingType} of function return type:{@link BooleanType}
    /// @see Postgres#regexpLike(Expression, Expression)
    /// @see Postgres#regexpLike(Expression, Expression, Expression)
    /// @see <a href="https://www.postgresql.org/docs/current/functions-matching.html#FUNCTIONS-POSIX-TABLE">text !~ text → boolean
    /// String does not match regular expression, case sensitively</a>
    private static IPredicate notTilde(final Expression left, final Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.NOT_TILDE, right);
    }

    /// 
    /// The {@link MappingType} of function return type:{@link BooleanType}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-matching.html#FUNCTIONS-POSIX-TABLE">text ~* text → boolean
    /// String matches regular expression, case insensitively</a>
    private static IPredicate tildeStar(final Expression left, final Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.TILDE_STAR, right);
    }

    /// 
    /// The {@link MappingType} of function return type:{@link BooleanType}
    /// @see <a href="https://www.postgresql.org/docs/current/functions-matching.html#FUNCTIONS-POSIX-TABLE">text !~* text → boolean
    /// String does not match regular expression, case insensitively</a>
    private static IPredicate notTildeStar(final Expression left, final Expression right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.NOT_TILDE_STAR, right);
    }


    /// 
    /// OVERLAPS operator
    /// @see <a href="https://www.postgresql.org/docs/current/functions-datetime.html"> OVERLAPS operato</a>
    public static Postgres._PeriodOverlapsClause period(final Expression start, final Expression endOrLength) {
        return PgExpressions.overlaps(start, endOrLength);
    }


    /// 
    /// OVERLAPS operator
    /// @see <a href="https://www.postgresql.org/docs/current/functions-datetime.html"> OVERLAPS operato</a>
    public static <T> Postgres._PeriodOverlapsClause period(Expression start, BiFunction<Expression, T, Expression> valueOperator, T value) {
        return period(start, valueOperator.apply(start, value));
    }

    /// 
    /// OVERLAPS operator
    /// @see <a href="https://www.postgresql.org/docs/current/functions-datetime.html"> OVERLAPS operato</a>
    public static <T> Postgres._PeriodOverlapsClause period(BiFunction<Expression, T, Expression> valueOperator, T value, Expression endOrLength) {
        return period(valueOperator.apply(endOrLength, value), endOrLength);
    }

    /// 
    /// OVERLAPS operator
    /// @see <a href="https://www.postgresql.org/docs/current/functions-datetime.html"> OVERLAPS operato</a>
    public static Postgres._PeriodOverlapsClause period(TypeInfer type, BiFunction<TypeInfer, Object, Expression> valueOperator, Object start, Object endOrLength) {
        return period(valueOperator.apply(type, start), valueOperator.apply(type, endOrLength));
    }


    /// 
    /// AT TIME ZONE operator,The {@link MappingType} of operator return type:
    /// 
    /// - If The {@link MappingType} of source is {@link MappingType.SqlLocalDateTime},then {@link OffsetDateTimeType}
    /// - If The {@link MappingType} of source is {@link MappingType.SqlOffsetDateTime},then {@link LocalDateTimeType}
    /// - If The {@link MappingType} of source is {@link MappingType.SqlLocalTime},then {@link OffsetTimeType}
    /// - If The {@link MappingType} of source is {@link MappingType.SqlOffsetTime},then {@link LocalTimeType}
    /// - Else raise {@link CriteriaException}
    /// 
    /// @param source non-multi value parameter/literal
    /// @param zone   non-multi value parameter/literal
    /// @throws CriteriaException throw when
    /// - source is multi value parameter/literal
    /// - zone is multi value parameter/literal
    /// - The {@link MappingType} of source error
    /// 
    /// @see <a href="https://www.postgresql.org/docs/current/functions-datetime.html#FUNCTIONS-DATETIME-ZONECONVERT-TABLE"> AT TIME ZONE Variants</a>
    public static Expression atTimeZone(final Expression source, final Expression zone) {
        return Expressions.dualExp(source, SQLs.AT_TIME_ZONE, zone);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-matching.html#FUNCTIONS-SIMILARTO-REGEXP">SIMILAR TO Regular Expressions</a>
    private static IPredicate similarTo(Expression exp, Expression pattern) {
        return Expressions.likePredicate(exp, DualBooleanOperator.SIMILAR_TO, pattern, SQLs.ESCAPE, null);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-matching.html#FUNCTIONS-SIMILARTO-REGEXP">SIMILAR TO Regular Expressions</a>
    private static IPredicate similarTo(Expression exp, Expression pattern, SQLs.WordEscape escape, Expression escapeChar) {
        return Expressions.likePredicate(exp, DualBooleanOperator.SIMILAR_TO, pattern, escape, escapeChar);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-matching.html#FUNCTIONS-SIMILARTO-REGEXP">SIMILAR TO Regular Expressions</a>
    private static IPredicate notSimilarTo(Expression exp, Expression pattern) {
        return Expressions.likePredicate(exp, DualBooleanOperator.NOT_SIMILAR_TO, pattern, SQLs.ESCAPE, null);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/functions-matching.html#FUNCTIONS-SIMILARTO-REGEXP">SIMILAR TO Regular Expressions</a>
    private static IPredicate notSimilarTo(Expression exp, Expression pattern, SQLs.WordEscape escape, Expression escapeChar) {
        return Expressions.likePredicate(exp, DualBooleanOperator.NOT_SIMILAR_TO, pattern, escape, escapeChar);
    }

    /// @see <a href="https://www.postgresql.org/docs/15/functions-comparisons.html#ROW-WISE-COMPARISON">row_constructor IS DISTINCT FROM row_constructor
    /// </a>
    private static <T extends Expression> IPredicate isDistinctFrom(T left, T right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.IS_DISTINCT_FROM, right);
    }

    /// @see <a href="https://www.postgresql.org/docs/15/functions-comparisons.html#ROW-WISE-COMPARISON">row_constructor IS NOT DISTINCT FROM row_constructor
    /// </a>
    private static <T extends Expression> IPredicate isNotDistinctFrom(T left, T right) {
        return PgExpressions.dualPredicate(left, PgDualBoolOperator.IS_NOT_DISTINCT_FROM, right);
    }


    /// @see <a href="https://www.postgresql.org/docs/current/sql-expressions.html#SQL-SYNTAX-COLLATE-EXPRS">Collation Expressions</a>
    /// @see <a href="https://www.postgresql.org/docs/16/collation.html">collation</a>
    private static SimpleResultExpression collate(Expression expr, String collation) {
        return Expressions.collateExp(expr, collation);
    }


    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem.ExpressionGroup parens() {
        return Expressions.emptyParens();
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem.ExpressionGroup parens(Expression exp) {
        return (GroupByItem.ExpressionGroup) Expressions.parens(null, exp);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem.ExpressionGroup parens(Expression exp1, Expression exp2) {
        return (GroupByItem.ExpressionGroup) Expressions.parens(null, exp1, exp2);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem.ExpressionGroup parens(Expression exp1, Expression exp2, Expression exp3,
                                                     Expression... rest) {
        return (GroupByItem.ExpressionGroup) Expressions.parens(null, exp1, exp2, exp3, rest);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem.ExpressionGroup parens(Consumer<Consumer<Expression>> consumer) {
        return (GroupByItem.ExpressionGroup) Expressions.parens(null, consumer);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem rollup(GroupByItem.ExpressionItem exp) {
        return Expressions.parens(Expressions.GroupingModifier.ROLLUP, exp);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem rollup(GroupByItem.ExpressionItem exp1, GroupByItem.ExpressionItem exp2) {
        return Expressions.parens(Expressions.GroupingModifier.ROLLUP, exp1, exp2);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem rollup(GroupByItem.ExpressionItem exp1, GroupByItem.ExpressionItem exp2,
                                     GroupByItem.ExpressionItem exp3, GroupByItem.ExpressionItem... rest) {
        return Expressions.parens(Expressions.GroupingModifier.ROLLUP, exp1, exp2, exp3, rest);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem rollup(Consumer<Consumer<GroupByItem.ExpressionItem>> consumer) {
        return Expressions.parens(Expressions.GroupingModifier.ROLLUP, consumer);
    }


    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem cube(GroupByItem.ExpressionItem exp) {
        return Expressions.parens(Expressions.GroupingModifier.CUBE, exp);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem cube(GroupByItem.ExpressionItem exp1, GroupByItem.ExpressionItem exp2) {
        return Expressions.parens(Expressions.GroupingModifier.CUBE, exp1, exp2);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem cube(GroupByItem.ExpressionItem exp1, GroupByItem.ExpressionItem exp2,
                                   GroupByItem.ExpressionItem exp3, GroupByItem.ExpressionItem... rest) {
        return Expressions.parens(Expressions.GroupingModifier.CUBE, exp1, exp2, exp3, rest);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem cube(Consumer<Consumer<GroupByItem.ExpressionItem>> consumer) {
        return Expressions.parens(Expressions.GroupingModifier.CUBE, consumer);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem groupingSets(GroupByItem item) {
        return Expressions.parens(Expressions.GroupingModifier.GROUPING_SETS, item);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem groupingSets(GroupByItem item1, GroupByItem item2) {
        return Expressions.parens(Expressions.GroupingModifier.GROUPING_SETS, item1, item2);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem groupingSets(GroupByItem item1, GroupByItem item2, GroupByItem item3, GroupByItem... rest) {
        return Expressions.parens(Expressions.GroupingModifier.GROUPING_SETS, item1, item2, item3, rest);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">grouping_element</a>
    public static GroupByItem groupingSets(Consumer<Consumer<GroupByItem>> consumer) {
        return Expressions.parens(Expressions.GroupingModifier.GROUPING_SETS, consumer);
    }


    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">ROWS FROM</a>
    public static _TabularWithOrdinalityFunction rowsFrom(Consumer<Postgres._RowsFromSpaceClause> consumer) {
        return PostgreFunctionUtils.rowsFrom(consumer);
    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-select.html">ROWS FROM</a>
    public static _TabularWithOrdinalityFunction rowsFrom(SQLs.SymbolSpace space, Consumer<Postgres.RowFromConsumer> consumer) {
        return PostgreFunctionUtils.rowsFrom(space, consumer);
    }


    /*-------------------below package method -------------------*/

    static String keyWordToString(Enum<?> keyWordEnum) {
        return _StringUtils.builder()
                .append(Postgres.class.getSimpleName())
                .append(_Constant.DOT)
                .append(keyWordEnum.name())
                .toString();
    }


    /*-------------------below private method -------------------*/


    /// @see #atTimeZone(Expression, Expression)
    private static MappingType atTimeZoneType(final MappingType left, final MappingType right) {
        final MappingType returnType;
        if (left instanceof MappingType.SqlLocalDateTime) {
            returnType = OffsetDateTimeType.INSTANCE;
        } else if (left instanceof MappingType.SqlOffsetDateTime) {
            returnType = LocalDateTimeType.INSTANCE;
        } else if (left instanceof MappingType.SqlLocalTime) {
            returnType = OffsetTimeType.INSTANCE;
        } else if (left instanceof MappingType.SqlOffsetTime) {
            returnType = LocalTimeType.INSTANCE;
        } else {
            String m = String.format("AT TIME ZONE operator don't support %s", left);
            throw ContextStack.clearStackAndCriteriaError(m);
        }
        return returnType;
    }

    /// @see #caret(Expression, Expression)
    private static MappingType caretResultType(final MappingType left, final MappingType right) {
        final MappingType returnType;
        if (left instanceof MappingType.SqlIntegerOrDecimalType
                && right instanceof MappingType.SqlIntegerOrDecimalType) {
            returnType = BigDecimalType.INSTANCE;
        } else {
            returnType = DoubleType.INSTANCE;
        }
        return returnType;
    }


}
