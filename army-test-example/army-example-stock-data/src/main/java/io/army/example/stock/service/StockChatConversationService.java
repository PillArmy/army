package io.army.example.stock.service;

import io.army.example.stock.domain.StockChatConversation;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface StockChatConversationService extends StockBaseService {


    List<StockChatConversation> queryUserConversation(long userId);

    @Nullable
    Long currentConversationId(long userId);

    List<Map<String, Object>> conversationMessageList(long userId, long conversationId);


    @Nullable
    Long getUserId(long conversationId);

    long deleteConversation(long userId, long conversationId);

    long updateTitle(long userId, long conversationId, String title);

}
