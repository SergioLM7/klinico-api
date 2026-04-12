package com.sergio.klinico.infrastructure.rest.controllers;

import com.sergio.klinico.application.services.PatientService;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.infrastructure.mappers.PatientMapper;
import com.sergio.klinico.infrastructure.rest.dto.requests.PatientRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.PaginatedResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.patient.PatientResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.patient.PatientSummaryResponse;
import com.sergio.klinico.infrastructure.rest.dto.validations.CreateGroup;
import com.sergio.klinico.infrastructure.rest.dto.validations.UpdateGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<PatientSummaryResponse> create(
            @Validated(CreateGroup.class) @RequestBody PatientRequest request) {
        log.info("REQUEST: POST /patients/create recibida");

        // 1. Mapeamos DTO -> Dominio
        Patient newPatient = patientMapper.toDomainFromDto(request);

        Patient savedPatient = patientService.create(newPatient);

        PatientSummaryResponse response = patientMapper.toSummaryResponseFromDomain(savedPatient);

        if (response.dni() != null)
            log.info("Paciente {} creado con éxito por el usuario {}", response.dni(), savedPatient.getCreatedBy());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO')")
    public ResponseEntity<PaginatedResponse<PatientResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REQUEST: GET /patients recibida");

        PaginatedResult<Patient> result = patientService.getAllPaginated(page, size);

        // Mapeamos cada Patient de dominio a PatientResponse
        List<PatientResponse> responseList = result.content().stream()
                .map(patientMapper::toResponseFromDomain)
                .toList();

        PaginatedResponse<PatientResponse> response = PaginatedResponse.create(responseList, result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<PaginatedResponse<PatientResponse>> searchBySurname(
            @RequestParam String surname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REQUEST: GET /patients/search recibida con apellido: {}", surname);

        PaginatedResult<Patient> result = patientService.searchBySurname(surname, page, size);

        // Mapeamos cada Patient de dominio a PatientResponse
        List<PatientResponse> responseList = result.content().stream()
                .map(patientMapper::toResponseFromDomain)
                .toList();

        PaginatedResponse<PatientResponse> response = PaginatedResponse.create(responseList, result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<PatientResponse> findById(@PathVariable UUID id) {
        log.info("REQUEST: GET /patients/{id} recibida");

        Patient patient = patientService.getById(id);

        PatientResponse response = patientMapper.toResponseFromDomain(patient);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO')")
    public ResponseEntity<PatientResponse> update(
            @PathVariable UUID id,
            @Validated(UpdateGroup.class) @RequestBody PatientRequest request) {
        log.info("REQUEST: PUT /patients/update/{} recibida", id);

        Patient patient = patientMapper.toDomainFromDto(request);
        patient.setPatientId(id);

        Patient updatedPatient = patientService.update(patient);

        PatientResponse response = patientMapper.toResponseFromDomain(updatedPatient);

        if (response != null)
            log.info("Paciente con ID {} modificado con éxito por el usuario {}", id, response.getLastModifiedBy());

        return ResponseEntity.ok(response);
    }

}