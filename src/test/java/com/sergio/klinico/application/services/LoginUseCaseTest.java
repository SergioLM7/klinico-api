package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.AuthException;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.enums.UserRole;
import com.sergio.klinico.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCase Tests")
class LoginUseCaseTest {

    @InjectMocks
    private LoginUseCase loginUseCase;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User activeUser;
    private User inactiveUser;
    private final String email = "test@example.com";
    private final String plainPassword = "password123";
    private final String encodedPassword = "encodedPassword123";

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password(encodedPassword)
                .name("Test")
                .surname("User")
                .role(UserRole.ADMINISTRATIVO)
                .active(true)
                .build();

        inactiveUser = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password(encodedPassword)
                .name("Inactive")
                .surname("User")
                .role(UserRole.SYSADMIN)
                .active(false)
                .build();
    }

    @Test
    @DisplayName("Should return user when credentials are correct and user is active")
    void execute_WhenCredentialsCorrectAndUserActive_ShouldReturnUser() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(plainPassword, encodedPassword)).thenReturn(true);

        User result = loginUseCase.execute(email, plainPassword);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertTrue(result.isActive());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(plainPassword, encodedPassword);
    }

    @Test
    @DisplayName("Should throw AuthException when user does not exist")
    void execute_WhenUserDoesNotExist_ShouldThrowAuthException() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        AuthException exception = assertThrows(AuthException.class, () -> loginUseCase.execute(email, plainPassword));

        assertEquals("Las credenciales son incorrectas", exception.getMessage());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw AuthException when user exists but is inactive")
    void execute_WhenUserExistsButInactive_ShouldThrowAuthException() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(inactiveUser));

        AuthException exception = assertThrows(AuthException.class, () ->
            loginUseCase.execute(email, plainPassword)
        );

        assertEquals("El usuario no está activo", exception.getMessage());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw AuthException when password does not match")
    void execute_WhenPasswordDoesNotMatch_ShouldThrowAuthException() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(plainPassword, encodedPassword)).thenReturn(false);

        AuthException exception = assertThrows(AuthException.class, () ->
            loginUseCase.execute(email, plainPassword)
        );

        assertEquals("Las credenciales son incorrectas", exception.getMessage());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(plainPassword, encodedPassword);
    }

    @Test
    @DisplayName("Should handle null email gracefully")
    void execute_WhenEmailIsNull_ShouldThrowAuthException() {
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        assertThrows(AuthException.class, () -> {
            loginUseCase.execute(null, plainPassword);
        });

        verify(userRepository).findByEmail(null);
    }

    @Test
    @DisplayName("Should handle null password gracefully")
    void execute_WhenPasswordIsNull_ShouldThrowAuthException() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(null, encodedPassword)).thenReturn(false);

        assertThrows(AuthException.class, () -> {
            loginUseCase.execute(email, null);
        });

        verify(passwordEncoder).matches(null, encodedPassword);
    }

    @Test
    @DisplayName("Should verify correct order of operations")
    void execute_ShouldVerifyCorrectOrderOfOperations() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(plainPassword, encodedPassword)).thenReturn(true);

        loginUseCase.execute(email, plainPassword);

        var inOrder = inOrder(userRepository, passwordEncoder);
        inOrder.verify(userRepository).findByEmail(email);
        inOrder.verify(passwordEncoder).matches(plainPassword, encodedPassword);
    }
}
