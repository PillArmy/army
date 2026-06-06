package io.army.example.type;

import io.army.dialect.Database;
import io.army.session.SyncSessionFactory;
import io.army.session.sync.SyncSessionTestSupport;
import io.army.util._Collections;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TypeTestSupport extends SyncSessionTestSupport {

    private static final Map<Database, SyncSessionFactory> SYNC_FACTORY_MAP = _Collections.concurrentHashMap();

    protected TypeTestSupport() {
        super(Database.PostgreSQL);
    }

    @Override
    protected SyncSessionFactory getSessionFactory(Database database) {
        return SYNC_FACTORY_MAP.computeIfAbsent(database, TypeFactoryUtils::createArmyTypeSyncFactory);
    }

}
