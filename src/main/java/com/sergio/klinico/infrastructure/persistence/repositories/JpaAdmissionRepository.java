package com.sergio.klinico.infrastructure.persistence.repositories;

import com.sergio.klinico.infrastructure.persistence.AdmissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaAdmissionRepository extends JpaRepository<AdmissionEntity, UUID> {
    boolean existsByPatientIdAndDischargeDateIsNull(UUID patientId);
    Page<AdmissionEntity> findByDischargeDateIsNull(Pageable pageable);

    // KPI: Estancia media. Filtramos por las que han sido dadas de alta (dischargeDate not null)
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
}
