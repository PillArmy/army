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

import io.army.executor.StmtExecutor;
import io.army.option.Option;
import io.army.transaction.HandleMode;
import io.army.transaction.Isolation;
import io.army.transaction.TransactionInfo;
import io.army.transaction.TransactionOption;

import io.army.lang.Nullable;
import java.util.function.Function;

/// This interface representing local {@link SyncSession} that support database local transaction.
/// The instance of this interface is created by {@link SyncSessionFactory.LocalSessionBuilder}.
/// This interface's directly underlying api is {@link StmtExecutor}.
/// This interface representing high-level database session. This interface's underlying database session is one of
/// 
/// - {@code java.sql.Connection}
/// - other database driver spi
/// 
/// @see SyncSessionFactory
/// @since 0.6.0
public sealed interface SyncLocalSession extends SyncSession, PackageSession.PackageLocalSession permits ArmySyncLocalSession {


/// This method is equivalent to following :
/// <pre>
/// <code>
/// // session is instance of {@link SyncLocalSession}
/// session.startTransaction(TransactionOption.option(),HandleMode.ERROR_IF_EXISTS) ;
/// </code>
/// </pre>
/// @see #startTransaction(TransactionOption, HandleMode)
    TransactionInfo startTransaction();

/// This method is equivalent to following :
/// <pre>
/// <code>
/// // session is instance of {@link SyncLocalSession}
/// session.startTransaction(option,HandleMode.ERROR_IF_EXISTS) ;
/// </code>
/// </pre>
/// @see #startTransaction(TransactionOption, HandleMode)
    TransactionInfo startTransaction(TransactionOption option);

/// Start local/pseudo transaction.
/// 
/// - Local transaction is supported by database server.
/// - Pseudo transaction({@link TransactionOption#isolation()} is {@link Isolation#PSEUDO}) is supported only by army readonly session.
/// Pseudo transaction is designed for some framework in readonly transaction,for example
/// {@code org.springframework.transaction.PlatformTransactionManager}
/// 
/// 
/// **NOTE**: if option representing pseudo transaction,then this method don't access database server.
/// Army prefer to start local transaction with one sql statement or multi-statement( if driver support),because transaction starting should keep atomicity and reduce network overhead.
/// <pre>For example: {@code TransactionOption.option(Isolation.READ_COMMITTED)},MySQL database will execute following sql :
/// <code>
/// SET TRANSACTION ISOLATION LEVEL READ COMMITTED ; START TRANSACTION READ WRITE
/// </code>
/// {@code TransactionOption.option()},MySQL database will execute following sql :
/// <code>
/// SET @@transaction_isolation = @@SESSION.transaction_isolation ; SELECT @@SESSION.transaction_isolation AS txIsolationLevel ; START TRANSACTION READ WRITE
/// // SET @@transaction_isolation = @@SESSION.transaction_isolation to  guarantee isolation is session isolation
/// </code>
/// </pre>
/// <pre>For example : {@code TransactionOption.option(Isolation.READ_COMMITTED)},PostgreSQL database will execute following sql :
/// <code>
/// START TRANSACTION ISOLATION LEVEL READ COMMITTED , READ WRITE
/// </code>
/// </pre>
/// **NOTE**:
/// 
/// - {@link TransactionInfo#valueOf(Option)} with {@link Option#START_MILLIS} always non-null.
/// - {@link TransactionInfo#valueOf(Option)} with {@link Option#DEFAULT_ISOLATION} always non-null.
/// - {@link TransactionInfo#valueOf(Option)} with {@link Option#TIMEOUT_MILLIS} always same with option
/// 
/// @param option non-null,if {@link TransactionOption#isolation()} is {@link Isolation#PSEUDO},then start pseudo transaction.
/// @param mode   non-null,
/// 
/// - {@link HandleMode#ERROR_IF_EXISTS} :  if session exists transaction then throw {@link SessionException}
/// - {@link HandleMode#COMMIT_IF_EXISTS} :  if session exists transaction then commit existing transaction.
/// - {@link HandleMode#ROLLBACK_IF_EXISTS} :  if session exists transaction then rollback existing transaction.
/// 
/// @throws IllegalArgumentException                  throw when pseudo transaction {@link TransactionOption#isReadOnly()} is false.
/// @throws java.util.ConcurrentModificationException throw when concurrent control transaction
/// @throws SessionException                          throw when
/// 
/// - session have closed
/// - pseudo transaction and {@link #isReadonlySession()} is false
/// - mode is {@link HandleMode#ERROR_IF_EXISTS} and {@link #hasTransactionInfo()} is true
/// - mode is {@link HandleMode#COMMIT_IF_EXISTS} and commit failure
/// - mode is {@link HandleMode#ROLLBACK_IF_EXISTS} and rollback failure
/// 
    TransactionInfo startTransaction(TransactionOption option, HandleMode mode);

/// This method is equivalent to following :
/// <pre>
/// <code>
/// // session is instance of {@link SyncLocalSession}
/// session.commit(Option.EMPTY_FUNC) ;
/// </code>
/// </pre>
/// @see #commit(Function)
    void commit();


/// Execute COMMIT command with dialect option or clear pseudo transaction
/// The implementation of this method **perhaps** support some of following :
/// 
/// - {@link Option#CHAIN}
/// - {@link Option#RELEASE}
/// 
/// 
/// - If session exist pseudo transaction ,then this method clear pseudo transaction only and don't access database server.
/// - Else this method always execute COMMIT command with dialect option,even if no transaction,because army is high-level database driver.
/// 
/// You can use {@link #commitIfExists(Function)} instead of this method.
/// @param optionFunc non-null, dialect option function. see {@link Option#EMPTY_FUNC}
/// @return 
/// - new transaction info :  {@link Option#CHAIN} is {@link Boolean#TRUE},new transaction info contain new {@link Option#START_MILLIS} value.
/// - null : {@link Option#CHAIN} isn't {@link Boolean#TRUE}
/// 
/// @throws IllegalArgumentException throw when
/// 
/// - {@link Option#CHAIN} is {@link Boolean#TRUE} and {@link Option#RELEASE} is {@link Boolean#TRUE}
/// - {@link Option#CHAIN} isn't null and database server don't support that.
/// - {@link Option#RELEASE} isn't null and database server don't support that.
/// 
/// @throws SessionException         throw when
/// 
/// - session have closed
/// - {@link #isRollbackOnly()} is true
/// - commit failure
/// 
    @Nullable
    TransactionInfo commit(Function<Option<?>, ?> optionFunc);

/// This method is equivalent to following :
/// <pre>
/// <code>
/// // session is instance of {@link SyncLocalSession}
/// session.commitIfExists(Option.EMPTY_FUNC) ;
/// </code>
/// </pre>
/// @see #commitIfExists(Function)
    void commitIfExists();

/// This method is equivalent to following :
/// <pre>
/// <code>
/// // session is instance of {@link SyncLocalSession}
/// if(session.hasTransactionInfo()){
/// session.commit(Option.EMPTY_FUNC) ;
/// }
/// </code>
/// </pre>
/// @see #commit(Function)
    @Nullable
    TransactionInfo commitIfExists(Function<Option<?>, ?> optionFunc);

/// This method is equivalent to following :
/// <pre>
/// <code>
/// // session is instance of {@link SyncLocalSession}
/// session.rollback(Option.EMPTY_FUNC) ;
/// </code>
/// </pre>
/// @see #rollback(Function)
    void rollback();

/// Execute ROLLBACK command with dialect option or clear pseudo transaction
/// The implementation of this method **perhaps** support some of following :
/// 
/// - {@link Option#CHAIN}
/// - {@link Option#RELEASE}
/// 
/// 
/// - If session exist pseudo transaction ,then this method clear pseudo transaction only and don't access database server.
/// - Else this method always execute ROLLBACK command with dialect option,even if no transaction,because army is high-level database driver.
/// 
/// You can use {@link #rollbackIfExists(Function)} instead of this method.
/// @param optionFunc non-null, dialect option function. see {@link Option#EMPTY_FUNC}
/// @return 
/// - new transaction info :  {@link Option#CHAIN} is {@link Boolean#TRUE},new transaction info contain new {@link Option#START_MILLIS} value.
/// - null : {@link Option#CHAIN} isn't {@link Boolean#TRUE}
/// 
/// @throws IllegalArgumentException throw when
/// 
/// - {@link Option#CHAIN} is {@link Boolean#TRUE} and {@link Option#RELEASE} is {@link Boolean#TRUE}
/// - {@link Option#CHAIN} isn't null and database server don't support that.
/// - {@link Option#RELEASE} isn't null and database server don't support that.
/// 
/// @throws SessionException         throw when
/// 
/// - session have closed
/// - rollback failure
/// 
    @Nullable
    TransactionInfo rollback(Function<Option<?>, ?> optionFunc);

/// This method is equivalent to following :
/// <pre>
/// <code>
/// // session is instance of {@link SyncLocalSession}
/// session.rollbackIfExists(Option.EMPTY_FUNC) ;
/// </code>
/// </pre>
/// @see #rollbackIfExists(Function)
    void rollbackIfExists();

/// This method is equivalent to following :
/// <pre>
/// <code>
/// // session is instance of {@link SyncLocalSession}
/// if(session.hasTransactionInfo()){
/// session.rollback(Option.EMPTY_FUNC) ;
/// }
/// </code>
/// </pre>
/// @see #rollback(Function)
    @Nullable
    TransactionInfo rollbackIfExists(Function<Option<?>, ?> optionFunc);


}
