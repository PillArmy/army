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

package io.army.spring.ai.vectorstore;

import io.army.annotation.Column;
import io.army.annotation.MappedSuperclass;
import io.army.annotation.Mapping;
import io.army.generator.snowflake.Snowflake8s;


/// Subclass for long-term memory storage of AI Agent
@MappedSuperclass
public abstract class SpringAiChatVectorStore extends SpringAiVectorStore {

    /// {@link Mapping#value()} should be one of below:
    /// - {@link io.army.mapping.SqlBigIntType}
    /// - {@link io.army.mapping.StringType}
    /// - {@link io.army.mapping.UUIDType}
    ///
    /// If the entity is used to store long-term chat session memory, the conversationId field is required; otherwise,
    ///  it is unnecessary and shall not be configurable.
    ///
    /// @see org.springframework.ai.chat.memory.ChatMemory#CONVERSATION_ID
    @Column(name = "${DEFAULT}", notNull = true, defaultValue = "${DEFAULT}", precision = Column.DEFAULT_EXP, comment = "${DEFAULT}")
    @Mapping("${DEFAULT}")
    private String conversationId;

    /// {@link org.springframework.ai.chat.messages.Message} batch no, use {@link Snowflake8s}
    @Column(name = "${DEFAULT}", notNull = true, defaultValue = "${DEFAULT}", precision = Column.DEFAULT_EXP, comment = "${DEFAULT}")
    private Long batchNo;


    public String getConversationId() {
        return conversationId;
    }

    public SpringAiChatVectorStore setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public Long getBatchNo() {
        return batchNo;
    }

    public SpringAiChatVectorStore setBatchNo(Long batchNo) {
        this.batchNo = batchNo;
        return this;
    }
}
