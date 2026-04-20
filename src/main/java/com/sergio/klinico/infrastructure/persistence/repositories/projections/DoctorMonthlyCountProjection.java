package com.sergio.klinico.infrastructure.persistence.repositories.projections;

public interface DoctorMonthlyCountProjection {
    String getDoctorId();
    String getDoctorName();
    String getDoctorSurname();
    Integer getMonth();
    Long getCount();
}
