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

package io.army.generator;

import io.army.annotation.GeneratorType;

import java.util.Map;

/// To avoid hard-coding, use GeneratorType.RUNTIME and configure in
/// classpath:META-INF/army/generator_strategy.properties:
/// entity_class_name.field_name=generator strategy class full qualified name
///
/// @see io.army.annotation.GeneratorType#RUNTIME
public interface GeneratorStrategy {

    /// @return {@link GeneratorType#PRECEDE} or {@link GeneratorType#POST}
    GeneratorType type();

    /// If {@link #type()} is {@link GeneratorType#POST},don't invoke this method
    Class<?> generatorClass();

    /// If {@link #type()} is {@link GeneratorType#POST},don't invoke this method
    Map<String, String> paramMap();

}
