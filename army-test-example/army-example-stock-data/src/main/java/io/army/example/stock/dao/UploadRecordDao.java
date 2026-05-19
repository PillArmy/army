package io.army.example.stock.dao;

public interface UploadRecordDao extends StockBaseDao {

    long uploadComplete(long id, String fileHast);


}
