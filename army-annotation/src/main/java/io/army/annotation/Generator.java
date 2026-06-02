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

/// Specifies a **field value generator** for the annotated property.
///
/// Generators produce values before (`PRECEDE`) or after (`POST`) an INSERT statement.
/// The most common use case is generating unique primary key values using Snowflake ID generators.
///
/// ## Generator Types
///
/// | Type       | Description                                               |
/// |------------|-----------------------------------------------------------|
/// | `PRECEDE`  | Application-side generator, runs before INSERT            |
/// | `POST`     | Database auto-increment, no application-side generation   |
/// | `RUNTIME`  | Strategy resolved from `TableMeta.properties` at runtime  |
/// | `DEFAULT`  | Framework decides based on context                        |
///
/// Configuration file path: `META-INF/army/TableMeta.properties` on the classpath.
/// Property key pattern: `{className}.{fieldName}.Generator.type`
///
/// ### Properties override example
/// ```properties
/// # File: META-INF/army/TableMeta.properties
///
/// # Override generator strategy and startTime for 'id' field
/// com.example.domain.Stock.id.GeneratorStrategy=io.army.generator.Snowflake8GeneratorStrategy:{"startTime":1779111192831}
/// ```
///
/// ### Example: Snowflake ID generator
/// ```java
/// @Generator(value = "io.army.generator.snowflake.Snowflake8Generator",
///            params = @Param(name = Snowflake8Generator.START_TIME, value = "1779012232202"))
/// @Column
/// public long id;
/// ```
///
/// @since 0.6.0
/// @see Column
/// @see GeneratorType
/// @see Param
/// @see OverrideParams
@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Generator {

    /// (Optional) The **generator type** controlling when and how the value is generated.
    ///
    /// Default is `GeneratorType.PRECEDE` (application-side generation before INSERT).
    ///
    /// @see GeneratorType
    GeneratorType type() default GeneratorType.PRECEDE;

    /// (Optional) The **fully-qualified class name** of the `FieldGenerator` implementation.
    ///
    /// Required for `PRECEDE` type generators. Ignored for `POST` type.
    /// The specified class must implement `io.army.generator.FieldGenerator`.
    String value() default "";

    /// (Optional) **Configuration parameters** passed to the generator during initialization.
    ///
    /// Common parameters include `startTime` for Snowflake generators.
    ///
    /// @see Param
    Param[] params() default {};
}
