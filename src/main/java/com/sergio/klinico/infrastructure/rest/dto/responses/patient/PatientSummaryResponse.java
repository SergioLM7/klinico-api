package com.sergio.klinico.infrastructure.rest.dto.responses.patient;

import com.sergio.klinico.domain.models.enums.PatientStatus;

import java.util.UUID;

public record PatientSummaryResponse(
        UUID patientId,
        String dni,
        String name,
        String surname,
        PatientStatus status
) {}