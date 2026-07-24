package com.cinebook.module.movie.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * `recentReviews` is only a small preview (first page) so this endpoint stays
 * fast; the full, paginated review list lives at GET /movies/{id}/reviews.
 */
public record MovieDetailResponse(
        String title,
        String description,
        String posterUrl,
        String backdropUrl,
        String trailerUrl,
        Integer duration,
        String ageRating,
        BigDecimal score,
        Long totalReviews,
        LocalDate releaseDate,
        String director,
        List<String> cast,
        List<GenreResponse> genres,
        List<ReviewResponse> recentReviews
) {
}
