---
name: "TypedField-complete-guide"
description: "完整的 TypedField 接口使用指南，包含所有方法、显式参数绑定和结合 SQLs.NULL/DEFAULT/TRUE/FALSE/ASTERISK 常量的使用方法和示例。Invoke when working with TypedField, need to use TypedField methods or SQLs special constants, or working with Army Criteria API's field operations."
---

# TypedField 接口完整使用指南

## 概述

`TypedField` 是 Army Criteria API 的核心接口，它继承自 `SqlField`、`TypedExpression` 和 `SimpleExpression`，提供了类型安全的字段操作。

## 继承关系
- `TypedField` → `SqlField` → `TypedExpression` → `Expression`

---

## 一、接口方法分类

### 1. 从 TypedExpression 继承的方法（显式参数绑定）

这些方法使用显式绑定参数，提供类型安全。

#### 比较操作方法

| 方法名 | 说明 | 示例 |
|--------|------|------|
| `equal(BiFunction, T)` | 等于 | `field.equal(SQLs::param, value)` |
| `notEqual(BiFunction, T)` | 不等于 | `field.notEqual(SQLs::literal, value)` |
| `nullSafeEqual(BiFunction, T)` | 空安全等于 | `field.nullSafeEqual(SQLs::const, value)` |
| `less(BiFunction, T)` | 小于 | `field.less(SQLs::param, value)` |
| `lessEqual(BiFunction, T)` | 小于等于 | `field.lessEqual(SQLs::literal, value)` |
| `greater(BiFunction, T)` | 大于 | `field.greater(SQLs::param, value)` |
| `greaterEqual(BiFunction, T)` | 大于等于 | `field.greaterEqual(SQLs::literal, value)` |

#### 范围操作方法

| 方法名 | 说明 | 示例 |
|--------|------|------|
| `between(BiFunction, T, AND, T)` | 范围 | `field.between(SQLs::param, v1, AND, v2)` |
| `between(BiFunction, T, AND, BiFunction, U)` | 双函数范围 | `field.between(SQLs::param, v1, AND, SQLs::literal, v2)` |
| `between(modifier, BiFunction, T, AND, T)` | 带修饰符范围 | `field.between(SYMMETRIC, SQLs::param, v1, AND, v2)` |
| `notBetween(BiFunction, T, AND, T)` | 不在范围内 | `field.notBetween(SQLs::param, v1, AND, v2)` |

#### LIKE/SIMILAR TO 方法

| 方法名 | 说明 | 示例 |
|--------|------|------|
| `like(BiFunction, T)` | 模糊匹配 | `field.like(SQLs::param, pattern)` |
| `like(BiFunction, T, ESCAPE, escapeChar)` | 带转义符模糊匹配 | `field.like(SQLs::param, pattern, ESCAPE, '\\')` |
| `notLike(BiFunction, T)` | 不匹配 | `field.notLike(SQLs::literal, pattern)` |
| `similarTo(BiFunction, T)` | Postgres 相似匹配 | `field.similarTo(SQLs::param, pattern)` |

#### IN 操作方法

| 方法名 | 说明 | 示例 |
|--------|------|------|
| `in(BiFunction, Collection)` | 在集合中 | `field.in(SQLs::rowParam, list)` |
| `in(TeNamedParamsFunc, String, int)` | 命名参数 IN | `field.in(SQLs::namedRowParam, "ids", 5)` |
| `notIn(BiFunction, Collection)` | 不在集合中 | `field.notIn(SQLs::rowLiteral, list)` |

#### 算术运算方法

| 方法名 | 说明 | 示例 |
|--------|------|------|
| `mod(BiFunction, T)` | 取模 | `field.mod(SQLs::param, 2)` |
| `times(BiFunction, T)` | 乘法 | `field.times(SQLs::literal, 10)` |
| `plus(BiFunction, T)` | 加法 | `field.plus(SQLs::param, value)` |
| `minus(BiFunction, T)` | 减法 | `field.minus(SQLs::literal, 5)` |
| `divide(BiFunction, T)` | 除法 | `field.divide(SQLs::param, 2)` |

#### 位运算方法

| 方法名 | 说明 | 示例 |
|--------|------|------|
| `bitwiseAnd(BiFunction, T)` | 按位与 | `field.bitwiseAnd(SQLs::param, 0xFF)` |
| `bitwiseOr(BiFunction, T)` | 按位或 | `field.bitwiseOr(SQLs::literal, 0x0F)` |
| `bitwiseXor(BiFunction, T)` | 按位异或 | `field.bitwiseXor(SQLs::param, mask)` |
| `rightShift(BiFunction, T)` | 右移 | `field.rightShift(SQLs::literal, 2)` |
| `leftShift(BiFunction, T)` | 左移 | `field.leftShift(SQLs::param, 1)` |

#### 向量距离方法（PostgreSQL 专属）

| 方法名 | 说明 | 示例 |
|--------|------|------|
| `l1Distance(BiFunction, T)` | L1 距离 | `field.l1Distance(SQLs::param, vec)` |
| `l2Distance(BiFunction, T)` | L2 距离 | `field.l2Distance(SQLs::literal, vec)` |
| `cosineDistance(BiFunction, T)` | 余弦距离 | `field.cosineDistance(SQLs::param, vec)` |
| `hammingDistance(BiFunction, T)` | 汉明距离 | `field.hammingDistance(SQLs::literal, vec)` |
| `jaccardDistance(BiFunction, T)` | 杰卡德距离 | `field.jaccardDistance(SQLs::param, vec)` |
| `negDot(BiFunction, T)` | 负内积 | `field.negDot(SQLs::literal, vec)` |

#### 方言操作方法

| 方法名 | 说明 |
|--------|------|
| `space(DualOperator, BiFunction, T)` | 双操作符表达式 |
| `space(BiOperator, BiFunction, T)` | 双操作符谓词 |
| `space(BiOperator, BiFunction, T, modifier, optional)` | 带修饰符的双操作符谓词 |

---

### 2. 从 Expression 继承的方法（隐式参数绑定）

这些方法接受任意对象作为操作数，自动转换为表达式。

| 方法名 | 说明 | 示例 |
|--------|------|------|
| `equal(Object)` | 等于 | `field.equal(value)` |
| `notEqual(Object)` | 不等于 | `field.notEqual(otherField)` |
| `nullSafeEqual(Object)` | 空安全等于 | `field.nullSafeEqual(SQLs.NULL)` |
| `less(Object)` | 小于 | `field.less(100)` |
| `lessEqual(Object)` | 小于等于 | `field.lessEqual(SQLs.literal(100))` |
| `greater(Object)` | 大于 | `field.greater(otherField)` |
| `greaterEqual(Object)` | 大于等于 | `field.greaterEqual(SQLs.NULL)` |
| `between(Object, AND, Object)` | 范围 | `field.between(v1, AND, v2)` |
| `notBetween(Object, AND, Object)` | 不在范围内 | `field.notBetween(a, AND, b)` |
| `is(TRUE/FALSE/UNKNOWN/NULL)` | 布尔检查 | `field.is(TRUE)` |
| `isNot(TRUE/FALSE/UNKNOWN/NULL)` | 布尔检查否定 | `field.isNot(FALSE)` |
| `isNull()` | 为空 | `field.isNull()` |
| `isNotNull()` | 不为空 | `field.isNotNull()` |
| `is(DISTINCT_FROM, Object)` | 不等于 | `field.is(DISTINCT_FROM, value)` |
| `isNot(DISTINCT_FROM, Object)` | 等于 | `field.isNot(DISTINCT_FROM, other)` |
| `like(Object)` | 模糊匹配 | `field.like("%abc%")` |
| `notLike(Object)` | 不匹配 | `field.notLike(pattern)` |
| `similarTo(Object)` | Postgres 相似 | `field.similarTo("a%")` |
| `equalAny/equalSome/equalAll(SQLValueList)` | ANY/SOME/ALL 匹配 |
| `lessAny/lessSome/lessAll(SQLValueList)` | ANY/SOME/ALL 比较 |
| `in(SQLValueList)` | 在列表中 | `field.in(list)` |
| `notIn(SQLValueList)` | 不在列表中 | `field.notIn(list)` |
| `mod(Object)` | 取模 | `field.mod(2)` |
| `times(Object)` | 乘法 | `field.times(10)` |
| `plus(Object)` | 加法 | `field.plus(other)` |
| `minus(Object)` | 减法 | `field.minus(5)` |
| `divide(Object)` | 除法 | `field.divide(2)` |
| `bitwiseAnd(Object)` | 按位与 | `field.bitwiseAnd(0xFF)` |
| `bitwiseOr(Object)` | 按位或 | `field.bitwiseOr(0x0F)` |
| `bitwiseXor(Object)` | 按位异或 | `field.bitwiseXor(mask)` |
| `rightShift(Object)` | 右移 | `field.rightShift(2)` |
| `leftShift(Object)` | 左移 | `field.leftShift(1)` |

---

### 3. TypedField 专属空间方法（批处理专用）

这些方法专用于批处理语句，自动使用字段名作为参数名。

#### 比较操作（空间方法）

| 方法名 | 说明 | 示例 |
|--------|------|------|
| `spaceEqual(BiFunction)` | 等于 | `field.spaceEqual(SQLs::namedParam)` |
| `spaceNotEqual(BiFunction)` | 不等于 | `field.spaceNotEqual(SQLs::namedLiteral)` |
| `spaceNullSafeEqual(BiFunction)` | 空安全等于 | `field.spaceNullSafeEqual(SQLs::namedConst)` |
| `spaceLess(BiFunction)` | 小于 | `field.spaceLess(SQLs::namedParam)` |
| `spaceLessEqual(BiFunction)` | 小于等于 | `field.spaceLessEqual(SQLs::namedLiteral)` |
| `spaceGreater(BiFunction)` | 大于 | `field.spaceGreater(SQLs::namedParam)` |
| `spaceGreaterEqual(BiFunction)` | 大于等于 | `field.spaceGreaterEqual(SQLs::namedConst)` |

#### LIKE/SIMILAR TO（空间方法）

| 方法名 | 说明 | 示例 |
|--------|------|------|
| `spaceLike(BiFunction)` | 模糊匹配 | `field.spaceLike(SQLs::namedParam)` |
| `spaceLike(BiFunction, ESCAPE, escapeChar)` | 带转义符模糊匹配 | `field.spaceLike(SQLs::namedParam, ESCAPE, '\\')` |
| `spaceNotLike(BiFunction)` | 不匹配 | `field.spaceNotLike(SQLs::namedLiteral)` |
| `spaceNotLike(BiFunction, ESCAPE, escapeChar)` | 不匹配带转义符 | `field.spaceNotLike(SQLs::namedParam, ESCAPE, '\\')` |
| `spaceSimilarTo(BiFunction)` | 相似匹配 | `field.spaceSimilarTo(SQLs::namedParam)` |
| `spaceSimilarTo(BiFunction, ESCAPE, escapeChar)` | 相似匹配带转义符 | `field.spaceSimilarTo(SQLs::namedLiteral, ESCAPE, '\\')` |
| `spaceNotSimilarTo(BiFunction)` | 不相似 | `field.spaceNotSimilarTo(SQLs::namedParam)` |
| `spaceNotSimilarTo(BiFunction, ESCAPE, escapeChar)` | 不相似带转义符 | `field.spaceNotSimilarTo(SQLs::namedLiteral, ESCAPE, '\\')` |

#### IN 操作（空间方法）

| 方法名 | 说明 | 示例 |
|--------|------|------|
| `spaceIn(BiFunction)` | 在集合中 | `field.spaceIn(SQLs::namedRowLiteral)` |
| `spaceNotIn(BiFunction)` | 不在集合中 | `field.spaceNotIn(SQLs::namedRowConst)` |
| `spaceIn(TeNamedParamsFunc, int)` | 命名参数 IN | `field.spaceIn(SQLs::namedRowParam, 5)` |
| `spaceNotIn(TeNamedParamsFunc, int)` | 命名参数 NOT IN | `field.spaceNotIn(SQLs::namedRowParam, 5)` |

#### 算术运算（空间方法）

| 方法名 | 说明 |
|--------|------|
| `spaceMod(BiFunction)` | 取模 |
| `spacePlus(BiFunction)` | 加法 |
| `spaceMinus(BiFunction)` | 减法 |
| `spaceTimes(BiFunction)` | 乘法 |
| `spaceDivide(BiFunction)` | 除法 |

#### 位运算（空间方法）

| 方法名 | 说明 |
|--------|------|
| `spaceBitwiseAnd(BiFunction)` | 按位与 |
| `spaceBitwiseOr(BiFunction)` | 按位或 |
| `spaceXor(BiFunction)` | 按位异或 |
| `spaceRightShift(BiFunction)` | 右移 |
| `spaceLeftShift(BiFunction)` | 左移 |

#### 向量距离（空间方法，PostgreSQL 专属）

| 方法名 | 说明 |
|--------|------|
| `spaceL1Distance(BiFunction)` | L1 距离 |
| `spaceL2Distance(BiFunction)` | L2 距离 |
| `spaceCosineDistance(BiFunction)` | 余弦距离 |
| `spaceHammingDistance(BiFunction)` | 汉明距离 |
| `spaceJaccardDistance(BiFunction)` | 杰卡德距离 |
| `spaceNegDot(BiFunction)` | 负内积 |

#### 方言操作（空间方法）

| 方法名 | 说明 |
|--------|------|
| `spaceSpace(DualOperator, BiFunction)` | 双操作符表达式 |
| `spaceSpace(BiOperator, BiFunction)` | 双操作符谓词 |
| `spaceSpace(BiOperator, BiFunction, modifier, optional)` | 带修饰符的双操作符谓词 |

---

## 二、SQLs 特殊常量使用

### 1. SQLs.NULL
表示 SQL 的 NULL 值。

```java
// 显式参数绑定
field.equal(SQLs::param, (Object) null)

// 隐式参数绑定
field.equal(SQLs.NULL)

// 检查 NULL
field.isNull()
field.isNotNull()
field.is(SQLs.NULL)
field.isNot(SQLs.NULL)
```

### 2. SQLs.DEFAULT
表示 SQL 的 DEFAULT 值。

```java
// INSERT/UPDATE 中使用
.set(field, SQLs.DEFAULT)
.defaultValue(field, SQLs.DEFAULT)
```

### 3. SQLs.TRUE / SQLs.FALSE
布尔值常量。

```java
// 比较
field.is(SQLs.TRUE)
field.isNot(SQLs.FALSE)

// 作为值
.set(field, SQLs.TRUE)
.where(field.equal(SQLs::param, true))
```

### 4. SQLs.ASTERISK
表示 SELECT 通配符 *。

```java
// SELECT 所有列
.select("t", PERIOD, ASTERISK)
.select(tableAlias, PERIOD, ASTERISK)
```

---

## 三、完整示例

### 示例 1: 基础查询（显式参数绑定

```java
final Select stmt = SQLs.query()
    .select(ChinaRegion_.id, ChinaRegion_.name)
    .from(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.id.equal(SQLs::literal, 1L))
    .and(ChinaRegion_.regionGdp.greater(SQLs::param, new BigDecimal("1000.00")))
    .and(ChinaRegion_.visible.is(SQLs.TRUE))
    .asQuery();
```

### 示例 2: 范围查询

```java
final Select stmt = SQLs.query()
    .select("c", PERIOD, ASTERISK)
    .from(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.regionGdp.between(SQLs::literal, new BigDecimal("100"), AND, new BigDecimal("1000")))
    .and(ChinaRegion_.name.like(SQLs::param, "%Region%"))
    .and(ChinaRegion_.parentId.isNull())
    .asQuery();
```

### 示例 3: 批处理更新（空间方法）

```java
final BatchUpdate stmt = Postgres.batchSingleUpdate()
    .update(ChinaRegion_.T, AS, "c")
    .set(ChinaRegion_.name, SQLs::param, "New Name")
    .setSpace(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::namedParam)
    .where(ChinaRegion_.id::spaceEqual, SQLs::namedParam)
    .asUpdate()
    .namedParamList(paramList);
```

### 示例 4: 带 DEFAULT 值插入

```java
final Insert stmt = SQLs.singleInsert()
    .insertInto(ChinaRegion_.T)
    .defaultValue(ChinaRegion_.visible, SQLs.TRUE)
    .defaultValue(ChinaRegion_.createTime, SQLs.DEFAULT)
    .values()
    .parens(s -> s
        .space(ChinaRegion_.name, SQLs::param, "Beijing")
        .comma(ChinaRegion_.regionGdp, SQLs::literal, new BigDecimal("10000.00"))
    )
    .asInsert();
```

### 示例 5: IN 操作

```java
// 单个 IN
List<Long> idList = Arrays.asList(1L, 2L, 3L);

final Select stmt = SQLs.query()
    .select(ChinaRegion_.id)
    .from(ChinaRegion_.T, AS, "c")
    .where(ChinaRegion_.id.in(SQLs::rowParam, idList))
    .asQuery();
```

---

## 四、参数绑定函数

### 常见绑定函数

| 函数 | 说明 | 用途 |
|------|------|
| `SQLs::param` | 参数化绑定（推荐） | 安全，防止 SQL 注入 |
| `SQLs::literal` | 字面量绑定 | 常量值 |
| `SQLs::const` | 常量绑定 | 编译时常量 |
| `SQLs::namedParam` | 命名参数绑定 | 批处理 |
| `SQLs::namedLiteral` | 命名字面量绑定 | 批处理常量 |
| `SQLs::namedConst` | 命名常量绑定 | 批处理编译时常量 |
| `SQLs::rowParam` | 行参数绑定 | IN 子句 |
| `SQLs::rowLiteral` | 行字面量绑定 | IN 子句常量 |
| `SQLs::namedRowParam` | 命名行参数绑定 | 批处理 IN 子句 |
| `SQLs::namedRowLiteral` | 命名行字面量绑定 | 批处理 IN 子句常量 |

---

## 五、排序相关方法

| 方法名 | 说明 | 示例 |
|--------|------|------|
| `asSortItem()` | 转换为排序项 | `field.asSortItem()` |
| `asc()` | 升序 | `field.asc()` |
| `desc()` | 降序 | `field.desc()` |
| `ascSpace(NullsFirstLast)` | 升序带 NULL 处理 | `field.ascSpace(NULLS_FIRST)` |
| `descSpace(NullsFirstLast)` | 降序带 NULL 处理 | `field.descSpace(NULLS_LAST)` |

---

## 六、数组切片方法（H2/PostgreSQL 专属）

```java
// 一维数组切片
field.slice(lower, COLON, upper)
field.slice(SQLs::param, 1, COLON, 10)
field.slice(lower, COLON)  // 无上限
field.slice(COLON, upper)  // 无下限
field.slice(COLON)  // 全部

// 二维数组切片
field.slice(lower1, COLON, upper1, lower2, COLON, upper2)

// 多维数组切片
field.sliceAtSubs(indexList)
```

---

## 七、点操作符方法

```java
// 访问复合类型
field.dot(key)
field.dot(key1, key2)
field.dot(key1, key2, key3)
field.dot(key1, key2, key3, key4)
field.dots(subscriptList)
```

---

## 八、括号操作符方法

```java
// JSON 下标访问
field.bracket(key)
field.bracket(key1, key2)
field.bracket(key1, key2, key3)
field.bracket(key1, key2, key3, key4)
field.brackets(subscriptList)

// 显式参数绑定版本
field.bracket(SQLs::param, key)
field.brackets(SQLs::param, subscriptList)
```

---

## 九、类型转换方法

```java
// 映射到已知类型（不支持编解码）
TypedExpression expr = field.mapTo(mappingType)
ArrayExpression arr = field.mapToArray(arrayMappingType)

// 强制类型转换（PostgreSQL）
TypedExpression cast = field.castTo(mappingType)
ArrayExpression castArr = field.castToArray(arrayMappingType)
```

---

## 十、其他 Expression 继承的其他方法

参考 Expression 接口的完整方法列表：
- 数组切片方法
- 点操作符方法
- 括号操作符方法
- 向量距离方法
- 类型转换方法

---

## 注意事项

1. **空间方法仅用于批处理语句，自动使用字段名作为参数名
2. **显式参数绑定提供更好的类型安全性
3. **隐式参数绑定更灵活，但类型检查在运行时
4. **TRUE/FALSE/NULL/ASTERISK/DEFAULT 是 SQL 级别的特殊常量
5. **空间方法和普通方法可以混合使用，但要注意上下文
6. **方言特定方法（如 PostgreSQL 的类似向量距离）仅在特定数据库中可用
