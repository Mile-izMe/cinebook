package com.cinebook.module.auth.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Refresh tokens are hashed (SHA-256) before being persisted, same idea as
 * hashing a password: if the DB is ever dumped, raw tokens are not exposed.
 * SHA-256 (not bcrypt) is fine here because we're not defending against
 * brute force on a low-entropy secret - the raw token itself is already a
 * high-entropy random UUID.
 */
@Component
public class TokenHasher {
    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}