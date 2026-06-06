---
name: MySQLs.singleDelete() 方法链
description: 完整的 Army Criteria API MySQLs.singleDelete() 方法链知识。用于理解、解释或记录从 MySQLs.singleDelete() 到 asDelete() 的完整 DELETE 语句构建流程，包括 MySQL 特有的修饰符、提示、分区、ORDER BY、LIMIT 等语法。
---

# MySQLs.singleDelete() 方法链 — 完整参考

> **适用范围**: 本技能 **仅限** 用于理解、解释、记录 `MySQLs.singleDelete()` 入口的 MySQL 特定 DELETE 语句构建流程。覆盖从 `MySQLs.singleDelete()` 到 `asDelete()` 的完整方法链，包括 WITH、DELETE、FROM、PARTITION、WHERE、AND、ORDER BY、LIMIT 等阶段。
> **不** 涵盖 batchSingleDelete、multiDelete、标准 SQLs.singleDelete。

> **源码依据**: 本技能基于以下核心源文件编写——`MySQLs.java`（入口定义）、`MySQLDelete.java`（MySQL DELETE 接口组合）、`MySQLSingleDeletes.java`（实现）、`MySQLCriteriaUnitTests.java`（示例）。所有描述均以实际源码接口签名和实现为准。

## 核心入口

```java
// MySQLs.java line 183-185
public static MySQLDelete._SingleWithSpec<Delete> singleDelete() {
    return MySQLSingleDeletes.simple();
}
```

**返回类型**: `MySQLDelete._SingleWithSpec<Delete>` — 实现类是 `MySQLSingleDeletes.MySQLSimpleDelete`。

**内部实现**: `MySQLSingleDeletes.simple()`:

```java
// MySQLSingleDeletes.java line 67-69
static _SingleWithSpec<Delete> simple() {
    return new MySQLSimpleDelete();
}
```

---

## 完整方法链 Diagram

> **阅读指南**: 每个叶子节点代表一个可直接调用的方法，带完整参数列表。接口链通过返回类型导航。

```
MySQLs.singleDelete() → MySQLDelete._SingleWithSpec<Delete>
│
├─① WITH (可选，仅一次声明；comma 追加 CTE 可重复)
│  ├─ 静态 WITH:
│  │  ├─ .with(String name)       → MySQLQuery._StaticCteParensSpec
│  │  └─ .withRecursive(String name) → MySQLQuery._StaticCteParensSpec
│  │     └─ (然后: .parens() → .as() → .space() 进入 DELETE)
│  │
│  └─ 动态 WITH:
│     ├─ .with(Consumer<MySQLCtes> consumer)       → MySQLDelete._SimpleSingleDeleteClause
│     ├─ .withRecursive(Consumer<MySQLCtes> consumer) → MySQLDelete._SimpleSingleDeleteClause
│     ├─ .ifWith(Consumer<MySQLCtes> consumer)      → MySQLDelete._SimpleSingleDeleteClause
│     └─ .ifWithRecursive(Consumer<MySQLCtes> consumer) → MySQLDelete._SimpleSingleDeleteClause
│
└─② DELETE (可选，带修饰符和提示)
   └─ .delete(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers)
         → DeleteStatement._SingleDeleteFromClause
      │
      └─③ FROM (必须，仅一次，不可重复)
         ├─ .deleteFrom(SingleTableMeta<?> table, SQLs.WordAs as, String tableAlias) 
         │     → MySQLDelete._SinglePartitionSpec
         └─ .from(SingleTableMeta<?> table, SQLs.WordAs as, String tableAlias) 
               → MySQLDelete._SinglePartitionSpec
            │
            └─④ PARTITION (可选，三种形式)
               ├─ .partition(String first, String... rest)
               ├─ .partition(Consumer<Consumer<String>> consumer)
               └─ .ifPartition(Consumer<Consumer<String>> consumer)
                  → 都返回 MySQLDelete._SingleWhereClause
                  │
                  └─⑤ WHERE (可选，多种形式)
                     ├─ .where(IPredicate predicate)                  → MySQLDelete._SingleWhereAndSpec
                     ├─ .where(Function<T, IPredicate> expOperator, T operand) → MySQLDelete._SingleWhereAndSpec
                     ├─ .whereIf(Supplier<IPredicate> supplier)          → MySQLDelete._SingleWhereAndSpec
                     ├─ .where(Consumer<Consumer<IPredicate>> consumer) → MySQLDelete._OrderBySpec (跳过 AND/ORDER BY)
                     ├─ (更多 whereIf 重载形式)
                     │
                     └─⑥ AND (可选，可重复)
                        ├─ .and(IPredicate predicate)                  → MySQLDelete._SingleWhereAndSpec
                        ├─ .and(Function<T, IPredicate> expOperator, T operand) → MySQLDelete._SingleWhereAndSpec
                        ├─ .ifAnd(Supplier<IPredicate> supplier)          → MySQLDelete._SingleWhereAndSpec
                        └─ (更多 ifAnd 重载形式)
                        │
                        └─⑦ ORDER BY (可选，可重复排序)
                           ├─ .orderBy(SortItem sortItem)                  → MySQLDelete._OrderByCommaSpec
                           ├─ .orderBy(SortItem sortItem1, SortItem sortItem2) → MySQLDelete._OrderByCommaSpec
                           └─ .comma(SortItem sortItem)
                              │
                              └─⑧ LIMIT (可选)
                                 ├─ .limit(long count)
                                 ├─ .limit(Function<Number, Expression> valueOperator, Number count)
                                 ├─ .ifLimit(Supplier<Number> supplier)
                                 └─ (更多 ifLimit 重载形式)
                                    │
                                    └─⑨ 结束: .asDelete() → Delete
```

---

## 逐层接口详解

### 0. 入口: `MySQLDelete._SingleWithSpec<Delete>`

```java
// MySQLDelete.java line 157-162
interface _SingleWithSpec<I extends Item>
        extends _MySQLDynamicWithClause<_SimpleSingleDeleteClause<I>>,
                _MySQLStaticWithClause<_SimpleSingleDeleteClause<I>>,
                _SimpleSingleDeleteClause<I> {
}
```

`_SingleWithSpec` 组合了三种能力：动态 CTE、静态 CTE、DELETE。所以 `MySQLs.singleDelete()` 返回的对象可以直接调用 `.delete()` 或 `.deleteFrom()`，也可以先用 `.with()` 定义 CTE。

---

### ① WITH (Common Table Expression)

> **语义约束**: `WITH` 关键字仅出现一次（即 `.with(name)` / `.withRecursive(name)` 只能调用一次）。后续 CTE 通过 `_CteComma.comma(String name)` 追加。`.space()` 是唯一退出 CTE 链、进入 DELETE 的路径。
> **禁止** 在 `.space()` 后再调用 `.with(name)`——类型系统不提供此路径。

**接口**: `_MySQLDynamicWithClause` + `_MySQLStaticWithClause`

**动态 CTE (运行时构建)**:

```java
MySQLs.singleDelete()
    .with(builder -> {
        builder.comma("cte1").as(s -> s.select(...).from(...).asQuery());
        builder.comma("cte2").as(s -> s.select(...).from(...).asQuery());
    })
    .deleteFrom(...)
```

**条件 CTE**:

```java
MySQLs.singleDelete()
    .ifWith(builder -> { ... })        // 仅当 consumer 内有实际操作时执行
    .ifWithRecursive(builder -> { ... }) // 递归条件 CTE
```

**静态 CTE**:

```java
MySQLs.singleDelete()
    .with("cte_name")
    .parens(s -> s.select(...).from(...).asQuery())
    .as()
    .space()
    .deleteFrom(...)
```

---

### ② DELETE (可选，带修饰符和提示)

```java
// MySQLDelete.java line 46-51
interface _SingleDeleteClause<T> extends DeleteStatement._SingleDeleteClause<T> {
    DeleteStatement._SingleDeleteFromClause<T> delete(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers);
}
```

> **规则**:
> - 可选调用，如果不调用则直接调用 `.deleteFrom()`
> - 可以传入提示（hint）和修饰符（modifier）
> - 调用后必须继续调用 `.from()` 或 `.deleteFrom()`

**MySQL 支持的修饰符**（来自 MySQLs.java）:
- `MySQLs.LOW_PRIORITY`
- `MySQLs.QUICK`
- `MySQLs.IGNORE`

**MySQL 支持的提示创建方法**:
- `MySQLs.qbName(String name)` - 查询块名称
- `MySQLs.orderIndex(String qbName, String tableAlias, List<String> indexList)` - 顺序索引

---

### ③ FROM (核心)

```java
// MySQLSingleDeletes.java line 103-129
// 两种形式：
_SinglePartitionSpec<I> deleteFrom(@Nullable SingleTableMeta<?> table, SQLs.WordAs as, @Nullable String alias);
_SinglePartitionSpec<I> from(SingleTableMeta<?> table, SQLs.WordAs as, String alias);
```

> **规则**:
> - **必须调用一次，且仅能调用一次**
> - 仅接受 `SingleTableMeta`
> - `SQLs.WordAs` 固定传入 `SQLs.AS`
> - `tableAlias` 必须提供非空字符串
> - 不可重复调用——类型系统和实现都强制执行此约束

**使用示例** (来自测试):

```java
// MySQLCriteriaUnitTests.java line 149-161
final Delete stmt;
stmt = MySQLs.singleDelete()
        .delete(hintSupplier, Arrays.asList(MySQLs.LOW_PRIORITY, MySQLs.QUICK, MySQLs.IGNORE))
        .from(ChinaRegion_.T, SQLs.AS, "r")  // 必须调用
        .partition("p1")
        .where(...)
        .asDelete();
```

---

### ④ PARTITION (可选，三种形式)

```java
// MySQLSingleDeletes.java line 132-147
// 三种形式：
_SingleWhereClause<I> partition(String first, String... rest);
_SingleWhereClause<I> partition(Consumer<Consumer<String>> consumer);
_SingleWhereClause<I> ifPartition(Consumer<Consumer<String>> consumer);
```

**三种形式说明**:

| 形式 | 说明 |
|-----|------|
| `partition(String first, String... rest)` | 直接指定一个或多个分区名 |
| `partition(Consumer<Consumer<String>> consumer)` | 使用 Consumer 动态构建分区列表 |
| `ifPartition(Consumer<Consumer<String>> consumer)` | 条件形式，仅当有实际操作时生效 |

**使用场景**: 
- 当表使用分区时，指定要删除的分区可以提高性能
- 避免全表扫描，直接在指定分区内删除

---

### ⑤ WHERE (可选)

#### 基本 WHERE 方法

```java
// Statement.java
WR where(Consumer<Consumer<IPredicate>> consumer);
WA where(IPredicate predicate);
<T> WA where(Function<T, IPredicate> expOperator, T operand);
WA whereIf(Supplier<IPredicate> supplier);
<T> WA whereIf(Function<T, IPredicate> expOperator, @Nullable T value);
<T> WA whereIf(ExpressionOperator<TypedExpression, T, IPredicate> expOp,
               BiFunction<TypedExpression, T, Expression> operator,
               @Nullable T value);
<T> WA whereIf(BiFunction<SQLs.BiOperator, T, IPredicate> expOp,
               SQLs.BiOperator operator,
               @Nullable T value);
<T> WA whereIf(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOp,
               SQLs.BiOperator op,
               BiFunction<TypedExpression, T, Expression> func,
               @Nullable T value);
<T> WA whereIf(BetweenValueOperator<T> expOp,
               BiFunction<TypedExpression, T, Expression> operator,
               @Nullable T value1, SQLs.WordAnd and,
               @Nullable T value2);
<T, U> WA whereIf(BetweenDualOperator<T, U> expOp,
                  BiFunction<TypedExpression, T, Expression> firstFuncRef,
                  @Nullable T first, SQLs.WordAnd and,
                  BiFunction<TypedExpression, U, Expression> secondFuncRef,
                  @Nullable U second);
```

#### 返回值:
- `.where(IPredicate)` / `.where(Function, operand)` → `_SingleWhereAndSpec` (可继续 `.and(...)`)
- `.where(Consumer)` → `_OrderBySpec` (直接跳转到 ORDER BY/LIMIT/asDelete，不能再 AND)

---

### ⑥ AND (可选，可重复)

```java
WA and(IPredicate predicate);
<T> WA and(Function<T, IPredicate> expOperator, T operand);
WA ifAnd(Supplier<IPredicate> supplier);
<T> WA ifAnd(Function<T, IPredicate> expOperator, @Nullable T value);
<T> WA ifAnd(ExpressionOperator<TypedExpression, T, IPredicate> expOp,
             BiFunction<TypedExpression, T, Expression> operator,
             @Nullable T value);
// ... 更多 ifAnd 重载方法
```

---

### ⑦ ORDER BY (可选，可重复排序)

```java
// 静态 ORDER BY:
_OrderByCommaSpec<I> orderBy(SortItem sortItem);
_OrderByCommaSpec<I> orderBy(SortItem sortItem1, SortItem sortItem2);
_OrderByCommaSpec<I> orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3);

// COMMA 追加排序:
_OrderByCommaSpec<I> comma(SortItem sortItem);
_OrderByCommaSpec<I> comma(SortItem sortItem1, SortItem sortItem2);
// ... 更多 comma 重载
```

**使用场景**: 
- 配合 LIMIT 使用，删除特定顺序的前 N 行
- 确保删除操作的确定性

---

### ⑧ LIMIT (可选)

```java
// 静态 LIMIT:
_LimitSpec<I> limit(long count);
<T extends Number> _LimitSpec<I> limit(Function<T, Expression> valueOperator, T count);

// 条件 LIMIT:
_LimitSpec<I> ifLimit(Supplier<Number> supplier);
<T extends Number> _LimitSpec<I> ifLimit(Function<T, Expression> valueOperator, Supplier<T> supplier);
_LimitSpec<I> ifLimit(Function<Object, Expression> valueOperator, Function<String, ?> function, String keyName);
```

**使用场景**: 
- 限制删除的行数，避免误删大量数据
- 分批删除大数据量

---

### ⑨ 结束语句

```java
// Statement.java line 1421-1424
interface _DmlDeleteSpec<I extends Item> extends Item {
    I asDelete();
}
```

- **返回类型**: `Delete` — 可传给 `session.delete(delete)` 或 `session.execute(delete)`
- **不可再次调用** — 调用后语句已标记为 prepared，再次操作会抛异常

---

## 完整示例

### 示例 1: 简单单表 DELETE

```java
final Delete stmt = MySQLs.singleDelete()
        .deleteFrom(User_.T, SQLs.AS, "u")
        .where(User_.id.equal(SQLs::literal, 1))
        .asDelete();
// 结果: DELETE FROM user AS u WHERE id = 1
```

### 示例 2: 带修饰符和提示的 DELETE

```java
// MySQLCriteriaUnitTests.java line 133-161
final Map<String, Object> map = _Collections.hashMap();
final LocalDateTime now = LocalDateTime.now();
map.put("startTime", now.minusDays(15));
map.put("endTime", now.plusDays(6));
map.put("version", "0");

final Supplier<List<Hint>> hintSupplier = () -> {
    final List<Hint> hintList = new ArrayList<>(2);
    hintList.add(MySQLs.qbName("regionDelete"));
    hintList.add(MySQLs.orderIndex("regionDelete", "r", Collections.singletonList("PRIMARY")));
    return hintList;
};
final Delete stmt;
stmt = MySQLs.singleDelete()
        .delete(hintSupplier, Arrays.asList(MySQLs.LOW_PRIORITY, MySQLs.QUICK, MySQLs.IGNORE))
        .from(ChinaRegion_.T, SQLs.AS, "r")
        .partition("p1")
        .where(ChinaRegion_.createTime.between(SQLs::literal, map.get("startTime"), AND, map.get("endTime")))
        .and(ChinaRegion_.updateTime.between(SQLs::param, map.get("startTime"), AND, map.get("endTime")))
        .ifAnd(ChinaRegion_.version::equal, SQLs::literal, map.get("version"))
        .orderBy(ChinaRegion_.name::desc, ChinaRegion_.id)
        .ifLimit(map.get("rowCount"))
        .asDelete();
```

### 示例 3: 带 PARTITION 的 DELETE

```java
final Delete stmt = MySQLs.singleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
        .partition("p1", "p2")
        .where(ChinaRegion_.id.greater(SQLs::literal, 100))
        .asDelete();
```

### 示例 4: 带 ORDER BY 和 LIMIT 的 DELETE

```java
final Delete stmt = MySQLs.singleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
        .where(ChinaRegion_.regionType.equal(SQLs::literal, RegionType.CITY))
        .orderBy(ChinaRegion_.population::desc)
        .limit(10)
        .asDelete();
```

### 示例 5: 条件 WHERE/AND

```java
final Delete stmt = MySQLs.singleDelete()
        .deleteFrom(User_.T, SQLs.AS, "u")
        .whereIf(() -> shouldFilterId ? User_.id.equal(id) : null)
        .ifAnd(() -> shouldFilterName ? User_.name.equal(name) : null)
        .asDelete();
```

### 示例 6: Consumer 风格的 WHERE

```java
final Delete stmt = MySQLs.singleDelete()
        .deleteFrom(User_.T, SQLs.AS, "u")
        .where(where -> {
            where.accept(User_.id.greater(SQLs::literal, 100));
            where.accept(User_.status.equal(SQLs::literal, 0));
        })
        .asDelete();
```

---

## 实现类继承链

```
SQLSyntax (MySQLs extends)
    └─ MySQLs
        └─ .singleDelete() → MySQLSingleDeletes.simple()
            └─ MySQLSimpleDelete
                └─ MySQLSingleDeletes<I>
                    └─ SingleDeleteStatement
                        └─ WhereClause
                            └─ ArmyStmtSpec
```

---

## 关键约束和规则

### 层级约束 (通过接口链强制执行)

接口名称本身就编码了当前上下文中可用的方法，DSL 通过**返回类型引导**下一个可调用的方法：

1. **WITH → DELETE/FROM**: 调用 `with()` 后只能进入 `_SimpleSingleDeleteClause`，即只能调用 `delete()` 或 `deleteFrom()`
2. **DELETE → FROM**: 调用 `delete()` 后进入 `_SingleDeleteFromClause`，只能调用 `from()` 或 `deleteFrom()`
3. **FROM → PARTITION/WHERE**: 调用 `deleteFrom()` 后进入 `_SinglePartitionSpec`，可调用 `partition()` 或直接 `where()`
4. **PARTITION → WHERE**: 调用 `partition()` 后进入 `_SingleWhereClause`，只能调用 `where()`
5. **WHERE → AND/ORDER BY**: 调用 `where(IPredicate)` 后进入 `_SingleWhereAndSpec`，可继续 `and()`，或直接 `orderBy()`/`limit()`/`asDelete()`
6. **AND → ORDER BY**: 调用 `and()` 后仍在 `_SingleWhereAndSpec`，可继续 `and()`，或直接 `orderBy()`/`limit()`/`asDelete()`
7. **ORDER BY → LIMIT**: 调用 `orderBy()` 后进入 `_OrderByCommaSpec`，可继续 `comma()`，或直接 `limit()`/`asDelete()`
8. **LIMIT → asDelete**: 调用 `limit()` 后进入 `_LimitSpec`，只能调用 `asDelete()`
9. **最终 → asDelete**: 最终必须调用 `asDelete()` 来结束构建并获得可执行的 `Delete` 对象

### 方法调用限制

| 方法/阶段 | 是否必须 | 可重复调用 | 说明 |
|----------|---------|-----------|------|
| `MySQLs.singleDelete()` | 是 | 否 | 入口，只能调用一次（每次调用创建新语句） |
| `with()` / `withRecursive()` | 否 | 否 | WITH 关键字只能出现一次，多个 CTE 用 comma 追加 |
| `delete()` (带修饰符) | 否 | 否 | 可选调用，带修饰符和提示 |
| `deleteFrom()` / `from()` | 是 | 否 | 必须且只能调用一次 |
| `partition()` 系列 | 否 | 否 | 只能调用一次 |
| `where()` | 否 | 否 | 只能调用一次（但可通过 Consumer 组合多条件） |
| `and()` | 否 | 是 | WHERE 后可以多次调用 AND |
| `orderBy()` / `comma()` | 否 | 是 | 可以多次调用，添加多个排序条件 |
| `limit()` 系列 | 否 | 否 | 只能调用一次 |
| `asDelete()` | 是 | 否 | 必须调用一次，结束构建 |

### null 检查

- `tableAlias` 不可为 null
- `table` 不可为 null
- CTE 的名称不可为 null
- 条件形式的 `ifWhere` / `ifAnd` 的 value 可为 null (null 时忽略该条件)

### 类型安全

- 返回类型的链式设计确保：不会在错误的阶段调用错误的方法
- `deleteFrom` 只接受 `SingleTableMeta`，不接受其他类型

---

## 与其他 DELETE 入口的区别

| 入口 | 返回类型 | 场景 |
|-----|---------|------|
| `MySQLs.singleDelete()` | `MySQLDelete._SingleWithSpec<Delete>` | MySQL 单表 DELETE，本技能覆盖 |
| `MySQLs.batchSingleDelete()` | `MySQLDelete._SingleWithSpec<_BatchDeleteParamSpec>` | MySQL 批量单表 DELETE |
| `MySQLs.multiDelete()` | `MySQLDelete._MultiWithSpec<Delete>` | MySQL 多表 DELETE |
| `SQLs.singleDelete()` | `_WithSpec<Delete>` | 标准 SQL 单表 DELETE |

---

## 相关接口参考

| 接口 | 说明 |
|------|------|
| `MySQLDelete._SingleWithSpec` | 起始接口，支持 WITH 子句 |
| `MySQLDelete._SimpleSingleDeleteClause` | 支持 DELETE 子句 |
| `DeleteStatement._SingleDeleteFromClause` | 支持 FROM 子句 |
| `MySQLDelete._SinglePartitionSpec` | 支持 PARTITION 子句 |
| `MySQLDelete._SingleWhereClause` | 支持 WHERE 子句 |
| `MySQLDelete._SingleWhereAndSpec` | 支持 AND 子句 |
| `MySQLDelete._OrderBySpec` | 支持 ORDER BY 子句 |
| `MySQLDelete._OrderByCommaSpec` | 支持 ORDER BY 后追加排序 |
| `MySQLDelete._LimitSpec` | 支持 LIMIT 子句 |
| `Delete` | 标准删除语句 |

---

## singleDelete vs query 对比分析

### 相同点

| 特性 | singleDelete | query |
|------|--------------|-------|
| WITH (CTE) | ✅ 支持 | ✅ 支持 |
| WHERE/AND | ✅ 支持 | ✅ 支持 |
| Partition | ✅ 支持 | ✅ 支持 |
| Hint | ✅ 支持 | ✅ 支持 |

### 核心差异

| 特性 | singleDelete | query |
|------|--------------|-------|
| **操作类型** | 删除数据 (DELETE) | 查询数据 (SELECT) |
| **SELECT 子句** | ❌ 不支持 | ✅ 必须 |
| **SET 子句** | ❌ 不支持 | ❌ 不支持 |
| **GROUP BY/HAVING** | ❌ 不支持 | ✅ 支持 |
| **ORDER BY/LIMIT** | ✅ 支持 | ✅ 支持 |
| **FOR UPDATE/SHARE** | ❌ 不支持 | ✅ 支持 |
| **返回类型** | `Delete` | `Select` |

### 选择指南

| 场景 | 推荐使用 | 原因 |
|------|----------|------|
| 删除数据 | `singleDelete` | 专为数据删除设计 |
| 查询数据 | `query` | 专为 SELECT 查询设计 |
| 需要 SELECT 子句 | `query` | 支持完整的选择项 |
| 需要 GROUP BY | `query` | 支持分组聚合 |
| 需要 FOR UPDATE | `query` | 支持行级锁 |

---

## singleDelete vs multiUpdate 对比分析

### 相同点

| 特性 | singleDelete | multiUpdate |
|------|--------------|-------------|
| WITH (CTE) | ✅ 支持 | ✅ 支持 |
| 修饰符 | ✅ LOW_PRIORITY/IGNORE/QUICK | ✅ LOW_PRIORITY/IGNORE |
| WHERE/AND | ✅ 支持 | ✅ 支持 |
| Batch 版本 | ✅ batchSingleDelete | ✅ batchMultiUpdate |
| Hint | ✅ 支持 | ✅ 支持 |

### 核心差异

| 特性 | singleDelete | multiUpdate |
|------|--------------|-------------|
| **操作类型** | 删除数据 (DELETE) | 更新数据 (UPDATE) |
| **表数量** | 单表删除 | 多表更新 |
| **JOIN** | ❌ 不支持 | ✅ 支持 |
| **INDEX HINT** | ❌ 不支持 | ✅ 支持 |
| **SET 子句** | ❌ 不支持 | ✅ 必须 |
| **ORDER BY/LIMIT** | ✅ 支持 | ❌ 不支持 |
| **返回类型** | `Delete` | `Update` |

### 选择指南

| 场景 | 推荐使用 | 原因 |
|------|----------|------|
| 删除数据 | `singleDelete` | 专为数据删除设计 |
| 更新数据 | `multiUpdate` | 专为数据更新设计 |
| 需要 ORDER BY/LIMIT | `singleDelete` | 支持排序和限制 |
| 需要 SET 子句 | `multiUpdate` | UPDATE 必须有 SET |
| 多表操作 | `multiUpdate` | 支持 JOIN |

---

## singleDelete vs multiDelete 对比分析

### 相同点

| 特性 | singleDelete | multiDelete |
|------|--------------|-------------|
| WITH (CTE) | ✅ 支持 | ✅ 支持 |
| 修饰符 | ✅ LOW_PRIORITY/IGNORE/QUICK | ✅ LOW_PRIORITY/IGNORE/QUICK |
| WHERE/AND | ✅ 支持 | ✅ 支持 |
| Partition | ✅ 支持 | ✅ 支持 |
| Batch 版本 | ✅ batchSingleDelete | ✅ batchMultiDelete |
| Hint | ✅ 支持 | ✅ 支持 |
| 返回类型 | `Delete` | `Delete` |

### 核心差异

| 特性 | singleDelete | multiDelete |
|------|--------------|-------------|
| **表数量** | 单表删除 | 多表删除 |
| **JOIN** | ❌ 不支持 | ✅ 支持 |
| **INDEX HINT** | ❌ 不支持 | ✅ useIndex/ignoreIndex/forceIndex |
| **USING 语法** | ❌ 不支持 | ✅ 支持 |
| **表源指定** | `from(table, AS, alias)` | `delete(alias...)` + `from/using(table)` |
| **ORDER BY/LIMIT** | ✅ 支持 | ❌ 不支持 |
| **返回接口** | `_SingleWithSpec<Delete>` | `_MultiWithSpec<Delete>` |

### 设计意图

- **singleDelete**：用于单表删除，支持 ORDER BY 和 LIMIT，适合简单删除场景
- **multiDelete**：用于多表关联删除，支持 JOIN 和 INDEX HINT，适合级联删除场景

### SQL 语法对比

**singleDelete 生成的 SQL**:
```sql
DELETE FROM user AS u WHERE u.id = 1
-- 或带修饰符:
DELETE LOW_PRIORITY QUICK IGNORE FROM user AS u PARTITION (p1) WHERE u.status = 0
```

**multiDelete 生成的 SQL**:
```sql
DELETE c, r FROM china_city AS c 
JOIN china_region AS r ON c.id = r.id 
WHERE r.region_type = 'CITY'
```

### 选择指南

| 场景 | 推荐使用 | 原因 |
|------|----------|------|
| 单表删除 | `singleDelete` | 更轻量级，支持 ORDER BY/LIMIT |
| 多表级联删除 | `multiDelete` | 支持 JOIN 和 USING 语法 |
| 需要 ORDER BY | `singleDelete` | 支持排序后删除 |
| 需要 LIMIT | `singleDelete` | 支持限制删除行数 |
| 需要 INDEX HINT | `multiDelete` | 支持索引提示优化 |
| 需要 USING 语法 | `multiDelete` | 支持 MySQL 特有的 USING 语法 |

---

## 自我进化指南

当 Army Criteria API 更新时，请按以下步骤更新本文档：

1. **检查接口变更**: 重新读取 `MySQLDelete.java`、`Statement.java`
2. **检查实现变更**: 重新读取 `MySQLSingleDeletes.java`、`SingleDeleteStatement.java`
3. **检查测试更新**: 查看 `MySQLCriteriaUnitTests.java` 的新增测试用例
4. **更新 Diagram**: 如有新方法/新子句，补充到 Diagram 中
5. **更新示例**: 补充新增用法的示例
6. **更新约束**: 如有新的语义约束，补充到规则部分
7. **检查相关文件**: 查看 `MySQLs.java`、`MySQLStatement.java` 等文件的变更
