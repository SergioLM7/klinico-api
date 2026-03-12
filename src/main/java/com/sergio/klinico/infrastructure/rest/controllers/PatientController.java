package com.sergio.klinico.infrastructure.rest.controllers;

import com.sergio.klinico.application.services.PatientService;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.infrastructure.mappers.PatientMapper;
import com.sergio.klinico.infrastructure.rest.dto.requests.PatientRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.PatientResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PatientController {

    private final PatientService patientService;
    private final PatientMapper patientMapper;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody PatientRequest request) {
        log.info("Recibida petición de creación de paciente: {}", request.getDni());

        // 1. Mapeamos DTO -> Dominio
        Patient newPatient = patientMapper.toDomainFromDto(request);

        Patient savedPatient = patientService.create(newPatient);

        PatientResponse response = patientMapper.toResponseFromDomain(savedPatient);

        if(response.getPatientId() != null) {
            log.info("Paciente {} creado con éxito.", response.getPatientId());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}