package io.army.example.stock.dao.impl;

import io.army.codec.JsonCodec;
import io.army.criteria.Select;
import io.army.criteria.impl.SQLs;
import io.army.example.stock.dao.StockChatConversationDao;
import io.army.example.stock.domain.StockChatConversation;
import io.army.example.stock.domain.StockChatConversation_;
import io.army.mapping.StringType;
import io.army.result.CurrentRecord;
import io.army.session.SyncSession;
import io.army.session.SyncSessionContext;
import io.army.spring.ai.chat.memory.SpringAiChatMemory_;
import io.army.util._Exceptions;
import io.army.util._StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.army.criteria.impl.SQLs.*;

@Repository
public class StockChatConversationDaoImpl extends ArmyStockBaseDao implements StockChatConversationDao {

    public StockChatConversationDaoImpl(SyncSessionContext sessionContext) {
        super(sessionContext);
    }

    @Override
    public List<StockChatConversation> queryUserConversation(long userId) {
        final Select stmt;
        stmt = SQLs.query()
                .select("t", PERIOD, StockChatConversation_.T)
                .from(StockChatConversation_.T, AS, "t")
                .where(StockChatConversation_.userId.equal(userId))
                .orderBy(StockChatConversation_.id.desc())
                .asQuery();
        return this.sessionContext.currentSession()
                .queryObjectList(stmt, StockChatConversation::new);
    }

    @Override
    public Map<String, Object> currentConversation(long userId) {
        final String w1 = "w1";
        final Select stmt;
        stmt = SQLs.query()
                .with(w1).as(ws -> ws.select(SpringAiChatMemory_.conversationId)
                        .from(SpringAiChatMemory_.T, AS, "t")
                        .where(SpringAiChatMemory_.userId.equal(userId))
                        .orderBy(SpringAiChatMemory_.id.desc())
                        .limit(1)
                        .asQuery()
                ).space()
                .select(SpringAiChatMemory_.conversationId, SpringAiChatMemory_.content)
                .comma(SpringAiChatMemory_.type, SpringAiChatMemory_.specializedData)
                .from(SpringAiChatMemory_.T, AS, "t")
                .crossJoin(w1)
                .where(SpringAiChatMemory_.conversationId.equal(refField(w1, SpringAiChatMemory_.CONVERSATION_ID)))
                .and(SpringAiChatMemory_.type.in(SQLs.rowLiteral(StringType.INSTANCE, List.of(MessageType.USER, MessageType.ASSISTANT))))
                .orderBy(SpringAiChatMemory_.id)
                .asQuery();

        final SyncSession session = this.sessionContext.currentSession();

        final JsonCodec jsonCodec = session.jsonCodec();

        final long[] conversationIdHolder = new long[1];
        final Function<CurrentRecord, Message> function;
        function = row -> {
            final String content;
            conversationIdHolder[0] = row.getNonNull(0, Long.class);
            content = row.getNonNull(1, String.class);

            final MessageType type;
            type = row.getNonNull(2, MessageType.class);
            final Message message;
            switch (type) {
                case USER:
                    message = new UserMessage(content);
                    break;
                case ASSISTANT: {
                    final String specializedData = row.get(3, String.class);
                    final AssistantMessage.Builder builder;
                    builder = AssistantMessage.builder()
                            .content(content);

                    List<AssistantMessage.ToolCall> list;
                    if (!_StringUtils.hasText(specializedData)) {
                        builder.toolCalls(List.of());
                    } else if ((list = jsonCodec.decodeList(specializedData, AssistantMessage.ToolCall.class)) == null) {
                        builder.toolCalls(List.of());
                    } else {
                        builder.toolCalls(list);
                    }
                    message = builder.build();
                }
                break;
                default:
                    throw _Exceptions.unexpectedEnum(type);

            }
            return message;
        };

        final List<Message> messageList;
        messageList = session.queryRecordList(stmt, function);
        if (messageList.isEmpty()) {
            return Map.of();
        }
        return Map.of(SpringAiChatMemory_.CONVERSATION_ID, conversationIdHolder[0], "messageList", messageList);
    }

    @Nullable
    @Override
    public Long getUserId(long conversationId) {
        final Select stmt;
        stmt = SQLs.query()
                .select(StockChatConversation_.userId)
                .from(StockChatConversation_.T, AS, "t")
                .where(StockChatConversation_.id.equal(conversationId))
                .asQuery();
        return this.sessionContext.currentSession()
                .queryOne(stmt, Long.class);
    }
}
