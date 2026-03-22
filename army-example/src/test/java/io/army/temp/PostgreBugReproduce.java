package io.army.temp;


import com.alibaba.druid.pool.DruidDataSource;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class PostgreBugReproduce {


    @Test
    public void reproduce() throws Exception {
        try (DruidDataSource db = createDataSource()) {
            try (Connection conn = db.getConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(ddl());
                    stmt.executeUpdate("START TRANSACTION READ ONLY");
                    System.out.printf("autoCommit is %s after start transaction%n", conn.getAutoCommit());

                    stmt.setFetchSize(1);
                    // org.postgresql.jdbc.PgStatement.executeInternal()
                    // no ->  flags |= QueryExecutor.QUERY_FORWARD_CURSOR;
                    try (ResultSet rs = stmt.executeQuery("SELECT u.id,u.name FROM u_user as u ORDER BY u.id")) {
                        while (rs.next()) {
                            System.out.printf("id:%s ; name : %s%n", rs.getLong(1), rs.getString(2));
                        }
                    }
                }

            }
        }

    }


    private static String ddl() {
        return """
                create table IF NOT EXISTS u_user
                (
                    id                 BIGSERIAL       not null primary key,
                    name varchar(15)  not null 
                );
                
                """;
    }


    private static DruidDataSource createDataSource() {

        final String url;
        url = "jdbc:postgresql://localhost:5432/postgres";

        final Properties properties = new Properties();
        properties.put("user", "army_w");
        properties.put("password", "army123");

        final DruidDataSource ds;
        ds = new DruidDataSource();

        ds.setUrl(url);
        ds.setDriverClassName(org.postgresql.Driver.class.getName());
        ds.setConnectProperties(properties);

        ds.setInitialSize(10);
        ds.setMaxActive(200);
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

}
