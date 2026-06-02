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

/// Overrides the `@Generator` configuration parameters of fields **inherited from a superclass**.
///
/// This class-level annotation allows a subclass to customize the generator parameters
/// of inherited fields without modifying the parent class. The most common use case is
/// providing a different `startTime` for Snowflake ID generators in child entities.
///
/// ### Example
/// ```java
/// @OverrideParams(fields = {
///     @FieldParam(name = "id", params = @Param(name = "startTime", value = "1600000000000")),
///     @FieldParam(name = "uid", params = @Param(name = "startTime", value = "1600000000000"))
/// })
/// public class ChildEntity extends BaseEntity { }
/// ```
///
/// @see Generator
/// @see FieldParam
/// @see Param
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OverrideParams {

    /// (Required) The **field-level parameter overrides**.
    ///
    /// Each `@FieldParam` specifies a field name and its replacement generator parameters.
    FieldParam[] fields();
}
