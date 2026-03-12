package com.sergio.klinico.domain.models;

import com.sergio.klinico.domain.models.enums.PatientStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    private UUID patientId;
    private String dni;
    private String name;
    private String surname;
    private Character sex;
    private LocalDate birthdate;
    private String address;
    private String contactNumber;
    private String relativeContactNumber;
    private PatientStatus status;
    private LocalDateTime createdAt;
    private UUID createdBy;
}
