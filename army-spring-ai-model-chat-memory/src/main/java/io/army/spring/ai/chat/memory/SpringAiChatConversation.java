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

package io.army.spring.ai.chat.memory;

import io.army.annotation.Column;
import io.army.annotation.DdlMode;
import io.army.annotation.Mapping;
import io.army.annotation.Table;

import java.time.LocalDateTime;

@Table(name = "${DEFAULT}", ddlMode = DdlMode.NONE, immutable = true, comment = "${DEFAULT}")
public class SpringAiChatConversation {

    @Column(name = "${DEFAULT}", precision = Column.DEFAULT_EXP, scale = Column.DEFAULT_EXP, comment = "${DEFAULT}")
    @Mapping("${DEFAULT}")
    private String id;

    @Column(name = "${DEFAULT}", notNull = true, defaultValue = "'1979-01-01 00:00:00'", comment = "${DEFAULT}")
    private LocalDateTime createTime;

    @Column(name = "${DEFAULT}", notNull = true, defaultValue = "${DEFAULT}", precision = Column.DEFAULT_EXP, scale = Column.DEFAULT_EXP, comment = "${DEFAULT}")
    @Mapping("${DEFAULT}")
    private String userId;

    public String getId() {
        return id;
    }

    public SpringAiChatConversation setId(String id) {
        this.id = id;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public SpringAiChatConversation setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public SpringAiChatConversation setUserId(String userId) {
        this.userId = userId;
        return this;
    }
}
