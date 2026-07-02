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
import io.army.mapping.JsonType;
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

public class JsonArrayType extends ArmyJsonArrayType {

    public static JsonArrayType from(final Class<?> javaType) {
        final JsonArrayType instance;
        if (javaType == String[].class) {
            instance = TEXT_LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(JsonArrayType.class, javaType);
        } else {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        }
        return instance;
    }

    public static JsonArrayType fromTypeArg(final Class<?> javaType, final Class<?> typeArg) {
        if (!javaType.isArray()) {
            throw errorJavaType(JsonArrayType.class, javaType);
        }
        final Class<?> componentType = ArrayUtils.underlyingComponent(javaType);
        final JsonType underlyingType;
        if (componentType == List.class) {
            underlyingType = JsonType.fromList(typeArg);
        } else if (componentType == Set.class) {
            underlyingType = JsonType.fromSet(typeArg);
        } else {
            throw errorJavaType(JsonArrayType.class, javaType);
        }
        return new CollectionJsonArrayType(javaType, underlyingType);
    }

    public static JsonArrayType fromTypeArgs(final Class<?> javaType, final Class<?> keyClass, final Class<?> valueClass) {
        if (!javaType.isArray()) {
            throw errorJavaType(JsonArrayType.class, javaType);
        }
        final Class<?> componentType = ArrayUtils.underlyingComponent(javaType);
        if (componentType != Map.class) {
            throw errorJavaType(JsonArrayType.class, javaType);
        }

        return new MapJsonArrayType(javaType, JsonType.fromMap(keyClass, valueClass));
    }


    public static final JsonArrayType TEXT_LINEAR = new JsonArrayType(String[].class, JsonType.TEXT);

    static {
        addArrayFromFunc(JsonArrayType.class, JsonArrayType::from);
        addArrayFromTypeArgFunc(JsonArrayType.class, JsonArrayType::fromTypeArg);
        addArrayFromTypeArgsFunc(JsonArrayType.class, JsonArrayType::fromTypeArgs);
    }


    final JsonType underlyingType;

    /// private constructor
    private JsonArrayType(Class<?> javaType, JsonType underlyingType) {
        super(javaType);
        this.underlyingType = underlyingType;
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
    public final Class<?> underlyingJavaType() {
        return this.underlyingType.javaType();
    }

    @Override
    public final MappingType underlyingType() {
        return this.underlyingType;
    }


    @Override
    final ArmyJsonArrayType doFromTypeArg(Class<?> javaType, Class<?> typeArg) {
        return JsonArrayType.fromTypeArg(javaType, typeArg);
    }

    @Override
    final ArmyJsonArrayType doFromTypeArgs(Class<?> javaType, Class<?> keyClass, Class<?> valueClass) {
        return JsonArrayType.fromTypeArgs(javaType, keyClass, valueClass);
    }

    @Override
    final ArmyJsonArrayType doFrom(Class<?> javaType) {
        return JsonArrayType.from(javaType);
    }


    private static final class ClassValueHolder {

        private static JsonArrayType fromObj(Class<?> javaType) {
            return new JsonArrayType(javaType, JsonType.from(ArrayUtils.underlyingComponent(javaType)));
        }

        private static final ClassValue<JsonArrayType> CLASS_VALUE = FuncClassValue.create(ClassValueHolder::fromObj);

    }

    private static final class CollectionJsonArrayType extends JsonArrayType implements UnaryGenericsMapping {

        private CollectionJsonArrayType(Class<?> javaType, JsonType underlyingType) {
            super(javaType, underlyingType);
        }

        @Override
        public Class<?> genericsType() {
            return ((UnaryGenericsMapping) this.underlyingType).genericsType();
        }

    } // CollectionJsonArrayType

    private static final class MapJsonArrayType extends JsonArrayType implements DualGenericsMapping {

        private MapJsonArrayType(Class<?> javaType, JsonType underlyingType) {
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
