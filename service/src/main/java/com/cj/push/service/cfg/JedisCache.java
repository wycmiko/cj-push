package com.cj.push.service.cfg;

import com.alibaba.fastjson.JSONObject;
import com.cj.push.common.caching.Cache;
import com.cj.push.common.caching.CacheObjectSerializer;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

/**
 * @author tangxd
 * @Description: TODO
 * @date 2017/10/27
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JedisCache implements Cache {

    @Autowired
    private JedisPool jedisCluster;
    //过期秒数
    @Value("${spring.redis.jedis.expire-time}")
    private int default_time;

    @Autowired
    private CacheObjectSerializer serializer;

    @Override
    public <T> T get(String key, Class<T> valueType) {
        String str;
        try (Jedis jedis = jedisCluster.getResource()) {
            str = jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (Strings.isNullOrEmpty(str)) {
            return null;
        }
        return serializer.deserialize(str, valueType);
    }

    @Override
    public <T> List<T> getList(String key, Class<T> valueType) {
        String str;
        List<T> list = new ArrayList<>();
        try (Jedis jedis = jedisCluster.getResource()) {
            str = jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return list;
        }
        if (Strings.isNullOrEmpty(str)) {
            return Collections.emptyList();
        }
        ArrayList array = serializer.deserialize(str, ArrayList.class);
        try {
            for (Object anArray : array) {
                if (anArray instanceof Integer
                        || anArray instanceof String
                        || anArray instanceof Double
                        || anArray instanceof Float
                        || anArray instanceof Long
                        || anArray instanceof Integer
                        || anArray instanceof String) {
                    T obj = (T) anArray;
                    list.add(obj);
                } else {
                    T obj = JSONObject.toJavaObject((JSONObject) anArray, valueType);
                    list.add(obj);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return list;
    }

    @Override
    public <T> boolean set(String key, T obj, long seconds) {
        if (null == obj) {
            return false;
        }
        String str = serializer.serialize(obj);
        try (Jedis jedis = jedisCluster.getResource()) {
            return "OK".equals(jedis.setex(key, Long.valueOf(seconds).intValue(), str));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public <T> boolean setByDefaultTime(String key, T obj) {
        if (null == obj) {
            return false;
        }
        String str = serializer.serialize(obj);
        try (Jedis jedis = jedisCluster.getResource()) {
            return "OK".equals(jedis.setex(key, Long.valueOf(default_time).intValue(), str));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean del(String key) {
        try (Jedis jedis = jedisCluster.getResource()) {
            return jedis.del(key) == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean del(String... keys) {
        boolean result = true;
        try (Jedis jedis = jedisCluster.getResource()) {
            jedis.del(keys);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean delWithPattern(String pattern) {
        boolean result = true;
        try (Jedis jedis = jedisCluster.getResource()) {
            Set<String> keys = jedis.keys(pattern);
            for (String key : keys) {
                jedis.del(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public long dels(String[] keys) {
        long c = 0;
        try (Jedis jedis = jedisCluster.getResource()) {
            for (String key : keys) {
                c = c + jedis.del(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    @Override
    public <T> Collection<T> gets(Collection<String> keys, Class<T> valueType) {
        List<String> strs = new ArrayList<>();
        try (Jedis jedis = jedisCluster.getResource()) {
            for (String key : keys) {
                strs.add(jedis.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<T> list = new ArrayList<>();
        for (String str : strs) {
            if (Strings.isNullOrEmpty(str)) {
                continue;
            }
            T obj = serializer.deserialize(str, valueType);
            list.add(obj);
        }
        return list;
    }

    @Override
    public <T> T hget(String key, String fieldKey, Class<T> valueType) {
        try (Jedis jedis = jedisCluster.getResource()) {
            String str = jedis.hget(key, fieldKey);
            if (!Strings.isNullOrEmpty(str)) {
                return serializer.deserialize(str, valueType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> boolean hset(String key, String fieldKey, T obj) {
        try (Jedis jedis = jedisCluster.getResource()) {
            Long hset = jedis.hset(key, fieldKey, serializer.serialize(obj));
            jedis.expire(key, default_time);
            return hset > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public <T> List<T> hgetall(String key, Class<T> valueType) {
        List<T> list = new ArrayList<>();
        try (Jedis jedis = jedisCluster.getResource()) {
            Map<String, String> kvs = jedis.hgetAll(key);
            for (Map.Entry<String, String> entry : kvs.entrySet()) {
                if (!Strings.isNullOrEmpty(entry.getValue())) {
                    list.add(serializer.deserialize(entry.getValue(), valueType));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean hdel(String key, String... fieldKeys) {
        if (fieldKeys == null || fieldKeys.length == 0) {
            return false;
        }
        try (Jedis jedis = jedisCluster.getResource()) {
            return jedis.hdel(key, fieldKeys) == fieldKeys.length;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public <T> void hmset(String key, Map<String, T> objs) {
        if (objs == null || objs.size() == 0) {
            return;
        }
        try (Jedis jedis = jedisCluster.getResource()) {
            Map<String, String> map = new TreeMap<>();
            for (Map.Entry<String, T> entry : objs.entrySet()) {
                map.put(entry.getKey(), serializer.serialize(entry.getValue()));
            }
            jedis.hmset(key, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public <T> List<T> hmget(String key, Class<T> valueType, String... params) {
        List<String> strs = new ArrayList<>();
        try (Jedis jedis = jedisCluster.getResource()) {
            strs = jedis.hmget(key, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<T> list = new ArrayList<>();
        for (String str : strs) {
            if (Strings.isNullOrEmpty(str)) {
                continue;
            }
            T obj = serializer.deserialize(str, valueType);
            list.add(obj);
        }
        return list;
    }
}
