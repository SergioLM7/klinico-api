package com.sergio.klinico.infrastructure.persistence.adapters;

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

@Component
@RequiredArgsConstructor
public class PatientPersistenceAdapter implements PatientRepository {

    private final JpaPatientRepository jpaRepository;
    private final PatientMapper patientMapper;

    @Override
    public Patient save(Patient patient) {
        PatientEntity entity = patientMapper.toEntity(patient);

        // Si es una actualización (el ID ya existe) le asignamos la versión actual a la entidad que vamos a guardar
        if (patient.getPatientId() != null) {
            jpaRepository.findById(patient.getPatientId()).ifPresent(existingEntity ->
                entity.setVersion(existingEntity.getVersion())
            );
        }

        PatientEntity savedEntity = jpaRepository.save(entity);
        return patientMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByDni(String dni) {
        return jpaRepository.existsByDni(dni);
    }

    @Override
    public Patient findById(UUID id) {
        Optional<PatientEntity> entity = jpaRepository.findById(id);
        return entity.map(patientMapper::toDomain).orElse(null);
    }

    @Override
    public List<Patient> findAllByIds(List<UUID> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(patientMapper::toDomain)
                .toList();
    }

    @Override
    public PaginatedResult<Patient> findAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PatientEntity> entitiesPage = jpaRepository.findAll(pageRequest);

        // Mapeamos las entidades a dominio
        List<Patient> domainList = entitiesPage.getContent().stream()
                .map(patientMapper::toDomain)
                .toList();

        // Devolvemos nuestro envoltorio propio
        return new PaginatedResult<>(
                domainList,
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast()
        );
    }

    @Override
    public PaginatedResult<Patient> findBySurnameAndStatusAlta(String surname, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PatientEntity> entitiesPage = jpaRepository.findBySurnameContainingIgnoreCaseAndStatus(
                surname, PatientStatus.ALTA, pageRequest);

        // Mapeamos las entidades a dominio
        List<Patient> domainList = entitiesPage.getContent().stream()
                .map(patientMapper::toDomain)
                .toList();

        // Devolvemos nuestro envoltorio propio
        return new PaginatedResult<>(
                domainList,
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast()
        );
    }

}