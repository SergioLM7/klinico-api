package com.sergio.klinico.infrastructure.mappers;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Episode;
import com.sergio.klinico.infrastructure.persistence.AdmissionEntity;
import com.sergio.klinico.infrastructure.persistence.EpisodeEntity;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaAdmissionRepository;
import com.sergio.klinico.infrastructure.rest.dto.requests.EpisodeRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.episode.EpisodeResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.episode.EpisodeSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(componentModel = "spring", imports = {
        com.sergio.klinico.domain.exceptions.BusinessException.class
})
public abstract class EpisodeMapper {

    @Autowired
    protected JpaAdmissionRepository jpaAdmissionRepository;

    // DE REQUEST A DOMINIO
    @Mapping(target = "episodeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    public abstract Episode toDomainFromDto(EpisodeRequest request);

    // DE DOMINIO A ENTIDAD (Para el adapter)
    @Mapping(target = "admission", expression = "java(resolveAdmission(domain.getAdmissionId()))")
    public abstract EpisodeEntity toEntity(Episode domain);

    // DE ENTIDAD A DOMINIO (Para recuperar de BD)
    @Mapping(target = "admissionId", source = "admission.admissionId")
    public abstract Episode toDomain(EpisodeEntity entity);

    // DE DOMINIO A RESPONSE (Para el GET paginated)
    public abstract EpisodeResponse toResponseFromDomain(Episode domain);

    // DE DOMINIO A RESPONSE (Para el create)
    public abstract EpisodeSummaryResponse toSummaryResponseFromDomain(Episode domain);

    // Método de apoyo para recuperar la entidad Admission
    protected AdmissionEntity resolveAdmission(UUID admissionId) {
        if (admissionId == null) return null;
        return jpaAdmissionRepository.findById(admissionId)
                .orElseThrow(() -> new BusinessException("No se encontró la admisión para vincular al episodio"));
    }
}
