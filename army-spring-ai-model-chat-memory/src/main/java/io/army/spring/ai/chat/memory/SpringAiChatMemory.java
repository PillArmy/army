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
import io.army.generator.GeneratorStrategy;
import org.springframework.ai.chat.messages.MessageType;

import java.time.LocalDateTime;


@Table(name = "${DEFAULT}",
        indexes = {
                @Index(name = "${DEFAULT}", fieldList = {"conversationId", "createTime"})
        },
        immutable = true,
        comment = "${DEFAULT}")
public class SpringAiChatMemory {


    /// Since the framework cannot decide the ID generation strategy on behalf of users, it is designed as a runtime type.
    /// Users need to configure it via classpath:META-INF/army/TableMeta.properties, following the format:
    /// entity_class_name.field_name = generator_strategy_class[:paramStr]
    ///
    /// If you do not specify it, {@link io.army.generator.PostGeneratorStrategy} will be used.
    ///
    /// example : io.army.spring.ai.chat.memory.SpringAiChatMemory.id=io.army.generator.SnowflakeGeneratorStrategy:{"startTime":1776386333818}
    ///
    /// @see GeneratorStrategy
    /// @see io.army.generator.PostGeneratorStrategy
    /// @see io.army.generator.SnowflakeGeneratorStrategy
    @Generator(type = GeneratorType.DEFAULT)
    @Column
    private Long id;

    @Column(name = "${DEFAULT}", defaultValue = "'1979-01-01 00:00:00'")
    private LocalDateTime createTime;

    @Column(name = "${DEFAULT}", notNull = true, precision = 36, comment = "${DEFAULT}")
    private String conversationId;

    @Column(name = "${DEFAULT}", notNull = true, comment = "${DEFAULT}")
    @Mapping("io.army.mapping.TextType")
    private String content;

    @Column(name = "${DEFAULT}", comment = "${DEFAULT}")
    @Mapping("io.army.mapping.JsonType")
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
