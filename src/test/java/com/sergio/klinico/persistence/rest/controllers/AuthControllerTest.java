package com.sergio.klinico.persistence.rest.controllers;

import com.sergio.klinico.application.services.LoginUseCase;
import com.sergio.klinico.domain.exceptions.AuthException;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.enums.UserRole;
import com.sergio.klinico.infrastructure.rest.controllers.AuthController;
import com.sergio.klinico.infrastructure.rest.dto.requests.LoginRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.LoginResponse;
import com.sergio.klinico.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.sergio.klinico.infrastructure.rest.advice.GlobalExceptionHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

        @InjectMocks
        private AuthController authController;

        @Mock
        private LoginUseCase loginUseCase;

        @Mock
        private JwtService jwtService;

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        private LoginRequest testLoginRequest;
        private LoginResponse testLoginResponse;
        private User testUser;
        private final String testEmail = "test@example.com";
        private final String testPassword = "password123";
        private final String testToken = "jwt.token.here";
        private final UUID userId = UUID.randomUUID();

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders
                                .standaloneSetup(authController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
                objectMapper = new ObjectMapper();

                testUser = User.builder()
                                .id(userId)
                                .email(testEmail)
                                .name("Juan")
                                .surname("García")
                                .role(UserRole.ADMINISTRATIVO)
                                .active(true)
                                .build();

                testLoginRequest = new LoginRequest();
                testLoginRequest.setEmail(testEmail);
                testLoginRequest.setPassword(testPassword);

                testLoginResponse = LoginResponse.builder()
                                .token(testToken)
                                .userId(userId)
                                .email(testEmail)
                                .name("Juan García")
                                .role(UserRole.ADMINISTRATIVO.name())
                                .build();
        }

    @Test
    @DisplayName("Should return 200 and LoginResponse when credentials are valid")
    void login_WhenCredentialsAreValid_ShouldReturn200AndLoginResponse() throws Exception {
        when(loginUseCase.execute(testEmail, testPassword)).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn(testToken);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(testToken))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.name").value("Juan García"))
                .andExpect(jsonPath("$.role").value(UserRole.ADMINISTRATIVO.name()));

        verify(loginUseCase).execute(testEmail, testPassword);
        verify(jwtService).generateToken(testUser);
    }

    @Test
    @DisplayName("Should return 401 when authentication fails")
    void login_WhenAuthenticationFails_ShouldReturn401() throws Exception {
        when(loginUseCase.execute(testEmail, testPassword))
                .thenThrow(new AuthException("Las credenciales son incorrectas"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLoginRequest)))
                .andExpect(status().isUnauthorized());

        verify(loginUseCase).execute(testEmail, testPassword);
        verify(jwtService, never()).generateToken(any(User.class));
    }

        @Test
        @DisplayName("Should return 400 when email is invalid")
        void login_WhenEmailIsInvalid_ShouldReturn400() throws Exception {
                LoginRequest invalidRequest = new LoginRequest();
                invalidRequest.setEmail("invalid-email");
                invalidRequest.setPassword(testPassword);

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());

                verify(loginUseCase, never()).execute(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when email is blank")
        void login_WhenEmailIsBlank_ShouldReturn400() throws Exception {
                LoginRequest invalidRequest = new LoginRequest();
                invalidRequest.setEmail("");
                invalidRequest.setPassword(testPassword);

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());

                verify(loginUseCase, never()).execute(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when password is blank")
        void login_WhenPasswordIsBlank_ShouldReturn400() throws Exception {
                LoginRequest invalidRequest = new LoginRequest();
                invalidRequest.setEmail(testEmail);
                invalidRequest.setPassword("");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());

                verify(loginUseCase, never()).execute(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when request body is missing")
        void login_WhenRequestBodyIsMissing_ShouldReturn400() throws Exception {
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value(
                                                "El cuerpo de la solicitud es requerido y debe estar en formato JSON válido"));

                verify(loginUseCase, never()).execute(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when request body is empty")
        void login_WhenRequestBodyIsEmpty_ShouldReturn400() throws Exception {
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message", allOf(
                                                containsString("Error de validación:"),
                                                containsString("email: El email es obligatorio"),
                                                containsString("password: La contraseña es obligatoria"))));

                verify(loginUseCase, never()).execute(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return correct response structure for different user roles")
        void login_WhenUserHasDifferentRole_ShouldReturnCorrectRole() throws Exception {
                User doctorUser = User.builder()
                                .id(userId)
                                .email(testEmail)
                                .name("María")
                                .surname("López")
                                .role(UserRole.MEDICO)
                                .active(true)
                                .build();

                when(loginUseCase.execute(testEmail, testPassword)).thenReturn(doctorUser);
                when(jwtService.generateToken(doctorUser)).thenReturn(testToken);

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testLoginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.role").value(UserRole.MEDICO.name()));

                verify(loginUseCase).execute(testEmail, testPassword);
                verify(jwtService).generateToken(doctorUser);
        }

        @Test
        @DisplayName("Should handle null user name or surname gracefully")
        void login_WhenUserNameOrSurnameIsNull_ShouldHandleGracefully() throws Exception {
                User userWithNullName = User.builder()
                                .id(userId)
                                .email(testEmail)
                                .name(null)
                                .surname("García")
                                .role(UserRole.ADMINISTRATIVO)
                                .active(true)
                                .build();

                when(loginUseCase.execute(testEmail, testPassword)).thenReturn(userWithNullName);
                when(jwtService.generateToken(userWithNullName)).thenReturn(testToken);

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testLoginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("null García"));

                verify(loginUseCase).execute(testEmail, testPassword);
                verify(jwtService).generateToken(userWithNullName);
        }

    @Test
    @DisplayName("Should verify correct order of operations")
    void login_ShouldVerifyCorrectOrderOfOperations() throws Exception {
        when(loginUseCase.execute(testEmail, testPassword)).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn(testToken);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLoginRequest)))
                .andExpect(status().isOk());

        var inOrder = inOrder(loginUseCase, jwtService);
        inOrder.verify(loginUseCase).execute(testEmail, testPassword);
        inOrder.verify(jwtService).generateToken(testUser);
    }
}
