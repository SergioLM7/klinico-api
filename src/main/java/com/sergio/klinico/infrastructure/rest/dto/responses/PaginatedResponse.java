package com.sergio.klinico.infrastructure.rest.dto.responses;

import com.sergio.klinico.domain.models.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private boolean isLast;

    public static <T> PaginatedResponse<T> create(List<T> data, PaginatedResult<?> result) {
        return PaginatedResponse.<T>builder()
                .data(data)
                .totalElements(result.totalElements())
                .totalPages(result.totalPages())
                .currentPage(result.currentPage())
                .isLast(result.isLast())
                .build();
    }
}