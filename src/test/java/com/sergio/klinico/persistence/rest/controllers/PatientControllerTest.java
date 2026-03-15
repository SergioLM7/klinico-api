package com.sergio.klinico.persistence.rest.controllers;

import com.sergio.klinico.application.services.PatientService;
import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.domain.models.enums.PatientStatus;
import com.sergio.klinico.infrastructure.mappers.PatientMapper;
import com.sergio.klinico.infrastructure.rest.controllers.PatientController;
import com.sergio.klinico.infrastructure.rest.dto.requests.PatientRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.patient.PatientResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.patient.PatientSummaryResponse;
import com.sergio.klinico.infrastructure.rest.advice.GlobalExceptionHandler;
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
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientController Tests")
class PatientControllerTest {

    @InjectMocks
    private PatientController patientController;

    @Mock
    private PatientService patientService;

    @Mock
    private PatientMapper patientMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    private Patient testPatient;
    private PatientRequest testPatientRequest;
    private PatientResponse testPatientResponse;
    private PatientSummaryResponse testPatientSummaryResponse;
    private final UUID patientId = UUID.randomUUID();
    private final String testDni = "12345678A";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(patientController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        testPatient = Patient.builder()
                .patientId(patientId)
                .dni(testDni)
                .name("Juan")
                .surname("García López")
                .sex('M')
                .birthdate(LocalDate.of(1990, 1, 1))
                .address("C/Amargura, 1, 38000, Madrid")
                .contactNumber("600000000")
                .relativeContactNumber("610100100")
                .status(PatientStatus.INGRESADO)
                .build();

        testPatientRequest = PatientRequest.builder()
                .dni(testDni)
                .name("Juan")
                .surname("García López")
                .sex("M")
                .birthdate(LocalDate.of(1990, 1, 1))
                .address("C/Amargura, 1, 38000, Madrid")
                .contactNumber("600000000")
                .relativeContactNumber("610100100")
                .status("INGRESADO")
                .build();

        testPatientResponse = PatientResponse.builder()
                .patientId(patientId)
                .dni(testDni)
                .name("Juan")
                .surname("García López")
                .sex('M')
                .birthdate(LocalDate.of(1990, 1, 1))
                .address("C/Amargura, 1, 38000, Madrid")
                .contactNumber("600000000")
                .relativeContactNumber("610100100")
                .status(PatientStatus.INGRESADO)
                .build();

        testPatientSummaryResponse = new PatientSummaryResponse(
                patientId,
                testDni,
                "Juan",
                "García López",
                PatientStatus.INGRESADO
        );
    }

    @Test
    @DisplayName("Should return 201 and PatientSummaryResponse when creating patient successfully")
    void create_WhenValidRequest_ShouldReturn201AndPatientSummaryResponse() throws Exception {
        when(patientMapper.toDomainFromDto(any(PatientRequest.class))).thenReturn(testPatient);
        when(patientService.create(any(Patient.class))).thenReturn(testPatient);
        when(patientMapper.toSummaryResponseFromDomain(any(Patient.class))).thenReturn(testPatientSummaryResponse);

        mockMvc.perform(post("/api/v1/patients/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatientRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.dni").value(testDni))
                .andExpect(jsonPath("$.name").value("Juan"))
                .andExpect(jsonPath("$.surname").value("García López"))
                .andExpect(jsonPath("$.status").value("INGRESADO"));

        verify(patientMapper).toDomainFromDto(any(PatientRequest.class));
        verify(patientService).create(any(Patient.class));
        verify(patientMapper).toSummaryResponseFromDomain(any(Patient.class));
    }

    @Test
    @DisplayName("Should return 400 when DNI is invalid during creation")
    void create_WhenDniIsInvalid_ShouldReturn400() throws Exception {
        PatientRequest invalidRequest = PatientRequest.builder()
                .dni("123")
                .name("Juan")
                .surname("García")
                .sex("M")
                .birthdate(LocalDate.of(1990, 1, 1))
                .status("INGRESADO")
                .build();

        mockMvc.perform(post("/api/v1/patients/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Formato de DNI no válido")));

        verify(patientMapper, never()).toDomainFromDto(any(PatientRequest.class));
        verify(patientService, never()).create(any(Patient.class));
    }

    @Test
    @DisplayName("Should return 400 when required fields are missing during creation")
    void create_WhenRequiredFieldsMissing_ShouldReturn400() throws Exception {
        PatientRequest invalidRequest = new PatientRequest();

        mockMvc.perform(post("/api/v1/patients/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("El DNI es obligatorio")))
                .andExpect(jsonPath("$.message").value(containsString("El nombre es obligatorio")))
                .andExpect(jsonPath("$.message").value(containsString("El apellido es obligatorio")))
                .andExpect(jsonPath("$.message").value(containsString("El sexo es obligatorio")))
                .andExpect(jsonPath("$.message").value(containsString("La fecha de nacimiento es obligatoria")))
                .andExpect(jsonPath("$.message").value(containsString("El estado inicial es obligatorio")));

        verify(patientMapper, never()).toDomainFromDto(any(PatientRequest.class));
        verify(patientService, never()).create(any(Patient.class));
    }

    @Test
    @DisplayName("Should return 400 when patient with DNI already exists")
    void create_WhenDniAlreadyExists_ShouldReturn400() throws Exception {
        when(patientMapper.toDomainFromDto(any(PatientRequest.class))).thenReturn(testPatient);
        when(patientService.create(any(Patient.class)))
                .thenThrow(new BusinessException("Ya existe un paciente registrado con el DNI: " + testDni));

        mockMvc.perform(post("/api/v1/patients/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatientRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ya existe un paciente registrado con el DNI: " + testDni));

        verify(patientMapper).toDomainFromDto(any(PatientRequest.class));
        verify(patientService).create(any(Patient.class));
    }

    @Test
    @DisplayName("Should return 200 and PatientPageResponse when getting all patients")
    void findAll_WhenCalled_ShouldReturn200AndPatientPageResponse() throws Exception {
        PaginatedResult<Patient> paginatedResult = new PaginatedResult<>(
                List.of(testPatient),
                1L,
                1,
                0,
                true
        );

        when(patientService.getAllPaginated(0, 10)).thenReturn(paginatedResult);
        when(patientMapper.toResponseFromDomain(any(Patient.class))).thenReturn(testPatientResponse);

        mockMvc.perform(get("/api/v1/patients")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.data[0].dni").value(testDni))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.last").value(true));

        verify(patientService).getAllPaginated(0, 10);
        verify(patientMapper).toResponseFromDomain(any(Patient.class));
    }

    @Test
    @DisplayName("Should return 200 and PatientResponse when finding patient by ID")
    void findById_WhenPatientExists_ShouldReturn200AndPatientResponse() throws Exception {
        when(patientService.getById(patientId)).thenReturn(testPatient);
        when(patientMapper.toResponseFromDomain(any(Patient.class))).thenReturn(testPatientResponse);

        mockMvc.perform(get("/api/v1/patients/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.dni").value(testDni))
                .andExpect(jsonPath("$.name").value("Juan"))
                .andExpect(jsonPath("$.surname").value("García López"))
                .andExpect(jsonPath("$.status").value("INGRESADO"));

        verify(patientService).getById(patientId);
        verify(patientMapper).toResponseFromDomain(any(Patient.class));
    }

    @Test
    @DisplayName("Should return 400 when patient ID is not found")
    void findById_WhenPatientNotFound_ShouldReturn400() throws Exception {
        when(patientService.getById(patientId))
                .thenThrow(new BusinessException("Paciente no encontrado"));

        mockMvc.perform(get("/api/v1/patients/{id}", patientId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Paciente no encontrado"));

        verify(patientService).getById(patientId);
        verify(patientMapper, never()).toResponseFromDomain(any(Patient.class));
    }

    @Test
    @DisplayName("Should return 200 and PatientResponse when updating patient successfully")
    void update_WhenValidRequest_ShouldReturn200AndPatientResponse() throws Exception {
        PatientRequest updateRequest = PatientRequest.builder()
                .name("Juan Carlos")
                .surname("García Pérez")
                .address("C/Nueva Dirección, 123")
                .contactNumber("611111111")
                .build();

        Patient updatedPatient = Patient.builder()
                .patientId(patientId)
                .dni(testDni)
                .name("Juan Carlos")
                .surname("García Pérez")
                .address("C/Nueva Dirección, 123")
                .contactNumber("611111111")
                .status(PatientStatus.INGRESADO)
                .build();

        PatientResponse updatedResponse = PatientResponse.builder()
                .patientId(patientId)
                .dni(testDni)
                .name("Juan Carlos")
                .surname("García Pérez")
                .address("C/Nueva Dirección, 123")
                .contactNumber("611111111")
                .status(PatientStatus.INGRESADO)
                .build();

        when(patientMapper.toDomainFromDto(any(PatientRequest.class))).thenReturn(updatedPatient);
        when(patientService.update(any(Patient.class))).thenReturn(updatedPatient);
        when(patientMapper.toResponseFromDomain(any(Patient.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/patients/update/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.name").value("Juan Carlos"))
                .andExpect(jsonPath("$.surname").value("García Pérez"))
                .andExpect(jsonPath("$.address").value("C/Nueva Dirección, 123"))
                .andExpect(jsonPath("$.contactNumber").value("611111111"));

        verify(patientMapper).toDomainFromDto(any(PatientRequest.class));
        verify(patientService).update(any(Patient.class));
        verify(patientMapper).toResponseFromDomain(any(Patient.class));
    }

    @Test
    @DisplayName("Should return 400 when updating non-existent patient")
    void update_WhenPatientDoesNotExist_ShouldReturn400() throws Exception {
        PatientRequest updateRequest = PatientRequest.builder()
                .name("Juan Carlos")
                .build();

        when(patientMapper.toDomainFromDto(any(PatientRequest.class))).thenReturn(testPatient);
        when(patientService.update(any(Patient.class)))
                .thenThrow(new BusinessException("Paciente no encontrado"));

        mockMvc.perform(put("/api/v1/patients/update/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Paciente no encontrado"));

        verify(patientMapper).toDomainFromDto(any(PatientRequest.class));
        verify(patientService).update(any(Patient.class));
        verify(patientMapper, never()).toResponseFromDomain(any(Patient.class));
    }

    @Test
    @DisplayName("Should return 400 when update request has invalid data")
    void update_WhenInvalidData_ShouldReturn400() throws Exception {
        PatientRequest invalidRequest = PatientRequest.builder()
                .name("A")
                .surname("B")
                .sex("X")
                .contactNumber("123")
                .build();

        mockMvc.perform(put("/api/v1/patients/update/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("El sexo debe ser 'M' o 'F'")))
                .andExpect(jsonPath("$.message").value(containsString("Número de contacto no válido")));

        verify(patientMapper, never()).toDomainFromDto(any(PatientRequest.class));
        verify(patientService, never()).update(any(Patient.class));
    }

    @Test
    @DisplayName("Should return 400 when request body is missing in update")
    void update_WhenRequestBodyIsMissing_ShouldReturn400() throws Exception {
        mockMvc.perform(put("/api/v1/patients/update/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El cuerpo de la solicitud es requerido y debe estar en formato JSON válido"));

        verify(patientMapper, never()).toDomainFromDto(any(PatientRequest.class));
        verify(patientService, never()).update(any(Patient.class));
    }
}
