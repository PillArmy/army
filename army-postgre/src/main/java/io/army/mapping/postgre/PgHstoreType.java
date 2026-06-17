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
import io.army.dialect.Database;
import io.army.dialect.UnsupportedDialectException;
import io.army.dialect._Constant;
import io.army.executor.DataAccessException;
import io.army.lang.Nullable;
import io.army.mapping.DualGenericsMapping;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping._ArmyBuildInType;
import io.army.mapping.array.PostgreArrays;
import io.army.meta.ServerMeta;
import io.army.serialize.NoBoundaryPairDeserializer;
import io.army.sqltype.ArmyType;
import io.army.sqltype.CustomType;
import io.army.sqltype.DataType;

import java.util.HashMap;
import java.util.Map;

/// Mapping type for PostgreSQL HSTORE key/value datatype.
///
/// @see <a href="https://www.postgresql.org/docs/current/hstore.html">hstore key/value datatype</a>
@SuppressWarnings("unused")
public final class PgHstoreType extends _ArmyBuildInType implements MappingType.SqlUserDefined, DualGenericsMapping {


    public static PgHstoreType fromMap(Class<?> keyClass, Class<?> valueClass) {
        if (keyClass != String.class) {
            throw errorJavaType(PgHstoreType.class, keyClass);
        } else if (valueClass != String.class) {
            throw errorJavaType(PgHstoreType.class, valueClass);
        }
        return INSTANCE;
    }


    public static final PgHstoreType INSTANCE = new PgHstoreType();

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

    private static final DataType DATA_TYPE = CustomType.builder()
            .typeName("HSTORE")
            .javaType(Map.class)
            .componentType(ArmyType.DIALECT_TYPE)
            .componentCreateDdl(false)
            .build();

    /// private constructor
    private PgHstoreType() {
    }

    @Override
    public Class<?> javaType() {
        return Map.class;
    }

    @Override
    public DataType map(ServerMeta meta) throws UnsupportedDialectException {
        if (meta.serverDatabase() != Database.PostgreSQL) {
            throw mapError(this, meta);
        }
        return DATA_TYPE;
    }

    @Override
    public String beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        if (!(source instanceof Map<?, ?> map)) {
            throw paramError(this, dataType, source, new IllegalArgumentException("source must be Map"));
        }
        return serialize(this, dataType, env, map, new StringBuilder(map.size() * 15))
                .toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        final Map<String, String> result;
        if (source instanceof Map<?, ?> map) {
            Object key, value;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                key = e.getKey();
                value = e.getValue();

                if (key == null) {
                    throw dataAccessError(this, dataType, source, keyMustNotBeNull());
                } else if (!(key instanceof String)) {
                    throw dataAccessError(this, dataType, source, keyMustBeString());
                } else if (value != null && !(value instanceof String)) {
                    throw dataAccessError(this, dataType, source, valueMustBeString());
                }
            } // loop
            result = (Map<String, String>) source;
        } else if (source instanceof String text) {
            text = text.trim();
            result = deserialize(this, dataType, env, text, 0, text.length(), null);
        } else {
            throw dataAccessError(this, dataType, source, null);
        }
        return result;
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return super.arrayTypeOfThis();
    }

    @Override
    public Class<?> firstGenericsType() {
        return String.class;
    }

    @Override
    public Class<?> secondGenericsType() {
        return String.class;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof PgHstoreType;
    }


    @SuppressWarnings("unused")
    public static StringBuilder serialize(PgHstoreType tye, DataType dataType, MappingEnv env,
                                          final Map<?, ?> source, StringBuilder builder) {
        // PostgreSQL allow empty hstore

        Object key, value;
        int count = 0;
        for (Map.Entry<?, ?> e : source.entrySet()) {
            if (count > 0) {
                builder.append(_Constant.COMMA);
            }
            key = e.getKey();
            value = e.getValue();
            if (key == null) {
                throw paramError(tye, dataType, source, keyMustNotBeNull());
            } else if (!(key instanceof String)) {
                throw paramError(tye, dataType, source, keyMustBeString());
            } else if (value != null && !(value instanceof String)) {
                throw paramError(tye, dataType, source, valueMustBeString());
            }

            PostgreArrays.encodeElement((String) key, builder);
            builder.append("=>");
            if (value == null) {
                builder.append("null");
            } else {
                PostgreArrays.encodeElement((String) value, builder);
            }

            count++;

        } // loop

        return builder;
    }


    @SuppressWarnings("unused")
    public static Map<String, String> deserialize(PgHstoreType tye, DataType dataType, MappingEnv env,
                                                  final String source, final int offset, final int endIndex,
                                                  @Nullable StringBuilder builder) {
        final Map<String, String> map = new HashMap<>();
        if (offset == endIndex) {
            // PostgreSQL allow empty hstore
            return map;
        }
        try {
            DESERIALIZER.deserialize(source, offset, endIndex, map::put, null, null, builder);
        } catch (Exception e) {
            throw dataAccessError(tye, dataType, source, e);
        }
        return map;
    }

    private static IllegalArgumentException keyMustNotBeNull() {
        return new IllegalArgumentException("key must not be null");
    }

    private static IllegalArgumentException keyMustBeString() {
        return new IllegalArgumentException("key must be String");
    }

    private static IllegalArgumentException valueMustBeString() {
        return new IllegalArgumentException("value must be String");
    }


}
