---
name: army-sql-generation
description: Generate database dialect SQL from Army Criteria API statement examples in asciidoc/index.adoc. Use when the user asks to add, update, or fix SQL blocks below Java statement examples in the Army documentation.
---

# Army SQL Generation

> **适用范围**: 本 Skill **仅限**用于 `asciidoc/index.adoc` 文档，为 Army Criteria API 的 Statement 示例生成对应的数据库方言
> SQL。不得用于其它文档或场景。

## Quick Start

When adding SQL below a statement example in `asciidoc/index.adoc`:

1. Identify the Java statement block (`[source,java]`) and dialect (MySQL/PostgreSQL)
2. **Check the document's Domain class declaration**:
    - **No `extends`** → use document's field order as-is (see R3 情况 1)
    - **Has `extends`** → read actual source code inheritance chain for full field order (see R3 情况 2, R8)
3. Apply the core rules below to generate parameterized SQL
4. Place the `.Generated SQL (dialect)` block immediately after the closing `----` of the Java block

Output format template:

```
.Generated SQL (PostgreSQL)
[source,sql]
----
-- brief comment explaining key behavior
STATEMENT_HEADER
COLUMNS_OR_CLAUSES
----
```

> **方言选择的铁律**: 模板中的 `(PostgreSQL)` 是默认值——详见 R7。

## Core Rules

### R0: Parameter Binding Mode — DEFAULT / LITERAL / CONST

Army 支持三种值绑定模式，对应 `SQLs` 中不同的 API 方法族。所有 SQL 默认使用 **DEFAULT** 模式（JDBC `?` 参数化）。

**API 到 SQL 输出的映射**：

[cols="1,1,2,3",options="header"]
|===
| 模式 | API 方法族 | SQL 输出 | 典型场景

| **DEFAULT** (参数化) | `SQLs.parameter()` + `SQLs.param()` + `SQLs.namedParam()` + `SQLs.rowParam()` +
`SQLs.namedRowParam()` | `?` | **默认模式**，所有用户数据，最安全
| **LITERAL** (带类型前缀) | `SQLs.literalValue()` + `SQLs.literal()` + `SQLs.namedLiteral()` + `SQLs.rowLiteral()` +
`SQLs.namedRowLiteral()` | 值 + 类型前缀 | 系统常量、配置键，需类型安全的场景
| **CONST** (无类型前缀) | `SQLs.constValue()` + `SQLs.constant()` + `SQLs.namedConst()` + `SQLs.rowConst()` +
`SQLs.namedRowConst()` | 纯值 | 系统常量、测试数据，无需类型注解
|===

**Row 方法族的 SQL 输出模式**：

- `rowParam(type, values)` → 输出 `? , ? , ? ...`；在 `IN` 操作符右侧 → `( ? , ? , ? ... )`
- `rowLiteral(type, values)` → 输出带类型前缀的字面量序列；在 `IN` 中 → 带括号
- `rowConst(type, values)` → 输出纯值序列（无类型前缀）
- `namedRowParam(type, name, size)` → 输出 `? , ? , ? ...`，执行时按 `name[i]` 从 batch 行数据取值
- `namedRowLiteral(type, name)` → 生成时从 batch 行数据读取集合，输出带类型前缀的字面量序列（**仅 VALUES INSERT**；batch
  UPDATE/DELETE 抛 `CriteriaException`）
- `namedRowConst(type, name)` → 同上，但无类型前缀（**仅 VALUES INSERT**；batch UPDATE/DELETE 抛 `CriteriaException`）

**Named* INSERT-Only 限制（MANDATORY）**：

`SQLs.namedLiteral()`、`SQLs.namedConst()`、`SQLs.namedRowLiteral()`、`SQLs.namedRowConst()` 四个方法**仅限** VALUES INSERT
语句使用。在 batch UPDATE 或 batch DELETE 中使用会在运行时抛出 `CriteriaException`。SQL 生成时遇到这些方法出现在非 INSERT
上下文中属于错误。

**literal vs const 在各方言中的 SQL 差异**：

[cols="1,3,3,3",options="header"]
|===
| 值类型 | PostgreSQL `literal` | PostgreSQL `const` | MySQL `literal`/`const`

| `Integer` | `100::INTEGER` | `100` | `100` (无差异)
| `BigDecimal` | `100.00::DECIMAL` | `100.00` | `100.00` (无差异)
| `String` | `'hello'::VARCHAR` | `'hello'` | `'hello'` (无差异)
| `LocalDate` | `DATE '2024-01-01'` | `'2024-01-01'` | `DATE '2024-01-01'` / `'2024-01-01'`
| `LocalDateTime` | `TIMESTAMP '...'` | `'...'` | `TIMESTAMP '...'` / `'...'`
|===

**关键规则**：

- **DEFAULT mode**: 所有值输出 `?`。**所有方言统一使用 `?`，包括 PostgreSQL（不是 `$1`/`$2`）**。使用 `-- param: value`
  注释标注显式参数绑定
- **LITERAL mode**: 值嵌入 SQL 并带类型前缀。PostgreSQL 用 `::TYPE` 语法；MySQL 仅对日期时间类型加前缀
- **CONST mode**: 值嵌入 SQL 不含类型前缀。所有方言行为一致（仅输出原始值）
- **`UPDATE_TIME_PLACEHOLDER`**: Army 根据语句中是否已有参数决定输出 `?` 还是 `CURRENT_TIMESTAMP`
- **`BATCH_NO_PARAM/LITERAL/CONST`**: 仅用于 batch 操作，输出当前批次行号（1-based）

**方法引用与 BiFunction 重载**：

[cols="1,3,3,3",options="header"]
|===
| API 方法 | 配合的接口 | 方法引用写法 | 参数来源

| `SQLs.param(TypeInfer, Object)`
| `TypedExpression`
| `field.equal(SQLs::param, value)`
| ① `TypeInfer` ← 字段自身 (left side)
② `Object` ← 用户值 (right side)

| `SQLs.namedParam(TypeInfer, String)`
| `TypedField`
| `field.spaceEqual(SQLs::namedParam)`
| ① `TypeInfer` ← 字段自身
② `String` ← `fieldName()` 作为 batch key

| `SQLs.literal(TypeInfer, Object)`
| `TypedExpression`
| `field.equal(SQLs::literal, value)`
| ① `TypeInfer` ← 字段自身 (left side)
② `Object` ← 用户值 (right side)

| `SQLs.namedLiteral(TypeInfer, String)`
| `TypedField`
| `field.spaceEqual(SQLs::namedLiteral)`
| ① `TypeInfer` ← 字段自身
② `String` ← `fieldName()` 作为 batch key

| `SQLs.constant(TypeInfer, Object)`
| `TypedExpression`
| `field.equal(SQLs::constant, value)`
| ① `TypeInfer` ← 字段自身 (left side)
② `Object` ← 用户值 (right side)

| `SQLs.namedConst(TypeInfer, String)`
| `TypedField`
| `field.spaceEqual(SQLs::namedConst)`
| ① `TypeInfer` ← 字段自身
② `String` ← `fieldName()` 作为 batch key

| `SQLs.rowParam(TypeInfer, Collection<?>)`
| `TypedExpression`
| `field.in(SQLs::rowParam, list)`
| ① `TypeInfer` ← 字段自身 (left side)
② `Collection<?>` ← 用户列表 (right side)

| `SQLs.rowLiteral(TypeInfer, Collection<?>)`
| `TypedExpression`
| `field.in(SQLs::rowLiteral, list)`
| ① `TypeInfer` ← 字段自身
② `Collection<?>` ← 用户列表

| `SQLs.rowConst(TypeInfer, Collection<?>)`
| `TypedExpression`
| `field.in(SQLs::rowConst, list)`
| ① `TypeInfer` ← 字段自身
② `Collection<?>` ← 用户列表
|===

**Row 方法引用的 SQL 输出示例**：

[source,sql]
----
-- rowParam + IN: 每个元素对应一个 ?
id IN (?, ?, ?, ?)

-- rowLiteral + IN (PostgreSQL): 带类型前缀
id IN (1::INTEGER, 2::INTEGER, 3::INTEGER)

-- rowConst + IN: 无类型前缀
id IN (1, 2, 3)

-- namedRowLiteral in VALUES INSERT (PostgreSQL):
INSERT INTO article(id, tag) VALUES (1, 'java'::VARCHAR, 'spring'::VARCHAR)
----

**核心区别**：

- **`TypedExpression` 重载** (`equal(BiFunction<TypedExpression, T, Expression>, T value)`)：用户传入 value，字段自身提供
  TypeInfer；用于单行操作，可覆盖全局 `LiteralMode`
- **`TypedField` space\* 方法** (`spaceEqual(BiFunction<TypedField, String, Expression>)`)：用户不传值，框架自动用
  `fieldName()` 作为 batch key；用于 batch 操作，SQL 中输出 `?`/literal/const，执行时按 key 从 batch 行数据取值
- **`set()` vs `setSpace()` for batch UPDATE SET**：`set()` 接受 `(field, Object)` 或
  `(field, BiFunction<F,E,AssignmentItem>, E)`，**不接受** `BiFunction<F,String,Expression>`。named 参数（
  `SQLs::namedParam`/`SQLs::namedLiteral`/`SQLs::namedConst`）**必须**通过 `setSpace(field, funcRef)` 传入。`where()` 中
  named 参数通过 `field.spaceEqual(SQLs::namedParam)` 直接调用（返回 `IPredicate`，传 `where(IPredicate)`），**不**使用
  `where(::spaceEqual, ...)` 形式。
- **`namedRowParam` 不使用 BiFunction 形式**：`namedRowParam` 有 3 个参数 `(TypeInfer, String, int)`，不能作为方法引用。在
  batch UPDATE SET 中需直接调用并通过 `set(field, Object)` 传入表达式。

> **占位符铁律**: Army 的 `ParamExpression` 在 SQL 中**始终输出 `?`**，方言无关。`StatementContext.appendParam()` 统一追加
`?` 到 SQL；方言 parser 不参与占位符格式决策。禁止在任何方言 SQL 中出现 `$1`、`$2` 等 PostgreSQL 原生占位符语法。

> **源码依据**: `ArmyLiteralExpression` 通过 `typeName` boolean 控制是否输出类型前缀；
> `PostgreParser.bindLiteral` 根据 `typeName` 决定是否追加 `::TYPE`；
> `MySQLParser.bindLiteral` 根据 `typeName` 决定是否追加 `TIMESTAMP ` / `DATE ` 等前缀。

> **安全机制**: Army 的字面量**不是**简单的字符串拼接，所有字面量最终通过
`io.army.dialect.ArmyParser#safeLiteral(TypeMeta, Object, boolean, StringBuilder)` 方法输出到
> SQL。该方法对字符串值进行正确的引号转义，数值类型天然继承自 `_ArmyNoInjectionType` 杜绝注入，因此 `literal`/`const` 模式与
`DEFAULT` 参数化一样安全。

### R1: Column Name Conversion

Java camelCase field names are converted to snake_case:

- `offerPrice` → `offer_price`
- `listingDate` → `listing_date`
- `createTime` → `create_time`
- `updateTime` → `update_time`

### R2: Army-Managed Fields (MANDATORY)

**Army 管理的字段始终出现在 INSERT 列列表中**，无论何种 INSERT 模式：

| 管理字段                 | 触发条件                     | INSERT 行为                              |
|----------------------|--------------------------|----------------------------------------|
| `id`                 | 有 `@Generator` 且非 `POST` | 始终在 INSERT 列中，值由 Generator 产生          |
| `createTime`         | 字段存在                     | 始终在 INSERT 列中，自动设为当前时间                 |
| `updateTime`         | 字段存在                     | 始终在 INSERT + 每次 UPDATE                 |
| `version`            | 字段存在                     | 始终在 INSERT（初始为 `0`）+ 每次 UPDATE         |
| `visible`            | 字段存在                     | 软删除标记，始终在 INSERT（初始 `true`）            |
| Discriminator        | `@Inheritance` 子类        | 始终在 INSERT 列中，值为 `@DiscriminatorValue` |
| 被 `@Generator` 注解的字段 | 字段有 `@Generator`         | 始终在 INSERT 列中，值由 Generator 产生          |

**关键规则**：

- Domain mode / Value mode：所有字段（含管理字段）按文档声明顺序排列
- **Query mode**：用户通过 `parens()` 显式指定的列**在前**，**用户未指定的** Army 管理字段**追加到列表后面**。
  若用户在 `parens()` 中已显式写了某个管理字段（如 `Stock_.id`），则框架**不重复追加**该字段。

### R3: INSERT Column Order (Domain Mode)

**CRITICAL**: When no explicit field list is specified (domain mode INSERT), column order follows the **field
declaration order** in the document's Domain class example — **top to bottom, exactly as declared in the document**.

**判断依据是文档中类的声明形式，分两种情况**：

#### 情况 1: 文档中的类是独立 POJO（无 `extends`）

字段顺序 = 文档中声明顺序，字段列表 = 文档中声明的字段（不多不少）。**严禁**凭空添加文档中不存在的字段，也**严禁**
按实际源码的继承链重排顺序。

```java
// 文档中的 Stock 示例 — 独立 POJO，无继承
@Table(name = "stock", ...)

public class Stock {
    public long id;
    public LocalDateTime createTime;
    public LocalDateTime updateTime;
    public int version;
    public String exchange;
    public String code;
    public String name;
    public StockStatus status;
    public BigDecimal offerPrice;
    public LocalDate listingDate;
}
```

对应 SQL（列顺序 = 文档声明顺序，10 列）：

```sql
INSERT INTO stock (id, create_time, update_time, version, exchange,
                   code, name, status, offer_price, listing_date)
VALUES (?, ?, ?, ?, ?,
        ?, ?, ?, ?, ?)
```

#### 情况 2: 文档中的类有 `extends`（带继承）

列顺序按**实际源码**的继承链「基类 → 子类、自上而下」排列，需要读取所有 `@MappedSuperclass` 祖先的源码来确定完整的字段顺序（参见
R8）。

> **核心原则**: 文档中 Domain 类的定义是 SQL 生成的**唯一上下文**。字段列表 = 文档中出现的字段（不增不减），字段顺序 =
> 文档中的声明顺序。不要用实际源码去"纠正"文档示例的字段列表或顺序。

### R4: POST Generator ID Exclusion

When `id` has `@Generator` with `GeneratorType.POST` (database auto-increment), `id` is **NOT** included in the INSERT
column list — **this rule applies ONLY to non-child tables** (parent tables and simple/standalone tables).

**Child tables ALWAYS include `id`** regardless of `GeneratorType`, because `InsertSupports.createFieldMap()`
always calls `insertTable.id()` for child tables with no POST exclusion logic.

```sql
-- Simple/Parent table with POST id: no id column
INSERT INTO stock (exchange, code, name, ...)
VALUES (?, ?, ?, ...)

-- PRECEDE id (Snowflake etc.): id IS included (all table types)
INSERT INTO stock (id, exchange, code, name, ...)
VALUES (?, ?, ?, ?, ...)

-- Child table with POST id: id STILL included (child always includes id)
INSERT INTO u_admin (id, admin_level, role, status)
VALUES (?, ?, ?, ?)
```

**Scope**: Domain mode, value mode, dynamic mode, query mode — all equally, but only for non-child tables.

**Note**: In the current documentation, `Stock.id` uses `@Generator("Snowflake8Generator")` which is `PRECEDE` — id IS
included in all Stock INSERT examples.

### R5: Single-Table Inheritance (Parent-Child INSERT)

For `@Inheritance` parent/child tables — the `child()` chain produces **two separate INSERT statements**.

#### R5a: Parent Table vs Child Table — DIFFERENT table names

Parent table and child table **MUST use different table names** in SQL:

- Parent: table name from `ParentTableMeta` (e.g., `u_parent_user`)
- Child: table name from `ChildTableMeta` (e.g., `u_admin`)

```sql
-- ✅ Correct: parent and child use DIFFERENT tables
INSERT INTO u_parent_user (id, create_time, update_time, version, name)
VALUES (?, ?, ?, ?, ?);

INSERT INTO u_admin (id, admin_level, role, status)
VALUES (?, ?, ?, ?);

-- ❌ Wrong: parent and child use SAME table name
INSERT INTO u_user (id, name, create_time, update_time, version)
VALUES (?, ?, ?, ?, ?);

INSERT INTO u_user (id, name, status, create_time, update_time, version)
VALUES (?, ?, ?, ?, ?, ?);
```

#### R5b: Parent INSERT Column Order

Parent INSERT follows `InsertSupports.createFieldMap()` order:

1. **Army-managed fields**: `id`, `createTime`, `updateTime`, `version` (from `_MetaBridge.RESERVED_FIELDS`)
2. **Discriminator column** (if applicable — NOT for parent context, only for child)
3. **Business fields** (from `fieldChain()`, in declaration order)

```sql
-- Parent INSERT: managed fields first, then business fields
INSERT INTO u_parent_user (id, create_time, update_time, version, name)
VALUES (?, ?, ?, ?, ?);
```

#### R5c: Child INSERT Column Order

Child INSERT follows `InsertSupports.createFieldMap()` order — **different from parent**:

1. **`id` only** — child tables auto-add `id` (from `insertTable.id()`), but do NOT add `createTime`/
   `updateTime`/`version` (those are parent-only managed fields per `_MetaBridge.RESERVED_FIELDS`)
2. **Child-specific business fields** (from `fieldChain()`, in declaration order)
3. **Discriminator column** — auto-injected at the end (value from `@DiscriminatorValue`)

```sql
-- Child INSERT: id first, child-only fields, discriminator last
INSERT INTO u_admin (id, admin_level, role, status)
VALUES (?, ?, ?, ?);
```

#### R5d: Non-Overlapping Fields Rule

Child table fields (except `id`) **MUST NOT** overlap with parent table fields. The child should have
only fields unique to the subclass. This is because:

- In single-table inheritance, parent and child **share** the physical table — parent fields ARE the table's fields
- Child-specific fields are the subclass's own additions
- `id` is the exception: both parent and child have it, but it's managed by Army

> **NOTE**: In `InsertSupports.createFieldMap()`, the parent path adds `_MetaBridge.RESERVED_FIELDS`
> (`id`, `createTime`, `updateTime`, `version`), while the child path calls `insertTable.id()` only —
> child tables do NOT auto-add `createTime`/`updateTime`/`version`.

### R6: Enum Type Values

Enum values in SQL use their database representation (typically the enum constant name as a string in DEFAULT mode).
Since default is parameterized, use `?` with a comment.

```sql
-- defaultValue(Stock_.status, StockStatus.NORMAL) means:
-- when domain object's status is null → 'NORMAL' is substituted per-row
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)  -- status uses ? or 'NORMAL' when null
```

### R7: Standard API → PostgreSQL Dialect (with Domain DML Exception)

**标准 Statement API（`SQLs.xxx()` 入口）生成的 SQL 默认使用 PostgreSQL 方言。**

**例外**: domain UPDATE/DELETE 操作 `ChildTableMeta` 时，PostgreSQL 与 MySQL 的 SQL 结构**根本上不同**（CTE vs
MULTI_TABLE），
因此两者都需提供 SQL 块。规则参见 D3-D5。

判定入口：

- 标准 API 入口：`SQLs.query()`, `SQLs.singleInsert()`, `SQLs.singleUpdate()`, `SQLs.singleDelete()`,
  `SQLs.domainUpdate()`, `SQLs.domainDelete()`, `SQLs.batchSingleUpdate()`, `SQLs.scalarSubQuery()`, `SQLs.subQuery()`
- 方言 API 入口：`MySQLs.xxx()` → MySQL 方言；`Postgres.xxx()` → PostgreSQL 方言

**规则**：

- 标准 API → `.Generated SQL (PostgreSQL)` 标签，SQL 语法遵循 PostgreSQL
- **Domain DML（`domainUpdate()` / `domainDelete()`）操作 `ChildTableMeta` 时**：
  同时提供 `.Generated SQL (PostgreSQL)` 和 `.Generated SQL (MySQL)` 两个 SQL 块（参见 D3-D5）
- `MySQLs.xxx()` → `.Generated SQL (MySQL)` 标签，可使用 MySQL 特有语法
- `Postgres.xxx()` → `.Generated SQL (PostgreSQL)` 标签，可使用 PostgreSQL 特有语法
- **MySQL 特有语法**（如 `ON DUPLICATE KEY UPDATE`、backtick 引用）**只能**出现在 `MySQLs.xxx()` 对应的 SQL 块中
- **PostgreSQL 特有语法**（如 `ON CONFLICT ... DO UPDATE`、`RETURNING`、`::type` 转换）出现在标准 API 或 `Postgres.xxx()`
  对应的 SQL 块中

## INSERT Mode-Specific Rules

### Domain Mode INSERT

- No explicit column list in Java → columns determined by field declaration order
- All fields with `@Column` annotation are included
- Reserved fields auto-populated by framework
- `defaultValue()` creates per-row conditional substitution

### Value Mode INSERT (Static)

- **No explicit `parens()` column-list** → column list is ALL table fields (from `SQLStmts.castFieldList(insertTable)`),
  including Army-managed fields (id, createTime, updateTime, version, visible) and discriminator fields
- **Unset nullable fields → `DEFAULT` keyword** (when `nullHandleMode=DEFAULT` and database ≠ SQLite, per
  `ValuesSyntaxInsertContext.handleNullMode`)
- `space()`/`comma()` set the VALUE expression for a field position; unset fields become `DEFAULT`
- `spaceIf()` / `ifComma()` are conditional for whether the VALUE is set; the COLUMN **always** appears in the INSERT
  list
- Multi-row: each `parens()` is one VALUES row; each row has the same number of VALUES items (one per column), but items
  may be `?` or `DEFAULT` depending on whether the field was set

**CRITICAL**: Value mode INSERT is always a **full-field INSERT** — unlike domain mode where the column list also
matches all fields, value mode column list is determined by the table's `fieldList()`, not by which fields the user
explicitly sets in `parens()`.

### Value Mode INSERT (Dynamic)

- **Same as static value mode**: column list is ALL table fields (full-field INSERT)
- `set()` and `ifSet()` determine whether a column has a VALUE expression or `DEFAULT` — the column itself is **always**
  in the INSERT list
- Rows **cannot** differ in column count because the column list is fixed (full table fieldList)
- Unset fields per-row → `DEFAULT` keyword in that row's VALUES
  ```sql
  INSERT INTO stock (id, create_time, update_time, version, exchange,
                     code, name, status, offer_price, listing_date)
  VALUES (?, ?, ?, ?, ?, ?, ?, DEFAULT, ?,           -- row 1: offer_price set
          DEFAULT),
         (?, ?, ?, ?, ?, ?, ?, DEFAULT, DEFAULT,     -- row 2: offer_price not set → DEFAULT
          DEFAULT)
  ```

### Query Mode INSERT

- Column list specified via `parens(Consumer<_StaticColumnSpaceClause>)` or omitted
- Source is a SELECT query (inline or pre-built)
- **Army-Managed Field Rule**: When `parens()` specifies columns, Army-managed fields (see R2) that were **not already
  specified by the user** are **appended after** user-specified columns. NEVER duplicate a field that the user already
  included.
- **Column count matching**: The INSERT column list and the SELECT output must have the same number of columns.
  Fields in the INSERT column list that are NOT in the user's SELECT are filled with `DEFAULT` keyword.
- **CRITICAL**: In `INSERT ... SELECT`, SQL syntax dictates that the SELECT statement determines ALL column values —
  this cannot be changed. Army only appends column names to the INSERT list; it does NOT generate values in query
  mode. `DEFAULT` in the SELECT always means the **database column's default value**.

  ```sql
  -- User specified: exchange, code, name
  -- Army-managed fields: id, create_time, update_time, version → appended after
  INSERT INTO stock (exchange, code, name, id, create_time,
                     update_time, version)
  SELECT sub.exchange, sub.code, sub.name, DEFAULT, DEFAULT,
         DEFAULT, DEFAULT
  FROM ...
  -- id, create_time, update_time, version: DB column default (all values from subquery)
  ```

  ```sql
  -- Form 4 example: status is a user field in parens() but not in SELECT
  INSERT INTO stock (exchange, code, name, status, id, create_time,
                     update_time, version)
  SELECT n.exchange, n.code, n.name, DEFAULT, DEFAULT, DEFAULT,
         DEFAULT, DEFAULT
  FROM nasdaq_table AS n
  -- status: DB column default (not in user SELECT)
  -- id, create_time, update_time, version: DB column default (appended by Army, all values from subquery)
  ```
- When column list is omitted entirely, SELECT determines all columns

> **NOTE**: In Query mode INSERT, Army only appends managed field column names to the INSERT list. ALL column values
> are determined by the SELECT statement — `DEFAULT` in SELECT means the database column's default value. There is
> no distinction between "Army-generated" and "DB default" in this mode; every `DEFAULT` is a SQL-level keyword
> that invokes the column's database default.

## SELECT Rules

- Column list follows `select()` argument order
- `AS "alias"` when alias is specified
- JOIN clause order: `FROM → JOIN → ON → JOIN → ON ...`
- WHERE expressions are straightforward translations of the Criteria chain

## UPDATE Rules

- `SET` clauses: comma-separated, `column = ?` with param comments
- `WHERE` from the Criteria chain
- `updateTime` always auto-refreshed (appears in SET)
- `version` always auto-incremented (appears in SET as `version = version + 1`)
- **MUST also apply to dialect-specific conflict clauses**:
    - `ON DUPLICATE KEY UPDATE` (MySQL): `update_time = VALUES(update_time)`, `version = version + 1`
    - `ON CONFLICT ... DO UPDATE` (PostgreSQL): `update_time = ?`, `version = version + 1`

### UPDATE SET 列别名 — 方言差异 (MANDATORY)

`SET` 子句中目标列（`=` **左侧**）是否允许表别名是**方言性**的：

| 方言         | SET 左侧列带别名 | 说明                                    |
|------------|------------|---------------------------------------|
| PostgreSQL | ❌ 不允许      | `SET name = ?` ✅ / `SET s.name = ?` ❌ |
| MySQL      | ✅ 允许       | `SET s.name = ?` 合法                   |

**PostgreSQL（标准 API 默认方言）**：

```sql
-- ✅ 正确：SET 左侧列无别名
UPDATE stock AS s
SET name = ?,
    offer_price = s.offer_price + ?,
    version = s.version + 1
WHERE s.id = ?

-- ❌ 错误：SET 左侧列带表别名（PostgreSQL 语法错误）
UPDATE stock AS s
SET s.name = ?,
    s.offer_price = s.offer_price + ?
WHERE s.id = ?
```

**MySQL**：

```sql
-- ✅ 正确：MySQL 允许 SET 左侧列表别名
UPDATE stock AS s
SET s.name = ?,
    s.offer_price = s.offer_price + ?
WHERE s.id = ?
```

**适用范围**：`singleUpdate`、`domainUpdate`、`batchSingleUpdate` 等所有 UPDATE 形式。`WHERE` 子句中的列引用不受此规则限制。

## DELETE Rules

- Soft delete (if visible field present): actually an UPDATE setting `visible = false`
- Hard delete: `DELETE FROM table WHERE ...`

## Domain UPDATE/DELETE — Child Table Rules (MANDATORY)

`SQLs.domainUpdate()` / `SQLs.domainDelete()` 和对应的 batch 变体当操作为 `ChildTableMeta` 时，
SQL 生成规则与普通单表 UPDATE/DELETE **完全不同**。这些规则通过阅读 `DialectParser` 源码推出，必须严格遵循。

### D1: Code Path Decision — childItemPairList() 决定路径

`handleDomainUpdate` 根据 `childItemPairList()` 是否为空选择不同的 SQL 生成路径：

```java
// ArmyParser.handleDomainUpdate 源码逻辑：
if (!(stmt.table() instanceof ChildTableMeta) || stmt.childItemPairList().isEmpty()) {
    // 路径 A: 无 child 字段 SET → 标准单表 UPDATE（操作父表）
    context = DomainUpdateContext.forSingle(outerContext, stmt, this, sessionSpec);
    this.parseStandardSingleUpdate(stmt, (_SingleUpdateContext) context);
} else if (mode == ChildUpdateMode.MULTI_TABLE) {
    // 路径 B: MySQL → multi-table UPDATE
    context = MultiUpdateContext.forChild(outerContext, stmt, this, sessionSpec);
    this.parseDomainChildUpdate(stmt, context);
} else if (mode == ChildUpdateMode.CTE) {
    // 路径 C: PostgreSQL → CTE-based UPDATE
    ...
    this.parseDomainChildUpdate(stmt, context);
}
```

- **childItemPairList() 为空**（只 SET 了 parent 字段）→ 路径 A：标准单表 UPDATE，目标表是**父表**
- **childItemPairList() 非空**（有 child 专属字段的 SET）→ 路径 B/C：CTE 或 multi-table

对于 DELETE，只要是 ChildTableMeta 就走 CTE/multi-table 路径，没有类似分支。

### D2: Target Table & Alias

当 `domainTable instanceof ChildTableMeta` 且 `stmt instanceof _DomainDml` 时：

- **目标表名**: PARENT 表的名称（如 `u_parent_user`），**不是** child 表（如 `u_admin`）
- **目标表别名**: `"p_of_" + userAlias`（由 `ArmyParser.parentAlias()` 生成），如用户传 `"a"` → 别名 `"p_of_a"`

```java
// SingleTableDmlContext 源码逻辑：
if (stmt instanceof _DomainDml && domainTable instanceof ChildTableMeta) {
    this.targetTable = ((ChildTableMeta<?>) this.domainTable).parentMeta();
    this.targetTableAlias = ArmyParser.parentAlias(this.domainTableAlias);
}
// parentAlias 实现：return "p_of_" + tableAlias;
```

### D3: Discriminator & Visible Auto-Injection

Domain UPDATE/DELETE 会自动在 WHERE 中注入：

| 注入条件             | 注入内容                                        | 注入位置     |
|------------------|---------------------------------------------|----------|
| 子表 domain UPDATE | `AND p_of_a.status = 'DISCRIMINATOR_VALUE'` | WHERE 末尾 |
| 子表 domain DELETE | `AND p_of_a.status = 'DISCRIMINATOR_VALUE'` | WHERE 末尾 |
| 父表有 visible 字段   | `AND p_of_a.visible = true`                 | WHERE 末尾 |

Discriminator 使用父表别名注入（如 `p_of_a.status = 'ADMIN'`），visible 同理。

### D3a: CTE WHERE Clause Order (MANDATORY)

PostgreSQL 的 CTE domain UPDATE/DELETE 中，`childDomainCteWhereClause` 生成的 WHERE 子句有**严格顺序**：

```java
// ArmyParser.childDomainCteWhereClause 源码
// ① JOIN 条件 — 总是最先输出
childContext.appendField(childTable.id());           // a.id
sqlBuilder.append(_Constant.SPACE_EQUAL);
childContext.appendField(childTable.parentMeta().id()); // = p_of_a.id
// ② 用户 WHERE — 追加在 JOIN 条件之后
for (int i = 0; i < predicateCount; i++) {
    sqlBuilder.append(_Constant.SPACE_AND);
    predicateList.get(i).appendSql(sqlBuilder, childContext);
}
// ③ discriminator — 在 childDomainCteWhereClause 之后调用
// ④ visible — 在 discriminator 之后调用
```

**CTE WHERE 完整顺序**（用户条件在前，Army 管理条件在后追加）：

| 序号 | 内容                                      | 来源                                              |
|----|-----------------------------------------|-------------------------------------------------|
| 1  | `a.id = p_of_a.id`                      | `childDomainCteWhereClause` 内置 JOIN 条件          |
| 2  | 用户 WHERE 谓词                             | `stmt.wherePredicateList()` — **在 Army 管理条件之前** |
| 3  | `p_of_a.status = 'DISCRIMINATOR_VALUE'` | `discriminator()` 自动**追加**                      |
| 4  | `p_of_a.visible = true`                 | `visiblePredicate()` 自动**追加**（若有 visible 字段）    |

**同样适用于 MySQL multi-table UPDATE/DELETE**：`dmlWhereClause`（用户 WHERE）→ `discriminator`（追加）→ `visiblePredicate`
（追加）。

**强制规则**: CTE/multi-table 的 WHERE 子句中，用户 WHERE 条件必须**在** discriminator 和 visible 之前，
因为 discriminator 和 visible 是 Army 框架在用户 WHERE 之后**追加**注入的。将 discriminator/visible 放在用户条件之前是严重错误。

**强制规则**: CTE 的 WHERE 子句中，`a.id = p_of_a.id` 必须始终作为**第一条** WHERE 条件出现，
它在用户 WHERE、discriminator、visible 之前。缺失此 JOIN 条件是严重错误。

此规则适用于:**所有** PostgreSQL 的 CTE domain UPDATE/DELETE，包括 batch 变体。

### D4: Domain UPDATE — Dialect Differences (MANDATORY)

当 **childItemPairList() 非空**时，两种方言生成**根本不同**的 SQL 结构：

| 行为                | PostgreSQL (CTE)                                                                                               | MySQL (MULTI_TABLE)                                                                   |
|-------------------|----------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| `childUpdateMode` | `CTE`                                                                                                          | `MULTI_TABLE`                                                                         |
| SQL 结构            | 两阶段 CTE: ① UPDATE child SET child_fields FROM parent + RETURNING id ② UPDATE parent SET parent_fields FROM cte | 单条 multi-table: `UPDATE child JOIN parent ON ... SET parent_fields, child_fields ...` |
| SET parent 字段     | 在主 UPDATE 中（无表别名，PostgreSQL 语法）                                                                                | 在 JOIN 后的 SET 中（有表别名 `p_of_a.`，MySQL 语法）                                              |
| SET child 字段      | 在 CTE 的 UPDATE 中                                                                                               | 在 JOIN 后的 SET 中（有表别名 `a.`）                                                            |
| SET 字段顺序          | parent 字段在 main，child 字段在 CTE                                                                                  | parent 字段 → child 字段 → updateTime → version                                           |

**PostgreSQL domain UPDATE（子表，有 child SET 字段）**:

```sql
-- domainUpdate().update(Admin_.T, AS, "a")
--   .set(Admin_.adminLevel, 5).set(ParentUser_.name, "Admin")
-- childItemPairList 非空 → CTE 路径
-- PostgreSQL 源码: PostgreParser.parseDomainChildUpdate
WITH a_update_cte AS (
    UPDATE u_admin AS a                    -- child table in CTE
    SET admin_level = ?                    -- child SET 字段 (childItemPairList)
    FROM u_parent_user AS p_of_a           -- parent table in CTE FROM
    WHERE a.id = p_of_a.id                  -- join condition (MUST be first)
    AND p_of_a.id = ?                    -- user WHERE
    AND p_of_a.status = 'ADMIN'            -- discriminator in CTE
    AND p_of_a.visible = true              -- visible in CTE
    RETURNING a.id AS id
)
UPDATE u_parent_user AS p_of_a             -- parent table in main
SET name = ?,                              -- parent SET 字段 (itemPairList)
    visible = ?,                           -- 无表别名（PostgreSQL 语法）
    update_time = ?,
    version = p_of_a.version + 1
FROM a_update_cte                          -- 引用 CTE 结果
WHERE p_of_a.id = a_update_cte.id          -- 用 RETURNING 的 id 定位父表行
```

**MySQL domain UPDATE（子表，有 child SET 字段）**:

```sql
-- domainUpdate().update(Admin_.T, AS, "a")
--   .set(Admin_.adminLevel, 5).set(ParentUser_.name, "Admin")
-- childItemPairList 非空 → MULTI_TABLE 路径
-- MySQL 源码: MySQLParser.parseDomainChildUpdate
UPDATE u_admin AS a                        -- child table (主表)
JOIN u_parent_user AS p_of_a               -- parent table (JOIN)
    ON a.id = p_of_a.id                    -- appendChildJoinParent 生成
SET p_of_a.name = ?,                       -- parent SET 字段 (itemPairList，先)
    p_of_a.visible = ?,                    -- 有表别名（MySQL 语法）
    a.admin_level = ?,                     -- child SET 字段 (childItemPairList，后)
    p_of_a.update_time = ?,                -- auto: updateTime（父表别名）
    p_of_a.version = p_of_a.version + 1    -- auto: version（父表别名）
WHERE p_of_a.id = ?                        -- user WHERE
AND p_of_a.status = 'ADMIN'                -- discriminator
AND p_of_a.visible = true                  -- visible
```

**重要**: 当 childItemPairList() 为空时，两种方言都走路径 A（标准单表 UPDATE 操作父表），不生成 CTE/multi-table。
文档应演示有 child 字段的主 use case。

### D5: Domain DELETE — Dialect Differences (MANDATORY)

DELETE 只要是 ChildTableMeta 就走 CTE/multi-table 路径，无需 childItemPairList 判断。

| 行为                | PostgreSQL (CTE)                                                              | MySQL (MULTI_TABLE)                                                                                          |
|-------------------|-------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| `childUpdateMode` | `CTE`                                                                         | `MULTI_TABLE`                                                                                                |
| SQL 结构            | 两阶段 CTE: ① DELETE child USING parent + RETURNING id ② DELETE parent USING cte | 单条多表 DELETE: `DELETE a, p_of_a FROM u_admin AS a JOIN u_parent_user AS p_of_a ON a.id = p_of_a.id WHERE ...` |
| 单表 DELETE 别名      | ✅ 支持 (`supportSingleDeleteAlias=true`)                                        | 仅 MySQL 8.0+ 支持                                                                                              |

**PostgreSQL domain DELETE（子表）**:

```sql
-- PostgreSQL 源码: PostgreParser.parseDomainChildDelete
WITH a_delete_cte AS (
    DELETE FROM u_admin AS a            -- child table in CTE
    USING u_parent_user AS p_of_a       -- parent table in CTE USING
    WHERE a.id = p_of_a.id              -- join condition (MUST be first)
    AND p_of_a.visible = ?            -- user WHERE
    AND p_of_a.status = 'ADMIN'         -- discriminator
    AND p_of_a.visible = true           -- visible
    RETURNING a.id AS id
)
DELETE FROM u_parent_user AS p_of_a     -- parent table in main
USING a_delete_cte
WHERE p_of_a.id = a_delete_cte.id
```

**MySQL domain DELETE（子表）**:

```sql
-- MySQL 源码: MySQLParser.parseDomainChildDelete
DELETE a, p_of_a                         -- 同时删除 child + parent 别名
FROM u_admin AS a                        -- child table
JOIN u_parent_user AS p_of_a             -- parent table via JOIN
    ON a.id = p_of_a.id                  -- appendChildJoinParent 生成
WHERE p_of_a.visible = ?                 -- user WHERE
AND p_of_a.status = 'ADMIN'              -- discriminator
AND p_of_a.visible = true                -- visible
```

### D6: Batch Domain DML

Batch domain UPDATE/DELETE 走与单条相同的方言路径。唯一区别是 WHERE 中使用 named param（`?`），
**且用户 WHERE 仍然在 discriminator/visible 之前**（Army 管理的条件是追加的）：

```sql
-- Batch UPDATE (PostgreSQL CTE): named param "id" 在 CTE WHERE 中
-- 注意: 用户 WHERE (p_of_a.id = ?) 在 discriminator/visible 之前
WITH a_update_cte AS (
    UPDATE u_admin AS a
    SET admin_level = ?           -- batch literal param
    FROM u_parent_user AS p_of_a
    WHERE a.id = p_of_a.id           -- join condition (MUST be first)
    AND p_of_a.id = ?             -- batch param: id (user WHERE BEFORE Army managed)
    AND p_of_a.status = 'ADMIN'   -- discriminator (appended)
    AND p_of_a.visible = true     -- visible (appended)
    RETURNING a.id AS id
)
UPDATE u_parent_user AS p_of_a
SET name = ?,                     -- batch param: name
    update_time = ?,
    version = p_of_a.version + 1
FROM a_update_cte WHERE p_of_a.id = a_update_cte.id

-- Batch UPDATE (MySQL multi-table):
-- 注意: 用户 WHERE (p_of_a.id = ?) 在 discriminator/visible 之前
UPDATE u_admin AS a
JOIN u_parent_user AS p_of_a ON a.id = p_of_a.id
SET p_of_a.name = ?,             -- batch param: name
    a.admin_level = ?,            -- batch literal param
    p_of_a.update_time = ?,
    p_of_a.version = p_of_a.version + 1
WHERE p_of_a.id = ?                 -- batch param: id (user WHERE BEFORE Army managed)
AND p_of_a.status = 'ADMIN'         -- discriminator (appended)
AND p_of_a.visible = true           -- visible (appended)
```

### D7: Domain API on Single Table

当 `domainUpdate()` / `domainDelete()` 操作的是 `SingleTableMeta`（非子表），走标准单表路径：

- UPDATE: 标准 `UPDATE table AS alias SET ... WHERE ...`（与 `singleUpdate` 相同）
- DELETE: 标准 `DELETE FROM table AS alias WHERE ...`（与 `singleDelete` 相同）
- 无 discriminator 注入（因为不是子表）
- SET 列别名仍遵循方言规则（R7 的 UPDATE SET 列别名表）

## Dialect-Specific Syntax

> **重要**: 以下方言特有语法**仅在对应方言 API 入口**（`MySQLs.xxx()` / `Postgres.xxx()`）的 SQL 块中使用。标准 API（
`SQLs.xxx()`）的 SQL 使用 PostgreSQL 通用语法，**不使用**任何方言特有语法（参见 R7）。

### MySQL

- INSERT: `INSERT INTO table (...) VALUES (...), (...)`
- ON DUPLICATE KEY UPDATE: after VALUES clause（**仅** `MySQLs.xxx()` 入口）
- LIMIT + OFFSET in SELECT
- Backtick quoting only when table/column is a reserved word

### PostgreSQL

- INSERT: `INSERT INTO table (...) VALUES (...), (...)`
- ON CONFLICT ... DO UPDATE: after VALUES clause（`Postgres.xxx()` 入口）
- `RETURNING` clause support（`Postgres.xxx()` 入口）
- `LIMIT ... OFFSET ...` in SELECT

## Formatting Standards

- SQL keywords UPPERCASE (`INSERT`, `SELECT`, `VALUES`, `WHERE`, `FROM`, `JOIN`, `LEFT JOIN`, `ON`, `AS`, `AND`, `OR`,
  `ORDER BY`, `GROUP BY`, `LIMIT`, `OFFSET`, `SET`, `UPDATE`, `DELETE`)
- Column lists aligned on new line with 19-space indent from statement start
- VALUES rows aligned with 7-space indent
- Parameter placeholders: `?`
- Comments: `--` prefix, placed above or inline as appropriate
- Comment all `?` with actual values for readability: `(?, ?, ?)  -- row 1: 'NYSE', 'AAPL', 'Apple Inc.'`
- Use `...` for ellipsis in multi-row placeholders

### R9: No Source Line Numbers (MANDATORY)

**源码引用中禁止使用行号**。行号会随着代码重构随时变化，写入文档或 Skill 后会成为过时的错误信息。

违反此规则的写法（必须修正）：

- `Source: SQLSyntax.java, line 103.` — ❌ 带行号
- `ArmyParser line 2603` — ❌ 带行号
- `（line 207-208）` — ❌ 带行号

正确写法：

- `Source: SQLSyntax.java.` — ✅ 仅引用文件
- `ArmyParser.handleDomainUpdate` — ✅ 引用方法名
- 使用 `ArmyParser#safeLiteral()` 的 javadoc 交叉引用格式 — ✅ 精确到方法

### R10: Example Code MUST Be Source-Backed (MANDATORY)

**写示例代码的每一行都必须有源码支持，不可违背。** 不得凭印象、猜测或"看起来合理"编写任何 API 调用链。

#### 铁律

- **每一行 API 调用** 必须在 Army 源码中有对应的接口方法定义
- **每个方法调用链的返回类型** 必须与实际源码的接口继承层次一致
- **禁止凭空编造** 不存在的重载、不存在的链式调用顺序

#### INSERT API 链式调用的关键分界点

`insertInto()` 返回 `_ColumnListSpec`，该接口**同时**继承了：

- `_ColumnListParensClause` → 拥有 `.parens(Consumer<_StaticColumnSpaceClause>)` — **列清单 parens**
- `_ComplexColumnDefaultSpec` → 拥有 `.values()` / `.defaultValue()` — **值绑定入口**

**两类 `parens()` 的区别**：

[cols="1,3,4",options="header"]
|===
| 位置 | 接口来源 | `space()` 可接受的参数

| `.parens()` **在 `.values()` 之前**
| `_ColumnListParensClause` → `_StaticColumnSpaceClause`
| **仅 `FieldMeta`**（列名），无 value 参数

| `.parens()` **在 `.values()` 之后**
| `_ValuesParensClause` → `_StaticValueSpaceClause`
| `(FieldMeta, operand)` / `(FieldMeta, BiFunction, value)` — 带值绑定
|===

**严禁的写法**（无源码支持）：

```java
// ❌ 错误：.parens() 在 .values() 之前，_StaticColumnSpaceClause 不接受 BiFunction
SQLs.singleInsert()
        .insertInto(Stock_.T)
        .parens(s -> s.space(Stock_.code, SQLs::namedLiteral)   // 编译错误！
                .comma(Stock_.name, SQLs::namedLiteral)
        )
        .values(stockList)
        .asInsert();
```

**正确的写法**（源码依据：
`_StaticValueSpaceClause.space(FieldMeta<T>, BiFunction<FieldMeta<T>, E, Expression>, E value)`）：

```java
// ✅ 正确：.values() 先进入 VALUES 模式，.parens() 在之后绑定值
SQLs.singleInsert()
        .insertInto(Stock_.T)
        .values()
        .parens(s -> s.space(Stock_.code, SQLs::namedLiteral, "code")
                .comma(Stock_.name, SQLs::namedLiteral, "name")
        )
        .asInsert();
```

#### 编写示例代码的自检流程

在写任何示例代码前，必须完成以下验证：

1. **确定 API 链每一步的返回类型**：从入口方法（如 `SQLs.singleInsert()`）开始，逐方法追踪返回的接口类型
2. **查看该接口的继承层次**：确认所有可用的方法（直接方法 + 继承方法）
3. **检查每个方法重载的参数签名**：确认 `space()` 有几参数版本、BiFunction 的泛型参数是什么
4. **对照已有测试用例**：在 `army-example/src/test/` 中搜索类似用法，确保 API 链与测试中一致

#### 常见陷阱

- **混淆列清单 `parens` 与值行 `parens`**：两者的 `space()` 重载完全不同，前者仅接受 `FieldMeta`，后者接受值绑定
- **`values(stockList)` 不是 VALUES 模式**：`values(List<T>)` 是 domain 模式（需要域对象），`values()`（无参）才是 VALUES 模式入口
- **方法引用 `SQLs::namedLiteral` 不能直接用作 `space()` 的第二个参数**：必须配合 BiFunction 重载
  `space(field, funcRef, value)`，第三个参数是用户传入的 key

> **核心原则**: 文档中的每一行 Java 示例代码，都必须能在 Army 源码中找到对应的接口方法签名。宁可少写，不可编造。

### R8: Document Context is King — Know When to Read Source (MANDATORY)

**文档中的 Domain 类定义决定了 SQL 的字段范围和顺序。实际源码仅在需要追溯继承链时才读取。**

#### 判定流程

1. **先看文档中类的声明**：检查 `public class Xxx` 后面是否有 `extends`
2. **无 `extends`（独立 POJO）→ 以文档为准**：
    - 字段列表 = 文档中声明的字段（不增不减）
    - 字段顺序 = 文档中的声明顺序
    - **不需要**读取实际源码，**禁止**用实际源码去"纠正"文档
3. **有 `extends` → 需要追溯继承链**：
    - 读取实际源码中所有 `@MappedSuperclass` 祖先的字段
    - 按「基类到子类、自上而下」排列全部字段
    - 文档中可能只展示了子类字段，SQL 必须包含继承的基类字段

#### 严禁行为

- ❌ 文档中是独立 POJO，却按实际源码的继承链重排字段顺序（如把基类字段提到最前）
- ❌ 凭空添加文档中不存在的字段（如文档中 Stock 只有 10 个字段，却加了 `full_name` 变成 11 列）
- ❌ 修改文档中 Domain 类定义来匹配 SQL

#### 正确做法

- ✅ 文档中 `public class Stock {` → 字段列表和顺序完全按文档声明，10 个字段就是 10 列
- ✅ 文档中 `public class StockQuotes extends StockBaseDomain<...> {` → 读取 StockBaseDomain → BaseDomain → MinBaseDomain
  继承链，确定完整字段顺序

## Verification Checklist

After generating SQL, verify:

- [ ] **Column order matches the document's Domain class declaration order** (independent POJO → document order; has
  `extends` → inheritance chain top→bottom from source)
- [ ] Column count matches the document's Domain class field count (no extras, no omissions)
- [ ] **Domain class field order in document was NOT modified** — SQL matches the document's context, not the other way
  around
- [ ] Value mode INSERT (no explicit `parens()` column-list): **must include ALL table fields**, not just user-specified
  ones
- [ ] Unset nullable fields in value mode → `DEFAULT` keyword (PostgreSQL, non-SQLite)
- [ ] Column order follows the applicable rule (declaration order for domain mode)
- [ ] POST id excluded when applicable (**non-child tables only**; child tables always include id per R4)
- [ ] DEFAULT mode → all values are `?` (not literals, except explicit `SQLs::literal`/`SQLs::constant` or
  `SQLs::literalValue`/`SQLs::constValue`); **all dialects use `?` — PostgreSQL does NOT use `$1`/`$2`**
- [ ] LITERAL mode SQL → values with type prefix (`::INTEGER`, `DATE '...'`, etc.) per dialect — see R0
- [ ] CONST mode SQL → values without type prefix (all dialects output raw value) — see R0
- [ ] `UPDATE_TIME_PLACEHOLDER` → output determined by whether statement has other params (`?` vs `CURRENT_TIMESTAMP`) —
  see R0
- [ ] `rowParam` SQL → multiple `?` placeholders (one per collection element); in `IN` → parenthesized `(?, ?, ?)` — see
  R0
- [ ] `rowLiteral` SQL → embedded values with type prefix; in `IN` → parenthesized; count matches collection size — see
  R0
- [ ] `rowConst` SQL → embedded values without type prefix; count matches collection size — see R0
- [ ] `namedRowParam` SQL → multiple `?` with named indices (`?:name[0], ?:name[1]`) — see R0
- [ ] `namedLiteral`/`namedConst` SQL → only in VALUES INSERT context, never in batch UPDATE/DELETE — see R0
- [ ] `namedRowLiteral`/`namedRowConst` SQL → only in VALUES INSERT context, never in batch UPDATE/DELETE — see R0
- [ ] Reserved fields handled per R2 table (including in dialect-specific conflict clauses)
- [ ] Enum handling per R6
- [ ] Dialect-specific syntax matches the `.Generated SQL (dialect)` label
- [ ] **UPDATE SET target column (`=` left side): no table alias for PostgreSQL; MySQL allows it**
- [ ] INSERT...SELECT: column count in INSERT list matches SELECT list (use DEFAULT for gaps)
- [ ] Query mode INSERT: SQL comments correctly state that Army appends column names only; all values come from the
  subquery (`DEFAULT` = DB column default)
- [ ] Dynamic INSERT: per-row VALUE items may use `DEFAULT` for unset fields; column list is always full
- [ ] **Parent-Child INSERT** (`child()` chain): parent and child use **DIFFERENT table names** (R5a)
- [ ] **Parent INSERT**: column order = Army-managed fields (`id, create_time, update_time, version`) → business
  fields (R5b)
- [ ] **Child INSERT**: column order = `id` first (**always included, even POST**) → child-only business fields →
  discriminator last (R5c)
- [ ] **Child fields**: (except `id`) MUST NOT overlap with parent fields (R5d)
- [ ] **Domain UPDATE: childItemPairList() 非空时 → CTE/multi-table 路径**；为空时 → 标准单表 UPDATE 操作父表 — see D1
- [ ] **Domain UPDATE on ChildTableMeta (CTE/multi-table path)**: PostgreSQL = 两阶段 CTE (
  `WITH cte AS (UPDATE child SET ... FROM parent) UPDATE parent SET ... FROM cte`)；MySQL = multi-table (
  `UPDATE child JOIN parent ON ... SET ...`) — see D4
- [ ] **Domain UPDATE SET clause ordering (MySQL multi-table)**: parent fields → child fields → updateTime → version —
  see D4
- [ ] **Domain UPDATE/DELETE**: discriminator (`AND p_of_a.status = 'DISCRIMINATOR_VALUE'`) auto-injected on parent
  alias — see D3
- [ ] **Domain UPDATE/DELETE**: visible filter (`AND p_of_a.visible = true`) auto-injected when parent has visible
  field — see D3
- [ ] **CTE WHERE clause order (PostgreSQL)**: `a.id = p_of_a.id` MUST be the FIRST WHERE condition, before user WHERE,
  discriminator, and visible — see D3a
- [ ] **CTE WHERE clause: `a.id = p_of_a.id` join condition MUST NOT be omitted** in any PostgreSQL CTE domain
  UPDATE/DELETE SQL — see D3a
- [ ] **WHERE clause ordering (ALL dialects)**: user WHERE MUST come BEFORE discriminator and visible — Army managed
  conditions are APPENDED, not prepended — see D3a
- [ ] **Target table for domain DML on child**: parent table name + alias `"p_of_" + userAlias` — see D2
- [ ] **Domain DELETE (PostgreSQL)**: CTE two-stage:
  `WITH cte AS (DELETE FROM child USING parent ... RETURNING id) DELETE FROM parent USING cte` — see D5
- [ ] **Domain DELETE (MySQL)**: multi-table: `DELETE child, parent FROM child JOIN parent ON ... WHERE ...` — see D5
- [ ] **Batch domain DML**: same dialect rules as single, with named param `?` as batch parameter — see D6
- [ ] **Domain API on SingleTableMeta**: standard single-table, no discriminator — see D7

## Skill Evolution Rules

本 Skill 是**活的文档**，每次使用后都可能需要进化。以下情况**必须**更新本 Skill：

### 触发更新的条件

1. **SQL 生成错误修复后**：当生成的 SQL 不符合 Army 框架的代码实现，或不符合目标数据库的方言语法时，在修复 SQL 后必须将修正经验更新到本
   Skill 中的对应规则，确保同类错误不再重现。

2. **新增规则时**：当发现现行规则无法覆盖的 SQL 生成场景，需要新增规则。新增的规则必须纳入本 Skill 对应的规则章节，保证规则体系的完整性。

3. **流程优化时**：当在使用中发现更优的工作流程（如更高效的信息收集顺序、更准确的验证方法、更好的列推断策略），能够更准确地为
   Statement 示例生成 SQL 时，必须更新本 Skill 中对应的流程描述（Quick Start 或 Verification Checklist）。

4. **规则描述不准确时**：当发现现有规则描述与框架实际行为不一致、或表述有歧义导致生成错误 SQL 时，必须**修正**
   该规则；若某条规则已被证明为**完全错误**（与 Army 代码实现矛盾），必须**删除**该规则，避免误导后续生成。

5. **工作流程有错误时**：当发现 Quick Start 或各规则章节中描述的流程步骤有错误（如顺序错误、遗漏关键步骤、判定逻辑缺陷），必须立即修正，确保智能体按正确流程执行。

6. **文档格式不规范时**：当发现文档中存在违反文体规范的写法（如源码引用带行号、缺少必要注释、格式不一致），必须修正并更新本
   Skill 中的对应规则（如 R9），防止同类问题重现。

7. **示例代码无源码支持时**：当发现文档中的示例代码使用了不存在的 API 链、错误的方法重载、或违反接口继承层次的调用顺序，必须修正并强化本
   Skill 中 R10 规则，确保后续不再出现凭印象编造 API 用法的错误。

### 更新后的自检要求

每次更新本 Skill 后，必须进行以下自检：

- [ ] 新增或修改的规则**表述清晰无歧义**，智能体能够准确理解并执行
- [ ] 规则间的**优先级关系明确**，不会产生冲突
- [ ] 示例代码和说明**与规则一一对应**，便于智能体匹配场景
- [ ] 规则措辞使用**指令式、可执行的语句**（如 "MUST"、"Column order follows..."），而非描述性散文
- [ ] 新增规则与已有规则**无冗余或矛盾**，确保整个 Skill 文件内聚一致
- [ ] 章节结构**层次分明**，智能体能够快速定位到所需规则
- [ ] **无源码行号引用**：所有 `Source:` 引用仅包含文件路径，不含 `line NNN`；所有注释中的源码引用不含 `(line NNN)`
- [ ] **字面量安全说明完整**：`literal`/`const` 相关描述必须强调通过 `ArmyParser#safeLiteral()` 保证安全，避免读者误解为字符串拼接
- [ ] **占位符语法正确**：所有方言的 param 输出均为 `?`（不是 `$1`/`$2`）
- [ ] 示例代码有源码支持：每行 Java 示例代码的 API 调用链在源码中有对应接口方法签名；`.parens()` 前后语义正确（列清单 vs
  值行），无不存在的重载
- [ ] batch UPDATE SET 使用 `setSpace()` 传 named 参数（`set()` 无对应重载）；WHERE 使用 `.spaceEqual()` 直接调用 — see R0
  核心区别
