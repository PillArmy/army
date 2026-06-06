package io.army.example.type.dao.impl;

import io.army.dao.ArmySyncBaseDao;
import io.army.example.type.dao.TypeBaseDao;
import io.army.session.SyncSessionContext;
import org.springframework.stereotype.Repository;

@Repository("armyTypeBaseDao")
public class ArmyTypeBaseDao extends ArmySyncBaseDao implements TypeBaseDao {

    public ArmyTypeBaseDao(SyncSessionContext sessionContext) {
        super(sessionContext);
    }

}
