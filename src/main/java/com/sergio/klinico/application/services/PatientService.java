package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.domain.models.enums.PatientStatus;
import com.sergio.klinico.domain.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;

    @Transactional
    public Patient create(Patient patient) {
        if (patientRepository.existsByDni(patient.getDni())) {
            log.error("El paciente con DNI {} ya existe en BD", patient.getDni());
            throw new BusinessException("Ya existe un paciente registrado con el DNI: " + patient.getDni());
        }
        return patientRepository.save(patient);
    }

    public Patient getById(UUID id) {
        Patient patient = patientRepository.findById(id);
        if (patient == null) {
            log.error("El paciente con ID {} no existe en BD", id);
            throw new BusinessException("Paciente no encontrado");
        }
        return patient;
    }

    public PaginatedResult<Patient> getAllPaginated(int page, int size) {
        return patientRepository.findAll(page, size);
    }

    public PaginatedResult<Patient> searchBySurname(String surname, int page, int size) {
        return patientRepository.findBySurnameAndStatusAlta(surname, page, size);
    }

    @Transactional
    public Patient update(Patient updatedData) {

        Patient currentPatient = getById(updatedData.getPatientId());
        PatientStatus previousStatus = currentPatient.getStatus();

        if (updatedData.getStatus() != currentPatient.getStatus()) {
            log.info("Paciente con ID {} va a cambiar su estado de {} a {}", updatedData.getPatientId(), currentPatient.getStatus(), updatedData.getStatus());
            currentPatient.applyStatusChange(updatedData.getStatus());
        }

        currentPatient.updateFields(updatedData);

        Patient updatedPatient = patientRepository.save(currentPatient);

        if(updatedPatient.getStatus() != previousStatus)
            log.info("Paciente con ID {} ha cambiado su estado de {} a {}", updatedData.getPatientId(), previousStatus, updatedPatient.getStatus());

        return updatedPatient;
    }
}