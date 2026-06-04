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

### R0: Literal Mode

All SQL uses JDBC `?` parameterization (Army default `LiteralMode.DEFAULT`).
Use `-- param: value` comments only for explicit function reference (e.g., `SQLs::param`).

### R1: Column Name Conversion

Java camelCase field names are converted to snake_case:

- `offerPrice` → `offer_price`
- `listingDate` → `listing_date`
- `createTime` → `create_time`
- `updateTime` → `update_time`

### R2: Reserved Field Behavior

Five reserved field names have auto-managed behavior:

| Field        | INSERT                   | UPDATE             | Notes                           |
|--------------|--------------------------|--------------------|---------------------------------|
| `id`         | Value from `@Generator`  | Never updatable    | PRECEDE→included; POST→excluded |
| `createTime` | Auto-set to current time | Never updatable    | Always in INSERT list           |
| `updateTime` | Auto-set to current time | Auto-refreshed     | In INSERT + every UPDATE        |
| `version`    | Auto-set to `0`          | Auto-incremented   | In INSERT + every UPDATE        |
| `visible`    | Auto-set to `true`       | Soft-delete toggle | Only if field exists on class   |

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
column list:

```sql
-- POST id: no id column
INSERT INTO stock (exchange, code, name, ...)
VALUES (?, ?, ?, ...)

-- PRECEDE id (Snowflake etc.): id IS included
INSERT INTO stock (id, exchange, code, name, ...)
VALUES (?, ?, ?, ?, ...)
```

This applies to domain mode, value mode, dynamic mode, and query mode equally.

**Note**: In the current documentation, `Stock.id` uses `@Generator("Snowflake8Generator")` which is `PRECEDE` — id IS
included in all Stock INSERT examples.

### R5: Single-Table Inheritance

For `@Inheritance` parent/child tables:

- **Parent rows**: exclude discriminator column and child-specific fields
- **Child rows**: include discriminator column (value from `@DiscriminatorValue`) and all parent+child fields
- Column order follows the respective class's field declaration order

### R6: Enum Type Values

Enum values in SQL use their database representation (typically the enum constant name as a string in DEFAULT mode).
Since default is parameterized, use `?` with a comment.

```sql
-- defaultValue(Stock_.status, StockStatus.NORMAL) means:
-- when domain object's status is null → 'NORMAL' is substituted per-row
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)  -- status uses ? or 'NORMAL' when null
```

### R7: Standard API → PostgreSQL Dialect ONLY (MANDATORY)

**标准 Statement API（`SQLs.xxx()` 入口）生成的 SQL 只能使用 PostgreSQL 方言。**

判定入口：

- 标准 API 入口：`SQLs.query()`, `SQLs.singleInsert()`, `SQLs.singleUpdate()`, `SQLs.singleDelete()`,
  `SQLs.domainUpdate()`, `SQLs.domainDelete()`, `SQLs.batchSingleUpdate()`, `SQLs.scalarSubQuery()`, `SQLs.subQuery()`
- 方言 API 入口：`MySQLs.xxx()` → MySQL 方言；`Postgres.xxx()` → PostgreSQL 方言

**规则**：

- 标准 API → `.Generated SQL (PostgreSQL)` 标签，SQL 语法遵循 PostgreSQL
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
- **Column count matching**: When INSERT column count > SELECT column count, unselected columns use `DEFAULT` in the
  SELECT clause
  ```sql
  -- 4 INSERT columns, 3 SELECT columns → status uses DEFAULT
  INSERT INTO stock (exchange, code, name, status)
  SELECT n.exchange, n.code, n.name, DEFAULT
  FROM nasdaq_table AS n
  ```
- When column list is omitted entirely, SELECT determines all columns

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

## DELETE Rules

- Soft delete (if visible field present): actually an UPDATE setting `visible = false`
- Hard delete: `DELETE FROM table WHERE ...`

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
- [ ] POST id excluded when applicable
- [ ] DEFAULT mode → all values are `?` (not literals, except explicit `SQLs::literalValue`)
- [ ] Reserved fields handled per R2 table (including in dialect-specific conflict clauses)
- [ ] Enum handling per R6
- [ ] Dialect-specific syntax matches the `.Generated SQL (dialect)` label
- [ ] INSERT...SELECT: column count in INSERT list matches SELECT list (use DEFAULT for gaps)
- [ ] Dynamic INSERT: per-row VALUE items may use `DEFAULT` for unset fields; column list is always full

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

### 更新后的自检要求

每次更新本 Skill 后，必须进行以下自检：

- [ ] 新增或修改的规则**表述清晰无歧义**，智能体能够准确理解并执行
- [ ] 规则间的**优先级关系明确**，不会产生冲突
- [ ] 示例代码和说明**与规则一一对应**，便于智能体匹配场景
- [ ] 规则措辞使用**指令式、可执行的语句**（如 "MUST"、"Column order follows..."），而非描述性散文
- [ ] 新增规则与已有规则**无冗余或矛盾**，确保整个 Skill 文件内聚一致
- [ ] 章节结构**层次分明**，智能体能够快速定位到所需规则
