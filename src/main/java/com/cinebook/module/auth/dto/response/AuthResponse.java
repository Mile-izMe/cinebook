package com.cinebook.module.auth.dto.response;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UUID id,
        String avatarUrl
) {
}
