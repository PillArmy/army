# Army Guava Module

## Overview

This module provides mapping support for Google Guava collection types to PostgreSQL range and multirange types.

## Dependency

To use this module, add the `army-guava` dependency to your project:

### Maven

```xml

<dependency>
    <groupId>io.army</groupId>
    <artifactId>army-guava</artifactId>
    <version>${army.version}</version>
</dependency>
```

### Gradle

```groovy
implementation "io.army:army-guava:${armyVersion}"
```

## Supported Types

### Range Types (`GuavaRangeType`)

Maps `com.google.common.collect.Range<C>` to PostgreSQL range types:

| Java Type               | PostgreSQL Type |
|-------------------------|-----------------|
| `Range<Integer>`        | `int4range`     |
| `Range<Long>`           | `int8range`     |
| `Range<BigDecimal>`     | `numrange`      |
| `Range<LocalDateTime>`  | `tsrange`       |
| `Range<OffsetDateTime>` | `tstzrange`     |
| `Range<LocalDate>`      | `daterange`     |

**Array Support**: `Range<C>[]` → PostgreSQL range array types (e.g., `int4range[]`) is implemented via
[GuavaRangeArrayType](file:///Users/zoro/repositories/java/hub/army/army-guava/src/main/java/io/army/mapping/guava/array/GuavaRangeArrayType.java).

### Multirange Types (`GuavaRangeSetType`)

Maps `com.google.common.collect.RangeSet<C>` to PostgreSQL multirange types:

| Java Type                  | PostgreSQL Type  |
|----------------------------|------------------|
| `RangeSet<Integer>`        | `int4multirange` |
| `RangeSet<Long>`           | `int8multirange` |
| `RangeSet<BigDecimal>`     | `nummultirange`  |
| `RangeSet<LocalDateTime>`  | `tsmultirange`   |
| `RangeSet<OffsetDateTime>` | `tstzmultirange` |
| `RangeSet<LocalDate>`      | `datemultirange` |

**Array Support**: `RangeSet<C>[]` → PostgreSQL multirange array types (e.g., `int4multirange[]`) is implemented via
[GuavaRangeSetArrayType](file:///Users/zoro/repositories/java/hub/army/army-guava/src/main/java/io/army/mapping/guava/array/GuavaRangeSetArrayType.java).

## Array Types

### Range Array Types (`GuavaRangeArrayType`)

Maps `Range<C>[]` to PostgreSQL range array types:

| Java Type                 | PostgreSQL Type |
|---------------------------|-----------------|
| `Range<Integer>[]`        | `int4range[]`   |
| `Range<Long>[]`           | `int8range[]`   |
| `Range<BigDecimal>[]`     | `numrange[]`    |
| `Range<LocalDateTime>[]`  | `tsrange[]`     |
| `Range<OffsetDateTime>[]` | `tstzrange[]`   |
| `Range<LocalDate>[]`      | `daterange[]`   |

### Multirange Array Types (`GuavaRangeSetArrayType`)

Maps `RangeSet<C>[]` to PostgreSQL multirange array types:

| Java Type                    | PostgreSQL Type    |
|------------------------------|--------------------|
| `RangeSet<Integer>[]`        | `int4multirange[]` |
| `RangeSet<Long>[]`           | `int8multirange[]` |
| `RangeSet<BigDecimal>[]`     | `nummultirange[]`  |
| `RangeSet<LocalDateTime>[]`  | `tsmultirange[]`   |
| `RangeSet<OffsetDateTime>[]` | `tstzmultirange[]` |
| `RangeSet<LocalDate>[]`      | `datemultirange[]` |

### Multi-dimensional Arrays

Both array types support multi-dimensional arrays:

| Java Type               | PostgreSQL Type      |
|-------------------------|----------------------|
| `Range<Integer>[][]`    | `int4range[][]`      |
| `RangeSet<Integer>[][]` | `int4multirange[][]` |
| `Range<Integer>[][][]`  | `int4range[][][]`    |

## Usage Example

### Entity Field Mapping

```java
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import io.army.annotation.Column;
import io.army.annotation.Mapping;

public class PostgreTypes {

    @Mapping("io.army.mapping.guava.GuavaRangeType")
    @Column(comment = "int4range type")
    public Range<Integer> int4RangeGuava;

    @Mapping("io.army.mapping.guava.GuavaRangeSetType")
    @Column(comment = "int4multirange type")
    public RangeSet<Integer> int4RangeSetGuava;

    // Array types
    @Mapping("io.army.mapping.guava.array.GuavaRangeArrayType")
    @Column(comment = "int4range array type")
    public Range<Integer>[] int4RangeArrayGuava;

    @Mapping("io.army.mapping.guava.array.GuavaRangeSetArrayType")
    @Column(comment = "int4multirange array type")
    public RangeSet<Integer>[] int4RangeSetArrayGuava;
}
```

### Range Operations

```java
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

// All range types supported
Range<Integer> all = Range.all();                        // (,)
        Range<Integer> singleton = Range.singleton(0);           // [0,0]
        Range<Integer> upToClosed = Range.upTo(0, BoundType.CLOSED);  // (,0]
        Range<Integer> upToOpen = Range.upTo(0, BoundType.OPEN);      // (,0)
        Range<Integer> downToClosed = Range.downTo(0, BoundType.CLOSED); // [0,)
        Range<Integer> downToOpen = Range.downTo(0, BoundType.OPEN);     // (0,)
        Range<Integer> range = Range.range(0, BoundType.OPEN, 10, BoundType.CLOSED); // (0,10]
```

### RangeSet Operations

```java
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

RangeSet<Integer> rangeSet = TreeRangeSet.create();
rangeSet.

add(Range.open(1, 3));           // {(1,3)}
        rangeSet.

add(Range.closed(5, 10));        // {(1,3), [5,10]}
        rangeSet.

add(Range.openClosed(15, 20));   // {(1,3), [5,10], (15,20]}
```

## License

Apache License 2.0