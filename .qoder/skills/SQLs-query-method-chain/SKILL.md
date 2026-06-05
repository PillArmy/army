---
name: SQLs.query() method chain
description: 完整的 Army Criteria API SQLs.query() 方法链知识。用于理解、解释或记录从 SQLs.query() 到 asQuery() 的完整 SELECT 查询构建流程。涵盖每一个接口、每一个方法和每一个合法路径。
---

# SQLs.query() method chain — 完整参考

> **适用范围**: 本 Skill **仅限**用于理解、解释、记录 `SQLs.query()` 入口的标准 SELECT 查询构建流程。
> 覆盖从 `SQLs.query()` 到 `asQuery()` 的完整方法链，包括 WITH、SELECT、FROM、JOIN、WHERE、
> AND、GROUP BY、HAVING、WINDOW、ORDER BY、LIMIT、FOR UPDATE、UNION/INTERSECT/EXCEPT/MINUS、
> asQuery() 全部 14 个阶段。**不**涵盖 INSERT/UPDATE/DELETE 语句链、子查询构建、方言特有语法。

> **源码依据**: 本 Skill 基于以下核心源文件编写——`Statement.java`（接口定义）、
> `StandardQuery.java`（标准 SELECT 接口组合）、`StandardQueries.java`（CTE/SimpleSelect 实现）、
> `SimpleQueries.java`（SELECT/WHERE/GROUP BY/HAVING/UNION/asQuery 核心实现）、
> `JoinableClause.java`（FROM/JOIN 实现）。所有描述均以实际源码接口签名和实现为准。

## 核心入口

```java
// SQLs.java line 450-451
public static StandardQuery.WithSpec<Select> query() {
    return StandardQueries.simpleQuery(StandardDialect.STANDARD20, SELECT_IDENTITY);
}
```

**返回类型**: `StandardQuery.WithSpec<Select>` — 实现类是 `StandardQueries.SimpleSelect<Select>`。

**内部实现**: `StandardQueries.simpleQuery()`:

```java
// StandardQueries.java line 85-87
static <I extends Item> WithSpec<I> simpleQuery(StandardDialect dialect, Function<? super Select, I> function) {
    return new SimpleSelect<>(dialect, null, null, function, null);
}
```

---

## 完整方法链 Diagram

> **阅读指南**: 每个叶子节点代表一个可直接调用的方法，带完整参数列表。
> 接口链通过返回类型导航，例如 `.select(Selection)` 返回 `_StandardSelectCommaClause`，
> 后者提供 `.comma(...)` 和 `.from(...)` 两个方向。

```
SQLs.query()  →  StandardQuery.WithSpec<Select>
│
├─① WITH (可选，仅一次声明；comma 追加 CTE 可重复)
│  ├─ .with(String name)                                            → _StaticCteParensSpec
│  │  ├─ .parens(String first, String... rest)                      → _StaticCteAsClause
│  │  │  └─ .as(Function<SelectSpec<_CteComma>, _CteComma> function)→ _CteComma
│  │  │     ├─ .comma(String name)                                  → _StaticCteParensSpec (追加 CTE)
│  │  │     └─ .space()                                             → SelectSpec (结束 CTE)
│  │  └─ .parens(Consumer<Consumer<String>> consumer)               → _StaticCteAsClause
│  │     └─ .as(Function<SelectSpec<_CteComma>, _CteComma> function)→ _CteComma
│  │        ├─ .comma(String name)                                  → _StaticCteParensSpec
│  │        └─ .space()                                             → SelectSpec
│  ├─ .withRecursive(String name)                                   → _StaticCteParensSpec
│  │  └─ (同上 .parens → .as → _CteComma → .comma / .space)
│  ├─ .with(Consumer<StandardCtes> consumer)                        → SelectSpec
│  ├─ .withRecursive(Consumer<StandardCtes> consumer)               → SelectSpec
│  ├─ .ifWith(Consumer<StandardCtes> consumer)                      → SelectSpec
│  └─ .ifWithRecursive(Consumer<StandardCtes> consumer)             → SelectSpec
│
├─② SELECT (必须，至少一个)
│  ├─ [Static] 返回 _StandardSelectCommaClause，可继续 .comma / .from
│  │  ├─ .select(Selection selection)                               → _StandardSelectCommaClause
│  │  ├─ .select(Function<String,Selection> function, String alias) → _StandardSelectCommaClause
│  │  ├─ .select(Selection selection1, Selection selection2)        → _StandardSelectCommaClause
│  │  ├─ .select(Function<String,Selection> function, String alias, Selection selection) → _StandardSelectCommaClause
│  │  ├─ .select(Selection selection, Function<String,Selection> function, String alias) → _StandardSelectCommaClause
│  │  ├─ .select(Function<String,Selection> f1, String a1, Function<String,Selection> f2, String a2) → _StandardSelectCommaClause
│  │  ├─ .select(SqlField field1, SqlField field2, SqlField field3) → _StandardSelectCommaClause
│  │  ├─ .select(SqlField field1, SqlField field2, SqlField field3, SqlField field4) → _StandardSelectCommaClause
│  │  ├─ .select(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table) → _StandardSelectCommaClause
│  │  ├─ .select(String pAlias, SQLs.SymbolDot period1, ParentTableMeta<P> parent, String cAlias, SQLs.SymbolDot period2, ComplexTableMeta<P,?> child) → _StandardSelectCommaClause
│  │  ├─ .selectAll()                                              → _StaticSelectSpaceClause
│  │  ├─ .selectDistinct()                                         → _StaticSelectSpaceClause
│  │  └─ .select(List<SQLs.Modifier> modifiers)                    → _StaticSelectSpaceClause
│  ├─ [Static Space] (在 selectAll/selectDistinct/select(modifiers) 后追加列)
│  │  ├─ .space(Selection selection)                               → _StandardSelectCommaClause
│  │  ├─ .space(Function<String,Selection> function, String alias) → _StandardSelectCommaClause
│  │  ├─ .space(Selection selection1, Selection selection2)        → _StandardSelectCommaClause
│  │  ├─ .space(Function<String,Selection> function, String alias, Selection selection) → _StandardSelectCommaClause
│  │  ├─ .space(Selection selection, Function<String,Selection> function, String alias) → _StandardSelectCommaClause
│  │  ├─ .space(Function<String,Selection> f1, String a1, Function<String,Selection> f2, String a2) → _StandardSelectCommaClause
│  │  ├─ .space(SqlField field1, SqlField field2, SqlField field3) → _StandardSelectCommaClause
│  │  ├─ .space(SqlField field1, SqlField field2, SqlField field3, SqlField field4) → _StandardSelectCommaClause
│  │  ├─ .space(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table) → _StandardSelectCommaClause
│  │  └─ .space(String pAlias, SQLs.SymbolDot period1, ParentTableMeta<P> parent, String cAlias, SQLs.SymbolDot period2, ComplexTableMeta<P,?> child) → _StandardSelectCommaClause
│  ├─ [Static Comma] (追加选择列，与 select 同签名)
│  │  ├─ .comma(Selection selection)                               → _StandardSelectCommaClause
│  │  ├─ .comma(Function<String,Selection> function, String alias) → _StandardSelectCommaClause
│  │  ├─ .comma(Selection selection1, Selection selection2)        → _StandardSelectCommaClause
│  │  ├─ .comma(Function<String,Selection> function, String alias, Selection selection) → _StandardSelectCommaClause
│  │  ├─ .comma(Selection selection, Function<String,Selection> function, String alias) → _StandardSelectCommaClause
│  │  ├─ .comma(Function<String,Selection> f1, String a1, Function<String,Selection> f2, String a2) → _StandardSelectCommaClause
│  │  ├─ .comma(SqlField field1, SqlField field2, SqlField field3) → _StandardSelectCommaClause
│  │  ├─ .comma(SqlField field1, SqlField field2, SqlField field3, SqlField field4) → _StandardSelectCommaClause
│  │  ├─ .comma(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table) → _StandardSelectCommaClause
│  │  └─ .comma(String pAlias, SQLs.SymbolDot period1, ParentTableMeta<P> parent, String cAlias, SQLs.SymbolDot period2, ComplexTableMeta<P,?> child) → _StandardSelectCommaClause
│  └─ [Dynamic] 返回 _FromSpec，直接进入 FROM
│     ├─ .select(Consumer<_DeferSelectSpaceClause> consumer)       → _FromSpec
│     ├─ .selects(Consumer<SelectionConsumer> consumer)            → _FromSpec
│     ├─ .select(SQLs.Modifier modifier, Consumer<_DeferSelectSpaceClause> consumer) → _FromSpec
│     └─ .selects(SQLs.Modifier modifier, Consumer<SelectionConsumer> consumer) → _FromSpec
│
├─③ FROM (必须，至少一个)
│  ├─ .from(TableMeta<?> table, SQLs.WordAs as, String tableAlias) → _JoinSpec
│  ├─ .from(DerivedTable derivedTable)                             → _AsClause
│  │  └─ .as(String alias)                                         → _JoinSpec
│  ├─ .from(Supplier<? extends DerivedTable> supplier)             → _AsClause
│  │  └─ .as(String alias)                                         → _JoinSpec
│  ├─ .from(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable) → _AsClause
│  │  └─ .as(String alias)                                         → _JoinSpec
│  ├─ .from(@Nullable SQLs.DerivedModifier modifier, Supplier<? extends DerivedTable> supplier) → _AsClause
│  │  └─ .as(String alias)                                         → _JoinSpec
│  ├─ .from(String cteName)                                        → _JoinSpec
│  ├─ .from(String cteName, SQLs.WordAs wordAs, String alias)      → _JoinSpec
│  └─ .from(Function<_NestedLeftParenSpec<_JoinSpec>, _JoinSpec> function) → _JoinSpec
│
├─④ JOIN (可选，可重复，任意组合)
│  ├─ [INNER/LEFT/RIGHT/FULL JOIN - Table]
│  │  ├─ .join(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias)    → _OnClause
│  │  ├─ .leftJoin(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias)→ _OnClause
│  │  ├─ .rightJoin(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias)→ _OnClause
│  │  └─ .fullJoin(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias)→ _OnClause
│  ├─ [INNER/LEFT/RIGHT/FULL JOIN - Derived Table]
│  │  ├─ .join(DerivedTable derivedTable)                                 → _AsClause
│  │  ├─ .leftJoin(DerivedTable derivedTable)                             → _AsClause
│  │  ├─ .rightJoin(DerivedTable derivedTable)                            → _AsClause
│  │  ├─ .fullJoin(DerivedTable derivedTable)                             → _AsClause
│  │  ├─ .join(Supplier<? extends DerivedTable> supplier)                 → _AsClause
│  │  ├─ .leftJoin(Supplier<? extends DerivedTable> supplier)             → _AsClause
│  │  ├─ .rightJoin(Supplier<? extends DerivedTable> supplier)            → _AsClause
│  │  └─ .fullJoin(Supplier<? extends DerivedTable> supplier)             → _AsClause
│  ├─ [INNER/LEFT/RIGHT/FULL JOIN - Derived + LATERAL modifier]
│  │  ├─ .join(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable)    → _AsClause
│  │  ├─ .leftJoin(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable)→ _AsClause
│  │  ├─ .rightJoin(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable)→ _AsClause
│  │  ├─ .fullJoin(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable)→ _AsClause
│  │  ├─ .join(@Nullable SQLs.DerivedModifier modifier, Supplier<? extends DerivedTable> supplier)    → _AsClause
│  │  ├─ .leftJoin(@Nullable SQLs.DerivedModifier modifier, Supplier<? extends DerivedTable> supplier)→ _AsClause
│  │  ├─ .rightJoin(@Nullable SQLs.DerivedModifier modifier, Supplier<? extends DerivedTable> supplier)→ _AsClause
│  │  └─ .fullJoin(@Nullable SQLs.DerivedModifier modifier, Supplier<? extends DerivedTable> supplier)→ _AsClause
│  ├─ [INNER/LEFT/RIGHT/FULL JOIN - CTE]
│  │  ├─ .join(String cteName)                                    → _OnClause
│  │  ├─ .leftJoin(String cteName)                                → _OnClause
│  │  ├─ .rightJoin(String cteName)                               → _OnClause
│  │  ├─ .fullJoin(String cteName)                                → _OnClause
│  │  ├─ .join(String cteName, SQLs.WordAs wordAs, String alias)  → _OnClause
│  │  ├─ .leftJoin(String cteName, SQLs.WordAs wordAs, String alias) → _OnClause
│  │  ├─ .rightJoin(String cteName, SQLs.WordAs wordAs, String alias)→ _OnClause
│  │  └─ .fullJoin(String cteName, SQLs.WordAs wordAs, String alias)→ _OnClause
│  ├─ [INNER/LEFT/RIGHT/FULL JOIN - Nested]
│  │  ├─ .join(Function<_NestedLeftParenSpec<_OnClause>, _OnClause> function)   → _OnClause
│  │  ├─ .leftJoin(Function<_NestedLeftParenSpec<_OnClause>, _OnClause> function)→ _OnClause
│  │  ├─ .rightJoin(Function<_NestedLeftParenSpec<_OnClause>, _OnClause> function)→ _OnClause
│  │  └─ .fullJoin(Function<_NestedLeftParenSpec<_OnClause>, _OnClause> function)→ _OnClause
│  ├─ [Dynamic JOIN]
│  │  ├─ .ifJoin(Consumer<StandardJoins> consumer)                → _JoinSpec
│  │  ├─ .ifLeftJoin(Consumer<StandardJoins> consumer)            → _JoinSpec
│  │  ├─ .ifRightJoin(Consumer<StandardJoins> consumer)           → _JoinSpec
│  │  └─ .ifFullJoin(Consumer<StandardJoins> consumer)            → _JoinSpec
│  ├─ [CROSS JOIN]
│  │  ├─ .crossJoin(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias) → _JoinSpec
│  │  ├─ .crossJoin(DerivedTable derivedTable)                    → _AsClause
│  │  │  └─ .as(String alias)                                     → _JoinSpec
│  │  ├─ .crossJoin(Supplier<? extends DerivedTable> supplier)    → _AsClause
│  │  │  └─ .as(String alias)                                     → _JoinSpec
│  │  ├─ .crossJoin(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable)→ _AsClause
│  │  │  └─ .as(String alias)                                     → _JoinSpec
│  │  ├─ .crossJoin(@Nullable SQLs.DerivedModifier modifier, Supplier<? extends DerivedTable> supplier)→ _AsClause
│  │  │  └─ .as(String alias)                                     → _JoinSpec
│  │  ├─ .crossJoin(String cteName)                               → _JoinSpec
│  │  ├─ .crossJoin(String cteName, SQLs.WordAs wordAs, String alias) → _JoinSpec
│  │  ├─ .crossJoin(Function<_NestedLeftParenSpec<_JoinSpec>, _JoinSpec> function) → _JoinSpec
│  │  └─ .ifCrossJoin(Consumer<StandardCrosses> consumer)         → _JoinSpec
│  └─ [ON clause] (JOIN 后必须 .on)
│     ├─ .on(IPredicate predicate)                                → _JoinSpec
│     ├─ .on(IPredicate predicate1, IPredicate predicate2)        → _JoinSpec
│     ├─ .on(Function<Expression,IPredicate> operator, Expression operandField) → _JoinSpec
│     ├─ .on(Function<Expression,IPredicate> op1, Expression exp1, Function<Expression,IPredicate> op2, Expression exp2) → _JoinSpec
│     └─ .on(Consumer<Consumer<IPredicate>> consumer)             → _JoinSpec
│
├─⑤ WHERE (可选)
│  ├─ .where(IPredicate predicate)                                → _WhereAndSpec
│  ├─ .where(Function<T, IPredicate> expOperator, T operand)      → _WhereAndSpec
│  ├─ .whereIf(Supplier<IPredicate> supplier)                     → _WhereAndSpec
│  ├─ .whereIf(Function<T, IPredicate> expOperator, @Nullable T value) → _WhereAndSpec
│  ├─ .whereIf(ExpressionOperator<TypedExpression,T,IPredicate> expOp, BiFunction<TypedExpression,T,Expression> op, @Nullable T value) → _WhereAndSpec
│  ├─ .whereIf(BiFunction<SQLs.BiOperator,T,IPredicate> expOp, SQLs.BiOperator operator, @Nullable T value) → _WhereAndSpec
│  ├─ .whereIf(TeFunction<SQLs.BiOperator,BiFunction<TypedExpression,T,Expression>,T,IPredicate> expOp, SQLs.BiOperator op, BiFunction<TypedExpression,T,Expression> func, @Nullable T value) → _WhereAndSpec
│  ├─ .whereIf(BetweenValueOperator<T> expOp, BiFunction<TypedExpression,T,Expression> op, @Nullable T v1, SQLs.WordAnd and, @Nullable T v2) → _WhereAndSpec
│  ├─ .whereIf(BetweenDualOperator<T,U> expOp, BiFunction<TypedExpression,T,Expression> f1, @Nullable T v1, SQLs.WordAnd and, BiFunction<TypedExpression,U,Expression> f2, @Nullable U v2) → _WhereAndSpec
│  ├─ .where(Consumer<Consumer<IPredicate>> consumer)            → _GroupBySpec
│  └─ .ifWhere(Consumer<Consumer<IPredicate>> consumer)          → _GroupBySpec
│
├─⑥ AND (可选，可重复)
│  ├─ .and(IPredicate predicate)                                  → _WhereAndSpec
│  ├─ .and(Function<T, IPredicate> expOperator, T operand)        → _WhereAndSpec
│  ├─ .ifAnd(Supplier<IPredicate> supplier)                       → _WhereAndSpec
│  ├─ .ifAnd(Function<T, IPredicate> expOperator, @Nullable T value) → _WhereAndSpec
│  ├─ .ifAnd(ExpressionOperator<TypedExpression,T,IPredicate> expOp, BiFunction<TypedExpression,T,Expression> op, @Nullable T value) → _WhereAndSpec
│  ├─ .ifAnd(BiFunction<SQLs.BiOperator,T,IPredicate> expOp, SQLs.BiOperator operator, @Nullable T value) → _WhereAndSpec
│  ├─ .ifAnd(TeFunction<SQLs.BiOperator,BiFunction<TypedExpression,T,Expression>,T,IPredicate> expOp, SQLs.BiOperator op, BiFunction<TypedExpression,T,Expression> func, @Nullable T value) → _WhereAndSpec
│  ├─ .ifAnd(BetweenValueOperator<T> expOp, BiFunction<TypedExpression,T,Expression> op, @Nullable T v1, SQLs.WordAnd and, @Nullable T v2) → _WhereAndSpec
│  └─ .ifAnd(BetweenDualOperator<T,U> expOp, BiFunction<TypedExpression,T,Expression> f1, @Nullable T v1, SQLs.WordAnd and, BiFunction<TypedExpression,U,Expression> f2, @Nullable U v2) → _WhereAndSpec
│
├─⑦ GROUP BY (可选)
│  ├─ .groupBy(GroupByItem item)                                   → _HavingSpec
│  ├─ .groupBy(GroupByItem item1, GroupByItem item2)               → _HavingSpec
│  ├─ .groupBy(GroupByItem item1, GroupByItem item2, GroupByItem item3) → _HavingSpec
│  ├─ .groupBy(GroupByItem item1, GroupByItem item2, GroupByItem item3, GroupByItem item4) → _HavingSpec
│  ├─ .groupBy(Consumer<Consumer<GroupByItem>> consumer)           → _HavingSpec
│  ├─ .ifGroupBy(Consumer<Consumer<GroupByItem>> consumer)         → _HavingSpec
│  ├─ .commaSpace(GroupByItem item)                                → _HavingSpec
│  ├─ .commaSpace(GroupByItem item1, GroupByItem item2)            → _HavingSpec
│  ├─ .commaSpace(GroupByItem item1, GroupByItem item2, GroupByItem item3) → _HavingSpec
│  └─ .commaSpace(GroupByItem item1, GroupByItem item2, GroupByItem item3, GroupByItem item4) → _HavingSpec
│
├─⑧ HAVING (可选，前提是 GROUP BY)
│  ├─ [Static] 返回 _HavingAndSpec，可继续 .spaceAnd
│  │  ├─ .having(IPredicate predicate)                            → _HavingAndSpec
│  │  ├─ .having(Supplier<IPredicate> supplier)                   → _HavingAndSpec
│  │  ├─ .having(Function<E, IPredicate> operator, E value)       → _HavingAndSpec
│  │  ├─ .having(Function<V,IPredicate> operator, Function<K,V> operand, K key) → _HavingAndSpec
│  │  ├─ .having(ExpressionOperator<TypedExpression,E,IPredicate> expOp, BiFunction<TypedExpression,E,Expression> valOp, E value) → _HavingAndSpec
│  │  ├─ .having(DialectBooleanOperator<E> fieldOp, BiFunction<TypedExpression,Expression,CompoundPredicate> op, BiFunction<TypedExpression,E,Expression> func, @Nullable E value) → _HavingAndSpec
│  │  ├─ .having(ExpressionOperator<TypedExpression,V,IPredicate> expOp, BiFunction<TypedExpression,V,Expression> valOp, Function<K,V> func, K key) → _HavingAndSpec
│  │  ├─ .having(DialectBooleanOperator<V> fieldOp, BiFunction<TypedExpression,Expression,CompoundPredicate> op, BiFunction<TypedExpression,V,Expression> func, Function<K,V> func2, K key) → _HavingAndSpec
│  │  ├─ .ifHaving(ExpressionOperator<TypedExpression,E,IPredicate> expOp, BiFunction<TypedExpression,E,Expression> valOp, Supplier<E> supplier) → _HavingAndSpec
│  │  ├─ .ifHaving(DialectBooleanOperator<E> fieldOp, BiFunction<TypedExpression,Expression,CompoundPredicate> op, BiFunction<TypedExpression,E,Expression> func, Supplier<E> supplier) → _HavingAndSpec
│  │  ├─ .ifHaving(ExpressionOperator<TypedExpression,V,IPredicate> expOp, BiFunction<TypedExpression,V,Expression> valOp, Function<K,V> func, K key) → _HavingAndSpec
│  │  └─ .ifHaving(DialectBooleanOperator<V> fieldOp, BiFunction<TypedExpression,Expression,CompoundPredicate> op, BiFunction<TypedExpression,V,Expression> func, Function<K,V> func2, K key) → _HavingAndSpec
│  ├─ [spaceAnd] 追加 AND 条件，返回 _HavingAndSpec
│  │  ├─ .spaceAnd(IPredicate predicate)                           → _HavingAndSpec
│  │  ├─ .spaceAnd(Supplier<IPredicate> supplier)                  → _HavingAndSpec
│  │  ├─ .spaceAnd(Function<E, IPredicate> operator, E value)      → _HavingAndSpec
│  │  ├─ .spaceAnd(Function<V,IPredicate> operator, Function<K,V> operand, K key) → _HavingAndSpec
│  │  ├─ .spaceAnd(ExpressionOperator<TypedExpression,E,IPredicate> expOp, BiFunction<TypedExpression,E,Expression> valOp, E value) → _HavingAndSpec
│  │  ├─ .spaceAnd(DialectBooleanOperator<E> fieldOp, BiFunction<TypedExpression,Expression,CompoundPredicate> op, BiFunction<TypedExpression,E,Expression> func, @Nullable E value) → _HavingAndSpec
│  │  ├─ .spaceAnd(ExpressionOperator<TypedExpression,V,IPredicate> expOp, BiFunction<TypedExpression,V,Expression> valOp, Function<K,V> func, K key) → _HavingAndSpec
│  │  ├─ .spaceAnd(DialectBooleanOperator<V> fieldOp, BiFunction<TypedExpression,Expression,CompoundPredicate> op, BiFunction<TypedExpression,V,Expression> func, Function<K,V> func2, K key) → _HavingAndSpec
│  │  ├─ .ifSpaceAnd(ExpressionOperator<TypedExpression,E,IPredicate> expOp, BiFunction<TypedExpression,E,Expression> valOp, Supplier<E> supplier) → _HavingAndSpec
│  │  ├─ .ifSpaceAnd(DialectBooleanOperator<E> fieldOp, BiFunction<TypedExpression,Expression,CompoundPredicate> op, BiFunction<TypedExpression,E,Expression> func, Supplier<E> supplier) → _HavingAndSpec
│  │  ├─ .ifSpaceAnd(ExpressionOperator<TypedExpression,V,IPredicate> expOp, BiFunction<TypedExpression,V,Expression> valOp, Function<K,V> func, K key) → _HavingAndSpec
│  │  └─ .ifSpaceAnd(DialectBooleanOperator<V> fieldOp, BiFunction<TypedExpression,Expression,CompoundPredicate> op, BiFunction<TypedExpression,V,Expression> func, Function<K,V> func2, K key) → _HavingAndSpec
│  └─ [Dynamic] 返回 _WindowSpec
│     ├─ .having(Consumer<Consumer<IPredicate>> consumer)         → _WindowSpec
│     └─ .ifHaving(Consumer<Consumer<IPredicate>> consumer)       → _WindowSpec
│
├─⑨ WINDOW (可选)
│  ├─ .window(String windowName)                                  → Window._WindowAsClause
│  │  ├─ .as()                                                    → _WindowCommaSpec
│  │  ├─ .as(@Nullable String existingWindowName)                 → _WindowCommaSpec
│  │  ├─ .as(Consumer<Window._StandardPartitionBySpec> consumer)  → _WindowCommaSpec
│  │  └─ .as(@Nullable String existingWindowName, Consumer<Window._StandardPartitionBySpec> consumer) → _WindowCommaSpec
│  ├─ .comma(String windowName)                                   → _WindowCommaSpec
│  ├─ .windows(Consumer<Window.Builder<Window._StandardPartitionBySpec>> consumer) → _OrderBySpec
│  └─ .ifWindows(Consumer<Window.Builder<Window._StandardPartitionBySpec>> consumer) → _OrderBySpec
│
├─⑩ ORDER BY (可选)
│  ├─ .orderBy(SortItem sortItem)                                 → _LimitSpec
│  ├─ .orderBy(SortItem sortItem1, SortItem sortItem2)            → _LimitSpec
│  ├─ .orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3) → _LimitSpec
│  ├─ .orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3, SortItem sortItem4) → _LimitSpec
│  ├─ .orderBy(Consumer<Consumer<SortItem>> consumer)             → _LimitSpec
│  ├─ .ifOrderBy(Consumer<Consumer<SortItem>> consumer)           → _LimitSpec
│  ├─ .spaceComma(SortItem sortItem)                              → _LimitSpec
│  ├─ .spaceComma(SortItem sortItem1, SortItem sortItem2)         → _LimitSpec
│  ├─ .spaceComma(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3) → _LimitSpec
│  └─ .spaceComma(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3, SortItem sortItem4) → _LimitSpec
│
├─⑪ LIMIT (可选)
│  ├─ [仅 Row Count]
│  │  ├─ .limit(Object rowCount)                                  → _LockSpec
│  │  ├─ .limit(BiFunction<MappingType,Number,Expression> operator, long rowCount) → _LockSpec
│  │  ├─ .ifLimit(@Nullable Object rowCount)                      → _LockSpec
│  │  └─ .ifLimit(BiFunction<MappingType,Number,Expression> operator, @Nullable Number rowCount) → _LockSpec
│  ├─ [Offset + Row Count]
│  │  ├─ .limit(Expression offset, Expression rowCount)           → _LockSpec
│  │  ├─ .limit(BiFunction<MappingType,Number,Expression> operator, long offset, long rowCount) → _LockSpec
│  │  ├─ .limit(BiFunction<MappingType,Number,Expression> operator, Supplier<N> offsetSupplier, Supplier<N> rowCountSupplier) → _LockSpec
│  │  ├─ .limit(BiFunction<MappingType,Object,Expression> operator, Function<String,?> function, String offsetKey, String rowCountKey) → _LockSpec
│  │  └─ .limit(Consumer<BiConsumer<Expression,Expression>> consumer) → _LockSpec
│  └─ [条件 Offset + Row Count]
│     ├─ .ifLimit(BiFunction<MappingType,Number,Expression> operator, Supplier<N> offsetSupplier, Supplier<N> rowCountSupplier) → _LockSpec
│     ├─ .ifLimit(BiFunction<MappingType,Object,Expression> operator, Function<String,?> function, String offsetKey, String rowCountKey) → _LockSpec
│     └─ .ifLimit(Consumer<BiConsumer<Expression,Expression>> consumer) → _LockSpec
│
├─⑫ FOR UPDATE (可选)
│  ├─ .forUpdate()                                                → _AsQueryClause
│  └─ .ifForUpdate(BooleanSupplier predicate)                     → _AsQueryClause
│
├─⑬ UNION / INTERSECT / EXCEPT / MINUS (可选，可重复)
│  ├─ .union()                                                    → SelectSpec
│  ├─ .unionAll()                                                 → SelectSpec
│  ├─ .unionDistinct()                                            → SelectSpec
│  ├─ .intersect()                                                → SelectSpec
│  ├─ .intersectAll()                                             → SelectSpec
│  ├─ .intersectDistinct()                                        → SelectSpec
│  ├─ .except()                                                   → SelectSpec
│  ├─ .exceptAll()                                                → SelectSpec
│  ├─ .exceptDistinct()                                           → SelectSpec
│  ├─ .minus()                                                    → SelectSpec
│  ├─ .minusAll()                                                 → SelectSpec
│  └─ .minusDistinct()                                            → SelectSpec
│
└─⑭ 结尾: asQuery()
   └─ .asQuery()                                                  → Select (可执行查询)
```

---

## 逐层接口详解

### 0. 入口: `StandardQuery.WithSpec<Select>`

```java
// StandardQuery.java line 341-344
interface WithSpec<I extends Item>
        extends _StandardDynamicWithClause<SelectSpec<I>>,   // with(Consumer)
        _StandardStaticWithClause<SelectSpec<I>>,     // with(name).parens(...).as(...)
        SelectSpec<I> {                               // SELECT clause
}
```

`WithSpec` 组合了三种能力：动态 CTE、静态 CTE、SELECT。所以 `SQLs.query()` 返回的对象可以直接 `.select(...)`，也可以先用
`.with(...)` 定义 CTE。

---

### ① WITH (Common Table Expression)

> **语义约束**: `WITH` 关键字仅出现一次（即 `.with(name)` / `.withRecursive(name)` 只能调用一次）。
> 后续 CTE 通过 `_CteComma.comma(String name)` 追加，该方法返回 `_StaticCteParensSpec`，
> 从而形成 `.comma(name) → .parens(...) → .as(...) → .comma(name) → ...` 的循环链。
> `.space()` 是唯一退出 CTE 链、进入 SELECT 的路径。
> **禁止**在 `.space()` 后再调用 `.with(name)`——类型系统不提供此路径。

**接口**: `_StandardDynamicWithClause` + `_StandardStaticWithClause`

**静态 CTE (编译时已知)**:

```java
SQLs.query()
    .

with("cte_name")                     // → _CteComma
    .

parens("col1","col2")              // → _StaticCteAsClause (可选列别名)
    .

as(s ->s.

select(...).

from(...)     // → _CteComma (可继续 .comma(name) 追加更多 CTE)
            .

asQuery())
        .

space()                              // → SelectSpec (结束 CTE，进入 SELECT)
    .

select(...)

// 条件 ifWith — 仅当 consumer 满足条件时执行
.

ifWith(builder ->{...})              // → SelectSpec
        .

ifWithRecursive(builder ->{...})     // → SelectSpec
```

**动态 CTE (运行时构建)**:

```java
SQLs.query()
    .

with(builder ->{                    // builder is CteBuilder
        builder.

comma("cte1").

parens("col").

as(s ->s.

select(...)...

asQuery());
        builder.

comma("cte2").

as(s ->s.

select(...)...

asQuery());
        })                                    // → SelectSpec
        .

select(...)
```

**实现**: `StandardQueries.java` line 147-156

---

### ② SELECT

#### 接口层

```java
// StandardQuery.java line 309-313
interface _StandardSelectClause<I extends Item>
        extends _ModifierListSelectClause<SQLs.Modifier, _StandardSelectCommaClause<I>>,
        _DynamicModifierSelectClause<SQLs.Modifier, _FromSpec<I>> {
}
```

#### 静态 SELECT — 编译时已知列

```java
// SimpleQueries.java line 125-223, 接口定义见 Query._StaticSelectClause
.select(selection)                              // 单个 Selection
.

select(refField, alias)                        // Function<String,Selection> + alias
.

select(sel1, sel2)                             // 2 个
.

select(refField, alias, sel)                   // refField + alias + Selection
.

select(sel, refField, alias)                   // Selection + refField + alias
.

select(ref1, alias1, ref2, alias2)             // 两个 refField
.

select(field1, field2, field3)                 // 3 个 SqlField
.

select(field1, field2, field3, field4)         // 4 个 SqlField
.

select(tableAlias, PERIOD, TableMeta)          // 一键展开整表列 (SELECT alias.*)
.

select(pAlias, PERIOD, parentT, cAlias, PERIOD, childT) // 父子表联合展开
.

selectAll()                                    // SELECT ALL (默认)
.

selectDistinct()                               // SELECT DISTINCT
.

select(List<Modifier>)                         // SELECT with modifiers
```

**返回值**:

- `.select(Selection)` → `_StandardSelectCommaClause<I>` (可继续 `.comma(...)` 或 `.from(...)`)
- `.selectAll()` / `.selectDistinct()` / `.select(List<Modifier>)` → `_StaticSelectSpaceClause<SR>` (可 `.space(...)`
  追加列)
    - `.space(...)` 后返回 `_StandardSelectCommaClause<I>`，此时可 `.comma(...)` 或 `.from(...)`

#### 动态 SELECT — 运行时构建列

```java
// SimpleQueries.java line 229-282
.select(consumer ->{
        consumer.

space(sel1)                        // 添加列
           .

comma(sel2)
           .

comma("alias",PERIOD, ASTERISK);   // SELECT alias.*
})                                              // → _FromSpec
        .

selects(consumer ->{ /* SelectionConsumer API */ })  // 同名 alter
        .

select(ALL, consumer)                          // 带 modifier
.

selects(DISTINCT, consumer)
```

**返回值**: 动态 SELECT 直接返回 `_FromSpec<I>`，不允许逗号分隔（顺序不定）。

#### 逗号分隔 — 追加列

```java
// Query._StaticSelectCommaClause — 方法签名与 _StaticSelectClause 相同
.comma(selection)                               // 追加一个选择列
.

comma(refField, alias)                         // Function<String,Selection> + alias
.

comma(sel1, sel2)                              // 追加两个
.

comma(refField, alias, sel)                    // refField + alias + Selection
.

comma(sel, refField, alias)                    // Selection + refField + alias
.

comma(ref1, alias1, ref2, alias2)              // 两个 refField
.

comma(field1, field2, field3)                  // 追加 3 个 SqlField
.

comma(field1, field2, field3, field4)          // 追加 4 个 SqlField
.

comma(tableAlias, PERIOD, TableMeta)           // 追加整表列
.

comma(pAlias, PERIOD, parentT, cAlias, PERIOD, childT)   // 追加父子表列
```

**返回值**: `_StandardSelectCommaClause<I>` → 可继续 `.comma(...)` 或 `.from(...)`。

> **注意**: `.comma(refField, alias)` 中的 `refField` 是 `Function<String, Selection>`（如 `p -> refField("t", p)`），
> 并非专用 refField，任何 `Function<String, Selection>` 均可。

---

### ③ FROM

#### 接口层

```java
// Statement.java line 267-332

// 基础表/导出表
interface _FromClause<FT, FS> {
    FT from(TableMeta<?> table, SQLs.WordAs as, String tableAlias);

    FS from(DerivedTable derivedTable);

    <T extends DerivedTable> FS from(Supplier<T> supplier);
}

// 带 DerivedModifier (LATERAL)
interface _FromModifierTabularClause<FT, FS> extends _FromClause<FT, FS> {
    FS from(@Nullable SQLs.DerivedModifier modifier, DerivedTable);

    FS from(@Nullable SQLs.DerivedModifier modifier, Supplier);
}

// CTE
interface _FromCteClause<R> {
    R from(String cteName);

    R from(String cteName, WordAs, String alias);
}

// Nested (逗号分隔)
interface _FromNestedClause<T extends Item, R extends Item> {
    R from(Function<T, R> function);
}
```

**StandardQuery 组合**:

```java
// StandardQuery.java line 287-292
interface _FromSpec<I extends Item>
        extends Statement._FromModifierTabularClause<_JoinSpec<I>, _AsClause<_JoinSpec<I>>>,
        _FromCteClause<_JoinSpec<I>>,
        _FromNestedClause<_NestedLeftParenSpec<_JoinSpec<I>>, _JoinSpec<I>>,
        _UnionSpec<I> {
}
```

#### 四种 FROM 形式

| 形式                  | 调用                                                  | 下一步           |
|---------------------|-----------------------------------------------------|---------------|
| **Table**           | `.from(Stock_.T, AS, "s")`                          | → `_JoinSpec` |
| **Derived Table**   | `.from(subQuery()).as("alias")`                     | → `_JoinSpec` |
| **Derived+LATERAL** | `.from(SQLs.LATERAL, subQuery()).as("a")`           | → `_JoinSpec` |
| **CTE**             | `.from("cte_name")` / `.from("cte", AS, "a")`       | → `_JoinSpec` |
| **Nested**          | `.from(s -> s.leftParen(t1).join(t2).rightParen())` | → `_JoinSpec` |

**实现**: `JoinableClause.java` line 83-158

---

### ④ JOIN

#### 接口层

```java
// Statement.java line 421-504 & 517-563

interface _JoinClause<JT, JS> {
    // 四种 JOIN 类型 × 三种目标类型
    JT leftJoin(TableMeta, WordAs, String);

    JS leftJoin(DerivedTable);

    <T> JS leftJoin(Supplier<T>);

    JT join(TableMeta, WordAs, String);

    JS join(DerivedTable);

    <T> JS join(Supplier<T>);

    JT rightJoin(TableMeta, WordAs, String);

    JS rightJoin(DerivedTable);

    <T> JS rightJoin(Supplier<T>);

    JT fullJoin(TableMeta, WordAs, String);

    JS fullJoin(DerivedTable);

    <T> JS fullJoin(Supplier<T>);
}

interface _CrossJoinClause<FT, FS> {
    FT crossJoin(TableMeta, WordAs, String);

    FS crossJoin(DerivedTable);

    <T> FS crossJoin(Supplier<T>);
}

interface _JoinCteClause<JC> {
    JC leftJoin(String cteName);

    JC leftJoin(String cteName, WordAs, String alias);  // CTE with alias

    JC join(String cteName);

    JC join(String cteName, WordAs, String alias);

    JC rightJoin(String cteName);

    JC rightJoin(String cteName, WordAs, String alias);

    JC fullJoin(String cteName);

    JC fullJoin(String cteName, WordAs, String alias);
}

interface _CrossJoinCteClause<FC> {
    FC crossJoin(String cteName);

    FC crossJoin(String cteName, WordAs, String alias);
}
```

**StandardQuery._JoinSpec**:

```java
// StandardQuery.java line 267-272
interface _JoinSpec<I extends Item>
        extends _StandardJoinClause<_JoinSpec<I>, _OnClause<_JoinSpec<I>>>,
        _JoinCteClause<_OnClause<_JoinSpec<I>>>,
        _CrossJoinCteClause<_JoinSpec<I>>,
        _WhereSpec<I> {
}
```

> **重要**: `StandardQuery._JoinSpec` **不包含** `straightJoin` / `ifStraightJoin` — 这些是 MySQL 方言专属方法，不在标准
> SQL 方法链中。

#### JOIN 链规则

| 调用                           | 返回类型        | 下一步                                                     |
|------------------------------|-------------|---------------------------------------------------------|
| `.join(table, AS, "a")`      | `_OnClause` | `.on(predicate)` → `_JoinSpec`                          |
| `.join(derived)`             | `_AsClause` | `.as("alias")` → `_OnClause` → `.on(...)` → `_JoinSpec` |
| `.join(Supplier)`            | `_AsClause` | 同上                                                      |
| `.join(cteName)`             | `_OnClause` | `.on(...)` → `_JoinSpec`                                |
| `.join(LATERAL, derived)`    | `_AsClause` | 同上 (PostgreSQL LATERAL)                                 |
| `.crossJoin(table, AS, "a")` | `_JoinSpec` | 无需 ON，直接进入 WHERE 等                                      |
| `.crossJoin(derived)`        | `_AsClause` | `.as("alias")` → `_JoinSpec`                            |
| `.crossJoin(cteName)`        | `_JoinSpec` | 无需 ON                                                   |

**动态 JOIN**:

```java
.ifJoin(joins ->joins
        .

join(table, AS, "a").

on(...)
    .

join(derived).

as("b").

on(...)
)
        .

ifLeftJoin(joins ->{...})
        .

ifRightJoin(joins ->{...})
        .

ifFullJoin(joins ->{...})
        .

ifCrossJoin(crosses ->{...})
```

**Nested JOIN**:

```java
.join(s ->s.

leftParen(t1, AS, "a")
        .

join(t2, AS, "b").

on(...)
        .

rightParen()
)                                       // → _OnClause (.on 在 lambda 内部完成)
```

**实现**: `StandardQueries.java` line 159-211，继承自 `JoinableClause`

---

### ⑤ WHERE

#### 接口层

```java
// Statement.java line 792-830

interface _WhereClause<WR, WA> {
    // 静态形式
    WA where(IPredicate predicate);                          // → and chain

    <T> WA where(Function<T, IPredicate> expOp, T operand);   // method-ref: PillUser_.id::equal, 1

    WA whereIf(Supplier<IPredicate> supplier);               // 条件 (supplier 返回 null 则忽略)

    // 动态形式
    WR where(Consumer<Consumer<IPredicate>> consumer);       // → next clause (跳过 and)

    // 条件形式
    <T> WA whereIf(Function<T, IPredicate>, @Nullable T);    // 仅当 non-null 时

    WR ifWhere(Consumer<Consumer<IPredicate>>);              // 动态 + 条件
}

// _QueryWhereClause 继承 _WhereClause，无额外方法
interface _QueryWhereClause<WR, WA> extends _WhereClause<WR, WA>, _MinQueryWhereClause<WR, WA> {
}
```

#### 返回值:

- `.where(IPredicate)` → `_WhereAndSpec` (可继续 `.and(...)`)
- `.where(Consumer)` → `_GroupBySpec` (直接跳转到 GROUP BY / ORDER BY 等)

#### 使用示例

```java
// 方法引用风格 (最常用)
.where(PillPerson_.id.equal(SQLs::literal, 1))

// 直接传 IPredicate
        .

where(PillUser_.nickName.equal(SQLs.param("脉兽秀秀")))

// Consumer 风格 (多条件)
        .

where(whereClause ->{
        whereClause.

accept(condition1);
    whereClause.

accept(condition2);
})
```

**实现**: `WhereClause.java` line 70+

---

### ⑥ AND

```java
// Statement.java line 851-875
interface _WhereAndClause<WA> {
    WA and(IPredicate predicate);

    <T> WA and(Function<T, IPredicate> expOp, T operand);   // method-ref 风格

    WA ifAnd(Supplier<IPredicate>);                          // 条件 and

    <T> WA ifAnd(Function<T, IPredicate>, @Nullable T);     // 条件 method-ref
}
```

```java
.and(PillUser_.nickName.equal(SQLs::param, "脉兽秀秀"))
        .

and(PillUser_.createTime.notBetween(SQLs::literal, d1, AND, d2))
        .

and(SQLs::exists, SQLs.subQuery().

select(...).

from(...).

asQuery())
        .

and(PillUser_.id::in, SQLs.subQuery().

select(...).

from(...).

asQuery())
```

**实现**: `WhereClause.java`

---

### ⑦ GROUP BY

```java
// SimpleQueries.java line 448-520

.groupBy(item)                              // 单个
.

groupBy(item1, item2)                      // 2 个
.

groupBy(item1, item2, item3)               // 3 个
.

groupBy(item1, item2, item3, item4)        // 4 个
.

groupBy(Consumer<Consumer<GroupByItem>>)   // 动态
.

ifGroupBy(Consumer)                        // 条件动态

// 追加 group by 项 (逗号分隔)
.

commaSpace(item)                           // 追加单个
.

commaSpace(item1, item2)                   // 追加 2 个
.

commaSpace(item1, item2, item3)            // 追加 3 个
.

commaSpace(item1, item2, item3, item4)     // 追加 4 个
```

```java
.groupBy(ChinaRegion_.regionType)
.

groupBy(o ->{
        o.

accept(Stock_.exchange);
    o.

accept(Stock_.status);
})
```

**返回值**: `_HavingSpec` (HAVING) 或 `_WindowSpec` (WINDOW) 或 `_OrderBySpec` (ORDER BY)

---

### ⑧ HAVING

```java
// SimpleQueries.java line 522-783, 接口见 Query._StaticHavingClause

// --- Static (返回 _HavingAndSpec，可继续 .spaceAnd) ---
.having(IPredicate)                                    // 静态单条件 → _HavingAndSpec
.

having(Function<T, IPredicate>, T)                     // method-ref → _HavingAndSpec
.

having(Supplier<IPredicate>)                           // lazy → _HavingAndSpec

// --- spaceAnd 系列 (追加 AND 条件) ---
.

spaceAnd(IPredicate)                                   // 追加 AND 条件 (短路: 如无 GROUP BY 则忽略)
.

spaceAnd(Supplier<IPredicate>)                         // 追加条件 (lazy)
.

spaceAnd(Function<E, IPredicate>, E)                   // method-ref 风格

// --- Dynamic (返回 _WindowSpec) ---
.

having(Consumer<Consumer<IPredicate>>)                 // 动态多条件 → _WindowSpec
.

ifHaving(Consumer<Consumer<IPredicate>>)               // 条件动态 → _WindowSpec
```

> **注意**: HAVING 追加条件使用 `spaceAnd(...)`，而非 `and(...)`。
> `_HavingAndClause` 只有 `spaceAnd`/`ifSpaceAnd` 系列方法。
>
> **返回类型区分**: static `.having(...)` 返回 `_HavingAndSpec`（可继续 `.spaceAnd`），
> dynamic `.having(Consumer)` / `.ifHaving(Consumer)` 返回 `_WindowSpec`（直接跳过 spaceAnd 阶段）。

```java
.having(min(ChinaRegion_.regionGdp).

greater(SQLs.literalValue(minGdp)))
        .

spaceAnd(ChinaRegion_.createTime.between(SQLs::literal, d1, AND, d2))
```

**实现**: `SimpleQueries.java` line 522-783

---

### ⑨ WINDOW

```java
// StandardQueries.java line 220-249
// 接口 Window._WindowAsClause + Window._DynamicWindowClause

.window("win_name")                         // → _WindowAsClause
    .

partitionBy(...)
    .

orderBy(...)
    .

as()                                   // → _WindowCommaSpec
.

comma("win2")                              // 追加第二个 window
.

windows(builder ->{                       // 动态 windows — 必须至少定义一个 window
        builder.

window("w1").

partitionBy(...)...

as();
    builder.

window("w2").

partitionBy(...)...

as();
})
        .

ifWindows(builder ->{...})             // 条件动态 windows
```

---

### ⑩ ORDER BY

```java
// Statement.java line 897-916

.orderBy(SortItem)                          // 单个
.

orderBy(s1, s2)                            // 2 个
.

orderBy(s1, s2, s3)                        // 3 个
.

orderBy(s1, s2, s3, s4)                    // 4 个
.

orderBy(Consumer<Consumer<SortItem>>)      // 动态
.

ifOrderBy(Consumer)                        // 条件动态

// 追加排序项 (逗号分隔)
.

spaceComma(item)                           // 追加单个
.

spaceComma(item1, item2)                   // 追加 2 个
.

spaceComma(item1, item2, item3)            // 追加 3 个
.

spaceComma(item1, item2, item3, item4)     // 追加 4 个
```

```java
.orderBy(PillPerson_.birthday, PillPerson_.id::desc)
.

orderBy(Stock_.offerPrice::desc)
.

orderBy(o ->{
        o.

accept(Stock_.offerPrice::asc);
    o.

accept(Stock_.createTime::desc);
})
```

**实现**: `OrderByClause.java` line 56+

---

### ⑪ LIMIT / FETCH

```java
// Statement.java _RowCountLimitClause + _LimitClause

// 基础 LIMIT — 仅行数
.limit(Object rowCount)                                  // Expression 或 Number
.

limit(BiFunction<MappingType, Number, Expression>, long)  // operator + rowCount
.

ifLimit(@Nullable Object)                               // 条件 (非 null 时)
.

ifLimit(BiFunction, @Nullable Number)                   // 条件 + operator

// 带 offset 的 LIMIT (offset 和 rowCount 二合一)
.

limit(Expression offset, Expression rowCount)           // Expression + Expression
.

limit(BiFunction, long offset, long rowCount)           // operator + offset + rowCount
.

limit(BiFunction, Supplier<N>, Supplier<N>)            // Supplier offset + Supplier count
.

limit(BiFunction, Function<String, ?>, String, String)  // Function + keyName

// Consumer 风格
.

limit(Consumer<BiConsumer<Expression, Expression>>)      // defer offset+count

// 条件 Offset + Row Count
.

ifLimit(BiFunction, Supplier<N>, Supplier<N>)          // Supplier 条件
.

ifLimit(BiFunction, Function<String, ?>, String, String) // Function 条件
.

ifLimit(Consumer<BiConsumer<Expression, Expression>>)    // Consumer 条件
```

```java
.limit(SQLs::literal, 0,10)                              // LIMIT 10 OFFSET 0
.

limit(SQLs::literal, criteria::get, "offset","rowCount")  // 动态
```

**实现**: `LimitRowOrderByClause.java`

---

### ⑫ FOR UPDATE

```java
// StandardQueries.java line 252-270

.forUpdate()                                        // SELECT ... FOR UPDATE
.

ifForUpdate(() ->condition)                       // 条件
```

**返回值**: `_AsQueryClause<I>` → 直接 `.asQuery()`

---

### ⑬ UNION / INTERSECT / EXCEPT / MINUS

```java
// SimpleQueries.java line 786-845

.union()                    // UNION
.

unionAll()                 // UNION ALL
.

unionDistinct()            // UNION DISTINCT
.

intersect()                // INTERSECT
.

intersectAll()             // INTERSECT ALL
.

intersectDistinct()        // INTERSECT DISTINCT
.

except()                   // EXCEPT
.

exceptAll()                // EXCEPT ALL
.

exceptDistinct()           // EXCEPT DISTINCT
.

minus()                    // MINUS (Oracle)
.

minusAll()                 // MINUS ALL
.

minusDistinct()            // MINUS DISTINCT
```

**返回值**: `SelectSpec` (开始新查询，不支持 `.from` 直接开始 — 必须从 `.select` 或 `.parens` 开始)

```java
SQLs.query()
    .

parens(s ->s.

select(...).

from(...).

asQuery())
        .

union()
    .

parens(s ->s.

select(...).

from(...).

asQuery())
        .

orderBy(...::desc)
    .

limit(SQLs::literal, 10)
    .

asQuery()
```

**规则**: UNION/INTERSECT/EXCEPT/MINUS 后的 ORDER BY/LIMIT 作用于整个结果集。

---

### ⑭ asQuery() — 链尾

```java
// SimpleQueries.java line 952-955
public final Q asQuery() {
    this.endQueryStatement(false);
    return this.onAsQuery();
}
```

- `this` → `Select` (SimpleSelect → `function.apply(this)` → `SELECT_IDENTITY` → 原样返回)
- `this` → `SubQuery` (SimpleSubQuery)
- `this` → `Update` / `Delete` 类似

**最终类型**: `Select` — 可传给 `session.query(select, ResultClass)`

---

## 实现类继承链

```
SQLSyntax (SQLs extends)
    └─ SQLs
        └─ .query() → StandardQueries.simpleQuery()
            └─ SimpleSelect
                └─ StandardQueries
                    └─ SimpleQueries
                        └─ JoinableClause
                            └─ WhereClause
                                └─ OrderByClause
                                    └─ LimitRowOrderByClause
```

## 关键约束和规则

### 层级约束 (通过接口链强制执行)

接口名称本身就编码了当前上下文中可用的方法，DSL 通过**返回类型引导**下一个可调用的方法：

| 当前所处接口                       | 可调用                                                           | 下一步接口                          | 不可调用                     |
|------------------------------|---------------------------------------------------------------|--------------------------------|--------------------------|
| `SelectSpec`                 | `select(...)`                                                 | `_StandardSelectCommaClause`   | `from(...)` 不可在 select 前 |
| `_StandardSelectCommaClause` | `comma(...)`, `from(...)`                                     | self / `_JoinSpec`             | —                        |
| `_FromSpec`                  | `from(...)`                                                   | `_JoinSpec`                    | `where(...)` 不可在 from 前  |
| `_JoinSpec`                  | `join(...)`, `where(...)`                                     | `_OnClause`/`_WhereSpec`       | `groupBy(...)` 跳过 where  |
| `_WhereAndSpec`              | `and(...)`, `groupBy(...)`                                    | self/`_GroupBySpec`            | —                        |
| `_GroupBySpec`               | `commaSpace(...)`, `having(...)`, `orderBy(...)`              | `_HavingSpec`/`_OrderBySpec`   | —                        |
| `_HavingSpec`                | `having(...)`, `spaceAnd(...)`, `window(...)`, `orderBy(...)` | `_HavingAndSpec`/`_WindowSpec` | —                        |
| `_LimitSpec`                 | `limit(...)`, `forUpdate()`, `asQuery()`                      | `_LockSpec`/`_AsQueryClause`   | —                        |

### 方法引用 (Method Reference) 模式

Army 大量使用 `::` 方法引用作为参数，关键三点：

1. **BiFunction 上下文**: `.where(field::equal, SQLs::literal, value)` — `::literal` 是
   `BiFunction<TypeInfer, T, Expression>`，匹配
2. **参数位置决定语义**: `SQLs::literal` 在 `.greater(op, val)` 中是第二个参数 operator，`val` 是第三个参数
3. **不是所有方法都能 `::`**: 单参方法如 `SQLs.literalValue(value)` **不能**作为 `::` 引用

### DEFAULT Mode

默认 `LiteralMode.DEFAULT`，普通值自动参数化：

```java
// ✅ 无需显式 literal/param
Stock_.offerPrice.greater(1000)          // 自动包装为 JDBC ?
SQLs.

round(Stock_.offerPrice, 2)

WHERE offer_price >?
```

## 典型完整示例

```java
Select stmt = SQLs.query()
        // ① WITH
        .with("active").as(w -> w.query()
                .select(Stock_.id, Stock_.name)
                .from(Stock_.T, AS, "s")
                .where(Stock_.status.equal(SQLs::param, StockStatus.ACTIVE))
                .asQuery()
        ).space()
        // ② SELECT
        .select("u", PERIOD, PillUser_.T, "p", PERIOD, PillPerson_.T)
        // ③ FROM
        .from(PillPerson_.T, AS, "p")
        // ④ JOIN
        .join(PillUser_.T, AS, "u")
        .on(PillUser_.id::equal, PillPerson_.id)
        // ⑤ WHERE
        .where(PillPerson_.id.equal(SQLs::literal, 1))
        // ⑥ AND
        .and(PillUser_.nickName.equal(SQLs::param, "脉兽秀秀"))
        .and(PillUser_.createTime.notBetween(SQLs::literal, d1, AND, d2))
        // ⑦ GROUP BY
        .groupBy(PillUser_.userType)
        // ⑧ HAVING
        .having(PillUser_.userType.equal(SQLs::literal, PillUserType.PERSON))
        // ⑨ WINDOW
        .window("w").partitionBy(PillUser_.userType).orderBy(PillUser_.id::desc).as()
        // ⑩ ORDER BY
        .orderBy(PillPerson_.birthday, PillPerson_.id::desc)
        // ⑪ LIMIT
        .limit(SQLs::literal, 0, 10)
        // ⑫ FOR UPDATE
        .forUpdate()
        // ⑭ asQuery
        .asQuery();
```

## 相关源码文件

| 文件                            | 大小    | 职责                                                             |
|-------------------------------|-------|----------------------------------------------------------------|
| `SQLs.java`                   | 1231L | 入口类，静态工厂方法 `query()` / `subQuery()` / `singleUpdate()` 等       |
| `Statement.java`              | 1602L | 所有基础子句接口 (`_FromClause`, `_JoinClause`, `_WhereClause` 等)      |
| `StandardQuery.java`          | 360L  | 标准 SELECT 的接口组合层次 (`_FromSpec`, `_JoinSpec`, `_WhereSpec` 等)   |
| `StandardStatement.java`      | 81L   | 标准 JOIN 接口组合 (`_StandardJoinClause`, `_NestedLeftParenSpec` 等) |
| `Query.java`                  | 460L  | SELECT/GROUP BY/HAVING 等子句接口                                   |
| `StandardQueries.java`        | 908L  | `SimpleSelect` / `SimpleSubQuery` / CTE builder 实现             |
| `SimpleQueries.java`          | 2605L | SELECT/WHERE/GROUP BY/HAVING/UNION/asQuery 核心实现                |
| `JoinableClause.java`         | 1239L | FROM/JOIN/CROSS JOIN 实现                                        |
| `WhereClause.java`            | —     | WHERE/AND 实现                                                   |
| `OrderByClause.java`          | —     | ORDER BY 实现                                                    |
| `LimitRowOrderByClause.java`  | —     | LIMIT/OFFSET 实现                                                |
| `StandardQueryUnitTests.java` | 376L  | 真实测试用例                                                         |

---

## Skill Evolution Rules

本 Skill 是**活的文档**，每次使用或审查后都可能需要进化。以下情况**必须**更新本 Skill：

### 触发更新的条件

1. **方法链路径描述错误时**：当发现 Diagram 或逐层接口详解中的方法签名、返回类型、可用路径与源码不一致，
   必须立即修正。**禁止**凭记忆或"看起来合理"描述 API 链——每一步都必须有源码接口定义支撑。

2. **接口继承层次遗漏时**：当发现某个阶段漏掉了接口继承的方法（如 `_CteComma` 继承的
   `_StaticWithCommaClause` / `_StaticSpaceClause` 未在 Diagram 中体现），必须补全。

3. **阶段标注语义不准确时**：当发现 "可选"/"必须"/"可重复" 等标注与实际方法链行为矛盾
   （如 WITH 标注为"可重复"但 `.with(name)` 只能调用一次），必须修正标注并补充语义约束说明。

4. **新增源码发现时**：当阅读源码发现新的合法路径、新增的重载方法、新的接口组合方式，且这些在现有
   Skill 中未被记录，必须追加到 Diagram 和对应章节。

5. **关键约束遗漏时**：当发现某些重要的类型约束、层级约束未在"关键约束和规则"章节中记录，
   导致智能体理解偏差，必须补充。

6. **表述有歧义导致误导时**：当发现现有文字描述存在歧义、或导致智能体做出错误判断，必须
   重新措辞，确保表述精准无歧义。

### 更新后的自检要求

每次更新本 Skill 后，必须进行以下自检：

- [ ] **每个方法签名都有源码对应**：Diagram 中的每一个方法签名都能在源码接口中找到精确定义
- [ ] **每个返回类型都正确**：方法链中的 `→` 指向的接口类型与源码返回类型完全一致
- [ ] **语义标注准确**："可选"/"必须"/"可重复" 等标注与实际接口行为无矛盾
- [ ] **循环/终止路径明确**：类似 `_CteComma.comma() → _StaticCteParensSpec` 的循环链
  标注清楚，并说明唯一的退出路径（如 `.space()`）
- [ ] **约束规则可执行**："关键约束和规则"章节中的每一条都是可验证、可执行的，而非模糊描述
- [ ] **无冗余或矛盾**：新增内容与已有内容一致，无重复描述或相互冲突
- [ ] **章节结构层次分明**：智能体能够快速定位到所需章节（Diagram → 逐层详解 → 约束规则 → 示例）
- [ ] **示例代码与 Diagram 一致**：典型完整示例中的每个调用都能在 Diagram 中找到对应路径
