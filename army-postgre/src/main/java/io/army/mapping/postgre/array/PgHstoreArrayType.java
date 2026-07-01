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

package io.army.mapping.postgre.array;

import io.army.criteria.CriteriaException;
import io.army.dialect.Database;
import io.army.dialect.UnsupportedDialectException;
import io.army.executor.DataAccessException;
import io.army.function.TextFunction;
import io.army.mapping.DualGenericsMapping;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.mapping.array.PostgreArrays;
import io.army.mapping.postgre.PgHstoreType;
import io.army.meta.ServerMeta;
import io.army.sqltype.ArmyType;
import io.army.sqltype.CustomType;
import io.army.sqltype.DataType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/// Mapping type for PostgreSQL HSTORE key/value array type.
///
/// @see <a href="https://www.postgresql.org/docs/current/hstore.html">hstore key/value datatype</a>
public abstract class PgHstoreArrayType extends _ArmyBuildInArrayType {

    public static PgHstoreArrayType from(Class<?> javaType) {
        if (!javaType.isArray()) {
            throw errorJavaType(PgHstoreArrayType.class, javaType);
        }
        return PgHstorePojoArrayType.CLASS_VALUE.get(javaType);
    }


    public static PgHstoreArrayType fromTypeArgs(Class<?> javaType, Class<?> keyClass, Class<?> valueClass) {
        if (!javaType.isArray()) {
            throw errorJavaType(PgHstoreArrayType.class, javaType);
        }
        final Class<?> underlyingClass = ArrayUtils.underlyingComponent(javaType);

        final PgHstoreArrayType instance;
        if (underlyingClass == Map.class) {
            instance = new PgHstoreMapArrayType(javaType, PgHstoreType.fromMap(keyClass, valueClass));
        } else if (underlyingClass == EnumMap.class) {
            instance = new PgHstoreEnumMapArrayType(javaType, PgHstoreType.fromEnumMap(keyClass, valueClass));
        } else {
            throw errorJavaType(PgHstoreArrayType.class, javaType);
        }
        return instance;
    }

    private static final DataType DATA_TYPE = CustomType.builder()
            .typeName("HSTORE[]")
            .javaType(Object.class)
            .componentType(ArmyType.DIALECT_TYPE)
            .componentCreateDdl(false)
            .build();


    final Class<?> javaType;

    final PgHstoreType underlyingType;

    /// private constructor
    private PgHstoreArrayType(Class<?> javaType, PgHstoreType underlyingType) {
        this.javaType = javaType;
        this.underlyingType = underlyingType;
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        if (meta.serverDatabase() != Database.PostgreSQL) {
            throw mapError(this, meta);
        }
        return DATA_TYPE;
    }

    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        final StringBuilder tempBuilder = new StringBuilder(128);

        final BiConsumer<Object, StringBuilder> consumer;
        consumer = (element, sqlBuilder) -> {
            tempBuilder.setLength(0); // firstly clear

            PgHstoreType.serialize(this.underlyingType, dataType, env, element, tempBuilder);
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
        func = (text, offset, end) -> PgHstoreType.deserialize(this.underlyingType, elementDataType, env, text, offset, end, builder);
        return PostgreArrays.arrayAfterGet(this, dataType, source, func, builder);
    }

    @Override
    public final Class<?> underlyingJavaType() {
        return this.underlyingType.javaType();
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
            if (this instanceof AbstractPgHstoreMapArrayType pa) {
                instance = fromTypeArgs(componentType, pa.firstGenericsType(), pa.secondGenericsType());
            } else if (this instanceof PgHstorePojoArrayType) {
                instance = from(componentType);
            } else {
                throw new IllegalStateException("bug");
            }
        } else {
            instance = this.underlyingType;
        }
        return instance;
    }


    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        final MappingType instance;
        if (this instanceof AbstractPgHstoreMapArrayType pa) {
            instance = fromTypeArgs(ArrayUtils.arrayClassOf(this.javaType), pa.firstGenericsType(), pa.secondGenericsType());
        } else if (this instanceof PgHstorePojoArrayType) {
            instance = from(ArrayUtils.arrayClassOf(this.javaType));
        } else {
            throw new IllegalStateException("bug");
        }
        return instance;
    }


    private static abstract class AbstractPgHstoreMapArrayType extends PgHstoreArrayType implements DualGenericsMapping {

        private AbstractPgHstoreMapArrayType(Class<?> javaType, PgHstoreType underlyingType) {
            super(javaType, underlyingType);
        }

        @Override
        public final Class<?> firstGenericsType() {
            return ((DualGenericsMapping) this.underlyingType).firstGenericsType();
        }

        @Override
        public final Class<?> secondGenericsType() {
            return ((DualGenericsMapping) this.underlyingType).secondGenericsType();
        }

        @Override
        public final int hashCode() {
            return Objects.hash(this.javaType, firstGenericsType(), secondGenericsType());
        }

        @Override
        public final boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof AbstractPgHstoreMapArrayType o) {
                match = o.getClass() == this.getClass()
                        && o.javaType == this.javaType
                        && o.firstGenericsType() == this.firstGenericsType()
                        && o.secondGenericsType() == this.secondGenericsType();
            } else {
                match = false;
            }
            return match;
        }


    } // AbstractPgHstoreMapArrayType


    private static final class PgHstoreMapArrayType extends AbstractPgHstoreMapArrayType {

        private PgHstoreMapArrayType(Class<?> javaType, PgHstoreType underlyingType) {
            super(javaType, underlyingType);
        }


    } // PgHstoreMapArrayType


    private static final class PgHstoreEnumMapArrayType extends AbstractPgHstoreMapArrayType {

        private PgHstoreEnumMapArrayType(Class<?> javaType, PgHstoreType underlyingType) {
            super(javaType, underlyingType);
        }


    } // PgHstoreEnumMapArrayType


    private static final class PgHstorePojoArrayType extends PgHstoreArrayType {

        private static PgHstorePojoArrayType fromPojo(Class<?> javaType) {
            return new PgHstorePojoArrayType(javaType, PgHstoreType.from(ArrayUtils.underlyingComponent(javaType)));
        }

        private static final ClassValue<PgHstorePojoArrayType> CLASS_VALUE = FuncClassValue.create(PgHstorePojoArrayType::fromPojo);

        private PgHstorePojoArrayType(Class<?> javaType, PgHstoreType underlyingType) {
            super(javaType, underlyingType);
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
            } else if (obj instanceof PgHstorePojoArrayType o) {
                match = o.javaType == this.javaType;
            } else {
                match = false;
            }
            return match;
        }


    } // PgHstorePojoArrayType


}
