package com.cinebook.module.user.controller;

import com.cinebook.common.response.ApiSuccessResponse;
import com.cinebook.common.security.CustomerUserDetails;
import com.cinebook.module.auth.dto.response.UserResponse;
import com.cinebook.module.user.dto.request.UpdateUserRequest;
import com.cinebook.module.user.dto.response.UserStatsResponse;
import com.cinebook.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/stats")
    public ResponseEntity<ApiSuccessResponse<UserStatsResponse>> getMyStats(
            @AuthenticationPrincipal CustomerUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        UserStatsResponse stats = userService.getMyStats(userId);
        return ResponseEntity.ok(ApiSuccessResponse.<UserStatsResponse>builder()
                .message("Get user stats success!")
                .data(stats)
                .build()
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiSuccessResponse<UserResponse>> updateUserProfile(
            @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal CustomerUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();
        UserResponse response = userService.updateProfile(userId, request);

        return ResponseEntity.ok().body(ApiSuccessResponse.<UserResponse>builder()
                .message("Update user profile success!")
                .data(response)
                .build());
    }
}
