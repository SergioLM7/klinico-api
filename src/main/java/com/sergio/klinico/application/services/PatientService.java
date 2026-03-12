package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.domain.repositories.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public Patient create(Patient patient) {
        if (patientRepository.existsByDni(patient.getDni())) {
            throw new BusinessException("Ya existe un paciente registrado con el DNI: " + patient.getDni());
        }
        return patientRepository.save(patient);
    }

    public Patient getById(UUID id) {
        Patient patient = patientRepository.findById(id);
        if (patient == null) {
            throw new BusinessException("Paciente no encontrado");
        }
        return patient;
    }

    public Page<Patient> getAllPaginated(Pageable pageable) {
        return patientRepository.findAll(pageable);
    }

    // El Update y el ChangeStatus irían aquí también
}