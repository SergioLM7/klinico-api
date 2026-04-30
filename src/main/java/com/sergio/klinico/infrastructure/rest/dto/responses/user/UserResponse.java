package com.sergio.klinico.infrastructure.rest.dto.responses.user;

import com.sergio.klinico.domain.models.enums.UserRole;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String surname,
        String email,
        UserRole role
) {}
