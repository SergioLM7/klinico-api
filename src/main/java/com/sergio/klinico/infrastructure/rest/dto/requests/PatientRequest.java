package com.sergio.klinico.infrastructure.rest.dto.requests;

import com.sergio.klinico.infrastructure.rest.dto.validations.CreateGroup;
import com.sergio.klinico.infrastructure.rest.dto.validations.UpdateGroup;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientRequest {

    @NotBlank(groups = CreateGroup.class, message = "El DNI es obligatorio")
    @Pattern(groups = CreateGroup.class, regexp = "^\\d{8}[TRWAGMYFPDXBNJZSQVHLCKE]$|^[XYZ]\\d{7}[TRWAGMYFPDXBNJZSQVHLCKE]$", message = "Formato de DNI no válido")
    private String dni;

    @NotBlank(groups = CreateGroup.class, message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, groups = { CreateGroup.class, UpdateGroup.class })
    private String name;

    @NotBlank(groups = CreateGroup.class, message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, groups = { CreateGroup.class, UpdateGroup.class })
    private String surname;

    @NotNull(groups = CreateGroup.class, message = "El sexo es obligatorio")
    @Pattern(groups = { CreateGroup.class, UpdateGroup.class }, regexp = "[MF]", message = "El sexo debe ser 'M' o 'F'")
    private String sex;

    @NotNull(groups = CreateGroup.class, message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    private LocalDate birthdate;

    private String address;

    @Pattern(regexp = "^\\d{9,15}$", message = "Número de contacto no válido", groups = { CreateGroup.class,
            UpdateGroup.class })
    private String contactNumber;

    @Pattern(regexp = "^\\d{9,15}$", message = "Número de familiar de contacto no válido", groups = { CreateGroup.class,
            UpdateGroup.class })
    private String relativeContactNumber;

    @NotBlank(groups = CreateGroup.class, message = "El estado inicial es obligatorio")
    private String status;

}
