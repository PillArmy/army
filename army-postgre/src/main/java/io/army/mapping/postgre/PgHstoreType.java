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

package io.army.mapping.postgre;

import io.army.criteria.CriteriaException;
import io.army.criteria.impl._MappingFactory;
import io.army.dialect.Database;
import io.army.dialect.LiteralHandler;
import io.army.dialect.UnsupportedDialectException;
import io.army.dialect._Constant;
import io.army.executor.DataAccessException;
import io.army.lang.Nullable;
import io.army.mapping.*;
import io.army.mapping.array.PostgreArrays;
import io.army.mapping.postgre.array.PgHstoreArrayType;
import io.army.meta.ServerMeta;
import io.army.pojo.ObjectAccessor;
import io.army.pojo.ObjectAccessorFactory;
import io.army.serialize.NoBoundaryPairDeserializer;
import io.army.sqltype.ArmyType;
import io.army.sqltype.CustomType;
import io.army.sqltype.DataType;
import io.army.struct.DefinedType;
import io.army.util.ArrayUtils;
import io.army.util.ClassUtils;
import io.army.util.FuncClassValue;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/// Mapping type for PostgreSQL HSTORE key/value datatype.
///
/// @see <a href="https://www.postgresql.org/docs/current/hstore.html">hstore key/value datatype</a>
@SuppressWarnings("unused")
public abstract class PgHstoreType extends _ArmyBuildInType implements MappingType.SqlUserDefined {

    public static PgHstoreType from(Class<?> javaType) {
        return HstorePojoType.CLASS_VALUE.get(javaType);
    }


    public static PgHstoreType fromMap(Class<?> keyClass, Class<?> valueClass) {
        return fromMapType(keyClass, valueClass, HstoreMapType::new);
    }

    public static PgHstoreType fromEnumMap(Class<?> keyClass, Class<?> valueClass) {
        if (!Enum.class.isAssignableFrom(keyClass)) {
            throw errorJavaType(PgHstoreType.class, keyClass);
        }
        return fromMapType(ClassUtils.enumClass(keyClass), valueClass, HstoreEnumMapType::new);
    }


    private static PgHstoreType fromMapType(Class<?> keyClass, Class<?> valueClass,
                                            BiFunction<MappingType, MappingType, AbstractHstoreMapType> constructor) {
        final MappingType keyType, valueType;
        keyType = _MappingFactory.getDefaultIfMatch(keyClass);
        if (keyType == null) {
            throw errorJavaType(PgHstoreType.class, keyClass);
        } else if (keyType instanceof CompositeMappingType) {
            throw errorJavaType(PgHstoreType.class, keyClass);
        }

        valueType = _MappingFactory.getDefaultIfMatch(valueClass);
        if (valueType == null) {
            throw errorJavaType(PgHstoreType.class, valueClass);
        } else if (valueType instanceof CompositeMappingType) {
            final DefinedType definedType = valueClass.getAnnotation(DefinedType.class);
            assert definedType != null;
            if (!definedType.immutable()) {
                throw errorJavaType(PgHstoreType.class, valueClass);
            }
        }
        return constructor.apply(keyType, valueType);
    }


    // public static final PgHstoreType INSTANCE = new PgHstoreType();

    private static final NoBoundaryPairDeserializer DESERIALIZER = NoBoundaryPairDeserializer.builder()
            .dataTypeLabel("PostgreSQL hstore")
            .pairSeparator("=>")
            .delim(_Constant.COMMA)

            .backSlashEscapeOn(true)
            .quoteEscapeOn(false)
            .quoteChar(_Constant.DOUBLE_QUOTE)

            .allowQuote(true)
            .allowNothing(false)
            .allowWhitespace(false)
            .nullAsNull(true)

            .build();


    final Class<?> javaType;

    /// private constructor
    private PgHstoreType(Class<?> javaType) {
        this.javaType = javaType;
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
        return obtainDataType();
    }

    @Override
    public final String beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        if (!this.javaType.isInstance(source)) {
            throw paramError(this, dataType, source, null);
        }
        final int fieldCount;
        if (this instanceof AbstractHstoreMapType) {
            fieldCount = ((Map<?, ?>) source).size();
        } else if (this instanceof HstorePojoType) {
            fieldCount = ((HstorePojoType) this).typeMap.size();
        } else {
            throw paramError(this, dataType, source, null);
        }

        return serialize(this, dataType, env, source, new StringBuilder(fieldCount * 15))
                .toString();
    }


    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        final Object value;
        if (isInstance(source)) {
            value = source;
        } else if (source instanceof String text) {
            value = deserialize(this, dataType, env, text, 0, text.length(), null);
        } else {
            throw dataAccessError(this, dataType, source, null);
        }
        return value;
    }


    public abstract MappingType keyType();

    public abstract boolean hasValueType(String keyName);

    public abstract MappingType valueType(String keyName);


    abstract CustomType obtainDataType();

    abstract boolean isInstance(Object source);

    abstract void serialize(DataType dataType, MappingEnv env, Object source, StringBuilder builder);

    abstract Object newInstance();

    abstract BiConsumer<CharSequence, CharSequence> createDeserializeConsumer(Object newInstance, DataType dataType, MappingEnv env, final Object source);


    @SuppressWarnings("unused")
    public static StringBuilder serialize(PgHstoreType type, DataType dataType, MappingEnv env,
                                          final Object source, StringBuilder builder) {
        if (!type.javaType.isInstance(source)) {
            throw paramError(type, dataType, source, null);
        }
        try {
            type.serialize(dataType, env, source, builder);
        } catch (CriteriaException e) {
            throw e;
        } catch (Exception e) {
            throw paramError(type, dataType, source, e);
        }
        return builder;
    }


    @SuppressWarnings("unused")
    public static Object deserialize(PgHstoreType type, DataType dataType, MappingEnv env,
                                     final CharSequence source, final int offset, final int endIndex,
                                     @Nullable StringBuilder builder) {
        final Object instance;
        instance = type.newInstance();
        if (offset == endIndex) {
            // PostgreSQL allow empty hstore
            return instance;
        }
        try {
            final BiConsumer<CharSequence, CharSequence> func;
            func = type.createDeserializeConsumer(instance, dataType, env, source);
            DESERIALIZER.deserialize(source, offset, endIndex, func, null, null, builder);
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw dataAccessError(type, dataType, source, e);
        }
        return instance;
    }


    private static IllegalArgumentException keyMustNotBeNull() {
        return new IllegalArgumentException("key must not be null");
    }


    private static abstract class AbstractHstoreMapType extends PgHstoreType implements DualGenericsMapping {

        private final MappingType keyType;

        private final MappingType valueType;

        private AbstractHstoreMapType(Class<?> mapClass, MappingType keyType, MappingType valueType) {
            super(mapClass);
            this.keyType = keyType;
            this.valueType = valueType;
        }

        @Override
        public final MappingType arrayTypeOfThis() throws CriteriaException {
            return PgHstoreArrayType.fromTypeArgs(ArrayUtils.arrayClassOf(this.javaType), firstGenericsType(), this.secondGenericsType());
        }

        @Override
        public final Class<?> firstGenericsType() {
            return this.keyType.javaType();
        }

        @Override
        public final Class<?> secondGenericsType() {
            return this.valueType.javaType();
        }

        @Override
        public final MappingType keyType() {
            return this.keyType;
        }

        @Override
        public final boolean hasValueType(String keyName) {
            // always true
            return true;
        }

        @Override
        public final MappingType valueType(String keyName) {
            return this.valueType;
        }

        @Override
        final void serialize(DataType dataType, MappingEnv env, final Object source, StringBuilder builder) {
            // PostgreSQL allow empty hstore

            final LiteralHandler handler = env.literalHandler();
            final DataType hstoreDataType = obtainDataType();

            Object key, value;
            int count = 0;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) source).entrySet()) {
                if (count > 0) {
                    builder.append(_Constant.COMMA);
                }
                key = e.getKey();
                value = e.getValue();
                if (key == null) {
                    throw paramError(this, dataType, source, keyMustNotBeNull());
                }

                handler.safeLiteral(this.keyType, key, false, builder, hstoreDataType);
                builder.append("=>");
                if (value == null) {
                    builder.append("null");
                } else {
                    handler.safeLiteral(this.valueType, value, false, builder, hstoreDataType);
                }

                count++;

            } // loop

        }


        @Override
        final BiConsumer<CharSequence, CharSequence> createDeserializeConsumer(Object newInstance, DataType dataType, MappingEnv env, final Object source) {

            final DataType keyDataType, valueDataType;
            keyDataType = this.keyType.map(env.serverMeta());
            valueDataType = this.valueType.map(env.serverMeta());

            @SuppressWarnings("unchecked") final Map<Object, Object> map = (Map<Object, Object>) newInstance;

            return (keyStr, valueStr) -> {

                if (keyStr != null && !(keyStr instanceof String)) {
                    keyStr = keyStr.toString();
                }
                if (valueStr != null && !(valueStr instanceof String)) {
                    valueStr = valueStr.toString();
                }

                Object key, value;
                if (keyStr == null) {
                    key = null;
                } else {
                    key = this.keyType.afterGet(keyDataType, env, keyStr);
                    if (key == MappingType.DOCUMENT_NULL_VALUE) {
                        key = null;
                    }
                }

                if (key == null) {
                    throw dataAccessError(this, dataType, source, keyMustNotBeNull());
                }

                if (valueStr == null) {
                    value = null;
                } else {
                    value = this.valueType.afterGet(valueDataType, env, valueStr);
                    if (value == MappingType.DOCUMENT_NULL_VALUE) {
                        value = null;
                    }
                }
                map.put(key, value);

            };
        }


    } // AbstractHstoreMapType

    private static final class HstoreMapType extends AbstractHstoreMapType {

        private static final CustomType DATA_TYPE = CustomType.builder()
                .typeName("HSTORE")
                .javaType(Map.class)
                .componentType(ArmyType.DIALECT_TYPE)
                .componentCreateDdl(false)
                .build();

        private HstoreMapType(MappingType keyType, MappingType valueType) {
            super(Map.class, keyType, valueType);
        }


        @Override
        public int hashCode() {
            return Objects.hash(this.javaType, this.firstGenericsType(), this.secondGenericsType());
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof HstoreMapType o) {
                match = o.javaType == this.javaType
                        && o.firstGenericsType() == this.firstGenericsType()
                        && o.secondGenericsType() == this.secondGenericsType();
            } else {
                match = false;
            }
            return match;
        }

        @Override
        CustomType obtainDataType() {
            return DATA_TYPE;
        }

        @Override
        Object newInstance() {
            return new HashMap<>();
        }

        @Override
        boolean isInstance(Object source) {
            if (!(source instanceof Map<?, ?>)) {
                return false;
            }

            final Class<?> keyClass, valueClass;
            keyClass = firstGenericsType();
            valueClass = secondGenericsType();

            boolean match = true;
            Object key, value;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) source).entrySet()) {
                key = e.getKey();

                if (!keyClass.isInstance(key)) {
                    match = false;
                    break;
                }

                value = e.getValue();
                if (value == null) {
                    continue;
                }

                if (!valueClass.isInstance(value)) {
                    match = false;
                    break;
                }

            }
            return match;
        }


    } // HstoreMapType

    private static final class HstoreEnumMapType extends AbstractHstoreMapType {

        private static final CustomType DATA_TYPE = CustomType.builder()
                .typeName("HSTORE")
                .javaType(EnumMap.class)
                .componentType(ArmyType.DIALECT_TYPE)
                .componentCreateDdl(false)
                .build();

        private HstoreEnumMapType(MappingType keyType, MappingType valueType) {
            super(EnumMap.class, keyType, valueType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.javaType, this.firstGenericsType(), this.secondGenericsType());
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof HstoreEnumMapType o) {
                match = o.javaType == this.javaType
                        && o.firstGenericsType() == this.firstGenericsType()
                        && o.secondGenericsType() == this.secondGenericsType();
            } else {
                match = false;
            }
            return match;
        }

        @Override
        CustomType obtainDataType() {
            return DATA_TYPE;
        }


        @Override
        Object newInstance() {
            return newEnumMap(firstGenericsType());
        }

        @SuppressWarnings({"unchecked"})
        private <E extends Enum<E>> EnumMap<E, Object> newEnumMap(Class<?> enumClass) {
            return new EnumMap<>((Class<E>) enumClass);
        }

        @Override
        boolean isInstance(Object source) {
            if (!(source instanceof EnumMap<?, ?>)) {
                return false;
            }

            final Class<?> keyClass, valueClass;
            keyClass = firstGenericsType();
            valueClass = secondGenericsType();

            boolean match = true;
            Object key, value;
            for (Map.Entry<?, ?> e : ((EnumMap<?, ?>) source).entrySet()) {
                key = e.getKey();

                if (!(key instanceof Enum<?>)) {
                    match = false;
                    break;
                } else if (!keyClass.isInstance(key)) {
                    match = false;
                    break;
                }

                value = e.getValue();
                if (value == null) {
                    continue;
                }

                if (!valueClass.isInstance(value)) {
                    match = false;
                    break;
                }

            }
            return match;
        }


    } // HstoreEnumMapType


    private static final class HstorePojoType extends PgHstoreType {

        private static HstorePojoType fromPojo(final Class<?> javaType) {
            final Map<String, MappingType> typeMap;
            typeMap = _MappingFactory.createPojoMappingTypeMap(javaType);
            if (typeMap.isEmpty()) {
                String m = String.format("%s have no field", javaType.getName());
                throw new IllegalArgumentException();
            }
            return new HstorePojoType(javaType, typeMap);
        }

        private static final ClassValue<HstorePojoType> CLASS_VALUE = FuncClassValue.create(HstorePojoType::fromPojo);


        private final Supplier<?> constructor;

        private final ObjectAccessor accessor;

        private final Map<String, MappingType> typeMap;

        private final CustomType dataType;

        private HstorePojoType(Class<?> javaType, Map<String, MappingType> typeMap) {
            super(javaType);
            this.constructor = ObjectAccessorFactory.pojoConstructor(javaType);
            this.accessor = ObjectAccessorFactory.forPojo(javaType);
            this.typeMap = Map.copyOf(typeMap);

            this.dataType = CustomType.builder()
                    .typeName("HSTORE")
                    .javaType(javaType)
                    .componentType(ArmyType.DIALECT_TYPE)
                    .componentCreateDdl(false)
                    .build();
        }

        @Override
        public MappingType arrayTypeOfThis() throws CriteriaException {
            return PgHstoreArrayType.from(ArrayUtils.arrayClassOf(this.javaType));
        }

        @Override
        public MappingType keyType() {
            return StringType.INSTANCE;
        }

        @Override
        public boolean hasValueType(String keyName) {
            return this.typeMap.get(keyName) != null;
        }

        @Override
        public MappingType valueType(String keyName) {
            final MappingType type;
            type = this.typeMap.get(keyName);
            if (type == null) {
                String m = String.format("HstorePojoType key %s not found", keyName);
                throw new IllegalArgumentException(keyName);
            }
            return type;
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
            } else if (obj instanceof HstorePojoType o) {
                match = o.javaType == this.javaType;
            } else {
                match = false;
            }
            return match;
        }


        @Override
        CustomType obtainDataType() {
            return this.dataType;
        }

        @Override
        Object newInstance() {
            return this.constructor.get();
        }

        @Override
        boolean isInstance(Object source) {
            return this.javaType.isInstance(source);
        }

        @Override
        BiConsumer<CharSequence, CharSequence> createDeserializeConsumer(Object newInstance, DataType dataType, MappingEnv env, Object source) {

            return (keyStr, valueStr) -> {
                if (keyStr == null) {
                    throw dataAccessError(this, dataType, source, keyMustNotBeNull());
                } else if (!(keyStr instanceof String)) {
                    keyStr = keyStr.toString();
                }


                final MappingType type;
                type = this.typeMap.get(keyStr);
                if (type == null) {
                    return;
                }

                Object value;
                if (valueStr == null) {
                    value = null;
                } else {
                    if (!(valueStr instanceof String)) {
                        valueStr = valueStr.toString();
                    }
                    value = type.afterGet(type.map(env.serverMeta()), env, valueStr);
                    if (value == MappingType.DOCUMENT_NULL_VALUE) {
                        value = null;
                    }
                }

                this.accessor.set(newInstance, (String) keyStr, value);
            };
        }

        @Override
        void serialize(DataType dataType, MappingEnv env, final Object source, StringBuilder builder) {
            // PostgreSQL allow empty hstore

            final LiteralHandler handler = env.literalHandler();

            final ObjectAccessor accessor;
            accessor = this.accessor;

            String fieldName;
            Object value;
            int count = 0;
            for (Map.Entry<String, MappingType> e : typeMap.entrySet()) {
                if (count > 0) {
                    builder.append(_Constant.COMMA);
                }

                fieldName = e.getKey();
                PostgreArrays.encodeElement(fieldName, builder);

                builder.append("=>");

                value = accessor.get(source, fieldName);
                if (value == null) {
                    builder.append("null");
                } else {
                    handler.safeLiteral(e.getValue(), value, false, builder, this.dataType);
                }

                count++;

            } // loop

        }


    } // HstorePojoType


}
