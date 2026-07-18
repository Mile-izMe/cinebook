package com.cinebook.module.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores single-use email-verification tokens in Redis with a TTL.
 * Key layout: verify:email:{token} -> userId
 */
@Service
@RequiredArgsConstructor
public class VerifyTokenService {

    private static final String KEY_PREFIX = "verify:email:";

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.redis.verify-token-ttl-minutes}")
    private long ttlMinutes;

    public String createToken(UUID userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + token, userId.toString(), Duration.ofMinutes(ttlMinutes));
        return token;
    }

    /**
     * Atomically reads and deletes the token in one Redis round-trip (GETDEL),
     * guaranteeing a token can only ever be consumed once even under
     * concurrent requests hitting verify-email at the same time.
     */
    public Optional<UUID> consumeToken(String token) {
        String value = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + token);
        return Optional.ofNullable(value).map(UUID::fromString);
    }
}
