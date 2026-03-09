package com.sergio.klinico.infrastructure.rest.controllers;

import com.sergio.klinico.application.services.LoginUseCase;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.infrastructure.rest.dto.requests.LoginRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.LoginResponse;
import com.sergio.klinico.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1. Ejecutamos la lógica de negocio
        User user = loginUseCase.execute(request.getEmail(), request.getPassword());

        // 2. Si es válido, generamos el token
        String token = jwtService.generateToken(user);

        // 3. Construimos la respuesta para Flutter
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName() + " " + user.getSurname())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(response);
    }
}