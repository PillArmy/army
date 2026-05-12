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

package io.army.result;

import io.army.lang.Nullable;
import io.army.mapping.MappingType;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface DataRecord extends ResultItem, ResultItem.ResultAccessSpec {

    RecordMeta getRecordMeta();

    @Nullable
    Object get(int indexBasedZero);


    Object getNonNull(int indexBasedZero);

    Object getOrDefault(int indexBasedZero, Object defaultValue);

    Object getOrSupplier(int indexBasedZero, Supplier<?> supplier);


    /// Javadoc see
    ///
    /// - {@link ResultRecord#get(int, Class)}
    /// - {@link CurrentRecord#get(int, Class)}
    ///
    @Nullable
    <T> T get(int indexBasedZero, Class<T> columnClass);

    <T> T getNonNull(int indexBasedZero, Class<T> columnClass);

    <T> T getOrDefault(int indexBasedZero, Class<T> columnClass, T defaultValue);

    <T> T getOrSupplier(int indexBasedZero, Class<T> columnClass, Supplier<T> supplier);

    @Nullable
    Object get(int indexBasedZero, MappingType type);

    @Nullable
    <T> T get(int indexBasedZero, Class<T> columnClass, MappingType type);

    <T> T getNonNull(int indexBasedZero, Class<T> columnClass, MappingType type);

    <T> T getOrDefault(int indexBasedZero, Class<T> columnClass, MappingType type, T defaultValue);

    <T> T getOrSupplier(int indexBasedZero, Class<T> columnClass, MappingType type, Supplier<T> supplier);


    @Nullable
    <T> List<T> getList(int indexBasedZero, Class<T> elementClass);

    <T> List<T> getNonNullList(int indexBasedZero, Class<T> elementClass);

    @Nullable
    <K, V> Map<K, V> getMap(int indexBasedZero, Class<K> keyClass, Class<V> valueClass);

    <K, V> Map<K, V> getNonNullMap(int indexBasedZero, Class<K> keyClass, Class<V> valueClass);

    @Nullable
    <T> List<T> getList(int indexBasedZero, Class<T> elementClass, MappingType type);

    <T> List<T> getNonNullList(int indexBasedZero, Class<T> elementClass, MappingType type);

    @Nullable
    <K, V> Map<K, V> getMap(int indexBasedZero, Class<K> keyClass, Class<V> valueClass, MappingType type);

    <K, V> Map<K, V> getNonNullMap(int indexBasedZero, Class<K> keyClass, Class<V> valueClass, MappingType type);


    /*-------------------below label methods -------------------*/

    @Nullable
    Object get(String columnLabel);


    Object getNonNull(String columnLabel);

    Object getOrDefault(String columnLabel, Object defaultValue);

    Object getOrSupplier(String columnLabel, Supplier<?> supplier);

    @Nullable
    <T> T get(String columnLabel, Class<T> columnClass);


    <T> T getNonNull(String columnLabel, Class<T> columnClass);


    <T> T getOrDefault(String columnLabel, Class<T> columnClass, T defaultValue);

    <T> T getOrSupplier(String columnLabel, Class<T> columnClass, Supplier<T> supplier);

    @Nullable
    Object get(String columnLabel, MappingType type);

    @Nullable
    <T> T get(String columnLabel, Class<T> columnClass, MappingType type);

    <T> T getNonNull(String columnLabel, Class<T> columnClass, MappingType type);

    <T> T getOrDefault(String columnLabel, Class<T> columnClass, MappingType type, T defaultValue);

    <T> T getOrSupplier(String columnLabel, Class<T> columnClass, MappingType type, Supplier<T> supplier);


    @Nullable
    <T> List<T> getList(String columnLabel, Class<T> elementClass);

    <T> List<T> getNonNullList(String columnLabel, Class<T> elementClass);

    @Nullable
    <K, V> Map<K, V> getMap(String columnLabel, Class<K> keyClass, Class<V> valueClass);

    <K, V> Map<K, V> getNonNullMap(String columnLabel, Class<K> keyClass, Class<V> valueClass);


    @Nullable
    <T> List<T> getList(String columnLabel, Class<T> elementClass, MappingType type);

    <T> List<T> getNonNullList(String columnLabel, Class<T> elementClass, MappingType type);

    @Nullable
    <K, V> Map<K, V> getMap(String columnLabel, Class<K> keyClass, Class<V> valueClass, MappingType type);

    <K, V> Map<K, V> getNonNullMap(String columnLabel, Class<K> keyClass, Class<V> valueClass, MappingType type);




}
