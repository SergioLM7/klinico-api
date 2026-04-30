package com.sergio.klinico.infrastructure.mappers;

import com.sergio.klinico.domain.models.Admission;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.infrastructure.persistence.AdmissionEntity;
import com.sergio.klinico.infrastructure.rest.dto.requests.AdmissionRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.admission.AdmissionResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.admission.AdmissionSummaryResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.patient.PatientPreviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {
        com.sergio.klinico.domain.exceptions.BusinessException.class
})
public interface AdmissionMapper {

    // De Request (DTO) a Dominio
    @Mapping(target = "admissionId", ignore = true)
    @Mapping(target = "hospitalizationLength", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Admission toDomainFromRequest(AdmissionRequest request);

    // De Dominio a Entidad
    AdmissionEntity toEntity(Admission domain);

    // De Entidad a Dominio
    @Mapping(target = "episodes", ignore = true)
    Admission toDomain(AdmissionEntity entity);

    // De Dominio a Response (Detalle completo)
    AdmissionResponse toResponseFromDomain(Admission domain);

    // De Dominio a Response enriquecido con datos del paciente (para listados)
    default AdmissionResponse toResponseFromDomain(Admission domain, Patient patient) {
        AdmissionResponse response = toResponseFromDomain(domain);
        if (patient != null) {
            response.setPatient(new PatientPreviewResponse(
                    patient.getPatientId(),
                    patient.getName(),
                    patient.getSurname(),
                    patient.getBirthdate(),
                    patient.getSex()
            ));
        }
        return response;
    }

    // De Dominio a Response (Resumen)
    AdmissionSummaryResponse toSummaryResponseFromDomain(Admission domain);
}
