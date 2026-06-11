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
import io.army.mapping.JsonbType;
import io.army.mapping.MappingType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;

public class JsonbArrayType extends ArmyJsonArrayType {

    public static JsonbArrayType from(final Class<?> javaType) {
        final JsonbArrayType instance;
        if (!javaType.isArray()) {
            throw errorJavaType(JsonbArrayType.class, javaType);
        } else if (javaType == String[].class) {
            instance = TEXT_LINEAR;
        } else {
            instance = new JsonbArrayType(javaType);
        }
        return instance;
    }


    public static final JsonbArrayType TEXT_LINEAR = new JsonbArrayType(String[].class);

    /// private constructor
    private JsonbArrayType(Class<?> javaType) {
        super(javaType, ArrayUtils.underlyingComponent(javaType));
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = PgType.JSONB_ARRAY;
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
    public MappingType underlyingType() {
        return JsonbType.from(this.underlyingJavaType);
    }

    @Override
    public final MappingType elementType() {
        final MappingType instance;
        final Class<?> javaType = this.javaType;
        if (ArrayUtils.dimensionOf(javaType) == 1) {
            instance = JsonbType.from(this.underlyingJavaType);
        } else {
            instance = from(javaType.getComponentType());
        }
        return instance;
    }

    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        return from(ArrayUtils.arrayClassOf(this.javaType));
    }


}
