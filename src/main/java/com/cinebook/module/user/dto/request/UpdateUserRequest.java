package com.cinebook.module.user.dto.request;

import jakarta.annotation.Nullable;

public record UpdateUserRequest(

        @Nullable
        String userName,

        @Nullable
        String email,

        @Nullable
        String phone,

        @Nullable
        String avatarUrl

) {
}
