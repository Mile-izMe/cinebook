package com.cinebook.module.auth.controller;

import com.cinebook.module.auth.dto.request.LoginRequest;
import com.cinebook.module.auth.dto.request.LogoutRequest;
import com.cinebook.module.auth.dto.request.RefreshRequest;
import com.cinebook.module.auth.dto.request.RegisterRequest;
import com.cinebook.module.auth.dto.response.AuthResponse;
import com.cinebook.module.auth.dto.response.RegisterResponse;
import com.cinebook.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Handle Authentication APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request,
                                       Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        authService.logout(userId, request);
        return ResponseEntity.noContent().build();
    }
}
