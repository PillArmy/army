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

/// This interface representing {@link StmtExecutor} factory spec .
/// This interface is base interface of following :
/// 
/// - {@code io.army.executor.SyncExecutorFactory}
/// - {@code io.army.reactive.executor.ReactiveStmtExecutorFactory}
/// 
/// The sub interface must override following methods :
/// 
/// - {@link #metaExecutor(Function)}
/// - {@link #localExecutor(String, boolean, Function)}
/// - {@link #rmExecutor(String, boolean, Function)}
/// 
/// @since 0.6.0
public interface ExecutorFactory extends CloseableSpec, OptionSpec {



/// For example:
/// 
/// - JDBC
/// - JDBD
/// - ODBC
/// 
/// @return driver spi name,The value returned typically is the name for this driver spi.
    String driverSpiName();

    /// @return JDBC always return false, JDBD always return true.
    boolean isResultItemDriverSpi();


    /// For example: io.army.jdbc or io.army.jdbd
/// @return executor vendor,The value returned typically is the package name for this vendor.
    String executorVendor();


    /// Sub interface must override this method return value type.
    Object metaExecutor(Function<Option<?>, ?> optionFunc);


    /// Sub interface must override this method return value type.
/// @param sessionName {@link io.army.session.Session}'s name.
    Object localExecutor(String sessionName, boolean readOnly, Function<Option<?>, ?> optionFunc);


    /// Sub interface must override this method return value type.
/// @param sessionName {@link io.army.session.Session}'s name.
    Object rmExecutor(String sessionName, boolean readOnly, Function<Option<?>, ?> optionFunc);

/// override {@link Object#toString()}
/// @return driver info, contain : 
/// - implementation class name
/// - session factory name
/// - {@link System#identityHashCode(Object)}
/// 
    @Override
    String toString();


}
