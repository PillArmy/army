---
name: SQLs.singleDelete() 方法链
description: 完整的 Army Criteria API SQLs.singleDelete() 方法链知识。用于理解、解释或记录从 SQLs.singleDelete() 到 asDelete() 的完整 DELETE 语句构建流程。
---

# SQLs.singleDelete() 方法链 — 完整参考

> **适用范围**: 本技能 **仅限** 用于理解、解释、记录 `SQLs.singleDelete()` 入口的标准 DELETE 语句构建流程。覆盖从 `SQLs.singleDelete()` 到 `asDelete()` 的完整方法链，包括 WITH、DELETE FROM、WHERE、AND 等阶段。
> **不** 涵盖 domainDelete、batchSingleDelete、方言特有语法。

> **源码依据**: 本技能基于以下核心源文件编写——`Statement.java`（接口定义）、`StandardDelete.java`（标准 DELETE 接口组合）、`StandardDeletes.java`（实现）、`SingleDeleteStatement.java`（核心实现）、`StandardDeleteUnitTests.java`（示例）。所有描述均以实际源码接口签名和实现为准。

## 核心入口

```java
// SQLs.java line 430-432
public static StandardDelete._WithSpec<Delete> singleDelete() {
    return StandardDeletes.singleDelete(StandardDialect.STANDARD20);
}
```

**返回类型**: `StandardDelete._WithSpec<Delete>` — 实现类是 `StandardDeletes.StandardSimpleDelete`。

**内部实现**: `StandardDeletes.singleDelete()`:

```java
// StandardDeletes.java line 47-49
static _WithSpec<Delete> singleDelete(StandardDialect dialect) {
    return new StandardSimpleDelete(dialect, null);
}
```

---

## 完整方法链 Diagram

> **阅读指南**: 每个叶子节点代表一个可直接调用的方法，带完整参数列表。
> 接口链通过返回类型导航。

```
SQLs.singleDelete() → StandardDelete._WithSpec<Delete>
│
├─① WITH (可选，仅一次声明；comma 追加 CTE 可重复)
│  ├─ .with(Consumer<StandardCtes> consumer)       → _StandardDeleteClause<Delete>
│  ├─ .withRecursive(Consumer<StandardCtes> consumer) → _StandardDeleteClause<Delete>
│  ├─ .ifWith(Consumer<StandardCtes> consumer)      → _StandardDeleteClause<Delete>
│  ├─ .ifWithRecursive(Consumer<StandardCtes> consumer) → _StandardDeleteClause<Delete>
│  └─ (静态 CTE 方式: 同 query 的 with(name) → parens() → as() → space()，但 DELETE 使用较少)
│
└─② DELETE FROM (必须，仅一次，不可重复)
   └─ .deleteFrom(SingleTableMeta<?> table, SQLs.WordAs as, String tableAlias) → _WhereSpec<Delete>
      │
      └─③ WHERE (可选)
         ├─ .where(IPredicate predicate)                  → _WhereAndSpec<Delete>
         ├─ .where(Function<T, IPredicate> expOperator, T operand) → _WhereAndSpec<Delete>
         ├─ .whereIf(Supplier<IPredicate> supplier)          → _WhereAndSpec<Delete>
         ├─ .whereIf(Function<T, IPredicate> expOperator, @Nullable T value) → _WhereAndSpec<Delete>
         ├─ .whereIf(ExpressionOperator<TypedExpression, T, IPredicate> expOp,
                     BiFunction<TypedExpression, T, Expression> operator,
                     @Nullable T value)                        → _WhereAndSpec<Delete>
         ├─ .whereIf(BiFunction<SQLs.BiOperator, T, IPredicate> expOp,
                     SQLs.BiOperator operator, @Nullable T value) → _WhereAndSpec<Delete>
         ├─ .whereIf(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>,
                     T, IPredicate> expOp, SQLs.BiOperator op,
                     BiFunction<TypedExpression, T, Expression> func,
                     @Nullable T value)                        → _WhereAndSpec<Delete>
         ├─ .whereIf(BetweenValueOperator<T> expOp,
                     BiFunction<TypedExpression, T, Expression> operator,
                     @Nullable T value1, SQLs.WordAnd and, @Nullable T value2) → _WhereAndSpec<Delete>
         ├─ .whereIf(BetweenDualOperator<T, U> expOp,
                     BiFunction<TypedExpression, T, Expression> firstFuncRef,
                     @Nullable T first, SQLs.WordAnd and,
                     BiFunction<TypedExpression, U, Expression> secondFuncRef,
                     @Nullable U second)                        → _WhereAndSpec<Delete>
         ├─ .where(Consumer<Consumer<IPredicate>> consumer) → _DmlDeleteSpec<Delete> (直接跳过 AND)
         ├─ .ifWhere(Consumer<Consumer<IPredicate>> consumer) → _DmlDeleteSpec<Delete> (条件 + 跳过 AND)
         │
         └─④ AND (可选，可重复)
            ├─ .and(IPredicate predicate)                  → _WhereAndSpec<Delete>
            ├─ .and(Function<T, IPredicate> expOperator, T operand) → _WhereAndSpec<Delete>
            ├─ .ifAnd(Supplier<IPredicate> supplier)          → _WhereAndSpec<Delete>
            ├─ .ifAnd(Function<T, IPredicate> expOperator, @Nullable T value) → _WhereAndSpec<Delete>
            ├─ .ifAnd(ExpressionOperator<TypedExpression, T, IPredicate> expOp,
                       BiFunction<TypedExpression, T, Expression> operator,
                       @Nullable T value)                        → _WhereAndSpec<Delete>
            ├─ .ifAnd(BiFunction<SQLs.BiOperator, T, IPredicate> expOp,
                       SQLs.BiOperator operator, @Nullable T value) → _WhereAndSpec<Delete>
            ├─ .ifAnd(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>,
                       T, IPredicate> expOp, SQLs.BiOperator op,
                       BiFunction<TypedExpression, T, Expression> func,
                       @Nullable T value)                        → _WhereAndSpec<Delete>
            ├─ .ifAnd(BetweenValueOperator<T> expOp,
                       BiFunction<TypedExpression, T, Expression> operator,
                       @Nullable T value1, SQLs.WordAnd and, @Nullable T value2) → _WhereAndSpec<Delete>
            └─ .ifAnd(BetweenDualOperator<T, U> expOp,
                       BiFunction<TypedExpression, T, Expression> firstFuncRef,
                       @Nullable T first, SQLs.WordAnd and,
                       BiFunction<TypedExpression, U, Expression> secondFuncRef,
                       @Nullable U second)                        → _WhereAndSpec<Delete>
            │
            └─⑤ 结束: asDelete()
               └─ .asDelete() → Delete (可执行的 DELETE 语句对象)
```

---

## 逐层接口详解

### 0. 入口: `StandardDelete._WithSpec<Delete>`

```java
// StandardDelete.java line 58-63
interface _WithSpec<I extends Item>
        extends _StandardDynamicWithClause<_StandardDeleteClause<I>>,  // with(Consumer)
                _StandardStaticWithClause<_StandardDeleteClause<I>>,    // with(name)
                _StandardDeleteClause<I> {                              // DELETE FROM
}
```

`_WithSpec` 组合了三种能力：动态 CTE、静态 CTE、DELETE FROM。所以 `SQLs.singleDelete()` 返回的对象可以直接 `.deleteFrom()`，也可以先用 `.with()` 定义 CTE。

---

### ① WITH (Common Table Expression)

> **语义约束**: `WITH` 关键字仅出现一次（即 `.with(name)` / `.withRecursive(name)` 只能调用一次）。
> 后续 CTE 通过 `_CteComma.comma(String name)` 追加。
> `.space()` 是唯一退出 CTE 链、进入 DELETE FROM 的路径。
> **禁止** 在 `.space()` 后再调用 `.with(name)`——类型系统不提供此路径。

**接口**: `_StandardDynamicWithClause` + `_StandardStaticWithClause`

**动态 CTE (运行时构建)**:

```java
SQLs.singleDelete()
    .with(builder -> {
        builder.comma("cte1").as(s -> s.select(...).from(...).asQuery());
        builder.comma("cte2").as(s -> s.select(...).from(...).asQuery());
    })
    .deleteFrom(...)
```

**条件 CTE**:

```java
SQLs.singleDelete()
    .ifWith(builder -> { ... })        // 仅当 consumer 内有实际操作时执行
    .ifWithRecursive(builder -> { ... }) // 递归条件 CTE
```

---

### ② DELETE FROM (核心)

```java
// StandardDelete.java line 26-30
interface _DeleteFromClause<R> extends Item {
    R deleteFrom(SingleTableMeta<?> table, SQLs.WordAs as, String tableAlias);
}
```

> **规则**:
> - **必须调用一次，且仅能调用一次**
> - 仅接受 `SingleTableMeta`（标准单表，不支持父子表联合）
> - `SQLs.WordAs` 固定传入 `SQLs.AS`
> - `tableAlias` 必须提供非空字符串
> - 不可重复调用——类型系统和实现都强制执行此约束

**使用示例** (来自测试):

```java
// StandardDeleteUnitTests.java line 39-46
final Delete stmt;
stmt = SQLs.singleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")  // 必须调用
        .where(...)
        .asDelete();
```

---

### ③ WHERE (可选)

#### 接口层

```java
// Statement.java line 792-816
interface _WhereClause<WR, WA> extends _MinWhereClause<WR, WA> {
    // 静态形式
    WA where(IPredicate predicate);

    <T> WA where(Function<T, IPredicate> expOperator, T operand);

    WA whereIf(Supplier<IPredicate> supplier);

    // 动态形式 (跳过 AND)
    WR where(Consumer<Consumer<IPredicate>> consumer);

    // 条件形式
    <T> WA whereIf(Function<T, IPredicate>, @Nullable T);

    <T> WA whereIf(ExpressionOperator<TypedExpression, T, IPredicate> expOp,
                   BiFunction<TypedExpression, T, Expression> operator,
                   @Nullable T value);

    <T> WA whereIf(BiFunction<SQLs.BiOperator, T, IPredicate> expOp,
                   SQLs.BiOperator operator, @Nullable T value);

    <T> WA whereIf(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>,
                   T, IPredicate> expOp, SQLs.BiOperator op,
                   BiFunction<TypedExpression, T, Expression> func,
                   @Nullable T value);

    <T> WA whereIf(BetweenValueOperator<T> expOp,
                   BiFunction<TypedExpression, T, Expression> operator,
                   @Nullable T value1, SQLs.WordAnd and, @Nullable T value2);

    <T, U> WA whereIf(BetweenDualOperator<T, U> expOp,
                      BiFunction<TypedExpression, T, Expression> firstFuncRef,
                      @Nullable T first, SQLs.WordAnd and,
                      BiFunction<TypedExpression, U, Expression> secondFuncRef,
                      @Nullable U second);
}
```

#### 返回值:

- `.where(IPredicate)` / `.where(Function, operand)` → `_WhereAndSpec` (可继续 `.and(...)`)
- `.where(Consumer)` → `_DmlDeleteSpec` (直接跳转到 asDelete，不能再 AND)

#### 使用示例

```java
// 方法引用风格 (最常用)
.where(ChinaRegion_.id.equal(SQLs::literal, 1))

// 直接传 IPredicate
.where(ChinaRegion_.name.equal(SQLs::param, "马鱼腮角"))

// Consumer 风格 (多条件，跳过 AND 链)
.where(whereClause -> {
    whereClause.accept(condition1);
    whereClause.accept(condition2);
})

// 条件 WHERE
.whereIf(() -> shouldApply ? predicate : null)
```

---

### ④ AND (可选，可重复)

```java
// Statement.java line 851-875
interface _WhereAndClause<WA> extends _MinWhereAndClause<WA> {
    WA and(IPredicate predicate);

    <T> WA and(Function<T, IPredicate> expOperator, T operand);

    WA ifAnd(Supplier<IPredicate> supplier);

    <T> WA ifAnd(Function<T, IPredicate>, @Nullable T);

    <T> WA ifAnd(ExpressionOperator<TypedExpression, T, IPredicate> expOp,
                 BiFunction<TypedExpression, T, Expression> operator,
                 @Nullable T value);

    <T> WA ifAnd(BiFunction<SQLs.BiOperator, T, IPredicate> expOp,
                 SQLs.BiOperator operator, @Nullable T value);

    <T> WA ifAnd(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>,
                 T, IPredicate> expOp, SQLs.BiOperator op,
                 BiFunction<TypedExpression, T, Expression> func,
                 @Nullable T value);

    <T> WA ifAnd(BetweenValueOperator<T> expOp,
                 BiFunction<TypedExpression, T, Expression> operator,
                 @Nullable T value1, SQLs.WordAnd and, @Nullable T value2);

    <T, U> WA ifAnd(BetweenDualOperator<T, U> expOp,
                    BiFunction<TypedExpression, T, Expression> firstFuncRef,
                    @Nullable T first, SQLs.WordAnd and,
                    BiFunction<TypedExpression, U, Expression> secondFuncRef,
                    @Nullable U second);
}
```

**使用示例**:

```java
// StandardDeleteUnitTests.java line 40-46
stmt = SQLs.singleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
        .where(ChinaRegion_.id.equal(1))
        .and(ChinaRegion_.name.equal(SQLs::param, "马鱼腮角"))
        .and(ChinaRegion_.version.equal(SQLs::param, 2))
        .asDelete();
```

---

### ⑤ asDelete() — 链尾

```java
// Statement.java line 1421-1424
interface _DmlDeleteSpec<I extends Item> extends Item {
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

- **返回类型**: `Delete` — 可传给 `session.delete(delete)` 或 `session.execute(delete)`
- **不可再次调用** — 调用后语句已标记为 prepared，再次操作会抛异常

---

## 完整示例

### 简单 DELETE (不带 WHERE)

```java
final Delete stmt = SQLs.singleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
        .asDelete();
// 结果: DELETE FROM china_region AS r
```

### 带 WHERE 的 DELETE

```java
final Delete stmt = SQLs.singleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
        .where(ChinaRegion_.id.equal(SQLs::literal, 1))
        .and(ChinaRegion_.name.equal(SQLs::param, "马鱼腮角"))
        .and(ChinaRegion_.version.equal(SQLs::param, 2))
        .asDelete();
// 结果: DELETE FROM china_region AS r WHERE id = 1 AND name = ? AND version = ?
```

### 动态 WHERE (Consumer 风格)

```java
final Delete stmt = SQLs.singleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
        .where(where -> {
            where.accept(ChinaRegion_.id.greater(SQLs::literal, 100));
            where.accept(ChinaRegion_.status.equal(SQLs::literal, 0));
        })
        .asDelete();
```

### 条件 WHERE/AND

```java
final Delete stmt = SQLs.singleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.AS, "r")
        .whereIf(() -> shouldFilterId ? ChinaRegion_.id.equal(id) : null)
        .ifAnd(() -> shouldFilterName ? ChinaRegion_.name.equal(name) : null)
        .asDelete();
```

---

## 实现类继承链

```
SQLSyntax (SQLs extends)
    └─ SQLs
        └─ .singleDelete() → StandardDeletes.singleDelete()
            └─ StandardSimpleDelete
                └─ StandardDeleteStatement
                    └─ StandardDeletes
                        └─ SingleDeleteStatement
                            └─ WhereClause
                                └─ ArmyStmtSpec
```

---

## 关键约束和规则

### 层级约束 (通过接口链强制执行)

接口名称本身就编码了当前上下文中可用的方法，DSL 通过**返回类型引导**下一个可调用的方法：

1. **WITH → DELETE FROM**: 调用 `with()` 后只能进入 `_StandardDeleteClause`，即只能调用 `deleteFrom()`
2. **DELETE FROM → WHERE**: 调用 `deleteFrom()` 后只能进入 `_WhereSpec`，即只能调用 `where()` 或相关方法
3. **WHERE → AND**: 调用 `where(IPredicate)` 后进入 `_WhereAndSpec`，可继续 `and()`
4. **最终 → asDelete**: 最终必须调用 `asDelete()` 来结束构建并获得可执行的 `Delete` 对象

### 方法调用限制

| 方法/阶段 | 是否必须 | 可重复调用 | 说明 |
|----------|---------|-----------|------|
| `SQLs.singleDelete()` | 是 | 否 | 入口，只能调用一次（每次调用创建新语句） |
| `with()` / `withRecursive()` | 否 | 否 | WITH 关键字只能出现一次，多个 CTE 用 comma 追加 |
| `deleteFrom()` | 是 | 否 | 必须且只能调用一次 |
| `where()` | 否 | 否 | 只能调用一次（但可通过 Consumer 组合多条件） |
| `and()` | 否 | 是 | 可重复调用，追加条件 |
| `asDelete()` | 是 | 否 | 必须调用一次，结束构建 |

### null 检查

- `tableAlias` 不可为 null
- `table` 不可为 null
- CTE 的名称不可为 null
- 条件形式的 `ifWhere` / `ifAnd` 的 value 可为 null (null 时忽略该条件)

### 类型安全

- `deleteFrom` 只接受 `SingleTableMeta`，不接受父子表或其他类型
- 返回类型的链式设计确保：不会在错误的阶段调用错误的方法

---

## 与其他 DELETE 入口的区别

| 入口 | 返回类型 | 场景 |
|-----|---------|------|
| `SQLs.singleDelete()` | `_WithSpec<Delete>` | 标准单表 DELETE，本技能覆盖 |
| `SQLs.domainDelete()` | `_DomainDeleteClause<Delete>` | domain 模型 DELETE，支持父子表 |
| `SQLs.batchSingleDelete()` | `_WithSpec<_BatchDeleteParamSpec>` | 批量单表 DELETE |
| `SQLs.batchDomainDelete()` | `_DomainDeleteClause<_BatchDeleteParamSpec>` | 批量 domain DELETE |

---

## 自我进化指南

当 Army Criteria API 更新时，请按以下步骤更新本文档：

1. **检查接口变更**: 重新读取 `StandardDelete.java`、`Statement.java`
2. **检查实现变更**: 重新读取 `StandardDeletes.java`、`SingleDeleteStatement.java`
3. **检查测试更新**: 查看 `StandardDeleteUnitTests.java` 的新增测试用例
4. **更新 Diagram**: 如有新方法/新子句，补充到 Diagram 中
5. **更新示例**: 补充新增用法的示例
6. **更新约束**: 如有新的语义约束，补充到规则部分
