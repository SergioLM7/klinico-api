package com.sergio.klinico.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "episodes")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EpisodeEntity extends AuditableEntity {

    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "episode_id")
    private UUID episodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id", nullable = false)
    private AdmissionEntity admission;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(name = "clinical_progress", columnDefinition = "TEXT")
    private String clinicalProgress;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "braden_score")
    private Integer bradenScore;

    @Column(name = "cam_score")
    private Boolean camScore;

    @Column(name = "chads2_score")
    private Integer chads2Score;
}
