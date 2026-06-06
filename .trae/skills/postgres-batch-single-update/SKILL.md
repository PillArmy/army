---
name: postgres-batch-single-update
description: 完整的 Army Criteria API Postgres.batchSingleUpdate() 方法链知识。用于理解、解释或记录从 Postgres.batchSingleUpdate() 到 asUpdate()/asReturningUpdate() 再到 namedParamList() 的完整批量 UPDATE 语句构建流程，涵盖 PostgreSQL 特有的 WITH、UPDATE、SET、FROM、JOIN、WHERE、RETURNING 等全部特性。
---

# Postgres.batchSingleUpdate() 方法链完整指南

## 概述

`Postgres.batchSingleUpdate()` 是 Army 框架中用于构建 PostgreSQL 批量单表 UPDATE 语句的入口方法。本指南详细介绍了其完整的方法链、参数说明、使用场景和示例代码。该方法支持 PostgreSQL 特有特性，如 WITH 子句（CTE）、FROM 子句（关联其他表）、RETURNING 子句（返回更新后的数据）、ONLY 修饰符、TABLESAMPLE 等。

**核心源码参考**：
- `Postgres.java` (第 254-258 行)：入口方法定义
- `PostgreUpdates.java`：实现类

---

## 完整方法链图

```
Postgres.batchSingleUpdate()
    ↓
┌──────────────────────────────────────────────────────────────────────────────────┐
│ _SingleWithSpec<_BatchUpdateParamSpec, _BatchReturningUpdateParamSpec>       │
│                                                                              │
│ ┌──────────────────────────────────────────────────────────────────────────┐ │
│ │ WITH 子句 (可选，可重复)                                                │ │
│ │ ├─ with(name) → _StaticCteParensSpec → as(...) → _CteComma          │ │
│ │ │   ├─ comma(name) (重复追加 CTE)                                      │ │
│ │ │   └─ space() (结束 WITH，进入 UPDATE)                                │ │
│ │ ├─ withRecursive(name) → _StaticCteParensSpec → as(...) → _CteComma │ │
│ │ ├─ with(Consumer<PostgreCtes>) → _SingleUpdateClause                │ │
│ │ ├─ withRecursive(Consumer<PostgreCtes>) → _SingleUpdateClause       │ │
│ │ └─ ifWith/ifWithRecursive(Consumer<PostgreCtes>)                     │ │
│ └──────────────────────────────────────────────────────────────────────────┘ │
│                                    ↓                                           │
│ ┌──────────────────────────────────────────────────────────────────────────┐ │
│ │ UPDATE 子句 (必需，不可重复)                                             │ │
│ │ ├─ update(table, AS, alias) → _SingleSetClause                        │ │
│ │ ├─ update(ONLY, table, AS, alias) → _SingleSetClause                  │ │
│ │ └─ update(table, ASTERISK, AS, alias) → _SingleSetClause              │ │
│ └──────────────────────────────────────────────────────────────────────────┘ │
│                                    ↓                                           │
│ ┌──────────────────────────────────────────────────────────────────────────┐ │
│ │ SET 子句 (必需，可重复调用 set* 方法)                                   │ │
│ │ ┌────────────────────────────────────────────────────────────────────┐ │ │
│ │ │ 静态 SET 方法                                                      │ │ │
│ │ │ ├─ set(field, value) → _SingleSetFromSpec                        │ │ │
│ │ │ ├─ set(field, valueOperator, value) → _SingleSetFromSpec        │ │ │
│ │ │ ├─ set(field, fieldOperator, valueOperator, value)               │ │ │
│ │ │ ├─ ifSet(field, value) → _SingleSetFromSpec                      │ │ │
│ │ │ ├─ ifSet(field, valueOperator, value) → _SingleSetFromSpec      │ │ │
│ │ │ ├─ setSpace(field, valueOperator) → _SingleSetFromSpec          │ │ │
│ │ │ └─ setSpace(field, fieldOperator, valueOperator)                │ │ │
│ │ ├────────────────────────────────────────────────────────────────────┤ │ │
│ │ │ 行 SET 方法                                                        │ │ │
│ │ │ ├─ setRow(field1, field2, subQuery) → _SingleSetFromSpec         │ │ │
│ │ │ ├─ setRow(field1, field2, field3, subQuery)                      │ │ │
│ │ │ ├─ setRow(List<field>, subQuery)                                 │ │ │
│ │ │ └─ ifSetRow(...) → _SingleSetFromSpec                            │ │ │
│ │ ├────────────────────────────────────────────────────────────────────┤ │ │
│ │ │ 动态 SET 方法                                                      │ │ │
│ │ │ └─ sets(Consumer<_BatchRowPairs<FieldMeta>>) → _SingleFromSpec   │ │ │
│ │ └────────────────────────────────────────────────────────────────────┘ │ │
│ └──────────────────────────────────────────────────────────────────────────┘ │
│                                    ↓                                           │
│ ┌──────────────────────────────────────────────────────────────────────────┐ │
│ │ FROM 子句 (可选，PostgreSQL 特有)                                       │ │
│ │ ├─ from(table) → _TableSampleJoinSpec                                 │ │ │
│ │ ├─ from(table, AS, alias) → _TableSampleJoinSpec                    │ │ │
│ │ ├─ from(subQuery, AS, alias) → _AsClause<_ParensJoinSpec>           │ │ │
│ │ ├─ from(DerivedTable) → _AsClause<_ParensJoinSpec>                   │ │ │
│ │ ├─ from(UndoneFunction) → _FuncColumnDefinitionAsClause             │ │ │
│ │ └─ from(Consumer) → _SingleJoinSpec                                  │ │ │
│ │                                                                          │ │
│ │ ┌────────────────────────────────────────────────────────────────────┐ │ │
│ │ │ TABLESAMPLE (可选)                                                  │ │ │
│ │ │ ├─ tableSample(Expression) → _RepeatableJoinClause              │ │ │
│ │ │ ├─ tableSample(method, valueOperator, value)                    │ │ │
│ │ │ └─ ifTableSample(...) → _RepeatableJoinClause                   │ │ │
│ │ ├────────────────────────────────────────────────────────────────────┤ │ │
│ │ │ REPEATABLE (可选)                                                  │ │ │
│ │ │ ├─ repeatable(Expression) → _SingleJoinSpec                    │ │ │
│ │ │ ├─ repeatable(valueOperator, value)                             │ │ │
│ │ │ └─ ifRepeatable(...) → _SingleJoinSpec                          │ │ │
│ │ ├────────────────────────────────────────────────────────────────────┤ │ │
│ │ │ JOIN 操作 (可重复)                                                 │ │ │
│ │ │ ├─ join(...) → _TableSampleOnSpec → on(...) → _SingleJoinSpec  │ │ │
│ │ │ ├─ leftJoin(...) → _TableSampleOnSpec → on(...) → _SingleJoinSpec│ │ │
│ │ │ ├─ rightJoin(...) → _TableSampleOnSpec → on(...) → _SingleJoinSpec││ │
│ │ │ ├─ fullJoin(...) → _TableSampleOnSpec → on(...) → _SingleJoinSpec ││ │
│ │ │ ├─ crossJoin(...) → _SingleJoinSpec                              │ │ │
│ │ │ └─ ifJoin/ifLeftJoin/... (Consumer<PostgreJoins>)                  │ │ │
│ │ └────────────────────────────────────────────────────────────────────┘ │ │
│ └──────────────────────────────────────────────────────────────────────────┘ │
│                                    ↓                                           │
│ ┌──────────────────────────────────────────────────────────────────────────┐ │
│ │ WHERE 子句 (可选)                                                        │ │
│ │ ┌────────────────────────────────────────────────────────────────────┐ │ │
│ │ │ 静态 WHERE                                                          │ │ │
│ │ │ ├─ where(predicate) → _SingleWhereAndSpec                        │ │ │
│ │ │ ├─ where(expOperator, operand) → _SingleWhereAndSpec            │ │ │
│ │ │ ├─ whereCurrentOf(cursorName) → _ReturningSpec                  │ │ │
│ │ │ └─ whereIf(...) → _SingleWhereAndSpec                             │ │ │
│ │ ├────────────────────────────────────────────────────────────────────┤ │ │
│ │ │ 动态 WHERE                                                          │ │ │
│ │ │ └─ where(Consumer<Consumer<IPredicate>>) → _ReturningSpec        │ │ │
│ │ └────────────────────────────────────────────────────────────────────┘ │ │
│ └──────────────────────────────────────────────────────────────────────────┘ │
│                                    ↓                                           │
│ ┌──────────────────────────────────────────────────────────────────────────┐ │
│ │ AND 子句 (可选，可重复，仅在静态 WHERE 后可用)                            │ │
│ │ ├─ and(predicate) → _SingleWhereAndSpec                              │ │ │
│ │ ├─ and(expOperator, operand) → _SingleWhereAndSpec                  │ │ │
│ │ └─ ifAnd(...) → _SingleWhereAndSpec                                   │ │ │
│ └──────────────────────────────────────────────────────────────────────────┘ │
│                                    ↓                                           │
│ ┌──────────────────────────────────────────────────────────────────────────┐ │
│ │ RETURNING 子句 (可选，PostgreSQL 特有)                                  │ │
│ │ ┌────────────────────────────────────────────────────────────────────┐ │ │
│ │ │ 静态 RETURNING                                                      │ │ │
│ │ │ ├─ returningAll() → _DqlUpdateSpec<_BatchReturningUpdateParamSpec>│ │ │
│ │ │ ├─ returning(selection) → _StaticReturningCommaSpec              │ │ │
│ │ │ ├─ returning(selection1, selection2) → _StaticReturningCommaSpec│ │ │
│ │ │ ├─ returning(Function<String, Selection>, alias)                 │ │ │
│ │ │ ├─ returning(alias, DOT, ASTERISK)                                │ │ │
│ │ │ ├─ returning(alias, DOT, table)                                  │ │ │
│ │ │ └─ returning(TableField, ...)                                    │ │ │
│ │ ├────────────────────────────────────────────────────────────────────┤ │ │
│ │ │ 动态 RETURNING                                                      │ │ │
│ │ │ └─ returning(Consumer<Returnings>) → _DqlUpdateSpec              │ │ │
│ │ ├────────────────────────────────────────────────────────────────────┤ │ │
│ │ │ 逗号追加 (可重复)                                                    │ │ │
│ │ │ └─ comma(...) → _StaticReturningCommaSpec                        │ │ │
│ │ └────────────────────────────────────────────────────────────────────┘ │ │
│ └──────────────────────────────────────────────────────────────────────────┘ │
│                                    ↓                                           │
│ ┌──────────────────────────────────────────────────────────────────────────┐ │
│ │ 结束语句                                                                │ │
│ │ ┌────────────────────────────────────────────────────────────────────┐ │ │
│ │ │ 普通 UPDATE 路径                                                     │ │ │
│ │ │ └─ asUpdate() → _BatchUpdateParamSpec                              │ │ │
│ │ │                              ↓                                      │ │ │
│ │ │                              └─ namedParamList(List) → BatchUpdate │ │ │
│ │ ├────────────────────────────────────────────────────────────────────┤ │ │
│ │ │ RETURNING UPDATE 路径                                               │ │ │
│ │ │ └─ asReturningUpdate() → _BatchReturningUpdateParamSpec          │ │ │
│ │ │                              ↓                                      │ │ │
│ │ │                              └─ namedParamList(List) → BatchReturningUpdate│ │
│ │ └────────────────────────────────────────────────────────────────────┘ │ │
│ └──────────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 核心接口详解

### 1. 入口方法

```java
// Postgres.java line 254-258
PostgreUpdate._SingleWithSpec<
    _BatchUpdateParamSpec,
    _BatchReturningUpdateParamSpec
> batchSingleUpdate()
```

**返回类型**：`PostgreUpdate._SingleWithSpec<_BatchUpdateParamSpec, _BatchReturningUpdateParamSpec>`

**实现类**：`PostgreUpdates.PrimaryBatchUpdateClause`

---

### 2. WITH 子句

#### 接口定义

```java
// 来自 _PostgreDynamicWithClause 和 PostgreQuery._PostgreStaticWithClause
```

#### 多种调用形式

| 方法 | 说明 |
|------|------|
| `with(name)` | 静态 WITH，指定 CTE 名称 |
| `withRecursive(name)` | 递归 WITH |
| `with(Consumer<PostgreCtes>)` | 动态 WITH，通过 Consumer 构建多个 CTE |
| `withRecursive(Consumer<PostgreCtes>)` | 动态递归 WITH |
| `ifWith(Consumer<PostgreCtes>)` | 条件 WITH |
| `ifWithRecursive(Consumer<PostgreCtes>)` | 条件递归 WITH |

#### 静态 WITH 使用示例

```java
Postgres.batchSingleUpdate()
    .with("recent_orders")
    .parens("id", "user_id", "amount")
    .as(q -> q.select(OrderMeta.ID, OrderMeta.USER_ID, OrderMeta.AMOUNT)
             .from(OrderMeta.TABLE, AS, "o")
             .where(OrderMeta.CREATE_TIME.greater(SQLs::param, "since"))
             .asQuery())
    .comma("active_users")
    .as(q -> q.select(UserMeta.ID)
             .from(UserMeta.TABLE, AS, "u")
             .where(UserMeta.STATUS.equal(SQLs::literal, UserStatus.ACTIVE))
             .asQuery())
    .space()
    .update(UserMeta.TABLE, AS, "u")
    // ...
```

#### 动态 WITH 使用示例

```java
Postgres.batchSingleUpdate()
    .with(ctes -> {
        ctes.comma("recent_orders")
            .parens("id", "user_id")
            .as(q -> q.select(OrderMeta.ID, OrderMeta.USER_ID)
                     .from(OrderMeta.TABLE, AS, "o")
                     .asQuery());
        
        ctes.comma("user_stats")
            .as(q -> q.select(...)
                     .asQuery());
    })
    .update(UserMeta.TABLE, AS, "u")
    // ...
```

---

### 3. UPDATE 子句

#### 接口定义

```java
// PostgreUpdate.java
<T> _SingleSetClause<I, Q, T> update(TableMeta<T> table, SQLs.WordAs as, String tableAlias);
<T> _SingleSetClause<I, Q, T> update(@Nullable SQLs.WordOnly only, TableMeta<T> table, 
                                      SQLs.WordAs as, String tableAlias);
<T> _SingleSetClause<I, Q, T> update(TableMeta<T> table, @Nullable SQLs.SymbolAsterisk star,
                                      SQLs.WordAs as, String tableAlias);
```

#### 多种调用形式

| 方法 | 说明 |
|------|------|
| `update(table, AS, alias)` | 基础 UPDATE，指定表和别名 |
| `update(ONLY, table, AS, alias)` | 带 ONLY 修饰符，仅更新基表，不更新继承表 |
| `update(table, ASTERISK, AS, alias)` | 带星号修饰符 |

#### 使用示例

```java
// 基础形式
Postgres.batchSingleUpdate()
    .update(UserMeta.TABLE, AS, "u")
    // ...

// 带 ONLY 修饰符
Postgres.batchSingleUpdate()
    .update(Postgres.ONLY, UserMeta.TABLE, AS, "u")
    // ...
```

---

### 4. SET 子句

#### 接口层次

```java
// 继承自 UpdateStatement._StaticBatchRowSetClause 和 _DynamicSetClause
```

#### 静态 SET 方法

| 方法 | 说明 |
|------|------|
| `set(field, value)` | 简单赋值 |
| `set(field, valueOperator, value)` | 使用值操作符赋值 |
| `set(field, fieldOperator, valueOperator, value)` | 使用字段和值操作符 |
| `ifSet(field, value)` | 条件赋值 |
| `ifSet(field, valueOperator, value)` | 条件使用值操作符 |
| `setSpace(field, valueOperator)` | 批量参数赋值 |
| `setSpace(field, fieldOperator, valueOperator)` | 带操作符的批量参数赋值 |

#### 行 SET 方法

| 方法 | 说明 |
|------|------|
| `setRow(field1, field2, subQuery)` | 使用子查询同时更新多个字段 |
| `setRow(field1, field2, field3, subQuery)` | 更新 3 个字段 |
| `setRow(field1, field2, field3, field4, subQuery)` | 更新 4 个字段 |
| `setRow(List<field>, subQuery)` | 使用字段列表 |
| `setRow(Consumer<Consumer<Field>>, subQuery)` | 动态选择字段 |
| `ifSetRow(...)` | 条件行 SET |

#### 动态 SET 方法

| 方法 | 说明 |
|------|------|
| `sets(Consumer<_BatchRowPairs<FieldMeta>>)` | 通过 Consumer 动态构建多个赋值 |

#### 使用示例

```java
// 简单批量 SET
Postgres.batchSingleUpdate()
    .update(UserMeta.TABLE, AS, "u")
    .set(UserMeta.NAME, SQLs::param, "name")
    .set(UserMeta.EMAIL, SQLs::param, "email")
    .where(UserMeta.ID.equal(SQLs::param, "id"))
    // ...

// 使用 setSpace
Postgres.batchSingleUpdate()
    .update(UserMeta.TABLE, AS, "u")
    .setSpace(UserMeta.NAME, SQLs::namedParam)
    .setSpace(UserMeta.AGE, SQLs::plusEqual, SQLs::namedParam)
    .where(UserMeta.ID.equal(SQLs::namedParam))
    // ...

// 操作符赋值
Postgres.batchSingleUpdate()
    .update(UserMeta.TABLE, AS, "u")
    .set(UserMeta.BALANCE, SQLs::plusEqual, SQLs::param, "amount")
    .where(UserMeta.ID.equal(SQLs::param, "id"))
    // ...

// 动态 SET
Postgres.batchSingleUpdate()
    .update(UserMeta.TABLE, AS, "u")
    .sets(pairs -> {
        pairs.set(UserMeta.NAME, SQLs::param, "name");
        pairs.set(UserMeta.EMAIL, SQLs::param, "email");
        pairs.ifSet(UserMeta.PHONE, SQLs::param, "phone");
    })
    .where(UserMeta.ID.equal(SQLs::param, "id"))
    // ...
```

---

### 5. FROM 子句 (PostgreSQL 特有)

这是 PostgreSQL 最强大的特性之一，允许在 UPDATE 中关联其他表。

#### 多种调用形式

| 方法 | 说明 |
|------|------|
| `from(table)` | 从表关联 |
| `from(table, AS, alias)` | 从表关联并指定别名 |
| `from(subQuery, AS, alias)` | 从子查询关联 |
| `from(DerivedTable)` | 从派生表关联 |
| `from(UndoneFunction)` | 从表函数关联 |
| `from(Consumer)` | 动态构建 FROM |

#### TABLESAMPLE 选项

| 方法 | 说明 |
|------|------|
| `tableSample(Expression)` | 指定采样方法 |
| `tableSample(method, valueOperator, value)` | 使用值操作符指定采样参数 |
| `tableSample(method, valueOperator, Supplier<value>)` | 使用 Supplier |
| `tableSample(method, valueOperator, Function, keyName)` | 使用 key-value |
| `ifTableSample(...)` | 条件 TABLESAMPLE |

#### REPEATABLE 选项

| 方法 | 说明 |
|------|------|
| `repeatable(Expression)` | 设置可重复种子 |
| `repeatable(valueOperator, value)` | 使用值操作符 |
| `repeatable(Supplier)` | 使用 Supplier |
| `ifRepeatable(...)` | 条件 REPEATABLE |

#### JOIN 操作

| 方法 | 说明 |
|------|------|
| `join(table)` | 内连接 |
| `join(table, AS, alias)` | 内连接并指定别名 |
| `leftJoin(table)` | 左连接 |
| `rightJoin(table)` | 右连接 |
| `fullJoin(table)` | 全连接 |
| `crossJoin(table)` | 交叉连接 |
| `join(subQuery, AS, alias)` | 连接子查询 |
| `join(DerivedTable)` | 连接派生表 |
| `join(UndoneFunction)` | 连接表函数 |
| `ifJoin(Consumer<PostgreJoins>)` | 条件 JOIN |
| `ifLeftJoin(Consumer<PostgreJoins>)` | 条件左连接 |

#### ON 子句

| 方法 | 说明 |
|------|------|
| `on(predicate)` | 指定连接条件 |
| `on(expOperator, operand)` | 使用表达式操作符 |
| `onIf(Supplier<predicate>)` | 条件 ON |

#### 使用示例

```java
// 简单 FROM + JOIN
Postgres.batchSingleUpdate()
    .update(UserMeta.TABLE, AS, "u")
    .set(UserMeta.TOTAL_ORDERS, UserMeta.TOTAL_ORDERS.plus(SQLs::literal, 1))
    .from(OrderMeta.TABLE, AS, "o")
    .where(UserMeta.ID.equal(SQLs.refField("o", "user_id")))
    .where(SQLs.refField("o", "status").equal(SQLs::param, "status"))
    // ...

// 带 TABLESAMPLE
Postgres.batchSingleUpdate()
    .update(UserMeta.TABLE, AS, "u")
    .set(UserMeta.RANDOM_SCORE, SQLs::param, "score")
    .from(LargeTableMeta.TABLE, AS, "lt")
    .tableSample(SQLs::literal, "BERNOULLI", SQLs::literal, 10)
    .repeatable(SQLs::literal, 12345)
    .where(LargeTableMeta.USER_ID.equal(UserMeta.ID))
    // ...

// 多 JOIN
Postgres.batchSingleUpdate()
    .update(UserMeta.TABLE, AS, "u")
    .set(UserMeta.LATEST_ORDER_DATE, SQLs.refField("o", "create_time"))
    .from(OrderMeta.TABLE, AS, "o")
    .leftJoin(PaymentMeta.TABLE, AS, "p")
    .on(PaymentMeta.ORDER_ID.equal(OrderMeta.ID))
    .where(UserMeta.ID.equal(OrderMeta.USER_ID))
    .where(OrderMeta.CREATE_TIME.greater(SQLs::param, "startDate"))
    // ...
```

---

### 6. WHERE 子句

#### 多种调用形式

| 方法 | 说明 |
|------|------|
| `where(predicate)` | 静态 WHERE 条件 |
| `where(expOperator, operand)` | 使用表达式操作符 |
| `whereCurrentOf(cursorName)` | 基于游标位置更新 |
| `whereIf(Supplier<predicate>)` | 条件 WHERE |
| `whereIf(expOperator, value)` | 条件使用表达式 |
| `where(Consumer<Consumer<IPredicate>>)` | 动态 WHERE |
| `ifWhere(Consumer<Consumer<IPredicate>>)` | 条件动态 WHERE |

#### 使用示例

```java
// 静态 WHERE
Postgres.batchSingleUpdate()
    .update(UserMeta.TABLE, AS, "u")
    .set(UserMeta.STATUS, SQLs::param, "status")
    .where(UserMeta.ID.equal(SQLs::param, "id"))
    // ...

// 多 AND 条件
Postgres.batchSingleUpdate()
    .update(UserMeta.TABLE, AS, "u")
    .set(UserMeta.STATUS, SQLs::param, "status")
    .where(UserMeta.ID.equal(SQLs::param, "id"))
    .and(UserMeta.CREATE_TIME.greater(SQLs::param, "startDate"))
    .and(UserMeta.STATUS.notEqual(SQLs::param, "excludeStatus"))
    // ...

// 动态 WHERE
Postgres.batchSingleUpdate()
    .update(UserMeta.TABLE, AS, "u")
    .set(UserMeta.STATUS, SQLs::param, "status")
    .where(predicates -> {
        predicates.accept(UserMeta.ID.equal(SQLs::param, "id"));
        predicates.accept(UserMeta.CREATE_TIME.greater(SQLs::param, "startDate"));
    })
    // ...
```

---

### 7. RETURNING 子句 (PostgreSQL 特有)

这是 PostgreSQL 的强大特性，允许 UPDATE 语句返回更新后的数据。

#### 多种调用形式

| 方法 | 说明 |
|------|------|
| `returningAll()` | 返回所有列 |
| `returning(selection)` | 返回指定选择项 |
| `returning(selection1, selection2)` | 返回两个选择项 |
| `returning(Function<String, Selection>, alias)` | 返回派生列 |
| `returning(alias, DOT, ASTERISK)` | 返回指定别名表的所有列 |
| `returning(alias, DOT, table)` | 返回指定表的所有列 |
| `returning(TableField, TableField, TableField)` | 返回多个字段 |
| `returning(TableField, TableField, TableField, TableField)` | 返回 4 个字段 |
| `returning(Consumer<Returnings>)` | 动态选择返回列 |
| `comma(...)` | 追加返回列 |

#### 使用示例

```java
// 返回所有列
Postgres.batchSingleUpdate()
    .update(UserMeta.TABLE, AS, "u")
    .set(UserMeta.STATUS, SQLs::param, "status")
    .where(UserMeta.ID.equal(SQLs::param, "id"))
    .returningAll()
    .asReturningUpdate()
    .namedParamList(updates);

// 返回指定列
Postgres.batchSingleUpdate()
    .update(UserMeta.TABLE, AS, "u")
    .set(UserMeta.STATUS, SQLs::param, "status")
    .where(UserMeta.ID.equal(SQLs::param, "id"))
    .returning(UserMeta.ID, UserMeta.NAME, UserMeta.STATUS)
    .asReturningUpdate()
    .namedParamList(updates);

// 返回关联表列
Postgres.batchSingleUpdate()
    .update(UserMeta.TABLE, AS, "u")
    .set(UserMeta.STATUS, SQLs::param, "status")
    .from(OrderMeta.TABLE, AS, "o")
    .where(UserMeta.ID.equal(OrderMeta.USER_ID))
    .returning(UserMeta.ID, UserMeta.NAME)
    .comma(SQLs.refField("o", "order_no").as("lastOrderNo"))
    .asReturningUpdate()
    .namedParamList(updates);

// 动态返回
Postgres.batchSingleUpdate()
    .update(UserMeta.TABLE, AS, "u")
    .set(UserMeta.STATUS, SQLs::param, "status")
    .where(UserMeta.ID.equal(SQLs::param, "id"))
    .returning(r -> {
        r.accept(UserMeta.ID);
        r.accept(UserMeta.NAME);
        r.accept(UserMeta.STATUS);
        r.accept(UserMeta.UPDATE_TIME);
    })
    .asReturningUpdate()
    .namedParamList(updates);
```

---

### 8. 结束语句

#### asUpdate() 路径

```java
// 普通批量 UPDATE，不返回数据
BatchUpdate stmt = Postgres.batchSingleUpdate()
    // ... 构建语句 ...
    .asUpdate()
    .namedParamList(updates);

// 执行
List<Long> affectedRowsList = session.batchUpdate(stmt);
```

#### asReturningUpdate() 路径

```java
// 带 RETURNING 的批量 UPDATE
BatchReturningUpdate stmt = Postgres.batchSingleUpdate()
    // ... 构建语句，包含 RETURNING ...
    .asReturningUpdate()
    .namedParamList(updates);

// 执行
List<List<Map<String, Object>>> results = session.batchUpdateReturning(stmt);
```

---

## 完整使用示例

### 示例 1: 基础批量更新

```java
import io.army.criteria.*;
import io.army.criteria.impl.Postgres;
import static io.army.criteria.impl.SQLs.*;

public void basicBatchUpdate(SyncLocalSession session, List<UserUpdate> updates) {
    BatchUpdate stmt = Postgres.batchSingleUpdate()
        .update(UserMeta.TABLE, AS, "u")
        .set(UserMeta.NAME, SQLs::param, "name")
        .set(UserMeta.EMAIL, SQLs::param, "email")
        .set(UserMeta.PHONE, SQLs::param, "phone")
        .where(UserMeta.ID.equal(SQLs::param, "id"))
        .asUpdate()
        .namedParamList(updates);
    
    List<Long> affectedRows = session.batchUpdate(stmt);
    System.out.println("Updated rows per batch: " + affectedRows);
}
```

### 示例 2: 使用 setSpace 的批量更新

```java
public void batchUpdateWithSetSpace(SyncLocalSession session, List<Map<String, Object>> params) {
    BatchUpdate stmt = Postgres.batchSingleUpdate()
        .update(UserMeta.TABLE, AS, "u")
        .setSpace(UserMeta.NAME, SQLs::namedParam)
        .setSpace(UserMeta.EMAIL, SQLs::namedParam)
        .setSpace(UserMeta.AGE, SQLs::plusEqual, SQLs::namedParam)
        .where(UserMeta.ID.equal(SQLs::namedParam))
        .asUpdate()
        .namedParamList(params);
    
    session.batchUpdate(stmt);
}
```

### 示例 3: 带 FROM 子句的批量更新

```java
public void batchUpdateWithFrom(SyncLocalSession session, List<OrderUpdate> updates) {
    BatchUpdate stmt = Postgres.batchSingleUpdate()
        .update(UserMeta.TABLE, AS, "u")
        .set(UserMeta.TOTAL_ORDERS, UserMeta.TOTAL_ORDERS.plus(SQLs::param, "orderCount"))
        .set(UserMeta.TOTAL_SPENT, UserMeta.TOTAL_SPENT.plus(SQLs::param, "totalSpent"))
        .from(OrderMeta.TABLE, AS, "o")
        .join(PaymentMeta.TABLE, AS, "p")
        .on(PaymentMeta.ORDER_ID.equal(OrderMeta.ID))
        .where(UserMeta.ID.equal(OrderMeta.USER_ID))
        .and(OrderMeta.ID.equal(SQLs::param, "orderId"))
        .and(PaymentMeta.STATUS.equal(SQLs::literal, PaymentStatus.SUCCESS))
        .asUpdate()
        .namedParamList(updates);
    
    session.batchUpdate(stmt);
}
```

### 示例 4: 带 RETURNING 的批量更新

```java
public void batchUpdateWithReturning(SyncLocalSession session, List<UserUpdate> updates) {
    BatchReturningUpdate stmt = Postgres.batchSingleUpdate()
        .update(UserMeta.TABLE, AS, "u")
        .set(UserMeta.STATUS, SQLs::param, "newStatus")
        .set(UserMeta.UPDATE_TIME, SQLs::literal, LocalDateTime.now())
        .where(UserMeta.ID.equal(SQLs::param, "id"))
        .returning(UserMeta.ID, UserMeta.NAME, UserMeta.STATUS, UserMeta.UPDATE_TIME)
        .asReturningUpdate()
        .namedParamList(updates);
    
    List<List<Map<String, Object>>> results = session.batchUpdateReturning(stmt);
    
    for (List<Map<String, Object>> batchResult : results) {
        for (Map<String, Object> row : batchResult) {
            System.out.println("Updated user: " + row);
        }
    }
}
```

### 示例 5: 带 WITH 子句的批量更新

```java
public void batchUpdateWithCte(SyncLocalSession session, List<UpdateRequest> requests) {
    BatchUpdate stmt = Postgres.batchSingleUpdate()
        .with("recent_orders", cte -> cte
            .parens("user_id", "order_count")
            .as(q -> q.select(OrderMeta.USER_ID, SQLs.count(OrderMeta.ID).as("order_count"))
                     .from(OrderMeta.TABLE, AS, "o")
                     .where(OrderMeta.CREATE_TIME.greater(SQLs::param, "since"))
                     .groupBy(OrderMeta.USER_ID)
                     .asQuery()))
        .update(UserMeta.TABLE, AS, "u")
        .set(UserMeta.ORDER_COUNT, SQLs.refField("ro", "order_count"))
        .from("recent_orders", AS, "ro")
        .where(UserMeta.ID.equal(SQLs.refField("ro", "user_id")))
        .asUpdate()
        .namedParamList(requests);
    
    session.batchUpdate(stmt);
}
```

### 示例 6: 条件 SET 和 WHERE

```java
public void dynamicBatchUpdate(SyncLocalSession session, List<UserUpdate> updates) {
    BatchUpdate stmt = Postgres.batchSingleUpdate()
        .update(UserMeta.TABLE, AS, "u")
        .sets(pairs -> {
            pairs.set(UserMeta.NAME, SQLs::param, "name");
            pairs.ifSet(UserMeta.EMAIL, SQLs::param, "email");
            pairs.ifSet(UserMeta.PHONE, SQLs::param, "phone");
        })
        .where(UserMeta.ID.equal(SQLs::param, "id"))
        .ifAnd(() -> UserMeta.STATUS.equal(SQLs::param, "oldStatus"))
        .asUpdate()
        .namedParamList(updates);
    
    session.batchUpdate(stmt);
}
```

---

## 可重复性总结

| 子句 | 可重复 | 说明 |
|------|--------|------|
| WITH (`.with(name)`) | ❌ | WITH 关键字只能出现一次 |
| CTE (`.comma(name)`) | ✅ | 可以追加多个 CTE |
| UPDATE (`.update()`) | ❌ | 只能更新一个表 |
| SET (`.set()`) | ✅ | 可以设置多个字段 |
| FROM (`.from()`) | ❌ | FROM 关键字只能出现一次 |
| JOIN (`.join()`, `.leftJoin()` 等) | ✅ | 可以 JOIN 多个表 |
| WHERE (`.where()`) | ❌ | WHERE 关键字只能出现一次 |
| AND (`.and()`) | ✅ | 可以追加多个 AND 条件 |
| RETURNING (`.returning()`) | ❌ | RETURNING 关键字只能出现一次 |
| RETURNING comma (`.comma()`) | ✅ | 可以追加多个返回列 |

---

## PostgreSQL 特有特性

1. **WITH 子句 (CTE)**：支持通用表表达式，包括递归 CTE
2. **FROM 子句**：UPDATE 可以关联其他表（与 MySQL 的 UPDATE ... JOIN 不同）
3. **RETURNING 子句**：UPDATE 可以返回更新后的数据
4. **ONLY 修饰符**：仅更新基表，不更新继承表
5. **TABLESAMPLE**：对 FROM 中的表进行采样
6. **REPEATABLE**：设置可重复采样种子

---

## 参数映射说明

批量更新时，参数列表中的对象需要与 SQL 中的参数名对应：

```java
// 参数对象示例
public class UserUpdate {
    private Long id;           // 对应 "id"
    private String name;       // 对应 "name"
    private String email;      // 对应 "email"
    private String phone;      // 对应 "phone"
    
    // getters and setters
}

// 或者使用 Map
Map<String, Object> params = new HashMap<>();
params.put("id", 1L);
params.put("name", "张三");
params.put("email", "zhangsan@example.com");
```

---

## 注意事项

1. **WHERE 子句**：虽然可选，但建议总是添加 WHERE 子句以避免全表更新
2. **参数列表**：使用 `namedParamList()` 设置批量参数，参数名需与 SQL 中的参数名对应
3. **表别名**：UPDATE 中的表建议使用别名，特别是在使用 FROM/JOIN 时
4. **字段可更新**：确保只更新标记为可更新的字段
5. **RETURNING 性能**：RETURNING 会增加网络传输，仅在需要时使用
6. **批量大小**：考虑数据库连接和内存，合理设置批量大小

---

## 相关接口

- `PostgreUpdate._SingleWithSpec` - 起始接口
- `PostgreUpdate._SingleUpdateClause` - UPDATE 子句接口
- `PostgreUpdate._SingleSetClause` - SET 子句接口
- `PostgreUpdate._SingleFromSpec` - FROM 子句接口
- `PostgreUpdate._TableSampleJoinSpec` - TABLESAMPLE 接口
- `PostgreUpdate._RepeatableJoinClause` - REPEATABLE 接口
- `PostgreUpdate._SingleJoinSpec` - JOIN 接口
- `PostgreUpdate._SingleWhereClause` - WHERE 子句接口
- `PostgreUpdate._SingleWhereAndSpec` - WHERE AND 接口
- `PostgreUpdate._ReturningSpec` - RETURNING 接口
- `PostgreUpdate._StaticReturningCommaSpec` - RETURNING 逗号接口
- `Statement._BatchUpdateParamSpec` - 批量参数接口
- `Statement._BatchReturningUpdateParamSpec` - 批量 RETURNING 参数接口
- `BatchUpdate` - 最终批量更新语句
- `BatchReturningUpdate` - 最终批量返回更新语句

---

## 自我进化指南

本技能支持自我进化。如需更新或扩展内容，可以：

1. 添加更多高级场景的示例代码
2. 补充性能优化建议
3. 添加与其他方言（如 MySQL）的对比说明
4. 补充更多 PostgreSQL 特有功能的详细说明
5. 添加更多实际项目中的最佳实践
6. 更新方法链图以适应新版本的 Army 框架
7. 补充更多错误处理和调试技巧
