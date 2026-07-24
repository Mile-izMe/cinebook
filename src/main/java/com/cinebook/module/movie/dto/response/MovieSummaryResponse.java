package com.cinebook.module.movie.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MovieSummaryResponse(
        UUID id,
        String title,
        String posterUrl,
        BigDecimal score,
        String ageRating,
        List<String> genres
) {
}
