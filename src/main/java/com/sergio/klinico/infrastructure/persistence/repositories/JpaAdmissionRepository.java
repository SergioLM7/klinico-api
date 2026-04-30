package com.sergio.klinico.infrastructure.persistence.repositories;

import com.sergio.klinico.infrastructure.persistence.AdmissionEntity;
import com.sergio.klinico.infrastructure.persistence.repositories.projections.DoctorMonthlyAvgProjection;
import com.sergio.klinico.infrastructure.persistence.repositories.projections.DoctorMonthlyCountProjection;
import com.sergio.klinico.infrastructure.persistence.repositories.projections.MonthlyAvgProjection;
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
public interface JpaAdmissionRepository extends JpaRepository<AdmissionEntity, UUID> {
    boolean existsByPatientIdAndDischargeDateIsNull(UUID patientId);
    Page<AdmissionEntity> findByDischargeDateIsNull(Pageable pageable);

    // KPI: Estancia media global (sin filtro temporal). Filtramos por las que han sido dadas de alta (dischargeDate not null)
    @Query("SELECT AVG(a.hospitalizationLength) FROM AdmissionEntity a " +
            "WHERE a.serviceId = :serviceId AND a.dischargeDate IS NOT NULL")
    Double getAverageLengthByService(@Param("serviceId") UUID serviceId);

    // Búsqueda paginada para Médicos
    Page<AdmissionEntity> findByAssignedDoctorIdAndDischargeDateIsNull(UUID doctorId, Pageable pageable);

    // Búsqueda paginada para Jefes de Servicio
    Page<AdmissionEntity> findByServiceIdAndDischargeDateIsNull(UUID serviceId, Pageable pageable);

    // Búsqueda por apellido de paciente y serviceId con join a patients
    @Query(value = "SELECT a.* FROM admissions a JOIN patients p ON a.patient_id = p.patient_id " +
            "WHERE LOWER(p.surname) LIKE LOWER(CONCAT('%', :surname, '%')) " +
            "AND a.service_id = :serviceId " +
            "AND a.discharge_date IS NULL", nativeQuery = true)
    Page<AdmissionEntity> findByPatientSurnameContainingIgnoreCaseAndServiceIdAndDischargeDateIsNull(
            @Param("surname") String surname,
            @Param("serviceId") UUID serviceId,
            Pageable pageable);

    // KPI: Ingresos por servicio agrupados por mes (año completo)
    @Query(value = """
            SELECT EXTRACT(MONTH FROM a.created_at)::int AS month,
                   COUNT(a.admission_id) AS count
            FROM admissions a
            WHERE a.service_id = :serviceId
              AND EXTRACT(YEAR FROM a.created_at) = :year
            GROUP BY EXTRACT(MONTH FROM a.created_at)
            ORDER BY month
            """, nativeQuery = true)
    List<MonthlyCountProjection> countAdmissionsByServiceAndYear(
            @Param("serviceId") UUID serviceId,
            @Param("year") int year);

    // KPI: Ingresos por servicio para un mes concreto
    @Query(value = """
            SELECT EXTRACT(MONTH FROM a.created_at)::int AS month,
                   COUNT(a.admission_id) AS count
            FROM admissions a
            WHERE a.service_id = :serviceId
              AND EXTRACT(YEAR FROM a.created_at) = :year
              AND EXTRACT(MONTH FROM a.created_at) = :month
            GROUP BY EXTRACT(MONTH FROM a.created_at)
            """, nativeQuery = true)
    List<MonthlyCountProjection> countAdmissionsByServiceAndYearAndMonth(
            @Param("serviceId") UUID serviceId,
            @Param("year") int year,
            @Param("month") int month);

    // KPI: Ingresos por médico agrupados por mes (año completo)
    @Query(value = """
            SELECT u.user_id::text AS doctor_id,
                   u.name         AS doctor_name,
                   u.surname      AS doctor_surname,
                   EXTRACT(MONTH FROM a.created_at)::int AS month,
                   COUNT(a.admission_id) AS count
            FROM admissions a
            JOIN users u ON u.user_id = a.assigned_doctor_id
            WHERE a.service_id = :serviceId
              AND EXTRACT(YEAR FROM a.created_at) = :year
            GROUP BY u.user_id, u.name, u.surname, EXTRACT(MONTH FROM a.created_at)
            ORDER BY u.surname, month
            """, nativeQuery = true)
    List<DoctorMonthlyCountProjection> countAdmissionsByDoctorAndServiceAndYear(
            @Param("serviceId") UUID serviceId,
            @Param("year") int year);

    // KPI: Ingresos por médico para un mes concreto
    @Query(value = """
            SELECT u.user_id::text AS doctor_id,
                   u.name         AS doctor_name,
                   u.surname      AS doctor_surname,
                   EXTRACT(MONTH FROM a.created_at)::int AS month,
                   COUNT(a.admission_id) AS count
            FROM admissions a
            JOIN users u ON u.user_id = a.assigned_doctor_id
            WHERE a.service_id = :serviceId
              AND EXTRACT(YEAR FROM a.created_at) = :year
              AND EXTRACT(MONTH FROM a.created_at) = :month
            GROUP BY u.user_id, u.name, u.surname, EXTRACT(MONTH FROM a.created_at)
            ORDER BY u.surname
            """, nativeQuery = true)
    List<DoctorMonthlyCountProjection> countAdmissionsByDoctorAndServiceAndYearAndMonth(
            @Param("serviceId") UUID serviceId,
            @Param("year") int year,
            @Param("month") int month);

    // KPI: Estancia media por servicio agrupada por mes (año completo, solo altas)
    @Query(value = """
            SELECT EXTRACT(MONTH FROM a.discharge_date)::int AS month,
                   AVG(a.hospitalization_length) AS avg_days
            FROM admissions a
            WHERE a.service_id = :serviceId
              AND a.discharge_date IS NOT NULL
              AND EXTRACT(YEAR FROM a.discharge_date) = :year
            GROUP BY EXTRACT(MONTH FROM a.discharge_date)
            ORDER BY month
            """, nativeQuery = true)
    List<MonthlyAvgProjection> avgStayByServiceAndYear(
            @Param("serviceId") UUID serviceId,
            @Param("year") int year);

    // KPI: Estancia media por servicio para un mes concreto (solo altas)
    @Query(value = """
            SELECT EXTRACT(MONTH FROM a.discharge_date)::int AS month,
                   AVG(a.hospitalization_length) AS avg_days
            FROM admissions a
            WHERE a.service_id = :serviceId
              AND a.discharge_date IS NOT NULL
              AND EXTRACT(YEAR FROM a.discharge_date) = :year
              AND EXTRACT(MONTH FROM a.discharge_date) = :month
            GROUP BY EXTRACT(MONTH FROM a.discharge_date)
            """, nativeQuery = true)
    List<MonthlyAvgProjection> avgStayByServiceAndYearAndMonth(
            @Param("serviceId") UUID serviceId,
            @Param("year") int year,
            @Param("month") int month);

    // KPI: Estancia media por médico agrupada por mes (año completo, solo altas)
    @Query(value = """
            SELECT u.user_id::text AS doctor_id,
                   u.name         AS doctor_name,
                   u.surname      AS doctor_surname,
                   EXTRACT(MONTH FROM a.discharge_date)::int AS month,
                   AVG(a.hospitalization_length) AS avg_days
            FROM admissions a
            JOIN users u ON u.user_id = a.assigned_doctor_id
            WHERE a.service_id = :serviceId
              AND a.discharge_date IS NOT NULL
              AND EXTRACT(YEAR FROM a.discharge_date) = :year
            GROUP BY u.user_id, u.name, u.surname, EXTRACT(MONTH FROM a.discharge_date)
            ORDER BY u.surname, month
            """, nativeQuery = true)
    List<DoctorMonthlyAvgProjection> avgStayByDoctorAndServiceAndYear(
            @Param("serviceId") UUID serviceId,
            @Param("year") int year);

    // KPI: Estancia media por médico para un mes concreto (solo altas)
    @Query(value = """
            SELECT u.user_id::text AS doctor_id,
                   u.name         AS doctor_name,
                   u.surname      AS doctor_surname,
                   EXTRACT(MONTH FROM a.discharge_date)::int AS month,
                   AVG(a.hospitalization_length) AS avg_days
            FROM admissions a
            JOIN users u ON u.user_id = a.assigned_doctor_id
            WHERE a.service_id = :serviceId
              AND a.discharge_date IS NOT NULL
              AND EXTRACT(YEAR FROM a.discharge_date) = :year
              AND EXTRACT(MONTH FROM a.discharge_date) = :month
            GROUP BY u.user_id, u.name, u.surname, EXTRACT(MONTH FROM a.discharge_date)
            ORDER BY u.surname
            """, nativeQuery = true)
    List<DoctorMonthlyAvgProjection> avgStayByDoctorAndServiceAndYearAndMonth(
            @Param("serviceId") UUID serviceId,
            @Param("year") int year,
            @Param("month") int month);
}
