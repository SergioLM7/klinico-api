package com.sergio.klinico.infrastructure.rest.dto.requests;

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

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^\\d{8}[TRWAGMYFPDXBNJZSQVHLCKE]$|^[XYZ]\\d{7}[TRWAGMYFPDXBNJZSQVHLCKE]$", message = "Formato de DNI no válido")
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50)
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100)
    private String surname;

    @NotNull(message = "El sexo es obligatorio")
    // Validaremos que sea 'M', 'F' u 'O' en la lógica o con un validador custom
    private Character sex;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    private LocalDate birthdate;

    private String address;

    @Pattern(regexp = "^\\d{9,15}$", message = "Número de contacto no válido")
    private String contactNumber;

    @Pattern(regexp = "^\\d{9,15}$", message = "Número de familiar de contacto no válido")
    private String relativeContactNumber;

    @NotBlank(message = "El estado inicial es obligatorio")
    private String status;

}
