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

import io.army.dialect._Constant;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.ArrayList;
import java.util.List;

/// ChatClient Advisor that adds conversation ID to system messages.
///
/// This advisor modifies system messages to include the current conversation ID,
/// making it available to the AI model in the system prompt.
///
/// ### Usage:
/// ```java
/// SystemMessageAdvisor advisor = SystemMessageAdvisor.of(Ordered.HIGHEST_PRECEDENCE + 10);
/// ```
///
/// ### Behavior:
/// For each SystemMessage in the prompt, appends the conversation ID at the end:
///
/// ```
/// original system text
/// current conversation id : abc123
/// ```
///
/// @see org.springframework.ai.chat.client.advisor.api.BaseAdvisor
public final class SystemMessageAdvisor implements BaseAdvisor {

    /// Creates a new SystemMessageAdvisor with the given order.
    ///
    /// @param order The advisor order
    /// @return The SystemMessageAdvisor instance
    public static SystemMessageAdvisor of(int order) {
        return new SystemMessageAdvisor(order);
    }


    /// The advisor order.
    private final int order;

    private SystemMessageAdvisor(int order) {
        this.order = order;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {

        final Object conversationId;
        conversationId = chatClientRequest.context().get(ChatMemory.CONVERSATION_ID);

        final List<Message> promptMessages;
        promptMessages = new ArrayList<>(chatClientRequest.prompt().getInstructions());

        final int size = promptMessages.size();

        SystemMessage.Builder builder;
        String original;
        Message message;

        for (int i = 0; i < size; i++) {
            message = promptMessages.get(i);

            if (!(message instanceof SystemMessage sm)) {
                continue;
            }

            original = sm.getText();
            if (original == null) {
                continue;
            }

            builder = sm.mutate();

            builder.text(original + _Constant.LF + "current conversation id : " + conversationId + _Constant.LF);

            promptMessages.set(i, builder.build());

        } // loop

        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().mutate().messages(promptMessages).build())
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        // do nothing
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return this.order;
    }


}
