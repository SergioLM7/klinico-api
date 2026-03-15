package com.sergio.klinico.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "admissions")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AdmissionEntity extends AuditableEntity {

    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "admission_id")
    private UUID admissionId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "assigned_doctor_id", nullable = false)
    private UUID assignedDoctorId;

    @Column(name = "discharge_date")
    private LocalDateTime dischargeDate;

    @Column(name = "hospitalization_length", insertable = false, updatable = false)
    private Integer hospitalizationLength;

    @Column(name = "room_number")
    private Integer roomNumber;

    @Column(name = "principal_diagnosis", columnDefinition = "TEXT")
    private String principalDiagnosis;

    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "chronic_treatment", columnDefinition = "TEXT")
    private String chronicTreatment;

    @Column(name = "basal_barthel")
    private Integer basalBarthel;

    @OneToMany(mappedBy = "admission", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EpisodeEntity> episodes = new ArrayList<>();
}