package io.army.example.type;

import io.army.dialect.Database;
import io.army.session.SyncSessionFactory;

import io.army.util._Collections;

import java.util.Map;

public abstract class TypeTestSupport {

    private static final Map<Database, SyncSessionFactory> SYNC_FACTORY_MAP = _Collections.concurrentHashMap();

    protected TypeTestSupport() {

    }


    protected SyncSessionFactory getSessionFactory(Database database) {
        return SYNC_FACTORY_MAP.computeIfAbsent(database, TypeFactoryUtils::createArmyTypeSyncFactory);
    }

}
