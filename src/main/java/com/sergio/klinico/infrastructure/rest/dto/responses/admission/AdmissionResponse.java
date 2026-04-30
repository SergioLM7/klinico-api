package com.sergio.klinico.infrastructure.rest.dto.responses.admission;

import com.sergio.klinico.infrastructure.rest.dto.responses.patient.PatientPreviewResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AdmissionResponse {
    private UUID admissionId;
    private PatientPreviewResponse patient;
    private UUID serviceId;
    private UUID assignedDoctorId;

    private LocalDateTime dischargeDate;
    private Integer hospitalizationLength;

    private String principalDiagnosis;
    private String medicalHistory;
    private String allergies;
    private String chronicTreatment;
    private Integer basalBarthel;
    private Integer roomNumber;

    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime lastModifiedAt;
    private UUID lastModifiedBy;
}
