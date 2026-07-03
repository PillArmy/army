package io.army.example.type.service.impl;

import io.army.example.type.dao.TypeBaseDao;
import io.army.example.type.service.TypeBaseService;
import io.army.spring.sync.ArmySyncBaseService;
import io.army.spring.sync.TransactionTemplate;

public abstract class AbstractTypeBaseService extends ArmySyncBaseService<TypeBaseDao> implements TypeBaseService {

    public AbstractTypeBaseService(TransactionTemplate transactionTemplate) {
        super(transactionTemplate);
    }

}
