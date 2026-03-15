package com.sergio.klinico.infrastructure.rest.dto.requests;

import com.sergio.klinico.infrastructure.rest.dto.validations.CreateGroup;
import com.sergio.klinico.infrastructure.rest.dto.validations.UpdateGroup;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdmissionRequest {

    @NotBlank(groups = CreateGroup.class, message = "El admissionID es obligatorio")
    private UUID admissionId;

    @NotBlank(groups = CreateGroup.class, message = "El assignedDoctorId es obligatorio")
    private UUID assignedDoctorId;

    @NotBlank(groups = CreateGroup.class, message = "El patientId es obligatorio")
    private UUID patientId;

    @NotBlank(groups = CreateGroup.class, message = "El serviceId es obligatorio")
    private UUID serviceId;

    private LocalDateTime dischargeDate;

    private Integer hospitalizationLength;

    @NotBlank(groups = CreateGroup.class, message = "El diagnóstico principal es obligatorio")
    private String principalDiagnosis;

    @NotBlank(groups = CreateGroup.class, message = "El historial médico es obligatorio")
    private String medicalHistory;

    private String allergies;

    private String chronicTreatment;

    @Max(value = 100, groups = {CreateGroup.class, UpdateGroup.class}, message = "El valor del Barthel no puede ser mayor que 100")
    @Min(value = 0, groups = {CreateGroup.class, UpdateGroup.class}, message = "El valor del Barthel no puede ser menor que 0")
    private Integer basalBarthel;

    private Integer roomNumber;
}
