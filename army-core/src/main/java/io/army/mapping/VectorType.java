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
import io.army.lang.Nullable;
import io.army.meta.ServerMeta;
import io.army.sqltype.ArmyType;
import io.army.sqltype.CustomType;
import io.army.sqltype.DataType;
import io.army.sqltype.MySQLType;
import io.army.util.BinaryUtils;

import java.util.function.Function;


///
/// This class representing vector type {@link MappingType}
///
/// see {@code  io.army.mapping.array.VectorArrayType}
///
/// @see <a href="https://github.com/pgvector/pgvector">pgvector</a>
/// @see <a href="https://github.com/pgvector/pgvector-java">pgvector-java</a>
/// @see <a href="https://dev.mysql.com/doc/refman/9.7/en/vector.html">MySQL Vector Type</a>
public final class VectorType extends _ArmyNoInjectionType implements MappingType.SqlUserDefined, MappingType.SqlVector {

    public static VectorType from(final Class<?> javaType) {
        if (javaType != float[].class) {
            throw errorJavaType(VectorType.class, javaType);
        }
        return INSTANCE;
    }

    public static final VectorType INSTANCE = new VectorType();

    private CustomType dataType;

    /// private constructor
    private VectorType() {
    }

    @Override
    public Class<?> javaType() {
        return float[].class;
    }

    @Override
    public DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = obtainDataType();
                break;
            case MySQL:
                if (meta.meetsMinimum(9, 0, 0)) {
                    dataType = MySQLType.VECTOR;
                    break;
                }
            default:
                throw mapError(this, meta);
        }
        return dataType;
    }

    @Override
    public Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return serialize(this, dataType, env, source);
    }

    @Override
    public float[] afterGet(final DataType dataType, MappingEnv env, final Object source) throws DataAccessException {
        return deserialize(this, dataType, env, source);
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return ArrayFactoryFuncHolder.FUNCTION.apply(float[][].class);
    }


    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof VectorType;
    }

    private CustomType obtainDataType() {
        CustomType dataType = this.dataType;
        if (dataType == null) {
            this.dataType = dataType = CustomType.builder()
                    .typeName("VECTOR")
                    .componentType(ArmyType.VECTOR)
                    .javaType(float[].class)
                    .componentCreateDdl(false)
                    .build();
        }
        return dataType;
    }

    @SuppressWarnings("unused")
    public static Object serialize(final VectorType type, final DataType dataType, MappingEnv env, final Object source) {
        if (!(source instanceof float[] vector)) {
            throw paramError(type, dataType, source, null);
        }
        final Object value;
        try {
            if (dataType == MySQLType.VECTOR) {
                value = vectorToBinaryLe(vector);
            } else {
                value = vectorToString(vector);
            }
        } catch (CriteriaException e) {
            throw e;
        } catch (Exception e) {
            throw paramError(type, dataType, source, e);
        }
        return value;
    }

    @SuppressWarnings("unused")
    public static float[] deserialize(final VectorType type, final DataType dataType, MappingEnv env, final Object source) {
        final float[] value;
        try {
            if (source instanceof float[]) {
                value = (float[]) source;
            } else if (dataType == MySQLType.VECTOR) {
                if (!(source instanceof byte[])) {
                    throw dataAccessError(type, dataType, source, null);
                }
                value = binaryLeToVector((byte[]) source);
            } else if (source instanceof String s) {
                value = stringToVector(s, 0, s.length());
            } else {
                throw paramError(type, dataType, source, null);
            }
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw dataAccessError(type, dataType, source, e);
        }
        return value;
    }


    public static String vectorToString(final float[] vector) {
        return vectorToString(vector, new StringBuilder(2 + vector.length * 10))
                .toString();
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

    public static float[] stringToVector(final CharSequence text, final int offset, final int end)
            throws IllegalArgumentException {
        if (text.charAt(offset) != '[' || text.charAt(end - 1) != ']') {
            throw new IllegalArgumentException("format error");
        }
        final String[] items = text.subSequence(offset + 1, end - 1).toString().split(",");
        final float[] vector = new float[items.length];
        for (int i = 0; i < items.length; i++) {
            vector[i] = Float.parseFloat(items[i]);
        }
        return vector;
    }


    public static byte[] vectorToBinaryLe(final float[] vector) {
        final byte[] bytes = new byte[vector.length << 2];
        vectorToBinaryLe(vector, bytes, 0);
        return bytes;
    }

    public static int vectorToBinaryLe(final float[] vector, final byte[] bytes, int offset) {
        for (float v : vector) {
            offset = BinaryUtils.writeFloatLe(v, bytes, offset);
        }
        return offset;
    }

    public static float[] binaryLeToVector(final byte[] bytes) {
        return binaryLeToVector(bytes, 0, bytes.length);
    }

    public static float[] binaryLeToVector(final byte[] bytes, int offset, final int endPos) {
        final int byteLength = endPos - offset;
        if (byteLength < 1 || byteLength % 4 != 0) {
            throw new IllegalArgumentException("invalid vector");
        }
        final float[] vector = new float[byteLength / 4];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = BinaryUtils.readFloatLe(bytes, offset);
            offset += 4;
        }
        return vector;
    }


    public static String binaryLeToVectorText(final byte[] bytes) {
        return binaryLeToVectorText(bytes, 0, bytes.length, null)
                .toString();
    }


    public static StringBuilder binaryLeToVectorText(final byte[] bytes, int offset, final int endPos, @Nullable StringBuilder builder) {
        final float[] vector;
        vector = binaryLeToVector(bytes, offset, endPos);
        if (builder == null) {
            builder = new StringBuilder(2 + vector.length * 10);
        }
        return vectorToString(vector, builder);
    }

    @SuppressWarnings("unused")
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

    private static class ArrayFactoryFuncHolder {

        private static final Function<Class<?>, MappingType> FUNCTION;

        static {
            FUNCTION = removeArrayFromFunc(VectorType.class);
        }

    } // ArrayFactoryFuncHolder


}
