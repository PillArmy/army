---
name: "batch-domain-delete-guide"
description: "Complete guide for SQLs.batchDomainDelete() method chain with syntax, usage examples, and best practices. Invoke when working with batch domain delete operations."
---

# SQLs.batchDomainDelete() 完整指南

## 方法链概览

```
SQLs.batchDomainDelete()
  └── deleteFrom(table, AS, alias)
        └── where(predicate) / where(consumer) / whereIf(...)
              ├── and(predicate) / and(operator, operand) / andIf(...) [可重复]
              └── asDelete()
                    └── namedParamList(paramList)
```

## 详细方法链

### 1. 入口方法
| 方法 | 描述 | 可重复 |
|------|------|--------|
| `SQLs.batchDomainDelete()` | 批量域删除语句构建器入口 | ❌ |

### 2. FROM 子句
| 方法 | 参数 | 描述 | 可重复 |
|------|------|------|--------|
| `deleteFrom(table, AS, alias)` | `TableMeta<?>` table, `SQLs.WordAs` AS, `String` alias | 指定要删除的表和别名 | ❌ |

### 3. WHERE 子句 (必需)
| 方法 | 参数 | 描述 | 可重复 |
|------|------|------|--------|
| `where(predicate)` | `IPredicate` | 添加单个 WHERE 条件 | ❌ |
| `where(consumer)` | `Consumer<Consumer<IPredicate>>` | 通过 lambda 批量添加条件 | ❌ |
| `whereIf(supplier)` | `Supplier<IPredicate>` | 条件性添加 WHERE 子句 | ❌ |
| `whereIf(operator, value)` | `Function<T, IPredicate>, `T` | 条件性添加条件 | ❌ |
| `and(predicate)` | `IPredicate` | 添加 AND 条件 | ✅ |
| `and(operator, operand)` | `Function<T, IPredicate>`, `T` | 添加 AND 条件 | ✅ |
| `ifAnd(supplier)` | `Supplier<IPredicate>` | 条件性添加 AND | ✅ |
| `ifAnd(operator, value)` | `Function<T, IPredicate>`, `T` | 条件性添加 AND | ✅ |

### 4. 语句完成
| 方法 | 参数 | 描述 | 可重复 |
|------|------|------|--------|
| `asDelete()` | 无 | 完成语句构建 | ❌ |
| `namedParamList(paramList)` | `List<?>` | 设置批量参数列表 | ❌ |

## IPredicate 条件表达式方法

### 基础比较操作
```java
field.equal(value)              // =
field.notEqual(value)         // <>
field.less(value)            // <
field.lessEqual(value)       // <=
field.greater(value)        // >
field.greaterEqual(value)  // >=
field.nullSafeEqual(value)   // <=> (MySQL)
```

### 范围操作
```java
field.between(first, AND, second)              // BETWEEN
field.notBetween(first, AND, second)       // NOT BETWEEN
field.between(SYMMETRIC, first, AND, second)  // Postgres
field.notBetween(SYMMETRIC, first, AND, second)
```

### IN 操作
```java
field.in(collection)          // IN (...)
field.notIn(collection)       // NOT IN (...)
```

### NULL 检查
```java
field.isNull()                // IS NULL
field.isNotNull()             // IS NOT NULL
```

### 逻辑组合
```java
predicate.or(predicate)              // OR
predicate.or(operator, operand)       // OR
predicate.orGroup(consumer)        // OR (...)
predicate.orGroup(p1, p2)       // OR (p1 OR p2)
predicate.orGroup(p1, p2, p3)   // OR (p1 OR p2 OR p3)
predicate.ifOr(supplier)       // 条件性 OR
predicate.ifOr(operator, value)  // 条件性 OR
predicate.ifOrGroup(consumer)    // 条件性 OR (...)
```

## 使用示例

### 示例 1: 简单批量删除
```java
final BatchDelete stmt = SQLs.batchDomainDelete()
    .deleteFrom(ChinaRegion_.T, AS, "cr")
    .where(ChinaRegion_.id.equal(SQLs::namedParam))
    .and(ChinaRegion_.version.equal(SQLs::param, "0"))
    .asDelete()
    .namedParamList(provinceList);
```

### 示例 2: 使用 Consumer 批量添加条件
```java
final BatchDelete stmt = SQLs.batchDomainDelete()
    .deleteFrom(ChinaProvince_.T, AS, "p")
    .where(and -> {
        and.accept(ChinaProvince_.id.equal(SQLs::namedParam));
        and.accept(ChinaRegion_.name.equal(SQLs::namedParam, ChinaRegion_.NAME));
    })
    .asDelete()
    .namedParamList(paramList);
```

### 示例 3: 使用 orGroup 条件分组
```java
final BatchDelete stmt = SQLs.batchDomainDelete()
    .deleteFrom(ChinaProvince_.T, AS, "p")
    .where(ChinaProvince_.id.equal(SQLs::namedParam))
    .and(ChinaRegion_.name.equal(SQLs::param, "江湖"))
    .and(ChinaProvince_.governor.equal(SQLs::param, "石教主")
        .orGroup(or -> {
            or.accept(ChinaProvince_.governor.equal(SQLs::param, "钟教主"));
            or.accept(ChinaProvince_.governor.equal(SQLs::param, "方腊"));
        })
    )
    .asDelete()
    .namedParamList(paramList);
```

### 示例 4: 条件性添加 whereIf
```java
final BatchDelete stmt = SQLs.batchDomainDelete()
    .deleteFrom(ChinaRegion_.T, AS, "cr")
    .whereIf(ChinaRegion_.id::equal, SQLs::namedParam, optionalId)
    .andIf(ChinaRegion_.name::equal, SQLs::param, optionalName)
    .asDelete()
    .namedParamList(paramList);
```

## 参数包装方法 (SQLs.*)

| 方法 | 说明 | 使用场景 |
|------|------|----------|
| `SQLs::param` | 普通参数 | 单值常量值 |
| `SQLs::literal` | 字面量 | 直接嵌入 SQL |
| `SQLs::constant` | 常量 | 编译时常量 |
| `SQLs::namedParam` | 命名参数 | 批量操作 |
| `SQLs::namedLiteral` | 命名字面量 | 批量字面量 |
| `SQLs::namedConst` | 命名常量 | 批量常量 |
| `SQLs::rowParam` | 行参数 | IN 子句集合 |

## 关键点说明

1. **域删除 (Domain Delete) vs 单表删除
   - `batchDomainDelete()`: 支持父子表结构的域删除
   - 与 `batchSingleDelete()`: 普通单表删除

2. **必须有 WHERE 子句**
   - DML 语句必须有 WHERE 条件，防止误删全表

3. **批量参数列表**
   - `namedParamList(List)`: List 中每个元素对应一次删除操作的参数
   - 通常是 `List<Map<String, Object>>` 或 `List<实体对象>`

4. **执行方式**
   ```java
   List<Long> rowList = session.batchUpdate(stmt);
   ```
   ```

