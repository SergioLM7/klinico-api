package com.sergio.klinico.infrastructure.rest.dto.responses.user;

public record UserServiceWorkloadResponse(
        String name,
        String surname,
        long admissionsAssigned
) {}