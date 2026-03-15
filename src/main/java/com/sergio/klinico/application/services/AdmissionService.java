package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Admission;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.domain.models.enums.PatientStatus;
import com.sergio.klinico.domain.repositories.AdmissionRepository;
import com.sergio.klinico.domain.repositories.PatientRepository;
import com.sergio.klinico.infrastructure.rest.dto.responses.admission.AdmissionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdmissionService {

    private final AdmissionRepository admissionRepository;
    private final PatientRepository patientRepository;

    @Transactional
    public Admission create(Admission admission) {
        UUID patientId = admission.getPatientId();

        Patient patient = patientRepository.findById(patientId);

        if (patient == null) {
            log.error("El paciente con ID {} no existe en BD", patientId);
            throw new BusinessException("Paciente asignado a la admisión no encontrado");
        }

        if(patient.getStatus() == PatientStatus.EXITUS) {
            log.error("El paciente con ID {} tiene estado EXITUS", patientId);
            throw new BusinessException("No se puede crear una nueva admisión para un paciente con estado EXITUS");
        } else if (patient.getStatus() == PatientStatus.INGRESADO) {
            log.error("El paciente con ID {} ya está ingresado", patientId);
            throw new BusinessException("No se puede crear una nueva admisión para un paciente que sigue ingresado");
        }

        if (admissionRepository.existsActiveAdmissionByPatientId(patientId)) {
            log.error("Ya existe en BD una admisión activa para el paciente {}", patientId);
            throw new BusinessException("Ya existe una admisión activa para el paciente solicitado. No se puede crear una nueva");
        }

        patient.applyStatusChange(PatientStatus.INGRESADO);
        patientRepository.save(patient);

        return admissionRepository.save(admission);
    }

    @Transactional
    public Admission assignRoom(UUID admissionId, Integer roomNumber) {

        Admission currentAdmission = admissionRepository.findById(admissionId);

        if(currentAdmission == null) {
            log.error("La admisión {}, a la que está intentando asignar un nº de habitación, no existe", admissionId);
            throw new BusinessException("La admisión a la que está intentando asignar un nº de habitación no existe");
        }

        currentAdmission.assignRoom(roomNumber);

        log.info("Habitación {} asignada con éxito a la admisión {}", roomNumber, admissionId);
        return admissionRepository.save(currentAdmission);
    }

    @Transactional
    public Admission dischargeAdmission(UUID admissionId) {

        Admission currentAdmission = admissionRepository.findById(admissionId);

        if(currentAdmission == null) {
            log.error("La admisión {}, a la que está intentando dar de alta, no existe", admissionId);
            throw new BusinessException("La admisión a la que está intentando dar de alta no existe");
        }

        currentAdmission.processDischarge();

        Patient patient = patientRepository.findById(currentAdmission.getPatientId());

        if (patient == null) {
            log.error("El paciente con ID {} no existe en BD", currentAdmission.getPatientId());
            throw new BusinessException("Paciente asignado a la admisión no existe en BD");
        }

        patient.applyStatusChange(PatientStatus.ALTA);
        log.info("El paciente {} ha sido dado de alta con éxito", patient.getPatientId());

        patientRepository.save(patient);

        log.info("La admisión {} ha sido dada de alta con éxito", admissionId);
        return admissionRepository.save(currentAdmission);
    }

    @Transactional
    public Admission update(UUID admissionId, Admission updatedData) {
        Admission currentAdmission = admissionRepository.findById(admissionId);

        if (currentAdmission == null) {
            log.error("La admisión {}, que está intentando modificar, no existe", admissionId);
            throw new BusinessException("La admisión que intenta actualizar no existe en BD");
        }

        currentAdmission.updateClinicalInformation(updatedData);

        return admissionRepository.save(currentAdmission);
    }

    public Admission getById(UUID id) {
        return admissionRepository.findById(id);
    }

    public Double getServiceEfficiencyKPI(UUID serviceId) {
        log.info("Calculando estancia media para el servicio: {}", serviceId);

        Double average = admissionRepository.getAverageHospitalizationLengthByService(serviceId);

        log.info("Estancia media para el servicio calculada: {}", average);
        return average != null ? average : 0.0;
    }

    public PaginatedResult<Admission> getActiveByDoctorId(UUID doctorId, int page) {
        return admissionRepository.findAllActiveByDoctor(doctorId, page, 10);
    }

    public PaginatedResult<Admission> getActiveByServiceId(UUID serviceId, int page) {
        return admissionRepository.findAllActiveByService(serviceId, page, 10);
    }

    public PaginatedResult<Admission> getAllActive(int page) {
        return admissionRepository.findAllActive(page, 10);
    }
}
