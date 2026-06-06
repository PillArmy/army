---
name: "army-ipredicate-sqls-guide"
description: "学习和使用 Army IPredicate 接口和 SQLs 常用方法的完整指南。包含 NULL、DEFAULT、TRUE、FALSE、ASTERISK、not()、bracket() 等核心 API 的详细说明、使用场景和示例代码。在需要了解或使用这些 API 时调用。"
---

# Army IPredicate & SQLs 完全指南

本指南详细介绍 Army 框架中 `IPredicate` 接口的声明、实现，以及 `SQLs` 类中的常用常量和方法（NULL、DEFAULT、TRUE、FALSE、ASTERISK、not()、bracket() 等）的使用场景和示例代码。

---

## 目录
- [IPredicate 接口](#ipredicate-接口)
- [SQLs 常量](#sqls-常量)
- [SQLs 核心方法](#sqls-核心方法)
- [完整示例代码](#完整示例代码)

---

## IPredicate 接口

### 接口声明

**文件位置**：`army-core/src/main/java/io/army/criteria/IPredicate.java

```java
public interface IPredicate extends TypedExpression, Statement._WhereAndClause<IPredicate> {
    
    /// @return always return {@link BooleanType#INSTANCE}
    @Override
    BooleanType typeMeta();

    IPredicate or(IPredicate predicate);

    <T> IPredicate or(Function<T, IPredicate> expOperator, T operand);

    IPredicate orGroup(Consumer<Consumer<IPredicate>> consumer);

    IPredicate orGroup(IPredicate t, IPredicate u);

    IPredicate orGroup(IPredicate t, IPredicate u, IPredicate v);

    IPredicate ifOrGroup(Consumer<Consumer<IPredicate>> consumer);

    IPredicate ifOr(Supplier<IPredicate> supplier);

    <T> IPredicate ifOr(Function<T, IPredicate> expOperator, @Nullable T value);

    <T> IPredicate ifOr(ExpressionOperator<TypedExpression, T, IPredicate> expOperator,
                        BiFunction<TypedExpression, T, Expression> operator, @Nullable T value);

    /// @param expOperator see {@link TypedExpression#space(SQLs.BiOperator, Object)}
    <T> IPredicate ifOr(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator, SQLs.BiOperator operator, @Nullable T value);

    /// @param expOperator see {@link TypedExpression#space(SQLs.BiOperator, BiFunction<TypedExpression, T, Expression)}
    <T> IPredicate ifOr(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator, SQLs.BiOperator operator,
                        BiFunction<TypedExpression, T, Expression> func, @Nullable T value);


    <T> IPredicate ifOr(BetweenValueOperator<T> expOperator, BiFunction<TypedExpression, T, Expression> operator,
                        @Nullable T value1, SQLs.WordAnd and, @Nullable T value2);

    <T, U> IPredicate ifOr(BetweenDualOperator<T, U> expOperator, BiFunction<TypedExpression, T, Expression> firstFuncRef,
                           @Nullable T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFuncRef,
                           @Nullable U second);
}
```

### IPredicate 方法说明

| 方法 | 说明 |
|------|------|
| `or(IPredicate)` | 逻辑 OR 连接两个谓词 |
| `or(Function, T)` | 使用表达式操作符 OR 连接 |
| `orGroup(Consumer)` | OR 分组 |
| `orGroup(IPredicate, IPredicate)` | 两个谓词的 OR 分组 |
| `orGroup(IPredicate, IPredicate, IPredicate)` | 三个谓词的 OR 分组 |
| `ifOrGroup(Consumer)` | 条件式 OR 分组 |
| `ifOr(Supplier)` | 条件式 OR |
| `ifOr(Function, T)` | 条件式 OR（带值） |
| `ifOr(ExpressionOperator, BiFunction, T)` | 条件式 OR（带操作符） |
| 其他 `ifOr` 重载 | 各种条件式 OR 变体 |

---

## SQLs 常量

### 1. NULL - SQL NULL 常量

**位置**：`army-core/src/main/java/io/army/criteria/impl/SQLs.java:149

```java
public static final WordNull NULL = NonOperationExpression.nullWord();
```

**用途**：表示 SQL 的 NULL 值，用于 NULL 比较或赋值。

---

### 2. DEFAULT - SQL DEFAULT 常量

**位置**：`army-core/src/main/java/io/army/criteria/impl/SQLs.java:147

```java
public static final WordDefault DEFAULT = new DefaultWord();
```

**用途**：表示 SQL 的 DEFAULT 值，用于 INSERT 或 UPDATE 语句中使用列的默认值。

---

### 3. TRUE - SQL TRUE 常量

**位置**：`army-core/src/main/java/io/army/criteria/impl/SQLs.java:142

```java
public static final WordBoolean TRUE = OperationPredicate.booleanWord(true);
```

**用途**：表示 SQL 的 TRUE 布尔值。

---

### 4. FALSE - SQL FALSE 常量

**位置**：`army-core/src/main/java/io/army/criteria/impl/SQLs.java:144

```java
public static final WordBoolean FALSE = OperationPredicate.booleanWord(false);
```

**用途**：表示 SQL 的 FALSE 布尔值。

---

### 5. ASTERISK - SQL 星号 (*)

**位置**：`army-core/src/main/java/io/army/criteria/impl/SQLs.java:130

```java
public static final SymbolAsterisk ASTERISK = new LiteralSymbolAsterisk();
```

**用途**：表示 SQL 的星号 (*)，用于 SELECT * 等场景。

---

## SQLs 核心方法

### 1. not() - 逻辑非

**位置**：`army-core/src/main/java/io/army/criteria/impl/SQLSyntax.java

**方法声明**：
```java
public static IPredicate not(IPredicate predicate)
```

**实现**：`OperationPredicate.notPredicate(predicate)

**用途**：对一个谓词进行逻辑非操作。

**使用场景**：
- 需要否定一个条件表达式
- 构建 NOT 条件
- 在 WHERE 子句中的否定查询

**调用形式**：
```java
// 直接调用
SQLs.not(ChinaRegion_.name.equal("test"))

// 链式调用
.where(SQLs.not(ChinaRegion_.name.equal("test")))
```

---

### 2. bracket() - 括号包裹

**位置**：`army-core/src/main/java/io/army/criteria/impl/SQLSyntax.java

**方法声明**：
```java
public static SimplePredicate bracket(IPredicate predicate)
```

**实现**：`OperationPredicate.bracketPredicate(predicate)

**用途**：用括号包裹一个谓词，改变运算优先级。

**使用场景**：
- 复杂条件组合时控制运算顺序
- AND/OR 优先级控制
- 确保条件分组

**调用形式**：
```java
// 直接使用
SQLs.bracket(ChinaRegion_.name.equal("江湖"))

// 在 WHERE 子句中
.and(SQLs.bracket(ChinaRegion_.name.equal(SQLs::literal, "江湖")))
```

---

### 3. parens() - 表达式括号

**位置**：`army-core/src/main/java/io/army/criteria/impl/SQLSyntax.java

**方法声明**：
```java
public static Expression parens(Expression expression)
```

**实现**：`OperationExpression.bracketExp(expression)

**用途**：用括号包裹一个表达式。

---

### 4. negate() - 数值取反

**位置**：`army-core/src/main/java/io/army/criteria/impl/SQLSyntax.java

**方法声明**：
```java
public static SimpleExpression negate(Expression exp)
```

**用途**：对数值表达式取反。

**调用形式**：
```java
// 示例
.set(ChinaRegion_.regionGdp, SQLs.negate(ChinaRegion_.regionGdp))
```

---

### 5. bitwiseNot() - 位运算非

**位置**：`army-core/src/main/java/io/army/criteria/impl/SQLSyntax.java

**方法声明**：
```java
public static SimpleExpression bitwiseNot(Expression exp)
```

**用途**：位运算 NOT 操作。

---

## 完整示例代码

### 示例 1：使用 SQLs.TRUE

```java
// 文件位置：army-example/src/test/java/io/army/session/reactive/mysql/InsertTests.java:124

final Insert stmt;
stmt = MySQLs.singleInsert()
        .insertInto(ChinaRegion_.T)
        .defaultValue(ChinaRegion_.visible, SQLs.TRUE)
        .values()
        .parens(s -> s.space(ChinaRegion_.name, SQLs::param, randomRegion(random))
                .comma(ChinaRegion_.regionGdp, SQLs::literal, randomDecimal(random))
                .comma(ChinaRegion_.parentId, SQLs::literal, random.nextInt())
        ).comma()
```

---

### 示例 2：使用 SQLs.bracket()

```java
// 文件位置：army-example/src/test/java/io/army/criteria/standard/unit/StandardUpdateUnitTests.java

final UpdateStatement stmt;
stmt = SQLs.singleUpdate()
        .update(ChinaRegion_.T, AS, "c")
        .set(ChinaRegion_.name, SQLs::param, "武侠江湖")
        .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, addGdp)
        .where(ChinaRegion_.id.between(SQLs::literal, map.get("firstId"), AND, map.get("secondId")))
        .and(SQLs.bracket(ChinaRegion_.name.equal(SQLs::literal, "江湖")))
        .and(ChinaRegion_.regionGdp.plus(SQLs::param, addGdp).greaterEqual(0))
        .asUpdate();
```

---

### 示例 3：使用 SQLs.ASTERISK

```java
// 文件位置：army-example/src/test/java/io/army/criteria/postgre/statement/FuncUnitTests.java

final Select stmt;
stmt = Postgres.query()
        .select(s -> s.space("func", PERIOD, ASTERISK))
        .from(jsonbPathQuery(SQLs.literal(JsonbType.TEXT, json), SQLs::literal, path))
        .as("func")
        .asQuery();
```

---

### 示例 4：使用 SQLs.negate()

```java
// 文件位置：army-example/src/test/java/io/army/criteria/standard/unit/StandardUpdateUnitTests.java

final Update stmt;
stmt = SQLs.domainUpdate()
        .update(ChinaProvince_.T, AS, "p")
        .set(ChinaRegion_.regionGdp, gdpAmount)
        .set(ChinaRegion_.regionGdp, SQLs.negate(ChinaRegion_.regionGdp))
        .set(ChinaRegion_.name, SQLs::param, "武侠江湖")
        .where(ChinaRegion_.id.equal(SQLs::literal, 1))
        .asUpdate();
```

---

### 示例 5：使用 SQLs.ASTERISK 在 SELECT 中

```java
// 文件位置：army-example/src/test/java/io/army/criteria/standard/unit/StandardQueryUnitTests.java

final Select stmt;
stmt = SQLs.query()
        .select(s -> s.space(SQLs.refField("us", "one"))
                .comma("us", PERIOD, ASTERISK))
        .from(SQLs.subQuery()
                .select(SQLs.literalValue(1)::as, "one")
                .comma("u", PERIOD, PillUser_.T)
                .from(PillUser_.T, AS, "u")
                .where(PillUser_.createTime.equal(SQLs::literal, LocalDateTime.now()))
                .limit(SQLs::literal, criteria::get, "offset", "rowCount")
                .asQuery())
        .as("us")
        .where(SQLs.refField("us", "one").equal(SQLs.parameter(1)))
        .asQuery();
```

---

## 相关类和文件位置

| 类/接口 | 文件路径 |
|--------|---------|
| IPredicate | `army-core/src/main/java/io/army/criteria/IPredicate.java` |
| SQLs | `army-core/src/main/java/io/army/criteria/impl/SQLs.java` |
| SQLSyntax | `army-core/src/main/java/io/army/criteria/impl/SQLSyntax.java` |
| OperationPredicate | `army-core/src/main/java/io/army/criteria/impl/OperationPredicate.java` |
| OperationExpression | `army-core/src/main/java/io/army/criteria/impl/OperationExpression.java` |
| NonOperationExpression | `army-core/src/main/java/io/army/criteria/impl/NonOperationExpression.java` |
| StandardUpdateUnitTests | `army-example/src/test/java/io/army/criteria/standard/unit/StandardUpdateUnitTests.java` |
| FuncUnitTests | `army-example/src/test/java/io/army/criteria/postgre/statement/FuncUnitTests.java` |
| InsertTests | `army-example/src/test/java/io/army/session/reactive/mysql/InsertTests.java` |

---

## 快速参考速查表

| API | 类型 | 用途 |
|-----|------|------|
| `SQLs.NULL` | 常量 | SQL NULL 值 |
| `SQLs.DEFAULT` | 常量 | SQL DEFAULT 值 |
| `SQLs.TRUE` | 常量 | SQL TRUE 布尔值 |
| `SQLs.FALSE` | 常量 | SQL FALSE 布尔值 |
| `SQLs.ASTERISK` | 常量 | SQL 星号 (*) |
| `SQLs.not(IPredicate)` | 方法 | 逻辑非操作 |
| `SQLs.bracket(IPredicate)` | 方法 | 括号包裹谓词 |
| `SQLs.parens(Expression)` | 方法 | 括号包裹表达式 |
| `SQLs.negate(Expression)` | 方法 | 数值取反 |
| `SQLs.bitwiseNot(Expression)` | 方法 | 位运算非 |
