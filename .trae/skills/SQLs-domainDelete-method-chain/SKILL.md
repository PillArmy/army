---
name: SQLs-domainDelete-method-chain
description: 完整的 Army Criteria API SQLs.domainDelete() 方法链知识。用于理解、解释或记录从 SQLs.domainDelete() 到 asDelete() 的完整 DELETE 语句构建流程。涵盖每一个接口、每一个方法和每一个合法路径。
---

# SQLs.domainDelete() 方法链 - 完整参考

&gt; **适用范围**：本 Skill **仅限**用于理解、解释、记录 `SQLs.domainDelete()` 入口的标准 DELETE 语句构建流程。
&gt; 覆盖从 `SQLs.domainDelete()` 到 `asDelete()` 的完整方法链。**不**涵盖 WITH 子句、方言特有语法或单表删除。

&gt; **源码依据**：本 Skill 基于以下核心源文件编写——`StandardDelete.java`（接口定义）、`StandardDeletes.java`（核心实现）、`Statement.java`（公共接口）。所有描述均以实际源码接口签名和实现为准。

## 核心入口

```java
// SQLs.java
public static StandardDelete._DomainDeleteClause&lt;Delete&gt; domainDelete() {
    return StandardDeletes.domainDelete();
}

public static StandardDelete._DomainDeleteClause&lt;Statement._BatchDeleteParamSpec&gt; batchDomainDelete() {
    return StandardDeletes.batchDomainDelete();
}
```

**返回类型**：`StandardDelete._DomainDeleteClause&lt;Delete&gt;` - 实现类是 `StandardDeletes.DomainDeleteStatement`。

---

## 完整方法链 Diagram

```
SQLs.domainDelete()  →  StandardDelete._DomainDeleteClause&lt;Delete&gt;
  │
  ├─① deleteFrom (必须，仅一次)
  │  └─ .deleteFrom(TableMeta&lt;?&gt; table, SQLs.WordAs as, String tableAlias)  →  StandardDelete._WhereSpec&lt;Delete&gt;
  │
  ├─② WHERE (可选)
  │  ├─ .where(IPredicate predicate)                                      →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  ├─ .where(Function&lt;T, IPredicate&gt; expOperator, T operand)             →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  ├─ .whereIf(Supplier&lt;IPredicate&gt; supplier)                           →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  ├─ .whereIf(Function&lt;T, IPredicate&gt; expOperator, @Nullable T value)   →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  ├─ .whereIf(ExpressionOperator&lt;TypedExpression, T, IPredicate&gt; expOperator,
  │  │          BiFunction&lt;TypedExpression, T, Expression&gt; operator, @Nullable T value)  →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  ├─ .whereIf(BiFunction&lt;SQLs.BiOperator, T, IPredicate&gt; expOperator, 
  │  │          SQLs.BiOperator operator, @Nullable T value)  →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  ├─ .whereIf(TeFunction&lt;SQLs.BiOperator, BiFunction&lt;TypedExpression, T, Expression&gt;, T, IPredicate&gt; expOperator,
  │  │          SQLs.BiOperator operator, BiFunction&lt;TypedExpression, T, Expression&gt; func, @Nullable T value)  →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  ├─ .whereIf(BetweenValueOperator&lt;T&gt; expOperator,
  │  │          BiFunction&lt;TypedExpression, T, Expression&gt; operator,
  │  │          @Nullable T value1, SQLs.WordAnd and, @Nullable T value2)  →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  ├─ .whereIf(BetweenDualOperator&lt;T, U&gt; expOperator,
  │  │          BiFunction&lt;TypedExpression, T, Expression&gt; firstFuncRef,
  │  │          @Nullable T first, SQLs.WordAnd and,
  │  │          BiFunction&lt;TypedExpression, U, Expression&gt; secondFuncRef,
  │  │          @Nullable U second)  →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  └─ .where(Consumer&lt;Consumer&lt;IPredicate&gt;&gt; consumer)                  →  Statement._DmlDeleteSpec&lt;Delete&gt;
  │
  ├─③ AND (可选，可重复)
  │  ├─ .and(IPredicate predicate)                                          →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  ├─ .and(Function&lt;T, IPredicate&gt; expOperator, T operand)               →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  ├─ .ifAnd(Supplier&lt;IPredicate&gt; supplier)                              →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  ├─ .ifAnd(Function&lt;T, IPredicate&gt; expOperator, @Nullable T value)     →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  ├─ .ifAnd(ExpressionOperator&lt;TypedExpression, T, IPredicate&gt; expOperator,
  │  │          BiFunction&lt;TypedExpression, T, Expression&gt; operator, @Nullable T value)  →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  ├─ .ifAnd(BiFunction&lt;SQLs.BiOperator, T, IPredicate&gt; expOperator, 
  │  │          SQLs.BiOperator operator, @Nullable T value)  →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  ├─ .ifAnd(TeFunction&lt;SQLs.BiOperator, BiFunction&lt;TypedExpression, T, Expression&gt;, T, IPredicate&gt; expOperator,
  │  │          SQLs.BiOperator operator, BiFunction&lt;TypedExpression, T, Expression&gt; func, @Nullable T value)  →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  ├─ .ifAnd(BetweenValueOperator&lt;T&gt; expOperator,
  │  │          BiFunction&lt;TypedExpression, T, Expression&gt; operator,
  │  │          @Nullable T value1, SQLs.WordAnd and, @Nullable T value2)  →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │  └─ .ifAnd(BetweenDualOperator&lt;T, U&gt; expOperator,
  │  │          BiFunction&lt;TypedExpression, T, Expression&gt; firstFuncRef,
  │  │          @Nullable T first, SQLs.WordAnd and,
  │  │          BiFunction&lt;TypedExpression, U, Expression&gt; secondFuncRef,
  │  │          @Nullable U second)  →  StandardDelete._WhereAndSpec&lt;Delete&gt;
  │
  └─④ 结尾: asDelete()
     └─ .asDelete()  →  Delete (可执行删除语句)
```

---

## 逐层接口详解

### 0. 入口: `StandardDelete._DomainDeleteClause&lt;I extends Item&gt;`

```java
// StandardDelete.java
interface _DomainDeleteClause&lt;I extends Item&gt; extends Item {
    _WhereSpec&lt;I&gt; deleteFrom(TableMeta&lt;?&gt; table, SQLs.WordAs as, String tableAlias);
}
```

`_DomainDeleteClause` 是 domainDelete 的入口接口，提供唯一的 `deleteFrom` 方法。

---

### ① deleteFrom (必须，仅一次)

&gt; **语义约束**：`deleteFrom` 只能调用一次。类型系统确保在调用 deleteFrom 后无法再次调用。

```java
// 接口: StandardDelete._DomainDeleteClause
// 实现: StandardDeletes.DomainDeleteStatement
.deleteFrom(ChinaRegion_.T, AS, "c")
```

**参数说明**：
- `table`：表元数据（TableMeta&lt;?&gt;），支持单表、父表、子表
- `as`：`SQLs.AS` 关键字
- `tableAlias`：表别名

**返回类型**：`StandardDelete._WhereSpec&lt;I&gt;` - 进入 WHERE 子句构建阶段

**使用场景**：
- 单表删除：使用单表元数据（SingleTableMeta）
- 父表删除：使用父表元数据（ParentTableMeta）
- 子表删除：使用子表元数据（ChildTableMeta）

---

### ② WHERE (可选)

#### 接口定义

```java
// Statement.java
interface _WhereClause&lt;WR, WA&gt; extends _MinWhereClause&lt;WR, WA&gt; {
    // 静态形式
    WA where(IPredicate predicate);
    &lt;T&gt; WA where(Function&lt;T, IPredicate&gt; expOperator, T operand);
    
    // 条件形式
    WA whereIf(Supplier&lt;IPredicate&gt; supplier);
    &lt;T&gt; WA whereIf(Function&lt;T, IPredicate&gt; expOperator, @Nullable T value);
    &lt;T&gt; WA whereIf(ExpressionOperator&lt;TypedExpression, T, IPredicate&gt; expOperator,
                   BiFunction&lt;TypedExpression, T, Expression&gt; operator, @Nullable T value);
    &lt;T&gt; WA whereIf(BiFunction&lt;SQLs.BiOperator, T, IPredicate&gt; expOperator,
                   SQLs.BiOperator operator, @Nullable T value);
    &lt;T&gt; WA whereIf(TeFunction&lt;SQLs.BiOperator, BiFunction&lt;TypedExpression, T, Expression&gt;, T, IPredicate&gt; expOperator,
                   SQLs.BiOperator operator, BiFunction&lt;TypedExpression, T, Expression&gt; func, @Nullable T value);
    &lt;T&gt; WA whereIf(BetweenValueOperator&lt;T&gt; expOperator,
                   BiFunction&lt;TypedExpression, T, Expression&gt; operator,
                   @Nullable T value1, SQLs.WordAnd and, @Nullable T value2);
    &lt;T, U&gt; WA whereIf(BetweenDualOperator&lt;T, U&gt; expOperator,
                      BiFunction&lt;TypedExpression, T, Expression&gt; firstFuncRef,
                      @Nullable T first, SQLs.WordAnd and,
                      BiFunction&lt;TypedExpression, U, Expression&gt; secondFuncRef,
                      @Nullable U second);
    
    // 动态形式
    WR where(Consumer&lt;Consumer&lt;IPredicate&gt;&gt; consumer);
}
```

#### 返回值说明：
- `.where(IPredicate)` / `.where(Function, T)` → `_WhereAndSpec&lt;I&gt;` (可继续 `.and(...)`)
- `.where(Consumer)` → `_DmlDeleteSpec&lt;I&gt;` (直接进入 asDelete 阶段)

#### WHERE 子句形式总览

| 形式 | 方法签名 | 返回类型 | 使用场景 |
|------|---------|---------|---------|
| **静态 IPredicate** | `where(IPredicate)` | `_WhereAndSpec&lt;I&gt;` | 已知条件，可继续 AND |
| **方法引用** | `where(Function&lt;T, IPredicate&gt;, T)` | `_WhereAndSpec&lt;I&gt;` | 方法引用风格，最常用 |
| **条件 Supplier** | `whereIf(Supplier&lt;IPredicate&gt;)` | `_WhereAndSpec&lt;I&gt;` | 条件性添加 WHERE |
| **条件方法引用** | `whereIf(Function&lt;T, IPredicate&gt;, @Nullable T)` | `_WhereAndSpec&lt;I&gt;` | 条件性添加，值可为 null |
| **ExpressionOperator** | `whereIf(ExpressionOperator, BiFunction, @Nullable T)` | `_WhereAndSpec&lt;I&gt;` | 表达式操作符风格 |
| **BiOperator** | `whereIf(BiFunction&lt;BiOperator, T, IPredicate&gt;, BiOperator, @Nullable T)` | `_WhereAndSpec&lt;I&gt;` | 二元操作符风格 |
| **TeFunction** | `whereIf(TeFunction, BiOperator, BiFunction, @Nullable T)` | `_WhereAndSpec&lt;I&gt;` | 三元函数风格 |
| **BetweenValue** | `whereIf(BetweenValueOperator, BiFunction, @Nullable T, WordAnd, @Nullable T)` | `_WhereAndSpec&lt;I&gt;` | BETWEEN 值操作符 |
| **BetweenDual** | `whereIf(BetweenDualOperator, BiFunction, @Nullable T, WordAnd, BiFunction, @Nullable U)` | `_WhereAndSpec&lt;I&gt;` | BETWEEN 双值操作符 |
| **动态 Consumer** | `where(Consumer&lt;Consumer&lt;IPredicate&gt;&gt;)` | `_DmlDeleteSpec&lt;I&gt;` | 多个动态条件，跳过 AND 阶段 |

#### 使用示例

```java
// 静态 IPredicate
.where(ChinaRegion_.id.equal(SQLs::literal, 1))

// 方法引用风格（最常用）
.where(ChinaRegion_.id::equal, 1)
.where(ChinaRegion_.id.between(SQLs::literal, 1, AND, 10))

// Consumer 风格（多条件）
.where(consumer -&gt; {
    consumer.accept(ChinaRegion_.id.equal(SQLs::literal, 1));
    consumer.accept(ChinaRegion_.name.equal(SQLs::param, "test"));
})

// whereIf 条件风格
.whereIf(() -&gt; shouldDelete ? ChinaRegion_.id.equal(SQLs::literal, 1) : null)
.whereIf(ChinaRegion_.id::equal, nullableId)
```

---

### ③ AND (可选，可重复)

#### 接口定义

```java
// Statement.java
interface _WhereAndClause&lt;WA&gt; extends _MinWhereAndClause&lt;WA&gt; {
    WA and(IPredicate predicate);
    &lt;T&gt; WA and(Function&lt;T, IPredicate&gt; expOperator, T operand);
    WA ifAnd(Supplier&lt;IPredicate&gt; supplier);
    &lt;T&gt; WA ifAnd(Function&lt;T, IPredicate&gt; expOperator, @Nullable T value);
    &lt;T&gt; WA ifAnd(ExpressionOperator&lt;TypedExpression, T, IPredicate&gt; expOperator,
                 BiFunction&lt;TypedExpression, T, Expression&gt; operator, @Nullable T value);
    &lt;T&gt; WA ifAnd(BiFunction&lt;SQLs.BiOperator, T, IPredicate&gt; expOperator,
                 SQLs.BiOperator operator, @Nullable T value);
    &lt;T&gt; WA ifAnd(TeFunction&lt;SQLs.BiOperator, BiFunction&lt;TypedExpression, T, Expression&gt;, T, IPredicate&gt; expOperator,
                 SQLs.BiOperator operator, BiFunction&lt;TypedExpression, T, Expression&gt; func, @Nullable T value);
    &lt;T&gt; WA ifAnd(BetweenValueOperator&lt;T&gt; expOperator,
                 BiFunction&lt;TypedExpression, T, Expression&gt; operator,
                 @Nullable T value1, SQLs.WordAnd and, @Nullable T value2);
    &lt;T, U&gt; WA ifAnd(BetweenDualOperator&lt;T, U&gt; expOperator,
                    BiFunction&lt;TypedExpression, T, Expression&gt; firstFuncRef,
                    @Nullable T first, SQLs.WordAnd and,
                    BiFunction&lt;TypedExpression, U, Expression&gt; secondFuncRef,
                    @Nullable U second);
}
```

**返回类型**：始终返回 `_WhereAndSpec&lt;I&gt;`，允许链式调用多个 AND

#### AND 子句形式总览

| 形式 | 方法签名 | 使用场景 |
|------|---------|---------|
| **静态 IPredicate** | `and(IPredicate)` | 已知条件 |
| **方法引用** | `and(Function&lt;T, IPredicate&gt;, T)` | 方法引用风格，最常用 |
| **条件 Supplier** | `ifAnd(Supplier&lt;IPredicate&gt;)` | 条件性添加 AND |
| **条件方法引用** | `ifAnd(Function&lt;T, IPredicate&gt;, @Nullable T)` | 条件性添加，值可为 null |
| **ExpressionOperator** | `ifAnd(ExpressionOperator, BiFunction, @Nullable T)` | 表达式操作符风格 |
| **BiOperator** | `ifAnd(BiFunction&lt;BiOperator, T, IPredicate&gt;, BiOperator, @Nullable T)` | 二元操作符风格 |
| **TeFunction** | `ifAnd(TeFunction, BiOperator, BiFunction, @Nullable T)` | 三元函数风格 |
| **BetweenValue** | `ifAnd(BetweenValueOperator, BiFunction, @Nullable T, WordAnd, @Nullable T)` | BETWEEN 值操作符 |
| **BetweenDual** | `ifAnd(BetweenDualOperator, BiFunction, @Nullable T, WordAnd, BiFunction, @Nullable U)` | BETWEEN 双值操作符 |

#### 使用示例

```java
// 静态 IPredicate
.and(ChinaRegion_.name.equal(SQLs::param, "江湖"))

// 方法引用风格
.and(ChinaRegion_.name::equal, "江湖")

// ifAnd 条件风格
.ifAnd(() -&gt; shouldFilter ? ChinaRegion_.name.equal(SQLs::param, "江湖") : null)
.ifAnd(ChinaRegion_.name::equal, nullableName)

// 复合条件（OR 组）
.and(ChinaProvince_.governor.equal(SQLs::param, "石教主").orGroup(consumer -&gt; {
    consumer.accept(ChinaProvince_.governor.equal(SQLs::param, "钟教主"));
    consumer.accept(ChinaProvince_.governor.equal(SQLs::param, "老钟"));
}))
```

---

### ④ 结尾: asDelete()

```java
// Statement._DmlDeleteSpec
interface _DmlDeleteSpec&lt;I extends Item&gt; extends Item {
    I asDelete();
}
```

**返回类型**：`Delete` - 可执行的删除语句对象，可传入 `session.update(delete)` 执行。

---

## 完整示例

### 单表删除

```java
final Delete stmt = SQLs.domainDelete()
    .deleteFrom(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.id.between(SQLs::literal, 1, AND, 10))
    .and(ChinaRegion_.name.equal(SQLs::param, "江湖"))
    .asDelete();

final long rows = session.update(stmt);
```

### 子表删除

```java
final Delete stmt = SQLs.domainDelete()
    .deleteFrom(ChinaProvince_.T, AS, "p")
    .where(ChinaProvince_.id.equal(SQLs::literal, 1))
    .and(ChinaRegion_.name.equal(SQLs::param, "江湖"))
    .and(ChinaProvince_.governor.equal(SQLs::param, "石教主").orGroup(consumer -&gt; {
        consumer.accept(ChinaProvince_.governor.equal(SQLs::param, "钟教主"));
        consumer.accept(ChinaProvince_.governor.equal(SQLs::param, "老钟"));
    }))
    .asDelete();
```

### 批量删除

```java
final BatchDelete stmt = SQLs.batchDomainDelete()
    .deleteFrom(ChinaRegion_.T, AS, "cr")
    .where(ChinaRegion_.id::spaceEqual, SQLs::namedParam)
    .and(ChinaRegion_.version.equal(SQLs::param, "0"))
    .asDelete()
    .namedParamList(paramList);

final List&lt;Long&gt; rowList = session.batchUpdate(stmt);
```

### 动态 WHERE 条件

```java
final Delete stmt = SQLs.domainDelete()
    .deleteFrom(ChinaRegion_.T, AS, "c")
    .where(consumer -&gt; {
        consumer.accept(ChinaRegion_.id.in(SQLs::rowParam, idList));
        consumer.accept(ChinaRegion_.createTime.between(
            SQLs::param, startTime, AND, endTime
        ));
    })
    .asDelete();
```

---

## 核心约束与规则

### 层级约束（通过接口链强制执行）

接口名称本身就编码了当前上下文中可用的方法，DSL 通过**返回类型引导**下一个可调用的方法：
- `_DomainDeleteClause` → 只能调用 `deleteFrom`
- `_WhereSpec` → 只能调用 `where*` 系列方法
- `_WhereAndSpec` → 只能调用 `and*` 系列方法或 `asDelete`
- `_DmlDeleteSpec` → 只能调用 `asDelete`

### 语义约束

| 约束 | 说明 |
|------|------|
| deleteFrom 只能调用一次 | 类型系统确保，再次调用会编译错误 |
| WHERE 可选但建议 | 无 WHERE 会删除整张表 |
| AND 可重复 | 支持多个 AND 条件链式调用 |
| asDelete 必须最后调用 | 结束语句构建，返回可执行对象 |

### domainDelete 与 singleDelete 的区别

| 特性 | domainDelete | singleDelete |
|------|-------------|-------------|
| 支持的表类型 | TableMeta（单表、父表、子表） | SingleTableMeta（仅单表） |
| WITH 子句 | ❌ 不支持 | ✅ 支持 |
| 域模型感知 | ✅ 支持父子表级联 | ❌ 仅单表 |
| 推荐场景 | 域模型删除 | 标准单表删除 |

---

## 实现类继承链

```
StandardDeletes (抽象基类)
  ├─ DomainDeleteStatement (抽象)
  │  ├─ DomainSimpleDelete (非批量)
  │  └─ DomainBatchDelete (批量)
```

