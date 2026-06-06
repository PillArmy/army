
---
name: "SQLs.constant"
description: "完整学习和使用 SQLs 类的常量表达式方法，包括 constant、namedConst、rowConst、namedRowConst。Invoke when user needs to work with constant expressions in Army Criteria API."
---

# SQLs.constant 完整指南

## 概述

本指南详细介绍 Army Criteria API 中 `SQLs` 类提供的所有常量表达式相关方法，包含声明、实现、使用场景和示例代码。

## 核心类

- `SQLs` (io.army.criteria.impl.SQLs)
- `SQLSyntax` (io.army.criteria.impl.SQLSyntax) - SQLs 的父类
- `ArmyLiteralExpression` - 单个字面量/常量表达式实现
- `ArmyRowLiteralExpression` - 多行字面量/常量表达式实现

## 常量类型方法汇总

| 方法 | 说明 | 返回类型 |
|------|------|----------|
| `constant(TypeInfer type, Object value)` | 创建常量表达式 | `LiteralExpression` |
| `namedConst(TypeInfer type, String name)` | 创建命名常量表达式 | `LiteralExpression` |
| `rowConst(TypeInfer type, Collection&lt;?&gt; values)` | 创建多行常量表达式 | `RowLiteralExpression` |
| `namedRowConst(TypeInfer type, String name)` | 创建命名的多行常量表达式 | `RowLiteralExpression` |

---

## 1. constant(TypeInfer type, Object value) 方法

### 声明
```java
public static LiteralExpression constant(TypeInfer type, Object value)
```

### 实现位置
`io.army.criteria.impl.SQLSyntax.constant()` (第 276 行)

### 功能
使用指定的类型推断信息创建常量表达式（typeName=false）。与 `literal` 方法类似，但生成的是不输出类型名称的常量字面量。

### 参数
- `type`: 类型推断信息，可以是：
  - `MappingType` - 映射类型
  - `FieldMeta` - 字段元数据
  - 其他 `TypeInfer` 实现
- `value`: 值，可以为 null

### 异常
- `CriteriaException`: 当 infer 是编码类型的 TableField 时抛出

### 使用场景
当需要创建不输出类型名称的 SQL 常量时，特别是在标准 SQL 语句中。

### 示例代码
```java
// 使用字段作为类型推断
SQLs.constant(ChinaRegion_.ID, 100L)

// 使用 MappingType
SQLs.constant(IntegerType.INSTANCE, 100)

// 作为方法引用在表达式链中使用
.where(ChinaRegion_.id.equal(SQLs::constant, 100L))
```

---

## 2. namedConst(TypeInfer type, String name) 方法

### 声明
```java
public static LiteralExpression namedConst(TypeInfer type, String name)
```

### 实现位置
`io.army.criteria.impl.SQLSyntax.namedConst()` (第 281 行)

### 功能
创建命名的非空常量表达式，仅用于 VALUES 插入语句。

### 参数
- `type`: 类型推断信息
- `name`: 非空且有文本内容的名称

### 异常
- `CriteriaException`: 当 infer 是编码类型的 TableField 或 name 没有文本时抛出

### 限制
- 不能用于批量更新或删除语句
- 只能用于 VALUES 插入语句

### 使用场景
在批量插入语句中使用命名常量。

### 预定义常量
```java
SQLs.BATCH_NO_CONST  // 预定义的批处理编号常量
```

---

## 3. rowConst(TypeInfer type, Collection&lt;?&gt; values) 方法

### 声明
```java
public static RowLiteralExpression rowConst(TypeInfer type, Collection&lt;?&gt; values)
```

### 实现位置
`io.army.criteria.impl.SQLSyntax.rowConst()` (第 285 行)

### 功能
创建多行常量表达式，会输出多个用逗号分隔的常量，在 IN/NOT IN 操作符右侧会自动加括号。与 `rowLiteral` 类似，但生成的是不输出类型名称的常量。

### 参数
- `type`: 元素的类型推断信息
- `values`: 非空且非空的集合

### 异常
- `CriteriaException`: 当 values 为空或 infer 是编码类型的 TableField 时抛出

### 使用场景
用于 IN 条件查询中的多个值作为常量使用。

### 示例代码
```java
// 创建多行常量
List&lt;Long&gt; idList = Arrays.asList(1L, 2L, 3L);
SQLs.rowConst(ChinaRegion_.ID, idList)

// 在 IN 条件中使用（方法引用方式）
.where(ChinaRegion_.id.in(SQLs::rowConst, idList))
```

---

## 4. namedRowConst(TypeInfer type, String name) 方法

### 声明
```java
public static RowLiteralExpression namedRowConst(TypeInfer type, String name)
```

### 实现位置
`io.army.criteria.impl.SQLSyntax.namedRowConst()` (第 291 行)

### 功能
创建命名的非空多行常量表达式，仅用于 VALUES 插入语句。

### 参数
- `type`: 元素的类型推断信息
- `name`: 非空且有文本内容的名称

### 限制
- 不能用于批量更新或删除语句
- 只能用于 VALUES 插入语句

---

## 预定义的常量常量

SQLs 类预定义了一些常用的常量供直接使用：

### 数字常量
```java
SQLs.CONST_MINUS_1  // -1
SQLs.CONST_0        // 0
SQLs.CONST_1        // 1
SQLs.CONST_2        // 2
SQLs.CONST_3        // 3
SQLs.CONST_4        // 4
SQLs.CONST_5        // 5
SQLs.CONST_6        // 6
SQLs.CONST_7        // 7
SQLs.CONST_8        // 8
SQLs.CONST_9        // 9
SQLs.CONST_10       // 10
SQLs.CONST_100      // 100
SQLs.CONST_1000     // 1000
SQLs.CONST_POINT_5  // 0.5
SQLs.CONST_DECIMAL_0
```

### 字符串常量
```java
SQLs.CONST_EMPTY_STRING  // ""
SQLs.CONST_SPACE         // " "
```

---

## 常量与字面量的区别

### Literal 系列（typeName=true）
- `literal()` / `literalValue()`
- `namedLiteral()`
- `rowLiteral()`
- `namedRowLiteral()`
- 预定义常量：`LITERAL_*` 系列

### Constant 系列（typeName=false）
- `constant()` / `constValue()`
- `namedConst()`
- `rowConst()`
- `namedRowConst()`
- 预定义常量：`CONST_*` 系列

### SQL 输出差异对比

| 值类型 | `literal`（带类型前缀） | `const`（不带类型前缀） |
|--------|------------------------|------------------------|
| `Integer(100)` | PostgreSQL: `100::INTEGER`<br>MySQL: `100` | `100`（所有数据库） |
| `BigDecimal("0.00")` | PostgreSQL: `0.00::DECIMAL`<br>MySQL: `0.00` | `0.00`（所有数据库） |
| `LocalDate` | `DATE '2024-01-01'` | `'2024-01-01'` |
| `LocalDateTime` | `TIMESTAMP '2024-01-01 12:00:00'` | `'2024-01-01T12:00:00'` |
| `String` | PostgreSQL: `'hello'::VARCHAR`<br>MySQL: `'hello'` | `'hello'`（所有数据库） |

### 实现机制差异

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

### 命名设计说明

- **`constValue` vs `const`**：`const` 是 Java 保留关键字，无法作为方法名，因此使用 `constValue`
- **`constant` vs `constValue`**：两者参数结构不同，`constValue(Object)` 自动推断类型，`constant(TypeInfer, Object)` 显式指定类型，使用不同名称避免重载歧义
- **`namedConst` vs `namedConstant`**：`namedConst` 是缩写形式，与 `namedLiteral` 保持命名模式一致

---

## 内部实现类

### ArmyLiteralExpression
单个字面量/常量表达式的实现类，包含：
- `AnonymousLiteral` - 匿名常量
- `ArmyNamedLiteral` - 命名常量

### ArmyRowLiteralExpression
多行字面量/常量表达式的实现类，包含：
- `AnonymousRowLiteral` - 匿名多行常量
- `ArmyNamedRowLiteral` - 命名多行常量

---

## 完整使用示例

### 示例 1：简单查询中的常量
```java
final Select stmt = SQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T)
    .where(ChinaRegion_.id.equal(SQLs.constant(ChinaRegion_.ID, 100L)))
    .asQuery();
```

### 示例 2：使用方法引用的常量
```java
final Select stmt = SQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T)
    .where(ChinaRegion_.id.equal(SQLs::constant, 100L))
    .limit(SQLs::constant, 10)
    .asQuery();
```

### 示例 3：IN 条件中的多行常量
```java
List&lt;Long&gt; idList = Arrays.asList(1L, 2L, 3L, 4L, 5L);

final Select stmt = SQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T)
    .where(ChinaRegion_.id.in(SQLs::rowConst, idList))
    .asQuery();
```

### 示例 4：使用预定义常量
```java
final Select stmt = SQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T)
    .where(ChinaRegion_.population.greater(SQLs.CONST_1000))
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
- 单个常量：直接渲染为对应类型的 SQL 常量
- 多行常量：渲染为 `(val1, val2, val3, ...)` 格式
- 编码字段会有特殊处理

---

## 与其他方法配合使用

### 与 TypedField 配合
```java
// 比较运算
ChinaRegion_.id.equal(SQLs.constant(ChinaRegion_.ID, 100L))
ChinaRegion_.population.greater(SQLs.CONST_1000)

// 赋值（在 UPDATE 语句中）
.plusEqual(ChinaRegion_.population, SQLs.constant(IntegerType.INSTANCE, 100))
```

### 与 Expression 接口方法配合
```java
SQLs.constant(IntegerType.INSTANCE, 100).plus(SQLs.constant(IntegerType.INSTANCE, 200))
SQLs.constant(StringType.INSTANCE, "hello").concat(SQLs.constant(StringType.INSTANCE, " world"))
```

### 与 IN/NOT IN 操作配合
```java
.where(ChinaRegion_.id.in(SQLs::rowConst, idList))
.where(ChinaRegion_.id.notIn(SQLs::rowConst, excludeList))
```

---

## 最佳实践

1. **优先使用方法引用**：在条件构建器中优先使用 `SQLs::constant` 这样的方法引用方式，代码更简洁。
2. **使用预定义常量**：对于常见的数值（如 0、1、100），使用 SQLs 类预定义的 `CONST_*` 常量。
3. **明确类型**：当类型推断可能出错时，显式指定类型。
4. **多行值使用 rowConst**：对于 IN 条件中的多个值，使用 `rowConst` 而不是多个 OR 条件。
5. **区分 literal 和 constant**：根据需要选择合适的系列，标准 SQL 优先使用 `constant` 系列。

---

## 源文件位置

- `SQLSyntax.java`: `/Users/zoro/repositories/trae/java/hub/army/army-core/src/main/java/io/army/criteria/impl/SQLSyntax.java`
- `SQLs.java`: `/Users/zoro/repositories/trae/java/hub/army/army-core/src/main/java/io/army/criteria/impl/SQLs.java`
- `ArmyLiteralExpression.java`: `/Users/zoro/repositories/trae/java/hub/army/army-core/src/main/java/io/army/criteria/impl/ArmyLiteralExpression.java`
- `ArmyRowLiteralExpression.java`: `/Users/zoro/repositories/trae/java/hub/army/army-core/src/main/java/io/army/criteria/impl/ArmyRowLiteralExpression.java`

---

## 自我进化说明

本技能会持续更新，当发现以下情况时会自动进化：
1. 新增的常量相关方法
2. 更优的使用方式或最佳实践
3. 修复发现的错误或不一致
4. 新增的示例代码和使用场景

