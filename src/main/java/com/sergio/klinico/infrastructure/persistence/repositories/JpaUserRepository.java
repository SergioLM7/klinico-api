package com.sergio.klinico.infrastructure.persistence.repositories;

import com.sergio.klinico.infrastructure.persistence.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailAndActiveTrue(String email);

    @Query(value = "SELECT * FROM users WHERE service_id = :serviceId AND role = CAST(:role AS user_role) AND is_active = true", nativeQuery = true)
    Optional<UserEntity> findByServiceIdAndRoleAndActiveTrue(@Param("serviceId") UUID serviceId, @Param("role") String role);
}
