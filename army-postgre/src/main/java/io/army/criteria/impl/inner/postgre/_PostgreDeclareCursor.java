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


package io.army.criteria.impl.inner.postgre;

import io.army.criteria.impl.inner._DeclareCursor;
import io.army.lang.Nullable;

/// Internal representation of a PostgreSQL DECLARE CURSOR statement.
public interface _PostgreDeclareCursor extends _DeclareCursor {


    /// Whether the cursor is binary.
    boolean isBinary();

    /// The sensitive mode (SCROLL/NO SCROLL), nullable.
    @Nullable
    Boolean sensitiveMode();

    /// The scroll mode (SCROLL/NO SCROLL), nullable.
    @Nullable
    Boolean scrollMode();

    /// The hold mode (WITH HOLD/WITHOUT HOLD), nullable.
    @Nullable
    Boolean holdMode();



}
