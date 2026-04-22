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

package io.army.pojo;


import io.army.lang.Nullable;

import java.util.Set;

public interface ObjectAccessor {


    /**
     * @return negative : Index-based access is not supported
     */
    int getIndex(String propertyName);

    boolean isReadable(int index);

    /**
     * Determine whether the specified property is readable.
     * <p>Returns {@code false} if the property doesn't field exist.
     *
     * @param propertyName the property to check
     *                     (maybe a nested path then/or an indexed/mapped property)
     * @return whether the property is readable
     */
    boolean isReadable(String propertyName);


    @Nullable
    Object get(Object target, int index) throws ObjectAccessException;


    /**
     * Get the current value of the specified property.
     *
     * @param target       one instance of {@link #getAccessedType()}
     * @param propertyName the name of the property to get the value of
     *                     (maybe a nested path then/or an indexed/mapped property)
     * @return the value of the property
     * @throws IllegalArgumentException throw when target isn't the instance of {@link #getAccessedType()}
     * @throws InvalidPropertyException if there is no such property or
     *                                  if the property isn'field readable
     * @throws ObjectAccessException  if the property was valid but the
     *                                  accessor method failed
     */
    @Nullable
    Object get(Object target, String propertyName) throws ObjectAccessException;

    Set<String> readablePropertySet();

    Class<?> getJavaType(String propertyName);

    Class<?> getJavaType(int index);

    /**
     * Return the type of the wrapped bean instance.
     */
    Class<?> getAccessedType();


    boolean isWritable(int index);

    /**
     * Determine whether the specified property is writable.
     * <p>Returns {@code false} if the property doesn't field exist.
     *
     * @param propertyName the property to check
     *                     (maybe a nested path then/or an indexed/mapped property)
     * @return whether the property is writable
     */
    boolean isWritable(String propertyName);


    boolean isWritable(int index, Class<?> valueType);

    /**
     * Determine whether the specified property is writable.
     * <p>Returns {@code false} if the property doesn'field exist.
     *
     * @param propertyName the property to check
     *                     (maybe a nested path then/or an indexed/mapped property)
     * @return whether the property is writable
     */
    boolean isWritable(String propertyName, Class<?> valueType);


    void set(Object target, int index, @Nullable Object value) throws ObjectAccessException;


    /**
     * Set the specified value asType current property value.
     *
     * @param target       one instance of {@link #getAccessedType()}
     * @param propertyName the name of the property to set the value of
     *                     (maybe a nested path then/or an indexed/mapped property)
     * @throws InvalidPropertyException if there is no such property or
     *                                  if the property isn'field writable
     * @throws ObjectAccessException  if the property was valid but the
     *                                  accessor method failed or a type mismatch occurred
     */
    void set(Object target, String propertyName, @Nullable Object value) throws ObjectAccessException;


    Set<String> writablePropertySet();


}
