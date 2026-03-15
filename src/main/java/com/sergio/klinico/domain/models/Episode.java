package com.sergio.klinico.domain.models;

import com.sergio.klinico.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Episode {
    private Long version;

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

    private void validateBraden(Integer braden) {
        if(braden == null) return;

        if (braden < 6 || braden > 23) {
            throw new BusinessException("La escala de Braden debe estar entre 6 y 23");
        }
    }

    private void validateChads2(Integer chads2) {
        if(chads2 == null) return;

        if (chads2 < 0 || chads2 > 9) {
            throw new BusinessException("La escala CHADS2 debe estar entre 0 y 9");
        }
    }

    public void validateUpdate(UUID doctorIdAttemptingUpdate) {
        if (!this.doctorId.equals(doctorIdAttemptingUpdate)) {
            throw new BusinessException("Solo el médico que creó el episodio puede modificarlo.");
        }

        long minutesSinceCreation = ChronoUnit.MINUTES.between(getCreatedAt() != null ? getCreatedAt() : LocalDateTime.now(), LocalDateTime.now());

        if (minutesSinceCreation > 120) {
            throw new BusinessException("El plazo máximo de 2 horas para modificar el episodio ha expirado");
        }
    }

    public void updateClinicalData(Episode updates) {
        validateBraden(updates.getBradenScore());
        validateChads2(updates.getChads2Score());

        if(updates.getBradenScore() != null) this.bradenScore = updates.getBradenScore();
        if(updates.getChads2Score() != null) this.chads2Score = updates.getChads2Score();
        if (updates.getClinicalProgress() != null) this.clinicalProgress = updates.getClinicalProgress();
        if (updates.getDiagnosis() != null) this.diagnosis = updates.getDiagnosis();
        if (updates.getCamScore() != null) this.camScore = updates.getCamScore();
    }
}