package io.army.example.stock.dao;

/// DAO interface for **upload record** data access operations.
///
/// <p>Provides the `uploadComplete` method to mark an upload as finished
/// by setting the completion time and file hash.</p>
public interface UploadRecordDao extends StockBaseDao {

    long uploadComplete(long id, String fileHast);


}
