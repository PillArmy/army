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
import io.army.codec.JsonCodec;
import io.army.codec.XmlCodec;
import io.army.dialect.Database;
import io.army.env.ArmyEnvironment;
import io.army.executor.ExecutorEnv;
import io.army.executor.ExecutorFactoryProvider;
import io.army.function.DefinedTypeMapFunc;
import io.army.generator.FieldGeneratorFactory;
import io.army.lang.Nullable;
import io.army.meta.SchemaMeta;
import io.army.option.Option;
import io.army.result.ResultRecord;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/// This interface representing the builder spec of {@link SessionFactory} .
/// This interface is base interface of all factory builder.
/// package interface
/// @param <B> factory builder java type,it is the sub interface of this interface
/// @param <R> sync session factory or Mono
/// @since 0.6.0
public sealed interface FactoryBuilder<B, R> permits PackageFactoryBuilder {


/// Required.
/// @param sessionFactoryName non-empty
/// @return **this**
    B name(String sessionFactoryName);

/// Required.
/// @return **this**
    B environment(ArmyEnvironment environment);

/// Required.
/// dataSource can be the instance of {@link io.army.datasource.ReadWriteSplittingDataSource}.
/// @return **this**
/// @see io.army.datasource.ReadWriteSplittingDataSource
    B datasource(Object dataSource);

    /// Required
    ///
    /// @see io.army.criteria.impl._TableMetaFactory#getTableMetaMap(SchemaMeta, List, boolean, Consumer, ClassLoader)
    B packagesToScan(List<String> packageList);

/// Optional.
/// @param catalog catalog or empty
/// @param schema  schema or empty
/// @return **this**
    B schema(String catalog, String schema);

    /*
     * <p>
     * Optional.
     *
     */
    //   B fieldCodecs(Collection<FieldCodec> fieldCodecs);

/// Optional.
/// @return **this**
    B jsonCodec(@Nullable JsonCodec codec);

/// Optional.
/// @return **this**
    B xmlCodec(@Nullable XmlCodec codec);

/// Optional.
/// @return **this**
    B factoryAdvice(@Nullable Collection<FactoryAdvice> factoryAdvices);


/// Optional.
/// @return **this**
    B fieldGeneratorFactory(@Nullable FieldGeneratorFactory factory);


/// Optional.
/// See
/// 
/// - {@link ExecutorFactoryProvider#createServerMeta(Function)}
/// - {@link Database#mapToDatabase(String, Function)}
/// 
/// @return **this**
    B nameToDatabaseFunc(@Nullable Function<String, Database> function);

/// Optional.
/// Set a consumer for validating {@link ExecutorFactoryProvider} is the instance which you want.
/// See {@code io.army.env.SyncKey#EXECUTOR_PROVIDER} and  see {@code io.army.env.ReactiveKey#EXECUTOR_PROVIDER}
/// @return **this**
    B executorFactoryProviderValidator(@Nullable Consumer<ExecutorFactoryProvider> consumer);

/// Optional.
/// See {@link ResultRecord#get(int, Class)} and {@link ExecutorEnv#converterFunc()}
/// @return **this**
    B columnConverterFunc(@Nullable Function<Class<?>, Function<Object, ?>> converterFunc);

    <T> B dataSourceOption(Option<T> option, @Nullable T value);

    /// Default : {@code Thread.currentThread().getContextClassLoader()}
    ///
    /// @param loader is used to load domain classes.
    /// @see io.army.criteria.impl._TableMetaFactory#getTableMetaMap(SchemaMeta, List, boolean, Consumer, ClassLoader)
    B classLoader(@Nullable ClassLoader loader);

    /// Default : false
    /// To optimize startup performance, loading of the corresponding static model classes
    /// and related validations is disabled by default after TableMeta creation. Enable it
    /// for enhanced safety guarantees.
    ///
    /// @param load true : load static model after create {@link io.army.meta.TableMeta}.
    /// @see io.army.criteria.impl._TableMetaFactory#getTableMetaMap(SchemaMeta, List, boolean, Consumer, ClassLoader)
    B loadStaticModel(boolean load);

    /// To improve startup performance, non-essential startup validation is disabled by default (already validated at compile time).
    /// It can be enabled for stronger consistency guarantees.
    B validateOnStartup(boolean yes);


    B definedTypeMapFunc(@Nullable DefinedTypeMapFunc func);

/// Create {@link SessionFactory} instance
/// @return 
/// - sync api : {@link SessionFactory} instance
/// - reactive api : Mono of {@link SessionFactory} instance
/// 
/// @throws SessionFactoryException throw (emit) when
/// 
/// - required properties absent
/// - name duplication
/// - access database occur error
/// - properties error
/// 
    R build();

}
