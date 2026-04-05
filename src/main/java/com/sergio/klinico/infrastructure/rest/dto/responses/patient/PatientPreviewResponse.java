package com.sergio.klinico.infrastructure.rest.dto.responses.patient;

import java.time.LocalDate;
import java.util.UUID;

public record PatientPreviewResponse(
        UUID patientId,
        String name,
        String surname,
        LocalDate birthdate,
        char sex
) {}