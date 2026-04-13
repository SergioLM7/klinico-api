package com.sergio.klinico.domain.repositories;

import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.enums.UserRole;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    Optional<User> findByServiceIdAndRoleAndActiveTrue(UUID serviceId, UserRole role);
}