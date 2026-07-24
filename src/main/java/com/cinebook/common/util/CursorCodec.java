package com.cinebook.common.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Encodes/decodes an opaque cursor for keyset (cursor) pagination.
 * Cursor payload is "{epochMilli}:{id}", base64-encoded so the client
 * cannot easily read or tamper with the raw values.
 * <p>
 * Reused by every list endpoint that paginates on (created_at, id):
 * movies, reviews, bookings, etc.
 */
@Component
public class CursorCodec {

    public record Cursor(
            Instant createdAt,
            UUID id
    ) {
    }

    public String encode(Instant createdAt, UUID id) {
        String raw = createdAt.toEpochMilli() + ":" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<Cursor> decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return Optional.empty();
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split(":", 2);
            return Optional.of(new Cursor(Instant.ofEpochMilli(Long.parseLong(parts[0])), UUID.fromString(parts[1])));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
