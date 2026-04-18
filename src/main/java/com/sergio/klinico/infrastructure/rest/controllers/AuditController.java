package com.sergio.klinico.infrastructure.rest.controllers;

import com.sergio.klinico.application.services.AuditService;
import com.sergio.klinico.infrastructure.rest.dto.responses.audit.RevisionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/admissions/{admissionId}/revisions")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<List<RevisionResponse.AdmissionRevisionResponse>> getAdmissionRevisions(
            @PathVariable UUID admissionId) {
        log.info("REQUEST: GET /audit/admissions/{}/revisions recibida", admissionId);

        List<RevisionResponse.AdmissionRevisionResponse> revisions = auditService.getAdmissionRevisions(admissionId);

        log.info("REQUEST: GET /audit/admissions/{}/revisions exitosa - {} revisiones encontradas", 
                admissionId, revisions.size());
        return ResponseEntity.ok(revisions);
    }

    @GetMapping("/episodes/{episodeId}/revisions")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<List<RevisionResponse.EpisodeRevisionResponse>> getEpisodeRevisions(
            @PathVariable UUID episodeId) {
        log.info("REQUEST: GET /audit/episodes/{}/revisions recibida", episodeId);

        List<RevisionResponse.EpisodeRevisionResponse> revisions = auditService.getEpisodeRevisions(episodeId);

        log.info("REQUEST: GET /audit/episodes/{}/revisions exitosa - {} revisiones encontradas", 
                episodeId, revisions.size());
        return ResponseEntity.ok(revisions);
    }

    @GetMapping("/revisions")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<?> getRevisionsByUser(
            @RequestParam UUID userId,
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
