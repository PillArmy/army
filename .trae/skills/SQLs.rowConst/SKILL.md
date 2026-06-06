
---
name: "SQLs.rowConst"
description: "完整学习和使用 SQLs.rowConst() 和 SQLs.namedRowConst() 方法，包括声明、实现、使用场景和示例。用于构建多行常量表达式，特别是在 IN 条件查询和批量插入语句中。"
---

# SQLs.rowConst 完整指南

## 概述

本指南详细介绍 Army Criteria API 中 `SQLs` 类提供的 `rowConst()` 和 `namedRowConst()` 方法，这两个方法用于创建多行常量表达式，特别适用于 IN 条件查询和 VALUES 插入语句。

## 核心类

- `SQLs` (io.army.criteria.impl.SQLs)
- `SQLSyntax` (io.army.criteria.impl.SQLSyntax) - SQLs 的父类
- `ArmyRowLiteralExpression` - 多行常量表达式核心实现类

## 方法汇总

| 方法 | 说明 | 返回类型 |
|------|------|----------|
| `rowConst(TypeInfer type, Collection&lt;?&gt; values)` | 创建匿名多行常量表达式 | `RowLiteralExpression` |
| `namedRowConst(TypeInfer type, String name)` | 创建命名多行常量表达式 | `RowLiteralExpression` |

---

## 1. rowConst(TypeInfer type, Collection&lt;?&gt; values) 方法

### 1.1 方法声明

```java
public static RowLiteralExpression rowConst(TypeInfer type, Collection&lt;?&gt; values)
```

**位置**：`io.army.criteria.impl.SQLSyntax` 第 285 行

**返回类型**：`RowLiteralExpression`

### 1.2 功能说明

创建多行常量表达式，会输出多个用逗号分隔的常量。当作为 IN/NOT IN 操作符的右操作数时，会自动输出括号包裹的格式 `(val1, val2, val3, ...)`。

与 `rowLiteral` 类似，但 `rowConst` 的 `typeName` 参数为 `false`，不会输出类型名称前缀，更适合标准 SQL 语句。

### 1.3 参数说明

- **`type`**: 类型推断信息，元素的类型，可以是：
  - `MappingType` - 映射类型
  - `FieldMeta` - 字段元数据
  - 其他 `TypeInfer` 实现

- **`values`**: 非空且非空的集合，包含多个值

### 1.4 异常

- `CriteriaException`: 当 `values` 为空集合或 `infer` 是编码类型的 TableField 时抛出

### 1.5 使用场景

1. **IN 条件查询**：当需要查询多个特定 ID 或值时
2. **NOT IN 条件查询**：排除多个特定值
3. **VALUES 插入语句**：插入多行常量数据

### 1.6 示例代码

#### 示例 1：基础用法

```java
// 创建多行常量表达式
List&lt;Long&gt; idList = Arrays.asList(1L, 2L, 3L, 4L, 5L);
RowLiteralExpression rowConst = SQLs.rowConst(ChinaRegion_.ID, idList);
```

#### 示例 2：在 IN 条件中使用（方法引用方式）

```java
final Select stmt = SQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T)
    .where(ChinaRegion_.id.in(SQLs::rowConst, Arrays.asList(1L, 2L, 3L)))
    .asQuery();
```

#### 示例 3：NOT IN 条件

```java
List&lt;Long&gt; excludeIds = Arrays.asList(99L, 100L);

final Select stmt = SQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T)
    .where(ChinaRegion_.id.notIn(SQLs::rowConst, excludeIds))
    .asQuery();
```

---

## 2. namedRowConst(TypeInfer type, String name) 方法

### 2.1 方法声明

```java
public static RowLiteralExpression namedRowConst(TypeInfer type, String name)
```

**位置**：`io.army.criteria.impl.SQLSyntax` 第 291 行

**返回类型**：`RowLiteralExpression`

### 2.2 功能说明

创建命名的非空多行常量表达式，仅用于 VALUES 插入语句。与 `namedRowLiteral` 类似，但 `namedRowConst` 的 `typeName` 参数为 `false`。

### 2.3 参数说明

- **`type`**: 元素的类型推断信息
- **`name`**: 非空且有文本内容的名称，用于在批量数据中引用

### 2.4 异常

- `CriteriaException`: 当 `name` 没有文本或 `infer` 是编码类型的 TableField 时抛出

### 2.5 限制

- 不能用于批量更新或删除语句
- 只能用于 VALUES 插入语句

### 2.6 使用场景

在批量插入语句中，当每行数据包含 Collection 类型字段，且需要将集合元素直接嵌入 SQL 时使用。

### 2.7 示例代码

#### 示例 1：基础用法

```java
// 创建命名多行常量表达式
RowLiteralExpression tags = SQLs.namedRowConst(StringType.INSTANCE, "tags");
```

#### 示例 2：在 INSERT 语句中使用

```java
final Insert stmt = SQLs.singleInsert()
    .insertInto(Article_.T)
    .values()
    .parens(s -&gt; s.space(Article_.title, SQLs::namedConst, "title")
                   .comma(Article_.tag, SQLs::namedRowConst, "tags")
    )
    .asInsert();
```

---

## 3. 实现原理

### 3.1 核心实现类

`ArmyRowLiteralExpression` 是多行常量表达式的核心实现类：

```java
// 位置: io.army.criteria.impl.ArmyRowLiteralExpression
static ArmyRowLiteralExpression multi(final TypeInfer infer, 
                                       final Collection&lt;?&gt; values,
                                       final boolean typeName)

static ArmyRowLiteralExpression named(final TypeInfer infer, 
                                       final String name,
                                       final boolean typeName)
```

### 3.2 实现细节

#### 匿名多行常量（rowConst）

- 返回 `AnonymousRowLiteral` 实例
- `columnSize()` 返回集合实际大小
- SQL 渲染时直接输出值列表，格式为 `(val1, val2, val3, ...)`

#### 命名多行常量（namedRowConst）

- 返回 `ArmyNamedRowLiteral` 实例
- `columnSize()` 返回 `-1`（因为大小未知，需在批量数据中确定）
- SQL 渲染时根据批量数据动态生成值

---

## 4. rowConst 与其他方法对比

### 4.1 rowConst vs rowLiteral

| 特性 | rowConst | rowLiteral |
|------|----------|------------|
| typeName 参数 | false | true |
| 类型前缀输出 | 不输出 | 可能输出（如 ::VARCHAR）|
| 适用场景 | 标准 SQL | 特定方言（如 PostgreSQL）|

### 4.2 rowConst vs rowParam

| 特性 | rowConst | rowParam |
|------|----------|----------|
| SQL 输出 | 直接嵌入常量值 | 使用 ? 占位符 |
| 性能 | 每次查询可能需要重新解析 | 可复用执行计划 |
| 安全性 | 值直接可见（可能有 SQL 注入风险）| 更安全（参数化） |

### 4.3 namedRowConst vs namedRowLiteral

| 特性 | namedRowConst | namedRowLiteral |
|------|---------------|-----------------|
| typeName 参数 | false | true |
| 类型前缀输出 | 不输出 | 可能输出 |

---

## 5. 完整使用示例

### 示例 1：IN 条件查询

```java
import java.util.Arrays;
import java.util.List;

public class RowConstExample {
    
    public void queryWithInCondition() {
        List&lt;Long&gt; targetIds = Arrays.asList(101L, 102L, 103L, 104L, 105L);
        
        final Select stmt = SQLs.query()
            .select(ChinaRegion_.id, ChinaRegion_.name, ChinaRegion_.population)
            .from(ChinaRegion_.T)
            .where(ChinaRegion_.id.in(SQLs::rowConst, targetIds))
            .and(ChinaRegion_.population.greater(SQLs.CONST_1000))
            .orderBy(ChinaRegion_.name)
            .asQuery();
        
        // 执行查询...
    }
}
```

### 示例 2：NOT IN 条件查询

```java
public void queryWithNotInCondition() {
    List&lt;Long&gt; excludeIds = Arrays.asList(999L, 888L, 777L);
    
    final Select stmt = SQLs.query()
        .select(ChinaRegion_.id, ChinaRegion_.name)
        .from(ChinaRegion_.T)
        .where(ChinaRegion_.id.notIn(SQLs::rowConst, excludeIds))
        .asQuery();
}
```

### 示例 3：批量插入使用命名多行常量

```java
public void batchInsertWithNamedRowConst() {
    final Insert stmt = SQLs.singleInsert()
        .insertInto(Article_.T)
        .values()
        .parens(s -&gt; s.space(Article_.id, SQLs::namedConst, "id")
                       .comma(Article_.title, SQLs::namedConst, "title")
                       .comma(Article_.tags, SQLs::namedRowConst, "tags")
                       .comma(Article_.createdAt, SQLs::namedConst, "createdAt")
        )
        .asInsert();
    
    // 准备批量数据
    List&lt;Map&lt;String, Object&gt;&gt; batchData = new ArrayList&lt;&gt;();
    
    Map&lt;String, Object&gt; row1 = new HashMap&lt;&gt;();
    row1.put("id", 1L);
    row1.put("title", "Java Guide");
    row1.put("tags", Arrays.asList("java", "tutorial", "programming"));
    row1.put("createdAt", LocalDateTime.now());
    batchData.add(row1);
    
    Map&lt;String, Object&gt; row2 = new HashMap&lt;&gt;();
    row2.put("id", 2L);
    row2.put("title", "SQL Tips");
    row2.put("tags", Arrays.asList("sql", "database", "army"));
    row2.put("createdAt", LocalDateTime.now());
    batchData.add(row2);
    
    // 执行批量插入...
}
```

---

## 6. 相关接口

### 6.1 RowLiteralExpression

多行字面量表达式接口，继承自 `RowValueExpression`。

### 6.2 RowExpression

多行表达式接口，用于表示由多个值组成的表达式。

### 6.3 NamedLiteral

命名字面量接口，提供 `name()` 方法获取名称。

---

## 7. 最佳实践

1. **优先使用方法引用**：在条件构建器中优先使用 `SQLs::rowConst` 这样的方法引用方式，代码更简洁。
2. **多行值使用 rowConst**：对于 IN 条件中的多个值，使用 `rowConst` 而不是多个 OR 条件。
3. **注意集合非空**：确保传入的 `values` 集合非空，否则会抛出异常。
4. **合理选择 rowConst vs rowParam**：根据实际需求选择，如果是少量固定值用 `rowConst`，如果是动态或大量值用 `rowParam`。
5. **标准 SQL 优先用 rowConst**：对于标准 SQL 语句，优先使用 `rowConst`（typeName=false）而不是 `rowLiteral`。
6. **命名多行常量仅用于 INSERT**：`namedRowConst` 只能用于 VALUES 插入语句，不要在 UPDATE/DELETE 中使用。

---

## 8. 源文件位置

- `SQLSyntax.java`: `/Users/zoro/repositories/trae/java/hub/army/army-core/src/main/java/io/army/criteria/impl/SQLSyntax.java`
- `ArmyRowLiteralExpression.java`: `/Users/zoro/repositories/trae/java/hub/army/army-core/src/main/java/io/army/criteria/impl/ArmyRowLiteralExpression.java`

---

## 9. 自我进化说明

本技能会持续更新，当发现以下情况时会自动进化：
1. 新增的相关方法或 API 变化
2. 更优的使用方式或最佳实践
3. 修复发现的错误或不一致
4. 新增的示例代码和使用场景
5. 新的数据库方言支持或特性

