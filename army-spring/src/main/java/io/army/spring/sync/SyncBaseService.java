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

package io.army.spring.sync;


import org.jspecify.annotations.Nullable;

import java.util.List;

public interface SyncBaseService {


    <T> void save(T domain);

    <T> void batchSave(List<T> domainList);

    @Nullable
    <T> T get(Class<T> domainClass, Object id);

    @Nullable
    <T> T getByUnique(Class<T> domainClass, String fieldName, Object fieldValue);

    @Nullable
    <T> T findById(Class<T> domainClass, Object id);

    @Nullable
    <T> T findByUnique(Class<T> domainClass, String fieldName, Object fieldValue);


    <T> long rowCount(Class<T> domainClass);

    <T> int rowCountAsInt(Class<T> domainClass);

    <T> long updateField(Class<T> domainClass, Object id, String fieldName, Object fieldValue);

    <T, F> long updateFieldWhenMatch(Class<T> domainClass, Object id, String fieldName, F fieldValue, @Nullable F defaultValue);


}
