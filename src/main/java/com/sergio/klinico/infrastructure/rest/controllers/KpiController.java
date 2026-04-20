package com.sergio.klinico.infrastructure.rest.controllers;

import com.sergio.klinico.application.services.KpiService;
import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.infrastructure.rest.dto.responses.kpi.DoctorKpiSeriesResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.kpi.MonthlyKpiEntryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kpis")
@PreAuthorize("hasRole('JEFESERVICIO')")
@RequiredArgsConstructor
@Slf4j
public class KpiController {

    private final KpiService kpiService;

    /**
     * Número de ingresos del servicio agrupados por mes.
     * Sin month → devuelve los 12 meses del año (0 para meses sin datos).
     * Con month → devuelve el mes solicitado (0 si sin datos).
     */
    @GetMapping("/admissions-by-service")
    public ResponseEntity<List<MonthlyKpiEntryResponse>> getAdmissionsByService(
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal User user
    ) {
        log.info("REQUEST: GET /kpis/admissions-by-service - serviceId: {}, year: {}, month: {}",
                user.getServiceId(), year, month);

        List<MonthlyKpiEntryResponse> response = kpiService
                .getAdmissionsByService(user.getServiceId(), year, month)
                .stream()
                .map(e -> new MonthlyKpiEntryResponse(e.month(), e.value()))
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Número de ingresos por médico del servicio agrupados por mes.
     * Cada elemento devuelve la serie temporal de un médico.
     */
    @GetMapping("/admissions-by-doctor")
    public ResponseEntity<List<DoctorKpiSeriesResponse>> getAdmissionsByDoctor(
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal User user
    ) {
        log.info("REQUEST: GET /kpis/admissions-by-doctor - serviceId: {}, year: {}, month: {}",
                user.getServiceId(), year, month);

        List<DoctorKpiSeriesResponse> response = kpiService
                .getAdmissionsByDoctor(user.getServiceId(), year, month)
                .stream()
                .map(d -> new DoctorKpiSeriesResponse(
                        d.doctorId(),
                        d.doctorName(),
                        d.doctorSurname(),
                        d.data().stream()
                                .map(e -> new MonthlyKpiEntryResponse(e.month(), e.value()))
                                .toList()))
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Número de éxitus del servicio agrupados por mes.
     * Usa modified_at del paciente como fecha de referencia del éxitus.
     */
    @GetMapping("/exitus")
    public ResponseEntity<List<MonthlyKpiEntryResponse>> getExitus(
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal User user
    ) {
        log.info("REQUEST: GET /kpis/exitus - serviceId: {}, year: {}, month: {}",
                user.getServiceId(), year, month);

        List<MonthlyKpiEntryResponse> response = kpiService
                .getExitus(user.getServiceId(), year, month)
                .stream()
                .map(e -> new MonthlyKpiEntryResponse(e.month(), e.value()))
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Estancia media (días) del servicio agrupada por mes.
     * Solo se consideran admisiones con alta (dischargeDate != null).
     */
    @GetMapping("/avg-stay")
    public ResponseEntity<List<MonthlyKpiEntryResponse>> getAvgStayByService(
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal User user
    ) {
        log.info("REQUEST: GET /kpis/avg-stay - serviceId: {}, year: {}, month: {}",
                user.getServiceId(), year, month);

        List<MonthlyKpiEntryResponse> response = kpiService
                .getAvgStayByService(user.getServiceId(), year, month)
                .stream()
                .map(e -> new MonthlyKpiEntryResponse(e.month(), e.value()))
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Estancia media (días) por médico del servicio agrupada por mes.
     * Solo se consideran admisiones con alta (dischargeDate != null).
     */
    @GetMapping("/avg-stay-by-doctor")
    public ResponseEntity<List<DoctorKpiSeriesResponse>> getAvgStayByDoctor(
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal User user
    ) {
        log.info("REQUEST: GET /kpis/avg-stay-by-doctor - serviceId: {}, year: {}, month: {}",
                user.getServiceId(), year, month);

        List<DoctorKpiSeriesResponse> response = kpiService
                .getAvgStayByDoctor(user.getServiceId(), year, month)
                .stream()
                .map(d -> new DoctorKpiSeriesResponse(
                        d.doctorId(),
                        d.doctorName(),
                        d.doctorSurname(),
                        d.data().stream()
                                .map(e -> new MonthlyKpiEntryResponse(e.month(), e.value()))
                                .toList()))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/service-global-efficiency/{serviceId}")
    public ResponseEntity<Double> getEfficiency(
            @PathVariable UUID serviceId,
            @AuthenticationPrincipal User user
    ) {
        log.info("REQUEST: /GET /kpis/service-global-efficiency/{} recibida", serviceId);

        if (!user.getServiceId().equals(serviceId)) {
            log.warn("El usuario {} intentó acceder a KPIs del servicio {}", user.getId(), serviceId);
            throw new BusinessException("No tienes permisos para ver las métricas de otro servicio.");
        }

        log.info("REQUEST: /GET /kpis/service-global-efficiency exitosa para el servicio {}", serviceId);
        return ResponseEntity.ok(kpiService.getServiceEfficiencyKPI(serviceId));
    }
}
