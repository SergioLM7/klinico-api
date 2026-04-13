package com.sergio.klinico.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalService {
    private UUID serviceId;
    private String name;
    private boolean active;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime lastModifiedAt;
    private UUID lastModifiedBy;
}
