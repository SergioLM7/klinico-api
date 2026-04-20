package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.UserWorkLoad;
import com.sergio.klinico.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public PaginatedResult<User> searchBySurnameAndServiceId(String surname, UUID serviceId, int page, int size) {
        return userRepository.searchBySurnameAndServiceId(surname, serviceId, page, size);
    }

    public PaginatedResult<UserWorkLoad> serviceWorkload (UUID serviceId, int page, int size) {
        return userRepository.calculateUserWorkload(serviceId, page, size);
    }
}
