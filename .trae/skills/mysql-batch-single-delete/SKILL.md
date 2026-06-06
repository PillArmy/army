---
name: "mysql-batch-single-delete"
description: "完整学习和使用 MySQLs.batchSingleDelete() 方法链。提供完整方法链图、详细说明、各子句多种形式、示例代码。当需要构建批量单表删除时调用此技能。"
---

# MySQLs.batchSingleDelete() 方法链完整指南

## 概述

`MySQLs.batchSingleDelete()` 是 Army Criteria API 中用于创建 MySQL 批量单表 DELETE 语句的入口方法。该方法返回 `MySQLDelete._SingleWithSpec<_BatchDeleteParamSpec>` 接口，允许构建支持批量参数绑定的单表删除语句。

## 核心入口

```java
// MySQLs.java line 188-190
public static MySQLDelete._SingleWithSpec<Statement._BatchDeleteParamSpec> batchSingleDelete() {
    return MySQLSingleDeletes.batch();
}
```

**内部实现**：
```java
// MySQLSingleDeletes.java line 71-73
static _SingleWithSpec<_BatchDeleteParamSpec> batch() {
    return new MySQLBatchDelete();
}
```

---

## 完整方法链 Diagram

```
MySQLs.batchSingleDelete()
    ↓
┌─────────────────────────────────────────────────────────────┐
│ MySQLDelete._SingleWithSpec<_BatchDeleteParamSpec>        │
├─────────────────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────────────────┐   │
│ │ WITH 子句 (可选，可重复)                              │   │
│ │ - with(name) → _StaticCteParensSpec                │   │
│ │ - withRecursive(name) → _StaticCteParensSpec       │   │
│ │ - with(consumer) → _SimpleSingleDeleteClause       │   │
│ │ - withRecursive(consumer) → _SimpleSingleDeleteClause│ │
│ └─────────────────────────────────────────────────────┘   │
│                          ↓                                  │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ DELETE 子句 (可选，带修饰符和提示)                    │   │
│ │ - delete(supplier, modifiers) → _SingleDeleteFromClause│ │
│ │                                                         │   │
│ │ ┌───────────────────────────────────────────────────┐ │ │
│ │ │ FROM 子句 (必须，仅一次)                            │ │ │
│ │ │ - deleteFrom(table, AS, alias) → _SinglePartitionSpec│ ││
│ │ │ - from(table, AS, alias) → _SinglePartitionSpec   │ │ │
│ │ └───────────────────────────────────────────────────┘ │ │
│ │                          ↓                              │ │
│ │ ┌───────────────────────────────────────────────────┐ │ │
│ │ │ PARTITION 子句 (可选，三种形式)                      │ │ │
│ │ │ - partition(first, ...rest) → _SingleWhereClause  │ │ │
│ │ │ - partition(consumer) → _SingleWhereClause        │ │ │
│ │ │ - ifPartition(consumer) → _SingleWhereClause       │ │ │
│ │ └───────────────────────────────────────────────────┘ │ │
│ │                          ↓                              │ │
│ │ ┌───────────────────────────────────────────────────┐ │ │
│ │ │ WHERE 子句 (可选，多种形式)                          │ │ │
│ │ │ - where(predicate) → _SingleWhereAndSpec          │ │ │
│ │ │ - where(expOperator, operand) → _SingleWhereAndSpec│ │ │
│ │ │ - where(consumer) → _OrderBySpec                   │ │ │
│ │ │ - whereIf(supplier) → _SingleWhereAndSpec          │ │ │
│ │ │                                                         │ │ │
│ │ │ ┌───────────────────────────────────────────────┐ │ │ │
│ │ │ │ AND 子句 (可选，可重复)                          │ │ │ │
│ │ │ │ - and(predicate) → _SingleWhereAndSpec        │ │ │ │
│ │ │ │ - and(expOperator, operand) → _SingleWhereAndSpec│ │ ││
│ │ │ │ - ifAnd(supplier) → _SingleWhereAndSpec        │ │ │ │
│ │ │ └───────────────────────────────────────────────┘ │ │ │
│ │ │                          ↓                          │ │ │
│ │ │ ┌───────────────────────────────────────────────┐ │ │ │
│ │ │ │ ORDER BY 子句 (可选，可重复排序)                 │ │ │ │
│ │ │ │ - orderBy(sortItem) → _OrderByCommaSpec       │ │ │ │
│ │ │ │ - orderBy(sortItem1, sortItem2) → _OrderByCommaSpec│ ││
│ │ │ │ - comma(sortItem)                              │ │ │ │
│ │ │ └───────────────────────────────────────────────┘ │ │ │
│ │ │                          ↓                          │ │ │
│ │ │ ┌───────────────────────────────────────────────┐ │ │ │
│ │ │ │ LIMIT 子句 (可选)                                │ │ │ │
│ │ │ │ - limit(count) → _DmlDeleteSpec               │ │ │ │
│ │ │ │ - limit(valueOperator, count) → _DmlDeleteSpec│ │ │ │
│ │ │ │ - ifLimit(supplier) → _DmlDeleteSpec           │ │ │ │
│ │ │ └───────────────────────────────────────────────┘ │ │ │
│ │ │                          ↓                          │ │ │
│ │ │ ┌───────────────────────────────────────────────┐ │ │ │
│ │ │ │ 结束语句                                        │ │ │ │
│ │ │ │ - asDelete() → _BatchDeleteParamSpec           │ │ │ │
│ │ │ │                                                │ │ │ │
│ │ │ │ ┌─────────────────────────────────────────┐   │ │ │ │
│ │ │ │ │ 最终构建                                  │   │ │ │ │
│ │ │ │ │ - namedParamList(paramList) → BatchDelete│   │ │ │ │
│ │ │ │ └─────────────────────────────────────────┘   │ │ │ │
│ │ │ └───────────────────────────────────────────────┘ │ │ │
│ │ └───────────────────────────────────────────────────┘ │ │
│ └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 逐层接口详解

### 0. 入口：`MySQLDelete._SingleWithSpec<_BatchDeleteParamSpec>`

```java
// MySQLDelete.java line 157-162
interface _SingleWithSpec<I extends Item>
        extends _MySQLDynamicWithClause<_SimpleSingleDeleteClause<I>>,
                _MySQLStaticWithClause<_SimpleSingleDeleteClause<I>>,
                _SimpleSingleDeleteClause<I> {
}
```

该接口组合了三种能力：
- **动态 CTE**：使用 Consumer 构建 CTE
- **静态 CTE**：使用链式方法构建 CTE
- **DELETE**：直接进入 DELETE 构建

---

### 1. WITH 子句 (可选)

#### 1.1 动态 CTE

```java
MySQLs.batchSingleDelete()
    .with(builder -> {
        builder.comma("cte1").as(s -> s.select(...).from(...).asQuery());
        builder.comma("cte2").as(s -> s.select(...).from(...).asQuery());
    })
    .deleteFrom(...)
```

#### 1.2 静态 CTE

```java
MySQLs.batchSingleDelete()
    .with("cte_name")
    .parens(s -> s.select(...).from(...).asQuery())
    .as()
    .space()
    .deleteFrom(...)
```

#### 1.3 条件 CTE

```java
MySQLs.batchSingleDelete()
    .ifWith(builder -> { ... })        // 仅当有实际操作时生效
    .ifWithRecursive(builder -> { ... })
```

---

### 2. DELETE 子句 (可选，带修饰符和提示)

```java
// MySQLDelete.java line 46-51
interface _SingleDeleteClause<T> extends DeleteStatement._SingleDeleteClause<T> {
    DeleteStatement._SingleDeleteFromClause<T> delete(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers);
}
```

#### 2.1 MySQL 支持的修饰符

- `MySQLs.LOW_PRIORITY`：降低删除操作优先级
- `MySQLs.QUICK`：快速删除，不合并索引叶子节点
- `MySQLs.IGNORE`：忽略删除过程中的错误

#### 2.2 MySQL 支持的提示

- `MySQLs.qbName(name)`：查询块名称
- `MySQLs.orderIndex(qbName, tableAlias, indexList)`：顺序索引提示

#### 2.3 使用示例

```java
final Supplier<List<Hint>> hintSupplier = () -> {
    final List<Hint> hintList = new ArrayList<>(2);
    hintList.add(MySQLs.qbName("regionDelete"));
    hintList.add(MySQLs.orderIndex("regionDelete", "r", Collections.singletonList("PRIMARY")));
    return hintList;
};

MySQLs.batchSingleDelete()
    .delete(hintSupplier, Arrays.asList(MySQLs.LOW_PRIORITY, MySQLs.QUICK, MySQLs.IGNORE))
    .from(...)
```

---

### 3. FROM 子句 (必须，仅一次)

```java
// MySQLSingleDeletes.java line 103-129
_SinglePartitionSpec<I> deleteFrom(@Nullable SingleTableMeta<?> table, SQLs.WordAs as, @Nullable String alias);
_SinglePartitionSpec<I> from(SingleTableMeta<?> table, SQLs.WordAs as, String alias);
```

#### 3.1 使用规则

- **必须调用一次，且仅能调用一次**
- 仅接受 `SingleTableMeta` 类型
- `SQLs.WordAs` 固定传入 `SQLs.AS`
- `tableAlias` 必须提供非空字符串

#### 3.2 两种调用形式

```java
// 形式 1：deleteFrom
MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")

// 形式 2：from (需要先调用 delete())
MySQLs.batchSingleDelete()
    .delete(...)
    .from(ChinaRegion_.T, SQLs.AS, "r")
```

---

### 4. PARTITION 子句 (可选，三种形式)

```java
// MySQLSingleDeletes.java line 132-147
// 三种形式：
_SingleWhereClause<I> partition(String first, String... rest);
_SingleWhereClause<I> partition(Consumer<Consumer<String>> consumer);
_SingleWhereClause<I> ifPartition(Consumer<Consumer<String>> consumer);
```

#### 4.1 形式对比

| 形式 | 说明 |
|-----|------|
| `partition(first, ...rest)` | 直接指定一个或多个分区名 |
| `partition(consumer)` | 使用 Consumer 动态构建分区列表 |
| `ifPartition(consumer)` | 条件形式，仅当有实际操作时生效 |

#### 4.2 使用示例

```java
// 直接指定分区
MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
    .partition("p1", "p2")

// 动态构建分区
MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
    .partition(list -> {
        list.accept("p1");
        list.accept("p2");
    })
```

---

### 5. WHERE 子句 (可选，多种形式)

#### 5.1 静态 WHERE

```java
MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
    .where(ChinaRegion_.id.equal(SQLs::param, "id"))
```

#### 5.2 带表达式操作符的 WHERE

```java
MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
    .where(ChinaRegion_.id::equal, SQLs::param, "id")
```

#### 5.3 Consumer 风格的 WHERE

```java
MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
    .where(where -> {
        where.accept(ChinaRegion_.id.greater(SQLs::param, "minId"));
        where.accept(ChinaRegion_.name.equal(SQLs::param, "name"));
    })
```

#### 5.4 条件 WHERE

```java
MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
    .whereIf(() -> shouldFilter ? ChinaRegion_.id.equal(SQLs::param, "id") : null)
```

---

### 6. AND 子句 (可选，可重复)

#### 6.1 静态 AND

```java
MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
    .where(ChinaRegion_.name.equal(SQLs::param, "name"))
    .and(ChinaRegion_.regionGdp.greater(SQLs::param, "minGdp"))
```

#### 6.2 条件 AND

```java
MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
    .where(ChinaRegion_.name.equal(SQLs::param, "name"))
    .ifAnd(() -> shouldFilterGdp ? ChinaRegion_.regionGdp.greater(SQLs::param, "minGdp") : null)
```

---

### 7. ORDER BY 子句 (可选，可重复排序)

```java
MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
    .where(...)
    .orderBy(ChinaRegion_.name::desc, ChinaRegion_.id)
```

---

### 8. LIMIT 子句 (可选)

#### 8.1 静态 LIMIT

```java
MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
    .where(...)
    .limit(10)
```

#### 8.2 带操作符的 LIMIT

```java
MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
    .where(...)
    .limit(SQLs::param, "limitCount")
```

#### 8.3 条件 LIMIT

```java
MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
    .where(...)
    .ifLimit(map.get("rowCount"))
```

---

### 9. 结束语句

#### 9.1 asDelete()

```java
// 调用 asDelete() 后进入批量参数接口
Statement._BatchDeleteParamSpec spec = MySQLs.batchSingleDelete()
    .deleteFrom(...)
    .where(...)
    .asDelete();
```

#### 9.2 namedParamList(paramList)

```java
// 最终构建 BatchDelete 对象
BatchDelete stmt = MySQLs.batchSingleDelete()
    .deleteFrom(...)
    .where(...)
    .asDelete()
    .namedParamList(paramList);
```

---

## 完整示例代码

### 示例 1：简单批量删除

```java
final List<Map<String, Object>> paramList = _Collections.arrayList();
Map<String, Object> paramMap;

paramMap = _Collections.hashMap();
paramMap.put("name", "水城");
paramMap.put("regionGdp", "39999.00");
paramList.add(paramMap);

paramMap = _Collections.hashMap();
paramMap.put("name", "凉都");
paramMap.put("regionGdp", new BigDecimal("99999.00"));
paramList.add(paramMap);

final BatchDelete stmt;
stmt = MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, AS, "r")
    .where(ChinaRegion_.name::spaceEqual, SQLs::namedParam)
    .and(ChinaRegion_.regionGdp::spaceEqual, SQLs::namedParam)
    .asDelete()
    .namedParamList(paramList);
```

### 示例 2：带修饰符和提示的批量删除

```java
final Map<String, Object> map = _Collections.hashMap();
final LocalDateTime now = LocalDateTime.now();
map.put("startTime", now.minusDays(15));
map.put("endTIme", now.plusDays(6));
map.put("version", "0");

final Supplier<List<Hint>> hintSupplier = () -> {
    final List<Hint> hintList = new ArrayList<>(2);
    hintList.add(MySQLs.qbName("regionDelete"));
    hintList.add(MySQLs.orderIndex("regionDelete", "r", Collections.singletonList("PRIMARY")));
    return hintList;
};

final List<Map<String, Object>> paramList = _Collections.arrayList();
// ... 填充 paramList

final BatchDelete stmt;
stmt = MySQLs.batchSingleDelete()
    .delete(hintSupplier, Arrays.asList(MySQLs.LOW_PRIORITY, MySQLs.QUICK, MySQLs.IGNORE))
    .from(ChinaRegion_.T, AS, "r")
    .partition("p1")
    .where(ChinaRegion_.name::spaceEqual, SQLs::namedParam)
    .and(ChinaRegion_.regionGdp::spaceEqual, SQLs::namedParam)
    .and(ChinaRegion_.updateTime.between(SQLs::literal, map.get("startTime"), AND, map.get("endTIme")))
    .ifAnd(ChinaRegion_.version::equal, SQLs::literal, map.get("version"))
    .orderBy(ChinaRegion_.name::desc)
    .ifLimit(map.get("rowCount"))
    .asDelete()
    .namedParamList(paramList);
```

### 示例 3：带分区的批量删除

```java
final List<Map<String, Object>> paramList = _Collections.arrayList();
// ... 填充 paramList

final BatchDelete stmt = MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, AS, "r")
    .partition("p1", "p2")
    .where(ChinaRegion_.id::spaceEqual, SQLs::namedParam)
    .asDelete()
    .namedParamList(paramList);
```

### 示例 4：带 ORDER BY 和 LIMIT 的批量删除

```java
final List<Map<String, Object>> paramList = _Collections.arrayList();
// ... 填充 paramList

final BatchDelete stmt = MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, AS, "r")
    .where(ChinaRegion_.regionType.equal(SQLs::literal, RegionType.CITY))
    .and(ChinaRegion_.id::spaceEqual, SQLs::namedParam)
    .orderBy(ChinaRegion_.population::desc)
    .limit(5)
    .asDelete()
    .namedParamList(paramList);
```

---

## 可重复性总结

| 子句 | 是否必须 | 可重复调用 | 说明 |
|-----|---------|-----------|------|
| `MySQLs.batchSingleDelete()` | 是 | 否 | 入口，每次调用创建新语句 |
| `with()` / `withRecursive()` | 否 | 否 | WITH 关键字仅一次，多个 CTE 用 comma 追加 |
| `delete()` (带修饰符) | 否 | 否 | 可选调用 |
| `deleteFrom()` / `from()` | 是 | 否 | 必须且仅能调用一次 |
| `partition()` 系列 | 否 | 否 | 仅能调用一次 |
| `where()` | 否 | 否 | 仅能调用一次 |
| `and()` | 否 | 是 | WHERE 后可多次调用 AND |
| `orderBy()` / `comma()` | 否 | 是 | 可多次调用添加排序条件 |
| `limit()` 系列 | 否 | 否 | 仅能调用一次 |
| `asDelete()` | 是 | 否 | 必须调用一次，结束构建 |
| `namedParamList()` | 是 | 否 | 必须调用一次，设置批量参数 |

---

## 参数映射说明

批量删除时，参数列表中的对象需要与 SQL 中的参数名对应：

```java
// SQL 中使用 SQLs::namedParam 引用参数名
.where(ChinaRegion_.name::spaceEqual, SQLs::namedParam) // 参数名 "name"
.and(ChinaRegion_.id::spaceEqual, SQLs::namedParam)     // 参数名 "id"

// 参数对象中需要有对应的字段名
public class DeleteParam {
    private String name; // 对应 "name"
    private Long id;     // 对应 "id"
    
    // getters and setters
}

// 也可以使用 Map
Map<String, Object> paramMap = new HashMap<>();
paramMap.put("name", "某个名称");
paramMap.put("id", 123L);
```

**注意**：
- 批处理参数（每行变化的参数）使用 `SQLs::namedParam` + 字段名
- 公共参数（每行相同的参数）使用 `SQLs::literal` 或 `SQLs::param`

---

## 注意事项

1. **表别名必须**：`deleteFrom()` 或 `from()` 必须提供表别名
2. **批处理参数**：使用 `SQLs::namedParam` 绑定每行变化的参数
3. **公共参数**：所有行相同的参数使用 `SQLs::literal` 或 `SQLs::param`
4. **修饰符使用**：合理使用 `LOW_PRIORITY`、`QUICK`、`IGNORE` 修饰符
5. **提示优化**：使用索引提示优化删除性能
6. **分区指定**：当表使用分区时，指定分区可提高性能
7. **LIMIT 约束**：批量删除时使用 LIMIT 避免一次删除过多数据

---

## 实现类继承链

```
MySQLSyntax (MySQLs extends)
    └─ MySQLs
        └─ .batchSingleDelete() → MySQLSingleDeletes.batch()
            └─ MySQLBatchDelete
                └─ MySQLSingleDeletes<_BatchDeleteParamSpec>
                    └─ SingleDeleteStatement
                        └─ ArmyStmtSpec
```

---

## 相关接口

| 接口 | 说明 |
|-----|------|
| `MySQLDelete._SingleWithSpec` | 起始接口，支持 WITH 子句 |
| `MySQLDelete._SimpleSingleDeleteClause` | 支持 DELETE 子句 |
| `DeleteStatement._SingleDeleteFromClause` | 支持 FROM 子句 |
| `MySQLDelete._SinglePartitionSpec` | 支持 PARTITION 子句 |
| `MySQLDelete._SingleWhereClause` | 支持 WHERE 子句 |
| `MySQLDelete._SingleWhereAndSpec` | 支持 AND 子句 |
| `MySQLDelete._OrderBySpec` | 支持 ORDER BY 子句 |
| `MySQLDelete._OrderByCommaSpec` | 支持 ORDER BY 后追加排序 |
| `MySQLDelete._LimitSpec` | 支持 LIMIT 子句 |
| `Statement._BatchDeleteParamSpec` | 批量参数接口 |
| `BatchDelete` | 最终批量删除语句 |

---

## 与其他 DELETE 入口的区别

| 入口 | 返回类型 | 场景 |
|-----|---------|------|
| `MySQLs.singleDelete()` | `MySQLDelete._SingleWithSpec<Delete>` | MySQL 单表 DELETE（单次删除） |
| `MySQLs.batchSingleDelete()` | `MySQLDelete._SingleWithSpec<_BatchDeleteParamSpec>` | MySQL 单表 DELETE（批量删除） |
| `MySQLs.multiDelete()` | `MySQLDelete._MultiWithSpec<Delete>` | MySQL 多表 DELETE（单次删除） |
| `MySQLs.batchMultiDelete()` | `MySQLDelete._MultiWithSpec<_BatchDeleteParamSpec>` | MySQL 多表 DELETE（批量删除） |
| `SQLs.singleDelete()` | `_WithSpec<Delete>` | 标准 SQL 单表 DELETE |

---

## 自我进化指南

当 Army Criteria API 更新时，请按以下步骤更新本文档：

1. **检查接口变更**：重新读取 `MySQLDelete.java`、`Statement.java`
2. **检查实现变更**：重新读取 `MySQLSingleDeletes.java`
3. **检查测试更新**：查看 `MySQLCriteriaUnitTests.java` 的新增测试用例
4. **更新 Diagram**：如有新方法/新子句，补充到 Diagram 中
5. **更新示例**：补充新增用法的示例
6. **更新约束**：如有新的语义约束，补充到规则部分
7. **检查相关文件**：查看 `MySQLs.java`、`MySQLStatement.java` 等文件的变更
