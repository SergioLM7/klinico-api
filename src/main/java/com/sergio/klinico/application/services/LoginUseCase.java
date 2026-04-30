package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.AuthException;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Caso de uso de autenticación: valida las credenciales de un usuario y lo devuelve
 * si son correctas.
 *
 * <p>El proceso de autenticación sigue los siguientes pasos:</p>
 * <ol>
 *   <li>Busca al usuario por email en el repositorio.</li>
 *   <li>Comprueba que el usuario esté activo.</li>
 *   <li>Verifica que la contraseña en texto plano coincida con el hash almacenado
 *       usando BCrypt.</li>
 * </ol>
 *
 * <p>Ante cualquier fallo se lanza una {@link AuthException} con un mensaje genérico
 * que no revela si el email existe o no, evitando ataques de enumeración de usuarios.</p>
 */
@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Autentica a un usuario comprobando email y contraseña.
     *
     * @param email         email del usuario que intenta autenticarse
     * @param plainPassword contraseña en texto plano introducida por el usuario
     * @return usuario autenticado con todos sus datos de dominio
     * @throws AuthException si el email no existe, el usuario está inactivo
     *                       o la contraseña no coincide
     */
    public User execute(String email, String plainPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("Las credenciales son incorrectas"));

        if (!user.isActive()) {
            throw new AuthException("El usuario no está activo");
        }

        if (!passwordEncoder.matches(plainPassword, user.getPassword())) {
            throw new AuthException("Las credenciales son incorrectas");
        }

        return user;
    }
}
