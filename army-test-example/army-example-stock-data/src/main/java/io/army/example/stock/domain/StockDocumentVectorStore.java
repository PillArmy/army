package io.army.example.stock.domain;

import io.army.annotation.Index;
import io.army.annotation.IndexField;
import io.army.annotation.Table;

import io.army.spring.ai.vectorstore.SpringAiVectorStore;

@Table(name = "stock_document_vector_store",
        indexes = {
                @Index(name = "${DEFAULT_VALUE}", type = "hnsw", fields = @IndexField(name = "embedding", opclass = "vector_l2_ops")),
                @Index(name = "${DEFAULT_VALUE}", unique = true, fieldList = "documentId"),
                @Index(name = "${DEFAULT_VALUE}", type = "gin", fieldList = "metadata")
        },
        comment = "股票文档向量存储")
public class StockDocumentVectorStore extends SpringAiVectorStore {


}
