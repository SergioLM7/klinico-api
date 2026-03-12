package com.sergio.klinico.infrastructure.rest.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PatientResponse {
    private UUID patientId;
    private String name;
    private String surname;
    private LocalDateTime createdAt;
}