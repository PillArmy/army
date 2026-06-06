---
name: "MySQLs-multiUpdate-method-chain"
description: "完整的 Army Criteria API MySQLs.multiUpdate() 方法链知识。学习、解释、记录多表 UPDATE 语句构建流程，涵盖 WITH、UPDATE、JOIN、SET、WHERE、AND 等子句。"
---

# MySQLs.multiUpdate() 方法链 — 完整参考

> **适用范围**: 本技能 **仅限** 用于理解、解释、记录 `MySQLs.multiUpdate()` 入口的 MySQL 特定多表 UPDATE 语句构建流程。覆盖从 `MySQLs.multiUpdate()` 到 `asUpdate()` 的完整方法链，包括 WITH、UPDATE、JOIN、PARTITION、INDEX HINT、SET、WHERE、AND 等阶段。
> **不** 涵盖 batchMultiUpdate、singleUpdate、标准 SQLs.singleUpdate。

> **源码依据**: 本技能基于以下核心源文件编写：`MySQLs.java`（入口定义）、`MySQLUpdate.java`（MySQL UPDATE 接口组合）、`MySQLMultiUpdates.java`（实现）、`MultiUpdateTests.java`（示例）。所有描述均以实际源码接口签名和实现为准。

## 核心入口

```java
// MySQLs.java line 174-176
public static MySQLUpdate._MultiWithSpec<Update> multiUpdate() {
    return MySQLMultiUpdates.simple();
}
```

**返回类型**: `MySQLUpdate._MultiWithSpec<Update>` — 实现类是 `MySQLMultiUpdates.MySQLSimpleUpdate`。

**内部实现**: `MySQLMultiUpdates.simple()`:

```java
// MySQLMultiUpdates.java line 70-72
static _MultiWithSpec<Update> simple() {
    return new MySQLSimpleUpdate();
}
```

---

## 完整方法链 Diagram

> **阅读指南**: 每个叶子节点代表一个可直接调用的方法，带完整参数列表。接口链通过返回类型导航。

```
MySQLs.multiUpdate() → MySQLUpdate._MultiWithSpec<Update>
│
├─① WITH (可选，仅一次声明；comma 追加 CTE 可重复)
│  ├─ 静态 WITH:
│  │  ├─ .with(String name) → MySQLQuery._StaticCteParensSpec
│  │  └─ .withRecursive(String name) → MySQLQuery._StaticCteParensSpec
│  │     └─ (然后: .parens() → .as() → .space() 进入 UPDATE)
│  │
│  └─ 动态 WITH:
│     ├─ .with(Consumer<MySQLCtes> consumer) → MySQLUpdate._MultiUpdateClause<Update>
│     ├─ .withRecursive(Consumer<MySQLCtes> consumer) → MySQLUpdate._MultiUpdateClause<Update>
│     ├─ .ifWith(Consumer<MySQLCtes> consumer) → MySQLUpdate._MultiUpdateClause<Update>
│     └─ .ifWithRecursive(Consumer<MySQLCtes> consumer) → MySQLUpdate._MultiUpdateClause<Update>
│
└─② UPDATE (可选，带修饰符和提示)
   └─ .update(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers)
         → MySQLUpdate._MultiUpdateSpaceClause<Update>
      │
      └─③ 表源 (必须，仅一次，不可重复)
         ├─ .update(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias)
         │     → MySQLUpdate._MultiIndexHintJoinSpec<Update>
         ├─ .update(DerivedTable derivedTable)
         │     → _AsClause<_ParensJoinSpec<Update>>
         ├─ .update(SQLs.DerivedModifier modifier, DerivedTable derivedTable)
         │     → _AsClause<_ParensJoinSpec<Update>>
         ├─ .update(Supplier<T> supplier)
         │     → _AsClause<_ParensJoinSpec<Update>>
         ├─ .update(SQLs.DerivedModifier modifier, Supplier<T> supplier)
         │     → _AsClause<_ParensJoinSpec<Update>>
         ├─ .update(String cteName)
         │     → MySQLUpdate._MultiJoinSpec<Update>
         ├─ .update(String cteName, SQLs.WordAs wordAs, String alias)
         │     → MySQLUpdate._MultiJoinSpec<Update>
         ├─ .update(Function<MySQLQuery._NestedLeftParenSpec<_MultiJoinSpec<Update>>, _MultiJoinSpec<Update>> function)
         │     → MySQLUpdate._MultiJoinSpec<Update>
         └─ .update(TableMeta<?> table)
               → MySQLUpdate._MultiPartitionJoinClause<Update>
         │
         ├─④ INDEX HINT (可选，可重复)
         │  ├─ .useIndex(String indexName)
         │  ├─ .useIndex(String indexName1, String indexName2)
         │  ├─ .useIndex(String indexName1, String indexName2, String indexName3)
         │  ├─ .useIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
         │  ├─ .useIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
         │  ├─ .ifUseIndex(Consumer<Consumer<String>> consumer)
         │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
         │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
         │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
         │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
         │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
         │  ├─ .ifUseIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
         │  ├─ .ignoreIndex(String indexName)
         │  ├─ .ignoreIndex(String indexName1, String indexName2)
         │  ├─ .ignoreIndex(String indexName1, String indexName2, String indexName3)
         │  ├─ .ignoreIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
         │  ├─ .ignoreIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
         │  ├─ .ifIgnoreIndex(Consumer<Consumer<String>> consumer)
         │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
         │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
         │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
         │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
         │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
         │  ├─ .ifIgnoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
         │  ├─ .forceIndex(String indexName)
         │  ├─ .forceIndex(String indexName1, String indexName2)
         │  ├─ .forceIndex(String indexName1, String indexName2, String indexName3)
         │  ├─ .forceIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
         │  ├─ .forceIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
         │  ├─ .ifForceIndex(Consumer<Consumer<String>> consumer)
         │  ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
         │  ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
         │  ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
         │  ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
         │  ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
         │  └─ .ifForceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
         │
         ├─⑤ JOIN (可选，可重复)
         │  ├─ .join(Function<MySQLQuery._NestedLeftParenSpec<_OnClause<_MultiJoinSpec<Update>>>, _OnClause<_MultiJoinSpec<Update>>> function)
         │  ├─ .leftJoin(Function<MySQLQuery._NestedLeftParenSpec<_OnClause<_MultiJoinSpec<Update>>>, _OnClause<_MultiJoinSpec<Update>>> function)
         │  ├─ .rightJoin(Function<MySQLQuery._NestedLeftParenSpec<_OnClause<_MultiJoinSpec<Update>>>, _OnClause<_MultiJoinSpec<Update>>> function)
         │  ├─ .fullJoin(Function<MySQLQuery._NestedLeftParenSpec<_OnClause<_MultiJoinSpec<Update>>>, _OnClause<_MultiJoinSpec<Update>>> function)
         │  ├─ .straightJoin(Function<MySQLQuery._NestedLeftParenSpec<_OnClause<_MultiJoinSpec<Update>>>, _OnClause<_MultiJoinSpec<Update>>> function)
         │  ├─ .crossJoin(Function<MySQLQuery._NestedLeftParenSpec<_MultiJoinSpec<Update>>>, _MultiJoinSpec<Update>> function)
         │  ├─ .join(TableMeta<?> table)
         │  ├─ .leftJoin(TableMeta<?> table)
         │  ├─ .rightJoin(TableMeta<?> table)
         │  ├─ .fullJoin(TableMeta<?> table)
         │  ├─ .straightJoin(TableMeta<?> table)
         │  ├─ .crossJoin(TableMeta<?> table)
         │  ├─ .ifCrossJoin(Consumer<MySQLCrosses> consumer)
         │  ├─ .ifLeftJoin(Consumer<MySQLJoins> consumer)
         │  ├─ .ifJoin(Consumer<MySQLJoins> consumer)
         │  ├─ .ifRightJoin(Consumer<MySQLJoins> consumer)
         │  ├─ .ifFullJoin(Consumer<MySQLJoins> consumer)
         │  └─ .ifStraightJoin(Consumer<MySQLJoins> consumer)
         │
         ├─⑥ ON (JOIN 后必须)
         │  └─ .on(...) → MySQLUpdate._MultiIndexHintOnSpec<Update>
         │
         └─⑦ SET (必须，至少一次)
            ├─ 静态 SET:
            │  ├─ .set(TableField field, Function<T, Expression> operator, T value)
            │  ├─ .set(TableField field, BiFunction<Expression, T, Expression> operator, T value)
            │  ├─ .set(TableField field, TriFunction<Expression, T, U, Expression> operator, T value1, SQLs.WordAnd and, U value2)
            │  └─ ... (更多重载)
            │
            ├─ 动态 SET:
            │  └─ .sets(Consumer<UpdateStatement._BatchItemPairs<TableField>> consumer)
            │
            └─⑧ WHERE (可选，多种形式)
               ├─ .where(IPredicate predicate) → MySQLUpdate._MultiWhereAndSpec<Update>
               ├─ .where(Function<T, IPredicate> expOperator, T operand) → MySQLUpdate._MultiWhereAndSpec<Update>
               ├─ .whereIf(Supplier<IPredicate> supplier) → MySQLUpdate._MultiWhereAndSpec<Update>
               ├─ .where(Consumer<Consumer<IPredicate>> consumer) → Statement._DmlUpdateSpec<Update> (跳过 AND)
               ├─ ... (更多 whereIf 重载形式)
               │
               └─⑨ AND (可选，可重复)
                  ├─ .and(IPredicate predicate) → MySQLUpdate._MultiWhereAndSpec<Update>
                  ├─ .and(Function<T, IPredicate> expOperator, T operand) → MySQLUpdate._MultiWhereAndSpec<Update>
                  ├─ .ifAnd(Supplier<IPredicate> supplier) → MySQLUpdate._MultiWhereAndSpec<Update>
                  ├─ ... (更多 ifAnd 重载形式)
                  │
                  └─⑩ 结束: .asUpdate() → Update
```

---

## 逐层接口详解

### 0. 入口: `MySQLUpdate._MultiWithSpec<Update>`

```java
// MySQLUpdate.java line 420-424
interface _MultiWithSpec<I extends Item> extends _MySQLDynamicWithClause<_MultiUpdateClause<I>>,
        _MySQLStaticWithClause<_MultiUpdateClause<I>>,
        _MultiUpdateClause<I> {
}
```

`_MultiWithSpec` 组合了三种能力：动态 CTE、静态 CTE、UPDATE。所以 `MySQLs.multiUpdate()` 返回的对象可以直接调用 `update()`，也可以先用 `with()` 定义 CTE。

---

### ① WITH (Common Table Expression)

> **语义约束**: `WITH` 关键字仅出现一次（即 `.with(name)` / `.withRecursive(name)` 只能调用一次）。后续 CTE 通过 `_CteComma.comma(String name)` 追加。`.space()` 是唯一退出 CTE 链、进入 UPDATE 的路径。
> **禁止** 在 `.space()` 后再调用 `.with(name)` — 类型系统不提供此路径。

**接口**: `_MySQLDynamicWithClause` + `_MySQLStaticWithClause`

**动态 CTE (运行时构建)**:

```java
MySQLs.multiUpdate()
    .with(builder -> {
        builder.comma("cte1").as(s -> s.select(...).from(...).asQuery());
        builder.comma("cte2").as(s -> s.select(...).from(...).asQuery());
    })
    .update(...)
```

**条件 CTE**:

```java
MySQLs.multiUpdate()
    .ifWith(builder -> { ... })        // 仅当 consumer 内有实际操作时执行
    .ifWithRecursive(builder -> { ... }) // 递归条件 CTE
```

**静态 CTE**:

```java
MySQLs.multiUpdate()
    .with("cte_name")
    .parens(s -> s.select(...).from(...).asQuery())
    .as()
    .space()
    .update(...)
```

---

### ② UPDATE (可选，带修饰符和提示)

```java
// MySQLUpdate.java line 382-405
interface _MultiUpdateClause<I extends Item> extends Item {
    _MultiUpdateSpaceClause<I> update(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers);
    // ... 其他 update 重载
}
```

> **规则**:
> - 可选调用，如果不调用则直接调用 `.update(table)`
> - 可以传入提示（hint）和修饰符（modifier）
> - 调用后必须继续调用 `.space(table)` 来指定表源

**MySQL 支持的修饰符**（来自 MySQLs.java）:
- `MySQLs.LOW_PRIORITY`
- `MySQLs.IGNORE`

**MySQL 支持的提示创建方法**:
- `MySQLs.setVar(String varAssignment)` - 设置 MySQL 变量
- `MySQLs.qbName(String name)` - 查询块名称
- 等等

---

### ③ 表源 (核心)

```java
// MySQLUpdate.java line 376-405
// 多种形式:
_MultiIndexHintJoinSpec<I> update(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);
_AsClause<_ParensJoinSpec<I>> update(DerivedTable derivedTable);
_AsClause<_ParensJoinSpec<I>> update(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable);
<T extends DerivedTable> _AsClause<_ParensJoinSpec<I>> update(Supplier<T> supplier);
<T extends DerivedTable> _AsClause<_ParensJoinSpec<I>> update(@Nullable SQLs.DerivedModifier modifier, Supplier<T> supplier);
_MultiJoinSpec<I> update(String cteName);
_MultiJoinSpec<I> update(String cteName, SQLs.WordAs wordAs, String alias);
_MultiJoinSpec<I> update(Function<MySQLQuery._NestedLeftParenSpec<_MultiJoinSpec<I>>, _MultiJoinSpec<I>> function);
_MultiPartitionJoinClause<I> update(TableMeta<?> table);
```

> **规则**:
> - **必须调用一次，且仅能调用一次**
> - 支持多种表源：`TableMeta`、`DerivedTable`、CTE、嵌套 join
> - `SQLs.WordAs` 固定传入 `SQLs.AS`
> - `tableAlias` 必须提供非空字符串
> - 不可重复调用 — 类型系统和实现都强制执行此约束

**使用示例** (来自测试):

```java
// MultiUpdateTests.java line 44-45
final Update stmt;
stmt = MySQLs.multiUpdate()
        .with("cte").as(sw -> sw.select(...).asQuery()).space()
        .update(ChinaProvince_.T, AS, "p")
        // ...
```

---

### ④ INDEX HINT (可选，可重复)

```java
// MySQLMultiUpdates.java line 325-531
// 多种形式:
_MultiIndexHintJoinSpec<I> useIndex(String indexName);
_MultiIndexHintJoinSpec<I> useIndex(String indexName1, String indexName2);
_MultiIndexHintJoinSpec<I> useIndex(String indexName1, String indexName2, String indexName3);
_MultiIndexHintJoinSpec<I> useIndex(Consumer<Clause._StaticStringSpaceClause> consumer);
_MultiIndexHintJoinSpec<I> useIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer);
_MultiIndexHintJoinSpec<I> ifUseIndex(Consumer<Consumer<String>> consumer);
// ... 更多 useIndex/ignoreIndex/forceIndex 重载，包括 FOR JOIN/FOR ORDER BY/FOR GROUP BY
```

**三种 INDEX HINT**:
1. **USE INDEX**: 建议 MySQL 使用指定索引
2. **IGNORE INDEX**: 告诉 MySQL 忽略指定索引
3. **FORCE INDEX**: 强制 MySQL 使用指定索引

**使用场景**: 
- 当 MySQL 查询优化器选择了错误的执行计划时
- 为了提高查询性能，指定更好的索引

---

### ⑤ JOIN (可选，可重复)

```java
// MySQLMultiUpdates.java line 209-301
// 多种 join 形式:
_OnClause<_MultiJoinSpec<I>> join(Function<MySQLQuery._NestedLeftParenSpec<_OnClause<_MultiJoinSpec<I>>>, _OnClause<_MultiJoinSpec<I>>> function);
_OnClause<_MultiJoinSpec<I>> leftJoin(Function<MySQLQuery._NestedLeftParenSpec<_OnClause<_MultiJoinSpec<I>>>, _OnClause<_MultiJoinSpec<I>>> function);
_OnClause<_MultiJoinSpec<I>> rightJoin(Function<MySQLQuery._NestedLeftParenSpec<_OnClause<_MultiJoinSpec<I>>>, _OnClause<_MultiJoinSpec<I>>> function);
_OnClause<_MultiJoinSpec<I>> fullJoin(Function<MySQLQuery._NestedLeftParenSpec<_OnClause<_MultiJoinSpec<I>>>, _OnClause<_MultiJoinSpec<I>>> function);
_OnClause<_MultiJoinSpec<I>> straightJoin(Function<MySQLQuery._NestedLeftParenSpec<_OnClause<_MultiJoinSpec<I>>>, _OnClause<_MultiJoinSpec<I>>> function);
_MultiJoinSpec<I> crossJoin(Function<MySQLQuery._NestedLeftParenSpec<_MultiJoinSpec<I>>, _MultiJoinSpec<I>> function);
// ... 还有 ifCrossJoin/ifLeftJoin/ifJoin/ifRightJoin/ifFullJoin/ifStraightJoin
// ... 还有直接接受 TableMeta 的重载
```

**JOIN 类型**:
- `join()` / `innerJoin()`: 内连接
- `leftJoin()`: 左外连接
- `rightJoin()`: 右外连接
- `fullJoin()`: 全外连接
- `straightJoin()`: 强制连接顺序
- `crossJoin()`: 交叉连接

**使用示例** (来自测试):

```java
// MultiUpdateTests.java line 51-53
.update(ChinaProvince_.T, AS, "p").useIndex(FOR, JOIN, "PRIMARY")
.join(ChinaRegion_.T, AS, "c").useIndex(FOR, JOIN, "PRIMARY").on(ChinaRegion_.id::equal, ChinaProvince_.id)
.join("cte").on(ChinaRegion_.id::equal, SQLs.refField("cte", ChinaRegion_.ID))
```

---

### ⑥ ON (JOIN 后必须)

```java
// Statement.java
_OnClause<_MultiJoinSpec<I>> on(IPredicate predicate);
_OnClause<_MultiJoinSpec<I>> on(Function<T, IPredicate> expOperator, T operand);
// ... 更多重载
```

> **规则**:
> - 每个 JOIN 后必须调用 ON 子句
> - 定义表之间的连接条件

---

### ⑦ SET (必须，至少一次)

#### 静态 SET

```java
// UpdateStatement.java
_MultiWhereSpec<I> set(TableField field, Function<T, Expression> operator, T value);
_MultiWhereSpec<I> set(TableField field, BiFunction<Expression, T, Expression> operator, T value);
_MultiWhereSpec<I> set(TableField field, TriFunction<Expression, T, U, Expression> operator, T value1, SQLs.WordAnd and, U value2);
// ... 更多重载
```

#### 动态 SET

```java
// MySQLUpdate.java line 245-247
interface _MultiSetClause<I extends Item> extends UpdateStatement._StaticBatchSetClause<TableField, _MultiWhereSpec<I>>,
        UpdateStatement._DynamicSetClause<UpdateStatement._BatchItemPairs<TableField>, _MultiWhereSpec<I>> {
}

// 动态 SET 方法
_MultiWhereSpec<I> sets(Consumer<UpdateStatement._BatchItemPairs<TableField>> consumer);
```

**使用示例** (来自测试):

```java
// MultiUpdateTests.java line 54
.set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, gdpAmount)
```

---

### ⑧ WHERE (可选)

#### 基本 WHERE 方法

```java
// Statement.java
WR where(Consumer<Consumer<IPredicate>> consumer);
WA where(IPredicate predicate);
<T> WA where(Function<T, IPredicate> expOperator, T operand);
WA whereIf(Supplier<IPredicate> supplier);
<T> WA whereIf(Function<T, IPredicate> expOperator, @Nullable T value);
// ... 更多 whereIf 重载
```

#### 返回值:
- `.where(IPredicate)` / `.where(Function, operand)` → `_MultiWhereAndSpec<I>` (可继续 `.and(...)`)
- `.where(Consumer)` → `_DmlUpdateSpec<I>` (直接跳转到 asUpdate，不能再 AND)

---

### ⑨ AND (可选，可重复)

```java
WA and(IPredicate predicate);
<T> WA and(Function<T, IPredicate> expOperator, T operand);
WA ifAnd(Supplier<IPredicate> supplier);
<T> WA ifAnd(Function<T, IPredicate> expOperator, @Nullable T value);
// ... 更多 ifAnd 重载
```

---

### ⑩ 结束语句

```java
// Statement.java
interface _DmlUpdateSpec<I extends Item> extends Item {
    I asUpdate();
}
```

- **返回类型**: `Update` — 可传给 `session.update(update)`
- **不可再次调用** — 调用后语句已标记为 prepared，再次操作会抛异常

---

## 完整示例

### 示例 1: 简单多表 UPDATE

```java
// MultiUpdateTests.java line 34-68
final Update stmt;
stmt = MySQLs.multiUpdate()
        .with("cte").as(sw -> sw.select(ChinaRegion_.id)
                .from(ChinaRegion_.T, AS, "c")
                .where(ChinaRegion_.id.in(SQLs::rowParam, extractRegionIdList(regionList)))
                .and(ChinaRegion_.regionType.equal(SQLs::param, RegionType.PROVINCE))
                .asQuery()
        ).space()
        .update(ChinaProvince_.T, AS, "p").useIndex(FOR, JOIN, "PRIMARY")
        .join(ChinaRegion_.T, AS, "c").useIndex(FOR, JOIN, "PRIMARY").on(ChinaRegion_.id::equal, ChinaProvince_.id)
        .join("cte").on(ChinaRegion_.id::equal, SQLs.refField("cte", ChinaRegion_.ID))
        .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, gdpAmount)
        .where(ChinaRegion_.id::in, SQLs.subQuery()
                .select(s -> s.space(SQLs.refField("subCte", ChinaRegion_.ID)))
                .from("cte", AS, "subCte")
                .asQuery()
        )
        .and(ChinaRegion_.createTime.between(SQLs::param, now.minusMinutes(10), AND, now.plusSeconds(1)))
        .and(ChinaRegion_.regionGdp.plus(SQLs::param, gdpAmount).greaterEqual(0))
        .asUpdate();
```

### 示例 2: 带修饰符和提示的 UPDATE

```java
// MultiUpdateTests.java line 153-187
final Supplier<List<Hint>> hintSupplier = () -> Collections.singletonList(MySQLs.setVar("foreign_key_checks=OFF"));
final Update stmt;
stmt = MySQLs.multiUpdate()
        .with("cte").as(sw -> sw.select(...).asQuery()).space()
        .update(hintSupplier, Collections.singletonList(MySQLs.LOW_PRIORITY))
        .space(ChinaProvince_.T, AS, "p").useIndex(FOR, JOIN, "PRIMARY")
        .join(ChinaRegion_.T, AS, "c").useIndex(FOR, JOIN, "PRIMARY").on(ChinaRegion_.id::equal, ChinaProvince_.id)
        .join("cte").on(ChinaRegion_.id::equal, SQLs.refField("cte", ChinaRegion_.ID))
        .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, gdpAmount)
        .where(...)
        .and(...)
        .asUpdate();
```

### 示例 3: 自连接 UPDATE

```java
// MultiUpdateTests.java line 116-148
final Update stmt;
stmt = MySQLs.multiUpdate()
        .update(ChinaRegion_.T, AS, "c").useIndex(FOR, JOIN, "PRIMARY")
        .join(SQLs.subQuery()
                .select(ChinaRegion_.id)
                .from(ChinaRegion_.T, AS, "ss")
                .where(ChinaRegion_.id.in(SQLs::rowParam, idList))
                .and(ChinaRegion_.regionType.equal(SQLs::param, RegionType.NONE))
                .asQuery()
        ).as("sc").on(ChinaRegion_.id::equal, SQLs.refField("sc", ChinaRegion_.ID))
        .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, gdpAmount)
        .where(ChinaRegion_.id.in(SQLs::rowParam, idList))
        .and(...)
        .asUpdate();
```

### 示例 4: 批量多表 UPDATE

```java
// MultiUpdateTests.java line 73-112
final BatchUpdate stmt;
stmt = MySQLs.batchMultiUpdate()
        .with("cte").as(sw -> sw.select(...).asQuery()).space()
        .update(ChinaProvince_.T, AS, "p").useIndex(FOR, JOIN, "PRIMARY")
        .join(ChinaRegion_.T, AS, "c").useIndex(FOR, JOIN, "PRIMARY").on(ChinaRegion_.id::equal, ChinaProvince_.id)
        .join("cte").on(ChinaRegion_.id::equal, SQLs.refField("cte", ChinaRegion_.ID))
        .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, gdpAmount)
        .where(...)
        .and(...)
        .asUpdate()
        .namedParamList(idMapList);
```

---

## 实现类继承链

```
SQLSyntax (MySQLs extends)
    └─ MySQLs
        └─ .multiUpdate() → MySQLMultiUpdates.simple()
            └─ MySQLSimpleUpdate
                └─ MySQLMultiUpdates<I>
                    └─ JoinableUpdate
                        └─ ArmyStmtSpec
```

---

## 关键约束和规则

### 层级约束 (通过接口链强制执行)

接口名称本身就编码了当前上下文中可用的方法，DSL 通过**返回类型引导**下一个可调用的方法：

1. **WITH → UPDATE**: 调用 `with()` 后只能进入 `_MultiUpdateClause`，即只能调用 `update()`
2. **UPDATE → 表源**: 调用 `update(modifier)` 后进入 `_MultiUpdateSpaceClause`，必须调用 `space()` 指定表源
3. **表源 → JOIN/SET**: 调用 `update(table)` 后进入 `_MultiIndexHintJoinSpec`，可继续 JOIN 或直接 SET
4. **JOIN → ON**: 调用 JOIN 后必须调用 ON
5. **ON → 下一个 JOIN/SET**: 调用 ON 后可继续 JOIN 或直接 SET
6. **SET → WHERE/AND**: 调用 SET 后进入 `_MultiWhereSpec`，可调用 WHERE 或继续 SET
7. **WHERE → AND/asUpdate**: 调用 `where(IPredicate)` 后进入 `_MultiWhereAndSpec`，可继续 AND 或直接 asUpdate
8. **AND → AND/asUpdate**: 调用 AND 后仍在 `_MultiWhereAndSpec`，可继续 AND 或直接 asUpdate
9. **最终 → asUpdate**: 最终必须调用 `asUpdate()` 来结束构建并获得可执行的 `Update` 对象

### 方法调用限制

| 方法/阶段 | 是否必须 | 可重复调用 | 说明 |
|----------|---------|-----------|------|
| `MySQLs.multiUpdate()` | 是 | 否 | 入口，只能调用一次（每次调用创建新语句） |
| `with()` / `withRecursive()` | 否 | 否 | WITH 关键字只能出现一次，多个 CTE 用 comma 追加 |
| `update(hints, modifiers)` | 否 | 否 | 可选调用，带修饰符和提示 |
| `update(table)` / `space(table)` | 是 | 否 | 必须且只能调用一次，指定第一个表源 |
| `join()` / `leftJoin()` 等 | 否 | 是 | 可以多次调用，添加多个 join |
| `on()` | 是（每个 JOIN 后） | 否 | 每个 JOIN 后必须且只能调用一次 ON |
| `useIndex()` / `ignoreIndex()` / `forceIndex()` | 否 | 是 | 可以多次调用，为每个表添加索引提示 |
| `set()` / `sets()` | 是 | 是 | 至少调用一次，可以多次调用添加多个 set |
| `where()` | 否 | 否 | 只能调用一次（但可通过 Consumer 组合多条件） |
| `and()` | 否 | 是 | WHERE 后可以多次调用 AND |
| `asUpdate()` | 是 | 否 | 必须调用一次，结束构建 |

### null 检查

- `tableAlias` 不可为 null
- `table` 不可为 null
- CTE 的名称不可为 null
- 条件形式的 `ifWhere` / `ifAnd` 的 value 可为 null (null 时忽略该条件)

### 类型安全

- 返回类型的链式设计确保：不会在错误的阶段调用错误的方法
- `TableField` 类型确保引用正确的表字段

---

## 与其他 UPDATE 入口的区别

| 入口 | 返回类型 | 场景 |
|-----|---------|------|
| `MySQLs.multiUpdate()` | `MySQLUpdate._MultiWithSpec<Update>` | MySQL 多表 UPDATE，本技能覆盖 |
| `MySQLs.batchMultiUpdate()` | `MySQLUpdate._MultiWithSpec<_BatchUpdateParamSpec>` | MySQL 批量多表 UPDATE |
| `MySQLs.singleUpdate()` | `MySQLUpdate._SingleWithSpec<Update>` | MySQL 单表 UPDATE |
| `SQLs.singleUpdate()` | `_WithSpec<Update>` | 标准 SQL 单表 UPDATE |

---

## 相关接口参考

| 接口 | 说明 |
|------|------|
| `MySQLUpdate._MultiWithSpec` | 起始接口，支持 WITH 子句 |
| `MySQLUpdate._MultiUpdateClause` | 支持 UPDATE 子句 |
| `MySQLUpdate._MultiUpdateSpaceClause` | 支持 UPDATE 后调用 space 指定表源 |
| `MySQLUpdate._MultiIndexHintJoinSpec` | 支持 INDEX HINT 和 JOIN |
| `MySQLUpdate._MultiJoinSpec` | 支持 JOIN |
| `MySQLUpdate._MultiIndexHintOnSpec` | JOIN 后支持 INDEX HINT 和 ON |
| `MySQLUpdate._MultiSetClause` | 支持 SET 子句 |
| `MySQLUpdate._MultiWhereSpec` | 支持 WHERE 子句 |
| `MySQLUpdate._MultiWhereAndSpec` | 支持 AND 子句 |
| `Update` | 标准更新语句 |

---

## multiUpdate vs singleDelete 对比分析

### 相同点

| 特性 | multiUpdate | singleDelete |
|------|-------------|--------------|
| WITH (CTE) | ✅ 支持 | ✅ 支持 |
| 修饰符 | ✅ LOW_PRIORITY/IGNORE | ✅ LOW_PRIORITY/IGNORE/QUICK |
| WHERE/AND | ✅ 支持 | ✅ 支持 |
| Batch 版本 | ✅ batchMultiUpdate | ✅ batchSingleDelete |
| Hint | ✅ 支持 | ✅ 支持 |

### 核心差异

| 特性 | multiUpdate | singleDelete |
|------|-------------|--------------|
| **操作类型** | 更新数据 (UPDATE) | 删除数据 (DELETE) |
| **表数量** | 多表更新 | 单表删除 |
| **JOIN** | ✅ 支持 | ❌ 不支持 |
| **INDEX HINT** | ✅ 支持 | ❌ 不支持 |
| **SET 子句** | ✅ 必须 | ❌ 不支持 |
| **ORDER BY/LIMIT** | ❌ 不支持 | ✅ 支持 |
| **返回类型** | `Update` | `Delete` |

### 选择指南

| 场景 | 推荐使用 | 原因 |
|------|----------|------|
| 更新数据 | `multiUpdate` | 专为数据更新设计 |
| 删除数据 | `singleDelete` | 专为数据删除设计 |
| 需要 SET 子句 | `multiUpdate` | UPDATE 必须有 SET |
| 需要 ORDER BY/LIMIT | `singleDelete` | 支持排序和限制 |
| 多表操作 | `multiUpdate` | 支持 JOIN |

---

## multiUpdate vs multiDelete 对比分析

### 相同点

| 特性 | multiUpdate | multiDelete |
|------|-------------|-------------|
| WITH (CTE) | ✅ 支持 | ✅ 支持 |
| INDEX HINT | ✅ useIndex/ignoreIndex/forceIndex | ✅ useIndex/ignoreIndex/forceIndex |
| JOIN 类型 | ✅ join/leftJoin/rightJoin/fullJoin/straightJoin/crossJoin | ✅ join/leftJoin/rightJoin/fullJoin/straightJoin/crossJoin |
| WHERE/AND | ✅ 支持 | ✅ 支持 |
| 修饰符 | ✅ LOW_PRIORITY/IGNORE | ✅ LOW_PRIORITY/IGNORE/QUICK |
| Batch 版本 | ✅ batchMultiUpdate | ✅ batchMultiDelete |
| 嵌套 JOIN | ✅ 支持 | ✅ 支持 |
| CTE 引用 | ✅ 支持 update(cteName) | ✅ 支持 delete(alias...) 后 from(cteName) |
| Derived Table | ✅ 支持 | ✅ 支持 |

### 核心差异

| 特性 | multiUpdate | multiDelete |
|------|-------------|-------------|
| **操作类型** | 更新数据 (UPDATE) | 删除数据 (DELETE) |
| **SET 子句** | ✅ `set()` / `sets()` | ❌ 不支持 |
| **USING 语法** | ❌ 不支持 | ✅ 支持 `using(table)` |
| **表源指定** | `update(table, AS, alias)` | `delete(alias...)` + `from/using(table)` |
| **主表概念** | 第一个 update 的表是更新目标 | 需要通过 delete() 明确指定删除的表 |
| **返回类型** | `Update` / `BatchUpdate` | `Delete` / `BatchDelete` |

### 设计意图

- **multiUpdate**：用于更新多个表中的数据，主表（第一个 update 的表）是更新目标，其他表通过 JOIN 引用
- **multiDelete**：用于从多个表中删除满足条件的数据，需要明确指定哪些表需要删除数据

### SQL 语法对比

**multiUpdate 生成的 SQL**:
```sql
UPDATE china_city AS c 
JOIN china_region AS r ON c.id = r.id 
SET c.status = 1
WHERE r.region_type = 'CITY'
```

**multiDelete 生成的 SQL**:
```sql
DELETE c, r FROM china_city AS c 
JOIN china_region AS r ON c.id = r.id 
WHERE r.region_type = 'CITY'
-- 或 USING 语法:
DELETE c, r USING china_city AS c 
JOIN china_region AS r ON c.id = r.id 
WHERE r.region_type = 'CITY'
```

### 选择指南

| 场景 | 推荐使用 | 原因 |
|------|----------|------|
| 更新多个相关表的数据 | `multiUpdate` | 专为多表更新设计 |
| 从多个相关表删除数据 | `multiDelete` | 专为多表删除设计 |
| 需要 SET 子句 | `multiUpdate` | UPDATE 必须有 SET |
| 需要 USING 语法 | `multiDelete` | 支持 MySQL 特有的 USING 语法 |
| 只需要更新一个表的数据 | `singleUpdate` | 更轻量级 |

---

## multiUpdate vs query 对比分析

### 相同点

| 特性 | multiUpdate | query |
|------|-------------|-------|
| WITH (CTE) | ✅ 支持 | ✅ 支持 |
| INDEX HINT | ✅ useIndex/ignoreIndex/forceIndex | ✅ useIndex/ignoreIndex/forceIndex |
| JOIN 类型 | ✅ join/leftJoin/rightJoin/fullJoin/straightJoin/crossJoin | ✅ join/leftJoin/rightJoin/fullJoin/straightJoin/crossJoin |
| WHERE/AND | ✅ 支持 | ✅ 支持 |
| 嵌套 JOIN | ✅ 支持 | ✅ 支持 |
| CTE 引用 | ✅ 支持 | ✅ 支持 |
| Derived Table | ✅ 支持 | ✅ 支持 |

### 核心差异

| 特性 | multiUpdate | query |
|------|-------------|-------|
| **操作类型** | 更新数据 (UPDATE) | 查询数据 (SELECT) |
| **SET 子句** | ✅ 支持 | ❌ 不支持 |
| **SELECT 子句** | ❌ 不支持 | ✅ 支持 |
| **GROUP BY/HAVING** | ❌ 不支持 | ✅ 支持 |
| **ORDER BY/LIMIT** | ❌ 不支持 | ✅ 支持 |
| **FOR UPDATE/SHARE** | ❌ 不支持 | ✅ 支持 |
| **返回类型** | `Update` | `Select` |

### 设计意图

- **multiUpdate**：用于更新多个表中的数据，强调数据更新操作
- **query**：用于查询数据，支持完整的 SELECT 特性集合

### 典型使用场景

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

### 选择指南

| 场景 | 推荐使用 | 原因 |
|------|----------|------|
| 更新数据 | `multiUpdate` | 专为数据更新设计 |
| 查询数据 | `query` | 专为 SELECT 查询设计 |
| 需要 SET 子句 | `multiUpdate` | UPDATE 必须有 SET |
| 需要 SELECT 子句 | `query` | 支持完整的选择项 |
| 需要 GROUP BY | `query` | 支持分组聚合 |
| 需要 ORDER BY/LIMIT | `query` | 支持排序和分页 |

---

## 自我进化指南

当 Army Criteria API 更新时，请按以下步骤更新本文档：

1. **检查接口变更**: 重新读取 `MySQLUpdate.java`、`Statement.java`
2. **检查实现变更**: 重新读取 `MySQLMultiUpdates.java`
3. **检查测试更新**: 查看 `MultiUpdateTests.java` 的新增测试用例
4. **更新 Diagram**: 如有新方法/新子句，补充到 Diagram 中
5. **更新示例**: 补充新增用法的示例
6. **更新约束**: 如有新的语义约束，补充到规则部分
7. **检查相关文件**: 查看 `MySQLs.java`、`MySQLStatement.java` 等文件的变更
