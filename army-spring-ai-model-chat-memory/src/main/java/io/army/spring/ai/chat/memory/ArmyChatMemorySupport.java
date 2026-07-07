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
import io.army.criteria.LiteralMode;
import io.army.criteria.NullMode;
import io.army.criteria.impl.SQLs;
import io.army.meta.FieldMeta;
import io.army.meta.PrimaryFieldMeta;
import io.army.meta.SimpleTableMeta;
import io.army.pojo.ObjectAccessorFactory;
import io.army.result.CurrentRecord;
import io.army.session.SyncSession;
import io.army.session.SyncSessionContext;
import io.army.util._Exceptions;
import io.army.util._StringUtils;
import org.springframework.ai.chat.messages.*;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.army.criteria.impl.SQLs.AS;

abstract class ArmyChatMemorySupport<T extends SpringAiChatMemory> {

    public static final String PRIMARY_ID = "chat_memory_primary_id";

    final SyncSessionContext sessionContext;

    final SimpleTableMeta<T> tableMeta;

    final PrimaryFieldMeta<T> id;

    final FieldMeta<T> type;

    final FieldMeta<T> content;

    final FieldMeta<T> specializedData;

    final FieldMeta<T> conversationId;

    final NullMode nullMode;

    final LiteralMode literalMode;

    final Supplier<T> constructor;

    final Function<CurrentRecord, Message> rowFunc;

    ArmyChatMemorySupport(BuilderSupport<?, T> builder) {
        this.sessionContext = builder.sessionContext;
        this.tableMeta = builder.tableMeta;
        this.nullMode = builder.nullMode;
        this.literalMode = builder.literalMode;

        this.id = this.tableMeta.id();
        this.type = this.tableMeta.field("type");
        this.content = this.tableMeta.field("content");
        this.specializedData = this.tableMeta.field("specializedData");
        this.conversationId = this.tableMeta.field("conversationId");

        this.constructor = ObjectAccessorFactory.pojoConstructor(this.tableMeta.javaType());

        this.rowFunc = messageReadFunc(this.sessionContext.sessionFactory().jsonCodec());
    }


    final void saveMessages(SyncSession session, String conversationId, List<Message> messages) {

        final int size = messages.size();
        if (size == 0) {
            return;
        }

        final List<T> domainList;
        domainList = createDomainList(conversationId, messages);

        final Insert insertStmt;
        insertStmt = SQLs.singleInsert()
                .nullMode(this.nullMode)
                .literalMode(this.literalMode)
                .insertInto(this.tableMeta)
                .values(domainList)
                .asInsert();

        session.update(insertStmt);
    }


    final void deleteMessageByConversationId(String messageName, String conversationId) {
        assertConversationId(conversationId);

        final Consumer<SyncSession> function;
        function = session -> {
            final Delete stmt;
            stmt = SQLs.singleDelete()
                    .deleteFrom(this.tableMeta, AS, "t")
                    .where(this.conversationId.equal(conversationId))
                    .asDelete();

            session.update(stmt);
        };

        final String sessionName = getClass().getName() + '.' + messageName;
        this.sessionContext.executeVoid(sessionName, false, function);
    }


    private List<T> createDomainList(String conversationId, List<Message> messages) {
        final JsonCodec codec = sessionContext.sessionFactory().jsonCodec();

        final List<T> list = new ArrayList<>(messages.size());

        T o;
        for (Message message : messages) {
            Objects.requireNonNull(message, "message cannot be null");

            o = this.constructor.get();
            o.setConversationId(conversationId)
                    .setContent(message.getText())
                    .setType(message.getMessageType());

            if (message instanceof AssistantMessage a) {
                o.setSpecializedData(codec.encode(a.getToolCalls()));
            } else if (message instanceof ToolResponseMessage t) {
                o.setSpecializedData(codec.encode(t.getResponses()));
            }

            list.add(o);
        } // loop

        return list;
    }


    static void assertConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
    }


    /// {@link io.army.criteria.Selection} order :
    /// 0. content
    /// 1. type
    /// 2. specializedData
    ///
    private static Function<CurrentRecord, Message> messageReadFunc(final JsonCodec codec) {
        return record -> {
            final String content;
            content = record.getNonNull(0, String.class);

            final String id = record.getNonNull(3, String.class);


            final Message message;
            switch (record.getNonNull(1, MessageType.class)) {
                case USER:
                    message = UserMessage.builder()
                            .metadata(Map.of(PRIMARY_ID, id))
                            .text(content)
                            .build();
                    break;
                case SYSTEM:
                    message = SystemMessage.builder()
                            .text(content)
                            .metadata(Map.of(PRIMARY_ID, id))
                            .build();
                    break;
                case ASSISTANT: {
                    final String specializedData = record.get(2, String.class);
                    final AssistantMessage.Builder<?> builder;
                    builder = AssistantMessage.builder()
                            .content(content)
                            .properties(Map.of(PRIMARY_ID, id));

                    List<AssistantMessage.ToolCall> list;
                    if (!_StringUtils.hasText(specializedData)) {
                        builder.toolCalls(List.of());
                    } else if ((list = codec.decodeList(specializedData, AssistantMessage.ToolCall.class)) == null) {
                        builder.toolCalls(List.of());
                    } else {
                        builder.toolCalls(list);
                    }
                    message = builder.build();
                }
                break;
                case TOOL: {
                    final String specializedData = record.get(2, String.class);
                    final ToolResponseMessage.Builder builder;
                    builder = ToolResponseMessage
                            .builder()
                            .metadata(Map.of(PRIMARY_ID, id));

                    List<ToolResponseMessage.ToolResponse> list;

                    if (!_StringUtils.hasText(specializedData)) {
                        builder.responses(List.of());
                    } else if ((list = codec.decodeList(specializedData, ToolResponseMessage.ToolResponse.class)) == null) {
                        builder.responses(List.of());
                    } else {
                        builder.responses(list);
                    }
                    message = builder.build();
                }
                break;
                default:
                    throw _Exceptions.unexpectedEnum(record.getNonNull("type", MessageType.class));

            }
            return message;
        };
    }


    @SuppressWarnings("unchecked")
    static abstract class BuilderSupport<B extends BuilderSupport<B, T>, T extends SpringAiChatMemory> {

        private SyncSessionContext sessionContext;

        private SimpleTableMeta<T> tableMeta;

        private NullMode nullMode = NullMode.DEFAULT;

        private LiteralMode literalMode = LiteralMode.DEFAULT;

        public final B sessionContext(SyncSessionContext sessionContext) {
            this.sessionContext = sessionContext;
            return (B) this;
        }

        public final B tableMeta(SimpleTableMeta<T> tableMeta) {
            this.tableMeta = tableMeta;
            return (B) this;
        }


        public final B nullMode(NullMode nullMode) {
            this.nullMode = nullMode;
            return (B) this;
        }

        public final B literalMode(LiteralMode literalMode) {
            this.literalMode = literalMode;
            return (B) this;
        }


        public abstract ArmyChatMemorySupport<T> build();


        final void checkFields() {
            if (this.sessionContext == null || this.nullMode == null || this.literalMode == null || this.tableMeta == null) {
                throw new NullPointerException("sessionContext, nullMode, literalMode, and tableMeta must be set");
            }
        }

    } // BuilderSupport

}
