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


import java.lang.annotation.*;

/// Specifies the **database table mapping** for the annotated domain entity class.
///
/// This annotation defines how a Java class maps to a database table, including
/// the table name, schema, indexes, DDL generation mode, and immutability constraints.
///
/// ## Placeholder Support
///
/// Most string attributes support placeholder expressions (`${DEFAULT}`, `${RUNTIME}`, `${OPTIONAL}`)
/// that are resolved at runtime from `TableMeta.properties`. See `TableMetaUtils` for details.
///
/// ## TableMeta.properties
///
/// The properties file is loaded from the classpath at:
/// `META-INF/army/TableMeta.properties`. The framework scans all JARs and directories
/// on the classpath and merges entries. Property keys follow the pattern:
/// `{className}.{fieldName|Table}.{Attribute}.{property}`
///
/// ### Configuration example
/// ```properties
/// # File: META-INF/army/TableMeta.properties
///
/// # Override generator strategy for the 'id' field
/// com.example.domain.Stock.id.GeneratorStrategy=io.army.generator.Snowflake8GeneratorStrategy:{"startTime":1779111192831}
///
/// # Override column precision
/// com.example.domain.Stock.embedding.Column.precision=1024
///
/// # Override mapping type
/// com.example.domain.Stock.id.Mapping.value=io.army.mapping.SqlBigIntType
/// ```
///
/// ### Example
/// ```java
/// @Table(name = "stock_quotes",
///        indexes = @Index(name = "${DEFAULT}", unique = true, fieldList = {"stockId", "date"}),
///        comment = "Daily stock quotes data")
/// public class StockQuotes extends StockBaseDomain<StockQuotes> {
///     @Generator(value = SNOWFLAKE8, params = @Param(name = "startTime", value = "1779111192831"))
///     @Column
///     public long id;
/// }
/// ```
///
/// @since Army 1.0
/// @see Column
/// @see Index
/// @see MappedSuperclass
/// @see DdlMode
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@MappedSuperclass
@Documented
public @interface Table {

    /// (Required) The **database table name**.
    ///
    /// Supports `${DEFAULT}` (auto-derive from class simple name) and
    /// `${RUNTIME}` (must be provided in `TableMeta.properties` — classpath: `META-INF/army/TableMeta.properties`) placeholders.
    /// The name must not be in camelCase format.
    ///
    /// See {@code io.army.meta.TableMeta#tableName()}
    String name();

    /// (Required) The **table comment** used for DDL generation and database documentation.
    ///
    /// Supports `${DEFAULT}` (auto-derive from class simple name) and `${RUNTIME}` placeholders.
    String comment();

    /// (Optional) The **catalog** of the table.
    ///
    /// Defaults to the database's default catalog. Supports `${DEFAULT}` and `${RUNTIME}`
    /// placeholders resolved via `{className}.Table.catalog` property key.
    String catalog() default "";

    /// (Optional) The **schema** of the table.
    ///
    /// Defaults to the database's default schema for the user. Supports `${DEFAULT}` and
    /// `${RUNTIME}` placeholders resolved via `{className}.Table.schema` property key.
    String schema() default "";

    /// (Optional) **Indexes** for the table, used during DDL generation.
    ///
    /// A primary key index on the `id` field is created automatically if none is declared.
    /// Each `@Index` supports placeholder-based name resolution and type configuration.
    ///
    /// @see Index
    Index[] indexes() default {};


    /// Specifies whether the table is **immutable** (read-only).
    ///
    /// When `true`, the framework disallows `UPDATE` DML operations on the table.
    /// Parent tables with `@Inheritance` cannot be marked as immutable.
    ///
    /// Default value is `false`.
    boolean immutable() default false;

    /// Specifies whether **all columns** in the table should be declared as `NOT NULL`
    /// during DDL generation.
    ///
    /// Reserved fields (`id`, `createTime`, `updateTime`, `version`, `visible`)
    /// and discriminator fields always enforce `NOT NULL` regardless of this setting.
    ///
    /// Default value is `false`.
    ///
    /// @see Column#notNull()
    boolean allColumnNotNull() default false;

    /// Specifies the **DDL generation mode** for this table.
    ///
    /// | Mode      | Behavior                                      |
    /// |-----------|-----------------------------------------------|
    /// | `CREATE`  | Always generate CREATE TABLE DDL              |
    /// | `NONE`    | Never generate CREATE TABLE DDL               |
    /// | `DEFAULT` | Resolve from `TableMeta.properties` (classpath: `META-INF/army/TableMeta.properties`) or default to `CREATE` |
    ///
    /// @see DdlMode
    DdlMode ddlMode() default DdlMode.CREATE;


    /// (Optional) Additional **table options** appended to the `CREATE TABLE` DDL statement.
    ///
    /// For example, MySQL engine and charset options: `"ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"`.
    String tableOptions() default "";

    /// (Optional) **Partition options** appended to the `CREATE TABLE` DDL statement.
    ///
    /// Used for database-specific partitioning clauses, e.g.,
    /// `"PARTITION BY RANGE (YEAR(create_time))"`.
    String partitionOptions() default "";
}
