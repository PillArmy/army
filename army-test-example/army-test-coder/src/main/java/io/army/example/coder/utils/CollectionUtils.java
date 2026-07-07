package io.army.example.coder.utils;

import java.util.HashMap;
import java.util.Map;

public abstract class CollectionUtils extends org.springframework.util.CollectionUtils {

    private CollectionUtils() {
        throw new UnsupportedOperationException();
    }


    public static Map<String, Object> hastMapForSize(int initialSize) {
        return new HashMap<>((int) (initialSize / 0.75F));
    }


}
