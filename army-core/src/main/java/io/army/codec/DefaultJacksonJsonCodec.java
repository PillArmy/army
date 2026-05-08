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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DefaultJacksonJsonCodec implements JsonCodec {

    public static DefaultJacksonJsonCodec crate(ObjectMapper objectMapper) {
        return new DefaultJacksonJsonCodec(objectMapper);
    }

    private final ObjectMapper objectMapper;


    private DefaultJacksonJsonCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String encode(Object obj) throws CodecException {
        try {
            return this.objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new CodecException(e);
        }
    }

    @Override
    public <T> T decode(String json, Class<T> objectClass) throws CodecException {
        try {
            return this.objectMapper.readValue(json, objectClass);
        } catch (JsonProcessingException e) {
            throw new CodecException(e);
        }
    }

    @Override
    public <T> List<T> decodeList(String json, Class<T> elementClass) throws CodecException {
        try {
            final CollectionType javaType;
            javaType = this.objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, elementClass);
            return this.objectMapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new CodecException(e);
        }
    }

    @Override
    public Map<String, Object> decodeMap(String json) throws CodecException {
        try {
            final MapType javaType;
            javaType = this.objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
            return this.objectMapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new CodecException(e);
        }
    }
}
