package org.apache.activemq.broker.region.chenk;

import org.springframework.data.redis.core.RedisTemplate;
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
        return redisTemplate.opsForValue().get(key);
    }

    public static Object getListByKey(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public static void put(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }
}