package com.sergio.klinico.infrastructure.rest.controllers;

import com.sergio.klinico.application.services.UserService;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.UserWorkLoad;
import com.sergio.klinico.infrastructure.rest.dto.responses.PaginatedResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.user.UserResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.user.UserServiceWorkloadResponse;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la consulta de usuarios del sistema.
 *
 * <p>Expone operaciones de búsqueda de usuarios dentro del servicio del usuario autenticado
 * y el cálculo de la carga de trabajo de cada médico del servicio.</p>
 *
 * <p>Base URL: {@code /api/v1/users}</p>
 */
@Tag(name = "Usuarios", description = "Búsqueda de usuarios y consulta de carga de trabajo por servicio")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Busca usuarios activos del mismo servicio que el usuario autenticado por apellido.
     *
     * <p>La búsqueda es parcial e insensible a mayúsculas. Solo devuelve usuarios activos
     * del mismo servicio que el usuario que realiza la petición. Roles permitidos:
     * {@code MEDICO}, {@code JEFESERVICIO}, {@code SYSADMIN}.</p>
     *
     * @param surname apellido o fragmento del apellido a buscar
     * @param page    número de página (0-indexed, por defecto 0)
     * @param size    tamaño de página (por defecto 10)
     * @param user    usuario autenticado inyectado por Spring Security
     * @return página de usuarios que coinciden con el apellido dentro del servicio del solicitante
     */
    @Operation(
            summary = "Buscar usuarios por apellido dentro del servicio",
            description = "Búsqueda parcial e insensible a mayúsculas por apellido. Devuelve usuarios activos " +
                    "del mismo servicio que el usuario autenticado. Roles: MEDICO, JEFESERVICIO, SYSADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados de búsqueda obtenidos correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO', 'SYSADMIN')")
    public ResponseEntity<PaginatedResponse<UserResponse>> searchBySurname(
            @Parameter(description = "Apellido o fragmento del apellido del usuario", example = "López", required = true)
            @RequestParam String surname,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Número de elementos por página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user
    ) {
        log.info("REQUEST: GET /users/search recibida con apellido: {}", surname);

        PaginatedResult<User> result = userService.searchBySurnameAndServiceId(surname, user.getServiceId(), page, size);

        List<UserResponse> responseList = result.content().stream()
                .map(u -> new UserResponse(u.getId(), u.getName(), u.getSurname(), u.getEmail(), u.getRole()))
                .toList();

        PaginatedResponse<UserResponse> response = PaginatedResponse.create(responseList, result);

        log.info("REQUEST: GET /users/search exitosa - {} usuarios encontrados", responseList.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Devuelve la carga de trabajo de los médicos del servicio del usuario autenticado.
     *
     * <p>La carga de trabajo se calcula como el número de ingresos activos asignados a cada médico.
     * Rol requerido: {@code JEFESERVICIO}.</p>
     *
     * @param page tamaño de página (por defecto 0)
     * @param size número de resultados por página (por defecto 10)
     * @param user usuario autenticado inyectado por Spring Security
     * @return página con la carga de trabajo de cada médico del servicio
     */
    @Operation(
            summary = "Carga de trabajo de los médicos del servicio",
            description = "Devuelve el número de ingresos activos asignados a cada médico del servicio " +
                    "del usuario autenticado. Rol: JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carga de trabajo obtenida correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/service-workload")
    @PreAuthorize("hasAnyRole('JEFESERVICIO')")
    public ResponseEntity<PaginatedResponse<UserServiceWorkloadResponse>> getServiceWorkload(
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Número de resultados por página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user
    ) {
        log.info("REQUEST: GET /users/service-workload recibida para el servicio: {}", user.getServiceId());

        PaginatedResult<UserWorkLoad> result = userService.serviceWorkload(user.getServiceId(), page, size);

        List<UserServiceWorkloadResponse> responseList = result.content().stream()
                .map(u -> new UserServiceWorkloadResponse(u.getName(), u.getSurname(), u.getAdmissionsAssigned()))
                .toList();

        PaginatedResponse<UserServiceWorkloadResponse> response = PaginatedResponse.create(responseList, result);

        log.info("REQUEST: GET /users/service-workload exitosa - {} usuarios encontrados", responseList.size());
        return ResponseEntity.ok(response);
    }
}
