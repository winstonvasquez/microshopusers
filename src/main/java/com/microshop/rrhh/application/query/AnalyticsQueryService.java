package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.analytics.HrAnalyticsDto;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Contract;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.domain.model.Goal;
import com.microshop.rrhh.domain.model.Payroll;
import com.microshop.rrhh.domain.model.PerformanceEvaluation;
import com.microshop.rrhh.domain.model.Training;
import com.microshop.rrhh.domain.model.VacationRequest;
import com.microshop.rrhh.infrastructure.persistence.repository.*;
import com.microshop.users.shared.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnalyticsQueryService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;
    private final ContractRepository contractRepository;
    private final TrainingRepository trainingRepository;
    private final PerformanceEvaluationRepository evaluationRepository;
    private final VacationRequestRepository vacationRepository;
    private final GoalRepository goalRepository;
    private final DepartmentRepository departmentRepository;
    private final TenantContext tenantContext;

    public HrAnalyticsDto getDashboardAnalytics() {
        Long tenantId = tenantContext.getCurrentTenantId();

        // Headcount
        long total = employeeRepository.countByTenantId(tenantId);
        long active = employeeRepository.countByTenantIdAndEstado(tenantId, Employee.EmployeeStatus.ACTIVO);

        // Headcount by department
        Map<String, Long> headcountByDept = new LinkedHashMap<>();
        for (EmployeeRepository.DepartmentHeadcount row : employeeRepository.countByDepartment(tenantId)) {
            headcountByDept.put(row.getNombre(), row.getTotal());
        }

        // Attendance today
        long todayAttendance = attendanceRepository.countByTenantIdAndFecha(tenantId, LocalDate.now());
        BigDecimal attendanceRate = active > 0
                ? BigDecimal.valueOf(todayAttendance).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(active), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Payroll
        BigDecimal totalPayroll = payrollRepository.sumSueldoBaseByTenantIdAndEstado(tenantId, Payroll.PayrollStatus.PAGADO);
        BigDecimal avgSalary = active > 0
                ? totalPayroll.divide(BigDecimal.valueOf(active), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Contracts
        long activeContracts = contractRepository.countByTenantIdAndEstado(tenantId, Contract.ContractStatus.ACTIVO);
        LocalDate in30Days = LocalDate.now().plusDays(AppConstants.Negocio.DIAS_ALERTA_VENCIMIENTO_CONTRATO);
        long expiring = contractRepository.countExpiringBefore(tenantId, in30Days);

        // Training
        long activeTrainings = trainingRepository.countByTenantIdAndEstado(tenantId, Training.TrainingStatus.EN_CURSO);
        long completedTrainings = trainingRepository.countByTenantIdAndEstado(tenantId, Training.TrainingStatus.COMPLETADO);
        long trainingHours = trainingRepository.sumDuracionHorasByTenantIdAndEstado(tenantId, Training.TrainingStatus.COMPLETADO);

        // Evaluations
        long pendingEvals = evaluationRepository.countByTenantIdAndEstado(tenantId, PerformanceEvaluation.EvaluationStatus.BORRADOR);
        long completedEvals = evaluationRepository.countByTenantIdAndEstadoIn(tenantId,
                List.of(PerformanceEvaluation.EvaluationStatus.COMPLETADA, PerformanceEvaluation.EvaluationStatus.APROBADA));
        long totalEvals = evaluationRepository.countByTenantId(tenantId);
        BigDecimal avgScore = totalEvals == 0 ? BigDecimal.ZERO
                : evaluationRepository.sumPuntajeByTenantId(tenantId)
                    .divide(BigDecimal.valueOf(totalEvals), 1, RoundingMode.HALF_UP);

        // Vacations
        long pendingVacations = vacationRepository.countByTenantIdAndEstado(tenantId, VacationRequest.VacationStatus.SOLICITADO);

        // Goals
        long activeGoals = goalRepository.countByTenantIdAndEstado(tenantId, Goal.GoalStatus.EN_PROGRESO);
        long completedGoals = goalRepository.countByTenantIdAndEstado(tenantId, Goal.GoalStatus.COMPLETADO);

        return HrAnalyticsDto.builder()
                .totalEmployees(total)
                .activeEmployees(active)
                .inactiveEmployees(total - active)
                .headcountByDepartment(headcountByDept)
                .todayAttendance(todayAttendance)
                .attendanceRate(attendanceRate)
                .totalPayrollCost(totalPayroll)
                .averageSalary(avgSalary)
                .activeContracts(activeContracts)
                .expiringContracts30Days(expiring)
                .activeTrainings(activeTrainings)
                .completedTrainings(completedTrainings)
                .totalTrainingHours(trainingHours)
                .pendingEvaluations(pendingEvals)
                .completedEvaluations(completedEvals)
                .averageScore(avgScore)
                .pendingVacationRequests(pendingVacations)
                .activeGoals(activeGoals)
                .completedGoals(completedGoals)
                .build();
    }
}
