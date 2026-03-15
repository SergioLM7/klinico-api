package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Admission;
import com.sergio.klinico.domain.models.Episode;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.repositories.AdmissionRepository;
import com.sergio.klinico.domain.repositories.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EpisodeService {

    private final AdmissionRepository admissionRepository;
    private final EpisodeRepository episodeRepository;

    @Transactional
    public Episode create(UUID admissionId, Episode newEpisode) {
        Admission admission = admissionRepository.findById(admissionId);

        if (admission == null) {
            log.error("La admisión {}, que está intentando modificar, no existe", admissionId);
            throw new BusinessException("La admisión solicitada no está en BD");
        }

        admission.addEpisode(newEpisode);

        return episodeRepository.save(newEpisode);
    }

    public PaginatedResult<Episode> getEpisodesByAdmission(UUID admissionId, int page) {
        return episodeRepository.findAllByAdmission(admissionId, page, 5);
    }

    public List<Episode> getEpisodeByEpisodeDate(UUID episodeId, LocalDate episodeDate) {
        return episodeRepository.findByEpisodeDate(episodeId, episodeDate);
    }

    @Transactional
    public Episode update(Episode updatedData, UUID episodeId, UUID doctorIdAttemptingUpdate) {

        Episode currentEpisode = episodeRepository.findById(episodeId);

        if(currentEpisode == null) {
            log.error("El episodio {}, que está intentando actualizar, no existe", episodeId);
            throw new BusinessException("El episodio que se intenta actualizar no existe en BD");
        }

        currentEpisode.validateUpdate(doctorIdAttemptingUpdate);

        currentEpisode.updateClinicalData(updatedData);

        return episodeRepository.save(currentEpisode);
    }
}
