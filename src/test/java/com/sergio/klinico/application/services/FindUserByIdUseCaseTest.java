package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.AuthException;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindUserByIdUseCase Tests")
class FindUserByIdUseCaseTest {

    @InjectMocks
    private FindUserByIdUseCase findUserByIdUseCase;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("Should return user when user exists and is active")
    void execute_WhenUserExists_ShouldReturnUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .active(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = findUserByIdUseCase.execute(userId);

        assertEquals(user, result);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw AuthException when user does not exist")
    void execute_WhenUserDoesNotExist_ShouldThrowAuthException() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        AuthException exception = assertThrows(AuthException.class, () -> findUserByIdUseCase.execute(userId));

        assertNotNull(exception);
        assertEquals("Usuario no encontrado o inactivo", exception.getMessage());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw AuthException when user is inactive")
    void execute_WhenUserIsInactive_ShouldThrowAuthException() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .active(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        AuthException exception = assertThrows(AuthException.class, () -> findUserByIdUseCase.execute(userId));

        assertNotNull(exception);
        assertEquals("Usuario no encontrado o inactivo", exception.getMessage());
        verify(userRepository).findById(userId);
    }
}
