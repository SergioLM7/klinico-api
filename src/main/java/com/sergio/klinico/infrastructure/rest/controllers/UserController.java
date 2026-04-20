package com.sergio.klinico.infrastructure.rest.controllers;

import com.sergio.klinico.application.services.UserService;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.UserWorkLoad;
import com.sergio.klinico.infrastructure.rest.dto.responses.PaginatedResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.user.UserResponse;
import com.sergio.klinico.infrastructure.rest.dto.responses.user.UserServiceWorkloadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MEDICO', 'JEFESERVICIO', 'SYSADMIN')")
    public ResponseEntity<PaginatedResponse<UserResponse>> searchBySurname(
            @RequestParam String surname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user
    ) {
        log.info("REQUEST: GET /users/search recibida con apellido: {}", surname);

        PaginatedResult<User> result = userService.searchBySurnameAndServiceId(surname, user.getServiceId(), page, size);

        List<UserResponse> responseList = result.content().stream()
                .map(u -> new UserResponse(u.getId(), u.getName(), u.getSurname(), u.getEmail(), u.getRole()))
                .toList();

        PaginatedResponse<UserResponse> response = PaginatedResponse.create(responseList, result);

        log.info("REQUEST: GET /users/search exitosa - {} usuarios encontrados", responseList.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/service-workload")
    @PreAuthorize("hasAnyRole('JEFESERVICIO')")
    public ResponseEntity<PaginatedResponse<UserServiceWorkloadResponse>> getServiceWorkload(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user
    ) {
        log.info("REQUEST: GET /users/service-workload recibida para el servicio: {}", user.getServiceId());

        PaginatedResult<UserWorkLoad> result = userService.serviceWorkload(user.getServiceId(), page, size);

        List<UserServiceWorkloadResponse> responseList = result.content().stream()
                .map(u -> new UserServiceWorkloadResponse(u.getName(), u.getSurname(), u.getAdmissionsAssigned()))
                .toList();

        PaginatedResponse<UserServiceWorkloadResponse> response = PaginatedResponse.create(responseList, result);

        log.info("REQUEST: GET /users/service-workload exitosa - {} usuarios encontrados", responseList.size());
        return ResponseEntity.ok(response);
    }
}
