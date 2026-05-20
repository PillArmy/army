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

package io.army.spring.sync;

import io.army.lang.Nullable;
import io.army.session.*;
import io.army.transaction.TransactionOption;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

final class SpringSyncSessionContext implements SyncSessionContext {

    static SpringSyncSessionContext create(SyncSessionFactory factory) {
        return new SpringSyncSessionContext(factory);
    }

    private final SyncSessionFactory factory;

    private SpringSyncSessionContext(SyncSessionFactory factory) {
        this.factory = factory;
    }

    @Override
    public SyncSessionFactory sessionFactory() {
        return this.factory;
    }


    @Override
    public <T extends SessionFactory> T sessionFactory(Class<T> factoryClass) {
        return factoryClass.cast(this.factory);
    }

    @Override
    public SyncSession currentSession() throws NoCurrentSessionException {
        final Object session;
        session = TransactionSynchronizationManager.getResource(this.factory);
        if (!(session instanceof SyncSession)) {
            throw new NoCurrentSessionException("no current session");
        }
        return (SyncSession) session;
    }

    @Override
    public <T extends SyncSession> T currentSession(Class<T> sessionClass) throws NoCurrentSessionException {
        return sessionClass.cast(currentSession());
    }

    @Nullable
    @Override
    public SyncSession tryCurrentSession() {
        final Object session;
        session = TransactionSynchronizationManager.getResource(this.factory);
        if (session instanceof SyncSession) {
            return (SyncSession) session;
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends SyncSession> T tryCurrentSession(Class<T> sessionClass) {
        final Object session;
        session = TransactionSynchronizationManager.getResource(this.factory);
        if (session instanceof SyncSession) {
            return sessionClass.cast(session);
        }
        return null;
    }


    @Override
    public <T> T execute(@Nullable String name, boolean readOnly, Function<SyncSession, T> function) {
        final SyncSession existSession, session;
        existSession = tryCurrentSession();
        if (existSession == null) {
            session = this.factory.localSession(name, readOnly);
        } else {
            session = existSession;
        }
        final T result;
        try {
            result = function.apply(session);
        } catch (SessionException e) {
            throw SpringUtils.wrapSessionError(e);
        } finally {
            if (session != existSession) {
                session.close();
            }
        }
        return result;
    }

    @Override
    public <T> T executeNotNull(@Nullable String name, boolean readOnly, Function<SyncSession, T> function) {
        return Objects.requireNonNull(execute(name, readOnly, function), "function must not return null");
    }

    @Override
    public <T> T executeInTransaction(@Nullable String name, Supplier<TransactionOption> optionSupplier, Function<SyncSession, T> function) {

        final SyncSession existSession, session;
        existSession = tryCurrentSession();
        TransactionOption option;
        if (existSession == null) {
            option = optionSupplier.get();
            session = this.factory.localSession(name, option.isReadOnly());
        } else {
            option = null;
            session = existSession;
        }
        final boolean newTransaction = session != existSession || !session.inTransaction();
        final T result;
        try {
            if (newTransaction) {
                if (option == null) {
                    option = optionSupplier.get();
                }
                ((SyncLocalSession) session).startTransaction(option);
            }
            result = function.apply(session);
            if (newTransaction) {
                ((SyncLocalSession) session).commit();
            }
        } catch (RuntimeException e) {
            if (newTransaction) {
                ((SyncLocalSession) session).rollback();
            } else {
                session.markRollbackOnly();
            }
            if (e instanceof SessionException se) {
                throw SpringUtils.wrapSessionError(se);
            }
            throw e;
        } finally {
            if (session != existSession) {
                session.close();
            }
        }

        return result;
    }


}
