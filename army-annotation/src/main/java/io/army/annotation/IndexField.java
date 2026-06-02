package io.army.annotation;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/// Defines a **single column within an index** with optional collation, opclass, sort order, and nulls order.
///
/// This annotation is used within `Index#fields()` for per-column index configuration,
/// particularly useful for PostgreSQL-specific features like `vector_ops` opclass in HNSW indexes.
///
/// ### Example
/// ```java
/// @Index(name = "${DEFAULT}", unique = true, type = "btree",
///     fields = {
///         @IndexField(name = "exchange"),
///         @IndexField(name = "code", collation = "en_US.utf8")
///     })
/// ```
///
/// ### PostgreSQL vector_ops example (HNSW)
/// ```java
/// @Index(name = "${DEFAULT}", type = "hnsw",
///     fields = @IndexField(name = "embedding", opclass = "vector_cosine_ops"))
/// ```
///
/// @see Index#fields()
/// @since Army 1.0
@Target({})
@Retention(RUNTIME)
public @interface IndexField {

    /// (Required) The **Java field name** corresponding to the indexed column.
    String name();

    /// (Optional) The **column collation** for this index field (e.g., `"en_US.utf8"`).
    ///
    /// Supports `${DEFAULT}` and `${OPTIONAL}` placeholders.
    /// PostgreSQL only — ignored on MySQL.
    String collation() default "";

    /// (Optional) The **operator class** for this index field (PostgreSQL only).
    ///
    /// Common values: `"vector_cosine_ops"` (for embedding vector indexes),
    /// `"jsonb_path_ops"` (for JSONB indexes). Supports `${DEFAULT}` and `${OPTIONAL}` placeholders.
    String opclass() default "";

    /// (Optional) The **sort order** for this index column.
    ///
    /// @see SortOrder
    SortOrder order() default SortOrder.DEFAULT;

    /// (Optional) The **nulls ordering** within this index column.
    ///
    /// @see NullsOrder
    NullsOrder nulls() default NullsOrder.DEFAULT;

}
