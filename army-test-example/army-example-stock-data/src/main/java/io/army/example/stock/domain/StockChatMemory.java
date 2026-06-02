package io.army.example.stock.domain;

import io.army.annotation.Index;
import io.army.annotation.Table;
import io.army.spring.ai.chat.memory.SpringAiChatMemory;

/// Domain entity for **short-term chat memory** storage (conversation message history).
///
/// <p>Maps to the `stock_chat_memory` table. Extends `SpringAiChatMemory` to integrate
/// with Spring AI's `ChatMemory` abstraction for maintaining conversational context.</p>
///
/// <p>This table is marked as **immutable** — memory records are append-only.
/// A non-unique index on `conversationId` enables efficient retrieval of messages
/// within a specific conversation.</p>
@Table(name = "stock_chat_memory",
        indexes = {
                @Index(name = "${DEFAULT_VALUE}", fieldList = {"conversationId"})
        },
        immutable = true,
        comment = "股票聊天短期记忆")
public class StockChatMemory extends SpringAiChatMemory {


}
