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
import io.army.criteria.*;
import io.army.criteria.impl.SQLs;
import io.army.generator.snowflake.Snowflake8s;
import io.army.meta.FieldMeta;
import io.army.meta.PrimaryFieldMeta;
import io.army.meta.SimpleTableMeta;
import io.army.pojo.ObjectAccessorFactory;
import io.army.result.CurrentRecord;
import io.army.session.SyncSession;
import io.army.session.SyncSessionContext;
import io.army.util._Exceptions;
import io.army.util._StringUtils;
import org.springframework.ai.chat.memory.ChatMemory;
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


/// Unlike org.springframework.ai.chat.memory.MessageWindowChatMemory, this class does not delete previous messages. in {@link #add(String, List)}.
public final class ArmyMessageChatMemory<T extends SpringAiChatMemory> implements ChatMemory {

    public static final String USER_ID = "chat_memory_user_id";

    private final SyncSessionContext sessionContext;

    private final int maxMessages;

    private final long snowflakeStartTime;

    private final SimpleTableMeta<T> tableMeta;

    private final PrimaryFieldMeta<T> id;

    private final FieldMeta<T> userId;

    private final FieldMeta<T> type;

    private final FieldMeta<T> content;

    private final FieldMeta<T> specializedData;

    private final FieldMeta<T> conversationId;

    private final NullMode nullMode;

    private final LiteralMode literalMode;

    private final Supplier<T> constructor;

    private final Function<CurrentRecord, Message> rowFunc;


    private ArmyMessageChatMemory(Builder<T> builder) {
        this.sessionContext = builder.sessionContext;
        this.maxMessages = builder.maxMessages;
        this.snowflakeStartTime = builder.snowflakeStartTime;
        this.tableMeta = builder.tableMeta;
        this.nullMode = builder.nullMode;
        this.literalMode = builder.literalMode;

        this.id = this.tableMeta.id();
        this.userId = this.tableMeta.field("userId");
        this.type = this.tableMeta.field("type");
        this.content = this.tableMeta.field("content");
        this.specializedData = this.tableMeta.field("specializedData");
        this.conversationId = this.tableMeta.field("conversationId");

        this.constructor = constructorFunc(this.tableMeta);

        this.rowFunc = messageReadFunc(this.sessionContext.sessionFactory().jsonCodec());
    }


    @Override
    public void add(String conversationId, List<Message> messages) {
        // Unlike org.springframework.ai.chat.memory.MessageWindowChatMemory, this class does not delete previous messages.
        assertConversationId(conversationId);
        Assert.notNull(messages, "messages cannot be null");

        final int messageCount = messages.size();
        if (messageCount == 0) {
            return;
        }

        final List<T> domainList = new ArrayList<>(messageCount);
        copyMessageInfo(conversationId, messages, domainList);

        final Insert stmt;
        stmt = SQLs.singleInsert()
                .nullMode(this.nullMode)
                .literalMode(this.literalMode)
                .insertInto(this.tableMeta)
                .values(domainList)
                .asInsert();

        final Consumer<SyncSession> function;
        function = session -> session.update(stmt);
        final String sessionName = getClass().getName() + '.' + "add";
        this.sessionContext.executeVoid(sessionName, false, function);
    }


    @Override
    public List<Message> get(final String conversationId) {
        assertConversationId(conversationId);

        final Select stmt;
        stmt = SQLs.query()
                .select(this.userId, this.content)
                .comma(this.type, this.specializedData)
                .from(this.tableMeta, AS, "t")
                .where(this.conversationId.equal(conversationId))
                .orderBy(this.id.desc())
                .limit(this.maxMessages)
                .asQuery();

        final Function<SyncSession, List<Message>> callBack;
        callBack = session -> session.queryRecordList(stmt, this.rowFunc);
        final String sessionName = getClass().getName() + '.' + "get";
        return this.sessionContext.executeNotNull(sessionName, true, callBack);
    }

    @Override
    public void clear(String conversationId) {
        assertConversationId(conversationId);

        final Delete stmt;
        stmt = SQLs.singleDelete()
                .deleteFrom(this.tableMeta, AS, "t")
                .where(this.conversationId.equal(conversationId))
                .asDelete();

        final Consumer<SyncSession> function;
        function = session -> session.update(stmt);

        final String sessionName = getClass().getName() + '.' + "clear";
        this.sessionContext.executeVoid(sessionName, false, function);
    }


    private void copyMessageInfo(String conversationId, List<Message> messages, List<T> list) {
        final JsonCodec codec = sessionContext.sessionFactory().jsonCodec();

        final Long batchNo;
        if (this.tableMeta.tryField("batchNo") == null) {
            batchNo = null;
        } else if (this.snowflakeStartTime < 0) {
            batchNo = Snowflake8s.defaultNext();
        } else {
            batchNo = Snowflake8s.next(this.snowflakeStartTime);
        }

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

            if (batchNo != null) {
                o.setBatchNo(batchNo);
            }

            list.add(o);
        } // loop

    }


    public static <T extends SpringAiChatMemory> Builder<T> builder(SyncSessionContext sessionContext, SimpleTableMeta<T> tableMeta) {
        return new Builder<>(sessionContext, tableMeta);
    }


    /// {@link io.army.criteria.Selection} order :
    /// 0. userId
    /// 1. content
    /// 2. type
    /// 3. specializedData
    ///
    public static Function<CurrentRecord, Message> messageReadFunc(final JsonCodec codec) {
        return record -> {
            final String content, userId;
            userId = record.get(0, String.class);
            content = record.getNonNull(1, String.class);

            final boolean userIdHasText = _StringUtils.hasText(userId);

            final Message message;
            switch (record.getNonNull(2, MessageType.class)) {
                case USER: {
                    if (userIdHasText) {
                        message = UserMessage.builder()
                                .metadata(Map.of(USER_ID, userId))
                                .text(content)
                                .build();
                    } else {
                        message = new UserMessage(content);
                    }
                }
                break;
                case SYSTEM: {
                    if (userIdHasText) {
                        message = SystemMessage.builder()
                                .metadata(Map.of(USER_ID, userId))
                                .text(content)
                                .build();
                    } else {
                        message = new SystemMessage(content);
                    }
                }
                break;
                case ASSISTANT: {
                    final String specializedData = record.get(3, String.class);
                    final AssistantMessage.Builder builder;
                    builder = AssistantMessage.builder()
                            .content(content);

                    if (userIdHasText) {
                        builder.properties(Map.of(USER_ID, userId));
                    }

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
                    final String specializedData = record.get(3, String.class);
                    final ToolResponseMessage.Builder builder;
                    builder = ToolResponseMessage.builder();

                    if (userIdHasText) {
                        builder.metadata(Map.of(USER_ID, userId));
                    }

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
    private static <T extends SpringAiChatMemory> Supplier<T> constructorFunc(SimpleTableMeta<T> tableMeta) {
        Supplier<T> constructor;
        if (tableMeta.javaType() == SpringAiChatMemory.class) {
            final Supplier<SpringAiChatMemory> func = SpringAiChatMemory::new;
            constructor = (Supplier<T>) func;
        } else {
            constructor = ObjectAccessorFactory.pojoConstructor(tableMeta.javaType());
        }
        return constructor;
    }

    private static void assertConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
    }


    public static final class Builder<T extends SpringAiChatMemory> {

        private final SyncSessionContext sessionContext;

        private final SimpleTableMeta<T> tableMeta;

        private int maxMessages = 20;

        private long snowflakeStartTime = -1;

        private NullMode nullMode = NullMode.DEFAULT;

        private LiteralMode literalMode = LiteralMode.DEFAULT;

        private Builder(SyncSessionContext sessionContext, SimpleTableMeta<T> tableMeta) {
            this.sessionContext = sessionContext;
            this.tableMeta = tableMeta;
        }

        public Builder<T> maxMessages(int maxMessages) {
            if (maxMessages < 1) {
                throw new IllegalArgumentException(String.format("%s[%s] %s", "maxMessages", maxMessages, "error"));
            }
            this.maxMessages = maxMessages;
            return this;
        }

        public Builder<T> snowflakeStartTime(long snowflakeStartTime) {
            this.snowflakeStartTime = snowflakeStartTime;
            return this;
        }

        public Builder<T> nullMode(NullMode nullMode) {
            this.nullMode = nullMode;
            return this;
        }

        public Builder<T> literalMode(LiteralMode literalMode) {
            this.literalMode = literalMode;
            return this;
        }

        public ArmyMessageChatMemory<T> build() {
            if (this.sessionContext == null || this.nullMode == null || this.literalMode == null || this.tableMeta == null) {
                throw new IllegalStateException("sessionContext, nullMode, and literalMode must be set");
            }
            return new ArmyMessageChatMemory<>(this);
        }

    } // Builder


}
