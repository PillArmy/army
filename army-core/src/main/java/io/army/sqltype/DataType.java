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

package io.army.sqltype;

import io.army.criteria.TypeDef;
import io.army.lang.Nullable;
import io.army.meta.DatabaseTypeObject;

/// This is base interface of following:
///
/// - {@link SQLType}
/// - {@link CustomType}
///
/// @since 0.6.0
public interface DataType extends TypeDef, DatabaseTypeObject {


    /// SQL type's alias (not type name) in Java language.
    ///
    /// @see #typeName()
    /// @see Enum#name()
    String name();


    boolean isUnknown();

    boolean isArray();

    ArmyType armyType();

    String componentTypeName();

    @Override
    default String objectName() {
        return typeName();
    }

    default String safeTypeAlias() {
        return typeName();
    }

    ///
    /// For example:
    ///
    /// - one dimension BIGINT_ARRAY return BIGINT
    /// - tow dimension BIGINT_ARRAY return BIGINT too
    ///
    /// @return element type of array(1-n dimension)
    @Nullable
    DataType elementType();

    TypeDef parens(long precision);

    TypeDef parens(int precision, int scale);

    static boolean isArrayTypeName(String typeName) {
        return typeName.endsWith("[]") || typeName.startsWith("_");
    }


}
