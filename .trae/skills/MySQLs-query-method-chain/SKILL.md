---
name: MySQLs.query() method chain
description: 完整的 Army Criteria API MySQLs.query() 方法链知识。用于理解、解释或记录从 MySQLs.query() 到 asQuery() 的完整 MySQL 专用 SELECT 查询构建流程。涵盖每一个接口、每一个方法和每一个合法路径，包括 MySQL 特有的索引提示、Straight JOIN、WITH ROLLUP、FOR UPDATE/SHARE 等特性。
---

# MySQLs.query() 方法链完整参考

## 适用范围

本 Skill **仅限**用于理解、解释、记录 `MySQLs.query()` 入口的 MySQL 专用 SELECT 查询构建流程。覆盖从 `MySQLs.query()` 到 `asQuery()` 的完整方法链，包括 MySQL 特有的 WITH CTE、修饰符 SELECT、Partition 选择、索引提示（USE INDEX/IGNORE INDEX/FORCE INDEX）、Straight JOIN、WITH ROLLUP、WINDOW、ORDER BY WITH ROLLUP、FOR UPDATE/FOR SHARE/LOCK IN SHARE MODE、SELECT INTO、UNION/INTERSECT/EXCEPT 等全部特性。

## 源码依据

本 Skill 基于以下核心源文件编写：
- `MySQLs.java`（入口定义）
- `MySQLQuery.java`（接口定义）
- `MySQLQueries.java`（实现）
- `MySQLQueryUnitTests.java`（单元测试示例）

所有描述均以实际源码接口签名和实现为准。

## 核心入口

```java
// MySQLs.java line 134-136
public static MySQLQuery.WithSpec<Select> query() {
    return MySQLQueries.simpleQuery();
}
```

**返回类型**：`MySQLQuery.WithSpec<Select>` — 实现类是 `MySQLQueries.SimpleSelect<Select>`。

**内部实现**：`MySQLQueries.simpleQuery()`：
```java
// MySQLQueries.java line 86-88
static WithSpec<Select> simpleQuery() {
    return new SimpleSelect<>(null, null, SQLs::identity);
}
```

---

## 完整方法链 Diagram

### 0. 入口

```
MySQLs.query() → MySQLQuery.WithSpec<Select>
```

### 1. WITH CTE 阶段（可选）

```
├─ [静态 WITH]
│  ├─ .with(String name) → _StaticCteParensSpec
│  │  ├─ .parens(String first, String... rest) → _StaticCteAsClause
│  │  ├─ .parens(Consumer<Consumer<String>> consumer) → _StaticCteAsClause
│  │  ├─ .ifParens(Consumer<Consumer<String>> consumer) → _StaticCteAsClause
│  │  └─ .as(Function<_StaticCteComplexCommandSpec, _CteComma> function) → _CteComma
│  │     ├─ .comma(String name) → _StaticCteParensSpec（追加 CTE，可重复）
│  │     └─ .space() → _SelectSpec（结束 CTE）
│  └─ .withRecursive(String name) → _StaticCteParensSpec（同上流程）
│
└─ [动态 WITH]
   ├─ .with(Consumer<MySQLCtes> consumer) → _SelectSpec
   ├─ .withRecursive(Consumer<MySQLCtes> consumer) → _SelectSpec
   ├─ .ifWith(Consumer<MySQLCtes> consumer) → _SelectSpec
   └─ .ifWithRecursive(Consumer<MySQLCtes> consumer) → _SelectSpec
```

### 2. SELECT 阶段（必须）

```
├─ [修饰符 SELECT]
│  ├─ .select(List<MySQLs.Modifier> modifiers, Selection selection) → _MySQLSelectCommaSpec
│  ├─ .select(List<MySQLs.Modifier> modifiers, Selection selection1, Selection selection2) → _MySQLSelectCommaSpec
│  ├─ .select(List<MySQLs.Modifier> modifiers, Function<String, Selection> function, String alias) → _MySQLSelectCommaSpec
│  ├─ .selectAll() → _StaticSelectSpaceClause
│  │  └─ .space(Selection selection) → _MySQLSelectCommaSpec
│  ├─ .selectDistinct() → _StaticSelectSpaceClause
│  │  └─ .space(Selection selection) → _MySQLSelectCommaSpec
│  └─ .select(List<MySQLs.Modifier> modifiers, Consumer<_DeferSelectSpaceClause> consumer) → _FromSpec
│
├─ [静态 SELECT（不引用 FROM）]
│  ├─ .select(Selection selection) → _MySQLSelectCommaSpec
│  ├─ .select(Selection selection1, Selection selection2) → _MySQLSelectCommaSpec
│  ├─ .select(Selection selection1, Selection selection2, Selection selection3) → _MySQLSelectCommaSpec
│  ├─ .select(Function<String, Selection> function, String alias) → _MySQLSelectCommaSpec
│  ├─ .select(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table) → _MySQLSelectCommaSpec
│  └─ [其他多参形式]
│
├─ [延迟 SELECT（引用 FROM）]
│  └─ .select(Consumer<_DeferSelectSpaceClause> consumer) → _FromSpec
│
├─ [动态 SELECT]
│  ├─ .selects(Consumer<SelectionConsumer> consumer) → _FromSpec
│  └─ .selects(List<MySQLs.Modifier> modifiers, Consumer<SelectionConsumer> consumer) → _FromSpec
│
├─ [逗号追加选择项]
│  └─ _MySQLSelectCommaSpec 上的 .comma(...) 变体与 .select(...) 一致
│
└─ [括号查询/UNION]
   └─ .parens(Function<WithSpec<_UnionOrderBySpec>, _UnionOrderBySpec> function) → _UnionOrderBySpec
```

### 3. FROM 阶段（必须）

```
├─ [基础表 FROM - 支持 Partition 选择]
│  ├─ .from(TableMeta<?> table) → _PartitionJoinSpec
│  │  └─ [Partition 选择]
│  │     ├─ .partition(String partitionName) → _PartitionJoinSpec
│  │     ├─ .partition(String partitionName1, String partitionName2) → _PartitionJoinSpec
│  │     ├─ .partition(Consumer<Clause._StaticStringSpaceClause> consumer) → _PartitionJoinSpec
│  │     └─ .as(String tableAlias) → _IndexHintJoinSpec
│  │
├─ [派生表 FROM]
│  ├─ .from(DerivedTable derivedTable) → _AsClause<_ParensJoinSpec>
│  │  └─ .as(String alias) → _ParensJoinSpec
│  │     └─ .parens(String first, String... rest) → _JoinSpec（列别名）
│  └─ [条件/修饰符变体]
│
├─ [CTE FROM]
│  ├─ .from(String cteName) → _JoinSpec
│  └─ .from(String cteName, SQLs.WordAs wordAs, String alias) → _JoinSpec
│
├─ [嵌套 FROM]
│  └─ .from(Function<_NestedLeftParenSpec<_JoinSpec>, _JoinSpec> function) → _JoinSpec
│
└─ [直接结束 FROM]
   ├─ .into(String firstVarName, String... rest) → _AsQueryClause
   └─ .union() / .unionAll() / .intersect() / .except() → _QueryValuesComplexSpec
```

### 4. 索引提示阶段（MySQL 特有，可选）

```
└─ _IndexHintJoinSpec 上：
   ├─ [USE INDEX]
   │  ├─ .useIndex(String indexName) → _IndexHintJoinSpec
   │  ├─ .useIndex(String indexName1, String indexName2) → _IndexHintJoinSpec
   │  ├─ .useIndex(String indexName1, String indexName2, String indexName3) → _IndexHintJoinSpec
   │  ├─ .useIndex(Consumer<Clause._StaticStringSpaceClause> consumer) → _IndexHintJoinSpec
   │  ├─ .useIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer) → _IndexHintJoinSpec
   │  └─ .ifUseIndex(Consumer<Consumer<String>> consumer) → _IndexHintJoinSpec
   │
   ├─ [USE INDEX - 带 FOR 子句]
   │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName) → _IndexHintJoinSpec
   │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2) → _IndexHintJoinSpec
   │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer) → _IndexHintJoinSpec
   │  └─ .ifUseIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer) → _IndexHintJoinSpec
   │
   ├─ [IGNORE INDEX]
   │  ├─ .ignoreIndex(String indexName) → _IndexHintJoinSpec
   │  ├─ .ignoreIndex(String indexName1, String indexName2) → _IndexHintJoinSpec
   │  ├─ .ignoreIndex(String indexName1, String indexName2, String indexName3) → _IndexHintJoinSpec
   │  ├─ .ignoreIndex(Consumer<Clause._StaticStringSpaceClause> consumer) → _IndexHintJoinSpec
   │  ├─ .ignoreIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer) → _IndexHintJoinSpec
   │  └─ .ifIgnoreIndex(Consumer<Consumer<String>> consumer) → _IndexHintJoinSpec
   │
   ├─ [IGNORE INDEX - 带 FOR 子句]
   │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName) → _IndexHintJoinSpec
   │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2) → _IndexHintJoinSpec
   │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer) → _IndexHintJoinSpec
   │  └─ .ifIgnoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer) → _IndexHintJoinSpec
   │
   └─ [FORCE INDEX]
      ├─ .forceIndex(String indexName) → _IndexHintJoinSpec
      ├─ .forceIndex(String indexName1, String indexName2) → _IndexHintJoinSpec
      ├─ .forceIndex(String indexName1, String indexName2, String indexName3) → _IndexHintJoinSpec
      ├─ .forceIndex(Consumer<Clause._StaticStringSpaceClause> consumer) → _IndexHintJoinSpec
      ├─ .forceIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer) → _IndexHintJoinSpec
      └─ .ifForceIndex(Consumer<Consumer<String>> consumer) → _IndexHintJoinSpec
      │
      └─ [FORCE INDEX - 带 FOR 子句]
         ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName) → _IndexHintJoinSpec
         ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2) → _IndexHintJoinSpec
         ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer) → _IndexHintJoinSpec
         └─ .ifForceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer) → _IndexHintJoinSpec
```

### 5. JOIN 阶段（可选，可重复）

```
├─ [CROSS JOIN - 支持 Partition 和索引提示]
│  ├─ .crossJoin(TableMeta<?> table) → _PartitionJoinSpec
│  ├─ .crossJoin(DerivedTable derivedTable) → _AsClause<_ParensJoinSpec>
│  ├─ .crossJoin(String cteName) → _JoinSpec
│  ├─ .crossJoin(String cteName, SQLs.WordAs wordAs, String alias) → _JoinSpec
│  ├─ .crossJoin(Function<_NestedLeftParenSpec<_JoinSpec>, _JoinSpec> function) → _JoinSpec
│  └─ .ifCrossJoin(Consumer<MySQLCrosses> consumer) → _JoinSpec
│
├─ [INNER/LEFT/RIGHT/FULL JOIN - 支持 Partition 和索引提示]
│  ├─ .join(TableMeta<?> table) → _PartitionOnSpec
│  ├─ .leftJoin(TableMeta<?> table) → _PartitionOnSpec
│  ├─ .rightJoin(TableMeta<?> table) → _PartitionOnSpec
│  ├─ .fullJoin(TableMeta<?> table) → _PartitionOnSpec
│  │
│  ├─ [JOIN - 派生表]
│  │  ├─ .join(DerivedTable derivedTable) → _AsParensOnClause<_JoinSpec>
│  │  ├─ .leftJoin(DerivedTable derivedTable) → _AsParensOnClause<_JoinSpec>
│  │  └─ [其他 JOIN 类型变体]
│  │
│  ├─ [JOIN - CTE]
│  │  ├─ .join(String cteName) → _OnClause<_JoinSpec>
│  │  ├─ .leftJoin(String cteName) → _OnClause<_JoinSpec>
│  │  └─ [其他 JOIN 类型变体]
│  │
│  ├─ [JOIN - 嵌套]
│  │  ├─ .join(Function<_NestedLeftParenSpec<_OnClause<_JoinSpec>>, _OnClause<_JoinSpec>> function) → _OnClause<_JoinSpec>
│  │  └─ [其他 JOIN 类型变体]
│  │
│  └─ [动态 JOIN]
│     ├─ .ifJoin(Consumer<MySQLJoins> consumer) → _JoinSpec
│     ├─ .ifLeftJoin(Consumer<MySQLJoins> consumer) → _JoinSpec
│     ├─ .ifRightJoin(Consumer<MySQLJoins> consumer) → _JoinSpec
│     ├─ .ifFullJoin(Consumer<MySQLJoins> consumer) → _JoinSpec
│     └─ [其他 JOIN 类型 if 变体]
│
├─ [STRAIGHT JOIN - MySQL 特有]
│  ├─ .straightJoin(TableMeta<?> table) → _PartitionOnSpec
│  ├─ .straightJoin(Function<_NestedLeftParenSpec<_OnClause<_JoinSpec>>, _OnClause<_JoinSpec>> function) → _OnClause<_JoinSpec>
│  └─ .ifStraightJoin(Consumer<MySQLJoins> consumer) → _JoinSpec
│
└─ [ON 子句]
   └─ _OnClause 上：
      ├─ [索引提示（JOIN 表）]
      │  └─ _IndexHintOnSpec 上的 useIndex/ignoreIndex/forceIndex 变体（同 FROM 索引提示）
      │
      └─ [ON 条件]
         ├─ .on(IPredicate predicate) → _JoinSpec
         ├─ .on(Function<T, IPredicate> expOperator, T operand) → _JoinSpec
         └─ .on(Consumer<Consumer<IPredicate>> consumer) → _JoinSpec
```

### 6. WHERE 阶段（可选）

```
├─ [静态 WHERE]
│  ├─ .where(IPredicate predicate) → _WhereAndSpec
│  ├─ .where(Function<T, IPredicate> expOperator, T operand) → _WhereAndSpec
│  └─ .where(Consumer<Consumer<IPredicate>> consumer) → _GroupBySpec
│
├─ [条件 WHERE]
│  ├─ .whereIf(Supplier<IPredicate> supplier) → _WhereAndSpec
│  ├─ .whereIf(Function<T, IPredicate> expOperator, @Nullable T value) → _WhereAndSpec
│  └─ .ifWhere(Consumer<Consumer<IPredicate>> consumer) → _GroupBySpec
│
└─ [直接跳过 WHERE]
   └─ 直接调用 _WhereSpec 继承的 _GroupBySpec 方法
```

### 7. AND 阶段（可选，可重复）

```
└─ _WhereAndSpec 上：
   ├─ .and(IPredicate predicate) → _WhereAndSpec
   ├─ .and(Function<T, IPredicate> expOperator, T operand) → _WhereAndSpec
   ├─ .ifAnd(Supplier<IPredicate> supplier) → _WhereAndSpec
   └─ .ifAnd(Function<T, IPredicate> expOperator, @Nullable T value) → _WhereAndSpec
```

### 8. GROUP BY 阶段（可选）

```
├─ [静态 GROUP BY]
│  ├─ .groupBy(GroupByItem item) → _GroupByCommaSpec
│  ├─ .groupBy(GroupByItem item1, GroupByItem item2) → _GroupByCommaSpec
│  ├─ .groupBy(GroupByItem item1, GroupByItem item2, GroupByItem item3) → _GroupByCommaSpec
│  └─ .groupBy(Consumer<Consumer<GroupByItem>> consumer) → _HavingSpec
│
├─ [条件 GROUP BY]
│  └─ .ifGroupBy(Consumer<Consumer<GroupByItem>> consumer) → _HavingSpec
│
├─ [逗号追加 GROUP BY 项]
│  └─ _GroupByCommaSpec 上的 .commaSpace(GroupByItem item) 等变体
│
└─ [WITH ROLLUP - MySQL 特有]
   ├─ .withRollup() → _HavingSpec
   └─ .ifWithRollup(BooleanSupplier supplier) → _HavingSpec
```

### 9. HAVING 阶段（可选，前提 GROUP BY）

```
├─ [静态 HAVING]
│  ├─ .having(IPredicate predicate) → _HavingAndSpec
│  ├─ .having(Function<T, IPredicate> expOperator, T operand) → _HavingAndSpec
│  └─ .having(Consumer<Consumer<IPredicate>> consumer) → _WindowSpec
│
├─ [条件 HAVING]
│  ├─ .ifHaving(Supplier<IPredicate> supplier) → _HavingAndSpec
│  ├─ .ifHaving(Function<T, IPredicate> expOperator, @Nullable T value) → _HavingAndSpec
│  └─ .ifHaving(Consumer<Consumer<IPredicate>> consumer) → _WindowSpec
│
└─ [spaceAnd 追加 AND 条件]
   └─ _HavingAndSpec 上的 .spaceAnd(...) 与上面 .having(...) 变体一致，以及 ifSpaceAnd(...) 变体
```

### 10. WINDOW 阶段（可选）

```
├─ [静态 WINDOW 定义]
│  ├─ .window(String windowName) → Window._WindowAsClause
│  │  ├─ .as() → _WindowCommaSpec
│  │  ├─ .as(String existingWindowName) → _WindowCommaSpec
│  │  ├─ .as(Consumer<MySQLWindow._PartitionBySpec> consumer) → _WindowCommaSpec
│  │  └─ .as(String existingWindowName, Consumer<MySQLWindow._PartitionBySpec> consumer) → _WindowCommaSpec
│  └─ _WindowCommaSpec 上的 .comma(String name) → 继续定义窗口（可重复）
│
└─ [动态 WINDOW 定义]
   ├─ .windows(Consumer<Window.Builder<MySQLWindow._PartitionBySpec>> consumer) → _OrderBySpec
   └─ .ifWindows(Consumer<Window.Builder<MySQLWindow._PartitionBySpec>> consumer) → _OrderBySpec
```

### 11. ORDER BY 阶段（可选）

```
├─ [静态 ORDER BY]
│  ├─ .orderBy(SortItem sortItem) → _LimitSpec
│  ├─ .orderBy(SortItem sortItem1, SortItem sortItem2) → _LimitSpec
│  ├─ .orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3) → _LimitSpec
│  └─ .orderBy(Consumer<Consumer<SortItem>> consumer) → _LimitSpec
│
├─ [条件 ORDER BY]
│  └─ .ifOrderBy(Consumer<Consumer<SortItem>> consumer) → _LimitSpec
│
├─ [逗号追加排序项]
│  └─ _OrderByCommaSpec 上的 .spaceComma(SortItem item) 等变体
│
└─ [WITH ROLLUP - MySQL 特有]
   ├─ .withRollup() → _LimitSpec
   └─ .ifWithRollup(BooleanSupplier supplier) → _LimitSpec
```

### 12. LIMIT 阶段（可选）

```
├─ [仅行数 LIMIT]
│  ├─ .limit(Object rowCount) → _LockSpec
│  ├─ .limit(BiFunction<MappingType, Number, Expression> valueOperator, Number rowCount) → _LockSpec
│  ├─ .ifLimit(@Nullable Object rowCount) → _LockSpec
│  └─ .ifLimit(BiFunction<MappingType, Number, Expression> valueOperator, @Nullable Number rowCount) → _LockSpec
│
└─ [偏移 + 行数 LIMIT]
   ├─ .limit(Expression offset, Expression rowCount) → _LockSpec
   ├─ .limit(BiFunction<MappingType, Number, Expression> valueOperator, Number offset, Number rowCount) → _LockSpec
   ├─ .limit(BiFunction<MappingType, Number, Expression> valueOperator, Supplier<Number> offsetSupplier, Supplier<Number> rowCountSupplier) → _LockSpec
   ├─ .limit(Consumer<BiConsumer<Expression, Expression>> consumer) → _LockSpec
   └─ [条件 ifLimit 变体]
```

### 13. FOR UPDATE/SHARE/LOCK IN SHARE MODE 阶段（MySQL 特有，可选）

```
├─ [静态 FOR UPDATE]
│  └─ .forUpdate() → _LockOfTableSpec
│     └─ [OF 表（可选）]
│        ├─ .of(TableMeta<?>... tables) → _LockWaitOptionSpec
│        └─ .of(String... tableAliases) → _LockWaitOptionSpec
│           └─ [等待选项（可选）]
│              ├─ .nowait() → _IntoOptionSpec
│              └─ .skipLocked() → _IntoOptionSpec
│
├─ [静态 FOR SHARE]
│  └─ .forShare() → _LockOfTableSpec
│     └─ [OF 表和等待选项同上]
│
├─ [LOCK IN SHARE MODE（旧版 MySQL 语法）]
│  ├─ .lockInShareMode() → _IntoOptionSpec
│  └─ .ifLockInShareMode(BooleanSupplier predicate) → _IntoOptionSpec
│
└─ [动态 FOR]
   └─ .ifFor(Consumer<_DynamicLockStrengthClause> consumer) → _LockSpec
      └─ Consumer 内可用：
         ├─ .update() → _DynamicLockOfTableSpec
         └─ .share() → _DynamicLockOfTableSpec
            └─ [OF 表和等待选项]
```

### 14. SELECT INTO 阶段（MySQL 特有，可选）

```
├─ .into(String firstVarName, String... rest) → _AsQueryClause
├─ .into(Consumer<Consumer<String>> consumer) → _AsQueryClause
└─ .ifInto(Consumer<Consumer<String>> consumer) → _AsQueryClause
```

### 15. UNION/INTERSECT/EXCEPT 阶段（可选，可重复）

```
├─ [UNION]
│  ├─ .union() → _QueryValuesComplexSpec
│  ├─ .unionAll() → _QueryValuesComplexSpec
│  └─ .unionDistinct() → _QueryValuesComplexSpec
│
├─ [INTERSECT]
│  ├─ .intersect() → _QueryValuesComplexSpec
│  ├─ .intersectAll() → _QueryValuesComplexSpec
│  └─ .intersectDistinct() → _QueryValuesComplexSpec
│
├─ [EXCEPT]
│  ├─ .except() → _QueryValuesComplexSpec
│  ├─ .exceptAll() → _QueryValuesComplexSpec
│  └─ .exceptDistinct() → _QueryValuesComplexSpec
│
└─ [括号查询]
   └─ .parens(Function<WithSpec<_UnionOrderBySpec>, _UnionOrderBySpec> function) → _UnionOrderBySpec
      ├─ [UNION 后的 ORDER BY]
      │  └─ _UnionOrderBySpec 上的 orderBy 变体
      ├─ [UNION 后的 LIMIT]
      │  └─ _UnionLimitSpec 上的 limit 变体
      └─ .asQuery() → Select
```

### 16. 结束阶段：asQuery()

```
└─ 任意阶段调用 .asQuery() → Select（可执行查询）
```

---

## 逐层接口详解

### 0. 入口接口：`MySQLQuery.WithSpec<Select>`

```java
// MySQLQuery.java line 316-320
interface WithSpec<I extends Item>
    extends _MySQLDynamicWithClause<_SelectSpec<I>>,
            _MySQLStaticWithClause<_SelectSpec<I>>,
            _SelectSpec<I> {
}
```

`WithSpec` 组合了三种能力：动态 CTE、静态 CTE、SELECT。所以 `MySQLs.query()` 返回的对象可以直接 `.select(...)`，也可以先用 `.with(...)` 定义 CTE。

---

### 1. WITH CTE 阶段

#### 语义约束

- `WITH` 关键字仅出现一次（即 `.with(name)` / `.withRecursive(name)` 只能调用一次）。
- 后续 CTE 通过 `_CteComma.comma(String name)` 追加，该方法返回 `_StaticCteParensSpec`，从而形成 `.comma(name) → .parens(...) → .as(...) → .comma(name) → ...` 的循环链。
- `.space()` 是唯一退出 CTE 链、进入 SELECT 的路径。

#### 静态 WITH（编译时已知）

```java
MySQLs.query()
    .with("cte_name")              // → _StaticCteParensSpec
    .parens("col1", "col2")       // → _StaticCteAsClause（可选列别名）
    .as(s -> s.select(...).from(...).asQuery())  // → _CteComma（可继续 .comma(name) 追加更多 CTE）
    .space()                      // → _SelectSpec（结束 CTE，进入 SELECT）
    .select(...)
```

#### 动态 WITH（运行时构建）

```java
MySQLs.query()
    .with(builder -> {           // builder 是 MySQLCtes
        builder.comma("cte1").parens("col").as(s -> s.select(...).asQuery());
        builder.comma("cte2").as(s -> s.select(...).asQuery());
    })                          // → _SelectSpec
    .select(...)
```

---

### 2. SELECT 阶段

#### MySQL 特有修饰符

MySQLs 提供以下修饰符：
```java
// MySQLs.java line 35-51
public static final Modifier ALL = MySQLWords.MySQLModifier.ALL;
public static final WordDistinct DISTINCT = MySQLWords.KeyWordDistinct.DISTINCT;
public static final Modifier DISTINCTROW = MySQLWords.MySQLModifier.DISTINCTROW;
public static final Modifier HIGH_PRIORITY = MySQLWords.MySQLModifier.HIGH_PRIORITY;
public static final Modifier STRAIGHT_JOIN = MySQLWords.MySQLModifier.STRAIGHT_JOIN;
public static final Modifier SQL_SMALL_RESULT = MySQLWords.MySQLModifier.SQL_SMALL_RESULT;
public static final Modifier SQL_BIG_RESULT = MySQLWords.MySQLModifier.SQL_BIG_RESULT;
public static final Modifier SQL_BUFFER_RESULT = MySQLWords.MySQLModifier.SQL_BUFFER_RESULT;
public static final Modifier SQL_NO_CACHE = MySQLWords.MySQLModifier.SQL_NO_CACHE;
public static final Modifier SQL_CALC_FOUND_ROWS = MySQLWords.MySQLModifier.SQL_CALC_FOUND_ROWS;
```

#### 修饰符 SELECT 示例

```java
MySQLs.query()
    .select(Arrays.asList(MySQLs.DISTINCT, MySQLs.SQL_CALC_FOUND_ROWS), User_.id)
    .from(User_.T, AS, "u")
    .asQuery();
```

---

### 3. FROM + Partition 选择阶段

#### Partition 选择（MySQL 特有）

```java
MySQLs.query()
    .select(User_.id)
    .from(User_.T)
    .partition("p2023", "p2024")  // 选择特定分区
    .as("u")
    .asQuery();
```

---

### 4. 索引提示阶段（MySQL 特有）

#### 索引提示语法

```java
// USE INDEX
MySQLs.query()
    .select(User_.id)
    .from(User_.T, AS, "u")
    .useIndex("idx_user_name")  // 使用特定索引
    .where(User_.name.equal(SQLs::literal, "test"))
    .asQuery();

// IGNORE INDEX
MySQLs.query()
    .select(User_.id)
    .from(User_.T, AS, "u")
    .ignoreIndex("idx_user_name")  // 忽略特定索引
    .where(User_.name.equal(SQLs::literal, "test"))
    .asQuery();

// FORCE INDEX
MySQLs.query()
    .select(User_.id)
    .from(User_.T, AS, "u")
    .forceIndex("idx_user_name")  // 强制使用特定索引
    .where(User_.name.equal(SQLs::literal, "test"))
    .asQuery();

// 带 FOR 子句
MySQLs.query()
    .select(User_.id)
    .from(User_.T, AS, "u")
    .useIndex(SQLs.FOR, SQLs.JOIN, "idx_user_id")  // FOR JOIN
    .join(Order_.T, AS, "o")
    .on(Order_.userId.equal(User_.id))
    .asQuery();
```

---

### 5. JOIN + Straight JOIN 阶段

#### Straight JOIN（MySQL 特有）

强制 MySQL 按照你指定的表顺序进行连接：

```java
MySQLs.query()
    .select(User_.id, Order_.id)
    .from(User_.T, AS, "u")
    .straightJoin(Order_.T, AS, "o")  // 强制先 u 后 o
    .on(Order_.userId.equal(User_.id))
    .asQuery();
```

---

### 6. GROUP BY + WITH ROLLUP 阶段

#### WITH ROLLUP（MySQL 特有）

```java
MySQLs.query()
    .select(ChinaRegion_.regionType, MySQLs.sum(ChinaRegion_.regionGdp).as("total"))
    .from(ChinaRegion_.T)
    .groupBy(ChinaRegion_.regionType)
    .withRollup()  // 增加汇总行
    .asQuery();
```

---

### 7. ORDER BY + WITH ROLLUP 阶段

#### ORDER BY WITH ROLLUP（MySQL 特有）

```java
MySQLs.query()
    .select(ChinaRegion_.regionType, ChinaRegion_.name, ChinaRegion_.regionGdp)
    .from(ChinaRegion_.T)
    .orderBy(ChinaRegion_.regionType, ChinaRegion_.name)
    .withRollup()
    .asQuery();
```

---

### 8. FOR UPDATE/SHARE 阶段

#### FOR UPDATE

```java
MySQLs.query()
    .select(User_.id, User_.balance)
    .from(User_.T, AS, "u")
    .where(User_.id.equal(SQLs::literal, 1))
    .forUpdate()
    .asQuery();
```

#### FOR SHARE

```java
MySQLs.query()
    .select(User_.id, User_.balance)
    .from(User_.T, AS, "u")
    .where(User_.id.equal(SQLs::literal, 1))
    .forShare()
    .asQuery();
```

#### LOCK IN SHARE MODE（旧语法）

```java
MySQLs.query()
    .select(User_.id, User_.balance)
    .from(User_.T, AS, "u")
    .where(User_.id.equal(SQLs::literal, 1))
    .lockInShareMode()
    .asQuery();
```

#### OF 子句（指定锁定哪些表）

```java
MySQLs.query()
    .select(User_.id, Order_.id)
    .from(User_.T, AS, "u")
    .join(Order_.T, AS, "o")
    .on(Order_.userId.equal(User_.id))
    .where(User_.id.equal(SQLs::literal, 1))
    .forUpdate()
    .of(User_.T)  // 只锁定 user 表
    .asQuery();
```

---

### 9. SELECT INTO 阶段

#### SELECT INTO 变量

```java
MySQLs.query()
    .select(User_.id, User_.name)
    .from(User_.T, AS, "u")
    .where(User_.id.equal(SQLs::literal, 1))
    .into("@id", "@name")
    .asQuery();
```

---

### 10. UNION/INTERSECT/EXCEPT 阶段

```java
MySQLs.query()
    .parens(s -> s.select(User_.id).from(User_.T, AS, "u1"))
    .union()
    .parens(s -> s.select(User_.id).from(User_.T, AS, "u2"))
    .orderBy(User_.id)
    .asQuery();
```

---

## 可重复 vs 不可重复

| 子句 | 可重复 | 说明 |
|-----|-------|------|
| WITH (`.with(name)`) | ❌ | WITH 关键字只能出现一次 |
| CTE (`.comma(name)`) | ✅ | 可以追加多个 CTE |
| SELECT (`.select(...)`) | ❌ | SELECT 关键字只能出现一次（后续用 `.comma(...)`） |
| FROM (`.from(...)`) | ❌ | FROM 关键字只能出现一次 |
| Partition (`.partition(...)`) | ❌ | Partition 选择只能出现一次 |
| 索引提示 (`.useIndex(...)` 等) | ✅ | 可以添加多个索引提示 |
| JOIN (`.join(...)` 等) | ✅ | 可以 JOIN 多个表 |
| WHERE (`.where(...)`) | ❌ | WHERE 关键字只能出现一次 |
| AND (`.and(...)`) | ✅ | 可以追加多个 AND 条件 |
| GROUP BY (`.groupBy(...)`) | ❌ | GROUP BY 关键字只能出现一次 |
| WITH ROLLUP (GROUP BY) | ❌ | WITH ROLLUP 只能出现一次 |
| HAVING (`.having(...)`) | ❌ | HAVING 关键字只能出现一次 |
| spaceAnd (`.spaceAnd(...)`) | ✅ | 可以追加多个 HAVING AND 条件 |
| WINDOW (`.window(...)`) | ❌ | WINDOW 关键字只能出现一次 |
| WINDOW comma (`.comma(...)`) | ✅ | 可以追加多个窗口定义 |
| ORDER BY (`.orderBy(...)`) | ❌ | ORDER BY 关键字只能出现一次 |
| WITH ROLLUP (ORDER BY) | ❌ | ORDER BY WITH ROLLUP 只能出现一次 |
| LIMIT (`.limit(...)`) | ❌ | LIMIT 关键字只能出现一次 |
| FOR UPDATE/SHARE | ❌ | FOR 子句只能出现一次 |
| SELECT INTO | ❌ | INTO 子句只能出现一次 |
| UNION/INTERSECT/EXCEPT | ✅ | 可以多次使用集合操作 |

---

## MySQL 特有特性汇总

1. **MySQL SELECT 修饰符**：ALL、DISTINCT、DISTINCTROW、HIGH_PRIORITY、STRAIGHT_JOIN、SQL_SMALL_RESULT、SQL_BIG_RESULT、SQL_BUFFER_RESULT、SQL_NO_CACHE、SQL_CALC_FOUND_ROWS
2. **Partition 选择**：`.partition(...)` 选择特定分区
3. **索引提示**：`.useIndex()`、`.ignoreIndex()`、`.forceIndex()`，支持 FOR JOIN/FOR ORDER BY/FOR GROUP BY
4. **Straight JOIN**：`.straightJoin()` 强制连接顺序
5. **WITH ROLLUP**：GROUP BY 和 ORDER BY 都支持 WITH ROLLUP
6. **FOR UPDATE/FOR SHARE**：支持 OF 子句指定锁定表
7. **LOCK IN SHARE MODE**：旧版共享锁语法
8. **SELECT INTO**：`SELECT ... INTO @var` 语法
9. **INTERSECT/EXCEPT**：MySQL 8.0+ 支持的集合操作

---

## 使用示例（来自 MySQLQueryUnitTests）

### 示例 1：基础查询 + 窗口函数

```java
final Select stmt;
stmt = MySQLs.query()
    .select(
        cases(ChinaRegion_.regionType)
            .when(RegionType.NONE).then(RegionType.NONE.name())
            .when(RegionType.PROVINCE).then(RegionType.PROVINCE.name())
            .when(RegionType.CITY).then(RegionType.CITY.name())
            .elseValue(NULL).end(StringType.INSTANCE)::as, 
        ChinaRegion_.REGION_TYPE
    )
    .comma(MySQLs.rowNumber().over()::as, "rowNumber")
    .comma(MySQLs.sum(ChinaRegion_.regionGdp).over(s -> s.partitionBy(ChinaRegion_.regionType))::as, "gdpSum")
    .comma(MySQLs.sum(MySQLs.DISTINCT, ChinaRegion_.regionGdp)
        .over(s -> s.partitionBy(ChinaRegion_.regionType)).as("distinctGdpSum")
    )
    .comma(lag(ChinaRegion_.population, SQLs.literalValue(1))
        .over("w", s -> s.orderBy(ChinaRegion_.id)
            .rows().between(UNBOUNDED_PRECEDING, AND, CURRENT_ROW)
        )::as, "log2"
    )
    .from(ChinaRegion_.T, AS, "cr")
    .where(ChinaRegion_.id.greaterEqual(SQLs::literal, 10))
    .and(ChinaRegion_.createTime.between(SQLs::literal, now.minusMinutes(10), AND, now))
    .window("w").as(s -> s.partitionBy(ChinaRegion_.regionType).orderBy(ChinaRegion_.id::desc))
    .orderBy(SQLs.refSelection("rowNumber")::desc)
    .limit(SQLs::literal, 1)
    .asQuery();
```

### 示例 2：UNION 查询

```java
final Select stmt;
stmt = MySQLs.query()
    .parens(s -> s.select(PillUser_.id)
        .from(PillUser_.T, AS, "p")
        .whereIf(PillUser_.id::equal, SQLs::literal, criteria.getId())
        .ifAnd(PillUser_.nickName::equal, SQLs::literal, criteria.getNickName())
        .groupBy(PillUser_.userType)
        .having(PillUser_.userType.equal(SQLs::literal, PillUserType.PERSON))
        .limit(SQLs::literal, 0, 10)
        .asQuery()
    )
    .union()
    .parens(s -> s.select(PillUser_.id)
        .from(PillUser_.T, AS, "p")
        .where(PillUser_.id.equal(SQLs::param, 2))
        .ifAnd(PillUser_.nickName::equal, SQLs::literal, this.randomPerson())
        .groupBy(PillUser_.userType)
        .having(PillUser_.userType.equal(SQLs::literal, PillUserType.PERSON))
        .union()
        .select(PillUser_.id)
        .from(PillUser_.T, AS, "p")
        .where(PillUser_.id.equal(SQLs::literal, 2))
        .ifAnd(PillUser_.nickName::equal, SQLs::param, this.randomPerson())
        .and(PillUser_.version.equal(SQLs::literal, 2))
        .groupBy(PillUser_.userType)
        .having(PillUser_.userType.equal(SQLs::literal, PillUserType.PERSON))
        .asQuery()
    )
    .unionAll()
    .select(PillUser_.id)
    .from(PillUser_.T, AS, "p")
    .where(PillUser_.id.equal(SQLs::literal, 2))
    .ifAnd(PillUser_.nickName::equal, SQLs::param, this.randomPerson())
    .and(PillUser_.version.equal(SQLs::literal, 2))
    .groupBy(PillUser_.userType)
    .having(PillUser_.userType.equal(SQLs::literal, PillUserType.PERSON))
    .unionDistinct()
    .parens(s -> s.select(PillUser_.id)
        .from(PillUser_.T, AS, "p")
        .where(PillUser_.id.equal(SQLs::literal, 2))
        .ifAnd(PillUser_.nickName::equal, SQLs::param, this.randomPerson())
        .and(PillUser_.version.equal(SQLs::literal, 2))
        .groupBy(PillUser_.userType)
        .having(PillUser_.userType.equal(SQLs::literal, PillUserType.PERSON))
        .limit(SQLs::literal, 0, 10)
        .asQuery()
    )
    .intersect()
    .select(PillUser_.id)
    .from(PillUser_.T, AS, "p")
    .where(PillUser_.id.equal(SQLs::literal, 2))
    .ifAnd(PillUser_.nickName::equal, SQLs::param, this.randomPerson())
    .and(PillUser_.version.equal(SQLs::literal, 2))
    .groupBy(PillUser_.userType)
    .having(PillUser_.userType.equal(SQLs::literal, PillUserType.PERSON))
    .except()
    .select(PillUser_.id)
    .from(PillUser_.T, AS, "p")
    .where(PillUser_.id.equal(SQLs::literal, 2))
    .ifAnd(PillUser_.nickName::equal, SQLs::param, this.randomPerson())
    .and(PillUser_.version.equal(SQLs::literal, 2))
    .groupBy(PillUser_.userType)
    .having(PillUser_.userType.equal(SQLs::literal, PillUserType.PERSON))
    .asQuery();
```

### 示例 3：WITH RECURSIVE

```java
final Select stmt;
stmt = MySQLs.query()
    .withRecursive("cte").parens("n").as(s -> s.select(SQLs.literalValue(1)::as, "r")
        .union()
        .select(cs -> cs.space(refField("cte", "n").plus(SQLs.literalValue(1))::as, "n"))
        .from("cte")
        .where(refField("cte", "n").less(SQLs.literalValue(10)))
        .asQuery()
    )
    .space()
    .parens(s -> s.select(cs -> cs.space(refField("cte", "n")))
        .from("cte")
        .asQuery()
    )
    .asQuery();
```

---

## 实现类继承链

```
SQLSyntax (MySQLs extends)
  └─ MySQLs
      └─ .query() → MySQLQueries.simpleQuery()
          └─ SimpleSelect
              └─ MySQLQueries
                  └─ SimpleQueries
                      └─ JoinableClause
                          └─ ...
```

---

## 与 SQLs.query() 的区别

| 特性 | SQLs.query() | MySQLs.query() |
|-----|-------------|----------------|
| 方言 | 标准 SQL | MySQL 专用 |
| MySQL 修饰符 | ❌ | ✅ (ALL/DISTINCTROW/HIGH_PRIORITY 等) |
| Partition 选择 | ❌ | ✅ |
| 索引提示 | ❌ | ✅ (USE INDEX/IGNORE INDEX/FORCE INDEX) |
| Straight JOIN | ❌ | ✅ |
| WITH ROLLUP | ❌ | ✅ (GROUP BY 和 ORDER BY 都支持) |
| FOR SHARE | ❌ | ✅ |
| LOCK IN SHARE MODE | ❌ | ✅ |
| SELECT INTO | ❌ | ✅ |
| INTERSECT/EXCEPT | 部分方言 | ✅ |
| 返回类型 | Select | Select |

---

## 关键约束和规则

### 层级约束（通过接口链强制执行）

接口名称本身就编码了当前上下文中可用的方法，DSL 通过返回类型引导下一个可调用的方法。

### 索引提示约束

- 索引提示必须紧跟在表引用之后（FROM 或 JOIN 表后）
- 可以为同一个表添加多个索引提示
- 索引提示可以带 FOR 子句（FOR JOIN/FOR ORDER BY/FOR GROUP BY）

### WITH ROLLUP 约束

- GROUP BY WITH ROLLUP 和 ORDER BY WITH ROLLUP 是独立的
- GROUP BY WITH ROLLUP 在 GROUP BY 后调用
- ORDER BY WITH ROLLUP 在 ORDER BY 后调用

### FOR UPDATE/SHARE 约束

- FOR UPDATE 和 FOR SHARE 不能同时使用
- OF 子句可选，用于指定锁定哪些表
- NOWAIT 和 SKIP LOCKED 是等待选项，互斥

---

## query vs multiDelete 对比分析

### 相同点

| 特性 | query | multiDelete |
|------|-------|-------------|
| WITH (CTE) | ✅ 支持 | ✅ 支持 |
| INDEX HINT | ✅ useIndex/ignoreIndex/forceIndex | ✅ useIndex/ignoreIndex/forceIndex |
| JOIN 类型 | ✅ join/leftJoin/rightJoin/fullJoin/straightJoin/crossJoin | ✅ join/leftJoin/rightJoin/fullJoin/straightJoin/crossJoin |
| WHERE/AND | ✅ 支持 | ✅ 支持 |
| 嵌套 JOIN | ✅ 支持 | ✅ 支持 |
| CTE 引用 | ✅ 支持 | ✅ 支持 |
| Derived Table | ✅ 支持 | ✅ 支持 |
| Partition | ✅ 支持（FROM 表） | ✅ 支持（USING/FROM 表） |

### 核心差异

| 特性 | query | multiDelete |
|------|-------|-------------|
| **操作类型** | 查询数据 (SELECT) | 删除数据 (DELETE) |
| **SELECT 子句** | ✅ 支持 | ❌ 不支持 |
| **SET 子句** | ❌ 不支持 | ❌ 不支持 |
| **GROUP BY/HAVING** | ✅ 支持 | ❌ 不支持 |
| **ORDER BY/LIMIT** | ✅ 支持 | ❌ 不支持 |
| **FOR UPDATE/SHARE** | ✅ 支持 | ❌ 不支持 |
| **UNION/INTERSECT/EXCEPT** | ✅ 支持 | ❌ 不支持 |
| **SELECT INTO** | ✅ 支持 | ❌ 不支持 |
| **返回类型** | `Select` | `Delete` |

### 设计意图

- **query**：用于查询数据，支持完整的 SELECT 特性集合，包括分组、排序、聚合、窗口函数等
- **multiDelete**：用于从多个表中删除满足条件的数据，强调数据删除操作

### 典型使用场景

**query 典型场景**：
```java
// 查询订单及其关联的订单项
MySQLs.query()
    .select(Order_.id, OrderItem_.id)
    .from(Order_.T, AS, "o")
    .join(OrderItem_.T, AS, "i")
    .on(Order_.id::equal, OrderItem_.orderId)
    .where(Order_.status.equal(0))
    .asQuery();
```

**multiDelete 典型场景**：
```java
// 删除订单及其关联的订单项
MySQLs.multiDelete()
    .delete("o", "i")
    .using(Order_.T)
    .as("o")
    .join(OrderItem_.T)
    .as("i")
    .on(Order_.id::equal, OrderItem_.orderId)
    .where(Order_.status.equal(0))
    .asDelete();
```

### 选择指南

| 场景 | 推荐使用 | 原因 |
|------|----------|------|
| 查询数据 | `query` | 专为 SELECT 查询设计 |
| 删除数据 | `multiDelete` | 专为多表删除设计 |
| 需要 SELECT 子句 | `query` | 支持完整的选择项 |
| 需要 GROUP BY | `query` | 支持分组聚合 |
| 需要 ORDER BY/LIMIT | `query` | 支持排序和分页 |
| 需要 FOR UPDATE | `query` | 支持行级锁 |

---

## query vs singleDelete 对比分析

### 相同点

| 特性 | query | singleDelete |
|------|-------|--------------|
| WITH (CTE) | ✅ 支持 | ✅ 支持 |
| WHERE/AND | ✅ 支持 | ✅ 支持 |
| Partition | ✅ 支持 | ✅ 支持 |
| Hint | ✅ 支持 | ✅ 支持 |

### 核心差异

| 特性 | query | singleDelete |
|------|-------|--------------|
| **操作类型** | 查询数据 (SELECT) | 删除数据 (DELETE) |
| **SELECT 子句** | ✅ 必须 | ❌ 不支持 |
| **SET 子句** | ❌ 不支持 | ❌ 不支持 |
| **GROUP BY/HAVING** | ✅ 支持 | ❌ 不支持 |
| **ORDER BY/LIMIT** | ✅ 支持 | ✅ 支持 |
| **FOR UPDATE/SHARE** | ✅ 支持 | ❌ 不支持 |
| **返回类型** | `Select` | `Delete` |

### 选择指南

| 场景 | 推荐使用 | 原因 |
|------|----------|------|
| 查询数据 | `query` | 专为 SELECT 查询设计 |
| 删除数据 | `singleDelete` | 专为数据删除设计 |
| 需要 SELECT 子句 | `query` | 支持完整的选择项 |
| 需要 GROUP BY | `query` | 支持分组聚合 |
| 需要 FOR UPDATE | `query` | 支持行级锁 |

---

## query vs multiUpdate 对比分析

### 相同点

| 特性 | query | multiUpdate |
|------|-------|-------------|
| WITH (CTE) | ✅ 支持 | ✅ 支持 |
| INDEX HINT | ✅ useIndex/ignoreIndex/forceIndex | ✅ useIndex/ignoreIndex/forceIndex |
| JOIN 类型 | ✅ join/leftJoin/rightJoin/fullJoin/straightJoin/crossJoin | ✅ join/leftJoin/rightJoin/fullJoin/straightJoin/crossJoin |
| WHERE/AND | ✅ 支持 | ✅ 支持 |
| 嵌套 JOIN | ✅ 支持 | ✅ 支持 |
| CTE 引用 | ✅ 支持 | ✅ 支持 |
| Derived Table | ✅ 支持 | ✅ 支持 |

### 核心差异

| 特性 | query | multiUpdate |
|------|-------|-------------|
| **操作类型** | 查询数据 (SELECT) | 更新数据 (UPDATE) |
| **SELECT 子句** | ✅ 支持 | ❌ 不支持 |
| **SET 子句** | ❌ 不支持 | ✅ 支持 |
| **GROUP BY/HAVING** | ✅ 支持 | ❌ 不支持 |
| **ORDER BY/LIMIT** | ✅ 支持 | ❌ 不支持 |
| **FOR UPDATE/SHARE** | ✅ 支持 | ❌ 不支持 |
| **返回类型** | `Select` | `Update` |

### 设计意图

- **query**：用于查询数据，支持完整的 SELECT 特性集合，包括分组、排序、聚合、窗口函数等
- **multiUpdate**：用于更新多个表中的数据，强调数据更新操作

### 典型使用场景

**query 典型场景**：
```java
// 查询订单及其订单项
MySQLs.query()
    .select(Order_.id, OrderItem_.id, OrderItem_.quantity)
    .from(Order_.T, AS, "o")
    .join(OrderItem_.T, AS, "i")
    .on(Order_.id::equal, OrderItem_.orderId)
    .where(Order_.status.equal(SQLs::literal, OrderStatus.PENDING))
    .asQuery();
```

**multiUpdate 典型场景**：
```java
// 更新订单状态
MySQLs.multiUpdate()
    .update(Order_.T, AS, "o")
    .join(OrderItem_.T, AS, "i")
    .on(Order_.id::equal, OrderItem_.orderId)
    .set(Order_.status, SQLs::literal, OrderStatus.CANCELLED)
    .where(Order_.id.equal(SQLs::literal, orderId))
    .asUpdate();
```

### 选择指南

| 场景 | 推荐使用 | 原因 |
|------|----------|------|
| 查询数据 | `query` | 专为 SELECT 查询设计 |
| 更新数据 | `multiUpdate` | 专为数据更新设计 |
| 需要 SELECT 子句 | `query` | 支持完整的选择项 |
| 需要 SET 子句 | `multiUpdate` | UPDATE 必须有 SET |
| 需要 GROUP BY | `query` | 支持分组聚合 |
| 需要 ORDER BY/LIMIT | `query` | 支持排序和分页 |

---

*本 Skill 基于源码分析生成，支持自我进化。当发现新的方法或使用模式时，请更新此文档。*
