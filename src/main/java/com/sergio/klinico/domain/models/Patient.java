package com.sergio.klinico.domain.models;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.enums.PatientStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    private UUID patientId;
    private String dni;
    private String name;
    private String surname;
    private Character sex;
    private LocalDate birthdate;
    private String address;
    private String contactNumber;
    private String relativeContactNumber;
    @Setter(AccessLevel.NONE)
    private PatientStatus status;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime lastModifiedAt;
    private UUID lastModifiedBy;

    public void applyStatusChange(PatientStatus newStatus) {
        if (newStatus == null) {
            throw new BusinessException("El estado del paciente no puede ser nulo");
        } else if(status == PatientStatus.EXITUS) {
            throw new BusinessException("No se puede cambiar el estado de un paciente que ya está exitus");
        } else if (status == PatientStatus.ALTA && newStatus == PatientStatus.EXITUS) {
            throw new BusinessException("No se puede cambiar directamente el estado de un paciente de alta a exitus");
        }
        status = newStatus;
    }

    public void updateFields(Patient updatedData) {
        if (updatedData.getName() != null) this.setName(updatedData.getName());
        if (updatedData.getSurname() != null) this.setSurname(updatedData.getSurname());
        if (updatedData.getAddress() != null) this.setAddress(updatedData.getAddress());
        if (updatedData.getContactNumber() != null) this.setContactNumber(updatedData.getContactNumber());
        if (updatedData.getRelativeContactNumber() != null) this.setRelativeContactNumber(updatedData.getRelativeContactNumber());
    }
}
