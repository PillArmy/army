/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.army.jdbc;

import io.army.ArmyException;
import io.army.dialect.Database;
import io.army.dialect.Dialect;
import io.army.env.ArmyEnvironment;
import io.army.env.ArmyKey;
import io.army.executor.*;
import io.army.lang.Nullable;
import io.army.mapping.MappingEnv;
import io.army.meta.ServerMeta;
import io.army.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("unused")
public final class JdbcExecutorFactoryProvider implements SyncExecutorFactoryProvider {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcExecutorFactoryProvider.class);

    public static JdbcExecutorFactoryProvider create(Object dataSource, String factoryName, ArmyEnvironment env) {
        if (!(dataSource instanceof DataSource)) {
            throw unsupportedDataSource(dataSource);
        }
        return new JdbcExecutorFactoryProvider((DataSource) dataSource, factoryName, env);
    }


    final DataSource dataSource;

    final String sessionFactoryName;

    private final Dialect usedDialect;

    int methodFlag = 0;

    ServerMeta meta;

    private JdbcExecutorFactoryProvider(DataSource dataSource, String factoryName, ArmyEnvironment env) {
        this.dataSource = dataSource;
        this.sessionFactoryName = factoryName;
        this.usedDialect = env.getOrDefault(ArmyKey.DIALECT);
    }

    @Override
    public SyncPreBootstrapExecutor createExecutor() {
        try {
            return new JdbcSyncPreBootstrapExecutor(this.dataSource.getConnection());
        } catch (SQLException e) {
            throw JdbcExecutor.wrapError(e);
        }
    }

    @Override
    public ServerMeta createServerMeta(@Nullable Function<String, Database> func)
            throws DataAccessException {

        final ServerMeta meta;
        try (Connection conn = this.dataSource.getConnection()) {
            final ServerMeta serverMeta;
            this.meta = serverMeta = builderServerMeta(conn, this.usedDialect, func);

            LOG.debug("create {}", serverMeta);

            this.methodFlag = createDriverImplFlags(conn, serverMeta.serverDatabase());

            return serverMeta;
        } catch (SQLException e) {
            throw JdbcExecutor.wrapError(e);
        } catch (Exception e) {
            String m = String.format("get server metadata occur error:%s", e.getMessage());
            throw new DataAccessException(m, e);
        }
    }


    @Override
    public SyncExecutorFactory createFactory(final ExecutorEnv env) throws DataAccessException {
        validateServerMeta(env.mappingEnv());
        return JdbcExecutorFactory.create(this, env);
    }


    private void validateServerMeta(final MappingEnv mappingEnv) {
        final ServerMeta serverMeta = this.meta;
        if (serverMeta == null) {
            throw new IllegalStateException(String.format("Don't create %s", ServerMeta.class.getName()));
        }
        if (mappingEnv.serverMeta() != serverMeta) {
            throw new IllegalArgumentException(String.format("%s not match.", ServerMeta.class.getName()));
        }
    }

    private static ServerMeta builderServerMeta(final Connection conn, final Dialect usedDialect,
                                                final @Nullable Function<String, Database> func)
            throws SQLException {
        final DatabaseMetaData metaData;
        metaData = conn.getMetaData();

        final String family, version;
        family = metaData.getDatabaseProductName();

        return ServerMeta.builder()

                .name(family)
                .database(Database.mapToDatabase(family, func))
                .catalog(conn.getCatalog())
                .schema(conn.getSchema())

                .version(metaData.getDatabaseProductVersion())
                .major(metaData.getDatabaseMajorVersion())
                .minor(metaData.getDatabaseMinorVersion())
                .subMinor(0) // TODO parse version

                .usedDialect(usedDialect)
                .supportSavePoint(metaData.supportsSavepoints())
                .driverSpi("java.sql")

                .build();
    }


    private static int createDriverImplFlags(final Connection conn, final Database serverDatabase) throws SQLException {

        int methodFlag = 0;

        try (PreparedStatement statement = conn.prepareStatement("SELECT 1 + ? AS armyJdbcTest")) {
            final Class<?> clazz = statement.getClass();

            if (definiteSetObjectMethod(clazz)) {
                methodFlag |= JdbcExecutorFactory.SET_OBJECT_METHOD;
            }
            if (definiteExecuteLargeUpdateMethod(clazz)) {
                methodFlag |= JdbcExecutorFactory.EXECUTE_LARGE_UPDATE_METHOD;
            }
            if (definiteExecuteLargeBatchMethod(clazz)) {
                methodFlag |= JdbcExecutorFactory.EXECUTE_LARGE_BATCH_METHOD;
            }

        }

        switch (serverDatabase) {
            case MySQL: {
                try (Statement statement = conn.createStatement()) {
                    statement.execute("SELECT 1 AS one ; SELECT 2 AS two");

                    statement.getMoreResults(Statement.CLOSE_ALL_RESULTS);

                    methodFlag |= JdbcExecutorFactory.MULTI_STMT;

                    LOG.debug("{} driver support multi-statement.", serverDatabase.name());
                } catch (SQLException e) {
                    LOG.debug("{} driver don't support multi-statement.", serverDatabase.name());
                }
            }
            break;
            case PostgreSQL:
            case SQLite:
            case H2:
            case Oracle:
            default:// no-op
        }

        return methodFlag;


    }


    private static boolean definiteSetObjectMethod(final Class<?> statementClass) {
        boolean match;
        try {
            final Method method;
            method = statementClass.getMethod("setObject", int.class, Object.class, SQLType.class);
            match = !method.getDeclaringClass().isInterface();
        } catch (NoSuchMethodException e) {
            match = false;
        }
        return match;
    }

    private static boolean definiteExecuteLargeUpdateMethod(final Class<?> statementClass) {
        boolean match;
        try {
            final Method method;
            method = statementClass.getMethod("executeLargeUpdate");
            match = !method.getDeclaringClass().isInterface();
        } catch (NoSuchMethodException e) {
            match = false;
        }
        return match;
    }

    private static boolean definiteExecuteLargeBatchMethod(final Class<?> statementClass) {
        boolean match;
        try {
            final Method method;
            method = statementClass.getMethod("executeLargeBatch");
            match = !method.getDeclaringClass().isInterface();
        } catch (NoSuchMethodException e) {
            match = false;
        }
        return match;
    }


    static DataSource getPrimaryDataSource(DataSource dataSource) {
        return dataSource;
    }


    private static ArmyException unsupportedDataSource(Object dataSource) {
        final String m;
        m = String.format("%s support only %s or %s,but dataSource is %s.",
                JdbcExecutorFactoryProvider.class.getName(),
                DataSource.class.getName(),
                XADataSource.class.getName(),
                ClassUtils.safeClassName(dataSource)
        );
        throw new ArmyException(m);
    }


    private static final class JdbcSyncPreBootstrapExecutor implements SyncPreBootstrapExecutor {

        final Connection conn;

        private JdbcSyncPreBootstrapExecutor(Connection conn) {
            this.conn = conn;
        }


        @Override
        public Void executeUpdate(List<String> sqlList) {
            if (sqlList.isEmpty()) {
                return null;
            }
            try (Statement statement = this.conn.createStatement()) {
                for (String sql : sqlList) {
                    statement.addBatch(sql);
                }
                statement.executeBatch();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        public <R> List<R> executeQuery(String sql, Function<CurrentRow, R> function) {
            try (Statement statement = this.conn.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    final CurrentRow currentRow;
                    currentRow = new JdbcCurrentRow(resultSet);

                    final boolean voidFunc = function instanceof VoidRowFunc;
                    final List<R> rowList;
                    if (voidFunc) {
                        rowList = List.of();
                    } else {
                        rowList = new ArrayList<>();
                    }

                    List<Object> columnList;
                    R row;
                    while (resultSet.next()) {
                        row = function.apply(currentRow);
                        if (row == currentRow) {
                            throw new RuntimeException("row == currentRow");
                        }
                        if (!voidFunc) {
                            rowList.add(row);
                        }
                    }
                    return rowList;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() {
            JdbcExecutor.closeResource(this.conn);
        }


    } // JdbcSyncPreBootstrapExecutor

    private static final class JdbcCurrentRow extends ArmyCurrentRow {

        private final ResultSet resultSet;

        private final int columnCount;

        private JdbcCurrentRow(ResultSet resultSet) {
            this.resultSet = resultSet;
            try {
                this.columnCount = resultSet.getMetaData().getColumnCount();
            } catch (SQLException e) {
                throw JdbcExecutor.wrapError(e);
            }
        }

        @Override
        public <R> R get(int indexBasedZero, Class<R> columnClass) {
            if (indexBasedZero < 0 || indexBasedZero >= this.columnCount) {
                String m = String.format("index not in [0,%s)", this.columnCount);
                throw new DataAccessException(m);
            }
            try {
                return this.resultSet.getObject(indexBasedZero + 1, columnClass);
            } catch (SQLException e) {
                throw JdbcExecutor.wrapError(e);
            }
        }

        @Override
        protected String getColumnLabel(int indexBasedZero) {
            try {
                return this.resultSet.getMetaData().getColumnLabel(indexBasedZero + 1);
            } catch (SQLException e) {
                throw JdbcExecutor.wrapError(e);
            }
        }
    } // JdbcCurrentRow


}
