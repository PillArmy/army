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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.type.CollectionType;
import tools.jackson.databind.type.MapType;
import tools.jackson.dataformat.xml.XmlMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultJacksonXmlCodec implements XmlCodec {

    public static DefaultJacksonXmlCodec create(XmlMapper xmlMapper) {
        return new DefaultJacksonXmlCodec(xmlMapper);
    }


    private final XmlMapper xmlMapper;

    private DefaultJacksonXmlCodec(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    @Override
    public String encode(Object obj) throws CodecException {
        try {
            return xmlMapper.writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new CodecException(e);
        }
    }

    @Override
    public <T> T decode(String xml, Class<T> objectClass) throws CodecException {
        try {
            return xmlMapper.readValue(xml, objectClass);
        } catch (JacksonException e) {
            throw new CodecException(e);
        }
    }

    @Override
    public <T> List<T> decodeList(String xml, Class<T> elementClass) throws CodecException {
        try {
            final CollectionType javaType;
            javaType = this.xmlMapper.getTypeFactory().constructCollectionType(ArrayList.class, elementClass);
            return this.xmlMapper.readValue(xml, javaType);
        } catch (JacksonException e) {
            throw new CodecException(e);
        }
    }

    @Override
    public <K, V> Map<K, V> decodeMap(String xml, Class<K> keyClass, Class<V> valueClass) throws CodecException {
        try {
            final MapType javaType;
            javaType = this.xmlMapper.getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass);
            return this.xmlMapper.readValue(xml, javaType);
        } catch (JacksonException e) {
            throw new CodecException(e);
        }
    }


}
