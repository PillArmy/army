---
name: army-example-code
description: Write correct Army Criteria API example code in asciidoc/index.adoc. Use when writing, fixing, or reviewing Java example code blocks that use Army framework APIs (Criteria statements, functions, window functions, expressions).
---

# Army Example Code Writing

> **适用范围**: 本 Skill **仅限**用于 `asciidoc/index.adoc` 文档及其他 Army 项目文档，为 Army Criteria API 编写正确的
> Java 示例代码块。不得用于其它项目。

## 核心铁律

**示例代码中使用的每个 API，必须与源码签名完全一致。**

不允许凭记忆或推测写 Method Reference、函数调用参数个数或类型——写之前必须读到源码确认。

### Rule 0: 源码为王 — 严禁无源码支撑的示例代码 (ZERO TOLERANCE)

> **绝对禁止在没有读取对应源码的情况下生成任何示例代码块。**

此规则没有任何例外，无论多么简单或多"显然"的 API。违反此规则是产生示例代码错误的根本原因。

**判定标准**：写 `[source,java]` 代码块前，你必须能够回答以下问题并给出源码引用：

1. 这个方法在哪个类中声明？该类是 `public` 还是 package-private？
2. 方法的完整签名是什么（参数类型、个数、返回类型）？
3. 方法链中每一步的返回类型能否赋给左边的变量？
4. 每个参数是否与源码中的参数类型兼容？

**如果上述任何一个问题无法用源码回答，不得写入示例代码**。

过往教训提供了充分证据：

- `SQLs.literalValue(Object)` 被错误当作 BiFunction → 因为没读源码
- `Functions` 被直接引用 → 因为没检查类修饰符
- `.asUpdate()` 被误认为返回 `BatchUpdate` → 因为没查返回类型接口

**一旦发现无源码支撑的示例代码，一律视为错误，必须修正。**

## 写示例前必须做的事

### Rule 1: Read Source Before Write (MANDATORY)

写任何示例代码块前，必须定位并读取相关类的源码：

1. 定位 API 所在的源码文件（如 `Functions.java`、`SQLs.java`、`Windows.java`、`OperationExpression.java` 等）
2. 读取类的声明（**类修饰符** — 是否为 `public`）
3. 读取目标方法的完整签名（参数类型、参数个数、返回类型）
4. **用源码验证**示例代码中的每个参数、每个 Method Reference 是否合法
5. **用源码验证**示例代码引用的类名是否正确（package-private 类必须用 public 子类替代）
6. 确认无误后再写入文档

### Rule 2: Method Reference 签名验证

Method Reference（`::` 语法）的签名由目标上下文决定。写 Method Reference 前必须确认：

| 上下文                                  | 期望的函数接口                                      | 示例                         |
|--------------------------------------|----------------------------------------------|----------------------------|
| `greater(BiFunction, T)`             | `BiFunction<TypedExpression, T, Expression>` | `SQLs::param` ✅            |
| `comma(field, BiFunction, value)`    | `BiFunction<TypedExpression, T, Expression>` | `SQLs::param` ✅            |
| `then(Object)` / `elseValue(Object)` | 直接传 `Expression` 或原始值                        | `SQLs.literalValue("x")` ✅ |
| `when(Object)`                       | 直接传 `IPredicate` 或原始值                        | `Stock_.x.greater(100)` ✅  |

### Rule 3: `SQLs.literalValue` — 何时用、何时不用

**签名**: `public static LiteralExpression literalValue(Object value)` — **仅一个参数**。

| 用法                                             | 是否正确 | 说明                                                 |
|------------------------------------------------|------|----------------------------------------------------|
| `greater(SQLs::literalValue, 100)`             | ❌    | `::literalValue` 不是 BiFunction，编译错误                |
| `greater(SQLs.literalValue(100))`              | ✅    | 直接传 Expression，正确                                  |
| `then(SQLs::literalValue, "HIGH")`             | ❌    | `then(Function, ...)` 重载期望 Function，不是 BiFunction  |
| `then(SQLs.literalValue("HIGH"))`              | ✅    | 直接传 Expression，正确                                  |
| `Windows.lag(expr, SQLs::literalValue, 1, ..)` | ❌    | `lag(Expression, Expression, Expression)` 期望 3 个参数 |
| `Windows.lag(expr, SQLs.literalValue(1), ..)`  | ✅    | 直接传 Expression，正确                                  |

### Rule 4: DEFAULT Mode 优先 — 大多数情况不需要 literalValue

Army 默认 `LiteralMode.DEFAULT`，所有普通值（`int`, `String`, `BigDecimal` 等）会自动包装为参数化表达式（JDBC `?`）。**不需要
**显式调用 `SQLs.literalValue()`：

```java
// ✅ 正确：DEFAULT mode 自动参数化
Stock_.offerPrice.greater(1000)
SQLs.round(Stock_.offerPrice, 2)
Windows.lag(Stock_.offerPrice, 1, 0)
SQLs.concat(Stock_.exchange, "-", Stock_.code)

// ❌ 错误：画蛇添足
Stock_.offerPrice.greater(SQLs::literalValue, 1000)
SQLs.round(Stock_.offerPrice, SQLs::literalValue, 2)
```

**仅在以下场景使用 `SQLs.literalValue(x)`**：

- 需要将值作为 SQL 字面量（而非 `?` 参数）嵌入 SQL 时
- 所在上下文处于 `LITERAL` 模式
- 需要显式控制值的类型推断

### Rule 6: Class Visibility — 每个引用的类必须确认 public (MANDATORY)

**示例代码中出现的每个类名，必须确认该类是 `public` 的。** 不是 `public` 的类不能被包外用户直接引用，必须通过其 `public`
子类访问。

**验证流程**：

1. 对于示例代码中出现的每个类名，读取其源码文件
2. 检查类声明是否包含 `public` 修饰符
3. 如果是 `abstract class Xxx`（无 `public`，包内可见），则不可直接使用该类名
4. 沿继承链向上查找，找到最接近的 `public` 类
5. 用 `public` 类的名称代替原类名

**已知的 package-private 类及其 public 子类**：

| Package-Private 类 | Public 子类 | 说明                                      |
|-------------------|-----------|-----------------------------------------|
| `Functions`       | `SQLs`    | `Functions → SQLSyntax → SQLs (public)` |

**通用规则**：

```java
// ❌ 错误：直接引用 package-private 类
Functions.countAsterisk()
Functions.sum(expr)
Functions.cases()

// ✅ 正确：通过 public 子类引用
SQLs.countAsterisk()
SQLs.sum(expr)
SQLs.cases()
```

**与 public 类对比**：

- `Windows` 类是 `public` 的 → `Windows.rowNumber()`、`Windows.lag()` 可以直接使用
- `SQLs` 类是 `public` 的 → `SQLs.query()`、`SQLs.param()` 可以直接使用
- 任何不是 `public` 的类 → 必须在继承链上找到其 public 子类

### Rule 5: 常量优先

`SQLs` 类预定义了常用常量，优先使用：

```java
SQLs.LITERAL_1, SQLs.LITERAL_100, SQLs.LITERAL_1000   // 字面量常量
SQLs.PARAM_1, SQLs.PARAM_100                           // 参数常量
```

### Rule 8: Batch 操作 — `asUpdate()`/`asDelete()` 后必须链接 `namedParamList()` (MANDATORY)

Batch 操作的 `.asUpdate()` / `.asDelete()` **不返回最终类型**（`BatchUpdate` / `BatchDelete`），而是返回
`_BatchUpdateParamSpec` / `_BatchDeleteParamSpec`。必须继续链式调用 `.namedParamList(paramList)` 才能得到最终类型。

**API 链**：

```
SQLs.batchSingleUpdate()
    → .update() → .sets() → .where() → .asUpdate()           // → _BatchUpdateParamSpec（非 BatchUpdate！）
    → .namedParamList(paramList)                              // → BatchUpdate ✅

SQLs.batchSingleDelete()
    → .deleteFrom() → .where() → .asDelete()                  // → _BatchDeleteParamSpec（非 BatchDelete！）
    → .namedParamList(paramList)                              // → BatchDelete ✅
```

```java
// ❌ 错误：.asUpdate() 不返回 BatchUpdate，编译错误
final BatchUpdate stmt;
stmt = SQLs.batchSingleUpdate()
        .update(...).sets(...).where(...)
        .asUpdate();  // ← 返回 _BatchUpdateParamSpec，不是 BatchUpdate

// ❌ 错误：stmt 不是 BatchUpdate，不能调用 .namedParamList()
session.update(stmt.namedParamList(paramList));

// ✅ 正确：.namedParamList() 链接在 .asUpdate() 后
final BatchUpdate stmt;
List<Map<String, Object>> paramList = List.of(...);
stmt = SQLs.batchSingleUpdate()
        .update(Stock_.T, AS, "s")              // 需先定义 paramList
        .sets(pairs -> pairs
                .setSpace(Stock_.name, SQLs::namedParam)
                .setSpace(Stock_.offerPrice, SQLs::namedLiteral)
        )
        .where(Stock_.id.spaceEqual(SQLs::namedParam))
        .asUpdate()
        .namedParamList(paramList);  // ← 必须在链中，返回 BatchUpdate

// 执行时直接传入 stmt
long rows = session.update(stmt);
```

**验证方法**：写 batch 操作示例前，必须读取 `.asUpdate()` / `.asDelete()` 的返回类型接口定义，确认是否已包含
`namedParamList()`。可以从单元测试中找参考（如 `StandardDeleteUnitTests.batchSingleDelete()`）。

### Rule 9: Function Availability — 标准 vs 方言函数必须区分 (MANDATORY)

**不能因为某个函数是常见 SQL 函数，就假设它在标准 API 中存在。** 必须从源码确认函数属于标准 API（`Functions.java`）还是方言
API（`MySQLTimeFunctions.java`、`PostgreStringFunctions.java` 等）。

| 函数类型 | 标准 API (`SQLs.xxx()`)                                                                                            | 方言 API (`MySQLs.xxx()` / `Postgres.xxx()`)                    |
|------|------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------|
| 数学   | `acos`, `asin`, `atan`, `ceil`, `cos`, `floor`, `ln`, `log`, `mod`, `pow`, `round`, `sign`, `sqrt`, `truncate` 等 | —                                                             |
| 字符串  | `length`, `lower`, `upper`, `trim`, `substring`                                                                  | `concat`（仅方言）、`replace` 等                                     |
| 日期时间 | —                                                                                                                | `extract`, `currentDate`, `currentTime`, `currentTimestamp` 等 |
| 条件   | —                                                                                                                | `coalesce`, `nullif`, `greatest`, `least` 等                   |
| CASE | `cases()` / `cases(expr)`                                                                                        | —                                                             |

**判例**：

- `SQLs.concat()` ❌ — `concat` 只存在于 `MySQLStringFunctions` / `PostgreStringFunctions`，不在 `Functions.java`
- `SQLs.extract()` ❌ — `extract` 只存在于 `MySQLTimeFunctions` / `PostgreDateTimeFunctions`，不在 `Functions.java`
- `SQLs.WordYear.YEAR` ❌ — `WordYear` 不存在于 `SQLs` 类
- `SQLs::values` ❌ — `values()` 只存在于 `MySQLMiscellaneousFunctions`，通过 `MySQLs::values` 访问
- `SQLs::excluded` ❌ — `excluded()` 只存在于 `PostgreSyntax`，通过 `Postgres::excluded` 访问
- `SQLs.round()` ✅ — 存在于 `Functions.java`
- `SQLs.length()` ✅ — 存在于 `Functions.java`

**规则**：文档中列举可用函数时，每个函数名必须通过源码验证。标准 API 示例只能使用 `Functions.java`
中声明的函数。方言函数必须明确标注"via dialect API"。

**方言特殊操作符补充**：某些方言特有的操作符（如 `VALUES()`、`EXCLUDED`）用于 `INSERT ... ON DUPLICATE KEY UPDATE` 或
`ON CONFLICT ... DO UPDATE` 的 `.set()` 子句中。它们**不在** `SQLs` 中，必须通过对应的方言入口类引用：

| 操作符            | 方言         | 正确引用                 | 错误引用               | 所在类（继承链）                                                 |
|----------------|------------|----------------------|--------------------|----------------------------------------------------------|
| `VALUES(col)`  | MySQL      | `MySQLs::values`     | `SQLs::values` ❌   | `MySQLMiscellaneousFunctions` → `MySQLSyntax` → `MySQLs` |
| `EXCLUDED.col` | PostgreSQL | `Postgres::excluded` | `SQLs::excluded` ❌ | `PostgreSyntax` → `Postgres`                             |

## 常见错误模式速查

### ❌ 错误：把单参函数当作 BiFunction

```java
// SQLs.literalValue 只有一个参数，不是 BiFunction！
greater(SQLs::literalValue, 100)       // ❌
then(SQLs::literalValue, "HIGH")        // ❌
round(x, SQLs::literalValue, 2)         // ❌
lag(x, SQLs::literalValue, 1, ...)      // ❌
```

### ✅ 正确：用原始值让 DEFAULT mode 自动处理

```java
greater(100)                    // ✅ DEFAULT mode auto-wraps
then("HIGH")                    // ✅
round(x, 2)                     // ✅
lag(x, 1, 0)                    // ✅
```

### ❌ 错误：直接引用 package-private 类

```java
// 任何非 public 的类，不可被外部包直接访问！
// 必须沿继承链找到 public 子类
Functions.countAsterisk()       // ❌ Functions 是 package-private，SQLs 是 public
Functions.sum(expr)             // ❌
Functions.cases()               // ❌
```

### ✅ 正确：通过 public 类调用

```java
SQLs.countAsterisk()            // ✅ SQLs 是 Functions 的 public 子类
SQLs.sum(expr)                  // ✅
SQLs.cases()                    // ✅
```

### ❌ 错误：Batch 操作 `asUpdate()`/`asDelete()` 后未链接 `namedParamList()`

```java
// .asUpdate() 返回 _BatchUpdateParamSpec，不是 BatchUpdate！
final BatchUpdate stmt;
stmt = SQLs.batchSingleUpdate()
        .update(...).sets(...).where(...)
        .asUpdate();                   // ❌ 类型不匹配

// 同理 .asDelete() 返回 _BatchDeleteParamSpec，不是 BatchDelete
final BatchDelete stmt;
stmt = SQLs.batchSingleDelete()
        .deleteFrom(...).where(...)
        .asDelete();                   // ❌ 类型不匹配
```

### ✅ 正确：`.namedParamList()` 链接在 `.asUpdate()`/`.asDelete()` 后

```java
// paramList 必须先定义，才能传入链式调用
final BatchUpdate stmt;
List<Map<String, Object>> paramList = List.of(...);
stmt = SQLs.batchSingleUpdate()
        .update(...).sets(...).where(...)
        .asUpdate()
        .namedParamList(paramList);     // ✅ 返回 BatchUpdate

// 执行
session.update(stmt);                  // ✅ stmt 是 BatchUpdate
```

### ❌ 错误：方言特殊操作符使用了错误的入口类

```java
// MySQL ON DUPLICATE KEY UPDATE: VALUES(col) 操作符不在 SQLs 中！
stmt = MySQLs.singleInsert()
        .insertInto(Stock_.T).values(stockList)
        .onDuplicateKeyUpdate()
        .set(Stock_.offerPrice, SQLs::values, Stock_.offerPrice)   // ❌ SQLs 没有 values()
        .asInsert();

// PostgreSQL ON CONFLICT DO UPDATE: EXCLUDED.col 操作符不在 SQLs 中！
stmt = Postgres.singleInsert()
        .insertInto(Stock_.T).values(stockList)
        .onConflict(Stock_.name).doUpdate()
        .set(Stock_.offerPrice, SQLs::excluded, Stock_.offerPrice)  // ❌ SQLs 没有 excluded()
        .returning(Stock_.id, Stock_.name)
        .asInsert();
```

### ✅ 正确：使用方言入口类引用

```java
// MySQL: 使用 MySQLs::values
stmt = MySQLs.singleInsert()
        .insertInto(Stock_.T).values(stockList)
        .onDuplicateKeyUpdate()
        .set(Stock_.offerPrice, MySQLs::values, Stock_.offerPrice)  // ✅
        .asInsert();

// PostgreSQL: 使用 Postgres::excluded
stmt = Postgres.singleInsert()
        .insertInto(Stock_.T).values(stockList)
        .onConflict(Stock_.name).doUpdate()
        .set(Stock_.offerPrice, Postgres::excluded, Stock_.offerPrice)  // ✅
        .returning(Stock_.id, Stock_.name)
        .asInsert();
```

## 验证 Checklist

写完示例代码后：

- [ ] 每个 API 方法的签名已从源码确认
- [ ] 所有 Method Reference 的签名与目标函数接口匹配
- [ ] 没有将 `SQLs::literalValue` 当作 BiFunction 使用
- [ ] 示例代码中出现的每个类名已确认是 `public` 的，package-private 类已替换为 public 子类
- [ ] Batch 操作的 `.namedParamList(paramList)` 已链接在 `.asUpdate()`/`.asDelete()` 之后
- [ ] 方言特殊操作符（`values`, `excluded`）使用了正确的方言入口类（`MySQLs::values`, `Postgres::excluded`），而非
  `SQLs::xxx`
- [ ] 在 DEFAULT mode 场景中没有多余的 `SQLs.literalValue()` 调用
- [ ] 示例代码可以编译通过（Java 语法无误）

## Skill 进化机制

本 Skill 是**活的文档**。以下情况必须更新：

1. **发现新错误模式**：当发现现有规则未覆盖的示例代码错误时，新增错误模式条目
2. **API 签名变更**：当 Army 框架 API 签名发生变化时，更新对应规则
3. **规则不够精确**：当现有规则描述与框架实际行为不一致时，修正并细化
4. **新增正确的 Method Reference**：当新增可在 BiFunction 上下文使用的函数时，更新 Rule 2 表格
