package com.cinebook.module.auth.dto.response;

import java.util.UUID;

public record RegisterResponse(UUID id, String email, boolean isVerified) {}
