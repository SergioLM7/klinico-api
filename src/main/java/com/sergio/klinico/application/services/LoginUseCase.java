package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.AuthException;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User execute(String email, String plainPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("Las credenciales son incorrectas"));

        if (!user.isActive()) {
            throw new AuthException("El usuario no está activo");
        }

        if (!passwordEncoder.matches(plainPassword, user.getPassword())) {
            throw new AuthException("Las credenciales son incorrectas");
        }

        return user;
    }
}