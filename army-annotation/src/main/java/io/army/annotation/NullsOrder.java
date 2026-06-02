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

/// Defines the **NULL values ordering** within an index column.
///
/// Used within `IndexField#nulls()` to control whether NULL values appear
/// before or after non-NULL values in the index. This is primarily useful
/// for PostgreSQL, which supports explicit `NULLS FIRST` / `NULLS LAST` syntax.
///
/// @see IndexField#nulls()
public enum NullsOrder {

    /// NULL values appear **before** non-NULL values in the index.
    NULLS_FIRST,

    /// NULL values appear **after** non-NULL values in the index.
    NULLS_LAST,

    /// Use the database's **default** nulls ordering.
    ///
    /// PostgreSQL default: `NULLS LAST` for ASC, `NULLS FIRST` for DESC.
    DEFAULT
}
