package com.sergio.klinico.domain.repositories;

import com.sergio.klinico.domain.models.Admission;
import com.sergio.klinico.domain.models.PaginatedResult;

import java.util.UUID;

public interface AdmissionRepository {
    Admission save(Admission admission);
    boolean existsActiveAdmissionByPatientId(UUID patientId);
    Admission findById(UUID id);
    Double getAverageHospitalizationLengthByService(UUID serviceId);
    PaginatedResult<Admission> findAllActiveByService(UUID serviceId, int page, int size);
    PaginatedResult<Admission> findAllActiveByDoctor(UUID doctorId, int page, int size);
    PaginatedResult<Admission> findAllActive(int page, int size);
}
