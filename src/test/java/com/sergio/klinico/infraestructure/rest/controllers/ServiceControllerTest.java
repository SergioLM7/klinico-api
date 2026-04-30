package com.sergio.klinico.infraestructure.rest.controllers;

import com.sergio.klinico.application.services.FindAllActiveServicesUseCase;
import com.sergio.klinico.domain.models.HospitalService;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.infrastructure.rest.advice.GlobalExceptionHandler;
import com.sergio.klinico.infrastructure.rest.controllers.ServiceController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceController Tests")
class ServiceControllerTest {

    @InjectMocks
    private ServiceController serviceController;

    @Mock
    private FindAllActiveServicesUseCase findAllActiveServicesUseCase;

    private MockMvc mockMvc;

    private final UUID serviceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(serviceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 200 with paginated service list matching name filter")
    void searchByName_WhenServicesMatch_ShouldReturn200WithServices() throws Exception {
        String name = "Cardiología";

        HospitalService hospitalService = HospitalService.builder()
                .serviceId(serviceId)
                .name("Cardiología")
                .active(true)
                .build();

        PaginatedResult<HospitalService> paginatedResult = new PaginatedResult<>(
                List.of(hospitalService), 1L, 1, 0, true
        );

        when(findAllActiveServicesUseCase.execute(name, 0, 5)).thenReturn(paginatedResult);

        mockMvc.perform(get("/api/v1/services/search")
                        .param("name", name)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].serviceId").value(serviceId.toString()))
                .andExpect(jsonPath("$.data[0].name").value("Cardiología"))
                .andExpect(jsonPath("$.data[0].active").value(true))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.last").value(true));

        verify(findAllActiveServicesUseCase).execute(name, 0, 5);
    }

    @Test
    @DisplayName("Should return 200 with empty data when no services match the filter")
    void searchByName_WhenNoServicesMatch_ShouldReturn200WithEmptyData() throws Exception {
        String name = "Inexistente";

        PaginatedResult<HospitalService> emptyResult = new PaginatedResult<>(
                List.of(), 0L, 0, 0, true
        );

        when(findAllActiveServicesUseCase.execute(name, 0, 5)).thenReturn(emptyResult);

        mockMvc.perform(get("/api/v1/services/search")
                        .param("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(findAllActiveServicesUseCase).execute(name, 0, 5);
    }

    @Test
    @DisplayName("Should use default pagination values when page and size are not provided")
    void searchByName_WhenNoPaginationProvided_ShouldUseDefaults() throws Exception {
        String name = "Medicina";

        PaginatedResult<HospitalService> paginatedResult = new PaginatedResult<>(
                List.of(), 0L, 0, 0, true
        );

        when(findAllActiveServicesUseCase.execute(name, 0, 5)).thenReturn(paginatedResult);

        mockMvc.perform(get("/api/v1/services/search").param("name", name))
                .andExpect(status().isOk());

        verify(findAllActiveServicesUseCase).execute(name, 0, 5);
    }

    @Test
    @DisplayName("Should return 200 with multiple services matching partial name")
    void searchByName_WithPartialName_ShouldReturnMatchingServices() throws Exception {
        String name = "Med";

        HospitalService service1 = HospitalService.builder()
                .serviceId(UUID.randomUUID()).name("Medicina Interna").active(true).build();
        HospitalService service2 = HospitalService.builder()
                .serviceId(UUID.randomUUID()).name("Medicina Intensiva").active(true).build();

        PaginatedResult<HospitalService> paginatedResult = new PaginatedResult<>(
                List.of(service1, service2), 2L, 1, 0, true
        );

        when(findAllActiveServicesUseCase.execute(name, 0, 5)).thenReturn(paginatedResult);

        mockMvc.perform(get("/api/v1/services/search")
                        .param("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}
