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
import io.army.lang.Nullable;
import io.army.mapping.MappingType;
import io.army.mapping._MappingFactory;
import io.army.meta.FieldMeta;
import io.army.meta.TableMeta;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static io.army.dialect.Database.PostgreSQL;

/// 
/// Package class,this class is base class of {@link SQLs}.
/// @see SQLs
/// @since 0.6.0
abstract class SQLSyntax extends Functions {


    /// package constructor
    SQLSyntax() {
    }

    /// Get default {@link MappingType} of javaType,if not found,throw {@link CriteriaException}
/// @return non-null
/// @throws CriteriaException throw when not found default {@link MappingType} of javaType
    public static MappingType mappingTypeOf(final Class<?> javaType) {
        final MappingType type;
        type = _MappingFactory.getDefaultIfMatch(javaType);
        if (type == null) {
            String m = String.format("Not found default %s of %s", MappingType.class.getName(), javaType.getName());
            throw ContextStack.clearStackAndCriteriaError(m);
        }
        return type;
    }

    /// Get default {@link MappingType} of javaType,if not found,return null
/// @return nullable
    @Nullable
    public static MappingType getMappingTypeOf(final Class<?> javaType) {
        return _MappingFactory.getDefaultIfMatch(javaType);
    }

    public static Expression identifier(String identifier) {
        return NonOperationExpression.identifier(identifier);
    }


/// 
/// Value must be below types:
/// 
/// - {@link Boolean}
/// - {@link String}
/// - {@link Integer}
/// - {@link Long}
/// - {@link Short}
/// - {@link Byte}
/// - {@link Double}
/// - {@link Float}
/// - {@link java.math.BigDecimal}
/// - {@link java.math.BigInteger}
/// - {@code  byte[]}
/// - {@link BitSet}
/// - {@link io.army.struct.CodeEnum}
/// - {@link io.army.struct.TextEnum}
/// - {@link java.time.LocalTime}
/// - {@link java.time.LocalDate}
/// - {@link java.time.LocalDateTime}
/// - {@link java.time.OffsetDateTime}
/// - {@link java.time.ZonedDateTime}
/// - {@link java.time.OffsetTime}
/// - {@link java.time.ZoneId}
/// - {@link java.time.Month}
/// - {@link java.time.DayOfWeek}
/// - {@link java.time.Year}
/// - {@link java.time.YearMonth}
/// - {@link java.time.MonthDay}
/// 
/// @param value non-null
/// @return parameter expression
/// @see #literalValue(Object)
    public static ParamExpression parameter(final Object value) {
        return ArmyParamExpression.from(value);
    }


    /// 
/// Create parameter expression, parameter expression output parameter placeholder({@code ?})
/// @param value nullable,if value is instance of {@link Supplier},then {@link Supplier#get()} will be invoked.
/// @throws io.army.criteria.CriteriaException throw when infer is codec {@link FieldMeta}.
/// @see #param(TypeInfer, Object)
/// @see #literal(TypeInfer, Object)
    public static ParamExpression param(final TypeInfer type, final @Nullable Object value) {
        final ParamExpression result;
        if (value instanceof Supplier) {
            result = ArmyParamExpression.single(type, ((Supplier<?>) value).get());
        } else {
            result = ArmyParamExpression.single(type, value);
        }
        return result;
    }


/// 
/// Create named non-null parameter expression for batch update(delete) and values insert.
/// @throws CriteriaException throw when 
/// - infer is codec {@link FieldMeta}.
/// - name have no text
/// 
/// @see #namedLiteral(TypeInfer, String)
    public static ParamExpression namedParam(final TypeInfer type, final String name) {
        return ArmyParamExpression.named(type, name);
    }


/// 
/// Value must be below types:
/// 
/// - {@link Boolean}
/// - {@link String}
/// - {@link Integer}
/// - {@link Long}
/// - {@link Short}
/// - {@link Byte}
/// - {@link Double}
/// - {@link Float}
/// - {@link java.math.BigDecimal}
/// - {@link java.math.BigInteger}
/// - {@code  byte[]}
/// - {@link BitSet}
/// - {@link io.army.struct.CodeEnum}
/// - {@link io.army.struct.TextEnum}
/// - {@link java.time.LocalTime}
/// - {@link java.time.LocalDate}
/// - {@link java.time.LocalDateTime}
/// - {@link java.time.OffsetDateTime}
/// - {@link java.time.ZonedDateTime}
/// - {@link java.time.OffsetTime}
/// - {@link java.time.ZoneId}
/// - {@link java.time.Month}
/// - {@link java.time.DayOfWeek}
/// - {@link java.time.Year}
/// - {@link java.time.YearMonth}
/// - {@link java.time.MonthDay}
/// 
/// @param value non-null
/// @return literal expression
/// @see SQLs#parameter(Object)
/// @see SQLs#constValue(Object)
    public static LiteralExpression literalValue(final Object value) {
        return ArmyLiteralExpression.from(value, true);
    }


    /// 
/// Create literal expression with nonNullValue.
/// This method is similar to {@link SQLs#literalValue(Object)},except that two exceptions :
/// @param nonNullValue non-null value
/// @see SQLs#literalValue(Object)
    public static LiteralExpression constValue(final Object nonNullValue) {
        return ArmyLiteralExpression.from(nonNullValue, false);
    }


    /// 
/// Create literal expression,literal expression will output literal of value
/// @param type  non-null
/// @param value nullable,if value is instance of {@link Supplier},then {@link Supplier#get()} will invoked.
/// @see #param(TypeInfer, Object)
/// @see #literal(TypeInfer, Object)
    public static LiteralExpression literal(final TypeInfer type, final @Nullable Object value) {
        final LiteralExpression result;
        if (value instanceof Supplier) {
            result = ArmyLiteralExpression.single(type, ((Supplier<?>) value).get(), true);
        } else {
            result = ArmyLiteralExpression.single(type, value, true);
        }
        return result;
    }

    public static LiteralExpression constant(final TypeInfer type, final @Nullable Object value) {
        final LiteralExpression result;
        if (value instanceof Supplier) {
            result = ArmyLiteralExpression.single(type, ((Supplier<?>) value).get(), false);
        } else {
            result = ArmyLiteralExpression.single(type, value, false);
        }
        return result;
    }


/// 
/// Create named non-null literal expression. This expression can only be used in values insert statement.
/// 
/// Note: this method couldn't be used in batch update(delete) statement.
/// @param type non-null
/// @param name non-null and non-empty
/// @return non-null named literal expression
/// @throws CriteriaException throw when 
/// - infer is codec {@link TableField}.
/// - name have no text
/// 
/// @see #namedParam(TypeInfer, String)
    public static LiteralExpression namedLiteral(final TypeInfer type, final String name) {
        return ArmyLiteralExpression.named(type, name, true);
    }

    public static LiteralExpression namedConst(final TypeInfer type, final String name) {
        return ArmyLiteralExpression.named(type, name, false);
    }


/// Create multi parameter expression, multi parameter expression will output multi parameter placeholders like below:
/// ? , ? , ? ...
/// but as right operand of  IN(or NOT IN) operator, will output (  ? , ? , ? ... )
/// @param type   non-null,the type of element of values.
/// @param values non-null and non-empty
/// @throws CriteriaException throw when 
/// - values is empty
/// - infer return codec {@link TableField}
/// 
/// @see #rowLiteral(TypeInfer, Collection)
    public static RowParamExpression rowParam(final TypeInfer type, final Collection<?> values) {
        return ArmyRowParamExpression.multi(type, values);
    }

    /// 
/// Create multi literal expression, multi literal expression will output multi LITERAL like below:
/// LITERAL , LITERAL , LITERAL ...
/// but as right operand of  IN(or NOT IN) operator, will output (  LITERAL , LITERAL , LITERAL ... )
/// @param type   non-null,the type of element of values.
/// @param values non-null and non-empty
/// @see #rowParam(TypeInfer, Collection)
    public static RowLiteralExpression rowLiteral(final TypeInfer type, final Collection<?> values) {
        return ArmyRowLiteralExpression.multi(type, values, true);
    }

    public static RowLiteralExpression rowConst(final TypeInfer type, final Collection<?> values) {
        return ArmyRowLiteralExpression.multi(type, values, false);
    }

/// 
/// Create named non-null multi parameter expression, multi parameter expression will output multi parameter placeholders like below:
/// ? , ? , ? ...
/// but as right operand of  IN(or NOT IN) operator, will output (  ? , ? , ? ... )
/// 
/// Named multi parameter expression is used in batch update(or delete) and values insert.
/// @param type non-null,the type of element of {@link Collection}
/// @param name non-null,the key name of {@link Map} or the field name of java bean.
/// @param size positive,the size of {@link Collection}
/// @return named non-null multi parameter expression
/// @throws CriteriaException throw when 
/// - name have no text
/// - size less than 1
/// - infer return codec {@link TableField}
/// 
/// @see #namedRowLiteral(TypeInfer, String)
    public static RowParamExpression namedRowParam(final TypeInfer type, final String name, final int size) {
        return ArmyRowParamExpression.named(type, name, size);
    }

    /// 
/// Create named non-null multi literal expression, multi literal expression will output multi LITERAL like below:
/// LITERAL , LITERAL , LITERAL ...
/// but as right operand of  IN(or NOT IN) operator, will output (  LITERAL , LITERAL , LITERAL ... )
/// 
/// This expression can only be used in values insert statement,this method couldn't be used in batch update(delete) statement.
/// @param type non-null,the type of element of {@link Collection}
/// @param name non-null,the key name of {@link Map} or the field name of java bean.
/// @return named non-null multi literal expression
/// @see #namedRowParam(TypeInfer, String, int)
    public static RowLiteralExpression namedRowLiteral(final TypeInfer type, final String name) {
        return ArmyRowLiteralExpression.named(type, name, true);
    }

    public static RowLiteralExpression namedRowConst(final TypeInfer type, final String name) {
        return ArmyRowLiteralExpression.named(type, name, false);
    }

    @Support({PostgreSQL})
    public static RowExpression row() {
        return RowExpressions.row(List.of());
    }

    public static RowExpression row(SubQuery subQuery) {
        ContextStack.assertNonNull(subQuery);
        return RowExpressions.row(List.of(subQuery));
    }


    ///
    /// @see #space(String, SQLs.SymbolDot, TableMeta)
    /// @see #space(String, SQLs.SymbolDot, SQLs.SymbolAsterisk)
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/row-constructor-optimization.html">MySQL Row Constructor Expression Optimization</a>
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/range-optimization.html#row-constructor-range-optimization">Range Optimization of Row Constructor Expressions</a>
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/row-subqueries.html">MySQL Row Subqueries</a>
    /// @see <a href="https://www.postgresql.org/docs/current/sql-expressions.html#SQL-SYNTAX-ROW-CONSTRUCTORS">Row Constructors</a>
    /// @see <a href="https://www.postgresql.org/docs/current/functions-comparisons.html#ROW-WISE-COMPARISON">Row Constructor Comparison</a>
    public static RowExpression row(List<?> columnList) {
        ContextStack.assertNonNull(columnList);
        return RowExpressions.row(columnList);
    }

    /// @see #row(List)
    public static RowElement space(String derivedAlias, SQLs.SymbolDot period,
                                   SQLs.SymbolAsterisk asterisk) {
        return ContextStack.peek().row(derivedAlias, period, asterisk);
    }

    ///@see #row(List)
    public static RowElement space(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table) {
        return ContextStack.peek().row(tableAlias, period, table); // register derived row
    }


/// 
/// Get a {@link QualifiedField}. You don't need a {@link QualifiedField},if no self-join in statement.
/// @throws CriteriaException throw when
/// - current statement don't support this method,eg: single-table UPDATE statement
/// - qualified field don't exists,here always is deferred,because army validate qualified field when statement end.
/// 
    public static <T> QualifiedField<T> field(String tableAlias, FieldMeta<T> field) {
        return ContextStack.peek().field(tableAlias, field);
    }

    /// 
/// Reference a derived field from current statement.
/// @param derivedAlias   derived table alias,
/// @param selectionAlias derived field alias
/// @throws CriteriaException            throw when current statement don't support derived field (eg: single-table UPDATE statement).
/// @throws UnknownDerivedFieldException throw when derived filed is unknown.
    public static DerivedField refField(String derivedAlias, String selectionAlias) {
        return ContextStack.peek().refField(derivedAlias, selectionAlias);
    }

    /// 
/// Reference a derived field from current statement.
/// @param derivedAlias   derived table alias,
/// @param selectionAlias derived field alias
/// @throws CriteriaException            throw when current statement don't support derived field (eg: single-table UPDATE statement).
/// @throws UnknownDerivedFieldException throw when derived filed is unknown.
    public static TypedDerivedField refTypedField(String derivedAlias, String selectionAlias) {
        final DerivedField field;
        field = ContextStack.peek().refField(derivedAlias, selectionAlias);
        if (!(field instanceof TypedDerivedField)) {
            String m = String.format("derived field(%s.%s) isn't %s", derivedAlias, selectionAlias,
                    TypedDerivedField.class.getName());
            throw ContextStack.clearStackAndCriteriaError(m);
        }
        return (TypedDerivedField) field;
    }


/// 
/// Reference a {@link  Selection} of current statement ,eg: ORDER BY clause.
/// The {@link Expression} returned don't support {@link Expression#as(String)} method.
/// 
/// **NOTE** : override,if selection alias duplication.
/// @throws CriteriaException then when 
/// - current statement don't support this method,eg: UPDATE statement
/// - the {@link Selection} not exists,here possibly is deferred,if you invoke this method before SELECT clause end. eg: postgre DISTINCT ON clause
/// 
    public static Expression refSelection(String selectionAlias) {
        return ContextStack.peek().refSelection(selectionAlias);
    }

/// 
/// Reference a {@link  Selection} of current statement ,eg: ORDER BY clause.
/// The {@link Expression} returned don't support {@link Expression#as(String)} method.
/// @param selectionOrdinal based 1 .
/// @throws CriteriaException throw when
/// - selectionOrdinal less than 1
/// - the {@link Selection} not exists,here possibly is deferred,if you invoke this method before SELECT clause end. eg: postgre DISTINCT ON clause
/// - current statement don't support this method,eg: UPDATE statement
/// 
    public static Expression refSelection(int selectionOrdinal) {
        return ContextStack.peek().refSelection(selectionOrdinal);
    }


    public static Expression parens(Expression expression) {
        return OperationExpression.bracketExp(expression);
    }

    public static SimplePredicate bracket(IPredicate predicate) {
        return OperationPredicate.bracketPredicate(predicate);
    }

    public static SimpleExpression bitwiseNot(Expression exp) {
        return Expressions.unaryExp(UnaryExpOperator.BITWISE_NOT, exp);
    }

    public static SimpleExpression negate(Expression exp) {
        return Expressions.unaryExp(UnaryExpOperator.NEGATE, exp);
    }

    public static IPredicate not(IPredicate predicate) {
        return OperationPredicate.notPredicate(predicate);
    }



    /*################################## blow sql key word operate method ##################################*/

    /// @param subQuery non-null
    public static IPredicate exists(SubQuery subQuery) {
        return Expressions.existsPredicate(false, subQuery);
    }

    /// @param subQuery non-null
    public static IPredicate notExists(SubQuery subQuery) {
        return Expressions.existsPredicate(true, subQuery);
    }

    public static ItemPair plusEqual(final SqlField field, final Object value) {
        return SQLs._itemPair(field, AssignOperator.PLUS_EQUAL, value);
    }

    public static ItemPair minusEqual(final SqlField field, final Object value) {
        return SQLs._itemPair(field, AssignOperator.MINUS_EQUAL, value);
    }

    public static ItemPair timesEqual(final SqlField field, final Object value) {
        return SQLs._itemPair(field, AssignOperator.TIMES_EQUAL, value);
    }

    public static ItemPair divideEqual(final SqlField field, final Object value) {
        return SQLs._itemPair(field, AssignOperator.DIVIDE_EQUAL, value);
    }

    public static ItemPair modeEqual(final SqlField field, final Object value) {
        return SQLs._itemPair(field, AssignOperator.MODE_EQUAL, value);
    }


//    public static <I extends Item, R extends Expression> SQLFunction._CaseFuncWhenClause<R> Case(
//            Function<_ItemExpression<I>, R> endFunc, Function<TypeInfer, I> asFunc) {
//        return FunctionUtils.caseFunction(null, endFunc, asFunc);
//    }




    /*-------------------below package method -------------------*/



    /*-------------------below private method-------------------*/


}
