package com.cinebook.module.movie.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MovieUpdateRequest(

        @NotBlank
        String title,

        @NotBlank
        String description,

        @NotNull
        @Positive
        Integer duration,

        @NotBlank
        String ageRating,

        @NotNull
        LocalDate releaseDate,

        @NotBlank
        String director,

        @NotEmpty
        List<String> cast,

        String trailerUrl,

        @NotEmpty
        List<UUID> genreIds
) {
}
