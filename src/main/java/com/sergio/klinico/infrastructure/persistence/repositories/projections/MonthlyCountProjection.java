package com.sergio.klinico.infrastructure.persistence.repositories.projections;

public interface MonthlyCountProjection {
    Integer getMonth();
    Long getCount();
}
