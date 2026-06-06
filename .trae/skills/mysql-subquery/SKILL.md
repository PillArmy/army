---
name: "mysql-subquery"
description: "Provides comprehensive documentation and usage guidelines for MySQLs.subQuery() method chain in Army framework. Invoke when user needs help with MySQL subqueries using Army criteria API."
---

# MySQLs.subQuery() 方法链完整文档

## 概述

本技能提供 `MySQLs.subQuery()` 方法链的完整文档，包括：
- 完整方法链 Diagram
- 每个方法的参数、用法和场景
- 可重复调用和不可重复调用方法的说明
- 子句的多种形式及其使用场景

## 方法链完整 Diagram

```
MySQLs.subQuery()
  ├─ with(String name)
  │   └─ comma(String name) → repeatable
  │       ├─ parens(String first, String... rest)
  │       │   └─ as(Function<_StaticCteComplexCommandSpec<_CteComma<I>>, _CteComma<I>> function)
  │       ├─ parens(Consumer<Consumer<String>> consumer)
  │       │   └─ as(Function<_StaticCteComplexCommandSpec<_CteComma<I>>, _CteComma<I>> function)
  │       ├─ ifParens(Consumer<Consumer<String>> consumer)
  │       │   └─ as(Function<_StaticCteComplexCommandSpec<_CteComma<I>>, _CteComma<I>> function)
  │       └─ as(Function<_StaticCteComplexCommandSpec<_CteComma<I>>, _CteComma<I>> function)
  ├─ withRecursive(String name)
  │   └─ comma(String name) → repeatable
  │       └─ [同 with() 的子结构]
  ├─ with(Consumer<MySQLCtes> consumer)
  ├─ withRecursive(Consumer<MySQLCtes> consumer)
  ├─ ifWith(Consumer<MySQLCtes> consumer)
  ├─ ifWithRecursive(Consumer<MySQLCtes> consumer)
  │
  ├─ select(Selection selection)
  │   ├─ space(Selection selection) → repeatable
  │   └─ comma(Selection selection) → repeatable
  ├─ select(Function<String, Selection> function, String alias)
  │   ├─ space(Selection selection) → repeatable
  │   └─ comma(Selection selection) → repeatable
  ├─ select(Selection selection1, Selection selection2)
  │   ├─ space(Selection selection) → repeatable
  │   └─ comma(Selection selection) → repeatable
  ├─ select(Function<String, Selection> function1, String alias1, Selection selection)
  │   ├─ space(Selection selection) → repeatable
  │   └─ comma(Selection selection) → repeatable
  ├─ select(Selection selection, Function<String, Selection> function2, String alias2)
  │   ├─ space(Selection selection) → repeatable
  │   └─ comma(Selection selection) → repeatable
  ├─ select(Function<String, Selection> function1, String alias1, Function<String, Selection> function2, String alias2)
  │   ├─ space(Selection selection) → repeatable
  │   └─ comma(Selection selection) → repeatable
  ├─ select(SqlField field1, SqlField field2, SqlField field3)
  │   ├─ space(Selection selection) → repeatable
  │   └─ comma(Selection selection) → repeatable
  ├─ select(SqlField field1, SqlField field2, SqlField field3, SqlField field4)
  │   ├─ space(Selection selection) → repeatable
  │   └─ comma(Selection selection) → repeatable
  ├─ select(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table)
  │   ├─ space(Selection selection) → repeatable
  │   └─ comma(Selection selection) → repeatable
  ├─ select(String parenAlias, SQLs.SymbolDot period1, ParentTableMeta<P> parent, String childAlias, SQLs.SymbolDot period2, ComplexTableMeta<P, ?> child)
  │   ├─ space(Selection selection) → repeatable
  │   └─ comma(Selection selection) → repeatable
  ├─ selectAll()
  │   └─ space(Selection selection) → repeatable
  ├─ selectDistinct()
  │   └─ space(Selection selection) → repeatable
  ├─ select(List<MySQLs.Modifier> modifiers)
  │   └─ space(Selection selection) → repeatable
  ├─ select(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers)
  │   └─ space(Selection selection) → repeatable
  ├─ select(Consumer<_DeferSelectSpaceClause> consumer)
  ├─ selects(Consumer<SelectionConsumer> consumer)
  ├─ select(MySQLs.Modifier modifier, Consumer<_DeferSelectSpaceClause> consumer)
  ├─ selects(MySQLs.Modifier modifier, Consumer<SelectionConsumer> consumer)
  ├─ select(List<MySQLs.Modifier> modifiers, Consumer<_DeferSelectSpaceClause> consumer)
  ├─ selects(List<MySQLs.Modifier> modifiers, Consumer<SelectionConsumer> consumer)
  ├─ select(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers, Consumer<_DeferSelectSpaceClause> consumer)
  ├─ selects(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers, Consumer<SelectionConsumer> consumer)
  │
  ├─ from(TableMeta<?> table)
  │   ├─ partition(String partitionName) → repeatable
  │   │   └─ [同 from() 后续方法]
  │   ├─ partition(String first, String... rest)
  │   │   └─ [同 from() 后续方法]
  │   ├─ partition(Consumer<Consumer<String>> consumer)
  │   │   └─ [同 from() 后续方法]
  │   ├─ ifPartition(Consumer<Consumer<String>> consumer)
  │   │   └─ [同 from() 后续方法]
  │   ├─ as(String alias)
  │   │   ├─ useIndex(String indexName)
  │   │   ├─ useIndex(String indexName1, String indexName2)
  │   │   ├─ useIndex(String indexName1, String indexName2, String indexName3)
  │   │   ├─ useIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
  │   │   ├─ useIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
  │   │   ├─ ifUseIndex(Consumer<Consumer<String>> consumer)
  │   │   ├─ ignoreIndex(String indexName)
  │   │   ├─ ignoreIndex(String indexName1, String indexName2)
  │   │   ├─ ignoreIndex(String indexName1, String indexName2, String indexName3)
  │   │   ├─ ignoreIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
  │   │   ├─ ignoreIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
  │   │   ├─ ifIgnoreIndex(Consumer<Consumer<String>> consumer)
  │   │   ├─ forceIndex(String indexName)
  │   │   ├─ forceIndex(String indexName1, String indexName2)
  │   │   ├─ forceIndex(String indexName1, String indexName2, String indexName3)
  │   │   ├─ forceIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
  │   │   ├─ forceIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
  │   │   ├─ ifForceIndex(Consumer<Consumer<String>> consumer)
  │   │   ├─ useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
  │   │   ├─ useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
  │   │   ├─ useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
  │   │   ├─ useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
  │   │   ├─ useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
  │   │   ├─ ifUseIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
  │   │   ├─ ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
  │   │   ├─ ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
  │   │   ├─ ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
  │   │   ├─ ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
  │   │   ├─ ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
  │   │   ├─ ifIgnoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
  │   │   ├─ forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
  │   │   ├─ forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
  │   │   ├─ forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
  │   │   ├─ forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
  │   │   ├─ forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
  │   │   ├─ ifForceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
  │   │   ├─ crossJoin(TableMeta<?> table)
  │   │   ├─ crossJoin(DerivedTable table)
  │   │   ├─ crossJoin(_Cte cte)
  │   │   ├─ crossJoin(Function<_NestedLeftParenSpec<_JoinSpec<I>>, _JoinSpec<I>> function)
  │   │   ├─ leftJoin(TableMeta<?> table)
  │   │   ├─ leftJoin(DerivedTable table)
  │   │   ├─ leftJoin(_Cte cte)
  │   │   ├─ leftJoin(Function<_NestedLeftParenSpec<_OnClause<_JoinSpec<I>>>, _OnClause<_JoinSpec<I>>> function)
  │   │   ├─ join(TableMeta<?> table)
  │   │   ├─ join(DerivedTable table)
  │   │   ├─ join(_Cte cte)
  │   │   ├─ join(Function<_NestedLeftParenSpec<_OnClause<_JoinSpec<I>>>, _OnClause<_JoinSpec<I>>> function)
  │   │   ├─ rightJoin(TableMeta<?> table)
  │   │   ├─ rightJoin(DerivedTable table)
  │   │   ├─ rightJoin(_Cte cte)
  │   │   ├─ rightJoin(Function<_NestedLeftParenSpec<_OnClause<_JoinSpec<I>>>, _OnClause<_JoinSpec<I>>> function)
  │   │   ├─ fullJoin(TableMeta<?> table)
  │   │   ├─ fullJoin(DerivedTable table)
  │   │   ├─ fullJoin(_Cte cte)
  │   │   ├─ fullJoin(Function<_NestedLeftParenSpec<_OnClause<_JoinSpec<I>>>, _OnClause<_JoinSpec<I>>> function)
  │   │   ├─ straightJoin(TableMeta<?> table)
  │   │   ├─ straightJoin(DerivedTable table)
  │   │   ├─ straightJoin(_Cte cte)
  │   │   ├─ straightJoin(Function<_NestedLeftParenSpec<_OnClause<_JoinSpec<I>>>, _OnClause<_JoinSpec<I>>> function)
  │   │   ├─ ifCrossJoin(Consumer<MySQLCrosses> consumer)
  │   │   ├─ ifLeftJoin(Consumer<MySQLJoins> consumer)
  │   │   ├─ ifJoin(Consumer<MySQLJoins> consumer)
  │   │   ├─ ifRightJoin(Consumer<MySQLJoins> consumer)
  │   │   ├─ ifFullJoin(Consumer<MySQLJoins> consumer)
  │   │   └─ ifStraightJoin(Consumer<MySQLJoins> consumer)
  │   └─ [同 from() 后续方法]
  ├─ from(DerivedTable table)
  │   └─ as(String alias)
  │       ├─ parens(String first, String... rest)
  │       ├─ parens(Consumer<Consumer<String>> consumer)
  │       ├─ ifParens(Consumer<Consumer<String>> consumer)
  │       └─ [同 from() 后续方法]
  ├─ from(_Cte cte)
  │   └─ as(String alias)
  │       └─ [同 from() 后续方法]
  ├─ from(Function<_NestedLeftParenSpec<_JoinSpec<I>>, _JoinSpec<I>> function)
  │
  ├─ where(Function<IPredicate, IPredicate> function)
  │   └─ and(Function<IPredicate, IPredicate> function) → repeatable
  ├─ ifWhere(Consumer<Consumer<IPredicate>> consumer)
  │
  ├─ groupBy(GroupByItem item)
  │   ├─ commaSpace(GroupByItem item) → repeatable
  │   ├─ withRollup()
  │   └─ ifWithRollup(BooleanSupplier supplier)
  ├─ groupBy(GroupByItem item1, GroupByItem item2)
  │   ├─ commaSpace(GroupByItem item) → repeatable
  │   ├─ withRollup()
  │   └─ ifWithRollup(BooleanSupplier supplier)
  ├─ groupBy(GroupByItem item1, GroupByItem item2, GroupByItem item3)
  │   ├─ commaSpace(GroupByItem item) → repeatable
  │   ├─ withRollup()
  │   └─ ifWithRollup(BooleanSupplier supplier)
  ├─ groupBy(GroupByItem item1, GroupByItem item2, GroupByItem item3, GroupByItem item4)
  │   ├─ commaSpace(GroupByItem item) → repeatable
  │   ├─ withRollup()
  │   └─ ifWithRollup(BooleanSupplier supplier)
  │
  ├─ having(Function<IPredicate, IPredicate> function)
  │   └─ and(Function<IPredicate, IPredicate> function) → repeatable
  ├─ ifHaving(Consumer<Consumer<IPredicate>> consumer)
  │
  ├─ window(String windowName)
  │   └─ as(...)
  │       └─ comma(String windowName) → repeatable
  ├─ windows(Consumer<Window.Builder<MySQLWindow._PartitionBySpec>> consumer)
  ├─ ifWindows(Consumer<Window.Builder<MySQLWindow._PartitionBySpec>> consumer)
  │
  ├─ orderBy(SortItem item)
  │   ├─ comma(SortItem item) → repeatable
  │   ├─ withRollup()
  │   └─ ifWithRollup(BooleanSupplier supplier)
  ├─ orderBy(SortItem item1, SortItem item2)
  │   ├─ comma(SortItem item) → repeatable
  │   ├─ withRollup()
  │   └─ ifWithRollup(BooleanSupplier supplier)
  ├─ orderBy(SortItem item1, SortItem item2, SortItem item3)
  │   ├─ comma(SortItem item) → repeatable
  │   ├─ withRollup()
  │   └─ ifWithRollup(BooleanSupplier supplier)
  ├─ orderBy(SortItem item1, SortItem item2, SortItem item3, SortItem item4)
  │   ├─ comma(SortItem item) → repeatable
  │   ├─ withRollup()
  │   └─ ifWithRollup(BooleanSupplier supplier)
  │
  ├─ limit(long offset, long rowCount)
  ├─ limit(long rowCount)
  │
  ├─ forUpdate()
  │   ├─ ofTable(String alias)
  │   │   └─ comma(String alias) → repeatable
  │   ├─ wait()
  │   ├─ nowait()
  │   ├─ skipLocked()
  │   ├─ into(String firstVarName, String... rest)
  │   ├─ into(Consumer<Consumer<String>> consumer)
  │   └─ ifInto(Consumer<Consumer<String>> consumer)
  ├─ forShare()
  │   └─ [同 forUpdate() 的子结构]
  ├─ ifFor(Consumer<_DynamicLockStrengthClause> consumer)
  ├─ lockInShareMode()
  │   └─ [同 forUpdate() 的子结构]
  ├─ ifLockInShareMode(BooleanSupplier predicate)
  │
  ├─ into(String firstVarName, String... rest)
  ├─ into(Consumer<Consumer<String>> consumer)
  ├─ ifInto(Consumer<Consumer<String>> consumer)
  │
  └─ asQuery()
```

## 子句多种形式说明

### 1. WITH 子句 (CTE - Common Table Expression)

#### 形式 A: 静态 CTE 定义
```java
MySQLs.subQuery()
    .with("cte1")
        .parens("col1", "col2")
        .as(c -> c.select(...)...)
    .comma("cte2")
        .as(c -> c.select(...)...)
    .select(...)
```

#### 形式 B: 递归 CTE 定义
```java
MySQLs.subQuery()
    .withRecursive("cte_name")
        .as(c -> c.select(...)...)
    .select(...)
```

#### 形式 C: 动态 CTE 定义
```java
MySQLs.subQuery()
    .with(cte -> {
        cte.comma("cte1").as(c -> c.select(...));
        if (condition) {
            cte.comma("cte2").as(c -> c.select(...));
        }
    })
    .select(...)
```

### 2. SELECT 子句

#### 形式 A: 静态选择单个字段
```java
MySQLs.subQuery()
    .select(Table_.field1)
```

#### 形式 B: 静态选择多个字段
```java
MySQLs.subQuery()
    .select(Table_.field1, Table_.field2, Table_.field3)
```

#### 形式 C: 使用表别名选择整表
```java
MySQLs.subQuery()
    .select("t", SQLs.dot, Table_.T)
```

#### 形式 D: 使用修饰符 (ALL/DISTINCT)
```java
MySQLs.subQuery()
    .selectDistinct()
    .space(Table_.field1)
```

#### 形式 E: 动态选择
```java
MySQLs.subQuery()
    .selects(consumer -> {
        consumer.space(Table_.field1);
        if (condition) {
            consumer.comma(Table_.field2);
        }
    })
```

### 3. FROM 子句

#### 形式 A: 基本表
```java
MySQLs.subQuery()
    .from(Table_.T)
```

#### 形式 B: 带别名
```java
MySQLs.subQuery()
    .from(Table_.T).as("t")
```

#### 形式 C: 分区选择
```java
MySQLs.subQuery()
    .from(Table_.T)
        .partition("p1", "p2")
        .as("t")
```

#### 形式 D: 派生表
```java
MySQLs.subQuery()
    .from(MySQLs.subQuery().select(...).asQuery())
        .as("sub")
```

#### 形式 E: 索引提示
```java
MySQLs.subQuery()
    .from(Table_.T).as("t")
        .useIndex("idx_name", "idx_age")
```

### 4. JOIN 子句

#### 形式 A: 内连接
```java
MySQLs.subQuery()
    .from(Table1_.T).as("t1")
    .join(Table2_.T).as("t2")
        .on(...)
```

#### 形式 B: 左连接
```java
MySQLs.subQuery()
    .from(Table1_.T).as("t1")
    .leftJoin(Table2_.T).as("t2")
        .on(...)
```

#### 形式 C: 嵌套连接
```java
MySQLs.subQuery()
    .from(nested -> nested
        .join(Table1_.T).as("t1")
        .join(Table2_.T).as("t2")
            .on(...)
    )
```

#### 形式 D: 动态连接
```java
MySQLs.subQuery()
    .from(Table_.T).as("t")
    .ifJoin(joins -> {
        if (condition) {
            joins.join(OtherTable_.T).as("o")
                .on(...);
        }
    })
```

### 5. WHERE 子句

#### 形式 A: 静态条件
```java
MySQLs.subQuery()
    .from(Table_.T)
    .where(p -> p.equal(Table_.id, SQLs::param, 1))
```

#### 形式 B: 动态条件
```java
MySQLs.subQuery()
    .from(Table_.T)
    .ifWhere(predicateConsumer -> {
        predicateConsumer.and(p -> p.equal(...));
        if (condition) {
            predicateConsumer.and(p -> p.greater(...));
        }
    })
```

### 6. GROUP BY 子句

#### 形式 A: 简单分组
```java
MySQLs.subQuery()
    .from(Table_.T)
    .groupBy(Table_.category)
```

#### 形式 B: 带 ROLLUP
```java
MySQLs.subQuery()
    .from(Table_.T)
    .groupBy(Table_.category)
    .withRollup()
```

### 7. ORDER BY 子句

#### 形式 A: 简单排序
```java
MySQLs.subQuery()
    .from(Table_.T)
    .orderBy(Table_.createTime.desc())
```

#### 形式 B: 多字段排序
```java
MySQLs.subQuery()
    .from(Table_.T)
    .orderBy(Table_.category.asc())
    .comma(Table_.createTime.desc())
```

### 8. LIMIT 子句

#### 形式 A: 仅限制行数
```java
MySQLs.subQuery()
    .from(Table_.T)
    .limit(10)
```

#### 形式 B: 带偏移
```java
MySQLs.subQuery()
    .from(Table_.T)
    .limit(10, 20)
```

### 9. LOCK 子句

#### 形式 A: FOR UPDATE
```java
MySQLs.subQuery()
    .from(Table_.T)
    .forUpdate()
```

#### 形式 B: FOR SHARE
```java
MySQLs.subQuery()
    .from(Table_.T)
    .forShare()
```

#### 形式 C: 带 NOWAIT/SKIP LOCKED
```java
MySQLs.subQuery()
    .from(Table_.T)
    .forUpdate()
    .nowait()
```

## 可重复调用和不可重复调用方法

### 可重复调用方法 (Repeatable)
- `comma(String name)` - 在 WITH 子句中添加多个 CTE
- `space(Selection selection)` - 选择多个字段
- `comma(Selection selection)` - 选择多个字段
- `and(Function<IPredicate, IPredicate> function)` - 添加多个 WHERE/HAVING 条件
- `commaSpace(GroupByItem item)` - 添加多个 GROUP BY 字段
- `comma(SortItem item)` - 添加多个 ORDER BY 字段
- `useIndex/ignoreIndex/forceIndex` 系列 - 添加多个索引提示
- `join/leftJoin/rightJoin` 系列 - 可以进行多次 JOIN 操作

### 不可重复调用方法 (Non-Repeatable)
- `select()` 系列 - 只能调用一次 select 方法
- `from()` - 只能调用一次 from 方法
- `where()` - 只能调用一次 where 方法 (可以用 and 多次添加条件)
- `groupBy()` - 只能调用一次 groupBy 方法
- `having()` - 只能调用一次 having 方法
- `orderBy()` - 只能调用一次 orderBy 方法
- `limit()` - 只能调用一次 limit 方法
- `forUpdate()/forShare()/lockInShareMode()` - 只能使用一种锁模式
- `asQuery()` - 结束查询构建，只能调用一次
- `withRollup()` - 只能调用一次

## 使用示例

### 示例 1: 简单子查询
```java
SubQuery subQuery = MySQLs.subQuery()
    .select(User_.id)
    .from(User_.T)
    .where(p -> p.equal(User_.status, SQLs::param, "active"))
    .asQuery();
```

### 示例 2: 带 CTE 的子查询
```java
SubQuery subQuery = MySQLs.subQuery()
    .with("active_users")
        .as(c -> c.select(User_.id, User_.name)
            .from(User_.T)
            .where(p -> p.equal(User_.status, SQLs::param, "active")))
    .select("au", SQLs.dot, ActiveUsers_.T)
    .from(_Cte("active_users")).as("au")
    .asQuery();
```

### 示例 3: 带 JOIN 的子查询
```java
SubQuery subQuery = MySQLs.subQuery()
    .select("o", SQLs.dot, Order_.T)
    .from(Order_.T).as("o")
    .join(User_.T).as("u")
        .on(p -> p.equal(Order_.userId, User_.id))
    .where(p -> p.greater(Order_.amount, SQLs::param, 100))
    .asQuery();
```

## 自我进化记录

### 版本 1.0 (当前)
- 初始版本
- 包含完整的方法链 Diagram
- 详细的子句多种形式说明
- 可重复调用和不可重复调用方法的区分
- 使用示例

---

## 未来改进方向

- 添加更多实际项目中的使用案例
- 补充性能优化建议
- 添加常见错误和解决方案
- 持续根据 Army 框架更新维护文档
