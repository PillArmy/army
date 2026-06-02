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

/// Specifies the **discriminator value** for an entity subtype in a single-table inheritance hierarchy.
///
/// This annotation is placed on child classes of an `@Inheritance`-annotated parent.
/// The value must match one of the enum constants defined in the parent's discriminator field type.
///
/// ### Example
/// ```java
/// // Parent class
/// @Inheritance("status")
/// public abstract class User extends BaseDomain<User> {
///     public UserStatus status;
/// }
///
/// // Child class
/// @DiscriminatorValue("ADMIN")
/// public class Admin extends User { }
/// ```
///
/// @since 0.6.0
/// @see Inheritance
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DiscriminatorValue {

    /// (Required) The **discriminator enum constant name** that identifies this entity subtype.
    ///
    /// Must match a value of the `CodeEnum` type used by the parent's discriminator field.
    String value();

}
