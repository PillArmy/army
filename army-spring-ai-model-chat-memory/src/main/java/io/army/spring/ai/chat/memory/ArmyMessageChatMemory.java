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
import io.army.generator.snowflake.Snowflakes;
import io.army.result.CurrentRecord;
import io.army.session.SyncSession;
import io.army.session.SyncSessionContext;
import io.army.util._Exceptions;
import io.army.util._StringUtils;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static io.army.criteria.impl.SQLs.AS;


/// Unlike org.springframework.ai.chat.memory.MessageWindowChatMemory, this class does not delete previous messages. in {@link #add(String, List)}.
public final class ArmyMessageChatMemory implements ChatMemory, EnvironmentAware {

    public static final String USER_ID = "chat_memory_user_id";

    private final SyncSessionContext sessionContext;

    private final int maxMessages;

    private final long snowflakeStartTime;

    private final NullMode nullMode;

    private final LiteralMode literalMode;

    private final Function<CurrentRecord, Message> rowFunc;

    private final String crossSessionKey;

    private Environment environment;

    private ArmyMessageChatMemory(Builder builder) {
        this.sessionContext = builder.sessionContext;
        this.maxMessages = builder.maxMessages;
        this.snowflakeStartTime = builder.snowflakeStartTime;
        this.nullMode = builder.nullMode;
        this.literalMode = builder.literalMode;

        this.rowFunc = messageReadFunc(this.sessionContext.sessionFactory().jsonCodec());
        final String factoryName;
        factoryName = this.sessionContext.sessionFactory().name();
        this.crossSessionKey = String.format("army.%s.spring.ai.chat.memory.cross-session", factoryName);
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

        final List<SpringAiChatMemory> domainList = new ArrayList<>(messageCount);
        copyMessageInfo(conversationId, messages, domainList);

        final Insert stmt;
        stmt = SQLs.singleInsert()
                .nullMode(this.nullMode)
                .literalMode(this.literalMode)
                .insertInto(SpringAiChatMemory_.T)
                .defaultValue(SpringAiChatMemory_.userId, SQLs.scalarSubQuery()
                        .select(SpringAiChatConversation_.userId)
                        .from(SpringAiChatConversation_.T, AS, "u")
                        .where(SpringAiChatConversation_.id.equal(SQLs.namedParam(SpringAiChatConversation_.id, SpringAiChatMemory_.CONVERSATION_ID)))
                        .asQuery()
                )
                .values(domainList)
                .asInsert();


        final Function<SyncSession, Void> function;
        function = session -> {
            session.update(stmt);
            return null;
        };
        final String sessionName = getClass().getName() + '.' + "add";
        this.sessionContext.execute(sessionName, false, function);
    }

    @Override
    public List<Message> get(final String conversationId) {
        assertConversationId(conversationId);


        final boolean crossSession;
        crossSession = this.environment.getProperty(this.crossSessionKey, Boolean.class, Boolean.FALSE);

        final Select stmt;
        stmt = SQLs.query()
                .select(SpringAiChatMemory_.userId, SpringAiChatMemory_.content)
                .comma(SpringAiChatMemory_.type, SpringAiChatMemory_.specializedData)
                .from(SpringAiChatMemory_.T, AS, "t")
                .ifCrossJoin(c -> {
                    if (!crossSession) {
                        return;
                    }
                    c.space(SQLs.subQuery()
                            .select(SpringAiChatMemory_.userId)
                            .from(SpringAiChatMemory_.T, AS, "t")
                            .where(SpringAiChatMemory_.conversationId.equal(conversationId))
                            .limit(1)
                            .asQuery()
                    ).as("u");

                })
                .where(wb -> {
                    if (crossSession) {
                        wb.accept(SpringAiChatMemory_.userId.equal(SQLs.refField("u", SpringAiChatMemory_.USER_ID)));
                    } else {
                        wb.accept(SpringAiChatMemory_.conversationId.equal(conversationId));
                    }
                })
                .orderBy(SpringAiChatMemory_.id.desc())
                .limit(this.maxMessages)
                .asQuery();

        final Function<SyncSession, List<Message>> callBack;
        callBack = session -> session.queryRecordList(stmt, this.rowFunc);
        final String sessionName = getClass().getName() + '.' + "get";

        final List<Message> list;
        list = this.sessionContext.execute(sessionName, true, callBack);
        assert list != null;    // here never null, Just suppress non‑null warnings
        return list;
    }

    @Override
    public void clear(String conversationId) {
        assertConversationId(conversationId);

        final Delete stmt;
        stmt = SQLs.singleDelete()
                .deleteFrom(SpringAiChatMemory_.T, AS, "t")
                .where(SpringAiChatMemory_.conversationId.equal(conversationId))
                .asDelete();

        final Function<SyncSession, Void> function;
        function = session -> {
            session.update(stmt);
            return null;
        };

        final String sessionName = getClass().getName() + '.' + "clear";
        this.sessionContext.execute(sessionName, false, function);

    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private void copyMessageInfo(String conversationId, List<Message> messages, List<SpringAiChatMemory> list) {
        final JsonCodec codec = sessionContext.sessionFactory().jsonCodec();

        final Long batchNo;
        if (SpringAiChatMemory_.T.tryField("batchNo") == null) {
            batchNo = null;
        } else if (this.snowflakeStartTime < 0) {
            batchNo = Snowflakes.defaultNext();
        } else {
            batchNo = Snowflakes.next(this.snowflakeStartTime);
        }

        SpringAiChatMemory o;

        for (Message message : messages) {

            Objects.requireNonNull(message, "message cannot be null");

            o = new SpringAiChatMemory()
                    .setConversationId(conversationId)
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


    public static Builder builder(SyncSessionContext sessionContext) {
        return new Builder(sessionContext);
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
                    throw _Exceptions.unexpectedEnum(record.getNonNull(SpringAiChatMemory_.TYPE, MessageType.class));

            }
            return message;
        };
    }


    private static void assertConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
    }


    public static final class Builder {

        private final SyncSessionContext sessionContext;

        private int maxMessages = 20;

        private long snowflakeStartTime = -1;

        private NullMode nullMode = NullMode.DEFAULT;

        private LiteralMode literalMode = LiteralMode.DEFAULT;

        private Builder(SyncSessionContext sessionContext) {
            this.sessionContext = sessionContext;
        }

        public Builder maxMessages(int maxMessages) {
            if (maxMessages < 1) {
                throw new IllegalArgumentException(String.format("%s[%s] %s", "maxMessages", maxMessages, "error"));
            }
            this.maxMessages = maxMessages;
            return this;
        }

        public Builder snowflakeStartTime(long snowflakeStartTime) {
            this.snowflakeStartTime = snowflakeStartTime;
            return this;
        }

        public Builder nullMode(NullMode nullMode) {
            this.nullMode = nullMode;
            return this;
        }

        public Builder literalMode(LiteralMode literalMode) {
            this.literalMode = literalMode;
            return this;
        }

        public ArmyMessageChatMemory build() {
            if (this.sessionContext == null || this.nullMode == null || this.literalMode == null) {
                throw new IllegalStateException("sessionContext, nullMode, and literalMode must be set");
            }
            return new ArmyMessageChatMemory(this);
        }

    } // Builder


}
