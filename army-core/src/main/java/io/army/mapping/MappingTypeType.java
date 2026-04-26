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
import io.army.mapping.array.MappingTypeArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.util.ArrayUtils;

import java.util.Objects;

/// Map {@link MappingType} to SQL string
public final class MappingTypeType extends _ArmyBuildInType implements MappingType.SqlString {

    public static MappingTypeType from(Class<?> javaType) {
        if (MappingType.class.isAssignableFrom(javaType)) {
            throw errorJavaType(MappingTypeType.class, javaType);
        }
        return INSTANCE;
    }


    public static final MappingTypeType INSTANCE = new MappingTypeType();

    /// private constructor
    private MappingTypeType() {
    }

    @Override
    public Class<?> javaType() {
        return MappingType.class;
    }

    @Override
    public DataType map(ServerMeta meta) throws UnsupportedDialectException {
        return StringType.mapToDataType(this, meta);
    }

    @Override
    public String beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        if (!(source instanceof MappingType m)) {
            throw paramError(this, dataType, source, null);
        }
        return m.map(env.serverMeta()).typeName();
    }

    @Override
    public MappingType afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        if (source instanceof MappingType) {
            return (MappingType) source;
        }
        if (!(source instanceof String s)) {
            throw dataAccessError(this, dataType, source, null);
        }
        final MappingType[] holder = new MappingType[1];

        try {
            env.typeMapFunc().apply(s, holder, 0);
        } catch (IllegalArgumentException e) {
            throw dataAccessError(this, dataType, source, e);
        }

        return Objects.requireNonNull(holder[0]);
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return MappingTypeArrayType.from(ArrayUtils.arrayClassOf(MappingType.class));
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof MappingTypeType;
    }


}
