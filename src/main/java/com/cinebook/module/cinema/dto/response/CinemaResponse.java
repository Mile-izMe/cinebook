package com.cinebook.module.cinema.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CinemaResponse(
        UUID id,
        String name,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        UUID cityId,
        String cityName
) {
}
