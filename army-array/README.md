# army-array

The `army-array` module provides **array type mappings** for the Army framework. Classes in this module are array
versions of the core mapping types defined in the `io/army/mapping` package of the `army-core` module.

## Purpose

This module extends `army-core` by adding support for array data types, enabling Army to handle:

- Single-dimensional arrays (e.g., `String[]`)
- Multi-dimensional arrays (e.g., `String[][]`, `String[][][]`)

## Architecture

### Base Class

All array types inherit from `_ArmyCoreArrayType`, which:

- Extends `_ArmyBuildInCoreType`
- Implements `ArrayMappingType` interface
- Provides factory methods for creating array type instances via reflection

### Core Array Types

Array types in this module correspond to core mapping types in `army-core`:

| Category         | Army-Core Type       | Army-Array Type           |
|------------------|----------------------|---------------------------|
| Primitive/Boxing | `BooleanType`        | `BooleanArrayType`        |
|                  | `IntegerType`        | `IntegerArrayType`        |
|                  | `LongType`           | `LongArrayType`           |
|                  | `FloatType`          | `FloatArrayType`          |
|                  | `DoubleType`         | `DoubleArrayType`         |
|                  | `ShortType`          | `ShortArrayType`          |
|                  | `ByteType`           | `ByteArrayType`           |
|                  | `CharacterType`      | `CharacterArrayType`      |
| Decimal          | `BigDecimalType`     | `BigDecimalArrayType`     |
|                  | `BigIntegerType`     | `BigIntegerArrayType`     |
| Date/Time        | `LocalDateType`      | `LocalDateArrayType`      |
|                  | `LocalTimeType`      | `LocalTimeArrayType`      |
|                  | `LocalDateTimeType`  | `LocalDateTimeArrayType`  |
|                  | `OffsetDateTimeType` | `OffsetDateTimeArrayType` |
|                  | `OffsetTimeType`     | `OffsetTimeArrayType`     |
|                  | `ZonedDateTimeType`  | `ZonedDateTimeArrayType`  |
|                  | `YearType`           | `YearArrayType`           |
|                  | `YearMonthType`      | `YearMonthArrayType`      |
|                  | `MonthDayType`       | `MonthDayArrayType`       |
|                  | `InstantType`        | `InstantArrayType`        |
| String/Text      | `StringType`         | `StringArrayType`         |
|                  | `TextType`           | `TextArrayType`           |
|                  | `SqlCharType`        | `SqlCharArrayType`        |
| Binary           | `BinaryType`         | `BinaryArrayType`         |
|                  | `VarBinaryType`      | `VarBinaryArrayType`      |
|                  | `BlobType`           | `BlobArrayType`           |
| JSON/XML         | `JsonType`           | `JsonArrayType`           |
|                  | `JsonbType`          | `JsonbArrayType`          |
|                  | `PreferredJsonbType` | `PreferredJsonbArrayType` |
|                  | `XmlType`            | `XmlArrayType`            |
| Enum             | `NameEnumType`       | `NameEnumArrayType`       |
|                  | `LabelEnumType`      | `LabelEnumArrayType`      |
|                  | `CodeEnumType`       | `CodeEnumArrayType`       |
| Composite        | `CompositeType`      | `CompositeArrayType`      |
| Other            | `UUIDType`           | `UUIDArrayType`           |
|                  | `BitSetType`         | `BitSetArrayType`         |
|                  | `ZoneIdType`         | `ZoneIdArrayType`         |
|                  | `VectorType`         | `VectorArrayType`         |
|                  | `MappingTypeType`    | `MappingTypeArrayType`    |
|                  | `IntervalType`       | `IntervalArrayType`       |
|                  | `SqlRecordType`      | `SqlRecordArrayType`      |

## Key Features

### Array Creation

Each array type provides a static factory method `from(Class<?> javaType)`:

```java
StringArrayType arrayType = StringArrayType.from(String[].class);
IntegerArrayType twoDimensional = IntegerArrayType.from(Integer[][].class);
```

### Dimension Handling

Array types support multiple dimensions:

- **Linear arrays**: `String[].class`
- **Multi-dimensional**: `String[][].class`, `String[][][].class`, etc.
- **Unlimited dimension**: Using `Object.class` as the javaType

### Underlying Type Access

Each array type maintains a reference to its corresponding non-array type:

```java
StringArrayType arrayType = StringArrayType.from(String[].class);
Class<?> underlyingJavaType = arrayType.underlyingJavaType(); // String.class
MappingType underlyingType = arrayType.underlyingType(); // StringType.INSTANCE
```

### Higher-Dimensional Arrays

Use `arrayTypeOfThis()` to create higher-dimensional array types:

```java
StringArrayType linear = StringArrayType.from(String[].class);
MappingType twoDim = linear.arrayTypeOfThis(); // StringArrayType for String[][]
```

## Supported Databases

Array types primarily target **PostgreSQL**, which has native support for SQL arrays. Other databases may throw
`UnsupportedDialectException` if they don't support array types natively.

## Dependency

This module depends on `army-core`:

```xml

<dependency>
    <groupId>io.qinarmy</groupId>
    <artifactId>army-core</artifactId>
</dependency>
```

## Design Patterns

### Reflection-Based Factory Method Invocation

The `io.army.criteria.impl._MappingFactory` class uses reflection to dynamically invoke factory methods from
`_ArmyCoreArrayType` to avoid compile-time dependencies between `army-core` and `army-array`.

**How it works:**

1. `_MappingFactory.createDefaultArrayFuncMap()` checks if `io.army.mapping.array._ArmyCoreArrayType` is present in the
   classpath using `ClassUtils.isPresent()`
2. If present, it uses `ReflectionUtils.invokeStaticFactoryMethod()` to call `createCoreArrayFuncMap()` which returns a
   `Map<Class<?>, Function<Class<?>, MappingType>>` mapping Java types to their array type factories
3. Similarly, `ArrayFuncHolder` uses reflection to obtain factory functions for composite, code enum, label enum, and
   name enum array types

**Why this design:**

- **Cross-module invocation**: `army-core` and `army-array` are separate modules. `army-core` cannot directly invoke
  methods from `army-array` classes at compile time via normal Java code. Reflection is used to call methods across
  module boundaries at runtime

```java
// In _MappingFactory
private static Map<Class<?>, Function<Class<?>, MappingType>> createDefaultArrayFuncMap() {
    final String className = "io.army.mapping.array._ArmyCoreArrayType";
    if (ClassUtils.isPresent(className, null)) {
        return (Map<Class<?>, Function<Class<?>, MappingType>>)
                ReflectionUtils.invokeStaticFactoryMethod(className, Map.class, "createCoreArrayFuncMap");
    } else {
        return Map.of();
    }
}
```

### Array Type Registration and Retrieval

Core mapping types in `army-core` use a registry pattern to register and retrieve their corresponding array types.

**Registration (in army-array):**

Each array type registers its factory function during class initialization using `addArrayFromFunc()`:

```java
// In StringArrayType
static {
    addArrayFromFunc(StringArrayType.class, StringArrayType::from);
}
```

**Retrieval (in army-core):**

Core mapping types retrieve the factory function using `removeArrayFromFunc()`:

```java
// In StringType
private static class ArrayFactoryFuncHolder {
    private static final Function<Class<?>, MappingType> FUNCTION;

    static {
        FUNCTION = removeArrayFromFunc(StringType.class);
    }
}

@Override
public MappingType arrayTypeOfThis() throws CriteriaException {
    return ArrayFactoryFuncHolder.FUNCTION.apply(String[].class);
}
```

**How `removeArrayFromFunc()` works:**

1. It constructs the array type class name by replacing `Type` suffix with `ArrayType` (e.g., `StringType` →
   `StringArrayType`)
2. It loads the array type class via `Class.forName()`
3. It removes and returns the registered factory function from the static registry map

**Why this design:**

- **Cross-module invocation**: Core types in `army-core` cannot directly reference array types in `army-array` at
  compile time. The registry pattern allows `army-core` to obtain array type factories from `army-array` without any
  compile-time dependency
- **Static registry**: Uses `ConcurrentHashMap` for thread-safe registration and retrieval across modules
- **Fail-fast**: If `army-array` is not in the classpath, throws a clear runtime error message

## Usage

Array types are typically obtained through the `arrayTypeOfThis()` method on core mapping types:

```java
MappingType stringType = StringType.INSTANCE;
MappingType stringArrayType = stringType.arrayTypeOfThis(); // StringArrayType
```

Or directly via the static `from()` method:

```java
MappingType intArrayType = IntegerArrayType.from(int[].class);
```