---
name: "expression-interface-usage"
description: "学习和学习 Expression 接口的声明和实现，掌握 Expression 的使用和方法的隐式参数绑定，结合 SQLs.NULL、SQLs.DEFAULT、SQLs.TRUE、SQLs.FALSE、SQLs.ASTERISK 和项目示例代码，学习与其他 skill 的配合使用。"
---

# Expression 接口学习指南

## 概述

本技能基于 Army 框架的 Expression 接口及其相关实现的完整学习指南。涵盖从源码级别理解 Expression、TypedExpression、TypedField 等接口的方法、用法、使用场景，以及与 SQLs 常量的配合使用。

---

## 核心接口层次

### Expression.java 接口层次

```
Expression (根接口)
├── SimpleExpression
│   ├── SqlField
│   ├── TypedExpression
│   └── TypedField
├── CompoundExpression
└── IPredicate
```

---

## Expression 接口完整方法清单

### 比较操作符

| 方法 | 参数类型 | 返回值 | 说明 |
|------|----------|------|------|
| `equal(Object)` | `Object` | `IPredicate` | `=` 等于操作 |
| `notEqual(Object)` | `Object` | `IPredicate` | `!=` 不等于操作 |
| `nullSafeEqual(Object)` | `Object` | `IPredicate` | `&lt;=&gt;` 安全等于 (MySQL/PostgreSQL/H2) |
| `less(Object)` | `Object` | `IPredicate` | `<` 小于操作 |
| `lessEqual(Object)` | `Object` | `IPredicate` | `<=` 小于等于操作 |
| `greater(Object)` | `Object` | `IPredicate` | `>` 大于操作 |
| `greaterEqual(Object)` | `Object` | `IPredicate` | `>=` 大于等于操作 |

### BETWEEN 操作

| 方法 | 说明 |
|------|------|
| `between(Object, SQLs.WordAnd, Object)` | 标准 BETWEEN |
| `notBetween(Object, SQLs.WordAnd, Object)` | 标准 NOT BETWEEN |
| `between(SQLs.BetweenModifier, Object, SQLs.WordAnd, Object)` | PostgreSQL/H2 带修饰符 BETWEEN |
| `notBetween(SQLs.BetweenModifier, Object, SQLs.WordAnd, Object)` | PostgreSQL/H2 带修饰符 NOT BETWEEN |

### IS 测试

| 方法 | 说明 |
|------|------|
| `is(SQLs.BoolTestWord)` | IS TRUE/IS FALSE/IS NULL/IS UNKNOWN |
| `isNot(SQLs.BoolTestWord)` | IS NOT TRUE/IS NOT FALSE/IS NOT NULL/IS NOT UNKNOWN |
| `isNull()` | IS NULL |
| `isNotNull()` | IS NOT NULL |
| `is(SQLs.IsComparisonWord, Object)` | PostgreSQL/H2: IS DISTINCT FROM |
| `isNot(SQLs.IsComparisonWord, Object)` | PostgreSQL/H2: IS NOT DISTINCT FROM |

### LIKE/SIMILAR TO

| 方法 | 说明 |
|------|------|
| `like(Object)` | LIKE 模式匹配 |
| `like(Object, SQLs.WordEscape, Object)` | LIKE 带 ESCAPE 字符 |
| `notLike(Object)` | NOT LIKE |
| `notLike(Object, SQLs.WordEscape, Object)` | NOT LIKE 带 ESCAPE |
| `similarTo(Object)` | PostgreSQL SIMILAR TO |
| `similarTo(Object, SQLs.WordEscape, Object)` | PostgreSQL SIMILAR TO 带 ESCAPE |
| `notSimilarTo(Object)` | PostgreSQL NOT SIMILAR TO |
| `notSimilarTo(Object, SQLs.WordEscape, Object)` | PostgreSQL NOT SIMILAR TO 带 ESCAPE |

### ANY/SOME/ALL 操作

| 方法 | 说明 |
|------|------|
| `equalAny(SQLValueList)` | `= ANY` |
| `equalSome(SQLValueList)` | `= SOME` |
| `equalAll(SQLValueList)` | `= ALL` |
| `notEqualAny(SQLValueList)` | `!= ANY` |
| `notEqualSome(SQLValueList)` | `!= SOME` |
| `notEqualAll(SQLValueList)` | `!= ALL` |
| `lessAny(SQLValueList)` | `< ANY` |
| `lessSome(SQLValueList)` | `< SOME` |
| `lessAll(SQLValueList)` | `< ALL` |
| `lessEqualAny(SQLValueList)` | `<= ANY` |
| `lessEqualSome(SQLValueList)` | `<= SOME` |
| `lessEqualAll(SQLValueList)` | `<= ALL` |
| `greaterAny(SQLValueList)` | `> ANY` |
| `greaterSome(SQLValueList)` | `> SOME` |
| `greaterAll(SQLValueList)` | `> ALL` |
| `greaterEqualAny(SQLValueList)` | `>= ANY` |
| `greaterEqualSome(SQLValueList)` | `>= SOME` |
| `greaterEqualAll(SQLValueList)` | `>= ALL` |

### IN 操作

| 方法 | 说明 |
|------|------|
| `in(SQLValueList)` | IN |
| `notIn(SQLValueList)` | NOT IN |

### 算术运算

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `mod(Object)` | `Expression` | 取模 `%` |
| `times(Object)` | `Expression` | 乘法 `*` |
| `plus(Object)` | `Expression` | 加法 `+` |
| `minus(Object)` | `Expression` | 减法 `-` |
| `divide(Object)` | `Expression` | 除法 `/` |

### 位运算

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `bitwiseAnd(Object)` | `Expression` | 按位与 `&` |
| `bitwiseOr(Object)` | `Expression` | 按位或 `|` |
| `bitwiseXor(Object)` | `Expression` | 按位异或 `^` |
| `rightShift(Object)` | `Expression` | 右移 `>>` |
| `leftShift(Object)` | `Expression` | 左移 `<<` |

### 数组切片（PostgreSQL/H2）

| 方法 | 说明 |
|------|
| `slice(Object, SQLs.SymbolColon, Object)` | 一维数组切片 |
| `slice(BiFunction, int, SQLs.SymbolColon, int)` | 一维数组切片带函数引用 |
| `slice(Object, SQLs.SymbolColon)` | 切片无上界 |
| `slice(SQLs.SymbolColon, Object)` | 切片无下界 |
| `slice(SQLs.SymbolColon)` | 全切片 |
| `slice(...)` | 多维数组切片 |
| `sliceAtSubs(List)` | 多维数组切片 |

### 点操作

| 方法 | 说明 |
|------|
| `dot(Object)` | 点操作访问 |
| `dot(Object, Object)` | 点操作访问两级 |
| `dot(Object, Object, Object)` | 点操作访问三级 |
| `dot(Object, Object, Object, Object)` | 点操作访问四级 |
| `dots(List)` | 点操作访问列表 |

### 方括号操作

| 方法 | 说明 |
|------|
| `bracket(Object)` | 方括号访问 |
| `bracket(Object, Object)` | 方括号访问两级 |
| `bracket(Object, Object, Object)` | 方括号访问三级 |
| `bracket(Object, Object, Object, Object)` | 方括号访问四级 |
| `brackets(List)` | 方括号访问列表 |
| `bracket(BiFunction, Object)` | 方括号访问带函数引用 |
| `brackets(BiFunction, List)` | 方括号访问列表带函数引用 |

### 向量距离操作（PostgreSQL/pgvector）

| 方法 | 说明 |
|------|
| `l1Distance(Object)` | L1 距离 `<+>` |
| `l2Distance(Object)` | L2 距离 `<->` |
| `cosineDistance(Object)` | 余弦距离 `<=>` |
| `hammingDistance(Object)` | 汉明距离 `<~>` |
| `jaccardDistance(Object)` | 雅卡德距离 `<%>` |
| `negDot(Object)` | 负内积 `<#>` |

### 类型转换

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `mapTo(MappingType)` | `TypedExpression` | 映射到已知类型 |
| `mapToArray(ArrayMappingType)` | `ArrayExpression` | 映射到数组类型 |
| `castTo(MappingType)` | `TypedExpression` | PostgreSQL 类型转换 |
| `castToArray(ArrayMappingType)` | `ArrayExpression` | PostgreSQL 数组类型转换 |

### 排序

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `asSortItem()` | `SortItem` | 作为排序项 |
| `asc()` | `SortItem` | 升序排序 |
| `desc()` | `SortItem` | 降序排序 |
| `ascSpace(SQLs.NullsFirstLast)` | `SortItem` | 升序带 NULL 处理 |
| `descSpace(SQLs.NullsFirstLast)` | `SortItem` | 降序带 NULL 处理 |

### 方言操作符

| 方法 | 说明 |
|------|
| `space(SQLs.DualOperator, Object)` | 方言双值操作符 |
| `space(SQLs.BiOperator, Object)` | 方言双值谓词 |
| `space(SQLs.BiOperator, Object, SQLToken, Object)` | 方言双值谓词带修饰符 |

---

## TypedExpression 接口方法

TypedExpression 继承 Expression 接口，提供了带函数引用（BiFunction）重载版本。

### TypedExpression 重载方法模式

```java
// 比较操作符重载版本
<T> IPredicate equal(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> IPredicate notEqual(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> IPredicate nullSafeEqual(BiFunction<TypedExpression, T, Expression> funcRef, @Nullable T value);
<T> IPredicate less(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> IPredicate lessEqual(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> IPredicate greater(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> IPredicate greaterEqual(BiFunction<TypedExpression, T, Expression> funcRef, T value);

// BETWEEN 操作重载版本
<T> IPredicate between(BiFunction<TypedExpression, T, Expression> funcRef, T first, SQLs.WordAnd and, T second);
<T, U> IPredicate between(BiFunction<TypedExpression, T, Expression> firstFuncRef, T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second);
<T> IPredicate notBetween(BiFunction<TypedExpression, T, Expression> funcRef, T first, SQLs.WordAnd and, T second);
<T, U> IPredicate notBetween(BiFunction<TypedExpression, T, Expression> firstFuncRef, T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second);

// 带修饰符 BETWEEN
@Support({PostgreSQL, H2})
<T> IPredicate between(@Nullable SQLs.BetweenModifier modifier, BiFunction<TypedExpression, T, Expression> funcRef, T first, SQLs.WordAnd and, T second);
<T, U> IPredicate between(@Nullable SQLs.BetweenModifier modifier, BiFunction<TypedExpression, T, Expression> firstFuncRef, T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second);
@Support({PostgreSQL, H2})
<T> IPredicate notBetween(@Nullable SQLs.BetweenModifier modifier, BiFunction<TypedExpression, T, Expression> funcRef, T first, SQLs.WordAnd and, T second);
<T, U> IPredicate notBetween(@Nullable SQLs.BetweenModifier modifier, BiFunction<TypedExpression, T, Expression> firstFuncRef, T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second);

// IS DISTINCT FROM 重载
@Support({PostgreSQL, H2})
<T> IPredicate is(SQLs.IsComparisonWord operator, BiFunction<TypedExpression, T, Expression> funcRef, @Nullable T value);
@Support({PostgreSQL, H2})
<T> IPredicate isNot(SQLs.IsComparisonWord operator, BiFunction<TypedExpression, T, Expression> funcRef, @Nullable T value);

// IN/ NOT IN 重载
IPredicate in(BiFunction<TypedExpression, Collection<?>, RowExpression> funcRef, Collection<?> value);
IPredicate notIn(BiFunction<TypedExpression, Collection<?>, RowExpression> funcRef, Collection<?> value);
IPredicate in(TeNamedParamsFunc<TypedExpression> funcRef, String paramName, int size);
IPredicate notIn(TeNamedParamsFunc<TypedExpression> funcRef, String paramName, int size);

// 算术运算重载
<T> Expression mod(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression times(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression plus(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression minus(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression divide(BiFunction<TypedExpression, T, Expression> funcRef, T value);

// 位运算重载
<T> Expression bitwiseAnd(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression bitwiseOr(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression bitwiseXor(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression rightShift(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression leftShift(BiFunction<TypedExpression, T, Expression> funcRef, T value);

// LIKE/SIMILAR TO 重载
<T> IPredicate like(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> IPredicate like(BiFunction<TypedExpression, T, Expression> funcRef, T value, SQLs.WordEscape escape, T escapeChar);
<T> IPredicate notLike(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> IPredicate notLike(BiFunction<TypedExpression, T, Expression> funcRef, T value, SQLs.WordEscape escape, T escapeChar);
@Support({PostgreSQL})
<T> IPredicate similarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value);
@Support({PostgreSQL})
<T> IPredicate similarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value, SQLs.WordEscape escape, T escapeChar);
@Support({PostgreSQL})
<T> IPredicate notSimilarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value);
@Support({PostgreSQL})
<T> IPredicate notSimilarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value, SQLs.WordEscape escape, T escapeChar);

// 向量距离操作重载（PostgreSQL）
@Support({PostgreSQL})
<T> Expression l1Distance(BiFunction<TypedExpression, T, Expression> funcRef, T value);
@Support({PostgreSQL})
<T> Expression l2Distance(BiFunction<TypedExpression, T, Expression> funcRef, T value);
@Support({PostgreSQL})
<T> Expression cosineDistance(BiFunction<TypedExpression, T, Expression> funcRef, T value);
@Support({PostgreSQL})
<T> Expression hammingDistance(BiFunction<TypedExpression, T, Expression> funcRef, T value);
@Support({PostgreSQL})
<T> Expression jaccardDistance(BiFunction<TypedExpression, T, Expression> funcRef, T value);
@Support({PostgreSQL})
<T> Expression negDot(BiFunction<TypedExpression, T, Expression> funcRef, T value);

// 方言操作符重载
<T> Expression space(SQLs.DualOperator operator, BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> IPredicate space(SQLs.BiOperator operator, BiFunction<TypedExpression, T, Expression> funcRef, T right);
<T> IPredicate space(SQLs.BiOperator operator, BiFunction<StringType, T, Expression> funcRef, T right, SQLToken modifier, T optionalExp);
```

---

## TypedField 接口方法

TypedField 继承 TypedExpression 和 SqlField，为批量语句提供 space* 系列方法。

### space* 系列方法模式

```java
// 批量语句的 space 方法
IPredicate spaceEqual(BiFunction<TypedField, String, Expression> namedOperator);
IPredicate spaceNotEqual(BiFunction<TypedField, String, Expression> namedOperator);
IPredicate spaceNullSafeEqual(BiFunction<TypedField, String, Expression> namedOperator);
IPredicate spaceLess(BiFunction<TypedField, String, Expression> namedOperator);
IPredicate spaceLessEqual(BiFunction<TypedField, String, Expression> namedOperator);
IPredicate spaceGreater(BiFunction<TypedField, String, Expression> namedOperator);
IPredicate spaceGreaterEqual(BiFunction<TypedField, String, Expression> namedOperator);

// LIKE/SIMILAR TO
IPredicate spaceLike(BiFunction<TypedField, String, Expression> namedOperator);
IPredicate spaceLike(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar);
IPredicate spaceNotLike(BiFunction<TypedField, String, Expression> namedOperator);
IPredicate spaceNotLike(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar);
@Support({PostgreSQL})
IPredicate spaceSimilarTo(BiFunction<TypedField, String, Expression> namedOperator);
@Support({PostgreSQL})
IPredicate spaceSimilarTo(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar);
@Support({PostgreSQL})
IPredicate spaceNotSimilarTo(BiFunction<TypedField, String, Expression> namedOperator);
@Support({PostgreSQL})
IPredicate spaceNotSimilarTo(BiFunction<TypedField, String, Expression> namedOperator, SQLs.WordEscape escape, Object escapeChar);

// IN
IPredicate spaceIn(BiFunction<TypedField, String, RowExpression> namedOperator);
IPredicate spaceNotIn(BiFunction<TypedField, String, RowExpression> namedOperator);
IPredicate spaceIn(TeNamedParamsFunc<TypedField> namedOperator, int size);
IPredicate spaceNotIn(TeNamedParamsFunc<TypedField> namedOperator, int size);

// 算术运算
Expression spaceMod(BiFunction<TypedField, String, Expression> namedOperator);
Expression spacePlus(BiFunction<TypedField, String, Expression> namedOperator);
Expression spaceMinus(BiFunction<TypedField, String, Expression> namedOperator);
Expression spaceTimes(BiFunction<TypedField, String, Expression> namedOperator);
Expression spaceDivide(BiFunction<TypedField, String, Expression> namedOperator);

// 位运算
Expression spaceBitwiseAnd(BiFunction<TypedField, String, Expression> namedOperator);
Expression spaceBitwiseOr(BiFunction<TypedField, String, Expression> namedOperator);
Expression spaceXor(BiFunction<TypedField, String, Expression> namedOperator);
Expression spaceRightShift(BiFunction<TypedField, String, Expression> namedOperator);
Expression spaceLeftShift(BiFunction<TypedField, String, Expression> namedOperator);

// 向量距离（PostgreSQL）
@Support({PostgreSQL})
Expression spaceL1Distance(BiFunction<TypedField, String, Expression> namedOperator);
@Support({PostgreSQL})
Expression spaceL2Distance(BiFunction<TypedField, String, Expression> namedOperator);
@Support({PostgreSQL})
Expression spaceCosineDistance(BiFunction<TypedField, String, Expression> namedOperator);
@Support({PostgreSQL})
Expression spaceHammingDistance(BiFunction<TypedField, String, Expression> namedOperator);
@Support({PostgreSQL})
Expression spaceJaccardDistance(BiFunction<TypedField, String, Expression> namedOperator);
@Support({PostgreSQL})
Expression spaceNegDot(BiFunction<TypedField, String, Expression> namedOperator);

// 方言操作符
Expression spaceSpace(SQLs.DualOperator operator, BiFunction<TypedField, String, Expression> namedOperator);
IPredicate spaceSpace(SQLs.BiOperator operator, BiFunction<TypedField, String, Expression> namedOperator);
IPredicate spaceSpace(SQLs.BiOperator operator, BiFunction<TypedField, String, Expression> namedOperator, SQLToken modifier, Object optionalExp);
```

---

## SQLs 核心常量

### NULL/DEFAULT/TRUE/FALSE/ASTERISK

| 常量 | 类型 | 说明 |
|------|------|------|
| `SQLs.NULL` | `WordNull` | SQL NULL 关键字 |
| `SQLs.DEFAULT` | `WordDefault` | SQL DEFAULT 关键字 |
| `SQLs.TRUE` | `WordBoolean` | SQL TRUE 关键字 |
| `SQLs.FALSE` | `WordBoolean` | SQL FALSE 关键字 |
| `SQLs.ASTERISK` | `SymbolAsterisk` | SQL `*` 符号 |

### 使用示例

```java
// NULL 示例
// NULL 示例
SELECT * FROM table WHERE column IS SQLs.NULL;
SELECT * FROM table WHERE column IS NOT SQLs.NULL;
```

```java
// TRUE/FALSE 示例
SELECT * FROM table WHERE column IS SQLs.TRUE;
SELECT * FROM table WHERE column IS SQLs.FALSE;
```

```java
// DEFAULT 示例
INSERT INTO table (column1, column2) VALUES (value1, SQLs.DEFAULT);
```

```java
// ASTERISK 示例
SELECT SQLs.ASTERISK FROM table;
SELECT count(SQLs.ASTERISK) FROM table;
```

---

## 实际代码示例

### 基础比较操作

```java
// 标准 Expression 方式
Select stmt = SQLs.query()
    .select(ChinaRegion_.id)
    .from(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.name.equal("北京"))
    .and(ChinaRegion_.id.greater(100))
    .and(ChinaRegion_.regionGdp.lessEqual(new BigDecimal("99999.99")))
    .asQuery();
```

### 使用函数引用（BiFunction）

```java
// TypedExpression 方式
Select stmt = SQLs.query()
    .select(ChinaRegion_.id)
    .from(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.name.equal(SQLs::param, "北京"))
    .and(ChinaRegion_.id.greater(SQLs::literal, 100))
    .and(ChinaRegion_.regionGdp.lessEqual(SQLs::constant, new BigDecimal("99999.99")))
    .asQuery();
```

### BETWEEN 操作

```java
Select stmt = SQLs.query()
    .select(ChinaRegion_.id)
    .from(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.regionGdp.between(SQLs::literal, new BigDecimal("1000"), SQLs.AND, new BigDecimal("2000"))
    .asQuery();
```

### IS NULL/IS NOT NULL

```java
Select stmt = SQLs.query()
    .select(ChinaRegion_.id)
    .from(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.parentId.isNull())
    .and(ChinaRegion_.name.isNotNull())
    .asQuery();
```

### LIKE 操作

```java
Select stmt = SQLs.query()
    .select(ChinaRegion_.id)
    .from(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.name.like("%州%"))
    .and(ChinaRegion_.name.like("%省_"))
    .asQuery();
```

### LIKE 带 ESCAPE 字符

```java
Select stmt = SQLs.query()
    .select(ChinaRegion_.id)
    .from(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.name.like("%\\%%", SQLs.ESCAPE, '\\'))
    .asQuery();
```

### 算术运算

```java
Select stmt = SQLs.query()
    .select(ChinaRegion_.regionGdp.plus(SQLs.literalValue(100)).as("adjusted_gdp"))
    .from(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.regionGdp.times(SQLs.literalValue(2)).greater(SQLs.literalValue(2000)))
    .asQuery();
```

### 位运算

```java
Select stmt = SQLs.query()
    .select(ChinaRegion_.id.bitwiseAnd(SQLs.literalValue(0xFF)).as("masked"))
    .from(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.id.bitwiseOr(SQLs.literalValue(0x01)).greater(SQLs.literalValue(0)))
    .asQuery();
```

### 排序

```java
Select stmt = SQLs.query()
    .select(ChinaRegion_.id)
    .from(ChinaRegion_.T, AS, "c")
    .orderBy(ChinaRegion_.name.asc())
    .and(ChinaRegion_.id.desc())
    .asQuery();
```

### IN 操作

```java
List<Integer> ids = Arrays.asList(1, 2, 3);
Select stmt = SQLs.query()
    .select(ChinaRegion_.id)
    .from(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.id.in(SQLs::rowParam, ids))
    .asQuery();
```

### PostgreSQL SIMILAR TO

```java
Select stmt = Postgres.query()
    .select(ChinaRegion_.id)
    .from(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.name.similarTo(SQLs::constant, "%(北京|上海)%"))
    .and(ChinaRegion_.name.notSimilarTo(SQLs::constant, "%广州%"))
    .asQuery();
```

### 批量语句的 space 方法

```java
List<Map<String, Object>> paramList = Arrays.asList(
    Collections.singletonMap("id", 1),
    Collections.singletonMap("id", 2)
);

// 批量更新
BatchUpdate stmt = SQLs.batchSingleUpdate()
    .update(ChinaRegion_.T, AS, "c")
    .sets(pairs -> pairs
        .setSpace(ChinaRegion_.name, SQLs::namedParam)
    )
    .where(ChinaRegion_.id.spaceEqual(SQLs::namedParam))
    .asUpdate()
    .namedParamList(paramList);
```

---

## 与其他 Skill 配合使用

| Skill 名称 | 配合场景 | 使用方式 |
|-----------|---------|----------|
| SQLs-query-method-chain | 查询构建 | Expression 的方法在查询 WHERE/HAVING 中使用 |
| SQLs-single-update-method-chain | 更新构建 | Expression 作为 SET/ WHERE 使用 |
| SQLs-single-delete-method-chain | 删除构建 | Expression 作为 WHERE 使用 |
| SQLs-domain-update-method-chain | 域对象更新 | Expression 在更新时构建条件 |
| SQLs-domain-delete-method-chain | 域对象删除 | Expression 删除时构建条件 |
| army-example-code | 示例代码 | 提供 Expression 使用示例 |

---

## Skill 进化机制

本 Skill 是活的文档，当发现新用法或框架 API 变更时，请更新本 Skill：

1. 新增方法时添加新条目
2. API 签名变更时更新对应部分
3. 发现更好的示例时添加示例代码
4. 新增方言特性时补充说明
