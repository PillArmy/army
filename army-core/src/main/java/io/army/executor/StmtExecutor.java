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

package io.army.executor;


import io.army.option.Option;
import io.army.spec.CloseableSpec;
import io.army.spec.OptionSpec;

import java.util.function.Function;

/// This interface representing executor or {@link io.army.stmt.Stmt}.
/// This interface is base interface of following :
/// 
/// - {@code io.army.executor.SyncStmtExecutor}
/// - {@code io.army.reactive.executor.ReactiveStmtExecutor}
/// 
/// **NOTE** : This interface isn't the sub interface of {@link CloseableSpec},
/// so all implementation of methods of this interface don't check whether closed or not,
/// but {@link io.army.session.Session} need to do that.
/// @see ExecutorFactory
/// @since 0.6.0
public interface StmtExecutor extends OptionSpec, DriverSpiHolder {


    /// 
    /// Session identifier(non-unique, for example : database server cluster),probably is following :
    /// 
    /// - server process id
    /// - server thread id
    /// - other identifier
    /// 
    /// **NOTE**: identifier will probably be updated if reconnect.
    /// 
    /// @return session identifier
    /// @throws DataAccessException throw when underlying database session have closed.
    long sessionIdentifier(Function<Option<?>, ?> sessionFunc) throws DataAccessException;


    /// @return true : underlying database session in transaction block.
    /// @throws DataAccessException throw when underlying database session have closed.
    boolean inTransaction(Function<Option<?>, ?> sessionFunc) throws DataAccessException;

    boolean isSameFactory(StmtExecutor s);

    /// override {@link Object#toString()}
    /// @return driver info, contain :
    /// - implementation class name
    /// - session name
    /// - {@link System#identityHashCode(Object)}
    /// 
    @Override
    String toString();

}
