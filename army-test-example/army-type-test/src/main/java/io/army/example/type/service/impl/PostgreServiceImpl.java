package io.army.example.type.service.impl;

import io.army.example.type.dao.PostgreDao;
import io.army.example.type.dao.TypeBaseDao;
import io.army.example.type.service.PostgreService;
import io.army.spring.sync.TransactionTemplate;
import org.springframework.stereotype.Service;

@Service("postgreService")
public class PostgreServiceImpl extends AbstractTypeBaseService implements PostgreService {

    private final PostgreDao postgreDao;

    public PostgreServiceImpl(TransactionTemplate transactionTemplate, PostgreDao postgreDao) {
        super(transactionTemplate);
        this.postgreDao = postgreDao;
    }


    @Override
    protected TypeBaseDao getDao() {
        return postgreDao;
    }

}
