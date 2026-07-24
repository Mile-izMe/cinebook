package com.cinebook.module.movie.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GenreCreateRequest(

        @NotBlank(message = "Genre name must not be empty!")
        String name

) {
}
