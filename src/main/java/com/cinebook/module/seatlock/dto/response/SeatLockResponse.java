package com.cinebook.module.seatlock.dto.response;

import java.time.Instant;
import java.util.UUID;

public record SeatLockResponse(
        UUID seatId,
        Instant expiresAt
) {
}