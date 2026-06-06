---
name: TypedExpression-complete-guide
description: 完整的 TypedExpression 接口学习指南，包括所有方法的使用、参数绑定、SQLs 常量（NULL/DEFAULT/TRUE/FALSE/ASTERISK）的应用，以及与其他 skill 的配合使用
---

# TypedExpression 完整学习指南

## 概述

`TypedExpression` 是 Army Criteria API 的核心接口，继承自 `Expression` 和 `TypeInfer`，提供了类型安全的 SQL 表达式构建能力。它的主要特点是支持 BiFunction 风格的显式参数绑定，提供了灵活的参数化查询方式。

**核心位置**: `/Users/zoro/repositories/trae/java/hub/army/army-core/src/main/java/io/army/criteria/TypedExpression.java`

---

## TypedExpression 接口定义

```java
public interface TypedExpression extends Expression, TypeInfer {
    // 所有方法均支持两种调用风格：
    // 1. 直接传值 (从 Expression 继承)
    // 2. 显式参数绑定 (TypedExpression 特有)
}
```

---

## 一、比较操作符

### 1. equal / notEqual

#### 方法签名
```java
// 从 Expression 继承的形式
IPredicate equal(Object operand);
IPredicate notEqual(Object operand);

// TypedExpression 特有的显式绑定形式
<T> IPredicate equal(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> IPredicate notEqual(BiFunction<TypedExpression, T, Expression> funcRef, T value);
```

#### 使用场景
- 等值条件判断
- 不等值条件判断

#### 使用示例

```java
import static io.army.criteria.impl.SQLs.*;

// 场景 1: 直接传值
BankUser_.id.equal(1L);
BankUser_.userName.notEqual("admin");

// 场景 2: 使用 SQLs 常量
BankUser_.deletedAt.equal(NULL);
BankUser_.active.equal(TRUE);
BankUser_.active.equal(FALSE);

// 场景 3: 显式参数绑定
BankUser_.id.equal(SQLs::param, 1L);
BankUser_.userName.equal(SQLs::literal, "admin");
BankUser_.userName.equal(SQLs::constant, "admin");

// 场景 4: 命名参数 (用于批量操作)
BankUser_.id.equal(SQLs::namedParam, "userId");
```

---

### 2. nullSafeEqual

#### 方法签名
```java
@Support({MySQL, PostgreSQL, H2})
IPredicate nullSafeEqual(Object operand);

<T> IPredicate nullSafeEqual(BiFunction<TypedExpression, T, Expression> funcRef, @Nullable T value);
```

#### 使用场景
- 安全的 null 比较，支持两边都是 null 的情况
- MySQL 的 `<=>` 操作符
- PostgreSQL 的 `IS NOT DISTINCT FROM`

#### 使用示例

```java
import static io.army.criteria.impl.SQLs.*;

// 安全的 null 比较
BankUser_.deletedAt.nullSafeEqual(NULL);
BankUser_.parentId.nullSafeEqual(SQLs::param, null);
```

---

### 3. less / lessEqual / greater / greaterEqual

#### 方法签名
```java
// 从 Expression 继承
IPredicate less(Object operand);
IPredicate lessEqual(Object operand);
IPredicate greater(Object operand);
IPredicate greaterEqual(Object operand);

// TypedExpression 显式绑定
<T> IPredicate less(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> IPredicate lessEqual(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> IPredicate greater(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> IPredicate greaterEqual(BiFunction<TypedExpression, T, Expression> funcRef, T value);
```

#### 使用场景
- 数值范围比较
- 日期时间比较

#### 使用示例

```java
import static io.army.criteria.impl.SQLs.*;

// 数值比较
BankUser_.age.greater(18);
BankUser_.balance.lessEqual(1000.00);

// 日期比较
BankUser_.createTime.greater(SQLs::literal, LocalDateTime.now().minusDays(7));

// 显式绑定
BankUser_.score.greaterEqual(SQLs::param, 60);
```

---

### 4. between / notBetween

#### 方法签名
```java
// 基础形式
IPredicate between(Object first, SQLs.WordAnd and, Object second);
IPredicate notBetween(Object first, SQLs.WordAnd and, Object second);

// 单参数绑定形式
<T> IPredicate between(BiFunction<TypedExpression, T, Expression> funcRef, T first, SQLs.WordAnd and, T second);
<T> IPredicate notBetween(BiFunction<TypedExpression, T, Expression> funcRef, T first, SQLs.WordAnd and, T second);

// 双参数分别绑定形式
<T, U> IPredicate between(BiFunction<TypedExpression, T, Expression> firstFuncRef, T first, 
                          SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second);
<T, U> IPredicate notBetween(BiFunction<TypedExpression, T, Expression> firstFuncRef, T first, 
                              SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second);

// PostgreSQL/H2 特有的带修饰符形式
@Support({PostgreSQL, H2})
<T> IPredicate between(@Nullable SQLs.BetweenModifier modifier, 
                      BiFunction<TypedExpression, T, Expression> funcRef, T first, 
                      SQLs.WordAnd and, T second);
@Support({PostgreSQL, H2})
<T, U> IPredicate between(@Nullable SQLs.BetweenModifier modifier, 
                         BiFunction<TypedExpression, T, Expression> firstFuncRef, T first, 
                         SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second);
@Support({PostgreSQL, H2})
<T> IPredicate notBetween(@Nullable SQLs.BetweenModifier modifier, 
                         BiFunction<TypedExpression, T, Expression> funcRef, T first, 
                         SQLs.WordAnd and, T second);
@Support({PostgreSQL, H2})
<T, U> IPredicate notBetween(@Nullable SQLs.BetweenModifier modifier, 
                             BiFunction<TypedExpression, T, Expression> firstFuncRef, T first, 
                             SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef, U second);
```

#### 使用场景
- 范围查询
- 时间区间查询

#### 使用示例

```java
import static io.army.criteria.impl.SQLs.*;

// 基础用法
BankUser_.age.between(18, AND, 60);
BankUser_.createTime.notBetween(startTime, AND, endTime);

// 单参数绑定
BankUser_.age.between(SQLs::param, 18, AND, 60);

// 分别绑定两个边界
BankUser_.createTime.between(SQLs::literal, startTime, AND, SQLs::param, endTime);

// PostgreSQL SYMMETRIC 修饰符
BankUser_.score.between(SYMMETRIC, SQLs::param, 60, AND, 100);
```

---

### 5. is / isNot (布尔测试)

#### 方法签名
```java
// 从 Expression 继承
IPredicate is(SQLs.BoolTestWord operand);
IPredicate isNot(SQLs.BoolTestWord operand);

// PostgreSQL/H2 特有的显式绑定形式
@Support({PostgreSQL, H2})
<T> IPredicate is(SQLs.IsComparisonWord operator, 
                 BiFunction<TypedExpression, T, Expression> funcRef, @Nullable T value);
@Support({PostgreSQL, H2})
<T> IPredicate isNot(SQLs.IsComparisonWord operator, 
                    BiFunction<TypedExpression, T, Expression> funcRef, @Nullable T value);
```

#### 使用场景
- NULL 检查
- 布尔值检查
- 未知值检查

#### 使用示例

```java
import static io.army.criteria.impl.SQLs.*;

// NULL 检查
BankUser_.deletedAt.is(NULL);
BankUser_.deletedAt.isNot(NULL);

// 布尔检查
BankUser_.active.is(TRUE);
BankUser_.active.isNot(FALSE);

// 未知值
BankUser_.status.is(UNKNOWN);

// PostgreSQL DISTINCT FROM
BankUser_.nickName.is(DISTINCT_FROM, SQLs::param, "admin");
```

---

### 6. isNull / isNotNull

#### 方法签名
```java
IPredicate isNull();
IPredicate isNotNull();
```

#### 使用场景
- NULL 值检查的快捷方式

#### 使用示例

```java
// 更简洁的写法
BankUser_.deletedAt.isNull();
BankUser_.deletedAt.isNotNull();
```

---

### 7. like / notLike

#### 方法签名
```java
// 从 Expression 继承
IPredicate like(Object pattern);
IPredicate like(Object pattern, SQLs.WordEscape escape, Object escapeChar);
IPredicate notLike(Object pattern);
IPredicate notLike(Object pattern, SQLs.WordEscape escape, Object escapeChar);

// TypedExpression 显式绑定
<T> IPredicate like(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> IPredicate like(BiFunction<TypedExpression, T, Expression> funcRef, T value, 
                    SQLs.WordEscape escape, T escapeChar);
<T> IPredicate notLike(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> IPredicate notLike(BiFunction<TypedExpression, T, Expression> funcRef, T value, 
                       SQLs.WordEscape escape, T escapeChar);
```

#### 使用场景
- 模糊查询
- 通配符匹配

#### 使用示例

```java
import static io.army.criteria.impl.SQLs.*;

// 基础模糊查询
BankUser_.userName.like("%zhang%");
BankUser_.email.notLike("@example.com");

// 显式绑定
BankUser_.userName.like(SQLs::param, "%zhang%");

// 带转义字符
BankUser_.userName.like(SQLs::literal, "test\\_%", ESCAPE, "\\");
```

---

### 8. similarTo / notSimilarTo (PostgreSQL 特有)

#### 方法签名
```java
@Support({PostgreSQL})
IPredicate similarTo(Object pattern);
@Support({PostgreSQL})
IPredicate similarTo(Object pattern, SQLs.WordEscape escape, Object escapeChar);
@Support({PostgreSQL})
IPredicate notSimilarTo(Object pattern);
@Support({PostgreSQL})
IPredicate notSimilarTo(Object pattern, SQLs.WordEscape escape, Object escapeChar);

@Support({PostgreSQL})
<T> IPredicate similarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value);
@Support({PostgreSQL})
<T> IPredicate similarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value, 
                          SQLs.WordEscape escape, T escapeChar);
@Support({PostgreSQL})
<T> IPredicate notSimilarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value);
@Support({PostgreSQL})
<T> IPredicate notSimilarTo(BiFunction<TypedExpression, T, Expression> funcRef, T value, 
                             SQLs.WordEscape escape, T escapeChar);
```

#### 使用场景
- PostgreSQL 正则表达式匹配

#### 使用示例

```java
import static io.army.criteria.impl.SQLs.*;

BankUser_.userName.similarTo(SQLs::literal, "a%");
```

---

## 二、IN / NOT IN 操作

### 方法签名
```java
// 从 Expression 继承
IPredicate in(SQLValueList row);
IPredicate notIn(SQLValueList row);

// TypedExpression 集合绑定
IPredicate in(BiFunction<TypedExpression, Collection<?>, RowExpression> funcRef, Collection<?> value);
IPredicate notIn(BiFunction<TypedExpression, Collection<?>, RowExpression> funcRef, Collection<?> value);

// 命名参数 (用于批量操作)
IPredicate in(TeNamedParamsFunc<TypedExpression> funcRef, String paramName, int size);
IPredicate notIn(TeNamedParamsFunc<TypedExpression> funcRef, String paramName, int size);
```

### 使用场景
- 集合成员检查
- 子查询结果匹配

### 使用示例

```java
import static io.army.criteria.impl.SQLs.*;

// 集合参数
List<Long> ids = Arrays.asList(1L, 2L, 3L);
BankUser_.id.in(SQLs::rowParam, ids);
BankUser_.id.notIn(SQLs::rowLiteral, ids);

// 命名参数 (批量操作)
BankUser_.id.in(SQLs::namedRowParam, "userIds", 5);
```

---

## 三、ANY / SOME / ALL 量词

### 方法签名
```java
IPredicate equalAny(SQLValueList operand);
IPredicate equalSome(SQLValueList operand);
IPredicate equalAll(SQLValueList operand);
IPredicate notEqualAny(SQLValueList operand);
IPredicate notEqualSome(SQLValueList operand);
IPredicate notEqualAll(SQLValueList operand);
IPredicate lessAny(SQLValueList operand);
IPredicate lessSome(SQLValueList operand);
IPredicate lessAll(SQLValueList operand);
IPredicate lessEqualAny(SQLValueList operand);
IPredicate lessEqualSome(SQLValueList operand);
IPredicate lessEqualAll(SQLValueList operand);
IPredicate greaterAny(SQLValueList operand);
IPredicate greaterSome(SQLValueList operand);
IPredicate greaterAll(SQLValueList operand);
IPredicate greaterEqualAny(SQLValueList operand);
IPredicate greaterEqualSome(SQLValueList operand);
IPredicate greaterEqualAll(SQLValueList operand);
```

### 使用场景
- 与子查询结果的量词比较

### 使用示例

```java
// 与子查询一起使用
BankUser_.score.greaterAny(subQueryResult);
```

---

## 四、算术操作

### 方法签名
```java
// 从 Expression 继承
Expression mod(Object operand);
Expression times(Object operand);
Expression plus(Object operand);
Expression minus(Object minuend);
Expression divide(Object divisor);

// TypedExpression 显式绑定
<T> Expression mod(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression times(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression plus(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression minus(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression divide(BiFunction<TypedExpression, T, Expression> funcRef, T value);
```

### 使用场景
- 数值计算

### 使用示例

```java
import static io.army.criteria.impl.SQLs.*;

// 直接计算
BankUser_.balance.plus(100.00);
BankUser_.age.times(2);

// 显式绑定
BankUser_.score.plus(SQLs::param, 10);
```

---

## 五、位操作

### 方法签名
```java
Expression bitwiseAnd(Object operand);
Expression bitwiseOr(Object operand);
Expression bitwiseXor(Object operand);
Expression rightShift(Object bitNumber);
Expression leftShift(Object bitNumber);

<T> Expression bitwiseAnd(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression bitwiseOr(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression bitwiseXor(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression rightShift(BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> Expression leftShift(BiFunction<TypedExpression, T, Expression> funcRef, T value);
```

### 使用场景
- 位掩码操作
- 位运算

### 使用示例

```java
import static io.army.criteria.impl.SQLs.*;

BankUser_.status.bitwiseAnd(SQLs::literal, 0b101);
```

---

## 六、数组切片 (PostgreSQL/H2 特有)

### 方法签名
```java
@Support({H2, PostgreSQL})
Expression slice(Object lower, SQLs.SymbolColon colon, Object upper);

@Support({H2, PostgreSQL})
Expression slice(BiFunction<IntegerType, Integer, Expression> func, int lower, 
                 SQLs.SymbolColon colon, int upper);

@Support({H2, PostgreSQL})
Expression slice(Object lower, SQLs.SymbolColon colon);

@Support({H2, PostgreSQL})
Expression slice(SQLs.SymbolColon colon, Object upper);

@Support({H2, PostgreSQL})
Expression slice(SQLs.SymbolColon colon);

// 二维数组切片
@Support({H2, PostgreSQL})
Expression slice(Object lower, SQLs.SymbolColon colon, Object upper, 
                 Object lower1, SQLs.SymbolColon colon1, Object upper1);

@Support({H2, PostgreSQL})
Expression slice(BiFunction<IntegerType, Integer, Expression> func, int lower, 
                 SQLs.SymbolColon colon, int upper, int lower1, 
                 SQLs.SymbolColon colon1, int upper1);

@Support({H2, PostgreSQL})
Expression slice(SQLs.SymbolColon colon, Object upper, 
                 Object lower1, SQLs.SymbolColon colon1, Object upper1);

@Support({H2, PostgreSQL})
Expression slice(Object lower, SQLs.SymbolColon colon, Object upper, 
                 Object lower1, SQLs.SymbolColon colon1);

@Support({H2, PostgreSQL})
Expression slice(SQLs.SymbolColon colon, SQLs.SymbolColon colon1);

// 通用下标访问
@Support({H2, PostgreSQL})
Expression sliceAtSubs(List<?> indexList);

@Support({H2, PostgreSQL})
Expression sliceAtSubs(BiFunction<MappingType, Integer, Expression> func, List<Integer> indexList);
```

### 使用场景
- PostgreSQL 数组切片访问

### 使用示例

```java
import static io.army.criteria.impl.SQLs.*;

// 一维数组切片 [1:5]
BankUser_.tags.slice(SQLs::literal, 1, COLON, 5);

// 从开头到第 5 个元素 [:5]
BankUser_.tags.slice(COLON, 5);

// 从第 1 个元素到末尾 [1:]
BankUser_.tags.slice(1, COLON);
```

---

## 七、点操作 (复合类型访问)

### 方法签名
```java
Expression dot(Object key);
Expression dot(Object key, Object key1);
Expression dot(Object key, Object key1, Object key2);
Expression dot(Object key, Object key1, Object key2, Object key3);
Expression dot(Object key, Object key1, Object key2, Object key3, Object key4);
Expression dots(List<?> subscriptList);
```

### 使用场景
- PostgreSQL 复合类型字段访问
- JSON 对象字段访问

### 使用示例

```java
import static io.army.criteria.impl.SQLs.*;

// 访问复合类型字段
BankUser_.address.dot("city");
```

---

## 八、方括号操作 (下标访问)

### 方法签名
```java
Expression bracket(Object key);
Expression bracket(Object key, Object key1);
Expression bracket(Object key, Object key1, Object key2);
Expression bracket(Object key, Object key1, Object key2, Object key3);
Expression bracket(Object key, Object key1, Object key2, Object key3, Object key4);

// 显式绑定版本
Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key);
Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1);
Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1, Object key2);
Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1, Object key2, Object key3);
Expression bracket(BiFunction<MappingType, Object, Expression> func, Object key, Object key1, Object key2, Object key3, Object key4);

Expression brackets(List<?> subscriptList);
Expression brackets(BiFunction<MappingType, Object, Expression> func, List<?> subscriptList);
```

### 使用场景
- 数组下标访问
- JSONB 下标访问

### 使用示例

```java
import static io.army.criteria.impl.SQLs.*;

// 数组下标访问
BankUser_.tags.bracket(0);

// JSONB 键访问
BankUser_.metadata.bracket("settings");
```

---

## 九、向量距离操作 (PostgreSQL pgvector 特有)

### 方法签名
```java
@Support({PostgreSQL})
Expression l1Distance(Object operand);

@Support({PostgreSQL})
Expression l2Distance(Object operand);

@Support({PostgreSQL})
Expression cosineDistance(Object operand);

@Support({PostgreSQL})
Expression hammingDistance(Object operand);

@Support({PostgreSQL})
Expression jaccardDistance(Object operand);

@Support({PostgreSQL})
Expression negDot(Object operand);

// 显式绑定版本
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
```

### 使用场景
- 向量相似度计算 (pgvector)

### 使用示例

```java
import static io.army.criteria.impl.SQLs.*;

// L2 距离 (<->)
BankUser_.embedding.l2Distance(queryVector);

// 余弦距离 (<=>)
BankUser_.embedding.cosineDistance(SQLs::param, queryVector);
```

---

## 十、类型转换操作

### 方法签名
```java
TypedExpression mapTo(MappingType typeMeta);
ArrayExpression mapToArray(ArrayMappingType type);

@Support({PostgreSQL})
TypedExpression castTo(MappingType type);

@Support({PostgreSQL})
ArrayExpression castToArray(ArrayMappingType type);
```

### 使用场景
- 类型映射 (不改变 SQL 输出)
- PostgreSQL CAST 操作

### 使用示例

```java
// 类型映射
BankUser_.id.mapTo(LongType.INSTANCE);

// PostgreSQL CAST
BankUser_.id.castTo(StringType.INSTANCE);
```

---

## 十一、排序操作

### 方法签名
```java
SortItem asSortItem();
SortItem asc();
SortItem desc();
SortItem ascSpace(@Nullable SQLs.NullsFirstLast firstLast);
SortItem descSpace(@Nullable SQLs.NullsFirstLast firstLast);
```

### 使用场景
- ORDER BY 子句

### 使用示例

```java
import static io.army.criteria.impl.SQLs.*;

// 基础排序
BankUser_.createTime.desc();
BankUser_.userName.asc();

// NULL 排序
BankUser_.deletedAt.ascSpace(NULLS_FIRST);
BankUser_.deletedAt.descSpace(NULLS_LAST);
```

---

## 十二、方言特有操作

### 方法签名
```java
Expression space(SQLs.DualOperator operator, Object right);
IPredicate space(SQLs.BiOperator operator, Object right);
IPredicate space(SQLs.BiOperator operator, Object right, SQLToken modifier, Object optionalExp);

// 显式绑定版本
<T> Expression space(SQLs.DualOperator operator, 
                    BiFunction<TypedExpression, T, Expression> funcRef, T value);
<T> IPredicate space(SQLs.BiOperator operator, 
                     BiFunction<TypedExpression, T, Expression> funcRef, T right);
<T> IPredicate space(SQLs.BiOperator operator, 
                     BiFunction<StringType, T, Expression> funcRef, T right, 
                     SQLToken modifier, T optionalExp);
```

### 使用场景
- 方言特有操作符

---

## SQLs 常量详解

### 1. ASTERISK (*)

**源码位置**: SQLs.java:130
```java
public static final SymbolAsterisk ASTERISK = new LiteralSymbolAsterisk();
```

**使用场景**:
- SELECT * 查询
- COUNT(*) 聚合
- 其他需要 * 的地方

**示例**:
```java
import static io.army.criteria.impl.SQLs.*;

// SELECT *
SQLs.query().select(ASTERISK).from(BankUser_.T).asQuery();

// COUNT(*)
SQLs.query().select(count(ASTERISK)).from(BankUser_.T).asQuery();

// table.*
SQLs.query().select("u", PERIOD, ASTERISK).from(BankUser_.T, AS, "u").asQuery();
```

---

### 2. TRUE / FALSE

**源码位置**: SQLs.java:142-144
```java
public static final WordBoolean TRUE = OperationPredicate.booleanWord(true);
public static final WordBoolean FALSE = OperationPredicate.booleanWord(false);
```

**相关参数常量**:
```java
public static final ParamExpression PARAM_TRUE = ArmyParamExpression.unsafeParam(BooleanType.INSTANCE, Boolean.TRUE);
public static final ParamExpression PARAM_FALSE = ArmyParamExpression.unsafeParam(BooleanType.INSTANCE, Boolean.FALSE);
```

**使用场景**:
- 布尔字面量
- IS TRUE / IS FALSE 谓词

**示例**:
```java
import static io.army.criteria.impl.SQLs.*;

// 作为谓词操作数
BankUser_.active.is(TRUE);
BankUser_.active.isNot(FALSE);

// 直接比较
BankUser_.active.equal(TRUE);
BankUser_.active.notEqual(FALSE);
```

---

### 3. NULL

**源码位置**: SQLs.java:149
```java
public static final WordNull NULL = NonOperationExpression.nullWord();
```

**使用场景**:
- NULL 字面量
- IS NULL / IS NOT NULL
- 安全比较

**示例**:
```java
import static io.army.criteria.impl.SQLs.*;

// NULL 检查
BankUser_.deletedAt.is(NULL);
BankUser_.deletedAt.isNot(NULL);

// 直接比较
BankUser_.deletedAt.equal(NULL);

// 安全比较
BankUser_.deletedAt.nullSafeEqual(NULL);
```

---

### 4. DEFAULT

**源码位置**: SQLs.java:147
```java
public static final WordDefault DEFAULT = new DefaultWord();
```

**使用场景**:
- INSERT 语句中使用列默认值
- UPDATE 语句中重置为默认值

**示例**:
```java
import static io.army.criteria.impl.SQLs.*;

// INSERT 使用默认值
SQLs.singleInsert()
    .insertInto(BankUser_.T)
    .values()
    .parens(p -> p
        .space(BankUser_.id, SQLs::param, 1L)
        .comma(BankUser_.createTime, DEFAULT)
    )
    .asInsert();

// UPDATE 重置为默认值
SQLs.singleUpdate()
    .update(BankUser_.T)
    .sets(s -> s.set(BankUser_.updateTime, DEFAULT))
    .where(BankUser_.id.equal(SQLs::param, 1L))
    .asUpdate();
```

---

## 参数绑定方式完整对照表

| 绑定方式 | 方法 | 用途 | 安全性 | 适用场景 |
|---------|------|------|--------|---------|
| **自动参数化** | `equal(value)` | 自动推断类型，JDBC 参数 | ✅ 高 | 大多数查询 |
| **显式参数** | `SQLs.param(type, value)` | 显式指定类型，JDBC 参数 | ✅ 高 | 需要类型安全 |
| **字面量** | `SQLs.literal(type, value)` | 嵌入 SQL 的字面量 (带类型) | ⚠️ 需注意 | 系统常量 |
| **常量** | `SQLs.constant(type, value)` | 嵌入 SQL 的常量 (无类型) | ⚠️ 需注意 | 固定值 |
| **命名参数** | `SQLs.namedParam(type, name)` | 批量操作命名参数 | ✅ 高 | 批量更新/删除 |
| **命名字面量** | `SQLs.namedLiteral(type, name)` | 批量插入命名字面量 | ⚠️ | 批量插入 |
| **命名常量** | `SQLs.namedConst(type, name)` | 批量插入命名常量 | ⚠️ | 批量插入 |
| **行参数** | `SQLs.rowParam(type, collection)` | IN 子句集合参数 | ✅ 高 | IN 查询 |
| **行字面量** | `SQLs.rowLiteral(type, collection)` | IN 子句集合字面量 | ⚠️ | IN 查询 |
| **行常量** | `SQLs.rowConst(type, collection)` | IN 子句集合常量 | ⚠️ | IN 查询 |
| **命名行参数** | `SQLs.namedRowParam(type, name, size)` | 批量 IN 子句 | ✅ 高 | 批量 IN |

---

## 完整示例：结合查询的 Skill 配合使用

### 与 SQLs-query-method-chain 配合

```java
import static io.army.criteria.impl.SQLs.*;

// 完整查询示例
final Select stmt = SQLs.query()
    .select(BankUser_.id, BankUser_.userName, BankUser_.email)
    .from(BankUser_.T, AS, "u")
    .where(BankUser_.userName.like(SQLs::param, "%zhang%"))
    .and(BankUser_.active.is(TRUE))
    .and(BankUser_.deletedAt.is(NULL))
    .and(BankUser_.age.between(SQLs::param, 18, AND, 60))
    .and(BankUser_.role.in(SQLs::rowParam, Arrays.asList("admin", "user")))
    .orderBy(BankUser_.createTime.desc())
    .limit(SQLs::literal, 10)
    .asQuery();

List<BankUser> users = session.query(stmt, BankUser.class);
```

### 与 SQLs-single-update-method-chain 配合

```java
import static io.army.criteria.impl.SQLs.*;

final Update stmt = SQLs.singleUpdate()
    .update(BankUser_.T)
    .sets(s -> s
        .set(BankUser_.nickName, SQLs::param, "newName")
        .set(BankUser_.updateTime, DEFAULT)
        .set(BankUser_.active, TRUE)
    )
    .where(BankUser_.id.equal(SQLs::param, 1L))
    .asUpdate();

session.update(stmt);
```

### 与 SQLs-single-insert-method-chain 配合

```java
import static io.army.criteria.impl.SQLs.*;

final Insert stmt = SQLs.singleInsert()
    .insertInto(BankUser_.T)
    .values()
    .parens(p -> p
        .space(BankUser_.id, SQLs::param, 1L)
        .comma(BankUser_.userName, SQLs::param, "test")
        .comma(BankUser_.createTime, DEFAULT)
        .comma(BankUser_.active, TRUE)
    )
    .asInsert();

session.insert(stmt);
```

---

## Skill 进化记录

### v1.0 (2025-06-06)
- 初始版本，完整覆盖 TypedExpression 所有方法
- 包含 SQLs 五大常量详解
- 提供完整的参数绑定方式对照表
- 包含与其他 skill 的配合使用示例
