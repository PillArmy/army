---
name: SQLs.singleUpdate() method chain
description: 完整的 Army Criteria API SQLs.singleUpdate() 方法链知识。用于理解、解释或记录从 SQLs.singleUpdate() 到 asUpdate() 的完整 UPDATE 语句构建流程。涵盖每一个接口、每一个方法和每一个合法路径。
---

# SQLs.singleUpdate() method chain — 完整参考

> **适用范围**: 本 Skill **仅限**用于理解、解释、记录 `SQLs.singleUpdate()` 入口的标准 UPDATE 语句构建流程。
> 覆盖从 `SQLs.singleUpdate()` 到 `asUpdate()` 的完整方法链，包括 WITH、UPDATE、SET、WHERE、AND、asUpdate() 全部 6 个阶段。**不**涵盖 domainUpdate()、batchSingleUpdate() 或方言特有语法。

> **源码依据**: 本 Skill 基于以下核心源文件编写——`StandardUpdate.java`（接口定义）、
> `UpdateStatement.java`（更新语句接口）、`StandardUpdates.java`（实现）、`StandardUpdateUnitTests.java`（示例）。所有描述均以实际源码接口签名和实现为准。

## 核心入口

```java
// SQLs.java line 411-413
public static StandardUpdate._WithSpec<Update> singleUpdate() {
    return StandardUpdates.singleUpdate(StandardDialect.STANDARD20);
}
```

**返回类型**: `StandardUpdate._WithSpec<Update>` — 实现类是 `StandardUpdates.StandardSimpleUpdateClause`。

**内部实现**: `StandardUpdates.singleUpdate()`:

```java
// StandardUpdates.java line 44-46
static _WithSpec<Update> singleUpdate(StandardDialect dialect) {
    return new StandardSimpleUpdateClause(dialect);
}
```

---

## 完整方法链 Diagram

> **阅读指南**: 每个叶子节点代表一个可直接调用的方法，带完整参数列表。
> 接口链通过返回类型导航。

```
SQLs.singleUpdate()  →  StandardUpdate._WithSpec<Update>
│
├─① WITH (可选，仅一次声明；comma 追加 CTE 可重复)
│  ├─ .with(String name)                                    →  StandardQuery._StaticCteParensSpec<_SingleUpdateClause<Update>>
│  │  ├─ .as(Function<SelectSpec<_CteComma>, _CteComma> function) → _CteComma
│  │  │  ├─ .comma(String name)                              → _StaticCteParensSpec (追加 CTE)
│  │  │  └─ .space()                                        → _SingleUpdateClause (结束 CTE)
│  │  ├─ .parens(String first, String... rest)              → _StaticCteAsClause
│  │  │  └─ .as(Function<SelectSpec<_CteComma>, _CteComma> function) → _CteComma
│  │  ├─ .parens(Consumer<Consumer<String>> consumer)       → _StaticCteAsClause
│  │  │  └─ .as(Function<SelectSpec<_CteComma>, _CteComma> function) → _CteComma
│  │  └─ .ifParens(Consumer<Consumer<String>> consumer)     → _StaticCteAsClause
│  │     └─ .as(Function<SelectSpec<_CteComma>, _CteComma> function) → _CteComma
│  ├─ .withRecursive(String name)                           → _StaticCteParensSpec (同上 .as/.parens/.ifParens → .as → _CteComma → .comma/.space)
│  ├─ .with(Consumer<StandardCtes> consumer)                → _SingleUpdateClause
│  ├─ .withRecursive(Consumer<StandardCtes> consumer)       → _SingleUpdateClause
│  ├─ .ifWith(Consumer<StandardCtes> consumer)              → _SingleUpdateClause
│  └─ .ifWithRecursive(Consumer<StandardCtes> consumer)     → _SingleUpdateClause
│
├─② UPDATE (必须)
│  └─ .update(SingleTableMeta<?> table, SQLs.WordAs as, String tableAlias) → _StandardSetClause<Update, FieldMeta<?>>
│
├─③ SET (必须，至少一个字段赋值；可重复追加)
│  ├─ [静态 SET]
│  │  ├─ .set(F field, @Nullable Object value)                          → _WhereSpec<Update, F>
│  │  ├─ .set(F field, BiFunction<F, E, AssignmentItem> valueOperator, @Nullable E value) → _WhereSpec<Update, F>
│  │  ├─ .set(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator, 
│  │  │         BiFunction<F, E, Expression> valueOperator, @Nullable E value) → _WhereSpec<Update, F>
│  │  ├─ .ifSet(F field, @Nullable Object value)                        → _WhereSpec<Update, F>
│  │  ├─ .ifSet(F field, BiFunction<F, E, AssignmentItem> valueOperator, @Nullable E value) → _WhereSpec<Update, F>
│  │  └─ .ifSet(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator,
│  │            BiFunction<F, E, Expression> valueOperator, @Nullable E value) → _WhereSpec<Update, F>
│  │
│  └─ [动态 SET]
│     └─ .sets(Consumer<_BatchItemPairs<F>> consumer)                   → _StandardWhereClause<Update>
│
├─④ WHERE (可选)
│  ├─ [静态 WHERE]
│  │  ├─ .where(IPredicate predicate)                                  → _WhereAndSpec<Update>
│  │  ├─ .where(Function<T, IPredicate> expOperator, T operand)        → _WhereAndSpec<Update>
│  │  ├─ .whereIf(Supplier<IPredicate> supplier)                       → _WhereAndSpec<Update>
│  │  ├─ .whereIf(Function<T, IPredicate> expOperator, @Nullable T value) → _WhereAndSpec<Update>
│  │  ├─ .whereIf(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
│  │  │            BiFunction<TypedExpression, T, Expression> operator, @Nullable T value) → _WhereAndSpec<Update>
│  │  ├─ .whereIf(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator, 
│  │  │            SQLs.BiOperator operator, @Nullable T value) → _WhereAndSpec<Update>
│  │  ├─ .whereIf(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator,
│  │  │            SQLs.BiOperator op, BiFunction<TypedExpression, T, Expression> func, @Nullable T value) → _WhereAndSpec<Update>
│  │  ├─ .whereIf(BetweenValueOperator<T> expOperator, 
│  │  │            BiFunction<TypedExpression, T, Expression> operator, @Nullable T value1, 
│  │  │            SQLs.WordAnd and, @Nullable T value2) → _WhereAndSpec<Update>
│  │  └─ .whereIf(BetweenDualOperator<T, U> expOperator, 
│  │             BiFunction<TypedExpression, T, Expression> firstFuncRef, @Nullable T first,
│  │             SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, @Nullable U second) → _WhereAndSpec<Update>
│  │
│  └─ [动态 WHERE]
│     ├─ .where(Consumer<Consumer<IPredicate>> consumer)               → _DmlUpdateSpec<Update>
│     └─ .ifWhere(Consumer<Consumer<IPredicate>> consumer)             → _DmlUpdateSpec<Update>
│
├─⑤ AND (可选，可重复；仅在静态 WHERE 后可用)
│  ├─ [静态 AND]
│  │  ├─ .and(IPredicate predicate)                                   → _WhereAndSpec<Update>
│  │  ├─ .and(Function<T, IPredicate> expOperator, T operand)         → _WhereAndSpec<Update>
│  │  ├─ .ifAnd(Supplier<IPredicate> supplier)                        → _WhereAndSpec<Update>
│  │  ├─ .ifAnd(Function<T, IPredicate> expOperator, @Nullable T value) → _WhereAndSpec<Update>
│  │  ├─ .ifAnd(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
│  │  │          BiFunction<TypedExpression, T, Expression> operator, @Nullable T value) → _WhereAndSpec<Update>
│  │  ├─ .ifAnd(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator,
│  │  │          SQLs.BiOperator operator, @Nullable T value) → _WhereAndSpec<Update>
│  │  ├─ .ifAnd(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator,
│  │  │          SQLs.BiOperator op, BiFunction<TypedExpression, T, Expression> func, @Nullable T value) → _WhereAndSpec<Update>
│  │  ├─ .ifAnd(BetweenValueOperator<T> expOperator, 
│  │  │          BiFunction<TypedExpression, T, Expression> operator, @Nullable T value1,
│  │  │          SQLs.WordAnd and, @Nullable T value2) → _WhereAndSpec<Update>
│  │  ├─ .ifAnd(BetweenDualOperator<T, U> expOperator,
│  │  │          BiFunction<TypedExpression, T, Expression> firstFuncRef, @Nullable T first,
│  │  │          SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, @Nullable U second) → _WhereAndSpec<Update>
│  │  ├─ .ifAnd(Function<T, Expression> expOperator1, @Nullable T operand1,
│  │  │          BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand) → _WhereAndSpec<Update>
│  │  └─ .ifAnd(ExpressionOperator<TypedExpression, T, Expression> expOperator1,
│  │             BiFunction<TypedExpression, T, Expression> operator, @Nullable T operand1,
│  │             BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand) → _WhereAndSpec<Update>
│
└─⑥ 结尾: asUpdate()
   └─ .asUpdate()                                                     → Update (可执行更新语句)
```

---

## 逐层接口详解

### 0. 入口: `StandardUpdate._WithSpec<Update>`

```java
// StandardUpdate.java line 64-67
interface _WithSpec<I extends Item> extends _StandardDynamicWithClause<_SingleUpdateClause<I>>,
        _StandardStaticWithClause<_SingleUpdateClause<I>>,
        _SingleUpdateClause<I> {
}
```

`_WithSpec` 组合了三种能力：动态 CTE、静态 CTE、UPDATE。所以 `SQLs.singleUpdate()` 返回的对象可以直接 `.update(...)`，也可以先用 `.with(...)` 定义 CTE。

---

### ① WITH (Common Table Expression)

> **语义约束**: `WITH` 关键字仅出现一次（即 `.with(name)` / `.withRecursive(name)` 只能调用一次）。
> 后续 CTE 通过 `_CteComma.comma(String name)` 追加，该方法返回 `_StaticCteParensSpec`，
> 从而形成 `.comma(name) → .parens(...) → .as(...) → .comma(name) → ...` 的循环链。
> `.space()` 是唯一退出 CTE 链、进入 UPDATE 的路径。
> **禁止**在 `.space()` 后再调用 `.with(name)`——类型系统不提供此路径。

**接口**: `_StandardDynamicWithClause` + `_StandardStaticWithClause`

**静态 CTE (编译时已知)**:

```java
SQLs.singleUpdate()
    .with("cte_name")                                    // → _StaticCteParensSpec
    .parens("col1", "col2")                              // → _StaticCteAsClause (可选列别名)
    .as(s -> s.select(...).from(...).asQuery())          // → _CteComma (可继续 .comma(name) 追加更多 CTE)
    .space()                                             // → _SingleUpdateClause (结束 CTE，进入 UPDATE)
    .update(...)
```

**动态 CTE (运行时构建)**:

```java
SQLs.singleUpdate()
    .with(builder -> {                                  // builder 是 StandardCtes
        builder.comma("cte1").parens("col").as(s -> s.select(...).asQuery());
        builder.comma("cte2").as(s -> s.select(...).asQuery());
    })                                                  // → _SingleUpdateClause
    .update(...)
```

**条件 CTE**:

```java
// 条件 with — 仅当满足条件时执行
SQLs.singleUpdate()
    .ifWith(builder -> {...})                           // → _SingleUpdateClause
    .ifWithRecursive(builder -> {...})                  // → _SingleUpdateClause
    .update(...)
```

---

### ② UPDATE

```java
// StandardUpdate.java line 57-61
interface _SingleUpdateClause<I extends Item> extends Item {
    <T> _StandardSetClause<I, FieldMeta<T>> update(SingleTableMeta<T> table, SQLs.WordAs as, String tableAlias);
}
```

**使用示例**:

```java
SQLs.singleUpdate()
    .update(ChinaRegion_.T, AS, "c")                   // → _StandardSetClause
    .set(...)
```

---

### ③ SET

#### 接口层次

```java
// StandardUpdate.java line 45-49
interface _StandardSetClause<I extends Item, F extends TableField>
        extends UpdateStatement._StaticBatchSetClause<F, _WhereSpec<I, F>>,
        UpdateStatement._DynamicSetClause<UpdateStatement._BatchItemPairs<F>, _StandardWhereClause<I>> {
}
```

```java
// UpdateStatement.java line 59-73
interface _StaticSetClause<F extends SqlField, SR> extends Item {
    SR set(F field, @Nullable Object value);
    <E> SR set(F field, BiFunction<F, E, AssignmentItem> valueOperator, @Nullable E value);
    <E> SR set(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator, 
               BiFunction<F, E, Expression> valueOperator, @Nullable E value);
    SR ifSet(F field, @Nullable Object value);
    <E> SR ifSet(F field, BiFunction<F, E, AssignmentItem> valueOperator, @Nullable E value);
    <E> SR ifSet(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator,
                 BiFunction<F, E, Expression> valueOperator, @Nullable E value);
}
```

```java
// UpdateStatement.java line 51-55
interface _DynamicSetClause<B extends _ItemPairBuilder, SR> {
    SR sets(Consumer<B> consumer);
}
```

#### SET 的三种形式总览

| 形式 | 接口来源 | 参数类型 | 返回类型 | 使用场景 |
|------|---------|---------|---------|---------|
| **静态简单赋值** | `_StaticSetClause` | `field, value` | `_WhereSpec` | 简单的字段赋值，值确定 |
| **静态操作符赋值** | `_StaticSetClause` | `field, valueOperator, value` | `_WhereSpec` | 使用操作符赋值，如 `+=`、`-=` 等 |
| **静态双操作符赋值** | `_StaticSetClause` | `field, fieldOperator, valueOperator, value` | `_WhereSpec` | 双操作符赋值 |
| **条件 SET** | `_StaticSetClause` | `ifSet(...)` | `_WhereSpec` | 条件性赋值 |
| **动态 SET** | `_DynamicSetClause` | `Consumer<_BatchItemPairs>` | `_StandardWhereClause` | 运行时动态确定赋值字段 |

#### 静态 SET 示例

```java
// 简单赋值
.set(ChinaRegion_.name, SQLs::param, "武侠江湖")

// 操作符赋值（如 +=）
.set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, addGdp)

// 条件 SET
.ifSet(ChinaRegion_.name, SQLs::param, "新名称")
```

#### 动态 SET 示例

```java
.sets(s -> {
    s.set(ChinaRegion_.name, SQLs::param, "武侠江湖");
    s.set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, addGdp);
})
```

---

### ④ WHERE

```java
// StandardUpdate.java line 40-42
interface _StandardWhereClause<I extends Item> extends _WhereClause<_DmlUpdateSpec<I>, _WhereAndSpec<I>> {
}
```

#### WHERE 的两种形式总览

| 形式 | 接口来源 | 参数类型 | 返回类型 | 使用场景 |
|------|---------|---------|---------|---------|
| **静态 WHERE** | `_WhereClause` | `IPredicate` 或 `Function<T, IPredicate>` | `_WhereAndSpec` | WHERE 条件确定，可继续 AND |
| **静态条件 WHERE** | `_WhereClause` | `whereIf(...)` | `_WhereAndSpec` | 条件性添加 WHERE |
| **动态 WHERE** | `_WhereClause` | `Consumer<Consumer<IPredicate>>` | `_DmlUpdateSpec` | 运行时动态确定 WHERE 条件，直接跳过 AND |
| **动态条件 WHERE** | `_MinQueryWhereClause` | `ifWhere(Consumer<Consumer<IPredicate>>)` | `_DmlUpdateSpec` | 条件性动态 WHERE |

#### 静态 WHERE 示例

```java
// 简单谓词
.where(ChinaRegion_.id.equal(SQLs::literal, 1))

// 方法引用风格（常用）
.where(ChinaRegion_.id::equal, SQLs::literal, 1)

// between
.where(ChinaRegion_.id.between(SQLs::literal, 1, AND, 100))

// 条件 WHERE
.whereIf(ChinaRegion_.id::equal, SQLs::literal, nullableId)
```

#### 动态 WHERE 示例

```java
.where(whereClause -> {
    whereClause.accept(ChinaRegion_.id.equal(SQLs::literal, 1));
    whereClause.accept(ChinaRegion_.name.equal(SQLs::param, "武侠江湖"));
})
```

---

### ⑤ AND

```java
// StandardUpdate.java line 33-37
interface _WhereAndSpec<I extends Item> extends UpdateStatement._UpdateWhereAndClause<_WhereAndSpec<I>>,
        _DmlUpdateSpec<I> {
}
```

```java
// UpdateStatement.java line 145-167
interface _UpdateWhereAndClause<WA> extends Statement._WhereAndClause<WA> {
    <T> WA ifAnd(Function<T, Expression> expOperator1, @Nullable T operand1,
                 BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand);
    <T> WA ifAnd(ExpressionOperator<TypedExpression, T, Expression> expOperator1,
                 BiFunction<TypedExpression, T, Expression> operator, @Nullable T operand1,
                 BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand);
}
```

**注意**: AND 只能在静态 WHERE 后使用，不能在动态 WHERE 后使用。

**使用示例**:

```java
.where(ChinaRegion_.id.equal(SQLs::literal, 1))
.and(ChinaRegion_.name.equal(SQLs::param, "武侠江湖"))
.and(ChinaRegion_.regionGdp.greater(SQLs::literal, BigDecimal.ZERO))
.ifAnd(ChinaRegion_.status::equal, SQLs::literal, nullableStatus)
```

---

### ⑥ asUpdate() — 链尾

```java
// Statement.java line 1415
interface _DmlUpdateSpec<I extends Item> extends Item {
    I asUpdate();
}
```

**最终类型**: `Update` — 可传给 `session.update(update)`

**完整示例** (来自 StandardUpdateUnitTests.java):

```java
final UpdateStatement stmt;
stmt = SQLs.singleUpdate()
    .update(ChinaRegion_.T, AS, "c")
    .set(ChinaRegion_.name, SQLs::param, "武侠江湖")
    .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, addGdp)
    .where(ChinaRegion_.id.between(SQLs::literal, map.get("firstId"), AND, map.get("secondId")))
    .and(SQLs.bracket(ChinaRegion_.name.equal(SQLs::literal, "江湖")))
    .and(ChinaRegion_.regionGdp.plus(SQLs::param, addGdp).greaterEqual(0))
    .asUpdate();
```

```java
final UpdateStatement stmt;
stmt = SQLs.singleUpdate()
    .update(PillUser_.T, AS, "up")
    .sets(s -> s.set(PillUser_.identityType, SQLs::literal, IdentityType.PERSON)
                .set(PillUser_.identityId, SQLs::literal, 888)
                .set(PillUser_.nickName, SQLs::param, "令狐冲"))
    .where(PillUser_.id.equal(SQLs::literal, "1"))
    .and(PillUser_.nickName.equal(SQLs::param, "zoro"))
    .asUpdate();
```

---

## 实现类继承链

```
SQLSyntax (SQLs extends)
  └─ SQLs
      └─ .singleUpdate() → StandardUpdates.singleUpdate()
          └─ StandardSimpleUpdateClause
              └─ StandardUpdates
                  └─ StandardSimpleUpdate
                      └─ SimpleSingleUpdate
                          └─ SingleUpdateStatement
```

---

## 关键约束和规则

### 层级约束 (通过接口链强制执行)

接口名称本身就编码了当前上下文中可用的方法，DSL 通过**返回类型引导**下一个可调用的方法：

1. **WITH → UPDATE**: 必须通过 `.space()` 或动态 WITH 结束 CTE 才能进入 UPDATE
2. **UPDATE → SET**: 必须先 `.update()` 才能 `.set()`
3. **SET → WHERE**: 必须至少有一个 `.set()` 才能进入 WHERE
4. **WHERE → AND**: 只有静态 WHERE 才能继续 `.and()`，动态 WHERE 直接到 asUpdate()
5. **AND → AND**: `.and()` 可以重复调用，每次都返回 `_WhereAndSpec`
6. **任意 → asUpdate**: 在 SET、WHERE、AND 之后都可以直接 `.asUpdate()`

### 可重复 vs 不可重复

| 子句 | 可重复 | 说明 |
|-----|-------|------|
| WITH (`.with(name)`) | ❌ | WITH 关键字只能出现一次 |
| CTE (`.comma(name)`) | ✅ | 可以追加多个 CTE |
| UPDATE (`.update()`) | ❌ | 只能更新一个表 |
| SET (`.set()`) | ✅ | 可以设置多个字段 |
| WHERE (`.where()`) | ❌ | WHERE 关键字只能出现一次 |
| AND (`.and()`) | ✅ | 可以追加多个 AND 条件 |

