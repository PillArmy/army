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

import io.army.criteria.SQLToken;
import io.army.criteria.SubStatement;
import io.army.criteria.impl.inner._Expression;
import io.army.lang.Nullable;

import java.util.List;

/// Internal representation of a PostgreSQL sub-statement with CTE search/cycle options.
public interface _PostgreCteStatement extends SubStatement {

    /// The materialization option, nullable.
    @Nullable
    SQLToken materializedOption();

    /// The wrapped sub-statement.
    SubStatement subStatement();


    /// Combined SEARCH and CYCLE specification for a CTE sub-statement.
    interface _SearchOptionClauseSpec extends _PostgreCteStatement {

        /// The search option (BREADTH FIRST or DEPTH FIRST), nullable.
        @Nullable
        SQLToken searchOption();

        /// The list of columns for FIRST BY.
        List<String> firstByList();

        /// The search sequence column name.
        String searchSeqColumnName();

        /// The list of cycle columns, nullable.
        @Nullable
        List<String> cycleColumnList();

        /// The cycle mark column name.
        String cycleMarkColumnName();

        /// The cycle mark value expression, nullable.
        @Nullable
        _Expression cycleMarkValue();

        /// The cycle mark default expression, nullable.
        @Nullable
        _Expression cycleMarkDefault();

        /// The cycle path column name.
        String cyclePathColumnName();

    }



}
