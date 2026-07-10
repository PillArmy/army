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

public final class SystemMessageAdvisor implements BaseAdvisor {

    public static SystemMessageAdvisor of(int order) {
        return new SystemMessageAdvisor(order);
    }


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
