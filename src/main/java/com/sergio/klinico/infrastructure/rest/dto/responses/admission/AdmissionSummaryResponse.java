package com.sergio.klinico.infrastructure.rest.dto.responses.admission;

import java.util.UUID;

public record AdmissionSummaryResponse (
        UUID admissionId,
        UUID patientId,
        UUID serviceId,
        UUID assignedDoctorId,
        String principalDiagnosis,
        String allergies,
        String chronicTreatment,
        Integer basalBarthel
) {}
