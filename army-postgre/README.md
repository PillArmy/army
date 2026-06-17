# Army PostgreSQL Module

## Overview

This module provides PostgreSQL dialect-specific criteria API and type mappings.

## Dependency

To use this module, add the `army-postgre` dependency to your project:

### Maven

```xml

<dependency>
    <groupId>io.army</groupId>
    <artifactId>army-postgre</artifactId>
    <version>${army.version}</version>
</dependency>
```

### Gradle

```groovy
implementation "io.army:army-postgre:${armyVersion}"
```

## SQL Statement Builders

All statement builders are available via `io.army.criteria.impl.Postgres`:

### SELECT Statements

```java
Postgres.query()       // Simple SELECT
Postgres.

batchQuery()  // Batch SELECT
Postgres.

subQuery()    // Subquery
Postgres.

scalarSubQuery() // Scalar subquery
```

### INSERT Statements

```java
Postgres.singleInsert() // Single-table INSERT
```

### UPDATE Statements

```java
Postgres.singleUpdate()      // Single-table UPDATE
Postgres.

batchSingleUpdate() // Batch single-table UPDATE with RETURNING
```

### DELETE Statements

```java
Postgres.singleDelete()      // Single-table DELETE
Postgres.

batchSingleDelete() // Batch single-table DELETE with RETURNING
```

### MERGE Statements

```java
Postgres.singleMerge() // Single-table MERGE
```

### VALUES Statements

```java
Postgres.valuesStmt() // VALUES statement
Postgres.

subValues()  // Sub-VALUES
```

### Cursor Statements

```java
Postgres.declareStmt()    // DECLARE cursor
Postgres.

closeCursor(name) // CLOSE specific cursor
Postgres.

closeAllCursor() // CLOSE ALL cursors
```

### Session Commands

```java
Postgres.setStmt()     // SET runtime parameter
Postgres.

show(name)    // SHOW parameter value
Postgres.

showAll()     // SHOW ALL parameters
```

## PostgreSQL-Specific Features

### RETURNING Clause

UPDATE and DELETE statements support `RETURNING` clause:

```java
Postgres.singleUpdate()
    .

with(...)
    .

update(...)
    .

set(...)
    .

where(...)
    .

returning(...)
    .

asReturningUpdate();
```

### CTE (WITH Clause)

All statements support Common Table Expressions:

```java
Postgres.query()
    .

with("cte_name")
    .

as(...)
    .

select(...)
    ...
```

### DISTINCT ON

PostgreSQL-specific `DISTINCT ON` syntax:

```java
Postgres.query()
    .

select(...)
    .

distinctOn(field1, field2)
    ...
```

### FOR UPDATE/SHARE

Locking clauses:

```java
Postgres.query()
    .

select(...)
    .

from(...)
    .

where(...)
    .

forUpdate()   // FOR UPDATE
    .

forShare()    // FOR SHARE
```

## PostgreSQL Operators

PostgreSQL-specific operators available via `Postgres`:

| Operator      | Description                         |
|---------------|-------------------------------------|
| `DARROW`      | `->` (JSON access)                  |
| `BI_ARROW`    | `->>` (JSON text access)            |
| `AMP_AMP`     | `&&` (overlap)                      |
| `POUND_POUND` | `##` (closest point)                |
| `AT_AT`       | `@@` (text search match)            |
| `TILDE`       | `~` (regex match)                   |
| `TILDE_STAR`  | `~*` (regex match case-insensitive) |

## Type Mappings

### HSTORE Type

`PgHstoreType` maps PostgreSQL `HSTORE` key/value type to Java `Map<String, String>`.

**Note**: Currently marked as TODO, not fully implemented.

## License

Apache License 2.0