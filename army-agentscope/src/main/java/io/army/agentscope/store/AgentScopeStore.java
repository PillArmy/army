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

package io.army.agentscope.store;

import io.army.annotation.Column;
import io.army.annotation.Generator;
import io.army.annotation.GeneratorType;
import io.army.annotation.Mapping;
import io.army.annotation.MappedSuperclass;

import java.time.LocalDateTime;

/// Base domain for {@link io.agentscope.harness.agent.filesystem.remote.store.BaseStore} persistence.
///
/// <p>Subclass this, add @Table, and optionally extra business columns. The logical unique key
/// is (namespaceHash, itemKeyHash) — SHA-256 digests because MySQL limits the byte length of
/// a unique index on utf8mb4 columns. The original namespacePath and itemKey are stored
/// verbatim for the search prefix query.</p>
@MappedSuperclass
public abstract class AgentScopeStore {

    @Generator(type = GeneratorType.DEFAULT)
    @Column(name = "${DEFAULT}", precision = Column.DEFAULT_EXP, scale = Column.DEFAULT_EXP)
    private String id;

    @Column(name = "${DEFAULT}", comment = "${DEFAULT}")
    private LocalDateTime createTime;

    @Column(name = "${DEFAULT}", comment = "${DEFAULT}")
    private LocalDateTime updateTime;

    /// don't use version ,because it is 1-based
    @Column(name = "${DEFAULT}", defaultValue = "1", comment = "${DEFAULT}")
    private Long valueVersion;

    @Column(name = "${DEFAULT}", notNull = true, updatable = false, precision = Column.DEFAULT_EXP, comment = "${DEFAULT}")
    private String namespacePath;

    @Column(name = "${DEFAULT}", notNull = true, updatable = false, precision = Column.DEFAULT_EXP, comment = "${DEFAULT}")
    private String itemKey;

    @Column(name = "${DEFAULT}", notNull = true, comment = "${DEFAULT}")
    @Mapping("io.army.mapping.PreferredJsonbType")
    private String valueJson;

    public String getId() {
        return id;
    }

    public AgentScopeStore setId(String id) {
        this.id = id;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public AgentScopeStore setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public AgentScopeStore setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public Long getValueVersion() {
        return valueVersion;
    }

    public AgentScopeStore setValueVersion(Long valueVersion) {
        this.valueVersion = valueVersion;
        return this;
    }

    public String getNamespacePath() {
        return namespacePath;
    }

    public AgentScopeStore setNamespacePath(String namespacePath) {
        this.namespacePath = namespacePath;
        return this;
    }

    public String getItemKey() {
        return itemKey;
    }

    public AgentScopeStore setItemKey(String itemKey) {
        this.itemKey = itemKey;
        return this;
    }

    public String getValueJson() {
        return valueJson;
    }

    public AgentScopeStore setValueJson(String valueJson) {
        this.valueJson = valueJson;
        return this;
    }
}
