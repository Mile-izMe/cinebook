package com.cinebook.module.lock.base;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

@Service
public class RedisLockBase {

    private final StringRedisTemplate redisTemplate;

    // SET KEY VALUE NX PX ttl => SET lock:seat:st1:E5 "user123" NX PX 300000
    public boolean tryLock(UUID showtimeId, UUID seatId, String randomValue, Duration ttl) {
        String key = String.format("lock:seat:%s:%s", showtimeId, seatId);

        // setIfAbsent <=> SET ... NX (SET IF NX)
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, randomValue, ttl);

        // Avoid NullPointerException when unbox
        return Boolean.TRUE.equals(success);
    }

    /**
     * Delete safety key by Lua Script (If only it matches with randomValue)
     */
    public boolean unlock(UUID showtimeId, UUID seatId, String randomValue) {
        String key = String.format("lock:seat:%s:%s", showtimeId, seatId);

        // Atomic - Lua Script
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "   return redis.call('del', KEYS[1]) " +
                "else " +
                "   return 0 " +
                "end";

        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);

        // Execute Lua in Redis
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), randomValue);

        return result != null && result == 1L; // 1L = Delete success
    }

    public RedisLockBase(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
