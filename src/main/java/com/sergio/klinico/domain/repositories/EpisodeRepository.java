package com.sergio.klinico.domain.repositories;

import com.sergio.klinico.domain.models.Episode;
import com.sergio.klinico.domain.models.PaginatedResult;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EpisodeRepository {
    Episode save(Episode episode);
    Episode findById(UUID episodeId);
    PaginatedResult<Episode> findAllByAdmission(UUID admissionId, int page, int size);
    List<Episode> findByEpisodeDate(UUID admissionId, LocalDate episodeDate);
}
