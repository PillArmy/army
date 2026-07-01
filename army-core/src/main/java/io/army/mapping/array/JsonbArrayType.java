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

import io.army.dialect.UnsupportedDialectException;
import io.army.mapping.DualGenericsMapping;
import io.army.mapping.JsonbType;
import io.army.mapping.MappingType;
import io.army.mapping.UnaryGenericsMapping;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonbArrayType extends ArmyJsonArrayType {

    public static JsonbArrayType from(final Class<?> javaType) {
        final JsonbArrayType instance;
        if (javaType == String[].class) {
            instance = TEXT_LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(JsonbArrayType.class, javaType);
        } else {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        }
        return instance;
    }

    public static JsonbArrayType fromTypeArg(final Class<?> javaType, final Class<?> typeArg) {
        if (!javaType.isArray()) {
            throw errorJavaType(JsonbArrayType.class, javaType);
        }
        final Class<?> componentType = ArrayUtils.underlyingComponent(javaType);
        final JsonbType underlyingType;
        if (componentType == List.class) {
            underlyingType = JsonbType.fromList(typeArg);
        } else if (componentType == Set.class) {
            underlyingType = JsonbType.fromSet(typeArg);
        } else {
            throw errorJavaType(JsonbArrayType.class, javaType);
        }
        return new CollectionJsonbArrayType(javaType, underlyingType);
    }

    public static JsonbArrayType fromTypeArgs(final Class<?> javaType, final Class<?> keyClass, final Class<?> valueClass) {
        if (!javaType.isArray()) {
            throw errorJavaType(JsonbArrayType.class, javaType);
        }
        final Class<?> componentType = ArrayUtils.underlyingComponent(javaType);
        if (componentType != Map.class) {
            throw errorJavaType(JsonbArrayType.class, javaType);
        }

        return new MapJsonbArrayType(javaType, JsonbType.fromMap(keyClass, valueClass));
    }


    public static final JsonbArrayType TEXT_LINEAR = new JsonbArrayType(String[].class, JsonbType.TEXT);

    final JsonbType underlyingType;

    /// private constructor
    private JsonbArrayType(Class<?> javaType, JsonbType underlyingType) {
        super(javaType);
        this.underlyingType = underlyingType;
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        return mapDataType(this, meta);
    }


    @Override
    public final Class<?> underlyingJavaType() {
        return this.underlyingType.javaType();
    }

    @Override
    public final MappingType underlyingType() {
        return this.underlyingType;
    }


    @Override
    final ArmyJsonArrayType doFromTypeArg(Class<?> javaType, Class<?> typeArg) {
        return fromTypeArg(javaType, typeArg);
    }

    @Override
    final ArmyJsonArrayType doFromTypeArgs(Class<?> javaType, Class<?> keyClass, Class<?> valueClass) {
        return fromTypeArgs(javaType, keyClass, valueClass);
    }

    @Override
    final ArmyJsonArrayType doFrom(Class<?> javaType) {
        return from(javaType);
    }

    static DataType mapDataType(ArmyJsonArrayType type, ServerMeta meta) {
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
                throw MAP_ERROR_HANDLER.apply(type, meta);
        }
        return dataType;
    }


    private static final class ClassValueHolder {

        private static JsonbArrayType fromObj(Class<?> javaType) {
            return new JsonbArrayType(javaType, JsonbType.from(ArrayUtils.underlyingComponent(javaType)));
        }

        private static final ClassValue<JsonbArrayType> CLASS_VALUE = FuncClassValue.create(ClassValueHolder::fromObj);

    }

    private static final class CollectionJsonbArrayType extends JsonbArrayType implements UnaryGenericsMapping {

        private CollectionJsonbArrayType(Class<?> javaType, JsonbType underlyingType) {
            super(javaType, underlyingType);
        }

        @Override
        public Class<?> genericsType() {
            return ((UnaryGenericsMapping) this.underlyingType).genericsType();
        }

    } // CollectionJsonArrayType

    private static final class MapJsonbArrayType extends JsonbArrayType implements DualGenericsMapping {

        private MapJsonbArrayType(Class<?> javaType, JsonbType underlyingType) {
            super(javaType, underlyingType);
        }

        @Override
        public Class<?> firstGenericsType() {
            return ((DualGenericsMapping) this.underlyingType).firstGenericsType();
        }

        @Override
        public Class<?> secondGenericsType() {
            return ((DualGenericsMapping) this.underlyingType).secondGenericsType();
        }


    } // MapJsonArrayType


}
