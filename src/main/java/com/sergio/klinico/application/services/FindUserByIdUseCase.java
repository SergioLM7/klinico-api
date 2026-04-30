package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.AuthException;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Caso de uso para obtener un usuario activo por su identificador único.
 *
 * <p>Se utiliza en los controladores para resolver el usuario destino de una operación
 * (por ejemplo, verificar que el médico cuyas admisiones se consultan pertenece al
 * mismo servicio que el usuario autenticado) antes de ejecutar la lógica de negocio.</p>
 */
@Service
@RequiredArgsConstructor
public class FindUserByIdUseCase {

    private final UserRepository userRepository;

    /**
     * Obtiene un usuario activo por su UUID.
     *
     * @param userId UUID del usuario a buscar
     * @return usuario encontrado y activo
     * @throws AuthException si el usuario no existe o está inactivo
     */
    public User execute(UUID userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new AuthException("Usuario no encontrado o inactivo"));
    }

}
