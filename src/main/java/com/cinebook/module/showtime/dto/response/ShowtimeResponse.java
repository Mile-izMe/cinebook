package com.cinebook.module.showtime.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShowtimeResponse(
        UUID id, UUID movieId, String movieTitle,
        UUID roomId, String roomName, UUID cinemaId, String cinemaName,
        LocalDateTime startTime, LocalDateTime endTime,
        String format, Integer basePrice
) {
}
