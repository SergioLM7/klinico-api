package com.sergio.klinico.infrastructure.persistence.adapters;

import com.sergio.klinico.domain.models.Admission;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.repositories.AdmissionRepository;
import com.sergio.klinico.infrastructure.mappers.AdmissionMapper;
import com.sergio.klinico.infrastructure.persistence.AdmissionEntity;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaAdmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdmissionPersistenceAdapter implements AdmissionRepository {

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
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<AdmissionEntity> entitiesPage = jpaRepository.findByDischargeDateIsNull(pageRequest);

        return new PaginatedResult<>(
                entitiesPage.getContent().stream().map(mapper::toDomain).toList(),
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast());
    }

    @Override
    public PaginatedResult<Admission> findAllActiveByService(UUID serviceId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdmissionEntity> entitiesPage = jpaRepository.findByServiceIdAndDischargeDateIsNull(serviceId,
                pageRequest);

        return new PaginatedResult<>(
                entitiesPage.getContent().stream().map(mapper::toDomain).toList(),
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast());
    }

    @Override
    public PaginatedResult<Admission> findAllActiveByDoctor(UUID doctorId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdmissionEntity> entitiesPage = jpaRepository.findByAssignedDoctorIdAndDischargeDateIsNull(doctorId,
                pageRequest);

        return new PaginatedResult<>(
                entitiesPage.getContent().stream().map(mapper::toDomain).toList(),
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast());
    }
}
