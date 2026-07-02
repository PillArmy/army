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
import io.army.mapping.BooleanType;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.sqltype.SQLType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;
import io.army.util._Assert;

import java.util.Objects;

public class BooleanArrayType extends _ArmyCoreArrayType {

    public static BooleanArrayType from(final Class<?> javaType) {
        final BooleanArrayType instance;

        final Class<?> underlyingJavaType;
        if (javaType == Boolean[].class) {
            instance = LINEAR;
        } else if (javaType == boolean[].class) {
            instance = PRIMITIVE_LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(BooleanArrayType.class, javaType);
        } else if ((underlyingJavaType = ArrayUtils.underlyingComponent(javaType)) == Boolean.class
                || underlyingJavaType == boolean.class) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(BooleanArrayType.class, javaType);
        }
        return instance;
    }


    /// one dimension array of {@code  boolean}
    public static final BooleanArrayType PRIMITIVE_LINEAR = new BooleanArrayType(boolean[].class);

    /// one dimension array of {@link Boolean}
    public static final BooleanArrayType LINEAR = new BooleanArrayType(Boolean[].class);

    /// unlimited dimension array of {@link Boolean}
    public static final BooleanArrayType UNLIMITED = new BooleanArrayType(Object.class, Boolean.class);

    static {
        addArrayFromFunc(BooleanArrayType.class, BooleanArrayType::from);
    }


    private final Class<?> javaType;

    private final Class<?> underlyingJavaType;

    /// private constructor
    private BooleanArrayType(Class<?> javaType) {
        this.javaType = javaType;
        this.underlyingJavaType = ArrayUtils.underlyingComponent(javaType);
    }

    /// private constructor
    private BooleanArrayType(Class<Object> javaType, Class<?> underlyingJavaType) {
        _Assert.isTrue(underlyingJavaType == Boolean.class, "");
        this.javaType = javaType;
        this.underlyingJavaType = underlyingJavaType;
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }


    @Override
    public final DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        final SQLType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = PgType.BOOLEAN_ARRAY;
                break;
            case MySQL:
            case SQLite:
            case H2:
            case Oracle:
            default:
                throw MAP_ERROR_HANDLER.apply(this, meta);
        }
        return dataType;
    }

    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        return PostgreArrays.arrayBeforeBind(source, BooleanArrayType::appendToText, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return PostgreArrays.arrayAfterGet(this, dataType, source, BooleanArrayType::parseText, null);
    }

    @Override
    public final Class<?> underlyingJavaType() {
        return this.underlyingJavaType;
    }

    @Override
    public final MappingType underlyingType() {
        return BooleanType.INSTANCE;
    }

    @Override
    public final MappingType elementType() {
        final Class<?> javaType = this.javaType;
        final MappingType instance;
        if (javaType == Object.class) {
            instance = this;
        } else if (javaType == Boolean[].class || javaType == boolean[].class) {
            instance = BooleanType.INSTANCE;
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
        } else if (obj instanceof BooleanArrayType o) {
            match = o.javaType == this.javaType
                    && o.underlyingJavaType == this.underlyingJavaType
            ;
        } else {
            match = false;
        }
        return match;
    }


    /*-------------------below static methods -------------------*/

    private static Boolean parseText(final String text, final int offset, final int end) {

        final Boolean value;
        if (text.regionMatches(true, offset, "true", 0, 4)) {
            if (offset + 4 != end) {
                throw new IllegalArgumentException("not boolean");
            }
            value = Boolean.TRUE;
        } else if (text.regionMatches(true, offset, "false", 0, 5)) {
            if (offset + 5 != end) {
                throw new IllegalArgumentException("not boolean");
            }
            value = Boolean.FALSE;
        } else {
            throw new IllegalArgumentException("not boolean");
        }
        return value;
    }

    private static void appendToText(final Object element, final StringBuilder appender) {
        if (!(element instanceof Boolean)) {
            // no bug,never here
            throw new IllegalArgumentException();
        }
        appender.append(element);
    }


    private static final class ClassValueHolder {

        private static final ClassValue<BooleanArrayType> CLASS_VALUE = FuncClassValue.create(BooleanArrayType::new);

    }

}
