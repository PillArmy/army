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
import io.army.mapping._ArmyBuildInArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.ArmyType;
import io.army.sqltype.CustomType;
import io.army.sqltype.DataType;
import io.army.struct.DefinedType;
import io.army.struct.TypeCategory;
import io.army.util.AnnotationUtils;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.util.Objects;
import java.util.function.BiConsumer;

/// Array mapping type for composite type elements.
///
/// @see io.army.mapping.CompositeType
public final class CompositeArrayType extends _ArmyBuildInArrayType implements MappingType.SqlUserDefined {

    public static CompositeArrayType from(final Class<?> javaType) {
        if (!javaType.isArray()) {
            throw errorJavaType(CompositeArrayType.class, javaType);
        }
        final DefinedType definedType;
        definedType = ArrayUtils.underlyingComponent(javaType).getAnnotation(DefinedType.class);
        if (definedType == null || definedType.category() != TypeCategory.COMPOSITE) {
            throw errorJavaType(CompositeArrayType.class, javaType);
        }
        return CLASS_VALUE.get(javaType);
    }


    private static final ClassValue<CompositeArrayType> CLASS_VALUE = FuncClassValue.create(CompositeArrayType::new);


    private final Class<?> javaType;

    private final Class<?> underlyingClass;

    private final CompositeType underlyingType;

    private final DataType dataType;

    private CompositeArrayType(Class<?> javaType) {
        this.javaType = javaType;
        this.underlyingClass = ArrayUtils.underlyingComponent(javaType);
        this.underlyingType = CompositeType.from(this.underlyingClass);
        this.dataType = CustomType.builder()
                .typeName(Objects.requireNonNull(AnnotationUtils.definedTypeNameOf(this.underlyingClass)) + "[]")
                .javaType(this.javaType)
                .componentType(ArmyType.COMPOSITE)
                .componentCreateDdl(true)
                .build();

    }


    @Override
    public Class<?> javaType() {
        return this.javaType;
    }


    @Override
    public DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = this.dataType;
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
            final StringBuilder tempBuilder = new StringBuilder();
            CompositeType.bindToLiteral(this.underlyingType, elementDataType, env, element, tempBuilder);
            PostgreArrays.encodeElement(tempBuilder.toString(), sqlBuilder);
        };
        return PostgreArrays.arrayBeforeBind(source, consumer, dataType, this);
    }

    @Override
    public Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        final DataType elementDataType;
        elementDataType = this.underlyingType.map(env.serverMeta());
        final TextFunction<?> func;
        func = (text, offset, end) -> CompositeType.parseToPojo(this.underlyingType, elementDataType, env, text, offset, end);
        return PostgreArrays.arrayAfterGet(this, dataType, source, func);
    }

    @Override
    public Class<?> underlyingJavaType() {
        return this.underlyingClass;
    }

    @Override
    public MappingType underlyingType() {
        return this.underlyingType;
    }

    @Override
    public MappingType elementType() {
        final Class<?> componentType;
        final MappingType instance;
        if ((componentType = this.javaType.getComponentType()).isArray()) {
            instance = from(componentType);
        } else {
            instance = this.underlyingType;
        }
        return instance;
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return from(ArrayUtils.arrayClassOf(this.javaType));
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.javaType);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof CompositeArrayType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }


}
