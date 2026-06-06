---
name: "Postgres-query-method-chain"
description: "完整的 Army Criteria API Postgres.query() 方法链知识。用于理解、解释或记录从 Postgres.query() 到 asQuery() 的完整 PostgreSQL 查询构建流程。 Invoke when user asks about Postgres.query() method chain, wants to build Postgres-specific SELECT queries, or needs help with Postgres dialect-specific SQL features."
---

# Postgres.query() 方法链完整参考

## 适用范围

本 Skill 仅限于理解、解释或记录 `Postgres.query()` 入口的 PostgreSQL 专有 SELECT 查询构建流程。涵盖从 `Postgres.query()` 到 `asQuery()` 的完整方法链，包括 PostgreSQL 特有的 WITH、DISTINCT ON、SELECT、FROM、JOIN、WHERE、AND、GROUP BY（支持 ALL/DISTINCT 修饰符）、HAVING、WINDOW、ORDER BY、LIMIT、FETCH、FOR UPDATE/NO KEY UPDATE/FOR SHARE/FOR KEY SHARE、UNION/INTERSECT/EXCEPT、CTE SEARCH/CYCLE 等全部阶段。

## 源码依据

本 Skill 基于以下核心源文件编写：
- `Postgres.java`（入口定义）
- `PostgreQuery.java`（接口定义）
- `PostgreQueries.java`（实现）
- `QueryUnitTests.java`（单元测试示例）

所有描述均以实际源码接口签名和实现为准。

## 核心入口

```java
// Postgres.java
public static PostgreQuery.WithSpec<Select> query() {
    return PostgreQueries.simpleQuery();
}
```

**返回类型**：`PostgreQuery.WithSpec<Select>` — 实现类是 `PostgreQueries.SimpleSelect<Select>`。

**内部实现**：`PostgreQueries.simpleQuery()`：

```java
// PostgreQueries.java
static WithSpec<Select> simpleQuery() {
    return new SimpleSelect<>(null, null, PostgreQueries::postgreIdentitySelect);
}
```

---

## 完整方法链 Diagram

### 0. 入口

```
Postgres.query() → PostgreQuery.WithSpec<Select>
```

### 1. WITH 阶段（可选）

```
├─ [静态 WITH]
│  ├─ .with(String name) → _StaticCteParensSpec
│  │  ├─ .parens(String first, String... rest) → _StaticCteAsClause
│  │  ├─ .parens(Consumer<Consumer<String>> consumer) → _StaticCteAsClause
│  │  ├─ .ifParens(Consumer<Consumer<String>> consumer) → _StaticCteAsClause
│  │  └─ [通过 .as() 进入 CTE 内容]
│  │     └─ .as(Function<StaticCteComplexCommandSpec, CteComma> function) → _CteComma
│  │        ├─ .comma(String name) → _StaticCteParensSpec（追加 CTE）
│  │        └─ .space() → SelectSpec（结束 CTE，进入 SELECT）
│  └─ .withRecursive(String name) → _StaticCteParensSpec（同上流程）
│
├─ [动态 WITH]
│  ├─ .with(Consumer<PostgreCtes> consumer) → SelectSpec
│  ├─ .withRecursive(Consumer<PostgreCtes> consumer) → SelectSpec
│  ├─ .ifWith(Consumer<PostgreCtes> consumer) → SelectSpec
│  └─ .ifWithRecursive(Consumer<PostgreCtes> consumer) → SelectSpec
│
└─ [CTE SEARCH/CYCLE（仅递归 CTE）]
   ├─ .search(Consumer<SearchBreadthDepthClause> consumer) → _CteComma
   │  ├─ .breadthFirstBy(String... columns) → _SetSearchSeqColumnClause
   │  │  └─ .set(String seqColumn)
   │  ├─ .depthFirstBy(String... columns) → _SetSearchSeqColumnClause
   │  │  └─ .set(String seqColumn)
   │  └─ （类似的 Consumer 变体）
   └─ .cycle(Consumer<CteCycleColumnNameSpace> consumer) → _CteComma
      └─ .space(String... cycleColumns) → _SetCycleMarkColumnClause
         └─ .set(String markColumn) → _CycleToMarkValueSpec
            ├─ .to(Expression markValue, WordDefault, Expression markDefault) → _CyclePathColumnClause
            │  └─ .using(String pathColumn)
            └─ （类似的 Consumer/if 变体）
```

### 2. SELECT 阶段（必须）

```
├─ [PostgreSQL 特有：DISTINCT ON]
│  ├─ .selectsDistinctOn(Consumer<SelectionConsumer> distinctOnConsumer, 
│  │                     Consumer<SelectionConsumer> selectConsumer) → FromSpec
│  ├─ .selectDistinctOn(GroupByItem item, ...) → _PostgreSelectCommaSpec
│  └─ [其他 selectDistinctOn 变体]
│
├─ [修饰符 SELECT]
│  ├─ .select(List<Postgres.Modifier> modifiers, Selection selection) → _PostgreSelectCommaSpec
│  ├─ .select(List<Postgres.Modifier> modifiers, Consumer<DeferSelectSpaceClause> consumer) → FromSpec
│  ├─ .selects(List<Postgres.Modifier> modifiers, Consumer<SelectionConsumer> consumer) → FromSpec
│  ├─ .selectAll() → _StaticSelectSpaceClause
│  │  └─ .space(Selection selection) → _PostgreSelectCommaSpec
│  ├─ .selectDistinct() → _StaticSelectSpaceClause
│  │  └─ .space(Selection selection) → _PostgreSelectCommaSpec
│  └─ [其他修饰符变体]
│
├─ [静态 SELECT（不引用 FROM）]
│  ├─ .select(Selection selection) → _PostgreSelectCommaSpec
│  ├─ .select(Selection selection1, Selection selection2) → _PostgreSelectCommaSpec
│  ├─ .select(Selection selection1, Selection selection2, Selection selection3) → _PostgreSelectCommaSpec
│  ├─ .select(Selection selection1, Selection selection2, Selection selection3, Selection selection4) → _PostgreSelectCommaSpec
│  ├─ .select(Function<String, Selection> function, String alias) → _PostgreSelectCommaSpec
│  ├─ .select(Function<String, Selection> function1, String alias1, Selection selection2) → _PostgreSelectCommaSpec
│  ├─ .select(Selection selection1, Function<String, Selection> function2, String alias2) → _PostgreSelectCommaSpec
│  ├─ .select(Function<String, Selection> function1, String alias1, Function<String, Selection> function2, String alias2) → _PostgreSelectCommaSpec
│  └─ [select(String alias, WordDot, TableMeta) 等变体]
│
├─ [延迟 SELECT（引用 FROM）]
│  └─ .select(Consumer<DeferSelectSpaceClause> consumer) → FromSpec
│
├─ [动态 SELECT]
│  └─ .selects(Consumer<SelectionConsumer> consumer) → FromSpec
│
└─ [逗号追加选择项]
   └─ _PostgreSelectCommaSpec 上的 .comma(...) 变体与 .select(...) 一致
```

### 3. FROM 阶段（必须）

```
├─ [基础表 FROM]
│  ├─ .from(TableMeta<?> table, WordAs, String alias) → _TableSampleJoinSpec
│  ├─ .from(TableModifier modifier, TableMeta<?> table, WordAs, String alias) → _TableSampleJoinSpec
│  └─ [变体：支持 ONLY 修饰符]
│
├─ [派生表 FROM]
│  ├─ .from(DerivedTable table) → _AsClause<_ParensJoinSpec>
│  │  └─ .as(String alias) → _ParensJoinSpec
│  ├─ .from(DerivedModifier modifier, DerivedTable table) → _AsClause<_ParensJoinSpec>
│  │  └─ .as(String alias) → _ParensJoinSpec
│  ├─ .from(Supplier<? extends DerivedTable> supplier) → _AsClause<_ParensJoinSpec>
│  │  └─ .as(String alias) → _ParensJoinSpec
│  ├─ .from(DerivedModifier modifier, Supplier<? extends DerivedTable> supplier) → _AsClause<_ParensJoinSpec>
│  │  └─ .as(String alias) → _ParensJoinSpec
│  └─ [_ParensJoinSpec 上的可选 .parens(...) 定义列别名]
│
├─ [CTE FROM]
│  ├─ .from(String cteName) → _JoinSpec
│  └─ .from(String cteName, WordAs, String alias) → _JoinSpec
│
├─ [表函数 FROM（PostgreSQL 特有）]
│  └─ .from(UndoneFunction func) → _FuncColumnDefinitionAsClause<_JoinSpec>
│     └─ .as(String alias).parens(String... columnAliases) → _JoinSpec
│
└─ [嵌套 FROM]
   └─ .from(Function<_NestedLeftParenSpec<_JoinSpec>, _JoinSpec> function) → _JoinSpec
```

### 3.1 TABLESAMPLE（PostgreSQL 特有，可选）

```
└─ _TableSampleJoinSpec 上：
   ├─ .tableSample(Expression method) → _RepeatableJoinClause
   ├─ .tableSample(BiFunction<BiFunction<MappingType, Expression, Expression>, Expression, Expression> method, 
   │               BiFunction<MappingType, Expression, Expression> valueOperator, Expression argument) → _RepeatableJoinClause
   ├─ .tableSample(BiFunction<BiFunction<MappingType, E, Expression>, E, Expression> method,
   │               BiFunction<MappingType, E, Expression> valueOperator, Supplier<E> supplier) → _RepeatableJoinClause
   ├─ .tableSample(BiFunction<BiFunction<MappingType, Object, Expression>, Object, Expression> method,
   │               BiFunction<MappingType, Object, Expression> valueOperator, Function<String, ?> function, String keyName) → _RepeatableJoinClause
   ├─ .ifTableSample(Supplier<Expression> supplier) → _RepeatableJoinClause
   └─ [其他 ifTableSample 变体]
```

### 3.2 REPEATABLE（PostgreSQL 特有，可选）

```
└─ _RepeatableJoinClause 上：
   ├─ .repeatable(Expression seed) → _JoinSpec
   ├─ .repeatable(Supplier<Expression> supplier) → _JoinSpec
   ├─ .repeatable(Function<Number, Expression> valueOperator, Number seedValue) → _JoinSpec
   ├─ .repeatable(Function<E, Expression> valueOperator, Supplier<E> supplier) → _JoinSpec
   ├─ .repeatable(Function<Object, Expression> valueOperator, Function<String, ?> function, String keyName) → _JoinSpec
   ├─ .ifRepeatable(Supplier<Expression> supplier) → _JoinSpec
   └─ [其他 ifRepeatable 变体]
```

### 4. JOIN 阶段（可选，可重复）

```
├─ [CROSS JOIN]
│  ├─ .crossJoin(TableMeta<?> table) → _TableSampleJoinSpec
│  ├─ .crossJoin(TableModifier modifier, TableMeta<?> table, WordAs, String alias) → _TableSampleJoinSpec
│  ├─ .crossJoin(DerivedTable table) → _AsClause<_ParensJoinSpec>
│  ├─ .crossJoin(DerivedModifier modifier, DerivedTable table) → _AsClause<_ParensJoinSpec>
│  ├─ .crossJoin(Supplier<? extends DerivedTable> supplier) → _AsClause<_ParensJoinSpec>
│  ├─ .crossJoin(DerivedModifier modifier, Supplier<? extends DerivedTable> supplier) → _AsClause<_ParensJoinSpec>
│  ├─ .crossJoin(String cteName) → _JoinSpec
│  ├─ .crossJoin(String cteName, WordAs, String alias) → _JoinSpec
│  ├─ .crossJoin(UndoneFunction func) → _FuncColumnDefinitionAsClause<_JoinSpec>
│  ├─ .crossJoin(Function<_NestedLeftParenSpec<_JoinSpec>, _JoinSpec> function) → _JoinSpec
│  └─ .ifCrossJoin(Consumer<PostgreCrosses> consumer) → _JoinSpec
│
├─ [INNER JOIN]
│  ├─ .join(TableMeta<?> table) → _TableSampleOnSpec
│  ├─ .join(TableModifier modifier, TableMeta<?> table, WordAs, String alias) → _TableSampleOnSpec
│  ├─ .join(DerivedTable table) → _AsParensOnClause<_JoinSpec>
│  ├─ .join(DerivedModifier modifier, DerivedTable table) → _AsParensOnClause<_JoinSpec>
│  ├─ .join(Supplier<? extends DerivedTable> supplier) → _AsParensOnClause<_JoinSpec>
│  ├─ .join(DerivedModifier modifier, Supplier<? extends DerivedTable> supplier) → _AsParensOnClause<_JoinSpec>
│  ├─ .join(String cteName) → _OnClause<_JoinSpec>
│  ├─ .join(String cteName, WordAs, String alias) → _OnClause<_JoinSpec>
│  ├─ .join(UndoneFunction func) → _FuncColumnDefinitionAsClause<_OnClause<_JoinSpec>>
│  ├─ .join(Function<_NestedLeftParenSpec<_OnClause<_JoinSpec>>, _OnClause<_JoinSpec>> function) → _OnClause<_JoinSpec>
│  ├─ .ifJoin(Consumer<PostgreJoins> consumer) → _JoinSpec
│  └─ [JOIN 后进入 _TableSampleOnSpec，可继续 .tableSample/.repeatable，然后 .on(...) → _JoinSpec]
│
├─ [LEFT JOIN]
│  └─ 与 .join(...) 变体相同，返回类型相同
│
├─ [RIGHT JOIN]
│  └─ 与 .join(...) 变体相同，返回类型相同
│
├─ [FULL JOIN]
│  └─ 与 .join(...) 变体相同，返回类型相同
│
└─ [ON 子句]
   └─ _OnClause 上：
      ├─ .on(IPredicate predicate) → _JoinSpec
      ├─ .on(Function<T, IPredicate> expOperator, T operand) → _JoinSpec
      ├─ .on(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
      │       BiFunction<TypedExpression, T, Expression> valueOperator, T operand) → _JoinSpec
      ├─ .onIf(Supplier<IPredicate> supplier) → _JoinSpec
      ├─ .onIf(Function<T, IPredicate> expOperator, T operand) → _JoinSpec
      ├─ .on(Consumer<Consumer<IPredicate>> consumer) → _JoinSpec
      └─ .ifOn(Consumer<Consumer<IPredicate>> consumer) → _JoinSpec
```

### 5. WHERE 阶段（可选）

```
├─ [静态 WHERE]
│  ├─ .where(IPredicate predicate) → _WhereAndSpec
│  ├─ .where(Function<T, IPredicate> expOperator, T operand) → _WhereAndSpec
│  ├─ .where(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
│  │          BiFunction<TypedExpression, T, Expression> valueOperator, T operand) → _WhereAndSpec
│  ├─ .whereIf(Supplier<IPredicate> supplier) → _WhereAndSpec
│  ├─ .whereIf(Function<T, IPredicate> expOperator, T operand) → _WhereAndSpec
│  ├─ .whereIf(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
│  │           BiFunction<TypedExpression, T, Expression> valueOperator, T operand) → _WhereAndSpec
│  ├─ .whereIf(BiFunction<BiOperator, T, IPredicate> expOperator, BiOperator operator, T operand) → _WhereAndSpec
│  ├─ .whereIf(TeFunction<BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator,
│  │           BiOperator operator, BiFunction<TypedExpression, T, Expression> func, T operand) → _WhereAndSpec
│  ├─ .whereIf(BetweenValueOperator<T> expOperator,
│  │           BiFunction<TypedExpression, T, Expression> valueOperator, T lower, WordAnd, T upper) → _WhereAndSpec
│  └─ .whereIf(BetweenDualOperator<T, U> expOperator,
│              BiFunction<TypedExpression, T, Expression> func1, T lower, WordAnd,
│              BiFunction<TypedExpression, U, Expression> func2, U upper) → _WhereAndSpec
│
└─ [动态 WHERE]
   ├─ .where(Consumer<Consumer<IPredicate>> consumer) → _GroupBySpec
   └─ .ifWhere(Consumer<Consumer<IPredicate>> consumer) → _GroupBySpec
```

### 6. AND 阶段（可选，可重复）

```
└─ _WhereAndSpec 上：
   ├─ .and(IPredicate predicate) → _WhereAndSpec
   ├─ .and(Function<T, IPredicate> expOperator, T operand) → _WhereAndSpec
   ├─ .and(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
   │       BiFunction<TypedExpression, T, Expression> valueOperator, T operand) → _WhereAndSpec
   ├─ .ifAnd(Supplier<IPredicate> supplier) → _WhereAndSpec
   ├─ .ifAnd(Function<T, IPredicate> expOperator, T operand) → _WhereAndSpec
   ├─ .ifAnd(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
   │        BiFunction<TypedExpression, T, Expression> valueOperator, T operand) → _WhereAndSpec
   ├─ .ifAnd(BiFunction<BiOperator, T, IPredicate> expOperator, BiOperator operator, T operand) → _WhereAndSpec
   ├─ .ifAnd(TeFunction<BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator,
   │        BiOperator operator, BiFunction<TypedExpression, T, Expression> func, T operand) → _WhereAndSpec
   ├─ .ifAnd(BetweenValueOperator<T> expOperator,
   │        BiFunction<TypedExpression, T, Expression> valueOperator, T lower, WordAnd, T upper) → _WhereAndSpec
   ├─ .ifAnd(BetweenDualOperator<T, U> expOperator,
   │        BiFunction<TypedExpression, T, Expression> func1, T lower, WordAnd,
   │        BiFunction<TypedExpression, U, Expression> func2, U upper) → _WhereAndSpec
   ├─ .ifAnd(Function<T, Expression> expOperator1, T operand1,
   │        BiFunction<Expression, Object, IPredicate> expOperator2, Object operand2) → _WhereAndSpec
   └─ .ifAnd(ExpressionOperator<TypedExpression, T, Expression> expOperator1,
             BiFunction<TypedExpression, T, Expression> valueOperator, T operand1,
             BiFunction<Expression, Object, IPredicate> expOperator2, Object operand2) → _WhereAndSpec
```

### 7. GROUP BY 阶段（可选）

```
├─ [PostgreSQL 特有：带修饰符 GROUP BY]
│  ├─ .groupBy(SQLs.Modifier modifier, GroupByItem item) → _GroupByCommaSpec
│  ├─ .groupBy(SQLs.Modifier modifier, GroupByItem item1, GroupByItem item2) → _GroupByCommaSpec
│  ├─ .groupBy(SQLs.Modifier modifier, GroupByItem item1, GroupByItem item2, GroupByItem item3) → _GroupByCommaSpec
│  ├─ .groupBy(SQLs.Modifier modifier, GroupByItem item1, GroupByItem item2, GroupByItem item3, GroupByItem item4) → _GroupByCommaSpec
│  ├─ .groupBy(SQLs.Modifier modifier, Consumer<Consumer<GroupByItem>> consumer) → _HavingSpec
│  ├─ .ifGroupBy(SQLs.Modifier modifier, Consumer<Consumer<GroupByItem>> consumer) → _HavingSpec
│
├─ [普通 GROUP BY]
│  ├─ .groupBy(GroupByItem item) → _GroupByCommaSpec
│  ├─ .groupBy(GroupByItem item1, GroupByItem item2) → _GroupByCommaSpec
│  ├─ .groupBy(GroupByItem item1, GroupByItem item2, GroupByItem item3) → _GroupByCommaSpec
│  ├─ .groupBy(GroupByItem item1, GroupByItem item2, GroupByItem item3, GroupByItem item4) → _GroupByCommaSpec
│  ├─ .groupBy(Consumer<Consumer<GroupByItem>> consumer) → _HavingSpec
│  └─ .ifGroupBy(Consumer<Consumer<GroupByItem>> consumer) → _HavingSpec
│
└─ [逗号追加 GROUP BY 项]
   └─ _GroupByCommaSpec 上的 .commaSpace(GroupByItem item) 等变体
```

### 8. HAVING 阶段（可选，前提是有 GROUP BY）

```
├─ [静态 HAVING]
│  ├─ .having(IPredicate predicate) → _HavingAndSpec
│  ├─ .having(Function<T, IPredicate> expOperator, T operand) → _HavingAndSpec
│  ├─ .having(Supplier<IPredicate> supplier) → _HavingAndSpec
│  ├─ .having(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
│  │          BiFunction<TypedExpression, T, Expression> valueOperator, T operand) → _HavingAndSpec
│  ├─ .having(DialectBooleanOperator<T> fieldOperator,
│  │          BiFunction<TypedExpression, Expression, CompoundPredicate> logicOp,
│  │          BiFunction<TypedExpression, T, Expression> valueOp, T operand) → _HavingAndSpec
│  ├─ .having(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
│  │          BiFunction<TypedExpression, T, Expression> valueOperator,
│  │          Function<String, T> function, String key) → _HavingAndSpec
│  ├─ .having(DialectBooleanOperator<T> fieldOperator,
│  │          BiFunction<TypedExpression, Expression, CompoundPredicate> logicOp,
│  │          BiFunction<TypedExpression, T, Expression> valueOp,
│  │          Function<String, T> keyFunction, String key) → _HavingAndSpec
│  ├─ .ifHaving(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
│  │           BiFunction<TypedExpression, T, Expression> valueOperator, Supplier<T> supplier) → _HavingAndSpec
│  └─ .ifHaving(DialectBooleanOperator<T> fieldOperator,
│              BiFunction<TypedExpression, Expression, CompoundPredicate> logicOp,
│              BiFunction<TypedExpression, T, Expression> valueOp, Supplier<T> supplier) → _HavingAndSpec
│
├─ [spaceAnd 追加 AND 条件]
│  └─ _HavingAndSpec 上的 .spaceAnd(...) 与上面 .having(...) 变体一致，以及 ifSpaceAnd(...) 变体
│
└─ [动态 HAVING]
   ├─ .having(Consumer<Consumer<IPredicate>> consumer) → _WindowSpec
   └─ .ifHaving(Consumer<Consumer<IPredicate>> consumer) → _WindowSpec
```

### 9. WINDOW 阶段（可选）

```
├─ [静态 WINDOW 定义]
│  ├─ .window(String name) → Window._WindowAsClause
│  │  ├─ .as() → _WindowCommaSpec
│  │  ├─ .as(String existingWindowName) → _WindowCommaSpec
│  │  ├─ .as(Consumer<PostgreWindow._PartitionBySpec> consumer) → _WindowCommaSpec
│  │  └─ .as(String existingWindowName, Consumer<PostgreWindow._PartitionBySpec> consumer) → _WindowCommaSpec
│  └─ _WindowCommaSpec 上的 .comma(String name) → 继续定义窗口
│
└─ [动态 WINDOW 定义]
   ├─ .windows(Consumer<Window.Builder<PostgreWindow._PartitionBySpec>> consumer) → _OrderBySpec
   └─ .ifWindows(Consumer<Window.Builder<PostgreWindow._PartitionBySpec>> consumer) → _OrderBySpec
```

### 10. ORDER BY 阶段（可选）

```
├─ [静态 ORDER BY]
│  ├─ .orderBy(SortItem sortItem) → _LimitSpec
│  ├─ .orderBy(SortItem sortItem1, SortItem sortItem2) → _LimitSpec
│  ├─ .orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3) → _LimitSpec
│  ├─ .orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3, SortItem sortItem4) → _LimitSpec
│  ├─ .orderBy(Consumer<Consumer<SortItem>> consumer) → _LimitSpec
│  └─ .ifOrderBy(Consumer<Consumer<SortItem>> consumer) → _LimitSpec
│
└─ [逗号追加排序项]
   └─ _OrderByCommaSpec 上的 .spaceComma(SortItem item) 等变体
```

### 11. LIMIT/FETCH 阶段（可选）

```
├─ [仅行数 LIMIT]
│  ├─ .limit(Object rowCount) → _LockSpec
│  ├─ .limit(BiFunction<MappingType, Number, Expression> valueOperator, Number rowCount) → _LockSpec
│  ├─ .ifLimit(Object rowCount) → _LockSpec
│  └─ .ifLimit(BiFunction<MappingType, Number, Expression> valueOperator, Number rowCount) → _LockSpec
│
├─ [带偏移量 LIMIT]
│  ├─ .limit(Expression offset, Expression rowCount) → _LockSpec
│  ├─ .limit(BiFunction<MappingType, Number, Expression> valueOperator, Number offset, Number rowCount) → _LockSpec
│  ├─ .limit(BiFunction<MappingType, Number, Expression> valueOperator,
│  │         Supplier<Number> offsetSupplier, Supplier<Number> rowCountSupplier) → _LockSpec
│  ├─ .limit(BiFunction<MappingType, Object, Expression> valueOperator,
│  │         Function<String, ?> function, String offsetKey, String rowCountKey) → _LockSpec
│  ├─ .limit(Consumer<BiConsumer<Expression, Expression>> consumer) → _LockSpec
│  ├─ .ifLimit(BiFunction<MappingType, Number, Expression> valueOperator,
│  │          Supplier<Number> offsetSupplier, Supplier<Number> rowCountSupplier) → _LockSpec
│  ├─ .ifLimit(BiFunction<MappingType, Object, Expression> valueOperator,
│  │          Function<String, ?> function, String offsetKey, String rowCountKey) → _LockSpec
│  └─ .ifLimit(Consumer<BiConsumer<Expression, Expression>> consumer) → _LockSpec
│
├─ [PostgreSQL 特有：OFFSET]
│  └─ _LimitOffsetSpec 上的 .offset(...) 变体与上面 limit 相似
│
└─ [PostgreSQL 特有：FETCH]
   └─ _FetchOffsetSpec/_FetchSpec 上的 .fetch(FIRST/NEXT, ...) 等变体
```

### 12. FOR UPDATE/... 阶段（PostgreSQL 特有，可选）

```
├─ [静态 FOR ...]
│  ├─ .forUpdate() → _LockOfTableSpec
│  ├─ .forShare() → _LockOfTableSpec
│  ├─ .forNoKeyUpdate() → _LockOfTableSpec
│  └─ .forKeyShare() → _LockOfTableSpec
│
├─ [OF 表（可选）]
│  └─ _LockOfTableSpec 上的 .of(TableMeta<?>... tables) / .of(String... tableAliases) / 条件变体
│
├─ [NOWAIT/SKIP LOCKED（可选）]
│  └─ _LockWaitOptionSpec 上的 .noWait() / .skipLocked()
│
├─ [动态 FOR ...]
│  └─ .ifFor(Consumer<_PostgreDynamicLockStrengthClause> consumer) → _LockSpec
│     └─ Consumer 内可用：.update() / .share() / .noKeyUpdate() / .keyShare() → _DynamicLockOfTableSpec
│
└─ [约束：不能与 GROUP BY 或 WINDOW 同时使用]
```

### 13. UNION/INTERSECT/EXCEPT 阶段（PostgreSQL 特有，可选，可重复）

```
├─ [UNION]
│  ├─ .union() → _QueryWithComplexSpec
│  ├─ .unionAll() → _QueryWithComplexSpec
│  └─ .unionDistinct() → _QueryWithComplexSpec
│
├─ [INTERSECT]
│  ├─ .intersect() → _QueryWithComplexSpec
│  ├─ .intersectAll() → _QueryWithComplexSpec
│  └─ .intersectDistinct() → _QueryWithComplexSpec
│
├─ [EXCEPT]
│  ├─ .except() → _QueryWithComplexSpec
│  ├─ .exceptAll() → _QueryWithComplexSpec
│  └─ .exceptDistinct() → _QueryWithComplexSpec
│
└─ [括号子查询]
   └─ .parens(Function<WithSpec<_UnionOrderBySpec>, _UnionOrderBySpec> function) → _UnionOrderBySpec
      └─ 可继续 .orderBy(...) / .limit(...) / .fetch(...) / .asQuery()
```

### 14. 结束阶段：asQuery()

```
└─ 任意阶段调用 .asQuery() → Select（可执行查询）
```

---

## 逐层接口详解

### 0. 入口接口：`PostgreQuery.WithSpec<Select>`

```java
// PostgreQuery.java
interface WithSpec<I extends Item>
        extends _PostgreDynamicWithClause<SelectSpec<I>>,
                _PostgreStaticWithClause<SelectSpec<I>>,
                SelectSpec<I> {
}
```

`WithSpec` 组合了三种能力：动态 CTE、静态 CTE、SELECT。所以 `Postgres.query()` 返回的对象可以直接 `.select(...)`，也可以先用 `.with(...)` 定义 CTE。

---

### 1. WITH（Common Table Expression）

#### 语义约束

- `WITH` 关键字仅出现一次（即 `.with(name)` / `.withRecursive(name)` 只能调用一次）。
- 后续 CTE 通过 `_CteComma.comma(String name)` 追加，该方法返回 `_StaticCteParensSpec`，从而形成 `.comma(name) → .parens(...) → .as(...) → .comma(name) → ...` 的循环链。
- `.space()` 是唯一退出 CTE 链、进入 SELECT 的路径。
- 禁止在 `.space()` 后再调用 `.with(name)`——类型系统不提供此路径。

#### 静态 WITH（编译时已知）

```java
Postgres.query()
    .with("cte_name")              // → _StaticCteParensSpec
    .parens("col1", "col2")       // → _StaticCteAsClause（可选列别名）
    .as(s -> s.select(...).from(...).asQuery())  // → _CteComma（可继续 .comma(name) 追加更多 CTE）
    .space()                      // → SelectSpec（结束 CTE，进入 SELECT）
    .select(...)
```

#### 动态 WITH（运行时构建）

```java
Postgres.query()
    .with(builder -> {           // builder 是 PostgreCtes
        builder.comma("cte1").parens("col").as(s -> s.select(...).asQuery());
        builder.comma("cte2").as(s -> s.select(...).asQuery());
    })                          // → SelectSpec
    .select(...)
```

#### 条件 WITH

```java
// 条件 with — 仅当满足条件时执行
Postgres.query()
    .ifWith(builder -> {...})   // → SelectSpec
    .ifWithRecursive(builder -> {...})  // → SelectSpec
    .select(...)
```

#### 递归 CTE 的 SEARCH 子句

```java
Postgres.query()
    .withRecursive("cte_name")
    .as(s -> s.select(...).unionAll().select(...))
    .search(s -> s.breadthFirstBy("col1", "col2").set("seq_col"))
    .space()
    .select(...)
```

或者：

```java
.search(s -> s.depthFirstBy("col1", "col2").set("seq_col"))
```

#### 递归 CTE 的 CYCLE 子句

```java
Postgres.query()
    .withRecursive("cte_name")
    .as(s -> s.select(...).unionAll().select(...))
    .cycle(s -> s.space("col1", "col2")
                 .set("is_cycle")
                 .to(literal(true), DEFAULT, literal(false))
                 .using("path_col"))
    .space()
    .select(...)
```

---

### 2. SELECT 阶段

#### PostgreSQL 特有：DISTINCT ON

```java
Postgres.query()
    .selectsDistinctOn(
        distinctOn -> distinctOn.selection(PillUser_.userType),
        select -> select.selection(PillUser_.id).selection(PillUser_.name)
    )
    .from(PillUser_.T, AS, "u")
    .orderBy(PillUser_.userType, PillUser_.id::desc)
    .asQuery()
```

或者：

```java
Postgres.query()
    .selectDistinctOn(PillUser_.userType, PillUser_.id)
    .comma(PillUser_.name)
    .from(PillUser_.T, AS, "u")
    .orderBy(PillUser_.userType, PillUser_.id::desc)
    .asQuery()
```

#### 修饰符 SELECT（Postgres.ALL / Postgres.DISTINCT）

```java
Postgres.query()
    .select(Arrays.asList(Postgres.ALL), PillUser_.id)
    .from(PillUser_.T, AS, "u")
    .asQuery()
```

#### 静态 SELECT（不引用 FROM）

```java
Postgres.query()
    .select(PillUser_.id)              // 单个 Selection
    .select(PillUser_.id, PillUser_.name)  // 2 个
    .select("u", PERIOD, PillUser_.T)   // SELECT u.* 一键展开整表
```

#### 延迟 SELECT（引用 FROM）

使用 `Consumer<DeferSelectSpaceClause>` 在 Consumer 内部通过 `refField()` 引用 FROM 表引用：

```java
// 引用子查询中的导出列
Postgres.query()
    .select(s -> s.space(refField("sub", "value").as("val"))
                 .comma(refField("sub", "name").as("nm")))
    .from(subQuery(...))
    .as("sub")
    .asQuery()
```

#### 动态 SELECT

```java
Postgres.query()
    .selects(s -> {
        s.selection(PillUser_.id);
        s.selection(PillUser_.name);
    })
    .from(PillUser_.T, AS, "u")
    .asQuery()
```

---

### 3. FROM 阶段

#### 基础表 FROM

```java
Postgres.query()
    .select("u", PERIOD, PillUser_.T)
    .from(PillUser_.T, AS, "u")   // → _TableSampleJoinSpec
    .asQuery()
```

#### PostgreSQL 特有：ONLY 修饰符

```java
Postgres.query()
    .select("u", PERIOD, PillUser_.T)
    .from(ONLY, PillUser_.T, AS, "u")
    .asQuery()
```

#### 派生表 FROM

```java
Postgres.query()
    .select("sub", PERIOD, ASTERISK)
    .from(subQuery(...))           // → _AsClause
    .as("sub")                     // → _ParensJoinSpec
    .asQuery()
```

#### 表函数 FROM（PostgreSQL 特有）

```java
Postgres.query()
    .select(s -> s.space(refField("func", "value").as("json1")))
    .from(jsonbPathQueryTz(jsonField, literal(path)))
    .as("func").parens("value")
    .asQuery()
```

---

### 3.1 TABLESAMPLE（PostgreSQL 特有）

```java
Postgres.query()
    .select("u", PERIOD, PillUser_.T)
    .from(PillUser_.T, AS, "u")
    .tableSample(literal("BERNOULLI"), literal(10))
    .asQuery()
```

或者：

```java
.tableSample(
    (methodOp, arg) -> methodOp.apply(TextType.TEXT, arg),
    SQLs::literal, "BERNOULLI",
    (sizeOp, size) -> sizeOp.apply(IntegerType.INSTANCE, size),
    SQLs::literal, 10
)
```

条件 TABLESAMPLE：

```java
.ifTableSample(() -> shouldSample ? literal("BERNOULLI") : null)
```

---

### 3.2 REPEATABLE（PostgreSQL 特有）

```java
Postgres.query()
    .select("u", PERIOD, PillUser_.T)
    .from(PillUser_.T, AS, "u")
    .tableSample(literal("SYSTEM"), literal(10))
    .repeatable(literal(12345))  // 种子值，确保每次采样结果相同
    .asQuery()
```

---

### 4. JOIN 阶段

JOIN 后返回的 `_TableSampleOnSpec` 也支持 `.tableSample()` 和 `.repeatable()`：

```java
Postgres.query()
    .select(...)
    .from(PillUser_.T, AS, "u")
    .join(Order_.T, AS, "o")
    .tableSample(literal("BERNOULLI"), literal(5))
    .on(Order_.userId::equal, PillUser_.id)
    .asQuery()
```

---

### 7. GROUP BY 阶段（PostgreSQL 特有：ALL/DISTINCT 修饰符）

```java
Postgres.query()
    .select(PillUser_.userType, count(ASTERISK).as("cnt"))
    .from(PillUser_.T, AS, "u")
    .groupBy(ALL, PillUser_.userType)  // PostgreSQL 特有：ALL 修饰符
    .asQuery()
```

或者：

```java
.groupBy(DISTINCT, PillUser_.userType)
```

---

### 12. FOR UPDATE/NO KEY UPDATE/FOR SHARE/FOR KEY SHARE 阶段（PostgreSQL 特有）

```java
Postgres.query()
    .select("u", PERIOD, PillUser_.T)
    .from(PillUser_.T, AS, "u")
    .where(PillUser_.id::equal, literal(1))
    .forUpdate()
    .of(PillUser_.T)           // 可选：仅锁定特定表
    .noWait()                  // 可选：不等待锁
    .asQuery()
```

其他变体：

```java
.forShare()
.forNoKeyUpdate()
.forKeyShare()
```

条件变体：

```java
.ifFor(lock -> lock.update())
```

#### 约束

根据代码注释，FOR UPDATE/NO KEY UPDATE/FOR SHARE/FOR KEY SHARE 不能与 GROUP BY 或 WINDOW 同时使用。

---

### 13. UNION/INTERSECT/EXCEPT 阶段（PostgreSQL 特有）

```java
Postgres.query()
    .parens(s -> s.select(PillUser_.id).from(PillUser_.T, AS, "u1"))
    .union()
    .parens(s -> s.select(PillUser_.id).from(PillUser_.T, AS, "u2"))
    .orderBy(PillUser_.id)
    .asQuery()
```

INTERSECT 和 EXCEPT 用法类似。

---

## 实现类继承链

```
SQLSyntax (SQLs extends)
  └─ Postgres
      └─ .query() → PostgreQueries.simpleQuery()
          └─ SimpleSelect
              └─ PostgreQueries
                  └─ SimpleQueries.WithCteDistinctOnSimpleQueries
                      └─ SimpleQueries
                          └─ JoinableClause
                              └─ ...
```

---

## 关键约束和规则

### 层级约束（通过接口链强制执行）

接口名称本身就编码了当前上下文中可用的方法，DSL 通过返回类型引导下一个可调用的方法。

### 可重复 vs 不可重复

| 子句 | 可重复 | 说明 |
|-----|-------|------|
| WITH (`.with(name)`) | ❌ | WITH 关键字只能出现一次 |
| CTE (`.comma(name)`) | ✅ | 可以追加多个 CTE |
| SELECT (`.select(...)`) | ❌ | 只能调用一次（后续用 `.comma(...)`） |
| FROM (`.from(...)`) | ❌ | FROM 关键字只能出现一次 |
| JOIN (`.join(...)` 等) | ✅ | 可以 JOIN 多个表 |
| WHERE (`.where(...)`) | ❌ | WHERE 关键字只能出现一次 |
| AND (`.and(...)`) | ✅ | 可以追加多个 AND 条件 |
| GROUP BY (`.groupBy(...)`) | ❌ | GROUP BY 关键字只能出现一次 |
| HAVING (`.having(...)`) | ❌ | HAVING 关键字只能出现一次 |
| WINDOW (`.window(...)`) | ❌ | WINDOW 关键字只能出现一次 |
| ORDER BY (`.orderBy(...)`) | ❌ | ORDER BY 关键字只能出现一次 |
| LIMIT (`.limit(...)`) | ❌ | LIMIT 关键字只能出现一次 |
| FOR ... (`.forUpdate(...)`) | ✅ | 可以有多个 FOR 子句 |
| UNION/INTERSECT/EXCEPT | ✅ | 可以多次使用集合操作 |

### PostgreSQL 特有特性汇总

1. **DISTINCT ON**：`.selectsDistinctOn(...)` / `.selectDistinctOn(...)`
2. **WITH 子句**：支持 `SEARCH` 和 `CYCLE` 子句
3. **ONLY 修饰符**：`from(ONLY, table, ...)` 仅操作基表
4. **TABLESAMPLE**：`.tableSample(...)` 采样表数据
5. **REPEATABLE**：`.repeatable(...)` 采样种子
6. **GROUP BY ALL/DISTINCT**：`.groupBy(ALL/DISTINCT, ...)`
7. **FETCH FIRST/NEXT**：`.fetch(...)` 替代 LIMIT
8. **FOR NO KEY UPDATE**：`.forNoKeyUpdate()`
9. **FOR KEY SHARE**：`.forKeyShare()`
10. **INTERSECT/EXCEPT**：除了 UNION 外的集合操作
11. **表函数**：`.from(UndoneFunction)` + `.as(alias).parens(columns)`

---

## 与 SQLs.query() 的区别

| 特性 | SQLs.query() | Postgres.query() |
|------|-------------|-----------------|
| 方言 | 标准 SQL | PostgreSQL 特有 |
| DISTINCT ON | ❌ | ✅ |
| WITH SEARCH/CYCLE | ❌ | ✅ |
| ONLY 修饰符 | ❌ | ✅ |
| TABLESAMPLE | ❌ | ✅ |
| REPEATABLE | ❌ | ✅ |
| GROUP BY ALL/DISTINCT | ❌ | ✅ |
| FETCH 子句 | ❌ | ✅ |
| FOR NO KEY UPDATE | ❌ | ✅ |
| FOR KEY SHARE | ❌ | ✅ |
| INTERSECT/EXCEPT | 部分方言 | ✅ |
| 返回类型 | Select | Select |

---

## 使用示例（来自 QueryUnitTests）

### 示例 1：DISTINCT ON

```java
final Select stmt;
stmt = Postgres.query()
        .selectsDistinctOn(
            s -> {
                s.accept(refSelection("aa\\nbb"));
                refSelection(2);
            }, 
            s -> s.selection(literalValue("aa'").as("aa\\nbb"), PillUser_.id)
        )
        .from(PillUser_.T, AS, "u")
        .orderBy(PillUser_.id)
        .offset(literal, 1, ROWS)
        .fetch(FIRST, literal, 4, ROWS, ONLY)
        .asQuery();
```

### 示例 2：动态 WINDOW

```java
final Select stmt;
stmt = Postgres.query()
        .select(literalValue(1).as("r"))
        .from(PillUser_.T, AS, "u")
        .windows(w -> {
            w.window("w1").as(s -> s.partitionBy(PillUser_.userType).orderBy(PillUser_.id));
            w.window("w2").as(s -> s.orderBy(PillUser_.id));
        })
        .asQuery();
```

### 示例 3：表函数

```java
final Select stmt;
stmt = Postgres.query()
        .select(s -> s.space(refField("func", "value").as("json1"))
                     .comma(refField("func2", "value").as("json2"))
                     .comma(refField("func2", "ordinal").as("json3")))
        .from(jsonbPathQueryTz(jsonField, literal(path)))
        .as("func").parens("value")
        .crossJoin(jsonbPathQueryTz(jsonField, literal, varPath, literal, vars)::withOrdinality)
        .as("func2").parens("value", "ordinal")
        .asQuery();
```
