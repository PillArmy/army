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
import io.army.dialect.Database;
import io.army.dialect._Constant;
import io.army.meta.ServerMeta;
import io.army.sqltype.*;
import io.army.util.HexUtils;
import io.army.util._Exceptions;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/// This class map {@code byte[]} to sql binary type.
/// If you need to map varbinary ,you can use {@link VarBinaryType} instead of this class.
///
/// @see VarBinaryType
/// @see <a href="https://www.postgresql.org/docs/current/datatype-binary.html">PostgreSQL Binary Data Types</a>
/// @see <a href="https://dev.mysql.com/doc/refman/9.7/en/binary-varbinary.html">MySQL Binary Data Types</a>
public final class BinaryType extends _ArmyBuildInCoreType implements MappingType.SqlBinary {


    public static BinaryType from(final Class<?> fieldType) {
        if (fieldType != byte[].class) {
            throw errorJavaType(BinaryType.class, fieldType);
        }
        return INSTANCE;
    }

    public static final BinaryType INSTANCE = new BinaryType();


    /// private constructor
    private BinaryType() {
    }

    @Override
    public Class<?> javaType() {
        return byte[].class;
    }

    @Override
    public DataType map(final ServerMeta meta) {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case MySQL:
                dataType = MySQLType.BINARY;
                break;
            case PostgreSQL:
                dataType = PgType.BYTEA;
                break;
            case SQLite:
                dataType = SQLiteType.BINARY;
                break;
            default:
                throw mapError(this, meta);

        }
        return dataType;
    }


    @Override
    public byte[] beforeBind(DataType dataType, MappingEnv env, final Object source) {
        if (!(source instanceof byte[])) {
            throw paramError(this, dataType, source, null);
        }
        return (byte[]) source;
    }

    @Override
    public byte[] afterGet(DataType dataType, MappingEnv env, final Object source) {
        return deserialize(this, dataType, env, source);
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return ArrayFactoryFuncHolder.FUNCTION.apply(byte[][].class);
    }


    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof BinaryType;
    }

    @SuppressWarnings("unused")
    public static byte[] deserialize(MappingType type, DataType dataType, MappingEnv env, final Object source) {
        final byte[] value;
        if (source instanceof byte[]) {
            value = (byte[]) source;
        } else if (!(source instanceof String text)) {
            throw dataAccessError(type, dataType, source, null);
        } else if ((text = text.trim()).isEmpty()) {
            throw dataAccessError(type, dataType, source, null);
        } else if (!(dataType instanceof SQLType)) {
            throw dataAccessError(type, dataType, source, null);
        } else {
            try {
                value = decodeBinaryLiteral(((SQLType) dataType).database(), text, 0, text.length());
            } catch (Exception e) {
                throw dataAccessError(type, dataType, source, e);
            }
        }
        return value;
    }


    public static byte[] decodeBinaryLiteral(Database database, final String source, final int offset, final int endIndex) {
        final byte[] value;
        switch (database) {
            case PostgreSQL: {
                if (source.startsWith("\\x", offset)) {
                    value = HexUtils.decodeHex(source.substring(offset + 2, endIndex).getBytes(StandardCharsets.US_ASCII));
                } else {
                    value = decodePostgreOtcEscapeBinaryString(source, offset, endIndex);
                }
            }
            break;
            case MySQL: {
                if (source.startsWith("0x", offset)) {
                    value = HexUtils.decodeHex(source.substring(offset + 2, endIndex).getBytes(StandardCharsets.US_ASCII));
                } else {
                    throw new IllegalArgumentException("MySQL binary literal must start with 0x");
                }
            }
            break;
            default:
                throw _Exceptions.unexpectedEnum(database);
        }
        return value;
    }


    /// @throws IllegalArgumentException throw when source is not a valid binary literal
    private static byte[] decodePostgreOtcEscapeBinaryString(final CharSequence source, final int offset, final int endIndex) {

        try {

            final ByteArrayOutputStream out = new ByteArrayOutputStream();

            int lastWritten = offset;
            char ch;
            for (int i = offset, oct, boundary; i < endIndex; i++) {
                ch = source.charAt(i);

                if (ch != _Constant.BACK_SLASH) {
                    continue;
                }

                if (i > lastWritten) {
                    out.write(source.subSequence(lastWritten, i).toString().getBytes(StandardCharsets.ISO_8859_1));
                }

                if ((boundary = i + 4) <= endIndex && isOtcEscape(source, i, boundary)) {
                    oct = Integer.parseInt(source, i + 1, boundary, 8);
                    out.write(oct);
                    lastWritten = boundary;
                    i += 3;
                } else {
                    i++;
                    lastWritten = i;

                }

            } // loop

            if (lastWritten < endIndex) {
                out.write(source.subSequence(lastWritten, endIndex).toString().getBytes(StandardCharsets.ISO_8859_1));
            }

            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static boolean isOtcEscape(final CharSequence source, final int offset, final int endIndex) {
        boolean match;
        if (endIndex - offset != 4) {
            match = false;
        } else {
            match = source.charAt(offset) == _Constant.BACK_SLASH;
            char ch;
            for (int index = offset + 1; index < endIndex; index++) {
                ch = source.charAt(index);
                if (ch >= '0' && ch <= '7') {
                    continue;
                }
                match = false;
                break;
            }// loop
        }
        return match;
    }

    private static class ArrayFactoryFuncHolder {

        private static final Function<Class<?>, MappingType> FUNCTION;

        static {
            FUNCTION = removeArrayFromFunc(BinaryType.class);
        }

    } // ArrayFactoryFuncHolder


}
