package com.sergio.klinico.persistence.adapters;

import com.sergio.klinico.domain.models.Admission;
import com.sergio.klinico.domain.models.DoctorKpiSeries;
import com.sergio.klinico.domain.models.MonthlyKpiEntry;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.infrastructure.mappers.AdmissionMapper;
import com.sergio.klinico.infrastructure.persistence.AdmissionEntity;
import com.sergio.klinico.infrastructure.persistence.adapters.AdmissionPersistenceAdapter;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaAdmissionRepository;
import com.sergio.klinico.infrastructure.persistence.repositories.projections.DoctorMonthlyAvgProjection;
import com.sergio.klinico.infrastructure.persistence.repositories.projections.DoctorMonthlyCountProjection;
import com.sergio.klinico.infrastructure.persistence.repositories.projections.MonthlyAvgProjection;
import com.sergio.klinico.infrastructure.persistence.repositories.projections.MonthlyCountProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdmissionPersistenceAdapter Tests")
class AdmissionPersistenceAdapterTest {

    @InjectMocks
    private AdmissionPersistenceAdapter adapter;

    @Mock
    private JpaAdmissionRepository jpaRepository;

    @Mock
    private AdmissionMapper mapper;

    private final UUID admissionId = UUID.randomUUID();
    private final UUID serviceId   = UUID.randomUUID();
    private final UUID doctorId    = UUID.randomUUID();
    private final UUID patientId   = UUID.randomUUID();

    private AdmissionEntity testEntity;
    private Admission testDomain;

    @BeforeEach
    void setUp() {
        testEntity = new AdmissionEntity();
        testEntity.setAdmissionId(admissionId);

        testDomain = Admission.builder()
                .admissionId(admissionId)
                .patientId(patientId)
                .serviceId(serviceId)
                .assignedDoctorId(doctorId)
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();
    }

    @Test
    @DisplayName("save: new admission (null id) should not load existing version")
    void save_WhenNewAdmission_ShouldSaveWithoutVersionLookup() {
        Admission newAdmission = Admission.builder().build();

        when(mapper.toEntity(newAdmission)).thenReturn(testEntity);
        when(jpaRepository.save(testEntity)).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        Admission result = adapter.save(newAdmission);

        assertThat(result).isEqualTo(testDomain);
        verify(jpaRepository, never()).findById(any());
        verify(jpaRepository).save(testEntity);
    }

    @Test
    @DisplayName("save: existing admission should copy version from DB")
    void save_WhenExistingAdmission_ShouldCopyVersionFromDb() {
        testDomain.setAdmissionId(admissionId);
        AdmissionEntity existingEntity = new AdmissionEntity();
        existingEntity.setVersion(5L);

        when(mapper.toEntity(testDomain)).thenReturn(testEntity);
        when(jpaRepository.findById(admissionId)).thenReturn(Optional.of(existingEntity));
        when(jpaRepository.save(testEntity)).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        Admission result = adapter.save(testDomain);

        assertThat(result).isEqualTo(testDomain);
        assertThat(testEntity.getVersion()).isEqualTo(5L);
        verify(jpaRepository).findById(admissionId);
    }

    @Test
    @DisplayName("save: existing admission not found in DB should still save without version")
    void save_WhenExistingAdmissionNotFoundInDb_ShouldSaveAnyway() {
        testDomain.setAdmissionId(admissionId);

        when(mapper.toEntity(testDomain)).thenReturn(testEntity);
        when(jpaRepository.findById(admissionId)).thenReturn(Optional.empty());
        when(jpaRepository.save(testEntity)).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        Admission result = adapter.save(testDomain);

        assertThat(result).isEqualTo(testDomain);
        verify(jpaRepository).findById(admissionId);
        verify(jpaRepository).save(testEntity);
    }

    @Test
    @DisplayName("existsActiveAdmissionByPatientId: should delegate to JPA repository")
    void existsActiveAdmissionByPatientId_ShouldDelegateToJpa() {
        when(jpaRepository.existsByPatientIdAndDischargeDateIsNull(patientId)).thenReturn(true);

        boolean result = adapter.existsActiveAdmissionByPatientId(patientId);

        assertThat(result).isTrue();
        verify(jpaRepository).existsByPatientIdAndDischargeDateIsNull(patientId);
    }

    @Test
    @DisplayName("existsActiveAdmissionByPatientId: returns false when no active admission")
    void existsActiveAdmissionByPatientId_WhenNone_ShouldReturnFalse() {
        when(jpaRepository.existsByPatientIdAndDischargeDateIsNull(patientId)).thenReturn(false);

        assertThat(adapter.existsActiveAdmissionByPatientId(patientId)).isFalse();
    }

    @Test
    @DisplayName("findById: should return mapped domain when entity found")
    void findById_WhenFound_ShouldReturnDomain() {
        when(jpaRepository.findById(admissionId)).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        Admission result = adapter.findById(admissionId);

        assertThat(result).isEqualTo(testDomain);
    }

    @Test
    @DisplayName("findById: should return null when entity not found")
    void findById_WhenNotFound_ShouldReturnNull() {
        when(jpaRepository.findById(admissionId)).thenReturn(Optional.empty());

        Admission result = adapter.findById(admissionId);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getAverageHospitalizationLengthByService: should delegate to JPA")
    void getAverageHospitalizationLengthByService_ShouldDelegate() {
        when(jpaRepository.getAverageLengthByService(serviceId)).thenReturn(7.5);

        Double result = adapter.getAverageHospitalizationLengthByService(serviceId);

        assertThat(result).isEqualTo(7.5);
        verify(jpaRepository).getAverageLengthByService(serviceId);
    }

    @Test
    @DisplayName("findAllActive: should return paginated result with hospitalization length set")
    void findAllActive_ShouldReturnPaginatedResult() {
        Page<AdmissionEntity> page = new PageImpl<>(List.of(testEntity), PageRequest.of(0, 10), 1);
        when(jpaRepository.findByDischargeDateIsNull(any())).thenReturn(page);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        PaginatedResult<Admission> result = adapter.findAllActive(0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.content().get(0).getHospitalizationLength()).isNotNull();
    }

    @Test
    @DisplayName("findAllActive: empty page should return empty result")
    void findAllActive_WhenEmpty_ShouldReturnEmptyResult() {
        Page<AdmissionEntity> emptyPage = new PageImpl<>(List.of());
        when(jpaRepository.findByDischargeDateIsNull(any())).thenReturn(emptyPage);

        PaginatedResult<Admission> result = adapter.findAllActive(0, 10);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    @DisplayName("findAllActiveByService: should return paginated admissions for service")
    void findAllActiveByService_ShouldReturnPaginatedResult() {
        Page<AdmissionEntity> page = new PageImpl<>(List.of(testEntity), PageRequest.of(0, 10), 1);
        when(jpaRepository.findByServiceIdAndDischargeDateIsNull(eq(serviceId), any())).thenReturn(page);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        PaginatedResult<Admission> result = adapter.findAllActiveByService(serviceId, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findAllActiveByDoctor: should return paginated admissions for doctor")
    void findAllActiveByDoctor_ShouldReturnPaginatedResult() {
        Page<AdmissionEntity> page = new PageImpl<>(List.of(testEntity), PageRequest.of(0, 10), 1);
        when(jpaRepository.findByAssignedDoctorIdAndDischargeDateIsNull(eq(doctorId), any())).thenReturn(page);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        PaginatedResult<Admission> result = adapter.findAllActiveByDoctor(doctorId, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("searchByPatientSurnameAndServiceId: should return matching paginated admissions")
    void searchByPatientSurnameAndServiceId_ShouldReturnResult() {
        Page<AdmissionEntity> page = new PageImpl<>(List.of(testEntity), PageRequest.of(0, 10), 1);
        when(jpaRepository.findByPatientSurnameContainingIgnoreCaseAndServiceIdAndDischargeDateIsNull(
                eq("García"), eq(serviceId), any())).thenReturn(page);
        when(mapper.toDomain(testEntity)).thenReturn(testDomain);

        PaginatedResult<Admission> result = adapter.searchByPatientSurnameAndServiceId("García", serviceId, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("countAdmissionsByServiceAndYear: should map projections to MonthlyKpiEntry")
    void countAdmissionsByServiceAndYear_ShouldMapProjections() {
        MonthlyCountProjection p = mock(MonthlyCountProjection.class);
        when(p.getMonth()).thenReturn(3);
        when(p.getCount()).thenReturn(8L);
        when(jpaRepository.countAdmissionsByServiceAndYear(serviceId, 2025)).thenReturn(List.of(p));

        List<MonthlyKpiEntry> result = adapter.countAdmissionsByServiceAndYear(serviceId, 2025);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).month()).isEqualTo(3);
        assertThat(result.get(0).value()).isEqualTo(8.0);
    }

    @Test
    @DisplayName("countAdmissionsByServiceAndYearAndMonth: should map projections to MonthlyKpiEntry")
    void countAdmissionsByServiceAndYearAndMonth_ShouldMapProjections() {
        MonthlyCountProjection p = mock(MonthlyCountProjection.class);
        when(p.getMonth()).thenReturn(6);
        when(p.getCount()).thenReturn(4L);
        when(jpaRepository.countAdmissionsByServiceAndYearAndMonth(serviceId, 2025, 6)).thenReturn(List.of(p));

        List<MonthlyKpiEntry> result = adapter.countAdmissionsByServiceAndYearAndMonth(serviceId, 2025, 6);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).month()).isEqualTo(6);
        assertThat(result.get(0).value()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("countAdmissionsByDoctorAndServiceAndYear: should group projections into DoctorKpiSeries")
    void countAdmissionsByDoctorAndServiceAndYear_ShouldGroupByDoctor() {
        String doctorIdStr = doctorId.toString();
        DoctorMonthlyCountProjection p1 = mock(DoctorMonthlyCountProjection.class);
        when(p1.getDoctorId()).thenReturn(doctorIdStr);
        when(p1.getDoctorName()).thenReturn("Ana");
        when(p1.getDoctorSurname()).thenReturn("García");
        when(p1.getMonth()).thenReturn(1);
        when(p1.getCount()).thenReturn(3L);

        DoctorMonthlyCountProjection p2 = mock(DoctorMonthlyCountProjection.class);
        when(p2.getDoctorId()).thenReturn(doctorIdStr);
        when(p2.getMonth()).thenReturn(2);
        when(p2.getCount()).thenReturn(5L);

        when(jpaRepository.countAdmissionsByDoctorAndServiceAndYear(serviceId, 2025))
                .thenReturn(List.of(p1, p2));

        List<DoctorKpiSeries> result = adapter.countAdmissionsByDoctorAndServiceAndYear(serviceId, 2025);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).doctorId()).isEqualTo(doctorId);
        assertThat(result.get(0).doctorName()).isEqualTo("Ana");
        assertThat(result.get(0).data()).hasSize(2);
    }

    @Test
    @DisplayName("countAdmissionsByDoctorAndServiceAndYear: empty projection returns empty list")
    void countAdmissionsByDoctorAndServiceAndYear_WhenEmpty_ShouldReturnEmptyList() {
        when(jpaRepository.countAdmissionsByDoctorAndServiceAndYear(serviceId, 2025))
                .thenReturn(List.of());

        List<DoctorKpiSeries> result = adapter.countAdmissionsByDoctorAndServiceAndYear(serviceId, 2025);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("countAdmissionsByDoctorAndServiceAndYearAndMonth: should group projections into DoctorKpiSeries")
    void countAdmissionsByDoctorAndServiceAndYearAndMonth_ShouldGroupByDoctor() {
        DoctorMonthlyCountProjection p = mock(DoctorMonthlyCountProjection.class);
        when(p.getDoctorId()).thenReturn(doctorId.toString());
        when(p.getDoctorName()).thenReturn("Carlos");
        when(p.getDoctorSurname()).thenReturn("López");
        when(p.getMonth()).thenReturn(4);
        when(p.getCount()).thenReturn(2L);

        when(jpaRepository.countAdmissionsByDoctorAndServiceAndYearAndMonth(serviceId, 2025, 4))
                .thenReturn(List.of(p));

        List<DoctorKpiSeries> result = adapter.countAdmissionsByDoctorAndServiceAndYearAndMonth(serviceId, 2025, 4);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).doctorName()).isEqualTo("Carlos");
        assertThat(result.get(0).data().get(0).value()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("avgStayByServiceAndYear: should map projections including null avgDays as 0")
    void avgStayByServiceAndYear_ShouldMapProjections() {
        MonthlyAvgProjection p1 = mock(MonthlyAvgProjection.class);
        when(p1.getMonth()).thenReturn(1);
        when(p1.getAvgDays()).thenReturn(6.5);

        MonthlyAvgProjection p2 = mock(MonthlyAvgProjection.class);
        when(p2.getMonth()).thenReturn(2);
        when(p2.getAvgDays()).thenReturn(null);

        when(jpaRepository.avgStayByServiceAndYear(serviceId, 2025)).thenReturn(List.of(p1, p2));

        List<MonthlyKpiEntry> result = adapter.avgStayByServiceAndYear(serviceId, 2025);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).value()).isEqualTo(6.5);
        assertThat(result.get(1).value()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("avgStayByServiceAndYearAndMonth: should map projections to MonthlyKpiEntry")
    void avgStayByServiceAndYearAndMonth_ShouldMapProjections() {
        MonthlyAvgProjection p = mock(MonthlyAvgProjection.class);
        when(p.getMonth()).thenReturn(5);
        when(p.getAvgDays()).thenReturn(8.2);
        when(jpaRepository.avgStayByServiceAndYearAndMonth(serviceId, 2025, 5)).thenReturn(List.of(p));

        List<MonthlyKpiEntry> result = adapter.avgStayByServiceAndYearAndMonth(serviceId, 2025, 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).month()).isEqualTo(5);
        assertThat(result.get(0).value()).isEqualTo(8.2);
    }

    @Test
    @DisplayName("avgStayByDoctorAndServiceAndYear: should group projections including null avgDays as 0")
    void avgStayByDoctorAndServiceAndYear_ShouldGroupByDoctor() {
        DoctorMonthlyAvgProjection p1 = mock(DoctorMonthlyAvgProjection.class);
        when(p1.getDoctorId()).thenReturn(doctorId.toString());
        when(p1.getDoctorName()).thenReturn("Laura");
        when(p1.getDoctorSurname()).thenReturn("Martínez");
        when(p1.getMonth()).thenReturn(1);
        when(p1.getAvgDays()).thenReturn(5.0);

        DoctorMonthlyAvgProjection p2 = mock(DoctorMonthlyAvgProjection.class);
        when(p2.getDoctorId()).thenReturn(doctorId.toString());
        when(p2.getMonth()).thenReturn(2);
        when(p2.getAvgDays()).thenReturn(null);

        when(jpaRepository.avgStayByDoctorAndServiceAndYear(serviceId, 2025)).thenReturn(List.of(p1, p2));

        List<DoctorKpiSeries> result = adapter.avgStayByDoctorAndServiceAndYear(serviceId, 2025);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).data()).hasSize(2);
        assertThat(result.get(0).data().get(0).value()).isEqualTo(5.0);
        assertThat(result.get(0).data().get(1).value()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("avgStayByDoctorAndServiceAndYearAndMonth: should group projections into DoctorKpiSeries")
    void avgStayByDoctorAndServiceAndYearAndMonth_ShouldGroupByDoctor() {
        DoctorMonthlyAvgProjection p = mock(DoctorMonthlyAvgProjection.class);
        when(p.getDoctorId()).thenReturn(doctorId.toString());
        when(p.getDoctorName()).thenReturn("Javier");
        when(p.getDoctorSurname()).thenReturn("Fernández");
        when(p.getMonth()).thenReturn(7);
        when(p.getAvgDays()).thenReturn(4.3);

        when(jpaRepository.avgStayByDoctorAndServiceAndYearAndMonth(serviceId, 2025, 7))
                .thenReturn(List.of(p));

        List<DoctorKpiSeries> result = adapter.avgStayByDoctorAndServiceAndYearAndMonth(serviceId, 2025, 7);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).doctorSurname()).isEqualTo("Fernández");
        assertThat(result.get(0).data().get(0).value()).isEqualTo(4.3);
    }

    @Test
    @DisplayName("avgStayByDoctorAndServiceAndYearAndMonth: empty projection returns empty list")
    void avgStayByDoctorAndServiceAndYearAndMonth_WhenEmpty_ShouldReturnEmptyList() {
        when(jpaRepository.avgStayByDoctorAndServiceAndYearAndMonth(serviceId, 2025, 7))
                .thenReturn(List.of());

        assertThat(adapter.avgStayByDoctorAndServiceAndYearAndMonth(serviceId, 2025, 7)).isEmpty();
    }
}
