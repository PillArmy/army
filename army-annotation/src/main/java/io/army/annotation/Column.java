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


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;


/// Specifies the **database column mapping** for a field in a domain entity.
///
/// This annotation defines how a Java field maps to a database column, controlling
/// the column name, comment, nullability, insertability, updatability, precision/scale,
/// default value, and collation.
///
/// ## Placeholder Support
///
/// String and integer attributes support placeholder expressions:
/// - `${DEFAULT}` (value `-8150`) — resolve from properties or use built-in default
/// - `${RUNTIME}` (value `-2182`) — must be resolved from `TableMeta.properties` at runtime
///
/// The properties file is loaded from the classpath at:
/// `META-INF/army/TableMeta.properties`. Property keys follow the pattern:
/// `{className}.{fieldName}.Column.{property}`
///
/// ### Configuration example
/// ```properties
/// # File: META-INF/army/TableMeta.properties
///
/// # Override column precision for 'embedding' field
/// com.example.domain.Stock.embedding.Column.precision=1024
///
/// # Override column collation
/// com.example.domain.Stock.name.Column.collation=en_US.utf8
/// ```
///
/// ### Example
/// ```java
/// @Column(name = "user_name", notNull = true, precision = 50, comment = "User display name")
/// public String userName;
///
/// @Column(precision = 10, scale = 2, defaultValue = "0.00", comment = "Offer price")
/// public BigDecimal offerPrice;
/// ```
///
/// @see Table
/// @see Generator
/// @see Mapping
@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {

    /// Sentinel value indicating the attribute must be **resolved from `TableMeta.properties`**
    /// (classpath: `META-INF/army/TableMeta.properties`) at runtime. Throws `MetaException` if no property is found.
    int RUNTIME_EXP = -2182;

    /// Sentinel value indicating the attribute should use **built-in default conventions**
    /// if no property override is found in `TableMeta.properties` (classpath: `META-INF/army/TableMeta.properties`).
    ///
    /// For example, column names default to camelCase-to-snake_case conversion.
    int DEFAULT_EXP = -8150;

    /// (Optional) The **database column name**.
    ///
    /// Defaults to camelCase-to-snake_case conversion of the Java field name.
    /// Supports placeholder expressions for runtime resolution.
    ///
    /// See {@code io.army.criteria.impl.TableMetaUtils#columnName()}
    String name() default "";

    /// (Optional) The **column comment** for DDL generation.
    ///
    /// Reserved Army fields and discriminator fields do not require an explicit comment —
    /// the framework generates them automatically (e.g., `"primary key"`, `"create time"`).
    ///
    /// Supports `${DEFAULT}` (auto-generate from enum type or camelCase-to-spaced) and
    /// `${RUNTIME}` placeholders.
    String comment() default "";

    /// (Optional) Whether the column should be declared as **NOT NULL** in DDL.
    ///
    /// The following reserved fields always enforce NOT NULL regardless of this setting:
    /// - `id`, `createTime`, `updateTime`, `version`, `visible`
    /// - Discriminator field of parent table
    ///
    /// @see Table#allColumnNotNull()
    boolean notNull() default false;


    /// (Optional) Whether the column is included in SQL **INSERT** statements.
    ///
    /// Default is `true`. Reserved Army fields (`createTime`, `updateTime`, `version`, `visible`)
    /// and discriminator fields are always insertable.
    boolean insertable() default true;

    /// (Optional) Whether the column is included in SQL **UPDATE** statements.
    ///
    /// Default is `true`. Reserved fields `id` and `createTime` are never updatable.
    /// Fields on immutable tables are never updatable.
    boolean updatable() default true;

    /// (Optional) The **precision** (total digit count or length) for the column.
    ///
    /// Applies to numeric and string columns. Built-in defaults by type:
    /// - `SqlDecimal` → 16, `SqlString` → 255, `UUID` → 36, `Vector` → 1024
    ///
    /// Use `DEFAULT_EXP` to resolve from `TableMeta.properties`
    /// (classpath: `META-INF/army/TableMeta.properties`).
    int precision() default -1;

    /// (Optional) The **scale** (fractional digit count) for decimal columns.
    ///
    /// Built-in defaults: `SqlDecimal` → 3, temporal types → 6 (microseconds).
    /// Use `DEFAULT_EXP` to resolve from `TableMeta.properties`
    /// (classpath: `META-INF/army/TableMeta.properties`).
    int scale() default -1;

    /// (Optional) The **default value** SQL expression for the column.
    ///
    /// The following field types have automatic defaults and do not require this attribute:
    /// - Reserved fields: `id`, `createTime`, `updateTime`, `version`, `visible`
    /// - Discriminator fields
    /// - Numeric types (`Long`, `Integer`, `BigDecimal`, etc.) → `0`
    /// - `String` → `''`
    /// - `LocalTime` → `'00:00:00'`
    /// - Binary types (`InputStream`, `byte[]`) — no default applicable
    ///
    /// Supports `${DEFAULT}` placeholder for type-appropriate zero value generation.
    String defaultValue() default "";


    /// (Optional) The **collation** for the column (e.g., `"en_US.utf8"`).
    ///
    /// Can be overridden via `TableMeta.properties` (classpath: `META-INF/army/TableMeta.properties`) using the key
    /// `{className}.{fieldName}.Column.collation`.
    String collation() default "";



}
