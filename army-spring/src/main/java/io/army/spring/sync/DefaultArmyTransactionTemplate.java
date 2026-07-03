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


import io.army.session.SyncSession;
import io.army.spring.IsolationNotMatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.support.CallbackPreferringPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DefaultArmyTransactionTemplate implements TransactionTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultArmyTransactionTemplate.class);

    private static final StackWalker STACK_WALKER;


    static {
        STACK_WALKER = StackWalker.getInstance(EnumSet.of(StackWalker.Option.RETAIN_CLASS_REFERENCE));
    }


    private final PlatformTransactionManager transactionManager;

    private final boolean showTransactionName;


    public DefaultArmyTransactionTemplate(PlatformTransactionManager transactionManager, boolean showTransactionName) {
        Objects.requireNonNull(transactionManager);
        this.transactionManager = transactionManager;
        this.showTransactionName = showTransactionName;
    }


    @Override
    public <T> T execute(TransactionCallback<T> action) throws TransactionException {
        return execute(defaultDefinition(), action);
    }

    @Override
    public <T> T execute(boolean readOnly, TransactionCallback<T> action) throws TransactionException {
        return execute(TransactionTemplate.of(Isolation.DEFAULT, readOnly), action);
    }

    @Override
    public <T> T execute(Isolation isolation, boolean readOnly, TransactionCallback<T> action) throws TransactionException {
        return execute(TransactionTemplate.of(isolation, readOnly), action);
    }


    @Override
    public <T> T execute(TransactionDefinition definition, TransactionCallback<T> action) throws TransactionException {
        Objects.requireNonNull(definition, "Transaction definition must not be null");

        if (this.transactionManager instanceof CallbackPreferringPlatformTransactionManager cpptm) {
            return cpptm.execute(definition, action);
        }

        if (this.showTransactionName && definition instanceof DefaultTransactionDefinition td) {
            final String caller;
            caller = STACK_WALKER
                    .walk(this::filterFrame)
                    .map(this::parseCaller)
                    .orElse(null);

            if (caller != null) {
                td.setName(caller);
            }
        }


        final TransactionStatus status = this.transactionManager.getTransaction(definition);

        T result;
        try {
            if (this.transactionManager instanceof ArmySyncLocalTransactionManager tx && !status.isNewTransaction()) {
                final SyncSession session = tx.tryCurrentSession();
                if (session != null
                        && session.inTransaction()
                        && isolationNotMatch(session, definition)) {
                    throw isolationNotMatchError(session, definition);
                }
            }

            result = action.doInTransaction(status);
        } catch (RuntimeException | Error ex) {
            // Transactional code threw application exception -> rollback
            rollbackOnException(status, ex);
            throw ex;
        } catch (Throwable ex) {
            // Transactional code threw unexpected exception -> rollback
            rollbackOnException(status, ex);
            throw new UndeclaredThrowableException(ex, "TransactionCallback threw undeclared checked exception");
        }
        this.transactionManager.commit(status);
        return result;
    }


    @Override
    public <T> T executeNoNull(boolean readOnly, TransactionCallback<T> action) throws TransactionException {
        final T result;
        result = execute(TransactionTemplate.of(Isolation.DEFAULT, readOnly), action);
        return Objects.requireNonNull(result, "TransactionCallback returned null which is not allowed");
    }

    @Override
    public <T> T executeNoNull(Isolation isolation, boolean readOnly, TransactionCallback<T> action) throws TransactionException {
        final T result;
        result = execute(TransactionTemplate.of(isolation, readOnly), action);
        return Objects.requireNonNull(result, "TransactionCallback returned null which is not allowed");
    }

    @Override
    public <T> T executeNoNull(TransactionDefinition definition, TransactionCallback<T> action) throws TransactionException {
        final T result;
        result = execute(definition, action);
        return Objects.requireNonNull(result, "TransactionCallback returned null which is not allowed");
    }

    @Override
    public void executeWithoutResult(boolean readOnly, Consumer<TransactionStatus> action) throws TransactionException {
        execute(TransactionTemplate.of(Isolation.DEFAULT, readOnly), createCallback(action));
    }

    @Override
    public void executeWithoutResult(Isolation isolation, boolean readOnly, Consumer<TransactionStatus> action) throws TransactionException {
        execute(TransactionTemplate.of(isolation, readOnly), createCallback(action));
    }

    @Override
    public void executeWithoutResult(TransactionDefinition definition, Consumer<TransactionStatus> action) throws TransactionException {
        execute(definition, createCallback(action));
    }

    @Override
    public void executeWithoutResult(Consumer<TransactionStatus> action) throws TransactionException {
        execute(defaultDefinition(), createCallback(action));
    }


    private String parseCaller(StackWalker.StackFrame frame) {
        return frame.getClassName() + '#' + frame.getMethodName() + ':' + frame.getLineNumber();
    }

    private Optional<StackWalker.StackFrame> filterFrame(Stream<StackWalker.StackFrame> frameStream) {
        return frameStream
                .filter(this::filterClassName)
                .findFirst();
    }

    private boolean filterClassName(StackWalker.StackFrame frame) {
        return !DefaultArmyTransactionTemplate.class.getName().equals(frame.getClassName());
    }

    private TransactionDefinition defaultDefinition() {
        return new DefaultTransactionDefinition();
    }


    private void rollbackOnException(TransactionStatus status, Throwable ex) throws TransactionException {

        LOG.debug("Initiating transaction rollback on application exception", ex);
        try {
            this.transactionManager.rollback(status);
        } catch (TransactionSystemException ex2) {
            LOG.error("Application exception overridden by rollback exception", ex);
            ex2.initApplicationException(ex);
            throw ex2;
        } catch (RuntimeException | Error ex2) {
            LOG.error("Application exception overridden by rollback exception", ex);
            throw ex2;
        }
    }


    private static TransactionCallback<Void> createCallback(Consumer<TransactionStatus> action) {
        return status -> {
            action.accept(status);
            return null;
        };
    }

    private static boolean isolationNotMatch(SyncSession session, TransactionDefinition definition) {

        final io.army.transaction.Isolation armyIsolation = session.transactionInfo().isolation();
        final int isolation = definition.getIsolationLevel();
        if (armyIsolation == null || isolation == TransactionDefinition.ISOLATION_DEFAULT) {
            return false;
        }

        final int armyLevel;
        if (armyIsolation == io.army.transaction.Isolation.READ_COMMITTED) {
            armyLevel = TransactionDefinition.ISOLATION_READ_COMMITTED;
        } else if (armyIsolation == io.army.transaction.Isolation.REPEATABLE_READ) {
            armyLevel = TransactionDefinition.ISOLATION_REPEATABLE_READ;
        } else if (armyIsolation == io.army.transaction.Isolation.SERIALIZABLE) {
            armyLevel = TransactionDefinition.ISOLATION_SERIALIZABLE;
        } else if (armyIsolation == io.army.transaction.Isolation.READ_UNCOMMITTED) {
            armyLevel = TransactionDefinition.ISOLATION_READ_UNCOMMITTED;
        } else {
            armyLevel = TransactionDefinition.ISOLATION_DEFAULT;
        }

        // armyLevel representing existing transaction isolation
        return armyLevel > 0 && armyLevel < isolation;
    }


    private static IsolationNotMatchException isolationNotMatchError(SyncSession session, TransactionDefinition definition) {


        final int isolationLevel = definition.getIsolationLevel();
        final String isolationName = switch (isolationLevel) {
            case TransactionDefinition.ISOLATION_DEFAULT -> Isolation.DEFAULT.name();
            case TransactionDefinition.ISOLATION_READ_UNCOMMITTED -> Isolation.READ_UNCOMMITTED.name();
            case TransactionDefinition.ISOLATION_READ_COMMITTED -> Isolation.READ_COMMITTED.name();
            case TransactionDefinition.ISOLATION_REPEATABLE_READ -> Isolation.REPEATABLE_READ.name();
            case TransactionDefinition.ISOLATION_SERIALIZABLE -> Isolation.SERIALIZABLE.name();
            default -> "UNKNOWN";
        };

        String m = String.format("Existing transaction[%s] isolation[%s] and current transaction isolation[%s] not match,%s",
                session.name(), session.transactionInfo().isolation().name(), isolationName,
                "reject this transaction propagation");
        return new IsolationNotMatchException(m);
    }




}
