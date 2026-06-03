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

import io.army.criteria.SQLToken;
import io.army.lang.Nullable;

import java.util.List;

/// Internal interface for MySQL index hint specification.
public interface _IndexHint {

    /// Returns the index hint command type (USE/IGNORE/FORCE).
    SQLToken command();

    /// Returns the optional index hint purpose (JOIN/ORDER BY/GROUP BY).
    @Nullable
    SQLToken purpose();

    /// Returns the list of index names for this hint.
    List<String> indexNameList();


}
