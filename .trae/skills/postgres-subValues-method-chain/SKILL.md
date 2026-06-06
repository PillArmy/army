
---
name: "postgres-subValues-method-chain"
description: "Postgres.subValues() 方法链的完整文档，包括方法链图、可重复方法、各子句的多种调用形式和示例代码。Invoke when user needs to use Postgres.subValues() or build sub query VALUES statements."
---

# Postgres.subValues() 方法链完整文档

## 概述

Postgres.subValues() 用于创建子查询中的 VALUES 语句，可嵌套在其他查询中使用。

## 方法链完整图

```
Postgres.subValues()
  ├── 起始方法
  │   ├── values() → _StaticValuesRowClause&lt;I&gt;
  │   ├── values(Consumer&lt;ValuesParens&gt;) → _OrderBySpec&lt;I&gt;
  │   └── parens(Function&lt;ValuesSpec, _UnionOrderBySpec&gt;) → _UnionOrderBySpec&lt;I&gt;
  ├── Values 数据行定义
  │   ├── parens(Consumer&lt;_ValueStaticColumnSpaceClause&gt;) → _StaticValuesRowCommaSpec&lt;I&gt;
  │   ├── parens(SymbolSpace, Consumer&lt;_ValuesDynamicColumnClause&gt;) → _StaticValuesRowCommaSpec&lt;I&gt;
  │   │   └── space() / comma() 列值定义
  │   │       ├── space(Object) → Item
  │   │       ├── space(Object, Object) → _ValueStaticColumnDualCommaClause
  │   │       ├── space(Object, Object, Object) → Item
  │   │       ├── space(Object, Object, Object, Object) → _ValueStaticColumnQuadraCommaClause
  │   │       ├── space(Object, Object, Object, Object, Object) → _ValueStaticColumnOctupleCommaClause
  │   │       ├── space(Object, Object, Object, Object, Object, Object) → _ValueStaticColumnOctupleCommaClause
  │   │       ├── space(Object, Object, Object, Object, Object, Object, Object) → _ValueStaticColumnOctupleCommaClause
  │   │       ├── space(Object, Object, Object, Object, Object, Object, Object, Object) → _ValueStaticColumnOctupleCommaClause
  │   │       ├── comma(Object) → Item
  │   │       ├── comma(Object, Object) → _ValueStaticColumnDualCommaClause
  │   │       ├── comma(Object, Object, Object) → Item
  │   │       ├── comma(Object, Object, Object, Object) → _ValueStaticColumnQuadraCommaClause
  │   │       ├── comma(Object, Object, Object, Object, Object) → _ValueStaticColumnOctupleCommaClause
  │   │       ├── comma(Object, Object, Object, Object, Object, Object) → _ValueStaticColumnOctupleCommaClause
  │   │       ├── comma(Object, Object, Object, Object, Object, Object, Object) → _ValueStaticColumnOctupleCommaClause
  │   │       └── comma(Object, Object, Object, Object, Object, Object, Object, Object) → _ValueStaticColumnOctupleCommaClause
  │   ├── comma() → _StaticValuesRowClause&lt;I&gt; (可重复：是)
  │   │   └── 重复添加行数据
  ├── ORDER BY 子句
  │   ├── orderBy(SortItem) → _OrderByCommaSpec&lt;I&gt;
  │   ├── orderBy(SortItem, SortItem) → _OrderByCommaSpec&lt;I&gt;
  │   ├── orderBy(SortItem, SortItem, SortItem) → _OrderByCommaSpec&lt;I&gt;
  │   ├── orderBy(SortItem, SortItem, SortItem, SortItem) → _OrderByCommaSpec&lt;I&gt;
  │   ├── orderBy(Consumer&lt;Consumer&lt;SortItem&gt;&gt;) → _LimitSpec&lt;I&gt;
  │   ├── ifOrderBy(Consumer&lt;Consumer&lt;SortItem&gt;&gt;) → _LimitSpec&lt;I&gt;
  │   └── spaceComma(SortItem...) → _OrderByCommaSpec&lt;I&gt; (可重复：是)
  ├── LIMIT 子句
  │   ├── limit(Object) → _OffsetSpec&lt;I&gt;
  │   ├── limit(BiFunction, long) → _OffsetSpec&lt;I&gt;
  │   ├── ifLimit(Object) → _OffsetSpec&lt;I&gt;
  │   ├── ifLimit(BiFunction, Number) → _OffsetSpec&lt;I&gt;
  │   └── limitAll() → _OffsetSpec&lt;I&gt;
  ├── OFFSET 子句 (PostgreSQL 特有)
  │   ├── offset(Expression, FetchRow) → _FetchSpec&lt;I&gt;
  │   ├── offset(BiFunction, long, FetchRow) → _FetchSpec&lt;I&gt;
  │   ├── offset(BiFunction, Supplier, FetchRow) → _FetchSpec&lt;I&gt;
  │   ├── offset(BiFunction, Function, String, FetchRow) → _FetchSpec&lt;I&gt;
  │   ├── ifOffset(BiFunction, Number, FetchRow) → _FetchSpec&lt;I&gt;
  │   ├── ifOffset(BiFunction, Supplier, FetchRow) → _FetchSpec&lt;I&gt;
  │   └── ifOffset(BiFunction, Function, String, FetchRow) → _FetchSpec&lt;I&gt;
  ├── FETCH 子句 (PostgreSQL 特有)
  │   ├── fetch(FetchFirstNext, Expression, FetchRow, FetchOnlyWithTies) → _AsValuesClause&lt;I&gt;
  │   ├── fetch(FetchFirstNext, BiFunction, long, FetchRow, FetchOnlyWithTies) → _AsValuesClause&lt;I&gt;
  │   ├── fetch(FetchFirstNext, BiFunction, Supplier, FetchRow, FetchOnlyWithTies) → _AsValuesClause&lt;I&gt;
  │   ├── fetch(FetchFirstNext, BiFunction, Function, String, FetchRow, FetchOnlyWithTies) → _AsValuesClause&lt;I&gt;
  │   ├── ifFetch(FetchFirstNext, BiFunction, Number, FetchRow, FetchOnlyWithTies) → _AsValuesClause&lt;I&gt;
  │   ├── ifFetch(FetchFirstNext, BiFunction, Supplier, FetchRow, FetchOnlyWithTies) → _AsValuesClause&lt;I&gt;
  │   └── ifFetch(FetchFirstNext, BiFunction, Function, String, FetchRow, FetchOnlyWithTies) → _AsValuesClause&lt;I&gt;
  ├── UNION 相关操作
  │   ├── union() → _QueryComplexSpec&lt;I&gt;
  │   ├── unionAll() → _QueryComplexSpec&lt;I&gt;
  │   ├── unionDistinct() → _QueryComplexSpec&lt;I&gt;
  │   ├── except() → _QueryComplexSpec&lt;I&gt;
  │   ├── exceptAll() → _QueryComplexSpec&lt;I&gt;
  │   ├── exceptDistinct() → _QueryComplexSpec&lt;I&gt;
  │   ├── intersect() → _QueryComplexSpec&lt;I&gt;
  │   ├── intersectAll() → _QueryComplexSpec&lt;I&gt;
  │   └── intersectDistinct() → _QueryComplexSpec&lt;I&gt;
  └── 结束语句
      └── asValues() → SubValues
```

## 各子句详细说明

### 1. 起始方法

#### 方法 1：values()
```java
PostgreValues.ValuesSpec&lt;I&gt; values()
```
**用途**：开始静态 VALUES 语句构建
**返回类型**：`_StaticValuesRowClause&lt;I&gt;`
**可重复**：否
**示例**：
```java
Postgres.subValues()
    .values()
    .parens(v -&gt; v.space(1, "张三"))
    .asValues();
```

#### 方法 2：values(Consumer&lt;ValuesParens&gt;)
```java
_OrderBySpec&lt;I&gt; values(Consumer&lt;ValuesParens&gt; consumer)
```
**用途**：使用 Consumer 一次性定义多个 VALUES 行
**返回类型**：`_OrderBySpec&lt;I&gt;`
**可重复**：否
**示例**：
```java
Postgres.subValues()
    .values(v -&gt; {
        v.parens(row -&gt; row.space(1, "张三"));
        v.parens(row -&gt; row.space(2, "李四"));
    })
    .asValues();
```

#### 方法 3：parens(Function)
```java
_UnionOrderBySpec&lt;I&gt; parens(Function&lt;ValuesSpec&lt;_UnionOrderBySpec&lt;I&gt;&gt;, _UnionOrderBySpec&lt;I&gt;&gt; function)
```
**用途**：创建带括号的 VALUES 子查询
**返回类型**：`_UnionOrderBySpec&lt;I&gt;`
**可重复**：否

### 2. VALUES 行定义

#### 静态列值定义
**多种 parens 方法形式**：

**形式 1：静态空间方式**
```java
_StaticValuesRowCommaSpec&lt;I&gt; parens(Consumer&lt;_ValueStaticColumnSpaceClause&gt; consumer)
```
**用途**：使用静态列值定义单行数据
**场景**：列数固定且已知的情况
**示例**：
```java
.parens(row -&gt; row
    .space(1, "张三", BigDecimal.valueOf(9999.88), LocalDate.now())
    .comma(DayOfWeek.MONDAY, true)
)
```

**形式 2：动态空间方式**
```java
_StaticValuesRowCommaSpec&lt;I&gt; parens(SQLs.SymbolSpace space, Consumer&lt;_ValuesDynamicColumnClause&gt; consumer)
```
**用途**：使用动态方式定义列值
**场景**：需要更灵活的列值定义
**示例**：
```java
.parens(SQLs.SPACE, row -&gt; row
    .column(1)
    .column("张三")
    .column(BigDecimal.valueOf(9999.88))
)
```

#### 列值添加方法

**space 方法（多种参数形式）**：
- `space(Object)` - 单列值
- `space(Object, Object)` - 双列值
- `space(Object, Object, Object)` - 三列值
- `space(Object, Object, Object, Object)` - 四列值
- `space(Object, Object, Object, Object, Object)` - 五列值
- `space(Object, Object, Object, Object, Object, Object)` - 六列值
- `space(Object, Object, Object, Object, Object, Object, Object)` - 七列值
- `space(Object, Object, Object, Object, Object, Object, Object, Object)` - 八列值

**comma 方法（多种参数形式）**：
- `comma(Object)` - 追加单列
- `comma(Object, Object)` - 追加双列
- `comma(Object, Object, Object)` - 追加三列
- `comma(Object, Object, Object, Object)` - 追加四列
- `comma(Object, Object, Object, Object, Object)` - 追加五列
- `comma(Object, Object, Object, Object, Object, Object)` - 追加六列
- `comma(Object, Object, Object, Object, Object, Object, Object)` - 追加七列
- `comma(Object, Object, Object, Object, Object, Object, Object, Object)` - 追加八列

#### 行分隔

**comma() 方法**：
```java
_StaticValuesRowClause&lt;I&gt; comma()
```
**用途**：分隔多个 VALUES 行
**可重复**：是（可重复使用添加多行）
**示例**：
```java
Postgres.subValues()
    .values()
    .parens(row -&gt; row.space(1, "张三"))
    .comma()  // 分隔行，可重复
    .parens(row -&gt; row.space(2, "李四"))
    .comma()  // 分隔行，可重复
    .parens(row -&gt; row.space(3, "王五"))
    .asValues();
```

### 3. ORDER BY 子句

**多种调用形式**：

**形式 1：静态单个排序项**
```java
_OrderByCommaSpec&lt;I&gt; orderBy(SortItem sortItem)
```
**示例**：
```java
.orderBy(SQLs.refSelection("column2"))
```

**形式 2：静态多个排序项**
```java
_OrderByCommaSpec&lt;I&gt; orderBy(SortItem sortItem1, SortItem sortItem2)
_OrderByCommaSpec&lt;I&gt; orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3)
_OrderByCommaSpec&lt;I&gt; orderBy(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3, SortItem sortItem4)
```
**示例**：
```java
.orderBy(
    SQLs.refSelection("column2"), 
    SQLs.refSelection(2)::desc
)
```

**形式 3：动态 Consumer 方式**
```java
_LimitSpec&lt;I&gt; orderBy(Consumer&lt;Consumer&lt;SortItem&gt;&gt; consumer)
```
**示例**：
```java
.orderBy(items -&gt; {
    items.accept(SQLs.refSelection("column2"));
    items.accept(SQLs.refSelection(3)::desc);
})
```

**形式 4：条件性排序**
```java
_LimitSpec&lt;I&gt; ifOrderBy(Consumer&lt;Consumer&lt;SortItem&gt;&gt; consumer)
```
**示例**：
```java
.ifOrderBy(items -&gt; {
    if (needSort) {
        items.accept(SQLs.refSelection("column2"));
    }
})
```

**形式 5：追加排序项**
```java
_OrderByCommaSpec&lt;I&gt; spaceComma(SortItem sortItem)
_OrderByCommaSpec&lt;I&gt; spaceComma(SortItem sortItem1, SortItem sortItem2)
_OrderByCommaSpec&lt;I&gt; spaceComma(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3)
_OrderByCommaSpec&lt;I&gt; spaceComma(SortItem sortItem1, SortItem sortItem2, SortItem sortItem3, SortItem sortItem4)
```
**可重复**：是
**示例**：
```java
.orderBy(SQLs.refSelection("column1"))
.spaceComma(SQLs.refSelection("column2")::desc)  // 可重复追加
```

### 4. LIMIT 子句

**多种调用形式**：

**形式 1：直接 Object 值**
```java
_OffsetSpec&lt;I&gt; limit(Object rowCount)
```
**示例**：
```java
.limit(10)
```

**形式 2：使用 BiFunction 运算符**
```java
_OffsetSpec&lt;I&gt; limit(BiFunction&lt;MappingType, Number, Expression&gt; operator, long rowCount)
```
**示例**：
```java
.limit(SQLs::literal, 10)
```

**形式 3：条件性限制**
```java
_OffsetSpec&lt;I&gt; ifLimit(@Nullable Object rowCount)
_OffsetSpec&lt;I&gt; ifLimit(BiFunction&lt;MappingType, Number, Expression&gt; operator, @Nullable Number rowCount)
```
**示例**：
```java
.ifLimit(needLimit ? 10 : null)
```

**形式 4：限制所有**
```java
_OffsetSpec&lt;I&gt; limitAll()
```
**示例**：
```java
.limitAll()
```

### 5. OFFSET 子句（PostgreSQL 特有）

**多种调用形式**：

**形式 1：Expression 方式**
```java
_FetchSpec&lt;I&gt; offset(Expression start, SQLs.FetchRow row)
```
**示例**：
```java
.offset(SQLs.literal(5), SQLs.ROWS)
```

**形式 2：BiFunction 运算符方式**
```java
_FetchSpec&lt;I&gt; offset(BiFunction&lt;MappingType, Number, Expression&gt; operator, long start, SQLs.FetchRow row)
```
**示例**：
```java
.offset(SQLs::literal, 5, SQLs.ROWS)
```

**形式 3：Supplier 方式**
```java
&lt;N extends Number&gt; _FetchSpec&lt;I&gt; offset(BiFunction&lt;MappingType, Number, Expression&gt; operator, Supplier&lt;N&gt; supplier, SQLs.FetchRow row)
```
**示例**：
```java
.offset(SQLs::literal, () -&gt; 5, SQLs.ROWS)
```

**形式 4：Function 方式**
```java
_FetchSpec&lt;I&gt; offset(BiFunction&lt;MappingType, Object, Expression&gt; operator, Function&lt;String, ?&gt; function, String keyName, SQLs.FetchRow row)
```
**示例**：
```java
.offset(SQLs::literal, params::get, "offset", SQLs.ROWS)
```

**形式 5：条件性偏移**
```java
_FetchSpec&lt;I&gt; ifOffset(BiFunction&lt;MappingType, Number, Expression&gt; operator, @Nullable Number start, SQLs.FetchRow row)
&lt;N extends Number&gt; _FetchSpec&lt;I&gt; ifOffset(BiFunction&lt;MappingType, Number, Expression&gt; operator, Supplier&lt;N&gt; supplier, SQLs.FetchRow row)
_FetchSpec&lt;I&gt; ifOffset(BiFunction&lt;MappingType, Object, Expression&gt; operator, Function&lt;String, ?&gt; function, String keyName, SQLs.FetchRow row)
```

### 6. FETCH 子句（PostgreSQL 特有）

**多种调用形式**：

**形式 1：Expression 方式**
```java
_AsValuesClause&lt;I&gt; fetch(SQLs.FetchFirstNext firstOrNext, Expression count, SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies)
```
**示例**：
```java
.fetch(SQLs.FIRST, SQLs.literal(10), SQLs.ROWS, SQLs.ONLY)
```

**形式 2：BiFunction 运算符方式**
```java
_AsValuesClause&lt;I&gt; fetch(SQLs.FetchFirstNext firstOrNext, BiFunction&lt;MappingType, Number, Expression&gt; operator, long count, SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies)
```
**示例**：
```java
.fetch(SQLs.FIRST, SQLs::literal, 10, SQLs.ROWS, SQLs.ONLY)
```

**形式 3：Supplier 方式**
```java
&lt;N extends Number&gt; _AsValuesClause&lt;I&gt; fetch(SQLs.FetchFirstNext firstOrNext, BiFunction&lt;MappingType, Number, Expression&gt; operator, Supplier&lt;N&gt; supplier, SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies)
```
**示例**：
```java
.fetch(SQLs.FIRST, SQLs::literal, () -&gt; 10, SQLs.ROWS, SQLs.ONLY)
```

**形式 4：Function 方式**
```java
_AsValuesClause&lt;I&gt; fetch(SQLs.FetchFirstNext firstOrNext, BiFunction&lt;MappingType, Object, Expression&gt; operator, Function&lt;String, ?&gt; function, String keyName, SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies)
```
**示例**：
```java
.fetch(SQLs.FIRST, SQLs::literal, params::get, "fetch", SQLs.ROWS, SQLs.ONLY)
```

**形式 5：条件性获取**
```java
_AsValuesClause&lt;I&gt; ifFetch(SQLs.FetchFirstNext firstOrNext, BiFunction&lt;MappingType, Number, Expression&gt; operator, @Nullable Number count, SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies)
&lt;N extends Number&gt; _AsValuesClause&lt;I&gt; ifFetch(SQLs.FetchFirstNext firstOrNext, BiFunction&lt;MappingType, Number, Expression&gt; operator, Supplier&lt;N&gt; supplier, SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies)
_AsValuesClause&lt;I&gt; ifFetch(SQLs.FetchFirstNext firstOrNext, BiFunction&lt;MappingType, Object, Expression&gt; operator, Function&lt;String, ?&gt; function, String keyName, SQLs.FetchRow row, SQLs.FetchOnlyWithTies onlyWithTies)
```

### 7. UNION 相关操作

**UNION 操作**：
```java
_QueryComplexSpec&lt;I&gt; union()
_QueryComplexSpec&lt;I&gt; unionAll()
_QueryComplexSpec&lt;I&gt; unionDistinct()
```
**示例**：
```java
.unionAll()
.values()
.parens(row -&gt; row.space(4, "赵六"))
.asValues()
```

**EXCEPT 操作**：
```java
_QueryComplexSpec&lt;I&gt; except()
_QueryComplexSpec&lt;I&gt; exceptAll()
_QueryComplexSpec&lt;I&gt; exceptDistinct()
```

**INTERSECT 操作**：
```java
_QueryComplexSpec&lt;I&gt; intersect()
_QueryComplexSpec&lt;I&gt; intersectAll()
_QueryComplexSpec&lt;I&gt; intersectDistinct()
```

### 8. 结束语句

**asValues() 方法**：
```java
I asValues()
```
**用途**：结束 subValues 语句构建，返回 SubValues 对象
**返回类型**：`SubValues`
**可重复**：否（只能调用一次）
**示例**：
```java
.asValues()
```

## 完整示例

### 示例 1：简单的 subValues

```java
SubValues subValues = Postgres.subValues()
    .values()
    .parens(row -&gt; row.space(1, "张三", BigDecimal.valueOf(9999.88), LocalDate.now()))
    .comma()
    .parens(row -&gt; row.space(2, "李四", BigDecimal.valueOf(8888.66), LocalDate.now().plusDays(1)))
    .asValues();
```

### 示例 2：带排序和分页的 subValues

```java
SubValues subValues = Postgres.subValues()
    .values()
    .parens(row -&gt; row.space(1, "张三", BigDecimal.valueOf(9999.88), LocalDate.now()))
    .comma()
    .parens(row -&gt; row.space(2, "李四", BigDecimal.valueOf(8888.66), LocalDate.now().plusDays(1)))
    .comma()
    .parens(row -&gt; row.space(3, "王五", BigDecimal.valueOf(7777.44), LocalDate.now().minusDays(1)))
    .orderBy(SQLs.refSelection("column2"), SQLs.refSelection(3)::desc)
    .offset(SQLs::literal, 1, SQLs.ROWS)
    .fetch(SQLs.FIRST, SQLs::literal, 2, SQLs.ROWS, SQLs.ONLY)
    .asValues();
```

### 示例 3：在查询中使用 subValues

```java
Select query = Postgres.query()
    .select(s -&gt; s.space("t", PERIOD, ASTERISK))
    .from(
        Postgres.subValues()
            .values()
            .parens(row -&gt; row.space(1, "张三"))
            .comma()
            .parens(row -&gt; row.space(2, "李四"))
            .asValues()
    )
    .as("t")
    .asQuery();
```

### 示例 4：使用动态 Consumer 方式

```java
SubValues subValues = Postgres.subValues()
    .values(v -&gt; {
        v.parens(row -&gt; row.space(1, "张三", BigDecimal.valueOf(9999.88)));
        v.parens(row -&gt; row.space(2, "李四", BigDecimal.valueOf(8888.66)));
        v.parens(row -&gt; row.space(3, "王五", BigDecimal.valueOf(7777.44)));
    })
    .orderBy(SQLs.refSelection("column2"))
    .limit(SQLs::literal, 10)
    .asValues();
```

### 示例 5：UNION 操作

```java
SubValues subValues = Postgres.subValues()
    .values()
    .parens(row -&gt; row.space(1, "张三"))
    .comma()
    .parens(row -&gt; row.space(2, "李四"))
    .unionAll()
    .values()
    .parens(row -&gt; row.space(3, "王五"))
    .comma()
    .parens(row -&gt; row.space(4, "赵六"))
    .asValues();
```

## 可重复性总结

| 方法 | 可重复 | 说明 |
|------|--------|------|
| comma()（行分隔） | ✅ 是 | 可重复添加多行数据 |
| spaceComma()（排序项） | ✅ 是 | 可重复追加排序条件 |
| 起始 methods（values/parens） | ❌ 否 | 只能选择一种起始方式 |
| asValues() | ❌ 否 | 只能调用一次结束语句 |
| ORDER BY/LIMIT/OFFSET/FETCH | ❌ 否 | 每个子句通常只调用一次 |

## 常见参数说明

### SQLs.FetchFirstNext
- `SQLs.FIRST` - 取前 N 行
- `SQLs.NEXT` - 取下 N 行

### SQLs.FetchRow
- `SQLs.ROW` - 单行单位
- `SQLs.ROWS` - 多行单位

### SQLs.FetchOnlyWithTies
- `SQLs.ONLY` - 只取精确匹配的行
- `SQLs.WITH_TIES` - 包含与最后一行排序值相同的行

### 列引用方式
- `SQLs.refSelection("column2")` - 通过列名引用
- `SQLs.refSelection(2)` - 通过列索引引用（从1开始）
- 可链式调用 `::desc` 进行降序排序
