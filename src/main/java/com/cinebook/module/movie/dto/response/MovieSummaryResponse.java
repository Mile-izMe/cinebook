package com.cinebook.module.movie.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record MovieSummaryResponse(
        UUID id,
        String title,
        String posterUrl,
        String backdropUrl,
        BigDecimal score,
        String ageRating,
        List<String> genres
) {
}
