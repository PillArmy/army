
---
name: "army-named-row-param"
description: "Master SQLs.namedRowParam() and SQLs.rowParam() usage. Invoke when user needs to work with IN/NOT IN conditions using multi-value parameters in Army framework."
---

# Army namedRowParam 和 rowParam 完整技能

## 概述

此技能帮助掌握 Army 框架中的 `SQLs.namedRowParam()` 和 `SQLs.rowParam()` 方法，用于在 IN/NOT IN 操作符中处理多个值的条件匹配。

---

## 一、核心方法

### 1.1 rowParam(TypeInfer type, Collection&lt;?&gt; values)

创建匿名的多行参数表达式，用于单查询中的 IN 条件。

**方法签名**:
```java
public static RowParamExpression rowParam(TypeInfer type, Collection&lt;?&gt; values)
```

**使用形式**:
```java
field.in(SQLs::rowParam, values)
```

**适用场景**: 普通查询、单条语句执行。

---

### 1.2 namedRowParam(TypeInfer type, String name, int size)

创建命名的多行参数表达式，用于批处理操作。

**方法签名**:
```java
public static RowParamExpression namedRowParam(TypeInfer type, String name, int size)
```

**使用形式**:
```java
field.spaceIn(SQLs::namedRowParam, size)
```

**适用场景**: 批量更新、批量删除。

---

## 二、完整使用示例

### 示例1: 单查询中使用 rowParam()

```java
import static io.army.criteria.impl.SQLs.*;

List&lt;Long&gt; idList = Arrays.asList(1L, 2L, 3L);

Select select = MySQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T, AS, "r")
    .where(ChinaRegion_.id.in(SQLs::rowParam, idList))
    .asQuery();
```

---

### 示例2: 批处理删除中使用 namedRowParam()

```java
import static io.army.criteria.impl.SQLs.*;

List&lt;Map&lt;String, Object&gt;&gt; paramList = new ArrayList&lt;&gt;();

Map&lt;String, Object&gt; param1 = new HashMap&lt;&gt;();
param1.put("ids", Arrays.asList(1L, 2L));
paramList.add(param1);

Map&lt;String, Object&gt; param2 = new HashMap&lt;&gt;();
param2.put("ids", Arrays.asList(3L, 4L, 5L));
paramList.add(param2);

BatchDelete batchDelete = MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, AS, "r")
    .where(ChinaRegion_.id.spaceIn(SQLs::namedRowParam, 3))
    .asDelete()
    .namedParamList(paramList);
```

---

### 示例3: 与其他条件配合使用

```java
import static io.army.criteria.impl.SQLs.*;

List&lt;Map&lt;String, Object&gt;&gt; paramList = new ArrayList&lt;&gt;();

Map&lt;String, Object&gt; param1 = new HashMap&lt;&gt;();
param1.put("ids", Arrays.asList(1L, 2L, 3L));
param1.put("regionType", RegionType.PROVINCE);
paramList.add(param1);

BatchDelete batchDelete = MySQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, AS, "r")
    .where(ChinaRegion_.id.spaceIn(SQLs::namedRowParam, 3))
    .and(ChinaRegion_.regionType::spaceEqual, SQLs::namedParam)
    .asDelete()
    .namedParamList(paramList);
```

---

## 三、重要参数说明

| 参数 | 说明 |
|------|------|
| `size` | 参数列表的最大大小，决定 SQL 占位符数量 |
| `name` | 命名参数的名称，需与 `namedParamList()` 中 Map 的 key 一致 |
| `type` | 自动从字段元数据推断的类型 |

---

## 四、相关方法家族

除了上述核心方法外，还有以下相关方法可用：

- `SQLs.namedParam(TypeInfer type, String name)` - 单值命名参数
- `SQLs.param(TypeInfer type, Object value)` - 单值参数
- `SQLs.rowLiteral(TypeInfer type, Collection&lt;?&gt; values)` - 多行字面量
- `SQLs.namedRowLiteral(TypeInfer type, String name)` - 命名多行字面量
- `SQLs.rowConst(TypeInfer type, Collection&lt;?&gt; values)` - 多行常量
- `SQLs.namedRowConst(TypeInfer type, String name)` - 命名多行常量

---

## 五、注意事项

1. **size 参数**: 表示参数列表的最大大小，决定了占位符的数量。
2. **参数命名**: `namedParamList()` 中 Map 的 key 必须与 `namedRowParam()` 使用的参数名一致。
3. **类型兼容**: 所有值必须与字段类型兼容。
4. **正确使用**:
   - 单查询用 `rowParam()` 和 `field.in()`
   - 批处理用 `namedRowParam()` 和 `field.spaceIn()`

---

## 六、与其他技能配合

本技能可与以下技能配合使用：
- **MYSQL_BATCH_SINGLE_DELETE_SKILL** - MySQL 批量删除
- **POSTGRES_SINGLE_DELETE_SKILL** - PostgreSQL 删除
- **POSTGRES_BATCH_SINGLE_UPDATE_GUIDE** - PostgreSQL 批量更新
- 其他 Army 批处理相关技能

---

## 自我进化提示

此技能可根据以下场景进一步扩展：
1. 不同数据库的特殊处理
2. 更多实际项目中的复杂示例
3. 性能优化建议
4. 边界场景处理

