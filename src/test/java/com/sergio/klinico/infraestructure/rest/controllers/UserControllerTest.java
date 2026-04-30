package com.sergio.klinico.infraestructure.rest.controllers;

import com.sergio.klinico.application.services.UserService;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.UserWorkLoad;
import com.sergio.klinico.domain.models.enums.UserRole;
import com.sergio.klinico.infrastructure.rest.advice.GlobalExceptionHandler;
import com.sergio.klinico.infrastructure.rest.controllers.UserController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    private final UUID serviceId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        authenticatedUser = User.builder()
                .id(userId)
                .email("jefe@test.com")
                .name("Elena")
                .surname("Torres")
                .role(UserRole.JEFESERVICIO)
                .active(true)
                .serviceId(serviceId)
                .build();

        var auth = UsernamePasswordAuthenticationToken.authenticated(
                authenticatedUser, null, authenticatedUser.getAuthorities());
        var ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return 200 with matching users when searching by surname")
    void searchBySurname_WhenUsersMatch_ShouldReturn200() throws Exception {
        String surname = "García";

        User matchingUser = User.builder()
                .id(UUID.randomUUID())
                .name("Luis")
                .surname("García López")
                .email("luis@test.com")
                .role(UserRole.MEDICO)
                .active(true)
                .serviceId(serviceId)
                .build();

        PaginatedResult<User> paginatedResult = new PaginatedResult<>(
                List.of(matchingUser), 1L, 1, 0, true
        );

        when(userService.searchBySurnameAndServiceId(surname, serviceId, 0, 10))
                .thenReturn(paginatedResult);

        mockMvc.perform(get("/api/v1/users/search")
                        .param("surname", surname)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Luis"))
                .andExpect(jsonPath("$.data[0].surname").value("García López"))
                .andExpect(jsonPath("$.data[0].email").value("luis@test.com"))
                .andExpect(jsonPath("$.data[0].role").value(UserRole.MEDICO.name()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(userService).searchBySurnameAndServiceId(surname, serviceId, 0, 10);
    }

    @Test
    @DisplayName("Should return 200 with empty data when no users match the surname filter")
    void searchBySurname_WhenNoUsersMatch_ShouldReturn200WithEmptyData() throws Exception {
        String surname = "Inexistente";

        PaginatedResult<User> emptyResult = new PaginatedResult<>(List.of(), 0L, 0, 0, true);

        when(userService.searchBySurnameAndServiceId(surname, serviceId, 0, 10))
                .thenReturn(emptyResult);

        mockMvc.perform(get("/api/v1/users/search")
                        .param("surname", surname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Should use default pagination when page and size are not provided")
    void searchBySurname_WhenNoPaginationProvided_ShouldUseDefaults() throws Exception {
        String surname = "López";

        PaginatedResult<User> emptyResult = new PaginatedResult<>(List.of(), 0L, 0, 0, true);
        when(userService.searchBySurnameAndServiceId(surname, serviceId, 0, 10))
                .thenReturn(emptyResult);

        mockMvc.perform(get("/api/v1/users/search")
                        .param("surname", surname))
                .andExpect(status().isOk());

        verify(userService).searchBySurnameAndServiceId(surname, serviceId, 0, 10);
    }

    @Test
    @DisplayName("Should return 200 with workload data for the authenticated user's service")
    void getServiceWorkload_ShouldReturn200WithWorkloadData() throws Exception {
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

        PaginatedResult<UserWorkLoad> paginatedResult = new PaginatedResult<>(
                List.of(workLoad1, workLoad2), 2L, 1, 0, true
        );

        when(userService.serviceWorkload(serviceId, 0, 10)).thenReturn(paginatedResult);

        mockMvc.perform(get("/api/v1/users/service-workload")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("Ana"))
                .andExpect(jsonPath("$.data[0].surname").value("Martínez Sánchez"))
                .andExpect(jsonPath("$.data[0].admissionsAssigned").value(8))
                .andExpect(jsonPath("$.data[1].name").value("Pedro"))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(userService).serviceWorkload(serviceId, 0, 10);
    }

    @Test
    @DisplayName("Should return 200 with empty data when no workload exists for the service")
    void getServiceWorkload_WhenNoWorkload_ShouldReturn200WithEmptyData() throws Exception {
        PaginatedResult<UserWorkLoad> emptyResult = new PaginatedResult<>(List.of(), 0L, 0, 0, true);

        when(userService.serviceWorkload(serviceId, 0, 10)).thenReturn(emptyResult);

        mockMvc.perform(get("/api/v1/users/service-workload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Should use default pagination when page and size are not provided")
    void getServiceWorkload_WhenNoPaginationProvided_ShouldUseDefaults() throws Exception {
        PaginatedResult<UserWorkLoad> emptyResult = new PaginatedResult<>(List.of(), 0L, 0, 0, true);
        when(userService.serviceWorkload(serviceId, 0, 10)).thenReturn(emptyResult);

        mockMvc.perform(get("/api/v1/users/service-workload"))
                .andExpect(status().isOk());

        verify(userService).serviceWorkload(serviceId, 0, 10);
    }
}
