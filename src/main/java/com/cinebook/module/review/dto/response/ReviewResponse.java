package com.cinebook.module.review.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID userId,
        String userName,
        String userAvatarUrl,
        BigDecimal rating,
        String comment,
        Instant createdAt
) {
}
