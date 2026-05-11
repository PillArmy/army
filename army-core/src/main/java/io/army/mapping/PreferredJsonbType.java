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

package io.army.mapping;

import io.army.dialect.UnsupportedDialectException;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.MySQLType;
import io.army.sqltype.PgType;
import io.army.sqltype.SQLType;

/// Maps the Java type to JSONB by default if supported, otherwise uses JSON.
public final class PreferredJsonbType extends ArmyJsonType {

    public static PreferredJsonbType from(final Class<?> javaType) {
        return CLASS_VALUE.get(javaType);
    }


    private static final ClassValue<PreferredJsonbType> CLASS_VALUE = new ClassValue<>() {
        @Override
        protected PreferredJsonbType computeValue(Class<?> type) {
            return new PreferredJsonbType(type);
        }
    };


    private PreferredJsonbType(Class<?> javaType) {
        super(javaType);
    }

    @Override
    public DataType map(ServerMeta meta) throws UnsupportedDialectException {
        final SQLType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = PgType.JSONB;
                break;
            case MySQL:
                dataType = MySQLType.JSON;
                break;
            case Oracle:
            case H2:
            default:
                throw mapError(this, meta);
        }
        return dataType;
    }


}
