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

package io.army.session;

import io.army.advice.FactoryAdvice;
import io.army.dialect.*;
import io.army.env.ArmyEnvironment;
import io.army.env.ArmyKey;
import io.army.env.SyncKey;
import io.army.executor.*;
import io.army.mapping.MappingType;
import io.army.meta.ServerMeta;
import io.army.meta.TableMeta;
import io.army.schema.SchemaComparer;
import io.army.schema.SchemaInfo;
import io.army.schema.SchemaResult;
import io.army.util._Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

/// This class is a implementation of {@link SyncFactoryBuilder}.
/// This class is the builder of {@link ArmySyncSessionFactory}.
///
/// @see ArmySyncSessionFactory
/// @since 0.6.0
final class ArmySyncFactoryBuilder
        extends ArmyFactoryBuilder<SyncFactoryBuilder, SyncSessionFactory> implements SyncFactoryBuilder {

    /// @see SyncFactoryBuilder#builder()
    static SyncFactoryBuilder create() {
        return new ArmySyncFactoryBuilder();
    }

    private static final Logger LOG = LoggerFactory.getLogger(ArmySyncFactoryBuilder.class);

    SyncExecutorFactory stmtExecutorFactory;

    /// private constructor
    private ArmySyncFactoryBuilder() {
    }


    @Override
    ExecutorFactoryProvider createExecutorFactoryProvider(final String name, final Object dataSource, final ArmyEnvironment env) {
        return createExecutorProvider(name, env, dataSource, SyncExecutorFactoryProvider.class,
                SyncKey.EXECUTOR_PROVIDER, SyncKey.EXECUTOR_PROVIDER_MD5);
    }

    @Override
    ServerMeta createServerMeta(ExecutorFactoryProvider provider) {
        return ((SyncExecutorFactoryProvider) provider).createServerMeta(this.nameToDatabaseFunc);
    }

    @Override
    protected SyncSessionFactory buildAfterScanTableMeta(final String name, final ArmyEnvironment env,
                                                         final ExecutorFactoryProvider provider, final ServerMeta serverMeta) {

        try {

            // create ExecutorProvider
            final SyncExecutorFactoryProvider executorProvider = (SyncExecutorFactoryProvider) provider;

            // create DialectParser
            final DialectParser dialectParser;
            dialectParser = createDialectParser(name, false, serverMeta, env, executorProvider);

            //  create SyncExecutorFactory
            final SyncExecutorFactory executorFactory;
            executorFactory = executorProvider.createFactory(createExecutorEnv(name, env, dialectParser));

            final Consumer<ExecutorFactoryProvider> consumer = this.executorProviderConsumer;
            if (consumer != null) {
                consumer.accept(executorProvider);
            }

            final FactoryAdvice factoryAdvice;
            factoryAdvice = createFactoryAdviceComposite(this.factoryAdvices);
            //  invoke beforeInstance
            if (factoryAdvice != null) {
                factoryAdvice.beforeInstance(serverMeta, env);
            }

            // create SessionFactoryImpl instance
            this.stmtExecutorFactory = executorFactory;
            this.ddlMode = env.getOrDefault(ArmyKey.DDL_MODE);
            final ArmySyncSessionFactory sessionFactory;
            sessionFactory = ArmySyncSessionFactory.create(this);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Created {}", sessionFactory);
            }
            assert name.equals(sessionFactory.name());
            assert sessionFactory.executorFactory == this.stmtExecutorFactory;

            // invoke beforeInitialize
            if (factoryAdvice != null) {
                factoryAdvice.beforeInitialize(sessionFactory);
            }

            //  invoke initializingFactory
            initializingFactory(sessionFactory);

            //  invoke afterInitialize
            if (factoryAdvice != null) {
                factoryAdvice.afterInitialize(sessionFactory);
            }
            return sessionFactory;
        } catch (SessionFactoryException e) {
            throw e;
        } catch (Exception e) {
            throw new SessionFactoryException(e.getMessage(), e);
        }
    }

    @Override
    protected SyncSessionFactory handleError(SessionFactoryException cause) {
        throw cause;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }


    private Map<String, String> createTypeNameToSchemaMap(ServerMeta serverMeta, ParserFactory parserFactory,
                                                          SyncExecutorFactoryProvider provider) {
        try (SyncPreBootstrapExecutor executor = provider.createExecutor()) {

            final PreBootstrapParser preBootstrapParser;
            preBootstrapParser = parserFactory.createPreBootstrapParser(serverMeta);

            final List<String> sqlList;
            sqlList = preBootstrapParser.extensionStmts(this.extensionInCurrentSchema, this.extensionSchemaMap, this.extensionNameSet);

            final boolean debug = LOG.isDebugEnabled();
            if (debug) {
                LOG.debug("{}", ddlToSqlLog(sqlList));
            }
            executor.executeUpdate(sqlList);

            final String sql;
            sql = preBootstrapParser.queryDefinedTypeSchema();

            final Set<String> extensionNameSet = this.extensionNameSet;
            final Map<String, String> typeNameToSchemaMap = new HashMap<>();
            final Set<String> typeNameInExtensionSet = new HashSet<>();

            final String currentSchema = serverMeta.schema(); // currently, for PostgreSQL schema

            final PreBootstrapExecutor.VoidRowFunc function;
            function = row -> {
                final String typeName, schema, oldSchema, extensionName;

                typeName = row.getNonNull(0, String.class).toUpperCase(Locale.ROOT);
                schema = row.getNonNull(1, String.class);
                extensionName = row.get(2, String.class);

                if (extensionName != null
                        && !extensionNameSet.contains(extensionName.toLowerCase(Locale.ROOT))) {
                    // Unused types
                    return null;
                }
                if (extensionName != null) {
                    typeNameInExtensionSet.add(typeName);
                }

                oldSchema = typeNameToSchemaMap.putIfAbsent(typeName, schema);

                if (oldSchema != null
                        && !oldSchema.equals(schema)
                        && currentSchema != null
                        && !oldSchema.equals(currentSchema)) {
                    typeNameToSchemaMap.remove(typeName);  // Duplicate type name. The current schema will be used.
                }
                return null;
            };

            if (debug) {
                LOG.debug("{}", sql);
            }

            executor.executeQuery(sql, function);  // execute sql

            // below , remove unnecessary type name
            final Map<String, MappingType> definedTypeMap = this.definedTypeMap;

            final Iterator<Map.Entry<String, String>> iterator = typeNameToSchemaMap.entrySet().iterator();
            String upperCaseTypeName;
            for (Map.Entry<String, String> e; iterator.hasNext(); ) {
                e = iterator.next();

                if (e.getValue().equals(currentSchema)) {
                    iterator.remove(); // current schema don't need to be specified
                    continue;
                }

                upperCaseTypeName = e.getKey();

                if (!typeNameInExtensionSet.contains(upperCaseTypeName)
                        && !definedTypeMap.containsKey(upperCaseTypeName)) {
                    iterator.remove(); // Unused types
                }

            }

            return typeNameToSchemaMap;
        }
    }

    private DialectParser createDialectParser(String factoryName, boolean reactive, ServerMeta serverMeta,
                                              ArmyEnvironment env, SyncExecutorFactoryProvider provider) {
        final ParserFactory parserFactory;
        parserFactory = ParserFactories.createFactory(serverMeta);

        final Map<String, String> typeNameToSchemaMap;
        if (this.extensionNameSet.isEmpty()) {
            typeNameToSchemaMap = Map.of();
        } else {
            typeNameToSchemaMap = createTypeNameToSchemaMap(serverMeta, parserFactory, provider);
        }

        final DialectEnv dialectEnv;
        dialectEnv = createDialectEnv(factoryName, reactive, serverMeta, env, typeNameToSchemaMap);

        final DialectParser dialectParser;
        this.dialectParser = dialectParser = parserFactory.createDialectParser(dialectEnv);

        dialectEnv.clearTempProperties();
        return dialectParser;
    }


    /// @see #buildAfterScanTableMeta(String, ArmyEnvironment, ExecutorFactoryProvider, ServerMeta)
    private void initializingFactory(final ArmySyncSessionFactory sessionFactory) throws SessionFactoryException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Initializing {}", sessionFactory);
        }

        // initializing schema
        final DdlMode ddlMode;
        ddlMode = this.ddlMode;
        switch (ddlMode) {
            case NONE:
                // no-op
                break;
            case VALIDATE:
            case UPDATE:
            case DROP_CREATE://TODO detail
                initializingSchema(sessionFactory, ddlMode);
                break;
            default:
                throw _Exceptions.unexpectedEnum(ddlMode);
        }

    }


    /// @see #initializingFactory(ArmySyncSessionFactory)
    private void initializingSchema(final ArmySyncSessionFactory sessionFactory, final DdlMode ddlMode) {

        final long startTime;
        startTime = System.currentTimeMillis();

        final SyncExecutorFactory executorFactory;
        executorFactory = sessionFactory.executorFactory;

        try (SyncMetaExecutor executor = executorFactory.metaExecutor(dataSourceFunc())) {

            final Map<String, MappingType> definedTypeMap = this.definedTypeMap;

            //1.extract schema info.
            final SchemaInfo schemaInfo;
            schemaInfo = executor.extractInfo(sessionFactory.dialectParser.queryDefinedTypeStmts(definedTypeMap));

            //2.compare schema meta and schema info.
            final SchemaResult schemaResult;
            switch (ddlMode) {
                case VALIDATE:
                case UPDATE: {
                    final SchemaComparer schemaComparer;
                    schemaComparer = SchemaComparer.create(sessionFactory.serverMeta());
                    final Collection<TableMeta<?>> tableCollection;
                    tableCollection = sessionFactory.tableMap().values();
                    schemaResult = schemaComparer.compare(schemaInfo, sessionFactory.configSchemaMeta(), tableCollection, definedTypeMap);
                }
                break;
                case DROP_CREATE: {
                    final Collection<TableMeta<?>> tableCollection;
                    tableCollection = sessionFactory.tableMap().values();
                    schemaResult = SchemaResult.dropCreate(schemaInfo.catalog(), schemaInfo.schema(), tableCollection, definedTypeMap.values());
                }
                break;
                default:
                    throw _Exceptions.unexpectedEnum(ddlMode);
            }

            //3.validate or execute ddl
            switch (ddlMode) {
                case VALIDATE: {
                    if (schemaResult.hasChanges()) {
                        final SessionFactoryException error;
                        if ((error = validateSchema(sessionFactory, schemaResult)) != null) {
                            throw error;
                        }
                    }
                }
                break;
                case UPDATE:
                case DROP_CREATE: {
                    //create ddl
                    if (!schemaResult.hasChanges()) {
                        break;
                    }
                    final List<String> ddlList;
                    ddlList = parseMetaDdl(sessionFactory, schemaResult);
                    final int ddlSize = ddlList.size();
                    if (ddlSize == 0) {
                        break;
                    }

                    LOG.info("{}:\n\n{}", sessionFactory, ddlToSqlLog(ddlList));
                    executor.executeDdl(ddlList);
                }
                break;
                default:
                    throw _Exceptions.unexpectedEnum(ddlMode);
            }

            LOG.info("Initializing database of {}[{}],{}[{}],cost {} ms.",
                    SyncSessionFactory.class.getName(),
                    sessionFactory.name(),
                    DdlMode.class.getName(),
                    ddlMode,
                    System.currentTimeMillis() - startTime
            );
        } catch (Exception e) {
            String m = String.format("%s[%s] schema initializing failure.", SyncSessionFactory.class.getName(),
                    sessionFactory.name());
            throw new SessionFactoryException(m, e);
        }


    }


}
