# Army

[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/s01.oss.sonatype.org/io.qinarmy/army.svg)](https://s01.oss.sonatype.org/content/repositories/snapshots/io/qinarmy/army/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.qinarmy/army/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.qinarmy/army)
[![Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java support](https://img.shields.io/badge/Java-25+-green?logo=java&logoColor=white)](https://openjdk.java.net/)

## Design Philosophy

1. Don't create new world,just mapping real world.
2. We need standard,we need dialect,it's real world.

---

## Summary

Army gives you a type-safe, composable, dialect-aware API for writing SQL. It doesn't manage
sessions, cascade deletes, or generate schemas — other tools already do those things well. Army
does one thing: turns your domain model into correct, safe, readable SQL queries.

If you know SQL and want a framework that respects that, Army might be for you.

If you already know SQL and want a framework that respects that, Army is for you.

## Army convention

1. The interface( or class) that start with underline is army framework private interface( or class)

### [Army document](https://pillarmy.github.io/army/ "Army document pages")

### How to start ?

#### Maven

```xml

<dependencies>
    <dependency>
        <groupId>io.qinarmy</groupId>
        <artifactId>army-jdbc</artifactId>
        <version>0.6.6</version>
    </dependency>
    <dependency>
        <groupId>io.qinarmy</groupId>
        <artifactId>army-postgre</artifactId>
        <version>0.6.6</version>
    </dependency>
</dependencies>
```

##### appropriate maven module that contain domain class

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessors>io.army.modelgen.ArmyMetaModelDomainProcessor</annotationProcessors>
            </configuration>
        </plugin>
    </plugins>
</build>

```

#### Using army annotations mapping pojo ChinaRegion to china_region table

```java

// mapping domain to table

@Table(name = "stock",
        indexes = @Index(name = "uni_stock_exchange_code", fieldList = {"exchange", "code"}, unique = true),
        comment = "stock")
public class Stock {

    // auto-increment primary key use below:
    //   @Generator(type = GeneratorType.POST)
    @Generator(value = "io.army.generator.snowflake.Snowflake8Generator",
            params = @Param(name = "startTime", value = "1779012232202"))
    @Column
    private long id;

    @Column
    private LocalDateTime createTime;

    @Column
    private LocalDateTime updateTime;

    @Column(comment = "version")
    private int version;

    @Column(notNull = true, updatable = false, precision = 5, comment = "exchange code")
    private String exchange;

    @Column(notNull = true, updatable = false, precision = 15, comment = "stock code")
    private String code;

    @Column(notNull = true, precision = 130, comment = "company name")
    private String name;

    @Column(notNull = true, precision = 10, defaultValue = "'NORMAL'", comment = "listing status")
    private StockStatus status;

    @Column(precision = 10, scale = 2, defaultValue = "0.00", comment = "offer price")
    private BigDecimal offerPrice;

    @Column(defaultValue = "DATE '1970-01-01'", comment = "listing date")
    private LocalDate listingDate;


}


```

##### io.army.modelgen.ArmyMetaModelDomainProcessor generate static metamodel class

```java

// Army static metamodel class

@Generated(value = "io.army.modelgen.ArmyMetaModelDomainProcessor",
        date = "2024-01-02 07:13:54.605524+08:00",
        comments = "stock")
public abstract class Stock_ {

    private Stock_() {
        throw new UnsupportedOperationException();
    }

    public static final SimpleTableMeta<Stock> T;

    static {
        T = _TableMetaFactory.getSimpleTableMeta(Stock.class);

        final int fieldSize = T.fieldList().size();
        if (fieldSize != 11) {
            throw _TableMetaFactory.tableFiledSizeError(Stock.class, fieldSize);
        }
    }

    // Field name constants (type-safe references)
    public static final String NAME = "name";
    public static final String OFFER_PRICE = "offerPrice";
    public static final String EXCHANGE = "exchange";
    public static final String CODE = "code";
    // ... other fields

    // Field metadata
    public static final FieldMeta<Stock> name = T.field(NAME);
    public static final FieldMeta<Stock> offerPrice = T.field(OFFER_PRICE);
    public static final PrimaryFieldMeta<Stock> id = T.id();
    // ... other field metadata
}

```

## Why Army?

**Army is a type-safe, composable SQL DSL for Java — not an ORM, not a code generator, not a template engine.** It
treats SQL as the right abstraction and gives you an API that matches its power without hiding it.

Here's what a query looks like:

```java

// Write queries: type-safe, composable, dialect-aware
// import static io.army.criteria.impl.SQLs.*
Select stmt = SQLs.query()
        .select(s -> s.space(Stock_.id, Stock_.code, Stock_.name, Stock_.offerPrice)
                .comma(Exchange_.name, Exchange_.country))
        .from(Stock_.T, AS, "s")
        .join(Exchange_.T, AS, "e")
        .on(Stock_.exchange.equal(Exchange_.code))
        .where(Stock_.status.equal(StockStatus.ACTIVE))
        .and(Stock_.listingDate.greaterEqual(LocalDate.of(2020, 1, 1)))
        .orderBy(Stock_.offerPrice.desc())
        .limit(20)
        .asQuery();

List<StockSummary> results = session.queryObjectList(stmt, StockSummary::new);
```

A query that reads like SQL and returns your POJO.

---

### What makes Army different

Army is built on three design choices:

#### 1. Composable DSL — every clause is a reusable function

Army's `where`/`and` takes `Expression` objects, not lambdas. This means every condition
is a first-class object — you can pass it around, combine it, or extract it when needed.

But the idiomatic style is inline — just like writing SQL:

```java
Select stmt = SQLs.query()
        .select(Stock_.id, Stock_.code)
        .from(Stock_.T, AS, "s")
        .where(Stock_.status.equal(StockStatus.ACTIVE))
        .and(Stock_.listingDate.greaterEqual(LocalDate.of(2020, 1, 1)))
        .asQuery();
```

Conditions are `Expression` objects — inline by default, extractable when you need them.

#### 2. Three-layer type system — dialects are first-class, not an afterthought

Most frameworks flatten type mapping into a single step: database type name → Java class.
This breaks as soon as you need cross-dialect support.

Army separates the mapping into three orthogonal layers:

| Layer           | Question                         | Examples                                                  |
|-----------------|----------------------------------|-----------------------------------------------------------|
| **SQLType**     | What does THIS database call it? | `PgType.BIGINT` → `("BIGINT", Long.class)`                |
|                 |                                  | `MySQLType.INT_UNSIGNED` → `("INT UNSIGNED", Long.class)` |
|                 |                                  | `PgType.INT4RANGE` → `("INT4RANGE", Range.class)`         |
| **ArmyType**    | What does this type MEAN?        | `BIGINT` — 64-bit signed integer                          |
|                 |                                  | `INTEGER_UNSIGNED` — 32-bit unsigned (not signed)         |
|                 |                                  | `DIALECT_TYPE` — exists only in some databases            |
| **MappingType** | How does Java talk to JDBC?      | `beforeBind(Java)` → JDBC-compatible value                |
|                 |                                  | `afterGet(JDBC)` → Java value                             |

**Why this matters:**

MySQL `TINYINT UNSIGNED` is 0–255. Java `Byte` is -128–127. Army's `MySQLType.TINYINT_UNSIGNED` maps to `Short.class` —
automatically, because the type system knows what an unsigned 8-bit integer *means*.

Similarly, `SMALLINT UNSIGNED` (0–65535) maps to `Integer.class` — not `Short.class` — because 65535 doesn't fit in a
Java `short`. Army picks the smallest Java type that can't overflow. You don't configure it. It's already correct.

All five MySQL unsigned integer types are first-class citizens:

| MySQL Type           | Java Type    | Range             | Why this type?            |
|----------------------|--------------|-------------------|---------------------------|
| `TINYINT UNSIGNED`   | `Short`      | 0 ~ 255           | 255 塞不进 `byte`（-128~127）  |
| `SMALLINT UNSIGNED`  | `Integer`    | 0 ~ 65,535        | 65535 塞不进 `short`（±32767） |
| `MEDIUMINT UNSIGNED` | `Integer`    | 0 ~ 16,777,215    | 还在 `int` 范围内              |
| `INT UNSIGNED`       | `Long`       | 0 ~ 4,294,967,295 | 42 亿 塞不进 `int`（±21 亿）     |
| `BIGINT UNSIGNED`    | `BigInteger` | 0 ~ 2^64 − 1      | 已超 `long` 上限              |

#### 3. Results are plain POJOs — no state machine, no proxy, no `RecordN`

```java
List<StockSummary> stocks = session.queryObjectList(stmt, StockSummary::new);
StockSummary one = session.queryObject(stmt, StockSummary::new);
long count = session.queryOne(countStmt, Long.class);
```

The result is **your** class, constructed via method reference (compile-time checked). No `RecordN<T1,T2,...,TN>` with
`value1()`/`value2()`. No `attached`/`detached` lifecycle. No lazy-loading proxy that explodes when the session closes.
You can serialize it, cache it, pass it across threads — it's just data.

**Army deliberately does NOT manage:**

- Database connections → use your preferred connection pool
- Transactions → use Spring `@Transactional` or JTA
- Caches → use Redis, Caffeine, or your own
- Schema migrations → use Flyway or Liquibase

It's a type-safe SQL API. It does one thing and gets out of your way.

---

### Type mapping: define once, works everywhere

Army's `beforeBind` / `afterGet` contract means type conversion is a property of the type itself — not a per-column
annotation, not a per-query declaration:

```java
// CodeEnum: enums stored as integer codes (the most common real-world pattern)
public enum StockStatus implements CodeEnum<StockStatus> {
    NORMAL(0), SUSPENDED(1), DELISTED(2);
}

// Use anywhere — no @Enumerated, no TypeHandler, no Converter
Select stmt = SQLs.query()
        .select(Stock_.status)
        .from(Stock_.T, AS, "s")
        .where(Stock_.status.equal(StockStatus.NORMAL))
        .asQuery();
// Before binding: StockStatus.NORMAL → 0   (beforeBind extracts code)
// After getting:  0 → StockStatus.NORMAL   (afterGet looks up enum)
```

Three enum strategies out of the box: `CodeEnum` (integer), `LabelEnum` (string label), `NameEnum` (Java name). Plus
MySQL `SET` type → `EnumSet`.

---

### PostgreSQL: first-class dialect support

Army's `PgType` enum covers PostgreSQL-specific types — no plugins, no code-gen config, no
TypeHandler:

| Category   | Types                                                                                                                                            |
|------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| Range (12) | `INT4RANGE`, `INT8RANGE`, `NUMRANGE`, `TSRANGE`, `TSTZRANGE`, `DATERANGE`, plus 6 multirange variants                                            |
| Array (54) | `BOOLEAN_ARRAY`, `INTEGER_ARRAY`, `BIGINT_ARRAY`, `TEXT_ARRAY`, `UUID_ARRAY`, `JSONB_ARRAY`, ...                                                 |
| pgvector   | `l2Distance`, `cosineDistance`, `hammingDistance`, `jaccardDistance`, `innerProduct`, `l1Distance` — all six as first-class expression operators |
| Built-in   | `UUID` → `java.util.UUID`, `JSONB`                                                                                                               |

---

### Compile-time safety by design

```java
// Sealed type hierarchy — compiler enforces exhaustiveness
public sealed interface SQLType extends DataType
        permits PgType, MySQLType, SQLiteType, OracleType, H2Type {
}

// @Support annotation — IDE shows dialect compatibility at the call site
@Support(PostgreSQL)
Expression l2Distance(Object operand);   // pgvector: only PostgreSQL

// Keywords are types, not strings — compiler catches mistakes
SQLs.

query().

from(Stock_.T, AS, "s")     // ✅ WordAs
//     .from(Stock_.T, ON, "s")          // ❌ COMPILE ERROR: wrong type
```

