package com.cj.shop.common.caching;

/**
 * @author tangxd
 * @Description: TODO
 * @date 2017/10/27
 */
public interface CacheObjectSerializer {
    String serialize(Object obj);

    <T> T deserialize(String str, Class<T> valueType);
}
