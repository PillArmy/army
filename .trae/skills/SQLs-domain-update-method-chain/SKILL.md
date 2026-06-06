---
name: SQLs.domainUpdate() method chain
description: 完整的 Army Criteria API SQLs.domainUpdate() 方法链知识。用于理解、解释或记录从 SQLs.domainUpdate() 到 asUpdate() 的完整 UPDATE 语句构建流程。涵盖每一个接口、每一个方法和每一个合法路径。
---

# SQLs.domainUpdate() method chain — 完整参考

> **适用范围**: 本 Skill **仅限**用于理解、解释、记录 `SQLs.domainUpdate()` 入口的标准 UPDATE 语句构建流程。覆盖从 `SQLs.domainUpdate()` 到 `asUpdate()` 的完整方法链。不支持 WITH 子句（Domain API 设计上不支持）。

> **源码依据**: 本 Skill 基于以下核心源文件编写——`UpdateStatement.java`（接口定义）、`StandardUpdate.java`（标准 UPDATE 接口组合）、`StandardUpdates.java`（实现）、`SingleUpdateStatement.java`（基类实现）。所有描述均以实际源码接口签名和实现为准。

## 核心入口

```java
// SQLs.java line 406-408
public static StandardUpdate._DomainUpdateClause<Update> domainUpdate() {
    return StandardUpdates.simpleDomain();
}
```

**返回类型**: `StandardUpdate._DomainUpdateClause<Update>` — 实现类是 `StandardUpdates.DomainSimpleUpdateClaus`。

**内部实现**: `StandardUpdates.simpleDomain()`:
```java
// StandardUpdates.java line 52-54
static _DomainUpdateClause<Update> simpleDomain() {
    return new DomainSimpleUpdateClaus();
}
```

---

## 完整方法链 Diagram

```
SQLs.domainUpdate()  →  StandardUpdate._DomainUpdateClause<Update>
│
├─① UPDATE (必须，三种形式)
│  ├─ .update(TableMeta<?> table, String tableAlias)
│  │   →  _StandardSetClause<Update, FieldMeta<?>>
│  │
│  ├─ .update(SingleTableMeta<T> table, SQLs.WordAs as, String tableAlias)
│  │   →  _StandardSetClause<Update, FieldMeta<T>>
│  │
│  └─ .update(ChildTableMeta<T> table, SQLs.WordAs as, String tableAlias)
│      →  _StandardSetClause<Update, FieldMeta<? super T>>
│
├─② SET (必须，至少一个，多种形式)
│  ├─ [静态 SET - 基础形式]
│  │  ├─ .set(F field, @Nullable Object value)
│  │  │   →  _WhereSpec<Update, F> (可继续 set 或进入 where)
│  │  │
│  │  ├─ .set(F field, BiFunction<F, E, AssignmentItem> valueOperator, @Nullable E value)
│  │  │   →  _WhereSpec<Update, F>
│  │  │
│  │  └─ .set(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator,
│  │          BiFunction<F, E, Expression> valueOperator, @Nullable E value)
│  │      →  _WhereSpec<Update, F>
│  │
│  ├─ [条件 SET - ifSet 系列]
│  │  ├─ .ifSet(F field, @Nullable Object value)
│  │  │   →  _WhereSpec<Update, F>
│  │  │
│  │  ├─ .ifSet(F field, BiFunction<F, E, AssignmentItem> valueOperator, @Nullable E value)
│  │  │   →  _WhereSpec<Update, F>
│  │  │
│  │  └─ .ifSet(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator,
│  │          BiFunction<F, E, Expression> valueOperator, @Nullable E value)
│  │      →  _WhereSpec<Update, F>
│  │
│  ├─ [批量 SET - setSpace 系列]
│  │  ├─ .setSpace(F field, BiFunction<F, String, Expression> valueOperator)
│  │  │   →  _WhereSpec<Update, F>
│  │  │
│  │  └─ .setSpace(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator,
│  │          BiFunction<F, String, Expression> valueOperator)
│  │      →  _WhereSpec<Update, F>
│  │
│  └─ [动态 SET - sets 形式]
│     └─ .sets(Consumer<_BatchItemPairs<F>> consumer)
│         →  _StandardWhereClause<Update> (直接进入 where)
│
├─③ WHERE (可选)
│  ├─ [静态 WHERE - 返回 _WhereAndSpec]
│  │  ├─ .where(IPredicate predicate)
│  │  │   →  _WhereAndSpec<Update>
│  │  │
│  │  ├─ .where(Function<T, IPredicate> expOperator, T operand)
│  │  │   →  _WhereAndSpec<Update>
│  │  │
│  │  └─ .whereIf(Supplier<IPredicate> supplier)
│  │      →  _WhereAndSpec<Update>
│  │      └─ [ifWhereIf 系列]
│  │         ├─ .whereIf(Function<T, IPredicate> expOperator, @Nullable T value)
│  │         ├─ .whereIf(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
│  │         │              BiFunction<TypedExpression, T, Expression> operator, @Nullable T value)
│  │         ├─ .whereIf(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator,
│  │         │              SQLs.BiOperator operator, @Nullable T value)
│  │         ├─ .whereIf(TeFunction<SQLs.BiOperator,
│  │         │              BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator,
│  │         │              SQLs.BiOperator op, BiFunction<TypedExpression, T, Expression> func,
│  │         │              @Nullable T value)
│  │         ├─ .whereIf(BetweenValueOperator<T> expOperator,
│  │         │              BiFunction<TypedExpression, T, Expression> operator,
│  │         │              @Nullable T v1, SQLs.WordAnd and, @Nullable T v2)
│  │         └─ .whereIf(BetweenDualOperator<T, U> expOperator,
│  │                      BiFunction<TypedExpression, T, Expression> firstFuncRef,
│  │                      @Nullable T first, SQLs.WordAnd and,
│  │                      BiFunction<TypedExpression, U, Expression> secondFuncRef,
│  │                      @Nullable U second)
│  │
│  └─ [动态 WHERE - 返回 _DmlUpdateSpec]
│     └─ .where(Consumer<Consumer<IPredicate>> consumer)
│         →  _DmlUpdateSpec<Update> (直接进入 asUpdate)
│
├─④ AND (可选，可重复)
│  ├─ [静态 AND]
│  │  ├─ .and(IPredicate predicate)
│  │  │   →  _WhereAndSpec<Update>
│  │  │
│  │  ├─ .and(Function<T, IPredicate> expOperator, T operand)
│  │  │   →  _WhereAndSpec<Update>
│  │  │
│  │  └─ .ifAnd(Supplier<IPredicate> supplier)
│  │      →  _WhereAndSpec<Update>
│  │
│  └─ [条件 AND - ifAnd 系列]
│     ├─ .ifAnd(Function<T, IPredicate> expOperator, @Nullable T value)
│     ├─ .ifAnd(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
│     │          BiFunction<TypedExpression, T, Expression> operator, @Nullable T value)
│     ├─ .ifAnd(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator,
│     │          SQLs.BiOperator operator, @Nullable T value)
│     ├─ .ifAnd(TeFunction<SQLs.BiOperator,
│     │          BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator,
│     │          SQLs.BiOperator op, BiFunction<TypedExpression, T, Expression> func,
│     │          @Nullable T value)
│     ├─ .ifAnd(BetweenValueOperator<T> expOperator,
│     │          BiFunction<TypedExpression, T, Expression> operator,
│     │          @Nullable T v1, SQLs.WordAnd and, @Nullable T v2)
│     ├─ .ifAnd(BetweenDualOperator<T, U> expOperator,
│     │          BiFunction<TypedExpression, T, Expression> firstFuncRef,
│     │          @Nullable T first, SQLs.WordAnd and,
│     │          BiFunction<TypedExpression, U, Expression> secondFuncRef,
│     │          @Nullable U second)
│     ├─ .ifAnd(Function<T, Expression> expOperator1, @Nullable T operand1,
│     │          BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand)
│     └─ .ifAnd(ExpressionOperator<TypedExpression, T, Expression> expOperator1,
│                BiFunction<TypedExpression, T, Expression> operator, @Nullable T operand1,
│                BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand)
│
└─⑤ asUpdate() (必须，结束链)
   └─ .asUpdate() → Update (可执行的更新语句)
```

---

## 逐层接口详解

### 0. 入口: `StandardUpdate._DomainUpdateClause<Update>`

```java
// StandardUpdate.java line 70-78
interface _DomainUpdateClause<I extends Item> extends Item {

    _StandardSetClause<I, FieldMeta<?>> update(TableMeta<?> table, String tableAlias);

    <T> _StandardSetClause<I, FieldMeta<T>> update(SingleTableMeta<T> table, SQLs.WordAs as, String tableAlias);

    <T> _StandardSetClause<I, FieldMeta<? super T>> update(ChildTableMeta<T> table, SQLs.WordAs as, String tableAlias);
}
```

**注意**: `_DomainUpdateClause` 不支持 WITH 子句（这是 Domain API 的设计特点）。如果需要 WITH 子句，请使用 `SQLs.singleUpdate()`。

---

### ① UPDATE 子句

#### 三种形式详解

| 形式 | 签名 | 返回类型 | 使用场景 |
|------|------|---------|---------|
| **TableMeta 形式** | `.update(TableMeta<?> table, String tableAlias)` | `_StandardSetClause<Update, FieldMeta<?>>` | 更新任意表，无类型推断，字段类型为 `FieldMeta<?>` |
| **SingleTableMeta 形式** | `.update(SingleTableMeta<T> table, SQLs.WordAs as, String tableAlias)` | `_StandardSetClause<Update, FieldMeta<T>>` | 更新单表，有强类型推断，字段类型为 `FieldMeta<T>` |
| **ChildTableMeta 形式** | `.update(ChildTableMeta<T> table, SQLs.WordAs as, String tableAlias)` | `_StandardSetClause<Update, FieldMeta<? super T>>` | 更新子表，支持继承字段，字段类型为 `FieldMeta<? super T>` |

**示例**:
```java
// 示例来自 StandardUpdateUnitTests.java
SQLs.domainUpdate()
    .update(ChinaProvince_.T, AS, "p")  // 使用 AS 关键字
    .set(ChinaRegion_.regionGdp, gdpAmount)
    ...;

// 或简化形式（不带 AS）
SQLs.domainUpdate()
    .update(ChinaProvince_.T, "p")
    ...;
```

---

### ② SET 子句

#### SET 子句的多种形式

`_StandardSetClause` 继承了 `UpdateStatement._StaticBatchSetClause` 和 `UpdateStatement._DynamicSetClause`，提供了完整的 SET 语句构建能力。

##### 静态 SET - 基础形式

```java
// UpdateStatement.java line 60-71
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

**使用示例**:
```java
// 基础赋值
.set(ChinaRegion_.name, SQLs::param, "武侠江湖")

// 运算赋值（如 +=）
.set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, addGdp)

// 直接值
.set(ChinaRegion_.regionGdp, gdpAmount)

// 使用表达式
.set(ChinaRegion_.regionGdp, negate(ChinaRegion_.regionGdp))
```

##### 批量 SET - setSpace 系列

```java
// UpdateStatement.java line 80-85
interface _StaticBatchSetClause<F extends SqlField, SR> extends _StaticSetClause<F, SR> {

    SR setSpace(F field, BiFunction<F, String, Expression> valueOperator);

    SR setSpace(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator,
                BiFunction<F, String, Expression> valueOperator);
}
```

**使用场景**: 用于批量更新操作，使用命名参数占位符。

##### 动态 SET - sets 形式

```java
// UpdateStatement.java line 51-53
interface _DynamicSetClause<B extends _ItemPairBuilder, SR> {
    SR sets(Consumer<B> consumer);
}
```

**使用示例**:
```java
.sets(s -> s.set(PillUser_.identityType, SQLs::literal, IdentityType.PERSON)
             .set(PillUser_.identityId, SQLs::literal, 888)
             .set(PillUser_.nickName, SQLs::param, "令狐冲"))
```

**重要规则**:
- SET 子句必须至少有一个 set 操作
- 可以连续调用多个 set 方法
- 使用 `_WhereSpec` 接口，既可以继续 set，也可以进入 where

---

### ③ WHERE 子句

#### 静态 WHERE 形式

```java
// Statement.java line 767-816
interface _WhereClause<WR, WA> extends _MinWhereClause<WR, WA> {

    WA where(IPredicate predicate);

    <T> WA where(Function<T, IPredicate> expOperator, T operand);

    WA whereIf(Supplier<IPredicate> supplier);

    // ... 更多 ifWhereIf 重载
}
```

**使用示例**:
```java
// 简单条件
.where(ChinaRegion_.id.equal(SQLs::literal, 1))

// BETWEEN
.where(ChinaRegion_.id.between(SQLs::literal, map.get("firstId"), AND, map.get("secondId")))

// 复杂条件组合
.where(ChinaRegion_.id.equal(SQLs::literal, 1))
.and(ChinaRegion_.name.equal(SQLs::param, "江湖"))
.and(ChinaRegion_.regionGdp.plus(SQLs::literal, gdpAmount).greaterEqual(0))
```

#### 动态 WHERE 形式

```java
WR where(Consumer<Consumer<IPredicate>> consumer);
```

使用 Consumer 风格构建多个 WHERE 条件，直接进入下一步（asUpdate），不经过 AND 阶段。

---

### ④ AND 子句

```java
// UpdateStatement.java line 145-168
interface _UpdateWhereAndClause<WA> extends Statement._WhereAndClause<WA> {

    <T> WA ifAnd(Function<T, Expression> expOperator1, @Nullable T operand1,
                 BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand);

    <T> WA ifAnd(ExpressionOperator<TypedExpression, T, Expression> expOperator1,
                 BiFunction<TypedExpression, T, Expression> operator, @Nullable T operand1,
                 BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand);
}
```

**特点**:
- AND 子句可以重复调用多次
- 支持 ifAnd 条件形式
- `_UpdateWhereAndClause` 扩展了标准 `_WhereAndClause`，增加了对数值操作数的额外支持

---

### ⑤ asUpdate() — 链尾

```java
// Statement.java line 1411-1414
interface _DmlUpdateSpec<I extends Item> extends Item {
    I asUpdate();
}
```

**实现**:
```java
// SingleUpdateStatement.java line 57-60
@Override
public final I asUpdate() {
    this.endUpdateStatement();
    return this.onAsUpdate();
}
```

**最终类型**: `Update` — 可传给 session 执行的更新语句对象。

---

## 实现类继承链

```
SingleUpdateStatement (抽象基类)
  └─ StandardUpdates.SimpleSingleUpdate (抽象)
      ├─ StandardUpdates.StandardSimpleUpdate (非 Domain 单表更新)
      └─ StandardUpdates.DomainUpdateStatement (抽象，Domain 更新基类)
          ├─ StandardUpdates.DomainSimpleUpdate (Domain 单表更新)
          └─ StandardUpdates.DomainBatchUpdate (Domain 批量更新)
```

**DomainUpdateStatement 特点**:
- 支持子表更新
- 继承自 `SimpleSingleUpdate`
- 实现 `_DomainUpdate` 接口
- 管理子表字段配对列表

---

## 关键约束和规则

### 层级约束（通过接口链强制执行）

1. **必须先调用 update**: `SQLs.domainUpdate()` 返回的接口只有 `update` 方法，强制你先指定要更新的表
2. **必须至少有一个 set**: `_StandardSetClause` 只提供 set 相关方法，不允许直接跳到 where
3. **SET 之后可以选择继续 SET 或进入 WHERE**: `_WhereSpec` 同时继承 `_StandardSetClause` 和 `_StandardWhereClause`，允许你选择继续 set 或开始 where
4. **WHERE 之后可以选择 AND 或直接结束**:
   - 静态 where 返回 `_WhereAndSpec`，允许继续 and
   - 动态 where 返回 `_DmlUpdateSpec`，直接允许 asUpdate
5. **结束链必须调用 asUpdate**: 只有 `_DmlUpdateSpec` 提供 asUpdate 方法

### 不可重复 vs 可重复

| 阶段 | 是否可重复 | 说明 |
|------|-----------|------|
| UPDATE | ❌ 不可 | 只能指定一个表进行更新 |
| SET | ✅ 可重复 | 可以连续调用多个 set 方法设置多个字段 |
| WHERE | ❌ 不可 | 只能有一个 WHERE 关键字 |
| AND | ✅ 可重复 | 可以有多个 AND 条件 |
| asUpdate | ❌ 不可 | 链尾，只能调用一次 |

### Domain API 特有约束

1. **不支持 WITH 子句**: Domain API 设计上不提供 WITH 子句支持
2. **支持子表继承**: 使用 `ChildTableMeta` 可以更新继承自父表的字段
3. **类型推断**: 根据传入的 `TableMeta` 类型，SET 阶段的字段类型会有相应的类型约束

---

## 完整使用示例

### 示例 1: 基础更新（来自 StandardUpdateUnitTests）

```java
@Test
public void updateChild() {
    final BigDecimal gdpAmount = new BigDecimal("888.8");
    final ChinaRegion<?> criteria = new ChinaRegion<>();

    criteria.setRegionGdp(gdpAmount);
    final Update stmt;
    stmt = SQLs.domainUpdate()
        .update(ChinaProvince_.T, AS, "p")
        .set(ChinaRegion_.regionGdp, gdpAmount)
        .set(ChinaRegion_.regionGdp, negate(ChinaRegion_.regionGdp))
        .set(ChinaRegion_.name, SQLs::param, "武侠江湖")
        .set(ChinaRegion_.regionGdp, SQLs::plusEqual, gdpAmount)
        .where(ChinaRegion_.id.equal(SQLs::literal, 1))
        .and(ChinaRegion_.name.equal(SQLs::param, "江湖"))
        .and(ChinaRegion_.regionGdp.plus(SQLs::literal, gdpAmount).greaterEqual(0))
        .asUpdate();

    printStmt(LOG, stmt);
}
```

### 示例 2: 动态 SET

```java
@Test
public void dynamicSetUpdateOnlyParentField() {
    final UpdateStatement stmt;
    stmt = SQLs.domainUpdate()
        .update(PillUser_.T, AS, "up")
        .sets(s -> s.set(PillUser_.identityType, SQLs::literal, IdentityType.PERSON)
                     .set(PillUser_.identityId, SQLs::literal, 888)
                     .set(PillUser_.nickName, SQLs::param, "令狐冲"))
        .where(PillUser_.id.equal(SQLs::literal, "1"))
        .and(PillUser_.nickName.equal(SQLs::param, "zoro"))
        .asUpdate();

    printStmt(LOG, stmt);
}
```

### 示例 3: 条件 SET

```java
SQLs.domainUpdate()
    .update(User_.T, "u")
    .set(User_.name, "新名称")
    .ifSet(User_.email, emailValue)  // 仅当 emailValue != null 时设置
    .where(User_.id.equal(SQLs::param, userId))
    .asUpdate();
```

---

## 与 singleUpdate() 的对比

| 特性 | domainUpdate() | singleUpdate() |
|------|---------------|----------------|
| WITH 子句 | ❌ 不支持 | ✅ 支持 |
| 子表继承支持 | ✅ 完整支持 | ✅ 支持但类型约束不同 |
| 表别名指定方式 | 两种形式（带/不带 AS） | 仅带 AS 形式 |
| 返回类型设计 | 针对 Domain 场景优化 | 标准 SQL 场景 |

---

## 常见问题

### Q: domainUpdate() 和 singleUpdate() 该如何选择？

A:
- 如果你需要更新有继承关系的子表，或者希望使用更灵活的类型推断 → 使用 `domainUpdate()`
- 如果你需要 WITH 子句，或者需要构建标准 SQL 更新语句 → 使用 `singleUpdate()`

### Q: 如果没有 WHERE 条件会怎样？

A: 会更新表中的所有行！这是合法的 SQL，但通常不是你想要的。API 允许这种情况（WHERE 是可选的）。

### Q: 可以在 SET 中使用子查询吗？

A: 本 Skill 聚焦于 domainUpdate() 的方法链，关于子查询在 SET 中的使用，请参考 Army 的 RowSet 相关接口（`_StaticRowSetClause`）。

---

## 更新日志

- **v1.0** (2026-06-06): 初始版本，基于 Army Criteria API 代码分析创建
