package com.sergio.klinico.infraestructure.rest.controllers;

import com.sergio.klinico.application.services.AdmissionService;
import com.sergio.klinico.application.services.FindJefeServicioByServiceIdUseCase;
import com.sergio.klinico.application.services.FindUserByIdUseCase;
import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Admission;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.enums.UserRole;
import com.sergio.klinico.infrastructure.mappers.AdmissionMapper;
import com.sergio.klinico.infrastructure.rest.advice.GlobalExceptionHandler;
import com.sergio.klinico.infrastructure.rest.controllers.AdmissionController;
import com.sergio.klinico.infrastructure.rest.dto.requests.AdmissionRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.admission.AdmissionResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.admission.AdmissionSummaryResponse;
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
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdmissionController Tests")
class AdmissionControllerTest {

    @InjectMocks
    private AdmissionController admissionController;

    @Mock
    private AdmissionService admissionService;

    @Mock
    private AdmissionMapper admissionMapper;

    @Mock
    private FindUserByIdUseCase findUserByIdUseCase;

    @Mock
    private FindJefeServicioByServiceIdUseCase findJefeServicioByServiceIdUseCase;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final UUID serviceId = UUID.randomUUID();
    private final UUID admissionId = UUID.randomUUID();
    private final UUID patientId = UUID.randomUUID();
    private final UUID doctorId = UUID.randomUUID();

    private User authenticatedUser;
    private Admission testAdmission;
    private AdmissionResponse testAdmissionResponse;
    private AdmissionSummaryResponse testSummaryResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(admissionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        authenticatedUser = User.builder()
                .id(doctorId)
                .email("medico@test.com")
                .name("Carlos")
                .surname("Ruiz")
                .role(UserRole.MEDICO)
                .active(true)
                .serviceId(serviceId)
                .build();

        var auth = UsernamePasswordAuthenticationToken.authenticated(
                authenticatedUser, null, authenticatedUser.getAuthorities());
        var ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);

        testAdmission = Admission.builder()
                .admissionId(admissionId)
                .patientId(patientId)
                .serviceId(serviceId)
                .assignedDoctorId(doctorId)
                .build();

        testAdmissionResponse = AdmissionResponse.builder()
                .admissionId(admissionId)
                .serviceId(serviceId)
                .assignedDoctorId(doctorId)
                .build();

        testSummaryResponse = new AdmissionSummaryResponse(
                admissionId, patientId, serviceId, doctorId,
                "Diagnóstico principal", null, null, null
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return 200 with admissions when doctor belongs to same service as authenticated user")
    void getActiveByDoctorId_WhenSameService_ShouldReturn200() throws Exception {
        User assignedDoctor = User.builder()
                .id(doctorId)
                .serviceId(serviceId)
                .active(true)
                .build();

        PaginatedResult<Admission> paginatedResult = new PaginatedResult<>(
                List.of(testAdmission), 1L, 1, 0, true
        );

        when(findUserByIdUseCase.execute(doctorId)).thenReturn(assignedDoctor);
        when(admissionService.getActiveByDoctorId(doctorId, 0)).thenReturn(paginatedResult);
        when(admissionService.loadPatientMapForAdmissions(any())).thenReturn(Map.of());
        when(admissionMapper.toResponseFromDomain(any(Admission.class), any())).thenReturn(testAdmissionResponse);

        mockMvc.perform(get("/api/v1/admissions/doctor/{id}", doctorId)
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].admissionId").value(admissionId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(findUserByIdUseCase).execute(doctorId);
        verify(admissionService).getActiveByDoctorId(doctorId, 0);
    }

    @Test
    @DisplayName("Should return 400 when authenticated user tries to access admissions of doctor from another service")
    void getActiveByDoctorId_WhenDifferentService_ShouldReturn400() throws Exception {
        UUID otherServiceId = UUID.randomUUID();
        User doctorFromOtherService = User.builder()
                .id(doctorId)
                .serviceId(otherServiceId)
                .active(true)
                .build();

        when(findUserByIdUseCase.execute(doctorId)).thenReturn(doctorFromOtherService);

        mockMvc.perform(get("/api/v1/admissions/doctor/{id}", doctorId)
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "No tienes permisos para acceder a las admisiones de un médico de otro servicio"));

        verify(admissionService, never()).getActiveByDoctorId(any(), anyInt());
    }

    @Test
    @DisplayName("Should return 200 with admissions when user accesses their own service")
    void getActiveByServiceId_WhenOwnService_ShouldReturn200() throws Exception {
        PaginatedResult<Admission> paginatedResult = new PaginatedResult<>(
                List.of(testAdmission), 1L, 1, 0, true
        );

        when(admissionService.getActiveByServiceId(serviceId, 0)).thenReturn(paginatedResult);
        when(admissionService.loadPatientMapForAdmissions(any())).thenReturn(Map.of());
        when(admissionMapper.toResponseFromDomain(any(Admission.class), any())).thenReturn(testAdmissionResponse);

        mockMvc.perform(get("/api/v1/admissions/service/{id}", serviceId)
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].admissionId").value(admissionId.toString()));

        verify(admissionService).getActiveByServiceId(serviceId, 0);
    }

    @Test
    @DisplayName("Should return 400 when user tries to access admissions of a different service")
    void getActiveByServiceId_WhenDifferentService_ShouldReturn400() throws Exception {
        UUID otherServiceId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/admissions/service/{id}", otherServiceId)
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "No tienes permisos para acceder a las admisiones de otro servicio"));

        verify(admissionService, never()).getActiveByServiceId(any(), anyInt());
    }

    @Test
    @DisplayName("Should return 200 with all active admissions")
    void getAllActive_ShouldReturn200() throws Exception {
        PaginatedResult<Admission> paginatedResult = new PaginatedResult<>(
                List.of(testAdmission), 1L, 1, 0, true
        );

        when(admissionService.getAllActive(0)).thenReturn(paginatedResult);
        when(admissionService.loadPatientMapForAdmissions(any())).thenReturn(Map.of());
        when(admissionMapper.toResponseFromDomain(any(Admission.class), any())).thenReturn(testAdmissionResponse);

        mockMvc.perform(get("/api/v1/admissions").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(admissionService).getAllActive(0);
    }

    @Test
    @DisplayName("Should return 200 with admissions matching patient surname")
    void searchByPatientSurname_ShouldReturn200() throws Exception {
        String surname = "García";
        PaginatedResult<Admission> paginatedResult = new PaginatedResult<>(
                List.of(testAdmission), 1L, 1, 0, true
        );

        when(admissionService.searchByPatientSurnameAndServiceId(surname, serviceId, 0))
                .thenReturn(paginatedResult);
        when(admissionService.loadPatientMapForAdmissions(any())).thenReturn(Map.of());
        when(admissionMapper.toResponseFromDomain(any(Admission.class), any())).thenReturn(testAdmissionResponse);

        mockMvc.perform(get("/api/v1/admissions/search")
                        .param("surname", surname)
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        verify(admissionService).searchByPatientSurnameAndServiceId(surname, serviceId, 0);
    }

    @Test
    @DisplayName("Should return 201 with AdmissionSummaryResponse when creating admission successfully")
    void create_WhenValidRequest_ShouldReturn201() throws Exception {
        AdmissionRequest request = AdmissionRequest.builder()
                .patientId(patientId)
                .serviceId(serviceId)
                .principalDiagnosis("Diagnóstico")
                .medicalHistory("Sin antecedentes")
                .build();

        User jefeServicio = User.builder()
                .id(doctorId)
                .serviceId(serviceId)
                .build();

        when(admissionMapper.toDomainFromRequest(any(AdmissionRequest.class))).thenReturn(testAdmission);
        when(findJefeServicioByServiceIdUseCase.execute(serviceId)).thenReturn(jefeServicio);
        when(admissionService.create(any(Admission.class))).thenReturn(testAdmission);
        when(admissionMapper.toSummaryResponseFromDomain(any(Admission.class))).thenReturn(testSummaryResponse);

        mockMvc.perform(post("/api/v1/admissions/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.admissionId").value(admissionId.toString()))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()));

        verify(admissionService).create(any(Admission.class));
        verify(admissionMapper).toSummaryResponseFromDomain(any(Admission.class));
    }

    @Test
    @DisplayName("Should return 400 when creating admission throws BusinessException")
    void create_WhenBusinessException_ShouldReturn400() throws Exception {
        AdmissionRequest request = AdmissionRequest.builder()
                .patientId(patientId)
                .serviceId(serviceId)
                .build();

        User jefeServicio = User.builder().id(doctorId).serviceId(serviceId).build();

        when(admissionMapper.toDomainFromRequest(any(AdmissionRequest.class))).thenReturn(testAdmission);
        when(findJefeServicioByServiceIdUseCase.execute(serviceId)).thenReturn(jefeServicio);
        when(admissionService.create(any(Admission.class)))
                .thenThrow(new BusinessException("Paciente asignado a la admisión no encontrado"));

        mockMvc.perform(post("/api/v1/admissions/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Paciente asignado a la admisión no encontrado"));
    }

    @Test
    @DisplayName("Should return 200 with updated AdmissionResponse when assigning room successfully")
    void assignRoom_WhenValidRequest_ShouldReturn200() throws Exception {
        when(admissionService.assignRoom(admissionId, 202)).thenReturn(testAdmission);
        when(admissionMapper.toResponseFromDomain(any(Admission.class))).thenReturn(testAdmissionResponse);

        mockMvc.perform(patch("/api/v1/admissions/assign-room/{id}", admissionId)
                        .param("roomNumber", "202"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admissionId").value(admissionId.toString()));

        verify(admissionService).assignRoom(admissionId, 202);
    }

    @Test
    @DisplayName("Should return 400 when assigning room to non-existent admission")
    void assignRoom_WhenAdmissionNotFound_ShouldReturn400() throws Exception {
        when(admissionService.assignRoom(admissionId, 101))
                .thenThrow(new BusinessException("La admisión a la que está intentando asignar un nº de habitación no existe"));

        mockMvc.perform(patch("/api/v1/admissions/assign-room/{id}", admissionId)
                        .param("roomNumber", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "La admisión a la que está intentando asignar un nº de habitación no existe"));
    }

    @Test
    @DisplayName("Should return 200 with updated AdmissionResponse when reassigning doctor successfully")
    void assignDoctor_WhenValidRequest_ShouldReturn200() throws Exception {
        UUID newDoctorId = UUID.randomUUID();

        when(admissionService.reassignDoctor(admissionId, newDoctorId)).thenReturn(testAdmission);
        when(admissionMapper.toResponseFromDomain(any(Admission.class))).thenReturn(testAdmissionResponse);

        mockMvc.perform(patch("/api/v1/admissions/assign-doctor/{id}", admissionId)
                        .param("doctorId", newDoctorId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admissionId").value(admissionId.toString()));

        verify(admissionService).reassignDoctor(admissionId, newDoctorId);
    }

    @Test
    @DisplayName("Should return 400 when reassigning doctor to non-existent admission")
    void assignDoctor_WhenAdmissionNotFound_ShouldReturn400() throws Exception {
        UUID newDoctorId = UUID.randomUUID();

        when(admissionService.reassignDoctor(admissionId, newDoctorId))
                .thenThrow(new BusinessException("La admisión a la que está intentando reasignar el médico no existe"));

        mockMvc.perform(patch("/api/v1/admissions/assign-doctor/{id}", admissionId)
                        .param("doctorId", newDoctorId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "La admisión a la que está intentando reasignar el médico no existe"));
    }

    @Test
    @DisplayName("Should return 200 with updated AdmissionResponse when updating clinical info successfully")
    void updateClinicalInfo_WhenValidRequest_ShouldReturn200() throws Exception {
        AdmissionRequest request = AdmissionRequest.builder()
                .principalDiagnosis("Nuevo diagnóstico")
                .allergies("Penicilina")
                .build();

        when(admissionMapper.toDomainFromRequest(any(AdmissionRequest.class))).thenReturn(testAdmission);
        when(admissionService.update(eq(admissionId), any(Admission.class))).thenReturn(testAdmission);
        when(admissionMapper.toResponseFromDomain(any(Admission.class))).thenReturn(testAdmissionResponse);

        mockMvc.perform(put("/api/v1/admissions/clinical-update/{id}", admissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admissionId").value(admissionId.toString()));

        verify(admissionService).update(eq(admissionId), any(Admission.class));
    }

    @Test
    @DisplayName("Should return 400 when updating clinical info for non-existent admission")
    void updateClinicalInfo_WhenAdmissionNotFound_ShouldReturn400() throws Exception {
        AdmissionRequest request = AdmissionRequest.builder().principalDiagnosis("Diagnóstico").build();

        when(admissionMapper.toDomainFromRequest(any(AdmissionRequest.class))).thenReturn(testAdmission);
        when(admissionService.update(eq(admissionId), any(Admission.class)))
                .thenThrow(new BusinessException("La admisión que intenta actualizar no existe en BD"));

        mockMvc.perform(put("/api/v1/admissions/clinical-update/{id}", admissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La admisión que intenta actualizar no existe en BD"));
    }

    @Test
    @DisplayName("Should return 200 with discharged AdmissionResponse")
    void discharge_WhenAdmissionExists_ShouldReturn200() throws Exception {
        when(admissionService.dischargeAdmission(admissionId)).thenReturn(testAdmission);
        when(admissionMapper.toResponseFromDomain(any(Admission.class))).thenReturn(testAdmissionResponse);

        mockMvc.perform(post("/api/v1/admissions/discharge/{id}", admissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admissionId").value(admissionId.toString()));

        verify(admissionService).dischargeAdmission(admissionId);
    }

    @Test
    @DisplayName("Should return 400 when discharging non-existent admission")
    void discharge_WhenAdmissionNotFound_ShouldReturn400() throws Exception {
        when(admissionService.dischargeAdmission(admissionId))
                .thenThrow(new BusinessException("La admisión a la que está intentando dar de alta no existe"));

        mockMvc.perform(post("/api/v1/admissions/discharge/{id}", admissionId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "La admisión a la que está intentando dar de alta no existe"));
    }
}
