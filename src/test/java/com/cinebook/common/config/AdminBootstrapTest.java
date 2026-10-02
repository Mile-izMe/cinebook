package com.cinebook.common.config;

import com.cinebook.module.user.entity.Role;
import com.cinebook.module.user.entity.User;
import com.cinebook.module.user.repository.RoleRepository;
import com.cinebook.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AdminBootstrapTest {
    private final UserRepository users = mock(UserRepository.class);
    private final RoleRepository roles = mock(RoleRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private AdminBootstrap bootstrap() {
        return new AdminBootstrap(users, roles, encoder, "admin@example.invalid", "secure-test-password", "0900000000");
    }
    @Test void createsVerifiedAdminWithEncodedPassword() {
        var role = mock(Role.class);
        when(roles.findByRoleCode("ADMIN")).thenReturn(Optional.of(role));
        when(encoder.encode("secure-test-password")).thenReturn("encoded-password");
        bootstrap().run(null);
        var capture = ArgumentCaptor.forClass(User.class);
        verify(users).save(capture.capture());
        assertSame(role, capture.getValue().getRole());
        assertEquals("encoded-password", capture.getValue().getPassword());
        assertTrue(capture.getValue().isVerified());
    }
    @Test void neverResetsExistingAdmin() {
        var role = mock(Role.class);
        when(role.getRoleCode()).thenReturn("ADMIN");
        when(users.findByEmailAndDeletedAtIsNull("admin@example.invalid"))
                .thenReturn(Optional.of(User.builder().role(role).build()));
        bootstrap().run(null);
        verify(users, never()).save(any());
        verifyNoInteractions(encoder);
    }
    @Test void neverPromotesExistingCustomer() {
        var role = mock(Role.class);
        when(role.getRoleCode()).thenReturn("CUSTOMER");
        when(users.findByEmailAndDeletedAtIsNull("admin@example.invalid"))
                .thenReturn(Optional.of(User.builder().role(role).build()));
        assertThrows(IllegalStateException.class, () -> bootstrap().run(null));
        verify(users, never()).save(any());
    }
    @Test void rejectsWeakPassword() {
        assertThrows(IllegalStateException.class, () -> new AdminBootstrap(users, roles, encoder,
                "admin@example.invalid", "short", "0900000000").run(null));
        verifyNoInteractions(users);
    }
}
