package com.sergio.klinico.infraestructure.rest.controllers;

import com.sergio.klinico.application.services.AuditService;
import com.sergio.klinico.infrastructure.rest.advice.GlobalExceptionHandler;
import com.sergio.klinico.infrastructure.rest.controllers.AuditController;
import com.sergio.klinico.infrastructure.rest.dto.responses.audit.RevisionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditController Tests")
class AuditControllerTest {

    @InjectMocks
    private AuditController auditController;

    @Mock
    private AuditService auditService;

    private MockMvc mockMvc;

    private final UUID admissionId = UUID.randomUUID();
    private final UUID episodeId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(auditController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 200 with admission revisions list")
    void getAdmissionRevisions_WhenRevisionsExist_ShouldReturn200() throws Exception {
        RevisionResponse.AdmissionRevisionResponse revision = RevisionResponse.AdmissionRevisionResponse.builder()
                .revisionNumber(1)
                .revisionType("ADD")
                .revisionTimestamp(LocalDateTime.now())
                .admissionId(admissionId)
                .build();

        when(auditService.getAdmissionRevisions(admissionId)).thenReturn(List.of(revision));

        mockMvc.perform(get("/api/v1/audit/admissions/{id}/revisions", admissionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].revisionNumber").value(1))
                .andExpect(jsonPath("$[0].revisionType").value("ADD"));

        verify(auditService).getAdmissionRevisions(admissionId);
    }

    @Test
    @DisplayName("Should return 200 with empty list when no admission revisions exist")
    void getAdmissionRevisions_WhenNoRevisions_ShouldReturn200WithEmptyList() throws Exception {
        when(auditService.getAdmissionRevisions(admissionId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/audit/admissions/{id}/revisions", admissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(auditService).getAdmissionRevisions(admissionId);
    }

    @Test
    @DisplayName("Should return 200 with episode revisions list")
    void getEpisodeRevisions_WhenRevisionsExist_ShouldReturn200() throws Exception {
        RevisionResponse.EpisodeRevisionResponse revision = RevisionResponse.EpisodeRevisionResponse.builder()
                .revisionNumber(2)
                .revisionType("MOD")
                .revisionTimestamp(LocalDateTime.now())
                .episodeId(episodeId)
                .build();

        when(auditService.getEpisodeRevisions(episodeId)).thenReturn(List.of(revision));

        mockMvc.perform(get("/api/v1/audit/episodes/{id}/revisions", episodeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].revisionNumber").value(2))
                .andExpect(jsonPath("$[0].revisionType").value("MOD"));

        verify(auditService).getEpisodeRevisions(episodeId);
    }

    @Test
    @DisplayName("Should return 200 with empty list when no episode revisions exist")
    void getEpisodeRevisions_WhenNoRevisions_ShouldReturn200WithEmptyList() throws Exception {
        when(auditService.getEpisodeRevisions(episodeId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/audit/episodes/{id}/revisions", episodeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(auditService).getEpisodeRevisions(episodeId);
    }

    @Test
    @DisplayName("Should return 200 with admission revisions when entityType is 'admissions'")
    void getRevisionsByUser_WhenEntityTypeIsAdmissions_ShouldReturn200() throws Exception {
        RevisionResponse.AdmissionRevisionResponse revision = RevisionResponse.AdmissionRevisionResponse.builder()
                .revisionNumber(1)
                .revisionType("ADD")
                .userId(userId)
                .build();

        when(auditService.getAdmissionRevisionsByUser(userId)).thenReturn(List.of(revision));

        mockMvc.perform(get("/api/v1/audit/revisions")
                        .param("userId", userId.toString())
                        .param("entityType", "admissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].revisionType").value("ADD"));

        verify(auditService).getAdmissionRevisionsByUser(userId);
        verify(auditService, never()).getEpisodeRevisionsByUser(any());
    }

    @Test
    @DisplayName("Should return 200 with episode revisions when entityType is 'episodes'")
    void getRevisionsByUser_WhenEntityTypeIsEpisodes_ShouldReturn200() throws Exception {
        RevisionResponse.EpisodeRevisionResponse revision = RevisionResponse.EpisodeRevisionResponse.builder()
                .revisionNumber(3)
                .revisionType("MOD")
                .userId(userId)
                .build();

        when(auditService.getEpisodeRevisionsByUser(userId)).thenReturn(List.of(revision));

        mockMvc.perform(get("/api/v1/audit/revisions")
                        .param("userId", userId.toString())
                        .param("entityType", "episodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].revisionType").value("MOD"));

        verify(auditService).getEpisodeRevisionsByUser(userId);
        verify(auditService, never()).getAdmissionRevisionsByUser(any());
    }

    @Test
    @DisplayName("Should return 400 when entityType is invalid")
    void getRevisionsByUser_WhenInvalidEntityType_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/audit/revisions")
                        .param("userId", userId.toString())
                        .param("entityType", "invalid"))
                .andExpect(status().isBadRequest());

        verify(auditService, never()).getAdmissionRevisionsByUser(any());
        verify(auditService, never()).getEpisodeRevisionsByUser(any());
    }

    @Test
    @DisplayName("Should return 200 with empty list when no revisions match the user")
    void getRevisionsByUser_WhenNoRevisionsMatchUser_ShouldReturn200WithEmptyList() throws Exception {
        when(auditService.getAdmissionRevisionsByUser(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/audit/revisions")
                        .param("userId", userId.toString())
                        .param("entityType", "admissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
