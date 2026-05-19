package io.army.example.stock.service;

import io.army.example.stock.domain.UploadRecord;

import java.util.List;

public interface DocumentService extends StockBaseService {


    void storeDocuments(List<UploadRecord> recordList);

}
