package io.army.example.stock.dao.impl;

import io.army.criteria.Delete;
import io.army.criteria.Select;
import io.army.criteria.Update;
import io.army.criteria.impl.Postgres;
import io.army.criteria.impl.SQLs;
import io.army.example.stock.dao.StockChatConversationDao;
import io.army.example.stock.domain.StockChatConversation;
import io.army.example.stock.domain.StockChatConversation_;
import io.army.session.SyncSessionContext;
import io.army.spring.ai.chat.memory.SpringAiChatMemory_;
import io.army.util.RowMaps;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static io.army.criteria.impl.SQLs.*;

@Repository("stockChatConversationDao")
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


    @Nullable
    @Override
    public Long currentConversationId(long userId) {
        final Select stmt;
        stmt = SQLs.query()
                .select(SpringAiChatMemory_.conversationId)
                .from(StockChatConversation_.T, AS, "sc")
                .join(SpringAiChatMemory_.T, AS, "t").on(SpringAiChatMemory_.conversationId.equal(StockChatConversation_.id))
                .where(StockChatConversation_.userId.equal(userId))
                .orderBy(SpringAiChatMemory_.id.desc())
                .limit(1)
                .asQuery();
        return this.sessionContext.currentSession()
                .queryOne(stmt, Long.class);
    }

    @Override
    public List<Map<String, Object>> conversationMessageList(long userId, long conversationId) {
        final Select stmt;
        stmt = SQLs.query()
                .select(SpringAiChatMemory_.content.as("text"), SpringAiChatMemory_.type.as("messageType"))
                .comma(SpringAiChatMemory_.createTime)
                .from(StockChatConversation_.T, AS, "sc")
                .join(SpringAiChatMemory_.T, AS, "t").on(SpringAiChatMemory_.conversationId.equal(StockChatConversation_.id))
                .where(SpringAiChatMemory_.conversationId.equal(conversationId))
                .and(StockChatConversation_.userId.equal(userId))
                .and(SpringAiChatMemory_.type.in(SQLs::rowLiteral, List.of(MessageType.USER, MessageType.ASSISTANT)))
                .orderBy(SpringAiChatMemory_.id)
                .asQuery();
        return this.sessionContext.currentSession()
                .queryObjectList(stmt, RowMaps.hashMapConstructor(3));
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
    public long deleteConversation(long userId, long conversationId) {
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
        return this.sessionContext.currentSession().update(stmt);
    }

    @Override
    public long updateTitle(long userId, long conversationId, String title) {
        final Update stmt;
        stmt = SQLs.singleUpdate()
                .update(StockChatConversation_.T, AS, "t")
                .set(StockChatConversation_.title, title)
                .where(StockChatConversation_.userId.equal(userId))
                .and(StockChatConversation_.id.equal(conversationId))
                .asUpdate();
        return this.sessionContext.currentSession().update(stmt);
    }
}
