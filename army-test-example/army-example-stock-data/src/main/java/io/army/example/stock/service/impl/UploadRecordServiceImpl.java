package io.army.example.stock.service.impl;

import io.army.example.stock.dao.StockBaseDao;
import io.army.example.stock.dao.UploadRecordDao;
import io.army.example.stock.service.UploadRecordService;
import io.army.spring.TransactionTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

@Service("uploadRecordService")
public class UploadRecordServiceImpl extends AbstractStockBaseService implements UploadRecordService {


    private final UploadRecordDao uploadRecordDao;

    public UploadRecordServiceImpl(TransactionTemplate transactionTemplate, UploadRecordDao uploadRecordDao) {
        super(transactionTemplate);
        this.uploadRecordDao = uploadRecordDao;
    }

    @Override
    public long uploadComplete(long id, String fileHast) {
        final Long rowCount;
        rowCount = this.transactionTemplate.execute(Isolation.READ_COMMITTED, false,
                _ -> this.uploadRecordDao.uploadComplete(id, fileHast)
        );
        assert rowCount != null;
        return rowCount;
    }

    @Override
    protected StockBaseDao getDao() {
        return this.uploadRecordDao;
    }


}

