package com.sergio.klinico.infrastructure.mappers;

import com.sergio.klinico.domain.models.HospitalService;
import com.sergio.klinico.infrastructure.persistence.ServiceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", imports = {
        com.sergio.klinico.domain.exceptions.BusinessException.class
})
public interface ServiceMapper {

    ServiceEntity toEntity(HospitalService service);

    HospitalService toDomain(ServiceEntity serviceEntity);
}
