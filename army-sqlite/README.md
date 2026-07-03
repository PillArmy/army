## army-sqlite

This module provides SQLite dialect-specific criteria API and SQL handling.

### Main Packages

#### io.army.criteria.sqlite

SQLite-specific criteria interfaces.

- **`SQLiteStatement`**: Base interface for SQLite statements.

#### io.army.criteria.impl

Entry points for SQLite-specific criteria API.

- **`SQLites`**: Main entry point for SQLite statement builders.
- **`SQLiteFunctions`**: SQLite-specific functions.
- **`SQLiteSyntax`**: SQLite-specific syntax utilities.

#### io.army.dialect

SQLite dialect implementation.

- **`SQLiteDialect`**: SQLite-specific SQL syntax handling.
- **`SQLiteParser`**: SQLite SQL parser.
- **`SQLiteMappingHandler`**: SQLite type mapping handler.

### SQLite-Specific Features

- **SQLite Functions**: Built-in SQLite functions support.
- **Auto-increment**: SQLite INTEGER PRIMARY KEY AUTOINCREMENT.
- **Limit/Offset**: SQLite LIMIT and OFFSET support.
- **Type Affinity**: SQLite dynamic typing support.

### Usage Example

```java
// Build a SQLite SELECT query
Query query = SQLites.query()
        .select(Stock_.id, Stock_.name)
        .from(Stock_.T)
        .where(Stock_.code.eq("000001"))
        .limit(10)
        .offset(20)
        .asQuery();
```

### Dependencies

- **`army-core`**: Core criteria API and type mappings.