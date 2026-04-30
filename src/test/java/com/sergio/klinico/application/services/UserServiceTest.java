package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.UserWorkLoad;
import com.sergio.klinico.domain.models.enums.UserRole;
import com.sergio.klinico.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private UUID serviceId;

    @BeforeEach
    void setUp() {
        serviceId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should return paginated users matching surname and serviceId")
    void searchBySurnameAndServiceId_ShouldReturnPaginatedUsers() {
        String surname = "García";
        int page = 0;
        int size = 10;

        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Luis")
                .surname("García López")
                .role(UserRole.MEDICO)
                .active(true)
                .serviceId(serviceId)
                .build();

        PaginatedResult<User> expected = new PaginatedResult<>(List.of(user), 1L, 1, page, true);
        when(userRepository.searchBySurnameAndServiceId(surname, serviceId, page, size)).thenReturn(expected);

        PaginatedResult<User> result = userService.searchBySurnameAndServiceId(surname, serviceId, page, size);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals("García López", result.content().get(0).getSurname());
        verify(userRepository).searchBySurnameAndServiceId(surname, serviceId, page, size);
    }

    @Test
    @DisplayName("Should return empty result when no users match surname and serviceId")
    void searchBySurnameAndServiceId_WhenNoUsersMatch_ShouldReturnEmptyResult() {
        String surname = "Inexistente";
        int page = 0;
        int size = 10;

        PaginatedResult<User> emptyResult = new PaginatedResult<>(List.of(), 0L, 0, page, true);
        when(userRepository.searchBySurnameAndServiceId(surname, serviceId, page, size)).thenReturn(emptyResult);

        PaginatedResult<User> result = userService.searchBySurnameAndServiceId(surname, serviceId, page, size);

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0L, result.totalElements());
        verify(userRepository).searchBySurnameAndServiceId(surname, serviceId, page, size);
    }

    @Test
    @DisplayName("Should return paginated workload data for a given service")
    void serviceWorkload_ShouldReturnPaginatedWorkload() {
        int page = 0;
        int size = 10;

        UserWorkLoad workLoad1 = UserWorkLoad.builder()
                .name("Ana")
                .surname("Martínez Sánchez")
                .admissionsAssigned(8L)
                .build();

        UserWorkLoad workLoad2 = UserWorkLoad.builder()
                .name("Pedro")
                .surname("López Díaz")
                .admissionsAssigned(3L)
                .build();

        PaginatedResult<UserWorkLoad> expected = new PaginatedResult<>(
                List.of(workLoad1, workLoad2), 2L, 1, page, true
        );
        when(userRepository.calculateUserWorkload(serviceId, page, size)).thenReturn(expected);

        PaginatedResult<UserWorkLoad> result = userService.serviceWorkload(serviceId, page, size);

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals(8L, result.content().get(0).getAdmissionsAssigned());
        assertEquals(3L, result.content().get(1).getAdmissionsAssigned());
        verify(userRepository).calculateUserWorkload(serviceId, page, size);
    }

    @Test
    @DisplayName("Should return empty workload when service has no users")
    void serviceWorkload_WhenNoUsersInService_ShouldReturnEmptyResult() {
        int page = 0;
        int size = 10;

        PaginatedResult<UserWorkLoad> emptyResult = new PaginatedResult<>(List.of(), 0L, 0, page, true);
        when(userRepository.calculateUserWorkload(serviceId, page, size)).thenReturn(emptyResult);

        PaginatedResult<UserWorkLoad> result = userService.serviceWorkload(serviceId, page, size);

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        verify(userRepository).calculateUserWorkload(serviceId, page, size);
    }
}
