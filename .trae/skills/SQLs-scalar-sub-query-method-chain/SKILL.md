---
name: SQLs.scalarSubQuery() method chain
description: 完整的 Army Criteria API SQLs.scalarSubQuery() 方法链知识。用于理解、解释或记录从 SQLs.scalarSubQuery() 到 asQuery() 的完整标量子查询构建流程。涵盖每一个接口、每一个方法和每一个合法路径。
---

# SQLs.scalarSubQuery() method chain — 完整参考

> **适用范围**: 本 Skill **仅限**用于理解、解释、记录 `SQLs.scalarSubQuery()` 入口的标量子查询构建流程。
> 覆盖从 `SQLs.scalarSubQuery()` 到 `asQuery()` 的完整方法链，包括 WITH、SELECT、FROM、JOIN、WHERE、
> AND、GROUP BY、HAVING、WINDOW、ORDER BY、LIMIT、UNION、
> asQuery() 全部 13 个阶段。**不**涵盖 INSERT/UPDATE/DELETE 语句链、方言特有语法。

> **源码依据**: 本 Skill 基于以下核心源文件编写——`Statement.java`（接口定义）、
> `StandardQuery.java`（标准 SELECT 接口组合）、`StandardQueries.java`（CTE/SimpleSelect 实现）、
> `SimpleQueries.java`（SELECT/WHERE/GROUP BY/HAVING/UNION/asQuery 核心实现）、
> `JoinableClause.java`（FROM/JOIN 实现）。所有描述均以实际源码接口签名和实现为准。

## 核心入口

```java
// SQLs.java line 464-466
public static StandardQuery.WithSpec<Expression> scalarSubQuery() {
    return StandardQueries.subQuery(StandardDialect.STANDARD20, ContextStack.peek(), SCALAR_SUB_QUERY);
}
```

**返回类型**: `StandardQuery.WithSpec<Expression>` — 实现类是 `StandardQueries.SimpleSubQuery<Expression>`。

**内部实现**: `StandardQueries.subQuery()`:

```java
// StandardQueries.java line 94-97
static <I extends Item> WithSpec<I> subQuery(StandardDialect dialect, CriteriaContext outerContext,
                                             Function<? super SubQuery, I> function) {
    return new SimpleSubQuery<>(dialect, null, outerContext, function, null);
}
```

**与 `SQLs.subQuery()` 的区别**:
- `SQLs.scalarSubQuery()` 使用 `SCALAR_SUB_QUERY` 函数作为最终映射器
- `asQuery()` 返回 `Expression` 而非 `SubQuery`
- 标量子查询必须保证只返回一行一列，用于表达式上下文

---

## 完整方法链 Diagram

> **阅读指南**: 每个叶子节点代表一个可直接调用的方法，带完整参数列表。
> 接口链通过返回类型导航，例如 `.select(Selection)` 返回 `_StandardSelectCommaClause`，
> 后者提供 `.comma(...)` 和 `.from(...)` 两个方向。

```
SQLs.scalarSubQuery()  →  StandardQuery.WithSpec<Expression>
│
├─① WITH (可选，仅一次声明；comma 追加 CTE 可重复)
│  ├─ .with(String name)                                            → _StaticCteParensSpec
│  │  ├─ .as(Function<SelectSpec<_CteComma>, _CteComma> function)   → _CteComma (跳过 parens 直接 as)
│  │  │  ├─ .comma(String name)                                     → _StaticCteParensSpec (追加 CTE)
│  │  │  └─ .space()                                                → SelectSpec (结束 CTE)
│  │  ├─ .parens(String first, String... rest)                      → _StaticCteAsClause
│  │  │  └─ .as(Function<SelectSpec<_CteComma>, _CteComma> function)→ _CteComma
│  │  │     ├─ .comma(String name)                                  → _StaticCteParensSpec (追加 CTE)
│  │  │     └─ .space()                                             → SelectSpec (结束 CTE)
│  │  ├─ .parens(Consumer<Consumer<String>> consumer)               → _StaticCteAsClause
│  │  │  └─ .as(Function<SelectSpec<_CteComma>, _CteComma> function)→ _CteComma
│  │  │     ├─ .comma(String name)                                  → _StaticCteParensSpec
│  │  │     └─ .space()                                             → SelectSpec
│  │  └─ .ifParens(Consumer<Consumer<String>> consumer)             → _StaticCteAsClause
│  │     └─ .as(Function<SelectSpec<_CteComma>, _CteComma> function)→ _CteComma
│  │        ├─ .comma(String name)                                  → _StaticCteParensSpec
│  │        └─ .space()                                             → SelectSpec
│  ├─ .withRecursive(String name)                                   → _StaticCteParensSpec
│  │  └─ (同上 .as / .parens / .ifParens → .as → _CteComma → .comma / .space)
│  ├─ .with(Consumer<StandardCtes> consumer)                        → SelectSpec
│  ├─ .withRecursive(Consumer<StandardCtes> consumer)               → SelectSpec
│  ├─ .ifWith(Consumer<StandardCtes> consumer)                      → SelectSpec
│  └─ .ifWithRecursive(Consumer<StandardCtes> consumer)             → SelectSpec
│
├─② SELECT (必须，至少一个；分为三种形式)
│  ├─ [Static] 纯 Selection，不引用 FROM → _StandardSelectCommaClause
│  │  ├─ .select(Selection selection)                               → _StandardSelectCommaClause
│  │  ├─ .select(Selection selection1, Selection selection2)        → _StandardSelectCommaClause
│  │  ├─ .select(SqlField field1, SqlField field2, SqlField field3) → _StandardSelectCommaClause
│  │  ├─ .select(SqlField field1, SqlField field2, SqlField field3, SqlField field4) → _StandardSelectCommaClause
│  │  ├─ .select(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table) → _StandardSelectCommaClause
│  │  ├─ .select(String pAlias, SQLs.SymbolDot period1, ParentTableMeta<P> parent, String cAlias, SQLs.SymbolDot period2, ComplexTableMeta<P,?> child) → _StandardSelectCommaClause
│  │  ├─ .select(Function<String,Selection> function, String alias) → _StandardSelectCommaClause
│  │  ├─ .select(Function<String,Selection> function, String alias, Selection selection) → _StandardSelectCommaClause
│  │  ├─ .select(Selection selection, Function<String,Selection> function, String alias) → _StandardSelectCommaClause
│  │  └─ .select(Function<String,Selection> f1, String a1, Function<String,Selection> f2, String a2) → _StandardSelectCommaClause
│  ├─ [Modifier] selectAll / selectDistinct / select(modifiers)
│  │  ├─ .selectAll()                                              → _StaticSelectSpaceClause
│  │  ├─ .selectDistinct()                                         → _StaticSelectSpaceClause
│  │  └─ .select(List<SQLs.Modifier> modifiers)                    → _StaticSelectSpaceClause
│  ├─ [Space] (在 selectAll/selectDistinct/select(modifiers) 后追加列)
│  │  ├─ .space(Selection selection)                               → _StandardSelectCommaClause
│  │  ├─ .space(Selection selection1, Selection selection2)        → _StandardSelectCommaClause
│  │  ├─ .space(SqlField field1, SqlField field2, SqlField field3) → _StandardSelectCommaClause
│  │  ├─ .space(SqlField field1, SqlField field2, SqlField field3, SqlField field4) → _StandardSelectCommaClause
│  │  ├─ .space(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table) → _StandardSelectCommaClause
│  │  ├─ .space(String pAlias, SQLs.SymbolDot period1, ParentTableMeta<P> parent, String cAlias, SQLs.SymbolDot period2, ComplexTableMeta<P,?> child) → _StandardSelectCommaClause
│  │  ├─ .space(Function<String,Selection> function, String alias) → _StandardSelectCommaClause
│  │  ├─ .space(Function<String,Selection> function, String alias, Selection selection) → _StandardSelectCommaClause
│  │  ├─ .space(Selection selection, Function<String,Selection> function, String alias) → _StandardSelectCommaClause
│  │  └─ .space(Function<String,Selection> f1, String a1, Function<String,Selection> f2, String a2) → _StandardSelectCommaClause
│  ├─ [Comma] 追加选择列
│  │  ├─ .comma(Selection selection)                               → _StandardSelectCommaClause
│  │  ├─ .comma(Selection selection1, Selection selection2)        → _StandardSelectCommaClause
│  │  ├─ .comma(SqlField field1, SqlField field2, SqlField field3) → _StandardSelectCommaClause
│  │  ├─ .comma(SqlField field1, SqlField field2, SqlField field3, SqlField field4) → _StandardSelectCommaClause
│  │  ├─ .comma(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table) → _StandardSelectCommaClause
│  │  ├─ .comma(String pAlias, SQLs.SymbolDot period1, ParentTableMeta<P> parent, String cAlias, SQLs.SymbolDot period2, ComplexTableMeta<P,?> child) → _StandardSelectCommaClause
│  │  ├─ .comma(Function<String,Selection> function, String alias) → _StandardSelectCommaClause
│  │  ├─ .comma(Function<String,Selection> function, String alias, Selection selection) → _StandardSelectCommaClause
│  │  ├─ .comma(Selection selection, Function<String,Selection> function, String alias) → _StandardSelectCommaClause
│  │  └─ .comma(Function<String,Selection> f1, String a1, Function<String,Selection> f2, String a2) → _StandardSelectCommaClause
│  ├─ [Defer] Consumer<_DeferSelectSpaceClause>，引用 FROM 导出表/列 → _FromSpec
│  │  ├─ .select(Consumer<_DeferSelectSpaceClause> consumer)       → _FromSpec
│  │  ├─ .select(SQLs.Modifier modifier, Consumer<_DeferSelectSpaceClause> consumer) → _FromSpec
│  │  └─ Consumer 内可用: .space(Selection) / .comma(Selection) / .comma(alias,PERIOD,ASTERISK)
│  └─ [Dynamic] Consumer<SelectionConsumer>，不确定 selections → _FromSpec
│     ├─ .selects(Consumer<SelectionConsumer> consumer)            → _FromSpec
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
│  ├─ .comma(String windowName)                                   → Window._WindowAsClause
│  │  └─ (同上 .as() 选项 → _WindowCommaSpec，与 .window() 的 as 链相同)
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
│  │  ├─ .limit(Object rowCount)                                  → _AsQueryClause
│  │  ├─ .limit(BiFunction<MappingType,Number,Expression> operator, long rowCount) → _AsQueryClause
│  │  ├─ .ifLimit(@Nullable Object rowCount)                      → _AsQueryClause
│  │  └─ .ifLimit(BiFunction<MappingType,Number,Expression> operator, @Nullable Number rowCount) → _AsQueryClause
│  ├─ [Offset + Row Count]
│  │  ├─ .limit(Expression offset, Expression rowCount)           → _AsQueryClause
│  │  ├─ .limit(BiFunction<MappingType,Number,Expression> operator, long offset, long rowCount) → _AsQueryClause
│  │  ├─ .limit(BiFunction<MappingType,Number,Expression> operator, Supplier<N> offsetSupplier, Supplier<N> rowCountSupplier) → _AsQueryClause
│  │  ├─ .limit(BiFunction<MappingType,Object,Expression> operator, Function<String,?> function, String offsetKey, String rowCountKey) → _AsQueryClause
│  │  └─ .limit(Consumer<BiConsumer<Expression,Expression>> consumer) → _AsQueryClause
│  └─ [条件 Offset + Row Count]
│     ├─ .ifLimit(BiFunction<MappingType,Number,Expression> operator, Supplier<N> offsetSupplier, Supplier<N> rowCountSupplier) → _AsQueryClause
│     ├─ .ifLimit(BiFunction<MappingType,Object,Expression> operator, Function<String,?> function, String offsetKey, String rowCountKey) → _AsQueryClause
│     └─ .ifLimit(Consumer<BiConsumer<Expression,Expression>> consumer) → _AsQueryClause
│
├─⑫ UNION (可选，可重复；INTERSECT/EXCEPT/MINUS 仅方言提供)
│  ├─ .union()                                                    → SelectSpec
│  ├─ .unionAll()                                                 → SelectSpec
│  └─ .unionDistinct()                                            → SelectSpec
│
└─⑬ 结尾: asQuery()
   └─ .asQuery()                                                  → Expression (标量表达式，用于 SELECT/WHERE 等子句)
```

---

## 逐层接口详解

### 0. 入口: `StandardQuery.WithSpec<Expression>`

```java
// StandardQuery.java line 341-344
interface WithSpec<I extends Item>
        extends _StandardDynamicWithClause<SelectSpec<I>>,   // with(Consumer)
        _StandardStaticWithClause<SelectSpec<I>>,     // with(name).parens(...).as(...)
        SelectSpec<I> {                               // SELECT clause
}
```

`WithSpec` 组合了三种能力：动态 CTE、静态 CTE、SELECT。所以 `SQLs.scalarSubQuery()` 返回的对象可以直接 `.select(...)`，也可以先用
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
SQLs.scalarSubQuery()
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
SQLs.scalarSubQuery()
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

#### 三种形式总览

`_StandardSelectClause<I>` 继承两个父接口，提供三种语义形式：

```java
// StandardQuery.java line 309-313
interface _StandardSelectClause<I extends Item>
        extends _ModifierListSelectClause<SQLs.Modifier, _StandardSelectCommaClause<I>>,
        _DynamicModifierSelectClause<SQLs.Modifier, _FromSpec<I>> {
}
```

| 形式 | 接口来源 | 参数类型 | 返回类型 | 使用场景 |
|------|---------|---------|---------|---------|
| **Static** | `_StaticSelectClause` | `Selection` / `Function<String,Selection>` | `_StandardSelectCommaClause` | selections 确定，**不引用** FROM 导出表/列 |
| **Defer** | `_DynamicSelectClause.select()` | `Consumer<_DeferSelectSpaceClause>` | `_FromSpec` | selections 确定，**需要引用** FROM 导出表/列（如 `refField`） |
| **Dynamic** | `_DynamicSelectClause.selects()` | `Consumer<SelectionConsumer>` | `_FromSpec` | selections **不确定** |

> **核心语义**：
> - Static 返回 `_StandardSelectCommaClause`，允许后续 `.comma()` / `.from()`
> - Defer 和 Dynamic 都返回 `_FromSpec`，直接进入 FROM
> - Defer 的 `_DeferSelectSpaceClause` 和 Dynamic 的 `SelectionConsumer` 都继承 `_DeferContextSpec`，
>   提供对 FROM table references 的延迟引用能力
> - `_DeferSelectSpaceClause` 使用 `space()` / `comma()` (与 Static 同名)，`SelectionConsumer` 使用 `selection()`

---

#### 静态 SELECT — 不引用 FROM

```java
// 接口 Query._StaticSelectClause, 实现 SimpleQueries.java line 125-130
.select(PillUser_.nickName)                              // 单个 Selection

.select(PillUser_.id, PillUser_.nickName)                 // 2 个

.select(PillUser_.id, PillUser_.nickName, PillUser_.status) // 3 个 SqlField

.select(PillUser_.id, PillUser_.nickName,           // 4 个 SqlField
        PillUser_.status, PillUser_.createTime)

.select("u", PERIOD, PillUser_.T)                  // SELECT u.* 一键展开整表

.select("p", PERIOD, parentT,                   // 父子表联合展开
        "c", PERIOD, childT)
```

**规则**: 所有参数是编译时确定的 `Selection` 对象，不涉及对 FROM 中导出表/列的引用。

---

#### 延迟 SELECT (Defer) — 引用 FROM 导出表/列

使用 `Consumer<_DeferSelectSpaceClause>` 在 Consumer 内部通过 `refField()` 引用 FROM table references：

```java
// 接口 Query._DynamicSelectClause
// _DeferSelectSpaceClause extends _StaticSelectSpaceClause, _DeferContextSpec

// 引用子查询中的导出列
.select(s -> s.space(SQLs.refField("us", "one")))

// 追加更多列 + SELECT alias.*
.select(s -> s.space(SQLs.refField("us", "one"))
        .comma("us", PERIOD, ASTERISK)
)

// 带 alias 的列
.select(s -> s.space(BankPerson_.id.as("userId"))
        .comma(refField("cr", "id").as("regionId"))
        .comma(refField("cr", "name").as("regionName"))
)
```

**关键**: `_DeferSelectSpaceClause` 提供 `_DeferContextSpec`（标记接口），Consumer 内部可直接调用 `refField()` 引用 FROM 中已声明的表/导出列。`space()` 和 `comma()` 方法与 Static 同名。

> **为什么需要 Defer？** FROM 子句尚未声明时无法访问 table references。
> `_DeferSelectSpaceClause` 提供延迟上下文，`refField()` 通过 `ContextStack.peek()` 在上下文注入后解析。

---

#### 动态 SELECT (Dynamic) — 不确定 selections

`Consumer<SelectionConsumer>` 用法类似 `_DeferSelectSpaceClause`，但方法名叫 `selection()`：

```java
// 接口 Query._DynamicSelectClause
// SelectionConsumer extends _DeferContextSpec

.selects(consumer -> {
    consumer.selection(PillUser_.id);
    consumer.selection(PillUser_.nickName);
    consumer.selection("sub", PERIOD, ASTERISK); // SELECT sub.*
})

// 带 modifier
.selects(DISTINCT, consumer -> { consumer.selection(PillUser_.id); })
```

**Defer vs Dynamic 区分**:
- **Defer** = `select(Consumer<_DeferSelectSpaceClause>)` → 方法名 `select` + `space/comma` API，selections 确定但需引用 FROM
- **Dynamic** = `selects(Consumer<SelectionConsumer>)` → 方法名 `selects` + `selection()` API，selections 不确定

---

#### Modifier 选择 (selectAll/selectDistinct)

```java
.selectAll()                                    // SELECT ALL

.selectDistinct()                               // SELECT DISTINCT

.select(List<Modifier>)                    // SELECT with modifiers

// Modifier 后 .space() 追加列 → _StandardSelectCommaClause
.selectAll().space(PillUser_.nickName).from(PillUser_.T, AS, "u")
```

---

#### 逗号分隔 / Space 追加

在 `_StandardSelectCommaClause` 或 `_StaticSelectSpaceClause` 上追加列：

```java
// === comma() 追加 (返回 _StandardSelectCommaClause) ===
.comma(PillUser_.nickName)                             // Selection 或 SqlField

// === space() 追加 (仅 selectAll/selectDistinct 后) ===
.selectAll().space(PillUser_.id).space(PillUser_.nickName).comma(PillUser_.status).from(...)
```

> **注意**: `_StaticSelectCommaClause` 中 comma/space 方法签名与 select 一致，
> `Function<String,Selection>` 变体见 Static 形式。

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
| **Table**           | `.from(PillUser_.T, AS, "u")`                       | → `_JoinSpec` |
| **Derived Table**   | `.from(SQLs.subQuery()).as("alias")`                | → `_JoinSpec` |
| **Derived+LATERAL** | `.from(SQLs.LATERAL, SQLs.subQuery()).as("a")`      | → `_JoinSpec` |
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

> **重要**: `StandardQuery._JoinSpec` **不包含** `straightJoin` / `ifStraightJoin`——这些是 MySQL 方言专属方法，不在标准
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
.where(PillUser_.id.equal(SQLs::literal, 1))

// 直接传 IPredicate
    .
    where(PillUser_.nickName.equal(SQLs::param, "脉兽秀秀"))

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
    and(PillUser_.createTime.notBetween(SQLs::literal, LocalDateTime.now().minusDays(1), AND, LocalDateTime.now()))
    .
    and(SQLs::exists, SQLs.scalarSubQuery().select(...).from(...).asQuery())
    .
    and(PillUser_.id::in, SQLs.subQuery().select(...).from(...).asQuery())
```

**实现**: `WhereClause.java`

---

### ⑦ GROUP BY

```java
// SimpleQueries.java line 448-520

.groupBy(item)                              // 单个
.
    groupBy(item1, item2)                // 2 个
.
    groupBy(item1, item2, item3)         // 3 个
.
    groupBy(item1, item2, item3, item4)  // 4 个
.
    groupBy(Consumer<Consumer<GroupByItem>>)   // 动态
.
    ifGroupBy(Consumer)                   // 条件动态

// 追加 group by 项 (逗号分隔)
.
    commaSpace(item)                      // 追加单个
.
    commaSpace(item1, item2)              // 追加 2 个
.
    commaSpace(item1, item2, item3)       // 追加 3 个
.
    commaSpace(item1, item2, item3, item4) // 追加 4 个
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
// SimpleQueries.java line 522-783，接口见 Query._StaticHavingClause

// --- Static (返回 _HavingAndSpec，可继续 .spaceAnd) ---
.having(IPredicate)                            // 静态单条件 → _HavingAndSpec
.
    having(Function<T, IPredicate>, T)         // method-ref → _HavingAndSpec
.
    having(Supplier<IPredicate>)               // lazy → _HavingAndSpec

// --- spaceAnd 系列 (追加 AND 条件) ---
.
    spaceAnd(IPredicate)                       // 追加 AND 条件 (短路: 如无 GROUP BY 则忽略)
.
    spaceAnd(Supplier<IPredicate>)              // 追加条件 (lazy)
.
    spaceAnd(Function<E, IPredicate>, E)       // method-ref 风格

// --- Dynamic (返回 _WindowSpec) ---
.
    having(Consumer<Consumer<IPredicate>>)     // 动态多条件 → _WindowSpec
.
    ifHaving(Consumer<Consumer<IPredicate>>)   // 条件动态 → _WindowSpec
```

> **注意**: HAVING 追加条件使用 `spaceAnd(...)`，而非 `and(...)`。
> `_HavingAndClause` 只有 `spaceAnd`/`ifSpaceAnd` 系列方法。
>
> **返回类型区分**: static `.having(...)` 返回 `_HavingAndSpec`（可继续 `.spaceAnd`），
> dynamic `.having(Consumer)` / `.ifHaving(Consumer)` 返回 `_WindowSpec`（直接跳过 spaceAnd 阶段）。

```java
.having(min(ChinaRegion_.regionGdp).greater(SQLs.literalValue(minGdp)))
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
    .partitionBy(...).orderBy(...).as()    // → _WindowCommaSpec
.
    comma("win2").as()                     // 追加第二个 window → _WindowCommaSpec
.
    windows(builder ->{                   // 动态 windows — 必须至少定义一个 window
        builder.window("w1").partitionBy(...)...as();
        builder.window("w2").partitionBy(...)...as();
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
    orderBy(s1, s2)                        // 2 个
.
    orderBy(s1, s2, s3)                    // 3 个
.
    orderBy(s1, s2, s3, s4)                // 4 个
.
    orderBy(Consumer<Consumer<SortItem>>)  // 动态
.
    ifOrderBy(Consumer)                     // 条件动态

// 追加排序项 (逗号分隔)
.
    spaceComma(item)                        // 追加单个
.
    spaceComma(item1, item2)                // 追加 2 个
.
    spaceComma(item1, item2, item3)         // 追加 3 个
.
    spaceComma(item1, item2, item3, item4)  // 追加 4 个
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

### ⑪ LIMIT

```java
// Statement.java _RowCountLimitClause + _LimitClause

// 基础 LIMIT — 仅行数
.limit(Object rowCount)                                  // Expression 或 Number
.
    limit(BiFunction<MappingType, Number, Expression>, long)  // operator + rowCount
.
    ifLimit(@Nullable Object)                           // 条件 (非 null 时)
.
    ifLimit(BiFunction, @Nullable Number)               // 条件 + operator

// 带 offset 的 LIMIT (offset 和 rowCount 二合一)
.
    limit(Expression offset, Expression rowCount)       // Expression + Expression
.
    limit(BiFunction, long offset, long rowCount)       // operator + offset + rowCount
.
    limit(BiFunction, Supplier<N>, Supplier<N>)         // Supplier offset + Supplier count
.
    limit(BiFunction, Function<String, ?>, String, String) // Function + keyName

// Consumer 风格
.
    limit(Consumer<BiConsumer<Expression, Expression>>) // defer offset+count

// 条件 Offset + Row Count
.
    ifLimit(BiFunction, Supplier<N>, Supplier<N>)       // Supplier 条件
.
    ifLimit(BiFunction, Function<String, ?>, String, String) // Function 条件
.
    ifLimit(Consumer<BiConsumer<Expression, Expression>>) // Consumer 条件
```

```java
.limit(SQLs::literal, 0, 10)                              // LIMIT 10 OFFSET 0
.
    limit(SQLs::literal, criteria::get, "offset", "rowCount") // 动态
```

**实现**: `LimitRowOrderByClause.java`

---

### ⑫ UNION

```java
// RowSet._StaticUnionClause 接口，由 StandardQuery._UnionClause 继承

.union()                    // UNION
.
    unionAll()             // UNION ALL
.
    unionDistinct()        // UNION DISTINCT
```

> **注意**: INTERSECT / EXCEPT / MINUS 方法**不**在标准 `SQLs.scalarSubQuery()` 链中。它们定义在 `RowSet._StaticIntersectClause` / `_StaticExceptClause` / `_StaticMinusClause` 中，仅由方言特定的 Query 接口（如 `MySQLQuery`、`PostgreQuery`、`OracleQuery`）提供。

**返回值**: `SelectSpec` (开始新查询，不支持 `.from` 直接开始 — 必须从 `.select` 或 `.parens` 开始)

```java
SQLs.scalarSubQuery()
    .
    parens(s ->s.select(...).from(...).asQuery())
    .
    union()
    .
    parens(s ->s.select(...).from(...).asQuery())
    .
    orderBy(...::desc)
    .
    limit(SQLs::literal, 10)
    .
    asQuery()
```

**规则**: UNION 后的 ORDER BY/LIMIT 作用于整个结果集。

---

### ⑬ asQuery() — 链尾

```java
// StandardQueries.java line 561-563
I onAsQuery() {
    return function.apply(this);
}
```

- `this` → `SubQuery` (SimpleSubQuery)
- `function` → `SCALAR_SUB_QUERY` (Expressions.scalarExpression)
- 返回 `Expression` (标量表达式，可用于 SELECT/WHERE 等子句)

**最终类型**: `Expression` — 可用于表达式上下文（SELECT 列、WHERE 条件、HAVING 条件等）

**使用示例** (StandardQueryUnitTests.java line 80-93):

```java
Select stmt;
stmt = SQLs.query()
    .select(SQLs.scalarSubQuery()
        .select(PillUser_.nickName)
        .from(PillUser_.T, AS, "u")
        .where(PillUser_.id.equal(SQLs::param, 1))
        .asQuery().as("r")
    )
    .asQuery();
```

---

## 实现类继承链

```
SQLSyntax (SQLs extends)
    └─ SQLs
        └─ .scalarSubQuery() → StandardQueries.subQuery()
            └─ SimpleSubQuery<Expression>
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

### 标量子查询特殊约束

> **重要警告**: 标量子查询必须保证只返回**一行一列**，否则运行时会抛出异常。
> 常见场景：
> - 用于 SELECT 列表中作为计算列
> - 用于 WHERE 条件中作为比较值
> - 用于 HAVING 条件中作为比较值

### 返回类型区别

| 方法 | 返回类型 | 用途 |
|------|---------|------|
| `SQLs.query()` | `Select` | 完整查询，用于 `session.query()` |
| `SQLs.subQuery()` | `SubQuery` | 子查询，用于 FROM/JOIN/IN/EXISTS |
| `SQLs.scalarSubQuery()` | `Expression` | 标量表达式，用于 SELECT/WHERE/HAVING |

### 可重复 vs 不可重复方法

| 方法 | 是否可重复 | 说明 |
|------|-----------|------|
| `.with(name)` | ❌ | WITH 关键字只能出现一次 |
| `.comma(name)` (CTE) | ✅ | 追加多个 CTE |
| `.select(...)` | ❌ | SELECT 关键字只能出现一次 |
| `.comma(...)` (SELECT) | ✅ | 追加多个选择项 |
| `.from(...)` | ❌ | FROM 关键字只能出现一次 |
| `.join(...)` / `.leftJoin(...)` 等 | ✅ | 多次 JOIN |
| `.where(...)` | ❌ | WHERE 关键字只能出现一次 |
| `.and(...)` | ✅ | 追加多个 AND 条件 |
| `.groupBy(...)` | ❌ | GROUP BY 关键字只能出现一次 |
| `.commaSpace(...)` (GROUP BY) | ✅ | 追加多个分组项 |
| `.having(...)` | ❌ | HAVING 关键字只能出现一次 |
| `.spaceAnd(...)` (HAVING) | ✅ | 追加多个 HAVING AND 条件 |
| `.window(...)` | ❌ | WINDOW 关键字只能出现一次 |
| `.comma(...)` (WINDOW) | ✅ | 追加多个窗口定义 |
| `.orderBy(...)` | ❌ | ORDER BY 关键字只能出现一次 |
| `.spaceComma(...)` (ORDER BY) | ✅ | 追加多个排序项 |
| `.limit(...)` | ❌ | LIMIT 关键字只能出现一次 |
| `.union()` / `.unionAll()` 等 | ✅ | 多次 UNION |

---

## 最佳实践

### 标量子查询使用场景

1. **在 SELECT 列表中使用**:
   ```java
   .select(
       PillUser_.id,
       SQLs.scalarSubQuery()
           .select(count(BankAccount_.id))
           .from(BankAccount_.T, AS, "a")
           .where(BankAccount_.userId::equal, PillUser_.id)
           .asQuery().as("accountCount")
   )
   ```

2. **在 WHERE 条件中使用**:
   ```java
   .where(PillUser_.balance.greater(
       SQLs.scalarSubQuery()
           .select(avg(PillUser_.balance))
           .from(PillUser_.T, AS, "u")
           .asQuery()
   ))
   ```

3. **使用 LIMIT 1 确保单行**:
   ```java
   .select(PillUser_.nickName)
   .from(PillUser_.T, AS, "u")
   .limit(SQLs::literal, 1)
   ```

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-06-06 | 初始版本，基于 Army 0.6.x |

