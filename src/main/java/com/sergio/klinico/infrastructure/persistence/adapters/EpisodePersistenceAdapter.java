package com.sergio.klinico.infrastructure.persistence.adapters;

import com.sergio.klinico.domain.models.Episode;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.repositories.EpisodeRepository;
import com.sergio.klinico.infrastructure.mappers.EpisodeMapper;
import com.sergio.klinico.infrastructure.persistence.EpisodeEntity;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaEpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EpisodePersistenceAdapter implements EpisodeRepository {

    private final JpaEpisodeRepository jpaRepository;
    private final EpisodeMapper mapper;

    @Override
    public Episode save(Episode episode) {
        EpisodeEntity entity = mapper.toEntity(episode);

        // Si es una actualización (el ID ya existe) le asignamos la versión actual a la entidad que vamos a guardar
        if (episode.getEpisodeId() != null) {
            jpaRepository.findById(episode.getEpisodeId()).ifPresent(existingEntity ->
                    entity.setVersion(existingEntity.getVersion())
            );
        }
        EpisodeEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public PaginatedResult<Episode> findAllByAdmission(UUID admissionId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<EpisodeEntity> entitiesPage = jpaRepository.findByAdmission_AdmissionId(admissionId, pageRequest);

        List<Episode> domainList = entitiesPage.stream().map(mapper::toDomain).toList();

        return new PaginatedResult<>(
                domainList,
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast()
        );
    }

    @Override
    public Episode findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<Episode> findByEpisodeDate(UUID admissionId, LocalDate episodeDate) {

        return jpaRepository.findAllByCreatedAtDate(admissionId, episodeDate).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
