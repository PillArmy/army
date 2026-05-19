package io.army.example.stock.service.impl;

import io.army.example.stock.dao.StockBaseDao;
import io.army.example.stock.domain.UploadRecord;
import io.army.example.stock.service.DocumentService;
import io.army.spring.TransactionTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
public class DocumentServiceImpl extends AbstractStockBaseService implements DocumentService {

    private final StockBaseDao stockBaseDao;

    private final EmbeddingModel embeddingModel;

    public DocumentServiceImpl(TransactionTemplate transactionTemplate,
                               @Qualifier("armyStockBaseDao") StockBaseDao stockBaseDao,
                               EmbeddingModel embeddingModel) {
        super(transactionTemplate);
        this.stockBaseDao = stockBaseDao;
        this.embeddingModel = embeddingModel;
    }

    @Transactional(propagation = Propagation.NEVER)
    @Override
    public void storeDocuments(List<UploadRecord> recordList) {
        for (UploadRecord o : recordList) {
            try {
                Files.deleteIfExists(o.storePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    protected StockBaseDao getDao() {
        return this.stockBaseDao;
    }


}
