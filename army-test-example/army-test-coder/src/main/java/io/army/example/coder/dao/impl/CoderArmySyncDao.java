package io.army.example.coder.dao.impl;

import io.army.dao.ArmySyncBaseDao;
import io.army.example.coder.dao.CoderSyncDao;
import io.army.session.SyncSessionContext;
import org.springframework.stereotype.Repository;


@Repository("coderSyncDao")
public class CoderArmySyncDao extends ArmySyncBaseDao implements CoderSyncDao {

    public CoderArmySyncDao(SyncSessionContext sessionContext) {
        super(sessionContext);
    }


}
