package com.sergio.klinico.infrastructure.persistence.adapters;

import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.models.User;
import com.sergio.klinico.domain.models.UserWorkLoad;
import com.sergio.klinico.domain.models.enums.UserRole;
import com.sergio.klinico.domain.repositories.UserRepository;
import com.sergio.klinico.infrastructure.persistence.UserEntity;
import com.sergio.klinico.infrastructure.persistence.repositories.JpaUserRepository;
import com.sergio.klinico.infrastructure.persistence.repositories.projections.UserWorkloadProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia para la entidad {@link UserEntity}.
 *
 * <p>Implementa el puerto de dominio {@link UserRepository} mediante Spring Data JPA.
 * A diferencia del resto de adaptadores no utiliza MapStruct, sino que realiza el mapeo
 * manualmente mediante el builder de {@link User} para mantener el control explícito
 * sobre qué campos del dominio se exponen.</p>
 *
 * <p>La contraseña solo se incluye en el dominio cuando es estrictamente necesaria
 * (login y resolución de jefe de servicio); las consultas de búsqueda y carga de trabajo
 * no la mapean por seguridad.</p>
 */
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    /**
     * Busca un usuario activo por su dirección de correo electrónico.
     *
     * <p>Incluye la contraseña hasheada en el dominio para permitir la verificación
     * durante el proceso de login.</p>
     *
     * @param email dirección de correo electrónico del usuario
     * @return {@link Optional} con el usuario activo encontrado, o vacío si no existe
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmailAndActiveTrue(email)
                .map(entity -> User.builder()
                        .id(entity.getUserId())
                        .email(entity.getEmail())
                        .password(entity.getPassword())
                        .active(entity.isActive())
                        .role(entity.getRole())
                        .name(entity.getName())
                        .surname(entity.getSurname())
                        .serviceId(entity.getServiceId())
                        .build());
    }

    /**
     * Busca un usuario (activo o inactivo) por su identificador único.
     *
     * <p>No incluye la contraseña en el dominio ya que este método se usa para
     * resolución de usuarios en operaciones que no requieren autenticación.</p>
     *
     * @param id UUID del usuario
     * @return {@link Optional} con el usuario encontrado, o vacío si no existe
     */
    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id)
                .map(userEntity -> User.builder()
                        .id(userEntity.getUserId())
                        .email(userEntity.getEmail())
                        .active(userEntity.isActive())
                        .role(userEntity.getRole())
                        .name(userEntity.getName())
                        .surname(userEntity.getSurname())
                        .serviceId(userEntity.getServiceId())
                        .build());
    }

    /**
     * Busca el usuario activo con el rol y servicio indicados.
     *
     * <p>Se utiliza para encontrar al jefe de servicio activo de un servicio hospitalario.
     * Incluye la contraseña en el dominio por coherencia con la firma del repositorio,
     * aunque en este contexto no se usa para autenticación.</p>
     *
     * @param serviceId UUID del servicio hospitalario
     * @param role      rol del usuario a buscar (p.ej. {@link UserRole#JEFESERVICIO})
     * @return {@link Optional} con el usuario activo del rol y servicio indicados, o vacío si no existe
     */
    @Override
    public Optional<User> findByServiceIdAndRoleAndActiveTrue(UUID serviceId, UserRole role) {
        return jpaUserRepository.findByServiceIdAndRoleAndActiveTrue(serviceId, role.name())
                .map(entity -> User.builder()
                        .id(entity.getUserId())
                        .email(entity.getEmail())
                        .password(entity.getPassword())
                        .active(entity.isActive())
                        .role(entity.getRole())
                        .name(entity.getName())
                        .surname(entity.getSurname())
                        .serviceId(entity.getServiceId())
                        .build());
    }

    /**
     * Busca usuarios activos de un servicio por apellido (búsqueda parcial, insensible a
     * mayúsculas), ordenados alfabéticamente por apellido.
     *
     * @param surname   apellido o fragmento del apellido a buscar
     * @param serviceId UUID del servicio hospitalario al que se acota la búsqueda
     * @param page      número de página (0-indexed)
     * @param size      número de elementos por página
     * @return resultado paginado de usuarios activos que coinciden con el criterio
     */
    @Override
    public PaginatedResult<User> searchBySurnameAndServiceId(String surname, UUID serviceId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("surname").ascending());
        Page<UserEntity> entitiesPage = jpaUserRepository.findBySurnameContainingIgnoreCaseAndServiceIdAndActiveTrue(
                surname, serviceId, pageRequest);

        return new PaginatedResult<>(
                entitiesPage.getContent().stream()
                        .map(entity -> User.builder()
                                .id(entity.getUserId())
                                .email(entity.getEmail())
                                .active(entity.isActive())
                                .role(entity.getRole())
                                .name(entity.getName())
                                .surname(entity.getSurname())
                                .serviceId(entity.getServiceId())
                                .build())
                        .toList(),
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast());
    }

    /**
     * Calcula la carga de trabajo de los médicos de un servicio como el número de ingresos
     * activos asignados a cada uno.
     *
     * <p>Utiliza una consulta nativa con proyección {@link UserWorkloadProjection}
     * para obtener el nombre, apellido y número de ingresos activos de cada médico.</p>
     *
     * @param serviceId UUID del servicio hospitalario
     * @param page      número de página (0-indexed)
     * @param size      número de elementos por página
     * @return resultado paginado con la carga de trabajo de cada médico del servicio
     */
    @Override
    public PaginatedResult<UserWorkLoad> calculateUserWorkload(UUID serviceId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<UserWorkloadProjection> entitiesPage = jpaUserRepository.calculateUserWorkload(serviceId, pageRequest);

        return new PaginatedResult<>(entitiesPage.getContent().stream()
                .map(entity -> UserWorkLoad.builder()
                        .name(entity.getName())
                        .surname(entity.getSurname())
                        .admissionsAssigned(entity.getAdmissionsAssigned())
                        .build())
                .toList(),
                entitiesPage.getTotalElements(),
                entitiesPage.getTotalPages(),
                entitiesPage.getNumber(),
                entitiesPage.isLast());
    }
}
