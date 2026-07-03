package io.army.example.stock.service.impl;

import io.army.example.stock.dao.StockBaseDao;
import io.army.example.stock.dao.StockChatConversationDao;
import io.army.example.stock.domain.StockChatConversation;
import io.army.example.stock.service.StockChatConversationService;
import io.army.spring.sync.TransactionTemplate;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;
import java.util.Map;

/// Transactional service implementation for **stock chat conversation** operations.
///
/// <p>All read operations use read-only transactions, while write operations
/// (`deleteConversation`, `updateTitle`) explicitly specify `READ_COMMITTED` isolation
/// and non-read-only mode.</p>
@Service("stockChatConversationService")
public class StockChatConversationServiceImpl extends AbstractStockBaseService implements StockChatConversationService {


    private final StockChatConversationDao stockChatConversationDao;

    public StockChatConversationServiceImpl(TransactionTemplate transactionTemplate,
                                            StockChatConversationDao stockChatConversationDao) {
        super(transactionTemplate);
        this.stockChatConversationDao = stockChatConversationDao;
    }


    @Override
    public List<StockChatConversation> queryUserConversation(long userId) {
        return this.transactionTemplate.executeNoNull(true,
                _ -> this.stockChatConversationDao.queryUserConversation(userId)
        );
    }

    @Nullable
    @Override
    public Long currentConversationId(long userId) {
        return this.transactionTemplate.execute(true,
                _ -> this.stockChatConversationDao.currentConversationId(userId)
        );
    }


    @Override
    public List<Map<String, Object>> conversationMessageList(long userId, long conversationId) {
        return this.transactionTemplate.executeNoNull(true,
                _ -> this.stockChatConversationDao.conversationMessageList(userId, conversationId)
        );
    }

    @Nullable
    @Override
    public Long getUserId(long conversationId) {
        return this.transactionTemplate.execute(true,
                _ -> this.stockChatConversationDao.getUserId(conversationId)
        );
    }

    @Override
    public long deleteConversation(long userId, long conversationId) {
        return this.transactionTemplate.executeNoNull(Isolation.READ_COMMITTED, false,
                _ -> this.stockChatConversationDao.deleteConversation(userId, conversationId)
        );
    }

    @Override
    public long updateTitle(long userId, long conversationId, String title) {
        return this.transactionTemplate.executeNoNull(Isolation.READ_COMMITTED, false,
                _ -> this.stockChatConversationDao.updateTitle(userId, conversationId, title)
        );
    }

    @Override
    protected StockBaseDao getDao() {
        return this.stockChatConversationDao;
    }


}
