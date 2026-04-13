package com.sergio.klinico.domain.repositories;

import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.Patient;

import java.util.List;
import java.util.UUID;

public interface PatientRepository {
    Patient save(Patient patient);
    boolean existsByDni(String dni);
    Patient findById(UUID id);
    List<Patient> findAllByIds(List<UUID> ids);
    PaginatedResult<Patient> findAll(int page, int size);
    PaginatedResult<Patient> findBySurnameAndStatusAlta(String surname, int page, int size);
}
