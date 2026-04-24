package com.microshop.rrhh.application.dto.analytics;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
public record HrAnalyticsDto(
    // Headcount
    long totalEmployees,
    long activeEmployees,
    long inactiveEmployees,
    Map<String, Long> headcountByDepartment,

    // Attendance
    long todayAttendance,
    BigDecimal attendanceRate,

    // Payroll
    BigDecimal totalPayrollCost,
    BigDecimal averageSalary,

    // Contracts
    long activeContracts,
    long expiringContracts30Days,

    // Training
    long activeTrainings,
    long completedTrainings,
    long totalTrainingHours,

    // Evaluations
    long pendingEvaluations,
    long completedEvaluations,
    BigDecimal averageScore,

    // Vacations
    long pendingVacationRequests,

    // Goals
    long activeGoals,
    long completedGoals
) {}
