---
name: "mysql-scalar-subquery"
description: "Provides comprehensive documentation and usage guidelines for MySQLs.scalarSubQuery() method chain in Army framework. Invoke when user needs help with MySQL scalar subqueries using Army criteria API."
---

# MySQLs.scalarSubQuery() 方法链完整文档

## 概述

本技能提供 `MySQLs.scalarSubQuery()` 方法链的完整文档，包括：
- 完整方法链 Diagram
- 每个方法的参数、用法和场景
- 可重复调用和不可重复调用方法的说明
- 子句的多种形式及其使用场景

## 核心入口

```java
// MySQLs.java line 148-150
public static MySQLQuery.WithSpec<Expression> scalarSubQuery() {
    return MySQLQueries.subQuery(ContextStack.peek(), Expressions::scalarExpression);
}
```

**返回类型**: `MySQLQuery.WithSpec<Expression>` — 实现类是 `MySQLQueries.SimpleSubQuery<Expression>`。

**与 `MySQLs.subQuery()` 的区别**:
- `MySQLs.scalarSubQuery()` 使用 `Expressions.scalarExpression` 作为最终映射器
- `asQuery()` 返回 `Expression` 而非 `SubQuery`
- 标量子查询必须保证只返回一行一列，用于表达式上下文

---

## 完整方法链 Diagram

```
MySQLs.scalarSubQuery()  →  MySQLQuery.WithSpec<Expression>
  │
  ├─ ① WITH (可选，仅一次声明；comma 追加 CTE 可重复)
  │   ├─ .with(String name)
  │   │   ├─ .parens(String first, String... rest)
  │   │   │   └─ .as(Function<_StaticCteComplexCommandSpec<_CteComma>, _CteComma> function)
  │   │   ├─ .parens(Consumer<Consumer<String>> consumer)
  │   │   │   └─ .as(Function<_StaticCteComplexCommandSpec<_CteComma>, _CteComma> function)
  │   │   ├─ .ifParens(Consumer<Consumer<String>> consumer)
  │   │   │   └─ .as(Function<_StaticCteComplexCommandSpec<_CteComma>, _CteComma> function)
  │   │   └─ .as(Function<_StaticCteComplexCommandSpec<_CteComma>, _CteComma> function)
  │   │       ├─ .comma(String name) → repeatable (追加更多 CTE)
  │   │       └─ .space() → 结束 CTE，进入 SELECT
  │   ├─ .withRecursive(String name)
  │   │   └─ [同 with() 的子结构]
  │   ├─ .with(Consumer<MySQLCtes> consumer)
  │   ├─ .withRecursive(Consumer<MySQLCtes> consumer)
  │   ├─ .ifWith(Consumer<MySQLCtes> consumer)
  │   └─ .ifWithRecursive(Consumer<MySQLCtes> consumer)
  │
  ├─ ② SELECT (必须，至少一个；分为三种形式)
  │   ├─ [Static] 纯 Selection，不引用 FROM
  │   │   ├─ .select(Selection selection)
  │   │   ├─ .select(Function<String,Selection> function, String alias)
  │   │   ├─ .select(Selection selection1, Selection selection2)
  │   │   ├─ .select(Function<String,Selection> function1, String alias1, Selection selection2)
  │   │   ├─ .select(Selection selection1, Function<String,Selection> function2, String alias2)
  │   │   ├─ .select(Function<String,Selection> function1, String alias1, Function<String,Selection> function2, String alias2)
  │   │   ├─ .select(SqlField field1, SqlField field2, SqlField field3)
  │   │   ├─ .select(SqlField field1, SqlField field2, SqlField field3, SqlField field4)
  │   │   ├─ .select(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table)
  │   │   ├─ .select(String parenAlias, SQLs.SymbolDot period1, ParentTableMeta<P> parent, String childAlias, SQLs.SymbolDot period2, ComplexTableMeta<P, ?> child)
  │   │   │
  │   │   ├─ .comma(Selection selection) → repeatable
  │   │   ├─ .comma(Function<String,Selection> function, String alias) → repeatable
  │   │   ├─ .comma(Selection selection1, Selection selection2) → repeatable
  │   │   ├─ .comma(Function<String,Selection> function1, String alias1, Selection selection2) → repeatable
  │   │   ├─ .comma(Selection selection1, Function<String,Selection> function2, String alias2) → repeatable
  │   │   ├─ .comma(Function<String,Selection> function1, String alias1, Function<String,Selection> function2, String alias2) → repeatable
  │   │   ├─ .comma(SqlField field1, SqlField field2, SqlField field3) → repeatable
  │   │   ├─ .comma(SqlField field1, SqlField field2, SqlField field3, SqlField field4) → repeatable
  │   │   ├─ .comma(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table) → repeatable
  │   │   └─ .comma(String parenAlias, SQLs.SymbolDot period1, ParentTableMeta<P> parent, String childAlias, SQLs.SymbolDot period2, ComplexTableMeta<P, ?> child) → repeatable
  │   │
  │   ├─ [Modifier] selectAll / selectDistinct / select(modifiers)
  │   │   ├─ .selectAll()
  │   │   ├─ .selectDistinct()
  │   │   ├─ .select(List<MySQLs.Modifier> modifiers)
  │   │   └─ .select(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers)
  │   │       └─ .space(Selection selection) → repeatable
  │   │           └─ .comma(Selection selection) → repeatable
  │   │
  │   ├─ [Defer] Consumer<_DeferSelectSpaceClause>，引用 FROM 导出表/列
  │   │   ├─ .select(Consumer<_DeferSelectSpaceClause> consumer)
  │   │   └─ .select(MySQLs.Modifier modifier, Consumer<_DeferSelectSpaceClause> consumer)
  │   │
  │   └─ [Dynamic] Consumer<SelectionConsumer>，不确定 selections
  │       ├─ .selects(Consumer<SelectionConsumer> consumer)
  │       ├─ .selects(MySQLs.Modifier modifier, Consumer<SelectionConsumer> consumer)
  │       ├─ .select(List<MySQLs.Modifier> modifiers, Consumer<_DeferSelectSpaceClause> consumer)
  │       ├─ .selects(List<MySQLs.Modifier> modifiers, Consumer<SelectionConsumer> consumer)
  │       ├─ .select(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers, Consumer<_DeferSelectSpaceClause> consumer)
  │       └─ .selects(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers, Consumer<SelectionConsumer> consumer)
  │
  ├─ ③ FROM (必须，至少一个)
  │   ├─ [Table]
  │   │   ├─ .from(TableMeta<?> table)
  │   │   │   ├─ [Partition 选择]
  │   │   │   │   ├─ .partition(String partitionName)
  │   │   │   │   ├─ .partition(String first, String... rest)
  │   │   │   │   ├─ .partition(Consumer<Consumer<String>> consumer)
  │   │   │   │   └─ .ifPartition(Consumer<Consumer<String>> consumer)
  │   │   │   │
  │   │   │   └─ .as(String alias)
  │   │   │       ├─ [Index Hints]
  │   │   │       │   ├─ .useIndex(String indexName)
  │   │   │       │   ├─ .useIndex(String indexName1, String indexName2)
  │   │   │       │   ├─ .useIndex(String indexName1, String indexName2, String indexName3)
  │   │   │       │   ├─ .useIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
  │   │   │       │   ├─ .useIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
  │   │   │       │   ├─ .ifUseIndex(Consumer<Consumer<String>> consumer)
  │   │   │       │   ├─ .ignoreIndex(String indexName)
  │   │   │       │   ├─ .ignoreIndex(String indexName1, String indexName2)
  │   │   │       │   ├─ .ignoreIndex(String indexName1, String indexName2, String indexName3)
  │   │   │       │   ├─ .ignoreIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
  │   │   │       │   ├─ .ignoreIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
  │   │   │       │   ├─ .ifIgnoreIndex(Consumer<Consumer<String>> consumer)
  │   │   │       │   ├─ .forceIndex(String indexName)
  │   │   │       │   ├─ .forceIndex(String indexName1, String indexName2)
  │   │   │       │   ├─ .forceIndex(String indexName1, String indexName2, String indexName3)
  │   │   │       │   ├─ .forceIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
  │   │   │       │   ├─ .forceIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
  │   │   │       │   ├─ .ifForceIndex(Consumer<Consumer<String>> consumer)
  │   │   │       │   ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
  │   │   │       │   ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
  │   │   │       │   ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
  │   │   │       │   ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
  │   │   │       │   ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
  │   │   │       │   ├─ .ifUseIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
  │   │   │       │   ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
  │   │   │       │   ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
  │   │   │       │   ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
  │   │   │       │   ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
  │   │   │       │   ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
  │   │   │       │   ├─ .ifIgnoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
  │   │   │       │   ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
  │   │   │       │   ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
  │   │   │       │   ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
  │   │   │       │   ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
  │   │   │       │   ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
  │   │   │       │   └─ .ifForceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
  │   │   │       │
  │   │   │       ├─ [JOINs]
  │   │   │       │   ├─ .crossJoin(TableMeta<?> table) → repeatable
  │   │   │       │   ├─ .crossJoin(DerivedTable table) → repeatable
  │   │   │       │   │   └─ .as(String alias)
  │   │   │       │   ├─ .crossJoin(_Cte cte) → repeatable
  │   │   │       │   ├─ .crossJoin(Function<_NestedLeftParenSpec<_JoinSpec>, _JoinSpec> function) → repeatable
  │   │   │       │   ├─ .leftJoin(TableMeta<?> table) → repeatable
  │   │   │       │   │   └─ [同 on() 的子结构]
  │   │   │       │   ├─ .leftJoin(DerivedTable table) → repeatable
  │   │   │       │   │   └─ .as(String alias)
  │   │   │       │   │       └─ [同 on() 的子结构]
  │   │   │       │   ├─ .leftJoin(_Cte cte) → repeatable
  │   │   │       │   │   └─ [同 on() 的子结构]
  │   │   │       │   ├─ .leftJoin(Function<_NestedLeftParenSpec<_OnClause<_JoinSpec>>, _OnClause<_JoinSpec>> function) → repeatable
  │   │   │       │   ├─ .join(TableMeta<?> table) → repeatable
  │   │   │       │   │   └─ [同 on() 的子结构]
  │   │   │       │   ├─ .join(DerivedTable table) → repeatable
  │   │   │       │   │   └─ .as(String alias)
  │   │   │       │   │       └─ [同 on() 的子结构]
  │   │   │       │   ├─ .join(_Cte cte) → repeatable
  │   │   │       │   │   └─ [同 on() 的子结构]
  │   │   │       │   ├─ .join(Function<_NestedLeftParenSpec<_OnClause<_JoinSpec>>, _OnClause<_JoinSpec>> function) → repeatable
  │   │   │       │   ├─ .rightJoin(TableMeta<?> table) → repeatable
  │   │   │       │   │   └─ [同 on() 的子结构]
  │   │   │       │   ├─ .rightJoin(DerivedTable table) → repeatable
  │   │   │       │   │   └─ .as(String alias)
  │   │   │       │   │       └─ [同 on() 的子结构]
  │   │   │       │   ├─ .rightJoin(_Cte cte) → repeatable
  │   │   │       │   │   └─ [同 on() 的子结构]
  │   │   │       │   ├─ .rightJoin(Function<_NestedLeftParenSpec<_OnClause<_JoinSpec>>, _OnClause<_JoinSpec>> function) → repeatable
  │   │   │       │   ├─ .fullJoin(TableMeta<?> table) → repeatable
  │   │   │       │   │   └─ [同 on() 的子结构]
  │   │   │       │   ├─ .fullJoin(DerivedTable table) → repeatable
  │   │   │       │   │   └─ .as(String alias)
  │   │   │       │   │       └─ [同 on() 的子结构]
  │   │   │       │   ├─ .fullJoin(_Cte cte) → repeatable
  │   │   │       │   │   └─ [同 on() 的子结构]
  │   │   │       │   ├─ .fullJoin(Function<_NestedLeftParenSpec<_OnClause<_JoinSpec>>, _OnClause<_JoinSpec>> function) → repeatable
  │   │   │       │   ├─ .straightJoin(TableMeta<?> table) → repeatable (MySQL 独有)
  │   │   │       │   │   └─ [同 on() 的子结构]
  │   │   │       │   ├─ .straightJoin(DerivedTable table) → repeatable (MySQL 独有)
  │   │   │       │   │   └─ .as(String alias)
  │   │   │       │   │       └─ [同 on() 的子结构]
  │   │   │       │   ├─ .straightJoin(_Cte cte) → repeatable (MySQL 独有)
  │   │   │       │   │   └─ [同 on() 的子结构]
  │   │   │       │   ├─ .straightJoin(Function<_NestedLeftParenSpec<_OnClause<_JoinSpec>>, _OnClause<_JoinSpec>> function) → repeatable (MySQL 独有)
  │   │   │       │   ├─ .ifCrossJoin(Consumer<MySQLCrosses> consumer)
  │   │   │       │   ├─ .ifLeftJoin(Consumer<MySQLJoins> consumer)
  │   │   │       │   ├─ .ifJoin(Consumer<MySQLJoins> consumer)
  │   │   │       │   ├─ .ifRightJoin(Consumer<MySQLJoins> consumer)
  │   │   │       │   ├─ .ifFullJoin(Consumer<MySQLJoins> consumer)
  │   │   │       │   └─ .ifStraightJoin(Consumer<MySQLJoins> consumer)
  │   │   │       │
  │   │   │       └─ [ON clause 后继续 JOIN]
  │   │   │           ├─ .on(IPredicate predicate)
  │   │   │           ├─ .on(IPredicate predicate1, IPredicate predicate2)
  │   │   │           ├─ .on(Function<Expression, IPredicate> operator, Expression operand)
  │   │   │           ├─ .on(Function<Expression, IPredicate> operator1, Expression operand1, Function<Expression, IPredicate> operator2, Expression operand2)
  │   │   │           └─ .on(Consumer<Consumer<IPredicate>> consumer)
  │   │   │               └─ [可继续添加更多 JOIN]
  │   │   │
  │   ├─ [Derived Table]
  │   │   ├─ .from(DerivedTable table)
  │   │   │   └─ .as(String alias)
  │   │   │       ├─ .parens(String first, String... rest)
  │   │   │       ├─ .parens(Consumer<Consumer<String>> consumer)
  │   │   │       ├─ .ifParens(Consumer<Consumer<String>> consumer)
  │   │   │       └─ [同 from() 后续方法]
  │   │
  │   ├─ [CTE]
  │   │   ├─ .from(_Cte cte)
  │   │   │   └─ .as(String alias)
  │   │   │       └─ [同 from() 后续方法]
  │   │
  │   └─ [Nested]
  │       └─ .from(Function<_NestedLeftParenSpec<_JoinSpec>, _JoinSpec> function)
  │
  ├─ ④ WHERE (可选)
  │   ├─ .where(IPredicate predicate)
  │   ├─ .where(Function<T, IPredicate> expOperator, T operand)
  │   ├─ .whereIf(Supplier<IPredicate> supplier)
  │   ├─ .whereIf(Function<T, IPredicate> expOperator, @Nullable T value)
  │   ├─ .whereIf(ExpressionOperator<TypedExpression, T, IPredicate> expOp, BiFunction<TypedExpression, T, Expression> op, @Nullable T value)
  │   ├─ .whereIf(BiFunction<SQLs.BiOperator, T, IPredicate> expOp, SQLs.BiOperator operator, @Nullable T value)
  │   ├─ .whereIf(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOp, SQLs.BiOperator op, BiFunction<TypedExpression, T, Expression> func, @Nullable T value)
  │   ├─ .whereIf(BetweenValueOperator<T> expOp, BiFunction<TypedExpression, T, Expression> op, @Nullable T v1, SQLs.WordAnd and, @Nullable T v2)
  │   ├─ .whereIf(BetweenDualOperator<T, U> expOp, BiFunction<TypedExpression, T, Expression> f1, @Nullable T v1, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> f2, @Nullable U v2)
  │   ├─ .where(Consumer<Consumer<IPredicate>> consumer)
  │   └─ .ifWhere(Consumer<Consumer<IPredicate>> consumer)
  │       └─ .and(IPredicate predicate) → repeatable
  │       └─ .and(Function<T, IPredicate> expOperator, T operand) → repeatable
  │       └─ .ifAnd(Supplier<IPredicate> supplier) → repeatable
  │       └─ .ifAnd(Function<T, IPredicate> expOperator, @Nullable T value) → repeatable
  │       └─ .ifAnd(ExpressionOperator<TypedExpression, T, IPredicate> expOp, BiFunction<TypedExpression, T, Expression> op, @Nullable T value) → repeatable
  │       └─ .ifAnd(BiFunction<SQLs.BiOperator, T, IPredicate> expOp, SQLs.BiOperator operator, @Nullable T value) → repeatable
  │       └─ .ifAnd(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOp, SQLs.BiOperator op, BiFunction<TypedExpression, T, Expression> func, @Nullable T value) → repeatable
  │       └─ .ifAnd(BetweenValueOperator<T> expOp, BiFunction<TypedExpression, T, Expression> op, @Nullable T v1, SQLs.WordAnd and, @Nullable T v2) → repeatable
  │       └─ .ifAnd(BetweenDualOperator<T, U> expOp, BiFunction<TypedExpression, T, Expression> f1, @Nullable T v1, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> f2, @Nullable U v2) → repeatable
  │
  ├─ ⑤ GROUP BY (可选)
  │   ├─ .groupBy(GroupByItem item)
  │   ├─ .groupBy(GroupByItem item1, GroupByItem item2)
  │   ├─ .groupBy(GroupByItem item1, GroupByItem item2, GroupByItem item3)
  │   ├─ .groupBy(GroupByItem item1, GroupByItem item2, GroupByItem item3, GroupByItem item4)
  │   ├─ .groupBy(Consumer<Consumer<GroupByItem>> consumer)
  │   ├─ .ifGroupBy(Consumer<Consumer<GroupByItem>> consumer)
  │   │
  │   ├─ .commaSpace(GroupByItem item) → repeatable
  │   ├─ .commaSpace(GroupByItem item1, GroupByItem item2) → repeatable
  │   ├─ .commaSpace(GroupByItem item1, GroupByItem item2, GroupByItem item3) → repeatable
  │   └─ .commaSpace(GroupByItem item1, GroupByItem item2, GroupByItem item3, GroupByItem item4) → repeatable
  │       ├─ .withRollup() (MySQL 独有)
  │       └─ .ifWithRollup(BooleanSupplier supplier) (MySQL 独有)
  │
  ├─ ⑥ HAVING (可选，前提是 GROUP BY)
  │   ├─ [Static] 返回 _HavingAndSpec，可继续 .spaceAnd
  │   │   ├─ .having(IPredicate predicate)
  │   │   ├─ .having(Supplier<IPredicate> supplier)
  │   │   ├─ .having(Function<E, IPredicate> operator, E value)
  │   │   ├─ .having(Function<V, IPredicate> operator, Function<K, V> operand, K key)
  │   │   ├─ .having(ExpressionOperator<TypedExpression, E, IPredicate> expOp, BiFunction<TypedExpression, E, Expression> valOp, E value)
  │   │   ├─ .having(DialectBooleanOperator<E> fieldOp, BiFunction<TypedExpression, Expression, CompoundPredicate> op, BiFunction<TypedExpression, E, Expression> func, @Nullable E value)
  │   │   ├─ .having(ExpressionOperator<TypedExpression, V, IPredicate> expOp, BiFunction<TypedExpression, V, Expression> valOp, Function<K, V> func, K key)
  │   │   ├─ .having(DialectBooleanOperator<V> fieldOp, BiFunction<TypedExpression, Expression, CompoundPredicate> op, BiFunction<TypedExpression, V, Expression> func, Function<K, V> func2, K key)
  │   │   ├─ .ifHaving(ExpressionOperator<TypedExpression, E, IPredicate> expOp, BiFunction<TypedExpression, E, Expression> valOp, Supplier<E> supplier)
  │   │   ├─ .ifHaving(DialectBooleanOperator<E> fieldOp, BiFunction<TypedExpression, Expression, CompoundPredicate> op, BiFunction<TypedExpression, E, Expression> func, Supplier<E> supplier)
  │   │   ├─ .ifHaving(ExpressionOperator<TypedExpression, V, IPredicate> expOp, BiFunction<TypedExpression, V, Expression> valOp, Function<K, V> func, K key)
  │   │   └─ .ifHaving(DialectBooleanOperator<V> fieldOp, BiFunction<TypedExpression, Expression, CompoundPredicate> op, BiFunction<TypedExpression, V, Expression> func, Function<K, V> func2, K key)
  │   │
  │   ├─ [spaceAnd] 追加 AND 条件，返回 _HavingAndSpec → repeatable
  │   │   ├─ .spaceAnd(IPredicate predicate)
  │   │   ├─ .spaceAnd(Supplier<IPredicate> supplier)
  │   │   ├─ .spaceAnd(Function<E, IPredicate> operator, E value)
  │   │   ├─ .spaceAnd(Function<V, IPredicate> operator, Function<K, V> operand, K key)
  │   │   ├─ .spaceAnd(ExpressionOperator<TypedExpression, E, IPredicate> expOp, BiFunction<TypedExpression, E, Expression> valOp, E value)
  │   │   ├─ .spaceAnd(DialectBooleanOperator<E> fieldOp, BiFunction<TypedExpression, Expression, CompoundPredicate> op, BiFunction<TypedExpression, E, Expression> func, @Nullable E value)
  │   │   ├─ .spaceAnd(ExpressionOperator<TypedExpression, V, IPredicate> expOp, BiFunction<TypedExpression, V, Expression> valOp, Function<K, V> func, K key)
  │   │   ├─ .spaceAnd(DialectBooleanOperator<V> fieldOp, BiFunction<TypedExpression, Expression, CompoundPredicate> op, BiFunction<TypedExpression, V, Expression> func, Function<K, V> func2, K key)
  │   │   ├─ .ifSpaceAnd(ExpressionOperator<TypedExpression, E, IPredicate> expOp, BiFunction<TypedExpression, E, Expression> valOp, Supplier<E> supplier)
  │   │   ├─ .ifSpaceAnd(DialectBooleanOperator<E> fieldOp, BiFunction<TypedExpression, Expression, CompoundPredicate> op, BiFunction<TypedExpression, E, Expression> func, Supplier<E> supplier)
  │   │   ├─ .ifSpaceAnd(ExpressionOperator<TypedExpression, V, IPredicate> expOp, BiFunction<TypedExpression, V, Expression> valOp, Function<K, V> func, K key)
  │   │   └─ .ifSpaceAnd(DialectBooleanOperator<V> fieldOp, BiFunction<TypedExpression, Expression, CompoundPredicate> op, BiFunction<TypedExpression, V, Expression> func, Function<K, V> func2, K key)
  │   │
  │   └─ [Dynamic] 返回 _WindowSpec
  │       ├─ .having(Consumer<Consumer<IPredicate>> consumer)
  │       └─ .ifHaving(Consumer<Consumer<IPredicate>> consumer)
  │
  ├─ ⑦ WINDOW (可选)
  │   ├─ .window(String windowName)
  │   │   └─ .as(...)
  │   │       ├─ .comma(String windowName) → repeatable
  │   │       └─ .as(...)
  │   ├─ .windows(Consumer<Window.Builder<MySQLWindow._PartitionBySpec>> consumer)
  │   └─ .ifWindows(Consumer<Window.Builder<MySQLWindow._PartitionBySpec>> consumer)
  │
  ├─ ⑧ ORDER BY (可选)
  │   ├─ .orderBy(SortItem sortItem)
  │   ├─ .orderBy(SortItem sortItem1, SortItem sortItem2)
  │   ├─ .orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3)
  │   ├─ .orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3, SortItem sortItem4)
  │   ├─ .orderBy(Consumer<Consumer<SortItem>> consumer)
  │   ├─ .ifOrderBy(Consumer<Consumer<SortItem>> consumer)
  │   │
  │   ├─ .spaceComma(SortItem sortItem) → repeatable
  │   ├─ .spaceComma(SortItem sortItem1, SortItem sortItem2) → repeatable
  │   ├─ .spaceComma(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3) → repeatable
  │   └─ .spaceComma(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3, SortItem sortItem4) → repeatable
  │       ├─ .withRollup() (MySQL 独有)
  │       └─ .ifWithRollup(BooleanSupplier supplier) (MySQL 独有)
  │
  ├─ ⑨ LIMIT (可选)
  │   ├─ [仅 Row Count]
  │   │   ├─ .limit(Object rowCount)
  │   │   ├─ .limit(BiFunction<MappingType, Number, Expression> operator, long rowCount)
  │   │   ├─ .ifLimit(@Nullable Object rowCount)
  │   │   └─ .ifLimit(BiFunction<MappingType, Number, Expression> operator, @Nullable Number rowCount)
  │   │
  │   ├─ [Offset + Row Count]
  │   │   ├─ .limit(Expression offset, Expression rowCount)
  │   │   ├─ .limit(BiFunction<MappingType, Number, Expression> operator, long offset, long rowCount)
  │   │   ├─ .limit(BiFunction<MappingType, Number, Expression> operator, Supplier<N> offsetSupplier, Supplier<N> rowCountSupplier)
  │   │   ├─ .limit(BiFunction<MappingType, Object, Expression> operator, Function<String, ?> function, String offsetKey, String rowCountKey)
  │   │   └─ .limit(Consumer<BiConsumer<Expression, Expression>> consumer)
  │   │
  │   └─ [条件 Offset + Row Count]
  │       ├─ .ifLimit(BiFunction<MappingType, Number, Expression> operator, Supplier<N> offsetSupplier, Supplier<N> rowCountSupplier)
  │       ├─ .ifLimit(BiFunction<MappingType, Object, Expression> operator, Function<String, ?> function, String offsetKey, String rowCountKey)
  │       └─ .ifLimit(Consumer<BiConsumer<Expression, Expression>> consumer)
  │
  ├─ ⑩ LOCK (可选，MySQL 特有)
  │   ├─ .forUpdate()
  │   │   ├─ [of tables]
  │   │   │   ├─ .of(String tableAlias)
  │   │   │   ├─ .of(String tableAlias1, String tableAlias2)
  │   │   │   ├─ .of(String tableAlias1, String tableAlias2, String tableAlias3)
  │   │   │   ├─ .of(String tableAlias1, String tableAlias2, String tableAlias3, String tableAlias4)
  │   │   │   ├─ .of(String tableAlias1, String tableAlias2, String tableAlias3, String tableAlias4, String tableAlias5, String... rest)
  │   │   │   ├─ .of(Consumer<Consumer<String>> consumer)
  │   │   │   └─ .ifOf(Consumer<Consumer<String>> consumer)
  │   │   ├─ [lock wait options]
  │   │   │   ├─ .noWait()
  │   │   │   ├─ .skipLocked()
  │   │   │   ├─ .ifNoWait(BooleanSupplier supplier)
  │   │   │   └─ .ifSkipLocked(BooleanSupplier supplier)
  │   │   └─ [into options]
  │   │       ├─ .into(String firstVarName, String... rest)
  │   │       ├─ .into(Consumer<Consumer<String>> consumer)
  │   │       ├─ .ifInto(Consumer<Consumer<String>> consumer)
  │   │       └─ .asQuery()
  │   │
  │   ├─ .forShare()
  │   │   └─ [同 forUpdate() 的子结构]
  │   │
  │   ├─ .ifFor(Consumer<_DynamicLockStrengthClause> consumer)
  │   │   ├─ .update()
  │   │   └─ .share()
  │   │       └─ [同 forUpdate() 的子结构]
  │   │
  │   └─ .lockInShareMode() (MySQL 5.7 兼容)
  │       └─ [同 into options]
  │
  ├─ ⑪ UNION (可选，可重复)
  │   ├─ .union()
  │   ├─ .unionAll()
  │   ├─ .unionDistinct()
  │   ├─ .intersect() (MySQL 独有)
  │   ├─ .intersectAll() (MySQL 独有)
  │   ├─ .intersectDistinct() (MySQL 独有)
  │   ├─ .except() (MySQL 独有)
  │   ├─ .exceptAll() (MySQL 独有)
  │   ├─ .exceptDistinct() (MySQL 独有)
  │   └─ .parens(Function<WithSpec<_UnionOrderBySpec>, _UnionOrderBySpec> function)
  │
  └─ ⑫ 结尾: asQuery()
      └─ .asQuery() → Expression (标量表达式，用于 SELECT/WHERE 等子句)
```

---

## 各子句详细说明

### 1. WITH 子句 (CTE)

#### 方法说明

| 方法 | 参数 | 是否可重复 | 使用场景 |
|------|------|------------|----------|
| `.with(String name)` | CTE 名称 | 否（一次） | 创建静态命名 CTE |
| `.withRecursive(String name)` | CTE 名称 | 否（一次） | 创建递归 CTE |
| `.with(Consumer<MySQLCtes> consumer)` | CTE 构建器 | 否（一次） | 动态构建多个 CTE |
| `.withRecursive(Consumer<MySQLCtes> consumer)` | CTE 构建器 | 否（一次） | 动态构建多个递归 CTE |
| `.ifWith(Consumer<MySQLCtes> consumer)` | CTE 构建器 | 否（一次） | 条件性构建 CTE |
| `.ifWithRecursive(Consumer<MySQLCtes> consumer)` | CTE 构建器 | 否（一次） | 条件性构建递归 CTE |

#### CTE 内部方法

| 方法 | 参数 | 是否可重复 | 使用场景 |
|------|------|------------|----------|
| `.comma(String name)` | CTE 名称 | 是 | 追加更多 CTE |
| `.parens(String first, String... rest)` | 列别名 | 否 | 指定 CTE 列名 |
| `.parens(Consumer<Consumer<String>> consumer)` | 列别名消费器 | 否 | 动态指定 CTE 列名 |
| `.ifParens(Consumer<Consumer<String>> consumer)` | 列别名消费器 | 否 | 条件性指定 CTE 列名 |
| `.as(Function<...> function)` | CTE 查询函数 | 否 | 定义 CTE 查询 |
| `.space()` | 无 | 否 | 结束 CTE 定义，进入主查询 |

#### 使用示例

```java
MySQLs.scalarSubQuery()
    .with("total_sales")
    .as(s -> s
        .select(ChinaRegion_.id, ChinaRegion_.regionGdp)
        .from(ChinaRegion_.T)
        .asQuery()
    )
    .comma("region_avg")
    .as(s -> s
        .select(ChinaRegion_.parentId, SQLs.avg(ChinaRegion_.regionGdp).as("avg_gdp"))
        .from(ChinaRegion_.T)
        .groupBy(ChinaRegion_.parentId)
        .asQuery()
    )
    .space()
    .select(SQLs.refField("total_sales", ChinaRegion_.regionGdp))
    .from("total_sales")
    .asQuery();
```

---

### 2. SELECT 子句

#### 三种形式总览

`MySQLQuery._SelectSpec<I>` 继承多个接口，提供三种语义形式：

| 形式 | 接口来源 | 参数类型 | 返回类型 | 使用场景 |
|------|---------|---------|---------|----------|
| **Static** | `_StaticSelectClause` | `Selection` / `Function<String,Selection>` | `_MySQLSelectCommaSpec` | selections 确定，不引用 FROM 导出表/列 |
| **Defer** | `_DynamicSelectClause.select()` | `Consumer<_DeferSelectSpaceClause>` | `_FromSpec` | selections 确定，需要引用 FROM 导出表/列（如 `refField`） |
| **Dynamic** | `_DynamicSelectClause.selects()` | `Consumer<SelectionConsumer>` | `_FromSpec` | selections 不确定 |

#### Static Select 方法

```java
// 单个 Selection
.select(PillUser_.nickName)

// 多个 Selection
.select(PillUser_.id, PillUser_.nickName)
.select(PillUser_.id, PillUser_.nickName, PillUser_.status)
.select(PillUser_.id, PillUser_.nickName, PillUser_.status, PillUser_.createTime)

// 带别名的列
.select(PillUser_.nickName.as("user_name"))

// 组合形式
.select(PillUser_.id.as("user_id"), PillUser_.nickName)
.select(PillUser_.id, PillUser_.nickName.as("user_name"))
.select(PillUser_.id.as("user_id"), PillUser_.nickName.as("user_name"))

// 整表选择
.select("u", SQLs.PERIOD, PillUser_.T)

// 父子表联合选择
.select("p", SQLs.PERIOD, ParentTable_.T, "c", SQLs.PERIOD, ChildTable_.T)
```

#### Modifier Select 方法

```java
.selectAll()                    // SELECT ALL
.selectDistinct()               // SELECT DISTINCT
.select(modifiersList)          // SELECT with modifiers
.select(hintsSupplier, modifiersList)  // SELECT with hints and modifiers

// Modifier 后可以追加列
.selectAll()
    .space(PillUser_.nickName)
    .space(PillUser_.status)
```

#### Defer Select（引用 FROM 导出表/列）

```java
.select(s -> s.space(SQLs.refField("cte", ChinaRegion_.id)))

.select(s -> s
    .space(SQLs.refField("cte", ChinaRegion_.id).as("region_id"))
    .comma(SQLs.refField("cte", ChinaRegion_.name).as("region_name"))
)

// 带 modifier
.select(MySQLs.DISTINCT, s -> s.space(SQLs.refField("cte", ChinaRegion_.id)))
```

#### Dynamic Select（不确定 selections）

```java
.selects(consumer -> {
    consumer.selection(PillUser_.id);
    consumer.selection(PillUser_.nickName);
    consumer.selection("u", SQLs.PERIOD, SQLs.ASTERISK);  // SELECT u.*
})

// 带 modifier
.selects(MySQLs.DISTINCT, consumer -> {
    consumer.selection(PillUser_.id);
})
```

#### Comma/Space 追加方法

在 `_MySQLSelectCommaSpec` 或 `_StaticSelectSpaceClause` 上追加列：

```java
// comma() 追加
.comma(PillUser_.nickName)
.comma(PillUser_.nickName.as("user_name"))
.comma(PillUser_.id, PillUser_.nickName)
// ... 与 select() 相同的多种形式

// space() 追加（仅在 selectAll/selectDistinct 后）
.selectAll()
    .space(PillUser_.nickName)
    .comma(PillUser_.status)
```

---

### 3. FROM 子句

#### 四种 FROM 形式

| 形式 | 调用方法 | 说明 |
|------|---------|------|
| **Table** | `.from(TableMeta<?> table)` | 从基础表查询 |
| **Derived Table** | `.from(DerivedTable table)` | 从派生表（子查询）查询 |
| **CTE** | `.from(_Cte cte)` | 从 CTE 查询 |
| **Nested** | `.from(Function<...> function)` | 嵌套查询 |

#### 基础表 FROM

```java
MySQLs.scalarSubQuery()
    .select(ChinaRegion_.id)
    .from(ChinaRegion_.T)
        .partition("p1", "p2")  // MySQL 特有：分区选择
        .as("r")
        .useIndex("idx_region_id")  // MySQL 特有：索引提示
    .asQuery();
```

#### 分区选择（MySQL 独有）

| 方法 | 参数 | 是否可重复 | 使用场景 |
|------|------|------------|----------|
| `.partition(String partitionName)` | 分区名 | 否 | 选择单个分区 |
| `.partition(String first, String... rest)` | 分区名列表 | 否 | 选择多个分区 |
| `.partition(Consumer<Consumer<String>> consumer)` | 分区名消费器 | 否 | 动态选择分区 |
| `.ifPartition(Consumer<Consumer<String>> consumer)` | 分区名消费器 | 否 | 条件性选择分区 |

#### 索引提示（MySQL 独有）

| 方法组 | 说明 | 是否可重复 |
|--------|------|------------|
| `useIndex(...)` | 建议使用索引 | 否 |
| `ignoreIndex(...)` | 忽略索引 | 否 |
| `forceIndex(...)` | 强制使用索引 | 否 |

每种索引提示都有多种调用形式：
- 单个索引名：`.useIndex("idx_id")`
- 多个索引名：`.useIndex("idx_id", "idx_name")`
- Consumer 形式：`.useIndex(c -> { c.accept("idx_id"); c.accept("idx_name"); })`
- 条件性调用：`.ifUseIndex(c -> { ... })`
- 带 `FOR` 和 `Purpose`：`.useIndex(SQLs.FOR, SQLs.JOIN, "idx_id")`

#### 派生表 FROM

```java
MySQLs.scalarSubQuery()
    .select(SQLs.refField("sub", ChinaRegion_.id))
    .from(MySQLs.subQuery()
        .select(ChinaRegion_.id, ChinaRegion_.name)
        .from(ChinaRegion_.T)
        .asQuery()
    )
        .as("sub")
        .parens("id", "name")  // 可选：列别名
    .asQuery();
```

#### CTE FROM

```java
MySQLs.scalarSubQuery()
    .with("region_cte")
    .as(s -> s.select(ChinaRegion_.id).from(ChinaRegion_.T).asQuery())
    .space()
    .select(SQLs.refField("r", ChinaRegion_.id))
    .from("region_cte")
        .as("r")
    .asQuery();
```

---

### 4. JOIN 子句

#### JOIN 类型

| JOIN 类型 | 方法 | MySQL 独有 |
|----------|------|-----------|
| CROSS JOIN | `.crossJoin(...)` | 否 |
| LEFT JOIN | `.leftJoin(...)` | 否 |
| INNER JOIN | `.join(...)` | 否 |
| RIGHT JOIN | `.rightJoin(...)` | 否 |
| FULL JOIN | `.fullJoin(...)` | 否 |
| STRAIGHT JOIN | `.straightJoin(...)` | 是 |

#### JOIN 目标类型

每种 JOIN 类型都支持四种目标：
1. Table：`.join(TableMeta<?> table)`
2. Derived Table：`.join(DerivedTable table)`
3. CTE：`.join(_Cte cte)` / `.join(String cteName)`
4. Nested：`.join(Function<...> function)`

#### 使用示例

```java
MySQLs.scalarSubQuery()
    .select(ChinaRegion_.id)
    .from(ChinaRegion_.T, SQLs.AS, "r")
        .leftJoin(ChinaProvince_.T, SQLs.AS, "p")
            .on(ChinaRegion_.id::equal, ChinaProvince_.regionId)
        .straightJoin(ChinaCity_.T, SQLs.AS, "c")  // MySQL 独有
            .on(ChinaProvince_.id::equal, ChinaCity_.provinceId)
    .asQuery();
```

#### 动态 JOIN

```java
MySQLs.scalarSubQuery()
    .select(ChinaRegion_.id)
    .from(ChinaRegion_.T, SQLs.AS, "r")
        .ifJoin(joins -> {
            if (needProvinceJoin) {
                joins.join(ChinaProvince_.T, SQLs.AS, "p")
                     .on(ChinaRegion_.id::equal, ChinaProvince_.regionId);
            }
        })
    .asQuery();
```

---

### 5. WHERE 子句

#### 静态 WHERE

```java
.where(ChinaRegion_.id.equal(SQLs::literal, 1))

.where(ChinaRegion_.id::equal, 1)  // 方法引用风格

.where(ChinaRegion_.id::equal, SQLs::param, "region_id")
```

#### 条件性 WHERE

```java
.whereIf(ChinaRegion_.id::equal, regionId)  // 仅当 regionId != null 时

.whereIf(() -> {
    if (needFilter) {
        return ChinaRegion_.id.equal(SQLs::literal, 1);
    }
    return null;
})
```

#### 动态 WHERE

```java
.where(w -> {
    w.accept(ChinaRegion_.id.equal(SQLs::literal, 1));
    w.accept(ChinaRegion_.name.like(SQLs::literal, "%test%"));
})
```

#### AND 条件追加

```java
.where(ChinaRegion_.id.greater(SQLs::literal, 100))
    .and(ChinaRegion_.regionGdp.less(SQLs::literal, 10000))
    .and(ChinaRegion_.name.like(SQLs::literal, "%test%"))
    .ifAnd(ChinaRegion_.createTime::greater, startTime)  // 条件性
```

---

### 6. GROUP BY 子句

#### 静态 GROUP BY

```java
.groupBy(ChinaRegion_.parentId)

.groupBy(ChinaRegion_.parentId, ChinaRegion_.regionType)

.groupBy(ChinaRegion_.parentId, ChinaRegion_.regionType, ChinaRegion_.status)
```

#### 动态 GROUP BY

```java
.groupBy(g -> {
    g.accept(ChinaRegion_.parentId);
    g.accept(ChinaRegion_.regionType);
})
```

#### WITH ROLLUP（MySQL 独有）

```java
.groupBy(ChinaRegion_.parentId)
    .withRollup()  // MySQL 特有

.groupBy(ChinaRegion_.parentId)
    .ifWithRollup(() -> needRollup)  // 条件性
```

#### 逗号追加

```java
.groupBy(ChinaRegion_.parentId)
    .commaSpace(ChinaRegion_.regionType)
    .commaSpace(ChinaRegion_.status)
```

---

### 7. HAVING 子句

#### 注意事项

HAVING 子句必须在 GROUP BY 之后使用，并且追加条件使用 `spaceAnd(...)` 而非 `and(...)`。

#### 静态 HAVING

```java
.having(SQLs.avg(ChinaRegion_.regionGdp).greater(SQLs::literal, 10000))

.having(SQLs.avg(ChinaRegion_.regionGdp)::greater, 10000)
```

#### spaceAnd 追加

```java
.having(SQLs.avg(ChinaRegion_.regionGdp).greater(SQLs::literal, 10000))
    .spaceAnd(SQLs.count(ChinaRegion_.id).greater(SQLs::literal, 10))
    .ifSpaceAnd(SQLs.sum(ChinaRegion_.regionGdp)::greater, 100000)
```

#### 动态 HAVING

```java
.having(h -> {
    h.accept(SQLs.avg(ChinaRegion_.regionGdp).greater(SQLs::literal, 10000));
    h.accept(SQLs.count(ChinaRegion_.id).greater(SQLs::literal, 10));
})
```

---

### 8. WINDOW 子句

#### 静态 WINDOW

```java
.window("sales_window")
    .as(w -> w
        .partitionBy(ChinaRegion_.parentId)
        .orderBy(ChinaRegion_.regionGdp::desc)
    )
    .comma("another_window")
    .as(w -> w
        .partitionBy(ChinaRegion_.regionType)
    )
```

#### 动态 WINDOW

```java
.windows(builder -> {
    builder.window("w1")
        .as(w -> w.partitionBy(ChinaRegion_.parentId));
    builder.window("w2")
        .as(w -> w.partitionBy(ChinaRegion_.regionType));
})
```

---

### 9. ORDER BY 子句

#### 静态 ORDER BY

```java
.orderBy(ChinaRegion_.id::asc)

.orderBy(ChinaRegion_.id::desc, ChinaRegion_.name::asc)
```

#### 动态 ORDER BY

```java
.orderBy(o -> {
    o.accept(ChinaRegion_.id::desc);
    o.accept(ChinaRegion_.createTime::asc);
})
```

#### ORDER BY WITH ROLLUP（MySQL 独有）

```java
.orderBy(ChinaRegion_.parentId)
    .withRollup()  // MySQL 特有
```

#### 逗号追加

```java
.orderBy(ChinaRegion_.parentId::desc)
    .spaceComma(ChinaRegion_.regionGdp::asc)
```

---

### 10. LIMIT 子句

#### 仅行数

```java
.limit(10)

.limit(SQLs::literal, 10)

.ifLimit(10)  // 条件性
```

#### Offset + 行数

```java
.limit(0, 10)  // offset 0, limit 10

.limit(SQLs::literal, 0, 10)

.limit(SQLs::literal, () -> offset, () -> limit)

.limit(SQLs::literal, params::get, "offset", "limit")

.limit((offsetExpr, limitExpr) -> {
    // 构建 offset 和 limit 表达式
})
```

---

### 11. LOCK 子句（MySQL 独有）

#### FOR UPDATE

```java
.select(ChinaRegion_.id)
.from(ChinaRegion_.T, SQLs.AS, "r")
.where(ChinaRegion_.id::equal, 1)
.forUpdate()
    .of("r")
    .noWait()
.asQuery();
```

#### FOR SHARE

```java
.select(ChinaRegion_.id)
.from(ChinaRegion_.T, SQLs.AS, "r")
.where(ChinaRegion_.id::equal, 1)
.forShare()
    .skipLocked()
.asQuery();
```

#### LOCK IN SHARE MODE（MySQL 5.7 兼容）

```java
.select(ChinaRegion_.id)
.from(ChinaRegion_.T, SQLs.AS, "r")
.where(ChinaRegion_.id::equal, 1)
.lockInShareMode()
.asQuery();
```

#### 动态 LOCK

```java
.ifFor(lock -> {
    if (needLock) {
        lock.update().noWait();
    }
})
```

---

### 12. UNION 子句

#### 基础 UNION

```java
MySQLs.scalarSubQuery()
    .select(ChinaRegion_.id)
    .from(ChinaRegion_.T)
    .union()
    .select(ChinaProvince_.id)
    .from(ChinaProvince_.T)
    .orderBy(ChinaRegion_.id::desc)
    .limit(10)
    .asQuery();
```

#### UNION 变体

| 方法 | 说明 |
|------|------|
| `.union()` | UNION（去重） |
| `.unionAll()` | UNION ALL（不去重） |
| `.unionDistinct()` | UNION DISTINCT（显式去重） |
| `.intersect()` | INTERSECT（MySQL 独有） |
| `.intersectAll()` | INTERSECT ALL（MySQL 独有） |
| `.except()` | EXCEPT（MySQL 独有） |
| `.exceptAll()` | EXCEPT ALL（MySQL 独有） |

---

### 13. asQuery() — 链尾

```java
Expression scalarExpr = MySQLs.scalarSubQuery()
    .select(ChinaRegion_.regionGdp)
    .from(ChinaRegion_.T)
    .where(ChinaRegion_.id::equal, 1)
    .asQuery();
```

**返回类型**: `Expression` — 标量表达式，可用于表达式上下文（SELECT 列、WHERE 条件、HAVING 条件等）。

---

## 使用示例

### 示例 1: 基础标量子查询

```java
Select stmt = MySQLs.query()
    .select(
        ChinaRegion_.id,
        ChinaRegion_.name,
        MySQLs.scalarSubQuery()
            .select(SQLs.count(ChinaProvince_.id))
            .from(ChinaProvince_.T)
            .where(ChinaProvince_.regionId::equal, ChinaRegion_.id)
            .asQuery()
            .as("province_count")
    )
    .from(ChinaRegion_.T)
    .asQuery();
```

### 示例 2: WHERE 条件中的标量子查询

```java
Select stmt = MySQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T)
    .where(ChinaRegion_.regionGdp::greater,
        MySQLs.scalarSubQuery()
            .select(SQLs.avg(ChinaRegion_.regionGdp))
            .from(ChinaRegion_.T)
            .asQuery()
    )
    .asQuery();
```

### 示例 3: 使用 CTE 的复杂标量子查询

```java
Select stmt = MySQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T)
    .where(ChinaRegion_.id::equal,
        MySQLs.scalarSubQuery()
            .with("top_region")
            .as(s -> s
                .select(ChinaRegion_.id)
                .from(ChinaRegion_.T)
                .orderBy(ChinaRegion_.regionGdp::desc)
                .limit(1)
                .asQuery()
            )
            .space()
            .select(SQLs.refField("top_region", ChinaRegion_.id))
            .from("top_region")
            .asQuery()
    )
    .asQuery();
```

### 示例 4: 带 JOIN 和索引提示的标量子查询

```java
Select stmt = MySQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T, SQLs.AS, "r")
    .where(ChinaRegion_.regionGdp::greater,
        MySQLs.scalarSubQuery()
            .select(SQLs.avg(ChinaProvince_.population))
            .from(ChinaProvince_.T, SQLs.AS, "p")
                .useIndex(SQLs.FOR, SQLs.JOIN, "idx_region_id")
                .join(ChinaRegion_.T, SQLs.AS, "r2")
                    .on(ChinaProvince_.regionId::equal, ChinaRegion_.id)
            .where(ChinaRegion_.parentId::equal, ChinaRegion_.parentId)
            .asQuery()
    )
    .asQuery();
```

---

## 完整方法链实现类位置

- **入口类**: `MySQLs.java` - 位于 `/Users/zoro/repositories/trae/java/hub/army/army-mysql/src/main/java/io/army/criteria/impl/MySQLs.java`
- **实现类**: `MySQLQueries.java` - 位于 `/Users/zoro/repositories/trae/java/hub/army/army-mysql/src/main/java/io/army/criteria/impl/MySQLQueries.java`
- **接口定义**: `MySQLQuery.java` - 位于 `/Users/zoro/repositories/trae/java/hub/army/army-mysql/src/main/java/io/army/criteria/mysql/MySQLQuery.java`
- **基础接口**: `Query.java` - 位于 `/Users/zoro/repositories/trae/java/hub/army/army-core/src/main/java/io/army/criteria/Query.java`
