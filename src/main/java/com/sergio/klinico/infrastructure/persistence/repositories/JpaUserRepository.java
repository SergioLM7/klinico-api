package com.sergio.klinico.infrastructure.persistence.repositories;

import com.sergio.klinico.infrastructure.persistence.UserEntity;
import com.sergio.klinico.infrastructure.persistence.repositories.projections.UserWorkloadProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = "SELECT * FROM users WHERE LOWER(surname) LIKE LOWER(CONCAT('%', :surname, '%')) AND service_id = :serviceId AND is_active = true",
            countQuery = "SELECT COUNT(*) FROM users WHERE LOWER(surname) LIKE LOWER(CONCAT('%', :surname, '%')) AND service_id = :serviceId AND is_active = true",
            nativeQuery = true)
    Page<UserEntity> findBySurnameContainingIgnoreCaseAndServiceIdAndActiveTrue(
            @Param("surname") String surname,
            @Param("serviceId") UUID serviceId,
            Pageable pageable);

    @Query(value = "SELECT u.name AS name, u.surname AS surname, COUNT(a.admission_id) AS admissionsAssigned " +
            "FROM users u " +
            "LEFT JOIN admissions a ON a.assigned_doctor_id = u.user_id AND a.discharge_date IS NULL " +
            "WHERE u.service_id = :serviceId " +
            "AND u.role = CAST('MEDICO' AS user_role) " +
            "AND u.is_active = true " +
            "GROUP BY u.user_id, u.name, u.surname " +
            "ORDER BY admissionsAssigned DESC, u.surname ASC",
            countQuery = "SELECT COUNT(*) FROM users u " +
                    "WHERE u.service_id = :serviceId " +
                    "AND u.role = CAST('MEDICO' AS user_role) " +
                    "AND u.is_active = true",
            nativeQuery = true)
    Page<UserWorkloadProjection> calculateUserWorkload(@Param("serviceId") UUID serviceId, Pageable pageable);
}
