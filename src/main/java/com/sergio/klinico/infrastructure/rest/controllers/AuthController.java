package com.sergio.klinico.infrastructure.rest.controllers;

import com.sergio.klinico.application.services.LoginUseCase;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.infrastructure.rest.dto.requests.LoginRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.LoginResponse;
import com.sergio.klinico.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("REQUEST: POST /login para usuario {} recibida", request.getEmail());

        User user = loginUseCase.execute(request.getEmail(), request.getPassword());

        String token = jwtService.generateToken(user);

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName() + " " + user.getSurname())
                .role(user.getRole().name())
                .serviceId(user.getServiceId())
                .build();

        log.info("Usuario {} ha iniciado sesión correctamente", user.getId());
        return ResponseEntity.ok(response);
    }
}