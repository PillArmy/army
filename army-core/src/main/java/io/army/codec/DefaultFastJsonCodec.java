package io.army.codec;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;

import java.lang.reflect.Type;
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
        try {
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            throw new CodecException(e);
        }
    }

    @Override
    public <T> T decode(String json, Class<T> objectClass) {
        try {
            return JSON.parseObject(json, objectClass);
        } catch (Exception e) {
            throw new CodecException(e);
        }
    }

    @Override
    public <T> List<T> decodeList(String json, Class<T> elementClass) {
        try {
            return JSON.parseArray(json, elementClass);
        } catch (Exception e) {
            throw new CodecException(e);
        }
    }

    @Override
    public <K, V> Map<K, V> decodeMap(String json, Class<K> keyClass, Class<V> valueClass) throws CodecException {
        try {
            return JSON.parseObject(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new CodecException(e);
        }
    }

    @Override
    public <K, V> List<Map<K, V>> decodeMapList(String json, Class<K> keyClass, Class<V> valueClass) throws CodecException {
        try {
            Type mapType = new ParameterizedTypeImpl(
                    new Type[]{keyClass, valueClass},
                    null,
                    Map.class
            );

            // 2. 构建外层泛型：List<Map<K, V>>
            Type listType = new ParameterizedTypeImpl(
                    new Type[]{mapType},
                    null,
                    List.class
            );

            return JSON.parseObject(json, listType);
        } catch (Exception e) {
            throw new CodecException(e);
        }
    }


}
