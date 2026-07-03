package io.army.example.stock.service.impl;

import io.army.example.stock.dao.StockBaseDao;
import io.army.example.stock.service.StockBaseService;
import io.army.spring.sync.ArmySyncBaseService;
import io.army.spring.sync.TransactionTemplate;


/// Abstract base service for the stock module, extending `ArmySyncBaseService`
/// with `StockBaseDao` as the generic DAO type.
///
/// <p>Provides transaction template injection and a template method `getDao()`
/// for subclass-specific DAO access.</p>
public abstract class AbstractStockBaseService extends ArmySyncBaseService<StockBaseDao> implements StockBaseService {


    public AbstractStockBaseService(TransactionTemplate transactionTemplate) {
        super(transactionTemplate);
    }


}
