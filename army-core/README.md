## army-core

The core module of the Army framework. It provides the essential building blocks for database access.

### Main Packages

#### io.army.criteria

Type-safe SQL building API. Use this to construct SELECT, INSERT, UPDATE, DELETE statements programmatically.

- **`SQLs`**: Main entry point for creating SQL statements
- **`Query`**: Build SELECT queries with WHERE, JOIN, GROUP BY, ORDER BY, LIMIT
- **`Update`**: Build UPDATE statements with SET and WHERE clauses
- **`Delete`**: Build DELETE statements with WHERE clauses
- **`Insert`**: Build INSERT statements with VALUES or SELECT subquery
- **Expressions**: Build complex expressions (CASE WHEN, functions, arithmetic)

#### io.army.session

Session management for database connections.

- **`Session`**: Represents a database session
- **`SessionFactory`**: Creates database sessions
- **`LocalSession`**: Local transaction session
- **`RmSession`**: XA transaction session

#### io.army.mapping

Type mapping between Java types and database types.

- **`MappingType`**: Base interface for all type mappings
- **Built-in types**: `StringType`, `IntegerType`, `LongType`, `BigDecimalType`, `LocalDateType`, `LocalDateTimeType`,
  etc.
- **Special types**: `JsonType`, `JsonbType`, `XmlType`, `UUIDType`, `EnumType`
- **Array types**: Support for database array types (via `army-array` module)

#### io.army.dialect

Database dialect support.

- **`Database`**: Enumeration of supported databases (MySQL, PostgreSQL, SQLite, H2, Oracle)
- **`Dialect`**: Database-specific SQL syntax handling
- **`MySQLDialect`**: MySQL-specific SQL support
- **`PostgreDialect`**: PostgreSQL-specific SQL support
- **`SQLiteDialect`**: SQLite-specific SQL support

#### io.army.codec

JSON and XML codecs for serializing/deserializing data.

- **`JsonCodec`**: JSON serialization/deserialization
- **`XmlCodec`**: XML serialization/deserialization
- Supports Jackson, Gson, and FastJson implementations

### Key Features

1. **Type-safe SQL**: Build SQL statements with compile-time type checking
2. **Database Agnostic**: Write SQL once, run on multiple databases
3. **Session Management**: Manage database connections and transactions
4. **Type Mapping**: Automatic conversion between Java and database types
5. **JSON/XML Support**: Built-in support for JSON and XML data types

### Usage Example

```java
// Build a simple SELECT query
Select query = SQLs.query()
                .select(Stock_.id, Stock_.name)
                .from(Stock_.T)
                .where(Stock_.exchange.equal("SHSE"))
                .orderBy(Stock_.code)
                .limit(10)
                .asQuery();

// Build an UPDATE statement
Update update = SQLs.singleUpdate()
        .update(Stock_.T)
        .set(Stock_.name, "New Name")
        .where(Stock_.id.equal(1L))
        .asUpdate();

// Build a DELETE statement
Delete delete = SQLs.singleDelete()
        .delete(Stock_.T)
        .where(Stock_.status.equal(StockStatus.DELISTED))
        .asDelete();
```

### Supported Databases

- MySQL
- PostgreSQL
- SQLite
- H2 (for testing)
- Oracle

### Dependencies

- **`army-struct`**: Core type definitions

---

## Default Type Mappings

The following table lists the default mappings from Java types to Army mapping types:

| Java Type             | Mapping Type         |
|-----------------------|----------------------|
| `String`              | `StringType`         |
| `boolean` / `Boolean` | `BooleanType`        |
| `int` / `Integer`     | `IntegerType`        |
| `long` / `Long`       | `LongType`           |
| `float` / `Float`     | `FloatType`          |
| `double` / `Double`   | `DoubleType`         |
| `short` / `Short`     | `ShortType`          |
| `byte` / `Byte`       | `ByteType`           |
| `char` / `Character`  | `SqlCharType`        |
| `BigInteger`          | `BigIntegerType`     |
| `BigDecimal`          | `BigDecimalType`     |
| `LocalDateTime`       | `LocalDateTimeType`  |
| `OffsetDateTime`      | `OffsetDateTimeType` |
| `ZonedDateTime`       | `ZonedDateTimeType`  |
| `LocalDate`           | `LocalDateType`      |
| `LocalTime`           | `LocalTimeType`      |
| `OffsetTime`          | `OffsetTimeType`     |
| `Instant`             | `InstantType`        |
| `Year`                | `YearType`           |
| `YearMonth`           | `YearMonthType`      |
| `MonthDay`            | `MonthDayType`       |
| `ZoneId`              | `ZoneIdType`         |
| `BitSet`              | `BitSetType`         |
| `UUID`                | `UUIDType`           |
| `byte[]`              | `VarBinaryType`      |

**Enum Handling**:

- If enum implements `CodeEnum` → `CodeEnumType`
- If enum implements `LabelEnum` → `LabelEnumType`
- Otherwise → `NameEnumType`

**Composite Types**:

- If class annotated with `@DefinedType(category = COMPOSITE)` → `CompositeType`

---

## CriteriaContext Stack

Army uses a **Context Stack** mechanism to manage statement construction. Each level of SQL statement (main query,
subquery, CTE) creates a `CriteriaContext` instance that is pushed onto the stack.

### How It Works

1. When you start building a statement with `SQLs.query()`, a primary `CriteriaContext` is created and pushed onto the
   stack
2. When you add a subquery or CTE, a nested `CriteriaContext` is pushed with a reference to its outer context
3. `SQLs.refField()`, `SQLs.field()`, and `SQLs.refSelection()` look up fields/selections in the current context

### Context Stack Operations

- **Push**: Add a new context when entering a nested scope (subquery, CTE)
- **Pop**: Remove the current context when exiting a scope
- **Peek**: Get the current context without removing it
- **Root**: Get the outermost context

### ThreadLocal Management

The context stack is stored in a `ThreadLocal<Stack>` to allow concurrent statement building across threads.

#### Cleanup Mechanisms

1. **Normal cleanup**: When `pop()` is called and there's no outer context, `ThreadLocal.remove()` is called
2. **Error cleanup**: If an exception occurs during statement building, `HOLDER.remove()` is called immediately
3. **JVM Cleaner**: Java `Cleaner` is registered on the statement object to ensure cleanup even if `asQuery()` is never
   called

#### ThreadLocal Leak Prevention

```java
// In ContextStack.push()
CLEANER.register(contextHolder, newStack::clear); // Register cleanup callback

// In ContextStack.pop()
if(context.

getOuterContext() ==null){
        assert stack.

isEmpty();
     HOLDER.

remove(); // Clean ThreadLocal for primary context
}

// In error paths
static CriteriaException clearStackAndCriteriaError(String msg) {
    HOLDER.remove(); // Always clean on error
    return new CriteriaException(msg);
}
```

### Design Reason

Army uses **static method style** for building SQL statements (SQL style), just like writing raw SQL text. This means no
session or connection is needed during statement construction. The `CriteriaContext` stack provides the necessary
context for resolving table aliases, field references, and selection labels during this stateless building process.

---

## How a DSL Statement Becomes SQL

This section explains how a statement that you built with the DSL (a `Select`, an `Insert`, ...) is finally
"expressed" as SQL text plus its bind parameters. Read this before looking at the parser source code.

### The core idea in one paragraph

A DSL statement is, from Army's point of view, just **a data holder**. Every piece you added with the DSL
(the selected columns, the tables, the WHERE predicates, the ORDER BY items...) is stored in a list on the
statement object, and the statement implements a set of **internal interfaces** (the `_`-prefixed interfaces
under `io.army.criteria.impl.inner`) that expose those lists. For example, a `Select` instance implements
`_Query`, which gives you `selectItemList()`, `tableBlockList()`, `wherePredicateList()`, `orderByList()`, ...

Producing SQL is then a **rendering walk**:

1. The grammar code — `DialectParser` (its only implementation is `ArmyParser`) — knows the **order in which
   SQL clauses must appear**. It walks the statement clause by clause, following that order.
2. Each small element (a column, a value, a function, a predicate, a window, ...) **writes itself** into a
   shared `StringBuilder`. In the code this is called being *self-described*: an expression implements
   `_SelfDescribed` and implements its `appendSql(StringBuilder, _SqlContext)` method.
3. Because every `?` placeholder must line up with its value, the whole walk shares **one context object**,
   `_SqlContext`, which carries both the `StringBuilder` and the **parameter list**.

When the walk ends, `sqlBuilder.toString()` *is* the SQL, and the parameter list *is* the bind parameters.

The SQL text is **not stored on the statement**. It is regenerated every time the statement is parsed, which is
also what makes batch rendering cheap (the same statement object can be parsed repeatedly).

```
DSL statement object                 
  (a data holder)                    
      │ implements internal interfaces, e.g. Select → _Query
      │ exposes each clause: selectItemList(), tableBlockList(),
      │ wherePredicateList(), orderByList(), ...
      ▼
 DialectParser  /  ArmyParser         ← knows the SQL grammar order
      │  renders one clause after another
      │                     writes into
      ▼                             ▼
 _SqlContext (the shared clipboard)  ▲
   • StringBuilder  (sqlBuilder)     │
   • parameter list (params)         │
                                     │ each leaf writes itself via
                                     │ _SelfDescribed.appendSql(...)
```

### 1. The statement exposes its clauses (the `_`-interfaces)

The internal interfaces in `io.army.criteria.impl.inner` are the "contract" between the DSL layer and the parser.
They do nothing but expose data:

| Internal interface                | Statement that implements it             | Exposes (excerpt)                                                                                                            |
|-----------------------------------|------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| `_Query`                          | a `Select` / query statement             | `hintList()`, `modifierList()`, `tableBlockList()`, `wherePredicateList()`, `groupByList()`, `havingList()`, select items... |
| `_StandardQuery`                  | a standard (SQL:2008) query              | the above + `windowList()`, and the standard `ORDER BY` / `LIMIT` / lock specs                                               |
| `_Insert` / `_Update` / `_Delete` | the corresponding DML                    | target table, `itemPairList()` / `returningList()`, `wherePredicateList()`, child DML...                                     |
| `_Expression`                     | any expression                           | extends `Expression` **and** `_SelfDescribed`                                                                                |
| `_Predicate`                      | any WHERE predicate                      | extends `IPredicate` **and** `_Expression`                                                                                   |
| `_SelfDescribed`                  | any element that can write itself as SQL | `void appendSql(StringBuilder sqlBuilder, _SqlContext context)`                                                              |

The parser never needs to know "how the user built the DSL". It simply checks which internal interface the
statement implements, calls the getters, and renders whatever is inside.

### 2. The grammar machine: `DialectParser` / `ArmyParser`

`DialectParser` (`io.army.dialect`) is a sealed interface whose only implementation is `ArmyParser`. It is the
entry point of the walk — each method takes a statement and returns an executable `Stmt`:

```java
Stmt select(SelectStatement select, boolean useMultiStmt, SessionSpec sessionSpec);

Stmt insert(InsertStatement insert, SessionSpec sessionSpec);

Stmt update(UpdateStatement update, boolean useMultiStmt, SessionSpec sessionSpec);

Stmt delete(DeleteStatement delete, boolean useMultiStmt, SessionSpec sessionSpec);
```

`ArmyParser` is big on purpose: **it is the single place that knows the SQL grammar**. For a standard query the
core method reads almost like the SQL grammar itself (clause by clause, numbered 1-9):

```java
private void parseStandardQuery(_StandardQuery stmt, _SimpleQueryContext context) {
    standardWithClause(stmt, context);                        // (0) WITH  [...]
    StringBuilder builder = context.sqlBuilder();
    if (!builder.isEmpty()) builder.append(_Constant.SPACE);
    builder.append(_Constant.SELECT);                         // "SELECT"
    standardSelectClause(stmt.modifierList(), builder);       // (1) DISTINCT / ALL
    selectionListClause(context);                             // (2) the select list
    List<_TabularBlock> blockList = stmt.tableBlockList();
    if (!blockList.isEmpty()) {
        builder.append(_Constant.SPACE_FROM);                 // (3) " FROM"
        standardTableReferences(blockList, context, false);   //     tables & joins
    }
    queryWhereClause(blockList, stmt.wherePredicateList(), context); // (4) WHERE
    groupByAndHavingClause(stmt, context);                    // (5) GROUP BY / HAVING
    windowClause(stmt.windowList(), context, ...);            // (6) WINDOW
    orderByClause(stmt.orderByList(), context);               // (7) ORDER BY
    standardLimitClause(stmt.offsetExp(), stmt.rowCountExp(), context); // (8) LIMIT / OFFSET
    if (lock != null) standardLockClause(lock, context);      // (9) FOR UPDATE, ...
}
```

`handleSelect` is where you can see the "statement implements an internal interface" check in action. The same
`select(...)` entry can receive different statement *shapes* and dispatches by their interface:

```java
private _SelectContext handleSelect(_SqlContext outerContext, SelectStatement stmt, ...) {
    stmt.prepared();                                    // mark: this statement is being parsed
    if (stmt instanceof _Query) {                       // a plain SELECT
        context = SimpleSelectContext.create(...);
        if (stmt instanceof StandardQuery) parseStandardQuery((_StandardQuery) stmt, context);
        else parseSimpleQuery((_Query) stmt, context);
    } else if (stmt instanceof _UnionRowSet) {          // a UNION / INTERSECT / EXCEPT row set
        handleQuery((Query) union.leftRowSet(), context);       // render the left row set first
        context.sqlBuilder().append(union.unionType().spaceWords); // then the set operator (UNION, ...)
        this.handleRowSet(union.rightRowSet(), context);         // then the right row set
    } else if (stmt instanceof _ParensRowSet) {         // a parenthesized " ( SELECT ... ) "
        handleParenRowSet(context, (ParensRowSet) stmt);         // wraps the row set in "(" ... ")"
    }
    return context;
}
```

All dialect variations (quoted identifiers, function name case, which keywords a dialect forbids as user names,
etc.) are centralized in this layer through helpers such as `identifier(...)` and the many `_Constant` fragments,
so the DSL leaves never need to know them.

### 3. `_SqlContext`: the shared clipboard (`StringBuilder` + parameters)

`_SqlContext` (`io.army.dialect`) is the object handed to every `appendSql(...)` call. Its javadoc says it is used
by both `DialectParser` and the criteria-API implementations (e.g. `_Expression`). Its most important members:

- `StringBuilder sqlBuilder()` — the one and only SQL text buffer;
- `void appendParam(SQLParam)` — write a `?` placeholder **and** register the value in the parameter list;
- `void appendLiteral(...)` / `appendFuncName(...)` / `appendSubQuery(...)` / `identifier(...)` — helpers so leaves
  don't have to re-implement quoting or literal escaping.

The base implementation is the abstract class `StatementContext` (`io.army.dialect`, same package), whose javadoc
states it is the base class of all `_SqlContext` implementations. Look at its fields:

```java
abstract class StatementContext implements _StmtContext, StmtParams {
    protected final StringBuilder sqlBuilder;      // new StringBuilder(1024) for the top statement
    private final ParamAccepter paramAccepter;     // wraps the ArrayList<SQLParam> parameter list
}
```

The sub-context trick that makes sub-queries and CTEs work is visible in the constructor: when a context is
created **with an outer context**, it does not allocate a new buffer or a new parameter list — it reuses the
outer one:

```java
if (parentOrOuterContext == null) {
    this.sqlBuilder = new StringBuilder(1024);          // top-level statement: fresh buffer
} else {
    this.sqlBuilder = parentOrOuterContext.sqlBuilder;  // sub-query: share the same buffer
}
...
this.paramAccepter = parentOrOuterContext.paramAccepter; // share the parameter list
```

(For a few special sub-contexts — INSERT statements and batch DML that read named parameters — the code wraps the
shared accepter in a `ParamAccepterWithOuter` instead of assigning it directly; it still points at the same
underlying `ArrayList<SQLParam>`, and every flag it sets is propagated to the outer accepter.)

That is why a `WHERE id IN (subquery)` can be written in the *middle* of the whole SQL text and still keep all
parameters in the correct global order.

How a bind parameter becomes a `?` — this is the one method every expression uses when it has a value to pass:

```java
public final void appendParam(SQLParam sqlParam) {
    ArrayList<SQLParam> paramList = this.paramAccepter.paramList;
    ...
    this.sqlBuilder.append(SPACE_PLACEHOLDER);   // SPACE_PLACEHOLDER = " ?"
    paramList.add(sqlParam);                     // keep the value in the same order
}
```

(The single/multi/named-parameter variants all converge on this same pattern.)

### 4. Every expression describes itself: `_SelfDescribed`

`_SelfDescribed` has exactly one method:

```java
public interface _SelfDescribed {
    void appendSql(StringBuilder sqlBuilder, _SqlContext context);
}
```

The contract (from its javadoc) is: **append a space, then append my own SQL text**. The space keeps the words of
the final SQL separated. Pre-built constants help: `_Constant.SPACE_WHERE` is `" WHERE"`, `_Constant.SPACE_AND`
is `" AND"`, `_Constant.SELECT` is `"SELECT"` — every fragment carries the spacing it needs, so the code never
concatenates strings by hand.

Because an expression often contains other expressions, `appendSql` is naturally **recursive**. For example, an
`AND` predicate is just a binary tree node that asks its left child, writes ` AND `, then asks its right child:

```java
public void appendSql(StringBuilder sqlBuilder, _SqlContext context) {
    this.left.appendSql(sqlBuilder, context);          // left operand writes itself
    sqlBuilder.append(_Constant.SPACE_AND);            // " AND"
    right.appendSql(sqlBuilder, context);              // right operand writes itself
}
```

(The real `AndPredicate` additionally wraps the right operand in parentheses when the right operand is itself an
`AND`, so the operator grouping stays correct — the snippet above keeps the essence but drops that detail.)

A bracket predicate shows the same idea at one more level of nesting:

```java
public void appendSql(StringBuilder sqlBuilder, _SqlContext context) {
    sqlBuilder.append(_Constant.SPACE_LEFT_PAREN);     // " ("
    this.predicate.appendSql(sqlBuilder, context);     // the inner predicate writes itself
    sqlBuilder.append(_Constant.SPACE_RIGHT_PAREN);    // " )"
}
```

At the leaves you find the simple writers:

- a boolean word (`TRUE`/`FALSE`) appends a pre-built `spaceWord`;
- a column field delegates to `context.appendField(...)`, which is where table-alias/quoting decisions live;
- a parameter value calls `context.appendParam(...)` as shown above, which produces the `?`.

In other words: **the grammar layer decides the order and the keywords, every element decides its own text, and
the `_SqlContext` decides everything that is dialect-dependent** (quoting, literals, placeholders).

### 5. Step by step: a real example

Take the example from the *Usage Example* section:

```java
Select query = SQLs.query()
        .select(Stock_.id, Stock_.name)
        .from(Stock_.T)
        .where(Stock_.exchange.equal("SHSE"))
        .orderBy(Stock_.code)
        .limit(10)
        .asQuery();
```

When this statement is executed, Army eventually calls `DialectParser.select(query, ...)`. What happens inside:

1. `handleSelect` calls `stmt.prepared()`, sees the statement implements `_Query`, and creates a
   `SimpleSelectContext` — a fresh `_SqlContext` with an empty `StringBuilder` and an empty parameter list.
2. Because the statement is a `StandardQuery`, `parseStandardQuery` renders, in grammar order:
    - `SELECT` (from `_Constant.SELECT`);
    - the select list: `id`, `name`. Each item is asked to describe itself; a plain field writes its (quoted if
      needed) column name;
    - ` FROM stock` — the table object name, where quoting rules are applied;
    - the WHERE clause (see below);
    - ` ORDER BY code`;
    - the LIMIT clause with row count `10`.
3. The WHERE clause is handled by `queryWhereClause`. It is nothing more than a loop over the predicate list:

```java
if (predicateSize > 0) {
    sqlBuilder.append(_Constant.SPACE_WHERE);           // " WHERE"
    for (int i = 0; i < predicateSize; i++) {
        if (i > 0) sqlBuilder.append(_Constant.SPACE_AND);   // " AND" between predicates
        predicateList.get(i).appendSql(sqlBuilder, context); // let each predicate write itself
    }
}
```

The predicate `Stock_.exchange.equal("SHSE")` writes roughly ` exchange = ?`, and — because it contains a
Java value — it calls `context.appendParam(...)`, which appends ` ?` and registers the value `"SHSE"` as the
**first** parameter. (In a single-table query the column appears without a table-alias prefix; when a query
joins several tables, the same field is qualified with the alias of its table instead.)

4. The same happens for `LIMIT 10`: the row-count expression is an expression that holds `10`, so a second `?`
   is appended and `10` becomes the **second** parameter.

The final `Stmt` then looks like this (an illustrative single-table rendering — quoting and aliases are added by
the dialect layer, so the exact text may differ for your dialect):

```sql
SELECT id, name
FROM stock
WHERE exchange = ?
ORDER BY code LIMIT ?
-- params: ["SHSE", 10]
```

(If several tables are involved — e.g. `from(Stock_.T.as("t0"), ...)` — the columns are qualified with those
aliases, and the tables, joins and `ON` conditions are all rendered by `standardTableReferences`.)

### 6. What comes out: `context.build()` → a `Stmt`

A `StatementContext` is more than the parser's clipboard: it is also the carrier of the finished product.
`_StmtContext` extends `_SqlContext` and adds `Stmt build()`. Looking at `StatementContext`:

```java
public final String sql() {
    return this.sqlBuilder.toString();
}       // the SQL text

public final List<SQLParam> paramList() {
    return ...paramAccepter.paramList;
} // the parameters
```

So every select entry method ends with `.build()`:

```java
public final Stmt select(SelectStatement select, boolean useMultiStmt, SessionSpec sessionSpec) {
    ...
    stmt = handleSelect(null, select, sessionSpec, null).build();
    ...
}
```

`build()` hands the context to a factory (`Stmts.queryStmt(context)`, `Stmts.dmlStmt(context)`, ...) which creates
the concrete `Stmt` (a `SimpleStmt` / `QueryStmt` / `BatchStmt`...) holding the SQL string and the ordered
parameter list, ready for JDBC binding and execution.

### Why this design

- **The grammar lives in exactly one place** (`ArmyParser`); a leaf expression never needs to know whether it is
  being written into a `WHERE`, a `SELECT` list, or a `RETURNING` clause.
- **Every element only knows itself** — a column writes a column, a predicate writes a predicate; complexity is
  composed by delegation and recursion instead of one huge formatter.
- **Parameters stay ordered automatically** because they are appended in the same single walk as the text, into
  the same shared context — including inside sub-queries and CTEs.
- **Statements are immutable data, SQL is a view** — the same statement object can be re-rendered any number of
  times (batches, different sessions), and printed for debugging via the same writer logic.

---

## ObjectAccessorFactory

The `ObjectAccessorFactory` provides type-safe access to POJO properties using `MethodHandles` and `LambdaMetafactory`
for high-performance field access.

### Features

- **POJO Access**: Creates accessors for JavaBean-style properties (getter/setter)
- **Field Access**: Creates accessors for `FieldAccessPojo` implementations (direct field access)
- **Map Access**: Built-in accessor for `Map` instances
- **Constructor Access**: Creates `Supplier<T>` for default constructors
- **Caching**: Uses `ClassValue` for cache accessors per class

### Usage

```java
// Get accessor for a POJO
ObjectAccessor accessor = ObjectAccessorFactory.forPojo(Stock.class);

// Read property
Object value = accessor.get(stock, "name");

// Write property
accessor.

set(stock, "name","New Name");

// Get constructor
Supplier<Stock> constructor = ObjectAccessorFactory.pojoConstructor(Stock.class);
Stock newStock = constructor.get();
```

### Accessor Types

- **BeanWriterAccessor**: For standard JavaBeans with getter/setter methods
- **MapWriterAccessor**: For `Map<String, Object>` instances
- **Field-based**: For classes implementing `FieldAccessPojo` (direct public field access)

---

## DataType System

Army has a comprehensive type system that makes **Type a first-class citizen** in the framework.

### Core Interfaces

#### DataType (`io.army.sqltype`)

Base interface for all database types.

- `armyType()`: Returns the generic `ArmyType` enum
- `elementType()`: Returns the element type for arrays
- `isArray()`: Checks if this is an array type
- `typeName()`: Returns the database type name

#### SQLType

Database-specific type enumerations implementing `DataType`:

- `PgType`: PostgreSQL types
- `MySQLType`: MySQL types
- `SQLiteType`: SQLite types
- `OracleType`: Oracle types
- `H2Type`: H2 types

#### ArmyType

Generic type enum that abstracts across databases:

- `BOOLEAN`, `BIGINT`, `VARCHAR`, `DATE`, `TIMESTAMP`, etc.
- `ARRAY`, `COMPOSITE`, `RANGE`, `JSON`, `JSONB`
- Used for result set metadata via `RecordMeta.getArmyType(int)`

### MappingType (`io.army.mapping`)

Maps Java types to database types.

- `javaType()`: The Java type
- `map(ServerMeta)`: Maps to a `DataType` for a specific database
- `beforeBind()`: Convert Java value to database value
- `afterGet()`: Convert database value to Java value

### Type Cooperation

```
Java Type
    ↓ MappingFactory.getDefault()
MappingType
    ↓ MappingType.map(ServerMeta)
DataType (SQLType/CustomType)
    ↓ MappingType.beforeBind()
Database Value
    ↓ JDBC
Database Column
```

**Example**:

```java
// Java type → MappingType
MappingType type = _MappingFactory.getDefault(String.class); // StringType

// MappingType → DataType (for PostgreSQL)
DataType dataType = type.map(serverMeta); // PgType.VARCHAR

// Java value → Database value
Object dbValue = type.beforeBind(dataType, env, "hello");
```

### Type as First-Class Citizen

Army treats types as first-class citizens:

1. **Explicit type mappings**: Every field has a defined `MappingType`
2. **Type inference**: Types are inferred from Java types at compile time
3. **Type metadata**: Full metadata available through `MappingType` and `DataType`
4. **Type compatibility**: `compatibleFor()` method for type conversion
5. **Array support**: `arrayTypeOfThis()` for creating array type mappings

---

## Array and Composite Serialization

### Array Types

Array types handle serialization/deserialization through the `ArrayMappingType` interface:

```java
public interface SqlArray {
    Class<?> underlyingJavaType();  // Element type (e.g., String)

    MappingType elementType();      // Element's MappingType

    MappingType underlyingType();   // Non-array MappingType

    MappingType arrayTypeOfThis();  // Higher-dimensional array
}
```

**Array Serialization/Deserialization**

Army provides `ArraySerializer` and `ArrayDeserializer` for handling array types, primarily for PostgreSQL arrays.

#### ArraySerializer

Converts Java arrays to database text representation:

```java
public interface ArraySerializer {
    String serialize(Class<?> underlyingJavaType, Object array,
                     BiConsumer<Object, StringBuilder> consumer);
}
```

**Builder Configuration**:

| Method                            | Description                            |
|-----------------------------------|----------------------------------------|
| `leftBoundary(char)`              | Left boundary character (default `{`)  |
| `rightBoundary(char)`             | Right boundary character (default `}`) |
| `delimChar(char)`                 | Element delimiter (default `,`)        |
| `rectangularMatrixArray(boolean)` | Enable rectangular matrix array format |

**Default PostgreSQL Array Serializer** (from `PostgreArrays`):

```java
ArraySerializer serializer = ArraySerializer.builder()
        .leftBoundary('{')
        .rightBoundary('}')
        .delimChar(',')
        .rectangularMatrixArray(true)
        .build();
```

**Serialization Flow**:

1. Iterates through array elements
2. For each element, calls the `BiConsumer` to append the element text
3. Builds the array string in PostgreSQL array format: `{element1,element2,"quoted element"}`

#### ArrayDeserializer

Parses database array text back to Java arrays:

```java
public interface ArrayDeserializer extends Deserializer {
    Object deserialize(String text, int offset, int endIndex,
                       MappingType type, TextFunction<?> func,
                       char[] boundaries, TextToIntFunc skipFunc,
                       StringBuilder builder);
}
```

**Builder Configuration** (extends `Deserializer.Builder`):

| Method                          | Description                                          |
|---------------------------------|------------------------------------------------------|
| `leftBoundary(char)`            | Left boundary character (default `{`)                |
| `rightBoundary(char)`           | Right boundary character (default `}`)               |
| `delim(char)`                   | Element delimiter (default `,`)                      |
| `skipPrefixFunc(TextToIntFunc)` | Function to skip explicit dimensions (e.g., `[1:3]`) |
| `backSlashEscapeOn(boolean)`    | Enable backslash escape                              |
| `nullAsNull(boolean)`           | Treat `null` text as Java `null`                     |

**Default PostgreSQL Array Deserializer** (from `PostgreArrays`):

```java
ArrayDeserializer deserializer = ArrayDeserializer.builder()
        .dataTypeLabel("PostgreSQL Array")
        .leftBoundary('{')
        .rightBoundary('}')
        .delim(',')
        .skipPrefixFunc(PostgreArrays::skipExplicitDimensions)
        .backSlashEscapeOn(true)
        .quoteEscapeOn(false)
        .nullAsNull(true)
        .allowQuote(true)
        .allowWhitespace(false)
        .allowNothing(false)
        .quoteChar('"')
        .build();
```

**Deserialization Flow**:

1. Skips explicit dimensions prefix (e.g., `[1:3][1:2]{...}`)
2. Parses elements between `{` and `}` boundaries
3. For each element, invokes `TextFunction` to convert text to Java object
4. Supports quoted elements with backslash escape
5. Supports multi-dimensional arrays

**How ArrayMappingType Uses Serializers**:

In array mapping types like `StringArrayType`:

- `beforeBind()`: Calls `PostgreArrays.arrayBeforeBind()` which uses `ArraySerializer`
- `afterGet()`: Calls `PostgreArrays.arrayAfterGet()` which uses `ArrayDeserializer`

**PostgreSQL Array Format**:

- Input: `{value1,value2,"quoted value"}`
- Output: Same format
- Supports backslash escape
- Supports multi-dimensional: `{{1,2},{3,4}}`

### Composite Types

Composite types map Java POJOs to database composite types (like PostgreSQL row types).

**Requirements**:

- POJO must be annotated with `@DefinedType(name = "TYPE_NAME", fieldOrder = {"field1", "field2"})`
- Fields must be public or have getters/setters

**Serialization Flow**:

```java
// Create composite type
CompositeType type = CompositeType.from(ProductInfo.class);

// Get field list
List<CompositeField> fields = type.fieldList();

// Access composite field
CompositeField priceField = type.field("price");
```

**PostgreSQL Composite Format**:

- Input: `'(value1,value2,"quoted value")'`
- Output: Same format
- Supports backslash escape and quote escape

### Nested Composite Types

Composite types can contain other composite types in serialization:

```java

import io.army.annotation.Column;

@DefinedType(name = "MANAGER_INFO", fieldOrder = {"name", "department"})
public class ManagerInfo {

    @Column
    public String name;

    @Column
    public String department;
}

@DefinedType(name = "PRODUCT_INFO", fieldOrder = {"name", "manager"})
public class ProductInfo {

    @Column
    public String name;

    @Column
    public ManagerInfo manager;  // Nested composite type
}
```

**Important**: Currently, **serialization supports nested composite types**, but **deserialization does not**. When
parsing composite strings, `RecordDeserializer` throws a syntax error if it encounters nested record boundaries. This
limitation is due to PostgreSQL's composite type input syntax not supporting nested records directly.

### Deserialization

Army provides `RecordDeserializer` for parsing composite type strings. It uses a callback-based approach where a
`TextFunction` is invoked for each field value.

**Builder Configuration**:

| Method                       | Description                                     |
|------------------------------|-------------------------------------------------|
| `dataTypeLabel(String)`      | Set the type name for error messages            |
| `leftBoundary(char)`         | Left boundary character (default `{`)           |
| `rightBoundary(char)`        | Right boundary character (default `}`)          |
| `delim(char)`                | Item delimiter (default `,`)                    |
| `quoteChar(char)`            | Quote character (`"` or `'`)                    |
| `backSlashEscapeOn(boolean)` | Enable backslash escape                         |
| `quoteEscapeOn(boolean)`     | Enable quote escape (e.g., `""` escapes to `"`) |
| `allowNothing(boolean)`      | Allow empty fields (nothing represents null)    |
| `allowWhitespace(boolean)`   | Allow whitespace in unquoted fields             |
| `allowQuote(boolean)`        | Allow quoted elements                           |
| `nullAsNull(boolean)`        | Treat `null` text as Java `null`                |

**PostgreSQL Composite Deserializer Example** (from `CompositeType`):

```java
RecordDeserializer deserializer = RecordDeserializer.builder()
        .dataTypeLabel("PostgreSQL Composite")
        .leftBoundary('(')
        .delim(',')
        .rightBoundary(')')
        .backSlashEscapeOn(true)
        .quoteEscapeOn(true)
        .quoteChar('"')
        .allowQuote(true)
        .allowNothing(true)
        .allowWhitespace(true)
        .nullAsNull(false)
        .build();
```

**How it works**:

1. Parses text between `leftBoundary` and `rightBoundary`
2. For each field, invokes the `TextFunction` with the field's offset and end index
3. Supports quoted elements with escape handling
4. Supports empty fields when `allowNothing(true)` is set
5. Calls the function with `offset=0, endIndex=0` to represent a null field