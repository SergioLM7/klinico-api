package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Admission;
import com.sergio.klinico.domain.models.Episode;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.repositories.AdmissionRepository;
import com.sergio.klinico.domain.repositories.EpisodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EpisodeService Tests")
class EpisodeServiceTest {

    @InjectMocks
    private EpisodeService episodeService;

    @Mock
    private AdmissionRepository admissionRepository;

    @Mock
    private EpisodeRepository episodeRepository;

    private UUID admissionId;
    private UUID episodeId;
    private UUID doctorId;
    private Admission activeAdmission;
    private Episode testEpisode;

    @BeforeEach
    void setUp() {
        admissionId = UUID.randomUUID();
        episodeId = UUID.randomUUID();
        doctorId = UUID.randomUUID();

        activeAdmission = Admission.builder()
                .admissionId(admissionId)
                .roomNumber(101)
                .build();

        testEpisode = Episode.builder()
                .episodeId(episodeId)
                .admissionId(admissionId)
                .doctorId(doctorId)
                .clinicalProgress("Evolución favorable")
                .diagnosis("Neumonía")
                .bradenScore(18)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create episode when admission exists and is not discharged")
    void create_WhenAdmissionExistsAndActive_ShouldCreateEpisode() {
        when(admissionRepository.findById(admissionId)).thenReturn(activeAdmission);
        when(episodeRepository.save(any(Episode.class))).thenReturn(testEpisode);

        Episode result = episodeService.create(admissionId, testEpisode);

        assertNotNull(result);
        assertEquals(episodeId, result.getEpisodeId());
        assertEquals(admissionId, result.getAdmissionId());
        verify(admissionRepository).findById(admissionId);
        verify(episodeRepository).save(testEpisode);
    }

    @Test
    @DisplayName("Should throw BusinessException when admission does not exist")
    void create_WhenAdmissionNotFound_ShouldThrowBusinessException() {
        when(admissionRepository.findById(admissionId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                episodeService.create(admissionId, testEpisode)
        );

        assertEquals("La admisión solicitada no está en BD", exception.getMessage());
        verify(admissionRepository).findById(admissionId);
        verify(episodeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when admission is already discharged")
    void create_WhenAdmissionIsDischarged_ShouldThrowBusinessException() {
        Admission dischargedAdmission = Admission.builder()
                .admissionId(admissionId)
                .roomNumber(101)
                .dischargeDate(LocalDateTime.now().minusDays(1))
                .build();

        when(admissionRepository.findById(admissionId)).thenReturn(dischargedAdmission);

        assertThrows(BusinessException.class, () ->
                episodeService.create(admissionId, testEpisode)
        );

        verify(admissionRepository).findById(admissionId);
        verify(episodeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when admission has no room assigned")
    void create_WhenAdmissionHasNoRoom_ShouldThrowBusinessException() {
        Admission admissionWithoutRoom = Admission.builder()
                .admissionId(admissionId)
                .roomNumber(null)
                .build();

        when(admissionRepository.findById(admissionId)).thenReturn(admissionWithoutRoom);

        assertThrows(BusinessException.class, () ->
                episodeService.create(admissionId, testEpisode)
        );

        verify(admissionRepository).findById(admissionId);
        verify(episodeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return paginated episodes for a given admission")
    void getEpisodesByAdmission_ShouldReturnPaginatedResult() {
        int page = 0;
        PaginatedResult<Episode> expected = new PaginatedResult<>(List.of(testEpisode), 1L, 1, page, true);

        when(episodeRepository.findAllByAdmission(admissionId, page, 5)).thenReturn(expected);

        PaginatedResult<Episode> result = episodeService.getEpisodesByAdmission(admissionId, page);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(episodeId, result.content().get(0).getEpisodeId());
        verify(episodeRepository).findAllByAdmission(admissionId, page, 5);
    }

    @Test
    @DisplayName("Should return episodes for a given date and admission")
    void getEpisodeByEpisodeDate_ShouldReturnEpisodeList() {
        LocalDate episodeDate = LocalDate.now();

        when(episodeRepository.findByEpisodeDate(admissionId, episodeDate))
                .thenReturn(List.of(testEpisode));

        List<Episode> result = episodeService.getEpisodeByEpisodeDate(admissionId, episodeDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(episodeId, result.getFirst().getEpisodeId());
        verify(episodeRepository).findByEpisodeDate(admissionId, episodeDate);
    }

    @Test
    @DisplayName("Should return empty list when no episodes exist for that date")
    void getEpisodeByEpisodeDate_WhenNoneFound_ShouldReturnEmptyList() {
        LocalDate episodeDate = LocalDate.now().minusDays(30);

        when(episodeRepository.findByEpisodeDate(admissionId, episodeDate)).thenReturn(List.of());

        List<Episode> result = episodeService.getEpisodeByEpisodeDate(admissionId, episodeDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(episodeRepository).findByEpisodeDate(admissionId, episodeDate);
    }

    @Test
    @DisplayName("Should update episode when it exists and the requesting doctor owns it")
    void update_WhenEpisodeExistsAndDoctorMatches_ShouldUpdateEpisode() {
        Episode updatedData = Episode.builder()
                .clinicalProgress("Mejoría notable")
                .diagnosis("Neumonía leve")
                .bradenScore(20)
                .build();

        when(episodeRepository.findById(episodeId)).thenReturn(testEpisode);
        when(episodeRepository.save(any(Episode.class))).thenReturn(testEpisode);

        Episode result = episodeService.update(updatedData, episodeId, doctorId);

        assertNotNull(result);
        verify(episodeRepository).findById(episodeId);
        verify(episodeRepository).save(testEpisode);
    }

    @Test
    @DisplayName("Should throw BusinessException when episode does not exist")
    void update_WhenEpisodeNotFound_ShouldThrowBusinessException() {
        Episode updatedData = Episode.builder().clinicalProgress("Nueva evolución").build();

        when(episodeRepository.findById(episodeId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                episodeService.update(updatedData, episodeId, doctorId)
        );

        assertEquals("El episodio que se intenta actualizar no existe en BD", exception.getMessage());
        verify(episodeRepository).findById(episodeId);
        verify(episodeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when a different doctor tries to update the episode")
    void update_WhenDifferentDoctorTriesToUpdate_ShouldThrowBusinessException() {
        UUID otherDoctorId = UUID.randomUUID();

        when(episodeRepository.findById(episodeId)).thenReturn(testEpisode);

        Episode episode = Episode.builder().build();

        BusinessException exception = assertThrows(BusinessException.class, () ->
                episodeService.update(episode, episodeId, otherDoctorId)
        );

        assertEquals("Solo el médico que creó el episodio puede modificarlo.", exception.getMessage());
        verify(episodeRepository).findById(episodeId);
        verify(episodeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when episode update window of 2 hours has expired")
    void update_WhenUpdateWindowExpired_ShouldThrowBusinessException() {
        Episode oldEpisode = Episode.builder()
                .episodeId(episodeId)
                .doctorId(doctorId)
                .clinicalProgress("Evolución inicial")
                .createdAt(LocalDateTime.now().minusHours(3))
                .build();

        Episode episode = Episode.builder().build();

        when(episodeRepository.findById(episodeId)).thenReturn(oldEpisode);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                episodeService.update(episode, episodeId, doctorId)
        );

        assertEquals("El plazo máximo de 2 horas para modificar el episodio ha expirado", exception.getMessage());
        verify(episodeRepository).findById(episodeId);
        verify(episodeRepository, never()).save(any());
    }
}
