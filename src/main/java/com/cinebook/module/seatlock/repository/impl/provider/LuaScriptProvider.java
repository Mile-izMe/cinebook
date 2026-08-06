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

    public RedisScript<Long> getSafeUnlockScript() {
        return new DefaultRedisScript<>(UNLOCK_LUA, Long.class);
    }
}
