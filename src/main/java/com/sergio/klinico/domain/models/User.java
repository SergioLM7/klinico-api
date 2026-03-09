package com.sergio.klinico.domain.models;

import com.sergio.klinico.domain.models.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class User {
    private UUID id;
    private String email;
    private String password;
    private String name;
    private String surname;
    private UserRole role;
    private boolean active;
}