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

package io.army.mapping.array;

import io.army.criteria.CriteriaException;
import io.army.dialect.Database;
import io.army.dialect.UnsupportedDialectException;
import io.army.executor.DataAccessException;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

public final class XmlArrayType extends _ArmyBuildInArrayType {


    public static XmlArrayType from(final Class<?> javaType) {
        if (!javaType.isArray()) {
            throw errorJavaType(XmlArrayType.class, javaType);
        }
        return CLASS_VALUE.get(javaType);
    }

    public static final XmlArrayType UNLIMITED = new XmlArrayType(Object.class);

    public static final XmlArrayType TEXT_LINEAR = new XmlArrayType(String[].class);

    private static final ClassValue<XmlArrayType> CLASS_VALUE = FuncClassValue.create(XmlArrayType::new);


    private final Class<?> javaType;

    private final Class<?> underlyingType;

    private XmlArrayType(Class<?> javaType) {
        this.javaType = javaType;
        if (javaType == Object.class) {
            this.underlyingType = String.class;
        } else {
            this.underlyingType = ArrayUtils.underlyingComponent(javaType);
        }
    }

    @Override
    public Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        if (meta.serverDatabase() != Database.PostgreSQL) {
            throw MAP_ERROR_HANDLER.apply(this, meta);
        }
        return PgType.VARCHAR_ARRAY;
    }

    @Override
    public Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> underlyingJavaType() {
        return this.underlyingType;
    }

    @Override
    public MappingType elementType() {
        // TODO
        throw new UnsupportedOperationException();
    }


}
