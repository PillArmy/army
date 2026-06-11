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
import io.army.sqltype.DataType;
import io.army.util._Collections;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

abstract non-sealed class TypeMappingHandlerSupport implements MappingHandler {

    private final ServerMeta serverMeta;


    private final BiFunction<String, ServerMeta, MappingType> unrecognizedMappingFunc;

    private final Map<String, TypeMappingBundle> dataTypeToBundleMap;

    TypeMappingHandlerSupport(DialectEnv env) {
        this.serverMeta = env.serverMeta();
        this.dataTypeToBundleMap = Map.copyOf(createDataTypeToBundleMap(env.nameToTypeMap()));
        this.unrecognizedMappingFunc = env.unrecognizedMappingFunc();
    }


    final TypeMappingBundle handleDefined(final String typeName) {
        final String upperCaseTypeName = typeName.toUpperCase(Locale.ROOT);

        final TypeMappingBundle bundle;
        bundle = this.dataTypeToBundleMap.get(upperCaseTypeName);
        if (bundle != null) {
            return bundle;
        }

        final DataType dataType = DataType.from(typeName);

        final BiFunction<String, ServerMeta, MappingType> function = this.unrecognizedMappingFunc;
        MappingType mappingType = null;
        if (function != null) {
            mappingType = function.apply(upperCaseTypeName, this.serverMeta);
        }
        if (mappingType == null) {
            if (dataType.isArray()) {
                mappingType = StringArrayType.UNLIMITED;
            } else {
                mappingType = StringType.INSTANCE;
            }
        }
        return TypeMappingBundle.of(dataType, mappingType);
    }

    private static Map<String, TypeMappingBundle> createDataTypeToBundleMap(Map<String, MappingType> typeMap) {
        final Map<String, TypeMappingBundle> map = _Collections.hashMapForSize(typeMap.size());
        DataType dataType;
        MappingType mappingType;
        String typeName;
        for (Map.Entry<String, MappingType> e : typeMap.entrySet()) {
            typeName = e.getKey();
            Objects.requireNonNull(typeMap);

            dataType = DataType.from(typeName);
            mappingType = Objects.requireNonNull(e.getValue());

            map.put(typeName, TypeMappingBundle.of(dataType, mappingType));
        }
        return Map.copyOf(map);
    }


}
