package io.army.example.stock.web.controller;

import io.army.util._Collections;

import java.util.Map;

/// Utility class for constructing **standardized API response maps**.
///
/// <p>All responses follow the convention:</p>
/// ```json
/// {"code": 0, "msg": "ok", "data": {...}}
/// ```
///
/// <p>Error responses use non-zero `code` values with descriptive `msg`.</p>
public abstract class DataUtils {


    private static final Map<String, Object> SIMPLE_OK = Map.of("code", 0, "msg", "ok", "data", Map.of());

    private DataUtils() {
    }

    public static Map<String, Object> ok() {
        return SIMPLE_OK;
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
