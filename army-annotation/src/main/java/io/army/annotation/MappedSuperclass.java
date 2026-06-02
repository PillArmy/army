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

/// Marks a class as a **mapped superclass** whose field mappings are inherited by entity subclasses,
/// but which does not itself correspond to a database table.
///
/// This is the foundation of Army's domain model inheritance. Classes like `BaseDomain<T>` use
/// this annotation to share common fields (`createTime`, `updateTime`, `version`, `visible`)
/// across all entity types without creating their own table.
///
/// Unlike `@Inheritance`, a `@MappedSuperclass` does **not** require a discriminator column
/// and cannot be queried directly. It only serves as a field-mapping template.
///
/// ### Example
/// ```java
/// @MappedSuperclass
/// public abstract class BaseDomain<T> extends MinBaseDomain<T> {
///     @Column(comment = "update time")
///     public LocalDateTime updateTime;
///
///     @Column(comment = "version", defaultValue = "0")
///     public Integer version;
///
///     @Column(comment = "visible", defaultValue = "1")
///     public Boolean visible;
/// }
/// ```
///
/// @since 0.6.0
/// @see Inheritance
/// @see Table
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MappedSuperclass {

}
