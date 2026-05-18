package io.army.example.stock.dao;

import io.army.example.stock.domain.StockChatConversation;
import org.jspecify.annotations.Nullable;


import java.util.List;
import java.util.Map;

public interface StockChatConversationDao extends StockBaseDao {


    List<StockChatConversation> queryUserConversation(long userId);


    Map<String, Object> currentConversation(long userId);

    @Nullable
    Long getUserId(long conversationId);

    long deleteConversation(long userId, long conversationId);

    long updateTitle(long userId, long conversationId, String title);


}
