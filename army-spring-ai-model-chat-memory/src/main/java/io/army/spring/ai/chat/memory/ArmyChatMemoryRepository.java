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
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.util.Assert;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.army.criteria.impl.SQLs.AS;


/// Army implementation of Spring AI's {@link ChatMemoryRepository} interface.
///
/// This class provides a lower-level persistence layer for chat memory with the following features:
///
/// - **Full Replace Storage**: {@link #saveAll(String, List)} deletes existing messages before inserting new ones
/// - **No Windowing**: Returns all messages without limit
/// - **Distinct Query**: {@link #findConversationIds()} returns all distinct conversation IDs
/// - **Type-Safe**: Uses Army's compile-time metamodel
///
/// ### Key Differences from {@link ArmyMessageChatMemory}:
/// - No message windowing logic ({@link #maxMessages()} returns null)
/// - {@link #saveAll(String, List)} replaces all messages, does NOT append
/// - {@link #findByConversationId(String)} returns all messages without limit
/// - Provides {@link #findConversationIds()} which is not available in ArmyMessageChatMemory
///
/// ### Usage:
/// ```java
/// ArmyChatMemoryRepository&lt;CoderChatMemory&gt; repository = ArmyChatMemoryRepository.builder(context, CoderChatMemory_.T)
///         .build();
///
/// // With Spring AI's MessageWindowChatMemory
/// MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder(repository)
///         .maxMessages(20)
///         .build();
/// ```
///
/// @param <T> The domain class type extending {@link SpringAiChatMemory}
/// @see ArmyMessageChatMemory
public final class ArmyChatMemoryRepository<T extends SpringAiChatMemory> extends ArmyChatMemorySupport<T>
        implements ChatMemoryRepository {


    private ArmyChatMemoryRepository(Builder<T> builder) {
        super(builder);
    }


    /// Returns all distinct conversation IDs.
    ///
    /// @return The list of distinct conversation IDs
    @Override
    public List<String> findConversationIds() {

        final Function<SyncSession, List<String>> callBack;
        callBack = session -> {

            final Select stmt;
            stmt = SQLs.query()
                    .selectDistinct()
                    .space(this.conversationId)
                    .from(this.tableMeta, AS, "t")
                    .asQuery();

            return session.queryList(stmt, String.class);
        };

        final String sessionName = getClass().getName() + '.' + "findConversationIds";
        return this.sessionContext.executeNotNull(sessionName, true, callBack);
    }


    /// Finds all messages for a conversation.
    ///
    /// Returns ALL messages in descending order (newest first), without any limit.
    ///
    /// @param conversationId The conversation identifier
    /// @return The list of all messages for the conversation
    @Override
    public List<Message> findByConversationId(String conversationId) {
        assertConversationId(conversationId);

        final Select stmt;
        stmt = SQLs.query()
                .select(this.content)
                .comma(this.type, this.specializedData, this.id)
                .from(this.tableMeta, AS, "t")
                .where(this.conversationId.equal(conversationId))
                .orderBy(this.id.desc())
                .asQuery();

        final Function<SyncSession, List<Message>> callBack;
        callBack = session -> session.queryRecordList(stmt, this.rowFunc);

        final String sessionName = getClass().getName() + '.' + "findByConversationId";
        return this.sessionContext.executeNotNull(sessionName, true, callBack);
    }


    /// Saves messages for a conversation with full replace semantics.
    ///
    /// Deletes all existing messages for the conversation, then inserts the new messages.
    /// This is different from {@link ArmyMessageChatMemory#add(String, List)} which appends messages.
    ///
    /// @param conversationId The conversation identifier
    /// @param messages       The messages to save
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        assertConversationId(conversationId);
        Assert.notNull(messages, "messages cannot be null");

        final Consumer<SyncSession> function;
        function = session -> {
            deleteByConversationId(conversationId);
            saveMessages(session, conversationId, messages);
        };

        final String sessionName = getClass().getName() + '.' + "saveAll";
        this.sessionContext.executeVoid(sessionName, false, function);
    }


    /// Deletes all messages for a conversation.
    ///
    /// @param conversationId The conversation identifier
    @Override
    public void deleteByConversationId(String conversationId) {
        deleteMessageByConversationId("deleteByConversationId", conversationId);
    }

    /// Returns null as this repository does not support message windowing.
    ///
    /// @return Always returns null
    @Nullable
    @Override
    Integer maxMessages() {
        return null;
    }

    /// Creates a new builder for ArmyChatMemoryRepository.
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


    public static final class Builder<T extends SpringAiChatMemory> extends BuilderSupport<Builder<T>, T> {


        private Builder() {
        }

        @Override
        public ArmyChatMemoryRepository<T> build() {
            checkFields();
            return new ArmyChatMemoryRepository<>(this);
        }

    }

}