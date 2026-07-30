package com.cinebook.module.city.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CityCreateRequest(

        @NotBlank(message = "City name must not be empty!")
        String cityName
) {
}
