package com.sergio.klinico.infrastructure.persistence.adapters;

import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.UserWorkLoad;
import com.sergio.klinico.domain.models.enums.UserRole;
import com.sergio.klinico.domain.repositories.UserRepository;
import com.sergio.klinico.infrastructure.persistence.UserEntity;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaUserRepository;
import com.sergio.klinico.infrastructure.persistence.repositories.projections.UserWorkloadProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmailAndActiveTrue(email)
                .map(entity -> User.builder()
                        .id(entity.getUserId())
                        .email(entity.getEmail())
                        .password(entity.getPassword())
                        .active(entity.isActive())
                        .role(entity.getRole())
                        .name(entity.getName())
                        .surname(entity.getSurname())
                        .serviceId(entity.getServiceId())
                        .build());
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id)
                .map(userEntity -> User.builder()
                        .id(userEntity.getUserId())
                        .email(userEntity.getEmail())
                        .active(userEntity.isActive())
                        .role(userEntity.getRole())
                        .name(userEntity.getName())
                        .surname(userEntity.getSurname())
                        .serviceId(userEntity.getServiceId())
                        .build());
    }

    @Override
    public Optional<User> findByServiceIdAndRoleAndActiveTrue(UUID serviceId, UserRole role) {
        return jpaUserRepository.findByServiceIdAndRoleAndActiveTrue(serviceId, role.name())
                .map(entity -> User.builder()
                        .id(entity.getUserId())
                        .email(entity.getEmail())
                        .password(entity.getPassword())
                        .active(entity.isActive())
                        .role(entity.getRole())
                        .name(entity.getName())
                        .surname(entity.getSurname())
                        .serviceId(entity.getServiceId())
                        .build());
    }

    @Override
    public PaginatedResult<User> searchBySurnameAndServiceId(String surname, UUID serviceId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("surname").ascending());
        Page<UserEntity> entitiesPage = jpaUserRepository.findBySurnameContainingIgnoreCaseAndServiceIdAndActiveTrue(
                surname, serviceId, pageRequest);

        return new PaginatedResult<>(
                entitiesPage.getContent().stream()
                        .map(entity -> User.builder()
                                .id(entity.getUserId())
                                .email(entity.getEmail())
                                .active(entity.isActive())
                                .role(entity.getRole())
                                .name(entity.getName())
                                .surname(entity.getSurname())
                                .serviceId(entity.getServiceId())
                                .build())
                        .toList(),
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast());
    }

    @Override
    public PaginatedResult<UserWorkLoad> calculateUserWorkload(UUID serviceId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<UserWorkloadProjection> entitiesPage = jpaUserRepository.calculateUserWorkload(serviceId, pageRequest);

        return new PaginatedResult<>(entitiesPage.getContent().stream()
                .map(entity -> UserWorkLoad.builder()
                        .name(entity.getName())
                        .surname(entity.getSurname())
                        .admissionsAssigned(entity.getAdmissionsAssigned())
                        .build())
                .toList(),
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast());
    }
}