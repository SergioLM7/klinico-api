package com.sergio.klinico.infrastructure.rest.dto.responses.audit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RevisionResponse {
    private Integer revisionNumber;
    private LocalDateTime revisionTimestamp;
    private String revisionType;
    private UUID userId;
    
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdmissionRevisionResponse extends RevisionResponse {
        private UUID admissionId;
        private UUID patientId;
        private UUID serviceId;
        private UUID assignedDoctorId;
        private LocalDateTime dischargeDate;
        private Integer hospitalizationLength;
        private String principalDiagnosis;
        private String medicalHistory;
        private String allergies;
        private String chronicTreatment;
        private Integer basalBarthel;
        private Integer roomNumber;
        private LocalDateTime createdAt;
        private UUID createdBy;
        private LocalDateTime lastModifiedAt;
        private UUID lastModifiedBy;
    }
    
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EpisodeRevisionResponse extends RevisionResponse {
        private UUID episodeId;
        private UUID admissionId;
        private UUID doctorId;
        private String clinicalProgress;
        private String diagnosis;
        private Integer bradenScore;
        private Boolean camScore;
        private Integer chads2Score;
        private LocalDateTime createdAt;
        private UUID createdBy;
        private LocalDateTime lastModifiedAt;
        private UUID lastModifiedBy;
    }
}
