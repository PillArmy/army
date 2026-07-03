## army-struct

The foundational module that defines core types and annotations used across the Army framework.

**Key Design Principle: Minimal Dependency**

This module is intentionally designed with **zero external dependencies** (except for `jsr305` for nullable
annotations). This allows users to:

1. **Declare enums** implementing `CodeEnum` or `LabelEnum` without pulling in heavy dependencies
2. **Declare composite types** with `@DefinedType` annotation in POJOs
3. **Use annotations** (`@NonNull`, `@Nullable`) for null safety
4. **Implement POJOs** with `FieldAccessPojo` for direct field access

These types can exist in your domain model code as pure Java interfaces and annotations, independent of the full Army
framework. When you need to use these types with the full Army ORM functionality, you simply add `army-core` as a
dependency.

### Main Types

#### io.army.struct

**Enums**

- **`CodeEnum`**: Base interface for enums mapped by numeric code. Army persists the `code()` value to the database
  instead of `ordinal()`.
- **`LabelEnum`**: Base interface for enums mapped by string label.

**Annotation**

- **`@DefinedType`**: Marks a Java class as a database user-defined type. This annotation is used to map Java classes to
  database types like PostgreSQL's composite types, domains, and ranges.

**Attributes**:

- `name`: The database type name (required)
- `category`: The type category (`COMPOSITE`, `DOMAIN`, `RANGE`, etc.), defaults to `COMPOSITE`
- `fieldOrder`: Specifies the order of fields for composite types. Required for composite types.
- `immutable`: Whether the type is immutable, defaults to `false`

**Other**

- **`TypeCategory`**: Enumeration of type categories (COMPOSITE, DOMAIN, RANGE, etc.).

#### io.army.lang

**Annotations**

- **`@NonNull`**: Marks a field, parameter, or return value as non-null.
- **`@NonNullApi`**: Makes all parameters and return values non-null by default within a package.
- **`@Nullable`**: Marks a field, parameter, or return value as nullable.

#### io.army.pojo

- **`FieldAccessPojo`**: Base interface for POJOs that provide field-level access.

### Usage Example

**CodeEnum**

```java
public enum StockStatus implements CodeEnum {
    ACTIVE(1),
    DELISTED(0);

    private final int code;

    StockStatus(int code) {
        this.code = code;
    }

    @Override
    public int code() {
        return this.code;
    }
}
```

**Composite Type with @DefinedType**

```java
@DefinedType(
    name = "PRODUCT_INFO",
    category = TypeCategory.COMPOSITE,
    fieldOrder = {"productName", "price", "available"},
    immutable = false
)
public class ProductInfo {
    public String productName;
    public BigDecimal price;
    public boolean available;
}
```

**Key Points**:

- `name`: Must match the database type name (case-sensitive for some databases)
- `fieldOrder`: Required for composite types, determines column order in database
- `category`: `COMPOSITE` for row types, `DOMAIN` for domain types, `RANGE` for range types
- `immutable`: Set to `true` if the type should be immutable (affects serialization behavior)

### Dependencies

This is a core module with no external dependencies. It provides type definitions used by `army-core`,
`army-annotation`, and other modules.