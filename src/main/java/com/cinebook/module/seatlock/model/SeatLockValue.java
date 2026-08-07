package com.cinebook.module.seatlock.model;

import java.time.Instant;
import java.util.UUID;

public record SeatLockValue(
        String ownerId,
        UUID seatId,
        String lockToken,
        String bookingId,
        Instant lockedAt,
        Instant expiresAt,
        Instant maxExpiresAt
) {
}
