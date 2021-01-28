package org.apache.activemq.broker.region.chenk;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.io.Serializable;

/**
 * @Author chenk
 * @create 2021/1/15 9:42
 */

public class RedisPlugin {
    private static RedisTemplate<Serializable, Object> redisTemplate;

    public RedisPlugin() {
    }

    public static RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<Serializable, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public static Object getByKey(String key) {
        ValueOperations<Serializable, Object> valueOperation = redisTemplate.opsForValue();
        return valueOperation.get(key);
    }
    public static void put(String key, Object value) {
        ValueOperations<Serializable, Object> valueOperation = redisTemplate.opsForValue();
        valueOperation.set(key, value);
    }
}