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
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.util.Assert;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.army.criteria.impl.SQLs.AS;


/// Unlike {@link ArmyMessageChatMemory}, this class is a lower-level persistence layer:
/// <ul>
///     <li>{@link #saveAll(String, List)} : FULL REPLACE (delete existing then insert), NOT append-only</li>
///     <li>{@link #findByConversationId(String)} : Returns ALL messages in chronological order (ASC), no limit</li>
///     <li>{@link #findConversationIds()} : Returns distinct conversation IDs (new query)</li>
///     <li>No message windowing logic (maxMessages is ChatMemory's responsibility, not Repository's)</li>
/// </ul>
public final class ArmyChatMemoryRepository<T extends SpringAiChatMemory> extends ArmyChatMemorySupport<T>
        implements ChatMemoryRepository {


    private ArmyChatMemoryRepository(Builder<T> builder) {
        super(builder);
    }


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


    @Override
    public void deleteByConversationId(String conversationId) {
        deleteMessageByConversationId("deleteByConversationId", conversationId);
    }


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