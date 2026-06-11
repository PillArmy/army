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
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping.YearType;
import io.army.mapping._ArmyBuildInArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.time.Year;
import java.util.Objects;

public class YearArrayType extends _ArmyBuildInArrayType {


    public static YearArrayType from(Class<?> javaType) {
        final YearArrayType instance;
        if (javaType == Year[].class) {
            instance = LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(YearArrayType.class, javaType);
        } else if (ArrayUtils.underlyingComponent(javaType) == Year.class) {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        } else {
            throw errorJavaType(YearArrayType.class, javaType);
        }
        return instance;
    }

    public static final YearArrayType LINEAR = new YearArrayType(Year[].class);

    private final Class<?> javaType;

    /// private constructor
    private YearArrayType(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public final Class<?> javaType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Class<?> underlyingJavaType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final MappingType elementType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public final MappingType underlyingType() {
        return YearType.INSTANCE;
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
        } else if (obj instanceof YearArrayType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }

    private static final class ClassValueHolder {

        private static final ClassValue<YearArrayType> CLASS_VALUE = FuncClassValue.create(YearArrayType::new);

    }


}
