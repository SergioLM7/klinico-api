package com.sergio.klinico.infrastructure.rest.dto.responses.patient;

import com.sergio.klinico.domain.models.enums.PatientStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PatientResponse {
    private UUID patientId;
    private String dni;
    private String name;
    private String surname;
    private char sex;
    private LocalDate birthdate;
    private String address;
    private String contactNumber;
    private String relativeContactNumber;
    private PatientStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;
}
