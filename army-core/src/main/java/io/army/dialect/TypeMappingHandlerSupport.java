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

import io.army.function.DefinedTypeMapFunc;
import io.army.mapping.MappingType;
import io.army.meta.ServerMeta;

import java.util.Map;

abstract non-sealed class TypeMappingHandlerSupport implements TypeMappingHandler {

    private final ServerMeta serverMeta;

    private final DefinedTypeMapFunc function;

    private final Map<String, MappingType> typeMap;

    TypeMappingHandlerSupport(DialectEnv env) {
        this.serverMeta = env.serverMeta();
        this.function = env.definedTypeMapFunc();

        this.typeMap = Map.copyOf(env.nameToTypeMap());

    }


    final MappingType handleDefinedType(final String typeName, final MappingType defaultType) {
        final DefinedTypeMapFunc func = this.function;
        MappingType type = null;
        if (func != null) {
            type = func.apply(typeName, this.serverMeta, this.typeMap);
        }
        if (type != null) {
            return type;
        }
        final int index = typeName.indexOf('[');
        if (index < 0) {
            type = this.typeMap.getOrDefault(typeName, defaultType);
        } else if ((type = this.typeMap.get(typeName.substring(0, index))) == null) {
            //实现 SqlArray 的类不能实现 SqlComposite, 因为数组类型是因基础类型产生的
            type = defaultType;
        } else {
            type = ((MappingType.SqlArray) type.arrayTypeOfThis()).unlimited();
        }
        return type;
    }


}
