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
import io.army.dialect.UnsupportedDialectException;
import io.army.executor.DataAccessException;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.transaction.Isolation;


public final class IsolationType extends _ArmyBuildInType implements MappingType.SqlString {


    public static IsolationType from(Class<?> javaType) {
        if (javaType != Isolation.class) {
            throw errorJavaType(IsolationType.class, javaType);
        }
        return INSTANCE;
    }

    public static final IsolationType INSTANCE = new IsolationType();

    /// private constructor
    private IsolationType() {
    }

    @Override
    public Class<?> javaType() {
        return Isolation.class;
    }

    @Override
    public DataType map(ServerMeta meta) throws UnsupportedDialectException {
        return StringType.mapToDataType(this, meta);
    }

    @Override
    public String beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        if (!(source instanceof Isolation)) {
            throw paramError(this, dataType, source, null);
        }
        return ((Isolation) source).name();
    }

    @Override
    public Isolation afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        final Isolation value;
        if (source instanceof Isolation) {
            value = (Isolation) source;
        } else if (source instanceof String) {
            try {
                value = env.mappingHandler().nameToIsolation((String) source);
            } catch (Exception e) {
                throw dataAccessError(this, dataType, source, e);
            }
        } else {
            throw dataAccessError(this, dataType, source, null);
        }
        return value;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof IsolationType;
    }


}
