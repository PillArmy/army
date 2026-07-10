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


/// Army implementation of Spring AI's {@link ChatMemory} interface.
///
/// This class provides a higher-level chat memory implementation with the following features:
///
/// - **Append-Only Storage**: Messages are appended without deleting previous ones
/// - **Message Windowing**: Configurable max message count per conversation
/// - **Tool Integration**: Built-in memory tool for AI agent calling
/// - **Type-Safe**: Uses Army's compile-time metamodel
///
/// ### Key Differences from Spring AI's MessageWindowChatMemory:
/// - Does NOT delete previous messages in {@link #add(String, List)}
/// - Messages are retained in storage, only limited during retrieval
/// - Automatic message retention via {@link #maxMessages} during {@link #get(String)}
///
/// ### Usage:
/// ```java
/// ArmyMessageChatMemory&lt;CoderChatMemory&gt; memory = ArmyMessageChatMemory.builder(context, CoderChatMemory_.T)
///         .maxMessages(30)
///         .build();
/// ```
///
/// @param <T> The domain class type extending {@link SpringAiChatMemory}
/// @see ArmyChatMemoryRepository
/// @see ArmyMessageChatMemoryAdvisor
public final class ArmyMessageChatMemory<T extends SpringAiChatMemory> extends ArmyChatMemorySupport<T> implements ChatMemory {


    /// Maximum number of messages per conversation.
    /// Messages beyond this limit are still stored but not retrieved.
    private final int maxMessages;

    private ArmyMessageChatMemory(Builder<T> builder) {
        super(builder);
        this.maxMessages = builder.maxMessages;
    }


    /// Adds messages to the chat memory.
    ///
    /// Messages are appended to storage without deleting previous messages.
    /// This is different from Spring AI's {@link org.springframework.ai.chat.memory.MessageWindowChatMemory}
    /// which deletes old messages when adding new ones.
    ///
    /// @param conversationId The conversation identifier
    /// @param messages       The messages to add
    @Override
    public void add(String conversationId, List<Message> messages) {
        assertConversationId(conversationId);
        Assert.notNull(messages, "messages cannot be null");

        final Consumer<SyncSession> function;
        function = session -> {
            saveMessages(session, conversationId, messages);
        };
        final String sessionName = getClass().getName() + '.' + "add";
        this.sessionContext.executeVoid(sessionName, false, function);
    }


    /// Retrieves messages for a conversation.
    ///
    /// Returns up to {@link #maxMessages} most recent messages in descending order (newest first).
    /// Messages beyond the limit are still stored but not returned.
    ///
    /// @param conversationId The conversation identifier
    /// @return The list of messages, limited by maxMessages
    @Override
    public List<Message> get(final String conversationId) {
        assertConversationId(conversationId);

        final Function<SyncSession, List<Message>> callBack;
        callBack = session -> {

            final Select stmt;
            stmt = SQLs.query()
                    .select(this.content, this.type, this.specializedData, this.id)
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


    /// Clears all messages for a conversation.
    ///
    /// @param conversationId The conversation identifier
    @Override
    public void clear(String conversationId) {
        deleteMessageByConversationId("clear", conversationId);
    }


    /// Returns the maximum number of messages per conversation.
    @Override
    Integer maxMessages() {
        return this.maxMessages;
    }

    /// Creates a new builder for ArmyMessageChatMemory.
    ///
    /// @param sessionContext The Army SyncSessionContext
    /// @param tableMeta The compile-time metamodel for the domain class
    /// @param <T> The domain class type extending SpringAiChatMemory
    /// @return The builder
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
