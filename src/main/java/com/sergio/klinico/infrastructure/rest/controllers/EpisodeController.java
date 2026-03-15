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

@RestController
@RequestMapping("/api/v1/episodes")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EpisodeController {

    private final EpisodeService episodeService;
    private final EpisodeMapper episodeMapper;

    @PostMapping("/create")
    @PreAuthorize("hasRole('MEDICO', 'JEFESERVICIO')")
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

    @GetMapping("/{admissionId}")
    @PreAuthorize("hasRole('MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<PaginatedResponse<EpisodeResponse>> getEpisodesByAdmissionId (
            @PathVariable UUID admissionId,
            @RequestParam int page
    ) {
        log.info("REQUEST: GET /episodes/{} recibida", admissionId);

        PaginatedResult<Episode> episodes = episodeService.getEpisodesByAdmission(admissionId, page);

        List<EpisodeResponse> responseList = episodes.content().stream()
                .map(episodeMapper::toResponseFromDomain)
                .toList();

        PaginatedResponse<EpisodeResponse> response = PaginatedResponse.create(responseList, episodes);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{episodeId}/{episodeDate}")
    @PreAuthorize("hasRole('MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<List<EpisodeResponse>> getEpisodeByDate(
            @PathVariable UUID episodeId,
            @PathVariable LocalDate episodeDate
    ) {
        log.info("REQUEST: GET /episodes/{}/{} recibida", episodeId, episodeDate);

        List<Episode> episodeList = episodeService.getEpisodeByEpisodeDate(episodeId, episodeDate);

        List<EpisodeResponse> response = episodeList.stream()
                .map(episodeMapper::toResponseFromDomain)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{episodeId}")
    @PreAuthorize("hasRole('MEDICO', 'JEFESERVICIO')")
    public ResponseEntity<EpisodeResponse> update(
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
