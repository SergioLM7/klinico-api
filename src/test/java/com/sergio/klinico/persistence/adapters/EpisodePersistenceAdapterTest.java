package com.sergio.klinico.persistence.adapters;

import com.sergio.klinico.domain.models.Episode;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.infrastructure.mappers.EpisodeMapper;
import com.sergio.klinico.infrastructure.persistence.AdmissionEntity;
import com.sergio.klinico.infrastructure.persistence.EpisodeEntity;
import com.sergio.klinico.infrastructure.persistence.UserEntity;
import com.sergio.klinico.infrastructure.persistence.adapters.EpisodePersistenceAdapter;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaEpisodeRepository;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EpisodePersistenceAdapter Tests")
class EpisodePersistenceAdapterTest {

    @InjectMocks
    private EpisodePersistenceAdapter adapter;

    @Mock
    private JpaEpisodeRepository jpaRepository;

    @Mock
    private JpaUserRepository jpaUserRepository;

    @Mock
    private EpisodeMapper mapper;

    private final UUID episodeId   = UUID.randomUUID();
    private final UUID admissionId = UUID.randomUUID();
    private final UUID doctorId    = UUID.randomUUID();
    private final UUID creatorId   = UUID.randomUUID();

    private EpisodeEntity testEntity;
    private Episode testDomain;

    @BeforeEach
    void setUp() {
        AdmissionEntity admissionEntity = new AdmissionEntity();
        admissionEntity.setAdmissionId(admissionId);

        testEntity = new EpisodeEntity();
        testEntity.setEpisodeId(episodeId);
        testEntity.setAdmission(admissionEntity);
        testEntity.setDoctorId(doctorId);
        testEntity.setClinicalProgress("Evolución favorable");
        testEntity.setDiagnosis("Neumonía");

        testDomain = Episode.builder()
                .episodeId(episodeId)
                .admissionId(admissionId)
                .doctorId(doctorId)
                .clinicalProgress("Evolución favorable")
                .diagnosis("Neumonía")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("save: new episode (null id) should not load existing version")
    void save_WhenNewEpisode_ShouldSaveWithoutVersionLookup() {
        Episode newEpisode = Episode.builder().build();

        when(mapper.toEntity(newEpisode)).thenReturn(testEntity);
        when(jpaRepository.save(testEntity)).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        Episode result = adapter.save(newEpisode);

        assertThat(result).isEqualTo(testDomain);
        verify(jpaRepository, never()).findById(any());
    }

    @Test
    @DisplayName("save: existing episode should copy version from DB")
    void save_WhenExistingEpisode_ShouldCopyVersionFromDb() {
        testDomain.setEpisodeId(episodeId);
        EpisodeEntity existingEntity = new EpisodeEntity();
        existingEntity.setVersion(2L);

        when(mapper.toEntity(testDomain)).thenReturn(testEntity);
        when(jpaRepository.findById(episodeId)).thenReturn(Optional.of(existingEntity));
        when(jpaRepository.save(testEntity)).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        Episode result = adapter.save(testDomain);

        assertThat(result).isEqualTo(testDomain);
        assertThat(testEntity.getVersion()).isEqualTo(2L);
        verify(jpaRepository).findById(episodeId);
    }

    @Test
    @DisplayName("save: existing episode not found in DB should still save")
    void save_WhenExistingEpisodeNotInDb_ShouldSaveAnyway() {
        testDomain.setEpisodeId(episodeId);

        when(mapper.toEntity(testDomain)).thenReturn(testEntity);
        when(jpaRepository.findById(episodeId)).thenReturn(Optional.empty());
        when(jpaRepository.save(testEntity)).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        Episode result = adapter.save(testDomain);

        assertThat(result).isEqualTo(testDomain);
    }

    @Test
    @DisplayName("findAllByAdmission: should return paginated episodes with creator name resolved")
    void findAllByAdmission_WithCreatorId_ShouldReturnPaginatedEpisodesWithName() {
        testEntity.setCreatedBy(creatorId);

        UserEntity creatorEntity = UserEntity.builder()
                .userId(creatorId)
                .name("Marta")
                .surname("Sánchez")
                .build();

        Page<EpisodeEntity> page = new PageImpl<>(List.of(testEntity), PageRequest.of(0, 10), 1);
        when(jpaRepository.findByAdmission_AdmissionId(eq(admissionId), any())).thenReturn(page);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);
        when(jpaUserRepository.findById(creatorId)).thenReturn(Optional.of(creatorEntity));

        PaginatedResult<Episode> result = adapter.findAllByAdmission(admissionId, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getCreatedByName()).isEqualTo("Dr. Marta Sánchez");
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findAllByAdmission: null createdBy should not query user repository")
    void findAllByAdmission_WithNullCreatedBy_ShouldNotQueryUserRepository() {
        testEntity.setCreatedBy(null);

        Page<EpisodeEntity> page = new PageImpl<>(List.of(testEntity), PageRequest.of(0, 10), 1);
        when(jpaRepository.findByAdmission_AdmissionId(eq(admissionId), any())).thenReturn(page);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        PaginatedResult<Episode> result = adapter.findAllByAdmission(admissionId, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getCreatedByName()).isNull();
        verify(jpaUserRepository, never()).findById(any());
    }

    @Test
    @DisplayName("findAllByAdmission: createdBy present but user not found leaves name null")
    void findAllByAdmission_WhenCreatorNotFound_ShouldLeaveNameNull() {
        testEntity.setCreatedBy(creatorId);

        Page<EpisodeEntity> page = new PageImpl<>(List.of(testEntity), PageRequest.of(0, 10), 1);
        when(jpaRepository.findByAdmission_AdmissionId(eq(admissionId), any())).thenReturn(page);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);
        when(jpaUserRepository.findById(creatorId)).thenReturn(Optional.empty());

        PaginatedResult<Episode> result = adapter.findAllByAdmission(admissionId, 0, 10);

        assertThat(result.content().get(0).getCreatedByName()).isNull();
    }

    @Test
    @DisplayName("findAllByAdmission: empty page returns empty result")
    void findAllByAdmission_WhenEmpty_ShouldReturnEmptyResult() {
        Page<EpisodeEntity> emptyPage = new PageImpl<>(List.of());
        when(jpaRepository.findByAdmission_AdmissionId(eq(admissionId), any())).thenReturn(emptyPage);

        PaginatedResult<Episode> result = adapter.findAllByAdmission(admissionId, 0, 10);

        assertThat(result.content()).isEmpty();
    }

    @Test
    @DisplayName("findById: should return episode with creator name when found")
    void findById_WhenFound_ShouldReturnEpisodeWithCreatorName() {
        testEntity.setCreatedBy(creatorId);
        UserEntity creatorEntity = UserEntity.builder()
                .userId(creatorId).name("Pedro").surname("Díaz").build();

        when(jpaRepository.findById(episodeId)).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);
        when(jpaUserRepository.findById(creatorId)).thenReturn(Optional.of(creatorEntity));

        Episode result = adapter.findById(episodeId);

        assertThat(result).isNotNull();
        assertThat(result.getCreatedByName()).isEqualTo("Dr. Pedro Díaz");
    }

    @Test
    @DisplayName("findById: should return null when episode not found")
    void findById_WhenNotFound_ShouldReturnNull() {
        when(jpaRepository.findById(episodeId)).thenReturn(Optional.empty());

        Episode result = adapter.findById(episodeId);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("findByEpisodeDate: should return list of episodes for given date")
    void findByEpisodeDate_ShouldReturnEpisodesForDate() {
        LocalDate date = LocalDate.now();
        testEntity.setCreatedBy(null);

        when(jpaRepository.findAllByCreatedAtDate(admissionId, date)).thenReturn(List.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        List<Episode> result = adapter.findByEpisodeDate(admissionId, date);

        assertThat(result).hasSize(1).contains(testDomain);
    }

    @Test
    @DisplayName("findByEpisodeDate: should return empty list when no episodes for date")
    void findByEpisodeDate_WhenNone_ShouldReturnEmptyList() {
        LocalDate date = LocalDate.now().minusDays(10);
        when(jpaRepository.findAllByCreatedAtDate(admissionId, date)).thenReturn(List.of());

        List<Episode> result = adapter.findByEpisodeDate(admissionId, date);

        assertThat(result).isEmpty();
    }
}
