package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.enums.UserRole;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindJefeServicioByServiceIdUseCase Tests")
class FindJefeServicioByServiceIdUseCaseTest {

    @InjectMocks
    private FindJefeServicioByServiceIdUseCase findJefeServicioByServiceIdUseCase;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("Should return jefe de servicio when found and active for given serviceId")
    void execute_WhenJefeServicioExists_ShouldReturnUser() {
        UUID serviceId = UUID.randomUUID();
        User jefeServicio = User.builder()
                .id(UUID.randomUUID())
                .name("Carlos")
                .surname("Ruiz Pérez")
                .role(UserRole.JEFESERVICIO)
                .active(true)
                .serviceId(serviceId)
                .build();

        when(userRepository.findByServiceIdAndRoleAndActiveTrue(serviceId, UserRole.JEFESERVICIO))
                .thenReturn(Optional.of(jefeServicio));

        User result = findJefeServicioByServiceIdUseCase.execute(serviceId);

        assertNotNull(result);
        assertEquals(UserRole.JEFESERVICIO, result.getRole());
        assertEquals(serviceId, result.getServiceId());
        assertTrue(result.isActive());
        verify(userRepository).findByServiceIdAndRoleAndActiveTrue(serviceId, UserRole.JEFESERVICIO);
    }

    @Test
    @DisplayName("Should throw BusinessException when no active jefe de servicio found for serviceId")
    void execute_WhenJefeServicioNotFound_ShouldThrowBusinessException() {
        UUID serviceId = UUID.randomUUID();

        when(userRepository.findByServiceIdAndRoleAndActiveTrue(serviceId, UserRole.JEFESERVICIO))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                findJefeServicioByServiceIdUseCase.execute(serviceId)
        );

        assertEquals("No se encontró un Jefe de Servicio activo para el servicio especificado", exception.getMessage());
        verify(userRepository).findByServiceIdAndRoleAndActiveTrue(serviceId, UserRole.JEFESERVICIO);
    }
}
