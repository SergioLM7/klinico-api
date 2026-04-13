package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.enums.UserRole;
import com.sergio.klinico.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindJefeServicioByServiceIdUseCase {

    private final UserRepository userRepository;

    public User execute(UUID serviceId) {
        return userRepository.findByServiceIdAndRoleAndActiveTrue(serviceId, UserRole.JEFESERVICIO)
                .orElseThrow(() -> new BusinessException("No se encontró un Jefe de Servicio activo para el servicio especificado"));
    }

}
