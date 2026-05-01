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

import io.army.criteria.dialect.BatchReturningDelete;
import io.army.criteria.dialect.BatchReturningUpdate;
import io.army.criteria.impl.SQLs;
import io.army.dialect.Dialect;
import io.army.function.BetweenDualOperator;
import io.army.function.BetweenValueOperator;
import io.army.function.ExpressionOperator;
import io.army.function.TeFunction;
import io.army.lang.Nullable;
import io.army.mapping.MappingType;
import io.army.meta.TableMeta;
import io.army.stmt.Stmt;

import java.util.List;
import java.util.function.*;

/// 
/// This interface representing sql statement,this interface is base interface of below:
/// 
/// - {@link Select}
/// - {@link InsertStatement}
/// - {@link UpdateStatement}
/// - {@link DeleteStatement}
/// - {@link SubQuery}
/// - {@link Values}
/// 
/// * @since 0.6.0
public interface Statement extends Item {

    /// assert statement prepared
    void prepared();

    boolean isPrepared();


    interface StatementMockSpec {

        /// @param none nothing
        String mockAsString(Dialect dialect, Visible visible, boolean none);

        Stmt mockAsStmt(Dialect dialect, Visible visible);

    }


    interface DmlStatementSpec {

    }

    interface JoinBuilder {

    }


    interface _ElementCommaClause {

        _ElementCommaClause comma(SQLExpression exp);

        _ElementCommaClause comma(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table);

        _ElementCommaClause comma(String derivedAlias, SQLs.SymbolDot period, SQLs.SymbolAsterisk asterisk);

    }

    interface _ElementSpaceClause {

        _ElementCommaClause space(SQLExpression exp);

        _ElementCommaClause space(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table);

        _ElementCommaClause space(String derivedAlias, SQLs.SymbolDot period, SQLs.SymbolAsterisk asterisk);

    }

    interface _ElementConsumer {

        _ElementConsumer accept(SQLExpression exp);

        _ElementConsumer accept(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table);

        _ElementConsumer accept(String derivedAlias, SQLs.SymbolDot period, SQLs.SymbolAsterisk asterisk);

    }

    @Deprecated
    interface _ElementObjectCommaClause {

        _ElementObjectCommaClause comma(String keName, SQLExpression exp);

        _ElementObjectCommaClause comma(Expression key, SQLExpression exp);

        _ElementObjectCommaClause comma(String keName, String derivedAlias, SQLs.SymbolDot period, SQLs.SymbolAsterisk asterisk);

        _ElementObjectCommaClause comma(Expression key, String derivedAlias, SQLs.SymbolDot period, SQLs.SymbolAsterisk asterisk);
    }


    @Deprecated
    interface _ElementObjectSpaceClause {

        _ElementObjectCommaClause space(String keName, SQLExpression exp);

        _ElementObjectCommaClause space(Expression key, SQLExpression exp);

        _ElementObjectCommaClause space(String keName, String derivedAlias, SQLs.SymbolDot period, SQLs.SymbolAsterisk asterisk);

        _ElementObjectCommaClause space(Expression key, String derivedAlias, SQLs.SymbolDot period, SQLs.SymbolAsterisk asterisk);
    }


    interface _StringObjectCommaClause {

        _StringObjectCommaClause comma(String key, String value);
    }

    interface _StringObjectSpaceClause {

        _StringObjectCommaClause space(String key, String value);
    }

    interface _StringObjectConsumer {

        _StringObjectConsumer accept(String key, String value);
    }


/// 
/// This interface representing AS clause.
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @param <AR> next clause java type
/// @since 0.6.0
    interface _AsClause<AR> extends Item {

        AR as(String alias);
    }

    interface _StaticAsClaus<AR> extends Item {

        AR as();
    }

    interface _StaticBetweenClause<BR> {

        BR between();
    }

    interface _StaticAndClause<AR> extends Item {

        AR and();
    }


/// 
/// This interface representing bind params clause for batch update(delete).
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @param <R> next clause java type
/// @since 0.6.0
    interface _BatchParamClause<R extends Item> extends Item {

        R namedParamList(List<?> paramList);
    }

    interface _BatchUpdateParamSpec extends _BatchParamClause<BatchUpdate> {

    }

    interface _BatchDeleteParamSpec extends _BatchParamClause<BatchDelete> {

    }

    interface _BatchReturningUpdateParamSpec extends _BatchParamClause<BatchReturningUpdate> {

    }

    interface _BatchReturningDeleteParamSpec extends _BatchParamClause<BatchReturningDelete> {

    }

    interface _BatchSelectParamSpec extends _BatchParamClause<BatchSelect> {

    }


    interface _LeftParenNestedClause<T extends Item, R extends Item> {

        R leftParen(Function<T, R> function);
    }

/// 
/// This interface representing RIGHT BRACKET clause in join expression.
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @param <RR> next clause java type
/// @since 0.6.0
    interface _RightParenClause<RR> extends Item {

        RR rightParen();

    }


/// 
/// This interface representing ON clause.
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @param <OR> next clause java type
/// @since 0.6.0
    interface _OnClause<OR> extends Item {

        OR on(IPredicate predicate);

        OR on(IPredicate predicate1, IPredicate predicate2);

        OR on(Function<Expression, IPredicate> operator, Expression operandField);

        OR on(Function<Expression, IPredicate> operator1, Expression operandField1
                , Function<Expression, IPredicate> operator2, Expression operandField2);

        OR on(Consumer<Consumer<IPredicate>> consumer);


    }


/// 
/// This interface representing FROM clause.
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @param <FT> next clause java type
/// @param <FS> next clause java type
/// @since 0.6.0
    interface _FromClause<FT, FS> extends Item {

        FT from(TableMeta<?> table, SQLs.WordAs as, String tableAlias);

        FS from(DerivedTable derivedTable);

        <T extends DerivedTable> FS from(Supplier<T> supplier);

    }

    interface _FromModifierTabularClause<FT, FS> extends _FromClause<FT, FS> {

        FS from(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable);

        <T extends DerivedTable> FS from(@Nullable SQLs.DerivedModifier modifier, Supplier<T> supplier);
    }

    interface _FromModifierClause<FT, FS> extends _FromModifierTabularClause<FT, FS> {

        FT from(@Nullable SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);

    }

    interface _FromNestedClause<T extends Item, R extends Item> {

        R from(Function<T, R> function);

    }


/// 
/// This interface representing dialect FROM clause.
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @param <R> same with the FS of {@link _FromClause}
/// @see _FromClause
/// @since 0.6.0
    interface _FromCteClause<R> {

        R from(String cteName);

        R from(String cteName, SQLs.WordAs wordAs, String alias);

    }

    interface _FromModifierCteClause<R> extends _FromCteClause<R> {

        R from(SQLs.DerivedModifier modifier, String cteName);

        R from(SQLs.DerivedModifier modifier, String cteName, SQLs.WordAs wordAs, String alias);
    }


    interface _FromUndoneFunctionClause<R> {

        R from(UndoneFunction func);
    }

    interface _FromModifierUndoneFunctionClause<R> extends _FromUndoneFunctionClause<R> {

        R from(@Nullable SQLs.DerivedModifier modifier, UndoneFunction func);
    }


/// 
/// This interface representing FROM clause.
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @param <FT> next clause java type
/// @param <FS> next clause java type
/// @since 0.6.0
    interface _UsingItemClause<FT, FS> extends Item {

        FT using(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);

        FS using(DerivedTable derivedTable);

        <T extends DerivedTable> FS using(Supplier<T> supplier);

    }

    interface _UsingModifierTabularClause<FT, FS> extends _UsingItemClause<FT, FS> {

        FS using(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable);

        <T extends DerivedTable> FS using(@Nullable SQLs.DerivedModifier modifier, Supplier<T> supplier);
    }

    interface _UsingModifierClause<FT, FS> extends _UsingModifierTabularClause<FT, FS> {

        FT using(@Nullable SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);

    }

    interface _UsingNestedClause<T extends Item, R extends Item> {

        R using(Function<T, R> function);

    }

/// 
/// This interface representing dialect FROM clause.
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @param <FC> same with the FS of {@link _FromClause}
/// @see _FromClause
/// @since 0.6.0
    interface _UsingCteClause<FC> {

        FC using(String cteName);

        FC using(String cteName, SQLs.WordAs wordAs, String alias);

    }

    interface _UsingModifierCteClause<FC> extends _UsingCteClause<FC> {

        FC using(SQLs.DerivedModifier modifier, String cteName);

        FC using(SQLs.DerivedModifier modifier, String cteName, SQLs.WordAs wordAs, String alias);
    }


    interface _UsingUndoneFunctionClause<R> {

        R using(UndoneFunction function);
    }

    interface _UsingModifierUndoneFunctionClause<R> extends _UsingUndoneFunctionClause<R> {

        R using(@Nullable SQLs.DerivedModifier modifier, UndoneFunction function);
    }


/// 
/// This interface representing JOIN clause.
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @param <JT> next clause java type,it's sub interface of {@link _OnClause}
/// @param <JS> next clause java type,it's sub interface of {@link _OnClause}
/// @see _CrossJoinClause
/// @since 0.6.0
    interface _JoinClause<JT, JS> extends Item {

        JT leftJoin(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);

        JS leftJoin(DerivedTable derivedTable);

        <T extends DerivedTable> JS leftJoin(Supplier<T> supplier);

        JT join(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);

        JS join(DerivedTable derivedTable);

        <T extends DerivedTable> JS join(Supplier<T> supplier);

        JT rightJoin(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);

        JS rightJoin(DerivedTable derivedTable);


        <T extends DerivedTable> JS rightJoin(Supplier<T> supplier);

        JT fullJoin(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);

        JS fullJoin(DerivedTable derivedTable);

        <T extends DerivedTable> JS fullJoin(Supplier<T> supplier);

    }


    interface _JoinModifierTabularClause<JT, JS> extends _JoinClause<JT, JS> {

        JS leftJoin(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable);

        <T extends DerivedTable> JS leftJoin(@Nullable SQLs.DerivedModifier modifier, Supplier<T> supplier);

        JS join(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable);

        <T extends DerivedTable> JS join(@Nullable SQLs.DerivedModifier modifier, Supplier<T> supplier);

        JS rightJoin(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable);

        <T extends DerivedTable> JS rightJoin(@Nullable SQLs.DerivedModifier modifier, Supplier<T> supplier);

        JS fullJoin(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable);

        <T extends DerivedTable> JS fullJoin(@Nullable SQLs.DerivedModifier modifier, Supplier<T> supplier);
    }

    interface _JoinModifierClause<JT, JS> extends _JoinModifierTabularClause<JT, JS> {

        JT leftJoin(@Nullable SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);

        JT join(@Nullable SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);

        JT rightJoin(@Nullable SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);

        JT fullJoin(@Nullable SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);

    }


    interface _JoinUndoneFunctionClause<R> {

        R leftJoin(UndoneFunction func);

        R join(UndoneFunction func);

        R rightJoin(UndoneFunction func);

        R fullJoin(UndoneFunction func);
    }

    interface _JoinModifierUndoneFunctionClause<R> extends _JoinUndoneFunctionClause<R> {

        R leftJoin(@Nullable SQLs.DerivedModifier modifier, UndoneFunction func);

        R join(@Nullable SQLs.DerivedModifier modifier, UndoneFunction func);

        R rightJoin(@Nullable SQLs.DerivedModifier modifier, UndoneFunction func);

        R fullJoin(@Nullable SQLs.DerivedModifier modifier, UndoneFunction func);

    }


/// 
/// This interface representing CROSS JOIN clause.
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @since 0.6.0
    interface _CrossJoinClause<FT, FS> {

        FT crossJoin(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);

        FS crossJoin(DerivedTable derivedTable);

        <T extends DerivedTable> FS crossJoin(Supplier<T> supplier);

    }

    interface _CrossJoinModifierTabularClause<FT, FS> extends _CrossJoinClause<FT, FS> {

        FS crossJoin(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable);

        <T extends DerivedTable> FS crossJoin(@Nullable SQLs.DerivedModifier modifier, Supplier<T> supplier);
    }

    interface _CrossJoinModifierClause<FT, FS> extends _CrossJoinModifierTabularClause<FT, FS> {

        FT crossJoin(@Nullable SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);


    }

    interface _CrossUndoneFunctionClause<R> {

        R crossJoin(UndoneFunction func);
    }


    interface _CrossModifierUndoneFunctionClause<R> extends _CrossUndoneFunctionClause<R> {

        R crossJoin(@Nullable SQLs.DerivedModifier modifier, UndoneFunction func);

    }

    interface _JoinNestedClause<T extends Item, R extends Item> {

        R leftJoin(Function<T, R> function);

        R join(Function<T, R> function);

        R rightJoin(Function<T, R> function);

        R fullJoin(Function<T, R> function);

    }

    interface _StraightJoinNestedClause<T extends Item, R extends Item> {

        R straightJoin(Function<T, R> function);
    }

    interface _CrossJoinNestedClause<T extends Item, R extends Item> {

        R crossJoin(Function<T, R> function);
    }


    interface _DynamicJoinClause<B extends JoinBuilder, JD> {

        JD ifLeftJoin(Consumer<B> consumer);

        JD ifJoin(Consumer<B> consumer);

        JD ifRightJoin(Consumer<B> consumer);

        JD ifFullJoin(Consumer<B> consumer);
    }

    interface _DynamicCrossJoinClause<B extends JoinBuilder, JD> {

        JD ifCrossJoin(Consumer<B> consumer);
    }

    interface _DynamicStraightJoinClause<B extends JoinBuilder, JD> {

        JD ifStraightJoin(Consumer<B> consumer);
    }


/// 
/// This interface representing JOIN CTE clause.
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @param <JC> same with the JS of {@link _JoinClause}
/// @since 0.6.0
    interface _JoinCteClause<JC> {

        JC leftJoin(String cteName);

        JC leftJoin(String cteName, SQLs.WordAs wordAs, String alias);

        JC join(String cteName);

        JC join(String cteName, SQLs.WordAs wordAs, String alias);

        JC rightJoin(String cteName);

        JC rightJoin(String cteName, SQLs.WordAs wordAs, String alias);

        JC fullJoin(String cteName);

        JC fullJoin(String cteName, SQLs.WordAs wordAs, String alias);

    }

    interface _JoinModifierCteClause<JC> extends _JoinCteClause<JC> {

        JC leftJoin(SQLs.DerivedModifier modifier, String cteName);

        JC leftJoin(SQLs.DerivedModifier modifier, String cteName, SQLs.WordAs wordAs, String alias);

        JC join(SQLs.DerivedModifier modifier, String cteName);

        JC join(SQLs.DerivedModifier modifier, String cteName, SQLs.WordAs wordAs, String alias);

        JC rightJoin(SQLs.DerivedModifier modifier, String cteName);

        JC rightJoin(SQLs.DerivedModifier modifier, String cteName, SQLs.WordAs wordAs, String alias);

        JC fullJoin(SQLs.DerivedModifier modifier, String cteName);

        JC fullJoin(SQLs.DerivedModifier modifier, String cteName, SQLs.WordAs wordAs, String alias);

    }

    interface _CrossJoinCteClause<FC> {

        FC crossJoin(String cteName);

        FC crossJoin(String cteName, SQLs.WordAs wordAs, String alias);

    }

    interface _CrossJoinModifierCteClause<FC> extends _CrossJoinCteClause<FC> {

        FC crossJoin(SQLs.DerivedModifier modifier, String cteName);

        FC crossJoin(SQLs.DerivedModifier modifier, String cteName, SQLs.WordAs wordAs, String alias);

    }


/// 
/// This interface representing a left bracket clause after key word 'FROM' or key word 'JOIN'.
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @param <LT> next clause java type
/// @param <LS> next clause java type
/// @since 0.6.0
    interface _NestedLeftParenClause<LT, LS> extends Item {

        LT leftParen(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);

        LS leftParen(DerivedTable derivedTable);

        <T extends DerivedTable> LS leftParen(Supplier<T> supplier);

    }

    interface _NestedLeftParenModifierTabularClause<LT, LS> extends _NestedLeftParenClause<LT, LS> {

        LS leftParen(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable);

        <T extends DerivedTable> LS leftParen(@Nullable SQLs.DerivedModifier modifier, Supplier<T> supplier);

    }

    interface _NestedLeftParenModifierClause<LT, LS> extends _NestedLeftParenModifierTabularClause<LT, LS> {

        LT leftParen(@Nullable SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);

    }

    interface _NestedLeftParenUndoneFunctionClause<R> {

        R leftParen(UndoneFunction func);
    }

    interface _NestedLeftParenModifierUndoneFunctionClause<R> extends _NestedLeftParenUndoneFunctionClause<R> {

        R leftParen(@Nullable SQLs.DerivedModifier modifier, UndoneFunction func);
    }


    interface _NestedTableLeftParenClause<LP> {

        LP leftParen(TableMeta<?> table);

    }


    interface _DynamicTabularItemClause<FT, FS> {

        FT space(TableMeta<?> table, SQLs.WordAs wordAs, String alias);

        FS space(DerivedTable derivedTable);

        <T extends DerivedTable> FS space(Supplier<T> supplier);

    }

    interface _DynamicTabularDerivedModifierClause<FT, FS> extends _DynamicTabularItemClause<FT, FS> {

        FS space(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable);

        <T extends DerivedTable> FS space(@Nullable SQLs.DerivedModifier modifier, Supplier<T> supplier);

    }


    interface _DynamicTabularModifierClause<FT, FS> extends _DynamicTabularDerivedModifierClause<FT, FS> {


        FT space(@Nullable SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs wordAs, String alias);

    }

    interface _DynamicTabularUndoneFunctionClause<R> {

        R space(UndoneFunction func);
    }

    interface _DynamicTabularModifierUndoneFunctionClause<R> extends _DynamicTabularUndoneFunctionClause<R> {

        R space(@Nullable SQLs.DerivedModifier modifier, UndoneFunction func);
    }


    interface _DynamicTabularCteClause<R extends Item> {

        R space(String cteName);

        R space(String cteName, SQLs.WordAs as, String alias);

    }

    interface _DynamicTabularNestedClause<T extends Item, R extends Item> {

        R space(Function<T, R> function);
    }


    interface _MinWhereClause<WR, WA> {

        WR where(Consumer<Consumer<IPredicate>> consumer);

        WA where(IPredicate predicate);

        WA whereIf(Supplier<IPredicate> supplier);

    }

    interface _MinQueryWhereClause<WR, WA> extends _MinWhereClause<WR, WA> {

        WR ifWhere(Consumer<Consumer<IPredicate>> consumer);
    }

/// 
/// This interface representing WHERE clause.
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @param <WR> next clause java type
/// @param <WA> next clause java type
/// @since 0.6.0
    interface _WhereClause<WR, WA> extends _MinWhereClause<WR, WA> {

        <T> WA where(Function<T, IPredicate> expOperator, T operand);

        <T> WA whereIf(Function<T, IPredicate> expOperator, @Nullable T value);

        <T> WA whereIf(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
                       BiFunction<TypedExpression, T, Expression> operator, @Nullable T value);

        /// @param expOperator see {@link TypedExpression#space(SQLs.BiOperator, Object)}
        <T> WA whereIf(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator, SQLs.BiOperator operator, @Nullable T value);

        /// @param expOperator see {@link TypedExpression#space(SQLs.BiOperator, BiFunction, Object)}
        <T> WA whereIf(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator, SQLs.BiOperator operator,
                       BiFunction<TypedExpression, T, Expression> func, @Nullable T value);


        <T> WA whereIf(BetweenValueOperator<T> expOperator, BiFunction<TypedExpression, T, Expression> operator,
                       @Nullable T value1, SQLs.WordAnd and, @Nullable T value2);

        <T, U> WA whereIf(BetweenDualOperator<T, U> expOperator, BiFunction<TypedExpression, T, Expression> firstFuncRef,
                          @Nullable T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef,
                          @Nullable U second);

    }

/// 
/// This interface representing WHERE clause in SELECT statement.
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @param <WR> next clause java type
/// @param <WA> next clause java type
/// @since 0.6.0
    interface _QueryWhereClause<WR, WA> extends _WhereClause<WR, WA>, _MinQueryWhereClause<WR, WA> {

    }


    interface _MinWhereAndClause<WA> {

        WA and(IPredicate predicate);

        WA ifAnd(Supplier<IPredicate> supplier);

    }


/// 
/// This interface representing AND clause.
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @param <WA> next clause java type
/// @since 0.6.0
    interface _WhereAndClause<WA> extends _MinWhereAndClause<WA> {

        <T> WA and(Function<T, IPredicate> expOperator, T operand);

        <T> WA ifAnd(Function<T, IPredicate> expOperator, @Nullable T value);

        <T> WA ifAnd(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
                     BiFunction<TypedExpression, T, Expression> operator, @Nullable T value);

        /// @param expOperator see {@link TypedExpression#space(SQLs.BiOperator, Object)}
        <T> WA ifAnd(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator, SQLs.BiOperator operator, @Nullable T value);

        /// @param expOperator see {@link TypedExpression#space(SQLs.BiOperator, BiFunction, Object)}
        <T> WA ifAnd(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator, SQLs.BiOperator operator,
                     BiFunction<TypedExpression, T, Expression> func, @Nullable T value);


        <T> WA ifAnd(BetweenValueOperator<T> expOperator, BiFunction<TypedExpression, T, Expression> operator,
                     @Nullable T value1, SQLs.WordAnd and, @Nullable T value2);

        <T, U> WA ifAnd(BetweenDualOperator<T, U> expOperator, BiFunction<TypedExpression, T, Expression> firstFuncRef,
                        @Nullable T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef,
                        @Nullable U second);

    }

    interface _SimpleWhereAndClause extends _WhereAndClause<_SimpleWhereAndClause> {

    }

    interface _SimpleWhereClause extends _WhereClause<Item, _SimpleWhereAndClause> {

    }


    interface _OrderByCommaClause<R> {

        R spaceComma(SortItem sortItem);

        R spaceComma(SortItem sortItem1, SortItem sortItem2);

        R spaceComma(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3);

        R spaceComma(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3, SortItem sortItem4);
    }

    interface _StaticOrderByClause<R> extends Item {

        R orderBy(SortItem sortItem);

        R orderBy(SortItem sortItem1, SortItem sortItem2);

        R orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3);

        R orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3, SortItem sortItem4);


    }


    interface _DynamicOrderByClause<R> {

        R orderBy(Consumer<Consumer<SortItem>> consumer);

        R ifOrderBy(Consumer<Consumer<SortItem>> consumer);
    }

    interface _SimpleOrderByCommaClause extends _OrderByCommaClause<_SimpleOrderByCommaClause> {

    }

    interface _SimpleOrderByClause extends _StaticOrderByClause<_SimpleOrderByCommaClause>,
            _DynamicOrderByClause<Item> {

    }


    @Deprecated
    interface _DynamicOrderByClause0<B, R> {

        R orderBy(Consumer<B> consumer);

        R ifOrderBy(Consumer<B> consumer);
    }


/// 
/// This interface representing row count limit clause.
/// * 
/// **Note:**
/// Application developer isn't allowed to directly use this interface,so you couldn't declare this interface type variable
/// ,because army don't guarantee compatibility to future distribution.
/// *
/// @since 0.6.0
    interface _RowCountLimitClause<R> extends Item {

        R limit(Expression rowCount);

        R limit(BiFunction<MappingType, Number, Expression> operator, long rowCount);

        <N extends Number> R limit(BiFunction<MappingType, Number, Expression> operator, Supplier<N> supplier);

        R limit(BiFunction<MappingType, Number, Expression> operator, Function<String, ?> function, String keyName);

        <N extends Number> R ifLimit(BiFunction<MappingType, Number, Expression> operator, Supplier<N> supplier);

        R ifLimit(BiFunction<MappingType, Number, Expression> operator, Function<String, ?> function, String keyName);

        R ifLimit(Supplier<Expression> supplier);

    }


    interface _DmlRowCountLimitClause<LR> extends _RowCountLimitClause<LR> {

        LR limit(BiFunction<MappingType, String, Expression> operator, String paramName);

        LR ifLimit(BiFunction<MappingType, String, Expression> operator, @Nullable String paramName);
    }

    interface _RowCountLimitAllClause<LR> extends _RowCountLimitClause<LR> {

        LR limitAll();

        LR ifLimitAll(BooleanSupplier supplier);

    }


    interface _QueryLimitOffsetClause<R> {

        R offset(Expression start);


/// @param operator the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param start    non-negative integer
        R offset(BiFunction<MappingType, Number, Expression> operator, long start);


/// @param operator the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param supplier return non-negative integer
        <N extends Number> R offset(BiFunction<MappingType, Number, Expression> operator, Supplier<N> supplier);

/// @param operator the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param function {@link Function#apply(Object)} return non-negative integer
/// @param keyName  keyName that is passed to function
        R offset(BiFunction<MappingType, Object, Expression> operator, Function<String, ?> function, String keyName);

/// @param operator the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param start    non-negative integer
        R ifOffset(BiFunction<MappingType, Number, Expression> operator, @Nullable Number start);

/// @param operator the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param supplier return non-negative integer
        <N extends Number> R ifOffset(BiFunction<MappingType, Number, Expression> operator, Supplier<N> supplier);

/// @param operator the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param function {@link Function#apply(Object)} return non-negative integer
/// @param keyName  keyName that is passed to function
        R ifOffset(BiFunction<MappingType, Object, Expression> operator, Function<String, ?> function, String keyName);

    }


    interface _QueryFetchOffsetClause<R> {

        R offset(Expression start, SQLs.FetchRow row);


/// @param operator the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param start    non-negative integer
/// @param row      {@link SQLs#ROW} or {@link SQLs#ROWS}
        R offset(BiFunction<MappingType, Number, Expression> operator, long start, SQLs.FetchRow row);


/// @param operator the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param supplier return non-negative integer
/// @param row      {@link SQLs#ROW} or {@link SQLs#ROWS}
        <N extends Number> R offset(BiFunction<MappingType, Number, Expression> operator, Supplier<N> supplier,
                                    SQLs.FetchRow row);

/// @param operator the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param function {@link Function#apply(Object)} return non-negative integer
/// @param keyName  keyName that is passed to function
/// @param row      {@link SQLs#ROW} or {@link SQLs#ROWS}
        R offset(BiFunction<MappingType, Object, Expression> operator, Function<String, ?> function, String keyName, SQLs.FetchRow row);

/// @param operator the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param start    non-negative integer
/// @param row      {@link SQLs#ROW} or {@link SQLs#ROWS}
        R ifOffset(BiFunction<MappingType, Number, Expression> operator, @Nullable Number start, SQLs.FetchRow row);

/// @param operator the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param supplier return non-negative integer
/// @param row      {@link SQLs#ROW} or {@link SQLs#ROWS}
        <N extends Number> R ifOffset(BiFunction<MappingType, Number, Expression> operator, Supplier<N> supplier, SQLs.FetchRow row);

/// @param operator the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param function {@link Function#apply(Object)} return non-negative integer
/// @param keyName  keyName that is passed to function
/// @param row      {@link SQLs#ROW} or {@link SQLs#ROWS}
        R ifOffset(BiFunction<MappingType, Object, Expression> operator, Function<String, ?> function, String keyName, SQLs.FetchRow row);

    }

    interface _QueryFetchClause<R> {

        /// @param firstOrNext  {@link SQLs#FIRST} or {@link SQLs#NEXT}
/// @param count        non-negative
/// @param row          {@link SQLs#ROW} or {@link SQLs#ROWS}
/// @param onlyWithTies {@link SQLs#ONLY} or {@link SQLs#WITH_TIES}
        R fetch(SQLs.FetchFirstNext firstOrNext, Expression count, SQLs.FetchRow row
                , SQLs.FetchOnlyWithTies onlyWithTies);


/// @param firstOrNext  {@link SQLs#FIRST} or {@link SQLs#NEXT}
/// @param operator     the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param count        non-negative
/// @param row          {@link SQLs#ROW} or {@link SQLs#ROWS}
/// @param onlyWithTies {@link SQLs#ONLY} or {@link SQLs#WITH_TIES}
        R fetch(SQLs.FetchFirstNext firstOrNext, BiFunction<MappingType, Number, Expression> operator
                , long count, SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies);

/// @param firstOrNext  {@link SQLs#FIRST} or {@link SQLs#NEXT}
/// @param operator     the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param supplier     return non-negative integer
/// @param row          {@link SQLs#ROW} or {@link SQLs#ROWS}
/// @param onlyWithTies {@link SQLs#ONLY} or {@link SQLs#WITH_TIES}
        <N extends Number> R fetch(SQLs.FetchFirstNext firstOrNext, BiFunction<MappingType, Number, Expression> operator
                , Supplier<N> supplier, SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies);

/// @param firstOrNext  {@link SQLs#FIRST} or {@link SQLs#NEXT}
/// @param operator     the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param function     {@link Function#apply(Object)} return non-negative integer
/// @param keyName      keyName that is passed to function
/// @param row          {@link SQLs#ROW} or {@link SQLs#ROWS}
/// @param onlyWithTies {@link SQLs#ONLY} or {@link SQLs#WITH_TIES}
        R fetch(SQLs.FetchFirstNext firstOrNext, BiFunction<MappingType, Object, Expression> operator
                , Function<String, ?> function, String keyName, SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies);

/// @param firstOrNext  {@link SQLs#FIRST} or {@link SQLs#NEXT}
/// @param operator     the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param count        non-negative
/// @param row          {@link SQLs#ROW} or {@link SQLs#ROWS}
/// @param onlyWithTies {@link SQLs#ONLY} or {@link SQLs#WITH_TIES}
        R ifFetch(SQLs.FetchFirstNext firstOrNext, BiFunction<MappingType, Number, Expression> operator
                , @Nullable Number count, SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies);

/// @param firstOrNext  {@link SQLs#FIRST} or {@link SQLs#NEXT}
/// @param operator     the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param supplier     return non-negative integer
/// @param row          {@link SQLs#ROW} or {@link SQLs#ROWS}
/// @param onlyWithTies {@link SQLs#ONLY} or {@link SQLs#WITH_TIES}
        <N extends Number> R ifFetch(SQLs.FetchFirstNext firstOrNext, BiFunction<MappingType, Number, Expression> operator
                , Supplier<N> supplier, SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies);

/// @param firstOrNext  {@link SQLs#FIRST} or {@link SQLs#NEXT}
/// @param operator     the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param function     {@link Function#apply(Object)} return non-negative integer
/// @param keyName      keyName that is passed to function
/// @param row          {@link SQLs#ROW} or {@link SQLs#ROWS}
/// @param onlyWithTies {@link SQLs#ONLY} or {@link SQLs#WITH_TIES}
        R ifFetch(SQLs.FetchFirstNext firstOrNext, BiFunction<MappingType, Object, Expression> operator
                , Function<String, ?> function, String keyName, SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies);
    }

    interface _LimitClause<LR> extends _RowCountLimitClause<LR> {

        LR limit(Expression offset, Expression rowCount);

        LR limit(BiFunction<MappingType, Number, Expression> operator, long offset, long rowCount);

        <N extends Number> LR limit(BiFunction<MappingType, Number, Expression> operator, Supplier<N> offsetSupplier, Supplier<N> rowCountSupplier);

        LR limit(BiFunction<MappingType, Object, Expression> operator, Function<String, ?> function, String offsetKey, String rowCountKey);

        LR limit(Consumer<BiConsumer<Expression, Expression>> consumer);


        <N extends Number> LR ifLimit(BiFunction<MappingType, Number, Expression> operator, Supplier<N> offsetSupplier, Supplier<N> rowCountSupplier);

        LR ifLimit(BiFunction<MappingType, Object, Expression> operator, Function<String, ?> function, String offsetKey, String rowCountKey);

        LR ifLimit(Consumer<BiConsumer<Expression, Expression>> consumer);

    }

    interface _FetchPercentClause<R> extends _QueryFetchClause<R> {

        /// @param firstOrNext  {@link SQLs#FIRST} or {@link SQLs#NEXT}
/// @param percent      the percentage of the total number of selected rows
/// @param wordPercent  {@link SQLs#PERCENT}
/// @param row          {@link SQLs#ROW} or {@link SQLs#ROWS}
/// @param onlyWithTies {@link SQLs#ONLY} or {@link SQLs#WITH_TIES}
        R fetch(SQLs.FetchFirstNext firstOrNext, Expression percent, SQLs.WordPercent wordPercent, SQLs.FetchRow row
                , SQLs.FetchOnlyWithTies onlyWithTies);


/// @param firstOrNext  {@link SQLs#FIRST} or {@link SQLs#NEXT}
/// @param operator     the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param percent      non-null,the percentage of the total number of selected rows
/// @param wordPercent  {@link SQLs#PERCENT}
/// @param row          {@link SQLs#ROW} or {@link SQLs#ROWS}
/// @param onlyWithTies {@link SQLs#ONLY} or {@link SQLs#WITH_TIES}
        R fetch(SQLs.FetchFirstNext firstOrNext, BiFunction<MappingType, Number, Expression> operator
                , Number percent, SQLs.WordPercent wordPercent, SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies);

/// @param firstOrNext  {@link SQLs#FIRST} or {@link SQLs#NEXT}
/// @param operator     the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param supplier     {@link  Supplier#get()} return non-null percent
/// @param wordPercent  {@link SQLs#PERCENT}
/// @param row          {@link SQLs#ROW} or {@link SQLs#ROWS}
/// @param onlyWithTies {@link SQLs#ONLY} or {@link SQLs#WITH_TIES}
        <N extends Number> R fetch(SQLs.FetchFirstNext firstOrNext, BiFunction<MappingType, Number, Expression> operator
                , Supplier<N> supplier, SQLs.WordPercent wordPercent, SQLs.FetchRow row
                , SQLs.FetchOnlyWithTies onlyWithTies);

/// @param firstOrNext  {@link SQLs#FIRST} or {@link SQLs#NEXT}
/// @param operator     the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param function     {@link Function#apply(Object)} return non-null percent
/// @param keyName      keyName that is passed to function
/// @param wordPercent  {@link SQLs#PERCENT}
/// @param row          {@link SQLs#ROW} or {@link SQLs#ROWS}
/// @param onlyWithTies {@link SQLs#ONLY} or {@link SQLs#WITH_TIES}
        R fetch(SQLs.FetchFirstNext firstOrNext, BiFunction<MappingType, Object, Expression> operator
                , Function<String, ?> function, String keyName, SQLs.WordPercent wordPercent
                , SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies);

/// @param firstOrNext  {@link SQLs#FIRST} or {@link SQLs#NEXT}
/// @param operator     the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param percent      nullable,percent
/// @param wordPercent  {@link SQLs#PERCENT}
/// @param row          {@link SQLs#ROW} or {@link SQLs#ROWS}
/// @param onlyWithTies {@link SQLs#ONLY} or {@link SQLs#WITH_TIES}
        R ifFetch(SQLs.FetchFirstNext firstOrNext, BiFunction<MappingType, Number, Expression> operator
                , @Nullable Number percent, SQLs.WordPercent wordPercent, SQLs.FetchRow row
                , SQLs.FetchOnlyWithTies onlyWithTies);

/// @param firstOrNext  {@link SQLs#FIRST} or {@link SQLs#NEXT}
/// @param operator     the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param supplier     return nullable percent
/// @param wordPercent  {@link SQLs#PERCENT}
/// @param row          {@link SQLs#ROW} or {@link SQLs#ROWS}
/// @param onlyWithTies {@link SQLs#ONLY} or {@link SQLs#WITH_TIES}
        <N extends Number> R ifFetch(SQLs.FetchFirstNext firstOrNext, BiFunction<MappingType, Number, Expression> operator
                , Supplier<N> supplier, SQLs.WordPercent wordPercent, SQLs.FetchRow row
                , SQLs.FetchOnlyWithTies onlyWithTies);

/// @param firstOrNext  {@link SQLs#FIRST} or {@link SQLs#NEXT}
/// @param operator     the method reference of below:
/// 
/// - {@link SQLs#literal(TypeInfer, Object)}
/// - {@link SQLs#param(TypeInfer, Object)}
/// 
/// @param function     {@link Function#apply(Object)} return nullable percent
/// @param keyName      keyName that is passed to function
/// @param wordPercent  {@link SQLs#PERCENT}
/// @param row          {@link SQLs#ROW} or {@link SQLs#ROWS}
/// @param onlyWithTies {@link SQLs#ONLY} or {@link SQLs#WITH_TIES}
        R ifFetch(SQLs.FetchFirstNext firstOrNext, BiFunction<MappingType, Object, Expression> operator
                , Function<String, ?> function, String keyName, SQLs.WordPercent wordPercent
                , SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies);

    }

    @Deprecated
    interface _CommaStringDualSpec<PR> extends Statement._RightParenClause<PR> {

        Statement._RightParenClause<PR> comma(String string);

        _CommaStringDualSpec<PR> comma(String string1, String string2);
    }

    @Deprecated
    interface _CommaStringQuadraSpec<PR> extends Statement._RightParenClause<PR> {

        Statement._RightParenClause<PR> comma(String string);

        Statement._RightParenClause<PR> comma(String string1, String string2);

        Statement._RightParenClause<PR> comma(String string1, String string2, String string3);

        _CommaStringQuadraSpec<PR> comma(String string1, String string2, String string3, String string4);

    }

    @Deprecated
    interface _LeftParenStringDualClause<PR> extends Item {

        Statement._RightParenClause<PR> leftParen(String string);

        _CommaStringDualSpec<PR> leftParen(String string1, String string2);


    }

    @Deprecated
    interface _LeftParenStringDynamicClause<RR> extends Item {

        Statement._RightParenClause<RR> leftParen(Consumer<Consumer<String>> consumer);
    }

    @Deprecated
    interface _LeftParenStringDynamicOptionalClause<RR> extends Item {

        Statement._RightParenClause<RR> leftParenIf(Consumer<Consumer<String>> consumer);

    }


    @Deprecated
    interface _LeftParenStringDualSpec<RR>
            extends _LeftParenStringDualClause<RR>, _LeftParenStringDynamicClause<RR> {

    }

    @Deprecated
    interface _LeftParenStringDualOptionalSpec<RR> extends _LeftParenStringDualSpec<RR>
            , _LeftParenStringDynamicOptionalClause<RR> {

    }


    @Deprecated
    interface _LeftParenStringQuadraSpec<RR> extends _LeftParenStringDualSpec<RR> {

        _CommaStringQuadraSpec<RR> leftParen(String string1, String string2, String string3, String string4);

    }

    @Deprecated
    interface _LeftParenStringQuadraOptionalSpec<RR> extends _LeftParenStringQuadraSpec<RR>
            , _LeftParenStringDynamicOptionalClause<RR> {


    }


    interface _ParensStringClause<R> extends Item {

        R parens(String first, String... rest);

        R parens(Consumer<Consumer<String>> consumer);
    }


    interface _OptionalParensStringClause<R> extends _ParensStringClause<R> {

        R ifParens(Consumer<Consumer<String>> consumer);
    }


    interface _ParensOnSpec<R> extends _OptionalParensStringClause<_OnClause<R>>, _OnClause<R> {

    }

    interface _AsParensOnClause<R> extends _AsClause<_ParensOnSpec<R>> {

    }


    interface _DmlInsertClause<I extends Item> extends Item {

        I asInsert();

    }

    interface _DqlInsertClause<Q extends Item> {

        Q asReturningInsert();
    }


    interface _DmlUpdateSpec<I extends Item> extends Item {

        I asUpdate();
    }

    interface _DqlUpdateSpec<I extends Item> extends Item {

        I asReturningUpdate();
    }

    interface _DmlDeleteSpec<I extends Item> extends Item {

        I asDelete();
    }

    interface _DqlDeleteSpec<Q extends Item> extends Item {

        Q asReturningDelete();
    }

    interface _MultiStmtSpec extends Item {

        MultiResultStatement asMultiStmt();

    }

    interface _AsValuesClause<I extends Item> {

        I asValues();

    }


/// This is base interface of below:
/// 
/// - {@link UpdateStatement}
/// - {@link SubUpdate}
/// 
/// @since 0.6.0
    interface DmlUpdate {


    }

/// This is base interface of below:
/// 
/// - {@link DeleteStatement}
/// 
/// @since 0.6.0
    @Deprecated
    interface DmlDelete {


    }

    interface _AsCommandClause<I extends Item> extends Item {

        I asCommand();
    }


/// This is base interface of below:
/// 
/// - {@link InsertStatement}
/// 
/// @since 0.6.0
    @Deprecated
    interface DmlInsert extends Item {


    }

    @Deprecated
    interface DqlInsert extends Item {

    }


    interface _DynamicWithClause<B extends CteBuilderSpec, WE extends Item> extends Item {
        WE with(Consumer<B> consumer);

        WE withRecursive(Consumer<B> consumer);

        WE ifWith(Consumer<B> consumer);

        WE ifWithRecursive(Consumer<B> consumer);

    }

    interface _StaticWithClause<WS> {

        WS with(String name);

        WS withRecursive(String name);

    }


    interface _DynamicCteAsClause<T extends Item, R extends Item> {

        R as(Function<T, R> function);
    }

    interface _CommaClause<R> extends Item {

        R comma();
    }

    interface _StaticWithCommaClause<R> extends Item {

        R comma(String name);
    }


    @FunctionalInterface
    interface _StaticSpaceClause<R> {

        R space();
    }


/// This interface is public interface that developer can directly use,but just method reference,for example :
/// <pre>
/// <code>
/// &#64;Test
/// public void simple(final SyncLocalSession session) {
/// final PostgreMerge stmt;
/// stmt = Postgres.singleMerge()
/// .mergeInto(Captcha_.T, AS, "c")
/// .using(RegisterRecord_.T, AS, "r").on(Captcha_.requestNo::equal, RegisterRecord_.requestNo)
/// .whenNotMatched().then(Statement.DoNothingClause::doNothing)
/// .whenMatched().then(PostgreMerge.MatchedMergeActionSpec::delete)
/// .asCommand();
/// final long rows;
/// rows = session.update(stmt);
/// LOG.debug("{} row : {}", session.name(), rows);
/// }
/// </code>
/// </pre>
    interface DoNothingClause<R extends Item> {

        R doNothing();

    }

    interface _EndFlag extends Item {

    }

    interface _DeferContextSpec {

/// //
/// //         * 
/// //         * This method is similar to {@link SQLs#refThis(String, String)},except that this method don't access {@link ThreadLocal}.
/// //        /
//        DerivedField refThis(String derivedAlias, String selectionAlias);
//
/// //
/// //         * 
/// //         * This method is similar to {@link SQLs#refOuter(String, String)},except that this method don't access {@link ThreadLocal}.
/// //        /
//        DerivedField refOuter(String derivedAlias, String selectionAlias);
//
//        <T> QualifiedField<T> field(String tableAlias, FieldMeta<T> field);

    }


    interface _DeferClauseSpec extends _DeferContextSpec {

/// //
/// //         * 
/// //         * This method is similar to {@link SQLs#refSelection(String)},except that :
/// //         * 
/// //         *     - this method 
/// //         *     - this method don't access {@link ThreadLocal}.
/// //         * 
/// //        /
//        Expression refSelection(String selectionName);
//
/// //
/// //         * 
/// //         * This method is similar to {@link SQLs#refSelection(int)},except that this method don't access {@link ThreadLocal}.
/// //        /         *
//         * @param selectionOrdinal based 1
//         */
//        Expression refSelection(int selectionOrdinal);

    }


}
