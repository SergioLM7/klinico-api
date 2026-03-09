package com.sergio.klinico.domain.models;

import com.sergio.klinico.domain.models.enums.UserRole;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class User implements UserDetails {
    private UUID id;
    private String email;
    private String password;
    private String name;
    private String surname;
    private UserRole role;
    private boolean active;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override public boolean isEnabled() { return active; }
}