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
import io.army.mapping.XmlType;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.util.Objects;

public class XmlArrayType extends _ArmyBuildInArrayType {


    public static XmlArrayType from(final Class<?> javaType) {
        final XmlArrayType instance;
        if (javaType == String[].class) {
            instance = TEXT_LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(XmlArrayType.class, javaType);
        } else if (ArrayUtils.underlyingComponent(javaType) == String.class) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(XmlArrayType.class, javaType);
        }
        return instance;
    }

    public static final XmlArrayType UNLIMITED = new XmlArrayType(Object.class);

    public static final XmlArrayType TEXT_LINEAR = new XmlArrayType(String[].class);

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
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        if (meta.serverDatabase() != Database.PostgreSQL) {
            throw MAP_ERROR_HANDLER.apply(this, meta);
        }
        return PgType.XML_ARRAY;
    }

    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public final Class<?> underlyingJavaType() {
        return this.underlyingType;
    }

    @Override
    public final MappingType underlyingType() {
        return XmlType.from(this.underlyingType);
    }

    @Override
    public final MappingType elementType() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.javaType, this.underlyingType);
    }

    @Override
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof XmlArrayType o) {
            match = o.javaType == this.javaType
                    && o.underlyingType == this.underlyingType;
        } else {
            match = false;
        }
        return match;
    }


    private static final class ClassValueHolder {

        private static final ClassValue<XmlArrayType> CLASS_VALUE = FuncClassValue.create(XmlArrayType::new);

    }


}
