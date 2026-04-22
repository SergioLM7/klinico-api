package com.sergio.klinico.persistence.adapters;

import com.sergio.klinico.domain.models.MonthlyKpiEntry;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.enums.PatientStatus;
import com.sergio.klinico.infrastructure.mappers.PatientMapper;
import com.sergio.klinico.infrastructure.persistence.PatientEntity;
import com.sergio.klinico.infrastructure.persistence.adapters.PatientPersistenceAdapter;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaPatientRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientPersistenceAdapter Tests")
class PatientPersistenceAdapterTest {

    @InjectMocks
    private PatientPersistenceAdapter adapter;

    @Mock
    private JpaPatientRepository jpaRepository;

    @Mock
    private PatientMapper patientMapper;

    private final UUID patientId = UUID.randomUUID();
    private final UUID serviceId = UUID.randomUUID();

    private PatientEntity testEntity;
    private Patient testDomain;

    @BeforeEach
    void setUp() {
        testEntity = new PatientEntity();
        testEntity.setPatientId(patientId);
        testEntity.setDni("12345678A");
        testEntity.setName("Lucía");
        testEntity.setSurname("Fernández");
        testEntity.setSex('F');
        testEntity.setBirthdate(LocalDate.of(1985, 3, 15));
        testEntity.setStatus(PatientStatus.ALTA);

        testDomain = Patient.builder()
                .patientId(patientId)
                .dni("12345678A")
                .name("Lucía")
                .surname("Fernández")
                .status(PatientStatus.ALTA)
                .build();
    }

    @Test
    @DisplayName("save: new patient (null id) should not look up version")
    void save_WhenNewPatient_ShouldSaveWithoutVersionLookup() {
        Patient newPatient = Patient.builder().build();

        when(patientMapper.toEntity(newPatient)).thenReturn(testEntity);
        when(jpaRepository.save(testEntity)).thenReturn(testEntity);
        when(patientMapper.toDomain(testEntity)).thenReturn(testDomain);

        Patient result = adapter.save(newPatient);

        assertThat(result).isEqualTo(testDomain);
        verify(jpaRepository, never()).findById(any());
    }

    @Test
    @DisplayName("save: existing patient should copy version from DB")
    void save_WhenExistingPatient_ShouldCopyVersionFromDb() {
        testDomain.setPatientId(patientId);
        PatientEntity existingEntity = new PatientEntity();
        existingEntity.setVersion(3L);

        when(patientMapper.toEntity(testDomain)).thenReturn(testEntity);
        when(jpaRepository.findById(patientId)).thenReturn(Optional.of(existingEntity));
        when(jpaRepository.save(testEntity)).thenReturn(testEntity);
        when(patientMapper.toDomain(testEntity)).thenReturn(testDomain);

        Patient result = adapter.save(testDomain);

        assertThat(result).isEqualTo(testDomain);
        assertThat(testEntity.getVersion()).isEqualTo(3L);
        verify(jpaRepository).findById(patientId);
    }

    @Test
    @DisplayName("save: existing patient not in DB should still save")
    void save_WhenExistingPatientNotFoundInDb_ShouldSaveAnyway() {
        testDomain.setPatientId(patientId);

        when(patientMapper.toEntity(testDomain)).thenReturn(testEntity);
        when(jpaRepository.findById(patientId)).thenReturn(Optional.empty());
        when(jpaRepository.save(testEntity)).thenReturn(testEntity);
        when(patientMapper.toDomain(testEntity)).thenReturn(testDomain);

        Patient result = adapter.save(testDomain);

        assertThat(result).isEqualTo(testDomain);
    }

    @Test
    @DisplayName("existsByDni: should return true when DNI exists")
    void existsByDni_WhenExists_ShouldReturnTrue() {
        when(jpaRepository.existsByDni("12345678A")).thenReturn(true);

        assertThat(adapter.existsByDni("12345678A")).isTrue();
    }

    @Test
    @DisplayName("existsByDni: should return false when DNI does not exist")
    void existsByDni_WhenNotExists_ShouldReturnFalse() {
        when(jpaRepository.existsByDni("99999999Z")).thenReturn(false);

        assertThat(adapter.existsByDni("99999999Z")).isFalse();
    }

    @Test
    @DisplayName("findById: should return mapped domain when entity found")
    void findById_WhenFound_ShouldReturnDomain() {
        when(jpaRepository.findById(patientId)).thenReturn(Optional.of(testEntity));
        when(patientMapper.toDomain(testEntity)).thenReturn(testDomain);

        Patient result = adapter.findById(patientId);

        assertThat(result).isEqualTo(testDomain);
    }

    @Test
    @DisplayName("findById: should return null when entity not found")
    void findById_WhenNotFound_ShouldReturnNull() {
        when(jpaRepository.findById(patientId)).thenReturn(Optional.empty());

        Patient result = adapter.findById(patientId);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("findAllByIds: should return mapped domain list")
    void findAllByIds_ShouldReturnDomainList() {
        UUID id2 = UUID.randomUUID();
        List<UUID> ids = List.of(patientId, id2);

        when(jpaRepository.findAllById(ids)).thenReturn(List.of(testEntity));
        when(patientMapper.toDomain(testEntity)).thenReturn(testDomain);

        List<Patient> result = adapter.findAllByIds(ids);

        assertThat(result).hasSize(1).contains(testDomain);
    }

    @Test
    @DisplayName("findAllByIds: should return empty list when no entities found")
    void findAllByIds_WhenNoneFound_ShouldReturnEmptyList() {
        when(jpaRepository.findAllById(any())).thenReturn(List.of());

        List<Patient> result = adapter.findAllByIds(List.of(UUID.randomUUID()));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll: should return paginated result")
    void findAll_ShouldReturnPaginatedResult() {
        Page<PatientEntity> page = new PageImpl<>(List.of(testEntity), PageRequest.of(0, 10), 1);
        when(jpaRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(patientMapper.toDomain(testEntity)).thenReturn(testDomain);

        PaginatedResult<Patient> result = adapter.findAll(0, 10);

        assertThat(result.content()).hasSize(1).contains(testDomain);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    @DisplayName("findAll: empty page should return empty result")
    void findAll_WhenEmpty_ShouldReturnEmptyResult() {
        Page<PatientEntity> emptyPage = new PageImpl<>(List.of());
        when(jpaRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        PaginatedResult<Patient> result = adapter.findAll(0, 10);

        assertThat(result.content()).isEmpty();
    }

    @Test
    @DisplayName("findBySurnameAndStatusAlta: should return paginated matching patients")
    void findBySurnameAndStatusAlta_ShouldReturnMatchingPatients() {
        Page<PatientEntity> page = new PageImpl<>(List.of(testEntity), PageRequest.of(0, 10), 1);
        when(jpaRepository.findBySurnameContainingIgnoreCaseAndStatus(
                eq("Fernández"), eq(PatientStatus.ALTA), any())).thenReturn(page);
        when(patientMapper.toDomain(testEntity)).thenReturn(testDomain);

        PaginatedResult<Patient> result = adapter.findBySurnameAndStatusAlta("Fernández", 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findBySurnameAndStatusAlta: no matches returns empty result")
    void findBySurnameAndStatusAlta_WhenNoMatch_ShouldReturnEmpty() {
        Page<PatientEntity> emptyPage = new PageImpl<>(List.of());
        when(jpaRepository.findBySurnameContainingIgnoreCaseAndStatus(
                any(), eq(PatientStatus.ALTA), any())).thenReturn(emptyPage);

        PaginatedResult<Patient> result = adapter.findBySurnameAndStatusAlta("Inexistente", 0, 10);

        assertThat(result.content()).isEmpty();
    }

    @Test
    @DisplayName("countExitusByServiceAndYear: should map projections to MonthlyKpiEntry")
    void countExitusByServiceAndYear_ShouldMapProjections() {
        MonthlyCountProjection p = mock(MonthlyCountProjection.class);
        when(p.getMonth()).thenReturn(2);
        when(p.getCount()).thenReturn(1L);
        when(jpaRepository.countExitusByServiceAndYear(serviceId, 2025)).thenReturn(List.of(p));

        List<MonthlyKpiEntry> result = adapter.countExitusByServiceAndYear(serviceId, 2025);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).month()).isEqualTo(2);
        assertThat(result.get(0).value()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("countExitusByServiceAndYear: empty projection returns empty list")
    void countExitusByServiceAndYear_WhenEmpty_ShouldReturnEmptyList() {
        when(jpaRepository.countExitusByServiceAndYear(serviceId, 2025)).thenReturn(List.of());

        assertThat(adapter.countExitusByServiceAndYear(serviceId, 2025)).isEmpty();
    }

    @Test
    @DisplayName("countExitusByServiceAndYearAndMonth: should map projections to MonthlyKpiEntry")
    void countExitusByServiceAndYearAndMonth_ShouldMapProjections() {
        MonthlyCountProjection p = mock(MonthlyCountProjection.class);
        when(p.getMonth()).thenReturn(11);
        when(p.getCount()).thenReturn(2L);
        when(jpaRepository.countExitusByServiceAndYearAndMonth(serviceId, 2025, 11)).thenReturn(List.of(p));

        List<MonthlyKpiEntry> result = adapter.countExitusByServiceAndYearAndMonth(serviceId, 2025, 11);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).month()).isEqualTo(11);
        assertThat(result.get(0).value()).isEqualTo(2.0);
    }
}
