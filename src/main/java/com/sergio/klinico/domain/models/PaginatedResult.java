package com.sergio.klinico.domain.models;

import java.util.List;

public record PaginatedResult<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int currentPage,
        boolean isLast
) {}