package com.cinebook.common.config;

import com.cinebook.module.user.entity.User;
import com.cinebook.module.user.repository.RoleRepository;
import com.cinebook.module.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Optional first-run administrator. Never changes an existing user's role or password. */
@Component
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true")
public class AdminBootstrap implements ApplicationRunner {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;
    private final String phone;

    public AdminBootstrap(UserRepository users, RoleRepository roles, PasswordEncoder encoder,
                          @Value("${app.bootstrap.email}") String email,
                          @Value("${app.bootstrap.password}") String password,
                          @Value("${app.bootstrap.phone}") String phone) {
        this.users = users; this.roles = roles; this.encoder = encoder;
        this.email = email; this.password = password; this.phone = phone;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() || phone.isBlank() || password.length() < 12) {
            throw new IllegalStateException("Admin bootstrap requires email, phone and a password of at least 12 characters");
        }
        var existing = users.findByEmailAndDeletedAtIsNull(email);
        if (existing.isPresent()) {
            if (!"ADMIN".equals(existing.get().getRole().getRoleCode())) {
                throw new IllegalStateException("Bootstrap email belongs to a non-admin user");
            }
            return;
        }
        var role = roles.findByRoleCode("ADMIN").orElseThrow(() -> new IllegalStateException("Missing ADMIN role"));
        users.save(User.builder().role(role).userName("CineBook Admin").email(email).phone(phone)
                .password(encoder.encode(password)).verified(true).build());
    }
}
