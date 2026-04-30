package com.sergio.klinico.infrastructure.persistence.adapters;

import com.sergio.klinico.domain.models.MonthlyKpiEntry;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.domain.models.enums.PatientStatus;
import com.sergio.klinico.domain.repositories.PatientRepository;
import com.sergio.klinico.infrastructure.mappers.PatientMapper;
import com.sergio.klinico.infrastructure.persistence.PatientEntity;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaPatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia para la entidad {@link PatientEntity}.
 *
 * <p>Implementa el puerto de dominio {@link PatientRepository} mediante Spring Data JPA,
 * traduciendo entre los objetos de dominio {@link Patient} y las entidades de persistencia
 * {@link PatientEntity} a través de {@link PatientMapper}.</p>
 *
 * <p>Para las operaciones de actualización se recupera la versión actual de la entidad
 * antes de persistir, garantizando el correcto funcionamiento del bloqueo optimista.</p>
 *
 * <p>También implementa las consultas de KPI de éxitus, agregando datos de pacientes
 * con estado {@link PatientStatus#EXITUS} por servicio y mes.</p>
 */
@Component
@RequiredArgsConstructor
public class PatientPersistenceAdapter implements PatientRepository {

    private final JpaPatientRepository jpaRepository;
    private final PatientMapper patientMapper;

    /**
     * Persiste un paciente nuevo o actualiza uno existente.
     *
     * <p>Si el paciente ya tiene un ID asignado, se carga la versión actual de la entidad
     * para garantizar el control de concurrencia optimista.</p>
     *
     * @param patient paciente de dominio a guardar
     * @return paciente de dominio persistido
     */
    @Override
    public Patient save(Patient patient) {
        PatientEntity entity = patientMapper.toEntity(patient);

        if (patient.getPatientId() != null) {
            jpaRepository.findById(patient.getPatientId()).ifPresent(existingEntity ->
                entity.setVersion(existingEntity.getVersion())
            );
        }

        PatientEntity savedEntity = jpaRepository.save(entity);
        return patientMapper.toDomain(savedEntity);
    }

    /**
     * Comprueba si existe un paciente registrado con el DNI indicado.
     *
     * @param dni DNI a comprobar
     * @return {@code true} si ya existe un paciente con ese DNI
     */
    @Override
    public boolean existsByDni(String dni) {
        return jpaRepository.existsByDni(dni);
    }

    /**
     * Busca un paciente por su identificador único.
     *
     * @param id UUID del paciente
     * @return paciente de dominio, o {@code null} si no existe
     */
    @Override
    public Patient findById(UUID id) {
        Optional<PatientEntity> entity = jpaRepository.findById(id);
        return entity.map(patientMapper::toDomain).orElse(null);
    }

    /**
     * Devuelve una lista de pacientes por sus identificadores.
     *
     * @param ids lista de UUIDs de pacientes
     * @return lista de pacientes de dominio correspondientes a los IDs indicados
     */
    @Override
    public List<Patient> findAllByIds(List<UUID> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(patientMapper::toDomain)
                .toList();
    }

    /**
     * Devuelve todos los pacientes del sistema de forma paginada.
     *
     * @param page número de página (0-indexed)
     * @param size número de elementos por página
     * @return resultado paginado con todos los pacientes
     */
    @Override
    public PaginatedResult<Patient> findAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PatientEntity> entitiesPage = jpaRepository.findAll(pageRequest);

        List<Patient> domainList = entitiesPage.getContent().stream()
                .map(patientMapper::toDomain)
                .toList();

        return new PaginatedResult<>(
                domainList,
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast()
        );
    }

    /**
     * Busca pacientes con estado {@link PatientStatus#ALTA} por apellido
     * (búsqueda parcial, insensible a mayúsculas).
     *
     * @param surname apellido o fragmento del apellido a buscar
     * @param page    número de página (0-indexed)
     * @param size    número de elementos por página
     * @return resultado paginado de pacientes con estado {@code ALTA} que coinciden
     */
    @Override
    public PaginatedResult<Patient> findBySurnameAndStatusAlta(String surname, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PatientEntity> entitiesPage = jpaRepository.findBySurnameContainingIgnoreCaseAndStatus(
                surname, PatientStatus.ALTA, pageRequest);

        List<Patient> domainList = entitiesPage.getContent().stream()
                .map(patientMapper::toDomain)
                .toList();

        return new PaginatedResult<>(
                domainList,
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast()
        );
    }

    /**
     * Cuenta los éxitus de un servicio agrupados por mes para un año completo.
     *
     * @param serviceId UUID del servicio hospitalario
     * @param year      año de referencia
     * @return lista de entradas mensuales con el recuento de éxitus
     */
    @Override
    public List<MonthlyKpiEntry> countExitusByServiceAndYear(UUID serviceId, int year) {
        return jpaRepository.countExitusByServiceAndYear(serviceId, year).stream()
                .map(p -> new MonthlyKpiEntry(p.getMonth(), p.getCount().doubleValue()))
                .toList();
    }

    /**
     * Cuenta los éxitus de un servicio para un mes y año concretos.
     *
     * @param serviceId UUID del servicio hospitalario
     * @param year      año de referencia
     * @param month     mes de referencia (1-12)
     * @return lista con la entrada del mes indicado (puede estar vacía si no hay datos)
     */
    @Override
    public List<MonthlyKpiEntry> countExitusByServiceAndYearAndMonth(UUID serviceId, int year, int month) {
        return jpaRepository.countExitusByServiceAndYearAndMonth(serviceId, year, month).stream()
                .map(p -> new MonthlyKpiEntry(p.getMonth(), p.getCount().doubleValue()))
                .toList();
    }

}
