package com.sergio.klinico.infrastructure.rest.controllers;

import com.sergio.klinico.application.services.KpiService;
import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.infrastructure.rest.dto.responses.kpi.DoctorKpiSeriesResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.kpi.MonthlyKpiEntryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la consulta de indicadores clave de rendimiento (KPIs) del servicio.
 *
 * <p>Todos los endpoints están restringidos al rol {@code JEFESERVICIO} y operan sobre
 * el servicio del usuario autenticado. Los KPIs disponibles son:</p>
 * <ul>
 *   <li>Número de ingresos por servicio y por médico (mensual/anual)</li>
 *   <li>Número de éxitus (mensual/anual)</li>
 *   <li>Estancia media en días por servicio y por médico (mensual/anual)</li>
 *   <li>Eficiencia global del servicio (estancia media histórica)</li>
 * </ul>
 *
 * <p>Cuando se solicita un año completo sin especificar mes, se devuelven los 12 meses
 * con valor {@code 0.0} para aquellos sin datos. Si se especifica un mes concreto
 * y no hay datos, se devuelve ese mes con valor {@code 0.0}.</p>
 *
 * <p>Base URL: {@code /api/v1/kpis}</p>
 */
@Tag(name = "KPIs", description = "Indicadores clave de rendimiento del servicio hospitalario")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/kpis")
@PreAuthorize("hasRole('JEFESERVICIO')")
@RequiredArgsConstructor
@Slf4j
public class KpiController {

    private final KpiService kpiService;

    /**
     * Número de ingresos del servicio agrupados por mes.
     *
     * <p>Sin {@code month} devuelve los 12 meses del año (0 para meses sin datos).
     * Con {@code month} devuelve el mes solicitado (0 si no hay datos).</p>
     *
     * @param year  año de referencia para el cálculo
     * @param month mes de referencia (1-12, opcional)
     * @param user  usuario autenticado inyectado por Spring Security
     * @return lista de entradas mensuales con el número de ingresos
     */
    @Operation(
            summary = "Ingresos por servicio (mensual/anual)",
            description = "Devuelve el número de ingresos del servicio del usuario autenticado agrupados por mes. " +
                    "Sin 'month' devuelve los 12 meses del año; con 'month' devuelve solo ese mes. " +
                    "Los meses sin datos devuelven valor 0. Rol: JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "KPI calculado correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/admissions-by-service")
    public ResponseEntity<List<MonthlyKpiEntryResponse>> getAdmissionsByService(
            @Parameter(description = "Año de referencia", example = "2026", required = true)
            @RequestParam int year,
            @Parameter(description = "Mes de referencia (1-12, opcional). Si se omite se devuelven los 12 meses.")
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
     *
     * <p>Cada elemento de la lista contiene la serie temporal de ingresos de un médico.
     * Sin {@code month} devuelve los 12 meses del año; con {@code month} devuelve solo ese mes.</p>
     *
     * @param year  año de referencia
     * @param month mes de referencia (1-12, opcional)
     * @param user  usuario autenticado inyectado por Spring Security
     * @return lista de series por médico, cada una con sus datos mensuales
     */
    @Operation(
            summary = "Ingresos por médico (mensual/anual)",
            description = "Devuelve el número de ingresos agrupados por médico y mes para el servicio del " +
                    "usuario autenticado. Cada elemento incluye la serie temporal de un médico. Rol: JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "KPI calculado correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/admissions-by-doctor")
    public ResponseEntity<List<DoctorKpiSeriesResponse>> getAdmissionsByDoctor(
            @Parameter(description = "Año de referencia", example = "2026", required = true)
            @RequestParam int year,
            @Parameter(description = "Mes de referencia (1-12, opcional). Si se omite se devuelven los 12 meses.")
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
     *
     * <p>Usa la fecha de modificación del paciente como referencia del éxitus.
     * Sin {@code month} devuelve los 12 meses del año; con {@code month} devuelve solo ese mes.</p>
     *
     * @param year  año de referencia
     * @param month mes de referencia (1-12, opcional)
     * @param user  usuario autenticado inyectado por Spring Security
     * @return lista de entradas mensuales con el número de éxitus
     */
    @Operation(
            summary = "Éxitus por servicio (mensual/anual)",
            description = "Devuelve el número de éxitus del servicio agrupados por mes. " +
                    "Usa la fecha de modificación del paciente como fecha de referencia. Rol: JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "KPI calculado correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/exitus")
    public ResponseEntity<List<MonthlyKpiEntryResponse>> getExitus(
            @Parameter(description = "Año de referencia", example = "2026", required = true)
            @RequestParam int year,
            @Parameter(description = "Mes de referencia (1-12, opcional). Si se omite se devuelven los 12 meses.")
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
     * Estancia media (en días) del servicio agrupada por mes.
     *
     * <p>Solo se consideran ingresos con alta registrada (campo {@code dischargeDate} no nulo).
     * Sin {@code month} devuelve los 12 meses del año; con {@code month} devuelve solo ese mes.</p>
     *
     * @param year  año de referencia
     * @param month mes de referencia (1-12, opcional)
     * @param user  usuario autenticado inyectado por Spring Security
     * @return lista de entradas mensuales con la estancia media en días
     */
    @Operation(
            summary = "Estancia media por servicio (mensual/anual)",
            description = "Devuelve la estancia media en días del servicio agrupada por mes. " +
                    "Solo incluye ingresos con alta registrada. Rol: JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "KPI calculado correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/avg-stay")
    public ResponseEntity<List<MonthlyKpiEntryResponse>> getAvgStayByService(
            @Parameter(description = "Año de referencia", example = "2026", required = true)
            @RequestParam int year,
            @Parameter(description = "Mes de referencia (1-12, opcional). Si se omite se devuelven los 12 meses.")
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
     * Estancia media (en días) por médico del servicio agrupada por mes.
     *
     * <p>Solo se consideran ingresos con alta registrada (campo {@code dischargeDate} no nulo).
     * Sin {@code month} devuelve los 12 meses del año; con {@code month} devuelve solo ese mes.</p>
     *
     * @param year  año de referencia
     * @param month mes de referencia (1-12, opcional)
     * @param user  usuario autenticado inyectado por Spring Security
     * @return lista de series por médico, cada una con la estancia media mensual
     */
    @Operation(
            summary = "Estancia media por médico (mensual/anual)",
            description = "Devuelve la estancia media en días agrupada por médico y mes para el servicio " +
                    "del usuario autenticado. Solo incluye ingresos con alta registrada. Rol: JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "KPI calculado correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/avg-stay-by-doctor")
    public ResponseEntity<List<DoctorKpiSeriesResponse>> getAvgStayByDoctor(
            @Parameter(description = "Año de referencia", example = "2026", required = true)
            @RequestParam int year,
            @Parameter(description = "Mes de referencia (1-12, opcional). Si se omite se devuelven los 12 meses.")
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

    /**
     * Calcula la eficiencia global del servicio como la estancia media histórica de todos sus ingresos.
     *
     * <p>El usuario autenticado solo puede consultar la eficiencia de su propio servicio.
     * Rol requerido: {@code JEFESERVICIO}.</p>
     *
     * @param serviceId UUID del servicio hospitalario
     * @param user      usuario autenticado inyectado por Spring Security
     * @return estancia media global del servicio en días ({@code 0.0} si no hay datos)
     */
    @Operation(
            summary = "Eficiencia global del servicio",
            description = "Calcula la estancia media histórica de todos los ingresos del servicio " +
                    "como indicador de eficiencia. El usuario solo puede consultar su propio servicio. Rol: JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "KPI calculado correctamente"),
            @ApiResponse(responseCode = "400", description = "El serviceId no pertenece al usuario autenticado",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/service-global-efficiency/{serviceId}")
    public ResponseEntity<Double> getEfficiency(
            @Parameter(description = "UUID del servicio hospitalario", required = true)
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
