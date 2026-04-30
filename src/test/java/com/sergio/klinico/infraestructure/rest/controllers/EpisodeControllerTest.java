package com.sergio.klinico.infraestructure.rest.controllers;

import com.sergio.klinico.application.services.EpisodeService;
import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Episode;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.enums.UserRole;
import com.sergio.klinico.infrastructure.mappers.EpisodeMapper;
import com.sergio.klinico.infrastructure.rest.advice.GlobalExceptionHandler;
import com.sergio.klinico.infrastructure.rest.controllers.EpisodeController;
import com.sergio.klinico.infrastructure.rest.dto.requests.EpisodeRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.episode.EpisodeResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.episode.EpisodeSummaryResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EpisodeController Tests")
class EpisodeControllerTest {

    @InjectMocks
    private EpisodeController episodeController;

    @Mock
    private EpisodeService episodeService;

    @Mock
    private EpisodeMapper episodeMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final UUID admissionId = UUID.randomUUID();
    private final UUID episodeId = UUID.randomUUID();
    private final UUID doctorId = UUID.randomUUID();
    private final UUID serviceId = UUID.randomUUID();

    private User authenticatedDoctor;
    private Episode testEpisode;
    private EpisodeResponse testEpisodeResponse;
    private EpisodeSummaryResponse testSummaryResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(episodeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        authenticatedDoctor = User.builder()
                .id(doctorId)
                .email("medico@test.com")
                .name("Ana")
                .surname("López")
                .role(UserRole.MEDICO)
                .active(true)
                .serviceId(serviceId)
                .build();

        var auth = UsernamePasswordAuthenticationToken.authenticated(
                authenticatedDoctor, null, authenticatedDoctor.getAuthorities());
        var ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);

        testEpisode = Episode.builder()
                .episodeId(episodeId)
                .admissionId(admissionId)
                .doctorId(doctorId)
                .clinicalProgress("Evolución favorable")
                .diagnosis("Neumonía")
                .createdAt(LocalDateTime.now())
                .build();

        testEpisodeResponse = EpisodeResponse.builder()
                .episodeId(episodeId)
                .admissionId(admissionId)
                .doctorId(doctorId)
                .clinicalProgress("Evolución favorable")
                .diagnosis("Neumonía")
                .build();

        testSummaryResponse = new EpisodeSummaryResponse(
                episodeId, admissionId, doctorId, LocalDateTime.now()
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return 201 with EpisodeSummaryResponse when creating episode successfully")
    void create_WhenValidRequest_ShouldReturn201() throws Exception {
        EpisodeRequest request = EpisodeRequest.builder()
                .admissionId(admissionId)
                .doctorId(doctorId)
                .clinicalProgress("Evolución favorable")
                .diagnosis("Neumonía leve")
                .bradenScore(18)
                .build();

        when(episodeMapper.toDomainFromDto(any(EpisodeRequest.class))).thenReturn(testEpisode);
        when(episodeService.create(admissionId, testEpisode)).thenReturn(testEpisode);
        when(episodeMapper.toSummaryResponseFromDomain(any(Episode.class))).thenReturn(testSummaryResponse);

        mockMvc.perform(post("/api/v1/episodes/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.episodeId").value(episodeId.toString()))
                .andExpect(jsonPath("$.admissionId").value(admissionId.toString()))
                .andExpect(jsonPath("$.doctorId").value(doctorId.toString()));

        verify(episodeService).create(admissionId, testEpisode);
        verify(episodeMapper).toSummaryResponseFromDomain(any(Episode.class));
    }

    @Test
    @DisplayName("Should return 400 when required fields are missing in episode creation")
    void create_WhenRequiredFieldsMissing_ShouldReturn400() throws Exception {
        EpisodeRequest invalidRequest = EpisodeRequest.builder().build();

        mockMvc.perform(post("/api/v1/episodes/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("El admissionID es obligatorio")))
                .andExpect(jsonPath("$.message").value(containsString("El doctorID es obligatorio")))
                .andExpect(jsonPath("$.message").value(containsString("El progreso clínico es obligatorio")))
                .andExpect(jsonPath("$.message").value(containsString("El diagnóstico es obligatorio")));

        verify(episodeService, never()).create(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when bradenScore is out of range")
    void create_WhenBradenScoreOutOfRange_ShouldReturn400() throws Exception {
        EpisodeRequest invalidRequest = EpisodeRequest.builder()
                .admissionId(admissionId)
                .doctorId(doctorId)
                .clinicalProgress("Progreso")
                .diagnosis("Diagnóstico")
                .bradenScore(30)
                .build();

        mockMvc.perform(post("/api/v1/episodes/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("El valor de Braden no puede ser mayor que 23")));

        verify(episodeService, never()).create(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when creating episode throws BusinessException")
    void create_WhenBusinessException_ShouldReturn400() throws Exception {
        EpisodeRequest request = EpisodeRequest.builder()
                .admissionId(admissionId)
                .doctorId(doctorId)
                .clinicalProgress("Progreso")
                .diagnosis("Diagnóstico")
                .build();

        when(episodeMapper.toDomainFromDto(any(EpisodeRequest.class))).thenReturn(testEpisode);
        when(episodeService.create(admissionId, testEpisode))
                .thenThrow(new BusinessException("La admisión solicitada no está en BD"));

        mockMvc.perform(post("/api/v1/episodes/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La admisión solicitada no está en BD"));
    }

    @Test
    @DisplayName("Should return 200 with paginated episodes for a given admission")
    void getEpisodesByAdmissionId_ShouldReturn200() throws Exception {
        PaginatedResult<Episode> paginatedResult = new PaginatedResult<>(
                List.of(testEpisode), 1L, 1, 0, true
        );

        when(episodeService.getEpisodesByAdmission(admissionId, 0)).thenReturn(paginatedResult);
        when(episodeMapper.toResponseFromDomain(any(Episode.class))).thenReturn(testEpisodeResponse);

        mockMvc.perform(get("/api/v1/episodes/{admissionId}", admissionId)
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].episodeId").value(episodeId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(episodeService).getEpisodesByAdmission(admissionId, 0);
    }

    @Test
    @DisplayName("Should return 200 with empty data when no episodes exist for admission")
    void getEpisodesByAdmissionId_WhenNoEpisodes_ShouldReturn200WithEmptyData() throws Exception {
        PaginatedResult<Episode> emptyResult = new PaginatedResult<>(List.of(), 0L, 0, 0, true);

        when(episodeService.getEpisodesByAdmission(admissionId, 0)).thenReturn(emptyResult);

        mockMvc.perform(get("/api/v1/episodes/{admissionId}", admissionId)
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Should return 200 with episodes for a specific date")
    void getEpisodeByDate_ShouldReturn200() throws Exception {
        LocalDate episodeDate = LocalDate.now();

        when(episodeService.getEpisodeByEpisodeDate(admissionId, episodeDate))
                .thenReturn(List.of(testEpisode));
        when(episodeMapper.toResponseFromDomain(any(Episode.class))).thenReturn(testEpisodeResponse);

        mockMvc.perform(get("/api/v1/episodes/{admissionId}/{date}", admissionId, episodeDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].episodeId").value(episodeId.toString()));

        verify(episodeService).getEpisodeByEpisodeDate(admissionId, episodeDate);
    }

    @Test
    @DisplayName("Should return 200 with empty list when no episodes for that date")
    void getEpisodeByDate_WhenNoEpisodes_ShouldReturn200WithEmptyList() throws Exception {
        LocalDate episodeDate = LocalDate.now().minusDays(30);

        when(episodeService.getEpisodeByEpisodeDate(admissionId, episodeDate)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/episodes/{admissionId}/{date}", admissionId, episodeDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should return 200 with updated EpisodeResponse when update is successful")
    void update_WhenValidRequest_ShouldReturn200() throws Exception {
        EpisodeRequest request = EpisodeRequest.builder()
                .clinicalProgress("Nueva evolución")
                .diagnosis("Diagnóstico actualizado")
                .bradenScore(20)
                .build();

        when(episodeMapper.toDomainFromDto(any(EpisodeRequest.class))).thenReturn(testEpisode);
        when(episodeService.update(any(Episode.class), eq(episodeId), eq(doctorId))).thenReturn(testEpisode);
        when(episodeMapper.toResponseFromDomain(any(Episode.class))).thenReturn(testEpisodeResponse);

        mockMvc.perform(put("/api/v1/episodes/update/{id}", episodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.episodeId").value(episodeId.toString()))
                .andExpect(jsonPath("$.diagnosis").value("Neumonía"));

        verify(episodeService).update(any(Episode.class), eq(episodeId), eq(doctorId));
    }

    @Test
    @DisplayName("Should return 400 when update throws BusinessException")
    void update_WhenBusinessException_ShouldReturn400() throws Exception {
        EpisodeRequest request = EpisodeRequest.builder()
                .clinicalProgress("Nueva evolución")
                .diagnosis("Nuevo diagnóstico")
                .build();

        when(episodeMapper.toDomainFromDto(any(EpisodeRequest.class))).thenReturn(testEpisode);
        when(episodeService.update(any(Episode.class), eq(episodeId), eq(doctorId)))
                .thenThrow(new BusinessException("Solo el médico que creó el episodio puede modificarlo."));

        mockMvc.perform(put("/api/v1/episodes/update/{id}", episodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Solo el médico que creó el episodio puede modificarlo."));
    }

    @Test
    @DisplayName("Should return 400 when chads2Score is out of valid range on update")
    void update_WhenChads2ScoreOutOfRange_ShouldReturn400() throws Exception {
        EpisodeRequest invalidRequest = EpisodeRequest.builder()
                .chads2Score(15)
                .build();

        mockMvc.perform(put("/api/v1/episodes/update/{id}", episodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        containsString("El valor de chads2Score no puede ser mayor que 9")));

        verify(episodeService, never()).update(any(), any(), any());
    }
}
