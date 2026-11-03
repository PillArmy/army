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

package io.army.agentscope.snapshot;

import io.army.annotation.*;

import java.time.LocalDateTime;

/// Base domain for {@link io.agentscope.harness.agent.sandbox.snapshot.RemoteSnapshotClient} persistence.
///
/// Subclass this, add @Table, and optionally extra business columns. The primary key id is the
/// snapshot ID itself: no generator is declared, upload assigns it. data holds the snapshot tar
/// archive and is fixed to io.army.mapping.LongBlobType, which maps to bytea on PostgreSQL and
/// LONGBLOB on MySQL.
///
/// Reserved fields createTime, updateTime and version are managed by Army.
@MappedSuperclass
public abstract class AgentScopeSnapshot {

    @Column(name = "${DEFAULT}", precision = Column.DEFAULT_EXP, scale = Column.DEFAULT_EXP, comment = "${DEFAULT}")
    private String id;

    @Column(name = "${DEFAULT}", comment = "${DEFAULT}")
    private LocalDateTime createTime;

    @Column(name = "${DEFAULT}", comment = "${DEFAULT}")
    private LocalDateTime updateTime;

    @Column(name = "${DEFAULT}",defaultValue = "1", comment = "${DEFAULT}")
    private Long version;

    @Column(name = "${DEFAULT}", notNull = true, comment = "Snapshot tar archive")
    @Mapping("io.army.mapping.LongBlobType")
    private byte[] data;

    public String getId() {
        return id;
    }

    public AgentScopeSnapshot setId(String id) {
        this.id = id;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public AgentScopeSnapshot setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public AgentScopeSnapshot setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public Long getVersion() {
        return version;
    }

    public AgentScopeSnapshot setVersion(Long version) {
        this.version = version;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public AgentScopeSnapshot setData(byte[] data) {
        this.data = data;
        return this;
    }
}
