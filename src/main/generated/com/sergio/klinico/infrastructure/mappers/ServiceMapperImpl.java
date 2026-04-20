package com.sergio.klinico.infrastructure.mappers;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.HospitalService;
import com.sergio.klinico.infrastructure.persistence.ServiceEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-20T18:39:15+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class ServiceMapperImpl implements ServiceMapper {

    @Override
    public ServiceEntity toEntity(HospitalService service) {
        if ( service == null ) {
            return null;
        }

        ServiceEntity serviceEntity = new ServiceEntity();

        serviceEntity.setCreatedAt( service.getCreatedAt() );
        serviceEntity.setCreatedBy( service.getCreatedBy() );
        serviceEntity.setLastModifiedAt( service.getLastModifiedAt() );
        serviceEntity.setLastModifiedBy( service.getLastModifiedBy() );
        serviceEntity.setServiceId( service.getServiceId() );
        serviceEntity.setName( service.getName() );
        serviceEntity.setActive( service.isActive() );

        return serviceEntity;
    }

    @Override
    public HospitalService toDomain(ServiceEntity serviceEntity) {
        if ( serviceEntity == null ) {
            return null;
        }

        HospitalService.HospitalServiceBuilder hospitalService = HospitalService.builder();

        hospitalService.serviceId( serviceEntity.getServiceId() );
        hospitalService.name( serviceEntity.getName() );
        hospitalService.active( serviceEntity.isActive() );
        hospitalService.createdAt( serviceEntity.getCreatedAt() );
        hospitalService.createdBy( serviceEntity.getCreatedBy() );
        hospitalService.lastModifiedAt( serviceEntity.getLastModifiedAt() );
        hospitalService.lastModifiedBy( serviceEntity.getLastModifiedBy() );

        return hospitalService.build();
    }
}
