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

/// Marks a class as the **root entity of an inheritance hierarchy** using the
/// **single-table inheritance** strategy (one table for all subtypes).
///
/// The `value()` attribute specifies the Java field name that maps to a discriminator column.
/// The discriminator field's mapped column must be an `Enum` type implementing
/// `io.army.struct.CodeEnum`.
///
/// ## Inheritance Model
/// ```
/// @Inheritance("status")          ← parent table
/// └── User (abstract base)
///     ├── Admin   @DiscriminatorValue("ADMIN")
///     └── Member  @DiscriminatorValue("MEMBER")
/// ```
///
/// ### Example
/// ```java
/// @Table(name = "u_user", comment = "User entity")
/// @Inheritance("status")
/// public abstract class User extends BaseDomain<User> {
///     @Column(comment = "User status")
///     public UserStatus status;
/// }
///
/// @DiscriminatorValue("ADMIN")
/// public class Admin extends User { }
/// ```
///
/// @see DiscriminatorValue
/// @see MappedSuperclass
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Inheritance {


    /// (Required) The **Java field name** of the discriminator column.
    ///
    /// The referenced field's mapped column type must be an `Enum` implementing
    /// `io.army.struct.CodeEnum`. The framework uses this column to filter rows
    /// by entity subtype in queries.
    String value();


}
