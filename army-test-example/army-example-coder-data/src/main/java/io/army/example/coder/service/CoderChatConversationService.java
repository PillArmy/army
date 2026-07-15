package io.army.example.coder.service;

import io.army.example.coder.domain.CoderChatConversation;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface CoderChatConversationService extends CoderBaseService {

    List<CoderChatConversation> queryUserConversation(long userId);

    @Nullable
    Long currentConversationId(long userId);

    List<Map<String, Object>> conversationMessageList(long userId, long conversationId);


    @Nullable
    Long getUserId(long conversationId);

    long deleteConversation(long userId, long conversationId);

    long updateTitle(long userId, long conversationId, String title);


}
