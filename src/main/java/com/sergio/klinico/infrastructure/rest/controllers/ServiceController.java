package com.sergio.klinico.infrastructure.rest.controllers;

import com.sergio.klinico.application.services.FindAllActiveServicesUseCase;
import com.sergio.klinico.domain.models.HospitalService;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.infrastructure.rest.dto.responses.PaginatedResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.service.ServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Slf4j
public class ServiceController {

    private final FindAllActiveServicesUseCase findAllActiveServicesUseCase;

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO', 'ADMINISTRATIVO')")
    public ResponseEntity<PaginatedResponse<ServiceResponse>> searchByName(
            @RequestParam String name,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "5") int size) {
        log.info("REQUEST: GET /services/search recibida con filtro de búsqueda: {}", name);

        PaginatedResult<HospitalService> services = findAllActiveServicesUseCase.execute(name, page, size);

        List<ServiceResponse> responseList = services.content().stream()
                .map(service -> new ServiceResponse(
                        service.getServiceId(),
                        service.getName(),
                        service.isActive()
                ))
                .toList();

        PaginatedResponse<ServiceResponse> response = PaginatedResponse.create(responseList, services);

        log.info("REQUEST: GET /services/search exitosa - {} servicios encontrados", response.getData().size());
        return ResponseEntity.ok(response);
    }
}
