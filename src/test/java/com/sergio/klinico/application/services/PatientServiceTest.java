package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.domain.models.enums.PatientStatus;
import com.sergio.klinico.domain.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientService Tests")
class PatientServiceTest {

    @InjectMocks
    private PatientService patientService;

    @Mock
    private PatientRepository patientRepository;

    private Patient testPatient;
    private final String testDni = "12345678A";
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
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
    }

    @Test
    @DisplayName("Should create patient when DNI does not exist")
    void create_WhenDniDoesNotExist_ShouldCreatePatient() {
        when(patientRepository.existsByDni(testDni)).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        Patient result = patientService.create(testPatient);

        assertNotNull(result);
        assertEquals(testDni, result.getDni());
        verify(patientRepository).existsByDni(testDni);
        verify(patientRepository).save(testPatient);
    }

    @Test
    @DisplayName("Should throw BusinessException when DNI already exists")
    void create_WhenDniExists_ShouldThrowBusinessException() {
        when(patientRepository.existsByDni(testDni)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            patientService.create(testPatient);
        });

        assertEquals("Ya existe un paciente registrado con el DNI: " + testDni, exception.getMessage());
        verify(patientRepository).existsByDni(testDni);
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should return patient when ID exists")
    void getById_WhenIdExists_ShouldReturnPatient() {
        when(patientRepository.findById(patientId)).thenReturn(testPatient);

        Patient result = patientService.getById(patientId);

        assertNotNull(result);
        assertEquals(patientId, result.getPatientId());
        verify(patientRepository).findById(patientId);
    }

    @Test
    @DisplayName("Should throw BusinessException when patient does not exist")
    void getById_WhenPatientDoesNotExist_ShouldThrowBusinessException() {
        when(patientRepository.findById(patientId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            patientService.getById(patientId);
        });

        assertEquals("Paciente no encontrado", exception.getMessage());
        verify(patientRepository).findById(patientId);
    }

    @Test
    @DisplayName("Should return paginated result when getting all patients")
    void getAllPaginated_ShouldReturnPaginatedResult() {
        int page = 0;
        int size = 10;
        PaginatedResult<Patient> expectedResult = new PaginatedResult<>(
                List.of(testPatient),
                1L,
                1,
                page,
                true
        );

        when(patientRepository.findAll(page, size)).thenReturn(expectedResult);

        PaginatedResult<Patient> result = patientService.getAllPaginated(page, size);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(1L, result.totalElements());
        verify(patientRepository).findAll(page, size);
    }

    @Test
    @DisplayName("Should update patient when status changes")
    void update_WhenStatusChanges_ShouldUpdatePatient() {
        Patient updatedData = Patient.builder()
                .patientId(patientId)
                .status(PatientStatus.ALTA)
                .name("Rosa")
                .build();

        Patient existingPatient = Patient.builder()
                .patientId(patientId)
                .dni(testDni)
                .status(PatientStatus.INGRESADO)
                .name("Rosalía")
                .build();

        when(patientRepository.findById(patientId)).thenReturn(existingPatient);
        when(patientRepository.save(any(Patient.class))).thenReturn(existingPatient);

        Patient result = patientService.update(updatedData);

        assertNotNull(result);
        verify(patientRepository).findById(patientId);
        verify(patientRepository).save(existingPatient);
    }

    @Test
    @DisplayName("Should update patient fields when provided")
    void update_WhenFieldsProvided_ShouldUpdatePatient() {
        Patient updatedData = Patient.builder()
                .patientId(patientId)
                .name("José")
                .surname("Domínguez Díaz")
                .address("C/Prueba, 23, Ciudad Real")
                .contactNumber("623456781")
                .relativeContactNumber("678901232")
                .status(PatientStatus.INGRESADO)
                .build();

        Patient existingPatient = Patient.builder()
                .patientId(patientId)
                .name("Pepe")
                .surname("Domínguez")
                .address("C/Prueba, 23, Ciudad Real")
                .contactNumber("623456789")
                .relativeContactNumber("678901234")
                .status(PatientStatus.INGRESADO)
                .build();

        when(patientRepository.findById(patientId)).thenReturn(existingPatient);
        when(patientRepository.save(any(Patient.class))).thenReturn(existingPatient);

        Patient result = patientService.update(updatedData);

        assertNotNull(result);
        verify(patientRepository).findById(patientId);
        verify(patientRepository).save(existingPatient);
    }

    @Test
    @DisplayName("Should not update fields when they are null")
    void update_WhenFieldsAreNull_ShouldNotUpdateFields() {
        Patient updatedData = Patient.builder()
                .patientId(patientId)
                .name(null)
                .surname(null)
                .address(null)
                .contactNumber(null)
                .relativeContactNumber(null)
                .status(PatientStatus.INGRESADO)
                .build();

        Patient existingPatient = Patient.builder()
                .patientId(patientId)
                .name("Pepe")
                .surname("Domínguez")
                .address("C/Prueba, 23, Ciudad Real")
                .contactNumber("623456789")
                .relativeContactNumber("678901234")
                .status(PatientStatus.INGRESADO)
                .build();

        when(patientRepository.findById(patientId)).thenReturn(existingPatient);
        when(patientRepository.save(any(Patient.class))).thenReturn(existingPatient);

        Patient result = patientService.update(updatedData);

        assertNotNull(result);
        verify(patientRepository).findById(patientId);
        verify(patientRepository).save(existingPatient);
    }

    @Test
    @DisplayName("Should throw BusinessException when updating non-existent patient")
    void update_WhenPatientDoesNotExist_ShouldThrowBusinessException() {
        UUID nonExistentId = UUID.randomUUID();
        Patient updatedData = Patient.builder()
                .patientId(nonExistentId)
                .name("Domingo")
                .build();

        when(patientRepository.findById(nonExistentId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            patientService.update(updatedData);
        });

        assertEquals("Paciente no encontrado", exception.getMessage());
        verify(patientRepository).findById(nonExistentId);
        verify(patientRepository, never()).save(any(Patient.class));
    }
}
