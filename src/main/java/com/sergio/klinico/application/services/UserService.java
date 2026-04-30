package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.UserWorkLoad;
import com.sergio.klinico.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio de aplicación para la consulta de usuarios del sistema.
 *
 * <p>Proporciona operaciones de búsqueda de usuarios activos dentro de un servicio
 * hospitalario y el cálculo de la carga de trabajo de los médicos.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * Busca usuarios activos por apellido dentro de un servicio hospitalario concreto.
     *
     * <p>La búsqueda es parcial e insensible a mayúsculas. Solo devuelve usuarios activos.</p>
     *
     * @param surname   apellido o fragmento del apellido a buscar
     * @param serviceId UUID del servicio hospitalario al que se acota la búsqueda
     * @param page      número de página (0-indexed)
     * @param size      número de elementos por página
     * @return resultado paginado de usuarios activos que coinciden con el criterio
     */
    public PaginatedResult<User> searchBySurnameAndServiceId(String surname, UUID serviceId, int page, int size) {
        return userRepository.searchBySurnameAndServiceId(surname, serviceId, page, size);
    }

    /**
     * Calcula la carga de trabajo de los médicos de un servicio hospitalario.
     *
     * <p>La carga se define como el número de ingresos activos asignados a cada médico.
     * Devuelve únicamente los médicos que tienen al menos un ingreso activo asignado.</p>
     *
     * @param serviceId UUID del servicio hospitalario
     * @param page      número de página (0-indexed)
     * @param size      número de elementos por página
     * @return resultado paginado con la carga de trabajo de cada médico del servicio
     */
    public PaginatedResult<UserWorkLoad> serviceWorkload(UUID serviceId, int page, int size) {
        return userRepository.calculateUserWorkload(serviceId, page, size);
    }
}
