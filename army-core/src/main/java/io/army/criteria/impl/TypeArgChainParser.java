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

package io.army.criteria.impl;

import io.army.lang.Nullable;
import io.army.util.ArrayUtils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

final class TypeArgChainParser {

    private final Class<?>[] paramTypeArray;

    private final List<Object> valueList;


    TypeArgChainParser(Class<?>[] paramTypeArray) {
        this.paramTypeArray = paramTypeArray;
        this.valueList = new ArrayList<>(paramTypeArray.length);
    }


    @Nullable
    Object[] parseTypeChain(final Type type) {
        int index = 0;
        if (type instanceof ParameterizedType) {
            index = parseParameterizedType(index, (ParameterizedType) type);
        } else if (type instanceof GenericArrayType) {
            index = parseGenericArrayType(index, (GenericArrayType) type);
        } else {
            throw new IllegalArgumentException();
        }

        final Object[] valueArray;
        if (index != this.paramTypeArray.length || this.valueList.size() != this.paramTypeArray.length) {
            valueArray = null;
        } else {
            valueArray = this.valueList.toArray(new Object[0]);
        }
        return valueArray;
    }


    private int parseParameterizedType(int offset, final ParameterizedType genericType) {
        final Type[] typeArray;
        typeArray = genericType.getActualTypeArguments();
        final List<Object> valueList = this.valueList;

        Type type;
        if ((type = genericType.getRawType()) instanceof Class<?>) {
            offset++;
            valueList.add(type);
        } else {
            offset = -7;
        }

        for (int i = 0; offset > -1 && i < typeArray.length; i++) {
            type = typeArray[i];
            if (type instanceof ParameterizedType) {
                offset = parseParameterizedType(offset, (ParameterizedType) type);
            } else if (type instanceof GenericArrayType) {
                offset = parseGenericArrayType(offset, (GenericArrayType) type);
            } else if (type instanceof Class<?>) {
                offset++;
                valueList.add(type);
            } else {
                offset = -7;
            }

        } // loop
        return offset;
    }


    private int parseGenericArrayType(int offset, final GenericArrayType genericType) {
        final List<Object> valueList = this.valueList;
        int dimension = 0;
        Type type = genericType;
        for (; type instanceof GenericArrayType; dimension++) {
            type = ((GenericArrayType) type).getGenericComponentType();
        }

        final Type rawType;
        if (type instanceof Class<?>) {
            valueList.add(ArrayUtils.arrayClassOf((Class<?>) type, dimension));
            offset++;
        } else if (!(type instanceof ParameterizedType)) {
            offset = -7;
        } else if ((rawType = ((ParameterizedType) type).getRawType()) instanceof Class<?>) {
            valueList.add(ArrayUtils.arrayClassOf((Class<?>) rawType, dimension));
            offset++;
            offset = parseParameterizedType(offset, (ParameterizedType) type);
        } else {
            offset = -7;
        }
        return offset;
    }


}
