package io.army.example.type;

import io.army.dialect.Database;
import io.army.dialect.PostgreDialect;
import io.army.env.*;
import io.army.generator.StandaloneFieldGeneratorFactory;
import io.army.session.DdlMode;
import io.army.session.SyncFactoryBuilder;
import io.army.session.SyncSessionFactory;
import io.army.util._Collections;

import java.util.List;
import java.util.Map;

public abstract class TypeFactoryUtils {

    private TypeFactoryUtils() {
    }

    public static SyncSessionFactory createArmyTypeSyncFactory(final Database database) {

        return SyncFactoryBuilder.builder()
                .name(mapDatabaseToFactoryName(database))
                .packagesToScan(List.of("io.army.example.type.domain"))
                .datasource(TypeDataSourceUtils.createDataSource(database))
                .environment(createEnvironment(database))
                .fieldGeneratorFactory(StandaloneFieldGeneratorFactory.getInstance())
                .build();
    }

    private static ArmyEnvironment createEnvironment(final Database database) {
        final Map<String, Object> map = _Collections.hashMap();
        map.put(ArmyKey.DATABASE.name, database);
        map.put(ArmyKey.DIALECT.name, PostgreDialect.POSTGRE15);
        map.put(ArmyKey.VISIBLE_MODE.name, AllowMode.SUPPORT);
        map.put(ArmyKey.DATASOURCE_CLOSE_METHOD.name, "close");
        map.put(ArmyKey.QUERY_INSERT_MODE.name, AllowMode.SUPPORT);
        map.put(ArmyKey.DDL_MODE.name, DdlMode.UPDATE);
        map.put(ArmyKey.SQL_LOG_MODE.name, SqlLogMode.BEAUTIFY_DEBUG);
        map.put(ArmyKey.SQL_LOG_PARSING_TAKE_TIME.name, Boolean.TRUE);
        map.put(ArmyKey.SQL_LOG_EXECUTION_TAKE_TIME.name, Boolean.TRUE);
        map.put(ArmyKey.QUALIFIED_TABLE_NAME_ENABLE.name, Boolean.FALSE);
        map.put(ArmyKey.DATABASE_NAME_MODE.name, NameMode.DEFAULT);
        map.put(ArmyKey.TABLE_NAME_MODE.name, NameMode.DEFAULT);
        map.put(ArmyKey.COLUMN_NAME_MODE.name, NameMode.DEFAULT);
        map.put(ArmyKey.OBJECT_NAME_CACHE_MODE.name, ObjectNameCacheMode.ALL);
        return StandardEnvironment.from(map);
    }

    private static String mapDatabaseToFactoryName(final Database database) {
        return "postgre-type";
    }

}
