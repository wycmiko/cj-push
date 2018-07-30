package com.cj.shop.service.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * properties工具类
 * properties
 *
 * @author yuchuanWeng( )
 * @date 2018/4/14
 * @since 1.0
 */
public class PropertiesUtil {

    /**
     * 修改properties（保留原Properties 添加新）
     *
     * @param properties_json 查询出来的properties属性
     * @param map             前台传来的参数
     * @return properties的json串
     */
    public static String changeProperties(String properties_json, Map<String, Object> map) {
        JSONObject jsonObject = null;
        if (StringUtils.isEmpty(properties_json)) {
            properties_json = "{}";
        }
        if (map != null && !map.isEmpty()) {
            jsonObject = JSONObject.parseObject(properties_json, JSONObject.class);
            Set<Map.Entry<String, Object>> entries = map.entrySet();
            for (Map.Entry<String, Object> m : entries) {
                jsonObject.put(m.getKey(), m.getValue());
            }
            if (jsonObject != null)
            return jsonObject.toJSONString();
        }
        return null;
    }

    public static String addProperties(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
           return "{}";
        } else {
            return JSON.toJSONString(map);
        }
    }

    /**
     * java 8 根据某字段ID去重 保存顺序
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
