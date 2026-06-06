---
name: "postgres-single-delete"
description: "PostgreSQL 单表删除方法链完整文档，包含所有可用方法、参数、使用场景和示例。Invoke when user needs to use or understand Postgres.singleDelete() method chain."
---

# Postgres.singleDelete() 方法链完整文档

## 概述

`Postgres.singleDelete()` 是 Army Criteria 框架中用于构建 PostgreSQL 单表 DELETE 语句的流式 API。该方法链支持复杂的删除操作，包括：

- WITH 子句（CTE）
- DELETE FROM 子句（支持 ONLY 和 ASTERISK 修饰符）
- USING 子句与 JOIN
- WHERE 子句
- RETURNING 子句

## 方法链完整结构

```
Postgres.singleDelete()
  ├─ [WITH 子句] 可选
  │     ├─ with(name)
  │     ├─ withRecursive(name)
  │     ├─ with(Consumer)
  │     ├─ withRecursive(Consumer)
  │     ├─ ifWith(Consumer)
  │     └─ ifWithRecursive(Consumer)
  ├─ DELETE FROM 子句 (必需)
  │     ├─ deleteFrom(table, SQLs.AS, alias)
  │     ├─ deleteFrom(SQLs.ONLY, table, SQLs.AS, alias)
  │     └─ deleteFrom(table, SQLs.ASTERISK, SQLs.AS, alias)
  ├─ [USING/JOIN 子句] 可选，可重复
  │     ├─ USING (多种形式)
  │     ├─ JOIN (多种类型)
  │     ├─ LEFT JOIN
  │     ├─ RIGHT JOIN
  │     ├─ FULL JOIN
  │     ├─ CROSS JOIN
  │     ├─ 动态 join 方法
  │     ├─ tableSample/ifTableSample
  │     └─ repeatable/ifRepeatable
  ├─ [WHERE 子句] 可选
  │     ├─ where(Consumer)
  │     ├─ where(predicate)
  │     ├─ where(Function, operand)
  │     ├─ whereIf(Supplier)
  │     ├─ whereIf(Function, value)
  │     ├─ whereCurrentOf(cursorName)
  │     ├─ and(predicate)
  │     ├─ and(Function, operand)
  │     ├─ ifAnd(Supplier)
  │     └─ ifAnd(Function, value)
  ├─ [RETURNING 子句] 可选
  │     ├─ returningAll()
  │     ├─ returning(Consumer)
  │     ├─ returning(selection)
  │     ├─ returning(selection1, selection2)
  │     ├─ returning(Function, alias)
  │     └─ comma(...)
  └─ 结束语句
        ├─ asDelete() → Delete
        └─ asReturningDelete() → ReturningDelete
```

## 详细方法说明

### 1. 起始方法

```java
PgSingleDeleteSpec<Delete, ReturningDelete> singleDelete()
```

**说明**: 创建单表 DELETE 语句构建器，这是整个方法链的起点。

**示例**:
```java
Postgres.singleDelete()
  // 后续方法链
```

---

### 2. WITH 子句 (可选)

#### 静态 WITH 子句

```java
PostgreQuery._StaticCteParensSpec<PgSingleDeleteClause<I, Q>> with(String name)
PostgreQuery._StaticCteParensSpec<PgSingleDeleteClause<I, Q>> withRecursive(String name)
```

**参数**:
- `name`: CTE 名称

**使用场景**: 需要定义 CTE（公共表表达式）时使用。

---

#### 动态 WITH 子句

```java
PgSingleDeleteClause<I, Q> with(Consumer<PostgreCtes> consumer)
PgSingleDeleteClause<I, Q> withRecursive(Consumer<PostgreCtes> consumer)
PgSingleDeleteClause<I, Q> ifWith(Consumer<PostgreCtes> consumer)
PgSingleDeleteClause<I, Q> ifWithRecursive(Consumer<PostgreCtes> consumer)
```

**参数**:
- `consumer`: 用于构建 CTE 的回调函数
- `ifWith/ifWithRecursive`: 条件性地添加 WITH 子句

**使用场景**: 需要动态构建 CTE 或条件性添加 CTE 时使用。

---

### 3. DELETE FROM 子句 (必需)

```java
PostgreDelete._SingleUsingSpec<I, Q> deleteFrom(TableMeta<?> table, SQLs.WordAs as, String alias)
PostgreDelete._SingleUsingSpec<I, Q> deleteFrom(SQLs.WordOnly only, TableMeta<?> table, SQLs.WordAs as, String alias)
PostgreDelete._SingleUsingSpec<I, Q> deleteFrom(TableMeta<?> table, SQLs.SymbolAsterisk star, SQLs.WordAs as, String alias)
```

**参数**:
- `table`: 要删除的表元数据
- `as`: 始终使用 `SQLs.AS`
- `alias`: 表别名
- `only`: 使用 `SQLs.ONLY` 限制只删除本表，不包括继承表
- `star`: 使用 `SQLs.ASTERISK` 明确指定删除本表和所有继承表

**使用场景**:
- 基本删除操作: `deleteFrom(table, SQLs.AS, "t")
- 只删除本表: `deleteFrom(SQLs.ONLY, table, SQLs.AS, "t")
- 删除本表及继承表: `deleteFrom(table, SQLs.ASTERISK, SQLs.AS, "t")

---

### 4. USING/JOIN 子句 (可选，可重复)

#### USING 子句

```java
// 基本 USING
PostgreDelete._TableSampleJoinSpec<I, Q> using(TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias)
PostgreDelete._TableSampleJoinSpec<I, Q> using(SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs wordAs, String tableAlias)
PostgreDelete._ParensJoinSpec<I, Q> using(DerivedTable derivedTable)
PostgreDelete._ParensJoinSpec<I, Q> using(Supplier<DerivedTable> supplier)
PostgreDelete._ParensJoinSpec<I, Q> using(SQLs.DerivedModifier modifier, DerivedTable derivedTable)
PostgreDelete._SingleJoinSpec<I, Q> using(String cteName)
PostgreDelete._SingleJoinSpec<I, Q> using(String cteName, SQLs.WordAs wordAs, String alias)
PostgreDelete._SingleJoinSpec<I, Q> using(SQLs.DerivedModifier modifier, String cteName)
PostgreDelete._SingleJoinSpec<I, Q> using(SQLs.DerivedModifier modifier, String cteName, SQLs.WordAs wordAs, String alias)
PostgreDelete._SingleJoinSpec<I, Q> using(UndoneFunction func)
PostgreDelete._SingleJoinSpec<I, Q> using(SQLs.DerivedModifier modifier, UndoneFunction func)

// 嵌套 USING
PostgreDelete._SingleJoinSpec<I, Q> using(Function<_NestedLeftParenSpec<_SingleJoinSpec<I, Q>>, _SingleJoinSpec<I, Q>> function)
```

---

#### JOIN 子句

```java
// JOIN (内连接)
PostgreDelete._TableSampleOnSpec<I, Q> join(TableMeta<?> table, SQLs.WordAs as, String tableAlias)
PostgreDelete._TableSampleOnSpec<I, Q> join(SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs as, String tableAlias)
PostgreDelete._AsParensOnClause<PostgreDelete._SingleJoinSpec<I, Q>> join(DerivedTable derivedTable)
PostgreDelete._AsParensOnClause<PostgreDelete._SingleJoinSpec<I, Q>> join(Supplier<DerivedTable> supplier)
PostgreDelete._AsParensOnClause<PostgreDelete._SingleJoinSpec<I, Q>> join(SQLs.DerivedModifier modifier, DerivedTable derivedTable)
Statement._OnClause<PostgreDelete._SingleJoinSpec<I, Q>> join(String cteName)
Statement._OnClause<PostgreDelete._SingleJoinSpec<I, Q>> join(String cteName, SQLs.WordAs as, String alias)
Statement._OnClause<PostgreDelete._SingleJoinSpec<I, Q>> join(Function<_NestedLeftParenSpec<_OnClause<_SingleJoinSpec<I, Q>>>, _OnClause<_SingleJoinSpec<I, Q>>> function)

// LEFT JOIN
PostgreDelete._TableSampleOnSpec<I, Q> leftJoin(TableMeta<?> table, SQLs.WordAs as, String tableAlias)
PostgreDelete._TableSampleOnSpec<I, Q> leftJoin(SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs as, String tableAlias)
// ... 其他 LEFT JOIN 形式同 JOIN

// RIGHT JOIN
PostgreDelete._TableSampleOnSpec<I, Q> rightJoin(TableMeta<?> table, SQLs.WordAs as, String tableAlias)
// ... 其他 RIGHT JOIN 形式同 JOIN

// FULL JOIN
PostgreDelete._TableSampleOnSpec<I, Q> fullJoin(TableMeta<?> table, SQLs.WordAs as, String tableAlias)
// ... 其他 FULL JOIN 形式同 JOIN

// CROSS JOIN
PostgreDelete._TableSampleJoinSpec<I, Q> crossJoin(TableMeta<?> table, SQLs.WordAs as, String tableAlias)
PostgreDelete._TableSampleJoinSpec<I, Q> crossJoin(SQLs.TableModifier modifier, TableMeta<?> table, SQLs.WordAs as, String tableAlias)
PostgreDelete._ParensJoinSpec<I, Q> crossJoin(DerivedTable derivedTable)
// ... 其他 CROSS JOIN 形式
PostgreDelete._SingleJoinSpec<I, Q> crossJoin(Function<_NestedLeftParenSpec<_SingleJoinSpec<I, Q>>, _SingleJoinSpec<I, Q>> function)
```

---

#### 动态 JOIN 方法

```java
PostgreDelete._SingleJoinSpec<I, Q> ifLeftJoin(Consumer<PostgreJoins> consumer)
PostgreDelete._SingleJoinSpec<I, Q> ifJoin(Consumer<PostgreJoins> consumer)
PostgreDelete._SingleJoinSpec<I, Q> ifRightJoin(Consumer<PostgreJoins> consumer)
PostgreDelete._SingleJoinSpec<I, Q> ifFullJoin(Consumer<PostgreJoins> consumer)
PostgreDelete._SingleJoinSpec<I, Q> ifCrossJoin(Consumer<PostgreCrosses> consumer)
```

**使用场景**: 条件性地添加 JOIN 子句。

---

#### ON 子句 (用于 JOIN)

```java
OR on(IPredicate predicate)
OR on(IPredicate predicate1, IPredicate predicate2)
OR on(Function<Expression, IPredicate> operator, Expression operandField)
OR on(Function<Expression, IPredicate> operator1, Expression operandField1,
   Function<Expression, IPredicate> operator2, Expression operandField2)
OR on(Consumer<Consumer<IPredicate>> consumer)
```

---

#### TABLESAMPLE 子句

```java
PostgreDelete._RepeatableJoinClause<I, Q> tableSample(Expression method)
<E> PostgreDelete._RepeatableJoinClause<I, Q> tableSample(
    BiFunction<BiFunction<MappingType, E, Expression>, E, Expression> method,
    BiFunction<MappingType, E, Expression> valueOperator,
    Supplier<E> supplier)
PostgreDelete._RepeatableJoinClause<I, Q> tableSample(
    BiFunction<BiFunction<MappingType, Object, Expression>, Object, Expression> method,
    BiFunction<MappingType, Object, Expression> valueOperator,
    Function<String, ?> function,
    String keyName)
PostgreDelete._RepeatableJoinClause<I, Q> tableSample(
    BiFunction<BiFunction<MappingType, Expression, Expression>, Expression, Expression> method,
    BiFunction<MappingType, Expression, Expression> valueOperator,
    Expression argument)
PostgreDelete._RepeatableJoinClause<I, Q> ifTableSample(Supplier<Expression> supplier)
<E> PostgreDelete._RepeatableJoinClause<I, Q> ifTableSample(
    BiFunction<BiFunction<MappingType, E, Expression>, E, Expression> method,
    BiFunction<MappingType, E, Expression> valueOperator,
    Supplier<E> supplier)
PostgreDelete._RepeatableJoinClause<I, Q> ifTableSample(
    BiFunction<BiFunction<MappingType, Object, Expression>, Object, Expression> method,
    BiFunction<MappingType, Object, Expression> valueOperator,
    Function<String, ?> function,
    String keyName)
```

**使用场景**: 需要对表进行抽样时使用。

---

#### REPEATABLE 子句

```java
PostgreDelete._SingleJoinSpec<I, Q> repeatable(Expression seed)
PostgreDelete._SingleJoinSpec<I, Q> repeatable(Supplier<Expression> supplier)
PostgreDelete._SingleJoinSpec<I, Q> repeatable(Function<Number, Expression> valueOperator, Number seedValue)
<E extends Number> PostgreDelete._SingleJoinSpec<I, Q> repeatable(Function<E, Expression> valueOperator, Supplier<E> supplier)
PostgreDelete._SingleJoinSpec<I, Q> repeatable(Function<Object, Expression> valueOperator, Function<String, ?> function, String keyName)
PostgreDelete._SingleJoinSpec<I, Q> ifRepeatable(Supplier<Expression> supplier)
<E extends Number> PostgreDelete._SingleJoinSpec<I, Q> ifRepeatable(Function<E, Expression> valueOperator, Supplier<E> supplier)
PostgreDelete._SingleJoinSpec<I, Q> ifRepeatable(Function<Object, Expression> valueOperator, Function<String, ?> function, String keyName)
```

**使用场景**: TABLESAMPLE 时需要可重复抽样时使用。

---

### 5. WHERE 子句 (可选)

#### 基本 WHERE 方法

```java
WR where(Consumer<Consumer<IPredicate>> consumer)
WA where(IPredicate predicate)
<T> WA where(Function<T, IPredicate> expOperator, T operand)
WA whereIf(Supplier<IPredicate> supplier)
<T> WA whereIf(Function<T, IPredicate> expOperator, T value)
<T> WA whereIf(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
    BiFunction<TypedExpression, T, Expression> operator, T value)
<T> WA whereIf(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator,
    SQLs.BiOperator operator, T value)
<T> WA whereIf(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator,
    SQLs.BiOperator operator, BiFunction<TypedExpression, T, Expression> func, T value)
<T> WA whereIf(BetweenValueOperator<T> expOperator,
    BiFunction<TypedExpression, T, Expression> operator, T value1, SQLs.WordAnd and, T value2)
<T, U> WA whereIf(BetweenDualOperator<T, U> expOperator,
    BiFunction<TypedExpression, T, Expression> firstFuncRef, T first,
    SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second)
```

---

#### AND 方法 (WHERE 之后使用)

```java
WA and(IPredicate predicate)
<T> WA and(Function<T, IPredicate> expOperator, T operand)
WA ifAnd(Supplier<IPredicate> supplier)
<T> WA ifAnd(Function<T, IPredicate> expOperator, T value)
<T> WA ifAnd(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
    BiFunction<TypedExpression, T, Expression> operator, T value)
<T> WA ifAnd(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator,
    SQLs.BiOperator operator, T value)
// ... 更多 ifAnd 重载方法
```

---

#### WHERE CURRENT OF

```java
PostgreDelete._ReturningSpec<I, Q> whereCurrentOf(String cursorName)
```

**使用场景**: 使用游标进行定位删除时。

---

### 6. RETURNING 子句 (可选)

#### 基本 RETURNING 方法

```java
_DqlDeleteSpec<Q> returningAll()
_DqlDeleteSpec<Q> returning(Consumer<Returnings> consumer)
_StaticReturningCommaSpec<Q> returning(Selection selection)
_StaticReturningCommaSpec<Q> returning(Selection selection1, Selection selection2)
_StaticReturningCommaSpec<Q> returning(Function<String, Selection> function, String alias)
_StaticReturningCommaSpec<Q> returning(Function<String, Selection> function1, String alias1,
    Function<String, Selection> function2, String alias2)
_StaticReturningCommaSpec<Q> returning(Function<String, Selection> function, String alias, Selection selection)
_StaticReturningCommaSpec<Q> returning(Selection selection, Function<String, Selection> function, String alias)
_StaticReturningCommaSpec<Q> returning(String derivedAlias, SQLs.SymbolDot period, SQLs.SymbolAsterisk star)
_StaticReturningCommaSpec<Q> returning(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table)
<P> _StaticReturningCommaSpec<Q> returning(String parenAlias, SQLs.SymbolDot period1, ParentTableMeta<P> parent,
    String childAlias, SQLs.SymbolDot period2, ComplexTableMeta<P, ?> child)
_StaticReturningCommaSpec<Q> returning(TableField field1, TableField field2, TableField field3)
_StaticReturningCommaSpec<Q> returning(TableField field1, TableField field2, TableField field3, TableField field4)
```

---

#### COMMA 方法 (RETURNING 后添加更多列)

```java
_StaticReturningCommaSpec<Q> comma(Selection selection)
_StaticReturningCommaSpec<Q> comma(Selection selection1, Selection selection2)
_StaticReturningCommaSpec<Q> comma(Function<String, Selection> function, String alias)
// ... 更多 comma 重载形式
```

---

### 7. 结束语句

#### 无 RETURNING 的情况

```java
Delete asDelete()
```

#### 有 RETURNING 的情况

```java
ReturningDelete asReturningDelete()
```

---

## 使用示例

### 示例 1: 简单单表删除

```java
Delete deleteStmt = Postgres.singleDelete()
  .deleteFrom(User_.T, SQLs.AS, "u")
  .where(User_.id.equal(SQLs::literal, 1))
  .asDelete();
```

---

### 示例 2: 带 USING 的删除

```java
Delete deleteStmt = Postgres.singleDelete()
  .deleteFrom(ChinaRegion_.T, SQLs.AS, "c")
  .using(HistoryChinaRegion_.T, SQLs.AS, "hc")
  .where(HistoryChinaRegion_.id::equal, ChinaRegion_.id)
  .and(ChinaRegion_.id.equal(SQLs::literal, 1))
  .asDelete();
```

---

### 示例 3: 带 JOIN 的删除

```java
Delete deleteStmt = Postgres.singleDelete()
  .deleteFrom(User_.T, SQLs.AS, "u")
  .leftJoin(Order_.T, SQLs.AS, "o").on(User_.id::equal, Order_.userId)
  .where(Order_.id::isNull)
  .asDelete();
```

---

### 示例 4: 带 RETURNING 的删除

```java
ReturningDelete returningDelete = Postgres.singleDelete()
  .deleteFrom(ChinaRegion_.T, SQLs.ASTERISK, SQLs.AS, "c")
  .using(HistoryChinaRegion_.T, SQLs.AS, "hc")
  .where(HistoryChinaRegion_.id::equal, ChinaRegion_.id)
  .and(ChinaRegion_.id.equal(SQLs::literal, 1))
  .returning("c", SQLs.PERIOD, ChinaRegion_.T)
  .comma(HistoryChinaRegion_.id)
  .asReturningDelete();
```

---

### 示例 5: 带 WITH 子句的删除

```java
Delete deleteStmt = Postgres.singleDelete()
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

### 示例 6: 条件性 WHERE 子句

```java
Delete deleteStmt = Postgres.singleDelete()
  .deleteFrom(User_.T, SQLs.AS, "u")
  .whereIf(() -> shouldFilterId ? User_.id.equal(id) : null)
  .ifAnd(() -> shouldFilterName ? User_.name.equal(name) : null)
  .asDelete();
```

---

## 方法重复性说明

| 子句 | 是否可重复 | 说明 |
|------|-----------|------|
| WITH | ❌ 否 | 只能有一个 WITH 子句 |
| DELETE FROM | ❌ 否 | 只能有一个 DELETE FROM 子句 |
| USING/JOIN | ✅ 是 | 可以多次调用，添加多个 JOIN/USING |
| WHERE | ❌ 否 | 只能有一个 WHERE 关键字，后续用 AND |
| AND | ✅ 是 | WHERE 后可以多次调用 AND |
| RETURNING | ❌ 否 | 只能有一个 RETURNING 关键字，后续用 COMMA |
| COMMA | ✅ 是 | RETURNING 后可以多次调用 COMMA |

---

## 最佳实践

1. **始终使用别名**: 为所有表指定别名，提高可读性和避免冲突
2. **使用条件方法**: 使用 `ifWhere`、`ifAnd` 等方法进行条件性添加子句
3. **RETURNING 子句**: 需要返回删除的数据时使用
4. **安全性**: DELETE 语句建议有 WHERE 子句，避免误删全部数据
5. **ONLY vs ASTERISK**: 根据表继承需求选择合适的修饰符

---

## 相关接口

- `PgSingleDeleteSpec`: 起始接口
- `PgSingleDeleteClause`: DELETE FROM 子句接口
- `PostgreDelete`: Postgre DELETE 语句定义
- `Delete`: 标准删除语句
- `ReturningDelete`: 带 RETURNING 的删除语句
