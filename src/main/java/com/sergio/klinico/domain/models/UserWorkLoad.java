package com.sergio.klinico.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWorkLoad {
    private String name;
    private String surname;
    private long admissionsAssigned;
}
