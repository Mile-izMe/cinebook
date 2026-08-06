package com.cinebook.module.seatlock.repository.impl;

import com.cinebook.module.seatlock.model.SeatLockValue;
import com.cinebook.module.seatlock.repository.SeatLockRepository;
import com.cinebook.module.seatlock.repository.impl.factory.SeatLockKeyFactory;
import com.cinebook.module.seatlock.repository.impl.provider.LuaScriptProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisSeatLockRepository implements SeatLockRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final LuaScriptProvider provider;
    private final SeatLockKeyFactory keyFactory;

    // SET KEY VALUE NX PX ttl
    @Override
    public boolean acquireLock(String key, SeatLockValue value, Duration ttl) {
        String json = writeJson(value);
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
                .setIfAbsent(key, json, ttl));
    }

    @Override
    public SeatLockValue getLock(String key) {
        String json = redisTemplate.opsForValue().get(key);
        return json != null ? readJson(json) : null;
    }

    @Override
    public void deleteLock(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public Long safeUnLock(String key, String lockToken) {
        RedisScript<Long> luaScript = provider.getSafeUnlockScript();
        return redisTemplate.execute(luaScript, List.of(key), lockToken);
    }

    @Override
    public Set<UUID> findLockedSeatIdsByShowtime(UUID showtimeId) {
        Set<UUID> lockedSeatIds = new HashSet<>();

        String pattern = keyFactory.buildShowtimePattern(showtimeId);
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();

        try (var cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                String seatIdStr = key.substring(key.lastIndexOf(':') + 1);
                lockedSeatIds.add(UUID.fromString(seatIdStr));
            }
        }

        return lockedSeatIds;
    }

    private String writeJson(SeatLockValue value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize SeatLockValue", e);
        }
    }

    private SeatLockValue readJson(String json) {
        try {
            return objectMapper.readValue(json, SeatLockValue.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize SeatLockValue", e);
        }
    }
}
