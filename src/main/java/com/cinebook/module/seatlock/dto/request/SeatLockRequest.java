package com.cinebook.module.seatlock.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SeatLockRequest(
        @NotNull UUID showtimeId,
        @NotEmpty List<UUID> seatIds
) {
}