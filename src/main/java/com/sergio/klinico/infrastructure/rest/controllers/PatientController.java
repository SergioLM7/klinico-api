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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión del catálogo de pacientes.
 *
 * <p>Permite crear, consultar, buscar y actualizar pacientes. La creación y edición
 * están restringidas al rol {@code ADMINISTRATIVO}; la consulta está disponible
 * también para {@code MEDICO} y {@code JEFESERVICIO}.</p>
 *
 * <p>Base URL: {@code /api/v1/patients}</p>
 */
@Tag(name = "Pacientes", description = "CRUD de pacientes del sistema hospitalario")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PatientController {

    private final PatientService patientService;
    private final PatientMapper patientMapper;

    /**
     * Crea un nuevo paciente en el sistema.
     *
     * <p>Requiere rol {@code ADMINISTRATIVO}. Lanza {@code BusinessException} si
     * ya existe un paciente con el mismo DNI.</p>
     *
     * @param request datos del paciente a crear, validados con el grupo {@link CreateGroup}
     * @return resumen del paciente creado con HTTP 201
     */
    @Operation(
            summary = "Crear paciente",
            description = "Registra un nuevo paciente. El DNI debe ser único en el sistema. Rol requerido: ADMINISTRATIVO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paciente creado correctamente",
                    content = @Content(schema = @Schema(implementation = PatientSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o DNI duplicado",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public ResponseEntity<PatientSummaryResponse> create(
            @Validated(CreateGroup.class) @RequestBody PatientRequest request) {
        log.info("REQUEST: POST /patients/create recibida");

        Patient newPatient = patientMapper.toDomainFromDto(request);

        Patient savedPatient = patientService.create(newPatient);

        PatientSummaryResponse response = patientMapper.toSummaryResponseFromDomain(savedPatient);

        if (response.dni() != null)
            log.info("Paciente {} creado con éxito por el usuario {}", response.dni(), savedPatient.getCreatedBy());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Devuelve todos los pacientes del sistema de forma paginada.
     *
     * <p>Requiere rol {@code ADMINISTRATIVO}.</p>
     *
     * @param page número de página (0-indexed, por defecto 0)
     * @param size tamaño de página (por defecto 10)
     * @return página de pacientes ordenada por defecto de persistencia
     */
    @Operation(
            summary = "Listar todos los pacientes (paginado)",
            description = "Devuelve todos los pacientes registrados. Rol requerido: ADMINISTRATIVO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO')")
    public ResponseEntity<PaginatedResponse<PatientResponse>> findAll(
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Número de elementos por página", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        log.info("REQUEST: GET /patients recibida");

        PaginatedResult<Patient> result = patientService.getAllPaginated(page, size);

        List<PatientResponse> responseList = result.content().stream()
                .map(patientMapper::toResponseFromDomain)
                .toList();

        PaginatedResponse<PatientResponse> response = PaginatedResponse.create(responseList, result);

        return ResponseEntity.ok(response);
    }

    /**
     * Busca pacientes por apellido (búsqueda parcial, insensible a mayúsculas).
     *
     * <p>Solo devuelve pacientes con estado {@code ALTA}. Roles permitidos:
     * {@code ADMINISTRATIVO}, {@code MEDICO}, {@code JEFESERVICIO}.</p>
     *
     * @param surname apellido o fragmento del apellido a buscar
     * @param page    número de página (0-indexed, por defecto 0)
     * @param size    tamaño de página (por defecto 10)
     * @return página de pacientes que coinciden con el apellido buscado
     */
    @Operation(
            summary = "Buscar pacientes por apellido",
            description = "Búsqueda parcial e insensible a mayúsculas por apellido. Solo devuelve pacientes con estado ALTA. " +
                    "Roles: ADMINISTRATIVO, MEDICO, JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados de búsqueda obtenidos correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<PaginatedResponse<PatientResponse>> searchBySurname(
            @Parameter(description = "Apellido o fragmento del apellido del paciente", example = "García", required = true)
            @RequestParam String surname,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Número de elementos por página", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        log.info("REQUEST: GET /patients/search recibida con apellido: {}", surname);

        PaginatedResult<Patient> result = patientService.searchBySurname(surname, page, size);

        List<PatientResponse> responseList = result.content().stream()
                .map(patientMapper::toResponseFromDomain)
                .toList();

        PaginatedResponse<PatientResponse> response = PaginatedResponse.create(responseList, result);

        log.info("REQUEST: GET /patients/search exitosa - {} pacientes encontrados", response.getData().size());
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un paciente por su identificador único.
     *
     * <p>Roles permitidos: {@code ADMINISTRATIVO}, {@code MEDICO}, {@code JEFESERVICIO}.</p>
     *
     * @param id UUID del paciente
     * @return datos completos del paciente
     */
    @Operation(
            summary = "Obtener paciente por ID",
            description = "Devuelve los datos completos de un paciente dado su UUID. " +
                    "Roles: ADMINISTRATIVO, MEDICO, JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "400", description = "Paciente no encontrado",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<PatientResponse> findById(
            @Parameter(description = "UUID del paciente", required = true)
            @PathVariable UUID id) {
        log.info("REQUEST: GET /patients/{id} recibida");

        Patient patient = patientService.getById(id);

        PatientResponse response = patientMapper.toResponseFromDomain(patient);

        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza los datos de un paciente existente.
     *
     * <p>Requiere rol {@code ADMINISTRATIVO}. La petición se valida con el grupo
     * {@link UpdateGroup}. Si el estado cambia se aplican las reglas de negocio
     * definidas en el dominio.</p>
     *
     * @param id      UUID del paciente a actualizar
     * @param request nuevos datos del paciente, validados con el grupo {@link UpdateGroup}
     * @return datos completos del paciente tras la actualización
     */
    @Operation(
            summary = "Actualizar paciente",
            description = "Modifica los datos de un paciente existente. Si se cambia el estado del paciente " +
                    "se aplican las validaciones de negocio del dominio. Rol requerido: ADMINISTRATIVO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o paciente no encontrado",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO')")
    public ResponseEntity<PatientResponse> update(
            @Parameter(description = "UUID del paciente a actualizar", required = true)
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
