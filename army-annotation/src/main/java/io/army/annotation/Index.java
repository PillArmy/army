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

package io.army.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/// @since 0.6.0
@Target({})
@Retention(RUNTIME)
public @interface Index {

    /// (Optional) The name of the indexMap; defaults to a provider-generated name.
    ///
    /// see {@code io.army.criteria.impl.TableMetaUtils#parseIndexName(io.army.meta.TableMeta, java.lang.Class, io.army.annotation.Index, int, io.army.criteria.impl.MetaContext)}
    String name();

    /// (Optional) The names of index field .
    /// in asSort.
    String[] fieldList() default {};

    /// (Optional) The fields of index.
    ///
    /// Field takes precedence over {@link #fieldList()}.
    IndexField[] fields() default {};

    /// (Optional) Whether the indexMap is unique.
    boolean unique() default false;

    /// - MySQL: BTREE,HASH,FULLTEXT,SPATIAL
    /// - PostgreSQL: btree, hash, gist, spgist, gin, brin
    ///
    /// see {@code io.army.criteria.impl.TableMetaUtils#parseIndexType(java.lang.Class, java.lang.String, int, io.army.criteria.impl.MetaContext)}
    String type() default "";

}
