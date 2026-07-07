package io.army.example.coder.service.impl;

import io.army.example.coder.dao.CoderSyncDao;
import io.army.example.coder.service.CoderBaseService;
import io.army.spring.sync.ArmySyncBaseService;
import io.army.spring.sync.TransactionTemplate;

public abstract class ArmyCoderBaseService extends ArmySyncBaseService<CoderSyncDao> implements CoderBaseService {


    public ArmyCoderBaseService(TransactionTemplate transactionTemplate) {
        super(transactionTemplate);
    }


}
