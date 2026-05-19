package io.army.example.stock.service.impl;

import io.army.example.stock.dao.StockBaseDao;
import io.army.example.stock.dao.StockChatConversationDao;
import io.army.example.stock.domain.StockChatConversation;
import io.army.example.stock.service.StockChatConversationService;
import io.army.spring.TransactionTemplate;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;
import java.util.Map;

@Service
public class StockChatConversationServiceImpl extends AbstractStockBaseService implements StockChatConversationService {


    private final StockChatConversationDao stockChatConversationDao;

    public StockChatConversationServiceImpl(TransactionTemplate transactionTemplate,
                                            StockChatConversationDao stockChatConversationDao) {
        super(transactionTemplate);
        this.stockChatConversationDao = stockChatConversationDao;
    }


    @Override
    public List<StockChatConversation> queryUserConversation(long userId) {
        final List<StockChatConversation> list;
        list = this.transactionTemplate.execute(Isolation.DEFAULT, true,
                _ -> this.stockChatConversationDao.queryUserConversation(userId)
        );

        return list;
    }

    @Nullable
    @Override
    public Long currentConversationId(long userId) {
        return this.transactionTemplate.execute(Isolation.DEFAULT, true,
                _ -> this.stockChatConversationDao.currentConversationId(userId)
        );
    }


    @Override
    public List<Map<String, Object>> conversationMessageList(long userId, long conversationId) {
        return this.transactionTemplate.execute(Isolation.DEFAULT, true,
                _ -> this.stockChatConversationDao.conversationMessageList(userId, conversationId)
        );
    }

    @Nullable
    @Override
    public Long getUserId(long conversationId) {
        return this.transactionTemplate.execute(Isolation.DEFAULT, true,
                _ -> this.stockChatConversationDao.getUserId(conversationId)
        );
    }

    @Override
    public long deleteConversation(long userId, long conversationId) {
        final Long rowCount;
        rowCount = this.transactionTemplate.execute(Isolation.READ_COMMITTED, false,
                _ -> this.stockChatConversationDao.deleteConversation(userId, conversationId)
        );
        assert rowCount != null; // here never null, Just suppress non‑null warnings
        return rowCount;
    }

    @Override
    public long updateTitle(long userId, long conversationId, String title) {
        final Long rowCount;
        rowCount = this.transactionTemplate.execute(Isolation.READ_COMMITTED, false,
                _ -> this.stockChatConversationDao.updateTitle(userId, conversationId, title)
        );
        assert rowCount != null; // here never null, Just suppress non‑null warnings
        return rowCount;
    }

    @Override
    protected StockBaseDao getDao() {
        return this.stockChatConversationDao;
    }


}
