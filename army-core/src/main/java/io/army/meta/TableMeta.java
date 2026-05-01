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

import io.army.criteria.TabularItem;
import io.army.lang.Nullable;
import io.army.util.SQLStmts;

import java.util.List;

/// @see SchemaMeta
/// @see FieldMeta
/// @see IndexMeta
/// @see IndexFieldMeta
public interface TableMeta<T> extends TabularItem, DatabaseObject.TypeObject {


    Class<T> javaType();

    /// 
/// Table name,Equivalence : {@link  FieldMeta#objectName()}
    String tableName();

    boolean immutable();

    boolean allColumnNotNull();

    String comment();

    PrimaryFieldMeta<T> id();

    FieldMeta<? super T> createTime();

    /// @throws IllegalArgumentException throw when no this field
/// @see #tryUpdateTime()
    FieldMeta<? super T> updateTime();

    /// @throws IllegalArgumentException throw when no this field
/// @see #tryVersion()
    FieldMeta<? super T> version();

    /// @throws IllegalArgumentException throw when no this field
/// @see #tryVisible()
    FieldMeta<? super T> visible();

    /// @see #updateTime()
    @Nullable
    FieldMeta<? super T> tryUpdateTime();

    /// @see #version() v
    @Nullable
    FieldMeta<? super T> tryVersion();

    /// @see #visible()
    @Nullable
    FieldMeta<? super T> tryVisible();


    PrimaryFieldMeta<? super T> nonChildId();


    @Nullable
    FieldMeta<? super T> discriminator();

    @Nullable
    Enum<?> discriminatorValue();


    /// contain primary key
    List<IndexMeta<T>> indexList();

    /// @return unmodified list, always same instance.
/// @see SQLStmts#castFieldList(TableMeta)
    List<FieldMeta<T>> fieldList();


    String tableOptions();

    String partitionOptions();

    SchemaMeta schema();

    boolean containField(String fieldName);

    boolean containComplexField(String fieldName);

    boolean isThisField(FieldMeta<?> field);

    boolean isComplexField(FieldMeta<?> field);

    /// @throws IllegalArgumentException when not found matched {@link FieldMeta} for fieldName
    FieldMeta<T> field(String fieldName);

    @Nullable
    FieldMeta<T> tryField(String fieldName);


    FieldMeta<? super T> complexFiled(String filedName);

    @Nullable
    FieldMeta<? super T> tryComplexFiled(String filedName);

    /// @throws IllegalArgumentException when not found matched {@link IndexFieldMeta} for fieldName
    IndexFieldMeta<T> indexField(String fieldName);

    /// @throws IllegalArgumentException when not found matched {@link UniqueFieldMeta} for fieldName
    UniqueFieldMeta<T> uniqueField(String fieldName);

    List<FieldMeta<?>> fieldChain();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    String toString();


}
