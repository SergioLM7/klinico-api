package com.sergio.klinico.infrastructure.rest.dto.responses.kpi;

import java.util.List;
import java.util.UUID;

public record DoctorKpiSeriesResponse(
        UUID doctorId,
        String doctorName,
        String doctorSurname,
        List<MonthlyKpiEntryResponse> data
) {}
