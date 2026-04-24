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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<Employee> employees = employeeRepository.findByTenantId(tenantId);
        Map<String, Long> headcountByDept = employees.stream()
                .filter(e -> e.getDepartment() != null)
                .collect(Collectors.groupingBy(
                        e -> {
                            try { return e.getDepartment().getNombre(); }
                            catch (Exception ex) { return "Sin departamento"; }
                        },
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        // Attendance today
        long todayAttendance = attendanceRepository.countByTenantIdAndFecha(tenantId, LocalDate.now());
        BigDecimal attendanceRate = active > 0
                ? BigDecimal.valueOf(todayAttendance).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(active), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Payroll
        List<Payroll> payrolls = payrollRepository.findByTenantId(tenantId);
        BigDecimal totalPayroll = payrolls.stream()
                .filter(p -> p.getEstado() == Payroll.PayrollStatus.PAGADO)
                .map(Payroll::getSueldoBase)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgSalary = active > 0
                ? totalPayroll.divide(BigDecimal.valueOf(active), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Contracts
        long activeContracts = contractRepository.findByTenantIdAndEstado(tenantId, Contract.ContractStatus.ACTIVO).size();
        LocalDate in30Days = LocalDate.now().plusDays(30);
        long expiring = contractRepository.findByTenantIdAndEstado(tenantId, Contract.ContractStatus.ACTIVO).stream()
                .filter(c -> c.getFechaFin() != null && c.getFechaFin().isBefore(in30Days))
                .count();

        // Training
        long activeTrainings = trainingRepository.countByTenantIdAndEstado(tenantId, Training.TrainingStatus.EN_CURSO);
        long completedTrainings = trainingRepository.countByTenantIdAndEstado(tenantId, Training.TrainingStatus.COMPLETADO);
        List<Training> allTrainings = trainingRepository.findByTenantIdAndEstado(tenantId, Training.TrainingStatus.COMPLETADO);
        long trainingHours = allTrainings.stream()
                .mapToLong(t -> t.getDuracionHoras() != null ? t.getDuracionHoras() : 0)
                .sum();

        // Evaluations
        List<PerformanceEvaluation> evals = evaluationRepository.findByTenantId(tenantId);
        long pendingEvals = evals.stream()
                .filter(e -> e.getEstado() == PerformanceEvaluation.EvaluationStatus.BORRADOR).count();
        long completedEvals = evals.stream()
                .filter(e -> e.getEstado() == PerformanceEvaluation.EvaluationStatus.COMPLETADA ||
                             e.getEstado() == PerformanceEvaluation.EvaluationStatus.APROBADA).count();
        BigDecimal avgScore = evals.isEmpty() ? BigDecimal.ZERO
                : evals.stream().map(PerformanceEvaluation::getPuntaje)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(evals.size()), 1, RoundingMode.HALF_UP);

        // Vacations
        long pendingVacations = vacationRepository.countByTenantIdAndEstado(tenantId, VacationRequest.VacationStatus.SOLICITADO);

        // Goals
        List<Goal> goals = goalRepository.findByTenantId(tenantId);
        long activeGoals = goals.stream()
                .filter(g -> g.getEstado() == Goal.GoalStatus.EN_PROGRESO).count();
        long completedGoals = goals.stream()
                .filter(g -> g.getEstado() == Goal.GoalStatus.COMPLETADO).count();

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
