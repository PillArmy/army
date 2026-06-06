---
name: "sqls-parameter"
description: "Teaches how to use SQLs.parameter(), SQLs.param(), SQLs.namedParam(), SQLs.rowParam(), and SQLs.namedRowParam() methods with examples. Invoke when user needs to learn or use SQL parameter binding in Army framework."
---

# SQLs.parameter() 技能

## 概述

`SQLs.parameter()` 是 Army 框架中用于创建 SQL 参数占位符的核心工具类方法。该方法生成 JDBC 参数占位符 (`?`)，确保 SQL 注入安全，并支持各种类型的参数绑定。

## 核心方法列表

### 1. parameter(Object) - 自动类型推断参数

**声明**：
```java
public static ParamExpression parameter(final Object value)
```

**描述**：
- 根据传入值的 Java 类型自动推断对应的 `MappingType`
- 最简单的参数绑定方式，类似于 JPA 的 `setParameter()`
- 支持多种常见数据类型

**支持的类型**：
- `Boolean`, `String`, `Integer`, `Long`, `Short`, `Byte`
- `Double`, `Float`, `BigDecimal`, `BigInteger`, `byte[]`, `BitSet`
- `CodeEnum`, `TextEnum`
- 时间类型：`LocalTime`, `LocalDate`, `LocalDateTime`, `OffsetDateTime`, `ZonedDateTime`, `OffsetTime`
- 时区/日期相关：`ZoneId`, `Month`, `DayOfWeek`, `Year`, `YearMonth`, `MonthDay`

**使用场景**：
- 单值查询条件
- UPDATE/INSERT 语句的参数绑定
- 任何需要参数占位符的地方

**示例代码**：
```java
// 数值类型
Stock_.offerPrice.greater(SQLs.parameter(BigDecimal.valueOf(100)))

// 枚举类型
Stock_.status.equal(SQLs.parameter(StockStatus.NORMAL))

// 字符串类型
Stock_.name.equal(SQLs.parameter("hello"))

// 日期类型
Stock_.listingDate.greater(SQLs.parameter(LocalDate.now()))

// 在完整查询中使用
final Select stmt = SQLs.query()
    .select(BankUser_.id, BankUser_.nickName)
    .from(BankUser_.T, AS, "u")
    .where(BankUser_.nickName.equal(SQLs.parameter(map.get("nickName"))))
    .asQuery();
```

---

### 2. param(TypeInfer, Object) - 显式类型参数

**声明**：
```java
public static ParamExpression param(final TypeInfer type, final @Nullable Object value)
```

**描述**：
- 显式指定类型的参数创建方法
- 支持 `Supplier` 类型的值（会调用 `Supplier.get()`）
- 可以传入 `null` 值

**参数**：
- `type`: 类型推断器（通常是字段元数据或类型引用）
- `value`: 参数值，可以是 `null` 或 `Supplier`

**使用场景**：
- 需要明确指定类型的场景
- 可能为 null 的参数
- 使用方法引用的延迟绑定

**示例代码**：
```java
// 在 WHERE 条件中使用方法引用
.where(ChinaRegion_.createTime.between(SQLs::param, now.minusMinutes(10), AND, now.plusSeconds(1)))

// 直接调用
Stock_.price.equal(SQLs.param(BigDecimalType.INSTANCE, BigDecimal.valueOf(100)))
```

---

### 3. namedParam(TypeInfer, String) - 命名参数

**声明**：
```java
public static ParamExpression namedParam(final TypeInfer type, final String name)
```

**描述**：
- 创建命名参数，用于批量更新/删除和 VALUES 插入
- 参数名对应 Map 的 key 或 Java Bean 的字段名
- 确保参数非空

**参数**：
- `type`: 类型推断器
- `name`: 参数名称（Map 键或 Bean 字段名）

**使用场景**：
- 批量 UPDATE 操作
- 批量 DELETE 操作
- 批量 INSERT 操作

**示例代码**：
```java
// 批量删除示例
final BatchDelete stmt = SQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.id.equal(SQLs.namedParam(LongType.INSTANCE, "id")))
    .asDelete()
    .namedParamList(extractRegionIdMapList(regionList));
```

---

### 4. rowParam(TypeInfer, Collection) - 多行参数

**声明**：
```java
public static RowParamExpression rowParam(final TypeInfer type, final Collection<?> values)
```

**描述**：
- 创建多行参数表达式，生成多个占位符：`?, ?, ?...`
- 作为 IN/NOT IN 操作符的右操作数时，自动添加括号：`(?, ?, ?...)`
- 集合不能为空

**参数**：
- `type`: 集合元素的类型推断器
- `values`: 非空且非空的集合

**使用场景**：
- IN 查询
- NOT IN 查询
- 批量条件查询

**示例代码**：
```java
// IN 查询示例
final Delete stmt = SQLs.singleDelete()
    .deleteFrom(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.id.in(SQLs::rowParam, extractRegionIdList(regionList)))
    .asDelete();

// 完整的使用示例
List<Long> idList = Arrays.asList(1L, 2L, 3L);
final Select stmt = SQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T)
    .where(ChinaRegion_.id.in(SQLs.rowParam(LongType.INSTANCE, idList)))
    .asQuery();
```

---

### 5. namedRowParam(TypeInfer, String, int) - 命名多行参数

**声明**：
```java
public static RowParamExpression namedRowParam(final TypeInfer type, final String name, final int size)
```

**描述**：
- 创建命名的多行参数，用于批量操作
- 生成多个占位符
- 参数名对应 Map 的 key 或 Java Bean 的字段名

**参数**：
- `type`: 集合元素的类型推断器
- `name`: 参数名称
- `size`: 集合大小（必须大于 0）

**使用场景**：
- 批量 IN 查询
- 批量 DELETE/UPDATE 操作中的多行条件

**示例代码**：
```java
// 批量删除中的命名多行参数
final BatchDelete stmt = SQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.id.in(SQLs.namedRowParam(LongType.INSTANCE, "idList", 5)))
    .asDelete();
```

---

## 与其他方法的对比

| 方法 | 占位符 | 类型推断 | 命名 | 多行 | 使用场景 |
|------|--------|----------|------|------|----------|
| `parameter(Object)` | `?` | 自动 | 否 | 否 | 简单单值参数 |
| `param(TypeInfer, Object)` | `?` | 显式 | 否 | 否 | 需要明确类型或 null 值 |
| `namedParam(TypeInfer, String)` | `?` | 显式 | 是 | 否 | 批量操作 |
| `rowParam(TypeInfer, Collection)` | `?, ?, ?` | 显式 | 否 | 是 | IN 查询 |
| `namedRowParam(TypeInfer, String, int)` | `?, ?, ?` | 显式 | 是 | 是 | 批量 IN 查询 |

---

## 预定义常量参数

SQLs 类提供了一些常用的预定义参数常量：

```java
// 数值常量
SQLs.PARAM_0, SQLs.PARAM_1, SQLs.PARAM_2, ..., SQLs.PARAM_1000
SQLs.PARAM_MINUS_1, SQLs.PARAM_POINT_5
SQLs.PARAM_DECIMAL_0

// 布尔常量
SQLs.PARAM_TRUE, SQLs.PARAM_FALSE

// 字符串常量
SQLs.PARAM_EMPTY_STRING, SQLs.PARAM_SPACE

// 特殊常量
SQLs.BATCH_NO_PARAM  // 用于批量操作的批次号
```

---

## 完整示例

### 示例 1：简单查询中的参数使用

```java
import java.time.LocalDate;
import java.math.BigDecimal;

public class QueryExample {
    
    public Select createStockQuery(BigDecimal minPrice, String status) {
        return SQLs.query()
            .select(Stock_.id, Stock_.name, Stock_.offerPrice)
            .from(Stock_.T)
            .where(Stock_.offerPrice.greater(SQLs.parameter(minPrice)))
            .and(Stock_.status.equal(SQLs.parameter(status)))
            .and(Stock_.listingDate.less(SQLs.parameter(LocalDate.now())))
            .asQuery();
    }
}
```

### 示例 2：IN 查询使用 rowParam

```java
import java.util.List;

public class InQueryExample {
    
    public Delete createDeleteByIds(List<Long> ids) {
        return SQLs.singleDelete()
            .deleteFrom(ChinaRegion_.T, AS, "c")
            .where(ChinaRegion_.id.in(SQLs::rowParam, ids))
            .asDelete();
    }
}
```

### 示例 3：批量操作使用 namedParam

```java
import java.util.List;
import java.util.Map;

public class BatchExample {
    
    public BatchDelete createBatchDelete(List<Map<String, Object>> paramList) {
        return SQLs.batchSingleDelete()
            .deleteFrom(ChinaRegion_.T, AS, "c")
            .where(ChinaRegion_.id.equal(SQLs.namedParam(LongType.INSTANCE, "id")))
            .asDelete()
            .namedParamList(paramList);
    }
}
```

### 示例 4：复杂查询混合使用

```java
import java.time.LocalDateTime;
import java.util.List;

public class ComplexQueryExample {
    
    public Select createComplexQuery(List<Long> userIds, LocalDateTime startTime) {
        return SQLs.query()
            .select(BankUser_.id, BankUser_.nickName, BankAccount_.balance)
            .from(BankUser_.T, AS, "u")
            .join(BankAccount_.T, AS, "a").on(BankUser_.id::equal, BankAccount_.userId)
            .where(BankUser_.id.in(SQLs::rowParam, userIds))
            .and(BankAccount_.createTime.greater(SQLs::param, startTime))
            .and(BankAccount_.balance.greater(SQLs.parameter(BigDecimal.valueOf(1000))))
            .asQuery();
    }
}
```

---

## 最佳实践

1. **优先使用 `parameter(Object)`**：对于简单场景，自动类型推断更方便
2. **使用 `rowParam` 进行 IN 查询**：避免 SQL 拼接，防止注入
3. **批量操作使用 `namedParam`**：配合 `namedParamList()` 使用
4. **利用预定义常量**：如 `SQLs.PARAM_0`, `SQLs.PARAM_TRUE` 等
5. **配合方法引用**：使用 `SQLs::param`, `SQLs::rowParam` 使代码更简洁

---

## 注意事项

1. `parameter(Object)` 的值不能为 `null`，如果需要传 null 请使用 `param(TypeInfer, Object)`
2. `rowParam` 的集合不能为空
3. `namedParam` 和 `namedRowParam` 主要用于批量操作
4. 所有这些方法都生成 JDBC 参数占位符，确保 SQL 注入安全
