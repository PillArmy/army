package io.army.example.stock.dao.impl;

import io.army.criteria.Delete;
import io.army.criteria.Select;
import io.army.criteria.impl.Postgres;
import io.army.criteria.impl.SQLs;
import io.army.example.stock.dao.StockChatConversationDao;
import io.army.example.stock.domain.StockChatConversation;
import io.army.example.stock.domain.StockChatConversation_;
import io.army.mapping.StringType;
import io.army.result.CurrentRecord;
import io.army.session.SyncSessionContext;
import io.army.spring.ai.chat.memory.SpringAiChatMemory_;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
                .comma(SpringAiChatMemory_.type, SpringAiChatMemory_.createTime)
                .from(SpringAiChatMemory_.T, AS, "t")
                .crossJoin(w1)
                .where(SpringAiChatMemory_.conversationId.equal(refField(w1, SpringAiChatMemory_.CONVERSATION_ID)))
                .and(SpringAiChatMemory_.type.in(SQLs.rowLiteral(StringType.INSTANCE, List.of(MessageType.USER, MessageType.ASSISTANT))))
                .orderBy(SpringAiChatMemory_.id)
                .asQuery();

        final long[] conversationIdHolder = new long[1];
        final Function<CurrentRecord, Map<String, Object>> function;
        function = row -> {
            final String content;
            conversationIdHolder[0] = row.getNonNull(0, Long.class);
            content = row.getNonNull(1, String.class);
            final MessageType type;
            type = row.getNonNull(2, MessageType.class);
            final LocalDateTime createTime;
            createTime = row.getNonNull(3, LocalDateTime.class);
            return Map.of("text", content, "messageType", type, "createTime", createTime);
        };

        final List<Map<String, Object>> messageList;
        messageList = this.sessionContext.currentSession().queryRecordList(stmt, function);
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

    @Override
    public void deleteConversation(long userId, long conversationId) {
        final String w1 = "w1";
        final Delete stmt;
        stmt = Postgres.singleDelete()
                .with(w1).as(ws -> ws.deleteFrom(StockChatConversation_.T, AS, "t")
                        .where(StockChatConversation_.userId.equal(userId))
                        .and(StockChatConversation_.id.equal(conversationId))
                        .returning(StockChatConversation_.id)
                        .asReturningDelete()
                ).space()
                .deleteFrom(SpringAiChatMemory_.T, AS, "t")
                .using(w1)
                .where(SpringAiChatMemory_.conversationId.equal(refField(w1, StockChatConversation_.ID)))
                .asDelete();
        this.sessionContext.currentSession().update(stmt);
    }


}
