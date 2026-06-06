---
name: "mysql-single-update"
description: "Complete guide and reference for MySQLs.singleUpdate() method chain with all clauses, parameter forms, usage examples, and best practices. Invoke when user asks about MySQL singleUpdate or needs to build UPDATE statements."
---

# MySQL singleUpdate 方法链完整指南

## 概述

`MySQLs.singleUpdate()` 是 Army 框架中用于创建 MySQL 单表 UPDATE 语句的入口方法。本指南详细介绍其完整的方法链、参数说明、使用场景和示例代码。

---

## 完整方法链图

```
MySQLs.singleUpdate()
    → WITH 子句
        ↳ with(String name)
        ↳ withRecursive(String name)
    → UPDATE 子句
        ↳ update(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers)
        ↳ update(SingleTableMeta<T> table, SQLs.WordAs wordAs, String alias)
        ↳ update(ComplexTableMeta<P, ?> table, SQLs.WordAs wordAs, String alias)
        ↳ update(SingleTableMeta<T> table)
        ↳ update(ComplexTableMeta<P, ?> table)
    → PARTITION 子句（可选）
        ↳ partition(String partitionName)
        ↳ partition(String... partitionNames)
        ↳ partition(Consumer<Consumer<String>> consumer)
        ↳ ifPartition(Consumer<Consumer<String>> consumer)
    → 表别名（可选）
        ↳ as(String alias)
    → INDEX HINT 子句（可重复）
        ↳ useIndex(String indexName)
        ↳ useIndex(String indexName1, String indexName2)
        ↳ useIndex(String indexName1, String indexName2, String indexName3)
        ↳ useIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
        ↳ useIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
        ↳ ifUseIndex(Consumer<Consumer<String>> consumer)
        ↳ ignoreIndex(String indexName)
        ↳ ignoreIndex(String indexName1, String indexName2)
        ↳ ignoreIndex(String indexName1, String indexName2, String indexName3)
        ↳ ignoreIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
        ↳ ignoreIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
        ↳ ifIgnoreIndex(Consumer<Consumer<String>> consumer)
        ↳ forceIndex(String indexName)
        ↳ forceIndex(String indexName1, String indexName2)
        ↳ forceIndex(String indexName1, String indexName2, String indexName3)
        ↳ forceIndex(Consumer<Clause._StaticStringSpaceClause> consumer)
        ↳ forceIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
        ↳ ifForceIndex(Consumer<Consumer<String>> consumer)
        ↳ useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
        ↳ useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
        ↳ useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
        ↳ useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
        ↳ useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
        ↳ ifUseIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
        ↳ ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
        ↳ ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
        ↳ ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
        ↳ ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
        ↳ ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
        ↳ ifIgnoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
        ↳ forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)
        ↳ forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2)
        ↳ forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName1, String indexName2, String indexName3)
        ↳ forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Clause._StaticStringSpaceClause> consumer)
        ↳ forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)
        ↳ ifForceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, Consumer<Consumer<String>> consumer)
    → SET 子句（可重复）
        ↳ set(FieldMeta<T> field, Object value)
        ↳ set(FieldMeta<T> field, BiFunction<FieldMeta<T>, E, AssignmentItem> valueOperator, E value)
        ↳ set(FieldMeta<T> field, BiFunction<FieldMeta<T>, Expression, AssignmentItem> fieldOperator, BiFunction<FieldMeta<T>, E, Expression> valueOperator, E value)
        ↳ ifSet(FieldMeta<T> field, Object value)
        ↳ ifSet(FieldMeta<T> field, BiFunction<FieldMeta<T>, E, AssignmentItem> valueOperator, E value)
        ↳ ifSet(FieldMeta<T> field, BiFunction<FieldMeta<T>, Expression, AssignmentItem> fieldOperator, BiFunction<FieldMeta<T>, E, Expression> valueOperator, E value)
        ↳ setSpace(FieldMeta<T> field, BiFunction<FieldMeta<T>, String, Expression> valueOperator)
        ↳ setSpace(FieldMeta<T> field, BiFunction<FieldMeta<T>, Expression, AssignmentItem> fieldOperator, BiFunction<FieldMeta<T>, String, Expression> valueOperator)
        ↳ sets(Consumer<UpdateStatement._BatchItemPairs<FieldMeta<T>>> consumer)
    → WHERE 子句（可选）
        ↳ where(Consumer<Consumer<IPredicate>> consumer)
        ↳ where(IPredicate predicate)
        ↳ whereIf(Supplier<IPredicate> supplier)
        ↳ where(Function<T, IPredicate> expOperator, T operand)
        ↳ whereIf(Function<T, IPredicate> expOperator, T value)
        ↳ whereIf(ExpressionOperator<TypedExpression, T, IPredicate> expOperator, BiFunction<TypedExpression, T, Expression> operator, T value)
        ↳ whereIf(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator, SQLs.BiOperator operator, T value)
        ↳ whereIf(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator, SQLs.BiOperator operator, BiFunction<TypedExpression, T, Expression> func, T value)
        ↳ whereIf(BetweenValueOperator<T> expOperator, BiFunction<TypedExpression, T, Expression> operator, T value1, SQLs.WordAnd and, T value2)
        ↳ whereIf(BetweenDualOperator<T, U> expOperator, BiFunction<TypedExpression, T, Expression> firstFuncRef, T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second)
        ↳ AND 子句（可重复）
            ↳ and(IPredicate predicate)
            ↳ ifAnd(Supplier<IPredicate> supplier)
            ↳ and(Function<T, IPredicate> expOperator, T operand)
            ↳ ifAnd(Function<T, IPredicate> expOperator, T value)
            ↳ ifAnd(ExpressionOperator<TypedExpression, T, IPredicate> expOperator, BiFunction<TypedExpression, T, Expression> operator, T value)
            ↳ ifAnd(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator, SQLs.BiOperator operator, T value)
            ↳ ifAnd(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator, SQLs.BiOperator operator, BiFunction<TypedExpression, T, Expression> func, T value)
            ↳ ifAnd(BetweenValueOperator<T> expOperator, BiFunction<TypedExpression, T, Expression> operator, T value1, SQLs.WordAnd and, T value2)
            ↳ ifAnd(BetweenDualOperator<T, U> expOperator, BiFunction<TypedExpression, T, Expression> firstFuncRef, T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second)
    → ORDER BY 子句（可选）
        ↳ orderBy(SortItem sortItem)
        ↳ orderBy(SortItem sortItem1, SortItem sortItem2)
        ↳ orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3)
        ↳ orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3, SortItem sortItem4)
        ↳ spaceComma(SortItem sortItem)
        ↳ spaceComma(SortItem sortItem1, SortItem sortItem2)
        ↳ spaceComma(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3)
        ↳ spaceComma(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3, SortItem sortItem4)
    → LIMIT 子句（可选）
        ↳ limit(Object rowCount)
        ↳ limit(BiFunction<MappingType, Number, Expression> operator, long rowCount)
        ↳ limit(BiFunction<MappingType, String, Expression> operator, String paramName)
        ↳ ifLimit(Object rowCount)
        ↳ ifLimit(BiFunction<MappingType, Number, Expression> operator, Number rowCount)
        ↳ ifLimit(BiFunction<MappingType, String, Expression> operator, String paramName)
    → 结束语句
        ↳ asUpdate()
```

---

## 各子句详解

### 1. WITH 子句

**用途**：定义公共表达式（CTE），可在 UPDATE 语句中引用

**方法**：
- `with(String name)` - 定义普通 CTE
- `withRecursive(String name)` - 定义递归 CTE

### 2. UPDATE 子句

**用途**：指定要更新的表

**形式**：
1. `update(Supplier<List<Hint>> hints, List<MySQLs.Modifier> modifiers) - 带提示和修饰符
2. `update(SingleTableMeta<T> table, SQLs.WordAs wordAs, String alias)` - 单表带别名
3. `update(ComplexTableMeta<P, ?> table, SQLs.WordAs wordAs, String alias)` - 复杂表带别名
4. `update(SingleTableMeta<T> table)` - 单表无别名
5. `update(ComplexTableMeta<P, ?> table)` - 复杂表无别名

**修饰符支持**：
- `MySQLs.LOW_PRIORITY
- `MySQLs.IGNORE

### 3. PARTITION 子句

**用途**：指定要更新的分区

**形式**：
1. `partition(String partitionName)` - 单个分区
2. `partition(String... partitionNames)` - 多个分区
3. `partition(Consumer<Consumer<String>> consumer)` - 动态分区列表
4. `ifPartition(Consumer<Consumer<String>> consumer)` - 条件性动态分区

### 4. 表别名

**用途**：为表指定别名

**形式**：
- `as(String alias)`

### 5. INDEX HINT 子句（可重复）

**用途**：为查询优化器提供索引使用提示

**形式分类**：

#### USE INDEX
1. `useIndex(String indexName)`
2. `useIndex(String indexName1, String indexName2)`
3. `useIndex(String indexName1, String indexName2, String indexName3)`
4. `useIndex(Consumer<Clause._StaticStringSpaceClause> consumer)`
5. `useIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)`
6. `ifUseIndex(Consumer<Consumer<String>> consumer)`

#### IGNORE INDEX
1. `ignoreIndex(String indexName)`
2. `ignoreIndex(String indexName1, String indexName2)`
3. `ignoreIndex(String indexName1, String indexName2, String indexName3)`
4. `ignoreIndex(Consumer<Clause._StaticStringSpaceClause> consumer)`
5. `ignoreIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)`
6. `ifIgnoreIndex(Consumer<Consumer<String>> consumer)`

#### FORCE INDEX
1. `forceIndex(String indexName)`
2. `forceIndex(String indexName1, String indexName2)`
3. `forceIndex(String indexName1, String indexName2, String indexName3)`
4. `forceIndex(Consumer<Clause._StaticStringSpaceClause> consumer)`
5. `forceIndex(SQLs.SymbolSpace space, Consumer<Consumer<String>> consumer)`
6. `ifForceIndex(Consumer<Consumer<String>> consumer)`

#### 带 FOR/USE 目的的形式：
- `useIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)` 等
- `ignoreIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)` 等
- `forceIndex(SQLs.WordFor wordFor, SQLs.IndexHintPurpose purpose, String indexName)` 等

### 6. SET 子句（可重复）

**用途**：指定要更新的字段和值

**形式**：

#### 基础形式
1. `set(FieldMeta<T> field, Object value)` - 直接赋值
2. `set(FieldMeta<T> field, BiFunction<FieldMeta<T>, E, AssignmentItem> valueOperator, E value)` - 使用操作符
3. `set(FieldMeta<T> field, BiFunction<FieldMeta<T>, Expression, AssignmentItem> fieldOperator, BiFunction<FieldMeta<T>, E, Expression> valueOperator, E value)` - 表达式操作
4. `ifSet(FieldMeta<T> field, Object value)` - 条件赋值
5. `ifSet(FieldMeta<T> field, BiFunction<FieldMeta<T>, E, AssignmentItem> valueOperator, E value)` - 条件操作符赋值
6. `ifSet(FieldMeta<T> field, BiFunction<FieldMeta<T>, Expression, AssignmentItem> fieldOperator, BiFunction<FieldMeta<T>, E, Expression> valueOperator, E value)` - 条件表达式操作

#### 空间操作形式
7. `setSpace(FieldMeta<T> field, BiFunction<FieldMeta<T>, String, Expression> valueOperator)`
8. `setSpace(FieldMeta<T> field, BiFunction<FieldMeta<T>, Expression, AssignmentItem> fieldOperator, BiFunction<FieldMeta<T>, String, Expression> valueOperator)`

#### 动态批量形式
9. `sets(Consumer<UpdateStatement._BatchItemPairs<FieldMeta<T>>> consumer)`

### 7. WHERE 子句（可选）

**用途**：指定更新的条件

**形式**：
1. `where(Consumer<Consumer<IPredicate>> consumer)` - 动态条件
2. `where(IPredicate predicate)` - 单个条件
3. `whereIf(Supplier<IPredicate> supplier)` - 条件性条件
4. `where(Function<T, IPredicate> expOperator, T operand)` - 表达式条件
5. `whereIf(Function<T, IPredicate> expOperator, T value)` - 条件性表达式条件
6. `whereIf(ExpressionOperator<TypedExpression, T, IPredicate> expOperator, BiFunction<TypedExpression, T, Expression> operator, T value)`
7. `whereIf(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator, SQLs.BiOperator operator, T value)`
8. `whereIf(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator, SQLs.BiOperator operator, BiFunction<TypedExpression, T, Expression> func, T value)`
9. `whereIf(BetweenValueOperator<T> expOperator, BiFunction<TypedExpression, T, Expression> operator, T value1, SQLs.WordAnd and, T value2)`
10. `whereIf(BetweenDualOperator<T, U> expOperator, BiFunction<TypedExpression, T, Expression> firstFuncRef, T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second)`

**AND 子句（可重复）**：
- 与 WHERE 子句相同形式，但使用 `and` 和 `ifAnd` 方法

### 8. ORDER BY 子句（可选）

**用途**：指定更新的顺序

**形式**：
1. `orderBy(SortItem sortItem)`
2. `orderBy(SortItem sortItem1, SortItem sortItem2)`
3. `orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3)`
4. `orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3, SortItem sortItem4)`

**追加排序项（可重复）**：
- `spaceComma(SortItem sortItem)`
- `spaceComma(SortItem sortItem1, SortItem sortItem2)`
- 等

### 9. LIMIT 子句（可选）

**用途**：限制更新的行数

**形式**：
1. `limit(Object rowCount)`
2. `limit(BiFunction<MappingType, Number, Expression> operator, long rowCount)`
3. `limit(BiFunction<MappingType, String, Expression> operator, String paramName)`
4. `ifLimit(Object rowCount)`
5. `ifLimit(BiFunction<MappingType, Number, Expression> operator, Number rowCount)`
6. `ifLimit(BiFunction<MappingType, String, Expression> operator, String paramName)`

### 10. 结束语句

**用途**：完成语句构建

**形式**：
- `asUpdate()` - 返回 Update 语句对象

---

## 使用示例

### 示例 1：简单更新

```java
Update stmt = MySQLs.singleUpdate()
    .update(ChinaRegion_.T, AS, "c")
    .set(ChinaRegion_.name, SQLs::literal, "新名称")
    .set(ChinaRegion_.updateTime, SQLs::literal, LocalDateTime.now())
    .where(ChinaRegion_.id::equal, SQLs::param, 1L)
    .asUpdate();
```

### 示例 2：条件更新

```java
Update stmt = MySQLs.singleUpdate()
    .update(ChinaRegion_.T)
    .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, new BigDecimal("1000.00"))
    .whereIf(ChinaRegion_.id::equal, SQLs::param, criteria.getId())
    .and(ChinaRegion_.createTime.less(SQLs::literal, LocalDateTime.now().minusDays(2)))
    .asUpdate();
```

### 示例 3：使用索引提示

```java
Update stmt = MySQLs.singleUpdate()
    .update(ChinaRegion_.T, AS, "c")
    .useIndex("idx_region_gdp")
    .set(ChinaRegion_.name, SQLs::literal, "更新名称")
    .where(ChinaRegion_.regionGdp.greater(SQLs::param, new BigDecimal("10000")))
    .orderBy(ChinaRegion_.createTime.desc())
    .limit(SQLs::literal, 10)
    .asUpdate();
```

### 示例 4：批量更新形式

```java
Update stmt = MySQLs.singleUpdate()
    .update(ChinaRegion_.T)
    .sets(pairs -> pairs
        .set(ChinaRegion_.name, SQLs::literal, "批量名称")
        .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, new BigDecimal("500"))
    )
    .where(ChinaRegion_.parentId::equal, SQLs::param, 0L)
    .asUpdate();
```

---

## 方法重复规则

| 子句 | 是否可重复 | 说明 |
|------|-----------|------|
| WITH 子句 | 否 | 只能调用一次 |
| UPDATE 子句 | 否 | 只能指定一个表 |
| PARTITION 子句 | 否 | 只能调用一次 |
| 表别名 | 否 | 只能指定一个别名 |
| INDEX HINT | 是 | 可以多次调用，累积多个索引提示 |
| SET 子句 | 是 | 可以多次调用，累积多个字段更新 |
| WHERE 子句 | 否 | 只能调用一次，但 AND 子句可重复 |
| ORDER BY 子句 | 否 | 只能调用一次，但 spaceComma 可重复 |
| LIMIT 子句 | 否 | 只能调用一次 |

---

## 最佳实践

1. **始终使用 WHERE 子句**：避免全表更新
2. **合理使用索引提示**：在复杂查询中提高性能
3. **使用批量形式**：更新多个字段时使用 sets()
4. **条件语句**：使用 ifSet、ifWhere 等方法处理可选条件
5. **限制更新行数**：使用 LIMIT 限制更新行数
