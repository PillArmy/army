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

/// A **name-value pair** used to configure a field generator's initialization parameters.
///
/// This annotation is used within `Generator#params()` and `FieldParam#params()` to
/// pass configuration values (such as `startTime` for Snowflake generators) to the
/// generator implementation.
///
/// ### Example
/// ```java
/// @Generator(value = "io.army.generator.snowflake.Snowflake8Generator",
///     params = @Param(name = Snowflake8Generator.START_TIME, value = "1779012232202"))
/// @Column
/// public long id;
/// ```
///
/// @see Generator
/// @see FieldParam
/// @see OverrideParams
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Param {

    /// (Required) The **parameter name** recognized by the generator implementation.
    String name();

    /// (Required) The **parameter value** as a string.
    ///
    /// The generator implementation is responsible for parsing this string
    /// into the appropriate type (e.g., `long` for `startTime`).
    String value();
}
