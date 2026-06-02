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

/// Defines the **strategy types** for field value generators.
///
/// Controls when and how a generator produces values relative to the INSERT lifecycle.
///
/// ## Type Summary
///
/// | Value     | Description                                                            |
/// |-----------|------------------------------------------------------------------------|
/// | `PRECEDE` | Application-side generation before INSERT (e.g., Snowflake ID)         |
/// | `POST`    | Database auto-increment; no application-side generation needed         |
/// | `RUNTIME` | Strategy resolved from `TableMeta.properties` at runtime               |
/// | `DEFAULT` | Framework decides based on context (e.g., parent table inheritance)    |
///
/// Configuration file path: `META-INF/army/TableMeta.properties` on the classpath.
///
/// ### Properties override example
/// ```properties
/// # File: META-INF/army/TableMeta.properties
///
/// # Override generator type for 'id' field (value: PRECEDE, POST, or DEFAULT)
/// com.example.domain.Stock.id.Generator.type=POST
/// ```
///
/// @see Generator#type()
public enum GeneratorType {

    /// Application-side generator that produces a value **before** the INSERT statement.
    ///
    /// The generator must implement `io.army.generator.FieldGenerator`.
    /// This is the default type.
    PRECEDE,

    /// Database auto-increment column — **no** application-side generation.
    ///
    /// The database assigns the value during INSERT. After INSERT, the framework
    /// reads the generated key back into the entity.
    POST,

    /// Strategy resolved from `TableMeta.properties` (located at `META-INF/army/TableMeta.properties`
    /// on the classpath) using the key `{className}.{fieldName}.Generator.type`.
    ///
    /// The property value must be one of `PRECEDE`, `POST`, or `DEFAULT`.
    ///
    /// See {@code io.army.meta.GeneratorStrategy}
    RUNTIME,

    /// Framework-decided strategy based on the entity context.
    ///
    /// For child entities inheriting from a parent with `@Inheritance`, defaults to `POST`.
    /// Otherwise, behaves like `PRECEDE`.
    DEFAULT;


    @Override
    public final String toString() {
        return String.format("%s.%s", GeneratorType.class.getName(), this.name());
    }

}
