package com.cinebook.module.movie.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MovieCreateRequest(

        @NotBlank(message = "Title must not be empty!")
        String title,

        @NotBlank(message = "Description must not be empty!")
        String description,

        @NotNull(message = "Duration must not be empty!")
        @Positive(message = "Duration must larger than 0")
        Integer duration,

        @NotBlank(message = "Age rating must not be empty!")
        String ageRating,

        @NotNull(message = "Release Date must not be empty!")
        LocalDate releaseDate,

        @NotBlank(message = "Director must not be empty")
        String director,

        @NotEmpty(message = "Cast must not be empty!")
        List<String> cast,

        String trailerUrl,

        @NotEmpty(message = "Film must at least belong to 1 genre!")
        List<UUID> genreIds,

        @Nullable
        String posterUrl,

        @Nullable
        String backdropUrl,

        @Nullable
        String posterObjectKey,

        @Nullable
        String backdropObjectKey
) {
}
