package com.cinebook.module.showtime.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShowtimeCreateRequest(
        @NotNull(message = "Movie must not be empty!")
        UUID movieId,

        @NotNull(message = "Room must not be empty!")
        UUID roomId,

        @NotNull(message = "Start time must not be empty!")
        @Future(message = "Start time must be in future!")
        LocalDateTime startTime,

        @NotBlank(message = "Format must not be empty!")
        @Pattern(regexp = "^(2D|3D|IMAX)$", message = "Format only accepts 2D, 3D or IMAX!")
        String format,

        @NotNull @Positive(message = "Ticket price must be larger than 0!")
        Integer basePrice
) {
}
