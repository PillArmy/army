package io.army.example.coder.domain;

import io.army.annotation.Index;
import io.army.annotation.IndexField;
import io.army.annotation.Table;
import io.army.spring.ai.vectorstore.SpringAiChatVectorStore;

/// Domain entity for **chat vector store** — long-term memory backed by vector embeddings.
///
/// <p>Maps to the `stock_chat_vector_store` table. Extends `SpringAiChatVectorStore`
/// to persist conversation embeddings with cosine similarity search.</p>
///
/// ### Indexes
/// - **HNSW** index on `embedding` with `vector_cosine_ops` for ANN similarity search
/// - Non-unique index on `conversationId` for conversation-scoped queries
/// - **GIN** index on `metadata` for JSONB metadata filtering
@Table(name = "coder_chat_vector_store",
        indexes = {
                @Index(name = "${DEFAULT_VALUE}", type = "hnsw", fields = @IndexField(name = "embedding", opclass = "vector_cosine_ops")),
                @Index(name = "${DEFAULT_VALUE}", fieldList = "conversationId"),
                @Index(name = "${DEFAULT_VALUE}", type = "gin", fieldList = "metadata")
        },
        comment = "程序员聊天长期记忆向量存储")
public class CoderChatVectorStore extends SpringAiChatVectorStore {


}
