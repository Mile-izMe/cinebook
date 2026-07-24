package com.cinebook.module.movie.dto.response;

import java.util.UUID;

public record GenreResponse(
        UUID id,
        String name
) {
}
