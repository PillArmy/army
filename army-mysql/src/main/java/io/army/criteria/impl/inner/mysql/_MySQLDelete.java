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

package io.army.criteria.impl.inner.mysql;

import io.army.criteria.DialectStatement;
import io.army.criteria.dialect.Hint;
import io.army.criteria.impl.MySQLs;
import io.army.criteria.impl.inner._Delete;
import io.army.criteria.impl.inner._Statement;

import java.util.List;

/// Internal interface for MySQL-specific DELETE statement details.
public interface _MySQLDelete extends _Delete, DialectStatement, _Statement._WithClauseSpec {

    /// Returns the list of hints for this DELETE statement.
    List<Hint> hintList();

    /// Returns the list of MySQL-specific modifiers for this DELETE.
    List<MySQLs.Modifier> modifierList();


}
