package com.sergio.klinico.infrastructure.persistence.adapters;

import com.sergio.klinico.domain.models.HospitalService;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.repositories.ServiceRepository;
import com.sergio.klinico.infrastructure.mappers.ServiceMapper;
import com.sergio.klinico.infrastructure.persistence.ServiceEntity;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador de persistencia para la entidad {@link ServiceEntity}.
 *
 * <p>Implementa el puerto de dominio {@link ServiceRepository} mediante Spring Data JPA,
 * traduciendo entre los objetos de dominio {@link HospitalService} y las entidades de
 * persistencia {@link ServiceEntity} a través de {@link ServiceMapper}.</p>
 */
@Component
@RequiredArgsConstructor
public class ServicePersistenceAdapter implements ServiceRepository {

    private final JpaServiceRepository jpaServiceRepository;
    private final ServiceMapper serviceMapper;

    /**
     * Busca servicios hospitalarios activos cuyo nombre contenga el texto indicado
     * (búsqueda parcial, insensible a mayúsculas), devolviendo el resultado paginado.
     *
     * @param name nombre o fragmento del nombre del servicio a buscar
     * @param page número de página (0-indexed)
     * @param size número de resultados por página
     * @return resultado paginado de servicios activos que coinciden con el criterio
     */
    @Override
    public PaginatedResult<HospitalService> findByNameContainingIgnoreCaseAndActiveTrue(String name, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<ServiceEntity> entitiesPage = jpaServiceRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageRequest);

        List<HospitalService> domainList = entitiesPage.getContent().stream()
                .map(serviceMapper::toDomain)
                .toList();

        return new PaginatedResult<>(
                domainList,
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast()
        );
    }
}
