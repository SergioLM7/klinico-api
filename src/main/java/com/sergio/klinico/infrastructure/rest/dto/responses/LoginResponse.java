package com.sergio.klinico.infrastructure.rest.dto.responses;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class LoginResponse {
    private String token;
    private UUID userId;
    private String email;
    private String name;
    private String role;
}
