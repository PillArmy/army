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

/// Defines the **DDL (Data Definition Language) generation mode** for a table entity.
///
/// Controls whether the framework should generate `CREATE TABLE` statements during
/// schema initialization.
///
/// ## Mode Summary
///
/// | Value     | Behavior                                               |
/// |-----------|--------------------------------------------------------|
/// | `CREATE`  | Always generate `CREATE TABLE` DDL                     |
/// | `NONE`    | Never generate `CREATE TABLE` DDL                      |
/// | `DEFAULT` | Resolve from `TableMeta.properties`, fallback to `CREATE` |
///
/// Configuration file path: `META-INF/army/TableMeta.properties` on the classpath.
///
/// ### Properties override example
/// ```properties
/// # File: META-INF/army/TableMeta.properties
///
/// # Override DDL mode for Stock entity (value: CREATE, NONE, or DEFAULT)
/// com.example.domain.Stock.Table.ddlMode=NONE
/// ```
///
/// @see Table#ddlMode()
public enum DdlMode {

    /// **Never** generate `CREATE TABLE` DDL for this entity.
    ///
    /// Use this for entities that map to externally-managed or pre-existing tables.
    NONE,

    /// **Always** generate `CREATE TABLE` DDL for this entity.
    ///
    /// This is the default mode for most entities.
    CREATE,

    /// Resolve the DDL mode from `TableMeta.properties` (located at `META-INF/army/TableMeta.properties`
    /// on the classpath) using the key `{className}.Table.ddlMode`.
    ///
    /// If no property is found, falls back to `CREATE`.
    DEFAULT
}
