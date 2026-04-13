package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.models.HospitalService;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.repositories.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindAllActiveServicesUseCase {

    private final ServiceRepository serviceRepository;

    public PaginatedResult<HospitalService> execute(String name, int page, int size) {
        return serviceRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, page, size);
    }
}
