package io.army.example.stock.domain;

import io.army.annotation.Index;
import io.army.annotation.Table;
import io.army.spring.ai.chat.memory.SpringAiChatMemory;

@Table(name = "stock_chat_memory",
        indexes = {
                @Index(name = "${DEFAULT_NAME}", fieldList = {"conversationId"})
        },
        immutable = true,
        comment = "股票聊天短期记忆")
public class StockChatMemory extends SpringAiChatMemory {


}
