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

import io.army.criteria.Selection;
import io.army.criteria.impl.SQLs;
import io.army.criteria.impl.inner._Cte;
import io.army.criteria.impl.inner._SelfDescribed;
import io.army.lang.Nullable;

/// Internal representation of a PostgreSQL CTE (Common Table Expression) with search/cycle clauses.
public interface _PostgreCte extends _Cte {

    /// The materialization modifier (MATERIALIZED, NOT MATERIALIZED), nullable.
    @Nullable
    SQLs.WordMaterialized modifier();

    /// The SEARCH clause, nullable.
    @Nullable
    _SearchClause searchClause();

    /// The CYCLE clause.
    _CycleClause cycleClause();


    /// SEARCH clause for depth-first or breadth-first ordering.
    interface _SearchClause extends _SelfDescribed {

        /// The search sequence column selection.
        Selection searchSeqSelection();


    }

    /// CYCLE clause for cycle detection.
    interface _CycleClause extends _SelfDescribed {

        /// The cycle mark column selection.
        Selection cycleMarkSelection();


        /// The cycle path column selection.
        Selection cyclePathSelection();

    }



}
