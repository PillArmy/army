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

/// Specifies the **type mapping** between a Java field and its database column representation.
///
/// This annotation controls how Army's mapping system converts Java types to/from
/// database types. It is especially important for non-standard mappings such as
/// MySQL `LONGTEXT`, PostgreSQL `ENUM`, set types, and binary LOB types.
///
/// ## Resolution Priority
///
/// `type()` takes precedence over `value()`. At least one must be specified
/// unless the framework can infer the mapping from the Java type automatically.
///
/// ### Example: MySQL LONGTEXT with charset
/// ```java
/// @Mapping("io.army.mapping.mysql.MySQLLongTextType", charset = "UTF-8")
/// @Column(comment = "User article content")
/// public java.nio.file.Path article;
/// ```
///
/// ### Example: MySQL SET type with elements
/// ```java
/// @Mapping("io.army.mapping.mysql.MySQLSetType", elements = DayOfWeek.class)
/// @Column(comment = "Update day of week")
/// public java.util.Set<DayOfWeek> dayOfWeek;
/// ```
///
/// ### Example: PostgreSQL ENUM with params
/// ```java
/// @Mapping(type = PostgreEnumType.class, params = {"gender_enum"})
/// @Column(comment = "Gender")
/// public Gender gender;
/// ```
///
/// @see Column
/// @see Generator
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapping {

    /// (Optional) The **fully-qualified class name** of the `MappingType` implementation.
    ///
    /// Either this or `type()` must be specified; `type()` takes precedence.
    /// For `TextMappingType` implementations representing binary data, `charset()` is also required.
    String value() default "";

    /// (Optional) The **class of the `MappingType` implementation**.
    ///
    /// Takes precedence over `value()`. Use this when the mapping type class
    /// is available at compile time.
    Class<?> type() default void.class;

    boolean fromJavaField() default false;

    /// (Optional) **Extra parameters** for the mapping type configuration.
    ///
    /// Commonly used for PostgreSQL `ENUM` types to specify the enum type name,
    /// or for collection types to specify element type parameters.
    String[] params() default {};

    /// (Optional) A **Java method reference** for constructing or creating instances
    /// of the mapped Java type.
    ///
    /// The format follows the `"ClassName::methodName"` convention:
    /// - `"com.example.YourClass::new"` — references the constructor
    ///   (resolved via `_ArmyPgRangeType#loadConstructor(Class, String, int, Class)`).
    /// - `"com.example.YourClass::forName"` — references a public static
    ///   factory method (resolved via
    ///   `_ArmyPgRangeType#loadFactoryMethod(Class, String, int, Class)`).
    ///
    /// This is consumed by `MappingType` factory methods such as:
    /// - `PgSingleRangeType#fromMethod(Class, String, String)`
    /// - `PgMultiRangeType#fromMethod(Class, String, String)`
    /// - `PostgreSingleRangeArrayType#fromMethod(Class, String, String)`
    /// - `PostgreMultiRangeArrayType#fromMethod(Class, String, String)`
    /// which pass it to
    /// `PgRangeType#createRangeFunction(Class, Class, String)`
    /// to build a `RangeFunction` for range value conversion.
    ///
    /// For non-range types, the framework dispatches via
    /// `_MappingFactory#doMap(Class, Field, Mapping)` — which reflectively
    /// invokes the appropriate factory method (`from`, `fromMethod`,
    /// `forText`, `forElements`, etc.) based on the `MappingType` subclass.
    String func() default "";

    /// (Optional) The **character set name** for text-based binary mappings.
    ///
    /// Required when `value()` is a `TextMappingType` implementation representing
    /// binary data (e.g., `InputStream`, `Path`). Ignored otherwise.
    ///
    /// ### Example
    /// ```java
    /// @Mapping("io.army.mapping.mysql.MySQLLongTextType", charset = "UTF-8")
    /// @Column(comment = "User info")
    /// public InputStream userInfo;
    /// ```
    String charset() default "";

    /// (Optional) The **element type(s)** for collection mapping types.
    ///
    /// Required when `value()` is an `ElementMappingType` implementation (e.g., MySQL `SET` type).
    /// Specifies the Java class of each collection element.
    /// Not needed for array types — the component type is obtained via reflection.
    ///
    /// ### Example
    /// ```java
    /// @Mapping("io.army.mapping.mysql.MySQLSetType", elements = DayOfWeek.class)
    /// @Column(comment = "Update day of week")
    /// public java.util.Set<DayOfWeek> dayOfWeek;
    /// ```
    Class<?>[] elements() default {};


}
