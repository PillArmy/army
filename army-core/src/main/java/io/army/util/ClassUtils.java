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

package io.army.util;

import io.army.lang.Nullable;
import io.army.struct.LabelEnum;

import java.util.ArrayList;
import java.util.List;

/// Utility class for class loading and reflection operations.
///
/// @since 0.6.0
public abstract class ClassUtils {

    public static final String PUBLISHER_CLASS_NAME = "org.reactivestreams.Publisher";

    public static final String FLUX_CLASS_NAME = "reactor.core.publisher.Flux";

    public static boolean isReactivePresent() {
        return isPresent("io.army.reactive.Session", null);
    }

    public static boolean isSyncPresent() {
        return isPresent("io.army.sync.Session", null);
    }


    private ClassUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean isPresent(String className, @Nullable ClassLoader classLoader) {
        boolean present;
        try {
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            Class.forName(className, false, classLoader);
            present = true;
        } catch (ClassNotFoundException e) {
            present = false;
        }
        return present;
    }

    public static boolean isAssignableFrom(String className, @Nullable ClassLoader classLoader, Class<?> target) {
        try {
            return Class.forName(className, false, classLoader).isAssignableFrom(target);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }


    @Nullable
    public static String safeClassName(@Nullable Object value) {
        return value == null ? null : value.getClass().getName();
    }

    public static Class<?> enumClass(Class<?> clazz) {
        return checkEnumClass(clazz);
    }

    public static Enum<?>[] enumConstantsOf(Class<?> clazz) {
        return checkEnumClass(clazz).getEnumConstants();
    }

    /// @return unmodifiable list
    public static List<String> enumNameListOf(Class<?> clazz) {
        final Enum<?>[] enumArray = checkEnumClass(clazz).getEnumConstants();
        final List<String> list = new ArrayList<>(enumArray.length);
        for (Enum<?> e : enumArray) {
            list.add(e.name());
        }
        return List.copyOf(list);
    }

    /// @return unmodifiable list
    public static List<String> textEnumLabelList(Class<?> clazz) {
        final Class<? extends Enum<?>> enumClass;
        enumClass = checkEnumClass(clazz);
        if (!LabelEnum.class.isAssignableFrom(enumClass)) {
            String m = String.format("%s not %s", enumClass.getName(), LabelEnum.class.getName());
            throw new IllegalArgumentException(m);
        }
        final Enum<?>[] enumArray = enumClass.getEnumConstants();
        final List<String> list = new ArrayList<>(enumArray.length);
        for (Enum<?> e : enumArray) {
            list.add(((LabelEnum) e).label());
        }
        return List.copyOf(list);
    }


    @Nullable
    public static Class<?> tryLoadClass(final String className, @Nullable ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        Class<?> clazz;
        try {
            clazz = Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException e) {
            clazz = null;
        }
        return clazz;
    }

    public static boolean isAssignableFrom(final Class<?> left, final Class<?> right) {
        final boolean assignable;
        if (left.isAssignableFrom(right)) {
            assignable = true;
        } else if (right.isPrimitive()) {
            assignable = left.isAssignableFrom(wrapperClassOf(right));
        } else if (left.isPrimitive()) {
            assignable = right == wrapperClassOf(left);
        } else {
            assignable = false;
        }
        return assignable;
    }


    public static boolean isWrapperClass(final Class<?> wrapperClass, final Class<?> primitiveClass) {
        final boolean match;
        if (primitiveClass.isPrimitive()) {
            match = wrapperClass == wrapperClassOf(primitiveClass);
        } else {
            match = false;
        }
        return match;
    }

    public static Class<?> wrapperClassIfNeed(final Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return wrapperClassOf(clazz);
        }
        return clazz;
    }

    public static Class<?> wrapperClassOf(final Class<?> primitive) {
        final Class<?> wrapper;
        if (primitive == int.class) {
            wrapper = Integer.class;
        } else if (primitive == long.class) {
            wrapper = Long.class;
        } else if (primitive == boolean.class) {
            wrapper = Boolean.class;
        } else if (primitive == double.class) {
            wrapper = Double.class;
        } else if (primitive == float.class) {
            wrapper = Float.class;
        } else if (primitive == short.class) {
            wrapper = Short.class;
        } else if (primitive == byte.class) {
            wrapper = Byte.class;
        } else if (primitive == char.class) {
            wrapper = Character.class;
        } else if (primitive == void.class) {
            wrapper = Void.class;
        } else {
            throw new IllegalArgumentException(String.format("%s isn't primitive", primitive.getName()));
        }
        return wrapper;
    }


    @SuppressWarnings("unchecked")
    private static Class<? extends Enum<?>> checkEnumClass(Class<?> clazz) {
        if (!Enum.class.isAssignableFrom(clazz)) {
            String m = String.format("%s isn't enum", clazz.getName());
            throw new IllegalArgumentException(m);
        }
        if (clazz.isAnonymousClass()) {
            clazz = clazz.getSuperclass();
        }
        return (Class<? extends Enum<?>>) clazz;
    }


}
