package io.army.example.stock.service;

/// Service interface for **upload record** business operations.
///
/// <p>Provides a transactional method to mark file uploads as complete.</p>
public interface UploadRecordService extends StockBaseService {

    long uploadComplete(long id, String fileHast);


}
