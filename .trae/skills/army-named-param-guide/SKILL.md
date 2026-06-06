---
name: "army-named-param-guide"
description: "学习和使用 Army SQLs.namedParam 系列方法的完整指南。包含所有相关方法、使用场景和实际示例。在需要了解或使用命名参数时调用。"
---

# Army SQLs.namedParam 完全指南

本指南详细介绍 Army 框架中 `SQLs.namedParam` 系列方法的声明、实现、使用场景和示例代码。

---

## 目录
- [命名参数概述](#命名参数概述)
- [核心方法详解](#核心方法详解)
- [使用场景与示例](#使用场景与示例)
- [相关常量](#相关常量)
- [完整示例代码](#完整示例代码)

---

## 命名参数概述

Army 框架的命名参数用于批量操作（批量更新、删除）和 VALUES 插入语句。通过名称引用参数，而不是位置索引，使代码更加清晰和可维护。

### 关键特性
- 支持单值参数和多行参数
- 可用于 WHERE 条件、SET 子句等
- 与批量操作紧密配合
- 支持非空约束和可空选项

---

## 核心方法详解

### 1. `namedParam` - 单值命名参数（非空）

#### 方法声明
```java
public static ParamExpression namedParam(TypeInfer type, String name)
```

#### 位置
`io.army.criteria.impl.SQLSyntax.namedParam()`

#### 实现类
`io.army.criteria.impl.ArmyParamExpression.ImmutableNamedNonNullParam`

#### 参数说明
- `type`: 类型推断器（TypeInfer），用于确定参数的数据类型
- `name`: 参数名称，不能为空字符串

#### 返回值
`ParamExpression` - 参数表达式对象

#### 异常
- `CriteriaException`: 当 infer 是编码字段或 name 没有文本时

#### 使用场景
- 批量更新语句的 WHERE 条件
- 批量删除语句的条件
- VALUES 插入语句的值

#### 调用形式

**形式 1：方法引用 + 类型推断**
```java
.where(ChinaRegion_.id::spaceEqual, SQLs::namedParam)
```

**形式 2：直接传入字段元数据**
```java
.where(ChinaRegion_.id.equal(SQLs::namedParam, ChinaRegion_.ID))
```

**形式 3：显式传入类型和名称**
```java
.setSpace(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::namedParam)
```

---

### 2. `namedRowParam` - 多行命名参数（非空）

#### 方法声明
```java
public static RowParamExpression namedRowParam(TypeInfer type, String name, int size)
```

#### 位置
`io.army.criteria.impl.SQLSyntax.namedRowParam()`

#### 实现类
`io.army.criteria.impl.ArmyRowParamExpression.ArmyNamedRowParam`

#### 参数说明
- `type`: 集合元素的类型推断器
- `name`: 参数名称
- `size`: 集合大小，必须大于 0

#### 返回值
`RowParamExpression` - 多行参数表达式

#### 异常
- `CriteriaException`: 当 name 没有文本、size < 1 或 infer 是编码字段时

#### 使用场景
- IN 子句中的多行参数
- 需要多个占位符的场景

---

### 3. `namedLiteral` - 命名字面量

#### 方法声明
```java
public static LiteralExpression namedLiteral(TypeInfer type, String name)
```

#### 位置
`io.army.criteria.impl.SQLSyntax.namedLiteral()`

#### 说明
创建命名非空字面量表达式。**注意**：此方法仅适用于 VALUES 插入语句，不能用于批量更新/删除语句。

---

### 4. `namedRowLiteral` - 多行命名字面量

#### 方法声明
```java
public static RowLiteralExpression namedRowLiteral(TypeInfer type, String name)
```

#### 位置
`io.army.criteria.impl.SQLSyntax.namedRowLiteral()`

#### 说明
创建命名非空多行字面量表达式。仅适用于 VALUES 插入语句。

---

### 5. `namedConst` - 命名常量

#### 方法声明
```java
public static LiteralExpression namedConst(TypeInfer type, String name)
```

#### 位置
`io.army.criteria.impl.SQLSyntax.namedConst()`

---

### 6. `namedRowConst` - 多行命名常量

#### 方法声明
```java
public static RowLiteralExpression namedRowConst(TypeInfer type, String name)
```

#### 位置
`io.army.criteria.impl.SQLSyntax.namedRowConst()`

---

## 相关常量

SQLs 类提供了预定义的命名参数常量：

```java
// 批量序号字面量
public static final LiteralExpression BATCH_NO_LITERAL = 
    SQLs.namedLiteral(IntegerType.INSTANCE, "$ARMY_BATCH_NO$");

// 批量序号常量
public static final LiteralExpression BATCH_NO_CONST = 
    SQLs.namedConst(IntegerType.INSTANCE, "$ARMY_BATCH_NO$");

// 批量序号参数
public static final ParamExpression BATCH_NO_PARAM = 
    SQLs.namedParam(IntegerType.INSTANCE, "$ARMY_BATCH_NO$");
```

---

## 使用场景与示例

### 场景 1：批量更新（Batch Update）

```java
final BatchUpdate stmt;
stmt = SQLs.batchSingleUpdate()
        .update(ChinaRegion_.T, AS, "p")
        .setSpace(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::namedParam)
        .where(ChinaRegion_.id::spaceEqual, SQLs::namedParam)
        .and(ChinaRegion_.regionGdp.spacePlus(SQLs::namedParam).greaterEqual(0))
        .asUpdate()
        .namedParamList(this.createProvinceList());
```

### 场景 2：批量删除（Batch Delete）

```java
final BatchDelete stmt;
stmt = MySQLs.batchMultiDelete()
        .delete(hintSupplier, Arrays.asList(MySQLs.LOW_PRIORITY))
        .from("c", "r")
        .using(ChinaCity_.T)
        .as("c")
        .join(ChinaRegion_.T)
        .as("r").on(ChinaCity_.id::equal, ChinaRegion_.id)
        .where(ChinaRegion_.id::spaceEqual, SQLs::namedParam)
        .asDelete()
        .namedParamList(paramList);
```

### 场景 3：使用字段名作为参数名

```java
final BatchUpdate stmt;
stmt = SQLs.batchDomainUpdate()
        .update(ChinaProvince_.T, AS, "p")
        .set(ChinaRegion_.name, SQLs::param, "武侠江湖")
        .where(ChinaRegion_.id.equal(SQLs::namedParam, ChinaRegion_.ID))
        .and(ChinaRegion_.name.equal(SQLs::namedParam, ChinaRegion_.NAME))
        .asUpdate()
        .namedParamList(this.createProvinceList());
```

### 场景 4：复合表达式中的命名参数

```java
.and(ChinaRegion_.regionGdp.plus(SQLs::namedParam, ChinaRegion_.REGION_GDP)
        .greaterEqual(0))
```

### 场景 5：条件式命名参数

```java
.ifAnd(ChinaRegion_.regionGdp::plus, SQLs::namedParam, ChinaRegion_.REGION_GDP,
        Expression::greaterEqual, LITERAL_DECIMAL_0)
```

---

## 调用形式总结

### 形式 A：方法引用（最简洁）

```java
.where(ChinaRegion_.id::spaceEqual, SQLs::namedParam)
```
- 利用方法引用自动推断参数类型
- 适用于大多数场景

### 形式 B：显式传入字段名

```java
.where(ChinaRegion_.id.equal(SQLs::namedParam, ChinaRegion_.ID))
```
- 显式指定字段名作为参数名
- 更清晰、可读性更好

### 形式 C：在 SET 子句中使用

```java
.setSpace(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::namedParam)
```
- 用于更新操作中的赋值

### 形式 D：配合 `namedParamList`

```java
.asUpdate()
.namedParamList(this.createProvinceList());
```
- 将命名参数与实际参数列表绑定
- 参数列表可以是 Map 列表或对象列表

---

## 完整示例代码

### 示例 1：批量更新父表

```java
@Test
public void batchUpdateParent() {
    final BatchUpdate stmt;
    stmt = SQLs.batchSingleUpdate()
            .update(ChinaRegion_.T, AS, "p")
            .setSpace(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::namedParam)
            .where(ChinaRegion_.id::spaceEqual, SQLs::namedParam)
            .and(ChinaRegion_.regionGdp.spacePlus(SQLs::namedParam).greaterEqual(0))
            .and(ChinaRegion_.regionGdp.plus(SQLs::namedParam, ChinaRegion_.REGION_GDP)
                    .greaterEqual(0))
            .and(ChinaRegion_.version.equal(SQLs::param, "0"))
            .asUpdate()
            .namedParamList(this.createProvinceList());

    printStmt(LOG, stmt);
}
```

### 示例 2：批量更新子表

```java
@Test
public void batchUpdateChild() {
    final BigDecimal gdpAmount = new BigDecimal("888.8");

    final BatchUpdate stmt;
    stmt = SQLs.batchDomainUpdate()
            .update(ChinaProvince_.T, AS, "p")
            .set(ChinaRegion_.name, SQLs::param, "武侠江湖")
            .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, gdpAmount)
            .where(ChinaRegion_.id.equal(SQLs::namedParam, ChinaRegion_.ID))
            .and(ChinaRegion_.name.equal(SQLs::namedParam, ChinaRegion_.NAME))
            .and(ChinaRegion_.regionGdp.plus(SQLs::literal, gdpAmount)
                    .greaterEqual(0))
            .asUpdate()
            .namedParamList(this.createProvinceList());

    printStmt(LOG, stmt);
}
```

### 示例 3：MySQL 批量删除

```java
@Test
public void multiDeleteWithMapCriteria() {
    final Consumer<Map<String, Object>> daoMethod = map -> {
        final List<Map<String, Object>> paramList = createParamList();

        final BatchDelete stmt;
        stmt = MySQLs.batchMultiDelete()
                .delete(hintSupplier, Arrays.asList(MySQLs.LOW_PRIORITY, MySQLs.QUICK))
                .from("c", "r")
                .using(ChinaCity_.T)
                .as("c")
                .join(ChinaRegion_.T)
                .as("r").on(ChinaCity_.id::equal, ChinaRegion_.id)
                .where(ChinaRegion_.id::spaceEqual, SQLs::namedParam)
                .and(ChinaRegion_.createTime.between(SQLs::literal, 
                        map.get("startTime"), AND, map.get("endTIme")))
                .asDelete()
                .namedParamList(paramList);

        print80Stmt(LOG, stmt);
    };
}
```

---

## 内部实现原理

### ArmyParamExpression 类层次结构

```
ArmyParamExpression (抽象基类)
├── AnonymousParam (匿名参数)
└── ImmutableNamedParam (命名参数基类)
    └── ImmutableNamedNonNullParam (非空命名参数)
```

### ArmyRowParamExpression 类层次结构

```
ArmyRowParamExpression (抽象基类)
├── AnonymousMultiParam (匿名多行参数)
└── ArmyNamedRowParam (命名多行参数)
```

### 关键方法

**ArmyParamExpression.named()** - 创建命名参数
```java
static ArmyParamExpression named(TypeInfer infer, String name) {
    // 验证参数
    // 创建 ImmutableNamedNonNullParam 实例
}
```

**ArmyRowParamExpression.named()** - 创建命名多行参数
```java
static ArmyRowParamExpression named(TypeInfer infer, String name, int size) {
    // 验证参数
    // 创建 ArmyNamedRowParam 实例
}
```

---

## 最佳实践

1. **优先使用方法引用形式**：`SQLs::namedParam` 最简洁
2. **使用字段名作为参数名**：`SQLs::namedParam, ChinaRegion_.ID` 更清晰
3. **配合 `namedParamList` 使用**：绑定实际参数列表
4. **注意类型推断**：确保 TypeInfer 能正确推断类型
5. **非空 vs 可空**：根据业务需求选择合适的约束

---

## 相关文件位置

| 类/接口 | 文件路径 |
|--------|---------|
| SQLSyntax | `army-core/src/main/java/io/army/criteria/impl/SQLSyntax.java` |
| SQLs | `army-core/src/main/java/io/army/criteria/impl/SQLs.java` |
| ArmyParamExpression | `army-core/src/main/java/io/army/criteria/impl/ArmyParamExpression.java` |
| ArmyRowParamExpression | `army-core/src/main/java/io/army/criteria/impl/ArmyRowParamExpression.java` |
| NamedParam | `army-core/src/main/java/io/army/criteria/NamedParam.java` |
| StandardUpdateUnitTests | `army-example/src/test/java/io/army/criteria/standard/unit/StandardUpdateUnitTests.java` |
| MySQLCriteriaUnitTests | `army-example/src/test/java/io/army/criteria/mysql/unit/MySQLCriteriaUnitTests.java` |

---

## 快速参考速查表

| 方法 | 用途 | 空值 | 适用场景 |
|------|------|------|---------|
| `namedParam(type, name)` | 单值参数 | 非空 | 批量更新/删除/VALUES |
| `namedNullableParam(type, name)` | 单值参数 | 可空 | 批量更新/删除/VALUES |
| `namedRowParam(type, name, size)` | 多行参数 | 非空 | IN 子句等 |
| `namedLiteral(type, name)` | 命名字面量 | 非空 | VALUES 插入 |
| `namedConst(type, name)` | 命名常量 | 非空 | VALUES 插入 |

