package io.army.example.stock.domain;

import io.army.annotation.Index;
import io.army.annotation.IndexField;
import io.army.annotation.Table;
import io.army.spring.ai.vectorstore.SpringAiChatVectorStore;

@Table(name = "stock_chat_vector_store",
        indexes = {
                @Index(name = "${DEFAULT_NAME}", type = "hnsw", fields = @IndexField(name = "embedding", opclass = "vector_l2_ops")),
                @Index(name = "${DEFAULT_NAME}", fieldList = "conversationId")
        },
        comment = "股票聊天长期记忆向量存储")
public class StockChatVectorStore extends SpringAiChatVectorStore {


}
