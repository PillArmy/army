---
name: "mysql-batch-single-update"
description: "完整学习和使用 MySQLs.batchSingleUpdate() 方法链。提供方法链图、详细说明、示例代码。当需要构建批量单表更新时调用。"
---

# MySQL batchSingleUpdate() 方法链完整指南

## 概述

`MySQLs.batchSingleUpdate()` 是 Army 框架中用于创建 MySQL 批量单表更新语句的入口方法。本指南详细介绍了其完整的方法链、参数说明、使用场景和示例代码。

---

## 完整方法链图

```
MySQLs.batchSingleUpdate()
    ↓
┌─────────────────────────────────────────────────────────────┐
│ _SingleWithSpec                                         │
├─────────────────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────────────────┐ │
│ │ WITH 子句 (可选，可重复)                              │ │
│ │ - with(name) → _StaticCteParensSpec                  │ │
│ │ - withRecursive(name) → _StaticCteParensSpec        │ │
│ │ - with(consumer) → _SingleUpdateClause              │ │
│ │ - withRecursive(consumer) → _SingleUpdateClause │ │
│ └─────────────────────────────────────────────────────┘ │
│                      ↓                                 │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ UPDATE 子句 (必需，不可重复)                        │ │
│ │ - update(supplier, modifiers) → _SingleUpdateSpaceClause │ │
│ │ - update(table, AS, alias)                         │ │
│ │ - update(complexTable, AS, alias)                    │ │
│ │ - update(table) → _SinglePartitionClause         │ │
│ │ - update(complexTable) → _SinglePartitionClause    │ │
│ │                                                         │ │
│ │ 【SPACE 变体】                                        │ │
│ │ - space(table, AS, alias)                              │ │
│ │ - space(complexTable, AS, alias)                     │ │
│ │ - space(table) → _SinglePartitionClause         │ │
│ │ - space(complexTable) → _SinglePartitionClause    │ │
│ └─────────────────────────────────────────────────────┘ │
│                      ↓                                 │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ PARTITION 子句 (可选，不可重复)                     │ │
│ │ - parens(consumer) → _SingleIndexHintSpec           │ │
│ └─────────────────────────────────────────────────────┘ │
│                      ↓                                 │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ INDEX HINT 子句 (可选，可重复)                        │ │
│ │                                                         │ │
│ │ 【USE INDEX】                                        │ │
│ │ - useIndex(indexName)                               │ │
│ │ - useIndex(indexName1, indexName2)                   │ │
│ │ - useIndex(indexName1, indexName2, indexName3)         │ │
│ │ - useIndex(consumer)                               │ │
│ │ - useIndex(space, consumer)                          │ │
│ │ - ifUseIndex(consumer)                          │ │
│ │ - useIndex(FOR, JOIN/ORDER_BY/GROUP_BY, indexName)  │ │
│ │ - useIndex(FOR, purpose, consumer)                │ │
│ │ - ifUseIndex(FOR, purpose, consumer)          │ │
│ │                                                         │ │
│ │ 【IGNORE INDEX】                                      │ │
│ │ - ignoreIndex(indexName)                            │ │
│ │ - ignoreIndex(indexName1, indexName2)              │ │
│ │ - ignoreIndex(indexName1, indexName2, indexName3)        │ │
│ │ - ignoreIndex(consumer)                            │ │
│ │ - ignoreIndex(space, consumer)                   │ │
│ │ - ifIgnoreIndex(consumer)                         │ │
│ │ - ignoreIndex(FOR, purpose, indexName)         │ │
│ │ - ifIgnoreIndex(FOR, purpose, consumer)       │ │
│ │                                                         │ │
│ │ 【FORCE INDEX】                                      │ │
│ │ - forceIndex(indexName)                            │ │
│ │ - forceIndex(indexName1, indexName2)              │ │
│ │ - forceIndex(indexName1, indexName2, indexName3)        │ │
│ │ - forceIndex(consumer)                            │ │
│ │ - forceIndex(space, consumer)                       │ │
│ │ - ifForceIndex(consumer)                         │ │
│ │ - forceIndex(FOR, purpose, indexName)          │ │
│ │ - ifForceIndex(FOR, purpose, consumer)        │ │
│ └─────────────────────────────────────────────────────┘ │
│                      ↓                                 │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ SET 子句 (必需，不可重复)                            │ │
│ │                                                         │ │
│ │ 【静态方法】                                          │ │
│ │ - set(field, value) → _SingleWhereSpec               │ │
│ │ - set(field, valueOperator, value)                  │ │
│ │ - set(field, fieldOperator, valueOperator, value)  │ │
│ │ - ifSet(field, value)                             │ │
│ │ - ifSet(field, valueOperator, value)              │ │
│ │ - ifSet(field, fieldOperator, valueOperator, value)│ │
│ │ - setSpace(field, valueOperator)                   │ │
│ │ - setSpace(field, fieldOperator, valueOperator)   │ │
│ │                                                         │ │
│ │ 【动态方法】                                          │ │
│ │ - sets(consumer) → _SingleWhereClause                  │ │
│ └─────────────────────────────────────────────────────┘ │
│                      ↓                                 │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ WHERE 子句 (必需，不可重复)                           │ │
│ │                                                         │ │
│ │ 【静态WHERE】                                          │ │
│ │ - where(predicate) → _SingleWhereAndSpec            │ │
│ │ - where(expOperator, operand)                      │ │
│ │ - where(consumer) → _OrderBySpec                  │ │
│ │                                                         │ │
│ │ 【条件WHERE】                                          │ │
│ │ - whereIf(supplier)                                   │ │
│ │ - whereIf(expOperator, value)                         │ │
│ │ - whereIf(expOperator, operator, value)           │ │
│ │                                                         │ │
│ │ 【AND 子句】(可重复)                                  │ │
│ │ - and(predicate)                                   │ │
│ │ - and(expOperator, operand)                        │ │
│ │ - ifAnd(supplier)                                  │ │
│ │ - ifAnd(expOperator, value)                       │ │
│ │ - ifAnd(expOperator, operand, expOperator2, operand2)  │ │
│ └─────────────────────────────────────────────────────┘ │
│                      ↓                                 │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ ORDER BY 子句 (可选，不可重复)                      │ │
│ │ - orderBy(field) → _OrderByCommaSpec               │ │
│ │ - orderBy(field1, field2)                           │ │
│ │ - orderBy(consumer)                              │ │
│ │                                                         │ │
│ │ 【COMMA 连接】(可重复)                                │ │
│ │ - comma(field)                                     │ │
│ │ - comma(field1, field2)                             │ │
│ │ - comma(consumer)                                │ │
│ └─────────────────────────────────────────────────────┘ │
│                      ↓                                 │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ LIMIT 子句 (可选，不可重复)                           │ │
│ │ - limit(value) → _DmlUpdateSpec                  │ │
│ │ - limit(valueOperator, value)                      │ │
│ │ - limit(valueOperator, value1, valueOperator2, value2)│ │
│ └─────────────────────────────────────────────────────┘ │
│                      ↓                                 │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ 结束语句                                             │ │
│ │ - asUpdate() → _BatchUpdateParamSpec               │ │
│ │                                                         │ │
│ │ 【最终构建】                                          │ │
│ │ - namedParamList(paramList) → BatchUpdate          │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## 详细方法说明

### 1. 入口方法

```java
MySQLUpdate._SingleWithSpec<Statement._BatchUpdateParamSpec> batchSingleUpdate()
```

**说明**: 创建批量单表 UPDATE 语句，支持批量参数绑定。

---

### 2. WITH 子句

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `with(name)` | String name | `_StaticCteParensSpec` | 添加静态 CTE |
| `withRecursive(name)` | String name | `_StaticCteParensSpec` | 添加递归 CTE |
| `with(consumer)` | Consumer&lt;MySQLCtes&gt; | `_SingleUpdateClause` | 动态添加 CTE |
| `withRecursive(consumer)` | Consumer&lt;MySQLCtes&gt; | `_SingleUpdateClause` | 动态添加递归 CTE |

---

### 3. UPDATE 子句

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `update(supplier, modifiers)` | Supplier&lt;List&lt;Hint&gt;&gt;, List&lt;MySQLs.Modifier&gt; | `_SingleUpdateSpaceClause` | 带提示和修饰符 |
| `update(table, AS, alias)` | SingleTableMeta, SQLs.WordAs, String | `_SingleIndexHintSpec` | 更新表并指定别名 |
| `update(complexTable, AS, alias)` | ComplexTableMeta, SQLs.WordAs, String | `_SingleIndexHintSpec` | 更新复杂表 |
| `update(table)` | SingleTableMeta | `_SinglePartitionClause` | 更新表（可指定分区 |
| `update(complexTable)` | ComplexTableMeta | `_SinglePartitionClause` | 更新复杂表（可指定分区） |
| `space(table, AS, alias)` | SingleTableMeta, SQLs.WordAs, String | `_SingleIndexHintSpec` | 使用 space 语法 |
| `space(complexTable, AS, alias)` | ComplexTableMeta, SQLs.WordAs, String | `_SingleIndexHintSpec` | 使用 space 语法（复杂表） |
| `space(table)` | SingleTableMeta | `_SinglePartitionClause` | 使用 space 语法（可指定分区） |
| `space(complexTable)` | ComplexTableMeta | `_SinglePartitionClause` | 使用 space 语法（复杂表，可指定分区） |

---

### 4. INDEX HINT 子句

#### 4.1 USE INDEX

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `useIndex(indexName)` | String | `_SingleIndexHintSpec` | 使用指定索引 |
| `useIndex(indexName1, indexName2)` | String, String | `_SingleIndexHintSpec` | 使用多个索引 |
| `useIndex(indexName1, indexName2, indexName3)` | String, String, String | `_SingleIndexHintSpec` | 使用三个索引 |
| `useIndex(consumer)` | Consumer&lt;Clause._StaticStringSpaceClause&gt; | `_SingleIndexHintSpec` | 动态指定索引 |
| `useIndex(space, consumer)` | SQLs.SymbolSpace, Consumer&lt;Consumer&lt;String&gt;&gt; | `_SingleIndexHintSpec` | 使用 space 语法 |
| `ifUseIndex(consumer)` | Consumer&lt;Consumer&lt;String&gt;&gt; | `_SingleIndexHintSpec` | 条件使用索引 |
| `useIndex(FOR, purpose, indexName)` | SQLs.WordFor, SQLs.IndexHintPurpose, String | `_SingleIndexHintSpec` | 指定用途使用索引 |
| `useIndex(FOR, purpose, consumer)` | SQLs.WordFor, SQLs.IndexHintPurpose, Consumer&lt;Clause._StaticStringSpaceClause&gt; | `_SingleIndexHintSpec` | 指定用途动态使用索引 |
| `ifUseIndex(FOR, purpose, consumer)` | SQLs.WordFor, SQLs.IndexHintPurpose, Consumer&lt;Consumer&lt;String&gt;&gt; | `_SingleIndexHintSpec` | 条件指定用途使用索引 |

#### 4.2 IGNORE INDEX

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `ignoreIndex(indexName)` | String | `_SingleIndexHintSpec` | 忽略指定索引 |
| `ignoreIndex(indexName1, indexName2)` | String, String | `_SingleIndexHintSpec` | 忽略多个索引 |
| `ignoreIndex(indexName1, indexName2, indexName3)` | String, String, String | `_SingleIndexHintSpec` | 忽略三个索引 |
| `ignoreIndex(consumer)` | Consumer&lt;Clause._StaticStringSpaceClause&gt; | `_SingleIndexHintSpec` | 动态忽略索引 |
| `ignoreIndex(space, consumer)` | SQLs.SymbolSpace, Consumer&lt;Consumer&lt;String&gt;&gt; | `_SingleIndexHintSpec` | 使用 space 语法忽略索引 |
| `ifIgnoreIndex(consumer)` | Consumer&lt;Consumer&lt;String&gt;&gt; | `_SingleIndexHintSpec` | 条件忽略索引 |
| `ignoreIndex(FOR, purpose, indexName)` | SQLs.WordFor, SQLs.IndexHintPurpose, String | `_SingleIndexHintSpec` | 指定用途忽略索引 |
| `ignoreIndex(FOR, purpose, consumer)` | SQLs.WordFor, SQLs.IndexHintPurpose, Consumer&lt;Clause._StaticStringSpaceClause&gt; | `_SingleIndexHintSpec` | 指定用途动态忽略索引 |
| `ifIgnoreIndex(FOR, purpose, consumer)` | SQLs.WordFor, SQLs.IndexHintPurpose, Consumer&lt;Consumer&lt;String&gt;&gt; | `_SingleIndexHintSpec` | 条件指定用途忽略索引 |

#### 4.3 FORCE INDEX

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `forceIndex(indexName)` | String | `_SingleIndexHintSpec` | 强制使用指定索引 |
| `forceIndex(indexName1, indexName2)` | String, String | `_SingleIndexHintSpec` | 强制使用多个索引 |
| `forceIndex(indexName1, indexName2, indexName3)` | String, String, String | `_SingleIndexHintSpec` | 强制使用三个索引 |
| `forceIndex(consumer)` | Consumer&lt;Clause._StaticStringSpaceClause&gt; | `_SingleIndexHintSpec` | 动态强制使用索引 |
| `forceIndex(space, consumer)` | SQLs.SymbolSpace, Consumer&lt;Consumer&lt;String&gt;&gt; | `_SingleIndexHintSpec` | 使用 space 语法强制索引 |
| `ifForceIndex(consumer)` | Consumer&lt;Consumer&lt;String&gt;&gt; | `_SingleIndexHintSpec` | 条件强制使用索引 |
| `forceIndex(FOR, purpose, indexName)` | SQLs.WordFor, SQLs.IndexHintPurpose, String | `_SingleIndexHintSpec` | 指定用途强制使用索引 |
| `forceIndex(FOR, purpose, consumer)` | SQLs.WordFor, SQLs.IndexHintPurpose, Consumer&lt;Clause._StaticStringSpaceClause&gt; | `_SingleIndexHintSpec` | 指定用途动态强制使用索引 |
| `ifForceIndex(FOR, purpose, consumer)` | SQLs.WordFor, SQLs.IndexHintPurpose, Consumer&lt;Consumer&lt;String&gt;&gt; | `_SingleIndexHintSpec` | 条件指定用途强制使用索引 |

**注：SQLs.IndexHintPurpose 可选值：`JOIN`, `ORDER_BY`, `GROUP_BY`

---

### 5. SET 子句

#### 5.1 静态 SET 方法

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `set(field, value)` | FieldMeta, Object | `_SingleWhereSpec` | 设置字段值 |
| `set(field, valueOperator, value)` | FieldMeta, BiFunction, Object | `_SingleWhereSpec` | 使用值操作符 |
| `set(field, fieldOperator, valueOperator, value)` | FieldMeta, BiFunction, BiFunction, Object | `_SingleWhereSpec` | 使用字段和值操作符 |
| `ifSet(field, value)` | FieldMeta, Object | `_SingleWhereSpec` | 条件设置(非null时) |
| `ifSet(field, valueOperator, value)` | FieldMeta, BiFunction, Object | `_SingleWhereSpec` | 条件使用值操作符 |
| `ifSet(field, fieldOperator, valueOperator, value)` | FieldMeta, BiFunction, BiFunction, Object | `_SingleWhereSpec` | 条件使用字段和值操作符 |
| `setSpace(field, valueOperator)` | FieldMeta, BiFunction | `_SingleWhereSpec` | 使用空格语法 |
| `setSpace(field, fieldOperator, valueOperator)` | FieldMeta, BiFunction, BiFunction | `_SingleWhereSpec` | 使用字段和空格语法 |

#### 5.2 动态 SET 方法

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|
| `sets(consumer)` | Consumer&lt;UpdateStatement._BatchItemPairs&gt; | `_SingleWhereClause` | 动态设置多个字段 |

---

### 6. WHERE 子句

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `where(predicate)` | IPredicate | `_SingleWhereAndSpec` | WHERE 条件 |
| `where(expOperator, operand)` | Function, Object | `_SingleWhereAndSpec` | 使用表达式操作符 |
| `where(consumer)` | Consumer&lt;Consumer&lt;IPredicate&gt;&gt; | `_OrderBySpec` | 动态 WHERE 条件 |
| `whereIf(supplier)` | BooleanSupplier, Supplier&lt;IPredicate&gt; | `_SingleWhereAndSpec` | 条件 WHERE |
| `whereIf(expOperator, value)` | Function, Object | `_SingleWhereAndSpec` | 条件使用表达式 |
| `whereIf(expOperator, operator, value)` | Function, BiFunction, Object | `_SingleWhereAndSpec` | 条件使用表达式操作符 |
| `and(predicate)` | IPredicate | `_SingleWhereAndSpec` | AND 条件(可重复) |
| `and(expOperator, operand)` | Function, Object | `_SingleWhereAndSpec` | AND 使用表达式 |
| `ifAnd(supplier)` | BooleanSupplier, Supplier&lt;IPredicate&gt; | `_SingleWhereAndSpec` | 条件 AND |
| `ifAnd(expOperator, value)` | Function, Object | `_SingleWhereAndSpec` | 条件 AND 使用表达式 |
| `ifAnd(expOperator, operand, expOperator2, operand2)` | Function, Object, BiFunction, Object | `_SingleWhereAndSpec` | 条件 AND 带第二个操作符 |

---

### 7. ORDER BY 子句

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `orderBy(field)` | FieldMeta | `_OrderByCommaSpec` | 按字段排序 |
| `orderBy(field1, field2)` | FieldMeta, FieldMeta | `_OrderByCommaSpec` | 按多个字段排序 |
| `orderBy(consumer)` | Consumer&lt;Clause._OrderByItemClause&gt; | `_OrderByCommaSpec` | 动态排序 |
| `comma(field)` | FieldMeta | `_OrderByCommaSpec` | 追加排序字段(可重复) |
| `comma(field1, field2)` | FieldMeta, FieldMeta | `_OrderByCommaSpec` | 追加多个排序字段 |
| `comma(consumer)` | Consumer&lt;Clause._OrderByItemClause&gt; | `_OrderByCommaSpec` | 动态追加排序 |

---

### 8. LIMIT 子句

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `limit(value)` | Object | `_DmlUpdateSpec` | 限制行数 |
| `limit(valueOperator, value)` | BiFunction, Object | `_DmlUpdateSpec` | 使用操作符限制行数 |
| `limit(valueOperator, value1, valueOperator2, value2)` | BiFunction, Object, BiFunction, Object | `_DmlUpdateSpec` | 带偏移的限制 |

---

### 9. 结束语句

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `asUpdate()` | - | `_BatchUpdateParamSpec` | 构建为批量更新语句 |
| `namedParamList(paramList)` | List&lt;?&gt; | `BatchUpdate` | 设置批量参数列表 |

---

## 可重复性总结

| 子句 | 可重复 | 说明 |
|------|--------|------|
| WITH | ✅ | 可添加多个 CTE |
| UPDATE | ❌ | 只能有一个 UPDATE |
| PARTITION | ❌ | 最多一个 PARTITION |
| INDEX HINT | ✅ | 可多个索引提示 |
| SET | ❌ | 只能有一个 SET 子句 |
| WHERE | ❌ | 只能有一个 WHERE |
| AND | ✅ | 可多个 AND |
| ORDER BY | ❌ | 只能有一个 ORDER BY |
| COMMA(ORDER BY) | ✅ | 可多个排序字段 |
| LIMIT | ❌ | 只能有一个 LIMIT |

---

## 完整示例代码

### 示例 1: 基础批量更新

```java
import io.army.criteria.*;
import io.army.criteria.impl.MySQLs;
import static io.army.criteria.impl.SQLs.*;

public void basicBatchUpdate(SyncLocalSession session, List<User> users) {
    BatchUpdate stmt = MySQLs.batchSingleUpdate()
        .update(UserMeta.TABLE, AS, "u")
        .set(UserMeta.NAME, SQLs::param, "name")
        .set(UserMeta.AGE, SQLs::param, "age")
        .where(UserMeta.ID.equal(SQLs::param, "id"))
        .asUpdate()
        .namedParamList(users);
    
    List<Long> rowList = session.batchUpdate(stmt);
    System.out.println("Updated rows: " + rowList);
}
```

### 示例 2: 带索引提示的批量更新

```java
public void batchUpdateWithIndexHint(SyncLocalSession session, List<UpdateParam> params) {
    BatchUpdate stmt = MySQLs.batchSingleUpdate()
        .update(UserMeta.TABLE, AS, "u")
        .useIndex(FOR, ORDER_BY, "idx_create_time")
        .set(UserMeta.STATUS, SQLs::literal, UserStatus.ACTIVE)
        .set(UserMeta.LAST_LOGIN, SQLs::param, "lastLogin")
        .where(UserMeta.ID.in(SQLs::param, "ids"))
        .orderBy(UserMeta.CREATE_TIME.desc())
        .asUpdate()
        .namedParamList(params);
    
    session.batchUpdate(stmt);
}
```

### 示例 3: 条件 SET 子句

```java
public void dynamicBatchUpdate(SyncLocalSession session, List<UserUpdate> updates) {
    BatchUpdate stmt = MySQLs.batchSingleUpdate()
        .update(UserMeta.TABLE, AS, "u")
        .sets(pairs -> {
            pairs.set(UserMeta.NAME, SQLs::param, "name");
            pairs.set(UserMeta.EMAIL, SQLs::param, "email");
            pairs.ifSet(UserMeta.PHONE, SQLs::param, "phone");
        })
        .where(UserMeta.ID.equal(SQLs::param, "id"))
        .asUpdate()
        .namedParamList(updates);
    
    session.batchUpdate(stmt);
}
```

### 示例 4: 带 WITH 子句的批量更新

```java
public void batchUpdateWithCte(SyncLocalSession session, List<UpdateRequest> requests) {
    BatchUpdate stmt = MySQLs.batchSingleUpdate()
        .with("recent_orders", cte -> cte
            .as(MySQLs.query()
                .select("o", OrderMeta.ID, OrderMeta.USER_ID)
                .from(OrderMeta.TABLE, AS, "o")
                .where(OrderMeta.CREATE_TIME.greater(SQLs::param, "since"))
                .asQuery()))
        .update(UserMeta.TABLE, AS, "u")
        .set(UserMeta.ORDER_COUNT, UserMeta.ORDER_COUNT.add(SQLs::literal, 1))
        .where(UserMeta.ID.in(MySQLs.subQuery()
            .select(s -> s.space(SQLs.refField("recent_orders", "user_id")))
        .asUpdate()
        .namedParamList(requests);
    
    session.batchUpdate(stmt);
}
```

### 示例 5: 带 ORDER BY 和 LIMIT 的批量更新

```java
public void batchUpdateWithOrderLimit(SyncLocalSession session, List<LimitParam> params) {
    BatchUpdate stmt = MySQLs.batchSingleUpdate()
        .update(UserMeta.TABLE, AS, "u")
        .set(UserMeta.VIP_LEVEL, SQLs::literal, VipLevel.GOLD)
        .where(UserMeta.POINTS.greater(SQLs::param, "minPoints"))
        .orderBy(UserMeta.POINTS.desc())
        .limit(SQLs::param, "limit")
        .asUpdate()
        .namedParamList(params);
    
    session.batchUpdate(stmt);
}
```

### 示例 6: 使用修饰符和提示的批量更新

```java
public void batchUpdateWithModifier(SyncLocalSession session, List<SafeUpdate> updates) {
    BatchUpdate stmt = MySQLs.batchSingleUpdate()
        .update(() -> Collections.singletonList(MySQLs.setVar("sql_safe_updates=0")), 
                  Collections.singletonList(MySQLs.LOW_PRIORITY))
        .space(UserMeta.TABLE, AS, "u")
        .set(UserMeta.BALANCE, UserMeta.BALANCE.add(SQLs::param, "amount"))
        .where(UserMeta.ID.equal(SQLs::param, "id"))
        .asUpdate()
        .namedParamList(updates);
    
    session.batchUpdate(stmt);
}
```

---

## 参数映射说明

批量更新时，参数列表中的对象需要与 SQL 中的参数名对应：

```java
// 参数对象示例
public class UserUpdate {
    private Long id;           // 对应 "id"
    private String name;       // 对应 "name"
    private String email;      // 对应 "email"
    
    // getters and setters
}
```

---

## 注意事项

1. **WHERE 子句必需**: DML 语句必须有 WHERE 子句，防止全表更新
2. **参数列表**: 使用 `namedParamList()` 设置批量参数
3. **表别名**: UPDATE 中的表建议使用别名
4. **字段可更新**: 只能更新标记为可更新的字段
5. **索引提示**: 合理使用索引提示优化查询性能
6. **修饰符**: MySQL 支持 LOW_PRIORITY、IGNORE 等修饰符

---

## 相关接口

- `MySQLUpdate._SingleWithSpec` - 起始接口
- `MySQLUpdate._SingleIndexHintSpec` - 索引提示接口
- `MySQLUpdate._SingleSetClause` - SET 子句接口
- `MySQLUpdate._SingleWhereClause` - WHERE 子句接口
- `MySQLUpdate._OrderBySpec` - ORDER BY 子句接口
- `MySQLUpdate._LimitSpec` - LIMIT 子句接口
- `Statement._BatchUpdateParamSpec` - 批量参数接口
- `BatchUpdate` - 最终批量更新语句
