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

`PgHstoreType` maps PostgreSQL `HSTORE` key/value type to Java types. It supports three mapping modes:

#### Mapping Modes

| Java Type                       | Factory Method                        | Description                                                              |
|---------------------------------|---------------------------------------|--------------------------------------------------------------------------|
| `Map<K, V>`                     | `fromMap(keyType, valueType)`         | Key-value mapping where K and V have default mappings                    |
| `EnumMap<K extends Enum<K>, V>` | `fromEnumMap(enumKeyType, valueType)` | Enum keys with arbitrary values                                          |
| POJO class                      | `from(pojoClass)`                     | POJO fields (with `@Column` annotation) mapped to hstore key-value pairs |

**Constraints**:

- Key type cannot be a composite type
- When value type is a composite type, it must be immutable (annotated with `@DefinedType(immutable = true)`)
- Both key and value types must have default mappings available (see [
  `_MappingFactory#getDefaultIfMatch`](../army-core/src/main/java/io/army/criteria/impl/_MappingFactory.java#L68-L123)
  for default mapping implementation)

#### Entity Field Examples

```java
import io.army.annotation.Column;
import io.army.annotation.Mapping;

import java.util.Map;
import java.util.EnumMap;
import java.time.DayOfWeek;

public class PostgreTypes {

    // Map<String, String> mapping
    @Mapping("io.army.mapping.postgre.PgHstoreType")
    @Column(comment = "hstore type")
    public Map<String, String> hstore;

    // EnumMap<DayOfWeek, String> mapping
    @Mapping("io.army.mapping.postgre.PgHstoreType")
    @Column(comment = "hstore type")
    public EnumMap<DayOfWeek, String> dayOfWeekStringEnumMap;

    // POJO mapping
    @Mapping("io.army.mapping.postgre.PgHstoreType")
    @Column(comment = "hstore type")
    public HstorePojo hstorePojo;
}
```

#### HSTORE Array Type

`PgHstoreArrayType` maps PostgreSQL `HSTORE[]` array type to Java array types. It supports the same three mapping modes
as `PgHstoreType`:

| Java Type                              | Factory Method                                   | Description                                      |
|----------------------------------------|--------------------------------------------------|--------------------------------------------------|
| `Map<K, V>[]`                          | `fromTypeArgs(javaType, keyType, valueType)`     | Array of `Map<K, V>` elements                    |
| `EnumMap<K extends Enum<K>, V>[]`      | `fromTypeArgs(javaType, enumKeyType, valueType)` | Array of `EnumMap<K, V>` elements                |
| POJO class array (e.g. `HstorePojo[]`) | `from(javaType)`                                 | Internal POJO type inferred from array component |

**Constraints**: Same as `PgHstoreType` — key type cannot be composite; composite value type must be immutable; key and
value must have default mappings (see [
`_MappingFactory#getDefaultIfMatch`](../army-core/src/main/java/io/army/criteria/impl/_MappingFactory.java#L68-L123)).

**Internals**: `PgHstoreArrayType` extends `_ArmyBuildInArrayType`, with three subclasses:

- `PgHstoreMapArrayType` — handles `Map<K,V>[]`, implements `DualGenericsMapping`
- `PgHstoreEnumMapArrayType` — handles `EnumMap<K extends Enum<K>,V>[]`, implements `DualGenericsMapping`
- `PgHstorePojoArrayType` — handles POJO arrays, cached via `ClassValue`

Serialization/deserialization per element delegates to `PgHstoreType.serialize` / `PgHstoreType.deserialize`, wrapping
with `PostgreArrays.arrayBeforeBind` / `PostgreArrays.arrayAfterGet`.

#### Entity Field Examples

```java
import io.army.annotation.Column;
import io.army.annotation.Mapping;

import java.util.Map;
import java.util.EnumMap;
import java.time.DayOfWeek;

public class PostgreTypes {

    // Map<String, String> array
    @Mapping("io.army.mapping.postgre.array.PgHstoreArrayType")
    @Column(comment = "hstore array type")
    public Map<String, String>[] hstoreArray;

    // EnumMap<DayOfWeek, String> array
    @Mapping("io.army.mapping.postgre.array.PgHstoreArrayType")
    @Column(comment = "hstore array type")
    public EnumMap<DayOfWeek, String>[] dayOfWeekStringEnumMapArray;

    // POJO array
    @Mapping("io.army.mapping.postgre.array.PgHstoreArrayType")
    @Column(comment = "hstore array type")
    public HstorePojo[] hstorePojoArray;
}
```

## License

Apache License 2.0