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

package io.army.meta;


import io.army.lang.Nullable;

/**
 * <p>
 * This interface is base interface of below:
 * <ul>
 *     <li>{@link SimpleTableMeta}</li>
 *     <li>{@link ParentTableMeta}</li>
 * </ul>
 *
 * @param <T> domain java type
 */
public interface SingleTableMeta<T> extends TableMeta<T> {


    FieldMeta<T> createTime();

    /**
     * @throws IllegalArgumentException throw when no this field
     * @see #tryUpdateTime()
     */
    FieldMeta<T> updateTime();

    /**
     * @throws IllegalArgumentException throw when no this field
     * @see #tryVersion()
     */
    FieldMeta<T> version();

    /**
     * @throws IllegalArgumentException throw when no this field
     * @see #tryVisible()
     */
    FieldMeta<T> visible();

    /**
     * @see #updateTime()
     */
    @Nullable
    FieldMeta<T> tryUpdateTime();

    /**
     * @see #version() v
     */
    @Nullable
    FieldMeta<T> tryVersion();

    /**
     * @see #visible()
     */
    @Nullable
    FieldMeta<T> tryVisible();

}
