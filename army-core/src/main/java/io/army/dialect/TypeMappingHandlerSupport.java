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
import io.army.mapping._ArmyBuildInType;
import io.army.meta.ServerMeta;
import io.army.sqltype.ArmyType;
import io.army.sqltype.CustomType;
import io.army.sqltype.DataType;
import io.army.sqltype.SQLType;
import io.army.transaction.Isolation;
import io.army.util._Collections;
import io.army.util._Exceptions;

import java.util.*;
import java.util.function.BiFunction;

abstract non-sealed class TypeMappingHandlerSupport implements MappingHandler {

    private final ServerMeta serverMeta;

    private final BiFunction<String, ServerMeta, TypeMappingBundle> unrecognizedMappingFunc;

    private final Map<String, TypeMappingBundle> buildInTypeToBundleMap;

    private final Map<String, TypeMappingBundle> dataTypeToBundleMap;

    TypeMappingHandlerSupport(DialectEnv env) {
        this.serverMeta = env.serverMeta();
        this.unrecognizedMappingFunc = env.unrecognizedMappingFunc();
        this.buildInTypeToBundleMap = Map.copyOf(createBuildInTypeBundleMap(env));
        this.dataTypeToBundleMap = Map.copyOf(createDefinedTypeToBundleMap(env.definedTypeSet()));

    }


    @Override
    public final TypeMappingBundle handleType(final String typeName) {
        final String upperCaseTypeName = typeName.toUpperCase(Locale.ROOT);

        TypeMappingBundle bundle;
        bundle = buildInTypeToBundleMap.get(upperCaseTypeName);
        if (bundle == null) {
            bundle = handleDefined(typeName, upperCaseTypeName);
        }
        return bundle;
    }

    abstract Map<String, TypeMappingBundle> createBuildInTypeBundleMap(DialectEnv env);


    MappingType obtainStringArrayType() {
        String m = String.format("%s don't support array type", this.serverMeta.usedDialect());
        throw new RuntimeException(m);
    }

    private TypeMappingBundle handleDefined(final String typeName, final String upperCaseTypeName) {
        TypeMappingBundle bundle;
        bundle = this.dataTypeToBundleMap.get(upperCaseTypeName.toUpperCase(Locale.ROOT));
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
            bundle = TypeMappingBundle.of(dataType, obtainStringArrayType());
        } else {
            bundle = TypeMappingBundle.of(dataType, StringType.INSTANCE);
        }
        return bundle;
    }


    static String standardIsolationToName(final Isolation isolation) {
        final String name;
        if (isolation == Isolation.READ_COMMITTED) {
            name = "READ COMMITTED";
        } else if (isolation == Isolation.REPEATABLE_READ) {
            name = "REPEATABLE READ";
        } else if (isolation == Isolation.SERIALIZABLE) {
            name = "SERIALIZABLE";
        } else if (isolation == Isolation.READ_UNCOMMITTED) {
            name = "READ UNCOMMITTED";
        } else {
            throw _Exceptions.unknownIsolation(isolation);
        }
        return name;
    }


    static <E extends Enum<E>> EnumMap<E, MappingType> createSQLTypeToMappingTypeMap(Class<E> enumClass, ServerMeta serverMeta,
                                                                                     Set<MappingType> definedTypeSet) {
        final EnumMap<E, MappingType> map = new EnumMap<>(enumClass);
        final EnumSet<E> set = EnumSet.noneOf(enumClass);

        DataType dataType;
        E key;
        MappingType oldValue;

        boolean oldValueIsBuildIn, newIsBuildIn;

        for (MappingType mappingType : definedTypeSet) {
            dataType = mappingType.map(serverMeta);
            if (!(dataType instanceof SQLType)) {
                continue;
            }
            if (!enumClass.isInstance(dataType)) {
                continue;
            }

            key = enumClass.cast(dataType);

            if (set.contains(key)) {
                continue;
            }

            oldValue = map.put(key, mappingType);

            if (oldValue == null) {
                continue;
            }

            // TODO add 配置,若重复则抛出异常

            oldValueIsBuildIn = oldValue instanceof _ArmyBuildInType;
            newIsBuildIn = mappingType instanceof _ArmyBuildInType;

            if (oldValueIsBuildIn == newIsBuildIn) {
                map.remove(key);
                set.add(key);
            } else if (oldValueIsBuildIn) {
                map.put(key, oldValue);
            }


        } // loop
        return map;
    }


    private Map<String, TypeMappingBundle> createDefinedTypeToBundleMap(Set<MappingType> typSet) {
        final Map<String, TypeMappingBundle> map = _Collections.hashMapForSize(typSet.size() << 1);
        DataType dataType;
        TypeMappingBundle bundle;
        for (MappingType mappingType : typSet) {
            dataType = mappingType.map(this.serverMeta);
            if (!(dataType instanceof CustomType)) {
                continue;
            }
            bundle = TypeMappingBundle.of(dataType, mappingType);
            // TODO add 配置,若重复则抛出异常
            map.put(Objects.requireNonNull(dataType.typeName()).toUpperCase(Locale.ROOT), bundle);
            map.put(Objects.requireNonNull(dataType.safeTypeAlias()).toUpperCase(Locale.ROOT), bundle);
        }
        return Map.copyOf(map);
    }


}
