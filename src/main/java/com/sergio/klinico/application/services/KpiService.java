package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.models.DoctorKpiSeries;
import com.sergio.klinico.domain.models.MonthlyKpiEntry;
import com.sergio.klinico.domain.repositories.AdmissionRepository;
import com.sergio.klinico.domain.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class KpiService {

    private final AdmissionRepository admissionRepository;
    private final PatientRepository patientRepository;

    public List<MonthlyKpiEntry> getAdmissionsByService(UUID serviceId, int year, Integer month) {
        log.info("KPI ingresos por servicio - serviceId: {}, year: {}, month: {}", serviceId, year, month);
        List<MonthlyKpiEntry> raw = month != null
                ? admissionRepository.countAdmissionsByServiceAndYearAndMonth(serviceId, year, month)
                : admissionRepository.countAdmissionsByServiceAndYear(serviceId, year);
        return fillMonths(raw, month);
    }

    public List<DoctorKpiSeries> getAdmissionsByDoctor(UUID serviceId, int year, Integer month) {
        log.info("KPI ingresos por médico - serviceId: {}, year: {}, month: {}", serviceId, year, month);
        List<DoctorKpiSeries> raw = month != null
                ? admissionRepository.countAdmissionsByDoctorAndServiceAndYearAndMonth(serviceId, year, month)
                : admissionRepository.countAdmissionsByDoctorAndServiceAndYear(serviceId, year);
        return fillDoctorMonths(raw, month);
    }

    public List<MonthlyKpiEntry> getExitus(UUID serviceId, int year, Integer month) {
        log.info("KPI éxitus - serviceId: {}, year: {}, month: {}", serviceId, year, month);
        List<MonthlyKpiEntry> raw = month != null
                ? patientRepository.countExitusByServiceAndYearAndMonth(serviceId, year, month)
                : patientRepository.countExitusByServiceAndYear(serviceId, year);
        return fillMonths(raw, month);
    }

    public List<MonthlyKpiEntry> getAvgStayByService(UUID serviceId, int year, Integer month) {
        log.info("KPI estancia media por servicio - serviceId: {}, year: {}, month: {}", serviceId, year, month);
        List<MonthlyKpiEntry> raw = month != null
                ? admissionRepository.avgStayByServiceAndYearAndMonth(serviceId, year, month)
                : admissionRepository.avgStayByServiceAndYear(serviceId, year);
        return fillMonths(raw, month);
    }

    public List<DoctorKpiSeries> getAvgStayByDoctor(UUID serviceId, int year, Integer month) {
        log.info("KPI estancia media por médico - serviceId: {}, year: {}, month: {}", serviceId, year, month);
        List<DoctorKpiSeries> raw = month != null
                ? admissionRepository.avgStayByDoctorAndServiceAndYearAndMonth(serviceId, year, month)
                : admissionRepository.avgStayByDoctorAndServiceAndYear(serviceId, year);
        return fillDoctorMonths(raw, month);
    }


    public Double getServiceEfficiencyKPI(UUID serviceId) {
        log.info("Calculando estancia media para el servicio: {}", serviceId);

        Double average = admissionRepository.getAverageHospitalizationLengthByService(serviceId);

        log.info("Estancia media para el servicio calculada: {}", average);
        return average != null ? average : 0.0;
    }

    /**
     * Rellena con valor 0.0 los meses sin datos.
     * En modo año devuelve los 12 meses; en modo mes devuelve ese único mes (0.0 si no hay datos).
     */
    private List<MonthlyKpiEntry> fillMonths(List<MonthlyKpiEntry> data, Integer month) {
        if (month != null) {
            return data.isEmpty() ? List.of(new MonthlyKpiEntry(month, 0.0)) : data;
        }
        Map<Integer, Double> byMonth = data.stream()
                .collect(Collectors.toMap(MonthlyKpiEntry::month, MonthlyKpiEntry::value));
        return IntStream.rangeClosed(1, 12)
                .mapToObj(m -> new MonthlyKpiEntry(m, byMonth.getOrDefault(m, 0.0)))
                .toList();
    }

    private List<DoctorKpiSeries> fillDoctorMonths(List<DoctorKpiSeries> doctors, Integer month) {
        return doctors.stream()
                .map(d -> new DoctorKpiSeries(
                        d.doctorId(),
                        d.doctorName(),
                        d.doctorSurname(),
                        fillMonths(d.data(), month)))
                .toList();
    }
}
