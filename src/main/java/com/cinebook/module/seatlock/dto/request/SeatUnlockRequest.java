package com.cinebook.module.seatlock.dto.request;

import java.util.Map;
import java.util.UUID;

public record SeatUnlockRequest(
        UUID showtimeId,
        Map<UUID, String> seatTokens
) {
}