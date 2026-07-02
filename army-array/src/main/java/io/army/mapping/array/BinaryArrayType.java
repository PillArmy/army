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
import io.army.mapping.BinaryType;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.sqltype.SQLType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.util.Objects;


/// This class is array type of {@link BinaryType}.
///
/// @see BinaryType
/// @see VarBinaryArrayType
/// @since 0.6.0
public class BinaryArrayType extends _ArmyCoreArrayType {


    public static BinaryArrayType from(final Class<?> javaType) {
        final BinaryArrayType instance;

        if (javaType == byte[][].class) {
            instance = LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(BinaryArrayType.class, javaType);
        } else if (ArrayUtils.underlyingComponentMatch(byte[].class, javaType)) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(BinaryArrayType.class, javaType);
        }
        return instance;
    }


    public static final BinaryArrayType LINEAR = new BinaryArrayType(byte[][].class);

    static {
        addArrayFromFunc(BinaryArrayType.class, BinaryArrayType::from);
    }


    private final Class<?> javaType;

    /// private constructor
    private BinaryArrayType(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        final SQLType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = PgType.BYTEA_ARRAY;
                break;
            case MySQL:
            case SQLite:
            default:
                throw MAP_ERROR_HANDLER.apply(this, meta);
        }
        return dataType;
    }


    @Override
    public String beforeBind(DataType dataType, MappingEnv env, final Object source) throws CriteriaException {
        return PostgreArrays.byteaArrayToText(this, dataType, source, new StringBuilder(), PARAM_ERROR_HANDLER)
                .toString();
    }

    @Override
    public Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, false, PostgreArrays::parseBytea,
                ACCESS_ERROR_HANDLER);
    }

    @Override
    public Class<?> underlyingJavaType() {
        return byte[].class;
    }

    @Override
    public MappingType underlyingType() {
        return BinaryType.INSTANCE;
    }

    @Override
    public MappingType elementType() {
        final MappingType instance;
        final Class<?> javaType = this.javaType;
        if (javaType == byte[][].class) {
            instance = BinaryType.INSTANCE;
        } else {
            instance = from(javaType.getComponentType());
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
        } else if (obj instanceof BinaryArrayType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }

    private static final class ClassValueHolder {

        private static final ClassValue<BinaryArrayType> CLASS_VALUE = FuncClassValue.create(BinaryArrayType::new);

    }


}
