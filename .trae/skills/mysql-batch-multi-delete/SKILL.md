---
name: MySQLs.batchMultiDelete() 方法链
description: 完整的 Army Criteria API MySQLs.batchMultiDelete() 方法链知识。用于理解、解释或记录从 MySQLs.batchMultiDelete() 到 namedParamList() 的完整批量多表 DELETE 语句构建流程，包括 MySQL 特有的修饰符、提示、分区、JOIN、USING、WHERE、AND 等语法。
---

# MySQLs.batchMultiDelete() 方法链 — 完整参考

> **适用范围**: 本技能 **仅限** 用于理解、解释、记录 `MySQLs.batchMultiDelete()` 入口的 MySQL 特定批量多表 DELETE 语句构建流程。覆盖从 `MySQLs.batchMultiDelete()` 到 `namedParamList()` 的完整方法链，包括 WITH、DELETE、FROM/USING、JOIN、PARTITION、INDEX HINT、WHERE、AND 等阶段。
> **不** 涵盖 multiDelete、singleDelete、batchSingleDelete、标准 SQLs 等其他 API。
> **源码依据**: 本技能基于以下核心源文件编写——`MySQLs.java`（入口定义）、`MySQLDelete.java`（MySQL DELETE 接口组合）、`MySQLMultiDeletes.java`（实现）。所有描述均以实际源码接口签名和实现为准。

## 核心入口

```java
// MySQLs.java line 196-199
public static MySQLDelete._MultiWithSpec<Statement._BatchDeleteParamSpec> batchMultiDelete() {
    return MySQLMultiDeletes.batch();
}
```

**返回类型**: `MySQLDelete._MultiWithSpec<Statement._BatchDeleteParamSpec>` — 实现类是 `MySQLMultiDeletes.MySQLBatchDelete`

**内部实现**: `MySQLMultiDeletes.batch()`:

```java
// MySQLMultiDeletes.java line 71-73
static _MultiWithSpec<_BatchDeleteParamSpec> batch() {
    return new MySQLBatchDelete();
}
```

## 完整方法链 Diagram

```
MySQLs.batchMultiDelete() → MySQLDelete._MultiWithSpec<_BatchDeleteParamSpec>
│
├─① WITH (可选，仅一次声明；comma 追加 CTE 可重复)
│  ├─ 静态 WITH:
│  │  ├─ .with(String name)       → MySQLQuery._StaticCteParensSpec
│  │  └─ .withRecursive(String name) → MySQLQuery._StaticCteParensSpec
│  │     └─ (然后: .parens() → .as() → .space() 进入 DELETE)
│  │
│  └─ 动态 WITH:
│     ├─ .with(Consumer<MySQLCtes> consumer)       → MySQLDelete._MySQLMultiDeleteClause
│     ├─ .withRecursive(Consumer<MySQLCtes> consumer) → MySQLDelete._MySQLMultiDeleteClause
│     ├─ .ifWith(Consumer<MySQLCtes> consumer)      → MySQLDelete._MySQLMultiDeleteClause
│     └─ .ifWithRecursive(Consumer<MySQLCtes> consumer) → MySQLDelete._MySQLMultiDeleteClause
│
└─② DELETE (核心，二选一)
   ├─ 方式 A: 带修饰符和提示
   │  └─ .delete(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers)
   │        → MySQLDelete._MultiDeleteFromAliasClause
   │        │
   │        └─ ③ FROM/SPACE (指定要删除的表别名)
   │           ├─ .from(String alias)                  → MySQLDelete._SimpleMultiDeleteUsingClause
   │           ├─ .from(String alias1, String alias2)  → MySQLDelete._SimpleMultiDeleteUsingClause
   │           ├─ .from(String alias1, String alias2, String alias3) → MySQLDelete._SimpleMultiDeleteUsingClause
   │           ├─ .from(String alias1, String alias2, String alias3, String alias4) → MySQLDelete._SimpleMultiDeleteUsingClause
   │           ├─ .from(List<String> aliasList)        → MySQLDelete._SimpleMultiDeleteUsingClause
   │           ├─ .from(Consumer<Consumer<String>> consumer) → MySQLDelete._SimpleMultiDeleteUsingClause
   │           ├─ .space(String alias)                 → MySQLDelete._MultiDeleteFromTableClause
   │           ├─ .space(String alias1, String alias2) → MySQLDelete._MultiDeleteFromTableClause
   │           ├─ .space(String alias1, String alias2, String alias3) → MySQLDelete._MultiDeleteFromTableClause
   │           ├─ .space(String alias1, String alias2, String alias3, String alias4) → MySQLDelete._MultiDeleteFromTableClause
   │           ├─ .space(List<String> aliasList)       → MySQLDelete._MultiDeleteFromTableClause
   │           └─ .space(Consumer<Consumer<String>> consumer) → MySQLDelete._MultiDeleteFromTableClause
   │
   └─ 方式 B: 直接指定表别名
      └─ .delete(String alias)                       → MySQLDelete._MultiDeleteFromTableClause
      └─ .delete(String alias1, String alias2)       → MySQLDelete._MultiDeleteFromTableClause
      └─ .delete(String alias1, String alias2, String alias3) → MySQLDelete._MultiDeleteFromTableClause
      └─ .delete(String alias1, String alias2, String alias3, String alias4) → MySQLDelete._MultiDeleteFromTableClause
      └─ .delete(List<String> aliasList)             → MySQLDelete._MultiDeleteFromTableClause
      └─ .delete(Consumer<Consumer<String>> consumer) → MySQLDelete._MultiDeleteFromTableClause
         │
         └─ ④ FROM/USING (必须，指定表来源，二选一语法)
            ├─ .from(TableMeta<?> table)              → MySQLDelete._MultiPartitionJoinClause
            ├─ .using(TableMeta<?> table)             → MySQLDelete._MultiPartitionJoinClause
            ├─ .from(Function<_NestedLeftParenSpec, _MultiJoinSpec>) → MySQLDelete._MultiJoinSpec
            ├─ .using(Function<_NestedLeftParenSpec, _MultiJoinSpec>) → MySQLDelete._MultiJoinSpec
            │
            └─ ⑤ PARTITION (可选，三种形式)
               ├─ .partition(String first, String... rest)
               ├─ .partition(Consumer<Consumer<String>> consumer)
               └─ .ifPartition(Consumer<Consumer<String>> consumer)
                  → 都返回 MySQLDelete._MultiIndexHintJoinSpec
                  │
                  └─ ⑥ AS (必须，指定表别名)
                     └─ .as(String alias)              → MySQLDelete._MultiIndexHintJoinSpec
                        │
                        ├─ ⑦ INDEX HINT (可选，可重复，多种形式)
                        │  ├─ .useIndex(String indexName)
                        │  ├─ .useIndex(String indexName1, String indexName2)
                        │  ├─ .useIndex(String indexName1, String indexName2, String indexName3)
                        │  ├─ .useIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
                        │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
                        │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
                        │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
                        │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
                        │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
                        │  ├─ .ifUseIndex(Consumer<Consumer<String>> consumer)
                        │  ├─ .ifUseIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
                        │  ├─ .ignoreIndex(String indexName)
                        │  ├─ .ignoreIndex(String indexName1, String indexName2)
                        │  ├─ .ignoreIndex(String indexName1, String indexName2, String indexName3)
                        │  ├─ .ignoreIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
                        │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
                        │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
                        │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
                        │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
                        │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
                        │  ├─ .ifIgnoreIndex(Consumer<Consumer<String>> consumer)
                        │  ├─ .ifIgnoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
                        │  ├─ .forceIndex(String indexName)
                        │  ├─ .forceIndex(String indexName1, String indexName2)
                        │  ├─ .forceIndex(String indexName1, String indexName2, String indexName3)
                        │  ├─ .forceIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
                        │  ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
                        │  ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
                        │  ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
                        │  ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
                        │  ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
                        │  ├─ .ifForceIndex(Consumer<Consumer<String>> consumer)
                        │  └─ .ifForceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
                        │     → 都返回 MySQLDelete._MultiIndexHintJoinSpec
                        │
                        └─ ⑧ JOIN (可选，可重复，多种类型)
                           ├─ .join(TableMeta<?> table)                    → MySQLDelete._MultiPartitionOnClause
                           ├─ .leftJoin(TableMeta<?> table)                → MySQLDelete._MultiPartitionOnClause
                           ├─ .rightJoin(TableMeta<?> table)               → MySQLDelete._MultiPartitionOnClause
                           ├─ .fullJoin(TableMeta<?> table)                → MySQLDelete._MultiPartitionOnClause
                           ├─ .straightJoin(TableMeta<?> table)            → MySQLDelete._MultiPartitionOnClause
                           ├─ .crossJoin(TableMeta<?> table)               → MySQLDelete._MultiIndexHintJoinSpec
                           ├─ .join(Function<_NestedLeftParenSpec, _OnClause>) → MySQLDelete._OnClause
                           ├─ .leftJoin(Function<_NestedLeftParenSpec, _OnClause>) → MySQLDelete._OnClause
                           ├─ .rightJoin(Function<_NestedLeftParenSpec, _OnClause>) → MySQLDelete._OnClause
                           ├─ .fullJoin(Function<_NestedLeftParenSpec, _OnClause>) → MySQLDelete._OnClause
                           ├─ .straightJoin(Function<_NestedLeftParenSpec, _OnClause>) → MySQLDelete._OnClause
                           ├─ .crossJoin(Function<_NestedLeftParenSpec, _MultiJoinSpec>) → MySQLDelete._MultiJoinSpec
                           ├─ .ifJoin(Consumer<MySQLJoins> consumer)       → MySQLDelete._MultiJoinSpec
                           ├─ .ifLeftJoin(Consumer<MySQLJoins> consumer)   → MySQLDelete._MultiJoinSpec
                           ├─ .ifRightJoin(Consumer<MySQLJoins> consumer)  → MySQLDelete._MultiJoinSpec
                           ├─ .ifFullJoin(Consumer<MySQLJoins> consumer)   → MySQLDelete._MultiJoinSpec
                           ├─ .ifStraightJoin(Consumer<MySQLJoins> consumer) → MySQLDelete._MultiJoinSpec
                           ├─ .ifCrossJoin(Consumer<MySQLCrosses> consumer) → MySQLDelete._MultiJoinSpec
                           │
                           └─ (JOIN 内部: PARTITION → AS → INDEX HINT → ON)
                              ├─ .partition(String first, String... rest)
                              ├─ .as(String alias)
                              ├─ (INDEX HINT 方法同上)
                              └─ .on(...) → 回到 MySQLDelete._MultiJoinSpec
                                 │
                                 └─ ⑨ WHERE (可选，多种形式)
                                    ├─ .where(IPredicate predicate)                  → MySQLDelete._MultiWhereAndSpec
                                    ├─ .where(Function<T, IPredicate> expOperator, T operand) → MySQLDelete._MultiWhereAndSpec
                                    ├─ .whereIf(Supplier<IPredicate> supplier)      → MySQLDelete._MultiWhereAndSpec
                                    ├─ .where(Consumer<Consumer<IPredicate>> consumer) → Statement._DmlDeleteSpec (跳过 AND)
                                    ├─ (更多 whereIf 重载形式)
                                    │
                                    └─⑩ AND (可选，可重复)
                                       ├─ .and(IPredicate predicate)                  → MySQLDelete._MultiWhereAndSpec
                                       ├─ .and(Function<T, IPredicate> expOperator, T operand) → MySQLDelete._MultiWhereAndSpec
                                       ├─ .ifAnd(Supplier<IPredicate> supplier)      → MySQLDelete._MultiWhereAndSpec
                                       └─ (更多 ifAnd 重载形式)
                                          │
                                          └─⑪ 结束: .asDelete() → _BatchDeleteParamSpec
                                             │
                                             └─⑫ .namedParamList(List<?> paramList) → BatchDelete
```

## 逐层接口详解

### 0. 入口: `MySQLDelete._MultiWithSpec<_BatchDeleteParamSpec>`

```java
// MySQLDelete.java line 356-360
interface _MultiWithSpec<I extends Item> extends _MySQLDynamicWithClause<_MySQLMultiDeleteClause<I>>,
        _MySQLStaticWithClause<_MySQLMultiDeleteClause<I>>,
        _MySQLMultiDeleteClause<I> {
}
```

`_MultiWithSpec` 组合了三种能力：动态 CTE、静态 CTE、DELETE。所以 `MySQLs.batchMultiDelete()` 返回的对象可以直接调用 `.delete()`，也可以先用 `.with()` 定义 CTE。

### ① WITH (Common Table Expression)

> **语义约束**: `WITH` 关键字仅出现一次（即 `.with(name)` / `.withRecursive(name)` 只能调用一次）。后续 CTE 通过 `_CteComma.comma(String name)` 追加。`.space()` 是唯一退出 CTE 链、进入 DELETE 的路径。
> **禁止** 在 `.space()` 后再调用 `.with(name)`——类型系统不提供此路径。

**接口**: `_MySQLDynamicWithClause` + `_MySQLStaticWithClause`

**动态 WITH (运行时构建)**:

```java
MySQLs.batchMultiDelete()
    .with(builder -> {
        builder.comma("cte1").as(s -> s.select(...).from(...).asQuery());
        builder.comma("cte2").as(s -> s.select(...).from(...).asQuery());
    })
    .delete("c", "r")
    .from(...)
```

**条件 WITH**:

```java
MySQLs.batchMultiDelete()
    .ifWith(builder -> { ... })        // 仅当 consumer 内有实际操作时执行
    .ifWithRecursive(builder -> { ... }) // 递归条件 WITH
```

**静态 WITH**:

```java
MySQLs.batchMultiDelete()
    .with("cte_name")
    .parens(s -> s.select(...).from(...).asQuery())
    .as()
    .space()
    .delete(...)
```

### ② DELETE (核心，二选一方式)

```java
// MySQLDelete.java line 324-340
interface _MySQLMultiDeleteClause<I extends Item> extends Item {
    _MultiDeleteFromAliasClause<I> delete(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers);

    _MultiDeleteFromTableClause<I> delete(String alias);
    _MultiDeleteFromTableClause<I> delete(String alias1, String alias2);
    _MultiDeleteFromTableClause<I> delete(String alias1, String alias2, String alias3);
    _MultiDeleteFromTableClause<I> delete(String alias1, String alias2, String alias3, String alias4);
    _MultiDeleteFromTableClause<I> delete(List<String> aliasList);
    _MultiDeleteFromTableClause<I> delete(Consumer<Consumer<String>> consumer);
}
```

> **规则**:
> - **必须调用**，这是多表 DELETE 的起点
> - 两种方式：带修饰符和提示，或直接指定要删除的表别名
> - 指定的表别名必须在后续 FROM/USING/JOIN 中出现

#### 方式 A: 带修饰符和提示

```java
MySQLs.batchMultiDelete()
    .delete(hintSupplier, modifierList)
    .from("c", "r")  // 指定要删除的表别名
    .using(...)
```

**MySQL 支持的修饰符**（来自 MySQLs.java）:
- `MySQLs.LOW_PRIORITY`
- `MySQLs.QUICK`
- `MySQLs.IGNORE`

**MySQL 支持的提示创建方法**:
- `MySQLs.qbName(String name)` - 查询块名称
- `MySQLs.orderIndex(String qbName, String tableAlias, List<String> indexList)` - 顺序索引

#### 方式 B: 直接指定表别名

```java
MySQLs.batchMultiDelete()
    .delete("c", "r")  // 直接指定要删除的表别名
    .from(...)
```

**delete() 的重载形式**:

| 形式 | 说明 |
|-----|------|
| `delete(String alias)` | 删除单个表 |
| `delete(String alias1, String alias2)` | 删除两个表 |
| `delete(String alias1, String alias2, String alias3)` | 删除三个表 |
| `delete(String alias1, String alias2, String alias3, String alias4)` | 删除四个表 |
| `delete(List<String> aliasList)` | 删除多个表（List 形式） |
| `delete(Consumer<Consumer<String>> consumer)` | 动态构建要删除的表列表 |

### ③ FROM/SPACE (指定要删除的表别名)

> **仅当使用方式 A (带修饰符和提示) 时需要调用**

```java
// MySQLDelete.java line 286-312
interface _MultiDeleteFromAliasClause<I extends Item> {
    _SimpleMultiDeleteUsingClause<I> from(String alias);
    _SimpleMultiDeleteUsingClause<I> from(String alias1, String alias2);
    _SimpleMultiDeleteUsingClause<I> from(String alias1, String alias2, String alias3);
    _SimpleMultiDeleteUsingClause<I> from(String alias1, String alias2, String alias3, String alias4);
    _SimpleMultiDeleteUsingClause<I> from(List<String> aliasList);
    _SimpleMultiDeleteUsingClause<I> from(Consumer<Consumer<String>> consumer);

    _MultiDeleteFromTableClause<I> space(String alias);
    _MultiDeleteFromTableClause<I> space(String alias1, String alias2);
    _MultiDeleteFromTableClause<I> space(String alias1, String alias2, String alias3);
    _MultiDeleteFromTableClause<I> space(String alias1, String alias2, String alias3, String alias4);
    _MultiDeleteFromTableClause<I> space(List<String> aliasList);
    _MultiDeleteFromTableClause<I> space(Consumer<Consumer<String>> consumer);
}
```

**from() vs space()**:
- `from()` → 使用 `DELETE ... FROM ... USING ...` 语法
- `space()` → 使用 `DELETE ... FROM ... JOIN ...` 语法（不使用 USING 关键字）

### ④ FROM/USING (必须，指定表来源)

```java
// MySQLMultiDeletes.java line 150-189
// 两种语法，二选一:
_MultiPartitionJoinClause<I> from(TableMeta<?> table);
_MultiPartitionJoinClause<I> using(TableMeta<?> table);

// 嵌套 JOIN 形式:
_MultiJoinSpec<I> from(Function<_NestedLeftParenSpec<MySQLDelete._MultiJoinSpec<I>>, MySQLDelete._MultiJoinSpec<I>> function);
_MultiJoinSpec<I> using(Function<_NestedLeftParenSpec<MySQLDelete._MultiJoinSpec<I>>, MySQLDelete._MultiJoinSpec<I>> function);
```

> **规则**:
> - **必须调用**，指定第一个表
> - 两种语法：`FROM` 或 `USING`
> - 选择哪种语法取决于 SQL 风格偏好

**from() vs using() 的区别**:
- `using()` → 使用 `DELETE ... FROM ... USING ... JOIN ...` 语法
- `from()` → 使用 `DELETE ... FROM ... JOIN ...` 语法

### ⑤ PARTITION (可选，三种形式)

```java
// 三种形式：
_MultiIndexHintJoinSpec<I> partition(String first, String... rest);
_MultiIndexHintJoinSpec<I> partition(Consumer<Consumer<String>> consumer);
_MultiIndexHintJoinSpec<I> ifPartition(Consumer<Consumer<String>> consumer);
```

**三种形式说明**:

| 形式 | 说明 |
|-----|------|
| `partition(String first, String... rest)` | 直接指定一个或多个分区名 |
| `partition(Consumer<Consumer<String>> consumer)` | 使用 Consumer 动态构建分区列表 |
| `ifPartition(Consumer<Consumer<String>> consumer)` | 条件形式，仅当有实际操作时生效 |

### ⑥ AS (必须，指定表别名)

```java
// 必须在每个表后调用
_MultiIndexHintJoinSpec<I> as(String alias);
```

> **规则**:
> - **必须调用**，为每个表指定别名
> - 别名必须与 DELETE 子句中指定的别名对应
> - 别名在整个语句中必须唯一

### ⑦ INDEX HINT (可选，可重复)

```java
// MySQLMultiDeletes.java line 288-495
// 三大类索引提示:

// 1. USE INDEX
_MultiIndexHintJoinSpec<I> useIndex(String indexName);
_MultiIndexHintJoinSpec<I> useIndex(String indexName1, String indexName2);
_MultiIndexHintJoinSpec<I> useIndex(String indexName1, String indexName2, String indexName3);
_MultiIndexHintJoinSpec<I> useIndex(Consumer<Clause._StaticStringSpaceClause> consumer);
_MultiIndexHintJoinSpec<I> useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName);
_MultiIndexHintJoinSpec<I> useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2);
_MultiIndexHintJoinSpec<I> useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3);
_MultiIndexHintJoinSpec<I> useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer);
_MultiIndexHintJoinSpec<I> useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer);
_MultiIndexHintJoinSpec<I> ifUseIndex(Consumer<Consumer<String>> consumer);
_MultiIndexHintJoinSpec<I> ifUseIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer);

// 2. IGNORE INDEX
_MultiIndexHintJoinSpec<I> ignoreIndex(String indexName);
// ... (类似 useIndex 的所有重载)

// 3. FORCE INDEX
_MultiIndexHintJoinSpec<I> forceIndex(String indexName);
// ... (类似 useIndex 的所有重载)
```

**索引提示的目的**:
- `SQLs.WordFor.FOR` + `SQLs.IndexHintPurpose.JOIN` - 用于 JOIN
- `SQLs.WordFor.FOR` + `SQLs.IndexHintPurpose.ORDER_BY` - 用于 ORDER BY
- `SQLs.WordFor.FOR` + `SQLs.IndexHintPurpose.GROUP_BY` - 用于 GROUP BY

### ⑧ JOIN (可选，可重复)

```java
// MySQLMultiDeletes.java line 161-267
// 多种 JOIN 类型:

// 基本 JOIN:
_MultiPartitionOnClause<I> join(TableMeta<?> table);
_MultiPartitionOnClause<I> leftJoin(TableMeta<?> table);
_MultiPartitionOnClause<I> rightJoin(TableMeta<?> table);
_MultiPartitionOnClause<I> fullJoin(TableMeta<?> table);
_MultiPartitionOnClause<I> straightJoin(TableMeta<?> table);
_MultiIndexHintJoinSpec<I> crossJoin(TableMeta<?> table);

// 嵌套 JOIN:
_OnClause<MySQLDelete._MultiJoinSpec<I>> join(Function<_NestedLeftParenSpec<_OnClause<MySQLDelete._MultiJoinSpec<I>>>, _OnClause<MySQLDelete._MultiJoinSpec<I>>> function);
_OnClause<MySQLDelete._MultiJoinSpec<I>> leftJoin(Function<_NestedLeftParenSpec<_OnClause<MySQLDelete._MultiJoinSpec<I>>>, _OnClause<MySQLDelete._MultiJoinSpec<I>>> function);
// ... (其他 JOIN 类型的嵌套形式)

// 条件 JOIN:
_MultiJoinSpec<I> ifJoin(Consumer<MySQLJoins> consumer);
_MultiJoinSpec<I> ifLeftJoin(Consumer<MySQLJoins> consumer);
// ... (其他 JOIN 类型的条件形式)
```

**JOIN 内部流程**:
1. `join(TableMeta)` → `_MultiPartitionOnClause`
2. (可选) `partition(...)`
3. `as(String alias)` → `_MultiIndexHintOnSpec`
4. (可选) 索引提示 (`useIndex()`, `ignoreIndex()`, `forceIndex()`)
5. `on(...)` → 回到 `_MultiJoinSpec`

### ⑨ WHERE (可选)

```java
// Statement.java
WR where(Consumer<Consumer<IPredicate>> consumer);
WA where(IPredicate predicate);
<T> WA where(Function<T, IPredicate> expOperator, T operand);
WA whereIf(Supplier<IPredicate> supplier);
// ... (更多 whereIf 重载形式)
```

#### 返回值:
- `.where(IPredicate)` / `.where(Function, operand)` → `_MultiWhereAndSpec` (可继续 `.and(...)`)
- `.where(Consumer)` → `_DmlDeleteSpec` (直接跳转到 asDelete，不能再 AND)

### ⑩ AND (可选，可重复)

```java
WA and(IPredicate predicate);
<T> WA and(Function<T, IPredicate> expOperator, T operand);
WA ifAnd(Supplier<IPredicate> supplier);
// ... (更多 ifAnd 重载方法)
```

### ⑪ 结束语句: asDelete()

```java
// Statement.java line 1421-1424
interface _DmlDeleteSpec<I extends Item> extends Item {
    I asDelete();
}
```

- **返回类型**: `_BatchDeleteParamSpec`
- **不可再次调用** — 调用后语句已标记为 prepared，再次操作会抛异常

### ⑫ 设置批量参数: namedParamList()

```java
// MySQLMultiDeletes.java line 724-730
public BatchDelete namedParamList(final List<?> paramList) {
    if (this.paramList != null) {
        throw ContextStack.clearStackAnd(_Exceptions::castCriteriaApi);
    }
    this.paramList = CriteriaUtils.paramList(paramList);
    return this;
}
```

- **参数**: `List<?> paramList` — 批量参数列表，每个元素可以是 `Map<String, Object>` 或包含 getter 方法的 POJO
- **返回类型**: `BatchDelete` — 最终可执行的批量删除语句
- **注意**: 只能调用一次

## 完整示例

### 示例 1: 基础批量多表删除

```java
import io.army.criteria.*;
import io.army.criteria.impl.MySQLs;
import static io.army.criteria.impl.SQLs.*;

public void basicBatchMultiDelete(SyncLocalSession session, List<Map<String, Object>> params) {
    BatchDelete stmt = MySQLs.batchMultiDelete()
        .delete("c", "r")
        .from(ChinaCity_.T)
        .as("c")
        .join(ChinaRegion_.T)
        .as("r")
        .on(ChinaCity_.regionId::equal, ChinaRegion_.id)
        .where(ChinaRegion_.id::equal, SQLs::namedParam, "regionId")
        .asDelete()
        .namedParamList(params);
    
    List<Long> rowList = session.batchDelete(stmt);
    System.out.println("Deleted rows per batch: " + rowList);
}
```

### 示例 2: 带修饰符和提示的批量多表删除

```java
public void batchMultiDeleteWithHints(SyncLocalSession session, List<Map<String, Object>> params) {
    final Supplier<List<Hint>> hintSupplier = () -> {
        final List<Hint> hintList = new ArrayList<>(2);
        hintList.add(MySQLs.qbName("region_delete"));
        hintList.add(MySQLs.orderIndex("region_delete", "r", Collections.singletonList("PRIMARY")));
        return hintList;
    };

    final List<MySQLs.Modifier> modifierList = Arrays.asList(
        MySQLs.LOW_PRIORITY, 
        MySQLs.QUICK, 
        MySQLs.IGNORE
    );

    BatchDelete stmt = MySQLs.batchMultiDelete()
        .delete(hintSupplier, modifierList)
        .from("c", "r", "u")
        .using(ChinaCity_.T)
        .partition("p1")
        .as("c")
        .join(ChinaRegion_.T)
        .partition("p1")
        .as("r")
        .on(ChinaCity_.regionId::equal, ChinaRegion_.id)
        .join(BankUser_.T)
        .as("u")
        .on(BankUser_.cityId::equal, ChinaCity_.id)
        .where(ChinaRegion_.createTime::between, SQLs::namedParam, "startTime", AND, SQLs::namedParam, "endTime")
        .and(ChinaRegion_.updateTime::between, SQLs::namedParam, "startTime", AND, SQLs::namedParam, "endTime")
        .ifAnd(() -> shouldFilterVersion ? ChinaRegion_.version::equal, SQLs::namedParam, "version" : null)
        .asDelete()
        .namedParamList(params);
    
    session.batchDelete(stmt);
}
```

### 示例 3: 带 CTE 的批量多表删除

```java
public void batchMultiDeleteWithCte(SyncLocalSession session, List<Map<String, Object>> params) {
    BatchDelete stmt = MySQLs.batchMultiDelete()
        .with("cte")
        .parens(s -> s
            .select(ChinaRegion_.id)
            .from(ChinaRegion_.T)
            .where(ChinaRegion_.regionType::equal, SQLs::namedParam, "regionType")
            .asQuery())
        .as()
        .space()
        .delete("c")
        .from(ChinaCity_.T)
        .as("c")
        .join("cte")
        .on(ChinaCity_.regionId::equal, SQLs.refField("cte", ChinaRegion_.id))
        .where(ChinaCity_.createTime::greaterOrEqual, SQLs::namedParam, "startDate")
        .asDelete()
        .namedParamList(params);
    
    session.batchDelete(stmt);
}
```

### 示例 4: 带索引提示的批量多表删除

```java
public void batchMultiDeleteWithIndexHints(SyncLocalSession session, List<Map<String, Object>> params) {
    BatchDelete stmt = MySQLs.batchMultiDelete()
        .delete("u", "a")
        .from(BankUser_.T)
        .as("u")
        .useIndex(FOR, JOIN, "idx_user_id")
        .join(BankAccount_.T)
        .as("a")
        .ignoreIndex(FOR, JOIN, "idx_account_id")
        .on(BankUser_.id::equal, BankAccount_.userId)
        .where(BankUser_.status::equal, SQLs::namedParam, "status")
        .and(BankAccount_.balance::less, SQLs::namedParam, "minBalance")
        .asDelete()
        .namedParamList(params);
    
    session.batchDelete(stmt);
}
```

### 示例 5: 使用 USING 语法的批量多表删除

```java
public void batchMultiDeleteWithUsing(SyncLocalSession session, List<Map<String, Object>> params) {
    BatchDelete stmt = MySQLs.batchMultiDelete()
        .delete("o", "i")
        .using(Order_.T)
        .as("o")
        .leftJoin(OrderItem_.T)
        .as("i")
        .on(Order_.id::equal, OrderItem_.orderId)
        .where(Order_.status::equal, SQLs::namedParam, "orderStatus")
        .and(Order_.createTime::less, SQLs::namedParam, "cutoffDate")
        .asDelete()
        .namedParamList(params);
    
    session.batchDelete(stmt);
}
```

## 两种主要语法对比

### 语法 1: DELETE ... FROM ... USING ...

```java
MySQLs.batchMultiDelete()
    .delete(hintSupplier, modifierList)
    .from("c", "r")  // 指定要删除的表别名
    .using(ChinaCity_.T)  // 使用 USING 关键字
    .as("c")
    .join(ChinaRegion_.T)
    .as("r")
    .on(...)
```

**生成的 SQL**:
```sql
DELETE /*+ ... */ c, r 
USING china_city AS c 
JOIN china_region AS r ON ...
```

### 语法 2: DELETE ... FROM ... JOIN ...

```java
MySQLs.batchMultiDelete()
    .delete("c", "r")
    .from(ChinaCity_.T)  // 不使用 USING 关键字
    .as("c")
    .join(ChinaRegion_.T)
    .as("r")
    .on(...)
```

**生成的 SQL**:
```sql
DELETE c, r 
FROM china_city AS c 
JOIN china_region AS r ON ...
```

## 实现类继承链

```
SQLSyntax (MySQLs extends)
    └─ MySQLs
        └─ .batchMultiDelete() → MySQLMultiDeletes.batch()
            └─ MySQLBatchDelete
                └─ MySQLMultiDeletes<_BatchDeleteParamSpec>
                    └─ JoinableDelete
                        └─ WhereClause
                            └─ ArmyStmtSpec
```

## 关键约束和规则

### 层级约束 (通过接口链强制执行)

接口名称本身就编码了当前上下文中可用的方法，DSL 通过 **返回类型引导** 下一个可调用的方法：

1. **WITH → DELETE**: 调用 `with()` 后只能进入 `_MySQLMultiDeleteClause`，即只能调用 `delete()`
2. **DELETE → FROM/USING**: 调用 `delete()` 后进入 `_MultiDeleteFromTableClause` 或 `_SimpleMultiDeleteUsingClause`，只能调用 `from()` 或 `using()`
3. **FROM/USING → PARTITION/AS**: 调用 `from()`/`using()` 后进入 `_MultiPartitionJoinClause`，可调用 `partition()` 或直接 `as()`
4. **PARTITION → AS**: 调用 `partition()` 后只能调用 `as()`
5. **AS → INDEX HINT/JOIN/WHERE**: 调用 `as()` 后进入 `_MultiIndexHintJoinSpec`，可调用索引提示、`join()` 或直接 `where()`
6. **JOIN → PARTITION/AS/ON**: 调用 `join()` 后进入 `_MultiPartitionOnClause`，可调用 `partition()`、`as()`，最终需要 `on()`
7. **WHERE → AND/asDelete**: 调用 `where(IPredicate)` 后进入 `_MultiWhereAndSpec`，可继续 `and()`，或直接 `asDelete()`
8. **AND → AND/asDelete**: 调用 `and()` 后仍在 `_MultiWhereAndSpec`，可继续 `and()`，或直接 `asDelete()`
9. **asDelete() → namedParamList()**: 调用 `asDelete()` 后进入 `_BatchDeleteParamSpec`，必须调用 `namedParamList()` 获得最终的 `BatchDelete`

### 方法调用限制

| 方法/阶段 | 是否必须 | 可重复调用 | 说明 |
|----------|---------|-----------|------|
| `MySQLs.batchMultiDelete()` | 是 | 否 | 入口，只能调用一次（每次调用创建新语句） |
| `with()` / `withRecursive()` | 否 | 否 | WITH 关键字只能出现一次，多个 CTE 用 comma 追加 |
| `delete()` | 是 | 否 | 必须且只能调用一次，指定要删除的表 |
| `from()` / `space()` (在 delete(hints, modifiers) 后) | 是 | 否 | 必须且只能调用一次 |
| `from()` / `using()` (主表) | 是 | 否 | 必须且只能调用一次 |
| `partition()` 系列 | 否 | 否 | 每个表只能调用一次 |
| `as()` (表别名) | 是 | 否 | 每个表必须且只能调用一次 |
| 索引提示 (`useIndex()` 等) | 否 | 是 | 每个表可以多次调用 |
| `join()` 系列 | 否 | 是 | 可以多次 JOIN 多个表 |
| `where()` | 否 | 否 | 只能调用一次（但可通过 Consumer 组合多条件） |
| `and()` | 否 | 是 | WHERE 后可以多次调用 AND |
| `asDelete()` | 是 | 否 | 必须调用一次，结束构建 |
| `namedParamList()` | 是 | 否 | 必须调用一次，设置批量参数 |

### 别名一致性规则

- DELETE 子句中指定的表别名必须在 FROM/USING/JOIN 子句中出现
- 所有表别名必须唯一
- 表别名不能为 null 或空字符串

### 批量参数规则

- 参数列表中的每个元素必须与 SQL 中的命名参数匹配
- 可以使用 `Map<String, Object>` 或包含 getter 方法的 POJO
- 命名参数使用 `SQLs::namedParam` 或 `SQLs.namedParam()` 定义

### 两种语法的选择

- **USING 语法**: 使用 `delete(hints, modifiers).from(...).using(...)` 风格
- **FROM 语法**: 使用 `delete(...).from(...)` 风格（不使用 USING 关键字）
- 两种语法功能等价，选择取决于个人偏好

## 与其他 DELETE 入口的区别

| 入口 | 返回类型 | 场景 |
|-----|---------|------|
| `MySQLs.multiDelete()` | `MySQLDelete._MultiWithSpec<Delete>` | MySQL 多表 DELETE（单次） |
| `MySQLs.batchMultiDelete()` | `MySQLDelete._MultiWithSpec<_BatchDeleteParamSpec>` | MySQL 批量多表 DELETE |
| `MySQLs.singleDelete()` | `MySQLDelete._SingleWithSpec<Delete>` | MySQL 单表 DELETE |
| `MySQLs.batchSingleDelete()` | `MySQLDelete._SingleWithSpec<_BatchDeleteParamSpec>` | MySQL 批量单表 DELETE |
| `SQLs.multiDelete()` | `_WithSpec<Delete>` | 标准 SQL 多表 DELETE |

## 相关接口参考

| 接口 | 说明 |
|------|------|
| `MySQLDelete._MultiWithSpec` | 起始接口，支持 WITH 子句 |
| `MySQLDelete._MySQLMultiDeleteClause` | 支持 DELETE 子句 |
| `MySQLDelete._MultiDeleteFromAliasClause` | 支持 FROM/SPACE 指定删除表 |
| `MySQLDelete._SimpleMultiDeleteUsingClause` | 支持 USING 语法 |
| `MySQLDelete._MultiDeleteFromTableClause` | 支持 FROM 子句 |
| `MySQLDelete._MultiPartitionJoinClause` | 支持 PARTITION 子句（主表） |
| `MySQLDelete._MultiIndexHintJoinSpec` | 支持索引提示和 JOIN |
| `MySQLDelete._MultiPartitionOnClause` | 支持 PARTITION 子句（JOIN 表） |
| `MySQLDelete._MultiIndexHintOnSpec` | 支持索引提示和 ON |
| `MySQLDelete._MultiJoinSpec` | 支持多种 JOIN 操作 |
| `MySQLDelete._MultiWhereClause` | 支持 WHERE 子句 |
| `MySQLDelete._MultiWhereAndSpec` | 支持 AND 子句 |
| `Statement._DmlDeleteSpec` | DML 删除规范 |
| `Statement._BatchDeleteParamSpec` | 批量参数接口 |
| `BatchDelete` | 最终批量删除语句 |

## 自我进化指南

当 Army Criteria API 更新时，请按以下步骤更新本文档：

1. **检查接口变更**: 重新读取 `MySQLDelete.java`、`Statement.java`
2. **检查实现变更**: 重新读取 `MySQLMultiDeletes.java`、`JoinableDelete.java`
3. **检查测试更新**: 查看相关测试文件的新增测试用例
4. **更新 Diagram**: 如有新方法/新子句，补充到 Diagram 中
5. **更新示例**: 补充新增用法的示例
6. **更新约束**: 如有新的语义约束，补充到规则部分
7. **检查相关文件**: 查看 `MySQLs.java` 等文件的变更
