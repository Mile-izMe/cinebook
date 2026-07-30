package com.cinebook.module.city.dto.response;

import java.util.UUID;

public record CityResponse(
        UUID id,
        String cityName
) {
}
