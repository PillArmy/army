package io.army.example.type.dao.impl;

import io.army.example.type.dao.PostgreDao;
import io.army.session.SyncSessionContext;
import org.springframework.stereotype.Repository;

@Repository("postgreDao")
public class PostgreDaoImpl extends ArmyTypeBaseDao implements PostgreDao {

    public PostgreDaoImpl(SyncSessionContext sessionContext) {
        super(sessionContext);
    }


}
