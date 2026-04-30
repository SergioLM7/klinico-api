package com.sergio.klinico.infrastructure.mappers;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.domain.models.enums.PatientStatus;
import com.sergio.klinico.infrastructure.persistence.PatientEntity;
import com.sergio.klinico.infrastructure.rest.dto.requests.PatientRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.patient.PatientResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.patient.PatientSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {
        com.sergio.klinico.domain.models.enums.PatientStatus.class,
        com.sergio.klinico.domain.exceptions.BusinessException.class
})
public interface PatientMapper {

    // DE REQUEST A DOMINIO
    @Mapping(target = "status", expression = "java(mapStatus(request.getStatus()))")
    @Mapping(target = "patientId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Patient toDomainFromDto(PatientRequest request);

    // DE DOMINIO A ENTIDAD (Para el adapter)
    PatientEntity toEntity(Patient patient);

    // DE ENTIDAD A DOMINIO (Para recuperar de BD)
    Patient toDomain(PatientEntity entity);

    // DE DOMINIO A RESPONSE (Para el GET paginated)
    PatientResponse toResponseFromDomain(Patient patient);

    // DE DOMINIO A RESPONSE (Para el create)
    PatientSummaryResponse toSummaryResponseFromDomain(Patient patient);

    /*
     * Transformación del estado (string) a un valor del enum PatientStatus
     */
    default PatientStatus mapStatus(String status) {
        if (status == null)
            return null;
        try {
            return PatientStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException _) {
            throw new BusinessException("Estado de paciente no válido: " + status);
        }
    }
}