/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.army.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/// Defines a **database index** on the table mapped by the enclosing `@Table` annotation.
///
/// Index names support placeholder expressions (`${DEFAULT}`, `${DEFAULT_VALUE}`, `${RUNTIME}`, `${OPTIONAL}`)
/// for flexible, environment-aware name resolution.
///
/// ## Name Conventions
///
/// | Placeholder        | Generated Name Pattern                |
/// |--------------------|---------------------------------------|
/// | `${DEFAULT_VALUE}` | `uni_{table}_{col}` or `idx_{table}_{col}` |
/// | `${DEFAULT}`       | Same as above, with properties override    |
/// | Literal string     | Used as-is                           |
///
/// ### Example
/// ```java
/// @Table(name = "stock",
///     indexes = {
///         @Index(name = "${DEFAULT}", unique = true, fieldList = {"exchange", "code"}),
///         @Index(name = "${DEFAULT_VALUE}", type = "gin", fieldList = "metadata")
///     })
/// public class Stock { ... }
/// ```
///
/// @since 0.6.0
/// @see Table#indexes()
/// @see IndexField
@Target({})
@Retention(RUNTIME)
public @interface Index {

    /// (Required) The **name of the index**.
    ///
    /// Supports placeholder expressions for automatic or runtime-resolved naming.
    String name();

    /// (Optional) **Simple field name list** for the index columns.
    ///
    /// Field names refer to Java field names and are resolved to column names.
    /// Use this for simple indexes without per-column configuration.
    /// For detailed per-column settings (collation, opclass, sort order), use `fields()` instead.
    ///
    /// @see #fields()
    String[] fieldList() default {};

    /// (Optional) **Detailed index field definitions** with per-column configuration.
    ///
    /// Takes precedence over `fieldList()` when both are specified.
    /// Each `@IndexField` can configure collation, opclass, sort order, and nulls order.
    ///
    /// @see IndexField
    IndexField[] fields() default {};

    /// (Optional) Whether the index enforces **uniqueness**.
    ///
    /// The primary key index is always unique and is generated automatically.
    boolean unique() default false;

    /// (Optional) The **index access method type** (database-specific).
    ///
    /// | Database   | Supported Types                                  |
    /// |------------|--------------------------------------------------|
    /// | MySQL      | `BTREE`, `HASH`, `FULLTEXT`, `SPATIAL`           |
    /// | PostgreSQL | `btree`, `hash`, `gist`, `spgist`, `gin`, `brin` |
    ///
    /// Supports `${DEFAULT}`, `${RUNTIME}`, and `${OPTIONAL}` placeholders.
    /// An empty string (default) uses the database's default index type.
    String type() default "";

}
