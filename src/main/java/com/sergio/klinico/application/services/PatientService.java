package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.domain.models.enums.PatientStatus;
import com.sergio.klinico.domain.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio de aplicación que gestiona el catálogo de pacientes del sistema hospitalario.
 *
 * <p>Proporciona las operaciones CRUD básicas sobre {@link Patient}, con las siguientes
 * restricciones de negocio:</p>
 * <ul>
 *   <li>El DNI debe ser único: no se pueden crear dos pacientes con el mismo DNI.</li>
 *   <li>Los cambios de estado se validan mediante la lógica de dominio
 *       ({@code Patient#applyStatusChange}), que aplica las transiciones permitidas.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;

    /**
     * Registra un nuevo paciente en el sistema.
     *
     * @param patient datos del paciente a crear
     * @return paciente persistido con los datos generados (ID, timestamps, etc.)
     * @throws BusinessException si ya existe un paciente registrado con el mismo DNI
     */
    @Transactional
    public Patient create(Patient patient) {
        if (patientRepository.existsByDni(patient.getDni())) {
            log.error("El paciente con DNI {} ya existe en BD", patient.getDni());
            throw new BusinessException("Ya existe un paciente registrado con el DNI: " + patient.getDni());
        }
        return patientRepository.save(patient);
    }

    /**
     * Obtiene un paciente por su identificador único.
     *
     * @param id UUID del paciente
     * @return paciente encontrado
     * @throws BusinessException si el paciente no existe
     */
    public Patient getById(UUID id) {
        Patient patient = patientRepository.findById(id);
        if (patient == null) {
            log.error("El paciente con ID {} no existe en BD", id);
            throw new BusinessException("Paciente no encontrado");
        }
        return patient;
    }

    /**
     * Devuelve todos los pacientes del sistema de forma paginada.
     *
     * @param page número de página (0-indexed)
     * @param size número de elementos por página
     * @return resultado paginado con los pacientes del sistema
     */
    public PaginatedResult<Patient> getAllPaginated(int page, int size) {
        return patientRepository.findAll(page, size);
    }

    /**
     * Busca pacientes con estado {@code ALTA} por apellido (búsqueda parcial, insensible a mayúsculas).
     *
     * <p>Solo devuelve pacientes con estado {@code ALTA}; los pacientes ingresados o
     * con estado {@code EXITUS} no aparecen en los resultados.</p>
     *
     * @param surname apellido o fragmento del apellido a buscar
     * @param page    número de página (0-indexed)
     * @param size    número de elementos por página
     * @return resultado paginado de pacientes con estado {@code ALTA} que coinciden con el apellido
     */
    public PaginatedResult<Patient> searchBySurname(String surname, int page, int size) {
        return patientRepository.findBySurnameAndStatusAlta(surname, page, size);
    }

    /**
     * Actualiza los datos de un paciente existente.
     *
     * <p>Si el estado del paciente cambia, se aplican las validaciones de transición
     * de estado definidas en el dominio mediante {@code Patient#applyStatusChange}.</p>
     *
     * @param updatedData objeto con los nuevos datos del paciente (debe incluir el {@code patientId})
     * @return paciente actualizado
     * @throws BusinessException si el paciente no existe o si la transición de estado no es válida
     */
    @Transactional
    public Patient update(Patient updatedData) {

        Patient currentPatient = getById(updatedData.getPatientId());
        PatientStatus previousStatus = currentPatient.getStatus();

        if (updatedData.getStatus() != currentPatient.getStatus()) {
            log.info("Paciente con ID {} va a cambiar su estado de {} a {}", updatedData.getPatientId(), currentPatient.getStatus(), updatedData.getStatus());
            currentPatient.applyStatusChange(updatedData.getStatus());
        }

        currentPatient.updateFields(updatedData);

        Patient updatedPatient = patientRepository.save(currentPatient);

        if(updatedPatient.getStatus() != previousStatus)
            log.info("Paciente con ID {} ha cambiado su estado de {} a {}", updatedData.getPatientId(), previousStatus, updatedPatient.getStatus());

        return updatedPatient;
    }
}
