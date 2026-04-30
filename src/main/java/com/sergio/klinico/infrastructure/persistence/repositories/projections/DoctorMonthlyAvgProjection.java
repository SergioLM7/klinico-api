package com.sergio.klinico.infrastructure.persistence.repositories.projections;

public interface DoctorMonthlyAvgProjection {
    String getDoctorId();
    String getDoctorName();
    String getDoctorSurname();
    Integer getMonth();
    Double getAvgDays();
}
