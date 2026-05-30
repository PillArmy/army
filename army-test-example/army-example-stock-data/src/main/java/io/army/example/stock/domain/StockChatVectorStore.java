package io.army.example.stock.domain;

import io.army.annotation.Index;
import io.army.annotation.IndexField;
import io.army.annotation.Table;
import io.army.spring.ai.vectorstore.SpringAiChatVectorStore;

@Table(name = "stock_chat_vector_store",
        indexes = {
                @Index(name = "${DEFAULT_VALUE}", type = "hnsw", fields = @IndexField(name = "embedding", opclass = "vector_cosine_ops")),
                @Index(name = "${DEFAULT_VALUE}", fieldList = "conversationId"),
                @Index(name = "${DEFAULT_VALUE}", type = "gin", fieldList = "metadata")
        },
        comment = "股票聊天长期记忆向量存储")
public class StockChatVectorStore extends SpringAiChatVectorStore {


}
