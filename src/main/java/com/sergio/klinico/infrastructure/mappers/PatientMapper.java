package com.sergio.klinico.infrastructure.mappers;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.domain.models.enums.PatientStatus;
import com.sergio.klinico.infrastructure.persistence.PatientEntity;
import com.sergio.klinico.infrastructure.rest.dto.requests.PatientRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.PatientResponse;
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

    // DE DOMINIO A ENTIDAD (Para el adaptador)
    PatientEntity toEntity(Patient patient);

    // DE ENTIDAD A DOMINIO (Para recuperar de DB)
    Patient toDomain(PatientEntity entity);

    // DE DOMINIO A RESPONSE (Para el controlador)
    PatientResponse toResponseFromDomain(Patient patient);

    /*
     *  Method to manage the status (string) and transform it to PatientStatus enum value
     */
    default PatientStatus mapStatus(String status) {
        if (status == null) return null;
        try {
            return PatientStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado de paciente no válido: " + status);
        }
    }
}