---
name: "postgres-scalar-subquery"
description: "完整指南，用于学习和使用 Postgres.scalarSubQuery() 方法链，包括所有可用方法、参数、调用顺序和实际示例。在需要使用标量子查询时调用此技能。"
---

# Postgres.scalarSubQuery() 完整指南

## 概述

`Postgres.scalarSubQuery()` 用于创建一个可作为标量表达式使用的子查询语句。该子查询必须返回单列且单行结果。

## 完整方法链

```
Postgres.scalarSubQuery()
├── [WITH 子句 - 可选]
│   ├── with(String name)
│   ├── withRecursive(String name)
│   └── [动态 WITH 子句]
│       └── ifWith(Consumer<WithClause> consumer)
├── SELECT 子句 - 必需
│   ├── select(Selection... selections)
│   ├── select(Consumer<SelectClause> consumer)
│   ├── select(DistinctModifier modifier, Selection... selections)
│   └── distinctOn(Expression... expressions)
├── [FROM 子句 - 可选]
│   ├── from(TableMeta table)
│   ├── from(TableMeta table, AS, String alias)
│   ├── from(DerivedTable derivedTable)
│   ├── from(DerivedTable derivedTable, AS, String alias)
│   ├── from(SubQuery subQuery)
│   ├── from(SubQuery subQuery, AS, String alias)
│   ├── from(Function<NestedClause, JoinSpec> function)
│   ├── crossJoin(...)
│   ├── join(...)
│   ├── leftJoin(...)
│   ├── rightJoin(...)
│   ├── fullJoin(...)
│   ├── ifJoin(...)
│   ├── ifLeftJoin(...)
│   ├── ifRightJoin(...)
│   ├── ifFullJoin(...)
│   └── ifCrossJoin(...)
├── [WHERE 子句 - 可选]
│   ├── where(Predicate predicate)
│   ├── where(Consumer<WhereClause> consumer)
│   ├── and(Predicate predicate)
│   ├── or(Predicate predicate)
│   └── ifWhere(Consumer<WhereClause> consumer)
├── [GROUP BY 子句 - 可选]
│   ├── groupBy(GroupByItem... items)
│   ├── groupBy(Consumer<GroupByClause> consumer)
│   ├── groupBy(Modifier modifier, GroupByItem... items)
│   └── ifGroupBy(...)
├── [HAVING 子句 - 可选]
│   ├── having(Predicate predicate)
│   ├── having(Consumer<HavingClause> consumer)
│   └── ifHaving(...)
├── [WINDOW 子句 - 可选]
│   ├── window(String name)
│   ├── windows(Consumer<WindowClause> consumer)
│   └── ifWindows(...)
├── [ORDER BY 子句 - 可选]
│   ├── orderBy(OrderItem... items)
│   ├── orderBy(Consumer<OrderByClause> consumer)
│   └── ifOrderBy(...)
├── [LIMIT 子句 - 可选]
│   ├── limit(Expression rowCount)
│   ├── limit(Function<MappingType, Object, Expression> func, Object rowCount)
│   ├── limit(Expression offset, Expression rowCount)
│   └── ifLimit(...)
├── [FETCH 子句 - 可选]
│   ├── fetch(Expression rowCount)
│   └── ifFetch(...)
├── [LOCK 子句 - 可选]
│   ├── forUpdate()
│   ├── forShare()
│   ├── forNoKeyUpdate()
│   ├── forKeyShare()
│   └── ifFor(...)
└── asQuery() - 必需，结束方法链
```

## 可重复与不可重复方法

### 可重复调用的方法
- `and(Predicate predicate)` - WHERE 子句中可以多次调用
- `or(Predicate predicate)` - WHERE 子句中可以多次调用
- `comma(Selection selection)` - SELECT 子句中可以多次添加选择项
- `comma(GroupByItem item)` - GROUP BY 子句中可以多次添加分组项
- `comma(OrderItem item)` - ORDER BY 子句中可以多次添加排序项
- JOIN 相关方法 - 可以添加多个 JOIN

### 不可重复调用的方法
- `select(...)` - 只能调用一次
- `from(...)` - 只能调用一次作为主 FROM 子句
- `where(...)` - 只能调用一次
- `groupBy(...)` - 只能调用一次
- `having(...)` - 只能调用一次
- `orderBy(...)` - 只能调用一次
- `limit(...)` - 只能调用一次
- `asQuery()` - 只能调用一次，结束方法链

## 各子句的调用形式与场景

### 1. SELECT 子句

**形式 1：直接选择列**
```java
Postgres.scalarSubQuery()
    .select(PillUser_.nickName)
```

**形式 2：选择多列（虽然标量子查询通常只返回单列）**
```java
Postgres.scalarSubQuery()
    .select(PillUser_.id, PillUser_.nickName)
```

**形式 3：使用 Consumer 构建**
```java
Postgres.scalarSubQuery()
    .select(s -> s.space(PillUser_.nickName))
```

**形式 4：带修饰符**
```java
Postgres.scalarSubQuery()
    .select(Postgres.DISTINCT, PillUser_.nickName)
```

**使用场景：** 定义标量子查询要返回的单个值。

### 2. FROM 子句

**形式 1：简单表**
```java
.from(PillUser_.T)
```

**形式 2：带别名**
```java
.from(PillUser_.T, Postgres.AS, "u")
```

**形式 3：衍生表/子查询**
```java
.from(Postgres.subQuery()
    .select(...)
    .from(...)
    .asQuery(), Postgres.AS, "sub")
```

**使用场景：** 指定标量子查询的数据来源。

### 3. WHERE 子句

**形式 1：简单条件**
```java
.where(PillUser_.id.equal(Postgres::param, 1))
```

**形式 2：复合条件**
```java
.where(PillUser_.id.equal(Postgres::param, 1))
.and(PillUser_.nickName.equal(Postgres::param, "test"))
```

**形式 3：使用 Consumer**
```java
.where(w -> w.space(PillUser_.id.equal(Postgres::param, 1)))
```

**形式 4：条件性添加**
```java
.ifWhere(w -> {
    if (condition) {
        w.space(PillUser_.id.equal(Postgres::param, 1));
    }
})
```

**使用场景：** 过滤标量子查询的结果。

### 4. GROUP BY & HAVING 子句

**形式 1：简单分组**
```java
.groupBy(PillUser_.userType)
```

**形式 2：带修饰符**
```java
.groupBy(Postgres.ALL, PillUser_.userType)
```

**形式 3：分组加聚合过滤**
```java
.groupBy(PillUser_.userType)
.having(Postgres.count(PillUser_.id).greater(Postgres.literalValue(1)))
```

**使用场景：** 对标量子查询的数据进行分组聚合。

### 5. ORDER BY & LIMIT 子句

**形式 1：排序**
```java
.orderBy(PillUser_.id::desc)
```

**形式 2：限制结果**
```java
.limit(Postgres::literal, 1)
```

**使用场景：** 确保标量子查询只返回单行结果。

## 完整示例

### 示例 1：基础标量子查询
```java
import io.army.criteria.Select;
import io.army.criteria.impl.Postgres;
import static io.army.criteria.impl.Postgres.*;

Select stmt = Postgres.query()
    .select(Postgres.scalarSubQuery()
        .select(PillUser_.nickName)
        .from(PillUser_.T, AS, "u")
        .where(PillUser_.id.equal(Postgres::param, 1))
        .asQuery().as("user_nickname")
    )
    .asQuery();
```

### 示例 2：在 WHERE 中使用标量子查询
```java
Select stmt = Postgres.query()
    .select("u", PERIOD, PillUser_.T)
    .from(PillUser_.T, AS, "u")
    .where(PillUser_.balance.greater(
        Postgres.scalarSubQuery()
            .select(Postgres.avg(BankAccount_.balance))
            .from(BankAccount_.T, AS, "a")
            .asQuery()
    ))
    .asQuery();
```

### 示例 3：带聚合函数的标量子查询
```java
Select stmt = Postgres.query()
    .select(
        Postgres.scalarSubQuery()
            .select(Postgres.count(PillUser_.id))
            .from(PillUser_.T)
            .where(PillUser_.userType.equal(Postgres::literal, PillUserType.PERSON))
            .asQuery().as("person_count"),
        Postgres.scalarSubQuery()
            .select(Postgres.avg(PillUser_.balance))
            .from(PillUser_.T)
            .asQuery().as("avg_balance")
    )
    .asQuery();
```

### 示例 4：条件性构建的标量子查询
```java
final Map<String, Object> criteria = ...;

Select stmt = Postgres.query()
    .select(Postgres.scalarSubQuery()
        .select(PillUser_.nickName)
        .from(PillUser_.T, AS, "u")
        .where(PillUser_.id.equal(Postgres::param, 1))
        .ifWhere(w -> {
            if (criteria.get("userType") != null) {
                w.and(PillUser_.userType.equal(Postgres::param, criteria.get("userType")));
            }
        })
        .orderBy(PillUser_.createTime::desc)
        .limit(Postgres::literal, 1)
        .asQuery().as("r")
    )
    .asQuery();
```

## 注意事项

1. **标量子查询限制**：必须返回单列且单行结果，否则会抛出异常。
2. **验证**：框架会验证子查询的选择列表大小。
3. **性能**：标量子查询可能对性能有影响，特别是在 SELECT 列表中有多个标量子查询时。
4. **上下文**：标量子查询可以引用外部查询的列（相关子查询）。
5. **Postgres 特有功能**：可以使用 Postgres 特有的函数和操作符。

## 常用 Postgres 特有功能

- `Postgres.DISTINCT ON` - 去重修饰符
- `Postgres.ARRAY` - 数组相关操作
- `Postgres.JSON` / `Postgres.JSONB` - JSON 操作
- `Postgres.AT_GT` / `Postgres.CARET_AT` 等操作符 - 范围操作
- `tableSample()` - 表采样
- `WITH RECURSIVE` - 递归 CTE

---
*本技能可根据代码库更新进行自我进化。*
