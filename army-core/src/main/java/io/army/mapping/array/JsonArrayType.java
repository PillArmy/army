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
import io.army.mapping.JsonType;
import io.army.mapping.MappingType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

public class JsonArrayType extends ArmyJsonArrayType {

    public static JsonArrayType from(final Class<?> javaType) {
        final JsonArrayType instance;
        if (!javaType.isArray()) {
            throw errorJavaType(JsonArrayType.class, javaType);
        } else if (javaType == String[].class) {
            instance = TEXT_LINEAR;
        } else {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        }
        return instance;
    }


    public static final JsonArrayType TEXT_LINEAR = new JsonArrayType(String[].class);

    /// private constructor
    private JsonArrayType(Class<?> javaType) {
        super(javaType, ArrayUtils.underlyingComponent(javaType));
    }


    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = PgType.JSON_ARRAY;
                break;
            case MySQL:
            case SQLite:
            case H2:
            case Oracle:
            default:
                throw mapError(this, meta);
        }
        return dataType;
    }


    @Override
    public final MappingType underlyingType() {
        return JsonType.from(this.underlyingJavaType);
    }

    @Override
    public final MappingType elementType() {
        final MappingType instance;
        final Class<?> javaType = this.javaType;
        if (javaType == Object.class) {
            instance = this;
        } else if (ArrayUtils.dimensionOf(javaType) == 1) {
            instance = JsonType.from(this.underlyingJavaType);
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
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof JsonArrayType o) {
            match = o.javaType == this.javaType
                    && o.underlyingJavaType == this.underlyingJavaType
            ;
        } else {
            match = false;
        }
        return match;
    }


    private static final class ClassValueHolder {

        private static final ClassValue<JsonArrayType> CLASS_VALUE = FuncClassValue.create(JsonArrayType::new);

    }

}
