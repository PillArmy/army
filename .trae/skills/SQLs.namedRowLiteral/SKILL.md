---
name: "SQLs.namedRowLiteral"
description: "完整学习和使用 SQLs.namedRowLiteral() 方法，包括声明、实现、使用场景和示例。用于构建命名多行字面量表达式。"
---

# SQLs.namedRowLiteral 完整指南

## 1. 方法声明

### 1.1 方法签名

```java
public static RowLiteralExpression namedRowLiteral(TypeInfer type, String name)
```

**位置**：`io.army.criteria.impl.SQLSyntax`

**返回类型**：`RowLiteralExpression`

### 1.2 相关常量和方法

- `SQLs.namedRowConst(TypeInfer type, String name)` - 命名多行常量表达式（无类型前缀）
- `SQLs.namedLiteral(TypeInfer type, String name)` - 命名单行字面量表达式
- `SQLs.rowLiteral(TypeInfer type, Collection<?> values)` - 匿名多行字面量表达式

## 2. 实现原理

### 2.1 核心实现类

`ArmyRowLiteralExpression` 是多行字面量表达式的核心实现类：

```java
// 位置: io.army.criteria.impl.ArmyRowLiteralExpression
static ArmyRowLiteralExpression named(final TypeInfer infer, final String name, final boolean typeName)
```

### 2.2 实现细节

- **命名多行字面量**：返回 `ArmyNamedRowLiteral` 实例
- **匿名多行字面量**：返回 `AnonymousRowLiteral` 实例
- `columnSize()` 对命名多行字面量返回 `-1`（因为大小未知）

## 3. 使用场景

### 3.1 主要场景

**仅用于 VALUES INSERT 语句**的批量插入场景，特别是：
- 当每行数据包含 `Collection` 类型字段时
- 当需要将集合元素直接嵌入 SQL 时
- 在批量插入语句中与 `.comma(Field, SQLs::namedRowLiteral, "fieldName")` 配合使用

### 3.2 配合使用的方法

- `TypedField.spaceIn(BiFunction<TypedField, String, RowExpression>)` - IN 条件
- `TypedField.spaceNotIn(BiFunction<TypedField, String, RowExpression>)` - NOT IN 条件

## 4. 使用示例

### 4.1 基础用法

```java
// 创建命名多行字面量
RowLiteralExpression tags = SQLs.namedRowLiteral(StringType.INSTANCE, "tags");
```

### 4.2 在 INSERT 语句中使用

```java
SQLs.singleInsert()
    .insertInto(Article_.T)
    .values()
    .parens(s -> s.space(Article_.title, SQLs::namedLiteral, "title")
                   .comma(Article_.tag, SQLs::namedRowLiteral, "tags")  // 使用命名多行字面量
    )
    .asInsert();
```

### 4.3 与 rowLiteral() 的对比

```java
// 匿名多行字面量（需要提供具体值）
RowLiteralExpression tags1 = SQLs.rowLiteral(StringType.INSTANCE, 
                                              Arrays.asList("java", "sql"));

// 命名多行字面量（仅指定名称，值在批量数据中提供）
RowLiteralExpression tags2 = SQLs.namedRowLiteral(StringType.INSTANCE, "tags");
```

## 5. 方法链关系

虽然 `namedRowLiteral()` 本身不构成方法链，但它常与以下方法链配合使用：

1. **INSERT 语句构建**：
   - `SQLs.singleInsert()` → `.insertInto()` → `.values()` → `.parens()` → `.comma()` → `.asInsert()`

2. **VALUES 语句构建**：
   - `MySQLs.valuesStmt()` / `Postgres.valuesStmt()` → `.values()` → `.row()` / `.parens()` → `.asValues()`

## 6. namedRowLiteral vs namedRowParam

| 特性 | namedRowLiteral | namedRowParam |
|------|-----------------|---------------|
| 参数占位符 | 直接嵌入字面量 | 使用 ? 占位符 |
| 类型前缀 | 有（如 ::VARCHAR） | 无 |
| size 参数 | 不需要 | 需要 |
| 使用场景 | 仅 VALUES INSERT | UPDATE/DELETE/INSERT |

## 7. 相关接口

- `RowExpression` - 多行表达式接口
- `RowLiteralExpression` - 多行字面量接口
- `NamedLiteral` - 命名字面量接口
- `TypedField` - 类型化字段接口（提供 `spaceIn` 等方法）

