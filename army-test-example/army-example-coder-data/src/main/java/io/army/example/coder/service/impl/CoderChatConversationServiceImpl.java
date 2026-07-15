package io.army.example.coder.service.impl;

import io.army.example.coder.dao.CoderChatConversationDao;
import io.army.example.coder.dao.CoderSyncDao;
import io.army.example.coder.domain.CoderChatConversation;
import io.army.example.coder.service.CoderChatConversationService;
import io.army.spring.sync.TransactionTemplate;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;
import java.util.Map;

@Service("coderChatConversationService")
public class CoderChatConversationServiceImpl extends ArmyCoderBaseService implements CoderChatConversationService {

    private final CoderChatConversationDao dao;

    public CoderChatConversationServiceImpl(TransactionTemplate transactionTemplate, CoderChatConversationDao dao) {
        super(transactionTemplate);
        this.dao = dao;
    }

    @Override
    public List<CoderChatConversation> queryUserConversation(long userId) {
        return this.transactionTemplate.executeNoNull(true,
                _ -> this.dao.queryUserConversation(userId)
        );
    }

    @Nullable
    @Override
    public Long currentConversationId(long userId) {
        return this.transactionTemplate.execute(true,
                _ -> this.dao.currentConversationId(userId)
        );
    }


    @Override
    public List<Map<String, Object>> conversationMessageList(long userId, long conversationId) {
        return this.transactionTemplate.executeNoNull(true,
                _ -> this.dao.conversationMessageList(userId, conversationId)
        );
    }

    @Nullable
    @Override
    public Long getUserId(long conversationId) {
        return this.transactionTemplate.execute(true,
                _ -> this.dao.getUserId(conversationId)
        );
    }

    @Override
    public long deleteConversation(long userId, long conversationId) {
        return this.transactionTemplate.executeNoNull(Isolation.READ_COMMITTED, false,
                _ -> this.dao.deleteConversation(userId, conversationId)
        );
    }

    @Override
    public long updateTitle(long userId, long conversationId, String title) {
        return this.transactionTemplate.executeNoNull(Isolation.READ_COMMITTED, false,
                _ -> this.dao.updateTitle(userId, conversationId, title)
        );
    }

    @Override
    protected CoderSyncDao getDao() {
        return this.dao;
    }
}
