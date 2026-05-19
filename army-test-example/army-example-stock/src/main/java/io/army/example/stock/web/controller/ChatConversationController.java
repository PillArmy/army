package io.army.example.stock.web.controller;

import io.army.example.stock.StringUtils;
import io.army.example.stock.domain.StockChatConversation;
import io.army.example.stock.domain.UploadRecord;
import io.army.example.stock.service.DocumentService;
import io.army.example.stock.service.StockChatConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@RestController
@RequestMapping("/api/chat/conversation")
public class ChatConversationController {

    private static final Logger LOG = LoggerFactory.getLogger(ChatConversationController.class);

    private final ChatClient chatClient;

    private final StockChatConversationService stockChatConversationService;

    private final DocumentService documentService;

    public ChatConversationController(ChatClient chatClient, StockChatConversationService stockChatConversationService,
                                      DocumentService documentService) {
        this.chatClient = chatClient;
        this.stockChatConversationService = stockChatConversationService;
        this.documentService = documentService;
    }

    @GetMapping("list")
    public Map<String, Object> conversationList(@CookieValue(value = "AuthToken") long userId) {
        final List<StockChatConversation> list;
        list = this.stockChatConversationService.queryUserConversation(userId);

        List<Map<String, Object>> rowList;
        if (list.isEmpty()) {
            rowList = List.of();
        } else {
            rowList = new ArrayList<>(list.size());
            Map<String, Object> row;
            for (StockChatConversation o : list) {
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
                                                  @RequestParam("content") String content) {

        final StockChatConversation conversation = new StockChatConversation()
                .setUserId(userId)
                .setTitle(StringUtils.truncate(content, 30))
                .setFirstContent(content);

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
                             @RequestParam("content") String content) {

        final Long memoryUserId;
        memoryUserId = this.stockChatConversationService.getUserId(conversationId);
        if (memoryUserId == null || userId != memoryUserId) {
            return Flux.error(new RuntimeException("userId not match"));
        }

        return this.chatClient.prompt(content)
                .advisors(s -> s.param(ChatMemory.CONVERSATION_ID, Long.toString(conversationId)))
                .stream()
                .content();

    }

    @PostMapping("delete/{conversationId}")
    public Map<String, Object> deleteConversation(@CookieValue(value = "AuthToken") long userId,
                                                  @PathVariable("conversationId") long conversationId) {
        final long rowCount;
        rowCount = this.stockChatConversationService.deleteConversation(userId, conversationId);
        if (rowCount > 0) {
            return DataUtils.ok();
        }
        return DataUtils.serverError(400, "无此会话");
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


    @PostMapping("{conversationId}/upload/documents")
    public WebAsyncTask<Map<String, Object>> uploadDocument(@CookieValue(value = "AuthToken") long userId,
                                                            @PathVariable("conversationId") long conversationId,
                                                            @RequestParam("files") MultipartFile[] fileArray) {

        final Callable<Map<String, Object>> callable = () -> {
            try {
                final Path[] pathArray = new Path[fileArray.length];
                MultipartFile file;
                final List<UploadRecord> recordList = new ArrayList<>(fileArray.length);
                UploadRecord o;
                String fieldName, suffix;
                for (int i = 0, peroidIndex; i < fileArray.length; i++) {
                    file = fileArray[i];
                    fieldName = file.getOriginalFilename();
                    suffix = file.getContentType();
                    if (suffix == null) {
                        peroidIndex = fieldName.lastIndexOf('.');
                        if (peroidIndex > 0) {
                            suffix = fieldName.substring(peroidIndex);
                        } else {
                            suffix = "document";
                        }
                    }
                    pathArray[i] = Files.createTempFile("stock_chat_doc", '.' + suffix);

                    o = new UploadRecord();
                    o.userId = userId;
                    o.conversationId = conversationId;
                    o.fileName = file.getOriginalFilename();
                    o.filePath = pathArray[i].toAbsolutePath().toString();
                    recordList.add(o);
                }

                this.stockChatConversationService.batchSave(recordList);

                Path path;
                for (int i = 0; i < fileArray.length; i++) {
                    file = fileArray[i];
                    path = pathArray[i];

                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                    o = recordList.get(i);
                    o.storePath = path;

                    this.stockChatConversationService.updateField(UploadRecord.class, o.id, "updateCompleteTime",
                            LocalDateTime.now());

                }

                this.documentService.storeDocuments(recordList);
                return DataUtils.ok();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        return new WebAsyncTask<>(callable);
    }

}
