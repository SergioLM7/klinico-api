package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.models.HospitalService;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.repositories.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para la búsqueda paginada de servicios hospitalarios activos por nombre.
 *
 * <p>Se utiliza en los formularios de selección de servicio al crear ingresos o
 * al buscar el servicio de destino de una admisión. Solo devuelve servicios
 * marcados como activos ({@code active = true}).</p>
 */
@Service
@RequiredArgsConstructor
public class FindAllActiveServicesUseCase {

    private final ServiceRepository serviceRepository;

    /**
     * Busca servicios hospitalarios activos cuyo nombre contenga el texto indicado
     * (búsqueda parcial, insensible a mayúsculas).
     *
     * @param name nombre o fragmento del nombre del servicio a buscar
     * @param page número de página (0-indexed)
     * @param size número de resultados por página
     * @return resultado paginado de servicios activos que coinciden con el criterio
     */
    public PaginatedResult<HospitalService> execute(String name, int page, int size) {
        return serviceRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, page, size);
    }
}
