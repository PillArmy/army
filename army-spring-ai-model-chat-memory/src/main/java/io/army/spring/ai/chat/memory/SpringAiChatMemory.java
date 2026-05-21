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

import io.army.annotation.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;

import java.time.LocalDateTime;


@MappedSuperclass
public abstract class SpringAiChatMemory {

    @Generator(type = GeneratorType.DEFAULT)
    @Column(name = "${DEFAULT}", precision = Column.DEFAULT_EXP, scale = Column.DEFAULT_EXP)
    private Long id;

    @Column(name = "${DEFAULT}", defaultValue = "'1979-01-01 00:00:00'")
    private LocalDateTime createTime;

    /// {@link Mapping#value()} should be one of below:
    /// - {@link io.army.mapping.SqlBigIntType}
    /// - {@link io.army.mapping.StringType}
    /// - {@link io.army.mapping.UUIDType}
    ///
    /// @see org.springframework.ai.chat.memory.ChatMemory#CONVERSATION_ID
    @Column(name = "${DEFAULT}", notNull = true, precision = Column.DEFAULT_EXP, comment = "${DEFAULT}")
    @Mapping("${DEFAULT}")
    private String conversationId;

    @Column(name = "${DEFAULT}", notNull = true, comment = "${DEFAULT}")
    @Mapping("io.army.mapping.TextType")
    private String content;

    /// store one of below :
    /// 1. {@link AssistantMessage#getToolCalls()}
    /// 2. {@link ToolResponseMessage#getResponses()}
    @Column(name = "${DEFAULT}", notNull = true, defaultValue = "'[]'", comment = "${DEFAULT}")
    @Mapping("io.army.mapping.PreferredJsonbType")
    private String specializedData;

    @Column(name = "${DEFAULT}", notNull = true, precision = 10, comment = "${DEFAULT}")
    private MessageType type;

    public Long getId() {
        return id;
    }

    public SpringAiChatMemory setId(Long id) {
        this.id = id;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public SpringAiChatMemory setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getConversationId() {
        return conversationId;
    }

    public SpringAiChatMemory setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public String getContent() {
        return content;
    }

    public SpringAiChatMemory setContent(String content) {
        this.content = content;
        return this;
    }

    public String getSpecializedData() {
        return specializedData;
    }

    public SpringAiChatMemory setSpecializedData(String specializedData) {
        this.specializedData = specializedData;
        return this;
    }

    public MessageType getType() {
        return type;
    }

    public SpringAiChatMemory setType(MessageType type) {
        this.type = type;
        return this;
    }

}
