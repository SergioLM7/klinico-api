package com.sergio.klinico.persistence.adapters;

import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.UserWorkLoad;
import com.sergio.klinico.domain.models.enums.UserRole;
import com.sergio.klinico.infrastructure.persistence.UserEntity;
import com.sergio.klinico.infrastructure.persistence.adapters.UserPersistenceAdapter;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaUserRepository;
import com.sergio.klinico.infrastructure.persistence.repositories.projections.UserWorkloadProjection;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserPersistenceAdapter Tests")
class UserPersistenceAdapterTest {

    @InjectMocks
    private UserPersistenceAdapter adapter;

    @Mock
    private JpaUserRepository jpaUserRepository;

    private final UUID userId    = UUID.randomUUID();
    private final UUID serviceId = UUID.randomUUID();

    private UserEntity testEntity;

    @BeforeEach
    void setUp() {
        testEntity = UserEntity.builder()
                .userId(userId)
                .email("medico@test.com")
                .password("hashed")
                .name("Carlos")
                .surname("Ruiz")
                .role(UserRole.MEDICO)
                .active(true)
                .serviceId(serviceId)
                .build();
    }

    @Test
    @DisplayName("findByEmail: should return mapped User when entity found")
    void findByEmail_WhenFound_ShouldReturnUser() {
        when(jpaUserRepository.findByEmailAndActiveTrue("medico@test.com"))
                .thenReturn(Optional.of(testEntity));

        Optional<User> result = adapter.findByEmail("medico@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
        assertThat(result.get().getEmail()).isEqualTo("medico@test.com");
        assertThat(result.get().getName()).isEqualTo("Carlos");
        assertThat(result.get().getSurname()).isEqualTo("Ruiz");
        assertThat(result.get().getRole()).isEqualTo(UserRole.MEDICO);
        assertThat(result.get().isActive()).isTrue();
        assertThat(result.get().getServiceId()).isEqualTo(serviceId);
    }

    @Test
    @DisplayName("findByEmail: should return empty Optional when not found")
    void findByEmail_WhenNotFound_ShouldReturnEmpty() {
        when(jpaUserRepository.findByEmailAndActiveTrue("unknown@test.com"))
                .thenReturn(Optional.empty());

        Optional<User> result = adapter.findByEmail("unknown@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findById: should return mapped User when entity found")
    void findById_WhenFound_ShouldReturnUser() {
        when(jpaUserRepository.findById(userId)).thenReturn(Optional.of(testEntity));

        Optional<User> result = adapter.findById(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
        assertThat(result.get().getEmail()).isEqualTo("medico@test.com");
        assertThat(result.get().getName()).isEqualTo("Carlos");
        assertThat(result.get().getSurname()).isEqualTo("Ruiz");
        assertThat(result.get().getRole()).isEqualTo(UserRole.MEDICO);
        assertThat(result.get().getServiceId()).isEqualTo(serviceId);
    }

    @Test
    @DisplayName("findById: should return empty Optional when not found")
    void findById_WhenNotFound_ShouldReturnEmpty() {
        when(jpaUserRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = adapter.findById(userId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByServiceIdAndRoleAndActiveTrue: should return mapped User when found")
    void findByServiceIdAndRoleAndActiveTrue_WhenFound_ShouldReturnUser() {
        when(jpaUserRepository.findByServiceIdAndRoleAndActiveTrue(serviceId, "JEFESERVICIO"))
                .thenReturn(Optional.of(testEntity));

        Optional<User> result = adapter.findByServiceIdAndRoleAndActiveTrue(serviceId, UserRole.JEFESERVICIO);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
        verify(jpaUserRepository).findByServiceIdAndRoleAndActiveTrue(serviceId, "JEFESERVICIO");
    }

    @Test
    @DisplayName("findByServiceIdAndRoleAndActiveTrue: should return empty when not found")
    void findByServiceIdAndRoleAndActiveTrue_WhenNotFound_ShouldReturnEmpty() {
        when(jpaUserRepository.findByServiceIdAndRoleAndActiveTrue(serviceId, "MEDICO"))
                .thenReturn(Optional.empty());

        Optional<User> result = adapter.findByServiceIdAndRoleAndActiveTrue(serviceId, UserRole.MEDICO);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("searchBySurnameAndServiceId: should return paginated mapped users")
    void searchBySurnameAndServiceId_ShouldReturnPaginatedResult() {
        Page<UserEntity> page = new PageImpl<>(List.of(testEntity), PageRequest.of(0, 10), 1);
        when(jpaUserRepository.findBySurnameContainingIgnoreCaseAndServiceIdAndActiveTrue(
                eq("Ruiz"), eq(serviceId), any())).thenReturn(page);

        PaginatedResult<User> result = adapter.searchBySurnameAndServiceId("Ruiz", serviceId, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getId()).isEqualTo(userId);
        assertThat(result.content().get(0).getSurname()).isEqualTo("Ruiz");
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.currentPage()).isEqualTo(0);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    @DisplayName("searchBySurnameAndServiceId: empty result returns empty paginated result")
    void searchBySurnameAndServiceId_WhenNoMatch_ShouldReturnEmpty() {
        Page<UserEntity> emptyPage = new PageImpl<>(List.of());
        when(jpaUserRepository.findBySurnameContainingIgnoreCaseAndServiceIdAndActiveTrue(
                any(), eq(serviceId), any())).thenReturn(emptyPage);

        PaginatedResult<User> result = adapter.searchBySurnameAndServiceId("Inexistente", serviceId, 0, 10);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    @DisplayName("calculateUserWorkload: should return paginated workload data")
    void calculateUserWorkload_ShouldReturnPaginatedResult() {
        UserWorkloadProjection projection = mock(UserWorkloadProjection.class);
        when(projection.getName()).thenReturn("Ana");
        when(projection.getSurname()).thenReturn("Martínez");
        when(projection.getAdmissionsAssigned()).thenReturn(5L);

        Page<UserWorkloadProjection> page = new PageImpl<>(
                List.of(projection), PageRequest.of(0, 10), 1);
        when(jpaUserRepository.calculateUserWorkload(eq(serviceId), any())).thenReturn(page);

        PaginatedResult<UserWorkLoad> result = adapter.calculateUserWorkload(serviceId, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getName()).isEqualTo("Ana");
        assertThat(result.content().get(0).getSurname()).isEqualTo("Martínez");
        assertThat(result.content().get(0).getAdmissionsAssigned()).isEqualTo(5L);
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("calculateUserWorkload: empty result returns empty paginated result")
    void calculateUserWorkload_WhenEmpty_ShouldReturnEmpty() {
        Page<UserWorkloadProjection> emptyPage = new PageImpl<>(List.of());
        when(jpaUserRepository.calculateUserWorkload(eq(serviceId), any())).thenReturn(emptyPage);

        PaginatedResult<UserWorkLoad> result = adapter.calculateUserWorkload(serviceId, 0, 10);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }
}
