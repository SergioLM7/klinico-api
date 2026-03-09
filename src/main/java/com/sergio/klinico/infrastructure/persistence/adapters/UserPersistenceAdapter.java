package com.sergio.klinico.infrastructure.persistence.adapters;

import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.repositories.UserRepository;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaUserRepository;
import lombok.RequiredArgsConstructor;
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
                        .build());
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id)
                .map(userEntity -> User.builder()
                        .id(userEntity.getUserId())
                        .active(userEntity.isActive())
                        .build());
    }
}