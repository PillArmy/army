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

package io.army.criteria;

import io.army.criteria.impl.SQLs;
import io.army.mapping.MappingType;

import java.util.function.BiFunction;

import static io.army.dialect.Database.H2;
import static io.army.dialect.Database.PostgreSQL;

/// 
/// This interface representing array {@link Expression}.
/// @see <a href="https://www.postgresql.org/docs/current/arrays.html">Arrays</a>
/// @since 0.6.0
//@Deprecated
public interface ArrayExpression extends SimpleExpression, TypedExpression {



}
