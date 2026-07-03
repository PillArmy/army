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