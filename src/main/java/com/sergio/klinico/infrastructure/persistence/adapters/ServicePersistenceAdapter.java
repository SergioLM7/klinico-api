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

@Component
@RequiredArgsConstructor
public class ServicePersistenceAdapter implements ServiceRepository {

    private final JpaServiceRepository jpaServiceRepository;
    private final ServiceMapper serviceMapper;

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
