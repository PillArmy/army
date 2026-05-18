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

package io.army.spring;


import io.army.session.SyncSession;
import io.army.spring.sync.ArmySyncLocalTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.support.CallbackPreferringPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class DefaultArmyTransactionTemplate implements TransactionTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultArmyTransactionTemplate.class);


    private static final TransactionDefinition DEFAULT_DEFINITION = new DefaultTransactionDefinition();

    private static final Map<Integer, TransactionDefinition> DEFINITION_MAP = Map.copyOf(createDefinitionMap());

    private static final int READ_ONLY_MASK = 0B1_0000;


    private final PlatformTransactionManager transactionManager;


    public DefaultArmyTransactionTemplate(PlatformTransactionManager transactionManager) {
        Objects.requireNonNull(transactionManager);
        this.transactionManager = transactionManager;
    }


    @Override
    public <T> T execute(Isolation isolation, boolean readOnly, TransactionCallback<T> action) throws TransactionException {
        return execute(from(isolation, readOnly), action);
    }

    @Override
    public <T> T execute(TransactionDefinition definition, TransactionCallback<T> action) throws TransactionException {
        Objects.requireNonNull(definition, "Transaction definition must not be null");

        if (this.transactionManager instanceof CallbackPreferringPlatformTransactionManager cpptm) {
            return cpptm.execute(definition, action);
        }

        final TransactionStatus status = this.transactionManager.getTransaction(definition);

        T result;
        try {
            if (this.transactionManager instanceof ArmySyncLocalTransactionManager tx && !status.isNewTransaction()) {
                final SyncSession session = tx.tryCurrentSession();
                if (session != null
                        && session.inTransaction()
                        && isolationNotMatch(session, definition)) {
                    throw isolationNotMatchError(session, definition, status);
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
    public <T> T execute(TransactionCallback<T> action) throws TransactionException {
        return execute(DEFAULT_DEFINITION, action);
    }


    @Override
    public void executeWithoutResult(Isolation isolation, boolean readOnly, Consumer<TransactionStatus> action) throws TransactionException {
        execute(from(isolation, readOnly), createCallback(action));
    }

    @Override
    public void executeWithoutResult(TransactionDefinition definition, Consumer<TransactionStatus> action) throws TransactionException {
        execute(definition, createCallback(action));
    }

    @Override
    public void executeWithoutResult(Consumer<TransactionStatus> action) throws TransactionException {
        execute(DEFAULT_DEFINITION, createCallback(action));
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


    private static IsolationNotMatchException isolationNotMatchError(SyncSession session, TransactionDefinition definition,
                                                                     TransactionStatus status) {


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
                status.getTransactionName(), session.transactionInfo().isolation().name(), isolationName,
                "reject this transaction propagation");
        return new IsolationNotMatchException(m);
    }


    private static TransactionDefinition from(Isolation isolation, boolean readOnly) {
        int value = isolation.value();
        if (isolation == Isolation.DEFAULT) {
            value = 0;
        }
        if (readOnly) {
            value |= READ_ONLY_MASK;
        }
        TransactionDefinition definition;
        definition = DEFINITION_MAP.get(value);
        if (definition == null) {
            definition = TransactionTemplate.of(isolation, readOnly);
        }
        return definition;
    }


    private static Map<Integer, TransactionDefinition> createDefinitionMap() {
        final Map<Integer, TransactionDefinition> map = new HashMap<>();

        final int mask = 0B1111;

        int value;
        TransactionDefinition oldValue;
        for (Isolation o : Isolation.values()) {
            if (o == Isolation.SERIALIZABLE || o == Isolation.READ_UNCOMMITTED) {
                continue;
            }
            value = o.value();
            if (value > mask) {
                String m = String.format("Isolation value %d is greater than %s", value, mask);
                throw new IllegalStateException(m);
            }

            if (o == Isolation.DEFAULT) {
                value = 0;
            }

            oldValue = map.putIfAbsent(value, TransactionTemplate.of(o, false));
            if (oldValue != null) {
                String m = String.format("Isolation value %d duplicate", value);
                throw new IllegalStateException(m);
            }
            oldValue = map.put(value | READ_ONLY_MASK, TransactionTemplate.of(o, true));
            if (oldValue != null) {
                String m = String.format("Isolation value %d duplicate", value);
                throw new IllegalStateException(m);
            }
        }
        return map;
    }


}
