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

package io.army.mapping.optional;

import io.army.criteria.CriteriaException;
import io.army.dialect.Database;
import io.army.dialect.UnsupportedDialectException;
import io.army.executor.DataAccessException;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping._ArmyNoInjectionType;
import io.army.mapping.array.VectorArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;


///
/// This class representing Postgre vector type {@link MappingType}
///
/// @see <a href="https://github.com/pgvector/pgvector">pgvector-java</a>
/// @see <a href="https://github.com/pgvector/pgvector-java">pgvector-java</a>
public final class VectorType extends _ArmyNoInjectionType {

    public static VectorType from(final Class<?> javaType) {
        if (javaType != float[].class) {
            throw errorJavaType(VectorType.class, javaType);
        }
        return INSTANCE;
    }

    public static final VectorType INSTANCE = new VectorType();


    private VectorType() {
    }

    @Override
    public Class<?> javaType() {
        return float[].class;
    }

    @Override
    public DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        if (meta.serverDatabase() != Database.PostgreSQL) {
            throw MAP_ERROR_HANDLER.apply(this, meta);
        }
        return PgType.VECTOR;
    }

    @Override
    public String beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        if (source instanceof String) {
            validateVector(this, dataType, (String) source);
            return (String) source;
        }
        if (!(source instanceof float[] vector)) {
            throw paramError(this, dataType, source, null);
        }
        return vectorToString(vector, new StringBuilder(2 + vector.length * 8))
                .toString();
    }

    @Override
    public float[] afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        if (source instanceof float[]) {
            return (float[]) source;
        }
        if (!(source instanceof String)) {
            throw dataAccessError(this, dataType, source, null);
        }
        final String text = ((String) source).trim();
        try {
            return stringToVector(text, 0, text.length());
        } catch (NumberFormatException e) {
            throw dataAccessError(this, dataType, source, e);
        }
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return VectorArrayType.from(ArrayUtils.arrayClassOf(float[].class));
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof VectorType;
    }


    public static StringBuilder vectorToString(final float[] vector, final StringBuilder sqlBuilder) {
        sqlBuilder.append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sqlBuilder.append(',');
            }
            sqlBuilder.append(vector[i]);
        }
        return sqlBuilder.append(']');
    }

    public static float[] stringToVector(final String text, final int offset, final int end)
            throws IllegalArgumentException {
        if (text.charAt(offset) != '[' || text.charAt(end - 1) != ']') {
            throw new IllegalArgumentException("format error");
        }
        final String[] items = text.substring(offset + 1, end - 1).split(",");
        final float[] vector = new float[items.length];
        for (int i = 0; i < items.length; i++) {
            vector[i] = Float.parseFloat(items[i]);
        }
        return vector;
    }

    public static void validateVector(MappingType type, DataType dataType, final String source) {
        if (source.charAt(0) != '[' || source.charAt(source.length() - 1) != ']') {
            throw dataAccessError(type, dataType, source, null);
        }
        final String[] items = source.substring(1, source.length() - 1).split(",");

        try {
            for (String item : items) {
                Float.parseFloat(item);
            }
        } catch (NumberFormatException e) {
            throw dataAccessError(type, dataType, source, e);
        }
    }


}
