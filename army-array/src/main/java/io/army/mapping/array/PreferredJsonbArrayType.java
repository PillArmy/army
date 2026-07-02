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
import io.army.mapping.MappingType;
import io.army.mapping.PreferredJsonbType;
import io.army.mapping.UnaryGenericsMapping;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.util.List;
import java.util.Map;
import java.util.Set;


/// @see PreferredJsonbType
public class PreferredJsonbArrayType extends ArmyJsonArrayType {

    public static PreferredJsonbArrayType from(final Class<?> javaType) {
        final PreferredJsonbArrayType instance;
        if (javaType == String[].class) {
            instance = TEXT_LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(PreferredJsonbArrayType.class, javaType);
        } else {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        }
        return instance;
    }

    public static PreferredJsonbArrayType fromTypeArg(final Class<?> javaType, final Class<?> typeArg) {
        if (!javaType.isArray()) {
            throw errorJavaType(PreferredJsonbArrayType.class, javaType);
        }
        final Class<?> componentType = ArrayUtils.underlyingComponent(javaType);
        final PreferredJsonbType underlyingType;
        if (componentType == List.class) {
            underlyingType = PreferredJsonbType.fromList(typeArg);
        } else if (componentType == Set.class) {
            underlyingType = PreferredJsonbType.fromSet(typeArg);
        } else {
            throw errorJavaType(PreferredJsonbArrayType.class, javaType);
        }
        return new CollectionJsonbArrayType(javaType, underlyingType);
    }

    public static PreferredJsonbArrayType fromTypeArgs(final Class<?> javaType, final Class<?> keyClass, final Class<?> valueClass) {
        if (!javaType.isArray()) {
            throw errorJavaType(PreferredJsonbArrayType.class, javaType);
        }
        final Class<?> componentType = ArrayUtils.underlyingComponent(javaType);
        if (componentType != Map.class) {
            throw errorJavaType(PreferredJsonbArrayType.class, javaType);
        }

        return new MapJsonbArrayType(javaType, PreferredJsonbType.fromMap(keyClass, valueClass));
    }


    public static final PreferredJsonbArrayType TEXT_LINEAR = new PreferredJsonbArrayType(String[].class, PreferredJsonbType.TEXT);

    static {
        addArrayFromFunc(PreferredJsonbArrayType.class, PreferredJsonbArrayType::from);
        addArrayFromTypeArgFunc(PreferredJsonbArrayType.class, PreferredJsonbArrayType::fromTypeArg);
        addArrayFromTypeArgsFunc(PreferredJsonbArrayType.class, PreferredJsonbArrayType::fromTypeArgs);
    }

    final PreferredJsonbType underlyingType;

    /// private constructor
    private PreferredJsonbArrayType(Class<?> javaType, PreferredJsonbType underlyingType) {
        super(javaType);
        this.underlyingType = underlyingType;
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        return JsonbArrayType.mapDataType(this, meta); // currently ,same
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

    private static final class ClassValueHolder {

        private static PreferredJsonbArrayType fromObj(Class<?> javaType) {
            return new PreferredJsonbArrayType(javaType, PreferredJsonbType.from(ArrayUtils.underlyingComponent(javaType)));
        }

        private static final ClassValue<PreferredJsonbArrayType> CLASS_VALUE = FuncClassValue.create(ClassValueHolder::fromObj);

    }

    private static final class CollectionJsonbArrayType extends PreferredJsonbArrayType implements UnaryGenericsMapping {

        private CollectionJsonbArrayType(Class<?> javaType, PreferredJsonbType underlyingType) {
            super(javaType, underlyingType);
        }

        @Override
        public Class<?> genericsType() {
            return ((UnaryGenericsMapping) this.underlyingType).genericsType();
        }

    } // CollectionJsonArrayType

    private static final class MapJsonbArrayType extends PreferredJsonbArrayType implements DualGenericsMapping {

        private MapJsonbArrayType(Class<?> javaType, PreferredJsonbType underlyingType) {
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
