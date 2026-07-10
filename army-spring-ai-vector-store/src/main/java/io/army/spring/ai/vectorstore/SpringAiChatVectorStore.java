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


/// Subclass for AI agent long-term memory storage.
///
/// This class extends {@link SpringAiVectorStore} with a conversation ID field,
/// enabling vector-based retrieval of long-term chat session memory.
///
/// ### Usage:
/// ```java
/// @Table(name = "coder_chat_vector_store")
/// public class CoderChatVectorStore extends SpringAiChatVectorStore {
///     // Inherits conversationId + all fields from SpringAiVectorStore
/// }
/// ```
///
/// ### Important:
/// - The {@link #conversationId} field must be indexed for optimal performance
/// - When using with {@link ArmyVectorStore}, the metadata must contain a "conversationId" key
///
/// @see ArmyVectorStore
/// @see ArmyVectorStoreChatMemoryAdvisor
@MappedSuperclass
public abstract class SpringAiChatVectorStore extends SpringAiVectorStore {

    /// Conversation identifier.
    ///
    /// {@link Mapping#value()} should be one of below:
    /// - {@link io.army.mapping.SqlBigIntType}
    /// - {@link io.army.mapping.StringType}
    /// - {@link io.army.mapping.UUIDType}
    ///
    /// **Note:** This field is required for long-term chat session memory storage.
    /// It must be indexed for optimal query performance.
    ///
    /// @see org.springframework.ai.chat.memory.ChatMemory#CONVERSATION_ID
    @Column(name = "${DEFAULT}", notNull = true, defaultValue = "${DEFAULT}", precision = Column.DEFAULT_EXP, comment = "${DEFAULT}")
    @Mapping("${DEFAULT}")
    private String conversationId;


    public String getConversationId() {
        return conversationId;
    }

    public SpringAiChatVectorStore setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

}
