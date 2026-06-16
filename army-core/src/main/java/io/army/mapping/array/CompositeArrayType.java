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
public class CompositeArrayType extends _ArmyBuildInArrayType {

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

    private final CustomType customType;

    private CompositeArrayType(Class<?> javaType) {
        this.javaType = javaType;
        this.underlyingClass = ArrayUtils.underlyingComponent(javaType);
        this.underlyingType = CompositeType.from(this.underlyingClass);
        this.customType = CustomType.builder()
                .typeName(Objects.requireNonNull(AnnotationUtils.definedTypeNameOf(this.underlyingClass)) + "[]")
                .javaType(this.javaType)
                .componentType(ArmyType.COMPOSITE)
                .elementInstance(this.underlyingType.getCustomType())
                .componentCreateDdl(true)
                .build();

    }


    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }


    @Override
    public final DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = this.customType;
                break;
            case SQLite:
            case MySQL:
            default:
                throw mapError(this, meta);
        }
        return dataType;
    }


    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {

        final StringBuilder tempBuilder = this.underlyingType.createStringBuilder();

        final BiConsumer<Object, StringBuilder> consumer;
        consumer = (element, sqlBuilder) -> {
            tempBuilder.setLength(0); // firstly clear

            CompositeType.serialize(this.underlyingType, env, element, tempBuilder);
            PostgreArrays.encodeElement(tempBuilder, sqlBuilder);
        };
        return PostgreArrays.arrayBeforeBind(source, consumer, dataType, this);
    }


    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        final DataType elementDataType;
        elementDataType = this.underlyingType.map(env.serverMeta());

        final StringBuilder builder = new StringBuilder(30);

        final TextFunction<?> func;
        func = (text, offset, end) -> CompositeType.deserialize(this.underlyingType, elementDataType, env, text, offset, end, builder);
        return PostgreArrays.arrayAfterGet(this, dataType, source, func, builder);
    }

    @Override
    public final Class<?> underlyingJavaType() {
        return this.underlyingClass;
    }

    @Override
    public final MappingType underlyingType() {
        return this.underlyingType;
    }

    @Override
    public final MappingType elementType() {
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
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        return from(ArrayUtils.arrayClassOf(this.javaType));
    }


    @Override
    public final int hashCode() {
        return Objects.hash(this.javaType);
    }

    @Override
    public final boolean equals(final Object obj) {
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
