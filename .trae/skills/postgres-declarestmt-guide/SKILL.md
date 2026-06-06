---
name: "postgres-declarestmt-guide"
description: "Guide for using Postgres.declareStmt() method chain with full diagrams, repeatability info, and clause details. Invoke when learning or using declareStmt()."
---

# Postgres.declareStmt() 方法链完整指南

## 概述

本文档详细介绍了 `Postgres.declareStmt()` 方法链的完整使用方法，包括方法链 Diagram、可重复性分析、各 clause 的多种形式及使用场景。

---

## 一、完整方法链 Diagram

```
Postgres.declareStmt()
│
├─→ declare(String name)
│   │
│   ├─ [可选] binary()
│   │   │
│   │   ├─ [可选] insensitive() / asensitive() / ifInsensitive(BooleanSupplier) / ifAsensitive(BooleanSupplier)
│   │   │   │
│   │   │   ├─ [可选] scroll() / noScroll() / ifScroll(BooleanSupplier) / ifNoScroll(BooleanSupplier)
│   │   │   │   │
│   │   │   │   └─→ cursor()
│   │   │   │       │
│   │   │   │       └─ [可选] withHold() / withoutHold() / ifWithHold(BooleanSupplier) / ifWithoutHold(BooleanSupplier)
│   │   │   │           │
│   │   │   │           └─→ forSpace() 或 forSpace(SubQuery query)
│   │   │   │               │
│   │   │   │               │  [如果使用 forSpace()]
│   │   │   │               └─→ PostgreQuery.WithSpec 完整查询链
│   │   │   │                   │
│   │   │   │                   ├─ [WITH 子句 (可选)]
│   │   │   │                   ├─ select(...)
│   │   │   │                   ├─ from(...)
│   │   │   │                   ├─ [where(...) (可选)]
│   │   │   │                   ├─ [groupBy(...) (可选)]
│   │   │   │                   ├─ [having(...) (可选)]
│   │   │   │                   ├─ [orderBy(...) (可选)]
│   │   │   │                   ├─ [limit(...) (可选)]
│   │   │   │                   ├─ [lock 相关 (可选)]
│   │   │   │                   └─→ asQuery()
│   │   │   │
│   │   │   └─→ ... (同上)
│   │   │
│   │   └─→ ... (同上)
│   │
│   └─→ ... (同上)
│
└─→ asCommand()
```

---

## 二、方法详细说明（含参数列表）

### 2.1 起始方法

| 方法 | 参数 | 说明 |
|------|------|------|
| `declareStmt()` | 无 | 创建 DECLARE 语句构建器，返回 `_PostgreDeclareClause` |

---

### 2.2 游标声明阶段

| 方法 | 参数 | 说明 | 返回类型 |
|------|------|------|----------|
| `declare(name)` | `String name` - 游标名称 | 声明游标名称 | `_BinarySpec` |
| `binary()` | 无 | 设置二进制模式 | `_InsensitiveSpec` |

---

### 2.3 敏感度（Sensitive）选项

| 方法 | 参数 | 说明 | 返回类型 |
|------|------|------|----------|
| `insensitive()` | 无 | 设置为不敏感模式 | `_ScrollSpec` |
| `asensitive()` | 无 | 设置为敏感模式 | `_ScrollSpec` |
| `ifInsensitive(supplier)` | `BooleanSupplier supplier` | 条件设置为不敏感 | `_ScrollSpec` |
| `ifAsensitive(supplier)` | `BooleanSupplier supplier` | 条件设置为敏感 | `_ScrollSpec` |

---

### 2.4 滚动（Scroll）选项

| 方法 | 参数 | 说明 | 返回类型 |
|------|------|------|----------|
| `scroll()` | 无 | 启用滚动 | `_CursorClause` |
| `noScroll()` | 无 | 禁用滚动 | `_CursorClause` |
| `ifScroll(supplier)` | `BooleanSupplier supplier` | 条件启用滚动 | `_CursorClause` |
| `ifNoScroll(supplier)` | `BooleanSupplier supplier` | 条件禁用滚动 | `_CursorClause` |

---

### 2.5 Cursor 阶段

| 方法 | 参数 | 说明 | 返回类型 |
|------|------|------|----------|
| `cursor()` | 无 | 标记 cursor 关键字 | `_HoldSpec` |

---

### 2.6 Hold 选项

| 方法 | 参数 | 说明 | 返回类型 |
|------|------|------|----------|
| `withHold()` | 无 | 启用 WITH HOLD | `_ForQueryClause` |
| `withoutHold()` | 无 | 禁用 WITH HOLD | `_ForQueryClause` |
| `ifWithHold(supplier)` | `BooleanSupplier supplier` | 条件启用 WITH HOLD | `_ForQueryClause` |
| `ifWithoutHold(supplier)` | `BooleanSupplier supplier` | 条件禁用 WITH HOLD | `_ForQueryClause` |

---

### 2.7 查询阶段

| 方法 | 参数 | 说明 | 返回类型 |
|------|------|------|----------|
| `forSpace()` | 无 | 进入查询构建空间 | `PostgreQuery.WithSpec` |
| `forSpace(query)` | `SubQuery query` - 已构建的子查询 | 直接使用已有子查询 | `_AsCommandClause` |

---

### 2.8 查询链（Select 相关）

| 方法 | 参数示例 | 说明 |
|------|---------|------|
| `select(...)` | `select("c", PERIOD, ChinaRegion_.T)` | 选择要查询的列 |
| `from(...)` | `from(ChinaRegion_.T, AS, "c")` | FROM 子句 |
| `where(...)` | `where(ChinaRegion_.id.in(...))` | WHERE 过滤条件 |
| `groupBy(...)` | - | GROUP BY 分组 |
| `having(...)` | - | HAVING 分组过滤 |
| `orderBy(...)` | `orderBy(ChinaRegion_.id)` | 排序 |
| `limit(...)` | `limit(SQLs::literal, 100)` | 限制结果数量 |
| `asQuery()` | 无 | 完成子查询构建 |

---

### 2.9 最终完成

| 方法 | 参数 | 说明 | 返回类型 |
|------|------|------|----------|
| `asCommand()` | 无 | 完成整个 DECLARE 语句构建 | `DmlCommand` |

---

## 三、方法可重复性分析

### 3.1 不可重复调用的方法

| 方法 | 原因 |
|------|------|
| `declare(name)` | 游标名称只能设置一次 |
| `binary()` | 二进制模式只能设置一次 |
| `insensitive()` / `asensitive()` | 敏感模式只能设置一种 |
| `scroll()` / `noScroll()` | 滚动模式只能设置一种 |
| `cursor()` | cursor 关键字只能出现一次 |
| `withHold()` / `withoutHold()` | Hold 模式只能设置一种 |
| `forSpace(query)` | 直接使用 SubQuery 只能调用一次 |
| `asQuery()` | 子查询完成只能调用一次 |
| `asCommand()` | 最终完成只能调用一次 |

### 3.2 可重复调用的方法

| 方法 | 说明 |
|------|------|
| `select(...)` + `comma(...)` | select 后可跟多个 comma 添加更多列 |
| `from(...)` + join 相关方法 | FROM 后可多次 join |
| `where(...)` + `and(...)` | WHERE 后可多次 and |
| `groupBy(...)` + `comma(...)` | GROUP BY 后可添加更多分组项 |
| `orderBy(...)` + `comma(...)` | ORDER BY 后可添加更多排序列 |

---

## 四、各 Clause 的多种形式与使用场景

### 4.1 declare 子句

**场景**：声明游标

| 形式 | 说明 |
|------|------|
| `declare("my_cursor")` | 最基本形式，直接指定游标名 |

---

### 4.2 binary 子句

**场景**：需要二进制模式时使用

| 形式 | 说明 |
|------|------|
| `.binary()` | 启用二进制模式 |
| (省略) | 默认非二进制 |

---

### 4.3 敏感/不敏感子句

**场景**：控制游标是否对底层数据变化敏感

| 形式 | 说明 | 适用场景 |
|------|------|---------|
| `.insensitive()` | 不敏感，游标不感知底层数据变化 | 只读查询、数据快照 |
| `.asensitive()` | 敏感，游标可感知底层数据变化 | 需要看到最新数据 |
| `.ifInsensitive(supplier)` | 条件启用不敏感 | 动态决定 |
| `.ifAsensitive(supplier)` | 条件启用敏感 | 动态决定 |
| (省略) | 使用数据库默认值 | 大多数场景 |

---

### 4.4 滚动子句

**场景**：控制是否可以在结果集中自由滚动

| 形式 | 说明 | 适用场景 |
|------|------|---------|
| `.scroll()` | 启用滚动，可自由移动 | 需要前后浏览、随机访问 |
| `.noScroll()` | 禁用滚动，只能向前 | 顺序处理、节省资源 |
| `.ifScroll(supplier)` | 条件启用滚动 | 动态决定 |
| `.ifNoScroll(supplier)` | 条件禁用滚动 | 动态决定 |
| (省略) | 使用数据库默认值 | 大多数场景 |

---

### 4.5 Hold 子句

**场景**：控制游标在事务提交后是否保持打开

| 形式 | 说明 | 适用场景 |
|------|------|---------|
| `.withHold()` | 启用 WITH HOLD，事务提交后游标仍打开 | 需要跨事务使用游标 |
| `.withoutHold()` | 禁用 WITH HOLD，事务提交后关闭 | 常规事务内使用 |
| `.ifWithHold(supplier)` | 条件启用 WITH HOLD | 动态决定 |
| `.ifWithoutHold(supplier)` | 条件禁用 WITH HOLD | 动态决定 |
| (省略) | 使用数据库默认值 | 大多数场景 |

---

### 4.6 查询子句

**场景**：定义游标要查询的数据

| 形式 | 说明 | 适用场景 |
|------|------|---------|
| `.forSpace()` | 进入查询构建空间，继续链式调用 | 需要动态构建查询 |
| `.forSpace(subQuery)` | 直接使用已构建好的 SubQuery | 查询已预先构建好 |

---

## 五、完整使用示例

### 示例 1：基本使用（来自 CursorTests.java）

```java
final DmlCommand stmt;
stmt = Postgres.declareStmt()
        .declare("my_china_region_cursor").cursor()
        .forSpace()
        .select("c", PERIOD, ChinaRegion_.T)
        .from(ChinaRegion_.T, AS, "c")
        .where(ChinaRegion_.id.in(SQLs::rowLiteral, extractRegionIdList(regionList)))
        .orderBy(ChinaRegion_.id)
        .limit(SQLs::literal, regionList.size())
        .asQuery()
        .asCommand();
```

### 示例 2：带完整选项

```java
final DmlCommand stmt;
stmt = Postgres.declareStmt()
        .declare("my_cursor")
        .binary()
        .insensitive()
        .scroll()
        .cursor()
        .withHold()
        .forSpace()
        .select("c", PERIOD, ChinaRegion_.T)
        .from(ChinaRegion_.T, AS, "c")
        .where(ChinaRegion_.id.greater(100))
        .orderBy(ChinaRegion_.id)
        .asQuery()
        .asCommand();
```

### 示例 3：条件选项

```java
final boolean needScroll = true;
final boolean needHold = false;

final DmlCommand stmt;
stmt = Postgres.declareStmt()
        .declare("my_cursor")
        .ifScroll(() -> needScroll)
        .cursor()
        .ifWithHold(() -> needHold)
        .forSpace()
        .select("c", PERIOD, ChinaRegion_.T)
        .from(ChinaRegion_.T, AS, "c")
        .asQuery()
        .asCommand();
```

---

## 六、相关接口

### 主要接口定义位置

| 接口 | 文件路径 |
|------|---------|
| `PostgreCursor` | `army-postgre/src/main/java/io/army/criteria/postgre/PostgreCursor.java` |
| `Postgres` | `army-postgre/src/main/java/io/army/criteria/impl/Postgres.java` |
| `PostgreDeclareCursors` | `army-postgre/src/main/java/io/army/criteria/impl/PostgreDeclareCursors.java` |
| `PostgreQuery` | `army-postgre/src/main/java/io/army/criteria/postgre/PostgreQuery.java` |

---

## 七、自我进化指南

### 7.1 如何更新本文档

当发现新的使用方式或 API 变化时，请按以下步骤更新：

1. **检查代码变更**：查看相关 Java 源文件的最新版本
2. **更新 Diagram**：如果方法链有变化，更新 Diagram 部分
3. **更新方法表**：添加/修改/删除方法条目
4. **更新可重复性分析**：根据实现确认可重复性
5. **添加新示例**：如果有新的使用模式，添加示例
6. **更新版本记录**：在下方添加更新记录

### 7.2 版本记录

| 版本 | 日期 | 更新内容 | 更新者 |
|------|------|---------|-------|
| 1.0 | 2026-06-06 | 初始版本，完整文档 | - |

---

## 八、参考资料

- PostgreSQL 官方文档：[DECLARE](https://www.postgresql.org/docs/current/sql-declare.html)
- 测试代码：`army-example/src/test/java/io/army/session/sync/postgre/CursorTests.java`
