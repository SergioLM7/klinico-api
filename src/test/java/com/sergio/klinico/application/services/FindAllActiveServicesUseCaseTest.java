package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.models.HospitalService;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.repositories.ServiceRepository;
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
@DisplayName("FindAllActiveServicesUseCase Tests")
class FindAllActiveServicesUseCaseTest {

    @InjectMocks
    private FindAllActiveServicesUseCase findAllActiveServicesUseCase;

    @Mock
    private ServiceRepository serviceRepository;

    @Test
    @DisplayName("Should return paginated active services matching name filter")
    void execute_ShouldReturnPaginatedActiveServices() {
        String name = "Cardiología";
        int page = 0;
        int size = 10;

        HospitalService service = HospitalService.builder()
                .serviceId(UUID.randomUUID())
                .name("Cardiología")
                .active(true)
                .build();

        PaginatedResult<HospitalService> expected = new PaginatedResult<>(List.of(service), 1L, 1, page, true);

        when(serviceRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, page, size)).thenReturn(expected);

        PaginatedResult<HospitalService> result = findAllActiveServicesUseCase.execute(name, page, size);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals("Cardiología", result.content().getFirst().getName());
        assertTrue(result.content().getFirst().isActive());
        verify(serviceRepository).findByNameContainingIgnoreCaseAndActiveTrue(name, page, size);
    }

    @Test
    @DisplayName("Should return empty result when no services match the name filter")
    void execute_WhenNoServicesMatch_ShouldReturnEmptyResult() {
        String name = "Inexistente";
        int page = 0;
        int size = 10;

        PaginatedResult<HospitalService> emptyResult = new PaginatedResult<>(List.of(), 0L, 0, page, true);

        when(serviceRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, page, size)).thenReturn(emptyResult);

        PaginatedResult<HospitalService> result = findAllActiveServicesUseCase.execute(name, page, size);

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0L, result.totalElements());
        verify(serviceRepository).findByNameContainingIgnoreCaseAndActiveTrue(name, page, size);
    }

    @Test
    @DisplayName("Should pass correct pagination parameters to repository")
    void execute_ShouldPassCorrectPaginationParameters() {
        String name = "Neurología";
        int page = 2;
        int size = 5;

        PaginatedResult<HospitalService> expected = new PaginatedResult<>(List.of(), 0L, 0, page, true);
        when(serviceRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, page, size)).thenReturn(expected);

        findAllActiveServicesUseCase.execute(name, page, size);

        verify(serviceRepository).findByNameContainingIgnoreCaseAndActiveTrue(name, page, size);
        verifyNoMoreInteractions(serviceRepository);
    }
}
