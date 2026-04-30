package com.sergio.klinico.infrastructure.persistence.adapters;

import com.sergio.klinico.domain.models.Episode;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.repositories.EpisodeRepository;
import com.sergio.klinico.infrastructure.mappers.EpisodeMapper;
import com.sergio.klinico.infrastructure.persistence.EpisodeEntity;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaEpisodeRepository;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Adaptador de persistencia para la entidad {@link EpisodeEntity}.
 *
 * <p>Implementa el puerto de dominio {@link EpisodeRepository} mediante Spring Data JPA,
 * traduciendo entre los objetos de dominio {@link Episode} y las entidades de persistencia
 * {@link EpisodeEntity} a través de {@link EpisodeMapper}.</p>
 *
 * <p>Enriquece los episodios con el nombre del médico que los creó, construyendo
 * el campo {@code createdByName} a partir de {@link JpaUserRepository} para
 * evitar consultas N+1 indeseadas desde la capa de presentación.</p>
 */
@Component
@RequiredArgsConstructor
public class EpisodePersistenceAdapter implements EpisodeRepository {

    private final JpaEpisodeRepository jpaRepository;
    private final JpaUserRepository jpaUserRepository;
    private final EpisodeMapper mapper;

    /**
     * Persiste un episodio nuevo o actualiza uno existente.
     *
     * <p>Si el episodio ya tiene un ID asignado, se carga la versión actual de la entidad
     * para garantizar el control de concurrencia optimista.</p>
     *
     * @param episode episodio de dominio a guardar
     * @return episodio de dominio persistido, enriquecido con el nombre del médico creador
     */
    @Override
    public Episode save(Episode episode) {
        EpisodeEntity entity = mapper.toEntity(episode);

        if (episode.getEpisodeId() != null) {
            jpaRepository.findById(episode.getEpisodeId()).ifPresent(existingEntity ->
                    entity.setVersion(existingEntity.getVersion())
            );
        }
        EpisodeEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    /**
     * Devuelve los episodios de un ingreso paginados, ordenados por fecha de creación descendente.
     *
     * <p>Cada episodio incluye el nombre del médico creador en el formato {@code "Dr. Nombre Apellido"}.</p>
     *
     * @param admissionId UUID del ingreso
     * @param page        número de página (0-indexed)
     * @param size        número de resultados por página
     * @return resultado paginado de episodios con el nombre del médico creador
     */
    @Override
    public PaginatedResult<Episode> findAllByAdmission(UUID admissionId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<EpisodeEntity> entitiesPage = jpaRepository.findByAdmission_AdmissionId(admissionId, pageRequest);

        List<Episode> domainList = entitiesPage.stream().map(this::toDomainWithCreatorName).toList();

        return new PaginatedResult<>(
                domainList,
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast()
        );
    }

    /**
     * Busca un episodio por su identificador único.
     *
     * @param id UUID del episodio
     * @return episodio de dominio enriquecido con el nombre del médico creador,
     *         o {@code null} si no existe
     */
    @Override
    public Episode findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomainWithCreatorName)
                .orElse(null);
    }

    /**
     * Devuelve los episodios de un ingreso registrados en una fecha concreta.
     *
     * @param admissionId UUID del ingreso
     * @param episodeDate fecha de creación de los episodios a buscar
     * @return lista de episodios del ingreso para esa fecha, puede estar vacía
     */
    @Override
    public List<Episode> findByEpisodeDate(UUID admissionId, LocalDate episodeDate) {
        return jpaRepository.findAllByCreatedAtDate(admissionId, episodeDate).stream()
                .map(this::toDomainWithCreatorName)
                .toList();
    }

    /**
     * Convierte una entidad de episodio a dominio e inyecta el nombre del médico creador.
     *
     * <p>Consulta {@link JpaUserRepository} para obtener el nombre y apellido del usuario
     * referenciado por {@code createdBy} y los concatena con el prefijo {@code "Dr. "}.</p>
     *
     * @param entity entidad JPA del episodio
     * @return episodio de dominio con el campo {@code createdByName} rellenado
     */
    private Episode toDomainWithCreatorName(EpisodeEntity entity) {
        Episode domain = mapper.toDomain(entity);
        if (entity.getCreatedBy() != null) {
            jpaUserRepository.findById(entity.getCreatedBy()).ifPresent(user ->
                    domain.setCreatedByName("Dr. " + user.getName() + " " + user.getSurname())
            );
        }
        return domain;
    }
}
