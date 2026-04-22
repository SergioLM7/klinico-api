package com.sergio.klinico.infraestructure.rest.controllers;

import com.sergio.klinico.application.services.KpiService;
import com.sergio.klinico.domain.models.DoctorKpiSeries;
import com.sergio.klinico.domain.models.MonthlyKpiEntry;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.enums.UserRole;
import com.sergio.klinico.infrastructure.rest.advice.GlobalExceptionHandler;
import com.sergio.klinico.infrastructure.rest.controllers.KpiController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KpiController Tests")
class KpiControllerTest {

    @InjectMocks
    private KpiController kpiController;

    @Mock
    private KpiService kpiService;

    private MockMvc mockMvc;

    private final UUID serviceId = UUID.randomUUID();
    private final UUID doctorId = UUID.randomUUID();
    private final int year = 2025;

    private User authenticatedJefe;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(kpiController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        authenticatedJefe = User.builder()
                .id(doctorId)
                .email("jefe@test.com")
                .name("Elena")
                .surname("Torres")
                .role(UserRole.JEFESERVICIO)
                .active(true)
                .serviceId(serviceId)
                .build();

        var auth = UsernamePasswordAuthenticationToken.authenticated(
                authenticatedJefe, null, authenticatedJefe.getAuthorities());
        var ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return 200 with 12 monthly admission entries when month is not specified")
    void getAdmissionsByService_WithoutMonth_ShouldReturn200With12Entries() throws Exception {
        List<MonthlyKpiEntry> kpiData = List.of(
                new MonthlyKpiEntry(1, 5.0), new MonthlyKpiEntry(2, 3.0),
                new MonthlyKpiEntry(3, 0.0), new MonthlyKpiEntry(4, 7.0),
                new MonthlyKpiEntry(5, 2.0), new MonthlyKpiEntry(6, 4.0),
                new MonthlyKpiEntry(7, 6.0), new MonthlyKpiEntry(8, 1.0),
                new MonthlyKpiEntry(9, 3.0), new MonthlyKpiEntry(10, 5.0),
                new MonthlyKpiEntry(11, 2.0), new MonthlyKpiEntry(12, 8.0)
        );

        when(kpiService.getAdmissionsByService(serviceId, year, null)).thenReturn(kpiData);

        mockMvc.perform(get("/api/v1/kpis/admissions-by-service")
                        .param("year", String.valueOf(year)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(12))
                .andExpect(jsonPath("$[0].month").value(1))
                .andExpect(jsonPath("$[0].value").value(5.0));

        verify(kpiService).getAdmissionsByService(serviceId, year, null);
    }

    @Test
    @DisplayName("Should return 200 with single monthly entry when month is specified")
    void getAdmissionsByService_WithMonth_ShouldReturn200WithSingleEntry() throws Exception {
        int month = 6;
        List<MonthlyKpiEntry> kpiData = List.of(new MonthlyKpiEntry(month, 4.0));

        when(kpiService.getAdmissionsByService(serviceId, year, month)).thenReturn(kpiData);

        mockMvc.perform(get("/api/v1/kpis/admissions-by-service")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value(month))
                .andExpect(jsonPath("$[0].value").value(4.0));

        verify(kpiService).getAdmissionsByService(serviceId, year, month);
    }

    @Test
    @DisplayName("Should return 200 with doctor series when fetching admissions by doctor")
    void getAdmissionsByDoctor_ShouldReturn200WithDoctorSeries() throws Exception {
        List<MonthlyKpiEntry> doctorData = List.of(
                new MonthlyKpiEntry(1, 3.0), new MonthlyKpiEntry(2, 2.0)
        );
        List<DoctorKpiSeries> seriesData = List.of(
                new DoctorKpiSeries(doctorId, "Ana", "Martínez", doctorData)
        );

        when(kpiService.getAdmissionsByDoctor(serviceId, year, null)).thenReturn(seriesData);

        mockMvc.perform(get("/api/v1/kpis/admissions-by-doctor")
                        .param("year", String.valueOf(year)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].doctorName").value("Ana"))
                .andExpect(jsonPath("$[0].doctorSurname").value("Martínez"))
                .andExpect(jsonPath("$[0].data").isArray());

        verify(kpiService).getAdmissionsByDoctor(serviceId, year, null);
    }

    @Test
    @DisplayName("Should return 200 with doctor series for specific month")
    void getAdmissionsByDoctor_WithMonth_ShouldReturn200() throws Exception {
        int month = 3;
        List<DoctorKpiSeries> seriesData = List.of(
                new DoctorKpiSeries(doctorId, "Pedro", "López", List.of(new MonthlyKpiEntry(month, 2.0)))
        );

        when(kpiService.getAdmissionsByDoctor(serviceId, year, month)).thenReturn(seriesData);

        mockMvc.perform(get("/api/v1/kpis/admissions-by-doctor")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorName").value("Pedro"));

        verify(kpiService).getAdmissionsByDoctor(serviceId, year, month);
    }

    @Test
    @DisplayName("Should return 200 with exitus data")
    void getExitus_ShouldReturn200() throws Exception {
        List<MonthlyKpiEntry> kpiData = List.of(
                new MonthlyKpiEntry(1, 1.0), new MonthlyKpiEntry(2, 0.0)
        );

        when(kpiService.getExitus(serviceId, year, null)).thenReturn(kpiData);

        mockMvc.perform(get("/api/v1/kpis/exitus")
                        .param("year", String.valueOf(year)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value(1))
                .andExpect(jsonPath("$[0].value").value(1.0));

        verify(kpiService).getExitus(serviceId, year, null);
    }

    @Test
    @DisplayName("Should return 200 with exitus data for specific month")
    void getExitus_WithMonth_ShouldReturn200() throws Exception {
        int month = 11;
        when(kpiService.getExitus(serviceId, year, month))
                .thenReturn(List.of(new MonthlyKpiEntry(month, 2.0)));

        mockMvc.perform(get("/api/v1/kpis/exitus")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value(2.0));
    }

    @Test
    @DisplayName("Should return 200 with average stay data by service")
    void getAvgStayByService_ShouldReturn200() throws Exception {
        List<MonthlyKpiEntry> kpiData = List.of(
                new MonthlyKpiEntry(1, 7.5), new MonthlyKpiEntry(2, 6.0)
        );

        when(kpiService.getAvgStayByService(serviceId, year, null)).thenReturn(kpiData);

        mockMvc.perform(get("/api/v1/kpis/avg-stay")
                        .param("year", String.valueOf(year)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value(1))
                .andExpect(jsonPath("$[0].value").value(7.5));

        verify(kpiService).getAvgStayByService(serviceId, year, null);
    }

    @Test
    @DisplayName("Should return 200 with average stay data for specific month")
    void getAvgStayByService_WithMonth_ShouldReturn200() throws Exception {
        int month = 5;
        when(kpiService.getAvgStayByService(serviceId, year, month))
                .thenReturn(List.of(new MonthlyKpiEntry(month, 8.2)));

        mockMvc.perform(get("/api/v1/kpis/avg-stay")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value(8.2));
    }

    @Test
    @DisplayName("Should return 200 with average stay per doctor series")
    void getAvgStayByDoctor_ShouldReturn200() throws Exception {
        List<DoctorKpiSeries> seriesData = List.of(
                new DoctorKpiSeries(doctorId, "Laura", "Sánchez",
                        List.of(new MonthlyKpiEntry(1, 5.5)))
        );

        when(kpiService.getAvgStayByDoctor(serviceId, year, null)).thenReturn(seriesData);

        mockMvc.perform(get("/api/v1/kpis/avg-stay-by-doctor")
                        .param("year", String.valueOf(year)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorName").value("Laura"))
                .andExpect(jsonPath("$[0].data[0].value").value(5.5));

        verify(kpiService).getAvgStayByDoctor(serviceId, year, null);
    }

    @Test
    @DisplayName("Should return 200 with average stay per doctor for specific month")
    void getAvgStayByDoctor_WithMonth_ShouldReturn200() throws Exception {
        int month = 7;
        List<DoctorKpiSeries> seriesData = List.of(
                new DoctorKpiSeries(doctorId, "Javier", "Fernández",
                        List.of(new MonthlyKpiEntry(month, 4.3)))
        );

        when(kpiService.getAvgStayByDoctor(serviceId, year, month)).thenReturn(seriesData);

        mockMvc.perform(get("/api/v1/kpis/avg-stay-by-doctor")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorSurname").value("Fernández"));
    }

    @Test
    @DisplayName("Should return 200 with efficiency value when serviceId matches user")
    void getEfficiency_WhenOwnService_ShouldReturn200() throws Exception {
        when(kpiService.getServiceEfficiencyKPI(serviceId)).thenReturn(7.25);

        mockMvc.perform(get("/api/v1/kpis/service-global-efficiency/{id}", serviceId))
                .andExpect(status().isOk())
                .andExpect(content().string("7.25"));

        verify(kpiService).getServiceEfficiencyKPI(serviceId);
    }

    @Test
    @DisplayName("Should return 400 when accessing efficiency of a different service")
    void getEfficiency_WhenDifferentService_ShouldReturn400() throws Exception {
        UUID otherServiceId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/kpis/service-global-efficiency/{id}", otherServiceId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "No tienes permisos para ver las métricas de otro servicio."));

        verify(kpiService, never()).getServiceEfficiencyKPI(any());
    }
}
