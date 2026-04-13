package com.sergio.klinico.infrastructure.persistence.repositories;

import com.sergio.klinico.domain.models.enums.UserRole;
import com.sergio.klinico.infrastructure.persistence.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailAndActiveTrue(String email);
    Optional<UserEntity> findByServiceIdAndRoleAndActiveTrue(UUID serviceId, UserRole role);
}
