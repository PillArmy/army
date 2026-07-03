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

import io.army.lang.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class _ArmyBuildInCoreType extends _ArmyBuildInType {

    protected _ArmyBuildInCoreType() {

        final String thisPackageName = this.getClass().getPackageName();

        if (!thisPackageName.equals("io.army.mapping") && !thisPackageName.equals("io.army.mapping.array")) {
            String m = String.format("Non army core class couldn't extend %s .", _ArmyBuildInCoreType.class.getName());
            throw new UnsupportedOperationException(m);
        }
    }


    private static final ConcurrentMap<Class<?>, Function<Class<?>, MappingType>> fromMap = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, BiFunction<Class<?>, String, MappingType>> fromParamFuncMap = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, BiFunction<Class<?>, Class<?>, MappingType>> fromTypeArgFuncMap = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, TeClassFunc> fromTypeArgsFuncMap = new ConcurrentHashMap<>();


    protected static void addArrayFromFunc(Class<? extends MappingType> javaType, Function<Class<?>, MappingType> function) {
        fromMap.put(javaType, function);
    }

    protected static void addArrayFromParamFunc(Class<? extends MappingType> javaType, BiFunction<Class<?>, String, MappingType> function) {
        fromParamFuncMap.put(javaType, function);
    }

    protected static void addArrayFromTypeArgFunc(Class<? extends MappingType> javaType, BiFunction<Class<?>, Class<?>, MappingType> function) {
        fromTypeArgFuncMap.put(javaType, function);
    }

    protected static void addArrayFromTypeArgsFunc(Class<? extends MappingType> javaType, TeClassFunc function) {
        fromTypeArgsFuncMap.put(javaType, function);
    }


    protected static Function<Class<?>, MappingType> removeArrayFromFunc(Class<? extends MappingType> javaType) {
        Function<Class<?>, MappingType> function;
        function = doRemoveFunc(javaType, fromMap, "from");
        if (function == null) {
            function = _ArmyBuildInCoreType::nullFunc;
        }
        return function;
    }

    protected static BiFunction<Class<?>, String, MappingType> removeArrayFromParamFunc(Class<? extends MappingType> javaType) {
        BiFunction<Class<?>, String, MappingType> function;
        function = doRemoveFunc(javaType, fromParamFuncMap, "fromParam");
        if (function == null) {
            function = _ArmyBuildInCoreType::nullParamFunc;
        }
        return function;
    }

    protected static BiFunction<Class<?>, Class<?>, MappingType> removeArrayFromTypeArgFunc(Class<? extends MappingType> javaType) {
        BiFunction<Class<?>, Class<?>, MappingType> function;
        function = doRemoveFunc(javaType, fromTypeArgFuncMap, "fromTypeArg");
        if (function == null) {
            function = _ArmyBuildInCoreType::nullBiFunc;
        }
        return function;
    }

    protected static TeClassFunc removeArrayFromTypeArgsFunc(Class<? extends MappingType> javaType) {
        TeClassFunc function;
        function = doRemoveFunc(javaType, fromTypeArgsFuncMap, "fromTypeArgs");
        if (function == null) {
            function = _ArmyBuildInCoreType::nullTeFunc;
        }
        return function;
    }


    private static MappingType nullFunc(Class<?> javaType) {
        throw notFoundArrayModuleError();
    }

    private static MappingType nullParamFunc(Class<?> javaType, String param) {
        throw notFoundArrayModuleError();
    }

    private static MappingType nullBiFunc(Class<?> javaType, Class<?> typeArg) {
        throw notFoundArrayModuleError();
    }

    private static MappingType nullTeFunc(Class<?> javaType, Class<?> keyClass, Class<?> valueClass) {
        throw notFoundArrayModuleError();
    }


    @Nullable
    private static <T> T doRemoveFunc(Class<?> javaType, Map<Class<?>, T> map, String methodName) {

        final String simpleName;
        simpleName = javaType.getSimpleName();
        final int index;
        index = simpleName.lastIndexOf("Type");
        if (index < 0) {
            throw new IllegalArgumentException("bug");
        }

        final String className = "io.army.mapping.array." + simpleName.substring(0, index) + "ArrayType";

        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }

        final T function;
        function = map.remove(clazz);
        if (function == null) {
            String m = String.format("bug: not found static factory '%s' method of %s for %s", methodName, className, javaType.getName());
            throw new IllegalStateException(m);
        }
        return function;
    }


    private static IllegalStateException notFoundArrayModuleError() {
        return new IllegalStateException("army-array module not in classpath,couldn't load core ArrayType");
    }


    protected interface TeClassFunc {

        MappingType apply(final Class<?> javaType, final Class<?> keyClass, final Class<?> valueClass);
    }


}
