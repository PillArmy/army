
---
name: SQLs.batchSingleDelete() 方法链
description: 完整的 Army Criteria API SQLs.batchSingleDelete() 方法链知识。用于理解、解释或记录从 SQLs.batchSingleDelete() 到 namedParamList() 的完整批量 DELETE 语句构建流程。
---

# SQLs.batchSingleDelete() 方法链 — 完整参考

&gt; **适用范围**: 本技能 **仅限** 用于理解、解释、记录 `SQLs.batchSingleDelete()` 入口的标准批量 DELETE 语句构建流程。覆盖从 `SQLs.batchSingleDelete()` 到 `namedParamList()` 的完整方法链，包括 WITH、DELETE FROM、WHERE、AND、asDelete()、namedParamList() 等阶段。
&gt; **不** 涵盖 singleDelete、domainDelete、batchDomainDelete、方言特有语法。

&gt; **源码依据**: 本技能基于以下核心源文件编写——`Statement.java`（接口定义）、`StandardDelete.java`（标准 DELETE 接口组合）、`StandardDeletes.java`（实现）、`SingleDeleteStatement.java`（核心实现）、`StandardDeleteUnitTests.java`（示例）。所有描述均以实际源码接口签名和实现为准。

---

## 核心入口

```java
// SQLs.java line 440-443
public static StandardDelete._WithSpec&lt;Statement._BatchDeleteParamSpec&gt; batchSingleDelete() {
    return StandardDeletes.batchSingleDelete(StandardDialect.STANDARD20);
}
```

**返回类型**: `StandardDelete._WithSpec&lt;Statement._BatchDeleteParamSpec&gt;` — 实现类是 `StandardDeletes.StandardBatchDelete`。

**内部实现**: `StandardDeletes.batchSingleDelete()`:

```java
// StandardDeletes.java line 51-53
static _WithSpec&lt;_BatchDeleteParamSpec&gt; batchSingleDelete(StandardDialect dialect) {
    return new StandardBatchDelete(dialect, null);
}
```

---

## 完整方法链 Diagram

&gt; **阅读指南**: 每个叶子节点代表一个可直接调用的方法，带完整参数列表。
&gt; 接口链通过返回类型导航。

```
SQLs.batchSingleDelete() → StandardDelete._WithSpec&lt;_BatchDeleteParamSpec&gt;
│
├─① WITH (可选，仅一次声明；comma 追加 CTE 可重复)
│  ├─ .with(Consumer&lt;StandardCtes&gt; consumer)       → _StandardDeleteClause&lt;_BatchDeleteParamSpec&gt;
│  ├─ .withRecursive(Consumer&lt;StandardCtes&gt; consumer) → _StandardDeleteClause&lt;_BatchDeleteParamSpec&gt;
│  ├─ .ifWith(Consumer&lt;StandardCtes&gt; consumer)      → _StandardDeleteClause&lt;_BatchDeleteParamSpec&gt;
│  ├─ .ifWithRecursive(Consumer&lt;StandardCtes&gt; consumer) → _StandardDeleteClause&lt;_BatchDeleteParamSpec&gt;
│  └─ (静态 CTE 方式: 同 query 的 with(name) → parens() → as() → space()，但 DELETE 使用较少)
│
└─② DELETE FROM (必须，仅一次，不可重复)
   └─ .deleteFrom(SingleTableMeta&lt;?&gt; table, SQLs.WordAs as, String tableAlias) → _WhereSpec&lt;_BatchDeleteParamSpec&gt;
      │
      └─③ WHERE (可选)
         ├─ .where(IPredicate predicate)                  → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
         ├─ .where(Function&lt;T, IPredicate&gt; expOperator, T operand) → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
         ├─ .whereIf(Supplier&lt;IPredicate&gt; supplier)          → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
         ├─ .whereIf(Function&lt;T, IPredicate&gt; expOperator, @Nullable T value) → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
         ├─ .whereIf(ExpressionOperator&lt;TypedExpression, T, IPredicate&gt; expOp,
                     BiFunction&lt;TypedExpression, T, Expression&gt; operator,
                     @Nullable T value)                        → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
         ├─ .whereIf(BiFunction&lt;SQLs.BiOperator, T, IPredicate&gt; expOp,
                     SQLs.BiOperator operator, @Nullable T value) → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
         ├─ .whereIf(TeFunction&lt;SQLs.BiOperator, BiFunction&lt;TypedExpression, T, Expression&gt;,
                     T, IPredicate&gt; expOp, SQLs.BiOperator op,
                     BiFunction&lt;TypedExpression, T, Expression&gt; func,
                     @Nullable T value)                        → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
         ├─ .whereIf(BetweenValueOperator&lt;T&gt; expOp,
                     BiFunction&lt;TypedExpression, T, Expression&gt; operator,
                     @Nullable T value1, SQLs.WordAnd and, @Nullable T value2) → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
         ├─ .whereIf(BetweenDualOperator&lt;T, U&gt; expOp,
                     BiFunction&lt;TypedExpression, T, Expression&gt; firstFuncRef,
                     @Nullable T first, SQLs.WordAnd and,
                     BiFunction&lt;TypedExpression, U, Expression&gt; secondFuncRef,
                     @Nullable U second)                        → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
         ├─ .where(Consumer&lt;Consumer&lt;IPredicate&gt;&gt; consumer) → _DmlDeleteSpec&lt;_BatchDeleteParamSpec&gt; (直接跳过 AND)
         ├─ .ifWhere(Consumer&lt;Consumer&lt;IPredicate&gt;&gt; consumer) → _DmlDeleteSpec&lt;_BatchDeleteParamSpec&gt; (条件 + 跳过 AND)
         │
         └─④ AND (可选，可重复)
            ├─ .and(IPredicate predicate)                  → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
            ├─ .and(Function&lt;T, IPredicate&gt; expOperator, T operand) → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
            ├─ .ifAnd(Supplier&lt;IPredicate&gt; supplier)          → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
            ├─ .ifAnd(Function&lt;T, IPredicate&gt; expOperator, @Nullable T value) → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
            ├─ .ifAnd(ExpressionOperator&lt;TypedExpression, T, IPredicate&gt; expOp,
                       BiFunction&lt;TypedExpression, T, Expression&gt; operator,
                       @Nullable T value)                        → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
            ├─ .ifAnd(BiFunction&lt;SQLs.BiOperator, T, IPredicate&gt; expOp,
                       SQLs.BiOperator operator, @Nullable T value) → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
            ├─ .ifAnd(TeFunction&lt;SQLs.BiOperator, BiFunction&lt;TypedExpression, T, Expression&gt;,
                       T, IPredicate&gt; expOp, SQLs.BiOperator op,
                       BiFunction&lt;TypedExpression, T, Expression&gt; func,
                       @Nullable T value)                        → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
            ├─ .ifAnd(BetweenValueOperator&lt;T&gt; expOp,
                       BiFunction&lt;TypedExpression, T, Expression&gt; operator,
                       @Nullable T value1, SQLs.WordAnd and, @Nullable T value2) → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
            └─ .ifAnd(BetweenDualOperator&lt;T, U&gt; expOp,
                       BiFunction&lt;TypedExpression, T, Expression&gt; firstFuncRef,
                       @Nullable T first, SQLs.WordAnd and,
                       BiFunction&lt;TypedExpression, U, Expression&gt; secondFuncRef,
                       @Nullable U second)                        → _WhereAndSpec&lt;_BatchDeleteParamSpec&gt;
            │
            └─⑤ 结束: asDelete()
               └─ .asDelete() → _BatchDeleteParamSpec (NOT BatchDelete !)
                  │
                  └─⑥ 必须: namedParamList(paramList)
                     └─ .namedParamList(List&lt;?&gt; paramList) → BatchDelete (可执行的批量 DELETE 语句对象)
```

---

## 逐层接口详解

### 0. 入口: `StandardDelete._WithSpec&lt;_BatchDeleteParamSpec&gt;`

```java
// StandardDelete.java line 58-63
interface _WithSpec&lt;I extends Item&gt;
        extends _StandardDynamicWithClause&lt;_StandardDeleteClause&lt;I&gt;&gt;,  // with(Consumer)
                _StandardStaticWithClause&lt;_StandardDeleteClause&lt;I&gt;&gt;,    // with(name)
                _StandardDeleteClause&lt;I&gt; {                              // DELETE FROM
}
```

`_WithSpec` 组合了三种能力：动态 CTE、静态 CTE、DELETE FROM。所以 `SQLs.batchSingleDelete()` 返回的对象可以直接 `.deleteFrom()`，也可以先用 `.with()` 定义 CTE。

---

### ① WITH (Common Table Expression)

&gt; **语义约束**: `WITH` 关键字仅出现一次（即 `.with(name)` / `.withRecursive(name)` 只能调用一次）。
&gt; 后续 CTE 通过 `_CteComma.comma(String name)` 追加。
&gt; `.space()` 是唯一退出 CTE 链、进入 DELETE FROM 的路径。
&gt; **禁止** 在 `.space()` 后再调用 `.with(name)`——类型系统不提供此路径。

**接口**: `_StandardDynamicWithClause` + `_StandardStaticWithClause`

**动态 CTE (运行时构建)**:

```java
SQLs.batchSingleDelete()
    .with(builder -&gt; {
        builder.comma("cte1").as(s -&gt; s.select(...).from(...).asQuery());
        builder.comma("cte2").as(s -&gt; s.select(...).from(...).asQuery());
    })
    .deleteFrom(...)
```

**条件 CTE**:

```java
SQLs.batchSingleDelete()
    .ifWith(builder -&gt; { ... })        // 仅当 consumer 内有实际操作时执行
    .ifWithRecursive(builder -&gt; { ... }) // 递归条件 CTE
```

---

### ② DELETE FROM (核心)

```java
// StandardDelete.java line 26-30
interface _DeleteFromClause&lt;R&gt; extends Item {
    R deleteFrom(SingleTableMeta&lt;?&gt; table, SQLs.WordAs as, String tableAlias);
}
```

&gt; **规则**:
&gt; - **必须调用一次，且仅能调用一次**
&gt; - 仅接受 `SingleTableMeta`（标准单表，不支持父子表联合）
&gt; - `SQLs.WordAs` 固定传入 `SQLs.AS`
&gt; - `tableAlias` 必须提供非空字符串
&gt; - 不可重复调用——类型系统和实现都强制执行此约束

---

### ③ WHERE (可选)

#### 接口层

与 `singleDelete()` 完全相同的 `_WhereClause` 接口，包括所有重载形式。

#### 返回值:

- `.where(IPredicate)` / `.where(Function, operand)` → `_WhereAndSpec` (可继续 `.and(...)`)
- `.where(Consumer)` → `_DmlDeleteSpec` (直接跳转到 asDelete，不能再 AND)

#### 批量 DELETE 特殊用法: spaceEqual()

在批量 DELETE 中，WHERE 子句常使用 `space*` 系列方法配合 `SQLs::namedParam`：

```java
// 必须在 batch 场景中使用 namedParam，因为每一行参数值不同
.where(ChinaRegion_.name.spaceEqual(SQLs::namedParam))
```

---

### ④ AND (可选，可重复)

与 `singleDelete()` 完全相同的 `_WhereAndClause` 接口，包括所有重载形式。

---

### ⑤ asDelete() — 中间链点

```java
// Statement.java line 1421-1424
interface _DmlDeleteSpec&lt;I extends Item&gt; extends Item {
    I asDelete();
}
```

```java
// SingleDeleteStatement.java line 128-131
@Override
public final I asDelete() {
    this.endDeleteStatement();
    return this.onAsDelete();
}
```

&gt; **⚠️ 重要**: `batchSingleDelete().asDelete()` **不** 返回 `BatchDelete`！
&gt; 它返回 `_BatchDeleteParamSpec`，必须继续链式调用 `.namedParamList()`。

**实现细节** (`StandardDeletes.java`):

```java
// StandardDeletes.java line 200-203
@Override
_BatchDeleteParamSpec onAsDelete() {
    return this;
}
```

---

### ⑥ namedParamList() — 链尾（必须调用）

```java
// Statement.java
interface _BatchDeleteParamSpec extends Item {
    BatchDelete namedParamList(List&lt;?&gt; paramList);
}
```

**实现** (`StandardDeletes.java`):

```java
// StandardDeletes.java line 183-189
@Override
public BatchDelete namedParamList(final List&lt;?&gt; paramList) {
    if (this.paramList != null) {
        throw ContextStack.clearStackAnd(_Exceptions::castCriteriaApi);
    }
    this.paramList = CriteriaUtils.paramList(paramList);
    return this;
}
```

- **参数**: `List&lt;?&gt;` — 通常是 `List&lt;Map&lt;String, Object&gt;&gt;`，每个 Map 代表一批删除的参数
- **返回类型**: `BatchDelete` — 可传给 `session.delete(batchDelete)` 或 `session.execute(batchDelete)`
- **不可再次调用** — 调用后 `paramList` 已设置，再次调用会抛异常

---

## 完整示例

### 基础批量 DELETE (来自官方测试)

```java
// StandardDeleteUnitTests.java line 53-70
final List&lt;Map&lt;String, String&gt;&gt; paramList = _Collections.arrayList();

paramList.add(Collections.singletonMap(ChinaRegion_.NAME, "马鱼腮角"));
paramList.add(Collections.singletonMap(ChinaRegion_.NAME, "五指礁"));

final BatchDelete stmt;
stmt = SQLs.batchSingleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.AS, "c")
        .where(ChinaRegion_.createTime.less(SQLs::literal, LocalDateTime.now()))
        .and(ChinaRegion_.name.spaceEqual(SQLs::namedParam))  // ⚠️ 注意这里用 spaceEqual + namedParam
        .and(ChinaRegion_.version.equal(SQLs::param, 2))
        .asDelete()
        .namedParamList(paramList);  // ⚠️ 必须在 asDelete() 之后链式调用
```

**关键要点**:
1. `paramList` 必须先定义，才能在链式调用中传入
2. WHERE 子句中变化的参数用 `space*` + `SQLs::namedParam`
3. WHERE 子句中不变的参数用普通 `equal(SQLs::param, value)`
4. **必须**在 `asDelete()` 后链式调用 `namedParamList(paramList)`

### 简单批量 DELETE (不带 WHERE)

```java
final List&lt;Map&lt;String, Object&gt;&gt; paramList = ...;
final BatchDelete stmt = SQLs.batchSingleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
        .asDelete()
        .namedParamList(paramList);
```

### 带 WHERE 的批量 DELETE

```java
final List&lt;Map&lt;String, Object&gt;&gt; paramList = ...;
final BatchDelete stmt = SQLs.batchSingleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
        .where(ChinaRegion_.id.spaceEqual(SQLs::namedParam))  // 变化的 id 用 namedParam
        .and(ChinaRegion_.version.equal(SQLs::literal, 1))    // 固定的 version 用 literal
        .asDelete()
        .namedParamList(paramList);
```

### 动态 WHERE (Consumer 风格)

```java
final List&lt;Map&lt;String, Object&gt;&gt; paramList = ...;
final BatchDelete stmt = SQLs.batchSingleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
        .where(where -&gt; {
            where.accept(ChinaRegion_.id.spaceEqual(SQLs::namedParam));
            where.accept(ChinaRegion_.status.equal(SQLs::literal, 0));
        })
        .asDelete()
        .namedParamList(paramList);
```

### 条件 WHERE/AND

```java
final List&lt;Map&lt;String, Object&gt;&gt; paramList = ...;
final BatchDelete stmt = SQLs.batchSingleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
        .whereIf(() -&gt; shouldFilterId ? ChinaRegion_.id.spaceEqual(SQLs::namedParam) : null)
        .ifAnd(() -&gt; shouldFilterName ? ChinaRegion_.name.spaceEqual(SQLs::namedParam) : null)
        .asDelete()
        .namedParamList(paramList);
```

---

## 实现类继承链

```
SQLSyntax (SQLs extends)
    └─ SQLs
        └─ .batchSingleDelete() → StandardDeletes.batchSingleDelete()
            └─ StandardBatchDelete
                └─ StandardDeleteStatement
                    └─ StandardDeletes
                        └─ SingleDeleteStatement
                            └─ WhereClause
                                └─ ArmyStmtSpec
                                    └─ BatchDelete (最终)
```

---

## 关键约束和规则

### 层级约束 (通过接口链强制执行)

接口名称本身就编码了当前上下文中可用的方法，DSL 通过**返回类型引导**下一个可调用的方法：

1. **WITH → DELETE FROM**: 调用 `with()` 后只能进入 `_StandardDeleteClause`，即只能调用 `deleteFrom()`
2. **DELETE FROM → WHERE**: 调用 `deleteFrom()` 后只能进入 `_WhereSpec`，即只能调用 `where()` 或相关方法
3. **WHERE → AND**: 调用 `where(IPredicate)` 后进入 `_WhereAndSpec`，可继续 `and()`
4. **AND → asDelete**: 最终调用 `asDelete()` 获得 `_BatchDeleteParamSpec`
5. **asDelete → namedParamList**: **必须**继续调用 `namedParamList()` 才能获得最终的 `BatchDelete`

### 方法调用限制

| 方法/阶段 | 是否必须 | 可重复调用 | 说明 |
|----------|---------|-----------|------|
| `SQLs.batchSingleDelete()` | 是 | 否 | 入口，只能调用一次（每次调用创建新语句） |
| `with()` / `withRecursive()` | 否 | 否 | WITH 关键字只能出现一次，多个 CTE 用 comma 追加 |
| `deleteFrom()` | 是 | 否 | 必须且只能调用一次 |
| `where()` | 否 | 否 | 只能调用一次（但可通过 Consumer 组合多条件） |
| `and()` | 否 | 是 | 可重复调用，追加条件 |
| `asDelete()` | 是 | 否 | 必须调用一次，结束 DELETE 语句构建，返回 `_BatchDeleteParamSpec` |
| `namedParamList()` | 是 | 否 | **必须**在 `asDelete()` 后调用一次，返回最终 `BatchDelete` |

### null 检查

- `tableAlias` 不可为 null
- `table` 不可为 null
- CTE 的名称不可为 null
- `paramList` 不可为 null，必须在调用 `namedParamList()` 前准备好
- 条件形式的 `ifWhere` / `ifAnd` 的 value 可为 null (null 时忽略该条件)

### 类型安全

- `deleteFrom` 只接受 `SingleTableMeta`，不接受父子表或其他类型
- 返回类型的链式设计确保：不会在错误的阶段调用错误的方法
- `namedParamList()` 的 `paramList` 类型在编译时不强制，但运行时必须与 WHERE 子句中的 named 参数匹配

### 批量场景特殊约束

#### ❌ 错误: 中断链忘记 namedParamList()

```java
// 编译错误: _BatchDeleteParamSpec 不是 BatchDelete
final BatchDelete stmt = SQLs.batchSingleDelete()
        .deleteFrom(...)
        .where(...)
        .asDelete();  // ← 返回 _BatchDeleteParamSpec，不是 BatchDelete
```

#### ✅ 正确: 完整链式调用

```java
final BatchDelete stmt = SQLs.batchSingleDelete()
        .deleteFrom(...)
        .where(...)
        .asDelete()
        .namedParamList(paramList);  // ← 必须在这里
```

#### ❌ 错误: 分开调用（虽然语法上可能，但不符合 DSL 设计意图）

```java
// 不推荐，但技术上可行（因为 StandardBatchDelete 同时实现了两个接口）
final _BatchDeleteParamSpec spec = SQLs.batchSingleDelete()
        .deleteFrom(...)
        .where(...)
        .asDelete();
final BatchDelete stmt = spec.namedParamList(paramList);
```

#### ⚠️ 命名参数约束

在批量 UPDATE/DELETE 中，**只能**使用 `SQLs::namedParam`，不能使用 `SQLs::namedLiteral` 或 `SQLs::namedConst`（这两个仅限 VALUES INSERT 使用）：

```java
// ✅ 正确: batch DELETE 使用 namedParam
.where(ChinaRegion_.name.spaceEqual(SQLs::namedParam))

// ❌ 错误: namedLiteral 仅用于 INSERT，在 batch DELETE 中会运行时异常
.where(ChinaRegion_.name.spaceEqual(SQLs::namedLiteral))
```

---

## 与 singleDelete() 的关键区别

| 特性 | `singleDelete()` | `batchSingleDelete()` |
|------|------------------|----------------------|
| `asDelete()` 返回 | `Delete` | `_BatchDeleteParamSpec` |
| 额外方法 | 无 | `.namedParamList(List&lt;?&gt;)` |
| WHERE 常用参数方式 | `equal(SQLs::param, value)` | `spaceEqual(SQLs::namedParam)` |
| paramList 位置 | 不需要 | 必须在 `asDelete()` 后传入 |
| 最终执行对象 | `Delete` | `BatchDelete` |
| 单次删除行数 | 0..N | 0..N × paramList.size() |

---

## 与其他 DELETE 入口的区别

| 入口 | 返回类型 | 场景 |
|-----|---------|------|
| `SQLs.singleDelete()` | `_WithSpec&lt;Delete&gt;` | 标准单表 DELETE |
| `SQLs.domainDelete()` | `_DomainDeleteClause&lt;Delete&gt;` | domain 模型 DELETE，支持父子表 |
| `SQLs.batchSingleDelete()` | `_WithSpec&lt;_BatchDeleteParamSpec&gt;` | **本技能覆盖**：批量单表 DELETE |
| `SQLs.batchDomainDelete()` | `_DomainDeleteClause&lt;_BatchDeleteParamSpec&gt;` | 批量 domain DELETE |

---

## 自我进化指南

当 Army Criteria API 更新时，请按以下步骤更新本文档：

1. **检查接口变更**: 重新读取 `StandardDelete.java`、`Statement.java`
2. **检查实现变更**: 重新读取 `StandardDeletes.java`、`SingleDeleteStatement.java`
3. **检查测试更新**: 查看 `StandardDeleteUnitTests.java` 的新增测试用例
4. **更新 Diagram**: 如有新方法/新子句，补充到 Diagram 中
5. **更新示例**: 补充新增用法的示例
6. **更新约束**: 如有新的语义约束，补充到规则部分
7. **对比验证**: 对比 `singleDelete()` 的变化，确保一致性

---

## 快速参考卡片

```java
// ========================================
// 批量 DELETE 最小代码模板
// ========================================
final List&lt;Map&lt;String, Object&gt;&gt; paramList = ...; // 1. 先准备参数列表

final BatchDelete stmt = SQLs.batchSingleDelete()
    .deleteFrom(YourTable_.T, SQLs.AS, "alias")    // 2. DELETE FROM
    .where(YourTable_.id.spaceEqual(SQLs::namedParam)) // 3. WHERE (用 spaceEqual + namedParam)
    .asDelete()                                     // 4. asDelete()
    .namedParamList(paramList);                     // 5. ⚠️ 必须链式调用 namedParamList

session.delete(stmt);  // 执行
```

