package com.sergio.klinico.infrastructure.persistence.repositories;

import com.sergio.klinico.infrastructure.persistence.EpisodeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaEpisodeRepository extends JpaRepository<EpisodeEntity, UUID> {
    Page<EpisodeEntity> findByAdmission_AdmissionId(UUID admissionId, Pageable pageable);
    @Query("SELECT e FROM EpisodeEntity e WHERE CAST(e.createdAt AS date) = :date AND e.admission.id = :admissionId")
    List<EpisodeEntity> findAllByCreatedAtDate(@Param("admissionId") UUID admissionId, @Param("date") LocalDate date);
}
