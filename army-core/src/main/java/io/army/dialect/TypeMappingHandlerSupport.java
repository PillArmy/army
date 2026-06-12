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

package io.army.dialect;

import io.army.mapping.MappingType;
import io.army.mapping.StringType;
import io.army.mapping.array.StringArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.ArmyType;
import io.army.sqltype.CustomType;
import io.army.sqltype.DataType;
import io.army.util._Collections;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

abstract non-sealed class TypeMappingHandlerSupport implements MappingHandler {

    private final ServerMeta serverMeta;


    private final BiFunction<String, ServerMeta, TypeMappingBundle> unrecognizedMappingFunc;

    private final Map<String, TypeMappingBundle> dataTypeToBundleMap;

    TypeMappingHandlerSupport(DialectEnv env) {
        this.serverMeta = env.serverMeta();
        this.dataTypeToBundleMap = Map.copyOf(createDataTypeToBundleMap(env.definedTypeSet()));
        this.unrecognizedMappingFunc = env.unrecognizedMappingFunc();
    }


    final TypeMappingBundle handleDefined(final String typeName) {
        TypeMappingBundle bundle;
        bundle = this.dataTypeToBundleMap.get(typeName.toUpperCase(Locale.ROOT));
        if (bundle != null) {
            return bundle;
        }

        final BiFunction<String, ServerMeta, TypeMappingBundle> function = this.unrecognizedMappingFunc;
        if (function != null) {
            bundle = function.apply(typeName, this.serverMeta);
        }

        if (bundle != null) {
            return bundle;
        }


        final DataType dataType = CustomType.builder()
                .typeName(typeName)
                .javaType(Object.class)
                .componentType(ArmyType.UNKNOWN)
                .build();
        if (dataType.isArray()) {
            bundle = TypeMappingBundle.of(dataType, StringArrayType.UNLIMITED);
        } else {
            bundle = TypeMappingBundle.of(dataType, StringType.INSTANCE);
        }
        return bundle;
    }

    private Map<String, TypeMappingBundle> createDataTypeToBundleMap(Set<MappingType> typSet) {
        final Map<String, TypeMappingBundle> map = _Collections.hashMapForSize(typSet.size() << 1);
        DataType dataType;
        TypeMappingBundle bundle;
        for (MappingType mappingType : typSet) {
            dataType = mappingType.map(this.serverMeta);
            if (!(dataType instanceof CustomType)) {
                continue;
            }
            bundle = TypeMappingBundle.of(dataType, mappingType);
            map.put(Objects.requireNonNull(dataType.typeName()), bundle);
            map.put(Objects.requireNonNull(dataType.safeTypeAlias()), bundle);
        }
        return Map.copyOf(map);
    }


}
