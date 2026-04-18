package com.sergio.klinico.application.services;

import com.sergio.klinico.infrastructure.persistence.AdmissionEntity;
import com.sergio.klinico.infrastructure.persistence.EpisodeEntity;
import com.sergio.klinico.infrastructure.rest.dto.responses.audit.RevisionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<RevisionResponse.AdmissionRevisionResponse> getAdmissionRevisions(UUID admissionId) {
        log.info("Obteniendo historial de revisiones para admissionId: {}", admissionId);

        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        
        // Usar AuditQuery para obtener revisiones con tipo de revisión
        AuditQuery query = auditReader.createQuery()
            .forRevisionsOfEntity(AdmissionEntity.class, false, true)
            .add(AuditEntity.id().eq(admissionId))
            .addOrder(AuditEntity.revisionNumber().asc());
        
        List<Object[]> results = query.getResultList();
        List<RevisionResponse.AdmissionRevisionResponse> responseList = new ArrayList<>();

        for (Object[] result : results) {
            AdmissionEntity entity = (AdmissionEntity) result[0];
            if (entity != null) {
                RevisionType revisionType = (RevisionType) result[2];
                org.hibernate.envers.DefaultRevisionEntity revisionEntity = (org.hibernate.envers.DefaultRevisionEntity) result[1];
                Number revisionNumber = revisionEntity.getId();

                // Obtener timestamp de la revisión
                Long timestamp = revisionEntity.getTimestamp();

                LocalDateTime revisionDateTime = timestamp != null 
                    ? LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                    : null;

                RevisionResponse.AdmissionRevisionResponse response = RevisionResponse.AdmissionRevisionResponse.builder()
                    .revisionNumber(revisionNumber.intValue())
                    .revisionTimestamp(revisionDateTime)
                    .revisionType(revisionType != null ? revisionType.name() : "UNKNOWN")
                    .userId(entity.getCreatedBy())
                    .admissionId(entity.getAdmissionId())
                    .patientId(entity.getPatientId())
                    .serviceId(entity.getServiceId())
                    .assignedDoctorId(entity.getAssignedDoctorId())
                    .dischargeDate(entity.getDischargeDate())
                    .hospitalizationLength(entity.getHospitalizationLength())
                    .principalDiagnosis(entity.getPrincipalDiagnosis())
                    .medicalHistory(entity.getMedicalHistory())
                    .allergies(entity.getAllergies())
                    .chronicTreatment(entity.getChronicTreatment())
                    .basalBarthel(entity.getBasalBarthel())
                    .roomNumber(entity.getRoomNumber())
                    .createdAt(entity.getCreatedAt())
                    .createdBy(entity.getCreatedBy())
                    .lastModifiedAt(entity.getLastModifiedAt())
                    .lastModifiedBy(entity.getLastModifiedBy())
                    .build();

                responseList.add(response);
            }
        }

        log.info("Se encontraron {} revisiones para admissionId: {}", responseList.size(), admissionId);
        return responseList;
    }

    public List<RevisionResponse.EpisodeRevisionResponse> getEpisodeRevisions(UUID episodeId) {
        log.info("Obteniendo historial de revisiones para episodeId: {}", episodeId);

        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        
        // Usar AuditQuery para obtener revisiones con tipo de revisión
        AuditQuery query = auditReader.createQuery()
            .forRevisionsOfEntity(EpisodeEntity.class, false, true)
            .add(AuditEntity.id().eq(episodeId))
            .addOrder(AuditEntity.revisionNumber().asc());
        
        List<Object[]> results = query.getResultList();
        List<RevisionResponse.EpisodeRevisionResponse> responseList = new ArrayList<>();

        for (Object[] result : results) {
            EpisodeEntity entity = (EpisodeEntity) result[0];
            if (entity != null) {
                RevisionType revisionType = (RevisionType) result[2];
                org.hibernate.envers.DefaultRevisionEntity revisionEntity = (org.hibernate.envers.DefaultRevisionEntity) result[1];
                Number revisionNumber = revisionEntity.getId();

                // Obtener timestamp de la revisión
                Long timestamp = revisionEntity.getTimestamp();

                LocalDateTime revisionDateTime = timestamp != null 
                    ? LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                    : null;

                RevisionResponse.EpisodeRevisionResponse response = RevisionResponse.EpisodeRevisionResponse.builder()
                    .revisionNumber(revisionNumber.intValue())
                    .revisionTimestamp(revisionDateTime)
                    .revisionType(revisionType != null ? revisionType.name() : "UNKNOWN")
                    .userId(entity.getCreatedBy())
                    .episodeId(entity.getEpisodeId())
                    .admissionId(entity.getAdmission().getAdmissionId())
                    .doctorId(entity.getDoctorId())
                    .clinicalProgress(entity.getClinicalProgress())
                    .diagnosis(entity.getDiagnosis())
                    .bradenScore(entity.getBradenScore())
                    .camScore(entity.getCamScore())
                    .chads2Score(entity.getChads2Score())
                    .createdAt(entity.getCreatedAt())
                    .createdBy(entity.getCreatedBy())
                    .lastModifiedAt(entity.getLastModifiedAt())
                    .lastModifiedBy(entity.getLastModifiedBy())
                    .build();

                responseList.add(response);
            }
        }

        log.info("Se encontraron {} revisiones para episodeId: {}", responseList.size(), episodeId);
        return responseList;
    }

    public List<RevisionResponse.AdmissionRevisionResponse> getAdmissionRevisionsByUser(UUID userId) {
        log.info("Obteniendo revisiones de admissions por userId: {}", userId);

        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        
        // Usar AuditQuery para obtener todas las revisiones de AdmissionEntity
        AuditQuery query = auditReader.createQuery()
            .forRevisionsOfEntity(AdmissionEntity.class, false, true)
            .addOrder(AuditEntity.revisionNumber().asc());
        
        List<Object[]> results = query.getResultList();
        List<RevisionResponse.AdmissionRevisionResponse> responseList = new ArrayList<>();

        for (Object[] result : results) {
            AdmissionEntity entity = (AdmissionEntity) result[0];
            if (entity != null && entity.getCreatedBy() != null && entity.getCreatedBy().equals(userId)) {
                RevisionType revisionType = (RevisionType) result[2];
                org.hibernate.envers.DefaultRevisionEntity revisionEntity = (org.hibernate.envers.DefaultRevisionEntity) result[1];
                Number revisionNumber = revisionEntity.getId();

                // Obtener timestamp de la revisión
                Long timestamp = revisionEntity.getTimestamp();

                LocalDateTime revisionDateTime = timestamp != null ? LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()) : null;

                RevisionResponse.AdmissionRevisionResponse response = RevisionResponse.AdmissionRevisionResponse.builder().revisionNumber(revisionNumber.intValue()).revisionTimestamp(revisionDateTime)
                        .revisionType(revisionType != null ? revisionType.name() : "UNKNOWN").userId(entity.getCreatedBy()).admissionId(entity.getAdmissionId()).patientId(entity.getPatientId()).serviceId(entity.getServiceId())
                        .assignedDoctorId(entity.getAssignedDoctorId()).dischargeDate(entity.getDischargeDate()).hospitalizationLength(entity.getHospitalizationLength()).principalDiagnosis(entity.getPrincipalDiagnosis())
                        .medicalHistory(entity.getMedicalHistory()).allergies(entity.getAllergies()).chronicTreatment(entity.getChronicTreatment()).basalBarthel(entity.getBasalBarthel()).roomNumber(entity.getRoomNumber())
                        .createdAt(entity.getCreatedAt()).createdBy(entity.getCreatedBy()).lastModifiedAt(entity.getLastModifiedAt()).lastModifiedBy(entity.getLastModifiedBy()).build();

                responseList.add(response);
            }
        }

        log.info("Se encontraron {} revisiones de admissions para userId: {}", responseList.size(), userId);
        return responseList;
    }

    public List<RevisionResponse.EpisodeRevisionResponse> getEpisodeRevisionsByUser(UUID userId) {
        log.info("Obteniendo revisiones de episodes por userId: {}", userId);

        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        
        // Usar AuditQuery para obtener todas las revisiones de EpisodeEntity
        AuditQuery query = auditReader.createQuery()
            .forRevisionsOfEntity(EpisodeEntity.class, false, true)
            .addOrder(AuditEntity.revisionNumber().asc());
        
        List<Object[]> results = query.getResultList();
        List<RevisionResponse.EpisodeRevisionResponse> responseList = new ArrayList<>();

        for (Object[] result : results) {
            EpisodeEntity entity = (EpisodeEntity) result[0];
            if (entity != null) {
                if (entity.getCreatedBy() == null) {
                    log.warn("Episode con episodeId {} tiene createdBy null en revisión", entity.getEpisodeId());
                } else if (!entity.getCreatedBy().equals(userId)) {
                    log.debug("Episode con episodeId {} tiene createdBy {} diferente de {}", entity.getEpisodeId(), entity.getCreatedBy(), userId);
                } else {
                    RevisionType revisionType = (RevisionType) result[2];
                    org.hibernate.envers.DefaultRevisionEntity revisionEntity = (org.hibernate.envers.DefaultRevisionEntity) result[1];
                    Number revisionNumber = revisionEntity.getId();

                    // Obtener timestamp de la revisión
                    Long timestamp = revisionEntity.getTimestamp();

                    LocalDateTime revisionDateTime = timestamp != null
                        ? LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                        : null;

                        RevisionResponse.EpisodeRevisionResponse response = RevisionResponse.EpisodeRevisionResponse.builder()
                            .revisionNumber(revisionNumber.intValue())
                            .revisionTimestamp(revisionDateTime)
                            .revisionType(revisionType != null ? revisionType.name() : "UNKNOWN")
                            .userId(entity.getCreatedBy())
                            .episodeId(entity.getEpisodeId())
                            .admissionId(entity.getAdmission() != null ? entity.getAdmission().getAdmissionId() : null)
                            .doctorId(entity.getDoctorId())
                            .clinicalProgress(entity.getClinicalProgress())
                            .diagnosis(entity.getDiagnosis())
                            .bradenScore(entity.getBradenScore())
                            .camScore(entity.getCamScore())
                            .chads2Score(entity.getChads2Score())
                            .createdAt(entity.getCreatedAt())
                            .createdBy(entity.getCreatedBy())
                            .lastModifiedAt(entity.getLastModifiedAt())
                            .lastModifiedBy(entity.getLastModifiedBy())
                            .build();

                        responseList.add(response);
                }
            }
        }

        log.info("Se encontraron {} revisiones de episodes para userId: {}", responseList.size(), userId);
        return responseList;
    }
}
