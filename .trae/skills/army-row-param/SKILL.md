---
name: "army-row-param"
description: "全面掌握 SQLs.rowParam() 和 namedRowParam() 的使用，用于构建 Army 框架中的多行参数，支持 IN 条件查询和批量操作。Invoke when user needs to use row parameters in Army framework queries, especially for IN conditions or batch operations."
---

# Army SQLs.rowParam() 完整指南

## 概述

`SQLs.rowParam()` 和 `SQLs.namedRowParam()` 方法提供了创建多行参数表达式的能力。这些方法用于在 Army 框架中安全地构建 SQL 查询，生成多个 JDBC 参数占位符 `(?, ?, ?)`，特别适用于 IN 条件查询和批量操作。

**注意**：这些方法定义在 `SQLSyntax` 类中，但通过 `SQLs` 类（继承自 `SQLSyntax`）使用。

---

## 1. rowParam(TypeInfer, Collection) - 匿名多行参数

### 说明
创建多值参数表达式，用于 IN 或 NOT IN 条件，生成 `(?, ?, ?)` 形式的占位符。

### 方法签名
```java
public static RowParamExpression rowParam(final TypeInfer type, final Collection<?> values)
```

### 参数说明
- `type`: 集合元素的类型推断对象，可以是 `MappingType`、`FieldMeta`、`TypedField` 等
- `values`: 非空且非空的集合

### 实现细节
- 内部通过 `ArmyRowParamExpression.multi()` 方法创建 `AnonymousMultiParam` 实例
- 实现了 `MultiParam` 接口
- 会验证集合不能为空
- 会验证类型推断不能是编码字段（codec field）

### 使用场景
- IN 条件查询
- NOT IN 条件查询
- 批量值匹配

### 示例代码
```java
import static io.army.criteria.impl.SQLs.*;
import io.army.criteria.impl.Postgres;
import io.army.mapping.LongType;
import java.util.List;

// 示例 1: 使用 MappingType 进行类型推断
List<Long> ids = Arrays.asList(1L, 2L, 3L, 4L, 5L);
Postgres.query()
    .select(ASTERISK)
    .from(ChinaRegion_.T, AS, "r")
    .where(ChinaRegion_.id.in(rowParam(LongType.INSTANCE, ids)))
    .asQuery();

// 示例 2: 使用 FieldMeta 进行类型推断
Postgres.query()
    .select(ASTERISK)
    .from(ChinaRegion_.T, AS, "r")
    .where(ChinaRegion_.id.in(rowParam(ChinaRegion_.id, ids)))
    .asQuery();

// 示例 3: 使用 NOT IN
Postgres.query()
    .select(ASTERISK)
    .from(ChinaRegion_.T, AS, "r")
    .where(ChinaRegion_.id.notIn(rowParam(ChinaRegion_.id, ids)))
    .asQuery();

// 示例 4: 作为方法引用与 BiFunction 配合使用（推荐方式）
Postgres.query()
    .select(ASTERISK)
    .from(ChinaRegion_.T, AS, "r")
    .where(ChinaRegion_.id.in(SQLs::rowParam, ids))
    .asQuery();

// 示例 5: 在 DELETE 语句中使用
Delete stmt = SQLs.singleDelete()
    .deleteFrom(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.id.in(SQLs::rowParam, ids))
    .and(ChinaRegion_.createTime.between(SQLs::param, 
        LocalDateTime.now().minusMinutes(10), AND, 
        LocalDateTime.now().plusSeconds(1)))
    .asDelete();

// 示例 6: 与 QualifiedField 配合使用
Delete stmt = SQLs.singleDelete()
    .deleteFrom(ChinaRegion_.T, AS, "c")
    .where(SQLs.field("c", ChinaRegion_.id).in(SQLs::rowParam, ids))
    .asDelete();

// 示例 7: 在 CTE 中使用
Delete stmt = SQLs.singleDelete()
    .with("idListCte").as(c -> c.select(ChinaRegion_.id)
        .from(ChinaRegion_.T, AS, "t")
        .where(ChinaRegion_.id.in(SQLs::rowParam, ids))
        .and(ChinaRegion_.regionType.equal(SQLs::param, RegionType.NONE))
        .asQuery())
    .space()
    .deleteFrom(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.id::in, SQLs.subQuery()
        .select(s -> s.space(SQLs.refField("cte", "id")))
        .from("idListCte", AS, "cte")
        .asQuery())
    .asDelete();
```

### 实际项目示例（来自 Army 测试代码）
```java
// army-example/src/test/java/io/army/session/sync/standard/DeleteTests.java
@Test
public void deleteParent(final SyncLocalSession session) {
    final List<ChinaRegion<?>> regionList = createReginListWithCount(3);
    session.batchSave(regionList);
    
    final LocalDateTime now = LocalDateTime.now();
    
    final Delete stmt = SQLs.singleDelete()
        .deleteFrom(ChinaRegion_.T, AS, "c")
        .where(ChinaRegion_.id.in(SQLs::rowParam, extractRegionIdList(regionList)))
        .and(ChinaRegion_.createTime.between(SQLs::param, now.minusMinutes(10), AND, now.plusSeconds(1)))
        .asDelete();
    
    final long rows = session.update(stmt);
    Assert.assertEquals(rows, regionList.size());
}
```

---

## 2. namedRowParam(TypeInfer, String, int) - 命名多行参数

### 说明
创建命名的多值参数表达式，用于批处理操作中的 IN 条件。

### 方法签名
```java
public static RowParamExpression namedRowParam(final TypeInfer type, final String name, final int size)
```

### 参数说明
- `type`: 集合元素的类型推断对象
- `name`: 参数名称，用于从 Map 或 Java Bean 中获取值
- `size`: 集合大小（必须大于 0）

### 实现细节
- 内部通过 `ArmyRowParamExpression.named()` 方法创建 `ArmyNamedRowParam` 实例
- 实现了 `NamedParam.NamedRow` 接口
- 会验证名称必须有文本
- 会验证 size 必须大于 0
- 会验证类型推断不能是编码字段（codec field）

### 使用场景
- 批量更新中的 IN 条件
- 批量删除中的 IN 条件
- 批量插入中的多值操作
- 与 TypedField 的 spaceIn() 方法配合使用

### 与 TypedField 配合使用
TypedField 提供了 `spaceIn(TeNamedParamsFunc<TypedField>, int)` 方法，这是与 namedRowParam 配合使用的推荐方式：

```java
public interface TypedField {
    // ...
    IPredicate spaceIn(TeNamedParamsFunc<TypedField> namedOperator, int size);
    // ...
}
```

### 示例代码
```java
import static io.army.criteria.impl.SQLs.*;
import io.army.criteria.BatchDelete;
import io.army.criteria.BatchUpdate;
import io.army.mapping.LongType;

// 示例 1: 基本使用
BatchDelete stmt = SQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.id.in(namedRowParam(LongType.INSTANCE, "idList", 5)))
    .asDelete();

// 示例 2: 使用 FieldMeta
BatchDelete stmt = SQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.id.in(namedRowParam(ChinaRegion_.id, "idList", 10)))
    .asDelete();

// 示例 3: 与 spaceIn() 方法配合使用（推荐方式）
BatchDelete stmt = SQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.id.spaceIn(SQLs::namedRowParam, 5))
    .asDelete();

// 示例 4: 在批量更新中使用
BatchUpdate stmt = SQLs.batchSingleUpdate()
    .update(ChinaRegion_.T, AS, "r")
    .set(ChinaRegion_.name, SQLs::namedParam)
    .where(ChinaRegion_.id.spaceIn(SQLs::namedRowParam, 3))
    .asUpdate();

// 示例 5: 完整的批量操作示例
List<Map<String, Object>> paramList = Arrays.asList(
    Collections.singletonMap("idList", Arrays.asList(1L, 2L, 3L)),
    Collections.singletonMap("idList", Arrays.asList(4L, 5L, 6L))
);

BatchDelete stmt = SQLs.batchSingleDelete()
    .deleteFrom(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.id.spaceIn(SQLs::namedRowParam, 3))
    .asDelete()
    .namedParamList(paramList);

List<Long> rowList = session.batchUpdate(stmt);
```

### TeNamedParamsFunc 接口
`namedRowParam` 可以与 `TeNamedParamsFunc` 函数式接口配合使用：

```java
@FunctionalInterface
public interface TeNamedParamsFunc<E extends TypeInfer> {
    RowExpression apply(E exp, String paramName, int size);
}
```

这种设计允许将 `SQLs::namedRowParam` 作为方法引用传递给 TypedField 的 `spaceIn()` 方法。

---

## 3. ArmyRowParamExpression 内部实现

### 类结构
```java
abstract class ArmyRowParamExpression extends OperationRowExpression
    implements RowParamExpression, ArmySimpleExpression {
    
    // 匿名多行参数
    private static final class AnonymousMultiParam 
        extends ArmyRowParamExpression implements MultiParam {
        private final TypeMeta type;
        final List<?> valueList;
        // ...
    }
    
    // 命名多行参数
    private static final class ArmyNamedRowParam 
        extends ArmyRowParamExpression implements NamedParam.NamedRow {
        private final String name;
        private final TypeMeta type;
        private final int valueSize;
        // ...
    }
}
```

### SQL 输出格式
- `rowParam()` 输出：`( ?, ?, ? )`
- `namedRowParam()` 输出：`( ?:[name][0], ?:[name][1], ?:[name][2] )`

---

## 4. 与其他方法的对比

| 方法 | 占位符 | 类型推断 | 命名 | 多行 | 使用场景 |
|------|--------|----------|------|------|----------|
| `parameter(Object)` | `?` | 自动 | 否 | 否 | 简单单值参数 |
| `param(TypeInfer, Object)` | `?` | 显式 | 否 | 否 | 需要明确类型或 null 值 |
| `namedParam(TypeInfer, String)` | `?` | 显式 | 是 | 否 | 批量操作 |
| `rowParam(TypeInfer, Collection)` | `(?, ?, ?)` | 显式 | 否 | 是 | IN 查询 |
| `namedRowParam(TypeInfer, String, int)` | `(?:name[0], ?:name[1])` | 显式 | 是 | 是 | 批量 IN 查询 |

---

## 5. 与其他 Army 功能配合使用

### 5.1 与 TypedField 配合
```java
// 方法引用方式（推荐）
ChinaRegion_.id.in(SQLs::rowParam, ids);

// 显式调用方式
ChinaRegion_.id.in(rowParam(ChinaRegion_.id, ids));

// 批量操作方式
ChinaRegion_.id.spaceIn(SQLs::namedRowParam, 5);
```

### 5.2 与 CTE 配合
```java
SQLs.query()
    .with("selected_ids").as(c -> c.select(ChinaRegion_.id)
        .from(ChinaRegion_.T, AS, "t")
        .where(ChinaRegion_.id.in(SQLs::rowParam, ids))
        .asQuery())
    .select(ASTERISK)
    .from("selected_ids", AS, "si")
    .join(ChinaRegion_.T, AS, "r")
        .on(refField("si", "id").equal(ChinaRegion_.id))
    .asQuery();
```

### 5.3 与 QualifiedField 配合
```java
SQLs.query()
    .select(ASTERISK)
    .from(ChinaRegion_.T, AS, "r")
    .where(SQLs.field("r", ChinaRegion_.id).in(SQLs::rowParam, ids))
    .asQuery();
```

---

## 6. 完整综合示例

### 示例 1：查询 + 删除完整流程
```java
import static io.army.criteria.impl.SQLs.*;
import io.army.criteria.Delete;
import io.army.criteria.Select;
import io.army.example.bank.domain.user.ChinaRegion_;
import io.army.session.SyncLocalSession;
import java.time.LocalDateTime;
import java.util.List;

public class RowParamExample {
    
    public void queryAndDelete(SyncLocalSession session) {
        // 1. 先查询需要删除的 ID
        Select selectStmt = SQLs.query()
            .select(ChinaRegion_.id)
            .from(ChinaRegion_.T, AS, "r")
            .where(ChinaRegion_.regionType.equal(SQLs::param, RegionType.NONE))
            .asQuery();
        
        List<Long> ids = session.queryObjectList(selectStmt, 
            row -> (Long) row.get("id"));
        
        if (ids.isEmpty()) {
            return;
        }
        
        // 2. 使用 rowParam 删除这些记录
        Delete deleteStmt = SQLs.singleDelete()
            .deleteFrom(ChinaRegion_.T, AS, "r")
            .where(ChinaRegion_.id.in(SQLs::rowParam, ids))
            .and(ChinaRegion_.createTime.less(SQLs::param, 
                LocalDateTime.now().minusDays(30)))
            .asDelete();
        
        long deletedRows = session.update(deleteStmt);
        LOG.debug("Deleted {} records", deletedRows);
    }
}
```

### 示例 2：批量删除操作
```java
import static io.army.criteria.impl.SQLs.*;
import io.army.criteria.BatchDelete;
import io.army.example.bank.domain.user.ChinaRegion_;
import io.army.session.SyncLocalSession;
import java.util.List;
import java.util.Map;

public class BatchRowParamExample {
    
    public void batchDeleteByGroups(SyncLocalSession session, 
                                     List<List<Long>> idGroups) {
        
        // 1. 准备参数列表
        List<Map<String, Object>> paramList = idGroups.stream()
            .map(ids -> Collections.singletonMap("idList", ids))
            .collect(Collectors.toList());
        
        // 2. 使用 namedRowParam 创建批量删除语句
        int groupSize = idGroups.get(0).size(); // 假设所有组大小相同
        
        BatchDelete batchStmt = SQLs.batchSingleDelete()
            .deleteFrom(ChinaRegion_.T, AS, "c")
            .where(ChinaRegion_.id.spaceIn(SQLs::namedRowParam, groupSize))
            .asDelete()
            .namedParamList(paramList);
        
        // 3. 执行批量操作
        List<Long> rowList = session.batchUpdate(batchStmt);
        
        // 4. 验证结果
        long totalDeleted = rowList.stream().mapToLong(Long::longValue).sum();
        LOG.debug("Total deleted: {}", totalDeleted);
    }
}
```

### 示例 3：与 MySQL 变量配合使用
```java
import static io.army.criteria.impl.MySQLs.*;
import io.army.criteria.Select;
import io.army.example.bank.domain.user.ChinaRegion_;
import java.util.List;

public class MysqlRowParamWithVariable {
    
    public void rowNumberExample(SyncLocalSession session, List<Long> ids) {
        Select stmt = MySQLs.query()
            .select(s -> s.space(MySQLs.at("my_row_number").increment().as("rowNumber"))
                .comma("t", PERIOD, ChinaRegion_.T))
            .from(ChinaRegion_.T, AS, "t")
            .crossJoin(SQLs.subQuery()
                .select(MySQLs.at("my_row_number", SQLs.COLON_EQUAL, SQLs.LITERAL_0).as("n"))
                .asQuery())
            .as("s")
            .where(ChinaRegion_.id.in(SQLs::rowParam, ids))
            .orderBy(ChinaRegion_.id)
            .asQuery();
        
        List<Map<String, Object>> rowList = session.queryObjectList(stmt, RowMaps::hashMap);
    }
}
```

---

## 7. 最佳实践

1. **优先使用方法引用**：`field.in(SQLs::rowParam, values)` 比 `field.in(rowParam(type, values))` 更简洁
2. **非空验证**：`rowParam` 会验证集合不能为空，调用前确保集合非空
3. **批量操作**：使用 `namedRowParam` 时，确保所有批次的集合大小一致
4. **类型推断**：优先使用 `FieldMeta`（如 `ChinaRegion_.id`）进行类型推断，而不是 `MappingType`
5. **性能考虑**：非常大的集合可能需要分批处理，避免 SQL 语句过长
6. **SQL 安全**：始终使用这些参数方法，不要直接拼接 SQL 值

---

## 8. 注意事项

1. `rowParam` 的集合不能为空，否则会抛出 `CriteriaException`
2. `namedRowParam` 的 `size` 参数必须大于 0
3. 类型推断不能是编码字段（codec field），否则会抛出异常
4. `namedRowParam` 主要用于批量操作，普通查询使用 `rowParam`
5. 所有这些方法都生成 JDBC 参数占位符，确保 SQL 注入安全

---

## 9. 相关技能

- [army-sql-param](../army-sql-param/SKILL.md) - SQL 参数完整指南
- [army-named-param-guide](../army-named-param-guide/SKILL.md) - 命名参数指南
- [mysql-batch-single-update](../mysql-batch-single-update/SKILL.md) - MySQL 批量单个更新

---

## 进化说明

本 skill 支持自我进化，如果你发现新的使用场景、最佳实践或代码示例，请更新此文档！

