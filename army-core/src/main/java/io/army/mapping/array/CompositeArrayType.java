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
import io.army.dialect.UnsupportedDialectException;
import io.army.executor.DataAccessException;
import io.army.function.TextFunction;
import io.army.mapping.CompositeType;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping._ArmyBuildInType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.struct.DefinedType;
import io.army.util.ArrayUtils;

import java.util.Objects;
import java.util.function.BiConsumer;

/// @see io.army.mapping.CompositeType
public class CompositeArrayType extends _ArmyBuildInType implements MappingType.SqlArray, MappingType.SqlUserDefined {

    public static CompositeArrayType from(final Class<?> arrayClass) {
        if (!arrayClass.isArray()) {
            throw errorJavaType(CompositeArrayType.class, arrayClass);
        }
        if (ArrayUtils.underlyingComponent(arrayClass).getAnnotation(DefinedType.class) == null) {
            throw errorJavaType(CompositeArrayType.class, arrayClass);
        }
        return CLASS_VALUE.get(arrayClass);
    }


    private static final ClassValue<CompositeArrayType> CLASS_VALUE = new ClassValue<>() {
        @Override
        protected CompositeArrayType computeValue(Class<?> type) {
            return new CompositeArrayType(type);
        }
    };


    private final Class<?> arrayClass;

    private final Class<?> underlyingClass;

    private final CompositeType underlyingType;

    private CompositeArrayType(Class<?> arrayClass) {
        this.arrayClass = arrayClass;
        this.underlyingClass = ArrayUtils.underlyingComponent(arrayClass);
        this.underlyingType = CompositeType.from(this.underlyingClass);

    }


    @Override
    public Class<?> javaType() {
        return this.arrayClass;
    }

    @Override
    public String typeName() {
        return this.underlyingType.typeName() + "[]";
    }

    @Override
    public DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = DataType.from(typeName());
                break;
            case SQLite:
            case MySQL:
            default:
                throw mapError(this, meta);
        }
        return dataType;
    }


    @Override
    public Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        final DataType elementDataType;
        elementDataType = this.underlyingType.map(env.serverMeta());
        final BiConsumer<Object, StringBuilder> consumer;
        consumer = (element, sqlBuilder) -> {
            CompositeType.bindToLiteral(this.underlyingType, elementDataType, env, element, sqlBuilder);
        };
        return PostgreArrays.arrayBeforeBind(source, consumer, dataType, this);
    }

    @Override
    public Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        final DataType elementDataType;
        elementDataType = this.underlyingType.map(env.serverMeta());
        final TextFunction<?> func;
        func = (text, offset, end) -> CompositeType.parseToPojo(this.underlyingType, elementDataType, env, text, offset, end);
        return PostgreArrays.arrayAfterGet(this, dataType, source, false, func);
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        final Class<?> javaType = this.arrayClass;
        if (javaType == Object.class) { // unlimited dimension array
            return this;
        }
        return from(ArrayUtils.arrayClassOf(javaType));
    }

    @Override
    public Class<?> underlyingJavaType() {
        return this.underlyingClass;
    }

    @Override
    public MappingType elementType() {
        final Class<?> javaType = this.arrayClass, componentType;
        final MappingType instance;

        if (javaType == Object.class) {
            instance = this;
        } else if ((componentType = javaType.getComponentType()).isArray()) {
            instance = from(componentType);
        } else {
            instance = CompositeType.from(componentType);
        }
        return instance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.arrayClass);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof CompositeArrayType o) {
            match = o.arrayClass == this.arrayClass;
        } else {
            match = false;
        }
        return match;
    }


}
