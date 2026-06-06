package io.army.example.type;

import com.alibaba.druid.pool.DruidDataSource;
import io.army.dialect.Database;
import io.army.util._Exceptions;

import java.util.Properties;

public abstract class TypeDataSourceUtils {

    private TypeDataSourceUtils() {
        throw new UnsupportedOperationException();
    }

    public static DruidDataSource createDataSource(final Database database) {

        final String url = mapDatabaseToUrl(database);

        final DruidDataSource ds = new DruidDataSource();
        ds.setUrl(url);
        ds.setDriverClassName(mapDriverName(url));
        ds.setConnectProperties(mapDatabaseToProperties(database));

        ds.setInitialSize(5);
        ds.setMaxActive(50);
        ds.setMaxWait(27L * 1000L);
        ds.setValidationQuery("SELECT 1 ");

        ds.setTestOnBorrow(Boolean.FALSE);
        ds.setTestWhileIdle(Boolean.TRUE);
        ds.setTestOnReturn(Boolean.TRUE);
        ds.setTimeBetweenEvictionRunsMillis(5L * 1000L);

        ds.setRemoveAbandoned(Boolean.FALSE);
        ds.setMinEvictableIdleTimeMillis(30000L);
        return ds;
    }

    private static String mapDriverName(final String url) {
        if (url.startsWith("jdbc:mysql:")) {
            return com.mysql.cj.jdbc.Driver.class.getName();
        } else if (url.startsWith("jdbc:postgresql:")) {
            return org.postgresql.Driver.class.getName();
        } else if (url.startsWith("jdbc:sqlite:")) {
            return org.sqlite.JDBC.class.getName();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static String mapDatabaseToUrl(final Database database) {
        final String url;
        switch (database) {
            case MySQL:
                url = "jdbc:mysql://localhost:3306/army_type_test";
                break;
            case PostgreSQL:
                url = "jdbc:postgresql://localhost:5432/postgres";
                break;
            case SQLite:
                url = "jdbc:sqlite:src/test/resources/army_type_test.sqlite";
                break;
            default:
                throw _Exceptions.unexpectedEnum(database);
        }
        return url;
    }

    public static Properties mapDatabaseToProperties(final Database database) {
        final Properties properties = new Properties();
        properties.put("user", "army_w");
        properties.put("password", "army123");
        return properties;
    }

}
