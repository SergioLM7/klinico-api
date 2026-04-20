package com.sergio.klinico.infrastructure.persistence.repositories.projections;

public interface MonthlyAvgProjection {
    Integer getMonth();
    Double getAvgDays();
}
