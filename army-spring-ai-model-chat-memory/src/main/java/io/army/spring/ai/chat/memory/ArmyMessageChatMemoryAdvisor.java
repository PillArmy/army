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

import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;

/// ChatClient Advisor for automatic chat memory management.
///
/// This advisor automatically saves messages to chat memory before and after chat requests:
///
/// - **Before**: Saves user messages before the chat request
/// - **After**: Saves assistant messages after the chat response
///
/// ### Important Design Note:
/// This advisor **only performs save operations** and does NOT automatically retrieve and inject
/// memory into the prompt. Memory is sent to the AI model through the
/// {@link ArmyChatMemorySupport#memoryTool(String)} method (configured as `defaultTools`),
/// allowing the AI agent to actively query short-term memory when needed.
/// This is more efficient than sending all memory with every request.
///
/// ### Usage:
/// ```java
/// ArmyMessageChatMemoryAdvisor advisor = ArmyMessageChatMemoryAdvisor.builder(chatMemory)
///         .order(Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER)
///         .build();
/// ```
///
/// @see ArmyMessageChatMemory
/// @see ArmyChatMemorySupport#memoryTool(String)
/// @see org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor
public final class ArmyMessageChatMemoryAdvisor implements BaseChatMemoryAdvisor {

    /// The chat memory to manage.
    private final ChatMemory chatMemory;

    /// The advisor order.
    private final int order;

    /// The scheduler for async operations.
    private final Scheduler scheduler;

    private ArmyMessageChatMemoryAdvisor(ChatMemory chatMemory, int order, Scheduler scheduler) {
        Assert.notNull(chatMemory, "chatMemory cannot be null");
        Assert.notNull(scheduler, "scheduler cannot be null");
        this.chatMemory = chatMemory;
        this.order = order;
        this.scheduler = scheduler;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        final String conversationId;
        conversationId = getConversationId(chatClientRequest.context());

        final Message userMessage;
        userMessage = chatClientRequest.prompt().getLastUserOrToolResponseMessage();
        this.chatMemory.add(conversationId, userMessage);

        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {

        final ChatResponse response;
        response = chatClientResponse.chatResponse();
        if (response != null) {
            final List<Message> assistantMessages;
            assistantMessages = response
                    .getResults()
                    .stream()
                    .map(g -> (Message) g.getOutput())
                    .toList();

            if (!assistantMessages.isEmpty()) {
                this.chatMemory.add(this.getConversationId(chatClientResponse.context()), assistantMessages);
            }
        }

        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
                                                 StreamAdvisorChain streamAdvisorChain) {
        // Get the scheduler from BaseAdvisor
        final Scheduler scheduler = this.getScheduler();

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


    public static Builder builder(ChatMemory chatMemory) {
        return new Builder(chatMemory);
    }

    public static final class Builder {

        private int order = Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER;

        private Scheduler scheduler = BaseAdvisor.DEFAULT_SCHEDULER;

        private final ChatMemory chatMemory;

        private Builder(ChatMemory chatMemory) {
            Assert.notNull(chatMemory, "chatMemory cannot be null");
            this.chatMemory = chatMemory;
        }

        /**
         * Set the order.
         *
         * @param order the order
         * @return the builder
         */
        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder scheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        /**
         * Build the advisor.
         *
         * @return the advisor
         */
        public ArmyMessageChatMemoryAdvisor build() {
            return new ArmyMessageChatMemoryAdvisor(this.chatMemory, this.order, this.scheduler);
        }

    }


}
