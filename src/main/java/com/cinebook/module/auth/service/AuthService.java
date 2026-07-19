package com.cinebook.module.auth.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.auth.dto.request.RegisterRequest;
import com.cinebook.module.auth.dto.response.RegisterResponse;
import com.cinebook.module.auth.event.UserRegisteredEvent;
import com.cinebook.module.auth.messaging.MailEventPublisher;
import com.cinebook.module.user.entity.Role;
import com.cinebook.module.user.entity.User;
import com.cinebook.module.user.repository.RoleRepository;
import com.cinebook.module.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerifyTokenService verifyTokenService;
    private final MailEventPublisher mailEventPublisher;

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
                .orElseThrow(() -> new CinebookException(ErrorCode.INVALID_VERIY_TOKEN));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CinebookException(ErrorCode.USER_NOT_FOUND));

        user.setVerified(true);
        userRepository.save(user);
    }
}
