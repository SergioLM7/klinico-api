package com.sergio.klinico.domain.models;

import java.util.List;
import java.util.UUID;

public record DoctorKpiSeries(UUID doctorId, String doctorName, String doctorSurname, List<MonthlyKpiEntry> data) {}
