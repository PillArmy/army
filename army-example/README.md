## army-example

This module provides comprehensive tests and API usage examples for the Army framework.

### Test Coverage

- **Criteria API**: Unit tests for MySQL, PostgreSQL, and standard SQL dialects
- **Session API**: Synchronous and reactive session tests
- **Mapping**: Array types, composite types, HStore, XML, and range type tests
- **Dialect**: MySQL and PostgreSQL DDL generation tests
- **POJO**: Object accessor factory tests

### Example Applications

- **Bank Application**: A banking domain example with user registration, account management, and region data
- **Pill Application**: A simple CRUD application demonstrating both synchronous and reactive service patterns

### Key Features Demonstrated

1. Domain modeling with `@Table`, `@Column`, `@Generator` annotations
2. Type-safe criteria queries using generated metamodel
3. Service layer with transaction management
4. DAO layer with session-based data access
5. Support for MySQL, PostgreSQL, and SQLite databases
