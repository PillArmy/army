---
name: "army-sql-param"
description: "全面掌握 SQLs.param() 系列方法，包括 parameter、param、namedParam、rowParam、namedRowParam 等，用于构建 Army 框架 SQL 查询的参数表达式。Invoke when user needs to use SQL parameters in Army framework queries."
---

# Army SQLs.param() 完整指南

## 概述

`SQLs` 类（继承自 `SQLSyntax`）提供了一系列方法来创建 SQL 参数表达式。这些方法用于在 Army 框架中安全地构建 SQL 查询，生成 JDBC 参数占位符 `?`，防止 SQL 注入攻击。

## 方法总览

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `parameter(Object value)` | `ParamExpression` | 自动类型推断的单值参数 |
| `param(TypeInfer type, Object value)` | `ParamExpression` | 显式指定类型的单值参数 |
| `namedParam(TypeInfer type, String name)` | `ParamExpression` | 命名参数（用于批处理） |
| `rowParam(TypeInfer type, Collection<?> values)` | `RowParamExpression` | 多行参数（用于 IN 条件） |
| `namedRowParam(TypeInfer type, String name, int size)` | `RowParamExpression` | 命名多行参数（用于批处理） |

---

## 1. parameter(Object value) - 自动类型推断参数

### 说明
自动根据值的 Java 类型推断对应的数据库类型，生成参数表达式。

### 方法签名
```java
public static ParamExpression parameter(final Object value)
```

### 支持的类型
- `Boolean`, `String`, `Integer`, `Long`, `Short`, `Byte`
- `Double`, `Float`, `BigDecimal`, `BigInteger`
- `byte[]`, `BitSet`
- `CodeEnum`, `TextEnum`
- `LocalTime`, `LocalDate`, `LocalDateTime`
- `OffsetDateTime`, `ZonedDateTime`, `OffsetTime`
- `ZoneId`, `Month`, `DayOfWeek`, `Year`, `YearMonth`, `MonthDay`

### 使用场景
- 简单查询，Army 能自动识别类型
- 与 TypedField 配合使用时

### 示例代码
```java
import static io.army.criteria.impl.SQLs.*;

// 基本使用
ParamExpression nameParam = parameter("John Doe");
ParamExpression ageParam = parameter(25);

// 在查询中使用
Postgres.query()
    .select(ASTERISK)
    .from(User.class, "u")
    .where(User_.name.equal(parameter("John")))
    .and(User_.age.greater(parameter(18)))
    .asQuery();
```

---

## 2. param(TypeInfer type, Object value) - 显式类型参数

### 说明
显式指定参数的类型，当 Army 无法自动推断类型时使用。

### 方法签名
```java
public static ParamExpression param(final TypeInfer type, final @Nullable Object value)
```

### 参数说明
- `type`: 类型推断对象，可以是 `MappingType`、`FieldMeta`、`TypedField` 等
- `value`: 参数值，允许为 null

### 使用场景
- CTE 引用字段（无类型信息）
- 需要精确控制类型映射
- 值为 null 时需要明确类型
- 作为方法引用与 BiFunction 重载配合使用

### 示例代码
```java
import static io.army.criteria.impl.SQLs.*;
import io.army.mapping.IntegerType;
import io.army.mapping.StringType;

// 显式指定类型
ParamExpression priceParam = param(IntegerType.INSTANCE, 100);
ParamExpression nameParam = param(StringType.INSTANCE, "test");

// 使用 FieldMeta 作为类型推断
ParamExpression statusParam = param(User_.status, null);

// 在查询中使用
Postgres.query()
    .select(ASTERISK)
    .from(Product.class, "p")
    .where(Product_.offerPrice.greater(param(IntegerType.INSTANCE, 100)))
    .and(Product_.name.equal(param(StringType.INSTANCE, randomString())))
    .asQuery();

// 与 CTE 引用字段配合使用
Postgres.query()
    .select(ASTERISK)
    .from("cte", "c")
    .where(refField("cte", "offer_price").greater(param(IntegerType.INSTANCE, 100)))
    .asQuery();

// 作为方法引用使用
Product_.offerPrice.greater(SQLs::param, 100);
```

---

## 3. namedParam(TypeInfer type, String name) - 命名参数

### 说明
创建命名参数，主要用于批处理操作（批量更新、批量删除、批量插入）。

### 方法签名
```java
public static ParamExpression namedParam(final TypeInfer type, final String name)
```

### 参数说明
- `type`: 类型推断对象
- `name`: 参数名称，用于从 Map 或 Java Bean 中获取值

### 使用场景
- 批量更新 (batch update)
- 批量删除 (batch delete)
- VALUES 批量插入

### 示例代码
```java
import static io.army.criteria.impl.SQLs.*;

// 批量更新示例
Postgres.batchUpdate()
    .update(User.class, "u")
    .set()
        .field(User_.name).to(namedParam(User_.name, "name"))
        .field(User_.age).to(namedParam(User_.age, "age"))
    .where(User_.id.equal(namedParam(User_.id, "id")))
    .asStatement();

// 批量插入示例
Postgres.batchInsert()
    .insertInto(User.class)
    .values()
        .value(User_.id, namedParam(User_.id, "id"))
        .value(User_.name, namedParam(User_.name, "name"))
        .value(User_.age, namedParam(User_.age, "age"))
    .asStatement();
```

---

## 4. rowParam(TypeInfer type, Collection<?> values) - 多行参数

### 说明
创建多值参数表达式，用于 IN 或 NOT IN 条件，生成 `(?, ?, ?)` 形式的占位符。

### 方法签名
```java
public static RowParamExpression rowParam(final TypeInfer type, final Collection<?> values)
```

### 参数说明
- `type`: 集合元素的类型
- `values`: 非空且非空的集合

### 使用场景
- IN 条件查询
- NOT IN 条件查询
- 批量值匹配

### 示例代码
```java
import static io.army.criteria.impl.SQLs.*;
import java.util.Arrays;
import java.util.List;

List<Integer> ids = Arrays.asList(1, 2, 3, 4, 5);

// IN 查询
Postgres.query()
    .select(ASTERISK)
    .from(User.class, "u")
    .where(User_.id.in(rowParam(IntegerType.INSTANCE, ids)))
    .asQuery();

// NOT IN 查询
Postgres.query()
    .select(ASTERISK)
    .from(User.class, "u")
    .where(User_.id.notIn(rowParam(IntegerType.INSTANCE, ids)))
    .asQuery();

// 使用 FieldMeta
Postgres.query()
    .select(ASTERISK)
    .from(User.class, "u")
    .where(User_.status.in(rowParam(User_.status, Arrays.asList("ACTIVE", "PENDING"))))
    .asQuery();
```

---

## 5. namedRowParam(TypeInfer type, String name, int size) - 命名多行参数

### 说明
创建命名的多值参数，用于批处理操作中的 IN 条件。

### 方法签名
```java
public static RowParamExpression namedRowParam(final TypeInfer type, final String name, final int size)
```

### 参数说明
- `type`: 集合元素的类型
- `name`: 参数名称
- `size`: 集合大小（必须 > 0）

### 使用场景
- 批量更新中的 IN 条件
- 批量删除中的 IN 条件
- 批量插入中的多值操作

### 示例代码
```java
import static io.army.criteria.impl.SQLs.*;

// 批量删除
Postgres.batchDelete()
    .deleteFrom(User.class, "u")
    .where(User_.id.in(namedRowParam(User_.id, "ids", 5)))
    .asStatement();

// 批量更新
Postgres.batchUpdate()
    .update(User.class, "u")
    .set()
        .field(User_.status).to("DELETED")
    .where(User_.id.in(namedRowParam(User_.id, "ids", 10)))
    .asStatement();
```

---

## 6. 预定义常量参数

SQLs 类提供了一系列常用的预定义参数常量，避免重复创建：

| 常量 | 值 | 类型 |
|------|-----|------|
| `PARAM_MINUS_1` | -1 | Integer |
| `PARAM_0` | 0 | Integer |
| `PARAM_1` | 1 | Integer |
| `PARAM_2` | 2 | Integer |
| `PARAM_3` | 3 | Integer |
| `PARAM_5` | 5 | Integer |
| `PARAM_6` | 6 | Integer |
| `PARAM_7` | 7 | Integer |
| `PARAM_8` | 8 | Integer |
| `PARAM_9` | 9 | Integer |
| `PARAM_10` | 10 | Integer |
| `PARAM_100` | 100 | Integer |
| `PARAM_1000` | 1000 | Integer |
| `PARAM_POINT_5` | 0.5 | Double |
| `PARAM_DECIMAL_0` | 0.00 | BigDecimal |
| `PARAM_TRUE` | true | Boolean |
| `PARAM_FALSE` | false | Boolean |
| `PARAM_EMPTY_STRING` | "" | String |
| `PARAM_SPACE` | " " | String |
| `BATCH_NO_PARAM` | 批处理序号 | Integer |

### 使用示例
```java
import static io.army.criteria.impl.SQLs.*;

// 使用预定义参数
Postgres.query()
    .select(ASTERISK)
    .from(Product.class, "p")
    .where(Product_.stock.greater(PARAM_0))
    .and(Product_.discount.equal(PARAM_TRUE))
    .and(Product_.rating.greaterEqual(PARAM_POINT_5))
    .asQuery();
```

---

## 与其他 Army 功能配合使用

### 与 LiteralMode 配合
```java
import io.army.criteria.impl.Expressions;

// 设置全局 LiteralMode
Expressions.LITERAL_MODE = Expressions.LiteralMode.DEFAULT;  // 默认使用 param
Expressions.LITERAL_MODE = Expressions.LiteralMode.LITERAL;  // 使用字面量
Expressions.LITERAL_MODE = Expressions.LiteralMode.CONST;    // 使用常量
Expressions.LITERAL_MODE = Expressions.LiteralMode.PREFERENCE;  // 根据类型选择
```

### 与 TypeInfer 配合
任何实现了 `TypeInfer` 接口的对象都可以作为类型参数：
- `MappingType` (如 `IntegerType.INSTANCE`)
- `FieldMeta` (如 `User_.id`)
- `TypedField` (如 `User_.name`)

### 与批量操作配合
见 `namedParam` 和 `namedRowParam` 的示例。

---

## 最佳实践

1. **简单查询**：优先使用 `parameter()`，自动类型推断更简洁
2. **复杂场景**：使用 `param()` 显式指定类型，更可控
3. **CTE 查询**：必须使用 `param()` 显式指定类型
4. **批量操作**：使用 `namedParam()` 和 `namedRowParam()`
5. **IN 查询**：使用 `rowParam()` 处理集合
6. **常用值**：使用预定义常量（如 `PARAM_0`, `PARAM_TRUE`）
7. **SQL 安全**：始终使用参数表达式，不要直接拼接值

---

## 完整综合示例

```java
import static io.army.criteria.impl.SQLs.*;
import io.army.criteria.impl.Postgres;
import io.army.mapping.IntegerType;
import io.army.mapping.StringType;
import java.util.Arrays;
import java.util.List;

public class ParamExamples {

    // 简单查询 - 使用 parameter()
    public void simpleQuery() {
        Postgres.query()
            .select(ASTERISK)
            .from(User.class, "u")
            .where(User_.name.equal(parameter("Alice")))
            .and(User_.age.greater(parameter(18)))
            .asQuery();
    }

    // 精确类型控制 - 使用 param()
    public void explicitTypeQuery() {
        Postgres.query()
            .select(ASTERISK)
            .from(Product.class, "p")
            .where(Product_.price.greater(param(IntegerType.INSTANCE, 100)))
            .and(Product_.category.equal(param(StringType.INSTANCE, "ELECTRONICS")))
            .asQuery();
    }

    // IN 查询 - 使用 rowParam()
    public void inQuery() {
        List<Integer> ids = Arrays.asList(1, 2, 3, 4, 5);
        
        Postgres.query()
            .select(ASTERISK)
            .from(Order.class, "o")
            .where(Order_.userId.in(rowParam(IntegerType.INSTANCE, ids)))
            .and(Order_.status.notIn(rowParam(Order_.status, Arrays.asList("CANCELLED", "REFUNDED"))))
            .asQuery();
    }

    // CTE 查询 - 必须用 param()
    public void cteQuery() {
        Postgres.query()
            .with("active_users")
                .as(Postgres.query()
                    .select(User_.id, User_.name)
                    .from(User.class, "u")
                    .where(User_.active.equal(PARAM_TRUE))
                    .asSubQuery())
            .select(ASTERISK)
            .from("active_users", "au")
            .join(Order.class, "o").on(refField("active_users", "id").equal(Order_.userId))
            .where(refField("active_users", "name").like(param(StringType.INSTANCE, "A%")))
            .asQuery();
    }

    // 批量更新 - 使用 namedParam()
    public void batchUpdate() {
        Postgres.batchUpdate()
            .update(Product.class, "p")
            .set()
                .field(Product_.price).to(namedParam(Product_.price, "newPrice"))
                .field(Product_.updatedAt).to(CURRENT_TIMESTAMP)
            .where(Product_.id.equal(namedParam(Product_.id, "id")))
            .asStatement();
    }

    // 批量删除 - 使用 namedRowParam()
    public void batchDelete() {
        Postgres.batchDelete()
            .deleteFrom(Product.class, "p")
            .where(Product_.id.in(namedRowParam(Product_.id, "ids", 100)))
            .asStatement();
    }

    // 使用预定义常量
    public void constantParams() {
        Postgres.query()
            .select(ASTERISK)
            .from(Product.class, "p")
            .where(Product_.stock.greater(PARAM_0))
            .and(Product_.available.equal(PARAM_TRUE))
            .and(Product_.discount.greater(PARAM_POINT_5))
            .orderBy(Product_.price.asc())
            .limit(PARAM_10)
            .asQuery();
    }
}
```

---

## 进化说明

本 skill 支持自我进化，如果你发现新的使用场景、最佳实践或代码示例，请更新此文档！
