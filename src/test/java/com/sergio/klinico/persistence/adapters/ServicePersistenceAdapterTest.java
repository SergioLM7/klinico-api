package com.sergio.klinico.persistence.adapters;

import com.sergio.klinico.domain.models.HospitalService;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.infrastructure.mappers.ServiceMapper;
import com.sergio.klinico.infrastructure.persistence.ServiceEntity;
import com.sergio.klinico.infrastructure.persistence.adapters.ServicePersistenceAdapter;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaServiceRepository;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServicePersistenceAdapter Tests")
class ServicePersistenceAdapterTest {

    @InjectMocks
    private ServicePersistenceAdapter adapter;

    @Mock
    private JpaServiceRepository jpaServiceRepository;

    @Mock
    private ServiceMapper serviceMapper;

    private ServiceEntity testEntity;
    private HospitalService testDomain;

    @BeforeEach
    void setUp() {
        testEntity = new ServiceEntity(UUID.randomUUID(), "Cardiología", true);
        testDomain = HospitalService.builder()
                .serviceId(testEntity.getServiceId())
                .name("Cardiología")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should return paginated result with matching active services")
    void findByNameContainingIgnoreCaseAndActiveTrue_WhenMatchFound_ShouldReturnResult() {
        Page<ServiceEntity> page = new PageImpl<>(List.of(testEntity), PageRequest.of(0, 10), 1);
        when(jpaServiceRepository.findByNameContainingIgnoreCaseAndActiveTrue(eq("cardio"), any()))
                .thenReturn(page);
        when(serviceMapper.toDomain(testEntity)).thenReturn(testDomain);

        PaginatedResult<HospitalService> result =
                adapter.findByNameContainingIgnoreCaseAndActiveTrue("cardio", 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getName()).isEqualTo("Cardiología");
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.currentPage()).isEqualTo(0);
        assertThat(result.isLast()).isTrue();

        verify(jpaServiceRepository).findByNameContainingIgnoreCaseAndActiveTrue(eq("cardio"), any());
    }

    @Test
    @DisplayName("Should return empty paginated result when no active services match")
    void findByNameContainingIgnoreCaseAndActiveTrue_WhenNoMatch_ShouldReturnEmpty() {
        Page<ServiceEntity> emptyPage = new PageImpl<>(List.of());
        when(jpaServiceRepository.findByNameContainingIgnoreCaseAndActiveTrue(eq("xyz"), any()))
                .thenReturn(emptyPage);

        PaginatedResult<HospitalService> result =
                adapter.findByNameContainingIgnoreCaseAndActiveTrue("xyz", 0, 10);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    @DisplayName("Should return multiple services when several match the name")
    void findByNameContainingIgnoreCaseAndActiveTrue_WhenMultipleMatches_ShouldReturnAll() {
        ServiceEntity entity2 = new ServiceEntity(UUID.randomUUID(), "Medicina Interna", true);
        HospitalService domain2 = HospitalService.builder()
                .serviceId(entity2.getServiceId())
                .name("Medicina Interna")
                .active(true)
                .build();

        Page<ServiceEntity> page = new PageImpl<>(
                List.of(testEntity, entity2), PageRequest.of(0, 10), 2);
        when(jpaServiceRepository.findByNameContainingIgnoreCaseAndActiveTrue(eq("a"), any()))
                .thenReturn(page);
        when(serviceMapper.toDomain(testEntity)).thenReturn(testDomain);
        when(serviceMapper.toDomain(entity2)).thenReturn(domain2);

        PaginatedResult<HospitalService> result =
                adapter.findByNameContainingIgnoreCaseAndActiveTrue("a", 0, 10);

        assertThat(result.content()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2L);
    }
}
