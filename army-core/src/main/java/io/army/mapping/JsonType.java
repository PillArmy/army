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
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.MySQLType;
import io.army.sqltype.PgType;
import io.army.sqltype.SQLiteType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;


public class JsonType extends ArmyJsonType implements JsonMappingType {

    public static JsonType from(final Class<?> javaType) {
        final JsonType instance;
        if (javaType == String.class) {
            instance = TEXT;
        } else if (Map.class.isAssignableFrom(javaType) || Collection.class.isAssignableFrom(javaType)) {
            throw errorJavaType(JsonType.class, javaType);
        } else {
            instance = CLASS_VALUE.get(javaType);
        }
        return instance;
    }

    public static final JsonType TEXT = new JsonType(String.class);

    public static JsonType fromMap(Class<?> keyClass, Class<?> valueClass) {
        if (keyClass == String.class && valueClass == Object.class) {
            return MapJsonType.INSTANCE;
        }
        return new MapJsonType(keyClass, valueClass);
    }

    public static JsonType fromList(Class<?> elementClass) {
        return ListJsonType.INSTANCE_CACHE.get(elementClass);
    }

    public static JsonType fromSet(Class<?> elementClass) {
        return SetJsonType.INSTANCE_CACHE.get(elementClass);
    }


    private static final ClassValue<JsonType> CLASS_VALUE = FuncClassValue.create(JsonType::new);

    /// private constructor
    private JsonType(Class<?> javaType) {
        super(javaType);
    }


    @Override
    public final DataType map(final ServerMeta meta) {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case MySQL:
                dataType = MySQLType.JSON;
                break;
            case PostgreSQL:
                dataType = PgType.JSON;
                break;
            case SQLite:
                dataType = SQLiteType.JSON;
                break;
            case Oracle:
            case H2:
            default:
                throw MAP_ERROR_HANDLER.apply(this, meta);

        }
        return dataType;
    }

    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        final MappingType instance;
        if (this instanceof UnaryGenericsMapping ug) {
            instance = ArrayFactoryFuncHolder.TYPE_ARG_FUNC.apply(ArrayUtils.arrayClassOf(this.javaType), ug.genericsType());
        } else if (this instanceof DualGenericsMapping dg) {
            instance = ArrayFactoryFuncHolder.TYPE_ARGS_FUNC.apply(ArrayUtils.arrayClassOf(this.javaType), dg.firstGenericsType(), dg.secondGenericsType());
        } else {
            instance = ArrayFactoryFuncHolder.FUNCTION.apply(ArrayUtils.arrayClassOf(this.javaType));
        }
        return instance;
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

    private static final class SetJsonType extends JsonType implements UnaryGenericsMapping {

        private static final ClassValue<SetJsonType> INSTANCE_CACHE = FuncClassValue.create(SetJsonType::new);

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

    private static final class ListJsonType extends JsonType implements UnaryGenericsMapping {

        private static final ClassValue<ListJsonType> INSTANCE_CACHE = FuncClassValue.create(ListJsonType::new);

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

    private static final class MapJsonType extends JsonType implements DualGenericsMapping {

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

    private static class ArrayFactoryFuncHolder {

        private static final Function<Class<?>, MappingType> FUNCTION;

        private static final BiFunction<Class<?>, Class<?>, MappingType> TYPE_ARG_FUNC;

        private static final TeClassFunc TYPE_ARGS_FUNC;

        static {
            FUNCTION = removeArrayFromFunc(JsonType.class);
            TYPE_ARG_FUNC = removeArrayFromTypeArgFunc(JsonType.class);
            TYPE_ARGS_FUNC = removeArrayFromTypeArgsFunc(JsonType.class);
        }

    } // ArrayFactoryFuncHolder


}
