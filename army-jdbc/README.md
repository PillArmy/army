## army-jdbc

This module provides JDBC-based implementations for the `io.army.executor` package defined in `army-sync`.

### Core Components

- **`JdbcExecutorFactoryProvider`**: Entry point for creating JDBC executor factories. Accepts a `DataSource` and
  creates `ServerMeta` and `SyncExecutorFactory`.

- **`JdbcExecutorFactory`**: Creates `SyncLocalExecutor` and `SyncRmExecutor` instances based on the database type.

- **`JdbcExecutor`**: Abstract base class that implements `SyncExecutor` with JDBC. Provides common operations:
  - Execute updates (single and batch)
  - Execute queries (single and batch)
  - Transaction management (commit, rollback, savepoints)
  - Parameter binding with type mapping

- **Database-specific executors**:
  - `MySQLExecutor`: MySQL-specific implementation
  - `PostgreExecutor`: PostgreSQL-specific implementation
  - `SQLiteExecutor`: SQLite-specific implementation

- **`JdbcMetaExecutor`**: Executor for database metadata operations.

### Supported Databases

- MySQL
- PostgreSQL
- SQLite

### Dependencies

- `army-sync`: Core synchronous executor API
- Optional JDBC drivers (`postgresql`, `mysql-connector-j`, `gaussdbjdbc`)

### Usage

```java
DataSource dataSource = ...;
ArmyEnvironment env = ...;

JdbcExecutorFactoryProvider provider = JdbcExecutorFactoryProvider.create(dataSource, "myFactory", env);
ServerMeta serverMeta = provider.createServerMeta(null);
SyncExecutorFactory factory = provider.createFactory(executorEnv);

try(
SyncLocalExecutor executor = factory.localExecutor("session1", false, Option.EMPTY_FUNC)){
        // Execute SQL statements
        }
```
