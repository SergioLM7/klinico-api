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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST para la gestión del ciclo de vida de los ingresos hospitalarios.
 *
 * <p>Cubre las operaciones principales sobre un ingreso ({@link Admission}):
 * creación, consulta paginada (por médico, por servicio o global), búsqueda por
 * apellido de paciente, asignación de habitación, reasignación de médico,
 * actualización de información clínica y alta médica.</p>
 *
 * <p>Los controles de acceso se aplican tanto a nivel de rol mediante
 * {@code @PreAuthorize} como a nivel de negocio (verificación de que el usuario
 * solo accede a datos de su propio servicio).</p>
 *
 * <p>Base URL: {@code /api/v1/admissions}</p>
 */
@Tag(name = "Ingresos", description = "Gestión del ciclo de vida de los ingresos hospitalarios")
@SecurityRequirement(name = "bearerAuth")
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

    /**
     * Devuelve los ingresos activos asignados a un médico concreto, de forma paginada.
     *
     * <p>Solo se devuelven ingresos del mismo servicio que el usuario autenticado.
     * Roles permitidos: {@code MEDICO}, {@code JEFESERVICIO}.</p>
     *
     * @param assignedDoctorId UUID del médico cuyas admisiones activas se consultan
     * @param page             número de página (0-indexed)
     * @param user             usuario autenticado inyectado por Spring Security
     * @return página de ingresos activos del médico indicado
     */
    @Operation(
            summary = "Ingresos activos por médico",
            description = "Devuelve los ingresos activos asignados al médico especificado. " +
                    "El usuario autenticado debe pertenecer al mismo servicio que el médico consultado. " +
                    "Roles: MEDICO, JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de ingresos obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "El médico no pertenece al mismo servicio",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/doctor/{assignedDoctorId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<PaginatedResponse<AdmissionResponse>> getActiveByDoctorId(
            @Parameter(description = "UUID del médico asignado", required = true)
            @PathVariable UUID assignedDoctorId,
            @Parameter(description = "Número de página (0-indexed)", example = "0", required = true)
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

    /**
     * Devuelve todos los ingresos activos de un servicio hospitalario, de forma paginada.
     *
     * <p>El usuario autenticado debe pertenecer al mismo servicio consultado.
     * Rol requerido: {@code JEFESERVICIO}.</p>
     *
     * @param serviceId UUID del servicio hospitalario
     * @param page      número de página (0-indexed)
     * @param user      usuario autenticado inyectado por Spring Security
     * @return página de ingresos activos del servicio indicado
     */
    @Operation(
            summary = "Ingresos activos por servicio",
            description = "Devuelve todos los ingresos activos del servicio especificado. " +
                    "El usuario autenticado debe pertenecer al mismo servicio. Rol: JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de ingresos obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "El servicio no coincide con el del usuario autenticado",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/service/{serviceId}")
    @PreAuthorize("hasAnyRole('JEFESERVICIO')")
    public ResponseEntity<PaginatedResponse<AdmissionResponse>> getActiveByServiceId(
            @Parameter(description = "UUID del servicio hospitalario", required = true)
            @PathVariable UUID serviceId,
            @Parameter(description = "Número de página (0-indexed)", example = "0", required = true)
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

    /**
     * Devuelve todos los ingresos activos del sistema, de forma paginada.
     *
     * <p>Vista global para administración. Rol requerido: {@code ADMINISTRATIVO}.</p>
     *
     * @param page número de página (0-indexed)
     * @return página con todos los ingresos activos del hospital
     */
    @Operation(
            summary = "Listar todos los ingresos activos",
            description = "Vista global de todos los ingresos activos del hospital. Rol requerido: ADMINISTRATIVO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO')")
    public ResponseEntity<PaginatedResponse<AdmissionResponse>> getAllActive(
            @Parameter(description = "Número de página (0-indexed)", example = "0", required = true)
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

    /**
     * Busca ingresos activos por apellido del paciente dentro del servicio del usuario autenticado.
     *
     * <p>Búsqueda parcial e insensible a mayúsculas. La búsqueda queda acotada al servicio del
     * usuario autenticado. Roles permitidos: {@code MEDICO}, {@code JEFESERVICIO}.</p>
     *
     * @param surname apellido o fragmento del apellido del paciente
     * @param page    número de página (0-indexed)
     * @param user    usuario autenticado inyectado por Spring Security
     * @return página de ingresos activos que coinciden con el apellido indicado
     */
    @Operation(
            summary = "Buscar ingresos por apellido de paciente",
            description = "Búsqueda parcial e insensible a mayúsculas. Solo devuelve ingresos activos " +
                    "del servicio del usuario autenticado. Roles: MEDICO, JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados de búsqueda obtenidos correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<PaginatedResponse<AdmissionResponse>> searchByPatientSurname(
            @Parameter(description = "Apellido o fragmento del apellido del paciente", example = "Martínez", required = true)
            @RequestParam String surname,
            @Parameter(description = "Número de página (0-indexed)", example = "0", required = true)
            @RequestParam int page,
            @AuthenticationPrincipal User user
    ) {
        log.info("REQUEST: GET /admissions/search recibida con apellido: {}", surname);

        PaginatedResult<Admission> result = admissionService.searchByPatientSurnameAndServiceId(surname, user.getServiceId(), page);
        Map<UUID, Patient> patients = admissionService.loadPatientMapForAdmissions(result.content());

        List<AdmissionResponse> responseList = result.content().stream()
                .map(a -> admissionMapper.toResponseFromDomain(a, patients.get(a.getPatientId())))
                .toList();

        PaginatedResponse<AdmissionResponse> response = PaginatedResponse.create(responseList, result);

        log.info("REQUEST: GET /admissions/search exitosa - {} admisiones encontradas", responseList.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Crea un nuevo ingreso hospitalario para un paciente existente.
     *
     * <p>El médico responsable se asigna automáticamente al jefe de servicio activo del
     * servicio indicado en la petición. Lanza {@code BusinessException} si el paciente
     * ya tiene un ingreso activo, tiene estado {@code EXITUS} o no existe.
     * Roles permitidos: {@code MEDICO}, {@code JEFESERVICIO}.</p>
     *
     * @param request datos del ingreso a crear
     * @return resumen del ingreso recién creado con HTTP 201
     */
    @Operation(
            summary = "Crear ingreso hospitalario",
            description = "Registra un nuevo ingreso para un paciente. El médico responsable se asigna " +
                    "automáticamente al jefe de servicio activo. Roles: MEDICO, JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ingreso creado correctamente",
                    content = @Content(schema = @Schema(implementation = AdmissionSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, paciente no encontrado, " +
                    "ingreso activo ya existente o paciente con estado EXITUS",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
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

    /**
     * Asigna un número de habitación a un ingreso existente.
     *
     * <p>Rol requerido: {@code ADMINISTRATIVO}.</p>
     *
     * @param admissionId UUID del ingreso al que se asigna la habitación
     * @param roomNumber  número de habitación a asignar
     * @return ingreso actualizado con el número de habitación
     */
    @Operation(
            summary = "Asignar habitación a un ingreso",
            description = "Actualiza el número de habitación de un ingreso existente. Rol requerido: ADMINISTRATIVO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Habitación asignada correctamente"),
            @ApiResponse(responseCode = "400", description = "Ingreso no encontrado",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @PatchMapping("/assign-room/{admissionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO')")
    public ResponseEntity<AdmissionResponse> assignRoom(
            @Parameter(description = "UUID del ingreso", required = true)
            @PathVariable UUID admissionId,
            @Parameter(description = "Número de habitación a asignar", example = "205", required = true)
            @RequestParam Integer roomNumber) {
        log.info("REQUEST: /PATCH /admissions/{}/assign-room recibida", admissionId);

        Admission updated = admissionService.assignRoom(admissionId, roomNumber);

        log.info("Admission con ID {} modificada con éxito por el usuario {}", updated.getAdmissionId(), updated.getLastModifiedBy());
        return ResponseEntity.ok(admissionMapper.toResponseFromDomain(updated));
    }

    /**
     * Reasigna el médico responsable de un ingreso a otro médico activo del mismo servicio.
     *
     * <p>El nuevo médico debe estar activo y pertenecer al mismo servicio que el ingreso.
     * Rol requerido: {@code JEFESERVICIO}.</p>
     *
     * @param admissionId UUID del ingreso a reasignar
     * @param doctorId    UUID del nuevo médico responsable
     * @return ingreso con el médico actualizado
     */
    @Operation(
            summary = "Reasignar médico de un ingreso",
            description = "Cambia el médico responsable de un ingreso. El nuevo médico debe estar activo " +
                    "y pertenecer al mismo servicio que el ingreso. Rol: JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Médico reasignado correctamente"),
            @ApiResponse(responseCode = "400", description = "Ingreso o médico no encontrado, " +
                    "médico inactivo o de diferente servicio",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @PatchMapping("/assign-doctor/{admissionId}")
    @PreAuthorize("hasAnyRole('JEFESERVICIO')")
    public ResponseEntity<AdmissionResponse> assignDoctor(
            @Parameter(description = "UUID del ingreso", required = true)
            @PathVariable UUID admissionId,
            @Parameter(description = "UUID del nuevo médico responsable", required = true)
            @RequestParam UUID doctorId) {
        log.info("REQUEST: /PATCH /admissions/{}/assign-doctor recibida", admissionId);

        Admission updated = admissionService.reassignDoctor(admissionId, doctorId);

        log.info("Médico reasignado con éxito en la admisión {}", updated.getAdmissionId());
        return ResponseEntity.ok(admissionMapper.toResponseFromDomain(updated));
    }

    /**
     * Actualiza la información clínica de un ingreso (diagnóstico, historia, alergias, etc.).
     *
     * <p>Roles permitidos: {@code MEDICO}, {@code JEFESERVICIO}.</p>
     *
     * @param admissionId UUID del ingreso a actualizar
     * @param request     nuevos datos clínicos del ingreso
     * @return ingreso con la información clínica actualizada
     */
    @Operation(
            summary = "Actualizar información clínica del ingreso",
            description = "Modifica el diagnóstico principal, historia médica, alergias, tratamiento crónico " +
                    "y escala de Barthel basal de un ingreso. Roles: MEDICO, JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Información clínica actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Ingreso no encontrado",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @PutMapping("/clinical-update/{admissionId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<AdmissionResponse> updateClinicalInfo(
            @Parameter(description = "UUID del ingreso a actualizar", required = true)
            @PathVariable UUID admissionId,
            @RequestBody AdmissionRequest request) {
        log.info("REQUEST: /PUT /admissions/{}/clinical-update recibida", admissionId);

        Admission data = admissionMapper.toDomainFromRequest(request);
        data.setAdmissionId(admissionId);

        Admission updated = admissionService.update(admissionId, data);

        log.info("Admission con ID {} modificada con éxito por el usuario {}", updated.getAdmissionId(), updated.getLastModifiedBy());
        return ResponseEntity.ok(admissionMapper.toResponseFromDomain(updated));
    }

    /**
     * Da de alta a un paciente, cerrando el ingreso activo y actualizando su estado a {@code ALTA}.
     *
     * <p>Roles permitidos: {@code MEDICO}, {@code JEFESERVICIO}.</p>
     *
     * @param admissionId UUID del ingreso que se cierra con el alta
     * @return ingreso con la fecha de alta registrada
     */
    @Operation(
            summary = "Dar de alta a un paciente",
            description = "Cierra el ingreso activo registrando la fecha de alta y cambia el estado " +
                    "del paciente a ALTA. Roles: MEDICO, JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alta procesada correctamente"),
            @ApiResponse(responseCode = "400", description = "Ingreso no encontrado",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @PatchMapping("/discharge/{admissionId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<AdmissionResponse> discharge(
            @Parameter(description = "UUID del ingreso a dar de alta", required = true)
            @PathVariable UUID admissionId) {
        log.info("REQUEST: /POST /admissions/{}/discharge recibida", admissionId);

        Admission discharged = admissionService.dischargeAdmission(admissionId);

        log.info("Admission con ID {} dada de alta con éxito por el usuario {}", discharged.getAdmissionId(), discharged.getLastModifiedBy());
        return ResponseEntity.ok(admissionMapper.toResponseFromDomain(discharged));
    }
}
