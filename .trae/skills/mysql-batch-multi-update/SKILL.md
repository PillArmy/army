---
name: "mysql-batch-multi-update"
description: "完整学习和使用 MySQLs.batchMultiUpdate() 方法链。提供完整方法链图、详细说明、可重复性分析、各子句多种形式、示例代码。当需要构建批量多表更新时调用。"
---

# MySQL batchMultiUpdate() 方法链完整指南

## 概述

`MySQLs.batchMultiUpdate()` 是 Army 框架中用于创建 MySQL 批量多表更新语句的入口方法。本指南详细介绍了其完整的方法链、参数说明、可重复性分析、各子句的多种调用形式、使用场景和示例代码。

> **适用范围**: 本技能 **仅限** 用于理解、解释、记录 `MySQLs.batchMultiUpdate()` 入口的 MySQL 特定批量多表 UPDATE 语句构建流程。不涵盖 multiUpdate、singleUpdate、batchSingleUpdate、标准 SQLs 等其他 API。
> **源码依据**: 本技能基于以下核心源文件编写：`MySQLs.java`（入口定义）、`MySQLUpdate.java`（MySQL UPDATE 接口组合）、`MySQLMultiUpdates.java`（实现）、`MultiUpdateTests.java`（示例）。所有描述均以实际源码接口签名和实现为准。

---

## 完整方法链 Diagram

```
MySQLs.batchMultiUpdate() → MySQLUpdate._MultiWithSpec<_BatchUpdateParamSpec>
│
├─① WITH (可选，仅一次声明；comma 追加 CTE 可重复)
│  ├─ 静态 WITH:
│  │  ├─ .with(String name) → MySQLQuery._StaticCteParensSpec
│  │  └─ .withRecursive(String name) → MySQLQuery._StaticCteParensSpec
│  │     └─ (然后: .parens() → .as() → .space() 进入 UPDATE)
│  │
│  └─ 动态 WITH:
│     ├─ .with(Consumer<MySQLCtes> consumer) → MySQLUpdate._MultiUpdateClause<_BatchUpdateParamSpec>
│     ├─ .withRecursive(Consumer<MySQLCtes> consumer) → MySQLUpdate._MultiUpdateClause<_BatchUpdateParamSpec>
│     ├─ .ifWith(Consumer<MySQLCtes> consumer) → MySQLUpdate._MultiUpdateClause<_BatchUpdateParamSpec>
│     └─ .ifWithRecursive(Consumer<MySQLCtes> consumer) → MySQLUpdate._MultiUpdateClause<_BatchUpdateParamSpec>
│
└─② UPDATE (可选，带修饰符和提示)
   └─ .update(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers)
         → MySQLUpdate._MultiUpdateSpaceClause<_BatchUpdateParamSpec>
      │
      └─③ 表源 (必须，仅一次，不可重复)
         ├─ .update(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias)
         │     → MySQLUpdate._MultiIndexHintJoinSpec<_BatchUpdateParamSpec>
         ├─ .update(DerivedTable derivedTable)
         │     → _AsClause<_ParensJoinSpec<_BatchUpdateParamSpec>>
         ├─ .update(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable)
         │     → _AsClause<_ParensJoinSpec<_BatchUpdateParamSpec>>
         ├─ .update(Supplier<T> supplier)
         │     → _AsClause<_ParensJoinSpec<_BatchUpdateParamSpec>>
         ├─ .update(@Nullable SQLs.DerivedModifier modifier, Supplier<T> supplier)
         │     → _AsClause<_ParensJoinSpec<_BatchUpdateParamSpec>>
         ├─ .update(String cteName)
         │     → MySQLUpdate._MultiJoinSpec<_BatchUpdateParamSpec>
         ├─ .update(String cteName, SQLs.WordAs wordAs, String alias)
         │     → MySQLUpdate._MultiJoinSpec<_BatchUpdateParamSpec>
         ├─ .update(Function<MySQLQuery._NestedLeftParenSpec<_MultiJoinSpec<_BatchUpdateParamSpec>>, _MultiJoinSpec<_BatchUpdateParamSpec>> function)
         │     → MySQLUpdate._MultiJoinSpec<_BatchUpdateParamSpec>
         └─ .update(TableMeta<?> table)
               → MySQLUpdate._MultiPartitionJoinClause<_BatchUpdateParamSpec>
         │
         ├─【SPACE 变体】(与 update 语义相同，仅命名不同)
         │  ├─ .space(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias)
         │  ├─ .space(DerivedTable derivedTable)
         │  ├─ .space(@Nullable SQLs.DerivedModifier modifier, DerivedTable derivedTable)
         │  ├─ .space(Supplier<T> supplier)
         │  ├─ .space(@Nullable SQLs.DerivedModifier modifier, Supplier<T> supplier)
         │  ├─ .space(String cteName)
         │  ├─ .space(String cteName, SQLs.WordAs wordAs, String alias)
         │  ├─ .space(Function<MySQLQuery._NestedLeftParenSpec<_MultiJoinSpec<_BatchUpdateParamSpec>>, _MultiJoinSpec<_BatchUpdateParamSpec>> function)
         │  └─ .space(TableMeta<?> table)
         │
         ├─④ INDEX HINT (可选，可重复)
         │  ├─【USE INDEX】
         │  │  ├─ .useIndex(String indexName)
         │  │  ├─ .useIndex(String indexName1, String indexName2)
         │  │  ├─ .useIndex(String indexName1, String indexName2, String indexName3)
         │  │  ├─ .useIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
         │  │  ├─ .useIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
         │  │  ├─ .ifUseIndex(Consumer<Consumer<String>> consumer)
         │  │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
         │  │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
         │  │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
         │  │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
         │  │  ├─ .useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
         │  │  └─ .ifUseIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
         │  │
         │  ├─【IGNORE INDEX】
         │  │  ├─ .ignoreIndex(String indexName)
         │  │  ├─ .ignoreIndex(String indexName1, String indexName2)
         │  │  ├─ .ignoreIndex(String indexName1, String indexName2, String indexName3)
         │  │  ├─ .ignoreIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
         │  │  ├─ .ignoreIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
         │  │  ├─ .ifIgnoreIndex(Consumer<Consumer<String>> consumer)
         │  │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
         │  │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
         │  │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
         │  │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
         │  │  ├─ .ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
         │  │  └─ .ifIgnoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
         │  │
         │  └─【FORCE INDEX】
         │     ├─ .forceIndex(String indexName)
         │     ├─ .forceIndex(String indexName1, String indexName2)
         │     ├─ .forceIndex(String indexName1, String indexName2, String indexName3)
         │     ├─ .forceIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
         │     ├─ .forceIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
         │     ├─ .ifForceIndex(Consumer<Consumer<String>> consumer)
         │     ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
         │     ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
         │     ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
         │     ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
         │     ├─ .forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
         │     └─ .ifForceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
         │
         ├─⑤ JOIN (可选，可重复)
         │  ├─ 嵌套 JOIN:
         │  │  ├─ .join(Function<MySQLQuery._NestedLeftParenSpec<_OnClause<_MultiJoinSpec<_BatchUpdateParamSpec>>>, _OnClause<_MultiJoinSpec<_BatchUpdateParamSpec>>> function)
         │  │  ├─ .leftJoin(Function<MySQLQuery._NestedLeftParenSpec<_OnClause<_MultiJoinSpec<_BatchUpdateParamSpec>>>, _OnClause<_MultiJoinSpec<_BatchUpdateParamSpec>>> function)
         │  │  ├─ .rightJoin(Function<MySQLQuery._NestedLeftParenSpec<_OnClause<_MultiJoinSpec<_BatchUpdateParamSpec>>>, _OnClause<_MultiJoinSpec<_BatchUpdateParamSpec>>> function)
         │  │  ├─ .fullJoin(Function<MySQLQuery._NestedLeftParenSpec<_OnClause<_MultiJoinSpec<_BatchUpdateParamSpec>>>, _OnClause<_MultiJoinSpec<_BatchUpdateParamSpec>>> function)
         │  │  ├─ .straightJoin(Function<MySQLQuery._NestedLeftParenSpec<_OnClause<_MultiJoinSpec<_BatchUpdateParamSpec>>>, _OnClause<_MultiJoinSpec<_BatchUpdateParamSpec>>> function)
         │  │  └─ .crossJoin(Function<MySQLQuery._NestedLeftParenSpec<_MultiJoinSpec<_BatchUpdateParamSpec>>, _MultiJoinSpec<_BatchUpdateParamSpec>> function)
         │  │
         │  ├─ 直接表 JOIN:
         │  │  ├─ .leftJoin(TableMeta<?> table) → MySQLUpdate._MultiPartitionOnClause<_BatchUpdateParamSpec>
         │  │  ├─ .join(TableMeta<?> table) → MySQLUpdate._MultiPartitionOnClause<_BatchUpdateParamSpec>
         │  │  ├─ .rightJoin(TableMeta<?> table) → MySQLUpdate._MultiPartitionOnClause<_BatchUpdateParamSpec>
         │  │  ├─ .fullJoin(TableMeta<?> table) → MySQLUpdate._MultiPartitionOnClause<_BatchUpdateParamSpec>
         │  │  ├─ .straightJoin(TableMeta<?> table) → MySQLUpdate._MultiPartitionOnClause<_BatchUpdateParamSpec>
         │  │  └─ .crossJoin(TableMeta<?> table) → MySQLUpdate._MultiPartitionJoinClause<_BatchUpdateParamSpec>
         │  │
         │  └─ 条件 JOIN:
         │     ├─ .ifCrossJoin(Consumer<MySQLCrosses> consumer)
         │     ├─ .ifLeftJoin(Consumer<MySQLJoins> consumer)
         │     ├─ .ifJoin(Consumer<MySQLJoins> consumer)
         │     ├─ .ifRightJoin(Consumer<MySQLJoins> consumer)
         │     ├─ .ifFullJoin(Consumer<MySQLJoins> consumer)
         │     └─ .ifStraightJoin(Consumer<MySQLJoins> consumer)
         │
         ├─⑥ ON (JOIN 后必须)
         │  └─ .on(...) → MySQLUpdate._MultiIndexHintOnSpec<_BatchUpdateParamSpec>
         │
         └─⑦ SET (必须，至少一次)
            ├─ 静态 SET:
            │  ├─ .set(TableField field, Object value)
            │  ├─ .set(TableField field, BiFunction<TableField, T, Expression> valueOperator, T value)
            │  ├─ .set(TableField field, BiFunction<Expression, T, AssignmentItem> fieldOperator, BiFunction<TableField, T, Expression> valueOperator, T value)
            │  ├─ .ifSet(TableField field, Object value)
            │  ├─ .ifSet(TableField field, BiFunction<TableField, T, Expression> valueOperator, T value)
            │  ├─ .ifSet(TableField field, BiFunction<Expression, T, AssignmentItem> fieldOperator, BiFunction<TableField, T, Expression> valueOperator, T value)
            │  ├─ .setSpace(TableField field, BiFunction<TableField, String, Expression> valueOperator)
            │  └─ .setSpace(TableField field, BiFunction<Expression, Expression, AssignmentItem> fieldOperator, BiFunction<TableField, String, Expression> valueOperator)
            │
            ├─ 动态 SET:
            │  └─ .sets(Consumer<UpdateStatement._BatchItemPairs<TableField>> consumer)
            │
            └─⑧ WHERE (可选，多种形式)
               ├─ 静态 WHERE:
               │  ├─ .where(IPredicate predicate) → MySQLUpdate._MultiWhereAndSpec<_BatchUpdateParamSpec>
               │  ├─ .where(Function<TableField, IPredicate> expOperator, TableField operand)
               │  ├─ .where(Consumer<Consumer<IPredicate>> consumer) → Statement._DmlUpdateSpec<_BatchUpdateParamSpec> (跳过 AND)
               │
               ├─ 条件 WHERE:
               │  ├─ .whereIf(Supplier<IPredicate> supplier)
               │  ├─ .whereIf(Function<TableField, IPredicate> expOperator, Object value)
               │  └─ .whereIf(Function<TableField, IPredicate> expOperator, BiFunction<Expression, T, Expression> operator, T value)
               │
               └─⑨ AND (可选，可重复)
                  ├─ 静态 AND:
                  │  ├─ .and(IPredicate predicate) → MySQLUpdate._MultiWhereAndSpec<_BatchUpdateParamSpec>
                  │  ├─ .and(Function<TableField, IPredicate> expOperator, TableField operand)
                  │
                  ├─ 条件 AND:
                  │  ├─ .ifAnd(Supplier<IPredicate> supplier)
                  │  ├─ .ifAnd(Function<TableField, IPredicate> expOperator, Object value)
                  │  └─ .ifAnd(Function<TableField, IPredicate> expOperator1, Object operand1, Function<TableField, IPredicate> expOperator2, Object operand2)
                  │
                  └─⑩ 结束语句
                     ├─ .asUpdate() → _BatchUpdateParamSpec
                     └─ .namedParamList(List<?> paramList) → BatchUpdate
```

---

## 核心入口

```java
// MySQLs.java line 179-181
public static MySQLUpdate._MultiWithSpec<Statement._BatchUpdateParamSpec> batchMultiUpdate() {
    return MySQLMultiUpdates.batch();
}
```

**返回类型**: `MySQLUpdate._MultiWithSpec<_BatchUpdateParamSpec>` - 实现类是 `MySQLMultiUpdates.MySQLBatchUpdate`。

**内部实现**: `MySQLMultiUpdates.batch()`:

```java
// MySQLMultiUpdates.java line 74-76
static _MultiWithSpec<_BatchUpdateParamSpec> batch() {
    return new MySQLBatchUpdate();
}
```

---

## 可重复性总结

| 子句 | 可重复 | 说明 |
|------|--------|------|
| WITH | ✅ | 可添加多个 CTE |
| UPDATE | ❌ | 只能有一个 UPDATE |
| 表源 | ❌ | 只能指定一次表源 |
| PARTITION | ❌ | 最多一个 PARTITION |
| INDEX HINT | ✅ | 可多个索引提示 |
| JOIN | ✅ | 可多个 JOIN |
| ON | ✅ | 每个 JOIN 对应一个 ON |
| SET | ❌ | 只能有一个 SET 子句 |
| WHERE | ❌ | 只能有一个 WHERE |
| AND | ✅ | 可多个 AND |

---

## 详细方法说明

### 1. 入口方法

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `batchMultiUpdate()` | - | `_MultiWithSpec<_BatchUpdateParamSpec>` | 创建批量多表 UPDATE 语句 |

---

### 2. WITH 子句

#### 2.1 静态 CTE

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `with(name)` | String name | `_StaticCteParensSpec` | 添加静态 CTE |
| `withRecursive(name)` | String name | `_StaticCteParensSpec` | 添加递归 CTE |

#### 2.2 动态 CTE

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `with(consumer)` | Consumer&lt;MySQLCtes&gt; | `_MultiUpdateClause` | 动态添加 CTE |
| `withRecursive(consumer)` | Consumer&lt;MySQLCtes&gt; | `_MultiUpdateClause` | 动态添加递归 CTE |
| `ifWith(consumer)` | Consumer&lt;MySQLCtes&gt; | `_MultiUpdateClause` | 条件添加 CTE |
| `ifWithRecursive(consumer)` | Consumer&lt;MySQLCtes&gt; | `_MultiUpdateClause` | 条件添加递归 CTE |

> **语义约束**: WITH 关键字仅出现一次。后续 CTE 通过 comma 追加。space() 是唯一退出 CTE 链、进入 UPDATE 的路径。

---

### 3. UPDATE 子句

#### 3.1 带提示和修饰符

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `update(supplier, modifiers)` | Supplier&lt;List&lt;Hint&gt;&gt;, List&lt;MySQLs.Modifier&gt; | `_MultiUpdateSpaceClause` | 带提示和修饰符 |

**MySQL 支持的修饰符**（来自 MySQLs.java）:
- `MySQLs.LOW_PRIORITY`
- `MySQLs.IGNORE`

**MySQL 支持的提示创建方法**:
- `MySQLs.setVar(String varAssignment)` - 设置 MySQL 变量
- `MySQLs.qbName(String name)` - 查询块名称
- `MySQLs.orderIndex(String name, String tableAlias, List<String> indexList)` - 排序索引提示

#### 3.2 直接表源

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `update(table, AS, alias)` | TableMeta, SQLs.WordAs, String | `_MultiIndexHintJoinSpec` | 更新表并指定别名 |
| `update(derivedTable)` | DerivedTable | `_AsClause<_ParensJoinSpec>` | 使用派生表 |
| `update(modifier, derivedTable)` | @Nullable SQLs.DerivedModifier, DerivedTable | `_AsClause<_ParensJoinSpec>` | 带修饰符的派生表 |
| `update(supplier)` | Supplier&lt;T extends DerivedTable&gt; | `_AsClause<_ParensJoinSpec>` | 使用 Supplier 提供派生表 |
| `update(modifier, supplier)` | @Nullable SQLs.DerivedModifier, Supplier&lt;T&gt; | `_AsClause<_ParensJoinSpec>` | 带修饰符的 Supplier |
| `update(cteName)` | String | `_MultiJoinSpec` | 使用 CTE |
| `update(cteName, AS, alias)` | String, SQLs.WordAs, String | `_MultiJoinSpec` | 使用 CTE 并指定别名 |
| `update(function)` | Function&lt;_NestedLeftParenSpec&lt;_MultiJoinSpec&gt;, _MultiJoinSpec&gt; | `_MultiJoinSpec` | 使用嵌套 join |
| `update(table)` | TableMeta | `_MultiPartitionJoinClause` | 更新表（可指定分区） |

#### 3.3 SPACE 变体

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `space(table, AS, alias)` | TableMeta, SQLs.WordAs, String | `_MultiIndexHintJoinSpec` | 使用 space 语法 |
| `space(derivedTable)` | DerivedTable | `_AsClause<_ParensJoinSpec>` | 使用 space 语法（派生表） |
| `space(modifier, derivedTable)` | @Nullable SQLs.DerivedModifier, DerivedTable | `_AsClause<_ParensJoinSpec>` | space 语法带修饰符 |
| `space(supplier)` | Supplier&lt;T extends DerivedTable&gt; | `_AsClause<_ParensJoinSpec>` | space 语法 Supplier |
| `space(modifier, supplier)` | @Nullable SQLs.DerivedModifier, Supplier&lt;T&gt; | `_AsClause<_ParensJoinSpec>` | space 语法带修饰符的 Supplier |
| `space(cteName)` | String | `_MultiJoinSpec` | space 语法 CTE |
| `space(cteName, AS, alias)` | String, SQLs.WordAs, String | `_MultiJoinSpec` | space 语法 CTE 带别名 |
| `space(function)` | Function&lt;_NestedLeftParenSpec&lt;_MultiJoinSpec&gt;, _MultiJoinSpec&gt; | `_MultiJoinSpec` | space 语法嵌套 join |
| `space(table)` | TableMeta | `_MultiPartitionJoinClause` | space 语法可指定分区 |

> **规则**:
> - **必须调用一次，且仅能调用一次**
> - 支持多种表源：TableMeta、DerivedTable、CTE、嵌套 join
> - SQLs.WordAs 固定传入 SQLs.AS
> - tableAlias 必须提供非空字符串

---

### 4. PARTITION 子句

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `partition(partitionName)` | String | `_PartitionAsClause` | 指定单分区 |
| `partition(partitionName1, partitionName2)` | String, String | `_PartitionAsClause` | 指定双分区 |
| `partition(partitionName1, partitionName2, partitionName3)` | String, String, String | `_PartitionAsClause` | 指定三分区 |
| `partition(consumer)` | Consumer&lt;_StaticStringSpaceClause&gt; | `_PartitionAsClause` | 动态指定分区 |
| `ifPartition(consumer)` | Consumer&lt;Consumer&lt;String&gt;&gt; | `_PartitionAsClause` | 条件指定分区 |

然后继续调用:

| 方法 | 参数 | 返回类型 |
|------|------|----------|
| `as(alias)` | String | `_MultiIndexHintJoinSpec` / `_MultiIndexHintOnSpec` |

---

### 5. INDEX HINT 子句

#### 5.1 USE INDEX

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `useIndex(indexName)` | String | `_MultiIndexHintJoinSpec` | 使用指定索引 |
| `useIndex(indexName1, indexName2)` | String, String | `_MultiIndexHintJoinSpec` | 使用多个索引 |
| `useIndex(indexName1, indexName2, indexName3)` | String, String, String | `_MultiIndexHintJoinSpec` | 使用三个索引 |
| `useIndex(consumer)` | Consumer&lt;_StaticStringSpaceClause&gt; | `_MultiIndexHintJoinSpec` | 动态指定索引 |
| `useIndex(space, consumer)` | SQLs.SymbolSpace, Consumer&lt;Consumer&lt;String&gt;&gt; | `_MultiIndexHintJoinSpec` | 使用 space 语法 |
| `ifUseIndex(consumer)` | Consumer&lt;Consumer&lt;String&gt;&gt; | `_MultiIndexHintJoinSpec` | 条件使用索引 |
| `useIndex(FOR, purpose, indexName)` | SQLs.WordFor, SQLs.IndexHintPurpose, String | `_MultiIndexHintJoinSpec` | 指定用途使用索引 |
| `useIndex(FOR, purpose, indexName1, indexName2)` | SQLs.WordFor, SQLs.IndexHintPurpose, String, String | `_MultiIndexHintJoinSpec` | 指定用途使用多个索引 |
| `useIndex(FOR, purpose, indexName1, indexName2, indexName3)` | SQLs.WordFor, SQLs.IndexHintPurpose, String, String, String | `_MultiIndexHintJoinSpec` | 指定用途使用三个索引 |
| `useIndex(FOR, purpose, consumer)` | SQLs.WordFor, SQLs.IndexHintPurpose, Consumer&lt;_StaticStringSpaceClause&gt; | `_MultiIndexHintJoinSpec` | 指定用途动态使用索引 |
| `useIndex(FOR, purpose, space, consumer)` | SQLs.WordFor, SQLs.IndexHintPurpose, SQLs.SymbolSpace, Consumer&lt;Consumer&lt;String&gt;&gt; | `_MultiIndexHintJoinSpec` | 指定用途使用 space 语法 |
| `ifUseIndex(FOR, purpose, consumer)` | SQLs.WordFor, SQLs.IndexHintPurpose, Consumer&lt;Consumer&lt;String&gt;&gt; | `_MultiIndexHintJoinSpec` | 条件指定用途使用索引 |

#### 5.2 IGNORE INDEX

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `ignoreIndex(indexName)` | String | `_MultiIndexHintJoinSpec` | 忽略指定索引 |
| `ignoreIndex(indexName1, indexName2)` | String, String | `_MultiIndexHintJoinSpec` | 忽略多个索引 |
| `ignoreIndex(indexName1, indexName2, indexName3)` | String, String, String | `_MultiIndexHintJoinSpec` | 忽略三个索引 |
| `ignoreIndex(consumer)` | Consumer&lt;_StaticStringSpaceClause&gt; | `_MultiIndexHintJoinSpec` | 动态忽略索引 |
| `ignoreIndex(space, consumer)` | SQLs.SymbolSpace, Consumer&lt;Consumer&lt;String&gt;&gt; | `_MultiIndexHintJoinSpec` | 使用 space 语法忽略索引 |
| `ifIgnoreIndex(consumer)` | Consumer&lt;Consumer&lt;String&gt;&gt; | `_MultiIndexHintJoinSpec` | 条件忽略索引 |
| `ignoreIndex(FOR, purpose, indexName)` | SQLs.WordFor, SQLs.IndexHintPurpose, String | `_MultiIndexHintJoinSpec` | 指定用途忽略索引 |
| `ignoreIndex(FOR, purpose, indexName1, indexName2)` | SQLs.WordFor, SQLs.IndexHintPurpose, String, String | `_MultiIndexHintJoinSpec` | 指定用途忽略多个索引 |
| `ignoreIndex(FOR, purpose, indexName1, indexName2, indexName3)` | SQLs.WordFor, SQLs.IndexHintPurpose, String, String, String | `_MultiIndexHintJoinSpec` | 指定用途忽略三个索引 |
| `ignoreIndex(FOR, purpose, consumer)` | SQLs.WordFor, SQLs.IndexHintPurpose, Consumer&lt;_StaticStringSpaceClause&gt; | `_MultiIndexHintJoinSpec` | 指定用途动态忽略索引 |
| `ignoreIndex(FOR, purpose, space, consumer)` | SQLs.WordFor, SQLs.IndexHintPurpose, SQLs.SymbolSpace, Consumer&lt;Consumer&lt;String&gt;&gt; | `_MultiIndexHintJoinSpec` | 指定用途使用 space 语法忽略索引 |
| `ifIgnoreIndex(FOR, purpose, consumer)` | SQLs.WordFor, SQLs.IndexHintPurpose, Consumer&lt;Consumer&lt;String&gt;&gt; | `_MultiIndexHintJoinSpec` | 条件指定用途忽略索引 |

#### 5.3 FORCE INDEX

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `forceIndex(indexName)` | String | `_MultiIndexHintJoinSpec` | 强制使用指定索引 |
| `forceIndex(indexName1, indexName2)` | String, String | `_MultiIndexHintJoinSpec` | 强制使用多个索引 |
| `forceIndex(indexName1, indexName2, indexName3)` | String, String, String | `_MultiIndexHintJoinSpec` | 强制使用三个索引 |
| `forceIndex(consumer)` | Consumer&lt;_StaticStringSpaceClause&gt; | `_MultiIndexHintJoinSpec` | 动态强制使用索引 |
| `forceIndex(space, consumer)` | SQLs.SymbolSpace, Consumer&lt;Consumer&lt;String&gt;&gt; | `_MultiIndexHintJoinSpec` | 使用 space 语法强制索引 |
| `ifForceIndex(consumer)` | Consumer&lt;Consumer&lt;String&gt;&gt; | `_MultiIndexHintJoinSpec` | 条件强制使用索引 |
| `forceIndex(FOR, purpose, indexName)` | SQLs.WordFor, SQLs.IndexHintPurpose, String | `_MultiIndexHintJoinSpec` | 指定用途强制使用索引 |
| `forceIndex(FOR, purpose, indexName1, indexName2)` | SQLs.WordFor, SQLs.IndexHintPurpose, String, String | `_MultiIndexHintJoinSpec` | 指定用途强制使用多个索引 |
| `forceIndex(FOR, purpose, indexName1, indexName2, indexName3)` | SQLs.WordFor, SQLs.IndexHintPurpose, String, String, String | `_MultiIndexHintJoinSpec` | 指定用途强制使用三个索引 |
| `forceIndex(FOR, purpose, consumer)` | SQLs.WordFor, SQLs.IndexHintPurpose, Consumer&lt;_StaticStringSpaceClause&gt; | `_MultiIndexHintJoinSpec` | 指定用途动态强制使用索引 |
| `forceIndex(FOR, purpose, space, consumer)` | SQLs.WordFor, SQLs.IndexHintPurpose, SQLs.SymbolSpace, Consumer&lt;Consumer&lt;String&gt;&gt; | `_MultiIndexHintJoinSpec` | 指定用途使用 space 语法强制索引 |
| `ifForceIndex(FOR, purpose, consumer)` | SQLs.WordFor, SQLs.IndexHintPurpose, Consumer&lt;Consumer&lt;String&gt;&gt; | `_MultiIndexHintJoinSpec` | 条件指定用途强制使用索引 |

**注：SQLs.IndexHintPurpose 可选值：**
- `SQLs.JOIN`
- `SQLs.ORDER_BY`
- `SQLs.GROUP_BY`

---

### 6. JOIN 子句

#### 6.1 嵌套 JOIN

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `join(function)` | Function&lt;_NestedLeftParenSpec&lt;_OnClause&lt;_MultiJoinSpec&gt;&gt;, _OnClause&lt;_MultiJoinSpec&gt;&gt; | `_OnClause<_MultiJoinSpec>` | 内连接（嵌套） |
| `leftJoin(function)` | Function&lt;_NestedLeftParenSpec&lt;_OnClause&lt;_MultiJoinSpec&gt;&gt;, _OnClause&lt;_MultiJoinSpec&gt;&gt; | `_OnClause<_MultiJoinSpec>` | 左连接（嵌套） |
| `rightJoin(function)` | Function&lt;_NestedLeftParenSpec&lt;_OnClause&lt;_MultiJoinSpec&gt;&gt;, _OnClause&lt;_MultiJoinSpec&gt;&gt; | `_OnClause<_MultiJoinSpec>` | 右连接（嵌套） |
| `fullJoin(function)` | Function&lt;_NestedLeftParenSpec&lt;_OnClause&lt;_MultiJoinSpec&gt;&gt;, _OnClause&lt;_MultiJoinSpec&gt;&gt; | `_OnClause<_MultiJoinSpec>` | 全连接（嵌套） |
| `straightJoin(function)` | Function&lt;_NestedLeftParenSpec&lt;_OnClause&lt;_MultiJoinSpec&gt;&gt;, _OnClause&lt;_MultiJoinSpec&gt;&gt; | `_OnClause<_MultiJoinSpec>` | 强制连接顺序（嵌套） |
| `crossJoin(function)` | Function&lt;_NestedLeftParenSpec&lt;_MultiJoinSpec&gt;, _MultiJoinSpec&gt; | `_MultiJoinSpec` | 笛卡尔积（嵌套，无 ON） |

#### 6.2 直接表 JOIN

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `join(table)` | TableMeta | `_MultiPartitionOnClause` | 内连接 |
| `leftJoin(table)` | TableMeta | `_MultiPartitionOnClause` | 左连接 |
| `rightJoin(table)` | TableMeta | `_MultiPartitionOnClause` | 右连接 |
| `fullJoin(table)` | TableMeta | `_MultiPartitionOnClause` | 全连接 |
| `straightJoin(table)` | TableMeta | `_MultiPartitionOnClause` | 强制连接顺序 |
| `crossJoin(table)` | TableMeta | `_MultiPartitionJoinClause` | 笛卡尔积（无 ON） |

#### 6.3 条件 JOIN

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `ifCrossJoin(consumer)` | Consumer&lt;MySQLCrosses&gt; | `_MultiJoinSpec` | 条件笛卡尔积 |
| `ifLeftJoin(consumer)` | Consumer&lt;MySQLJoins&gt; | `_MultiJoinSpec` | 条件左连接 |
| `ifJoin(consumer)` | Consumer&lt;MySQLJoins&gt; | `_MultiJoinSpec` | 条件内连接 |
| `ifRightJoin(consumer)` | Consumer&lt;MySQLJoins&gt; | `_MultiJoinSpec` | 条件右连接 |
| `ifFullJoin(consumer)` | Consumer&lt;MySQLJoins&gt; | `_MultiJoinSpec` | 条件全连接 |
| `ifStraightJoin(consumer)` | Consumer&lt;MySQLJoins&gt; | `_MultiJoinSpec` | 条件强制连接顺序 |

---

### 7. ON 子句

JOIN 后必须紧跟 ON 子句，用于指定连接条件。

```java
.on(ChinaRegion_.id::equal, ChinaProvince_.regionId)
```

---

### 8. SET 子句

#### 8.1 静态 SET 方法

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `set(field, value)` | TableField, Object | `_MultiWhereSpec` | 设置字段值 |
| `set(field, valueOperator, value)` | TableField, BiFunction, Object | `_MultiWhereSpec` | 使用值操作符 |
| `set(field, fieldOperator, valueOperator, value)` | TableField, BiFunction, BiFunction, Object | `_MultiWhereSpec` | 使用字段和值操作符 |
| `ifSet(field, value)` | TableField, Object | `_MultiWhereSpec` | 条件设置(非null时) |
| `ifSet(field, valueOperator, value)` | TableField, BiFunction, Object | `_MultiWhereSpec` | 条件使用值操作符 |
| `ifSet(field, fieldOperator, valueOperator, value)` | TableField, BiFunction, BiFunction, Object | `_MultiWhereSpec` | 条件使用字段和值操作符 |
| `setSpace(field, valueOperator)` | TableField, BiFunction | `_MultiWhereSpec` | 使用空格语法 |
| `setSpace(field, fieldOperator, valueOperator)` | TableField, BiFunction, BiFunction | `_MultiWhereSpec` | 使用字段和空格语法 |

#### 8.2 动态 SET 方法

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `sets(consumer)` | Consumer&lt;UpdateStatement._BatchItemPairs&lt;TableField&gt;&gt; | `_MultiWhereSpec` | 动态设置多个字段 |

---

### 9. WHERE 子句

#### 9.1 静态 WHERE

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `where(predicate)` | IPredicate | `_MultiWhereAndSpec` | WHERE 条件 |
| `where(expOperator, operand)` | Function, Object | `_MultiWhereAndSpec` | 使用表达式操作符 |
| `where(consumer)` | Consumer&lt;Consumer&lt;IPredicate&gt;&gt; | `_DmlUpdateSpec` | 动态 WHERE 条件(跳过 AND) |

#### 9.2 条件 WHERE

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `whereIf(supplier)` | BooleanSupplier, Supplier&lt;IPredicate&gt; | `_MultiWhereAndSpec` | 条件 WHERE |
| `whereIf(expOperator, value)` | Function, Object | `_MultiWhereAndSpec` | 条件使用表达式 |
| `whereIf(expOperator, operator, value)` | Function, BiFunction, Object | `_MultiWhereAndSpec` | 条件使用表达式操作符 |

---

### 10. AND 子句

#### 10.1 静态 AND

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `and(predicate)` | IPredicate | `_MultiWhereAndSpec` | AND 条件(可重复) |
| `and(expOperator, operand)` | Function, Object | `_MultiWhereAndSpec` | AND 使用表达式 |

#### 10.2 条件 AND

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `ifAnd(supplier)` | BooleanSupplier, Supplier&lt;IPredicate&gt; | `_MultiWhereAndSpec` | 条件 AND |
| `ifAnd(expOperator, value)` | Function, Object | `_MultiWhereAndSpec` | 条件 AND 使用表达式 |
| `ifAnd(expOperator1, operand1, expOperator2, operand2)` | Function, Object, Function, Object | `_MultiWhereAndSpec` | 条件 AND 带第二个操作符 |

---

### 11. 结束语句

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|----------|------|
| `asUpdate()` | - | `_BatchUpdateParamSpec` | 构建为批量更新语句 |
| `namedParamList(paramList)` | List&lt;?&gt; | `BatchUpdate` | 设置批量参数列表 |

---

## 完整示例代码

### 示例 1: 基础批量多表更新

```java
import io.army.criteria.*;
import io.army.criteria.impl.MySQLs;
import static io.army.criteria.impl.SQLs.*;

public void basicBatchMultiUpdate(SyncLocalSession session, List<Map<String, Long>> params) {
    BatchUpdate stmt = MySQLs.batchMultiUpdate()
        .update(ChinaProvince_.T, AS, "p")
        .join(ChinaRegion_.T, AS, "r").on(ChinaRegion_.id::equal, ChinaProvince_.regionId)
        .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, "gdpAmount")
        .where(ChinaRegion_.id::equal, SQLs::namedParam, "regionId")
        .asUpdate()
        .namedParamList(params);
    
    List<Long> rowList = session.batchUpdate(stmt);
    System.out.println("Updated rows: " + rowList);
}
```

### 示例 2: 带 CTE 的批量多表更新

```java
public void batchMultiUpdateWithCte(SyncLocalSession session, List<Map<String, Long>> params) {
    BatchUpdate stmt = MySQLs.batchMultiUpdate()
        .with("cte").as(sw -> sw
            .select(ChinaRegion_.id)
            .from(ChinaRegion_.T, AS, "c")
            .where(ChinaRegion_.id::spaceEqual, SQLs::namedParam, "regionId")
            .and(ChinaRegion_.regionType.equal(SQLs::param, "regionType"))
            .asQuery())
        .space()
        .update(ChinaProvince_.T, AS, "p").useIndex(FOR, JOIN, "PRIMARY")
        .join(ChinaRegion_.T, AS, "r").useIndex(FOR, JOIN, "PRIMARY").on(ChinaRegion_.id::equal, ChinaProvince_.regionId)
        .join("cte").on(ChinaRegion_.id::equal, SQLs.refField("cte", ChinaRegion_.ID))
        .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, "gdpAmount")
        .where(ChinaRegion_.id::equal, SQLs.scalarSubQuery()
            .select(s -> s.space(SQLs.refField("cte", ChinaRegion_.ID)))
            .from("cte", AS, "subCte")
            .asQuery())
        .asUpdate()
        .namedParamList(params);
    
    session.batchUpdate(stmt);
}
```

### 示例 3: 动态 SET 子句

```java
public void dynamicBatchMultiUpdate(SyncLocalSession session, List<Map<String, Object>> params) {
    BatchUpdate stmt = MySQLs.batchMultiUpdate()
        .update(PillUser_.T).partition("p1").as("u").useIndex(FOR, JOIN, "PRIMARY")
        .join(BankAccount_.T, AS, "a").ignoreIndex(FOR, JOIN, "idx_account_id").on(PillUser_.id::equal, BankAccount_.id)
        .sets(pairs -> {
            pairs.setSpace(PillUser_.nickName, SQLs::namedParam, "nickName");
            pairs.setSpace(BankAccount_.balance, SQLs::plusEqual, SQLs::namedParam, "balanceDelta");
        })
        .whereIf(PillUser_.identityId::equal, SQLs::literal, "identityId")
        .ifAnd(PillUser_.nickName::equal, SQLs::literal, "oldNickName")
        .ifAnd(BankAccount_.createTime::between, SQLs::literal, "startTime", AND, "endTime")
        .ifAnd(BankAccount_.version::equal, SQLs::literal, "version")
        .asUpdate()
        .namedParamList(params);
    
    session.batchUpdate(stmt);
}
```

### 示例 4: 带修饰符和提示的批量多表更新

```java
public void batchMultiUpdateWithModifier(SyncLocalSession session, List<Map<String, Object>> params) {
    Supplier<List<Hint>> hintSupplier = () -> {
        List<Hint> hintList = new ArrayList<>();
        hintList.add(MySQLs.qbName("region_update"));
        hintList.add(MySQLs.orderIndex("region_update", "r", Collections.singletonList("PRIMARY")));
        return hintList;
    };
    
    BatchUpdate stmt = MySQLs.batchMultiUpdate()
        .update(hintSupplier, Arrays.asList(MySQLs.LOW_PRIORITY, MySQLs.IGNORE))
        .space(ChinaProvince_.T, AS, "p")
        .join(ChinaRegion_.T, AS, "r").on(ChinaRegion_.id::equal, ChinaProvince_.regionId)
        .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, "gdpAmount")
        .where(ChinaRegion_.id.in(SQLs::param, "regionIds"))
        .asUpdate()
        .namedParamList(params);
    
    session.batchUpdate(stmt);
}
```

### 示例 5: 使用空间语法和命名参数

```java
public void batchMultiUpdateWithSpace(SyncLocalSession session, List<Map<String, Object>> params) {
    BatchUpdate stmt = MySQLs.batchMultiUpdate()
        .update(User_.T, AS, "u")
        .join(Order_.T, AS, "o").on(User_.id::equal, Order_.userId)
        .setSpace(User_.totalOrders, SQLs::plusEqual, SQLs::namedParam, "orderIncrement")
        .setSpace(Order_.status, SQLs::param, "newStatus")
        .where(Order_.id::equal, SQLs::namedParam, "orderId")
        .and(User_.id::equal, SQLs::namedParam, "userId")
        .asUpdate()
        .namedParamList(params);
    
    session.batchUpdate(stmt);
}
```

---

## 参数映射说明

批量更新时，参数列表中的对象需要与 SQL 中的参数名对应：

```java
// 参数对象示例 (使用 Map)
Map<String, Object> paramMap = new HashMap<>();
paramMap.put("regionId", 123L);          // 对应 "regionId"
paramMap.put("gdpAmount", new BigDecimal("10000.00"));  // 对应 "gdpAmount"
paramMap.put("regionType", RegionType.PROVINCE);  // 对应 "regionType"

// 或者使用 POJO
public class RegionUpdateParam {
    private Long regionId;           // 对应 "regionId"
    private BigDecimal gdpAmount;    // 对应 "gdpAmount"
    private RegionType regionType;   // 对应 "regionType"
    
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
7. **JOIN 顺序**: 多表更新时注意 JOIN 顺序
8. **表源唯一性**: UPDATE 或 space 只能调用一次

---

## 相关接口

- `MySQLUpdate._MultiWithSpec` - 起始接口
- `MySQLUpdate._MultiUpdateClause` - UPDATE 子句接口
- `MySQLUpdate._MultiIndexHintJoinSpec` - 索引提示接口
- `MySQLUpdate._MultiJoinSpec` - JOIN 接口
- `MySQLUpdate._MultiSetClause` - SET 子句接口
- `MySQLUpdate._MultiWhereClause` - WHERE 子句接口
- `Statement._DmlUpdateSpec` - DML 更新规范
- `Statement._BatchUpdateParamSpec` - 批量参数接口
- `BatchUpdate` - 最终批量更新语句

