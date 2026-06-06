package io.army.example.type.service.impl;

import io.army.example.type.dao.PostgreDao;
import io.army.example.type.dao.TypeBaseDao;
import io.army.example.type.domain.Postgre;
import io.army.example.type.service.PostgreService;
import io.army.spring.TransactionTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("postgreService")
public class PostgreServiceImpl extends AbstractTypeBaseService implements PostgreService {

    private final PostgreDao postgreDao;

    public PostgreServiceImpl(TransactionTemplate transactionTemplate, PostgreDao postgreDao) {
        super(transactionTemplate);
        this.postgreDao = postgreDao;
    }

    @Override
    @Transactional
    public Postgre insert(Postgre postgre) {
        return postgreDao.insert(postgre);
    }

    @Override
    @Transactional
    public Postgre update(Postgre postgre) {
        return postgreDao.update(postgre);
    }

    @Override
    @Transactional(readOnly = true)
    public Postgre findById(Long id) {
        return postgreDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Postgre findFirst() {
        return postgreDao.findFirst();
    }

    @Override
    protected TypeBaseDao getDao() {
        return postgreDao;
    }

}
