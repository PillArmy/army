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

import io.army.codec.JsonCodec;
import io.army.criteria.Delete;
import io.army.criteria.Insert;
import io.army.criteria.Select;
import io.army.criteria.impl.SQLs;
import io.army.result.CurrentRecord;
import io.army.session.SyncSession;
import io.army.session.SyncSessionContext;
import io.army.transaction.TransactionOption;
import io.army.util._Exceptions;
import io.army.util._StringUtils;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.*;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static io.army.criteria.impl.SQLs.AS;

public final class ArmyChatMemoryRepository implements ChatMemoryRepository {

    public static ArmyChatMemoryRepository create(SyncSessionContext sessionContext) {
        return new ArmyChatMemoryRepository(sessionContext);
    }

    private final SyncSessionContext sessionContext;

    private ArmyChatMemoryRepository(SyncSessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }


    @Override
    public List<String> findConversationIds() {
        final Select stmt;
        stmt = SQLs.query()
                .select(List.of(SQLs.DISTINCT))
                .space(SpringAiChatMemory_.conversationId)
                .from(SpringAiChatMemory_.T, AS, "t")
                .orderBy(c -> {
                    if (!SpringAiChatMemory_.id.isSerial()) {
                        c.accept(SpringAiChatMemory_.createTime);
                    }
                    c.accept(SpringAiChatMemory_.id);
                })
                .asQuery();

        final Function<SyncSession, List<String>> function;
        function = session -> session.queryList(stmt, String.class);
        final String sessionName = getClass().getName() + '.' + "findConversationIds";
        return this.sessionContext.execute(sessionName, true, function);
    }

    @Override
    public List<Message> findByConversationId(final String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");

        final Select stmt;
        stmt = SQLs.query()
                .select(SpringAiChatMemory_.content, SpringAiChatMemory_.type, SpringAiChatMemory_.specializedData)
                .from(SpringAiChatMemory_.T, AS, "t")
                .where(SpringAiChatMemory_.conversationId.equal(conversationId))
                .orderBy(c -> {
                    if (!SpringAiChatMemory_.id.isSerial()) {
                        c.accept(SpringAiChatMemory_.createTime);
                    }
                    c.accept(SpringAiChatMemory_.id);
                })
                .asQuery();

        final JsonCodec jsonCodec;
        jsonCodec = this.sessionContext.sessionFactory().jsonCodec();

        final Function<CurrentRecord, Message> function = record -> {
            final String content;
            content = record.getNonNull(0, String.class);

            final Message message;
            switch (record.getNonNull(1, MessageType.class)) {
                case USER:
                    message = new UserMessage(content);
                    break;
                case SYSTEM:
                    message = new SystemMessage(content);
                    break;
                case ASSISTANT: {
                    final String specializedData = record.get(2, String.class);
                    if (_StringUtils.hasText(specializedData)) {
                        message = AssistantMessage.builder()
                                .content(content)
                                .toolCalls(jsonCodec.decodeList(specializedData, AssistantMessage.ToolCall.class))
                                .build();
                    } else {
                        message = new AssistantMessage(content);
                    }
                }
                break;
                case TOOL: {
                    final String specializedData = record.get(2, String.class);
                    final ToolResponseMessage.Builder builder;
                    builder = ToolResponseMessage.builder();
                    if (_StringUtils.hasText(specializedData)) {
                        builder.responses(jsonCodec.decodeList(specializedData, ToolResponseMessage.ToolResponse.class));
                    } else {
                        builder.responses(List.of());
                    }
                    message = builder.build();
                }
                break;
                default:
                    throw _Exceptions.unexpectedEnum(record.getNonNull(SpringAiChatMemory_.TYPE, MessageType.class));

            }
            return message;
        };
        final Function<SyncSession, List<Message>> callBack;
        callBack = session -> session.queryRecordList(stmt, function);
        final String sessionName = getClass().getName() + '.' + "findByConversationId";
        return this.sessionContext.execute(sessionName, true, callBack);
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");

        final JsonCodec jsonCodec;
        jsonCodec = this.sessionContext.sessionFactory().jsonCodec();

        final List<SpringAiChatMemory> domainList = new ArrayList<>(messages.size());
        SpringAiChatMemory domain;
        for (Message message : messages) {
            domain = new SpringAiChatMemory()
                    .setConversationId(conversationId)
                    .setContent(message.getText())
                    .setType(message.getMessageType());

            if (message instanceof AssistantMessage a) {
                domain.setSpecializedData(jsonCodec.encode(a.getToolCalls()));
            } else if (message instanceof ToolResponseMessage t) {
                domain.setSpecializedData(jsonCodec.encode(t.getResponses()));
            }
            domainList.add(domain);
        }

        final Insert stmt;
        stmt = SQLs.singleInsert()
                .insertInto(SpringAiChatMemory_.T)
                .values(domainList)
                .asInsert();

        final Function<SyncSession, Void> function;
        function = session -> {
            session.update(deleteByConversationIdStmt(conversationId));
            session.update(stmt);
            return null;
        };

        final String sessionName = getClass().getName() + '.' + "saveAll";
        this.sessionContext.executeInTransaction(sessionName, TransactionOption::option, function);
    }

    @Override
    public void deleteByConversationId(final String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");

        final Function<SyncSession, Void> function;
        function = session -> {
            session.update(deleteByConversationIdStmt(conversationId));
            return null;
        };

        final String sessionName = getClass().getName() + '.' + "deleteByConversationId";
        this.sessionContext.execute(sessionName, false, function);

    }

    private static Delete deleteByConversationIdStmt(final String conversationId) {
        return SQLs.singleDelete()
                .deleteFrom(SpringAiChatMemory_.T, AS, "t")
                .where(SpringAiChatMemory_.conversationId.equal(conversationId))
                .asDelete();
    }


}
