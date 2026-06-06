
---
name: "mysql-subValues-method-chain"
description: "Complete documentation and reference for MySQLs.subValues() method chain. Invoke when user needs to use or understand MySQL subValues statement construction."
---

# MySQLs.subValues() 方法链完整参考文档

## 概述

`MySQLs.subValues()` 用于创建 MySQL VALUES 子查询语句，可作为 SELECT 语句的数据源。该方法返回 `MySQLValues.ValuesSpec&lt;SubValues&gt;`，支持丰富的方法链操作。

参考：[MySQL VALUES Statement](https://dev.mysql.com/doc/refman/8.0/en/values.html)

---

## 完整方法链图

```
MySQLs.subValues()
  │
  ├─ values()
  │   │
  │   └─ row(Consumer&lt;ValueStaticColumnSpaceClause&gt;)
  │       │
  │       ├─ space(Object)
  │       ├─ space(Object, Object)
  │       ├─ space(Object, Object, Object)
  │       ├─ space(Object, Object, Object, Object)
  │       ├─ space(Object, Object, Object, Object, Object)
  │       ├─ space(Object, Object, Object, Object, Object, Object)
  │       ├─ space(Object, Object, Object, Object, Object, Object, Object)
  │       ├─ space(Object, Object, Object, Object, Object, Object, Object, Object)
  │       │
  │       ├─ comma(Object)
  │       ├─ comma(Object, Object)
  │       ├─ comma(Object, Object, Object)
  │       ├─ comma(Object, Object, Object, Object)
  │       ├─ comma(Object, Object, Object, Object, Object)
  │       ├─ comma(Object, Object, Object, Object, Object, Object)
  │       ├─ comma(Object, Object, Object, Object, Object, Object, Object)
  │       ├─ comma(Object, Object, Object, Object, Object, Object, Object, Object)
  │       │
  │       └─ [结束行] → _StaticValuesRowCommaSpec&lt;SubValues&gt;
  │           │
  │           ├─ comma() [可重复]
  │           │   └─ row(...) [可重复]
  │           │
  │           ├─ orderBy(Expression...) [可重复]
  │           │   └─ orderBy(Expression, Expression...) [可重复]
  │           │       └─ comma(Selection) [可重复]
  │           │           └─ comma(Selection) [可重复]
  │           │               └─ limit(Function&lt;SQLs, Expression&gt;)
  │           │                   └─ asValues() → SubValues
  │           │
  │           ├─ limit(Function&lt;SQLs, Expression&gt;)
  │           │   └─ asValues() → SubValues
  │           │
  │           ├─ union()
  │           ├─ unionAll()
  │           ├─ unionDistinct()
  │           ├─ intersect()
  │           ├─ intersectAll()
  │           ├─ intersectDistinct()
  │           ├─ except()
  │           ├─ exceptAll()
  │           ├─ exceptDistinct()
  │           └─ asValues() → SubValues
  │
  ├─ values(Consumer&lt;ValuesRows&gt;)
  │   │
  │   └─ orderBy(Expression...) [可重复]
  │       └─ orderBy(Expression, Expression...) [可重复]
  │           └─ comma(Selection) [可重复]
  │               └─ comma(Selection) [可重复]
  │                   └─ limit(Function&lt;SQLs, Expression&gt;)
  │                       └─ asValues() → SubValues
  │
  └─ parens(Function&lt;ValuesSpec&lt;_UnionOrderBySpec&lt;SubValues&gt;&gt;, _UnionOrderBySpec&lt;SubValues&gt;&gt;)
      │
      └─ [括号内完整 values 语句]
          │
          ├─ orderBy(Expression...) [可重复]
          │   └─ orderBy(Expression, Expression...) [可重复]
          │       └─ comma(Selection) [可重复]
          │           └─ comma(Selection) [可重复]
          │               └─ limit(Function&lt;SQLs, Expression&gt;)
          │                   └─ asValues() → SubValues
          │
          ├─ limit(Function&lt;SQLs, Expression&gt;)
          │   └─ asValues() → SubValues
          │
          ├─ union()
          ├─ unionAll()
          ├─ unionDistinct()
          ├─ intersect()
          ├─ intersectAll()
          ├─ intersectDistinct()
          ├─ except()
          ├─ exceptAll()
          ├─ exceptDistinct()
          └─ asValues() → SubValues

---

## 方法详细说明

### 1. 入口方法

```java
MySQLs.subValues()
```
- **返回类型**: `MySQLValues.ValuesSpec&lt;SubValues&gt;`
- **说明**: 创建 MySQL VALUES 子查询语句构建器

---

### 2. VALUES 子句 - 两种形式

#### 形式一：静态 values() - 推荐用于静态数据

```java
values()
```
- **返回类型**: `_StaticValuesRowClause&lt;SubValues&gt;`
- **使用场景**: 需要逐行构建数据时使用，提供类型安全的 row()、space()、comma() 方法
- **特点**: 更安全，支持方法链式调用，IDE 有更好的提示

#### 形式二：动态 values(Consumer)

```java
values(Consumer&lt;ValuesRows&gt;)
```
- **参数**: `consumer` - 接收 ValuesRows 进行动态数据构建
- **返回类型**: `_OrderBySpec&lt;SubValues&gt;`
- **使用场景**: 在 Consumer 内部调用 valuesRows.row() 构建多行数据
- **特点**: 更灵活，适合动态生成多行数据

---

### 3. row() 方法 - 两种形式

#### 形式一：静态列（推荐）

```java
row(Consumer&lt;_ValueStaticColumnSpaceClause&gt;)
```
- **参数**: `consumer` - 接收静态列构建器
- **使用场景**: 静态数据行，使用 space()、comma() 添加列
- **示例**:
  ```java
  .row(r -&gt; r.space(1, "Alice")
              .comma("New York", true))
  ```

#### 形式二：动态列

```java
row(SQLs.SymbolSpace, Consumer&lt;_ValuesDynamicColumnClause&gt;)
```
- **参数 1**: `space` - 符号空格（通常为 SQLs.SPACE）
- **参数 2**: `consumer` - 接收动态列构建器
- **使用场景**: 动态数据行，使用 column() 方法添加列
- **示例**:
  ```java
  .row(SQLs.SPACE, r -&gt; r.column(1).column("Alice"))
  ```

---

### 4. space() 和 comma() 方法 - 添加列

#### space() 方法族（起始列）

```java
space(Object)
space(Object, Object)
space(Object, Object, Object)
space(Object, Object, Object, Object)
space(Object, Object, Object, Object, Object)
space(Object, Object, Object, Object, Object, Object)
space(Object, Object, Object, Object, Object, Object, Object)
space(Object, Object, Object, Object, Object, Object, Object, Object)
```
- **说明**: 添加 1-8 个列值，用空格分隔
- **使用场景**: 一行的起始列
- **参数**: 可以是字面量、表达式或 null（会被转换为 SQL NULL）

#### comma() 方法族（后续列）

```java
comma(Object)
comma(Object, Object)
comma(Object, Object, Object)
comma(Object, Object, Object, Object)
comma(Object, Object, Object, Object, Object)
comma(Object, Object, Object, Object, Object, Object)
comma(Object, Object, Object, Object, Object, Object, Object)
comma(Object, Object, Object, Object, Object, Object, Object, Object)
```
- **说明**: 添加 1-8 个列值，用逗号分隔
- **使用场景**: 一行的后续列（在 space() 之后）
- **参数**: 可以是字面量、表达式或 null

---

### 5. comma() 方法 - 添加新行

```java
comma()
```
- **返回类型**: `_StaticValuesRowClause&lt;SubValues&gt;`
- **使用场景**: 在一行结束后，准备添加下一行
- **特点**: **可重复调用**，用于添加多行数据

---

### 6. parens() 方法 - 括号包裹

```java
parens(Function&lt;ValuesSpec&lt;_UnionOrderBySpec&lt;SubValues&gt;&gt;, _UnionOrderBySpec&lt;SubValues&gt;&gt;)
```
- **参数**: `function` - 接收 ValuesSpec，返回处理结果
- **返回类型**: `_UnionOrderBySpec&lt;SubValues&gt;`
- **使用场景**: 需要将整个 VALUES 语句用括号包裹，通常用于复杂的 UNION 操作
- **特点**: 在括号内部可以使用完整的 values 语句

---

### 7. UNION 家族方法 - 集合操作

```java
union()           // 并集（去重）
unionAll()        // 并集（保留重复）
unionDistinct()   // 并集（显式去重）
intersect()       // 交集
intersectAll()    // 交集（保留重复）
intersectDistinct() // 交集（显式去重）
except()          // 差集
exceptAll()       // 差集（保留重复）
exceptDistinct()  // 差集（显式去重）
```
- **返回类型**: `_ValuesQueryComplexSpec&lt;SubValues&gt;`
- **使用场景**: 合并多个 VALUES 语句的结果
- **特点**: 调用后可以继续链式调用 values()、select() 等
- **后续可用**:
  - `values()` / `values(Consumer)`
  - `select(...)` - 切换到 SELECT 语句
  - `parens(...)`

---

### 8. orderBy() 方法 - 排序

```java
orderBy(Expression...)
orderBy(Expression, Expression...)
```
- **返回类型**: `_OrderByCommaSpec&lt;SubValues&gt;`
- **使用场景**: 对结果集排序
- **特点**: **可重复调用**，每个 orderBy 可添加多个排序表达式
- **示例**:
  ```java
  .orderBy(SQLs.refSelection("column_1").desc())
  .orderBy(SQLs.refSelection("column_2"))
  ```

#### orderBy comma() 方法

```java
comma(Selection)
```
- **返回类型**: `_OrderByCommaSpec&lt;SubValues&gt;`
- **使用场景**: 在同一 orderBy 中添加多个排序项
- **特点**: **可重复调用**

---

### 9. limit() 方法 - 限制行数

```java
limit(Function&lt;SQLs, Expression&gt;)
```
- **参数**: `function` - 接收 SQLs 构建 limit 表达式
- **返回类型**: `_AsValuesClause&lt;SubValues&gt;`
- **使用场景**: 限制结果行数
- **示例**:
  ```java
  .limit(SQLs::literal, 10)
  ```

---

### 10. asValues() 方法 - 完成构建

```java
asValues()
```
- **返回类型**: `SubValues`
- **使用场景**: 完成 VALUES 语句构建，返回 SubValues 实例
- **说明**: 这是方法链的终点，调用后不能再链式调用

---

## 完整使用示例

### 示例 1：简单的 subValues 查询

```java
final Select stmt;
stmt = MySQLs.query()
    .select(s -&gt; s.space("v", PERIOD, ASTERISK))
    .from(MySQLs.subValues()
        .values()
        .row(r -&gt; r.space(1, "海问香", Decimals.valueOf("9999.88"), now)
                   .comma(DayOfWeek.MONDAY, TRUE, SQLs.literalValue(1).plus(SQLs::literal, 3)))
        .comma()
        .row(r -&gt; r.space(2, "大仓", Decimals.valueOf("9999.66"), now.plusDays(1))
                   .comma(DayOfWeek.SUNDAY, TRUE, SQLs.literalValue(13).minus(SQLs::literal, 3)))
        .orderBy(SQLs.refSelection("column_1"))
        .limit(SQLs::literal, 4)
        .asValues()
    ).as("v")
    .asQuery();
```

### 示例 2：使用 values(Consumer) 动态构建

```java
final Select stmt;
stmt = MySQLs.query()
    .select(s -&gt; s.space("v", PERIOD, ASTERISK))
    .from(MySQLs.subValues()
        .values(rows -&gt; {
            rows.row(r -&gt; r.space(1, "A"));
            rows.row(r -&gt; r.space(2, "B"));
        })
        .orderBy(SQLs.refSelection("column_1"))
        .asValues()
    ).as("v")
    .asQuery();
```

### 示例 3：使用 parens() 和 UNION ALL

```java
final Select stmt;
stmt = MySQLs.query()
    .select(s -&gt; s.space("v", PERIOD, ASTERISK))
    .from(MySQLs.subValues()
        .parens(v -&gt; v.values()
            .row(r -&gt; r.space(1, "海问香"))
            .comma()
            .row(r -&gt; r.space(2, "大仓"))
            .orderBy(SQLs.refSelection("column_1"))
            .asValues()
        )
        .unionAll()
        .values()
        .row(r -&gt; r.space(3, "卡拉肖克·玲"))
        .orderBy(SQLs.refSelection("column_1"))
        .asValues()
    ).as("v")
    .asQuery();
```

---

## 方法可重复性总结

| 方法 | 是否可重复 | 说明 |
|------|-----------|------|
| `values()` | ❌ 否 | 只能调用一次 |
| `values(Consumer)` | ❌ 否 | 只能调用一次 |
| `row(...)` | ✅ 是 | 每次 comma() 后可再次调用 |
| `comma()` (换行) | ✅ 是 | 多次添加行 |
| `space(...)` | ❌ 否 | 每行开始调用一次 |
| `comma(Object...)` (列) | ✅ 是 | 同一行内添加多列 |
| `parens(...)` | ❌ 否 | 通常只在最外层使用 |
| `union*()` / `intersect*()` / `except*()` | ✅ 是 | 多次集合操作 |
| `orderBy(...)` | ✅ 是 | 多次添加排序条件 |
| `comma(Selection)` (排序) | ✅ 是 | 同一 orderBy 内添加多个 |
| `limit(...)` | ❌ 否 | 只能调用一次 |
| `asValues()` | ❌ 否 | 终点方法 |

---

## 列名引用

VALUES 语句的列自动命名为 `column_0`, `column_1`, `column_2`, ...，可以通过以下方式引用：

```java
SQLs.refSelection("column_1")  // 按名称引用
SQLs.refSelection(1)           // 按索引引用（从0开始）
```

---

## 注意事项

1. **NULL 值**: 传递 `null` 会自动转换为 SQL NULL
2. **列数一致**: 所有行必须有相同数量的列
3. **不支持 DEFAULT**: VALUES 语句不支持 DEFAULT 关键字
4. **asValues() 必须调用**: 构建完成后必须调用 asValues()
5. **SubValues 用法**: 生成的 SubValues 通常用于 SELECT 语句的 FROM 子句，需要调用 `.as("alias")` 指定别名

---

## 相关接口

- `MySQLValues.ValuesSpec&lt;I&gt;` - 主接口
- `MySQLValues._StaticValuesRowClause&lt;I&gt;` - 静态行接口
- `MySQLValues._OrderBySpec&lt;I&gt;` - 排序接口
- `MySQLValues._ValuesQueryComplexSpec&lt;I&gt;` - 集合操作接口
- `SubValues` - 最终结果类型

---

## 与 MySQLs.valuesStmt() 的区别

| 特性 | MySQLs.subValues() | MySQLs.valuesStmt() |
|------|-------------------|---------------------|
| 返回类型 | `SubValues` | `Values` |
| 用途 | 作为子查询用于其他语句 | 独立的 VALUES 语句 |
| 使用场景 | SELECT FROM 子句、CTE 等 | 直接执行查询 |

