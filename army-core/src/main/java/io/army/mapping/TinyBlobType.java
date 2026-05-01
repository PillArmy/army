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

import io.army.criteria.CriteriaException;
import io.army.meta.ServerMeta;
import io.army.sqltype.*;

public final class TinyBlobType extends _ArmyBuildInType implements MappingType.SqlBlob {


    public static TinyBlobType from(final Class<?> javaType) {
        if (javaType != byte[].class) {
            throw errorJavaType(TinyBlobType.class, javaType);
        }
        return INSTANCE;
    }

    public static final TinyBlobType INSTANCE = new TinyBlobType();

    /// private constructor
    private TinyBlobType() {
    }


    @Override
    public Class<?> javaType() {
        return byte[].class;
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        // TODO fix me ,add TinyBlobArrayType
        throw new UnsupportedOperationException();
    }

    @Override
    public DataType map(final ServerMeta meta) {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case MySQL:
                dataType = MySQLType.TINYBLOB;
                break;
            case PostgreSQL:
                dataType = PgType.BYTEA;
                break;
            case SQLite:
                dataType = SQLiteType.BLOB;
                break;
            case Oracle:
                dataType = OracleDataType.BLOB;
                break;
            case H2:
                dataType = H2Type.VARBINARY;
                break;
            default:
                throw MAP_ERROR_HANDLER.apply(this, meta);

        }
        return dataType;
    }


    @Override
    public byte[] beforeBind(DataType dataType, MappingEnv env, final Object source) {
        if (!(source instanceof byte[])) {
            throw PARAM_ERROR_HANDLER.apply(this, dataType, source, null);
        }
        return (byte[]) source;
    }

    @Override
    public byte[] afterGet(DataType dataType, MappingEnv env, final Object source) {
        if (!(source instanceof byte[])) {
            throw ACCESS_ERROR_HANDLER.apply(this, dataType, source, null);
        }
        return (byte[]) source;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof TinyBlobType;
    }
}
