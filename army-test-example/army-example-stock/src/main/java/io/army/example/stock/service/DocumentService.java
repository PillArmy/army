package io.army.example.stock.service;

import io.army.example.stock.domain.UploadRecord;

import java.util.List;

/// Service interface for **document vectorization and storage** operations.
///
/// <p>Processes uploaded documents by converting them into vector embeddings
/// and storing them in the document vector store for RAG-based retrieval.</p>
public interface DocumentService extends StockBaseService {


    void storeDocuments(List<UploadRecord> recordList);

}
