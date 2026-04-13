package com.sergio.klinico.infrastructure.rest.dto.responses.service;

import java.util.UUID;

public record ServiceResponse(
        UUID serviceId,
        String name,
        boolean active
) {}
