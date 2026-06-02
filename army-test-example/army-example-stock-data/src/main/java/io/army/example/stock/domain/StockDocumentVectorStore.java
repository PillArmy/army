package io.army.example.stock.domain;

import io.army.annotation.Index;
import io.army.annotation.IndexField;
import io.army.annotation.Table;

import io.army.spring.ai.vectorstore.SpringAiVectorStore;

/// Domain entity for **document vector store** — stores embeddings of uploaded documents.
///
/// <p>Maps to the `stock_document_vector_store` table. Extends `SpringAiVectorStore`
/// to enable semantic search over uploaded document content using L2 (Euclidean) distance.</p>
///
/// ### Indexes
/// - **HNSW** index on `embedding` with `vector_l2_ops` for ANN similarity search
/// - Unique index on `documentId` for deduplication
/// - **GIN** index on `metadata` for JSONB metadata filtering
@Table(name = "stock_document_vector_store",
        indexes = {
                @Index(name = "${DEFAULT_VALUE}", type = "hnsw", fields = @IndexField(name = "embedding", opclass = "vector_l2_ops")),
                @Index(name = "${DEFAULT_VALUE}", unique = true, fieldList = "documentId"),
                @Index(name = "${DEFAULT_VALUE}", type = "gin", fieldList = "metadata")
        },
        comment = "股票文档向量存储")
public class StockDocumentVectorStore extends SpringAiVectorStore {


}
