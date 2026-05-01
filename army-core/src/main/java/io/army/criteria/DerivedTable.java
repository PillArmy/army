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

/// 
/// This interface representing derived table. This interface possibly is below:
/// 
/// - {@link SubQuery}
/// - {@link SubValues}
/// - the sql function that produce result set,for example JSON_TABLE()
/// - the sub INSERT/UPDATE/DELETE with RETURNING
/// 
/// @since 0.6.0
public interface DerivedTable extends TabularItem {


}
