---
name: "mysql-values-stmt"
description: "提供 MySQLs.valuesStmt() 方法链的完整文档、示例和使用指南。Invoke when user needs help with MySQL VALUES statement DSL or method chain documentation."
---

# MySQLs.valuesStmt() 方法链完整文档

## 概述
`MySQLs.valuesStmt()` 用于创建 MySQL 的 VALUES 语句，支持静态和动态值定义，可与 ORDER BY、LIMIT、UNION 等功能组合使用。

## 完整方法链 Diagram

```
MySQLs.valuesStmt()
├── .values()
│   ├── .row(Consumer<_ValueStaticColumnSpaceClause>)
│   │   ├── .space(Object exp)
│   │   ├── .space(Object exp1, Object exp2)
│   │   ├── .space(Object exp1, Object exp2, Object exp3)
│   │   ├── .space(Object exp1, Object exp2, Object exp3, Object exp4)
│   │   ├── .space(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5)
│   │   ├── .space(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5, Object exp6)
│   │   ├── .space(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5, Object exp6, Object exp7)
│   │   └── .space(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5, Object exp6, Object exp7, Object exp8)
│   │   ├── .comma(Object exp)
│   │   ├── .comma(Object exp1, Object exp2)
│   │   ├── .comma(Object exp1, Object exp2, Object exp3)
│   │   ├── .comma(Object exp1, Object exp2, Object exp3, Object exp4)
│   │   ├── .comma(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5)
│   │   ├── .comma(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5, Object exp6)
│   │   ├── .comma(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5, Object exp6, Object exp7)
│   │   └── .comma(Object exp1, Object exp2, Object exp3, Object exp4, Object exp5, Object exp6, Object exp7, Object exp8)
│   └── .comma()
│       └── [重复 .row(...) + .comma() 以添加多行]
│
├── .values(Consumer<ValuesRows>)
│
├── .parens(Function<ValuesSpec<_UnionOrderBySpec>, _UnionOrderBySpec>)
│
├── [以下子句在添加至少一行值后可用]
│   ├── .orderBy(Selection... selections)
│   │   ├── .comma(Selection... selections)
│   │   │   └── [可重复 .comma(...) 以添加更多排序列]
│   │   ├── .limit(BiFunction<Object, Object, Expression> funcRef, Object count)
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
│   ├── .orderBy(Consumer<_OrderBySpec>)
│   │
│   ├── .limit(BiFunction<Object, Object, Expression> funcRef, Object count)
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
│   │   │   ├── .values(Consumer<ValuesRows>)
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
2. **`.values(Consumer<ValuesRows>)`** - 用于动态值，每个块调用一次
3. **`.orderBy()`** - 每个查询只需调用一次（然后用 .comma() 添加更多列）
4. **`.limit()`** - 每个查询只需调用一次
5. **`.asValues()`** - 构建最终语句，每个语句只需调用一次

## 各子句的多种形式和使用场景

### 1. VALUES 行定义 - 两种形式

#### 形式 A：静态值（常用）
```java
.values()
.row(s -> s.space(val1, val2, ...)
            .comma(val3, val4, ...))
```
**场景**：直接在代码中定义已知的值，适合静态数据或简单情况

#### 形式 B：动态值
```java
.values(Consumer<ValuesRows>)
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
.orderBy(SQLs.refSelection("column1"), SQLs.refSelection(2)::desc)
```

#### 形式 B：动态排序
```java
.orderBy(Consumer<_OrderBySpec>)
```

### 4. 分页子句 - LIMIT

```java
.limit(SQLs::literal, 10)
```

### 5. 行集操作子句 - UNION 相关

```java
.union()           // UNION（去重）
.unionAll()        // UNION ALL（保留重复）
.unionDistinct()   // UNION DISTINCT
.intersect()       // INTERSECT
.intersectAll()    // INTERSECT ALL
.intersectDistinct() // INTERSECT DISTINCT
.except()          // EXCEPT
.exceptAll()       // EXCEPT ALL
.exceptDistinct()  // EXCEPT DISTINCT
```

## 完整使用示例

### 示例 1：简单 VALUES 语句
```java
final Values stmt = MySQLs.valuesStmt()
        .values()
        .row(s -> s.space(1, "海问香", Decimals.valueOf("9999.88"), now)
                .comma(DayOfWeek.MONDAY, TRUE, SQLs.literalValue(1).plus(SQLs::literal, 3)))
        .comma()
        .row(s -> s.space(2, "大仓", Decimals.valueOf("9999.66"), now.plusDays(1))
                .comma(DayOfWeek.SUNDAY, TRUE, SQLs.literalValue(13).minus(SQLs::literal, 3)))
        .comma()
        .row(s -> s.space(3, "卡拉肖克·玲", Decimals.valueOf("6666.88"), now.minusDays(3))
                .comma(DayOfWeek.FRIDAY, TRUE, SQLs.literalValue(3).minus(SQLs::literal, 3)))
        .comma()
        .row(s -> s.space(4, "幽弥狂", Decimals.valueOf("8888.88"), now.minusDays(8))
                .comma(DayOfWeek.TUESDAY, FALSE, SQLs.literalValue(81).divide(SQLs::literal, 3)))
        .orderBy(SQLs.refSelection("column1"), SQLs.refSelection(2)::desc)
        .limit(SQLs::literal, 4)
        .asValues();
```

### 示例 2：使用动态值
```java
final Values stmt = MySQLs.valuesStmt()
        .values(r -> r.row(s -> s.space(1, "海问香", Decimals.valueOf("9999.88"), now)
                                .comma(DayOfWeek.MONDAY, TRUE, SQLs.literalValue(1).plus(SQLs::literal, 3)))
                        .row(s -> s.space(2, "大仓", Decimals.valueOf("9999.66"), now.plusDays(1))
                                .comma(DayOfWeek.SUNDAY, TRUE, SQLs.literalValue(13).minus(SQLs::literal, 3)))
                        .row(s -> s.space(3, "卡拉肖克·玲", Decimals.valueOf("6666.88"), now.minusDays(3))
                                .comma(DayOfWeek.FRIDAY, TRUE, SQLs.literalValue(3).minus(SQLs::literal, 3)))
                        .row(s -> s.space(4, "幽弥狂", Decimals.valueOf("8888.88"), now.minusDays(8))
                                .comma(DayOfWeek.TUESDAY, FALSE, SQLs.literalValue(81).divide(SQLs::literal, 3))))
        .orderBy(SQLs.refSelection("column1"), SQLs.refSelection(2)::desc)
        .limit(SQLs::literal, 4)
        .asValues();
```

### 示例 3：UNION ALL 两个 VALUES
```java
final Values stmt = MySQLs.valuesStmt()
        .values()
        .row(s -> s.space(1, "海问香", Decimals.valueOf("9999.88"), now)
                .comma(DayOfWeek.MONDAY, TRUE, SQLs.literalValue(1).plus(SQLs::literal, 3)))
        .comma()
        .row(s -> s.space(2, "大仓", Decimals.valueOf("9999.66"), now.plusDays(1))
                .comma(DayOfWeek.SUNDAY, TRUE, SQLs.literalValue(13).minus(SQLs::literal, 3)))
        .comma()
        .row(s -> s.space(3, "卡拉肖克·玲", Decimals.valueOf("6666.88"), now.minusDays(3))
                .comma(DayOfWeek.FRIDAY, TRUE, SQLs.literalValue(3).minus(SQLs::literal, 3)))
        .comma()
        .row(s -> s.space(4, "幽弥狂", Decimals.valueOf("8888.88"), now.minusDays(8))
                .comma(DayOfWeek.TUESDAY, FALSE, SQLs.literalValue(81).divide(SQLs::literal, 3)))
        .unionAll()
        .values()
        .row(s -> s.space(1, "海问香", Decimals.valueOf("9999.88"), now)
                .comma(DayOfWeek.MONDAY, TRUE, SQLs.literalValue(1).plus(SQLs::literal, 3)))
        .comma()
        .row(s -> s.space(2, "大仓", Decimals.valueOf("9999.66"), now.plusDays(1))
                .comma(DayOfWeek.SUNDAY, TRUE, SQLs.literalValue(13).minus(SQLs::literal, 3)))
        .comma()
        .row(s -> s.space(3, "卡拉肖克·玲", Decimals.valueOf("6666.88"), now.minusDays(3))
                .comma(DayOfWeek.FRIDAY, TRUE, SQLs.literalValue(3).minus(SQLs::literal, 3)))
        .comma()
        .row(s -> s.space(4, "幽弥狂", Decimals.valueOf("8888.88"), now.minusDays(8))
                .comma(DayOfWeek.TUESDAY, FALSE, SQLs.literalValue(81).divide(SQLs::literal, 3)))
        .orderBy(SQLs.refSelection("column1"), SQLs.refSelection(2)::desc)
        .limit(SQLs::literal, 8)
        .asValues();
```

### 示例 4：使用括号嵌套
```java
final Values stmt = MySQLs.valuesStmt()
        .parens(v -> v.values()
                .row(s -> s.space(1, "海问香", Decimals.valueOf("9999.88"), now)
                        .comma(DayOfWeek.MONDAY, TRUE, SQLs.literalValue(1).plus(SQLs::literal, 3)))
                .comma()
                .row(s -> s.space(2, "大仓", Decimals.valueOf("9999.66"), now.plusDays(1))
                        .comma(DayOfWeek.SUNDAY, TRUE, SQLs.literalValue(13).minus(SQLs::literal, 3)))
                .comma()
                .row(s -> s.space(3, "卡拉肖克·玲", Decimals.valueOf("6666.88"), now.minusDays(3))
                        .comma(DayOfWeek.FRIDAY, TRUE, SQLs.literalValue(3).minus(SQLs::literal, 3)))
                .comma()
                .row(s -> s.space(4, "幽弥狂", Decimals.valueOf("8888.88"), now.minusDays(8))
                        .comma(DayOfWeek.TUESDAY, FALSE, SQLs.literalValue(81).divide(SQLs::literal, 3)))
                .orderBy(SQLs.refSelection("column1"), SQLs.refSelection(2)::desc)
                .limit(SQLs::literal, 4)
                .asValues())
        .unionAll()
        .values()
        .row(s -> s.space(1, "海问香", Decimals.valueOf("9999.88"), now)
                .comma(DayOfWeek.MONDAY, TRUE, SQLs.literalValue(1).plus(SQLs::literal, 3)))
        .comma()
        .row(s -> s.space(2, "大仓", Decimals.valueOf("9999.66"), now.plusDays(1))
                .comma(DayOfWeek.SUNDAY, TRUE, SQLs.literalValue(13).minus(SQLs::literal, 3)))
        .comma()
        .row(s -> s.space(3, "卡拉肖克·玲", Decimals.valueOf("6666.88"), now.minusDays(3))
                .comma(DayOfWeek.FRIDAY, TRUE, SQLs.literalValue(3).minus(SQLs::literal, 3)))
        .comma()
        .row(s -> s.space(4, "幽弥狂", Decimals.valueOf("8888.88"), now.minusDays(8))
                .comma(DayOfWeek.TUESDAY, FALSE, SQLs.literalValue(81).divide(SQLs::literal, 3)))
        .orderBy(SQLs.refSelection("column1"), SQLs.refSelection(2)::desc)
        .limit(SQLs::literal, 8)
        .asValues();
```

### 示例 5：VALUES UNION SELECT
```java
final Values stmt = MySQLs.valuesStmt()
        .values()
        .row(s -> s.space(1, "海问香", Decimals.valueOf("9999.88"), now)
                .comma(DayOfWeek.MONDAY, TRUE, SQLs.literalValue(1).plus(SQLs::literal, 3)))
        .comma()
        .row(s -> s.space(2, "大仓", Decimals.valueOf("9999.66"), now.plusDays(1))
                .comma(DayOfWeek.SUNDAY, TRUE, SQLs.literalValue(13).minus(SQLs::literal, 3)))
        .comma()
        .row(s -> s.space(3, "卡拉肖克·玲", Decimals.valueOf("6666.88"), now.minusDays(3))
                .comma(DayOfWeek.FRIDAY, TRUE, SQLs.literalValue(3).minus(SQLs::literal, 3)))
        .comma()
        .row(s -> s.space(4, "幽弥狂", Decimals.valueOf("8888.88"), now.minusDays(8))
                .comma(DayOfWeek.TUESDAY, FALSE, SQLs.literalValue(81).divide(SQLs::literal, 3)))
        .unionAll()
        .select(ChinaRegion_.population, ChinaRegion_.name, ChinaRegion_.regionGdp, ChinaRegion_.createTime)
        .comma(SQLs.literalValue(DayOfWeek.TUESDAY).as("week"), FALSE.as("myBoolean"))
        .comma(SQLs.literalValue(81).as("number"))
        .from(ChinaRegion_.T, AS, "t")
        .where(ChinaRegion_.id.in(SQLs::rowLiteral, extractRegionIdList(regionList)))
        .asQuery();
```

### 示例 6：SubQuery 中使用 subValues()
```java
final Select stmt = MySQLs.query()
        .select(s -> s.space("v", PERIOD, ASTERISK))
        .from(MySQLs.subValues()
                .values()
                .row(s -> s.space(1, "海问香", Decimals.valueOf("9999.88"), now)
                        .comma(DayOfWeek.MONDAY, TRUE, SQLs.literalValue(1).plus(SQLs::literal, 3)))
                .comma()
                .row(s -> s.space(2, "大仓", Decimals.valueOf("9999.66"), now.plusDays(1))
                        .comma(DayOfWeek.SUNDAY, TRUE, SQLs.literalValue(13).minus(SQLs::literal, 3)))
                .comma()
                .row(s -> s.space(3, "卡拉肖克·玲", Decimals.valueOf("6666.88"), now.minusDays(3))
                        .comma(DayOfWeek.FRIDAY, TRUE, SQLs.literalValue(3).minus(SQLs::literal, 3)))
                .comma()
                .row(s -> s.space(4, "幽弥狂", Decimals.valueOf("8888.88"), now.minusDays(8))
                        .comma(DayOfWeek.TUESDAY, FALSE, SQLs.literalValue(81).divide(SQLs::literal, 3)))
                .orderBy(SQLs.refSelection("column1"), SQLs.refSelection(2)::desc)
                .limit(SQLs::literal, 4)
                .asValues()
        ).as("v")
        .asQuery();
```

## 参考文件
- MySQLs.java: /Users/zoro/repositories/trae/java/hub/army/army-mysql/src/main/java/io/army/criteria/impl/MySQLs.java
- MySQLValues.java: /Users/zoro/repositories/trae/java/hub/army/army-mysql/src/main/java/io/army/criteria/mysql/MySQLValues.java
- MySQLSimpleValues.java: /Users/zoro/repositories/trae/java/hub/army/army-mysql/src/main/java/io/army/criteria/impl/MySQLSimpleValues.java
- MySQLValuesUnitTests.java: /Users/zoro/repositories/trae/java/hub/army/army-example/src/test/java/io/army/criteria/mysql/unit/MySQLValuesUnitTests.java
- ValuesTests.java: /Users/zoro/repositories/trae/java/hub/army/army-example/src/test/java/io/army/session/sync/mysql/ValuesTests.java
