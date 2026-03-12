package com.sergio.klinico.infrastructure.persistence;

import com.sergio.klinico.domain.models.enums.PatientStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "patients")
@Getter
@Setter
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
public class PatientEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "patient_id", updatable = false, nullable = false)
    private UUID patientId;

    @Column(nullable = false)
    private String dni;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private char sex;

    @Column(nullable = false)
    private LocalDate birthdate;

    @Column
    private String address;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "relative_contact_number")
    private String relativeContactNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "patient_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PatientStatus status;
}
