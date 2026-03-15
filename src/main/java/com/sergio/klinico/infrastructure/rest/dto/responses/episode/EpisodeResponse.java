package com.sergio.klinico.infrastructure.rest.dto.responses.episode;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EpisodeResponse {
    private UUID episodeId;
    private UUID admissionId;
    private UUID doctorId;

    private String clinicalProgress;
    private String diagnosis;
    private Integer bradenScore;
    private Boolean camScore;
    private Integer chads2Score;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;
}
