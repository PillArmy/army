package io.army.codec;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

import java.util.List;
import java.util.Map;

public final class DefaultFastJsonCodec implements JsonCodec {

    public static DefaultFastJsonCodec getInstance() {
        return INSTANCE;
    }

    private static final DefaultFastJsonCodec INSTANCE = new DefaultFastJsonCodec();

    private DefaultFastJsonCodec() {

    }

    @Override
    public String encode(Object obj) {
        return JSON.toJSONString(obj);
    }

    @Override
    public <T> T decode(String json, Class<T> objectClass) {
        return JSON.parseObject(json, objectClass);
    }

    @Override
    public <T> List<T> decodeList(String json, Class<T> elementClass) {
        return JSON.parseArray(json, elementClass);
    }

    @Override
    public <K, V> Map<K, V> decodeMap(String json, Class<K> keyClass, Class<V> valueClass) throws CodecException {
        return JSON.parseObject(json, new TypeReference<>() {
        });
    }
}
