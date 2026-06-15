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
import io.army.mapping.LongType;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.sqltype.SQLType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;
import io.army.util._Assert;

import java.util.Objects;

public class LongArrayType extends _ArmyBuildInArrayType {


    public static LongArrayType from(final Class<?> javaType) {
        final LongArrayType instance;
        final Class<?> componentType;
        if (javaType == Long[].class) {
            instance = LINEAR;
        } else if (javaType == long[].class) {
            instance = PRIMITIVE_LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(LongArrayType.class, javaType);
        } else if ((componentType = ArrayUtils.underlyingComponent(javaType)) == long.class
                || componentType == Long.class) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(LongArrayType.class, javaType);
        }
        return instance;
    }

    public static final LongArrayType UNLIMITED = new LongArrayType(Object.class, Long.class);

    public static final LongArrayType LINEAR = new LongArrayType(Long[].class);

    public static final LongArrayType PRIMITIVE_LINEAR = new LongArrayType(long[].class);


    private final Class<?> javaType;

    private final Class<?> underlyingJavaType;

    /// private constructor
    private LongArrayType(final Class<?> javaType) {
        this.javaType = javaType;
        this.underlyingJavaType = ArrayUtils.underlyingComponent(javaType);
    }

    /// private constructor
    private LongArrayType(final Class<Object> javaType, Class<?> underlyingJavaType) {
        _Assert.isTrue(underlyingJavaType == Long.class, "");

        this.javaType = javaType;
        this.underlyingJavaType = underlyingJavaType;
    }


    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }


    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        return mapToSqlType(this, meta);
    }

    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, LongArrayType::appendToText, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, LongArrayType::parseText, null, null, null);
    }


    @Override
    public final Class<?> underlyingJavaType() {
        return this.underlyingJavaType;
    }

    @Override
    public final MappingType underlyingType() {
        return LongType.INSTANCE;
    }

    @Override
    public final MappingType elementType() {
        final Class<?> javaType = this.javaType;
        final MappingType instance;
        if (javaType == Object.class) {
            instance = this;
        } else if (javaType == Long[].class || javaType == long[].class) {
            instance = LongType.INSTANCE;
        } else {
            instance = from(javaType.getComponentType());
        }
        return instance;
    }

    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        final Class<?> javaType = this.javaType;
        if (javaType == Object.class) { // unlimited dimension array
            return this;
        }
        return from(ArrayUtils.arrayClassOf(javaType));
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.javaType, this.underlyingJavaType);
    }

    @Override
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof LongArrayType o) {
            match = o.javaType == this.javaType
                    && o.underlyingJavaType == this.underlyingJavaType;
        } else {
            match = false;
        }
        return match;
    }

    /*-------------------below static methods -------------------*/

    static DataType mapToSqlType(final MappingType type, final ServerMeta meta) {
        final SQLType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = PgType.BIGINT_ARRAY;
                break;
            case Oracle:
            case H2:
            case MySQL:
            default:
                throw MAP_ERROR_HANDLER.apply(type, meta);
        }
        return dataType;
    }


    private static long parseText(final String text, final int offset, final int endIndex) {
        return Long.parseLong(text, offset, endIndex, 10);
    }

    private static void appendToText(final Object element, final StringBuilder appender) {
        if (!(element instanceof Long)) {
            // no bug,never here
            throw new IllegalArgumentException();
        }
        appender.append(element);
    }

    private static final class ClassValueHolder {

        private static final ClassValue<LongArrayType> CLASS_VALUE = FuncClassValue.create(LongArrayType::new);

    }


}
