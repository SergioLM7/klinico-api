package com.sergio.klinico.infrastructure.persistence.adapters;

import com.sergio.klinico.domain.models.Admission;
import com.sergio.klinico.domain.models.DoctorKpiSeries;
import com.sergio.klinico.domain.models.MonthlyKpiEntry;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.repositories.AdmissionRepository;
import com.sergio.klinico.infrastructure.mappers.AdmissionMapper;
import com.sergio.klinico.infrastructure.persistence.AdmissionEntity;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaAdmissionRepository;
import com.sergio.klinico.infrastructure.persistence.repositories.projections.DoctorMonthlyAvgProjection;
import com.sergio.klinico.infrastructure.persistence.repositories.projections.DoctorMonthlyCountProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para la entidad {@link AdmissionEntity}.
 *
 * <p>Implementa el puerto de dominio {@link AdmissionRepository} mediante Spring Data JPA,
 * traduciendo entre los objetos de dominio {@link Admission} y las entidades de persistencia
 * {@link AdmissionEntity} a través de {@link AdmissionMapper}.</p>
 *
 * <p>Para las operaciones de actualización se recupera la versión actual de la entidad
 * antes de persistir, garantizando el correcto funcionamiento del bloqueo optimista
 * gestionado por {@code @Version}.</p>
 *
 * <p>Las operaciones de listado paginado calculan el campo derivado
 * {@code hospitalizationLength} (días de estancia en curso) antes de devolver
 * los resultados al servicio de aplicación.</p>
 */
@Component
@RequiredArgsConstructor
public class AdmissionPersistenceAdapter implements AdmissionRepository {

    public static final String CREATED_AT = "createdAt";
    private final JpaAdmissionRepository jpaRepository;
    private final AdmissionMapper mapper;

    /**
     * Persiste un ingreso nuevo o actualiza uno existente.
     *
     * <p>Si el ingreso ya tiene un ID asignado, se carga la versión actual de la entidad
     * para garantizar el control de concurrencia optimista.</p>
     *
     * @param admission ingreso de dominio a guardar
     * @return ingreso de dominio persistido
     */
    @Override
    public Admission save(Admission admission) {
        AdmissionEntity entity = mapper.toEntity(admission);

        if (admission.getAdmissionId() != null) {
            jpaRepository.findById(admission.getAdmissionId()).ifPresent(existingEntity ->
                    entity.setVersion(existingEntity.getVersion())
            );
        }

        return mapper.toDomain(jpaRepository.save(entity));
    }

    /**
     * Comprueba si existe algún ingreso activo (sin fecha de alta) para el paciente indicado.
     *
     * @param patientId UUID del paciente
     * @return {@code true} si existe al menos un ingreso activo para ese paciente
     */
    @Override
    public boolean existsActiveAdmissionByPatientId(UUID patientId) {
        return jpaRepository.existsByPatientIdAndDischargeDateIsNull(patientId);
    }

    /**
     * Busca un ingreso por su identificador único.
     *
     * @param id UUID del ingreso
     * @return ingreso de dominio, o {@code null} si no existe
     */
    @Override
    public Admission findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);
    }

    /**
     * Calcula la estancia media global (en días) de todos los ingresos con alta de un servicio.
     *
     * @param serviceId UUID del servicio hospitalario
     * @return media de días de hospitalización, o {@code null} si no hay datos
     */
    @Override
    public Double getAverageHospitalizationLengthByService(UUID serviceId) {
        return jpaRepository.getAverageLengthByService(serviceId);
    }

    /**
     * Devuelve todos los ingresos activos del hospital paginados, ordenados por fecha de creación
     * descendente.
     *
     * @param page número de página (0-indexed)
     * @param size número de resultados por página
     * @return resultado paginado de ingresos activos con la estancia en curso calculada
     */
    @Override
    public PaginatedResult<Admission> findAllActive(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(CREATED_AT).descending());

        Page<AdmissionEntity> entitiesPage = jpaRepository.findByDischargeDateIsNull(pageRequest);

        return new PaginatedResult<>(
                entitiesPage.getContent().stream()
                        .map(mapper::toDomain)
                        .map(this::setHospitalizationLength)
                        .toList(),
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast());
    }

    /**
     * Devuelve los ingresos activos de un servicio hospitalario paginados, ordenados por fecha de
     * creación descendente.
     *
     * @param serviceId UUID del servicio hospitalario
     * @param page      número de página (0-indexed)
     * @param size      número de resultados por página
     * @return resultado paginado de ingresos activos del servicio con la estancia en curso calculada
     */
    @Override
    public PaginatedResult<Admission> findAllActiveByService(UUID serviceId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(CREATED_AT).descending());
        Page<AdmissionEntity> entitiesPage = jpaRepository.findByServiceIdAndDischargeDateIsNull(serviceId,
                pageRequest);

        return new PaginatedResult<>(
                entitiesPage.getContent().stream()
                        .map(mapper::toDomain)
                        .map(this::setHospitalizationLength)
                        .toList(),
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast());
    }

    /**
     * Devuelve los ingresos activos asignados a un médico paginados, ordenados por fecha de creación
     * descendente.
     *
     * @param doctorId UUID del médico asignado
     * @param page     número de página (0-indexed)
     * @param size     número de resultados por página
     * @return resultado paginado de ingresos activos del médico con la estancia en curso calculada
     */
    @Override
    public PaginatedResult<Admission> findAllActiveByDoctor(UUID doctorId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(CREATED_AT).descending());
        Page<AdmissionEntity> entitiesPage = jpaRepository.findByAssignedDoctorIdAndDischargeDateIsNull(doctorId,
                pageRequest);

        return new PaginatedResult<>(
                entitiesPage.getContent().stream()
                        .map(mapper::toDomain)
                        .map(this::setHospitalizationLength)
                        .toList(),
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast());
    }

    /**
     * Busca ingresos activos por apellido del paciente dentro de un servicio concreto.
     *
     * @param surname   apellido o fragmento del apellido del paciente
     * @param serviceId UUID del servicio al que se acota la búsqueda
     * @param page      número de página (0-indexed)
     * @param size      número de resultados por página
     * @return resultado paginado de ingresos activos que coinciden con el criterio
     */
    @Override
    public PaginatedResult<Admission> searchByPatientSurnameAndServiceId(String surname, UUID serviceId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("created_at").descending());
        Page<AdmissionEntity> entitiesPage = jpaRepository.findByPatientSurnameContainingIgnoreCaseAndServiceIdAndDischargeDateIsNull(
                surname, serviceId, pageRequest);

        return new PaginatedResult<>(
                entitiesPage.getContent().stream()
                        .map(mapper::toDomain)
                        .map(this::setHospitalizationLength)
                        .toList(),
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast());
    }

    /**
     * Cuenta los ingresos de un servicio agrupados por mes para un año completo.
     *
     * @param serviceId UUID del servicio hospitalario
     * @param year      año de referencia
     * @return lista de entradas mensuales con el recuento de ingresos
     */
    @Override
    public List<MonthlyKpiEntry> countAdmissionsByServiceAndYear(UUID serviceId, int year) {
        return jpaRepository.countAdmissionsByServiceAndYear(serviceId, year).stream()
                .map(p -> new MonthlyKpiEntry(p.getMonth(), p.getCount().doubleValue()))
                .toList();
    }

    /**
     * Cuenta los ingresos de un servicio para un mes y año concretos.
     *
     * @param serviceId UUID del servicio hospitalario
     * @param year      año de referencia
     * @param month     mes de referencia (1-12)
     * @return lista con la entrada del mes indicado (puede estar vacía si no hay datos)
     */
    @Override
    public List<MonthlyKpiEntry> countAdmissionsByServiceAndYearAndMonth(UUID serviceId, int year, int month) {
        return jpaRepository.countAdmissionsByServiceAndYearAndMonth(serviceId, year, month).stream()
                .map(p -> new MonthlyKpiEntry(p.getMonth(), p.getCount().doubleValue()))
                .toList();
    }

    /**
     * Cuenta los ingresos por médico de un servicio agrupados por mes para un año completo.
     *
     * @param serviceId UUID del servicio hospitalario
     * @param year      año de referencia
     * @return lista de series por médico con el recuento mensual de ingresos
     */
    @Override
    public List<DoctorKpiSeries> countAdmissionsByDoctorAndServiceAndYear(UUID serviceId, int year) {
        return groupDoctorCountRows(
                jpaRepository.countAdmissionsByDoctorAndServiceAndYear(serviceId, year));
    }

    /**
     * Cuenta los ingresos por médico de un servicio para un mes y año concretos.
     *
     * @param serviceId UUID del servicio hospitalario
     * @param year      año de referencia
     * @param month     mes de referencia (1-12)
     * @return lista de series por médico con el recuento del mes indicado
     */
    @Override
    public List<DoctorKpiSeries> countAdmissionsByDoctorAndServiceAndYearAndMonth(UUID serviceId, int year, int month) {
        return groupDoctorCountRows(
                jpaRepository.countAdmissionsByDoctorAndServiceAndYearAndMonth(serviceId, year, month));
    }

    /**
     * Calcula la estancia media en días de un servicio agrupada por mes para un año completo.
     *
     * @param serviceId UUID del servicio hospitalario
     * @param year      año de referencia
     * @return lista de entradas mensuales con la estancia media en días
     */
    @Override
    public List<MonthlyKpiEntry> avgStayByServiceAndYear(UUID serviceId, int year) {
        return jpaRepository.avgStayByServiceAndYear(serviceId, year).stream()
                .map(p -> new MonthlyKpiEntry(p.getMonth(), p.getAvgDays() != null ? p.getAvgDays() : 0.0))
                .toList();
    }

    /**
     * Calcula la estancia media en días de un servicio para un mes y año concretos.
     *
     * @param serviceId UUID del servicio hospitalario
     * @param year      año de referencia
     * @param month     mes de referencia (1-12)
     * @return lista con la entrada del mes indicado (puede estar vacía si no hay datos)
     */
    @Override
    public List<MonthlyKpiEntry> avgStayByServiceAndYearAndMonth(UUID serviceId, int year, int month) {
        return jpaRepository.avgStayByServiceAndYearAndMonth(serviceId, year, month).stream()
                .map(p -> new MonthlyKpiEntry(p.getMonth(), p.getAvgDays() != null ? p.getAvgDays() : 0.0))
                .toList();
    }

    /**
     * Calcula la estancia media en días por médico de un servicio agrupada por mes para un año
     * completo.
     *
     * @param serviceId UUID del servicio hospitalario
     * @param year      año de referencia
     * @return lista de series por médico con la estancia media mensual
     */
    @Override
    public List<DoctorKpiSeries> avgStayByDoctorAndServiceAndYear(UUID serviceId, int year) {
        return groupDoctorAvgRows(
                jpaRepository.avgStayByDoctorAndServiceAndYear(serviceId, year));
    }

    /**
     * Calcula la estancia media en días por médico de un servicio para un mes y año concretos.
     *
     * @param serviceId UUID del servicio hospitalario
     * @param year      año de referencia
     * @param month     mes de referencia (1-12)
     * @return lista de series por médico con la estancia media del mes indicado
     */
    @Override
    public List<DoctorKpiSeries> avgStayByDoctorAndServiceAndYearAndMonth(UUID serviceId, int year, int month) {
        return groupDoctorAvgRows(
                jpaRepository.avgStayByDoctorAndServiceAndYearAndMonth(serviceId, year, month));
    }

    /**
     * Agrupa filas de recuento mensual por doctor en series {@link DoctorKpiSeries}.
     *
     * @param rows proyecciones crudas de BD con doctor y recuento mensual
     * @return lista de series por médico
     */
    private List<DoctorKpiSeries> groupDoctorCountRows(List<DoctorMonthlyCountProjection> rows) {
        Map<String, List<DoctorMonthlyCountProjection>> grouped = rows.stream()
                .collect(Collectors.groupingBy(DoctorMonthlyCountProjection::getDoctorId));

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<DoctorMonthlyCountProjection> doctorRows = entry.getValue();
                    DoctorMonthlyCountProjection first = doctorRows.getFirst();
                    List<MonthlyKpiEntry> data = doctorRows.stream()
                            .map(r -> new MonthlyKpiEntry(r.getMonth(), r.getCount().doubleValue()))
                            .toList();
                    return new DoctorKpiSeries(
                            UUID.fromString(first.getDoctorId()),
                            first.getDoctorName(),
                            first.getDoctorSurname(),
                            data);
                })
                .toList();
    }

    /**
     * Agrupa filas de estancia media mensual por doctor en series {@link DoctorKpiSeries}.
     *
     * @param rows proyecciones crudas de BD con doctor y estancia media mensual
     * @return lista de series por médico
     */
    private List<DoctorKpiSeries> groupDoctorAvgRows(List<DoctorMonthlyAvgProjection> rows) {
        Map<String, List<DoctorMonthlyAvgProjection>> grouped = rows.stream()
                .collect(Collectors.groupingBy(DoctorMonthlyAvgProjection::getDoctorId));

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<DoctorMonthlyAvgProjection> doctorRows = entry.getValue();
                    DoctorMonthlyAvgProjection first = doctorRows.getFirst();
                    List<MonthlyKpiEntry> data = doctorRows.stream()
                            .map(r -> new MonthlyKpiEntry(r.getMonth(), r.getAvgDays() != null ? r.getAvgDays() : 0.0))
                            .toList();
                    return new DoctorKpiSeries(
                            UUID.fromString(first.getDoctorId()),
                            first.getDoctorName(),
                            first.getDoctorSurname(),
                            data);
                })
                .toList();
    }

    /**
     * Calcula y asigna la duración de hospitalización en curso a un ingreso sin alta.
     *
     * @param admission ingreso de dominio al que se le asigna la estancia calculada
     * @return el mismo ingreso con el campo {@code hospitalizationLength} actualizado
     */
    private Admission setHospitalizationLength(Admission admission) {
        admission.setHospitalizationLength(admission.getLiveHospitalizationLength());
        return admission;
    }

}
