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

@Component
@RequiredArgsConstructor
public class AdmissionPersistenceAdapter implements AdmissionRepository {

    public static final String CREATED_AT = "createdAt";
    private final JpaAdmissionRepository jpaRepository;
    private final AdmissionMapper mapper;

    @Override
    public Admission save(Admission admission) {
        AdmissionEntity entity = mapper.toEntity(admission);

        // Si es una actualización (el ID ya existe) le asignamos la versión actual a la entidad que vamos a guardar
        if (admission.getAdmissionId() != null) {
            jpaRepository.findById(admission.getAdmissionId()).ifPresent(existingEntity ->
                    entity.setVersion(existingEntity.getVersion())
            );
        }

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public boolean existsActiveAdmissionByPatientId(UUID patientId) {
        return jpaRepository.existsByPatientIdAndDischargeDateIsNull(patientId);
    }

    @Override
    public Admission findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public Double getAverageHospitalizationLengthByService(UUID serviceId) {
        return jpaRepository.getAverageLengthByService(serviceId);
    }

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

    @Override
    public List<MonthlyKpiEntry> countAdmissionsByServiceAndYear(UUID serviceId, int year) {
        return jpaRepository.countAdmissionsByServiceAndYear(serviceId, year).stream()
                .map(p -> new MonthlyKpiEntry(p.getMonth(), p.getCount().doubleValue()))
                .toList();
    }

    @Override
    public List<MonthlyKpiEntry> countAdmissionsByServiceAndYearAndMonth(UUID serviceId, int year, int month) {
        return jpaRepository.countAdmissionsByServiceAndYearAndMonth(serviceId, year, month).stream()
                .map(p -> new MonthlyKpiEntry(p.getMonth(), p.getCount().doubleValue()))
                .toList();
    }

    @Override
    public List<DoctorKpiSeries> countAdmissionsByDoctorAndServiceAndYear(UUID serviceId, int year) {
        return groupDoctorCountRows(
                jpaRepository.countAdmissionsByDoctorAndServiceAndYear(serviceId, year));
    }

    @Override
    public List<DoctorKpiSeries> countAdmissionsByDoctorAndServiceAndYearAndMonth(UUID serviceId, int year, int month) {
        return groupDoctorCountRows(
                jpaRepository.countAdmissionsByDoctorAndServiceAndYearAndMonth(serviceId, year, month));
    }

    @Override
    public List<MonthlyKpiEntry> avgStayByServiceAndYear(UUID serviceId, int year) {
        return jpaRepository.avgStayByServiceAndYear(serviceId, year).stream()
                .map(p -> new MonthlyKpiEntry(p.getMonth(), p.getAvgDays() != null ? p.getAvgDays() : 0.0))
                .toList();
    }

    @Override
    public List<MonthlyKpiEntry> avgStayByServiceAndYearAndMonth(UUID serviceId, int year, int month) {
        return jpaRepository.avgStayByServiceAndYearAndMonth(serviceId, year, month).stream()
                .map(p -> new MonthlyKpiEntry(p.getMonth(), p.getAvgDays() != null ? p.getAvgDays() : 0.0))
                .toList();
    }

    @Override
    public List<DoctorKpiSeries> avgStayByDoctorAndServiceAndYear(UUID serviceId, int year) {
        return groupDoctorAvgRows(
                jpaRepository.avgStayByDoctorAndServiceAndYear(serviceId, year));
    }

    @Override
    public List<DoctorKpiSeries> avgStayByDoctorAndServiceAndYearAndMonth(UUID serviceId, int year, int month) {
        return groupDoctorAvgRows(
                jpaRepository.avgStayByDoctorAndServiceAndYearAndMonth(serviceId, year, month));
    }

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

    private Admission setHospitalizationLength(Admission admission) {
        admission.setHospitalizationLength(admission.getLiveHospitalizationLength());
        return admission;
    }

}
