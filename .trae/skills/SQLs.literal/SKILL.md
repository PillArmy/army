
---
name: "SQLs.literal"
description: "完整学习和使用 SQLs 类的字面量表达式方法，包括 literalValue、literal、constValue、namedLiteral、rowLiteral 等。Invoke when user needs to work with literal expressions in Army Criteria API."
---

# SQLs.literal 完整指南

## 概述

本指南详细介绍 Army Criteria API 中 `SQLs` 类提供的所有字面量表达式相关方法，包含声明、实现、使用场景和示例代码。

## 核心类

- `SQLs` (io.army.criteria.impl.SQLs)
- `SQLSyntax` (io.army.criteria.impl.SQLSyntax) - SQLs 的父类
- `ArmyLiteralExpression` - 单个字面量表达式实现
- `ArmyRowLiteralExpression` - 多行字面量表达式实现

## 字面量类型方法汇总

| 方法 | 说明 | 返回类型 |
|------|------|----------|
| `literalValue(Object value)` | 创建自动推断类型的字面量 | `LiteralExpression` |
| `constValue(Object nonNullValue)` | 创建非空自动推断类型字面量 | `LiteralExpression` |
| `literal(TypeInfer type, Object value)` | 指定类型创建字面量 | `LiteralExpression` |
| `namedLiteral(TypeInfer type, String name)` | 创建命名字面量 | `LiteralExpression` |
| `rowLiteral(TypeInfer type, Collection&lt;?&gt; values)` | 创建多行字面量 | `RowLiteralExpression` |
| `namedRowLiteral(TypeInfer type, String name)` | 创建命名的多行字面量 | `RowLiteralExpression` |
| `constant(TypeInfer type, Object value)` | 创建常量字面量 | `LiteralExpression` |
| `namedConst(TypeInfer type, String name)` | 创建命名常量字面量 | `LiteralExpression` |
| `rowConst(TypeInfer type, Collection&lt;?&gt; values)` | 创建多行常量字面量 | `RowLiteralExpression` |
| `namedRowConst(TypeInfer type, String name)` | 创建命名的多行常量字面量 | `RowLiteralExpression` |

---

## 1. literalValue(Object value) 方法

### 声明
```java
public static LiteralExpression literalValue(Object value)
```

### 实现位置
`io.army.criteria.impl.SQLSyntax.literalValue()` (第 203 行)

### 功能
创建一个字面量表达式，根据传入的 value 自动推断类型。

### 参数
- `value`: 非空值，类型必须是：
  - Boolean, String, Integer, Long, Short, Byte
  - Double, Float, java.math.BigDecimal, java.math.BigInteger
  - byte[], BitSet
  - CodeEnum, TextEnum
  - java.time.LocalTime, LocalDate, LocalDateTime
  - java.time.OffsetDateTime, ZonedDateTime
  - java.time.OffsetTime, java.time.ZoneId
  - java.time.Month, DayOfWeek, Year, YearMonth, MonthDay

### 返回值
`LiteralExpression` 实例

### 使用场景
当你有一个具体的值，希望直接作为 SQL 字面量使用时。

### 示例代码
```java
// 数字字面量
SQLs.literalValue(100)

// 字符串字面量
SQLs.literalValue("hello")

// 布尔字面量
SQLs.literalValue(true)

// 枚举字面量
SQLs.literalValue(DayOfWeek.MONDAY)
```

### 实际项目示例
```java
// 来自 army-example/src/test/java/io/army/session/sync/postgre/ValuesTests.java
.where(SQLs.refField("cte", ChinaRegion_.ID).equal(SQLs.literalValue(firstId)))

// 来自 army-example/src/test/java/io/army/session/sync/postgre/ValuesTests.java
.comma(DayOfWeek.MONDAY, TRUE, SQLs.literalValue(1).plus(SQLs::literal, 3))
```

---

## 2. constValue(Object nonNullValue) 方法

### 声明
```java
public static LiteralExpression constValue(Object nonNullValue)
```

### 实现位置
`io.army.criteria.impl.SQLSyntax.constValue()` (第 215 行)

### 功能
类似于 `literalValue`，但要求值非空，生成常量字面量。

### 参数
- `nonNullValue`: 非空值，类型同 `literalValue`

### 使用场景
当你确定值不会为 null 时使用。

---

## 3. literal(TypeInfer type, Object value) 方法

### 声明
```java
public static LiteralExpression literal(TypeInfer type, Object value)
```

### 实现位置
`io.army.criteria.impl.SQLSyntax.literal()` (第 227 行)

### 功能
使用指定的类型推断信息创建字面量表达式。

### 参数
- `type`: 类型推断信息，可以是：
  - `MappingType` - 映射类型
  - `FieldMeta` - 字段元数据
  - 其他 `TypeInfer` 实现
- `value`: 值，可以为 null，如果是 Supplier 则会调用 get() 方法

### 异常
- `CriteriaException`: 当 infer 是编码类型的 TableField 时抛出

### 使用场景
当需要显式指定类型时，例如处理复杂的类型映射。

### 示例代码
```java
// 使用字段作为类型推断
SQLs.literal(ChinaRegion_.ID, 100L)

// 使用 MappingType
SQLs.literal(IntegerType.INSTANCE, 100)

// 作为方法引用在表达式链中使用
.where(ChinaRegion_.id.equal(SQLs::literal, 100L))
```

### 实际项目示例
```java
// 来自 army-example/src/test/java/io/army/session/sync/standard/QueryTests.java
.where(ChinaRegion_.id.equal(SQLs::literal, firstId))

// 来自 army-example/src/test/java/io/army/session/sync/postgre/ValuesTests.java
.limit(SQLs::literal, 4)
```

---

## 4. namedLiteral(TypeInfer type, String name) 方法

### 声明
```java
public static LiteralExpression namedLiteral(TypeInfer type, String name)
```

### 实现位置
`io.army.criteria.impl.SQLSyntax.namedLiteral()` (第 244 行)

### 功能
创建命名的非空字面量表达式，仅用于 VALUES 插入语句。

### 参数
- `type`: 类型推断信息
- `name`: 非空且有文本内容的名称

### 异常
- `CriteriaException`: 当 infer 是编码类型的 TableField 或 name 没有文本时抛出

### 限制
- 不能用于批量更新或删除语句
- 只能用于 VALUES 插入语句

### 使用场景
在批量插入语句中使用命名字面量。

### 预定义常量
```java
SQLs.BATCH_NO_LITERAL  // 预定义的批处理编号字面量
```

---

## 5. rowLiteral(TypeInfer type, Collection&lt;?&gt; values) 方法

### 声明
```java
public static RowLiteralExpression rowLiteral(TypeInfer type, Collection&lt;?&gt; values)
```

### 实现位置
`io.army.criteria.impl.SQLSyntax.rowLiteral()` (第 256 行)

### 功能
创建多行字面量表达式，会输出多个用逗号分隔的字面量，在 IN/NOT IN 操作符右侧会自动加括号。

### 参数
- `type`: 元素的类型推断信息
- `values`: 非空且非空的集合

### 异常
- `CriteriaException`: 当 values 为空或 infer 是编码类型的 TableField 时抛出

### 使用场景
用于 IN 条件查询中的多个值。

### 示例代码
```java
// 创建多行字面量
List&lt;Long&gt; idList = Arrays.asList(1L, 2L, 3L);
SQLs.rowLiteral(ChinaRegion_.ID, idList)

// 在 IN 条件中使用（方法引用方式）
.where(ChinaRegion_.id.in(SQLs::rowLiteral, idList))
```

### 实际项目示例
```java
// 来自 army-example/src/test/java/io/army/session/sync/postgre/ValuesTests.java
.where(ChinaRegion_.id.in(SQLs::rowLiteral, extractRegionIdList(regionList)))

// 来自 army-example/src/test/java/io/army/session/sync/postgre/QuerySuiteTests.java
.where(ChinaRegion_.id.in(SQLs::rowLiteral, extractRegionIdList(regionList)))
```

---

## 6. namedRowLiteral(TypeInfer type, String name) 方法

### 声明
```java
public static RowLiteralExpression namedRowLiteral(TypeInfer type, String name)
```

### 实现位置
`io.army.criteria.impl.SQLSyntax.namedRowLiteral()` (第 272 行)

### 功能
创建命名的非空多行字面量表达式，仅用于 VALUES 插入语句。

### 参数
- `type`: 元素的类型推断信息
- `name`: 非空且有文本内容的名称

### 限制
- 不能用于批量更新或删除语句
- 只能用于 VALUES 插入语句

---

## 7. constant/namedConst/rowConst/namedRowConst 系列方法

这些方法与上面的 literal 系列方法类似，区别是它们生成的是常量字面量（typeName=false），不会输出类型前缀。

### 方法列表
```java
constant(TypeInfer type, Object value)
namedConst(TypeInfer type, String name)
rowConst(TypeInfer type, Collection<?> values)
namedRowConst(TypeInfer type, String name)
```

---

## 8. literal 与 const 的核心区别

### SQL 输出差异

| 值类型 | `literal`（带类型前缀） | `const`（不带类型前缀） |
|--------|------------------------|------------------------|
| `Integer(100)` | PostgreSQL: `100::INTEGER`<br>MySQL: `100` | `100`（所有数据库） |
| `BigDecimal("0.00")` | PostgreSQL: `0.00::DECIMAL`<br>MySQL: `0.00` | `0.00`（所有数据库） |
| `LocalDate` | `DATE '2024-01-01'` | `'2024-01-01'` |
| `LocalDateTime` | `TIMESTAMP '2024-01-01 12:00:00'` | `'2024-01-01T12:00:00'` |
| `String` | PostgreSQL: `'hello'::VARCHAR`<br>MySQL: `'hello'` | `'hello'`（所有数据库） |

### 实现机制

`literal` 和 `const` 的核心差异在于 `typeName` 参数：

- **literal 系列**：调用 `ArmyLiteralExpression.from(value, true)`，`true` 表示输出类型名称
- **const 系列**：调用 `ArmyLiteralExpression.from(value, false)`，`false` 表示不输出类型名称

### 安全性说明

**重要**：`literal` 和 `const` 都经过相同的安全机制处理，并非简单的字符串拼接：

1. **类型映射保护**：所有值都经过 `MappingType` 检查，数值类型继承自 `_ArmyNoInjectionType`，从类型层面杜绝 SQL 注入
2. **安全字面量方法**：所有字面量最终通过 `ArmyParser.safeLiteral()` 输出，字符串会正确转义
3. **AST 生成路径**：值通过 AST → SQL 的路径生成，而非字符串模板拼接

### 使用场景选择

| 场景 | 推荐使用 | 原因 |
|------|----------|------|
| 需要确保数据库正确解析类型 | `literal` | 类型前缀保证类型明确，避免隐式转换问题 |
| 追求跨数据库一致性 | `const` | 输出格式一致，不受方言影响 |
| 日期/时间类型 | `literal` | `DATE`/`TIMESTAMP` 前缀确保正确解析 |
| 数值比较 | `const` | 数值类型在所有数据库中行为一致 |
| 系统常量、配置值 | `const` | 简洁且跨数据库一致 |

---

## 预定义的字面量常量

SQLs 类预定义了一些常用的字面量常量供直接使用：

### 数字字面量常量
```java
SQLs.LITERAL_MINUS_1  // -1
SQLs.LITERAL_0        // 0
SQLs.LITERAL_1        // 1
SQLs.LITERAL_2        // 2
SQLs.LITERAL_3        // 3
SQLs.LITERAL_4        // 4
SQLs.LITERAL_5        // 5
SQLs.LITERAL_6        // 6
SQLs.LITERAL_7        // 7
SQLs.LITERAL_8        // 8
SQLs.LITERAL_9        // 9
SQLs.LITERAL_10       // 10
SQLs.LITERAL_100      // 100
SQLs.LITERAL_1000     // 1000
SQLs.LITERAL_POINT_5  // 0.5
SQLs.LITERAL_DECIMAL_0
```

### 字符串字面量常量
```java
SQLs.LITERAL_EMPTY_STRING  // ""
SQLs.LITERAL_SPACE         // " "
```

### 常量版本（CONST_ 前缀）
所有上述字面量都有对应的常量版本，例如：
```java
SQLs.CONST_0
SQLs.CONST_1
SQLs.CONST_EMPTY_STRING
// ... 等等
```

---

## 内部实现类

### ArmyLiteralExpression
单个字面量表达式的实现类，包含：
- `AnonymousLiteral` - 匿名字面量
- `ArmyNamedLiteral` - 命名字面量

### ArmyRowLiteralExpression
多行字面量表达式的实现类，包含：
- `AnonymousRowLiteral` - 匿名多行字面量
- `ArmyNamedRowLiteral` - 命名多行字面量

---

## 完整使用示例

### 示例 1：简单查询中的字面量
```java
final Select stmt = SQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T)
    .where(ChinaRegion_.id.equal(SQLs.literalValue(100L)))
    .asQuery();
```

### 示例 2：使用方法引用的字面量
```java
final Select stmt = SQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T)
    .where(ChinaRegion_.id.equal(SQLs::literal, 100L))
    .limit(SQLs::literal, 10)
    .asQuery();
```

### 示例 3：IN 条件中的多行字面量
```java
List&lt;Long&gt; idList = Arrays.asList(1L, 2L, 3L, 4L, 5L);

final Select stmt = SQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T)
    .where(ChinaRegion_.id.in(SQLs::rowLiteral, idList))
    .asQuery();
```

### 示例 4：字面量表达式运算
```java
// 来自 ValuesTests.java
.comma(DayOfWeek.MONDAY, TRUE, SQLs.literalValue(1).plus(SQLs::literal, 3))
.comma(DayOfWeek.SUNDAY, TRUE, SQLs.literalValue(13).minus(SQLs::literal, 3))
.comma(DayOfWeek.FRIDAY, TRUE, SQLs.literalValue(3).minus(SQLs::literal, 3))
.comma(DayOfWeek.TUESDAY, FALSE, SQLs.literalValue(81).divide(SQLs::literal, 3))
```

### 示例 5：使用预定义常量
```java
final Select stmt = SQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T)
    .where(ChinaRegion_.population.greater(SQLs.LITERAL_1000))
    .asQuery();
```

---

## 实现细节

### 类型推断流程
1. 检查 `TypeInfer` 类型
2. 如果是 `QualifiedField`，提取其 `FieldMeta`
3. 否则使用原始的 `TypeMeta`
4. 根据 `typeName` 参数决定是否输出类型名称

### 值优化
对于常见的小整数值（-1 到 10、100、1000），会直接返回预定义的常量实例以提高性能。

### SQL 渲染
- 单个字面量：直接渲染为对应类型的 SQL 字面量
- 多行字面量：渲染为 `(val1, val2, val3, ...)` 格式
- 编码字段会有特殊处理

---

## 与其他方法配合使用

### 与 TypedField 配合
```java
// 比较运算
ChinaRegion_.id.equal(SQLs.literalValue(100L))
ChinaRegion_.population.greater(SQLs.LITERAL_1000)

// 赋值（在 UPDATE 语句中）
.plusEqual(ChinaRegion_.population, SQLs.literalValue(100))
```

### 与 Expression 接口方法配合
```java
SQLs.literalValue(100).plus(SQLs.literalValue(200))
SQLs.literalValue("hello").concat(SQLs.literalValue(" world"))
```

### 与 IN/NOT IN 操作配合
```java
.where(ChinaRegion_.id.in(SQLs::rowLiteral, idList))
.where(ChinaRegion_.id.notIn(SQLs::rowLiteral, excludeList))
```

---

## 最佳实践

1. **优先使用方法引用**：在条件构建器中优先使用 `SQLs::literal` 这样的方法引用方式，代码更简洁。
2. **使用预定义常量**：对于常见的数值（如 0、1、100），使用 SQLs 类预定义的常量。
3. **明确类型**：当类型推断可能出错时，显式指定类型。
4. **多行值使用 rowLiteral**：对于 IN 条件中的多个值，使用 `rowLiteral` 而不是多个 OR 条件。
5. **⭐ 作为右操作数时可省略**：当值作为 Expression 方法（如 `equal()`、`space()`、`plus()`、`like()` 等）的右操作数时，可直接传原始
   Java 值，无需 `literal()` 包装。框架通过 `Expressions.wrapRight()` 自动从左侧字段推断类型并包装。详见
   `expression-interface-usage` skill 的"隐式值包装"章节。

```java
// ❌ 冗余
field.space(Postgres.DARROW, SQLs.literal(TextType.INSTANCE, "value"))
// ✅ 简洁
field.space(Postgres.DARROW, "value")
```

---

## 源文件位置

- `SQLSyntax.java`: `/Users/zoro/repositories/trae/java/hub/army/army-core/src/main/java/io/army/criteria/impl/SQLSyntax.java`
- `SQLs.java`: `/Users/zoro/repositories/trae/java/hub/army/army-core/src/main/java/io/army/criteria/impl/SQLs.java`
- `ArmyLiteralExpression.java`: `/Users/zoro/repositories/trae/java/hub/army/army-core/src/main/java/io/army/criteria/impl/ArmyLiteralExpression.java`
- `ArmyRowLiteralExpression.java`: `/Users/zoro/repositories/trae/java/hub/army/army-core/src/main/java/io/army/criteria/impl/ArmyRowLiteralExpression.java`

---

## 自我进化说明

本技能会持续更新，当发现以下情况时会自动进化：
1. 新增的字面量相关方法
2. 更优的使用方式或最佳实践
3. 修复发现的错误或不一致
4. 新增的示例代码和使用场景

