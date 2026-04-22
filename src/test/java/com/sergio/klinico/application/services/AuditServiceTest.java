package com.sergio.klinico.application.services;

import com.sergio.klinico.infrastructure.persistence.AdmissionEntity;
import com.sergio.klinico.infrastructure.persistence.EpisodeEntity;
import com.sergio.klinico.infrastructure.rest.dto.responses.audit.RevisionResponse;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.AuditQueryCreator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService Tests")
class AuditServiceTest {

    @InjectMocks
    private AuditService auditService;

    @Mock
    private EntityManager entityManager;

    @Test
    @DisplayName("Should return admission revision list when revisions exist")
    void getAdmissionRevisions_WhenRevisionsExist_ShouldReturnRevisionList() {
        UUID admissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AdmissionEntity entity = new AdmissionEntity();
        entity.setAdmissionId(admissionId);
        entity.setCreatedBy(userId);

        DefaultRevisionEntity revisionEntity = mock(DefaultRevisionEntity.class);
        when(revisionEntity.getId()).thenReturn(1);
        when(revisionEntity.getTimestamp()).thenReturn(System.currentTimeMillis());

        Object[] resultEntry = { entity, revisionEntity, RevisionType.ADD };

        AuditReader auditReader = mock(AuditReader.class);
        AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);
        AuditQuery auditQuery = mock(AuditQuery.class);

        when(auditReader.createQuery()).thenReturn(queryCreator);
        when(queryCreator.forRevisionsOfEntity(eq(AdmissionEntity.class), anyBoolean(), anyBoolean()))
                .thenReturn(auditQuery);
        when(auditQuery.add(any())).thenReturn(auditQuery);
        when(auditQuery.addOrder(any())).thenReturn(auditQuery);
        when(auditQuery.getResultList()).thenReturn(Collections.singletonList(resultEntry));

        try (MockedStatic<AuditReaderFactory> mockedFactory = Mockito.mockStatic(AuditReaderFactory.class)) {
            mockedFactory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

            List<RevisionResponse.AdmissionRevisionResponse> result =
                    auditService.getAdmissionRevisions(admissionId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1, result.getFirst().getRevisionNumber());
            assertEquals("ADD", result.getFirst().getRevisionType());
        }
    }

    @Test
    @DisplayName("Should return empty list when no admission revisions exist")
    void getAdmissionRevisions_WhenNoRevisions_ShouldReturnEmptyList() {
        UUID admissionId = UUID.randomUUID();

        AuditReader auditReader = mock(AuditReader.class);
        AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);
        AuditQuery auditQuery = mock(AuditQuery.class);

        when(auditReader.createQuery()).thenReturn(queryCreator);
        when(queryCreator.forRevisionsOfEntity(eq(AdmissionEntity.class), anyBoolean(), anyBoolean()))
                .thenReturn(auditQuery);
        when(auditQuery.add(any())).thenReturn(auditQuery);
        when(auditQuery.addOrder(any())).thenReturn(auditQuery);
        when(auditQuery.getResultList()).thenReturn(List.of());

        try (MockedStatic<AuditReaderFactory> mockedFactory = Mockito.mockStatic(AuditReaderFactory.class)) {
            mockedFactory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

            List<RevisionResponse.AdmissionRevisionResponse> result =
                    auditService.getAdmissionRevisions(admissionId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    @DisplayName("Should skip null admission entities in revision results")
    void getAdmissionRevisions_WhenEntityIsNull_ShouldSkipNullEntries() {
        UUID admissionId = UUID.randomUUID();

        DefaultRevisionEntity revisionEntity = mock(DefaultRevisionEntity.class);
        Object[] nullEntry = { null, revisionEntity, RevisionType.MOD };

        AuditReader auditReader = mock(AuditReader.class);
        AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);
        AuditQuery auditQuery = mock(AuditQuery.class);

        when(auditReader.createQuery()).thenReturn(queryCreator);
        when(queryCreator.forRevisionsOfEntity(eq(AdmissionEntity.class), anyBoolean(), anyBoolean()))
                .thenReturn(auditQuery);
        when(auditQuery.add(any())).thenReturn(auditQuery);
        when(auditQuery.addOrder(any())).thenReturn(auditQuery);
        List<Object[]> nullResultList = new ArrayList<>();
        nullResultList.add(nullEntry);
        when(auditQuery.getResultList()).thenReturn(nullResultList);

        try (MockedStatic<AuditReaderFactory> mockedFactory = Mockito.mockStatic(AuditReaderFactory.class)) {
            mockedFactory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

            List<RevisionResponse.AdmissionRevisionResponse> result =
                    auditService.getAdmissionRevisions(admissionId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    @DisplayName("Should return episode revision list when revisions exist")
    void getEpisodeRevisions_WhenRevisionsExist_ShouldReturnRevisionList() {
        UUID episodeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID linkedAdmissionId = UUID.randomUUID();

        AdmissionEntity admissionEntity = new AdmissionEntity();
        admissionEntity.setAdmissionId(linkedAdmissionId);

        EpisodeEntity entity = new EpisodeEntity();
        entity.setEpisodeId(episodeId);
        entity.setCreatedBy(userId);
        entity.setAdmission(admissionEntity);

        DefaultRevisionEntity revisionEntity = mock(DefaultRevisionEntity.class);
        when(revisionEntity.getId()).thenReturn(2);
        when(revisionEntity.getTimestamp()).thenReturn(System.currentTimeMillis());

        Object[] resultEntry = { entity, revisionEntity, RevisionType.MOD };

        AuditReader auditReader = mock(AuditReader.class);
        AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);
        AuditQuery auditQuery = mock(AuditQuery.class);

        when(auditReader.createQuery()).thenReturn(queryCreator);
        when(queryCreator.forRevisionsOfEntity(eq(EpisodeEntity.class), anyBoolean(), anyBoolean()))
                .thenReturn(auditQuery);
        when(auditQuery.add(any())).thenReturn(auditQuery);
        when(auditQuery.addOrder(any())).thenReturn(auditQuery);
        when(auditQuery.getResultList()).thenReturn(Collections.singletonList(resultEntry));

        try (MockedStatic<AuditReaderFactory> mockedFactory = Mockito.mockStatic(AuditReaderFactory.class)) {
            mockedFactory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

            List<RevisionResponse.EpisodeRevisionResponse> result =
                    auditService.getEpisodeRevisions(episodeId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(2, result.get(0).getRevisionNumber());
            assertEquals("MOD", result.get(0).getRevisionType());
        }
    }

    @Test
    @DisplayName("Should return empty list when no episode revisions exist")
    void getEpisodeRevisions_WhenNoRevisions_ShouldReturnEmptyList() {
        UUID episodeId = UUID.randomUUID();

        AuditReader auditReader = mock(AuditReader.class);
        AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);
        AuditQuery auditQuery = mock(AuditQuery.class);

        when(auditReader.createQuery()).thenReturn(queryCreator);
        when(queryCreator.forRevisionsOfEntity(eq(EpisodeEntity.class), anyBoolean(), anyBoolean()))
                .thenReturn(auditQuery);
        when(auditQuery.add(any())).thenReturn(auditQuery);
        when(auditQuery.addOrder(any())).thenReturn(auditQuery);
        when(auditQuery.getResultList()).thenReturn(List.of());

        try (MockedStatic<AuditReaderFactory> mockedFactory = Mockito.mockStatic(AuditReaderFactory.class)) {
            mockedFactory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

            List<RevisionResponse.EpisodeRevisionResponse> result =
                    auditService.getEpisodeRevisions(episodeId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    @DisplayName("Should return admission revisions matching the given userId")
    void getAdmissionRevisionsByUser_WhenMatchingRevisionsExist_ShouldReturnFilteredList() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID admissionId = UUID.randomUUID();

        AdmissionEntity matchingEntity = new AdmissionEntity();
        matchingEntity.setAdmissionId(admissionId);
        matchingEntity.setCreatedBy(userId);

        AdmissionEntity nonMatchingEntity = new AdmissionEntity();
        nonMatchingEntity.setAdmissionId(UUID.randomUUID());
        nonMatchingEntity.setCreatedBy(otherUserId);

        DefaultRevisionEntity revisionEntity1 = mock(DefaultRevisionEntity.class);
        when(revisionEntity1.getId()).thenReturn(1);
        when(revisionEntity1.getTimestamp()).thenReturn(System.currentTimeMillis());

        DefaultRevisionEntity revisionEntity2 = mock(DefaultRevisionEntity.class);

        Object[] matchingEntry = { matchingEntity, revisionEntity1, RevisionType.ADD };
        Object[] nonMatchingEntry = { nonMatchingEntity, revisionEntity2, RevisionType.ADD };

        AuditReader auditReader = mock(AuditReader.class);
        AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);
        AuditQuery auditQuery = mock(AuditQuery.class);

        when(auditReader.createQuery()).thenReturn(queryCreator);
        when(queryCreator.forRevisionsOfEntity(eq(AdmissionEntity.class), anyBoolean(), anyBoolean()))
                .thenReturn(auditQuery);
        when(auditQuery.addOrder(any())).thenReturn(auditQuery);
        when(auditQuery.getResultList()).thenReturn(List.of(matchingEntry, nonMatchingEntry));

        try (MockedStatic<AuditReaderFactory> mockedFactory = Mockito.mockStatic(AuditReaderFactory.class)) {
            mockedFactory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

            List<RevisionResponse.AdmissionRevisionResponse> result =
                    auditService.getAdmissionRevisionsByUser(userId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(userId, result.get(0).getUserId());
        }
    }

    @Test
    @DisplayName("Should return empty list when no admission revisions match the userId")
    void getAdmissionRevisionsByUser_WhenNoMatchingRevisions_ShouldReturnEmptyList() {
        UUID userId = UUID.randomUUID();

        AuditReader auditReader = mock(AuditReader.class);
        AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);
        AuditQuery auditQuery = mock(AuditQuery.class);

        when(auditReader.createQuery()).thenReturn(queryCreator);
        when(queryCreator.forRevisionsOfEntity(eq(AdmissionEntity.class), anyBoolean(), anyBoolean()))
                .thenReturn(auditQuery);
        when(auditQuery.addOrder(any())).thenReturn(auditQuery);
        when(auditQuery.getResultList()).thenReturn(List.of());

        try (MockedStatic<AuditReaderFactory> mockedFactory = Mockito.mockStatic(AuditReaderFactory.class)) {
            mockedFactory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

            List<RevisionResponse.AdmissionRevisionResponse> result =
                    auditService.getAdmissionRevisionsByUser(userId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    @DisplayName("Should return episode revisions matching the given userId")
    void getEpisodeRevisionsByUser_WhenMatchingRevisionsExist_ShouldReturnFilteredList() {
        UUID userId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        UUID linkedAdmissionId = UUID.randomUUID();

        AdmissionEntity admissionEntity = new AdmissionEntity();
        admissionEntity.setAdmissionId(linkedAdmissionId);

        EpisodeEntity matchingEntity = new EpisodeEntity();
        matchingEntity.setEpisodeId(episodeId);
        matchingEntity.setCreatedBy(userId);
        matchingEntity.setAdmission(admissionEntity);

        EpisodeEntity entityWithNullCreatedBy = new EpisodeEntity();
        entityWithNullCreatedBy.setEpisodeId(UUID.randomUUID());
        entityWithNullCreatedBy.setCreatedBy(null);

        DefaultRevisionEntity revisionEntity = mock(DefaultRevisionEntity.class);
        when(revisionEntity.getId()).thenReturn(3);
        when(revisionEntity.getTimestamp()).thenReturn(System.currentTimeMillis());

        DefaultRevisionEntity revisionEntity2 = mock(DefaultRevisionEntity.class);

        Object[] matchingEntry = { matchingEntity, revisionEntity, RevisionType.ADD };
        Object[] nullCreatedByEntry = { entityWithNullCreatedBy, revisionEntity2, RevisionType.ADD };

        AuditReader auditReader = mock(AuditReader.class);
        AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);
        AuditQuery auditQuery = mock(AuditQuery.class);

        when(auditReader.createQuery()).thenReturn(queryCreator);
        when(queryCreator.forRevisionsOfEntity(eq(EpisodeEntity.class), anyBoolean(), anyBoolean()))
                .thenReturn(auditQuery);
        when(auditQuery.addOrder(any())).thenReturn(auditQuery);
        when(auditQuery.getResultList()).thenReturn(List.of(matchingEntry, nullCreatedByEntry));

        try (MockedStatic<AuditReaderFactory> mockedFactory = Mockito.mockStatic(AuditReaderFactory.class)) {
            mockedFactory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

            List<RevisionResponse.EpisodeRevisionResponse> result =
                    auditService.getEpisodeRevisionsByUser(userId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(userId, result.get(0).getUserId());
        }
    }

    @Test
    @DisplayName("Should return empty list when no episode revisions match the userId")
    void getEpisodeRevisionsByUser_WhenNoMatchingRevisions_ShouldReturnEmptyList() {
        UUID userId = UUID.randomUUID();

        AuditReader auditReader = mock(AuditReader.class);
        AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);
        AuditQuery auditQuery = mock(AuditQuery.class);

        when(auditReader.createQuery()).thenReturn(queryCreator);
        when(queryCreator.forRevisionsOfEntity(eq(EpisodeEntity.class), anyBoolean(), anyBoolean()))
                .thenReturn(auditQuery);
        when(auditQuery.addOrder(any())).thenReturn(auditQuery);
        when(auditQuery.getResultList()).thenReturn(List.of());

        try (MockedStatic<AuditReaderFactory> mockedFactory = Mockito.mockStatic(AuditReaderFactory.class)) {
            mockedFactory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

            List<RevisionResponse.EpisodeRevisionResponse> result =
                    auditService.getEpisodeRevisionsByUser(userId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}
