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
import io.army.mapping.array.JsonbArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.sqltype.SQLType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonbType extends ArmyJsonType implements JsonbMappingType {

    public static JsonbType from(final Class<?> javaType) {
        if (javaType == String.class) {
            return TEXT;
        }
        if (Map.class.isAssignableFrom(javaType) || Collection.class.isAssignableFrom(javaType)) {
            throw errorJavaType(PreferredJsonbType.class, javaType);
        }
        return CLASS_VALUE.get(javaType);
    }

    public static JsonbType fromMap(Class<?> keyClass, Class<?> valueClass) {
        if (keyClass == String.class && valueClass == Object.class) {
            return MapJsonType.INSTANCE;
        }
        return new MapJsonType(keyClass, valueClass);
    }

    public static JsonbType fromList(Class<?> elementClass) {
        return ListJsonType.INSTANCE_CACHE.get(elementClass);
    }

    public static JsonbType fromSet(Class<?> elementClass) {
        return SetJsonType.INSTANCE_CACHE.get(elementClass);
    }


    public static final JsonbType TEXT = new JsonbType(String.class);


    private static final ClassValue<JsonbType> CLASS_VALUE = new ClassValue<>() {
        @Override
        protected JsonbType computeValue(Class<?> type) {
            return new JsonbType(type);
        }
    };


    /// private constructor
    private JsonbType(Class<?> javaType) {
        super(javaType);
    }


    @Override
    public final DataType map(final ServerMeta meta) {
        final SQLType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = PgType.JSONB;
                break;
            case MySQL:
            case Oracle:
            case H2:
            default:
                throw MAP_ERROR_HANDLER.apply(this, meta);
        }
        return dataType;
    }


    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        if (getClass() != JsonbType.class) {
            throw dontSupportArrayType(this);
        }
        return JsonbArrayType.from(this.javaType);
    }

    @Override
    final MappingType creatMapType(Class<?> keyClass, Class<?> valueClass) {
        return fromMap(keyClass, valueClass);
    }

    @Override
    final MappingType createListType(Class<?> elementClass) {
        return fromList(elementClass);
    }

    @Override
    final MappingType createSetType(Class<?> elementClass) {
        return fromSet(elementClass);
    }


    private static final class SetJsonType extends JsonbType implements UnaryGenericsMapping {

        private static final ClassValue<SetJsonType> INSTANCE_CACHE = new ClassValue<>() {
            @Override
            protected SetJsonType computeValue(Class<?> type) {
                return new SetJsonType(type);
            }
        };

        private final Class<?> elementClass;

        private SetJsonType(Class<?> elementClass) {
            this.elementClass = elementClass;
            super(Set.class);
        }

        @Override
        public Class<?> genericsType() {
            return this.elementClass;
        }


    } // SetJsonType

    private static final class ListJsonType extends JsonbType implements UnaryGenericsMapping {

        private static final ClassValue<ListJsonType> INSTANCE_CACHE = new ClassValue<>() {
            @Override
            protected ListJsonType computeValue(Class<?> type) {
                return new ListJsonType(type);
            }
        };

        private final Class<?> elementClass;

        private ListJsonType(Class<?> elementClass) {
            this.elementClass = elementClass;
            super(List.class);
        }

        @Override
        public Class<?> genericsType() {
            return this.elementClass;
        }


    } // ListJsonType

    private static final class MapJsonType extends JsonbType implements DualGenericsMapping {

        private static final MapJsonType INSTANCE = new MapJsonType(String.class, Object.class);

        private final Class<?> keyClass;

        private final Class<?> valueClass;

        private MapJsonType(Class<?> keyClass, Class<?> valueClass) {
            this.keyClass = keyClass;
            this.valueClass = valueClass;
            super(Map.class);
        }

        @Override
        public Class<?> firstGenericsType() {
            return this.keyClass;
        }

        @Override
        public Class<?> secondGenericsType() {
            return this.valueClass;
        }


    } // MapJsonType


}
