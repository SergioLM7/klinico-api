package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.enums.UserRole;
import com.sergio.klinico.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Caso de uso para obtener el jefe de servicio activo de un servicio hospitalario.
 *
 * <p>Se utiliza durante la creación de ingresos para asignar automáticamente al
 * jefe de servicio como médico responsable inicial. Garantiza que siempre existe
 * un responsable médico válido en el servicio antes de aceptar el ingreso.</p>
 */
@Service
@RequiredArgsConstructor
public class FindJefeServicioByServiceIdUseCase {

    private final UserRepository userRepository;

    /**
     * Obtiene el jefe de servicio activo del servicio indicado.
     *
     * @param serviceId UUID del servicio hospitalario
     * @return usuario con rol {@link UserRole#JEFESERVICIO} activo en ese servicio
     * @throws BusinessException si no existe ningún jefe de servicio activo en el servicio indicado
     */
    public User execute(UUID serviceId) {
        return userRepository.findByServiceIdAndRoleAndActiveTrue(serviceId, UserRole.JEFESERVICIO)
                .orElseThrow(() -> new BusinessException("No se encontró un Jefe de Servicio activo para el servicio especificado"));
    }

}
