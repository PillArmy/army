package io.army.example.coder.dao.impl;

import io.army.criteria.Delete;
import io.army.criteria.Select;
import io.army.criteria.Update;
import io.army.criteria.impl.Postgres;
import io.army.criteria.impl.SQLs;
import io.army.example.coder.dao.CoderChatConversationDao;
import io.army.example.coder.domain.CoderChatConversation;
import io.army.example.coder.domain.CoderChatConversation_;
import io.army.example.coder.domain.CoderChatMemory_;
import io.army.session.SyncSessionContext;
import io.army.util.RowMaps;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static io.army.criteria.impl.SQLs.*;

@Repository("coderChatConversationDao")
public class CoderChatConversationDaoImpl extends CoderArmySyncDao implements CoderChatConversationDao {

    public CoderChatConversationDaoImpl(SyncSessionContext sessionContext) {
        super(sessionContext);
    }


    @Override
    public List<CoderChatConversation> queryUserConversation(long userId) {
        final Select stmt;
        stmt = SQLs.query()
                .select("t", PERIOD, CoderChatConversation_.T)
                .from(CoderChatConversation_.T, AS, "t")
                .where(CoderChatConversation_.userId.equal(userId))
                .orderBy(CoderChatConversation_.id.desc())
                .asQuery();
        return this.sessionContext.currentSession()
                .queryObjectList(stmt, CoderChatConversation::new);
    }


    @Nullable
    @Override
    public Long currentConversationId(long userId) {
        final Select stmt;
        stmt = SQLs.query()
                .select(CoderChatMemory_.conversationId)
                .from(CoderChatConversation_.T, AS, "sc")
                .join(CoderChatMemory_.T, AS, "t").on(CoderChatMemory_.conversationId.equal(CoderChatConversation_.id))
                .where(CoderChatConversation_.userId.equal(userId))
                .orderBy(CoderChatMemory_.id.desc())
                .limit(1)
                .asQuery();
        return this.sessionContext.currentSession().queryOne(stmt, Long.class);
    }

    @Override
    public List<Map<String, Object>> conversationMessageList(long userId, long conversationId) {
        final Select stmt;
        stmt = SQLs.query()
                .select(CoderChatMemory_.content.as("text"), CoderChatMemory_.type.as("messageType"))
                .comma(CoderChatMemory_.createTime)
                .from(CoderChatConversation_.T, AS, "sc")
                .join(CoderChatMemory_.T, AS, "t").on(CoderChatMemory_.conversationId.equal(CoderChatConversation_.id))
                .where(CoderChatMemory_.conversationId.equal(conversationId))
                .and(CoderChatConversation_.userId.equal(userId))
                .and(CoderChatMemory_.type.in(SQLs::rowLiteral, List.of(MessageType.USER, MessageType.ASSISTANT)))
                .and(CoderChatMemory_.specializedData.equal("[]"))
                .orderBy(CoderChatMemory_.id)
                .asQuery();
        return this.sessionContext.currentSession().queryObjectList(stmt, RowMaps.hashMapConstructor(3));
    }

    @Nullable
    @Override
    public Long getUserId(long conversationId) {
        final Select stmt;
        stmt = SQLs.query()
                .select(CoderChatConversation_.userId)
                .from(CoderChatConversation_.T, AS, "t")
                .where(CoderChatConversation_.id.equal(conversationId))
                .asQuery();
        return this.sessionContext.currentSession()
                .queryOne(stmt, Long.class);
    }

    @Override
    public long deleteConversation(long userId, long conversationId) {
        final String w1 = "w1";
        final Delete stmt;
        stmt = Postgres.singleDelete()
                .with(w1).as(ws -> ws.deleteFrom(CoderChatConversation_.T, AS, "t")
                        .where(CoderChatConversation_.userId.equal(userId))
                        .and(CoderChatConversation_.id.equal(conversationId))
                        .returning(CoderChatConversation_.id)
                        .asReturningDelete()
                ).space()
                .deleteFrom(CoderChatMemory_.T, AS, "t")
                .using(w1)
                .where(CoderChatMemory_.conversationId.equal(refField(w1, CoderChatConversation_.ID)))
                .asDelete();
        return this.sessionContext.currentSession().update(stmt);
    }

    @Override
    public long updateTitle(long userId, long conversationId, String title) {
        final Update stmt;
        stmt = SQLs.singleUpdate()
                .update(CoderChatConversation_.T, AS, "t")
                .set(CoderChatConversation_.title, title)
                .where(CoderChatConversation_.userId.equal(userId))
                .and(CoderChatConversation_.id.equal(conversationId))
                .asUpdate();
        return this.sessionContext.currentSession().update(stmt);
    }


}
