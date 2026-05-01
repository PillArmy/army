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

package io.army.session;


import io.army.dialect.Database;
import io.army.executor.StmtExecutor;
import io.army.meta.TableMeta;
import io.army.result.ChildUpdateException;
import io.army.spec.CloseableSpec;
import io.army.transaction.TransactionInfo;

import io.army.lang.Nullable;
import java.util.Map;
import java.util.Set;

/// This interface representing database session.
/// This interface is direct base interface of following :
/// 
/// - {@link LocalSession}
/// - {@link RmSession}
/// - {@code io.army.sync.SyncSession}
/// - {@code io.army.reactive.ReactiveSession}
/// 
/// @see SessionFactory
public sealed interface Session extends CloseableSpec, SessionSpec permits LocalSession, RmSession, PackageSession {


    /// 
    /// Session identifier(non-unique, for example : database server cluster),probably is following :
    /// 
    /// - server process id
    /// - server thread id
    /// - other identifier
    /// 
    /// **NOTE**: identifier will probably be updated if reconnect.
    /// *
    /// @throws SessionException throw when session have closed.
    long sessionIdentifier() throws SessionException;

    /// **NOTE** : This method don't check whether session closed or not.
    SessionFactory sessionFactory();

    /// **NOTE** : This method don't check whether session closed or not.
    boolean isReadonlySession();

    /// @return session in transaction block.
    /// @throws SessionException throw when session have closed
    boolean inTransaction() throws SessionException;

    boolean inPseudoTransaction();

    /// Test session whether hold one  {@link TransactionInfo} instance or not, the instance is current transaction info of this session.
    /// **NOTE** :
    /// 
    /// - This method don't check whether session closed or not
    /// - This method don't invoke {@link TransactionInfo#inTransaction()} method
    /// 
    /// <pre>The implementation of this method like following
    /// <code>
    /// &#64;Override
    /// public boolean hasTransactionInfo() {
    /// return this.transactionInfo != null;
    /// }
    /// </code>
    /// </pre>
    /// @return true : session hold one  {@link TransactionInfo} instance.
    boolean hasTransactionInfo();

    /// This method is equivalent to following :
    /// <pre>
    /// <code>
    /// // session is instance of {@link Session}
    /// session.inTransaction() || session.inPseudoTransaction()
    /// </code>
    /// </pre>
    /// @throws SessionException throw when {@link #inTransaction()} throw
    boolean inAnyTransaction() throws SessionException;

    /// Test session is whether rollback only or not.
    /// How to mark {@link Session}'s rollback only status ?
    /// 
    /// - local transaction  :
    /// 
    /// - {@link #markRollbackOnly()}
    /// - throw {@link ChildUpdateException} when execute dml
    /// 
    /// 
    /// - XA transaction :
    /// 
    /// - {@link #markRollbackOnly()}
    /// - pass {@link RmSession#TM_FAIL} flag to {@link RmSession}'s end() method
    /// - throw {@link ChildUpdateException} when execute dml
    /// 
    /// 
    /// 
    /// How to clear {@link Session}'s rollback only status ?
    /// 
    /// - local transaction  :
    /// 
    /// - rollback transaction
    /// - start new transaction
    /// 
    /// 
    /// - XA transaction :
    /// 
    /// - prepare current transaction
    /// - one phase rollback transaction
    /// - start new transaction
    /// 
    /// 
    /// 
    /// **NOTE** : This method don't check session whether closed or not.
    /// @return true : session is rollback only.
    /// @see #markRollbackOnly()
    /// @see RmSession#TM_FAIL
    boolean isRollbackOnly();

    /// Mark session rollback only
    /// More info ,see {@link #isRollbackOnly()}
    /// @throws SessionException throw when session have closed.
    /// @see #isRollbackOnly()
    void markRollbackOnly();


    /// **NOTE** : This method don't check whether session closed or not.
    boolean isReadOnlyStatus();


    /// **NOTE** : This method don't check whether session closed or not.
    boolean isReactive();


    /// **NOTE** : This method don't check whether session closed or not.
    boolean isQueryInsertAllowed();

    Database serverDatabase();


    /// **NOTE** : This method don't check whether session closed or not.
    /// @throws IllegalArgumentException throw,when not found {@link TableMeta}.
    <T> TableMeta<T> tableMeta(Class<T> domainClass);


    /// @param key The key of the attribute to return
    /// @return The attribute
    @Nullable
    Object getAttribute(Object key);

    /// Set a custom attribute.
    /// @param key   The attribute name
    /// @param value The attribute value
    void setAttribute(Object key, Object value);

    /// @return all the attributes names,a unmodified set.
    Set<Object> getAttributeKeys();

    /// Remove the attribute
    /// @param key The attribute key
    /// @return the attribute value if found, null otherwise
    @Nullable
    Object removeAttribute(Object key);

    int attributeSize();

    /// @return a unmodified set.
    Set<Map.Entry<Object, Object>> attributeEntrySet();

    /// override {@link Object#toString()}
    /// @return driver info, contain :
    /// - implementation class name
    /// - {@link #name()}
    /// - {@link System#identityHashCode(Object)}
    /// 
    @Override
    String toString();


    /// This interface is base interface of following :
    /// 
    /// - {@link RmSession}
    /// - RM {@link StmtExecutor}
    /// 
    /// **NOTE** : this interface never extends any interface.
    /// @since 0.6.0
    interface XaTransactionSupportSpec {

        boolean isSupportForget();

        /// @return the sub set of {@code  #start(Xid, int, TransactionOption)} support flags(bit set).
        int startSupportFlags();

        /// @return the sub set of {@code #end(Xid, int, Function)} support flags(bit set).
        int endSupportFlags();

        /// @return the sub set of {@code #commit(Xid, int, Function)} support flags(bit set).
        int commitSupportFlags();

        /// @return the sub set of {@code #recover(int, Function)} support flags(bit set).
        int recoverSupportFlags();


        /// @throws SessionException throw when underlying database session have closed.
        boolean isSameRm(XaTransactionSupportSpec s) throws SessionException;

    }


}
