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

package io.army.agentscope.state;

import io.army.annotation.*;

import java.time.LocalDateTime;

/// Base domain for {@link io.agentscope.core.state.AgentStateStore} persistence.
///
/// Subclass this, add @Table, and declare exactly one unique index over
/// (userId, sessionId, stateKey, itemIndex). One row is one state item: itemIndex is 0 for a
/// single value and the list position for list values. A list additionally stores a hash row
/// under stateKey + ":_hash".
///
/// Reserved fields id, createTime, updateTime and version are managed by Army. version starts
/// at 1 (defaultValue) and is incremented by the framework on each update. stateValue holds the
/// state object as JSON and is fixed to io.army.mapping.PreferredJsonbType.
@MappedSuperclass
public abstract class AgentScopeSessions {

    @Generator(type = GeneratorType.DEFAULT)
    @Column(name = "${DEFAULT}", precision = Column.DEFAULT_EXP, scale = Column.DEFAULT_EXP)
    @Mapping("${DEFAULT}")
    private String id;

    @Column(name = "${DEFAULT}",comment = "${DEFAULT}")
    private LocalDateTime createTime;

    @Column(name = "${DEFAULT}",comment = "${DEFAULT}")
    private LocalDateTime updateTime;

    @Column(name = "${DEFAULT}",notNull = true, defaultValue = "1", comment = "${DEFAULT}")
    private Long version;

    @Column(name = "${DEFAULT}", notNull = true, updatable = false, precision = Column.DEFAULT_EXP,comment = "${DEFAULT}")
    @Mapping("${DEFAULT}")
    private String userId;

    @Column(name = "${DEFAULT}", notNull = true, updatable = false, precision = Column.DEFAULT_EXP,comment = "${DEFAULT}")
    @Mapping("${DEFAULT}")
    private String sessionId;

    @Column(name = "${DEFAULT}", notNull = true, updatable = false,precision = Column.DEFAULT_EXP, comment = "${DEFAULT}")
    @Mapping("${DEFAULT}")
    private String stateKey;

    @Column(name = "${DEFAULT}", notNull = true, updatable = false,defaultValue = "${DEFAULT}", comment = "${DEFAULT}")
    @Mapping("${DEFAULT}")
    private Integer itemIndex;

    @Column(name = "${DEFAULT}", notNull = true, comment = "${DEFAULT}")
    @Mapping("io.army.mapping.PreferredJsonbType")
    private String stateValue;

    public String getId() {
        return id;
    }

    public AgentScopeSessions setId(String id) {
        this.id = id;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public AgentScopeSessions setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public AgentScopeSessions setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public Long getVersion() {
        return version;
    }

    public AgentScopeSessions setVersion(Long version) {
        this.version = version;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public AgentScopeSessions setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public AgentScopeSessions setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public String getStateKey() {
        return stateKey;
    }

    public AgentScopeSessions setStateKey(String stateKey) {
        this.stateKey = stateKey;
        return this;
    }

    public Integer getItemIndex() {
        return itemIndex;
    }

    public AgentScopeSessions setItemIndex(Integer itemIndex) {
        this.itemIndex = itemIndex;
        return this;
    }

    public String getStateValue() {
        return stateValue;
    }

    public AgentScopeSessions setStateValue(String stateValue) {
        this.stateValue = stateValue;
        return this;
    }
}
