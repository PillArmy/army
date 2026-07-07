package io.army.example.coder.dao;

import io.army.example.coder.domain.CoderChatConversation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface CoderChatConversationDao extends CoderSyncDao {

    List<CoderChatConversation> queryUserConversation(long userId);


    @Nullable
    Long currentConversationId(long userId);

    List<Map<String, Object>> conversationMessageList(long userId, long conversationId);


    @Nullable
    Long getUserId(long conversationId);

    long deleteConversation(long userId, long conversationId);

    long updateTitle(long userId, long conversationId, String title);

}
