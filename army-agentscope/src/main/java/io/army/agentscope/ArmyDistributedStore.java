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

package io.army.agentscope;

import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.DistributedStore;
import io.agentscope.harness.agent.filesystem.remote.store.BaseStore;
import io.agentscope.harness.agent.sandbox.SandboxExecutionGuard;
import io.agentscope.harness.agent.sandbox.snapshot.RemoteSnapshotSpec;
import io.agentscope.harness.agent.sandbox.snapshot.SandboxSnapshotSpec;
import io.army.agentscope.sandbox.ArmySandboxExecutionGuard;
import io.army.agentscope.snapshot.ArmyRemoteSnapshotClient;
import io.army.agentscope.state.ArmyAgentStateStore;
import io.army.agentscope.store.ArmyBaseStore;

import java.util.Objects;

public final class ArmyDistributedStore implements DistributedStore {

    private final ArmySandboxExecutionGuard.Builder sandboxExecutionGuardBuilder;

    private final ArmyRemoteSnapshotClient.Builder<?> snapshotClientBuilder;

    private final ArmyAgentStateStore.Builder<?> stateStoreBuilder;

    private final ArmyBaseStore.Builder<?> baseStoreBuilder;

    private ArmyDistributedStore(final Builder builder) {
        this.sandboxExecutionGuardBuilder = builder.sandboxExecutionGuardBuilder;
        this.snapshotClientBuilder = builder.snapshotClientBuilder;
        this.stateStoreBuilder = builder.stateStoreBuilder;
        this.baseStoreBuilder = builder.baseStoreBuilder;
    }


    @Override
    public AgentStateStore agentStateStore() {
        return this.stateStoreBuilder.build();
    }

    @Override
    public BaseStore baseStore() {
        return this.baseStoreBuilder.build();
    }

    @Override
    public SandboxSnapshotSpec sandboxSnapshotSpec() {
        return new RemoteSnapshotSpec(this.snapshotClientBuilder.build());
    }

    @Override
    public SandboxExecutionGuard sandboxExecutionGuard() {
        return this.sandboxExecutionGuardBuilder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private ArmySandboxExecutionGuard.Builder sandboxExecutionGuardBuilder;

        private ArmyRemoteSnapshotClient.Builder<?> snapshotClientBuilder;

        private ArmyAgentStateStore.Builder<?> stateStoreBuilder;

        private ArmyBaseStore.Builder<?> baseStoreBuilder;


        private Builder() {
        }

        public Builder sandboxExecutionGuardBuilder(final ArmySandboxExecutionGuard.Builder guardBuilder) {
            this.sandboxExecutionGuardBuilder = guardBuilder;
            return this;
        }

        public Builder snapshotClientBuilder(final ArmyRemoteSnapshotClient.Builder<?> snapshotClientBuilder) {
            this.snapshotClientBuilder = snapshotClientBuilder;
            return this;
        }

        public Builder stateStoreBuilder(final ArmyAgentStateStore.Builder<?> stateStoreBuilder) {
            this.stateStoreBuilder = stateStoreBuilder;
            return this;
        }

        public Builder baseStoreBuilder(final ArmyBaseStore.Builder<?> baseStoreBuilder) {
            this.baseStoreBuilder = baseStoreBuilder;
            return this;
        }


        public ArmyDistributedStore build() {
            Objects.requireNonNull(this.sandboxExecutionGuardBuilder, "guardBuilder");
            Objects.requireNonNull(this.snapshotClientBuilder, "snapshotClientBuilder");
            Objects.requireNonNull(this.stateStoreBuilder, "stateStoreBuilder");
            Objects.requireNonNull(this.baseStoreBuilder, "baseStoreBuilder");
            return new ArmyDistributedStore(this);
        }


    }


}
