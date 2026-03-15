package com.sergio.klinico.infrastructure.mappers;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Patient;
import com.sergio.klinico.domain.models.enums.PatientStatus;
import com.sergio.klinico.infrastructure.persistence.PatientEntity;
import com.sergio.klinico.infrastructure.rest.dto.requests.PatientRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.patient.PatientResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.patient.PatientSummaryResponse;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-15T14:05:25+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class PatientMapperImpl implements PatientMapper {

    @Override
    public Patient toDomainFromDto(PatientRequest request) {
        if ( request == null ) {
            return null;
        }

        Patient.PatientBuilder patient = Patient.builder();

        patient.dni( request.getDni() );
        patient.name( request.getName() );
        patient.surname( request.getSurname() );
        if ( request.getSex() != null ) {
            patient.sex( request.getSex().charAt( 0 ) );
        }
        patient.birthdate( request.getBirthdate() );
        patient.address( request.getAddress() );
        patient.contactNumber( request.getContactNumber() );
        patient.relativeContactNumber( request.getRelativeContactNumber() );

        patient.status( mapStatus(request.getStatus()) );

        return patient.build();
    }

    @Override
    public PatientEntity toEntity(Patient patient) {
        if ( patient == null ) {
            return null;
        }

        PatientEntity.PatientEntityBuilder<?, ?> patientEntity = PatientEntity.builder();

        patientEntity.createdAt( patient.getCreatedAt() );
        patientEntity.createdBy( patient.getCreatedBy() );
        patientEntity.lastModifiedAt( patient.getLastModifiedAt() );
        patientEntity.lastModifiedBy( patient.getLastModifiedBy() );
        patientEntity.patientId( patient.getPatientId() );
        patientEntity.dni( patient.getDni() );
        patientEntity.name( patient.getName() );
        patientEntity.surname( patient.getSurname() );
        if ( patient.getSex() != null ) {
            patientEntity.sex( patient.getSex() );
        }
        patientEntity.birthdate( patient.getBirthdate() );
        patientEntity.address( patient.getAddress() );
        patientEntity.contactNumber( patient.getContactNumber() );
        patientEntity.relativeContactNumber( patient.getRelativeContactNumber() );
        patientEntity.status( patient.getStatus() );

        return patientEntity.build();
    }

    @Override
    public Patient toDomain(PatientEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Patient.PatientBuilder patient = Patient.builder();

        patient.patientId( entity.getPatientId() );
        patient.dni( entity.getDni() );
        patient.name( entity.getName() );
        patient.surname( entity.getSurname() );
        patient.sex( entity.getSex() );
        patient.birthdate( entity.getBirthdate() );
        patient.address( entity.getAddress() );
        patient.contactNumber( entity.getContactNumber() );
        patient.relativeContactNumber( entity.getRelativeContactNumber() );
        patient.status( entity.getStatus() );
        patient.createdAt( entity.getCreatedAt() );
        patient.createdBy( entity.getCreatedBy() );
        patient.lastModifiedAt( entity.getLastModifiedAt() );
        patient.lastModifiedBy( entity.getLastModifiedBy() );

        return patient.build();
    }

    @Override
    public PatientResponse toResponseFromDomain(Patient patient) {
        if ( patient == null ) {
            return null;
        }

        PatientResponse.PatientResponseBuilder patientResponse = PatientResponse.builder();

        patientResponse.patientId( patient.getPatientId() );
        patientResponse.dni( patient.getDni() );
        patientResponse.name( patient.getName() );
        patientResponse.surname( patient.getSurname() );
        if ( patient.getSex() != null ) {
            patientResponse.sex( patient.getSex() );
        }
        patientResponse.birthdate( patient.getBirthdate() );
        patientResponse.address( patient.getAddress() );
        patientResponse.contactNumber( patient.getContactNumber() );
        patientResponse.relativeContactNumber( patient.getRelativeContactNumber() );
        patientResponse.status( patient.getStatus() );
        patientResponse.createdAt( patient.getCreatedAt() );
        if ( patient.getCreatedBy() != null ) {
            patientResponse.createdBy( patient.getCreatedBy().toString() );
        }
        patientResponse.lastModifiedAt( patient.getLastModifiedAt() );
        if ( patient.getLastModifiedBy() != null ) {
            patientResponse.lastModifiedBy( patient.getLastModifiedBy().toString() );
        }

        return patientResponse.build();
    }

    @Override
    public PatientSummaryResponse toSummaryResponseFromDomain(Patient patient) {
        if ( patient == null ) {
            return null;
        }

        UUID patientId = null;
        String dni = null;
        String name = null;
        String surname = null;
        PatientStatus status = null;

        patientId = patient.getPatientId();
        dni = patient.getDni();
        name = patient.getName();
        surname = patient.getSurname();
        status = patient.getStatus();

        PatientSummaryResponse patientSummaryResponse = new PatientSummaryResponse( patientId, dni, name, surname, status );

        return patientSummaryResponse;
    }
}
