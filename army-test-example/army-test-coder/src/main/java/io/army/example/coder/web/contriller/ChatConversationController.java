package io.army.example.coder.web.contriller;

import io.army.example.coder.domain.CoderChatConversation;
import io.army.example.coder.service.CoderChatConversationService;
import io.army.example.coder.utils.StringUtils;
import io.army.example.coder.web.DataUtils;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController("chatConversationController")
@RequestMapping("/api/chat/conversation")
public class ChatConversationController {


    private static final Logger LOG = LoggerFactory.getLogger(ChatConversationController.class);

    private final ChatClient chatClient;

    private final CoderChatConversationService stockChatConversationService;


    public ChatConversationController(@Qualifier("coderChatClient") ChatClient chatClient, CoderChatConversationService coderChatConversationService) {
        this.chatClient = chatClient;
        this.stockChatConversationService = coderChatConversationService;
    }

    @GetMapping("list")
    public Map<String, Object> conversationList(@CookieValue(value = "AuthToken") long userId) {
        final List<CoderChatConversation> list;
        list = this.stockChatConversationService.queryUserConversation(userId);

        List<Map<String, Object>> rowList;
        if (list.isEmpty()) {
            rowList = List.of();
        } else {
            rowList = new ArrayList<>(list.size());
            Map<String, Object> row;
            for (CoderChatConversation o : list) {
                row = Map.of("id", o.getId(), "title", o.getTitle(), "createTime", o.getCreateTime());
                rowList.add(row);
            }
        }
        return DataUtils.ok("list", rowList);
    }

    @GetMapping("currentId")
    public Map<String, Object> currentConversationId(@CookieValue(value = "AuthToken") long userId) {
        final Long conversationId = this.stockChatConversationService.currentConversationId(userId);
        return DataUtils.ok("conversationId", conversationId);
    }


    @GetMapping("{conversationId}/messages")
    public Map<String, Object> getConversation(@CookieValue(value = "AuthToken") long userId,
                                               @PathVariable("conversationId") long conversationId) {
        final List<Map<String, Object>> list;
        list = this.stockChatConversationService.conversationMessageList(userId, conversationId);
        return DataUtils.ok("list", list);
    }

    @PostMapping("create")
    public Map<String, Object> createConversation(@CookieValue(value = "AuthToken") long userId,
                                                  @RequestParam("content") @NotBlank String content) {

        final CoderChatConversation conversation = new CoderChatConversation()
                .setUserId(userId)
                .setTitle(StringUtils.myTruncate(content, 30));

        this.stockChatConversationService.save(conversation);

        final Long conversationId = conversation.getId();
        if (conversationId == null) {
            return DataUtils.serverError(500, "Failed to create conversation");
        }
        return DataUtils.ok(Map.of("conversationId", conversationId, "content", content));
    }


    @PostMapping("talk/{conversationId}")
    public Flux<String> talk(@CookieValue(value = "AuthToken") long userId,
                             @PathVariable("conversationId") long conversationId,
                             @RequestParam("content") @NotBlank String content) {

        final Long memoryUserId;
        memoryUserId = this.stockChatConversationService.getUserId(conversationId);
        if (memoryUserId == null || userId != memoryUserId) {
            return Flux.error(new RuntimeException("userId not match"));
        }

        final List<Message> messageList;
        final int index;
        if (content.charAt(0) == '/' && (index = content.indexOf(':')) > -1) {
            final SystemMessage systemMessage;
            systemMessage = new SystemMessage(content.substring(1, index));
            messageList = List.of(systemMessage, new UserMessage(content.substring(index + 1)));
        } else {
            messageList = List.of(new UserMessage(content));
        }
        return this.chatClient.prompt(new Prompt(messageList))
                .advisors(s -> s.param(ChatMemory.CONVERSATION_ID, Long.toString(conversationId)))
                .stream()
                .content();

    }

    @PostMapping("delete/{conversationId}")
    public Map<String, Object> deleteConversation(@CookieValue(value = "AuthToken") long userId,
                                                  @PathVariable("conversationId") long conversationId) {
        this.stockChatConversationService.deleteConversation(userId, conversationId);
        return DataUtils.ok();
    }

    @PostMapping("updateTitle/{conversationId}")
    public Map<String, Object> updateTitle(@CookieValue(value = "AuthToken") long userId,
                                           @PathVariable("conversationId") long conversationId,
                                           @RequestParam("title") String title) {

        final Map<String, Object> map;
        if (!StringUtils.hasText(title) || title.length() > 30) {
            map = DataUtils.serverError(400, "Invalid title");
        } else if (this.stockChatConversationService.updateTitle(userId, conversationId, title) > 0) {
            map = DataUtils.ok();
        } else {
            map = DataUtils.serverError(400, "无此会话");
        }
        return map;

    }


}
