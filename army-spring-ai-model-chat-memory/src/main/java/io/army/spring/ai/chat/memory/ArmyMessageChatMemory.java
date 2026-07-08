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

import io.army.criteria.Select;
import io.army.criteria.impl.SQLs;
import io.army.meta.SimpleTableMeta;
import io.army.session.SyncSession;
import io.army.session.SyncSessionContext;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.util.Assert;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.army.criteria.impl.SQLs.AS;


/// Unlike org.springframework.ai.chat.memory.MessageWindowChatMemory, this class does not delete previous messages. in {@link #add(String, List)}.
public final class ArmyMessageChatMemory<T extends SpringAiChatMemory> extends ArmyChatMemorySupport<T> implements ChatMemory {


    private final int maxMessages;

    private ArmyMessageChatMemory(Builder<T> builder) {
        super(builder);
        this.maxMessages = builder.maxMessages;
    }


    @Override
    public void add(String conversationId, List<Message> messages) {
        // Unlike org.springframework.ai.chat.memory.MessageWindowChatMemory, this class does not delete previous messages.
        assertConversationId(conversationId);
        Assert.notNull(messages, "messages cannot be null");

        final Consumer<SyncSession> function;
        function = session -> {
            saveMessages(session, conversationId, messages);
        };
        final String sessionName = getClass().getName() + '.' + "add";
        this.sessionContext.executeVoid(sessionName, false, function);
    }


    @Override
    public List<Message> get(final String conversationId) {
        assertConversationId(conversationId);

        final Function<SyncSession, List<Message>> callBack;
        callBack = session -> {

            final Select stmt;
            stmt = SQLs.query()
                    .select(this.content)
                    .comma(this.type, this.specializedData, this.id)
                    .from(this.tableMeta, AS, "t")
                    .where(this.conversationId.equal(conversationId))
                    .orderBy(this.id.desc())
                    .limit(this.maxMessages)
                    .asQuery();
            return session.queryRecordList(stmt, this.rowFunc);
        };
        final String sessionName = getClass().getName() + '.' + "get";
        return this.sessionContext.executeNotNull(sessionName, false, callBack);
    }


    @Override
    public void clear(String conversationId) {
        deleteMessageByConversationId("clear", conversationId);
    }


    public static <T extends SpringAiChatMemory> Builder<T> builder(SyncSessionContext sessionContext, SimpleTableMeta<T> tableMeta) {
        final Builder<T> builder = new Builder<>();
        return builder.sessionContext(sessionContext)
                .tableMeta(tableMeta);
    }


    public static final class Builder<T extends SpringAiChatMemory> extends ArmyChatMemorySupport.BuilderSupport<Builder<T>, T> {


        private int maxMessages = 20;


        private Builder() {

        }

        public Builder<T> maxMessages(int maxMessages) {
            if (maxMessages < 1) {
                throw new IllegalArgumentException(String.format("%s[%s] %s", "maxMessages", maxMessages, "error"));
            }
            this.maxMessages = maxMessages;
            return this;
        }


        public ArmyMessageChatMemory<T> build() {
            checkFields();
            return new ArmyMessageChatMemory<>(this);
        }

    } // Builder


}
