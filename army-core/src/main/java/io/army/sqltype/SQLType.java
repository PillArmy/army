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

import io.army.ArmyException;
import io.army.dialect.Database;
import io.army.lang.Nullable;
import io.army.mapping.MappingType;

import java.util.function.Supplier;

public sealed interface SQLType extends DataType
        permits PgType, MySQLType, SQLiteType, OracleDataType, H2Type {

    Database database();

    @Override
    default Class<?> javaType() {
        return firstJavaType();
    }

    Class<?> firstJavaType();

    @Nullable
    Class<?> secondJavaType();


    ///
    /// For example:
    ///
    /// - one dimension BIGINT_ARRAY return BIGINT
    /// - tow dimension BIGINT_ARRAY return BIGINT too
    ///
    /// @return element type of array(1-n dimension)
    @Nullable
    @Override
    SQLType elementType();

    @Override
    default String componentTypeName() {
        final SQLType type;
        type = elementType();
        String name;
        if (type == null) {
            name = typeName();
        } else {
            name = type.typeName();
        }
        return name;
    }


    default MappingType mapType(Supplier<? extends ArmyException> errorHandler) {
        throw new UnsupportedOperationException();
    }


}
