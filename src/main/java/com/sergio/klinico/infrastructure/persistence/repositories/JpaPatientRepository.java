package com.sergio.klinico.infrastructure.persistence.repositories;

import com.sergio.klinico.domain.models.enums.PatientStatus;
import com.sergio.klinico.infrastructure.persistence.PatientEntity;
import com.sergio.klinico.infrastructure.persistence.repositories.projections.MonthlyCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaPatientRepository extends JpaRepository<PatientEntity, UUID> {
    boolean existsByDni(String dni);
    Page<PatientEntity> findBySurnameContainingIgnoreCaseAndStatus(String surname, PatientStatus status, Pageable pageable);

    // KPI: Éxitus por servicio agrupados por mes (año completo).
    // Usamos modified_at como fecha de éxitus al no existir campo dedicado todavía.
    @Query(value = """
            SELECT EXTRACT(MONTH FROM p.modified_at)::int AS month,
                   COUNT(DISTINCT p.patient_id) AS count
            FROM patients p
            JOIN admissions a ON a.patient_id = p.patient_id
            WHERE p.status = 'EXITUS'
              AND a.service_id = :serviceId
              AND EXTRACT(YEAR FROM p.modified_at) = :year
            GROUP BY EXTRACT(MONTH FROM p.modified_at)
            ORDER BY month
            """, nativeQuery = true)
    List<MonthlyCountProjection> countExitusByServiceAndYear(
            @Param("serviceId") UUID serviceId,
            @Param("year") int year);

    // KPI: Éxitus por servicio para un mes concreto
    @Query(value = """
            SELECT EXTRACT(MONTH FROM p.modified_at)::int AS month,
                   COUNT(DISTINCT p.patient_id) AS count
            FROM patients p
            JOIN admissions a ON a.patient_id = p.patient_id
            WHERE p.status = 'EXITUS'
              AND a.service_id = :serviceId
              AND EXTRACT(YEAR FROM p.modified_at) = :year
              AND EXTRACT(MONTH FROM p.modified_at) = :month
            GROUP BY EXTRACT(MONTH FROM p.modified_at)
            """, nativeQuery = true)
    List<MonthlyCountProjection> countExitusByServiceAndYearAndMonth(
            @Param("serviceId") UUID serviceId,
            @Param("year") int year,
            @Param("month") int month);
}
