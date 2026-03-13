package com.sergio.klinico.infrastructure.rest.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PatientPageResponse {
    private List<PatientResponse> patients;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private boolean isLast;
}