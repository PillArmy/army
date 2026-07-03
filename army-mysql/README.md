## army-mysql

This module provides MySQL dialect-specific criteria API and SQL handling.

### Main Packages

#### io.army.criteria.mysql

MySQL-specific criteria interfaces for building SQL statements.

- **`MySQLQuery`**: Build SELECT queries with MySQL-specific features.
- **`MySQLUpdate`**: Build UPDATE statements with MySQL extensions.
- **`MySQLDelete`**: Build DELETE statements with MySQL features.
- **`MySQLInsert`**: Build INSERT statements with MySQL-specific syntax.
- **`MySQLReplace`**: Build REPLACE statements (MySQL-specific).
- **`MySQLLoadData`**: Build LOAD DATA statements for bulk import.
- **`MySQLWindow`**: Build window functions with MySQL syntax.

#### io.army.criteria.impl

Entry points for MySQL-specific criteria API.

- **`MySQLs`**: Main entry point for all MySQL statement builders.
- **`MySQLQueries`**: MySQL-specific query builders.
- **`MySQLUpdates`**: MySQL-specific update builders.
- **`MySQLDeletes`**: MySQL-specific delete builders.
- **`MySQLInserts`**: MySQL-specific insert builders.
- **`MySQLFunctions`**: MySQL-specific functions.
- **`MySQLHints`**: MySQL query hints.
- **`MySQLLoads`**: LOAD DATA builders.

#### io.army.dialect

MySQL dialect implementation.

- **`MySQLDialect`**: MySQL-specific SQL syntax handling.
- **`MySQLParser`**: MySQL SQL parser.
- **`MySQLIdentifierHandler`**: MySQL identifier quoting.

### MySQL-Specific Features

- **Index Hints**: `USE INDEX`, `FORCE INDEX`, `IGNORE INDEX`
- **REPLACE Statement**: MySQL-specific upsert operation
- **LOAD DATA**: Bulk data import from files
- **JSON Functions**: MySQL JSON path expressions
- **Window Functions**: MySQL window function support
- **Duplicate Key Update**: INSERT ... ON DUPLICATE KEY UPDATE

### Usage Example

```java
// Build a MySQL-specific SELECT query with index hint
Query query = MySQLs.query()
                .select(Stock_.id, Stock_.name)
                .from(Stock_.T)
                .useIndex("idx_stock_code")
                .where(Stock_.code.eq("000001"))
                .asQuery();

// Build an INSERT with ON DUPLICATE KEY UPDATE
Insert insert = MySQLs.singleInsert()
        .insert(Stock_.T)
        .values(...)
        .

onDuplicateKeyUpdate()
        .

set(Stock_.name, "Updated Name")
        .

asInsert();

// Build a REPLACE statement
Insert replace = MySQLs.replace()
        .replace(Stock_.T)
        .values(...)
        .

asInsert();
```

### Dependencies

- **`army-core`**: Core criteria API and type mappings.