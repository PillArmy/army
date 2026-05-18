package io.army.example.stock.web.controller;

import io.army.util._Collections;

import java.util.Map;

public abstract class DataUtils {

    private DataUtils() {
    }

    public static Map<String, Object> ok() {
        Map<String, Object> map = createOkResultMap();
        map.put("data", Map.of());
        return map;
    }

    public static Map<String, Object> ok(Object value) {
        Map<String, Object> map = createOkResultMap();
        map.put("data", value);
        return map;
    }

    public static Map<String, Object> ok(String key, Object value) {
        final Map<String, Object> map = createOkResultMap();
        map.put("data", Map.of(key, value));
        return map;
    }

    public static Map<String, Object> serverError(int code, String msg) {
        Map<String, Object> map = _Collections.hashMapForSize(5);
        map.put("code", code);
        map.put("msg", msg);
        map.put("data", Map.of());
        return map;
    }


    private static Map<String, Object> createOkResultMap() {
        Map<String, Object> map = _Collections.hashMapForSize(5);
        map.put("code", 0);
        map.put("msg", "ok");
        return map;
    }


}
