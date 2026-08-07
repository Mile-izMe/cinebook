package com.cinebook.module.seatlock.repository.impl.provider;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
public class LuaScriptProvider {

    // Lua: Read value by cJson, compare ownerId, only delete if match -
    // atomic "check-then-delete", avoid mistaking unlock other's lock.
    private static final String UNLOCK_LUA = """
            local raw = redis.call("get", KEYS[1])
            if raw == false then
                return 0
            end
            
            local data = cjson.decode(raw)
            
            if data.lockToken == ARGV[1] then
                return redis.call("del", KEYS[1])
            else
                return -1
            end
            """;

    private static final String EXTEND_TTL_LUA = """
            local raw = redis.call("get", KEYS[1])
            if raw == false then
                return "EXPIRED"
            end
            
            local data = cjson.decode(raw)
            if data.lockToken ~= ARGV[1] then
                return "TOKEN_MISMATCH"
            end
            
            local now = tonumber(ARGV[2])
            local maxExpiresAt = data.maxExpiresAt
            if now >= maxExpiresAt then
                return "MAX_HOLD_REACHED"
            end
            
            local ttlSeconds = tonumber(ARGV[3])
            local newExpiresAt = math.min(now + ttlSeconds, maxExpiresAt)
            local newTtl = newExpiresAt - now
            
            data.expiresAt = newExpiresAt
            local newJson = cjson.encode(data)
            redis.call("set", KEYS[1], newJson, "EX", newTtl)
            return "EXTENDED:" .. newTtl
            """;

    public RedisScript<Long> getSafeUnlockScript() {
        return new DefaultRedisScript<>(UNLOCK_LUA, Long.class);
    }

    public RedisScript<String> getExtendTtlScript() {
        return new DefaultRedisScript<>(EXTEND_TTL_LUA, String.class);
    }
}
