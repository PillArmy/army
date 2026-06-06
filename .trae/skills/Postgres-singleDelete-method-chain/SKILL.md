---
name: Postgres.singleDelete() 方法链
description: 完整的 Army Criteria API Postgres.singleDelete() 方法链知识。用于理解、解释或记录从 Postgres.singleDelete() 到 asDelete()/asReturningDelete() 的完整 DELETE 语句构建流程，包括 PostgreSQL 特有的 USING、RETURNING、TABLESAMPLE 等语法。
---

# Postgres.singleDelete() 方法链 — 完整参考

> **适用范围**: 本技能 **仅限** 用于理解、解释、记录 `Postgres.singleDelete()` 入口的 PostgreSQL 特定 DELETE 语句构建流程。覆盖从 `Postgres.singleDelete()` 到 `asDelete()`/`asReturningDelete()` 的完整方法链，包括 WITH、DELETE FROM、USING、JOIN、WHERE、AND、RETURNING 等阶段。
> **不** 涵盖 batchSingleDelete、标准 SQLs.singleDelete。

> **源码依据**: 本技能基于以下核心源文件编写——`Postgres.java`（入口定义）、`PostgreDelete.java`（PostgreSQL DELETE 接口组合）、`PostgreDeletes.java`（实现）、`_PostgreDelete.java`（内部实现）、`DeleteUnitTests.java`（示例）。所有描述均以实际源码接口签名和实现为准。

## 核心入口

```java
// Postgres.java line 262-264
public static PgSingleDeleteSpec<Delete, ReturningDelete> singleDelete() {
    return PostgreDeletes.simpleDelete();
}
```

**返回类型**: `PgSingleDeleteSpec<Delete, ReturningDelete>` — 实现类是 `PostgreDeletes.PrimarySimpleDelete`。

**内部实现**: `PostgreDeletes.simpleDelete()`:

```java
// PostgreDeletes.java line 73-75
static PgSingleDeleteSpec<Delete, ReturningDelete> simpleDelete() {
    return new PrimarySimpleDelete();
}
```

---

## 完整方法链 Diagram

> **阅读指南**: 每个叶子节点代表一个可直接调用的方法，带完整参数列表。接口链通过返回类型导航。

```
Postgres.singleDelete() → PgSingleDeleteSpec<Delete, ReturningDelete>
│
├─① WITH (可选，仅一次声明；comma 追加 CTE 可重复)
│  ├─ 静态 WITH:
│  │  ├─ .with(String name)       → PostgreQuery._StaticCteParensSpec
│  │  └─ .withRecursive(String name) → PostgreQuery._StaticCteParensSpec
│  │     └─ (然后: .parens() → .as() → .space() 进入 DELETE FROM)
│  │
│  ├─ 动态 WITH:
│  │  ├─ .with(Consumer<PostgreCtes> consumer)       → PgSingleDeleteClause
│  │  ├─ .withRecursive(Consumer<PostgreCtes> consumer) → PgSingleDeleteClause
│  │  ├─ .ifWith(Consumer<PostgreCtes> consumer)      → PgSingleDeleteClause
│  │  └─ .ifWithRecursive(Consumer<PostgreCtes> consumer) → PgSingleDeleteClause
│
└─② DELETE FROM (必须，仅一次，不可重复)
   ├─ .deleteFrom(TableMeta<?> table, SQLs.WordAs as, String tableAlias) 
   │     → PostgreDelete._SingleUsingSpec
   ├─ .deleteFrom(SQLs.WordOnly only, TableMeta<?> table, SQLs.WordAs as, String tableAlias) 
   │     → PostgreDelete._SingleUsingSpec
   └─ .deleteFrom(TableMeta<?> table, SQLs.SymbolAsterisk star, SQLs.WordAs as, String tableAlias) 
         → PostgreDelete._SingleUsingSpec
      │
      └─③ USING/JOIN (可选，可重复)
         ├─ USING 子句:
         │  ├─ .using(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias)
         │  ├─ .using(SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias)
         │  ├─ .using(DerivedTable derivedTable)
         │  ├─ .using(Supplier<DerivedTable> supplier)
         │  ├─ .using(SQLs.DerivedModifier modifier, DerivedTable derivedTable)
         │  ├─ .using(String cteName)
         │  ├─ .using(String cteName, SQLs.WordAs wordAs, String alias)
         │  ├─ .using(Function<_NestedLeftParenSpec<_SingleJoinSpec>, _SingleJoinSpec> function)
         │  └─ (更多 using 重载形式)
         │
         ├─ JOIN 子句:
         │  ├─ .join(TableMeta<?> table, SQLs.WordAs as, String tableAlias)
         │  ├─ .leftJoin(...) / .rightJoin(...) / .fullJoin(...) / .crossJoin(...)
         │  ├─ .ifLeftJoin(Consumer<PostgreJoins> consumer)
         │  ├─ .ifJoin(...) / .ifRightJoin(...) / .ifFullJoin(...) / .ifCrossJoin(...)
         │  └─ (每种 join 都有 table/derived/cte/nested/undone-function 形式)
         │
         ├─ TABLESAMPLE (可选，可重复):
         │  ├─ .tableSample(Expression method)
         │  ├─ .tableSample(BiFunction<...> method, ...)
         │  └─ .ifTableSample(...)
         │
         ├─ REPEATABLE (可选，可重复):
         │  ├─ .repeatable(Expression seed)
         │  ├─ .repeatable(Function<Number, Expression> valueOperator, Number seedValue)
         │  └─ .ifRepeatable(...)
         │
         └─④ WHERE (可选)
            ├─ .where(IPredicate predicate)                  → _SingleWhereAndSpec
            ├─ .where(Function<T, IPredicate> expOperator, T operand) → _SingleWhereAndSpec
            ├─ .whereIf(Supplier<IPredicate> supplier)          → _SingleWhereAndSpec
            ├─ .where(Consumer<Consumer<IPredicate>> consumer) → _ReturningSpec (跳过 AND)
            ├─ .whereCurrentOf(String cursorName)              → _ReturningSpec
            └─ (更多 whereIf 重载形式)
            │
            └─⑤ AND (可选，可重复)
               ├─ .and(IPredicate predicate)                  → _SingleWhereAndSpec
               ├─ .and(Function<T, IPredicate> expOperator, T operand) → _SingleWhereAndSpec
               ├─ .ifAnd(Supplier<IPredicate> supplier)          → _SingleWhereAndSpec
               └─ (更多 ifAnd 重载形式)
               │
               └─⑥ RETURNING (可选)
                  ├─ .returningAll()                              → _DqlDeleteSpec
                  ├─ .returning(Consumer<Returnings> consumer)     → _DqlDeleteSpec
                  ├─ .returning(Selection selection)               → _StaticReturningCommaSpec
                  ├─ .returning(Selection selection1, Selection selection2) → _StaticReturningCommaSpec
                  ├─ .returning(Function<String, Selection> function, String alias)
                  ├─ .returning(String derivedAlias, SQLs.SymbolDot period, SQLs.SymbolAsterisk star)
                  ├─ .returning(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table)
                  ├─ .returning(TableField field1, TableField field2, TableField field3)
                  └─ (更多 returning 重载形式)
                  │
                  └─⑦ COMMA (可选，可重复，RETURNING 后)
                     ├─ .comma(Selection selection)
                     ├─ .comma(Selection selection1, Selection selection2)
                     └─ (与 returning 相同的所有重载形式)
                     │
                     └─⑧ 结束:
                        ├─ .asDelete() → Delete (无 RETURNING)
                        └─ .asReturningDelete() → ReturningDelete (有 RETURNING)
```

---

## 逐层接口详解

### 0. 入口: `PgSingleDeleteSpec<Delete, ReturningDelete>`

```java
// PgSingleDeleteSpec.java
public interface PgSingleDeleteSpec<I extends Item, Q extends Item> 
        extends PgSingleDeleteClause<I, Q>,
                PostgreStatement._PostgreDynamicWithClause<PgSingleDeleteClause<I, Q>>,
                PostgreQuery._PostgreStaticWithClause<PgSingleDeleteClause<I, Q>> {
}
```

`PgSingleDeleteSpec` 组合了三种能力：动态 CTE、静态 CTE、DELETE FROM。所以 `Postgres.singleDelete()` 返回的对象可以直接 `.deleteFrom()`，也可以先用 `.with()` 定义 CTE。

---

### ① WITH (Common Table Expression)

> **语义约束**: `WITH` 关键字仅出现一次（即 `.with(name)` / `.withRecursive(name)` 只能调用一次）。后续 CTE 通过 `_CteComma.comma(String name)` 追加。`.space()` 是唯一退出 CTE 链、进入 DELETE FROM 的路径。
> **禁止** 在 `.space()` 后再调用 `.with(name)`——类型系统不提供此路径。

**接口**: `_PostgreDynamicWithClause` + `_PostgreStaticWithClause`

**动态 CTE (运行时构建)**:

```java
Postgres.singleDelete()
    .with(builder -> {
        builder.comma("cte1").as(s -> s.select(...).from(...).asQuery());
        builder.comma("cte2").as(s -> s.select(...).from(...).asQuery());
    })
    .deleteFrom(...)
```

**条件 CTE**:

```java
Postgres.singleDelete()
    .ifWith(builder -> { ... })        // 仅当 consumer 内有实际操作时执行
    .ifWithRecursive(builder -> { ... }) // 递归条件 CTE
```

**静态 CTE**:

```java
Postgres.singleDelete()
    .with("cte_name")
    .parens(s -> s.select(...).from(...).asQuery())
    .as()
    .space()
    .deleteFrom(...)
```

---

### ② DELETE FROM (核心)

```java
// PgSingleDeleteClause.java
public interface PgSingleDeleteClause<I extends Item, Q extends Item> extends Item {
    PostgreDelete._SingleUsingSpec<I, Q> deleteFrom(TableMeta<?> table, SQLs.WordAs as, String tableAlias);
    
    PostgreDelete._SingleUsingSpec<I, Q> deleteFrom(@Nullable SQLs.WordOnly only, TableMeta<?> table, SQLs.WordAs as, String tableAlias);
    
    PostgreDelete._SingleUsingSpec<I, Q> deleteFrom(TableMeta<?> table, @Nullable SQLs.SymbolAsterisk star, SQLs.WordAs as, String tableAlias);
}
```

> **规则**:
> - **必须调用一次，且仅能调用一次**
> - `SQLs.WordAs` 固定传入 `SQLs.AS`
> - `tableAlias` 必须提供非空字符串
> - 不可重复调用——类型系统和实现都强制执行此约束

**三种形式说明**:

| 形式 | 修饰符 | 说明 |
|-----|-------|------|
| 基础形式 | 无 | 标准删除，继承表行为由 PostgreSQL 默认决定 |
| ONLY 形式 | `SQLs.ONLY` | 仅删除本表，**不包括**继承表 |
| ASTERISK 形式 | `SQLs.ASTERISK` | 明确删除本表**和所有**继承表 |

**使用示例** (来自测试):

```java
// DeleteUnitTests.java line 49-56
final Delete stmt;
stmt = Postgres.singleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.AS, "c")
        .using(HistoryChinaRegion_.T, SQLs.AS, "hc")
        .where(HistoryChinaRegion_.id::equal, ChinaRegion_.id)
        .and(ChinaRegion_.id.equal(SQLs::literal, 1))
        .asDelete();
```

---

### ③ USING/JOIN (可选，可重复)

#### USING 子句

```java
// 基本 USING
using(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);
using(SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias);
using(DerivedTable derivedTable);
using(Supplier<DerivedTable> supplier);
using(SQLs.DerivedModifier modifier, DerivedTable derivedTable);
using(String cteName);
using(String cteName, SQLs.WordAs wordAs, String alias);
using(SQLs.DerivedModifier modifier, String cteName);
using(SQLs.DerivedModifier modifier, String cteName, SQLs.WordAs wordAs, String alias);
using(UndoneFunction func);
using(SQLs.DerivedModifier modifier, UndoneFunction func);

// 嵌套 USING
using(Function<_NestedLeftParenSpec<_SingleJoinSpec<I, Q>>, _SingleJoinSpec<I, Q>> function);
```

**使用场景**: 
- DELETE 操作需要引用其他表进行条件判断时
- 比 JOIN 更简洁的表引用方式

---

#### JOIN 子句

```java
// JOIN (内连接)
join(TableMeta<?> table, SQLs.WordAs as, String tableAlias);
join(SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs as, String tableAlias);
join(DerivedTable derivedTable);
join(Supplier<DerivedTable> supplier);
join(SQLs.DerivedModifier modifier, DerivedTable derivedTable);
join(String cteName);
join(String cteName, SQLs.WordAs as, String alias);
join(SQLs.DerivedModifier modifier, String cteName);
join(SQLs.DerivedModifier modifier, String cteName, SQLs.WordAs as, String alias);
join(UndoneFunction func);
join(SQLs.DerivedModifier modifier, UndoneFunction func);
join(Function<_NestedLeftParenSpec<_OnClause<_SingleJoinSpec<I, Q>>>, _OnClause<_SingleJoinSpec<I, Q>>> function);

// LEFT JOIN / RIGHT JOIN / FULL JOIN / CROSS JOIN
// 形式与 JOIN 完全相同
```

**动态 JOIN**:

```java
ifLeftJoin(Consumer<PostgreJoins> consumer);
ifJoin(Consumer<PostgreJoins> consumer);
ifRightJoin(Consumer<PostgreJoins> consumer);
ifFullJoin(Consumer<PostgreJoins> consumer);
ifCrossJoin(Consumer<PostgreCrosses> consumer);
```

**ON 子句 (用于 JOIN)**:

```java
on(IPredicate predicate);
on(IPredicate predicate1, IPredicate predicate2);
on(Function<Expression, IPredicate> operator, Expression operandField);
on(Function<Expression, IPredicate> operator1, Expression operandField1,
   Function<Expression, IPredicate> operator2, Expression operandField2);
on(Consumer<Consumer<IPredicate>> consumer);
```

---

#### TABLESAMPLE (表抽样)

```java
tableSample(Expression method);
<E> tableSample(BiFunction<BiFunction<MappingType, E, Expression>, E, Expression> method,
                BiFunction<MappingType, E, Expression> valueOperator,
                Supplier<E> supplier);
tableSample(BiFunction<BiFunction<MappingType, Object, Expression>, Object, Expression> method,
            BiFunction<MappingType, Object, Expression> valueOperator,
            Function<String, ?> function,
            String keyName);
tableSample(BiFunction<BiFunction<MappingType, Expression, Expression>, Expression, Expression> method,
            BiFunction<MappingType, Expression, Expression> valueOperator,
            Expression argument);

// 条件抽样
ifTableSample(Supplier<Expression> supplier);
<E> ifTableSample(BiFunction<BiFunction<MappingType, E, Expression>, E, Expression> method,
                  BiFunction<MappingType, E, Expression> valueOperator,
                  Supplier<E> supplier);
ifTableSample(BiFunction<BiFunction<MappingType, Object, Expression>, Object, Expression> method,
              BiFunction<MappingType, Object, Expression> valueOperator,
              Function<String, ?> function,
              String keyName);
```

**使用场景**: 大数据表的抽样删除，提高性能。

---

#### REPEATABLE (可重复抽样)

```java
repeatable(Expression seed);
repeatable(Supplier<Expression> supplier);
repeatable(Function<Number, Expression> valueOperator, Number seedValue);
<E extends Number> repeatable(Function<E, Expression> valueOperator, Supplier<E> supplier);
repeatable(Function<Object, Expression> valueOperator, Function<String, ?> function, String keyName);

// 条件设置
ifRepeatable(Supplier<Expression> supplier);
<E extends Number> ifRepeatable(Function<E, Expression> valueOperator, Supplier<E> supplier);
ifRepeatable(Function<Object, Expression> valueOperator, Function<String, ?> function, String keyName);
```

**使用场景**: 需要可重现的抽样结果时使用。

---

### ④ WHERE (可选)

#### 基本 WHERE 方法

```java
// Statement.java
WR where(Consumer<Consumer<IPredicate>> consumer);
WA where(IPredicate predicate);
<T> WA where(Function<T, IPredicate> expOperator, T operand);
WA whereIf(Supplier<IPredicate> supplier);
<T> WA whereIf(Function<T, IPredicate> expOperator, @Nullable T value);
<T> WA whereIf(ExpressionOperator<TypedExpression, T, IPredicate> expOp,
               BiFunction<TypedExpression, T, Expression> operator,
               @Nullable T value);
<T> WA whereIf(BiFunction<SQLs.BiOperator, T, IPredicate> expOp,
               SQLs.BiOperator operator,
               @Nullable T value);
<T> WA whereIf(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOp,
               SQLs.BiOperator op,
               BiFunction<TypedExpression, T, Expression> func,
               @Nullable T value);
<T> WA whereIf(BetweenValueOperator<T> expOp,
               BiFunction<TypedExpression, T, Expression> operator,
               @Nullable T value1, SQLs.WordAnd and,
               @Nullable T value2);
<T, U> WA whereIf(BetweenDualOperator<T, U> expOp,
                  BiFunction<TypedExpression, T, Expression> firstFuncRef,
                  @Nullable T first, SQLs.WordAnd and,
                  BiFunction<TypedExpression, U, Expression> secondFuncRef,
                  @Nullable U second);
```

#### WHERE CURRENT OF

```java
PostgreDelete._ReturningSpec<I, Q> whereCurrentOf(String cursorName);
```

**使用场景**: 使用游标进行定位删除时。

#### 返回值:

- `.where(IPredicate)` / `.where(Function, operand)` → `_SingleWhereAndSpec` (可继续 `.and(...)`)
- `.where(Consumer)` → `_ReturningSpec` (直接跳转到 RETURNING/asDelete，不能再 AND)
- `.whereCurrentOf()` → `_ReturningSpec` (直接跳转到 RETURNING/asDelete)

---

### ⑤ AND (可选，可重复)

```java
WA and(IPredicate predicate);
<T> WA and(Function<T, IPredicate> expOperator, T operand);
WA ifAnd(Supplier<IPredicate> supplier);
<T> WA ifAnd(Function<T, IPredicate> expOperator, @Nullable T value);
<T> WA ifAnd(ExpressionOperator<TypedExpression, T, IPredicate> expOp,
             BiFunction<TypedExpression, T, Expression> operator,
             @Nullable T value);
// ... 更多 ifAnd 重载方法
```

---

### ⑥ RETURNING (可选)

#### 基本 RETURNING 方法

```java
_DqlDeleteSpec<Q> returningAll();
_DqlDeleteSpec<Q> returning(Consumer<Returnings> consumer);
_StaticReturningCommaSpec<Q> returning(Selection selection);
_StaticReturningCommaSpec<Q> returning(Selection selection1, Selection selection2);
_StaticReturningCommaSpec<Q> returning(Function<String, Selection> function, String alias);
_StaticReturningCommaSpec<Q> returning(Function<String, Selection> function1, String alias1,
                                       Function<String, Selection> function2, String alias2);
_StaticReturningCommaSpec<Q> returning(Function<String, Selection> function, String alias, Selection selection);
_StaticReturningCommaSpec<Q> returning(Selection selection, Function<String, Selection> function, String alias);
_StaticReturningCommaSpec<Q> returning(String derivedAlias, SQLs.SymbolDot period, SQLs.SymbolAsterisk star);
_StaticReturningCommaSpec<Q> returning(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table);
<P> _StaticReturningCommaSpec<Q> returning(String parenAlias, SQLs.SymbolDot period1, ParentTableMeta<P> parent,
                                            String childAlias, SQLs.SymbolDot period2, ComplexTableMeta<P, ?> child);
_StaticReturningCommaSpec<Q> returning(TableField field1, TableField field2, TableField field3);
_StaticReturningCommaSpec<Q> returning(TableField field1, TableField field2, TableField field3, TableField field4);
```

#### COMMA 方法 (RETURNING 后添加更多列)

```java
_StaticReturningCommaSpec<Q> comma(Selection selection);
_StaticReturningCommaSpec<Q> comma(Selection selection1, Selection selection2);
// ... 更多 comma 重载形式 (与 returning 完全相同)
```

---

### ⑦ 结束语句

#### 无 RETURNING 的情况

```java
I asDelete();
```

- **返回类型**: `Delete` — 可传给 `session.delete(delete)` 或 `session.execute(delete)`
- **不可再次调用** — 调用后语句已标记为 prepared，再次操作会抛异常

#### 有 RETURNING 的情况

```java
Q asReturningDelete();
```

- **返回类型**: `ReturningDelete` — 可执行并获取被删除的行数据
- **不可再次调用** — 调用后语句已标记为 prepared，再次操作会抛异常

---

## 完整示例

### 示例 1: 简单单表 DELETE

```java
final Delete stmt = Postgres.singleDelete()
        .deleteFrom(User_.T, SQLs.AS, "u")
        .where(User_.id.equal(SQLs::literal, 1))
        .asDelete();
// 结果: DELETE FROM user AS u WHERE id = 1
```

### 示例 2: 带 USING 的 DELETE

```java
final Delete stmt = Postgres.singleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.AS, "c")
        .using(HistoryChinaRegion_.T, SQLs.AS, "hc")
        .where(HistoryChinaRegion_.id::equal, ChinaRegion_.id)
        .and(ChinaRegion_.id.equal(SQLs::literal, 1))
        .asDelete();
```

### 示例 3: 带 JOIN 的 DELETE

```java
final Delete stmt = Postgres.singleDelete()
        .deleteFrom(User_.T, SQLs.AS, "u")
        .leftJoin(Order_.T, SQLs.AS, "o").on(User_.id::equal, Order_.userId)
        .where(Order_.id::isNull)
        .asDelete();
```

### 示例 4: 带 RETURNING 的 DELETE

```java
// DeleteUnitTests.java line 62-71
final ReturningDelete stmt;
stmt = Postgres.singleDelete()
        .deleteFrom(ChinaRegion_.T, SQLs.ASTERISK, SQLs.AS, "c")
        .using(HistoryChinaRegion_.T, SQLs.AS, "hc")
        .where(HistoryChinaRegion_.id::equal, ChinaRegion_.id)
        .and(ChinaRegion_.id.equal(SQLs::literal, 1))
        .returning("c", SQLs.PERIOD, ChinaRegion_.T)
        .comma(HistoryChinaRegion_.id)
        .asReturningDelete();
```

### 示例 5: 返回所有列

```java
final ReturningDelete stmt = Postgres.singleDelete()
        .deleteFrom(User_.T, SQLs.AS, "u")
        .where(User_.status::equal, "inactive")
        .returningAll()
        .asReturningDelete();
```

### 示例 6: 条件 WHERE/AND

```java
final Delete stmt = Postgres.singleDelete()
        .deleteFrom(User_.T, SQLs.AS, "u")
        .whereIf(() -> shouldFilterId ? User_.id.equal(id) : null)
        .ifAnd(() -> shouldFilterName ? User_.name.equal(name) : null)
        .asDelete();
```

### 示例 7: 带 WITH 子句的 DELETE

```java
Postgres.singleDelete()
    .with("inactive_users")
    .as(Postgres.query()
        .select(User_.id)
        .from(User_.T, SQLs.AS, "u")
        .where(User_.status::equal, "inactive")
        .asSubQuery())
    .space()
    .deleteFrom(User_.T, SQLs.AS, "u")
    .using("inactive_users", SQLs.AS, "iu")
    .where(User_.id::equal, SQLs.field("iu", User_.id))
    .asDelete();
```

---

## 实现类继承链

```
SQLSyntax (Postgres extends)
    └─ Postgres
        └─ .singleDelete() → PostgreDeletes.simpleDelete()
            └─ PrimarySimpleDelete
                └─ PostgreDeletes<I, Q>
                    └─ JoinableDelete<I, Q, ...>
                        └─ JoinableClause<...>
                            └─ WhereClause
                                └─ ArmyStmtSpec
```

---

## 关键约束和规则

### 层级约束 (通过接口链强制执行)

接口名称本身就编码了当前上下文中可用的方法，DSL 通过**返回类型引导**下一个可调用的方法：

1. **WITH → DELETE FROM**: 调用 `with()` 后只能进入 `PgSingleDeleteClause`，即只能调用 `deleteFrom()`
2. **DELETE FROM → USING/JOIN**: 调用 `deleteFrom()` 后进入 `_SingleUsingSpec`，可调用 using/join
3. **USING/JOIN → WHERE**: 调用 using/join 后进入 `_SingleJoinSpec`，可继续 join 或调用 where
4. **WHERE → AND**: 调用 `where(IPredicate)` 后进入 `_SingleWhereAndSpec`，可继续 `and()`
5. **AND → RETURNING**: 调用 and 后仍在 `_SingleWhereAndSpec`，可调用 returning
6. **RETURNING → COMMA**: 调用 returning 后进入 `_StaticReturningCommaSpec`，可继续 comma
7. **最终 → asDelete/asReturningDelete**: 最终必须调用 `asDelete()` 或 `asReturningDelete()`

### 方法调用限制

| 方法/阶段 | 是否必须 | 可重复调用 | 说明 |
|----------|---------|-----------|------|
| `Postgres.singleDelete()` | 是 | 否 | 入口，只能调用一次（每次调用创建新语句） |
| `with()` / `withRecursive()` | 否 | 否 | WITH 关键字只能出现一次，多个 CTE 用 comma 追加 |
| `deleteFrom()` | 是 | 否 | 必须且只能调用一次 |
| `using()` / `join()` 系列 | 否 | 是 | 可以多次调用，添加多个 USING/JOIN |
| `tableSample()` / `repeatable()` | 否 | 是 | 可以多次调用 |
| `where()` | 否 | 否 | 只能调用一次（但可通过 Consumer 组合多条件） |
| `and()` | 否 | 是 | WHERE 后可以多次调用 AND |
| `returning()` | 否 | 否 | 只能有一个 RETURNING 关键字，后续用 COMMA |
| `comma()` | 否 | 是 | RETURNING 后可以多次调用 COMMA |
| `asDelete()` / `asReturningDelete()` | 是 | 否 | 必须调用一次，结束构建 |

### null 检查

- `tableAlias` 不可为 null
- `table` 不可为 null
- CTE 的名称不可为 null
- 条件形式的 `ifWhere` / `ifAnd` 的 value 可为 null (null 时忽略该条件)

### 类型安全

- 返回类型的链式设计确保：不会在错误的阶段调用错误的方法
- 若使用了 RETURNING 子句，必须调用 `asReturningDelete()`，否则调用 `asDelete()`

---

## 与其他 DELETE 入口的区别

| 入口 | 返回类型 | 场景 |
|-----|---------|------|
| `Postgres.singleDelete()` | `PgSingleDeleteSpec<Delete, ReturningDelete>` | PostgreSQL 单表 DELETE，本技能覆盖 |
| `Postgres.batchSingleDelete()` | `PgSingleDeleteSpec<_BatchDeleteParamSpec, _BatchReturningDeleteParamSpec>` | PostgreSQL 批量单表 DELETE |
| `SQLs.singleDelete()` | `_WithSpec<Delete>` | 标准 SQL 单表 DELETE |

---

## 相关接口参考

| 接口 | 说明 |
|------|------|
| `PgSingleDeleteSpec` | 起始接口，支持 WITH 子句 |
| `PgSingleDeleteClause` | 支持 deleteFrom 方法 |
| `PostgreDelete._SingleUsingSpec` | 支持 USING 子句 |
| `PostgreDelete._SingleJoinSpec` | 支持 JOIN 子句 |
| `PostgreDelete._SingleWhereClause` | 支持 WHERE 子句 |
| `PostgreDelete._SingleWhereAndSpec` | 支持 AND 子句 |
| `PostgreDelete._ReturningSpec` | 支持 RETURNING 子句 |
| `PostgreDelete._StaticReturningCommaSpec` | 支持 COMMA 子句 |
| `Delete` | 标准删除语句 |
| `ReturningDelete` | 带 RETURNING 的删除语句 |

---

## 自我进化指南

当 Army Criteria API 更新时，请按以下步骤更新本文档：

1. **检查接口变更**: 重新读取 `PostgreDelete.java`、`PgSingleDeleteSpec.java`、`PgSingleDeleteClause.java`、`Statement.java`
2. **检查实现变更**: 重新读取 `PostgreDeletes.java`、`_PostgreDelete.java`
3. **检查测试更新**: 查看 `DeleteUnitTests.java` 的新增测试用例
4. **更新 Diagram**: 如有新方法/新子句，补充到 Diagram 中
5. **更新示例**: 补充新增用法的示例
6. **更新约束**: 如有新的语义约束，补充到规则部分
7. **检查相关文件**: 查看 `Postgres.java`、`PostgreStatement.java` 等文件的变更
