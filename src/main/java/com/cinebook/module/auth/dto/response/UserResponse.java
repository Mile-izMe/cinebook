package com.cinebook.module.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private UUID roleId;
    private String email;
    private String userName;
    private String phone;
    private String roleCode;
    private String avatarUrl;
}
