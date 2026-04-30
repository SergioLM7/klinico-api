package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Admission;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.enums.PatientStatus;
import com.sergio.klinico.domain.repositories.AdmissionRepository;
import com.sergio.klinico.domain.repositories.PatientRepository;
import com.sergio.klinico.domain.repositories.UserRepository;
import com.sergio.klinico.infrastructure.rest.dto.responses.admission.AdmissionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación que encapsula la lógica de negocio del ciclo de vida
 * de los ingresos hospitalarios ({@link Admission}).
 *
 * <p>Orquesta las operaciones sobre ingresos coordinando los repositorios de dominio
 * {@link AdmissionRepository}, {@link PatientRepository} y {@link UserRepository}.
 * Las operaciones de escritura son transaccionales; las de solo lectura usan
 * {@code @Transactional(readOnly = true)} para optimizar el rendimiento.</p>
 *
 * <p>Reglas de negocio principales:</p>
 * <ul>
 *   <li>No se puede crear un ingreso para un paciente con estado {@code EXITUS} o {@code INGRESADO}.</li>
 *   <li>No puede existir más de un ingreso activo por paciente simultáneamente.</li>
 *   <li>Al dar de alta se registra la fecha de alta en el ingreso y el paciente pasa a estado {@code ALTA}.</li>
 *   <li>La reasignación de médico solo es válida si el nuevo médico está activo y pertenece al mismo servicio.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdmissionService {

    private final AdmissionRepository admissionRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    /**
     * Crea un nuevo ingreso hospitalario para el paciente referenciado en la admisión.
     *
     * <p>Valida que el paciente exista, que no tenga estado {@code EXITUS} ni {@code INGRESADO}
     * y que no haya ya un ingreso activo para él. Si todas las validaciones pasan,
     * el paciente pasa al estado {@code INGRESADO}.</p>
     *
     * @param admission datos del ingreso a crear (debe incluir el {@code patientId} y el {@code serviceId})
     * @return ingreso persistido con los datos generados (ID, timestamps, etc.)
     * @throws BusinessException si el paciente no existe, tiene estado inválido o ya tiene un ingreso activo
     */
    @Transactional
    public Admission create(Admission admission) {
        UUID patientId = admission.getPatientId();

        Patient patient = patientRepository.findById(patientId);

        if (patient == null) {
            log.error("El paciente con ID {} no existe en BD", patientId);
            throw new BusinessException("Paciente asignado a la admisión no encontrado");
        }

        if(patient.getStatus() == PatientStatus.EXITUS) {
            log.error("El paciente con ID {} tiene estado EXITUS", patientId);
            throw new BusinessException("No se puede crear una nueva admisión para un paciente con estado EXITUS");
        } else if (patient.getStatus() == PatientStatus.INGRESADO) {
            log.error("El paciente con ID {} ya está ingresado", patientId);
            throw new BusinessException("No se puede crear una nueva admisión para un paciente que sigue ingresado");
        }

        if (admissionRepository.existsActiveAdmissionByPatientId(patientId)) {
            log.error("Ya existe en BD una admisión activa para el paciente {}", patientId);
            throw new BusinessException("Ya existe una admisión activa para el paciente solicitado. No se puede crear una nueva");
        }

        patient.applyStatusChange(PatientStatus.INGRESADO);
        patientRepository.save(patient);

        return admissionRepository.save(admission);
    }

    /**
     * Asigna un número de habitación a un ingreso existente.
     *
     * @param admissionId UUID del ingreso al que se asigna la habitación
     * @param roomNumber  número de habitación a asignar
     * @return ingreso actualizado con el número de habitación
     * @throws BusinessException si el ingreso no existe
     */
    @Transactional
    public Admission assignRoom(UUID admissionId, Integer roomNumber) {

        Admission currentAdmission = admissionRepository.findById(admissionId);

        if(currentAdmission == null) {
            log.error("La admisión {}, a la que está intentando asignar un nº de habitación, no existe", admissionId);
            throw new BusinessException("La admisión a la que está intentando asignar un nº de habitación no existe");
        }

        currentAdmission.assignRoom(roomNumber);

        log.info("Habitación {} asignada con éxito a la admisión {}", roomNumber, admissionId);
        return admissionRepository.save(currentAdmission);
    }

    /**
     * Reasigna el médico responsable de un ingreso a otro médico activo del mismo servicio.
     *
     * @param admissionId UUID del ingreso a reasignar
     * @param newDoctorId UUID del nuevo médico responsable
     * @return ingreso actualizado con el nuevo médico asignado
     * @throws BusinessException si el ingreso no existe, el médico no existe/está inactivo
     *                           o no pertenece al mismo servicio que el ingreso
     */
    @Transactional
    public Admission reassignDoctor(UUID admissionId, UUID newDoctorId) {
        Admission currentAdmission = admissionRepository.findById(admissionId);

        if (currentAdmission == null) {
            log.error("La admisión {}, a la que está intentando reasignar el médico, no existe", admissionId);
            throw new BusinessException("La admisión a la que está intentando reasignar el médico no existe");
        }

        User newDoctor = userRepository.findById(newDoctorId)
                .filter(User::isActive)
                .orElseThrow(() -> new BusinessException("El médico seleccionado no existe o no está activo"));

        if (!newDoctor.getServiceId().equals(currentAdmission.getServiceId())) {
            log.warn("El médico {} no pertenece al servicio de la admisión {}", newDoctorId, admissionId);
            throw new BusinessException("El médico seleccionado no pertenece al mismo servicio que la admisión");
        }

        currentAdmission.reassignDoctor(newDoctorId);

        log.info("Médico {} reasignado con éxito a la admisión {}", newDoctorId, admissionId);
        return admissionRepository.save(currentAdmission);
    }

    /**
     * Procesa el alta médica de un paciente, cerrando su ingreso activo.
     *
     * <p>Registra la fecha de alta en el ingreso y actualiza el estado del paciente
     * asociado a {@code ALTA}.</p>
     *
     * @param admissionId UUID del ingreso a cerrar con el alta
     * @return ingreso actualizado con la fecha de alta registrada
     * @throws BusinessException si el ingreso no existe o el paciente asociado no existe en BD
     */
    @Transactional
    public Admission dischargeAdmission(UUID admissionId) {

        Admission currentAdmission = admissionRepository.findById(admissionId);

        if(currentAdmission == null) {
            log.error("La admisión {}, a la que está intentando dar de alta, no existe", admissionId);
            throw new BusinessException("La admisión a la que está intentando dar de alta no existe");
        }

        currentAdmission.processDischarge();

        Patient patient = patientRepository.findById(currentAdmission.getPatientId());

        if (patient == null) {
            log.error("El paciente con ID {} no existe en BD", currentAdmission.getPatientId());
            throw new BusinessException("Paciente asignado a la admisión no existe en BD");
        }

        patient.applyStatusChange(PatientStatus.ALTA);
        log.info("El paciente {} ha sido dado de alta con éxito", patient.getPatientId());

        patientRepository.save(patient);

        log.info("La admisión {} ha sido dada de alta con éxito", admissionId);
        return admissionRepository.save(currentAdmission);
    }

    /**
     * Actualiza la información clínica de un ingreso existente (diagnóstico, historia, etc.).
     *
     * @param admissionId UUID del ingreso a actualizar
     * @param updatedData objeto de dominio con los nuevos datos clínicos
     * @return ingreso actualizado
     * @throws BusinessException si el ingreso no existe
     */
    @Transactional
    public Admission update(UUID admissionId, Admission updatedData) {
        Admission currentAdmission = admissionRepository.findById(admissionId);

        if (currentAdmission == null) {
            log.error("La admisión {}, que está intentando modificar, no existe", admissionId);
            throw new BusinessException("La admisión que intenta actualizar no existe en BD");
        }

        currentAdmission.updateClinicalInformation(updatedData);

        return admissionRepository.save(currentAdmission);
    }

    /**
     * Obtiene un ingreso por su identificador único.
     *
     * @param id UUID del ingreso
     * @return ingreso encontrado, o {@code null} si no existe
     */
    public Admission getById(UUID id) {
        return admissionRepository.findById(id);
    }

    /**
     * Devuelve los ingresos activos asignados a un médico, paginados (10 por página).
     *
     * @param doctorId UUID del médico
     * @param page     número de página (0-indexed)
     * @return resultado paginado de ingresos activos del médico
     */
    public PaginatedResult<Admission> getActiveByDoctorId(UUID doctorId, int page) {
        return admissionRepository.findAllActiveByDoctor(doctorId, page, 10);
    }

    /**
     * Devuelve los ingresos activos de un servicio hospitalario, paginados (10 por página).
     *
     * @param serviceId UUID del servicio hospitalario
     * @param page      número de página (0-indexed)
     * @return resultado paginado de ingresos activos del servicio
     */
    public PaginatedResult<Admission> getActiveByServiceId(UUID serviceId, int page) {
        return admissionRepository.findAllActiveByService(serviceId, page, 10);
    }

    /**
     * Devuelve todos los ingresos activos del hospital, paginados (10 por página).
     *
     * @param page número de página (0-indexed)
     * @return resultado paginado con todos los ingresos activos
     */
    public PaginatedResult<Admission> getAllActive(int page) {
        return admissionRepository.findAllActive(page, 10);
    }

    /**
     * Busca ingresos activos por apellido del paciente dentro de un servicio concreto.
     *
     * @param surname   apellido o fragmento del apellido del paciente
     * @param serviceId UUID del servicio hospitalario al que se acota la búsqueda
     * @param page      número de página (0-indexed)
     * @return resultado paginado de ingresos activos que coinciden con el criterio
     */
    public PaginatedResult<Admission> searchByPatientSurnameAndServiceId(String surname, UUID serviceId, int page) {
        return admissionRepository.searchByPatientSurnameAndServiceId(surname, serviceId, page, 10);
    }

    /**
     * Construye un mapa de pacientes indexado por su UUID a partir de una lista de ingresos.
     *
     * <p>Se usa para enriquecer las respuestas de ingresos con los datos del paciente
     * evitando consultas N+1.</p>
     *
     * @param admissions lista de ingresos de los que se quiere cargar los pacientes
     * @return mapa {@code UUID → Patient} para todos los pacientes referenciados
     */
    public Map<UUID, Patient> loadPatientMapForAdmissions(List<Admission> admissions) {
        List<UUID> patientIds = admissions.stream()
                .map(Admission::getPatientId)
                .toList();
        return patientRepository.findAllByIds(patientIds).stream()
                .collect(Collectors.toMap(Patient::getPatientId, p -> p));
    }
}
