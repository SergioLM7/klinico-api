package com.sergio.klinico.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "services")
public class ServiceEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "service_id", updatable = false, nullable = false)
    private UUID serviceId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "is_active")
    private boolean active;
}
