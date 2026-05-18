package io.army.example.stock.service.impl;

import io.army.example.stock.dao.StockBaseDao;
import io.army.example.stock.dao.StockChatConversationDao;
import io.army.example.stock.domain.StockChatConversation;
import io.army.example.stock.service.StockChatConversationService;
import io.army.spring.ArmyTransactionTemplate;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;
import java.util.Map;

@Service
public class StockChatConversationServiceImpl extends AbstractStockBaseService implements StockChatConversationService {


    private final StockChatConversationDao stockChatConversationDao;

    public StockChatConversationServiceImpl(ArmyTransactionTemplate transactionTemplate,
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

    @Override
    public Map<String, Object> currentConversation(long userId) {
        final Map<String, Object> map;
        map = this.transactionTemplate.execute(Isolation.DEFAULT, true,
                _ -> this.stockChatConversationDao.currentConversation(userId)
        );
        return map;
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
        return this.transactionTemplate.execute(Isolation.READ_COMMITTED, false,
                _ -> this.stockChatConversationDao.deleteConversation(userId, conversationId)
        );
    }

    @Override
    public long updateTitle(long userId, long conversationId, String title) {
        return this.transactionTemplate.execute(Isolation.READ_COMMITTED, false,
                _ -> this.stockChatConversationDao.updateTitle(userId, conversationId, title)
        );

    }

    @Override
    protected StockBaseDao getDao() {
        return this.stockChatConversationDao;
    }


}
