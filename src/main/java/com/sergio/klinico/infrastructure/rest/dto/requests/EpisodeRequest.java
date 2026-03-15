package com.sergio.klinico.infrastructure.rest.dto.requests;

import com.sergio.klinico.infrastructure.rest.dto.validations.CreateGroup;
import com.sergio.klinico.infrastructure.rest.dto.validations.UpdateGroup;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EpisodeRequest {

    @NotBlank(groups = CreateGroup.class, message = "El admissionID es obligatorio")
    private UUID admissionId;

    @NotBlank(groups = CreateGroup.class, message = "El doctorID es obligatorio")
    private UUID doctorId;

    @NotBlank(groups = CreateGroup.class, message = "El progreso clínico es obligatorio")
    private String clinicalProgress;

    @NotBlank(groups = CreateGroup.class, message = "El diagnóstico es obligatorio")
    private String diagnosis;

    @Max(value = 23, groups = {CreateGroup.class, UpdateGroup.class}, message = "El valor de Braden no puede ser mayor que 23")
    @Min(value = 6, groups = {CreateGroup.class, UpdateGroup.class}, message = "El valor de Braden no puede ser menor que 6")
    private Integer bradenScore;

    private Boolean camScore;

    @Max(value = 9, groups = {CreateGroup.class, UpdateGroup.class}, message = "El valor de chads2Score no puede ser mayor que 9")
    @Min(value = 0, groups = {CreateGroup.class, UpdateGroup.class}, message = "El valor de chads2Score no puede ser menor que 0")
    private Integer chads2Score;
}
