package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Admission;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.enums.PatientStatus;
import com.sergio.klinico.domain.models.enums.UserRole;
import com.sergio.klinico.domain.repositories.AdmissionRepository;
import com.sergio.klinico.domain.repositories.PatientRepository;
import com.sergio.klinico.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdmissionService Tests")
class AdmissionServiceTest {

    @InjectMocks
    private AdmissionService admissionService;

    @Mock
    private AdmissionRepository admissionRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    private UUID admissionId;
    private UUID patientId;
    private UUID serviceId;
    private UUID doctorId;
    private Patient altaPatient;
    private Admission activeAdmission;

    @BeforeEach
    void setUp() {
        admissionId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        serviceId = UUID.randomUUID();
        doctorId = UUID.randomUUID();

        altaPatient = Patient.builder()
                .patientId(patientId)
                .dni("12345678A")
                .name("María")
                .surname("González Torres")
                .sex('F')
                .birthdate(LocalDate.of(1980, 5, 15))
                .build();
        altaPatient.applyStatusChange(PatientStatus.ALTA);

        activeAdmission = Admission.builder()
                .admissionId(admissionId)
                .patientId(patientId)
                .serviceId(serviceId)
                .assignedDoctorId(doctorId)
                .build();
    }

    @Test
    @DisplayName("Should create admission when patient exists and is eligible")
    void create_WhenPatientEligible_ShouldCreateAdmission() {
        Admission newAdmission = Admission.builder()
                .patientId(patientId)
                .serviceId(serviceId)
                .assignedDoctorId(doctorId)
                .build();

        when(patientRepository.findById(patientId)).thenReturn(altaPatient);
        when(admissionRepository.existsActiveAdmissionByPatientId(patientId)).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(altaPatient);
        when(admissionRepository.save(any(Admission.class))).thenReturn(newAdmission);

        Admission result = admissionService.create(newAdmission);

        assertNotNull(result);
        verify(patientRepository).findById(patientId);
        verify(admissionRepository).existsActiveAdmissionByPatientId(patientId);
        verify(patientRepository).save(altaPatient);
        verify(admissionRepository).save(newAdmission);
    }

    @Test
    @DisplayName("Should throw BusinessException when patient does not exist")
    void create_WhenPatientNotFound_ShouldThrowBusinessException() {
        Admission newAdmission = Admission.builder().patientId(patientId).build();

        when(patientRepository.findById(patientId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                admissionService.create(newAdmission)
        );

        assertEquals("Paciente asignado a la admisión no encontrado", exception.getMessage());
        verify(patientRepository).findById(patientId);
        verify(admissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when patient status is EXITUS")
    void create_WhenPatientIsExitus_ShouldThrowBusinessException() {
        Patient exitusPatient = Patient.builder()
                .patientId(patientId)
                .build();
        exitusPatient.applyStatusChange(PatientStatus.EXITUS);

        Admission newAdmission = Admission.builder().patientId(patientId).build();

        when(patientRepository.findById(patientId)).thenReturn(exitusPatient);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                admissionService.create(newAdmission)
        );

        assertEquals("No se puede crear una nueva admisión para un paciente con estado EXITUS", exception.getMessage());
        verify(admissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when patient is already INGRESADO")
    void create_WhenPatientIsIngresado_ShouldThrowBusinessException() {
        Patient ingresadoPatient = Patient.builder()
                .patientId(patientId)
                .build();
        ingresadoPatient.applyStatusChange(PatientStatus.INGRESADO);

        Admission newAdmission = Admission.builder().patientId(patientId).build();

        when(patientRepository.findById(patientId)).thenReturn(ingresadoPatient);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                admissionService.create(newAdmission)
        );

        assertEquals("No se puede crear una nueva admisión para un paciente que sigue ingresado", exception.getMessage());
        verify(admissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when active admission already exists for patient")
    void create_WhenActiveAdmissionExists_ShouldThrowBusinessException() {
        Admission newAdmission = Admission.builder().patientId(patientId).build();

        when(patientRepository.findById(patientId)).thenReturn(altaPatient);
        when(admissionRepository.existsActiveAdmissionByPatientId(patientId)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                admissionService.create(newAdmission)
        );

        assertEquals("Ya existe una admisión activa para el paciente solicitado. No se puede crear una nueva", exception.getMessage());
        verify(admissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should assign room when admission exists and has no room yet")
    void assignRoom_WhenAdmissionExistsAndNoRoomAssigned_ShouldAssignRoom() {
        when(admissionRepository.findById(admissionId)).thenReturn(activeAdmission);
        when(admissionRepository.save(any(Admission.class))).thenReturn(activeAdmission);

        Admission result = admissionService.assignRoom(admissionId, 202);

        assertNotNull(result);
        verify(admissionRepository).findById(admissionId);
        verify(admissionRepository).save(activeAdmission);
    }

    @Test
    @DisplayName("Should throw BusinessException when admission does not exist during room assignment")
    void assignRoom_WhenAdmissionNotFound_ShouldThrowBusinessException() {
        when(admissionRepository.findById(admissionId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                admissionService.assignRoom(admissionId, 101)
        );

        assertEquals("La admisión a la que está intentando asignar un nº de habitación no existe", exception.getMessage());
        verify(admissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when trying to assign room to discharged admission")
    void assignRoom_WhenAdmissionIsDischarged_ShouldThrowBusinessException() {
        Admission dischargedAdmission = Admission.builder()
                .admissionId(admissionId)
                .dischargeDate(LocalDateTime.now().minusDays(1))
                .build();

        when(admissionRepository.findById(admissionId)).thenReturn(dischargedAdmission);

        assertThrows(BusinessException.class, () ->
                admissionService.assignRoom(admissionId, 101)
        );

        verify(admissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when room is already assigned")
    void assignRoom_WhenRoomAlreadyAssigned_ShouldThrowBusinessException() {
        Admission admissionWithRoom = Admission.builder()
                .admissionId(admissionId)
                .roomNumber(101)
                .build();

        when(admissionRepository.findById(admissionId)).thenReturn(admissionWithRoom);

        assertThrows(BusinessException.class, () ->
                admissionService.assignRoom(admissionId, 202)
        );

        verify(admissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reassign doctor when admission and new doctor exist and belong to same service")
    void reassignDoctor_WhenValidDoctorAndSameService_ShouldReassignDoctor() {
        UUID newDoctorId = UUID.randomUUID();
        User newDoctor = User.builder()
                .id(newDoctorId)
                .role(UserRole.MEDICO)
                .active(true)
                .serviceId(serviceId)
                .build();

        when(admissionRepository.findById(admissionId)).thenReturn(activeAdmission);
        when(userRepository.findById(newDoctorId)).thenReturn(Optional.of(newDoctor));
        when(admissionRepository.save(any(Admission.class))).thenReturn(activeAdmission);

        Admission result = admissionService.reassignDoctor(admissionId, newDoctorId);

        assertNotNull(result);
        verify(admissionRepository).findById(admissionId);
        verify(userRepository).findById(newDoctorId);
        verify(admissionRepository).save(activeAdmission);
    }

    @Test
    @DisplayName("Should throw BusinessException when admission does not exist during doctor reassignment")
    void reassignDoctor_WhenAdmissionNotFound_ShouldThrowBusinessException() {
        UUID newDoctorId = UUID.randomUUID();

        when(admissionRepository.findById(admissionId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                admissionService.reassignDoctor(admissionId, newDoctorId)
        );

        assertEquals("La admisión a la que está intentando reasignar el médico no existe", exception.getMessage());
        verify(userRepository, never()).findById(any());
        verify(admissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when new doctor does not exist or is inactive")
    void reassignDoctor_WhenDoctorNotFoundOrInactive_ShouldThrowBusinessException() {
        UUID newDoctorId = UUID.randomUUID();

        when(admissionRepository.findById(admissionId)).thenReturn(activeAdmission);
        when(userRepository.findById(newDoctorId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                admissionService.reassignDoctor(admissionId, newDoctorId)
        );

        assertEquals("El médico seleccionado no existe o no está activo", exception.getMessage());
        verify(admissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when new doctor belongs to a different service")
    void reassignDoctor_WhenDoctorBelongsToDifferentService_ShouldThrowBusinessException() {
        UUID newDoctorId = UUID.randomUUID();
        UUID otherServiceId = UUID.randomUUID();
        User doctorFromOtherService = User.builder()
                .id(newDoctorId)
                .role(UserRole.MEDICO)
                .active(true)
                .serviceId(otherServiceId)
                .build();

        when(admissionRepository.findById(admissionId)).thenReturn(activeAdmission);
        when(userRepository.findById(newDoctorId)).thenReturn(Optional.of(doctorFromOtherService));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                admissionService.reassignDoctor(admissionId, newDoctorId)
        );

        assertEquals("El médico seleccionado no pertenece al mismo servicio que la admisión", exception.getMessage());
        verify(admissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should discharge admission and update patient status to ALTA")
    void dischargeAdmission_WhenAdmissionAndPatientExist_ShouldDischarge() {
        Patient ingresadoPatient = Patient.builder()
                .patientId(patientId)
                .build();
        ingresadoPatient.applyStatusChange(PatientStatus.INGRESADO);

        when(admissionRepository.findById(admissionId)).thenReturn(activeAdmission);
        when(patientRepository.findById(patientId)).thenReturn(ingresadoPatient);
        when(patientRepository.save(any(Patient.class))).thenReturn(ingresadoPatient);
        when(admissionRepository.save(any(Admission.class))).thenReturn(activeAdmission);

        Admission result = admissionService.dischargeAdmission(admissionId);

        assertNotNull(result);
        verify(admissionRepository).findById(admissionId);
        verify(patientRepository).findById(patientId);
        verify(patientRepository).save(ingresadoPatient);
        verify(admissionRepository).save(activeAdmission);
    }

    @Test
    @DisplayName("Should throw BusinessException when admission does not exist during discharge")
    void dischargeAdmission_WhenAdmissionNotFound_ShouldThrowBusinessException() {
        when(admissionRepository.findById(admissionId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                admissionService.dischargeAdmission(admissionId)
        );

        assertEquals("La admisión a la que está intentando dar de alta no existe", exception.getMessage());
        verify(patientRepository, never()).findById(any());
        verify(admissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when patient is not found during discharge")
    void dischargeAdmission_WhenPatientNotFound_ShouldThrowBusinessException() {
        when(admissionRepository.findById(admissionId)).thenReturn(activeAdmission);
        when(patientRepository.findById(patientId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                admissionService.dischargeAdmission(admissionId)
        );

        assertEquals("Paciente asignado a la admisión no existe en BD", exception.getMessage());
        verify(admissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when admission is already discharged")
    void dischargeAdmission_WhenAlreadyDischarged_ShouldThrowBusinessException() {
        Admission alreadyDischarged = Admission.builder()
                .admissionId(admissionId)
                .patientId(patientId)
                .dischargeDate(LocalDateTime.now().minusDays(2))
                .build();

        when(admissionRepository.findById(admissionId)).thenReturn(alreadyDischarged);

        assertThrows(BusinessException.class, () ->
                admissionService.dischargeAdmission(admissionId)
        );

        verify(admissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update clinical information when admission exists and is not discharged")
    void update_WhenAdmissionExistsAndActive_ShouldUpdateClinicalInfo() {
        Admission updatedData = Admission.builder()
                .principalDiagnosis("Neumonía bilateral")
                .allergies("Penicilina")
                .basalBarthel(80)
                .build();

        when(admissionRepository.findById(admissionId)).thenReturn(activeAdmission);
        when(admissionRepository.save(any(Admission.class))).thenReturn(activeAdmission);

        Admission result = admissionService.update(admissionId, updatedData);

        assertNotNull(result);
        verify(admissionRepository).findById(admissionId);
        verify(admissionRepository).save(activeAdmission);
    }

    @Test
    @DisplayName("Should throw BusinessException when admission does not exist during update")
    void update_WhenAdmissionNotFound_ShouldThrowBusinessException() {
        Admission updatedData = Admission.builder().principalDiagnosis("Diagnóstico").build();

        when(admissionRepository.findById(admissionId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                admissionService.update(admissionId, updatedData)
        );

        assertEquals("La admisión que intenta actualizar no existe en BD", exception.getMessage());
        verify(admissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return admission when found by id")
    void getById_WhenAdmissionExists_ShouldReturnAdmission() {
        when(admissionRepository.findById(admissionId)).thenReturn(activeAdmission);

        Admission result = admissionService.getById(admissionId);

        assertNotNull(result);
        assertEquals(admissionId, result.getAdmissionId());
        verify(admissionRepository).findById(admissionId);
    }

    @Test
    @DisplayName("Should return null when admission does not exist")
    void getById_WhenAdmissionNotFound_ShouldReturnNull() {
        when(admissionRepository.findById(admissionId)).thenReturn(null);

        Admission result = admissionService.getById(admissionId);

        assertNull(result);
        verify(admissionRepository).findById(admissionId);
    }

    @Test
    @DisplayName("Should return paginated active admissions for a given doctor")
    void getActiveByDoctorId_ShouldReturnPaginatedResult() {
        int page = 0;
        PaginatedResult<Admission> expected = new PaginatedResult<>(List.of(activeAdmission), 1L, 1, page, true);

        when(admissionRepository.findAllActiveByDoctor(doctorId, page, 10)).thenReturn(expected);

        PaginatedResult<Admission> result = admissionService.getActiveByDoctorId(doctorId, page);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(admissionRepository).findAllActiveByDoctor(doctorId, page, 10);
    }

    @Test
    @DisplayName("Should return paginated active admissions for a given service")
    void getActiveByServiceId_ShouldReturnPaginatedResult() {
        int page = 0;
        PaginatedResult<Admission> expected = new PaginatedResult<>(List.of(activeAdmission), 1L, 1, page, true);

        when(admissionRepository.findAllActiveByService(serviceId, page, 10)).thenReturn(expected);

        PaginatedResult<Admission> result = admissionService.getActiveByServiceId(serviceId, page);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(admissionRepository).findAllActiveByService(serviceId, page, 10);
    }

    @Test
    @DisplayName("Should return all active admissions paginated")
    void getAllActive_ShouldReturnPaginatedResult() {
        int page = 0;
        PaginatedResult<Admission> expected = new PaginatedResult<>(List.of(activeAdmission), 1L, 1, page, true);

        when(admissionRepository.findAllActive(page, 10)).thenReturn(expected);

        PaginatedResult<Admission> result = admissionService.getAllActive(page);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(admissionRepository).findAllActive(page, 10);
    }

    @Test
    @DisplayName("Should return admissions matching patient surname and serviceId")
    void searchByPatientSurnameAndServiceId_ShouldReturnPaginatedResult() {
        String surname = "González";
        int page = 0;
        PaginatedResult<Admission> expected = new PaginatedResult<>(List.of(activeAdmission), 1L, 1, page, true);

        when(admissionRepository.searchByPatientSurnameAndServiceId(surname, serviceId, page, 10))
                .thenReturn(expected);

        PaginatedResult<Admission> result = admissionService.searchByPatientSurnameAndServiceId(surname, serviceId, page);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(admissionRepository).searchByPatientSurnameAndServiceId(surname, serviceId, page, 10);
    }

    @Test
    @DisplayName("Should return a map of patients keyed by patientId for a list of admissions")
    void loadPatientMapForAdmissions_ShouldReturnPatientMap() {
        UUID patientId2 = UUID.randomUUID();
        Patient patient2 = Patient.builder().patientId(patientId2).name("Carlos").build();
        patient2.applyStatusChange(PatientStatus.ALTA);

        Admission admission2 = Admission.builder()
                .admissionId(UUID.randomUUID())
                .patientId(patientId2)
                .build();

        List<Admission> admissions = List.of(activeAdmission, admission2);
        List<Patient> patients = List.of(altaPatient, patient2);

        when(patientRepository.findAllByIds(List.of(patientId, patientId2))).thenReturn(patients);

        Map<UUID, Patient> result = admissionService.loadPatientMapForAdmissions(admissions);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(patientId));
        assertTrue(result.containsKey(patientId2));
        assertEquals("María", result.get(patientId).getName());
        verify(patientRepository).findAllByIds(List.of(patientId, patientId2));
    }
}
