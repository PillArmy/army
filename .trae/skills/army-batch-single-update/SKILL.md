---
name: "army-batch-single-update"
description: "完整的 Army 框架 batchSingleUpdate() 方法链使用指南，包含方法链图、使用示例和最佳实践。当需要使用批量单表更新功能时调用此技能。"
---

# Army BatchSingleUpdate 方法链完整指南

## 概述

`SQLs.batchSingleUpdate()` 是 Army 框架中用于创建标准批量单表更新语句的入口方法。本指南详细介绍了其完整的方法链、参数说明、使用场景和示例代码。

---

## 完整方法链图

```
SQLs.batchSingleUpdate()
    ↓
┌─────────────────────────────────────────────────────────────┐
│ _WithSpec (可选，可重复)                                      │
├─────────────────────────────────────────────────────────────┤
│ - with(name) → _StaticCteParensSpec                         │
│ - withRecursive(name) → _StaticCteParensSpec                │
│ - with(consumer) → _SingleUpdateClause                      │
│ - withRecursive(consumer) → _SingleUpdateClause             │
└─────────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────────┐
│ _SingleUpdateClause                                         │
├─────────────────────────────────────────────────────────────┤
│ - update(table, AS, alias) → _StandardSetClause            │
└─────────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────────┐
│ _StandardSetClause (可重复调用 set* 方法)                     │
├─────────────────────────────────────────────────────────────┤
│ 静态 SET 方法:                                               │
│ - set(field, value) → _StandardSetClause                    │
│ - set(field, valueOperator, value) → _StandardSetClause     │
│ - set(field, fieldOperator, valueOperator, value) → ...     │
│ - ifSet(field, value) → _StandardSetClause                  │
│ - ifSet(field, valueOperator, value) → _StandardSetClause   │
│                                                              │
│ 批量特定 SET 方法:                                           │
│ - setSpace(field, valueOperator) → _StandardSetClause       │
│ - setSpace(field, fieldOperator, valueOperator) → ...       │
│                                                              │
│ 动态 SET 方法:                                               │
│ - sets(consumer) → _StandardWhereClause                     │
└─────────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────────┐
│ _WhereClause (可选)                                          │
├─────────────────────────────────────────────────────────────┤
│ - where(predicate) → _WhereAndClause                        │
│ - whereIf(supplier) → _WhereAndClause                       │
│ - where(expOperator, operand) → _WhereAndClause             │
│ - where(consumer) → _DmlUpdateSpec                          │
└─────────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────────┐
│ _WhereAndClause (可选，可重复)                                │
├─────────────────────────────────────────────────────────────┤
│ - and(predicate) → _WhereAndClause                          │
│ - ifAnd(supplier) → _WhereAndClause                         │
│ - and(expOperator, operand) → _WhereAndClause               │
└─────────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────────┐
│ _DmlUpdateSpec                                              │
├─────────────────────────────────────────────────────────────┤
│ - asUpdate() → _BatchUpdateParamSpec                        │
└─────────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────────┐
│ _BatchUpdateParamSpec                                       │
├─────────────────────────────────────────────────────────────┤
│ - namedParamList(paramList) → BatchUpdate                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 各子句详解

### 1. WITH 子句 (可选，可重复)

**用途**: 定义通用表表达式 (CTE)，可以在后续的 UPDATE 语句中引用。

**多种调用形式**:

```java
// 形式1: 静态 WITH，单个 CTE 名称
.with("cte_name")
    .as(SQLs.query()
        .select(...)
        .from(...)
        .asQuery())

// 形式2: 递归 WITH
.withRecursive("cte_name")
    .as(SQLs.query()
        .select(...)
        .from(...)
        .asQuery())

// 形式3: 动态 WITH，使用 Consumer
.with(ctes -> {
    ctes.with("cte1").as(...);
    ctes.with("cte2").as(...);
})

// 形式4: 动态递归 WITH
.withRecursive(ctes -> {
    ctes.with("cte1").as(...);
})
```

---

### 2. UPDATE 子句 (必选)

**用途**: 指定要更新的表和别名。

**多种调用形式**:

```java
// 形式1: 使用 AS 关键字指定表别名
.update(UserMeta.TABLE, SQLs.AS, "u")

// 形式2: 使用 ChildTableMeta (用于领域更新)
.update(UserMeta.TABLE, SQLs.AS, "u")
```

---

### 3. SET 子句 (必选，可重复调用 set* 方法)

**用途**: 指定要更新的字段和值。

**多种调用形式**:

#### 3.1 静态 SET 方法

```java
// 形式1: 直接设置值
.set(UserMeta.NAME, "newName")
.set(UserMeta.AGE, 30)

// 形式2: 使用值操作符
.set(UserMeta.NAME, SQLs::param, "nameParam")
.set(UserMeta.AGE, SQLs::literal, 25)

// 形式3: 带字段操作符
.set(UserMeta.AGE, SQLs::plus, SQLs::param, "ageIncrement")

// 形式4: 条件 SET (ifSet)
.ifSet(UserMeta.PHONE, SQLs::param, "phoneParam")
```

#### 3.2 批量特定 SET 方法

```java
// 形式1: setSpace 用于批量参数
.setSpace(UserMeta.NAME, SQLs::namedParam)
.setSpace(UserMeta.AGE, SQLs::namedParam)

// 形式2: 带操作符的 setSpace
.setSpace(UserMeta.AGE, SQLs::plusEqual, SQLs::namedParam)
```

#### 3.3 动态 SET 方法

```java
// 使用 Consumer 动态添加多个 SET 项
.sets(pairs -> {
    pairs.set(UserMeta.NAME, SQLs::param, "name");
    pairs.set(UserMeta.EMAIL, SQLs::param, "email");
    pairs.ifSet(UserMeta.PHONE, SQLs::param, "phone");
})
```

---

### 4. WHERE 子句 (可选)

**用途**: 指定更新的条件。

**多种调用形式**:

```java
// 形式1: 单个谓词
.where(UserMeta.ID.equal(SQLs::param, "id"))

// 形式2: 条件 WHERE (whereIf)
.whereIf(() -> UserMeta.STATUS.equal(SQLs::literal, "ACTIVE"))

// 形式3: 使用表达式操作符
.where(UserMeta.ID::equal, 100)

// 形式4: 动态 WHERE，使用 Consumer
.where(predicates -> {
    predicates.accept(UserMeta.ID.equal(SQLs::param, "id"));
    predicates.accept(UserMeta.STATUS.equal(SQLs::literal, "ACTIVE"));
})
```

---

### 5. WHERE AND 子句 (可选，可重复)

**用途**: 添加多个 AND 条件。

**多种调用形式**:

```java
// 形式1: 添加单个谓词
.and(UserMeta.CREATE_TIME.greater(SQLs::param, "startDate"))

// 形式2: 条件 AND (ifAnd)
.ifAnd(() -> UserMeta.UPDATE_TIME.less(SQLs::param, "endDate"))

// 形式3: 使用表达式操作符
.and(UserMeta.AGE::greater, 18)
```

---

### 6. 完成语句构建

**用途**: 完成语句构建并设置批量参数。

```java
// 完成 UPDATE 语句构建
.asUpdate()

// 设置批量参数列表
.namedParamList(paramList)
```

---

## 使用示例

### 示例1: 基本批量更新

```java
import io.army.criteria.*;
import io.army.criteria.impl.SQLs;

public void basicBatchUpdate(SyncLocalSession session, List<User> users) {
    BatchUpdate stmt = SQLs.batchSingleUpdate()
        .update(UserMeta.TABLE, SQLs.AS, "u")
        .set(UserMeta.NAME, SQLs::param, "name")
        .set(UserMeta.AGE, SQLs::param, "age")
        .where(UserMeta.ID.equal(SQLs::param, "id"))
        .asUpdate()
        .namedParamList(users);
    
    ResultStates states = session.update(stmt);
    System.out.println("Updated: " + states.getAffectedRows());
}
```

### 示例2: 使用 setSpace 的批量更新

```java
public void batchUpdateWithSetSpace(SyncLocalSession session, List<Map<String, Object>> params) {
    BatchUpdate stmt = SQLs.batchSingleUpdate()
        .update(UserMeta.TABLE, SQLs.AS, "u")
        .setSpace(UserMeta.NAME, SQLs::namedParam)
        .setSpace(UserMeta.AGE, SQLs::plusEqual, SQLs::namedParam)
        .where(UserMeta.ID.equal(SQLs::namedParam))
        .asUpdate()
        .namedParamList(params);
    
    session.update(stmt);
}
```

### 示例3: 动态 SET 子句

```java
public void dynamicBatchUpdate(SyncLocalSession session, List<UserUpdate> updates) {
    BatchUpdate stmt = SQLs.batchSingleUpdate()
        .update(UserMeta.TABLE, SQLs.AS, "u")
        .sets(pairs -> {
            pairs.set(UserMeta.NAME, SQLs::param, "name");
            pairs.set(UserMeta.EMAIL, SQLs::param, "email");
            pairs.ifSet(UserMeta.PHONE, SQLs::param, "phone");
        })
        .where(UserMeta.ID.equal(SQLs::param, "id"))
        .asUpdate()
        .namedParamList(updates);
    
    session.update(stmt);
}
```

### 示例4: 带多个 WHERE 条件的批量更新

```java
public void batchUpdateWithWhere(SyncLocalSession session, List<UpdateParam> params) {
    BatchUpdate stmt = SQLs.batchSingleUpdate()
        .update(OrderMeta.TABLE, SQLs.AS, "o")
        .set(OrderMeta.STATUS, SQLs::literal, OrderStatus.SHIPPED)
        .set(OrderMeta.SHIP_TIME, SQLs::param, "shipTime")
        .where(OrderMeta.STATUS.equal(SQLs::literal, OrderStatus.PAID))
        .and(OrderMeta.CREATE_TIME.greater(SQLs::param, "startDate"))
        .and(OrderMeta.ID.in(SQLs::param, "orderIds"))
        .asUpdate()
        .namedParamList(params);
    
    session.update(stmt);
}
```

### 示例5: 带 WITH 子句的批量更新

```java
public void batchUpdateWithCte(SyncLocalSession session, List<UpdateRequest> requests) {
    BatchUpdate stmt = SQLs.batchSingleUpdate()
        .with("recent_orders", cte -> cte
            .as(SQLs.query()
                .select("o", OrderMeta.ID, OrderMeta.USER_ID)
                .from(OrderMeta.TABLE, SQLs.AS, "o")
                .where(OrderMeta.CREATE_TIME.greater(SQLs::param, "since"))
                .asQuery()))
        .update(UserMeta.TABLE, SQLs.AS, "u")
        .set(UserMeta.ORDER_COUNT, UserMeta.ORDER_COUNT.plus(SQLs::literal, 1))
        .from("recent_orders", SQLs.AS, "ro")
        .where(UserMeta.ID.equal("ro", OrderMeta.USER_ID))
        .asUpdate()
        .namedParamList(requests);
    
    session.update(stmt);
}
```

---

## 可重复调用的方法

| 方法 | 说明 |
|------|------|
| `with()`, `withRecursive()` | 可以多次调用添加多个 CTE |
| `set()`, `ifSet()`, `setSpace()` | 可以多次调用添加多个 SET 项 |
| `and()`, `ifAnd()` | 可以多次调用添加多个 AND 条件 |

---

## 不可重复调用的方法

| 方法 | 说明 |
|------|------|
| `update()` | 只能调用一次，指定更新表 |
| `where()` | 只能调用一次，开始 WHERE 子句 |
| `asUpdate()` | 只能调用一次，完成语句构建 |
| `namedParamList()` | 只能调用一次，设置批量参数 |

---

## 最佳实践

1. **使用 `setSpace()`**: 对于批量更新，推荐使用 `setSpace()` 配合 `SQLs::namedParam`，这样参数名会自动与字段名匹配
2. **条件更新**: 使用 `ifSet()` 来处理可选字段的更新
3. **动态构建**: 使用 `sets(Consumer)` 来灵活构建 SET 子句
4. **参数映射**: 确保 `namedParamList()` 中的对象与 SQL 中的参数名对应
5. **WHERE 条件**: 批量更新务必添加 WHERE 条件，避免全表更新

---

## 自我进化指南

本技能支持自我进化。如需更新或扩展内容：

1. 添加新的使用示例
2. 补充方言特定的用法（MySQL、PostgreSQL 等）
3. 添加更多高级场景说明
4. 优化方法链图的可视化
5. 补充性能优化建议

---

## 相关资源

- `StandardUpdates.java` - 标准更新实现
- `SQLs.java` - SQL 工具类入口
- `UpdateStatement.java` - 更新语句接口
- `StandardUpdate.java` - 标准更新接口
