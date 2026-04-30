package com.sergio.klinico.infrastructure.rest.controllers;

import com.sergio.klinico.application.services.LoginUseCase;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.infrastructure.rest.dto.requests.LoginRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.LoginResponse;
import com.sergio.klinico.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone el endpoint de autenticación.
 *
 * <p>Es el único endpoint público de la API: no requiere token JWT.
 * Tras una autenticación correcta devuelve un token firmado (HS256) que
 * debe incluirse en la cabecera {@code Authorization: Bearer <token>}
 * en todas las peticiones posteriores.</p>
 */
@Tag(name = "Autenticación", description = "Operaciones de inicio de sesión y obtención de token JWT")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final JwtService jwtService;

    /**
     * Autentica a un usuario con email y contraseña y devuelve un token JWT.
     *
     * @param request credenciales del usuario (email + contraseña en texto plano)
     * @return {@link LoginResponse} con el token JWT firmado y los datos básicos del usuario
     */
    @Operation(
            summary = "Login de usuario",
            description = "Valida las credenciales y devuelve un token JWT con los datos del usuario. " +
                    "El token tiene una validez configurada en la variable de entorno JWT_EXPIRATION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación correcta",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o usuario inactivo",
                    content = @Content(schema = @Schema()))
    })
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
