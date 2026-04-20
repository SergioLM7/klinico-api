package com.sergio.klinico.domain.repositories;

import com.sergio.klinico.domain.models.Admission;
import com.sergio.klinico.domain.models.DoctorKpiSeries;
import com.sergio.klinico.domain.models.MonthlyKpiEntry;
import com.sergio.klinico.domain.models.PaginatedResult;

import java.util.List;
import java.util.UUID;

public interface AdmissionRepository {
    Admission save(Admission admission);
    boolean existsActiveAdmissionByPatientId(UUID patientId);
    Admission findById(UUID id);
    Double getAverageHospitalizationLengthByService(UUID serviceId);
    PaginatedResult<Admission> findAllActiveByService(UUID serviceId, int page, int size);
    PaginatedResult<Admission> findAllActiveByDoctor(UUID doctorId, int page, int size);
    PaginatedResult<Admission> findAllActive(int page, int size);
    PaginatedResult<Admission> searchByPatientSurnameAndServiceId(String surname, UUID serviceId, int page, int size);

    // KPI: Ingresos por servicio
    List<MonthlyKpiEntry> countAdmissionsByServiceAndYear(UUID serviceId, int year);
    List<MonthlyKpiEntry> countAdmissionsByServiceAndYearAndMonth(UUID serviceId, int year, int month);

    // KPI: Ingresos por médico
    List<DoctorKpiSeries> countAdmissionsByDoctorAndServiceAndYear(UUID serviceId, int year);
    List<DoctorKpiSeries> countAdmissionsByDoctorAndServiceAndYearAndMonth(UUID serviceId, int year, int month);

    // KPI: Estancia media por servicio
    List<MonthlyKpiEntry> avgStayByServiceAndYear(UUID serviceId, int year);
    List<MonthlyKpiEntry> avgStayByServiceAndYearAndMonth(UUID serviceId, int year, int month);

    // KPI: Estancia media por médico
    List<DoctorKpiSeries> avgStayByDoctorAndServiceAndYear(UUID serviceId, int year);
    List<DoctorKpiSeries> avgStayByDoctorAndServiceAndYearAndMonth(UUID serviceId, int year, int month);
}
