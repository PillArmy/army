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
import io.army.dialect.UnsupportedDialectException;
import io.army.mapping.array.PreferredJsonbArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.MySQLType;
import io.army.sqltype.PgType;
import io.army.sqltype.SQLType;
import io.army.util.FuncClassValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/// Maps the Java type to JSONB by default if supported, otherwise uses JSON.
public class PreferredJsonbType extends ArmyJsonType implements JsonbMappingType {

    public static PreferredJsonbType from(final Class<?> javaType) {
        if (Map.class.isAssignableFrom(javaType) || Collection.class.isAssignableFrom(javaType)) {
            throw errorJavaType(PreferredJsonbType.class, javaType);
        }
        return CLASS_VALUE.get(javaType);
    }

    public static PreferredJsonbType fromMap(Class<?> keyClass, Class<?> valueClass) {
        if (keyClass == String.class && valueClass == Object.class) {
            return MapJsonType.INSTANCE;
        }
        return new MapJsonType(keyClass, valueClass);
    }

    public static PreferredJsonbType fromList(Class<?> elementClass) {
        return ListJsonType.INSTANCE_CACHE.get(elementClass);
    }

    public static PreferredJsonbType fromSet(Class<?> elementClass) {
        return SetJsonType.INSTANCE_CACHE.get(elementClass);
    }


    private static final ClassValue<PreferredJsonbType> CLASS_VALUE = FuncClassValue.create(PreferredJsonbType::new);


    private PreferredJsonbType(Class<?> javaType) {
        super(javaType);
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        final SQLType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = PgType.JSONB;
                break;
            case MySQL:
                dataType = MySQLType.JSON;
                break;
            case Oracle:
            case H2:
            default:
                throw mapError(this, meta);
        }
        return dataType;
    }


    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        if (getClass() != PreferredJsonbType.class) {
            throw dontSupportArrayType(this);
        }
        return PreferredJsonbArrayType.from(this.javaType);
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


    private static final class SetJsonType extends PreferredJsonbType implements UnaryGenericsMapping {

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

    private static final class ListJsonType extends PreferredJsonbType implements UnaryGenericsMapping {

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

    private static final class MapJsonType extends PreferredJsonbType implements DualGenericsMapping {

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
