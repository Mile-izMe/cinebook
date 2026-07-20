package com.cinebook.module.auth.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.common.security.JwtProvider;
import com.cinebook.module.auth.dto.request.LoginRequest;
import com.cinebook.module.auth.dto.request.LogoutRequest;
import com.cinebook.module.auth.dto.request.RefreshRequest;
import com.cinebook.module.auth.dto.request.RegisterRequest;
import com.cinebook.module.auth.dto.response.AuthResponse;
import com.cinebook.module.auth.dto.response.RegisterResponse;
import com.cinebook.module.auth.dto.response.UserResponse;
import com.cinebook.module.auth.entity.RefreshToken;
import com.cinebook.module.auth.event.UserRegisteredEvent;
import com.cinebook.module.auth.messaging.MailEventPublisher;
import com.cinebook.module.auth.repository.RefreshTokenRepository;
import com.cinebook.module.user.entity.Role;
import com.cinebook.module.user.entity.User;
import com.cinebook.module.user.repository.RoleRepository;
import com.cinebook.module.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerifyTokenService verifyTokenService;
    private final MailEventPublisher mailEventPublisher;
    private final JwtProvider jwtProvider;
    private final TokenHasher tokenHasher;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token-ttl-days}")
    private long refreshTokenTtlDays;

    // ---------------------------------------------------------------
    // Register
    // ---------------------------------------------------------------
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Rely on DB UNIQUE constraints as the real race-condition guard;
        // these checks just give a friendlier error on the common path.
        if (userRepository.existsByEmail(request.email())) {
            throw new CinebookException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByPhone(request.phone())) {
            throw new CinebookException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        Role customerRole = roleRepository.findByRoleCode("CUSTOMER")
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role not seeded"));

        User user = User.builder()
                .role(customerRole)
                .userName(request.userName())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .phone(request.phone())
                .verified(false)
                .build();

        user = userRepository.save(user);

        String verifyToken = verifyTokenService.createToken(user.getId());

        // Publish AFTER the user row is committed logically; enqueue only,
        // never send the email synchronously in this request.
        mailEventPublisher.publishUserRegistered(
                new UserRegisteredEvent(user.getId(), user.getEmail(), user.getUserName(), verifyToken)
        );

        return new RegisterResponse(user.getId(), user.getEmail(), user.isVerified());
    }

    // ---------------------------------------------------------------
    // Verify Email
    // ---------------------------------------------------------------
    @Transactional
    public void verifyEmail(String token) {
        UUID userId = verifyTokenService.consumeToken(token) // GETDEL: single-use
                .orElseThrow(() -> new CinebookException(ErrorCode.INVALID_VERIFY_TOKEN));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CinebookException(ErrorCode.USER_NOT_FOUND));

        user.setVerified(true);
        userRepository.save(user);
    }

    // ---------------------------------------------------------------
    // Login
    // ---------------------------------------------------------------
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new CinebookException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CinebookException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.isVerified()) {
            throw new CinebookException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().getRoleCode());
        String rawRefreshToken = issueRefreshToken(user, request.deviceId());

        return new AuthResponse(accessToken, rawRefreshToken, user.getId(), user.getAvatarUrl());
    }

    // ---------------------------------------------------------------
    // Refresh Token Rotation
    // ---------------------------------------------------------------
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String hash = tokenHasher.hash(request.refreshToken());

        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new CinebookException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!existing.getDeviceId().equals(request.deviceId())) {
            // token replayed from a different device than it was issued to
            throw new CinebookException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (existing.getRevokedAt() != null) {
            throw new CinebookException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new CinebookException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = existing.getUser();

        // Invalidate the old token immediately - rotation, not reuse
        existing.setRevokedAt(Instant.now());
        refreshTokenRepository.save(existing);
        refreshTokenRepository.delete(existing);

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().getRoleCode());
        String newRawRefreshToken = issueRefreshToken(user, request.deviceId());

        return new AuthResponse(accessToken, newRawRefreshToken, user.getId(), user.getAvatarUrl());
    }

    // ---------------------------------------------------------------
    // Logout
    // ---------------------------------------------------------------
    @Transactional
    public void logout(UUID userId, LogoutRequest request) {
        refreshTokenRepository.findByUserIdAndDeviceId(userId, request.deviceId())
                .ifPresent(refreshTokenRepository::delete);
        // No access-token blacklist: access token TTL is short (15 min), so
        // it naturally expires soon after logout - see Milestone 2.7 note.
    }

    // ---------------------------------------------------------------
    // Get User Profile
    // ---------------------------------------------------------------
    @Transactional
    public UserResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CinebookException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.builder()
                .id(user.getId())
                .roleId(user.getRole().getId())
                .email(user.getEmail())
                .userName(user.getUserName())
                .phone(user.getPhone())
                .roleCode(user.getRole().getRoleCode())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    // ---------------------------------------------------------------
    // Shared helper
    // ---------------------------------------------------------------
    private String issueRefreshToken(User user, String deviceId) {
        String rawToken = UUID.randomUUID().toString();
        String hash = tokenHasher.hash(rawToken);
        Instant expiresAt = Instant.now().plus(refreshTokenTtlDays, ChronoUnit.DAYS);

        // One active refresh token per (user, device): replace if one exists
        // for this device instead of accumulating rows forever
        RefreshToken token = refreshTokenRepository.findByUserIdAndDeviceId(user.getId(), deviceId)
                .orElseGet(() -> RefreshToken.builder().user(user).deviceId(deviceId).build());

        token.setTokenHash(hash);
        token.setExpiresAt(expiresAt);
        token.setRevokedAt(null);

        refreshTokenRepository.save(token);
        return rawToken;
    }
}
