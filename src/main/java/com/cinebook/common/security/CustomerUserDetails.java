package com.cinebook.common.security;

import com.cinebook.module.user.entity.User;
import lombok.Getter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class CustomerUserDetails implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String password;
    private final String roleCode;
    private final boolean verified;

    // CONSTRUCTOR 1: Use when query Database (Login)
    public CustomerUserDetails(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.roleCode = user.getRole().getRoleCode();
        this.verified = user.isVerified();
    }

    // CONSTRUCTOR 2: Use when construct manually (In Filter)
    public CustomerUserDetails(UUID userId, String email, String password, String roleCode, boolean verified) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.roleCode = roleCode;
        this.verified = verified;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // e.g. ROLE_ADMIN, ROLE_CUSTOMER -> required prefix for hasRole() checks
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleCode));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        // Unverified users can still authenticate (login) so we can return a
        // clear EMAIL_NOT_VERIFIED business error instead of a generic 401.
        // Real gating of unverified accounts happens explicitly in AuthService.
        return true;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}