package com.sergio.klinico.infrastructure.rest.controllers;

import com.sergio.klinico.application.services.FindAllActiveServicesUseCase;
import com.sergio.klinico.domain.models.HospitalService;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.infrastructure.rest.dto.responses.PaginatedResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.service.ServiceResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la consulta del catálogo de servicios hospitalarios activos.
 *
 * <p>Permite buscar servicios por nombre para facilitar la selección de servicio
 * al crear ingresos u otras operaciones que requieran identificar un servicio.
 * Solo se devuelven servicios marcados como activos en el sistema.</p>
 *
 * <p>Base URL: {@code /api/v1/services}</p>
 */
@Tag(name = "Servicios Hospitalarios", description = "Catálogo de servicios hospitalarios activos")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Slf4j
public class ServiceController {

    private final FindAllActiveServicesUseCase findAllActiveServicesUseCase;

    /**
     * Busca servicios hospitalarios activos por nombre (búsqueda parcial, insensible a mayúsculas).
     *
     * <p>Roles permitidos: {@code MEDICO}, {@code JEFESERVICIO}, {@code ADMINISTRATIVO}.</p>
     *
     * @param name nombre o fragmento del nombre del servicio a buscar
     * @param page número de página (0-indexed, por defecto 0)
     * @param size número de resultados por página (por defecto 5)
     * @return página de servicios activos que coinciden con el nombre buscado
     */
    @Operation(
            summary = "Buscar servicios hospitalarios por nombre",
            description = "Búsqueda parcial e insensible a mayúsculas sobre el nombre de servicios activos. " +
                    "Roles: MEDICO, JEFESERVICIO, ADMINISTRATIVO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados de búsqueda obtenidos correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO', 'ADMINISTRATIVO')")
    public ResponseEntity<PaginatedResponse<ServiceResponse>> searchByName(
            @Parameter(description = "Nombre o fragmento del nombre del servicio", example = "Cardiología", required = true)
            @RequestParam String name,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Número de resultados por página", example = "5")
            @RequestParam(defaultValue = "5") int size) {
        log.info("REQUEST: GET /services/search recibida con filtro de búsqueda: {}", name);

        PaginatedResult<HospitalService> services = findAllActiveServicesUseCase.execute(name, page, size);

        List<ServiceResponse> responseList = services.content().stream()
                .map(service -> new ServiceResponse(
                        service.getServiceId(),
                        service.getName(),
                        service.isActive()
                ))
                .toList();

        PaginatedResponse<ServiceResponse> response = PaginatedResponse.create(responseList, services);

        log.info("REQUEST: GET /services/search exitosa - {} servicios encontrados", response.getData().size());
        return ResponseEntity.ok(response);
    }
}
