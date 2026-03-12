package com.sergio.klinico.domain.repositories;

import com.sergio.klinico.domain.models.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PatientRepository {
    Patient save(Patient patient);
    boolean existsByDni(String dni);
    Patient findById(UUID id);
    Page<Patient> findAll(Pageable pageable);
}
