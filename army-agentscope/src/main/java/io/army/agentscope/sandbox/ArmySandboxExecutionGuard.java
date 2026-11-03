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

package io.army.agentscope.sandbox;

import io.agentscope.harness.agent.sandbox.SandboxExecutionGuard;
import io.agentscope.harness.agent.sandbox.SandboxIsolationKey;
import io.agentscope.harness.agent.sandbox.SandboxLease;
import io.army.criteria.Expression;
import io.army.criteria.Select;
import io.army.criteria.impl.MySQLs;
import io.army.criteria.impl.Postgres;
import io.army.criteria.impl.SQLs;
import io.army.dialect.Database;
import io.army.session.SyncSession;
import io.army.session.SyncSessionFactory;
import org.apache.commons.codec.digest.MurmurHash3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Supplier;

/// Army-backed {@link SandboxExecutionGuard}.
///
/// <p>Uses a dedicated table ({@code army_agentscope_sandbox_lock}) with a unique index on
/// {@code lockKey} to provide insert-if-absent CAS semantics. PostgreSQL uses
/// {@code ON CONFLICT DO NOTHING}; MySQL catches the duplicate-key exception. The lease
/// simply deletes the row on close.</p>
///
/// Only PostgreSQL and MySQL are supported.
public final class ArmySandboxExecutionGuard implements SandboxExecutionGuard {

    private static final Logger LOG = LoggerFactory.getLogger(ArmySandboxExecutionGuard.class);

    private final SyncSessionFactory sessionFactory;

    private final String keyPrefix;

    private final int lockTimeoutSeconds;


    private ArmySandboxExecutionGuard(final Builder builder) {
        this.sessionFactory = builder.sessionFactory;
        this.keyPrefix = builder.keyPrefix;
        this.lockTimeoutSeconds = builder.lockTimeoutSeconds;
    }


    @Override
    public SandboxLease tryEnter(final SandboxIsolationKey key) throws InterruptedException {

        final Database database = this.sessionFactory.dialectDatabase();

        final Object lockKey;
        lockKey = lockKey(database, key);

        final String sessionName = sessionName(key, database, lockKey);

        final long deadline;
        deadline = System.nanoTime() + Duration.ofSeconds(this.lockTimeoutSeconds).toNanos();

        final SyncSession session = this.sessionFactory.localSession(sessionName, true);

        try {
            Boolean locked;
            while (true) {
                final Select stmt;
                stmt = SQLs.query()
                        .select(lockExpression(database, lockKey).as("locked"))
                        .asQuery();

                locked = session.queryOne(stmt, Boolean.class);
                if (locked != null && locked) {
                    final Supplier<Select> supplier;
                    supplier = () -> SQLs.query()
                            .select(unlockExpression(database, lockKey).as("unlocked"))
                            .asQuery();

                    return new ArmyLease(session, supplier);
                }

                if (System.nanoTime() >= deadline) {
                    throw tryTimeoutInterrupt(sessionName);
                }

                Thread.onSpinWait(); // Spin wait

            } // loop

        } catch (InterruptedException e) {
            session.close();
            throw e;
        } catch (Throwable e) {
            session.close();
            String m = String.format("Failed to acquire lock: %s", sessionName);
            throw new RuntimeException(m, e);
        }
    }


    private Object lockKey(Database database, SandboxIsolationKey key) {
        return switch (database) {
            case PostgreSQL -> composeLockKeyHash(key);
            case MySQL -> composeLockName(key);
            default -> throw new UnsupportedOperationException(String.format("Unsupported database: %s", database));
        };
    }

    private Expression lockExpression(Database database, Object lockKey) {
        return switch (database) {
            case PostgreSQL -> Postgres.pgTryAdvisoryLock(lockKey);
            case MySQL -> MySQLs.getLock(lockKey, this.lockTimeoutSeconds);
            default -> throw new UnsupportedOperationException(String.format("Unsupported database: %s", database));
        };
    }

    private Expression unlockExpression(Database database, Object lockKey) {
        return switch (database) {
            case PostgreSQL -> Postgres.pgTryAdvisoryUnlock(lockKey);
            case MySQL -> MySQLs.releaseLock(lockKey);
            default -> throw new UnsupportedOperationException(String.format("Unsupported database: %s", database));
        };
    }


    private String sessionName(SandboxIsolationKey key, Database database, Object lockName) {
        final String lockFunc;
        switch (database) {
            case PostgreSQL:
                lockFunc = "advisory";
                break;
            case MySQL:
            default:
                lockFunc = "GET_LOCK";

        }
        return String.format("[%s %s lock : %s]%s:%s",
                this.sessionFactory.dialectDatabase().name(), lockFunc, lockName, key.getScope(), key.getValue());
    }


    private long composeLockKeyHash(SandboxIsolationKey key) {
        String raw = keyPrefix + key.getScope().name().toLowerCase() + ":" + key.getValue();
        long[] hash = MurmurHash3.hash128(raw.getBytes(StandardCharsets.UTF_8));
        return hash[0];
    }

    private String composeLockName(SandboxIsolationKey key) {
        String raw = keyPrefix + key.getScope().name().toLowerCase() + ":" + key.getValue();
        if (raw.length() <= 64) {
            return raw;
        }
        // MySQL GET_LOCK() supports max 64 characters; hash if longer
        int hash = raw.hashCode();
        return raw.substring(0, 50) + ":" + Integer.toHexString(hash);
    }


    private InterruptedException tryTimeoutInterrupt(String sessionName) {
        String m = String.format("%s timeout lockTimeoutSeconds[%s]", sessionName, this.lockTimeoutSeconds);
        return new InterruptedException(m);
    }


    private static final class ArmyLease implements SandboxLease {

        private final SyncSession session;

        private final Supplier<Select> unlockSupplier;

        private ArmyLease(SyncSession session, Supplier<Select> unlockSupplier) {
            this.session = session;
            this.unlockSupplier = unlockSupplier;
        }

        @Override
        public void close() {
            if (this.session.isClosed()) {
                return;
            }

            try (SyncSession session = this.session) {
                Object locked;
                locked = session.queryOne(this.unlockSupplier.get(), Object.class);
                LOG.debug("{} released lock state : {}", this.session.name(), locked);
            } catch (Throwable e) {
                LOG.warn("Failed to release lock: {}", this.session.name(), e);
            }

        }


    } // ArmyLease


    public static Builder builder() {
        return new Builder();
    }


    public static final class Builder {

        private static final String DEFAULT_KEY_PREFIX = "agentscope:sandbox:lock:";

        private static final int DEFAULT_LOCK_TIMEOUT_SECONDS = 1800; // 30 minutes

        private SyncSessionFactory sessionFactory;

        private String keyPrefix = DEFAULT_KEY_PREFIX;

        private int lockTimeoutSeconds = DEFAULT_LOCK_TIMEOUT_SECONDS;

        private Builder() {
        }

        public Builder sessionFactory(final SyncSessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
            return this;
        }

        public Builder keyPrefix(final String keyPrefix) {
            this.keyPrefix = keyPrefix;
            return this;
        }

        public Builder lockTimeoutSeconds(final int lockTimeoutSeconds) {
            this.lockTimeoutSeconds = lockTimeoutSeconds;
            return this;
        }

        public ArmySandboxExecutionGuard build() {
            if (this.sessionFactory == null) {
                throw new IllegalStateException("sessionFactory is required");
            }
            if (this.keyPrefix == null) {
                this.keyPrefix = DEFAULT_KEY_PREFIX;
            }
            return new ArmySandboxExecutionGuard(this);
        }


    } // Builder

}
