package com.sergio.klinico.domain.repositories;

import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.Patient;

import java.util.UUID;

public interface PatientRepository {
    Patient save(Patient patient);
    boolean existsByDni(String dni);
    Patient findById(UUID id);
    PaginatedResult<Patient> findAll(int page, int size);
}
