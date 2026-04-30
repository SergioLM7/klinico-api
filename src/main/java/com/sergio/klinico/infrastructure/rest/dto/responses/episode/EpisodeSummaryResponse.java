package com.sergio.klinico.infrastructure.rest.dto.responses.episode;

import java.time.LocalDateTime;
import java.util.UUID;

public record EpisodeSummaryResponse (
        UUID episodeId,
        UUID admissionId,
        UUID doctorId,
        LocalDateTime createdAt
) {}
