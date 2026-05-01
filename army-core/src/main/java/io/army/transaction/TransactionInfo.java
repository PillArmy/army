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

package io.army.transaction;


import io.army.executor.StmtExecutor;
import io.army.option.Option;
import io.army.session.RmSession;
import io.army.session.Session;

import io.army.lang.NonNull;
import io.army.lang.Nullable;

/// This interface representing the transaction info of session.
/// The developer of {@link StmtExecutor} can create the instance of this interface by following :
/// 
/// - {@link #notInTransaction(Isolation, boolean)}
/// - {@link #builder(boolean, Isolation, boolean)}
/// 
/// @since 0.6.0
public interface TransactionInfo extends TransactionSpec {


    /// {@link io.army.session.Session}'s transaction isolation level.
    /// 
    /// - {@link #inTransaction()} is true : the isolation representing current transaction isolation
    /// - Session level transaction isolation
    /// 
    /// @return non-null
    @NonNull
    Isolation isolation();

    /// Session whether in transaction block or not.
    /// **NOTE** : for XA transaction {@link XaStates#PREPARED} always return false.
    /// @return true : {@link Session} in transaction block.
    boolean inTransaction();

    /// @return true when
    /// 
    /// - {@link #inTransaction()} is true
    /// - database server demand client rollback(eg: PostgreSQL) or {@link Session#isRollbackOnly()}
    /// 
    boolean isRollbackOnly();

    /// 
    /// Application developer can get
    /// 
    /// - {@link XaStates} with {@link Option#XA_STATES}
    /// - {@link Xid} with {@link Option#XID}
    /// - {@code flag} of last phase with {@link Option#XA_FLAGS}
    /// 
    /// when this instance is returned by {@link RmSession}.
    /// 
    @Override
    <T> T valueOf(Option<T> option);


    static InfoBuilder builder(boolean inTransaction, Isolation isolation, boolean readOnly) {
        return ArmyTransactionInfo.infoBuilder(inTransaction, isolation, readOnly);
    }


    /// Get a {@link TransactionInfo} instance that {@link TransactionInfo#inTransaction()} is false
    /// and option is empty.
    static TransactionInfo notInTransaction(Isolation isolation, boolean readOnly) {
        return ArmyTransactionInfo.noInTransaction(isolation, readOnly);
    }

    static TransactionInfo pseudoLocal(final TransactionOption option) {
        return ArmyTransactionInfo.pseudoLocal(option);
    }

    /// Create pseudo transaction info for XA transaction start method.
    static TransactionInfo pseudoStart(final Xid xid, final int flags, final TransactionOption option) {
        return ArmyTransactionInfo.pseudoStart(xid, flags, option);
    }


    /// Create pseudo transaction info for XA transaction end method.
    static TransactionInfo pseudoEnd(final TransactionInfo info, final int flags) {
        return ArmyTransactionInfo.pseudoEnd(info, flags);
    }

    /// @throws IllegalArgumentException throw when
    /// 
    /// - info is unknown implementation
    /// - info's {@link TransactionInfo#inTransaction()} is false and info's {@link TransactionInfo#isolation()} isn't {@link Isolation#PSEUDO}
    /// 
    static TransactionInfo forRollbackOnly(TransactionInfo info) {
        return ArmyTransactionInfo.forRollbackOnly(info);
    }

    /// @throws IllegalArgumentException throw when
    /// 
    /// - info is unknown implementation
    /// - info's {@link TransactionInfo#inTransaction()} is false
    /// 
    static TransactionInfo forChain(TransactionInfo info) {
        return ArmyTransactionInfo.forChain(info);
    }

    static TransactionInfo forXaEnd(int flags, TransactionInfo info) {
        return ArmyTransactionInfo.forXaEnd(flags, info);
    }

    static TransactionInfo forXaJoinEnded(int flags, TransactionInfo info) {
        return ArmyTransactionInfo.forXaJoinEnded(flags, info);
    }

    interface InfoBuilder {

        <T> InfoBuilder option(Option<T> option, @Nullable T value);

        /// @throws IllegalArgumentException throw when not in transaction.
        InfoBuilder option(TransactionOption option);

        /// @throws IllegalArgumentException throw when not in transaction.
        InfoBuilder option(Xid xid, int flags, XaStates xaStates, TransactionOption option);


        /// Create a new {@link TransactionInfo} instance.
        /// **NOTE**: if satisfy following :
        /// 
        /// - in transaction is true
        /// - not found {@link Option#START_MILLIS}
        /// 
        /// then this method always auto add {@link Option#START_MILLIS}.
        /// @throws IllegalStateException throw when in transaction and not found {@link Option#DEFAULT_ISOLATION}.
        TransactionInfo build();

    }


}
