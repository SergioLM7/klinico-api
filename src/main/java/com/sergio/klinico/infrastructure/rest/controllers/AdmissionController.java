package com.sergio.klinico.infrastructure.rest.controllers;

import com.sergio.klinico.application.services.AdmissionService;
import com.sergio.klinico.application.services.FindJefeServicioByServiceIdUseCase;
import com.sergio.klinico.application.services.FindUserByIdUseCase;
import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Admission;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.infrastructure.mappers.AdmissionMapper;
import com.sergio.klinico.infrastructure.rest.dto.requests.AdmissionRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.PaginatedResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.admission.AdmissionResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.admission.AdmissionSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admissions")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdmissionController {

    private final AdmissionService admissionService;
    private final AdmissionMapper admissionMapper;
    private final FindUserByIdUseCase findUserByIdUseCase;
    private final FindJefeServicioByServiceIdUseCase findJefeServicioByServiceIdUseCase;

    @GetMapping("/doctor/{assignedDoctorId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<PaginatedResponse<AdmissionResponse>> getActiveByDoctorId(
            @PathVariable UUID assignedDoctorId,
            @RequestParam int page,
            @AuthenticationPrincipal User user
    ) {
        log.info("REQUEST: GET /admissions/doctor/{} recibida", assignedDoctorId);

        User assignedDoctor = findUserByIdUseCase.execute(assignedDoctorId);

        if (!user.getServiceId().equals(assignedDoctor.getServiceId())) {
            log.warn("El usuario {} intentó acceder a las admisiones de un médico de otro servicio {}", user.getId(), assignedDoctor.getServiceId());
            throw new BusinessException("No tienes permisos para acceder a las admisiones de un médico de otro servicio");
        }

        PaginatedResult<Admission> result = admissionService.getActiveByDoctorId(assignedDoctorId, page);
        Map<UUID, Patient> patients = admissionService.loadPatientMapForAdmissions(result.content());

        List<AdmissionResponse> responseList = result.content().stream()
                .map(a -> admissionMapper.toResponseFromDomain(a, patients.get(a.getPatientId())))
                .toList();

        PaginatedResponse<AdmissionResponse> response = PaginatedResponse.create(responseList, result);

        log.info("REQUEST: GET /admissions/doctor/{} exitosa", assignedDoctorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/service/{serviceId}")
    @PreAuthorize("hasAnyRole('JEFESERVICIO')")
    public ResponseEntity<PaginatedResponse<AdmissionResponse>> getActiveByServiceId(
            @PathVariable UUID serviceId,
            @RequestParam int page,
            @AuthenticationPrincipal User user
    ) {
        log.info("REQUEST: GET /admissions/service/{} recibida", serviceId);

        if (!user.getServiceId().equals(serviceId)) {
            log.warn("El usuario {} intentó acceder a las admisiones de otro servicio {}", user.getId(), serviceId);
            throw new BusinessException("No tienes permisos para acceder a las admisiones de otro servicio");
        }

        PaginatedResult<Admission> result = admissionService.getActiveByServiceId(serviceId, page);
        Map<UUID, Patient> patients = admissionService.loadPatientMapForAdmissions(result.content());

        List<AdmissionResponse> responseList = result.content().stream()
                .map(a -> admissionMapper.toResponseFromDomain(a, patients.get(a.getPatientId())))
                .toList();

        PaginatedResponse<AdmissionResponse> response = PaginatedResponse.create(responseList, result);

        log.info("REQUEST: GET /admissions/service/{} exitosa", serviceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO')")
    public ResponseEntity<PaginatedResponse<AdmissionResponse>> getAllActive(
            @RequestParam int page
    ) {
        log.info("REQUEST: GET /admissions recibida");

        PaginatedResult<Admission> result = admissionService.getAllActive(page);
        Map<UUID, Patient> patients = admissionService.loadPatientMapForAdmissions(result.content());

        List<AdmissionResponse> responseList = result.content().stream()
                .map(a -> admissionMapper.toResponseFromDomain(a, patients.get(a.getPatientId())))
                .toList();

        PaginatedResponse<AdmissionResponse> response = PaginatedResponse.create(responseList, result);

        log.info("REQUEST: GET /admissions para admisiones activas exitosa");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<AdmissionSummaryResponse> create(
            @RequestBody @Validated AdmissionRequest request) {
        log.info("REQUEST: /POST /admissions/create recibida");

        Admission admission = admissionMapper.toDomainFromRequest(request);
        
        User jefeServicio = findJefeServicioByServiceIdUseCase.execute(request.getServiceId());
        admission.setAssignedDoctorId(jefeServicio.getId());
        admission.setServiceId(request.getServiceId());

        Admission saved = admissionService.create(admission);

        log.info("Admission {} creada con éxito por el usuario {}", saved.getAdmissionId(), saved.getCreatedBy());
        return ResponseEntity.status(HttpStatus.CREATED).body(admissionMapper.toSummaryResponseFromDomain(saved));
    }

    @PatchMapping("/assign-room/{admissionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO')")
    public ResponseEntity<AdmissionResponse> assignRoom(
            @PathVariable UUID admissionId,
            @RequestParam Integer roomNumber) {
        log.info("REQUEST: /PATCH /admissions/{}/assign-room recibida", admissionId);

        Admission updated = admissionService.assignRoom(admissionId, roomNumber);

        log.info("Admission con ID {} modificada con éxito por el usuario {}", updated.getAdmissionId(), updated.getLastModifiedBy());
        return ResponseEntity.ok(admissionMapper.toResponseFromDomain(updated));
    }

    @PutMapping("/clinical-update/{admissionId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<AdmissionResponse> updateClinicalInfo(
            @PathVariable UUID admissionId,
            @RequestBody AdmissionRequest request) {
        log.info("REQUEST: /PUT /admissions/{}/clinical-update recibida", admissionId);

        Admission data = admissionMapper.toDomainFromRequest(request);
        data.setAdmissionId(admissionId);

        Admission updated = admissionService.update(admissionId, data);

        log.info("Admission con ID {} modificada con éxito por el usuario {}", updated.getAdmissionId(), updated.getLastModifiedBy());
        return ResponseEntity.ok(admissionMapper.toResponseFromDomain(updated));
    }

    @PostMapping("/discharge/{admissionId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<AdmissionResponse> discharge(@PathVariable UUID admissionId) {
        log.info("REQUEST: /POST /admissions/{}/discharge recibida", admissionId);

        Admission discharged = admissionService.dischargeAdmission(admissionId);

        log.info("Admission con ID {} dada de alta con éxito por el usuario {}", discharged.getAdmissionId(), discharged.getLastModifiedBy());
        return ResponseEntity.ok(admissionMapper.toResponseFromDomain(discharged));
    }

    @GetMapping("/kpi/efficiency/{serviceId}")
    @PreAuthorize("hasAnyRole('JEFESERVICIO')")
    public ResponseEntity<Double> getEfficiency(
            @PathVariable UUID serviceId,
            @AuthenticationPrincipal User user
    ) {
        log.info("REQUEST: /GET /admissions/kpi/efficiency/{} recibida", serviceId);

        if (!user.getServiceId().equals(serviceId)) {
            log.warn("El usuario {} intentó acceder a KPIs del servicio {}", user.getId(), serviceId);
            throw new BusinessException("No tienes permisos para ver las métricas de otro servicio.");
        }

        log.info("REQUEST: /GET /admissions/kpi/efficiency/ exitosa para el servicio {}", serviceId);
        return ResponseEntity.ok(admissionService.getServiceEfficiencyKPI(serviceId));
    }

}