## army-sync

This module provides blocking (synchronous) session APIs for database access.

### Main Packages

#### io.army.sync.session

Blocking session interfaces for database operations.

- **`SyncSessionFactory`**: Creates blocking database sessions.
- **`SyncSession`**: Represents a blocking database session.
- **`SyncLocalSession`**: Blocking session for local transactions.
- **`SyncRmSession`**: Blocking session for XA transactions (two-phase commit).

#### io.army.sync.executor

Blocking executor interfaces for running SQL statements.

- **`SyncExecutor`**: Base interface for blocking SQL execution.
- **`SyncLocalExecutor`**: Blocking executor for local transactions.
- **`SyncRmExecutor`**: Blocking executor for XA transactions.
- **`SyncMetaExecutor`**: Blocking executor for metadata operations.
- **`SyncExecutorFactory`**: Creates blocking executors.

#### io.army.sync.result

Blocking result interfaces for query results.

- **`SyncCursor`**: Blocking cursor for streaming results.
- **`SyncBatchQuery`**: Blocking batch query results.
- **`SyncMultiQuery`**: Blocking multi-query results.

#### io.army.sync.dao

Base DAO classes for data access.

- **`SyncBaseDao`**: Base DAO interface for blocking operations.
- **`ArmySyncBaseDao`**: Army implementation of blocking base DAO.

### Key Features

1. **Blocking Operations**: All database operations block until completion.
2. **Transaction Support**: Local and XA transactions.
3. **Query Execution**: Execute SELECT, INSERT, UPDATE, DELETE statements.
4. **Streaming Support**: Stream large result sets with cursors.

### Usage Example

```java
// Create session factory
SyncSessionFactory factory = ...;

// Open a local session
        try(
SyncLocalSession session = factory.localSession()){
        // Begin transaction
        session.

begin();

// Execute query
List<Stock> stocks = session.query(query).list();

// Execute update
int affected = session.update(update).execute();

// Commit transaction
    session.

commit();
}
```

### Dependencies

- **`army-core`**: Core types and criteria API.
- **`army-struct`**: Core type definitions.