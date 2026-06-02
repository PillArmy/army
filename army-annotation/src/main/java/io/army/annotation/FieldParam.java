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

/// Binds a **field name** to a set of `@Param` values within an `@OverrideParams` declaration.
///
/// This annotation acts as a grouping element that associates generator parameter overrides
/// with a specific inherited field. It is used exclusively inside `OverrideParams#fields()`.
///
/// ### Example
/// ```java
/// @OverrideParams(fields = @FieldParam(name = "id",
///     params = @Param(name = "startTime", value = "1600000000000")))
/// ```
///
/// @see OverrideParams
/// @see Param
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldParam {

    /// (Required) The **Java field name** of the inherited field to override.
    String name();


    /// (Required) The **replacement generator parameters** for the specified field.
    ///
    /// @see Param
    Param[] params();

}
