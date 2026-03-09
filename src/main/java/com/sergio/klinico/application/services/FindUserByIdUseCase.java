package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.AuthException;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindUserByIdUseCase {

    private final UserRepository userRepository;

    public User execute (UUID userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new AuthException("Usuario no encontrado o inactivo"));
    }

}
