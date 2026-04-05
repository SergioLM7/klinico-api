package com.sergio.klinico.infrastructure.mappers;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Admission;
import com.sergio.klinico.domain.models.Episode;
import com.sergio.klinico.infrastructure.persistence.AdmissionEntity;
import com.sergio.klinico.infrastructure.persistence.EpisodeEntity;
import com.sergio.klinico.infrastructure.rest.dto.requests.AdmissionRequest;
import com.sergio.klinico.infrastructure.rest.dto.responses.admission.AdmissionResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.admission.AdmissionSummaryResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T13:27:04+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class AdmissionMapperImpl implements AdmissionMapper {

    @Override
    public Admission toDomainFromRequest(AdmissionRequest request) {
        if ( request == null ) {
            return null;
        }

        Admission.AdmissionBuilder admission = Admission.builder();

        admission.patientId( request.getPatientId() );
        admission.serviceId( request.getServiceId() );
        admission.assignedDoctorId( request.getAssignedDoctorId() );
        admission.dischargeDate( request.getDischargeDate() );
        admission.principalDiagnosis( request.getPrincipalDiagnosis() );
        admission.medicalHistory( request.getMedicalHistory() );
        admission.allergies( request.getAllergies() );
        admission.chronicTreatment( request.getChronicTreatment() );
        admission.basalBarthel( request.getBasalBarthel() );
        admission.roomNumber( request.getRoomNumber() );

        return admission.build();
    }

    @Override
    public AdmissionEntity toEntity(Admission domain) {
        if ( domain == null ) {
            return null;
        }

        AdmissionEntity admissionEntity = new AdmissionEntity();

        admissionEntity.setCreatedAt( domain.getCreatedAt() );
        admissionEntity.setCreatedBy( domain.getCreatedBy() );
        admissionEntity.setLastModifiedAt( domain.getLastModifiedAt() );
        admissionEntity.setLastModifiedBy( domain.getLastModifiedBy() );
        admissionEntity.setVersion( domain.getVersion() );
        admissionEntity.setAdmissionId( domain.getAdmissionId() );
        admissionEntity.setPatientId( domain.getPatientId() );
        admissionEntity.setServiceId( domain.getServiceId() );
        admissionEntity.setAssignedDoctorId( domain.getAssignedDoctorId() );
        admissionEntity.setDischargeDate( domain.getDischargeDate() );
        admissionEntity.setHospitalizationLength( domain.getHospitalizationLength() );
        admissionEntity.setRoomNumber( domain.getRoomNumber() );
        admissionEntity.setPrincipalDiagnosis( domain.getPrincipalDiagnosis() );
        admissionEntity.setMedicalHistory( domain.getMedicalHistory() );
        admissionEntity.setAllergies( domain.getAllergies() );
        admissionEntity.setChronicTreatment( domain.getChronicTreatment() );
        admissionEntity.setBasalBarthel( domain.getBasalBarthel() );
        admissionEntity.setEpisodes( episodeListToEpisodeEntityList( domain.getEpisodes() ) );

        return admissionEntity;
    }

    @Override
    public Admission toDomain(AdmissionEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Admission.AdmissionBuilder admission = Admission.builder();

        admission.version( entity.getVersion() );
        admission.admissionId( entity.getAdmissionId() );
        admission.patientId( entity.getPatientId() );
        admission.serviceId( entity.getServiceId() );
        admission.assignedDoctorId( entity.getAssignedDoctorId() );
        admission.dischargeDate( entity.getDischargeDate() );
        admission.hospitalizationLength( entity.getHospitalizationLength() );
        admission.principalDiagnosis( entity.getPrincipalDiagnosis() );
        admission.medicalHistory( entity.getMedicalHistory() );
        admission.allergies( entity.getAllergies() );
        admission.chronicTreatment( entity.getChronicTreatment() );
        admission.basalBarthel( entity.getBasalBarthel() );
        admission.roomNumber( entity.getRoomNumber() );
        admission.createdAt( entity.getCreatedAt() );
        admission.createdBy( entity.getCreatedBy() );
        admission.lastModifiedAt( entity.getLastModifiedAt() );
        admission.lastModifiedBy( entity.getLastModifiedBy() );

        return admission.build();
    }

    @Override
    public AdmissionResponse toResponseFromDomain(Admission domain) {
        if ( domain == null ) {
            return null;
        }

        AdmissionResponse.AdmissionResponseBuilder admissionResponse = AdmissionResponse.builder();

        admissionResponse.admissionId( domain.getAdmissionId() );
        admissionResponse.serviceId( domain.getServiceId() );
        admissionResponse.assignedDoctorId( domain.getAssignedDoctorId() );
        admissionResponse.dischargeDate( domain.getDischargeDate() );
        admissionResponse.hospitalizationLength( domain.getHospitalizationLength() );
        admissionResponse.principalDiagnosis( domain.getPrincipalDiagnosis() );
        admissionResponse.medicalHistory( domain.getMedicalHistory() );
        admissionResponse.allergies( domain.getAllergies() );
        admissionResponse.chronicTreatment( domain.getChronicTreatment() );
        admissionResponse.basalBarthel( domain.getBasalBarthel() );
        admissionResponse.roomNumber( domain.getRoomNumber() );
        admissionResponse.createdAt( domain.getCreatedAt() );
        admissionResponse.createdBy( domain.getCreatedBy() );
        admissionResponse.lastModifiedAt( domain.getLastModifiedAt() );
        admissionResponse.lastModifiedBy( domain.getLastModifiedBy() );

        return admissionResponse.build();
    }

    @Override
    public AdmissionSummaryResponse toSummaryResponseFromDomain(Admission domain) {
        if ( domain == null ) {
            return null;
        }

        UUID admissionId = null;
        UUID patientId = null;
        UUID serviceId = null;
        UUID assignedDoctorId = null;
        String principalDiagnosis = null;
        String allergies = null;
        String chronicTreatment = null;
        Integer basalBarthel = null;

        admissionId = domain.getAdmissionId();
        patientId = domain.getPatientId();
        serviceId = domain.getServiceId();
        assignedDoctorId = domain.getAssignedDoctorId();
        principalDiagnosis = domain.getPrincipalDiagnosis();
        allergies = domain.getAllergies();
        chronicTreatment = domain.getChronicTreatment();
        basalBarthel = domain.getBasalBarthel();

        AdmissionSummaryResponse admissionSummaryResponse = new AdmissionSummaryResponse( admissionId, patientId, serviceId, assignedDoctorId, principalDiagnosis, allergies, chronicTreatment, basalBarthel );

        return admissionSummaryResponse;
    }

    protected EpisodeEntity episodeToEpisodeEntity(Episode episode) {
        if ( episode == null ) {
            return null;
        }

        EpisodeEntity episodeEntity = new EpisodeEntity();

        episodeEntity.setCreatedAt( episode.getCreatedAt() );
        episodeEntity.setCreatedBy( episode.getCreatedBy() );
        episodeEntity.setLastModifiedAt( episode.getLastModifiedAt() );
        episodeEntity.setLastModifiedBy( episode.getLastModifiedBy() );
        episodeEntity.setVersion( episode.getVersion() );
        episodeEntity.setEpisodeId( episode.getEpisodeId() );
        episodeEntity.setDoctorId( episode.getDoctorId() );
        episodeEntity.setClinicalProgress( episode.getClinicalProgress() );
        episodeEntity.setDiagnosis( episode.getDiagnosis() );
        episodeEntity.setBradenScore( episode.getBradenScore() );
        episodeEntity.setCamScore( episode.getCamScore() );
        episodeEntity.setChads2Score( episode.getChads2Score() );

        return episodeEntity;
    }

    protected List<EpisodeEntity> episodeListToEpisodeEntityList(List<Episode> list) {
        if ( list == null ) {
            return null;
        }

        List<EpisodeEntity> list1 = new ArrayList<EpisodeEntity>( list.size() );
        for ( Episode episode : list ) {
            list1.add( episodeToEpisodeEntity( episode ) );
        }

        return list1;
    }
}
