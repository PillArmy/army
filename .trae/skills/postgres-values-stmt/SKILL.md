---
name: "postgres-values-stmt"
description: "提供 Postgres.valuesStmt() 方法链的完整文档、示例和使用指南。Invoke when user needs help with Postgres VALUES statement DSL or method chain documentation."
---

# Postgres.valuesStmt() 方法链完整文档

## 概述
`Postgres.valuesStmt()` 用于创建 PostgreSQL 的 VALUES 语句，支持静态和动态值定义，可与 ORDER BY、LIMIT、OFFSET、FETCH、UNION 等功能组合使用。

## 完整方法链 Diagram

```
Postgres.valuesStmt()
├── .values()
│   ├── .parens(Consumer<_ValueStaticColumnSpaceClause>)
│   │   ├── .space(Object exp)
│   │   ├── .space(Object exp1, Object exp2)
│   │   ├── .space(Object exp1, Object exp2, Object exp3)
│   │   ├── .space(Object exp1, Object exp2, Object exp3, Object exp4)
│   │   ├── .space(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5)
│   │   ├── .space(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5, Object exp6)
│   │   ├── .space(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5, Object exp6, Object exp7)
│   │   ├── .space(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5, Object exp6, Object exp7, Object exp8)
│   │   ├── .comma(Object exp)
│   │   ├── .comma(Object exp1, Object exp2)
│   │   ├── .comma(Object exp1, Object exp2, Object exp3)
│   │   ├── .comma(Object exp1, Object exp2, Object exp3, Object exp4)
│   │   ├── .comma(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5)
│   │   ├── .comma(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5, Object exp6)
│   │   ├── .comma(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5, Object exp6, Object exp7)
│   │   └── .comma(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5, Object exp6, Object exp7, Object exp8)
│   └── .comma()
│       └── [重复 .parens(...) + .comma() 以添加多行]
│
├── .values(Consumer<ValuesParens>)
│
├── .parens(Function<ValuesSpec<_UnionOrderBySpec>, _UnionOrderBySpec>)
│
├── [以下子句在添加至少一行值后可用]
│   ├── .orderBy(Selection... selections)
│   │   ├── .comma(Selection... selections)
│   │   │   └── [可重复 .comma(...) 以添加更多排序列]
│   │   ├── .limit(BiFunction<Object, Object, Expression> funcRef, Object count)
│   │   ├── .limit(SQLs.WordLimitAll all)
│   │   ├── .offset(BiFunction<Object, Object, Expression> funcRef, Object offset, SQLs.WordRow row)
│   │   ├── .fetch(SQLs.WordFirstNext firstNext, BiFunction<Object, Object, Expression> funcRef, Object count, SQLs.WordRow row, SQLs.WordOnlyTies onlyTies)
│   │   ├── .union()
│   │   ├── .unionAll()
│   │   ├── .unionDistinct()
│   │   ├── .intersect()
│   │   ├── .intersectAll()
│   │   ├── .intersectDistinct()
│   │   ├── .except()
│   │   ├── .exceptAll()
│   │   ├── .exceptDistinct()
│   │   └── .asValues()
│   │
│   ├── .limit(BiFunction<Object, Object, Expression> funcRef, Object count)
│   │   ├── .offset(BiFunction<Object, Object, Expression> funcRef, Object offset, SQLs.WordRow row)
│   │   ├── .fetch(SQLs.WordFirstNext firstNext, BiFunction<Object, Object, Expression> funcRef, Object count, SQLs.WordRow row, SQLs.WordOnlyTies onlyTies)
│   │   └── .asValues()
│   │
│   ├── .offset(BiFunction<Object, Object, Expression> funcRef, Object offset, SQLs.WordRow row)
│   │   ├── .fetch(SQLs.WordFirstNext firstNext, BiFunction<Object, Object, Expression> funcRef, Object count, SQLs.WordRow row, SQLs.WordOnlyTies onlyTies)
│   │   └── .asValues()
│   │
│   ├── .fetch(SQLs.WordFirstNext firstNext, BiFunction<Object, Object, Expression> funcRef, Object count, SQLs.WordRow row, SQLs.WordOnlyTies onlyTies)
│   │   └── .asValues()
│   │
│   ├── .union()
│   ├── .unionAll()
│   ├── .unionDistinct()
│   ├── .intersect()
│   ├── .intersectAll()
│   ├── .intersectDistinct()
│   ├── .except()
│   ├── .exceptAll()
│   ├── .exceptDistinct()
│   │   ├── [UNION 后可以：]
│   │   │   ├── .values()
│   │   │   ├── .values(Consumer<ValuesParens>)
│   │   │   ├── .select(...)
│   │   │   └── .parens(Function<...>)
│   │   └── [然后又可以使用上述子句]
│   │
│   └── .asValues()
```

## 方法可重复性说明

### 可重复的方法
1. **`.comma()`** - 添加多个 VALUES 行（每添加一行后可调用）
2. **`.comma(Selection... selections)`** - 添加多个排序列（在 ORDER BY 子句中）
3. **UNION 子句** - `.union()/.unionAll()/.unionDistinct()/.intersect()/.except()` 等（可多次使用组合多个行集）

### 不可重复的方法
1. **`.values()`** - 每个 VALUES 语句块只需调用一次
2. **`.values(Consumer<ValuesParens>)`** - 用于动态值，每个块调用一次
3. **`.orderBy()`** - 每个查询只需调用一次（然后用 .comma() 添加更多列）
4. **`.limit()`** - 每个查询只需调用一次
5. **`.offset()`** - 每个查询只需调用一次
6. **`.fetch()`** - 每个查询只需调用一次
7. **`.asValues()`** - 构建最终语句，每个语句只需调用一次

## 各子句的多种形式和使用场景

### 1. VALUES 行定义 - 两种形式

#### 形式 A：静态值（常用）
```java
.values()
.parens(s -> s.space(val1, val2, ...)
             .comma(val3, val4, ...))
```
**场景**：直接在代码中定义已知的值，适合静态数据或简单情况

#### 形式 B：动态值
```java
.values(Consumer<ValuesParens>)
```
**场景**：通过 Consumer 动态构建值，适合程序化生成数据的复杂场景

### 2. 列值定义 - 多种方式

#### 方式 1：单个值
```java
.space(value)
```

#### 方式 2：多个值（最多 8 个）
```java
.space(val1, val2, val3, val4)
.space(val1, val2, val3, val4, val5, val6, val7, val8)
```

#### 方式 3：使用逗号追加
```java
.space(val1, val2)
.comma(val3)
.comma(val4, val5)
```

### 3. 排序子句 - 两种形式

#### 形式 A：静态排序
```java
.orderBy(SQLs.refSelection("column2"), SQLs.refSelection(1)::desc)
```

#### 形式 B：动态排序
```java
.orderBy(Consumer<OrderBySpec>)
```

### 4. 分页子句 - 多种组合

#### 方式 A：仅 LIMIT
```java
.limit(SQLs::literal, 10)
```

#### 方式 B：OFFSET + LIMIT
```java
.limit(SQLs::literal, 10)
.offset(SQLs::literal, 20, ROWS)
```

#### 方式 C：PostgreSQL FETCH 语法
```java
.offset(SQLs::literal, 20, ROWS)
.fetch(FIRST, SQLs::literal, 10, ROWS, ONLY)
```

### 5. 行集操作子句 - UNION 相关

```java
.union()           // UNION（去重）
.unionAll()        // UNION ALL（保留重复）
.unionDistinct()   // UNION DISTINCT
.intersect()       // INTERSECT
.intersectAll()    // INTERSECT ALL
.except()          // EXCEPT
.exceptAll()       // EXCEPT ALL
```

## 完整使用示例

### 示例 1：简单 VALUES 语句
```java
final Values stmt = Postgres.valuesStmt()
        .values()
        .parens(s -> s.space(1, "海问香", Decimals.valueOf("9999.88"), now)
                .comma(DayOfWeek.MONDAY, TRUE, SQLs.literalValue(1).plus(SQLs::literal, 3)))
        .comma()
        .parens(s -> s.space(2, "大仓", Decimals.valueOf("9999.66"), now.plusDays(1))
                .comma(DayOfWeek.SUNDAY, TRUE, SQLs.literalValue(13).minus(SQLs::literal, 3)))
        .orderBy(SQLs.refSelection("column2"), SQLs.refSelection(2)::desc)
        .limit(SQLs::literal, 10)
        .asValues();
```

### 示例 2：UNION ALL 两个 VALUES
```java
final Values stmt = Postgres.valuesStmt()
        .values()
        .parens(s -> s.space(1, "海问香", Decimals.valueOf("9999.88"), now))
        .comma()
        .parens(s -> s.space(2, "大仓", Decimals.valueOf("9999.66"), now.plusDays(1)))
        .unionAll()
        .values()
        .parens(s -> s.space(3, "卡拉肖克·玲", Decimals.valueOf("6666.88"), now.minusDays(3)))
        .comma()
        .parens(s -> s.space(4, "幽弥狂", Decimals.valueOf("8888.88"), now.minusDays(8)))
        .orderBy(SQLs.refSelection("column2"))
        .asValues();
```

### 示例 3：使用括号嵌套
```java
final Values stmt = Postgres.valuesStmt()
        .parens(v -> v.values()
                .parens(s -> s.space(1, "海问香", Decimals.valueOf("9999.88"), now))
                .comma()
                .parens(s -> s.space(2, "大仓", Decimals.valueOf("9999.66"), now.plusDays(1)))
                .orderBy(SQLs.refSelection("column2"))
                .limit(SQLs::literal, 2)
                .asValues())
        .unionAll()
        .values()
        .parens(s -> s.space(3, "卡拉肖克·玲", Decimals.valueOf("6666.88"), now.minusDays(3)))
        .asValues();
```

### 示例 4：VALUES UNION SELECT
```java
final Values stmt = Postgres.valuesStmt()
        .values()
        .parens(s -> s.space(1, "海问香", Decimals.valueOf("9999.88"), now))
        .unionAll()
        .select(ChinaRegion_.population, ChinaRegion_.name, ChinaRegion_.regionGdp, ChinaRegion_.createTime)
        .comma(SQLs.literalValue(DayOfWeek.TUESDAY).as("week"), FALSE.as("myBoolean"))
        .from(ChinaRegion_.T, AS, "t")
        .asQuery();
```

### 示例 5：使用 OFFSET 和 FETCH（PostgreSQL 特有）
```java
final Values stmt = Postgres.valuesStmt()
        .values()
        .parens(s -> s.space(1, "海问香", Decimals.valueOf("9999.88"), now))
        .comma()
        .parens(s -> s.space(2, "大仓", Decimals.valueOf("9999.66"), now.plusDays(1)))
        .comma()
        .parens(s -> s.space(3, "卡拉肖克·玲", Decimals.valueOf("6666.88"), now.minusDays(3)))
        .orderBy(SQLs.refSelection("column2"), SQLs.refSelection(2)::desc)
        .offset(SQLs::literal, 0, ROWS)
        .fetch(FIRST, SQLs::literal, 4, ROWS, ONLY)
        .asValues();
```

### 示例 6：SubQuery 中使用 subValues()
```java
final Select stmt = Postgres.query()
        .select(s -> s.space("v", PERIOD, ASTERISK))
        .from(Postgres.subValues()
                .values()
                .parens(s -> s.space(1, "海问香", Decimals.valueOf("9999.88"), now))
                .comma()
                .parens(s -> s.space(2, "大仓", Decimals.valueOf("9999.66"), now.plusDays(1)))
                .orderBy(SQLs.refSelection("column2"))
                .limit(SQLs::literal, 4)
                .asValues()
        ).as("v")
        .asQuery();
```

## 参考文件
- Postgres.java: /Users/zoro/repositories/trae/java/hub/army/army-postgre/src/main/java/io/army/criteria/impl/Postgres.java
- PostgreValues.java: /Users/zoro/repositories/trae/java/hub/army/army-postgre/src/main/java/io/army/criteria/postgre/PostgreValues.java
- PostgreSimpleValues.java: /Users/zoro/repositories/trae/java/hub/army/army-postgre/src/main/java/io/army/criteria/impl/PostgreSimpleValues.java
- ValuesTests.java: /Users/zoro/repositories/trae/java/hub/army/army-example/src/test/java/io/army/session/sync/postgre/ValuesTests.java
