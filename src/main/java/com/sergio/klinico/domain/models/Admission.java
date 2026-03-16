package com.sergio.klinico.domain.models;

import com.sergio.klinico.domain.exceptions.BusinessException;
import lombok.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admission {
    private Long version;

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

    private List<Episode> episodes;

    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime lastModifiedAt;
    private UUID lastModifiedBy;

    private void validateBarthel(Integer score) {
        if (score != null && (score < 0 || score > 100)) {
            throw new BusinessException("El índice de Barthel debe estar comprendido entre 0 y 100");
        }
    }

    public void assignRoom(Integer roomNumber) {
        if (this.isDischarged()) {
            throw new BusinessException("No se puede asignar habitación a una admisión con alta médica.");
        }
        if (this.roomNumber != null) {
            throw new BusinessException("La admisión ya tiene una habitación asignada.");
        }
        this.roomNumber = roomNumber;
    }

    public boolean isDischarged() {
        return this.dischargeDate != null;
    }

    public void processDischarge() {
        if (this.dischargeDate != null) {
            throw new BusinessException("La admisión ya tiene fecha de alta.");
        }
        this.dischargeDate = LocalDateTime.now();
    }

    public void addEpisode(Episode episode) {
        if (this.isDischarged()) {
            throw new BusinessException("No se pueden añadir episodios a una admisión que ya tiene el alta médica");
        }

        if (this.roomNumber == null) {
            throw new BusinessException("No se puede generar un episodio si el paciente aún no tiene habitación asignada");
        }

        episode.setAdmissionId(this.admissionId);
    }

    public void updateClinicalInformation(Admission updates) {
        if (this.isDischarged()) {
            throw new BusinessException("No se puede modificar la información clínica de una admisión ya dada de alta");
        }

        if (updates.getPrincipalDiagnosis() != null) this.principalDiagnosis = updates.getPrincipalDiagnosis();
        if (updates.getMedicalHistory() != null) this.medicalHistory = updates.getMedicalHistory();
        if (updates.getAllergies() != null) this.allergies = updates.getAllergies();
        if (updates.getChronicTreatment() != null) this.chronicTreatment = updates.getChronicTreatment();
        if (updates.getBasalBarthel() != null) {
            validateBarthel(updates.getBasalBarthel());
            this.basalBarthel = updates.getBasalBarthel();
        }
    }

    public int getLiveHospitalizationLength() {
        if (getCreatedAt() == null) return 0;

        if (this.hospitalizationLength != null) {
            return this.hospitalizationLength;
        }

        long duration = ChronoUnit.DAYS.between(getCreatedAt(), LocalDateTime.now());

        return (int) duration;
    }
}