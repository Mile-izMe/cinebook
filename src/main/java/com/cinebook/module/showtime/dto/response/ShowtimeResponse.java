package com.cinebook.module.showtime.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ShowtimeResponse(
        UUID id,
        UUID movieId,
        String movieTitle,
        Integer movieDuration,
        UUID roomId,
        String roomName,
        UUID cinemaId,
        String cinemaName,
        String cinemaAddress,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String format,
        Integer basePrice,
        List<String> genreNames
) {
}
