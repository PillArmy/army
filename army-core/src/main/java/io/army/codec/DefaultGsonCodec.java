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

package io.army.codec;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DefaultGsonCodec implements JsonCodec {

    public static DefaultGsonCodec create(Gson gson) {
        return new DefaultGsonCodec(gson);
    }

    private final Gson gson;

    private DefaultGsonCodec(Gson gson) {
        this.gson = gson;
    }

    @Override
    public String encode(Object obj) throws CodecException {
        try {
            return this.gson.toJson(obj);
        } catch (Exception e) {
            throw new CodecException(e);
        }
    }

    @Override
    public <T> T decode(String json, Class<T> objectClass) throws CodecException {
        try {
            return this.gson.fromJson(json, objectClass);
        } catch (JsonSyntaxException e) {
            throw new CodecException(e);
        }
    }

    @Override
    public <T> List<T> decodeList(String json, Class<T> elementClass) throws CodecException {

        try {
            final var listType = TypeToken.getParameterized(ArrayList.class, elementClass).getType();
            return this.gson.fromJson(json, listType);
        } catch (JsonSyntaxException e) {
            throw new CodecException(e);
        }
    }

    @Override
    public <K, V> Map<K, V> decodeMap(String json, Class<K> keyClass, Class<V> valueClass) throws CodecException {

        try {
            final var listType = TypeToken.getParameterized(HashMap.class, keyClass, valueClass).getType();

            return this.gson.fromJson(json, listType);
        } catch (JsonSyntaxException e) {
            throw new CodecException(e);
        }
    }

    @Override
    public <K, V> List<Map<K, V>> decodeMapList(String json, Class<K> keyClass, Class<V> valueClass) throws CodecException {
        try {
            Type mapType = TypeToken.getParameterized(Map.class, keyClass, valueClass).getType();
            Type listMapType = TypeToken.getParameterized(List.class, mapType).getType();
            return gson.fromJson(json, listMapType);
        } catch (Exception e) {
            throw new CodecException(e);
        }
    }


}
