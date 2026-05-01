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

package io.army.result;

import io.army.criteria.Selection;
import io.army.option.Option;
import io.army.session.Session;
import io.army.spec.CloseableSpec;
import io.army.spec.OptionSpec;

/// This interface representing database cursor.
/// This interface is base interface of following :
/// 
/// - {@link StmtCursor},it's produced by statement,army know {@link Selection} list
/// - {@link ProcCursor},it's produced by procedure,army don't know {@link Selection} list
/// - {@code io.army.sync.SyncCursor} blocking cursor
/// - {@code io.army.reactive.ReactiveCursor} reactive cursor
/// 
/// @see ResultStates#valueOf(Option)
/// @see <a href="https://www.postgresql.org/docs/current/sql-declare.html">PostgreSQL DECLARE</a>
/// @see <a href="https://www.postgresql.org/docs/current/sql-fetch.html">PostgreSQL FETCH</a>
/// @since 0.6.0
public interface Cursor extends CloseableSpec, OptionSpec {


    /// Get cursor name from DECLARE cursor statement or stored procedure.
    /// Postgre example 1 : my_cursor -> my_cursor
    /// Postgre example 2 : myCursor -> myCursor
    /// @see StmtCursor#safeName()
    String name();


    /// Get The {@link Session} that create this instance.
    /// @return The {@link Session} that create this instance.
    Session session();


}
