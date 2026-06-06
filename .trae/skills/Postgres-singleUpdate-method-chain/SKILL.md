---
name: Postgres.singleUpdate() method chain
description: 完整的 Army Criteria API Postgres.singleUpdate() 方法链知识。用于理解、解释或记录从 Postgres.singleUpdate() 到 asUpdate() 或 asReturningUpdate() 的完整 UPDATE 语句构建流程。涵盖 PostgreSQL 特有的 WITH、UPDATE、SET、FROM、JOIN、WHERE、RETURNING 等全部阶段。
---

# Postgres.singleUpdate() method chain — 完整参考

> **适用范围**: 本 Skill **仅限**用于理解、解释、记录 `Postgres.singleUpdate()` 入口的 PostgreSQL UPDATE 语句构建流程。
> 覆盖从 `Postgres.singleUpdate()` 到 `asUpdate()` 或 `asReturningUpdate()` 的完整方法链，包括 WITH、UPDATE、SET、FROM、JOIN、WHERE、RETURNING 全部 7 个阶段。**不**涵盖 `batchSingleUpdate()` 或其他方言。

> **源码依据**: 本 Skill 基于以下核心源文件编写——`Postgres.java`（入口定义）、
> `PostgreUpdate.java`（接口定义）、`PostgreUpdates.java`（实现）、`UpdateUnitTests.java`（示例）。所有描述均以实际源码接口签名和实现为准。

## 核心入口

```java
// Postgres.java line 249-252
public static PostgreUpdate._SingleWithSpec<Update, ReturningUpdate> singleUpdate() {
    return PostgreUpdates.simple();
}
```

**返回类型**: `PostgreUpdate._SingleWithSpec<Update, ReturningUpdate>` — 实现类是 `PostgreUpdates.PrimarySimpleUpdateClause`。

**内部实现**: `PostgreUpdates.simple()`:

```java
// PostgreUpdates.java line 79-82
static PostgreUpdate._SingleWithSpec<Update, ReturningUpdate> simple() {
    return new PrimarySimpleUpdateClause();
}
```

---

## 完整方法链 Diagram

> **阅读指南**: 每个叶子节点代表一个可直接调用的方法，带完整参数列表。
> 接口链通过返回类型导航。

```
Postgres.singleUpdate()  →  PostgreUpdate._SingleWithSpec<Update, ReturningUpdate>
│
├─① WITH (可选，仅一次声明；comma 追加 CTE 可重复)
│  ├─ [静态 WITH]
│  │  ├─ .with(String name)                                    →  PostgreQuery._StaticCteParensSpec<_SingleUpdateClause<Update, ReturningUpdate>>
│  │  │  ├─ .as(Function<SelectSpec<_CteComma>, _CteComma> function) → _CteComma
│  │  │  │  ├─ .comma(String name)                              → _StaticCteParensSpec (追加 CTE)
│  │  │  │  └─ .space()                                        → _SingleUpdateClause (结束 CTE)
│  │  │  ├─ .parens(String first, String... rest)              → _StaticCteAsClause
│  │  │  │  └─ .as(Function<SelectSpec<_CteComma>, _CteComma> function) → _CteComma
│  │  │  ├─ .parens(Consumer<Consumer<String>> consumer)       → _StaticCteAsClause
│  │  │  │  └─ .as(Function<SelectSpec<_CteComma>, _CteComma> function) → _CteComma
│  │  │  └─ .ifParens(Consumer<Consumer<String>> consumer)     → _StaticCteAsClause
│  │  │     └─ .as(Function<SelectSpec<_CteComma>, _CteComma> function) → _CteComma
│  │  └─ .withRecursive(String name)                           → _StaticCteParensSpec (同上 .as/.parens/.ifParens → .as → _CteComma → .comma/.space)
│  │
│  └─ [动态 WITH]
│     ├─ .with(Consumer<PostgreCtes> consumer)                → _SingleUpdateClause
│     ├─ .withRecursive(Consumer<PostgreCtes> consumer)       → _SingleUpdateClause
│     ├─ .ifWith(Consumer<PostgreCtes> consumer)              → _SingleUpdateClause
│     └─ .ifWithRecursive(Consumer<PostgreCtes> consumer)     → _SingleUpdateClause
│
├─② UPDATE (必须)
│  ├─ .update(TableMeta<T> table, SQLs.WordAs as, String tableAlias) → _SingleSetClause<Update, ReturningUpdate, FieldMeta<T>>
│  ├─ .update(SQLs.WordOnly only, TableMeta<T> table, SQLs.WordAs as, String tableAlias) → _SingleSetClause
│  └─ .update(TableMeta<T> table, SQLs.SymbolAsterisk star, SQLs.WordAs as, String tableAlias) → _SingleSetClause
│
├─③ SET (必须，至少一个字段赋值；可重复追加)
│  ├─ [静态 SET (单行)]
│  │  ├─ .set(F field, @Nullable Object value)                          → _SingleSetFromSpec
│  │  ├─ .set(F field, BiFunction<F, E, AssignmentItem> valueOperator, @Nullable E value) → _SingleSetFromSpec
│  │  ├─ .set(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator, 
│  │  │         BiFunction<F, E, Expression> valueOperator, @Nullable E value) → _SingleSetFromSpec
│  │  ├─ .ifSet(F field, @Nullable Object value)                        → _SingleSetFromSpec
│  │  ├─ .ifSet(F field, BiFunction<F, E, AssignmentItem> valueOperator, @Nullable E value) → _SingleSetFromSpec
│  │  └─ .ifSet(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator,
│  │            BiFunction<F, E, Expression> valueOperator, @Nullable E value) → _SingleSetFromSpec
│  │
│  ├─ [静态 SET (批量)]
│  │  ├─ .setSpace(F field, BiFunction<F, String, Expression> valueOperator) → _SingleSetFromSpec
│  │  └─ .setSpace(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator,
│  │                 BiFunction<F, String, Expression> valueOperator) → _SingleSetFromSpec
│  │
│  ├─ [静态 SET (行)]
│  │  ├─ .setRow(F field1, F field2, SubQuery subQuery) → _SingleSetFromSpec
│  │  ├─ .setRow(F field1, F field2, F field3, SubQuery subQuery) → _SingleSetFromSpec
│  │  ├─ .setRow(F field1, F field2, F field3, F field4, SubQuery subQuery) → _SingleSetFromSpec
│  │  ├─ .setRow(List<F> fieldList, SubQuery subQuery) → _SingleSetFromSpec
│  │  ├─ .setRow(Consumer<Consumer<F>> consumer, SubQuery subQuery) → _SingleSetFromSpec
│  │  ├─ .ifSetRow(F field1, F field2, Supplier<SubQuery> supplier) → _SingleSetFromSpec
│  │  ├─ .ifSetRow(F field1, F field2, F field3, Supplier<SubQuery> supplier) → _SingleSetFromSpec
│  │  ├─ .ifSetRow(F field1, F field2, F field3, F field4, Supplier<SubQuery> supplier) → _SingleSetFromSpec
│  │  ├─ .ifSetRow(List<F> fieldList, Supplier<SubQuery> supplier) → _SingleSetFromSpec
│  │  └─ .ifSetRow(Consumer<Consumer<F>> consumer, Supplier<SubQuery> supplier) → _SingleSetFromSpec
│  │
│  └─ [动态 SET]
│     └─ .sets(Consumer<UpdateStatement._BatchRowPairs<F>> consumer) → _SingleFromSpec
│
├─④ FROM (可选，PostgreSQL 特有；可使用 JOIN 连接其他表)
│  ├─ [基础 FROM]
│  │  ├─ .from(TableMeta<?> table) → _TableSampleJoinSpec
│  │  ├─ .from(SQLs.TableModifier modifier, TableMeta<?> table) → _TableSampleJoinSpec
│  │  ├─ .from(TableMeta<?> table, SQLs.WordAs as, String alias) → _TableSampleJoinSpec
│  │  ├─ .from(SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs as, String alias) → _TableSampleJoinSpec
│  │  ├─ .from(SubQuery subQuery, SQLs.WordAs as, String alias) → Statement._AsClause<_ParensJoinSpec>
│  │  ├─ .from(DerivedTable table) → Statement._AsClause<_ParensJoinSpec>
│  │  ├─ .from(UndoneFunction function) → PostgreStatement._FuncColumnDefinitionAsClause<_SingleJoinSpec>
│  │  ├─ .from(Function<_NestedLeftParenSpec<_SingleJoinSpec>, _SingleJoinSpec> function) → _SingleJoinSpec
│  │  └─ .from(Consumer<Consumer<String>> consumer) → _SingleJoinSpec
│  │
│  ├─ [TableSample (可选)]
│  │  ├─ .tableSample(Expression method) → _RepeatableJoinClause
│  │  ├─ .tableSample(BiFunction<BiFunction<MappingType, Expression, Expression>, Expression, Expression> method,
│  │  │                BiFunction<MappingType, Expression, Expression> valueOperator, Expression argument) → _RepeatableJoinClause
│  │  ├─ <E> .tableSample(BiFunction<BiFunction<MappingType, E, Expression>, E, Expression> method,
│  │  │                     BiFunction<MappingType, E, Expression> valueOperator, Supplier<E> supplier) → _RepeatableJoinClause
│  │  ├─ .tableSample(BiFunction<BiFunction<MappingType, Object, Expression>, Object, Expression> method,
│  │  │                BiFunction<MappingType, Object, Expression> valueOperator, Function<String, ?> function,
│  │  │                String keyName) → _RepeatableJoinClause
│  │  ├─ .ifTableSample(Supplier<Expression> supplier) → _RepeatableJoinClause
│  │  ├─ <E> .ifTableSample(BiFunction<BiFunction<MappingType, E, Expression>, E, Expression> method,
│  │  │                       BiFunction<MappingType, E, Expression> valueOperator, Supplier<E> supplier) → _RepeatableJoinClause
│  │  └─ .ifTableSample(BiFunction<BiFunction<MappingType, Object, Expression>, Object, Expression> method,
│  │                     BiFunction<MappingType, Object, Expression> valueOperator, Function<String, ?> function,
│  │                     String keyName) → _RepeatableJoinClause
│  │
│  ├─ [Repeatable (可选)]
│  │  ├─ .repeatable(Expression seed) → _SingleJoinSpec
│  │  ├─ .repeatable(Supplier<Expression> supplier) → _SingleJoinSpec
│  │  ├─ .repeatable(Function<Number, Expression> valueOperator, Number seedValue) → _SingleJoinSpec
│  │  ├─ <E extends Number> .repeatable(Function<E, Expression> valueOperator, Supplier<E> supplier) → _SingleJoinSpec
│  │  ├─ .repeatable(Function<Object, Expression> valueOperator, Function<String, ?> function, String keyName) → _SingleJoinSpec
│  │  ├─ .ifRepeatable(Supplier<Expression> supplier) → _SingleJoinSpec
│  │  ├─ <E extends Number> .ifRepeatable(Function<E, Expression> valueOperator, Supplier<E> supplier) → _SingleJoinSpec
│  │  └─ .ifRepeatable(Function<Object, Expression> valueOperator, Function<String, ?> function, String keyName) → _SingleJoinSpec
│  │
│  └─ [JOIN 操作 (可重复)]
│     ├─ .crossJoin(TableMeta<?> table) → _SingleJoinSpec
│     ├─ .crossJoin(SQLs.TableModifier modifier, TableMeta<?> table) → _SingleJoinSpec
│     ├─ .crossJoin(TableMeta<?> table, SQLs.WordAs as, String alias) → _SingleJoinSpec
│     ├─ .crossJoin(SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs as, String alias) → _SingleJoinSpec
│     ├─ .crossJoin(SubQuery subQuery, SQLs.WordAs as, String alias) → Statement._AsClause<_ParensJoinSpec>
│     ├─ .crossJoin(DerivedTable table) → Statement._AsClause<_ParensJoinSpec>
│     ├─ .crossJoin(UndoneFunction function) → PostgreStatement._FuncColumnDefinitionAsClause<_SingleJoinSpec>
│     ├─ .crossJoin(Function<_NestedLeftParenSpec<_SingleJoinSpec>, _SingleJoinSpec> function) → _SingleJoinSpec
│     ├─ .join(TableMeta<?> table) → _TableSampleOnSpec
│     ├─ .join(SQLs.TableModifier modifier, TableMeta<?> table) → _TableSampleOnSpec
│     ├─ .join(TableMeta<?> table, SQLs.WordAs as, String alias) → _TableSampleOnSpec
│     ├─ .join(SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs as, String alias) → _TableSampleOnSpec
│     ├─ .join(SubQuery subQuery, SQLs.WordAs as, String alias) → Statement._AsParensOnClause<_SingleJoinSpec>
│     ├─ .join(DerivedTable table) → Statement._AsParensOnClause<_SingleJoinSpec>
│     ├─ .join(UndoneFunction function) → PostgreStatement._FuncColumnDefinitionAsClause<Statement._OnClause<_SingleJoinSpec>>
│     ├─ .join(Function<_NestedLeftParenSpec<Statement._OnClause<_SingleJoinSpec>>, Statement._OnClause<_SingleJoinSpec>> function) → Statement._OnClause<_SingleJoinSpec>
│     ├─ .leftJoin(TableMeta<?> table) → _TableSampleOnSpec
│     ├─ .leftJoin(SQLs.TableModifier modifier, TableMeta<?> table) → _TableSampleOnSpec
│     ├─ .leftJoin(TableMeta<?> table, SQLs.WordAs as, String alias) → _TableSampleOnSpec
│     ├─ .leftJoin(SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs as, String alias) → _TableSampleOnSpec
│     ├─ .leftJoin(SubQuery subQuery, SQLs.WordAs as, String alias) → Statement._AsParensOnClause<_SingleJoinSpec>
│     ├─ .leftJoin(DerivedTable table) → Statement._AsParensOnClause<_SingleJoinSpec>
│     ├─ .leftJoin(UndoneFunction function) → PostgreStatement._FuncColumnDefinitionAsClause<Statement._OnClause<_SingleJoinSpec>>
│     ├─ .leftJoin(Function<_NestedLeftParenSpec<Statement._OnClause<_SingleJoinSpec>>, Statement._OnClause<_SingleJoinSpec>> function) → Statement._OnClause<_SingleJoinSpec>
│     ├─ .rightJoin(TableMeta<?> table) → _TableSampleOnSpec
│     ├─ .rightJoin(SQLs.TableModifier modifier, TableMeta<?> table) → _TableSampleOnSpec
│     ├─ .rightJoin(TableMeta<?> table, SQLs.WordAs as, String alias) → _TableSampleOnSpec
│     ├─ .rightJoin(SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs as, String alias) → _TableSampleOnSpec
│     ├─ .rightJoin(SubQuery subQuery, SQLs.WordAs as, String alias) → Statement._AsParensOnClause<_SingleJoinSpec>
│     ├─ .rightJoin(DerivedTable table) → Statement._AsParensOnClause<_SingleJoinSpec>
│     ├─ .rightJoin(UndoneFunction function) → PostgreStatement._FuncColumnDefinitionAsClause<Statement._OnClause<_SingleJoinSpec>>
│     ├─ .rightJoin(Function<_NestedLeftParenSpec<Statement._OnClause<_SingleJoinSpec>>, Statement._OnClause<_SingleJoinSpec>> function) → Statement._OnClause<_SingleJoinSpec>
│     ├─ .fullJoin(TableMeta<?> table) → _TableSampleOnSpec
│     ├─ .fullJoin(SQLs.TableModifier modifier, TableMeta<?> table) → _TableSampleOnSpec
│     ├─ .fullJoin(TableMeta<?> table, SQLs.WordAs as, String alias) → _TableSampleOnSpec
│     ├─ .fullJoin(SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs as, String alias) → _TableSampleOnSpec
│     ├─ .fullJoin(SubQuery subQuery, SQLs.WordAs as, String alias) → Statement._AsParensOnClause<_SingleJoinSpec>
│     ├─ .fullJoin(DerivedTable table) → Statement._AsParensOnClause<_SingleJoinSpec>
│     ├─ .fullJoin(UndoneFunction function) → PostgreStatement._FuncColumnDefinitionAsClause<Statement._OnClause<_SingleJoinSpec>>
│     ├─ .fullJoin(Function<_NestedLeftParenSpec<Statement._OnClause<_SingleJoinSpec>>, Statement._OnClause<_SingleJoinSpec>> function) → Statement._OnClause<_SingleJoinSpec>
│     ├─ .ifLeftJoin(Consumer<PostgreJoins> consumer) → _SingleJoinSpec
│     ├─ .ifJoin(Consumer<PostgreJoins> consumer) → _SingleJoinSpec
│     ├─ .ifRightJoin(Consumer<PostgreJoins> consumer) → _SingleJoinSpec
│     ├─ .ifFullJoin(Consumer<PostgreJoins> consumer) → _SingleJoinSpec
│     ├─ .ifCrossJoin(Consumer<PostgreCrosses> consumer) → _SingleJoinSpec
│     └─ [JOIN 后 ON]
│        ├─ .on(IPredicate predicate) → _SingleJoinSpec
│        ├─ .on(Function<T, IPredicate> expOperator, T operand) → _SingleJoinSpec
│        ├─ .on(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
│        │       BiFunction<TypedExpression, T, Expression> operator, T operand) → _SingleJoinSpec
│        ├─ .onIf(Supplier<IPredicate> supplier) → _SingleJoinSpec
│        ├─ .onIf(Function<T, IPredicate> expOperator, @Nullable T value) → _SingleJoinSpec
│        └─ .onIf(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
│                 BiFunction<TypedExpression, T, Expression> operator, @Nullable T value) → _SingleJoinSpec
│
├─⑤ WHERE (可选)
│  ├─ [静态 WHERE]
│  │  ├─ .where(IPredicate predicate)                                  → _SingleWhereAndSpec
│  │  ├─ .where(Function<T, IPredicate> expOperator, T operand)        → _SingleWhereAndSpec
│  │  ├─ .whereCurrentOf(String cursorName)                           → _ReturningSpec
│  │  ├─ .whereIf(Supplier<IPredicate> supplier)                       → _SingleWhereAndSpec
│  │  ├─ .whereIf(Function<T, IPredicate> expOperator, @Nullable T value) → _SingleWhereAndSpec
│  │  ├─ .whereIf(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
│  │  │            BiFunction<TypedExpression, T, Expression> operator, @Nullable T value) → _SingleWhereAndSpec
│  │  ├─ .whereIf(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator, 
│  │  │            SQLs.BiOperator operator, @Nullable T value) → _SingleWhereAndSpec
│  │  ├─ .whereIf(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator,
│  │  │            SQLs.BiOperator op, BiFunction<TypedExpression, T, Expression> func, @Nullable T value) → _SingleWhereAndSpec
│  │  ├─ .whereIf(BetweenValueOperator<T> expOperator, 
│  │  │            BiFunction<TypedExpression, T, Expression> operator, @Nullable T value1, 
│  │  │            SQLs.WordAnd and, @Nullable T value2) → _SingleWhereAndSpec
│  │  └─ .whereIf(BetweenDualOperator<T, U> expOperator,
│  │             BiFunction<TypedExpression, T, Expression> firstFuncRef, @Nullable T first,
│  │             SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, @Nullable U second) → _SingleWhereAndSpec
│  │
│  └─ [动态 WHERE]
│     ├─ .where(Consumer<Consumer<IPredicate>> consumer)               → _ReturningSpec
│     └─ .ifWhere(Consumer<Consumer<IPredicate>> consumer)             → _ReturningSpec
│
├─⑥ AND (可选，可重复；仅在静态 WHERE 后可用)
│  ├─ [静态 AND]
│  │  ├─ .and(IPredicate predicate)                                   → _SingleWhereAndSpec
│  │  ├─ .and(Function<T, IPredicate> expOperator, T operand)         → _SingleWhereAndSpec
│  │  ├─ .ifAnd(Supplier<IPredicate> supplier)                        → _SingleWhereAndSpec
│  │  ├─ .ifAnd(Function<T, IPredicate> expOperator, @Nullable T value) → _SingleWhereAndSpec
│  │  ├─ .ifAnd(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
│  │  │          BiFunction<TypedExpression, T, Expression> operator, @Nullable T value) → _SingleWhereAndSpec
│  │  ├─ .ifAnd(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator,
│  │  │          SQLs.BiOperator operator, @Nullable T value) → _SingleWhereAndSpec
│  │  ├─ .ifAnd(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator,
│  │  │          SQLs.BiOperator op, BiFunction<TypedExpression, T, Expression> func, @Nullable T value) → _SingleWhereAndSpec
│  │  ├─ .ifAnd(BetweenValueOperator<T> expOperator, 
│  │  │          BiFunction<TypedExpression, T, Expression> operator, @Nullable T value1,
│  │  │          SQLs.WordAnd and, @Nullable T value2) → _SingleWhereAndSpec
│  │  ├─ .ifAnd(BetweenDualOperator<T, U> expOperator,
│  │  │          BiFunction<TypedExpression, T, Expression> firstFuncRef, @Nullable T first,
│  │  │          SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, @Nullable U second) → _SingleWhereAndSpec
│  │  ├─ .ifAnd(Function<T, Expression> expOperator1, @Nullable T operand1,
│  │  │          BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand) → _SingleWhereAndSpec
│  │  └─ .ifAnd(ExpressionOperator<TypedExpression, T, Expression> expOperator1,
│  │             BiFunction<TypedExpression, T, Expression> operator, @Nullable T operand1,
│  │             BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand) → _SingleWhereAndSpec
│
├─⑦ RETURNING (可选，PostgreSQL 特有；不可重复)
│  ├─ [静态 RETURNING]
│  │  ├─ .returningAll()                                 → _DqlUpdateSpec<ReturningUpdate>
│  │  ├─ .returning(Selection selection)                → _StaticReturningCommaSpec
│  │  ├─ .returning(Selection selection1, Selection selection2) → _StaticReturningCommaSpec
│  │  ├─ .returning(Function<String, Selection> function, String alias) → _StaticReturningCommaSpec
│  │  ├─ .returning(Function<String, Selection> function1, String alias1,
│  │  │             Function<String, Selection> function2, String alias2) → _StaticReturningCommaSpec
│  │  ├─ .returning(Function<String, Selection> function, String alias, Selection selection) → _StaticReturningCommaSpec
│  │  ├─ .returning(Selection selection, Function<String, Selection> function, String alias) → _StaticReturningCommaSpec
│  │  ├─ .returning(String derivedAlias, SQLs.SymbolDot period, SQLs.SymbolAsterisk star) → _StaticReturningCommaSpec
│  │  ├─ .returning(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table) → _StaticReturningCommaSpec
│  │  ├─ <P> .returning(String parenAlias, SQLs.SymbolDot period1, ParentTableMeta<P> parent,
│  │  │                  String childAlias, SQLs.SymbolDot period2, ComplexTableMeta<P, ?> child) → _StaticReturningCommaSpec
│  │  ├─ .returning(TableField field1, TableField field2, TableField field3) → _StaticReturningCommaSpec
│  │  ├─ .returning(TableField field1, TableField field2, TableField field3, TableField field4) → _StaticReturningCommaSpec
│  │
│  ├─ [动态 RETURNING]
│  │  └─ .returning(Consumer<Returnings> consumer)      → _DqlUpdateSpec<ReturningUpdate>
│  │
│  └─ [RETURNING 逗号追加 (可重复)]
│     ├─ .comma(Selection selection)                    → _StaticReturningCommaSpec
│     ├─ .comma(Selection selection1, Selection selection2) → _StaticReturningCommaSpec
│     ├─ .comma(Function<String, Selection> function, String alias) → _StaticReturningCommaSpec
│     ├─ .comma(Function<String, Selection> function1, String alias1,
│     │          Function<String, Selection> function2, String alias2) → _StaticReturningCommaSpec
│     ├─ .comma(Function<String, Selection> function, String alias, Selection selection) → _StaticReturningCommaSpec
│     ├─ .comma(Selection selection, Function<String, Selection> function, String alias) → _StaticReturningCommaSpec
│     ├─ .comma(String derivedAlias, SQLs.SymbolDot period, SQLs.SymbolAsterisk star) → _StaticReturningCommaSpec
│     ├─ .comma(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table) → _StaticReturningCommaSpec
│     ├─ <P> .comma(String parenAlias, SQLs.SymbolDot period1, ParentTableMeta<P> parent,
│     │              String childAlias, SQLs.SymbolDot period2, ComplexTableMeta<P, ?> child) → _StaticReturningCommaSpec
│     ├─ .comma(TableField field1, TableField field2, TableField field3) → _StaticReturningCommaSpec
│     └─ .comma(TableField field1, TableField field2, TableField field3, TableField field4) → _StaticReturningCommaSpec
│
└─⑧ 结尾: asUpdate() 或 asReturningUpdate()
   ├─ [普通 UPDATE 结尾]
   │  └─ .asUpdate()                                                     → Update (可执行更新语句)
   │
   └─ [RETURNING UPDATE 结尾]
      └─ .asReturningUpdate()                                           → ReturningUpdate (可执行更新并返回结果)
```

---

## 逐层接口详解

### 0. 入口: `PostgreUpdate._SingleWithSpec<Update, ReturningUpdate>`

```java
// PostgreUpdate.java line 136-143
interface _SingleWithSpec<I extends Item, Q extends Item>
        extends _PostgreDynamicWithClause<_SingleUpdateClause<I, Q>>,
        PostgreQuery._PostgreStaticWithClause<_SingleUpdateClause<I, Q>>,
        _SingleUpdateClause<I, Q> {
}
```

`_SingleWithSpec` 组合了三种能力：动态 CTE、静态 CTE、UPDATE。所以 `Postgres.singleUpdate()` 返回的对象可以直接 `.update(...)`，也可以先用 `.with(...)` 定义 CTE。

---

### ① WITH (Common Table Expression)

> **语义约束**: `WITH` 关键字仅出现一次（即 `.with(name)` / `.withRecursive(name)` 只能调用一次）。
> 后续 CTE 通过 `_CteComma.comma(String name)` 追加，该方法返回 `_StaticCteParensSpec`，
> 从而形成 `.comma(name) → .parens(...) → .as(...) → .comma(name) → ...` 的循环链。
> `.space()` 是唯一退出 CTE 链、进入 UPDATE 的路径。
> **禁止**在 `.space()` 后再调用 `.with(name)`——类型系统不提供此路径。

**接口**: `_PostgreDynamicWithClause` + `PostgreQuery._PostgreStaticWithClause`

**静态 CTE (编译时已知)**:

```java
Postgres.singleUpdate()
    .with("cte_name")                                    // → _StaticCteParensSpec
    .parens("col1", "col2")                              // → _StaticCteAsClause (可选列别名)
    .as(s -> s.select(...).from(...).asQuery())          // → _CteComma (可继续 .comma(name) 追加更多 CTE)
    .space()                                             // → _SingleUpdateClause (结束 CTE，进入 UPDATE)
    .update(...)
```

**动态 CTE (运行时构建)**:

```java
Postgres.singleUpdate()
    .with(builder -> {                                  // builder 是 PostgreCtes
        builder.comma("cte1").parens("col").as(s -> s.select(...).asQuery());
        builder.comma("cte2").as(s -> s.select(...).asQuery());
    })                                                  // → _SingleUpdateClause
    .update(...)
```

**条件 CTE**:

```java
// 条件 with — 仅当满足条件时执行
Postgres.singleUpdate()
    .ifWith(builder -> {...})                           // → _SingleUpdateClause
    .ifWithRecursive(builder -> {...})                  // → _SingleUpdateClause
    .update(...)
```

---

### ② UPDATE

```java
// PostgreUpdate.java line 124-133
interface _SingleUpdateClause<I extends Item, Q extends Item> extends Item {
    <T> _SingleSetClause<I, Q, T> update(TableMeta<T> table, SQLs.WordAs as, String tableAlias);
    <T> _SingleSetClause<I, Q, T> update(@Nullable SQLs.WordOnly only, TableMeta<T> table,
                                         SQLs.WordAs as, String tableAlias);
    <T> _SingleSetClause<I, Q, T> update(TableMeta<T> table, @Nullable SQLs.SymbolAsterisk star,
                                         SQLs.WordAs as, String tableAlias);
}
```

**使用示例**:

```java
Postgres.singleUpdate()
    .update(ChinaRegion_.T, AS, "c")                   // → _SingleSetClause
    .set(...)

// 带 ONLY 修饰符
Postgres.singleUpdate()
    .update(ONLY, ChinaRegion_.T, AS, "c")             // → _SingleSetClause
    .set(...)
```

---

### ③ SET

#### 接口层次

```java
// PostgreUpdate.java line 96-101
interface _SingleSetClause<I extends Item, Q extends Item, T>
        extends UpdateStatement._StaticBatchRowSetClause<FieldMeta<T>, _SingleSetFromSpec<I, Q, T>>,
        UpdateStatement._DynamicSetClause<UpdateStatement._BatchRowPairs<FieldMeta<T>>, _SingleFromSpec<I, Q>> {
}
```

```java
// PostgreUpdate.java line 117-121
interface _SingleSetFromSpec<I extends Item, Q extends Item, T> extends _SingleFromSpec<I, Q>,
        _SingleSetClause<I, Q, T> {
}
```

```java
// UpdateStatement.java line 59-73
interface _StaticSetClause<F extends SqlField, SR> extends Item {
    SR set(F field, @Nullable Object value);
    <E> SR set(F field, BiFunction<F, E, AssignmentItem> valueOperator, @Nullable E value);
    <E> SR set(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator, 
               BiFunction<F, E, Expression> valueOperator, @Nullable E value);
    SR ifSet(F field, @Nullable Object value);
    <E> SR ifSet(F field, BiFunction<F, E, AssignmentItem> valueOperator, @Nullable E value);
    <E> SR ifSet(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator,
                 BiFunction<F, E, Expression> valueOperator, @Nullable E value);
}
```

```java
// UpdateStatement.java line 88-111
interface _StaticRowSetClause<F extends SqlField, SR> extends _StaticSetClause<F, SR> {
    SR setRow(F field1, F field2, SubQuery subQuery);
    SR setRow(F field1, F field2, F field3, SubQuery subQuery);
    SR setRow(F field1, F field2, F field3, F field4, SubQuery subQuery);
    SR setRow(List<F> fieldList, SubQuery subQuery);
    SR setRow(Consumer<Consumer<F>> consumer, SubQuery subQuery);
    SR ifSetRow(F field1, F field2, Supplier<SubQuery> supplier);
    SR ifSetRow(F field1, F field2, F field3, Supplier<SubQuery> supplier);
    SR ifSetRow(F field1, F field2, F field3, F field4, Supplier<SubQuery> supplier);
    SR ifSetRow(List<F> fieldList, Supplier<SubQuery> supplier);
    SR ifSetRow(Consumer<Consumer<F>> consumer, Supplier<SubQuery> supplier);
}
```

```java
// UpdateStatement.java line 51-55
interface _DynamicSetClause<B extends _ItemPairBuilder, SR> {
    SR sets(Consumer<B> consumer);
}
```

#### SET 的多种形式总览

| 形式 | 接口来源 | 参数类型 | 返回类型 | 使用场景 |
|------|---------|---------|---------|---------|
| **静态简单赋值** | `_StaticSetClause` | `field, value` | `_SingleSetFromSpec` | 简单的字段赋值，值确定 |
| **静态操作符赋值** | `_StaticSetClause` | `field, valueOperator, value` | `_SingleSetFromSpec` | 使用操作符赋值，如 `+=`、`-=` 等 |
| **静态双操作符赋值** | `_StaticSetClause` | `field, fieldOperator, valueOperator, value` | `_SingleSetFromSpec` | 双操作符赋值 |
| **条件 SET** | `_StaticSetClause` | `ifSet(...)` | `_SingleSetFromSpec` | 条件性赋值 |
| **批量 SET** | `_StaticBatchSetClause` | `setSpace(...)` | `_SingleSetFromSpec` | 批量更新场景 |
| **行 SET** | `_StaticRowSetClause` | `setRow(...)` | `_SingleSetFromSpec` | 使用子查询更新多个字段 |
| **条件行 SET** | `_StaticRowSetClause` | `ifSetRow(...)` | `_SingleSetFromSpec` | 条件性使用子查询更新 |
| **动态 SET** | `_DynamicSetClause` | `Consumer<_BatchRowPairs>` | `_SingleFromSpec` | 运行时动态确定赋值字段 |

#### 静态 SET 示例

```java
// 简单赋值
.set(ChinaRegion_.name, SQLs::param, "武侠江湖")

// 操作符赋值（如 +=）
.set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, addGdp)

// 条件 SET
.ifSet(ChinaRegion_.name, SQLs::param, "新名称")

// 行 SET（使用子查询）
.setRow(ChinaRegion_.regionGdp, ChinaRegion_.population, Postgres.subQuery()
    .select(HistoryChinaRegion_.regionGdp, HistoryChinaRegion_.population)
    .from(HistoryChinaRegion_.T, AS, "h")
    .where(HistoryChinaRegion_.id.equal(SQLs::literal, 1))
    .asQuery())
```

#### 动态 SET 示例

```java
.sets(s -> {
    s.set(ChinaRegion_.name, SQLs::param, "武侠江湖");
    s.set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, addGdp);
    s.setRow(ChinaRegion_.regionGdp, ChinaRegion_.population, subQuery);
})
```

---

### ④ FROM (PostgreSQL 特有，可 JOIN 其他表)

这是 PostgreSQL UPDATE 最强大的特性——可以在 UPDATE 中使用 FROM 子句关联其他表。

#### 接口层次

```java
// PostgreUpdate.java line 108-116
interface _SingleFromSpec<I extends Item, Q extends Item>
        extends _PostgreFromClause<_TableSampleJoinSpec<I, Q>, _ParensJoinSpec<I, Q>>,
        _PostgreFromUndoneFuncClause<_SingleJoinSpec<I, Q>>,
        _FromCteClause<_SingleJoinSpec<I, Q>>,
        _PostgreFromNestedClause<_SingleJoinSpec<I, Q>>,
        _SingleWhereClause<I, Q> {
}
```

```java
// PostgreUpdate.java line 90-94
interface _TableSampleJoinSpec<I extends Item, Q extends Item>
        extends _StaticTableSampleClause<_RepeatableJoinClause<I, Q>>, _SingleJoinSpec<I, Q> {
}
```

```java
// PostgreUpdate.java line 85-88
interface _RepeatableJoinClause<I extends Item, Q extends Item>
        extends PostgreQuery._RepeatableClause<_SingleJoinSpec<I, Q>>, _SingleJoinSpec<I, Q> {
}
```

#### FROM 的多种形式

| 形式 | 说明 | 使用场景 |
|------|------|---------|
| **基础 FROM** | 直接从表关联 | 简单的表关联更新 |
| **CTE FROM** | 从 CTE 关联 | 使用 WITH 子句中定义的 CTE |
| **子查询 FROM** | 从子查询关联 | 使用子查询作为关联源 |
| **函数 FROM** | 从表函数关联 | 使用 `unnest()`、`generate_series()` 等表函数 |
| **TableSample** | 采样表数据 | 大数据集随机采样更新 |
| **Repeatable** | 可重复采样种子 | 确保相同的采样结果 |

#### FROM + JOIN 完整示例

```java
Postgres.singleUpdate()
    .update(ChinaRegion_.T, AS, "c")
    .set(ChinaRegion_.name, SQLs::param, "新名称")
    .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, addGdp)
    // FROM 子句关联其他表
    .from(HistoryChinaRegion_.T, AS, "hc")
    // JOIN 更多表
    .leftJoin(Province_.T, AS, "p")
    .on(Province_.regionId::equal, ChinaRegion_.id)
    .join(City_.T, AS, "ct")
    .on(City_.provinceId::equal, Province_.id)
    .where(HistoryChinaRegion_.id::equal, ChinaRegion_.id)
    .asUpdate()
```

#### TableSample 示例

```java
Postgres.singleUpdate()
    .update(ChinaRegion_.T, AS, "c")
    .set(ChinaRegion_.name, SQLs::param, "采样更新")
    .from(LargeTable_.T, AS, "lt")
    .tableSample(SQLs.literal("BERNOULLI"), SQLs.literal(10))
    .repeatable(SQLs.literal(12345))
    .where(LargeTable_.id::equal, ChinaRegion_.id)
    .asUpdate()
```

---

### ⑤ WHERE

```java
// PostgreUpdate.java line 51-56
interface _SingleWhereClause<I extends Item, Q extends Item>
        extends _WhereClause<_ReturningSpec<I, Q>, _SingleWhereAndSpec<I, Q>>,
        _WhereCurrentOfClause<_ReturningSpec<I, Q>> {
}
```

#### WHERE 的多种形式总览

| 形式 | 接口来源 | 参数类型 | 返回类型 | 使用场景 |
|------|---------|---------|---------|---------|
| **静态 WHERE** | `_WhereClause` | `IPredicate` 或 `Function<T, IPredicate>` | `_SingleWhereAndSpec` | WHERE 条件确定，可继续 AND |
| **静态条件 WHERE** | `_WhereClause` | `whereIf(...)` | `_SingleWhereAndSpec` | 条件性添加 WHERE |
| **WHERE CURRENT OF** | `_WhereCurrentOfClause` | `String cursorName` | `_ReturningSpec` | 基于游标位置更新 |
| **动态 WHERE** | `_WhereClause` | `Consumer<Consumer<IPredicate>>` | `_ReturningSpec` | 运行时动态确定 WHERE 条件，直接跳过 AND |
| **动态条件 WHERE** | `_MinQueryWhereClause` | `ifWhere(Consumer<Consumer<IPredicate>>)` | `_ReturningSpec` | 条件性动态 WHERE |

#### 静态 WHERE 示例

```java
// 简单谓词
.where(ChinaRegion_.id.equal(SQLs::literal, 1))

// 方法引用风格（常用）
.where(ChinaRegion_.id::equal, SQLs::literal, 1)

// between
.where(ChinaRegion_.id.between(SQLs::literal, 1, AND, 100))

// 条件 WHERE
.whereIf(ChinaRegion_.id::equal, SQLs::literal, nullableId)

// WHERE CURRENT OF
.whereCurrentOf("my_cursor")
```

#### 动态 WHERE 示例

```java
.where(whereClause -> {
    whereClause.accept(ChinaRegion_.id.equal(SQLs::literal, 1));
    whereClause.accept(ChinaRegion_.name.equal(SQLs::param, "武侠江湖"));
})
```

---

### ⑥ AND

```java
// PostgreUpdate.java line 46-49
interface _SingleWhereAndSpec<I extends Item, Q extends Item>
        extends UpdateStatement._UpdateWhereAndClause<_SingleWhereAndSpec<I, Q>>, _ReturningSpec<I, Q> {
}
```

```java
// UpdateStatement.java line 145-167
interface _UpdateWhereAndClause<WA> extends Statement._WhereAndClause<WA> {
    <T> WA ifAnd(Function<T, Expression> expOperator1, @Nullable T operand1,
                 BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand);
    <T> WA ifAnd(ExpressionOperator<TypedExpression, T, Expression> expOperator1,
                 BiFunction<TypedExpression, T, Expression> operator, @Nullable T operand1,
                 BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand);
}
```

**注意**: AND 只能在静态 WHERE 后使用，不能在动态 WHERE 后使用。

**使用示例**:

```java
.where(ChinaRegion_.id.equal(SQLs::literal, 1))
.and(ChinaRegion_.name.equal(SQLs::param, "武侠江湖"))
.and(ChinaRegion_.regionGdp.greater(SQLs::literal, BigDecimal.ZERO))
.ifAnd(ChinaRegion_.status::equal, SQLs::literal, nullableStatus)
```

---

### ⑦ RETURNING (PostgreSQL 特有)

这是 PostgreSQL 的强大特性——UPDATE 语句可以返回更新后的数据。

#### 接口层次

```java
// PostgreUpdate.java line 38-43
interface _ReturningSpec<I extends Item, Q extends Item>
        extends _StaticDmlReturningClause<_StaticReturningCommaSpec<Q>>,
        _DynamicReturningClause<_DqlUpdateSpec<Q>>,
        _DmlUpdateSpec<I> {
}
```

```java
// PostgreUpdate.java line 32-36
interface _StaticReturningCommaSpec<Q extends Item>
        extends _StaticDmlReturningCommaClause<_StaticReturningCommaSpec<Q>>,
        _DqlUpdateSpec<Q> {
}
```

#### RETURNING 的多种形式总览

| 形式 | 说明 | 使用场景 |
|------|------|---------|
| **returningAll()** | 返回所有列 | 需要返回更新后的整行数据 |
| **静态列选择** | 指定单个或多个列 | 只需要特定列的更新后数据 |
| **表选择** | 返回整个表的所有列 | 从特定表返回所有列 |
| **派生表选择** | 返回 CTE 或子查询的列 | 从关联的 CTE 返回数据 |
| **动态选择** | 运行时确定返回列 | 灵活的动态列选择 |

#### RETURNING 示例

```java
// 返回所有列
.returningAll()

// 返回特定列
.returning(ChinaRegion_.id, ChinaRegion_.name)
.comma(ChinaRegion_.regionGdp)

// 返回整个表
.returning("c", PERIOD, ChinaRegion_.T)

// 返回关联表的列
.returning(ChinaRegion_.id, ChinaRegion_.name)
.comma(SQLs.field("hc", HistoryChinaRegion_.regionGdp).as("old_gdp"))

// 动态选择
.returning(r -> {
    r.accept(ChinaRegion_.id);
    r.accept(ChinaRegion_.name);
    r.accept(ChinaRegion_.regionGdp);
})

// 完整示例
.asReturningUpdate()
```

---

### ⑧ 链尾: asUpdate() 或 asReturningUpdate()

```java
// Statement.java 
interface _DmlUpdateSpec<I extends Item> extends Item {
    I asUpdate();
}

interface _DqlUpdateSpec<Q extends Item> extends Item {
    Q asReturningUpdate();
}
```

**最终类型**: 
- `Update` — 可传给 `session.update(update)`
- `ReturningUpdate` — 可传给 `session.update(returningUpdate)` 获取返回结果

**完整示例 1: 普通 UPDATE (来自 UpdateUnitTests.java)**:

```java
final Update stmt;
stmt = Postgres.singleUpdate()
    .update(ChinaRegion_.T, AS, "c")
    .set(ChinaRegion_.name, SQLs::param, "武侠江湖")
    .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::literal, new BigDecimal("100.00"))
    .setRow(ChinaRegion_.regionGdp, ChinaRegion_.population, Postgres.subQuery()
        .select(HistoryChinaRegion_.regionGdp, HistoryChinaRegion_.population)
        .from(HistoryChinaRegion_.T, AS, "h")
        .where(HistoryChinaRegion_.id.equal(SQLs::literal, 1))
        .asQuery())
    .from(HistoryChinaRegion_.T, AS, "hc")
    .where(HistoryChinaRegion_.id::equal, ChinaRegion_.id)
    .asUpdate();
```

**完整示例 2: RETURNING UPDATE (来自 UpdateUnitTests.java)**:

```java
final ReturningUpdate stmt;
stmt = Postgres.singleUpdate()
    .update(ChinaRegion_.T, AS, "c")
    .set(ChinaRegion_.name, SQLs::param, "武侠江湖")
    .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::literal, new BigDecimal("100.00"))
    .setRow(ChinaRegion_.regionGdp, ChinaRegion_.population, Postgres.subQuery()
        .select(HistoryChinaRegion_.regionGdp, HistoryChinaRegion_.population)
        .from(HistoryChinaRegion_.T, AS, "h")
        .where(HistoryChinaRegion_.id.equal(SQLs::literal, 1))
        .asQuery())
    .ifSetRow(ChinaRegion_.regionGdp, ChinaRegion_.population, Postgres.subQuery()
        .select(HistoryChinaRegion_.regionGdp, HistoryChinaRegion_.population)
        .from(HistoryChinaRegion_.T, AS, "h")
        .where(HistoryChinaRegion_.id.equal(SQLs::literal, 1))
        ::asQuery)
    .from(HistoryChinaRegion_.T, AS, "hc")
    .where(HistoryChinaRegion_.id::equal, ChinaRegion_.id)
    .returning("c", PERIOD, ChinaRegion_.T)
    .comma(SQLs.field("hc", HistoryChinaRegion_.regionGdp).as("old_gdp"))
    .asReturningUpdate();
```

---

## 实现类继承链

```
SQLSyntax (SQLs extends)
  └─ Postgres
      └─ .singleUpdate() → PostgreUpdates.simple()
          └─ PrimarySimpleUpdateClause
              └─ PostgreUpdates
                  └─ PostgreSimpleUpdate
                      └─ PostgreUpdates (基类)
                          └─ JoinableUpdate
                              └─ ...
```

---

## 关键约束和规则

### 层级约束 (通过接口链强制执行)

接口名称本身就编码了当前上下文中可用的方法，DSL 通过**返回类型引导**下一个可调用的方法：

1. **WITH → UPDATE**: 必须通过 `.space()` 或动态 WITH 结束 CTE 才能进入 UPDATE
2. **UPDATE → SET**: 必须先 `.update()` 才能 `.set()`
3. **SET → FROM/WHERE**: 必须至少有一个 `.set()` 才能进入 FROM 或 WHERE
4. **FROM → JOIN**: FROM 后可以重复 JOIN
5. **JOIN → ON**: JOIN 后必须有 ON
6. **WHERE → AND**: 只有静态 WHERE 才能继续 `.and()`，动态 WHERE 直接到 RETURNING 或结束
7. **AND → AND**: `.and()` 可以重复调用，每次都返回 `_SingleWhereAndSpec`
8. **任意 → RETURNING**: 在 SET、WHERE、AND 之后都可以直接 `.returning(...)`
9. **任意 → asUpdate()**: 任何阶段都可以直接 `.asUpdate()` 结束

### 可重复 vs 不可重复

| 子句 | 可重复 | 说明 |
|-----|-------|------|
| WITH (`.with(name)`) | ❌ | WITH 关键字只能出现一次 |
| CTE (`.comma(name)`) | ✅ | 可以追加多个 CTE |
| UPDATE (`.update()`) | ❌ | 只能更新一个表 |
| SET (`.set()`) | ✅ | 可以设置多个字段 |
| FROM (`.from()`) | ❌ | FROM 关键字只能出现一次 |
| JOIN (`.join()`, `.leftJoin()` 等) | ✅ | 可以 JOIN 多个表 |
| WHERE (`.where()`) | ❌ | WHERE 关键字只能出现一次 |
| AND (`.and()`) | ✅ | 可以追加多个 AND 条件 |
| RETURNING (`.returning(...)`) | ❌ | RETURNING 关键字只能出现一次 |
| RETURNING comma (`.comma(...)`) | ✅ | 可以追加多个返回列 |

### PostgreSQL 特有特性

1. **FROM 子句**: UPDATE 可以关联其他表（这是 PostgreSQL 特有，MySQL 使用 `UPDATE ... JOIN` 语法不同）
2. **RETURNING 子句**: UPDATE 可以返回更新后的数据
3. **ONLY 修饰符**: 可以只更新基表，不更新继承表
4. **TableSample**: 可以对 FROM 中的表进行采样
5. **WITH 子句**: 支持 CTE（通用表表达式）

---

## 与 SQLs.singleUpdate() 的区别

| 特性 | SQLs.singleUpdate() | Postgres.singleUpdate() |
|------|-------------------|------------------------|
| **方言** | 标准 SQL | PostgreSQL 特有 |
| **FROM 子句** | ❌ 不支持 | ✅ 支持，可以关联其他表 |
| **RETURNING 子句** | ❌ 不支持 | ✅ 支持，可以返回更新后的数据 |
| **ONLY 修饰符** | ❌ 不支持 | ✅ 支持 |
| **TableSample** | ❌ 不支持 | ✅ 支持 |
| **返回类型** | `Update` | `Update` 或 `ReturningUpdate` |
