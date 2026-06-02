package io.army.example.stock.service.impl;

import io.army.example.stock.dao.StockBaseDao;
import io.army.example.stock.dao.UploadRecordDao;
import io.army.example.stock.service.UploadRecordService;
import io.army.spring.TransactionTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

/// Transactional service implementation for **upload record** operations.
///
/// <p>The `uploadComplete` method runs in a `READ_COMMITTED` non-read-only transaction
/// to atomically mark an upload as finished.</p>
@Service("uploadRecordService")
public class UploadRecordServiceImpl extends AbstractStockBaseService implements UploadRecordService {


    private final UploadRecordDao uploadRecordDao;

    public UploadRecordServiceImpl(TransactionTemplate transactionTemplate, UploadRecordDao uploadRecordDao) {
        super(transactionTemplate);
        this.uploadRecordDao = uploadRecordDao;
    }

    @Override
    public long uploadComplete(long id, String fileHast) {
        return this.transactionTemplate.executeNoNull(Isolation.READ_COMMITTED, false,
                _ -> this.uploadRecordDao.uploadComplete(id, fileHast)
        );
    }

    @Override
    protected StockBaseDao getDao() {
        return this.uploadRecordDao;
    }


}

