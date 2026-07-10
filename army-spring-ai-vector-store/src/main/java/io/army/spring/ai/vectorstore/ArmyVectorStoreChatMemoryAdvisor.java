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

import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// ChatClient Advisor that integrates vector store for long-term memory storage.
///
/// This advisor automatically saves user and assistant messages to the vector store
/// for long-term memory retrieval. Messages are embedded and stored with conversation ID metadata.
///
/// ### Behavior:
/// 1. **Before**: Saves user messages to the vector store
/// 2. **After**: Saves assistant messages to the vector store
/// 3. Messages are filtered to only include USER and ASSISTANT types
///
/// ### Important Design Note:
/// This advisor **only performs save operations** and does NOT automatically retrieve and inject
/// long-term memory into the prompt. Long-term memory is sent to the AI model through the
/// {@link ArmyVectorStore#memoryTool(String)} method (configured as `defaultTools`),
/// allowing the AI agent to actively query long-term memory when needed through similarity search.
/// This is more efficient than sending all memory with every request.
///
/// ### Usage:
/// ```java
/// ArmyVectorStoreChatMemoryAdvisor advisor = ArmyVectorStoreChatMemoryAdvisor.builder(vectorStore)
///         .order(Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER)
///         .build();
/// ```
///
/// @see ArmyVectorStore
/// @see ArmyVectorStore#memoryTool(String)
/// @see org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor
public final class ArmyVectorStoreChatMemoryAdvisor implements BaseChatMemoryAdvisor {


    /// Metadata key for conversation ID.
    private static final String DOCUMENT_METADATA_CONVERSATION_ID = "conversationId";

    /// Metadata key for message type.
    private static final String DOCUMENT_METADATA_MESSAGE_TYPE = "messageType";


    /// The advisor order.
    private final int order;

    /// The scheduler for async operations.
    private final Scheduler scheduler;

    /// The vector store for storing messages.
    private final VectorStore vectorStore;

    private ArmyVectorStoreChatMemoryAdvisor(int order, Scheduler scheduler, VectorStore vectorStore) {
        Assert.notNull(scheduler, "scheduler cannot be null");
        Assert.notNull(vectorStore, "vectorStore cannot be null");
        this.order = order;
        this.scheduler = scheduler;
        this.vectorStore = vectorStore;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        final String conversationId;
        conversationId = getConversationId(chatClientRequest.context());

        UserMessage userMessage = chatClientRequest.prompt().getUserMessage();
        if (userMessage != null) {
            this.vectorStore.write(toDocuments(List.of(userMessage), conversationId));
        }
        return chatClientRequest;
    }


    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {

        final ChatResponse response = chatClientResponse.chatResponse();
        if (response != null) {

            final List<Message> assistantMessages;
            assistantMessages = response
                    .getResults()
                    .stream()
                    .map(g -> (Message) g.getOutput())
                    .toList();


            this.vectorStore.write(toDocuments(assistantMessages, this.getConversationId(chatClientResponse.context())));
        }
        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
                                                 StreamAdvisorChain streamAdvisorChain) {
        // Get the scheduler from BaseAdvisor
        Scheduler scheduler = this.getScheduler();
        // Process the request with the before method
        return Mono.just(chatClientRequest)
                .publishOn(scheduler)
                .map(request -> this.before(request, streamAdvisorChain))
                .flatMapMany(streamAdvisorChain::nextStream)
                .transform(flux -> new ChatClientMessageAggregator().aggregateChatClientResponse(flux,
                        response -> this.after(response, streamAdvisorChain)));
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    private List<Document> toDocuments(List<Message> messages, String conversationId) {
        return messages.stream()
                .filter(m -> m.getMessageType() == MessageType.USER || m.getMessageType() == MessageType.ASSISTANT)
                .map(message -> {
                    Map<String, Object> metadata = new HashMap<>(
                            message.getMetadata() != null ? message.getMetadata() : new HashMap<>());
                    metadata.put(DOCUMENT_METADATA_CONVERSATION_ID, conversationId);
                    metadata.put(DOCUMENT_METADATA_MESSAGE_TYPE, message.getMessageType().name());
                    if (message instanceof UserMessage userMessage) {
                        return Document.builder()
                                .text(userMessage.getText())
                                // userMessage.getMedia().get(0).getId()
                                // TODO vector store for memory would not store this into the
                                // vector store, could store an 'id' instead
                                // .media(userMessage.getMedia())
                                .metadata(metadata)
                                .build();
                    } else if (message instanceof AssistantMessage assistantMessage) {
                        return Document.builder().text(assistantMessage.getText()).metadata(metadata).build();
                    }
                    throw new RuntimeException("Unknown message type: " + message.getMessageType());
                })
                .toList();
    }

    /// Creates a new builder for ArmyVectorStoreChatMemoryAdvisor.
    ///
    /// @param vectorStore The vector store to use for storing messages
    /// @return The builder
    public static Builder builder(VectorStore vectorStore) {
        return new Builder(vectorStore);
    }


    /// Builder for ArmyVectorStoreChatMemoryAdvisor.
    public static final class Builder {

        /// Default scheduler.
        private Scheduler scheduler = BaseAdvisor.DEFAULT_SCHEDULER;

        /// Default advisor order.
        private int order = Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER;

        /// The vector store.
        private final VectorStore vectorStore;

        /// Creates a new builder instance.
        ///
        /// @param vectorStore The vector store to use
        private Builder(VectorStore vectorStore) {
            this.vectorStore = vectorStore;
        }


        /// Sets the scheduler for async operations.
        ///
        /// @param scheduler The scheduler
        /// @return The builder
        public Builder scheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        /// Sets the advisor order.
        ///
        /// @param order The order
        /// @return The builder
        public Builder order(int order) {
            this.order = order;
            return this;
        }

        /// Builds the advisor.
        ///
        /// @return The advisor instance
        public ArmyVectorStoreChatMemoryAdvisor build() {
            return new ArmyVectorStoreChatMemoryAdvisor(this.order, this.scheduler, this.vectorStore);
        }

    }


}
