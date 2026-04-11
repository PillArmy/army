/*
 * Copyright 2023-2043 the original author or authors.
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

package io.army.bean;


import io.army.lang.Nullable;
import io.army.util.ClassUtils;
import io.army.util._Collections;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * @since 0.6.0
 */
public abstract class ObjectAccessorFactory {

    private ObjectAccessorFactory() {
        throw new UnsupportedOperationException();
    }


    public static final ObjectAccessor MAP_ACCESSOR = MapWriterAccessor.INSTANCE;

    private static final ClassValue<ObjectAccessor> ACCESSOR_CACHE = new AccessorClassValue();

    private static final ClassValue<Supplier<?>> CONSTRUCTOR_CACHE = new ConstructorClassValue();


    public static ObjectAccessor forBean(Class<?> beanClass) {
        return ACCESSOR_CACHE.get(beanClass);
    }


    public static ObjectAccessor fromInstance(final Object instance) {
        final ObjectAccessor accessor;
        if (instance instanceof Map) {
            accessor = MAP_ACCESSOR;
        } else {
            accessor = forBean(instance.getClass());
        }
        return accessor;
    }

    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> beanConstructor(Class<T> beanClass) {
        return (Supplier<T>) CONSTRUCTOR_CACHE.get(beanClass);
    }


    private static ObjectAccessor createMethodAccessors(final Class<?> beanClass) {
        final Map<String, ValueReadAccessor> readerMap = _Collections.hashMap();
        final Map<String, ValueWriteAccessor> writerMap = _Collections.hashMap();

        final Map<String, Class<?>> fieldTypeMap = _Collections.hashMap();

        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        try {
            MethodHandle mh;
            CallSite site;
            String fieldName, methodName;
            int modifiers;
            ValueReadAccessor readAccessor;
            ValueWriteAccessor writeAccessor;
            Class<?> oldFieldType, fieldType;
            int methodType, minLength, minusOne;
            final int SET_METHOD = 1, GET_METHOD = 2;
            for (Class<?> clazz = beanClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
                for (Method method : clazz.getDeclaredMethods()) {

                    modifiers = method.getModifiers();
                    if (!Modifier.isPublic(modifiers)
                            || Modifier.isStatic(modifiers)) {
                        continue;
                    }

                    methodName = method.getName();
                    if (methodName.startsWith("set")) {
                        minLength = 4;
                        if (method.getParameterCount() != 1 || methodName.length() < 4) {
                            continue;
                        }
                        methodType = SET_METHOD;
                    } else if (methodName.startsWith("get")) {
                        minLength = 4;
                        if (method.getParameterCount() != 0
                                || method.getReturnType() == void.class
                                || methodName.length() < 4) {
                            continue;
                        }
                        methodType = GET_METHOD;
                    } else if (methodName.startsWith("is")) {
                        minLength = 3;
                        if (method.getParameterCount() != 0 || method.getReturnType() != boolean.class
                                || methodName.length() < 3) {
                            continue;
                        }
                        methodType = GET_METHOD;
                    } else {
                        continue;
                    }

                    minusOne = minLength - 1;
                    if (methodName.length() == minLength) {
                        fieldName = String.valueOf(Character.toLowerCase(methodName.charAt(minusOne)));
                    } else if (Character.isLowerCase(methodName.charAt(minusOne)) || Character.isUpperCase(methodName.charAt(minLength))) {
                        fieldName = methodName.substring(minusOne);
                    } else {
                        fieldName = Character.toLowerCase(methodName.charAt(minusOne)) + methodName.substring(minLength);
                    }
                    mh = lookup.unreflect(method);
                    switch (methodType) {
                        case SET_METHOD: {
                            fieldType = method.getParameterTypes()[0];
                            site = LambdaMetafactory.metafactory(
                                    lookup,
                                    "set",
                                    MethodType.methodType(ValueWriteAccessor.class),
                                    MethodType.methodType(void.class, Object.class, Object.class),
                                    mh,
                                    MethodType.methodType(void.class, beanClass, fieldType)
                            );
                            writeAccessor = (ValueWriteAccessor) site.getTarget().invokeExact();
                            writerMap.putIfAbsent(fieldName, writeAccessor);

                        }
                        break;
                        case GET_METHOD: {
                            fieldType = method.getReturnType();
                            site = LambdaMetafactory.metafactory(
                                    lookup,
                                    "get",
                                    MethodType.methodType(ValueReadAccessor.class),
                                    MethodType.methodType(Object.class, Object.class),
                                    mh,
                                    MethodType.methodType(fieldType, beanClass)
                            );
                            readAccessor = (ValueReadAccessor) site.getTarget().invokeExact();
                            readerMap.putIfAbsent(fieldName, readAccessor);
                        }
                        break;
                        default:
                            throw new IllegalStateException("unknown method type.");
                    } // switch

                    oldFieldType = fieldTypeMap.putIfAbsent(fieldName, fieldType);
                    if (oldFieldType != null && oldFieldType != fieldType && oldFieldType.isAssignableFrom(fieldType)) {
                        // Override getter
                        fieldTypeMap.put(fieldName, fieldType);
                    }

                }


            }
        } catch (ObjectAccessException e) {
            throw e;
        } catch (Throwable e) {
            throw new ObjectAccessException(e);
        }
        return createAccessor(beanClass, fieldTypeMap, readerMap, writerMap);
    }

    private static ObjectAccessor createFieldAccessorPair(final Class<?> beanClass) {
        final Map<String, ValueWriteAccessor> writerMap = _Collections.hashMap();
        final Map<String, ValueReadAccessor> readerMap = _Collections.hashMap();

        final Map<String, Class<?>> fieldTypeMap = _Collections.hashMap();

        try {
            int modifiers;
            String fieldName;
            ValueReadAccessor fieldReader;
            ValueWriteAccessor fieldWriter;
            for (Class<?> clazz = beanClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
                if (!FieldAccessBean.class.isAssignableFrom(clazz)) {
                    break;
                }
                for (Field field : clazz.getDeclaredFields()) {
                    modifiers = field.getModifiers();
                    if (!Modifier.isPublic(modifiers)
                            || Modifier.isStatic(modifiers)) {
                        continue;
                    }
                    fieldName = field.getName();

                    fieldReader = field::get;
                    readerMap.putIfAbsent(fieldName, fieldReader);

                    fieldWriter = field::set;
                    writerMap.putIfAbsent(fieldName, fieldWriter);

                    fieldTypeMap.putIfAbsent(fieldName, field.getType());
                }

            }
        } catch (Throwable e) {
            throw new ObjectAccessException(e);
        }
        return createAccessor(beanClass, fieldTypeMap, readerMap, writerMap);
    }

    private static ObjectAccessor createAccessor(final Class<?> beanClass, Map<String, Class<?>> fieldTypeMap,
                                                 final Map<String, ValueReadAccessor> readerMap,
                                                 final Map<String, ValueWriteAccessor> writerMap) {

        final Set<String> nameSet = new HashSet<>();
        nameSet.addAll(readerMap.keySet());
        nameSet.addAll(writerMap.keySet());

        final int nameCount = nameSet.size();
        final Map<String, Integer> nameToIndexMap = _Collections.hashMap();
        final Class<?>[] classArray = new Class<?>[nameCount];
        final ValueReadAccessor[] readerArray = new ValueReadAccessor[nameCount];
        final ValueWriteAccessor[] writeArray = new ValueWriteAccessor[nameCount];

        int index = 0;
        boolean setterAndGetterMatch = true;
        ValueReadAccessor reader;
        ValueWriteAccessor writer;
        for (String name : nameSet) {

            nameToIndexMap.put(name, index);

            classArray[index] = fieldTypeMap.get(name);
            reader = readerMap.get(name);
            writer = writerMap.get(name);

            if (reader == null || writer == null) {
                setterAndGetterMatch = false;
            }
            readerArray[index] = reader;
            writeArray[index] = writer;

            index++;
        }

        assert index == nameCount;
        return new BeanWriterAccessor(beanClass, nameToIndexMap, classArray, readerArray, writeArray, setterAndGetterMatch);
    }


    private static final class BeanWriterAccessor implements ObjectAccessor {

        private final Class<?> beanClass;

        private final Map<String, Integer> nameToIndexMap;

        private final Class<?>[] classArray;

        private final ValueReadAccessor[] readerArray;

        private final ValueWriteAccessor[] writeArray;

        private final boolean setterAndGetterMatch;

        private BeanWriterAccessor(Class<?> beanClass, Map<String, Integer> nameToIndexMap,
                                   Class<?>[] classArray, ValueReadAccessor[] readerArray,
                                   ValueWriteAccessor[] writeArray, boolean setterAndGetterMatch) {
            this.beanClass = beanClass;
            this.nameToIndexMap = Collections.unmodifiableMap(nameToIndexMap);
            this.classArray = classArray;
            this.readerArray = readerArray;
            this.writeArray = writeArray;
            this.setterAndGetterMatch = setterAndGetterMatch;
        }


        @Override
        public int getIndex(String propertyName) {
            final Integer index;
            index = this.nameToIndexMap.get(propertyName);
            return index == null ? -1 : index;
        }

        @Override
        public boolean isReadable(String propertyName) {
            final Integer index;
            index = this.nameToIndexMap.get(propertyName);
            return index != null && this.readerArray[index] != null;
        }

        @Override
        public boolean isReadable(int index) {
            return index > -1 && index < this.readerArray.length && this.readerArray[index] != null;
        }

        @Nullable
        @Override
        public Object get(Object target, final int index) throws ObjectAccessException {
            final ValueReadAccessor[] readerArray = this.readerArray;
            if (index < 0 || index >= readerArray.length) {
                throw invalidIndex(index);
            }
            final ValueReadAccessor accessor = readerArray[index];
            if (accessor == null) {
                String m = String.format("index[%s] is not readable for %s", index, this.beanClass.getName());
                throw new ObjectAccessException(m);
            }
            try {
                return accessor.get(target);
            } catch (Exception e) {
                throw accessError(index, e);
            }
        }

        @Nullable
        @Override
        public Object get(final Object target, final String propertyName) throws ObjectAccessException {
            final Integer index;
            index = this.nameToIndexMap.get(propertyName);
            final ValueReadAccessor accessor;
            if (index == null || (accessor = this.readerArray[index]) == null) {
                throw invalidProperty(propertyName);
            }
            try {
                return accessor.get(target);
            } catch (Exception e) {
                throw accessError(propertyName, e);
            }
        }

        @Override
        public Set<String> readablePropertySet() {
            if (this.setterAndGetterMatch) {
                return this.nameToIndexMap.keySet();
            }
            final Map<String, Integer> nameToIndexMap = this.nameToIndexMap;
            final ValueReadAccessor[] readerArray = this.readerArray;
            final Set<String> nameSet = new HashSet<>((int) (readerArray.length / 0.75f));
            for (Map.Entry<String, Integer> e : nameToIndexMap.entrySet()) {
                if (readerArray[e.getValue()] != null) {
                    nameSet.add(e.getKey());
                }
            }
            return Collections.unmodifiableSet(nameSet);
        }

        @Override
        public Class<?> getJavaType(String propertyName) {
            final int index;
            index = getIndex(propertyName);
            if (index < 0) {
                throw invalidProperty(propertyName);
            }
            return this.classArray[index];
        }

        @Override
        public Class<?> getJavaType(final int index) {
            if (index < 0 || index >= this.classArray.length) {
                throw invalidIndex(index);
            }
            return this.classArray[index];
        }

        @Override
        public Class<?> getAccessedType() {
            return this.beanClass;
        }

        @Override
        public boolean isWritable(int index) {
            return index > -1 && index < this.writeArray.length && this.writeArray[index] != null;
        }

        @Override
        public boolean isWritable(String propertyName) {
            final Integer index;
            index = this.nameToIndexMap.get(propertyName);
            return index != null && this.writeArray[index] != null;
        }

        @Override
        public boolean isWritable(final int index, Class<?> valueType) {
            if (index < 0 || index >= this.classArray.length) {
                return false;
            }
            return ClassUtils.isAssignableFrom(this.classArray[index], valueType);
        }

        @Override
        public boolean isWritable(String propertyName, Class<?> valueType) {
            final int index;
            index = getIndex(propertyName);
            if (index < 0) {
                throw invalidProperty(propertyName);
            }
            return isWritable(index, valueType);
        }


        @Override
        public void set(Object target, int index, @Nullable Object value) throws ObjectAccessException {
            final ValueWriteAccessor[] writeArray = this.writeArray;
            if (index < 0 || index >= writeArray.length) {
                throw invalidIndex(index);
            }
            final ValueWriteAccessor accessor = writeArray[index];
            if (accessor == null) {
                String m = String.format("index[%s] is not writeable for %s", index, this.beanClass.getName());
                throw new ObjectAccessException(m);
            }
            try {
                accessor.set(target, value);
            } catch (Exception e) {
                throw accessError(index, e);
            }
        }

        @Override
        public void set(final Object target, final String propertyName, final @Nullable Object value)
                throws ObjectAccessException {

            final Integer index;
            index = this.nameToIndexMap.get(propertyName);
            final ValueWriteAccessor accessor;
            if (index == null || (accessor = this.writeArray[index]) == null) {
                throw invalidProperty(propertyName);
            }
            try {
                accessor.set(target, value);
            } catch (Exception e) {
                throw accessError(propertyName, e);
            }
        }


        @Override
        public Set<String> writablePropertySet() {

            if (this.setterAndGetterMatch) {
                return this.nameToIndexMap.keySet();
            }
            final ValueWriteAccessor[] writeArray = this.writeArray;
            final Map<String, Integer> nameToIndexMap = this.nameToIndexMap;
            final Set<String> nameSet = new HashSet<>((int) (writeArray.length / 0.75f));
            for (Map.Entry<String, Integer> e : nameToIndexMap.entrySet()) {
                if (writeArray[e.getValue()] != null) {
                    nameSet.add(e.getKey());
                }
            }
            return Collections.unmodifiableSet(nameSet);
        }

        @Override
        public String toString() {
            return String.format("%s of %s.", ObjectAccessor.class.getName(), this.beanClass.getName());
        }


        private ObjectAccessException invalidIndex(int index) {
            final Class<?> beanClass = this.beanClass;
            String m = String.format("%s is invalid index for %s", index, beanClass.getName());
            return new ObjectAccessException(m);
        }

        private InvalidPropertyException invalidProperty(String propertyName) {
            final Class<?> beanClass = this.beanClass;
            String m = String.format("%s is invalid property for %s", propertyName, beanClass.getName());
            return new InvalidPropertyException(m, beanClass, propertyName);
        }

        private ObjectAccessException accessError(Object propertyNameOrIndex, Throwable cause) {
            final Class<?> beanClass = this.beanClass;
            final String m;
            final ObjectAccessException e;
            if (propertyNameOrIndex instanceof String) {
                m = String.format("%s property of %s access occur error.", propertyNameOrIndex, beanClass.getName());
                e = new InvalidPropertyException(m, beanClass, (String) propertyNameOrIndex, cause);
            } else {
                m = String.format("index[%s] of %s access occur error.", propertyNameOrIndex, beanClass.getName());
                e = new ObjectAccessException(m, cause);
            }
            return e;
        }


    }//BeanWriterAccessor


    private static final class MapWriterAccessor implements ObjectAccessor {

        private static final MapWriterAccessor INSTANCE = new MapWriterAccessor();

        @Override
        public boolean isWritable(String propertyName) {
            return true;
        }

        @Override
        public boolean isWritable(int index) {
            throw invalidIndex();
        }

        @Override
        public boolean isWritable(int index, Class<?> valueType) {
            throw invalidIndex();
        }

        @Override
        public boolean isWritable(String propertyName, Class<?> valueType) {
            //TODO currently ,always true
            return true;
        }

        @Override
        public Class<?> getJavaType(String propertyName) {
            return Object.class;
        }


        @Override
        public void set(Object target, int index, @Nullable Object value) throws ObjectAccessException {
            throw invalidIndex();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void set(final Object target, final String propertyName, final @Nullable Object value)
                throws ObjectAccessException {

            if (value == null && target instanceof ConcurrentMap) {
                ((Map<String, Object>) target).remove(propertyName);
            } else {
                ((Map<String, Object>) target).put(propertyName, value);
            }

        }


        @Override
        public Set<String> writablePropertySet() {
            // map always empty
            return Set.of();
        }


        @Override
        public boolean isReadable(int index) {
            throw invalidIndex();
        }

        @Override
        public boolean isReadable(final String propertyName) {
            return true;
        }


        @Override
        public Object get(Object target, int index) throws ObjectAccessException {
            throw invalidIndex();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object get(final Object target, final String propertyName) throws ObjectAccessException {
            if (!(target instanceof Map)) {
                throw new IllegalArgumentException("target non-map");
            }
            return ((Map<String, Object>) target).get(propertyName);
        }

        @Override
        public int getIndex(String propertyName) {
            return -1;
        }

        @Override
        public Class<?> getJavaType(int index) {
            throw invalidIndex();
        }

        @Override
        public Set<String> readablePropertySet() {
            // map always empty
            return Set.of();
        }

        @Override
        public Class<?> getAccessedType() {
            return Map.class;
        }


        private static ObjectAccessException invalidIndex() {
            return new ObjectAccessException("accessor of Map don't support index");
        }


    }// MapWriterAccessor

    private static final class AccessorClassValue extends ClassValue<ObjectAccessor> {

        private AccessorClassValue() {
        }

        @Override
        protected ObjectAccessor computeValue(Class<?> type) {
            final ObjectAccessor accessor;
            if (FieldAccessBean.class.isAssignableFrom(type)) {
                accessor = createFieldAccessorPair(type);
            } else {
                accessor = createMethodAccessors(type);
            }
            return accessor;
        }


    } // AccessorClassValue

    private static final class ConstructorClassValue extends ClassValue<Supplier<?>> {

        private ConstructorClassValue() {
        }

        @Override
        protected Supplier<?> computeValue(Class<?> type) {
            final Constructor<?> constructor;
            try {
                constructor = type.getConstructor();
            } catch (NoSuchMethodException e) {
                String m = String.format("%s no default Constructor.", type.getName());
                throw new ObjectAccessException(m, e);
            }

            try {
                final MethodHandles.Lookup lookup = MethodHandles.lookup();
                final MethodHandle mh = lookup.unreflectConstructor(constructor);
                final CallSite site = LambdaMetafactory.metafactory(
                        lookup,
                        "get",
                        MethodType.methodType(Supplier.class),
                        MethodType.methodType(Object.class),
                        mh,
                        MethodType.methodType(type)
                );

                return (Supplier<?>) site.getTarget().invokeExact();
            } catch (Throwable e) {
                String m = String.format("%s constructor LambdaMetafactory error.", type.getName());
                throw new ObjectAccessException(m, e);
            }
        }


    } // ConstructorClassValue


}
