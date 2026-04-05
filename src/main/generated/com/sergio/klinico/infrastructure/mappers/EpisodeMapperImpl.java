package com.sergio.klinico.infrastructure.mappers;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Episode;
import com.sergio.klinico.infrastructure.persistence.AdmissionEntity;
import com.sergio.klinico.infrastructure.persistence.EpisodeEntity;
import com.sergio.klinico.infrastructure.rest.dto.requests.EpisodeRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.episode.EpisodeResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.episode.EpisodeSummaryResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-28T12:03:57+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class EpisodeMapperImpl extends EpisodeMapper {

    @Override
    public Episode toDomainFromDto(EpisodeRequest request) {
        if ( request == null ) {
            return null;
        }

        Episode.EpisodeBuilder episode = Episode.builder();

        episode.admissionId( request.getAdmissionId() );
        episode.doctorId( request.getDoctorId() );
        episode.clinicalProgress( request.getClinicalProgress() );
        episode.diagnosis( request.getDiagnosis() );
        episode.bradenScore( request.getBradenScore() );
        episode.camScore( request.getCamScore() );
        episode.chads2Score( request.getChads2Score() );

        return episode.build();
    }

    @Override
    public EpisodeEntity toEntity(Episode domain) {
        if ( domain == null ) {
            return null;
        }

        EpisodeEntity episodeEntity = new EpisodeEntity();

        episodeEntity.setCreatedAt( domain.getCreatedAt() );
        episodeEntity.setCreatedBy( domain.getCreatedBy() );
        episodeEntity.setLastModifiedAt( domain.getLastModifiedAt() );
        episodeEntity.setLastModifiedBy( domain.getLastModifiedBy() );
        episodeEntity.setVersion( domain.getVersion() );
        episodeEntity.setEpisodeId( domain.getEpisodeId() );
        episodeEntity.setDoctorId( domain.getDoctorId() );
        episodeEntity.setClinicalProgress( domain.getClinicalProgress() );
        episodeEntity.setDiagnosis( domain.getDiagnosis() );
        episodeEntity.setBradenScore( domain.getBradenScore() );
        episodeEntity.setCamScore( domain.getCamScore() );
        episodeEntity.setChads2Score( domain.getChads2Score() );

        episodeEntity.setAdmission( resolveAdmission(domain.getAdmissionId()) );

        return episodeEntity;
    }

    @Override
    public Episode toDomain(EpisodeEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Episode.EpisodeBuilder episode = Episode.builder();

        episode.admissionId( entityAdmissionAdmissionId( entity ) );
        episode.version( entity.getVersion() );
        episode.episodeId( entity.getEpisodeId() );
        episode.doctorId( entity.getDoctorId() );
        episode.clinicalProgress( entity.getClinicalProgress() );
        episode.diagnosis( entity.getDiagnosis() );
        episode.bradenScore( entity.getBradenScore() );
        episode.camScore( entity.getCamScore() );
        episode.chads2Score( entity.getChads2Score() );
        episode.createdAt( entity.getCreatedAt() );
        episode.createdBy( entity.getCreatedBy() );
        episode.lastModifiedAt( entity.getLastModifiedAt() );
        episode.lastModifiedBy( entity.getLastModifiedBy() );

        return episode.build();
    }

    @Override
    public EpisodeResponse toResponseFromDomain(Episode domain) {
        if ( domain == null ) {
            return null;
        }

        EpisodeResponse.EpisodeResponseBuilder episodeResponse = EpisodeResponse.builder();

        episodeResponse.episodeId( domain.getEpisodeId() );
        episodeResponse.admissionId( domain.getAdmissionId() );
        episodeResponse.doctorId( domain.getDoctorId() );
        episodeResponse.clinicalProgress( domain.getClinicalProgress() );
        episodeResponse.diagnosis( domain.getDiagnosis() );
        episodeResponse.bradenScore( domain.getBradenScore() );
        episodeResponse.camScore( domain.getCamScore() );
        episodeResponse.chads2Score( domain.getChads2Score() );
        episodeResponse.createdAt( domain.getCreatedAt() );
        if ( domain.getCreatedBy() != null ) {
            episodeResponse.createdBy( domain.getCreatedBy().toString() );
        }
        episodeResponse.lastModifiedAt( domain.getLastModifiedAt() );
        if ( domain.getLastModifiedBy() != null ) {
            episodeResponse.lastModifiedBy( domain.getLastModifiedBy().toString() );
        }

        return episodeResponse.build();
    }

    @Override
    public EpisodeSummaryResponse toSummaryResponseFromDomain(Episode domain) {
        if ( domain == null ) {
            return null;
        }

        UUID episodeId = null;
        UUID admissionId = null;
        UUID doctorId = null;
        LocalDateTime createdAt = null;

        episodeId = domain.getEpisodeId();
        admissionId = domain.getAdmissionId();
        doctorId = domain.getDoctorId();
        createdAt = domain.getCreatedAt();

        EpisodeSummaryResponse episodeSummaryResponse = new EpisodeSummaryResponse( episodeId, admissionId, doctorId, createdAt );

        return episodeSummaryResponse;
    }

    private UUID entityAdmissionAdmissionId(EpisodeEntity episodeEntity) {
        AdmissionEntity admission = episodeEntity.getAdmission();
        if ( admission == null ) {
            return null;
        }
        return admission.getAdmissionId();
    }
}
