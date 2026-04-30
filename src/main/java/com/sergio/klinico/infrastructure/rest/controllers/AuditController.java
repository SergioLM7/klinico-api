package com.sergio.klinico.infrastructure.rest.controllers;

import com.sergio.klinico.application.services.AuditService;
import com.sergio.klinico.infrastructure.rest.dto.responses.audit.RevisionResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la consulta del historial de auditoría generado por Hibernate Envers.
 *
 * <p>Expone el registro de revisiones (CREATE / MOD / DEL) de ingresos y episodios clínicos.
 * Todos los endpoints están restringidos al rol {@code SYSADMIN}.</p>
 *
 * <p>Las revisiones se ordenan de forma ascendente por número de revisión (más antiguas primero).</p>
 *
 * <p>Base URL: {@code /api/v1/audit}</p>
 */
@Tag(name = "Auditoría", description = "Historial de revisiones Envers de ingresos y episodios clínicos")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditController {

    private final AuditService auditService;

    /**
     * Devuelve el historial completo de revisiones de un ingreso concreto.
     *
     * <p>Incluye todas las operaciones registradas por Envers (INSERT, UPDATE, DELETE)
     * con su timestamp y el tipo de operación. Rol requerido: {@code SYSADMIN}.</p>
     *
     * @param admissionId UUID del ingreso cuyo historial se consulta
     * @return lista ordenada de revisiones del ingreso
     */
    @Operation(
            summary = "Historial de revisiones de un ingreso",
            description = "Devuelve todas las revisiones Envers (INSERT/UPDATE/DELETE) del ingreso indicado, " +
                    "ordenadas de más antigua a más reciente. Rol: SYSADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/admissions/{admissionId}/revisions")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<List<RevisionResponse.AdmissionRevisionResponse>> getAdmissionRevisions(
            @Parameter(description = "UUID del ingreso hospitalario", required = true)
            @PathVariable UUID admissionId) {
        log.info("REQUEST: GET /audit/admissions/{}/revisions recibida", admissionId);

        List<RevisionResponse.AdmissionRevisionResponse> revisions = auditService.getAdmissionRevisions(admissionId);

        log.info("REQUEST: GET /audit/admissions/{}/revisions exitosa - {} revisiones encontradas",
                admissionId, revisions.size());
        return ResponseEntity.ok(revisions);
    }

    /**
     * Devuelve el historial completo de revisiones de un episodio clínico concreto.
     *
     * <p>Incluye todas las operaciones registradas por Envers (INSERT, UPDATE, DELETE)
     * con su timestamp y el tipo de operación. Rol requerido: {@code SYSADMIN}.</p>
     *
     * @param episodeId UUID del episodio cuyo historial se consulta
     * @return lista ordenada de revisiones del episodio
     */
    @Operation(
            summary = "Historial de revisiones de un episodio clínico",
            description = "Devuelve todas las revisiones Envers (INSERT/UPDATE/DELETE) del episodio indicado, " +
                    "ordenadas de más antigua a más reciente. Rol: SYSADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/episodes/{episodeId}/revisions")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<List<RevisionResponse.EpisodeRevisionResponse>> getEpisodeRevisions(
            @Parameter(description = "UUID del episodio clínico", required = true)
            @PathVariable UUID episodeId) {
        log.info("REQUEST: GET /audit/episodes/{}/revisions recibida", episodeId);

        List<RevisionResponse.EpisodeRevisionResponse> revisions = auditService.getEpisodeRevisions(episodeId);

        log.info("REQUEST: GET /audit/episodes/{}/revisions exitosa - {} revisiones encontradas",
                episodeId, revisions.size());
        return ResponseEntity.ok(revisions);
    }

    /**
     * Devuelve las revisiones de ingresos o episodios filtradas por el usuario que las realizó.
     *
     * <p>El parámetro {@code entityType} determina sobre qué entidad se filtra:
     * {@code admissions} para ingresos o {@code episodes} para episodios.
     * Rol requerido: {@code SYSADMIN}.</p>
     *
     * @param userId     UUID del usuario cuyas revisiones se consultan
     * @param entityType tipo de entidad auditada: {@code "admissions"} o {@code "episodes"}
     * @return lista de revisiones del tipo indicado realizadas por el usuario
     */
    @Operation(
            summary = "Revisiones de un usuario por tipo de entidad",
            description = "Devuelve todas las revisiones Envers realizadas por un usuario sobre ingresos " +
                    "('admissions') o episodios ('episodes'). Rol: SYSADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Revisiones obtenidas correctamente"),
            @ApiResponse(responseCode = "400", description = "entityType inválido (debe ser 'admissions' o 'episodes')",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/revisions")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<?> getRevisionsByUser(
            @Parameter(description = "UUID del usuario cuyas revisiones se consultan", required = true)
            @RequestParam UUID userId,
            @Parameter(description = "Tipo de entidad auditada: 'admissions' o 'episodes'",
                    example = "admissions", required = true)
            @RequestParam String entityType) {
        log.info("REQUEST: GET /audit/revisions recibida con userId: {} y entityType: {}", userId, entityType);

        if (!entityType.equals("admissions") && !entityType.equals("episodes")) {
            log.warn("entityType inválido: {}", entityType);
            return ResponseEntity.badRequest().body("entityType debe ser 'admissions' o 'episodes'");
        }

        if (entityType.equals("admissions")) {
            List<RevisionResponse.AdmissionRevisionResponse> revisions = auditService.getAdmissionRevisionsByUser(userId);
            log.info("REQUEST: GET /audit/revisions exitosa para admissions - {} revisiones encontradas", revisions.size());
            return ResponseEntity.ok(revisions);
        } else {
            List<RevisionResponse.EpisodeRevisionResponse> revisions = auditService.getEpisodeRevisionsByUser(userId);
            log.info("REQUEST: GET /audit/revisions exitosa para episodes - {} revisiones encontradas", revisions.size());
            return ResponseEntity.ok(revisions);
        }
    }
}
