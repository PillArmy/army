# Army is a better SQL framework

[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/s01.oss.sonatype.org/io.qinarmy/army.svg)](https://s01.oss.sonatype.org/content/repositories/snapshots/io/qinarmy/army/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.qinarmy/army/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.qinarmy/army)
[![Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java support](https://img.shields.io/badge/Java-25+-green?logo=java&logoColor=white)](https://openjdk.java.net/)

## Design Philosophy

1. Don't create new world,just mapping real world.
2. We need standard,we need dialect,it's real world.

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

    //auto-increment primary key use below:
    //  @Generator(type = GeneratorType.POST)    
    @Generator(value = "io.army.generator.snowflake.Snowflake8Generator",
            params = @Param(name = "startTime", value = "1779012232202"))
    @Column
    public long id;

    @Column
    public LocalDateTime createTime;

    @Column
    public LocalDateTime updateTime;

    @Column(comment = "version")
    public int version;

    @Column(notNull = true, updatable = false, precision = 5, comment = "exchange code")
    public String exchange;

    @Column(notNull = true, updatable = false, precision = 15, comment = "stock code")
    public String code;

    @Column(notNull = true, precision = 130, comment = "company name")
    public String name;

    @Column(notNull = true, precision = 10, defaultValue = "'NORMAL'", comment = "listing status")
    public StockStatus status;

    @Column(precision = 10, scale = 2, defaultValue = "0.00", comment = "offer price")
    public BigDecimal offerPrice;

    @Column(defaultValue = "DATE '1970-01-01'", comment = "listing date")
    public LocalDate listingDate;


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

