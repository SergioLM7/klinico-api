package com.sergio.klinico.infrastructure.persistence.repositories.projections;

public interface UserWorkloadProjection {
    String getName();
    String getSurname();
    Long getAdmissionsAssigned();
}
