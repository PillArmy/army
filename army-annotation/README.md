## army-annotation

This module provides annotations and a compile-time annotation processor for Army framework.

### Annotations

Define database mapping metadata for domain entity classes:

- **`@Table`**: Maps a Java class to a database table. Configures table name, schema, indexes, DDL mode, and table
  options.
- **`@Column`**: Maps a Java field to a database column. Configures column name, nullability, precision, scale, and
  default value.
- **`@Generator`**: Configures ID generation strategy (e.g., Snowflake8).
- **`@Mapping`**: Specifies custom mapping type for a field.
- **`@Index`**: Defines table indexes for DDL generation.
- **`@Inheritance`**: Configures inheritance strategy for entity hierarchies.
- **`@MappedSuperclass`**: Marks a class whose mapping information is inherited by subclasses.
- **`@DiscriminatorValue`**: Specifies the discriminator value for entity inheritance.
- **`@OverrideParams`**: Overrides mapping parameters for a field.
- **`@DefinedType`**: Defines a composite type for database composite types like PostgreSQL. *(Declared in `army-struct`
  module)*

### Annotation Processor

**`ArmyMetaModelDomainProcessor`**: A compile-time annotation processor that:

1. Scans classes annotated with `@Table`
2. Parses annotation metadata (table name, columns, indexes, etc.)
3. Generates static metamodel classes with type-safe field references
4. Supports placeholder expressions (`${DEFAULT}`, `${RUNTIME}`) resolved from `TableMeta.properties`

### How to Use ArmyMetaModelDomainProcessor

#### 1. Add Dependency

Add `army-annotation` as a compile-time dependency in your build tool:

**Maven**:

```xml

<dependency>
  <groupId>io.army</groupId>
  <artifactId>army-annotation</artifactId>
  <version>${army.version}</version>
  <scope>provided</scope>
</dependency>
```

**Gradle**:

```groovy
implementation "io.army:army-annotation:${armyVersion}"
```

#### 2. Configure Annotation Processor

Configure the Maven compiler plugin to use `ArmyMetaModelDomainProcessor`:

**Maven**:

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

**Gradle**:

```groovy
annotationProcessor "io.army:army-annotation:${armyVersion}"
```

#### 3. Annotate Domain Classes

Add `@Table` annotation to your domain classes:

```java
@Table(name = "stock", comment = "股票表")
public class Stock {
    @Column(notNull = true)
    public long id;
    
    @Column(notNull = true, precision = 130)
    public String name;
    
    @Column
    public BigDecimal price;
}
```

#### 4. Compile Your Project

The processor runs automatically during compilation. Generated classes are placed in:

```
target/generated-sources/annotations/
```

**Maven Compile**:

```bash
mvn compile
```

**Gradle Compile**:

```bash
gradle compileJava
```

#### 5. Processor Output

During compilation, the processor outputs progress information:

```
[INFO] io.army.modelgen.ArmyMetaModelDomainProcessor generate 5 army static metamodel class source file, take 120 ms.
```

#### 6. Use Generated Metamodel

The processor generates `<DomainName>_` classes that you can use in your Criteria API queries:

```java
// Type-safe table reference
SimpleTableMeta<Stock> stockTable = Stock_.T;

// Type-safe field references
FieldMeta<Stock> nameField = Stock_.name;
PrimaryFieldMeta<Stock> idField = Stock_.id;

// Build queries with type safety
Query query = SQLs.query()
        .select(Stock_.id, Stock_.name)
        .from(Stock_.T)
        .where(Stock_.price.greaterThan(new BigDecimal("100")))
        .asQuery();
```

#### 7. Processor Configuration

The processor supports configuration through:

- **`TableMeta.properties`**: Resolves placeholder expressions (`${DEFAULT}`)
- **Supported Java Version**: Java 25 (`@SupportedSourceVersion(SourceVersion.RELEASE_25)`)
- **Supported Annotations**: Only `@Table` annotation (`@SupportedAnnotationTypes("io.army.annotation.Table")`)

#### 8. Composite Type Processing

The processor also generates metamodel for classes annotated with `@DefinedType` (composite types):

```java

@DefinedType(name = "PRODUCT_INFO", fieldOrder = {"name", "price"})
public class ProductInfo {
  public String name;
  public BigDecimal price;
}
```

Generates `ProductInfo_.java` with `CompositeType` metadata.

#### Processor Implementation Details

The processor follows the standard Java annotation processing lifecycle:

1. **`init(ProcessingEnvironment)`**: Initializes the processor with the processing environment
2. **`process(Set<TypeElement>, RoundEnvironment)`**: Processes `@Table` annotated elements
3. **`generateTableStaticModelClass()`**: Generates metamodel source code
4. **`writeClassFiles()`**: Writes generated source files to the output directory

The processor batches file writes (50 classes at a time) for better performance.

### Generated Static Metamodel

For each domain class annotated with `@Table`, the processor generates a corresponding static metamodel class named
`<DomainName>_`.

**Generated class structure:**

- `T`: A `SimpleTableMeta<Domain>` instance containing table metadata
- Field name constants (e.g., `ID = "id"`)
- Type-safe field metadata references (e.g., `PrimaryFieldMeta<Stock> id`)
- For generic domain classes: additional `CLASS` field and `constructor()` method

### Example: Stock Domain

**Input: `Stock.java`**

```java

@Table(name = "stock", indexes = {
        @Index(name = "${DEFAULT}", unique = true, fieldList = {"exchange", "code"})
}, comment = "股票")
public class Stock {

  @Generator(value = "io.army.generator.snowflake.Snowflake8Generator",
          params = @Param(name = "startTime", value = "1779012232202"))
  @Column
  public long id;

  @Column
  public LocalDateTime createTime;

  @Column
  public LocalDateTime updateTime;

  @Column
  public int version;

  @Column(notNull = true, updatable = false, precision = 5, comment = "交易所代码")
  public String exchange;

  @Column(notNull = true, updatable = false, precision = 15, comment = "股票代码")
  public String code;

  @Column(notNull = true, precision = 130, comment = "股票公司名称")
  public String name;

  @Column(notNull = true, precision = 10, defaultValue = "'NORMAL'", comment = "股票在市状态")
  public StockStatus status;

  @Column(defaultValue = "''", comment = "公司全称")
  public String fullName;

  @Column(defaultValue = "DATE '1970-01-01'", comment = "股票上市日期")
  public LocalDate listingDate;

  @Column(precision = 10, scale = 2, defaultValue = "0.00", comment = "股票发行价")
  public BigDecimal offerPrice;
}
```

**Generated: `Stock_.java`**

Total 11 fields.

```java

@Generated(value = "io.army.modelgen.ArmyMetaModelDomainProcessor", date = "...")
public final class Stock_ {

  public static final SimpleTableMeta<Stock> T;

  static {
    T = _TableMetaFactory.getSimpleTableMeta(Stock.class);
    final int fieldSize = T.fieldList().size();
    if (fieldSize != 11) {
      throw _TableMetaFactory.tableFiledSizeError(Stock.class, fieldSize);
    }
  }

  /*-------------------following table filed names-------------------*/

  /// {@link Stock#id } primary key
  public static final String ID = "id";

  /// {@link Stock#createTime } create time
  public static final String CREATE_TIME = "createTime";

  /// {@link Stock#updateTime } update time
  public static final String UPDATE_TIME = "updateTime";

  /// {@link Stock#version } version for optimistic lock
  public static final String VERSION = "version";

  /// {@link Stock#exchange } 交易所代码
  public static final String EXCHANGE = "exchange";

  /// {@link Stock#code } 股票代码
  public static final String CODE = "code";

  /// {@link Stock#name } 股票公司名称
  public static final String NAME = "name";

  /// {@link Stock#status } @see StockStatus
  public static final String STATUS = "status";

  /// {@link Stock#fullName } 公司全称
  public static final String FULL_NAME = "fullName";

  /// {@link Stock#listingDate } 股票上市日期
  public static final String LISTING_DATE = "listingDate";

  /// {@link Stock#offerPrice } 股票发行价
  public static final String OFFER_PRICE = "offerPrice";

  /*-------------------following table field meta-------------------*/

  /// {@link Stock#id } primary key
  public static final PrimaryFieldMeta<Stock> id = T.id();

  /// {@link Stock#createTime } create time
  public static final FieldMeta<Stock> createTime = T.field(CREATE_TIME);

  /// {@link Stock#updateTime } update time
  public static final FieldMeta<Stock> updateTime = T.field(UPDATE_TIME);

  /// {@link Stock#version } version for optimistic lock
  public static final FieldMeta<Stock> version = T.field(VERSION);

  /// {@link Stock#exchange } 交易所代码
  public static final FieldMeta<Stock> exchange = T.field(EXCHANGE);

  /// {@link Stock#code } 股票代码
  public static final FieldMeta<Stock> code = T.field(CODE);

  /// {@link Stock#name } 股票公司名称
  public static final FieldMeta<Stock> name = T.field(NAME);

  /// {@link Stock#status } @see StockStatus
  public static final FieldMeta<Stock> status = T.field(STATUS);

  /// {@link Stock#fullName } 公司全称
  public static final FieldMeta<Stock> fullName = T.field(FULL_NAME);

  /// {@link Stock#listingDate } 股票上市日期
  public static final FieldMeta<Stock> listingDate = T.field(LISTING_DATE);

  /// {@link Stock#offerPrice } 股票发行价
  public static final FieldMeta<Stock> offerPrice = T.field(OFFER_PRICE);
}
```

### Usage in Criteria API

The generated metamodel enables type-safe SQL construction:

```java
// Type-safe field reference
FieldMeta<Stock> nameField = Stock_.name;

// Type-safe table reference
SimpleTableMeta<Stock> stockTable = Stock_.T;
```

### Composite Type Metamodel

For classes annotated with `@DefinedType` (composite types), the processor generates a metamodel class named
`<TypeName>_`. Composite types support nesting - a composite type can contain fields of another composite type.

**Generated class structure:**

- `T`: A `CompositeType` instance containing composite type metadata
- `CompositeField` references for each field in the composite type

#### Example: Nested Composite Types

**Input: `ManagerInfo.java` (inner composite type)**

```java

@DefinedType(name = "MANAGER_INFO", fieldOrder = {"id"})
public class ManagerInfo implements FieldAccessPojo {

  @Column
  public Long id;
}
```

**Generated: `ManagerInfo_.java`**

```java

@Generated(value = "io.army.modelgen.ArmyMetaModelDomainProcessor", date = "...")
public final class ManagerInfo_ {

  public static final CompositeType T;

  static {
    T = CompositeType.from(ManagerInfo.class);
    final int fieldSize = T.fieldList().size();
    if (fieldSize != 1) {
      throw _TableMetaFactory.compositeFieldSizeError(ManagerInfo.class, fieldSize);
    }
  }

  /*-------------------following table field meta-------------------*/

  /// {@link ManagerInfo#id }
  public static final CompositeField id = T.field("id");
}
```

**Input: `ProductInfo.java` (outer composite type with embedded composite field)**

```java

@MappedSuperclass
@DefinedType(name = "PRODUCT_INFO",
        fieldOrder = {"productId", "productName", "price", "available", "releaseDate", "intArray", "textArray", "managerInfo"})
public class ProductInfo implements FieldAccessPojo {

  @Column
  public Long productId;

  @Column
  public String productName;

  @Column
  public BigDecimal price;

  @Column
  public Boolean available;

  @Column
  public LocalDate releaseDate;

  @Column
  public int[] intArray;

  @Column
  public String[] textArray;

  @Column
  public ManagerInfo managerInfo;
}
```

**Generated: `ProductInfo_.java`**

```java

@Generated(value = "io.army.modelgen.ArmyMetaModelDomainProcessor", date = "...")
public final class ProductInfo_ {

  public static final CompositeType T;

  static {
    T = CompositeType.from(ProductInfo.class);
    final int fieldSize = T.fieldList().size();
    if (fieldSize != 8) {
      throw _TableMetaFactory.compositeFieldSizeError(ProductInfo.class, fieldSize);
    }
  }

  /*-------------------following table field meta-------------------*/

  /// {@link ProductInfo#productId }
  public static final CompositeField productId = T.field("productId");

  /// {@link ProductInfo#productName }
  public static final CompositeField productName = T.field("productName");

  /// {@link ProductInfo#price }
  public static final CompositeField price = T.field("price");

  /// {@link ProductInfo#available }
  public static final CompositeField available = T.field("available");

  /// {@link ProductInfo#releaseDate }
  public static final CompositeField releaseDate = T.field("releaseDate");

  /// {@link ProductInfo#intArray }
  public static final CompositeField intArray = T.field("intArray");

  /// {@link ProductInfo#textArray }
  public static final CompositeField textArray = T.field("textArray");

  /// {@link ProductInfo#managerInfo }
  public static final CompositeField managerInfo = T.field("managerInfo");
}
```

**Usage:**

```java
// Access composite type metadata
CompositeType productType = ProductInfo_.T;
CompositeType managerType = ManagerInfo_.T;

// Access composite field
CompositeField priceField = ProductInfo_.price;
CompositeField managerIdField = ManagerInfo_.id;

// Nested composite field access
CompositeField managerField = ProductInfo_.managerInfo;
```

### Dependencies

- **`army-struct`**: Core type definitions and utilities
