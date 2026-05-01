package io.army.transaction;

import io.army.spec.IterableOptionSpec;

/// Package interface
/// This interface is base interface of following :
/// 
/// - {@link TransactionOption}
/// - {@link TransactionInfo}
/// 
interface TransactionSpec extends IterableOptionSpec {



    /// @return true : transaction is read-only.
    boolean isReadOnly();

/// 
/// override {@link Object#toString()}
/// 
/// 
/// @return transaction info, contain
/// 
/// - implementation class name
/// - transaction info
/// - {@link System#identityHashCode(Object)}
/// 
    @Override
    String toString();


}
