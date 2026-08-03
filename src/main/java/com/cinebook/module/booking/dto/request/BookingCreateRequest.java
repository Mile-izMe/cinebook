package com.cinebook.module.booking.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BookingCreateRequest(
        @NotNull(message = "Showtime must no be empty!")
        UUID showtimeId,

        @NotEmpty(message = "Must select at least 1 chair!")
        List<UUID> seatIds
) {
}