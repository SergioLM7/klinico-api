package com.sergio.klinico.infrastructure.rest.controllers;

import com.sergio.klinico.application.services.EpisodeService;
import com.sergio.klinico.domain.models.Episode;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.infrastructure.mappers.EpisodeMapper;
import com.sergio.klinico.infrastructure.rest.dto.requests.EpisodeRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.PaginatedResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.episode.EpisodeResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.episode.EpisodeSummaryResponse;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión de los episodios clínicos (evolutivos de la ronda médica).
 *
 * <p>Un episodio ({@link Episode}) representa una nota de la ronda médica vinculada a un ingreso
 * concreto. Los episodios recogen el progreso clínico, el diagnóstico y los scores de valoración
 * (Braden, CAM, CHADS2) registrados por el médico en cada visita.</p>
 *
 * <p>Solo los médicos y jefes de servicio pueden crear, consultar y actualizar episodios.
 * Cada episodio solo puede ser modificado por el médico que lo creó.</p>
 *
 * <p>Base URL: {@code /api/v1/episodes}</p>
 */
@Tag(name = "Episodios Clínicos", description = "Gestión de los episodios de ronda médica asociados a ingresos")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/episodes")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EpisodeController {

    private final EpisodeService episodeService;
    private final EpisodeMapper episodeMapper;

    /**
     * Crea un nuevo episodio clínico vinculado a un ingreso existente.
     *
     * <p>Lanza {@code BusinessException} si el ingreso especificado no existe.
     * Roles permitidos: {@code MEDICO}, {@code JEFESERVICIO}.</p>
     *
     * @param request datos del episodio, incluyendo el ID del ingreso al que pertenece,
     *                validados con el grupo {@link CreateGroup}
     * @return resumen del episodio creado con HTTP 201
     */
    @Operation(
            summary = "Crear episodio clínico",
            description = "Registra un nuevo episodio de ronda médica asociado a un ingreso. " +
                    "Roles: MEDICO, JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Episodio creado correctamente",
                    content = @Content(schema = @Schema(implementation = EpisodeSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o ingreso no encontrado",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<EpisodeSummaryResponse> create(
            @Validated(CreateGroup.class)
            @RequestBody EpisodeRequest request) {
        log.info("REQUEST: POST /episodes/create recibida");

        Episode newEpisode = episodeMapper.toDomainFromDto(request);

        Episode savedEpisode = episodeService.create(request.getAdmissionId(), newEpisode);

        EpisodeSummaryResponse summaryResponse = episodeMapper.toSummaryResponseFromDomain(savedEpisode);

        log.info("Episodio {} creado con éxito por el usuario {}", savedEpisode.getEpisodeId(), savedEpisode.getCreatedBy());
        return ResponseEntity.status(HttpStatus.CREATED).body(summaryResponse);
    }

    /**
     * Devuelve los episodios clínicos de un ingreso de forma paginada, ordenados por fecha descendente.
     *
     * <p>Roles permitidos: {@code MEDICO}, {@code JEFESERVICIO}.</p>
     *
     * @param admissionId UUID del ingreso cuyos episodios se consultan
     * @param page        número de página (0-indexed)
     * @return página de episodios del ingreso indicado (5 por página)
     */
    @Operation(
            summary = "Obtener episodios de un ingreso",
            description = "Devuelve los episodios clínicos asociados al ingreso indicado, ordenados por " +
                    "fecha descendente (5 episodios por página). Roles: MEDICO, JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Episodios obtenidos correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/{admissionId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<PaginatedResponse<EpisodeResponse>> getEpisodesByAdmissionId(
            @Parameter(description = "UUID del ingreso hospitalario", required = true)
            @PathVariable UUID admissionId,
            @Parameter(description = "Número de página (0-indexed)", example = "0", required = true)
            @RequestParam int page
    ) {
        log.info("REQUEST: GET /episodes/{} recibida", admissionId);

        PaginatedResult<Episode> episodes = episodeService.getEpisodesByAdmission(admissionId, page);

        List<EpisodeResponse> responseList = episodes.content().stream()
                .map(episodeMapper::toResponseFromDomain)
                .toList();

        PaginatedResponse<EpisodeResponse> response = PaginatedResponse.create(responseList, episodes);

        log.info("REQUEST: GET /{admissionId} ejecutada con éxito");
        return ResponseEntity.ok(response);
    }

    /**
     * Devuelve los episodios de un ingreso registrados en una fecha concreta.
     *
     * <p>Roles permitidos: {@code MEDICO}, {@code JEFESERVICIO}.</p>
     *
     * @param admissionId UUID del ingreso
     * @param episodeDate fecha del episodio en formato {@code YYYY-MM-DD}
     * @return lista de episodios registrados en la fecha indicada (puede estar vacía)
     */
    @Operation(
            summary = "Obtener episodios de un ingreso por fecha",
            description = "Devuelve todos los episodios de un ingreso registrados en una fecha concreta. " +
                    "Roles: MEDICO, JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Episodios obtenidos correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/{admissionId}/{episodeDate}")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<List<EpisodeResponse>> getEpisodeByDate(
            @Parameter(description = "UUID del ingreso hospitalario", required = true)
            @PathVariable UUID admissionId,
            @Parameter(description = "Fecha del episodio (formato YYYY-MM-DD)", example = "2026-04-30", required = true)
            @PathVariable LocalDate episodeDate
    ) {
        log.info("REQUEST: GET /episodes/{}/{} recibida", admissionId, episodeDate);

        List<Episode> episodeList = episodeService.getEpisodeByEpisodeDate(admissionId, episodeDate);

        List<EpisodeResponse> response = episodeList.stream()
                .map(episodeMapper::toResponseFromDomain)
                .toList();

        log.info("REQUEST: GET /episodes/{}/{} ejecutada con éxito", admissionId, episodeDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza un episodio clínico existente.
     *
     * <p>Solo el médico que creó el episodio puede modificarlo (validación implementada
     * en el dominio mediante {@code Episode#validateUpdate}). La petición se valida
     * con el grupo {@link UpdateGroup}. Roles permitidos: {@code MEDICO}, {@code JEFESERVICIO}.</p>
     *
     * @param episodeId UUID del episodio a actualizar
     * @param request   nuevos datos clínicos del episodio, validados con {@link UpdateGroup}
     * @param user      usuario autenticado inyectado por Spring Security
     * @return episodio con los datos clínicos actualizados
     */
    @Operation(
            summary = "Actualizar episodio clínico",
            description = "Modifica un episodio existente. Solo el médico que lo creó puede actualizarlo. " +
                    "Roles: MEDICO, JEFESERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Episodio actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Episodio no encontrado o el médico no es el autor",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @PutMapping("/update/{episodeId}")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<EpisodeResponse> update(
            @Parameter(description = "UUID del episodio a actualizar", required = true)
            @PathVariable UUID episodeId,
            @Validated(UpdateGroup.class)
            @RequestBody EpisodeRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.info("REQUEST: PUT /episodes/update/{} recibida", episodeId);

        Episode data = episodeMapper.toDomainFromDto(request);
        data.setEpisodeId(episodeId);

        Episode episodeUpdated = episodeService.update(data, episodeId, user.getId());

        EpisodeResponse response = episodeMapper.toResponseFromDomain(episodeUpdated);

        log.info("Episode con ID {} modificado con éxito por el usuario {}", episodeId, response.getLastModifiedBy());
        return ResponseEntity.ok(response);
    }

}
