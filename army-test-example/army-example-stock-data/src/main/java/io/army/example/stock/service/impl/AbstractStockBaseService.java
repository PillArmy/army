package io.army.example.stock.service.impl;

import io.army.example.stock.dao.StockBaseDao;
import io.army.example.stock.service.StockBaseService;
import io.army.spring.TransactionTemplate;
import io.army.spring.sync.ArmySyncBaseService;


public abstract class AbstractStockBaseService extends ArmySyncBaseService<StockBaseDao> implements StockBaseService {


    public AbstractStockBaseService(TransactionTemplate transactionTemplate) {
        super(transactionTemplate);
    }


}
