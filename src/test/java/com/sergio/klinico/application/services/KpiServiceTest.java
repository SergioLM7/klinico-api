package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.models.DoctorKpiSeries;
import com.sergio.klinico.domain.models.MonthlyKpiEntry;
import com.sergio.klinico.domain.repositories.AdmissionRepository;
import com.sergio.klinico.domain.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KpiService Tests")
class KpiServiceTest {

    @InjectMocks
    private KpiService kpiService;

    @Mock
    private AdmissionRepository admissionRepository;

    @Mock
    private PatientRepository patientRepository;

    private UUID serviceId;
    private final int year = 2025;

    @BeforeEach
    void setUp() {
        serviceId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should return 12 monthly entries when month is null")
    void getAdmissionsByService_WhenMonthIsNull_ShouldReturn12Entries() {
        List<MonthlyKpiEntry> rawData = List.of(
                new MonthlyKpiEntry(1, 5.0),
                new MonthlyKpiEntry(6, 10.0)
        );

        when(admissionRepository.countAdmissionsByServiceAndYear(serviceId, year)).thenReturn(rawData);

        List<MonthlyKpiEntry> result = kpiService.getAdmissionsByService(serviceId, year, null);

        assertNotNull(result);
        assertEquals(12, result.size());
        assertEquals(5.0, result.get(0).value());
        assertEquals(0.0, result.get(1).value());
        assertEquals(10.0, result.get(5).value());
        verify(admissionRepository).countAdmissionsByServiceAndYear(serviceId, year);
        verify(admissionRepository, never()).countAdmissionsByServiceAndYearAndMonth(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should return single monthly entry when month is specified")
    void getAdmissionsByService_WhenMonthIsSpecified_ShouldReturnSingleEntry() {
        int month = 3;
        List<MonthlyKpiEntry> rawData = List.of(new MonthlyKpiEntry(month, 7.0));

        when(admissionRepository.countAdmissionsByServiceAndYearAndMonth(serviceId, year, month)).thenReturn(rawData);

        List<MonthlyKpiEntry> result = kpiService.getAdmissionsByService(serviceId, year, month);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(month, result.get(0).month());
        assertEquals(7.0, result.get(0).value());
        verify(admissionRepository).countAdmissionsByServiceAndYearAndMonth(serviceId, year, month);
        verify(admissionRepository, never()).countAdmissionsByServiceAndYear(any(), anyInt());
    }

    @Test
    @DisplayName("Should return zero value when month is specified but no data found")
    void getAdmissionsByService_WhenMonthSpecifiedAndNoData_ShouldReturnZero() {
        int month = 8;

        when(admissionRepository.countAdmissionsByServiceAndYearAndMonth(serviceId, year, month)).thenReturn(List.of());

        List<MonthlyKpiEntry> result = kpiService.getAdmissionsByService(serviceId, year, month);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(month, result.get(0).month());
        assertEquals(0.0, result.get(0).value());
    }

    @Test
    @DisplayName("Should return doctor series with 12 entries when month is null")
    void getAdmissionsByDoctor_WhenMonthIsNull_ShouldReturnDoctorSeriesWith12Entries() {
        UUID doctorId = UUID.randomUUID();
        List<MonthlyKpiEntry> doctorData = List.of(new MonthlyKpiEntry(3, 4.0));
        List<DoctorKpiSeries> rawData = List.of(
                new DoctorKpiSeries(doctorId, "Ana", "Martínez", doctorData)
        );

        when(admissionRepository.countAdmissionsByDoctorAndServiceAndYear(serviceId, year)).thenReturn(rawData);

        List<DoctorKpiSeries> result = kpiService.getAdmissionsByDoctor(serviceId, year, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(12, result.get(0).data().size());
        assertEquals("Ana", result.get(0).doctorName());
        verify(admissionRepository).countAdmissionsByDoctorAndServiceAndYear(serviceId, year);
    }

    @Test
    @DisplayName("Should return doctor series for specific month when month is specified")
    void getAdmissionsByDoctor_WhenMonthIsSpecified_ShouldReturnDoctorSeriesForMonth() {
        int month = 5;
        UUID doctorId = UUID.randomUUID();
        List<MonthlyKpiEntry> doctorData = List.of(new MonthlyKpiEntry(month, 3.0));
        List<DoctorKpiSeries> rawData = List.of(
                new DoctorKpiSeries(doctorId, "Pedro", "López", doctorData)
        );

        when(admissionRepository.countAdmissionsByDoctorAndServiceAndYearAndMonth(serviceId, year, month))
                .thenReturn(rawData);

        List<DoctorKpiSeries> result = kpiService.getAdmissionsByDoctor(serviceId, year, month);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).data().size());
        assertEquals(3.0, result.get(0).data().get(0).value());
        verify(admissionRepository).countAdmissionsByDoctorAndServiceAndYearAndMonth(serviceId, year, month);
    }

    @Test
    @DisplayName("Should return 12 monthly exitus entries when month is null")
    void getExitus_WhenMonthIsNull_ShouldReturn12Entries() {
        List<MonthlyKpiEntry> rawData = List.of(new MonthlyKpiEntry(11, 2.0));

        when(patientRepository.countExitusByServiceAndYear(serviceId, year)).thenReturn(rawData);

        List<MonthlyKpiEntry> result = kpiService.getExitus(serviceId, year, null);

        assertNotNull(result);
        assertEquals(12, result.size());
        assertEquals(2.0, result.get(10).value());
        assertEquals(0.0, result.get(0).value());
        verify(patientRepository).countExitusByServiceAndYear(serviceId, year);
    }

    @Test
    @DisplayName("Should return exitus entry for specific month")
    void getExitus_WhenMonthIsSpecified_ShouldReturnSingleEntry() {
        int month = 12;
        List<MonthlyKpiEntry> rawData = List.of(new MonthlyKpiEntry(month, 1.0));

        when(patientRepository.countExitusByServiceAndYearAndMonth(serviceId, year, month)).thenReturn(rawData);

        List<MonthlyKpiEntry> result = kpiService.getExitus(serviceId, year, month);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1.0, result.get(0).value());
        verify(patientRepository).countExitusByServiceAndYearAndMonth(serviceId, year, month);
    }

    @Test
    @DisplayName("Should return 12 average stay entries when month is null")
    void getAvgStayByService_WhenMonthIsNull_ShouldReturn12Entries() {
        List<MonthlyKpiEntry> rawData = List.of(
                new MonthlyKpiEntry(2, 7.5),
                new MonthlyKpiEntry(4, 5.0)
        );

        when(admissionRepository.avgStayByServiceAndYear(serviceId, year)).thenReturn(rawData);

        List<MonthlyKpiEntry> result = kpiService.getAvgStayByService(serviceId, year, null);

        assertNotNull(result);
        assertEquals(12, result.size());
        assertEquals(7.5, result.get(1).value());
        assertEquals(5.0, result.get(3).value());
        verify(admissionRepository).avgStayByServiceAndYear(serviceId, year);
    }

    @Test
    @DisplayName("Should return average stay for specific month")
    void getAvgStayByService_WhenMonthIsSpecified_ShouldReturnSingleEntry() {
        int month = 7;
        List<MonthlyKpiEntry> rawData = List.of(new MonthlyKpiEntry(month, 6.3));

        when(admissionRepository.avgStayByServiceAndYearAndMonth(serviceId, year, month)).thenReturn(rawData);

        List<MonthlyKpiEntry> result = kpiService.getAvgStayByService(serviceId, year, month);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(6.3, result.get(0).value());
        verify(admissionRepository).avgStayByServiceAndYearAndMonth(serviceId, year, month);
    }

    @Test
    @DisplayName("Should return doctor series with 12 average stay entries when month is null")
    void getAvgStayByDoctor_WhenMonthIsNull_ShouldReturnDoctorSeriesWith12Entries() {
        UUID doctorId = UUID.randomUUID();
        List<MonthlyKpiEntry> doctorData = List.of(new MonthlyKpiEntry(1, 8.0));
        List<DoctorKpiSeries> rawData = List.of(
                new DoctorKpiSeries(doctorId, "Laura", "Sánchez", doctorData)
        );

        when(admissionRepository.avgStayByDoctorAndServiceAndYear(serviceId, year)).thenReturn(rawData);

        List<DoctorKpiSeries> result = kpiService.getAvgStayByDoctor(serviceId, year, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(12, result.get(0).data().size());
        assertEquals(8.0, result.get(0).data().get(0).value());
        verify(admissionRepository).avgStayByDoctorAndServiceAndYear(serviceId, year);
    }

    @Test
    @DisplayName("Should return doctor average stay for specific month")
    void getAvgStayByDoctor_WhenMonthIsSpecified_ShouldReturnDoctorSeriesForMonth() {
        int month = 9;
        UUID doctorId = UUID.randomUUID();
        List<MonthlyKpiEntry> doctorData = List.of(new MonthlyKpiEntry(month, 4.5));
        List<DoctorKpiSeries> rawData = List.of(
                new DoctorKpiSeries(doctorId, "Javier", "Fernández", doctorData)
        );

        when(admissionRepository.avgStayByDoctorAndServiceAndYearAndMonth(serviceId, year, month))
                .thenReturn(rawData);

        List<DoctorKpiSeries> result = kpiService.getAvgStayByDoctor(serviceId, year, month);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).data().size());
        assertEquals(4.5, result.get(0).data().get(0).value());
        verify(admissionRepository).avgStayByDoctorAndServiceAndYearAndMonth(serviceId, year, month);
    }

    @Test
    @DisplayName("Should return average hospitalization length when data exists")
    void getServiceEfficiencyKPI_WhenDataExists_ShouldReturnAverage() {
        when(admissionRepository.getAverageHospitalizationLengthByService(serviceId)).thenReturn(7.25);

        Double result = kpiService.getServiceEfficiencyKPI(serviceId);

        assertNotNull(result);
        assertEquals(7.25, result);
        verify(admissionRepository).getAverageHospitalizationLengthByService(serviceId);
    }

    @Test
    @DisplayName("Should return 0.0 when no hospitalization data exists")
    void getServiceEfficiencyKPI_WhenNoData_ShouldReturnZero() {
        when(admissionRepository.getAverageHospitalizationLengthByService(serviceId)).thenReturn(null);

        Double result = kpiService.getServiceEfficiencyKPI(serviceId);

        assertNotNull(result);
        assertEquals(0.0, result);
        verify(admissionRepository).getAverageHospitalizationLengthByService(serviceId);
    }
}
