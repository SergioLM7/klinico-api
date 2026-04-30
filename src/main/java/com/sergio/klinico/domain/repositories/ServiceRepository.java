package com.sergio.klinico.domain.repositories;

import com.sergio.klinico.domain.models.HospitalService;
import com.sergio.klinico.domain.models.PaginatedResult;

public interface ServiceRepository {
    PaginatedResult<HospitalService> findByNameContainingIgnoreCaseAndActiveTrue(String name, int page, int size);
}
