package com.cinebook.module.auth.controller;

import com.cinebook.common.response.ApiSuccessResponse;
import com.cinebook.common.security.CustomerUserDetails;
import com.cinebook.module.auth.dto.request.LoginRequest;
import com.cinebook.module.auth.dto.request.LogoutRequest;
import com.cinebook.module.auth.dto.request.RefreshRequest;
import com.cinebook.module.auth.dto.request.RegisterRequest;
import com.cinebook.module.auth.dto.response.AuthResponse;
import com.cinebook.module.auth.dto.response.RegisterResponse;
import com.cinebook.module.auth.dto.response.UserResponse;
import com.cinebook.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Handle Authentication APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiSuccessResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse register = authService.register(request);

        ApiSuccessResponse<RegisterResponse> response = ApiSuccessResponse.<RegisterResponse>builder()
                .message("Register successful")
                .data(register)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse auth = authService.login(request);

        ApiSuccessResponse<AuthResponse> response = ApiSuccessResponse.<AuthResponse>builder()
                .message("Login successful")
                .data(auth)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse auth = authService.refresh(request);

        ApiSuccessResponse<AuthResponse> response = ApiSuccessResponse.<AuthResponse>builder()
                .message("Refresh Token successful")
                .data(auth)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiSuccessResponse<UserResponse>> getMe(
            @AuthenticationPrincipal CustomerUserDetails userDetails) {

        UserResponse profile = authService.getUserProfile(userDetails.getUserId());

        ApiSuccessResponse<UserResponse> response = ApiSuccessResponse.<UserResponse>builder()
                .message("Get user profile successful")
                .data(profile)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request,
                                       Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        authService.logout(userId, request);
        return ResponseEntity.noContent().build();
    }
}
